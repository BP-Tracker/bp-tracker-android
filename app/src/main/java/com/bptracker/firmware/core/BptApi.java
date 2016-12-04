package com.bptracker.firmware.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.data.BptContract.DeviceFunctionCallEntry;
import com.bptracker.firmware.Firmware.Function;
import com.bptracker.firmware.FirmwareException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */

public abstract class BptApi {

    public static final int ARG_LATITUDE = 10; //TODO: should these be in an enum?
    public static final int ARG_LONGITUDE = 20;
    public static final int ARG_STATE = 30;
    public static final int ARG_EVENT_TYPE = 40;
    public static final int ARG_PROPERTY = 50;
    public static final int ARG_TEST_INPUT = 60;
    public static final int ARG_STRING_DATA = 70;
    public static final int ARG_DATE_DATA = 71;
    public static final int ARG_BOOLEAN_DATA = 72;
    public static final int ARG_SOFTWARE_RESET = 80;
    public static final int ARG_PROPERTY_RESET = 81;


    public static int RESULT_SOURCE_FUNCTION = 1;
    public static int RESULT_SOURCE_EVENT = 2;

    private static final int FUNCTION_ARG_CAPACITY = 10;
    private static final int RECEIVER_QUEUE_CAPACITY = 10; // maximum active functions

    private String mCloudDeviceId;
    private String mFunctionName;
    private Uri mUri;

    private WeakReference<ResultCallback> mResultCallback;
    private boolean mCallInProgress;
    private Context mContext;
    private String[] mFunctionArgs;
    private int mLargestFunctionPos;
    private EventReceiver mEventReceiver;


    // Class specific - for subscribing to particle.io events
    private static final Object mLock;
    private static long mCloudSubscriptionId;
    private static final BlockingQueue<EventReceiver> mEventReceiverQueue;

    static {
        mEventReceiverQueue = new LinkedBlockingQueue<>(RECEIVER_QUEUE_CAPACITY);
        mLock = new Object();
    }

    interface EventReceiver {

        /**
         * Receives incoming private device events during an active function call. This allows
         * implementors a chance to listen for an event that is known to be returned from the call.
         * Once the event is received this method must return true to indicate it's
         * finished. The class will then automatically unregister the receiver.
         * Return false when the event is not the right one and before returning true invoke
         * completeCall.
         *
         * Notes:
         *
         * 1 - The order of the events arrived may interleave with other devices. Be sure
         *      to compare the deviceId in the payload or invoke isMyDevice.
         *
         * 2 - The function call may timeout and no events may arrive
         *
         * 3 - Due to the nature of the function and event publish model, the event received
         *      may not necessarily originate from the particular function call. (This is because
         *      events are not tied to a function).
         *
         * 4 - Don't register a receiver if the function does not return an event and
         *      always register otherwise.
         *
         * @param name  The event name
         * @param event The event payload
         * @return  Return true when the particular event has arrived, false if it has not.
         */
        public boolean receive(String name, ParticleEvent event);
    }


    public interface ResultCallback {
        /**
         * Returns the result of the function call. Some functions return results in a
         * particle.io event with the same name. Hence, this method will be called twice.
         * @param function  The BptApi instance
         * @param source    The result source: either RESULT_SOURCE_FUNCTION or RESULT_SOURCE_EVENT.
         * @param result    The function or event result
         */
        public void onFunctionResult(BptApi function, int source, String result);

        /**
         * Returns when the request could not be completed on the firmware.
         * @param function this
         * @param reason the error reason
         */
        public void onFunctionError(BptApi function, String reason);

        /**
         * Called when the call has timed out.
         * @param function this
         * @param source  the source of the timeout
         */
        public void onFunctionTimeout(BptApi function, int source);
    }


    public BptApi(Context context, String cloudDeviceId, String functionName) {
        init(context, cloudDeviceId, functionName);
    }

    public BptApi(Context context, String cloudDeviceId, String functionName, String[] functionArgs){
        init(context, cloudDeviceId, functionName);
        mFunctionArgs = functionArgs;
        mLargestFunctionPos = functionArgs.length - 1;
    }

    private void init(Context context, String cloudDeviceId, String functionName){ //TODO: complete
        mContext = context;
        mCloudDeviceId = cloudDeviceId;
        mFunctionName = functionName;
        mCallInProgress = false;
        mResultCallback = null;
        mEventReceiver = null;
        mFunctionArgs = new String[FUNCTION_ARG_CAPACITY];
        mLargestFunctionPos = -1;
    }


    /**
     * Abstract method to add arguments to the function. Internally, use addArgumentAtPos
     * to add the arguments. Can throw an IllegalArgumentException
     * @param argumentId    An ID the concrete class understands
     * @param arg           The argument
     */
    public abstract void addArgument(int argumentId, String arg);


    public void addArgument(int argumentId, long arg) {
        this.addArgument(argumentId, Long.toString(arg));
    }

    public void addArgument(int argumentId, int arg) {
        this.addArgument(argumentId, Integer.toString(arg));
    }

    public void addArgument(int argumentId, float arg) {
        this.addArgument(argumentId, Float.toString(arg));
    }

    public void addArgument(int argumentId, Object arg) {
        this.addArgument(argumentId, arg.toString());
    }

    /**
     * Perform a final validation of all arguments just before making the call.
     * @param args  A copy of the argument list that the function will be called with.
     *              Invoke addArgumentAtPos to add/modify the arguments.
     * @throws IllegalArgumentException when the arguments are not valid
     */
    protected abstract void validateArgsForCall(String[] args);


    /**
     * Adds or replaces the argument value at the specified position starting
     * at position 1. Note: only basic validation occurs here.
     * @param pos The position to place the argument at
     * @param arg The argument value
     */
     protected void addArgumentAtPos(int pos, String arg) {

        if(pos <= 0 ){
            throw new IllegalArgumentException("position must be greater than 0");
        }

         if(pos > FUNCTION_ARG_CAPACITY ){
             throw new IllegalArgumentException("a maximum of " + FUNCTION_ARG_CAPACITY
                     + " arguments are supported");
         }

         mFunctionArgs[pos - 1] = arg;

         if(mLargestFunctionPos < pos - 1) {
             mLargestFunctionPos = pos - 1;
         }
    }

    /**
     * Call this method during initialization or validation when the function is expected to
     * return data in a particle event. The receiver will be enabled once the function is called
     * and disabled and unregistered when its receive method returns true;
     *
     *
     * @param receiver  The event receiver. When this is null the class will create a
     *                  local receiver and listen for the first event that matches the function name.
     *                  Call completeCall and return true once the target event was found.
     */
    protected void registerEventReceiver(@Nullable EventReceiver receiver){
        _log.d("registerEventReceiver called");
        if (mEventReceiver != null) {
            throw new IllegalArgumentException("A receiver has already been registered");
        }

        mEventReceiver = receiver == null ? new FunctionNameEventReceiver() : receiver;
    }

    /**
     * Registers a default event receiver
     */
    protected void registerEventReceiver(){
        registerEventReceiver(null);
    }

    protected void unregisterEventReceiver(){
        if(mEventReceiver != null){
            mEventReceiverQueue.remove(mEventReceiver);
            mEventReceiver = null;
        }
    }

    /**
     * Is the event coming from this device.
     * @param event The event
     * @return  True if the event came from this device
     */
    protected boolean isMyDevice(ParticleEvent event){
        return this.getDeviceId().equals(event.deviceId);
    }

    /**
     * Adds a result listener for the function
     * @param callback A ResultCallback listener
     */
    public void setResultCallback(ResultCallback callback) {
        mResultCallback = new WeakReference<ResultCallback>(callback);
    }

    /**
     * Cancels any pending calls and resets the object to the pre-call state
     *
     * @param clearArguments Set to true at also clear the arguments
     */
    public void reset(boolean clearArguments){

        if (mResultCallback != null) {
            mResultCallback.clear();
            mResultCallback = null;
        }

        unregisterEventReceiver();

        mCallInProgress = false;

        if(clearArguments) {
            mFunctionArgs = new String[FUNCTION_ARG_CAPACITY];
            mLargestFunctionPos = -1;
        }
    }

    public String getDeviceId(){
        return mCloudDeviceId;
    }

    @Nullable
    public Uri getCallUri(){
        return mUri;
    }

    private Uri createNewFunctionUri(String deviceId, String funcName, String funcArgs){

        ContentValues v = new ContentValues();

        v.put(DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID, deviceId);
        v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_NAME, funcName);
        v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_ARGS, funcArgs);
        v.put(DeviceFunctionCallEntry.COLUMN_PUBLISH_DATE, new Date().getTime());


        Uri uri = mContext.getContentResolver().insert(DeviceFunctionCallEntry.CONTENT_URI, v);

        return uri;
    }

    private void updateFunctionUri(Uri uri, int source, int resultOrEventId, String extra) {

        if(source != RESULT_SOURCE_FUNCTION && source != RESULT_SOURCE_EVENT){
            throw new RuntimeException("source unknown" + source);
        }

        if(mResultCallback != null){

            ResultCallback cb = mResultCallback.get();

            if(cb != null){
                cb.onFunctionResult(this, BptApi.RESULT_SOURCE_FUNCTION,
                        source == RESULT_SOURCE_FUNCTION
                                ? Integer.toString(resultOrEventId) : extra);
            }
        }

        ContentValues v = new ContentValues();
        if(source == RESULT_SOURCE_FUNCTION){
            v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_RETURN, resultOrEventId);
        }else{

            if(resultOrEventId > 0) {
                v.put(DeviceFunctionCallEntry.COLUMN_EVENT_ID, resultOrEventId);
            }

            if (extra != null) {
                v.put(DeviceFunctionCallEntry.COLUMN_EVENT_DATA, extra);
            }
        }

        mContext.getContentResolver().update(uri, v, null, null);
    }

    private void subscribeToDeviceEvents(final ParticleCloud cloud) throws IOException {
        synchronized (mLock) {

            if(mCloudSubscriptionId <= 0 && mEventReceiverQueue.size() > 0){

                // only one ParticleEventHandler should be assigned for the class
                mCloudSubscriptionId = cloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                    @Override
                    public void onEvent(String eventName, ParticleEvent particleEvent) {
                        _log.d("onEvent received [thread=" + Thread.currentThread().getName()
                                + "][eventName=" + eventName + "][receiverQueue=" +
                                mEventReceiverQueue.size() + "]");

                        EventReceiver receiver = mEventReceiverQueue.peek();

                        if(receiver != null){ //could have been unregistered
                            boolean foundEvent = receiver.receive(eventName, particleEvent);
                            if (foundEvent) {
                                //_log.d("!event found !" + eventName + " [data=" + particleEvent.dataPayload + "]");

                                //don't take because receiver calls callComplete which calls reset
                                mEventReceiverQueue.remove(receiver);
                            }
                        }

                        if(mEventReceiverQueue.size() <= 0){
                            unsubscribeToDeviceEvents(cloud);
                        }
                    }

                    @Override
                    public void onEventError(Exception e) {
                        _log.d("onEventError called!!! + " + e.getMessage());

                    }
                });

                _log.d("subscribed to events [subscriptionId=" + mCloudSubscriptionId + "]");
            }
        }
    }

    private static void unsubscribeToDeviceEvents(ParticleCloud cloud)  {
        synchronized (mLock){

            if (mCloudSubscriptionId >= 0) {
                _log.d("unsubscribing to device events [subscriptionId=" + mCloudSubscriptionId + "]");

                try {
                    cloud.unsubscribeFromEventWithID(mCloudSubscriptionId);
                } catch (ParticleCloudException e) {
                    _log.w("could not unsubscribe from events: " + e.getBestMessage());
                }

                mCloudSubscriptionId = -1;
            }
        }
    }

    /**
     * Calls the ResultCallback function and completes the call. Subclasses
     * should call this in the EventReceiver.receive method.
     *
     * @param eventData The data to put into DeviceFunctionCallEntry.COLUMN_EVENT_DATA
     * @param event   The event payload to pass into DeviceFunctionCallEntry.COLUMN_EVENT_ID TODO: support this
     */
    void completeCall(@Nullable String eventData, @Nullable ParticleEvent event){
        _log.i("completing function call for " + mUri);

        updateFunctionUri(mUri, RESULT_SOURCE_EVENT, 0, eventData);


        ResultCallback cb = mResultCallback.get();
        if (cb != null) {
            cb.onFunctionResult(this, RESULT_SOURCE_EVENT, eventData );
        }

        mCallInProgress = false;
        reset(false);
    }

    private void doRegisterEventReceiver(){
        if(mEventReceiver == null){
            throw new RuntimeException("Event receiver is null");
        }

        mEventReceiverQueue.add(mEventReceiver);
    }


    private String[] resizeFunctionArgs(){
        String[] resized = new String[mLargestFunctionPos + 1];
        for(int i = 0; i <= mLargestFunctionPos; i++) {
            resized[i] = mFunctionArgs[i];
        }

        return resized;
    }

    /**
     * Performs a validation of all the arguments and invokes the function on the particle
     * cloud.
     * @return  A uri of the function call of the form: content://com.bptracker/function-calls/[id]
     * @throws FirmwareException When no ResultCallback listener is attached and an error occurred
     * during setup.
     * @return  The URI of the function call or null if an error occurred
     */
    public @Nullable Uri call() {

        _log.v("calling function " + mFunctionName + " with " + (mLargestFunctionPos + 1) + " arguments");

        if (mCallInProgress == true) {
            throw new FirmwareException("A call is already in progress");
        }

        mCallInProgress = true;

        try {
            validateArgsForCall(resizeFunctionArgs());
        } catch (IllegalArgumentException e) {
            mCallInProgress = false;

            if (mResultCallback != null) {


                ResultCallback cb = mResultCallback.get();
                if (cb != null) {
                    cb.onFunctionError(this, e.getMessage());
                }

                reset(false); //TODO: reset here?
                return null;

            }else{
                reset(false);
                throw new FirmwareException(e);
            }
        }

        if(mEventReceiver != null){
            doRegisterEventReceiver();
        }

        mFunctionArgs = resizeFunctionArgs();

        final String funcArgs;

        if(mFunctionArgs.length > 0) {
            StringBuffer buf = new StringBuffer(mFunctionArgs.length);
            for (int i = 0; i < mLargestFunctionPos; i++) {

                String a = TextUtils.isEmpty(mFunctionArgs[i]) ? "" : mFunctionArgs[i];
                buf.append(a);
                buf.append(",");
            }
            buf.append(mFunctionArgs[mLargestFunctionPos]);
            funcArgs = buf.toString();
        } else {
            funcArgs = "";
        }

        mUri = createNewFunctionUri(mCloudDeviceId, mFunctionName, funcArgs);

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Void>(){

           @Override
           public Void callApi(ParticleCloud cloud) throws ParticleCloudException, IOException {
               ParticleDevice device = cloud.getDevice(mCloudDeviceId);

               List<String> a = new ArrayList<>(1);
               a.add( funcArgs );

               subscribeToDeviceEvents(cloud);

               try {
                   int result = device.callFunction(mFunctionName, a);
                   updateFunctionUri(mUri, RESULT_SOURCE_FUNCTION, result, null);

                   if (mEventReceiver == null) { // function completed
                       mCallInProgress = false;
                       reset(false);
                   }

               } catch (ParticleDevice.FunctionDoesNotExistException e) {
                   throw new ParticleCloudException(e);
               }

                return null;
           }

           @Override
           public void onSuccess(Void aVoid) { }

           @Override
           public void onFailure(ParticleCloudException e) {
               _log.e("call onFailure" + e.getBestMessage());

               if (mResultCallback != null) { // TODO: test this
                   ResultCallback r = mResultCallback.get();
                   if (r != null) {
                       r.onFunctionError(BptApi.this, e.getMessage());
                   }
               }else{
                   e.printStackTrace();
               }
           }
        });

        _log.v("returning uri " + mUri);
        return mUri;
    }

    public static BptApi createInstance(Context context, Function function, String cloudDeviceId,
                                        ResultCallback callback){

        BptApi f;

        switch (function){
            case BPT_GPS:
                f = new GpsFunction(context, cloudDeviceId);
                break;
            case BPT_STATE:
                f = new StateFunction(context, cloudDeviceId);
                break;
            case BPT_DIAG:
                f = new DiagnosticFunction(context, cloudDeviceId);
                break;
            case BPT_PROBE:
                f = new ProbeFunction(context, cloudDeviceId);
                break;
            case BPT_STATUS:
                f = new StatusFunction(context, cloudDeviceId);
                break;
            case BPT_TEST:
                f = new TestFunction(context, cloudDeviceId);
                break;
            case BPT_ACK:
                f = new AckFunction(context, cloudDeviceId);
                break;
            case BPT_REGISTER:
                f = new RegisterFunction(context, cloudDeviceId);
                break;
            case BPT_RESET:
                f = new ResetFunction(context, cloudDeviceId);
                break;
            default:
                throw new RuntimeException("Cannot create instance for function "
                        + function.getName() + "- not supported");
        }

        f.setResultCallback(callback);
        return f;
    }

    private class FunctionNameEventReceiver implements EventReceiver {

        @Override
        public boolean receive(String name, ParticleEvent event) {

            if(isMyDevice(event) && name.equals(mFunctionName)){

                completeCall(event.dataPayload, event);
                return true;
            }

            return false;
        }
    }

    private static final TLog _log = TLog.get(BptApi.class);
}