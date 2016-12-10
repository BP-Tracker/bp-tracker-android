package com.bptracker.firmware.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.bptracker.data.BptContract.DeviceFunctionCallEntry;
import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.FirmwareException;
import com.bptracker.util.IntentUtil;

import java.io.IOException;
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
import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */
public class BptApi {

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

    private static final int RECEIVER_QUEUE_CAPACITY = 10; // maximum active functions

    private static HandlerThread mHandlerThread; // lazy loaded worker thread for API calls
    private static BptApi mBptApiInstance;
    private static Context mContext;
    private static final Object mLock;
    private static long mCloudSubscriptionId;
    private static final BlockingQueue<FunctionResultReceiver> mEventReceiverQueue;

    static {
        mEventReceiverQueue = new LinkedBlockingQueue<>(RECEIVER_QUEUE_CAPACITY);
        mLock = new Object();
    }


    public interface ResultCallback {
        /**
         * Returns the result of the function call. Some functions return results in a
         * particle.io event with the same name. Hence, this method will be called twice.
         * @param function  The Function instance
         * @param result    The result
         * @param eventResult    The function or event result
         */
        public void onFunctionResult(Function function, int result, @Nullable String eventResult);

        /**
         * Returns when the request could not be completed on the firmware.
         * @param function this
         * @param reason the error reason
         */
        public void onFunctionError(Function function, String reason);

        /**
         * Called when the call has timed out.
         * @param function this
         * @param source  the source of the timeout
         */
        public void onFunctionTimeout(Function function, int source);
    }

    interface FunctionResultReceiver {

        /**
         * Receives function results and incoming device events during a function call. This allows
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
         * @return  Return a non-null string when the particular event has arrived.
         */
        public @Nullable String receiveEvent(String name, ParticleEvent event);

        public void receiveResult(int result);

        public boolean doReceiveEvents();

        public Uri getUri();

        public void setUri(Uri uri);

    }

    public static BptApi getInstance() {

        if (mBptApiInstance == null) {
            throw new IllegalStateException("init not called before using this class. "
                    + "Are you calling BptApi.init() in your Application.onCreate()?");
        }
        return mBptApiInstance;
    }

    private Context getContext(){
        return mContext;
    }

    private static Looper getLooper(){
        synchronized (BptApi.class){
            if(mHandlerThread == null){

                mHandlerThread = new HandlerThread("BptApi Worker Thread");
                mHandlerThread.start();
            }else if(!mHandlerThread.isAlive()){
                _log.w("BptApi Worker Thread finished, recreating");

                mHandlerThread = new HandlerThread("BptApi Worker Thread");
                mHandlerThread.start();
            }
        }
        return mHandlerThread.getLooper();
    }

    /**
     * Initialize the cloud SDK.  Must be called somewhere in your Application.onCreate()
     *
     * (or anywhere else before your first Activity.onCreate() is called)
     */
    public static void init(Context ctx){

        if (mBptApiInstance != null) {
            _log.w("Calling BptApi.init() more than once does not re-initialize the class.");
            return;
        }

        Context appContext = ctx.getApplicationContext();
        mBptApiInstance = new BptApi(appContext);
    }


    /**
     * Performs a validation of all the arguments and invokes the function on the particle
     * cloud. See also RunFunctionService for a broadcast based implementation.
     *
     * @return  A uri of the function call of the form: content://com.bptracker/function-calls/[id]
     * @throws FirmwareException When no ResultCallback listener is attached and an error occurred
     * during setup.
     * @return  The URI of the function call or null if an error occurred
     */
    @Nullable
    public static Uri call(final Function function, final ResultCallback callback){
        BptApi api = BptApi.getInstance();

        return api.callFunction(function, callback);
    }


    public Uri callFunction(Function function, ResultCallback callback) {
        _log.v("calling function " + function + " [thread=" + Thread.currentThread().getName() + "]");

        Uri uri = createNewFunctionUri(function, true);
        Handler functionHandler = new FunctionHandler(BptApi.getLooper());

        Message m = Message.obtain(functionHandler, FunctionHandler.MSG_RUN_FUNCTION, callback);
        m.getData().putParcelable(IntentUtil.EXTRA_FUNCTION, function);
        m.sendToTarget();

        _log.v("returning uri " + uri);
        return uri;
    }




    // runs the function
    private static class FunctionHandler extends Handler {
        public static final int MSG_RUN_FUNCTION = 1;
        public static final int MSG_QUIT_HANDLER = 2;

        public FunctionHandler(Looper looper){
            super(looper);
        }

        public void handleMessage(Message msg) {
            _log.d("handleMessage " + msg.what + " in " + Thread.currentThread());

            switch (msg.what){

                case MSG_QUIT_HANDLER:
                    this.getLooper().quit(); //TODO: is this enough?

                case MSG_RUN_FUNCTION:

                    Function f = msg.getData().getParcelable(IntentUtil.EXTRA_FUNCTION);
                    FunctionObserver observer = new FunctionObserver(this, f, (ResultCallback) msg.obj );

                    try {

                        List<String> args = new ArrayList<>(1);
                        args.add( f.getArgs() );

                        ParticleCloud cloud = ParticleCloudSDK.getCloud();
                        ParticleDevice device = cloud.getDevice(f.getDeviceId());

                        if(f.doReceiveEvents()){
                            mEventReceiverQueue.add(f);
                            BptApi.subscribeToDeviceEvents(cloud);
                            mContext.getContentResolver().registerContentObserver(f.getUri(), false, observer);
                        }


                        int result = device.callFunction(f.getName(), args);
                        f.receiveResult(result);
                        observer.updateOrSendResult(result);

                    } catch (ParticleDevice.FunctionDoesNotExistException
                                        | ParticleCloudException| IOException e ) {
                        _log.e("particle exception - " + e.getMessage());

                        observer.sendErrorToCallback( e.getMessage() );
                        observer.unRegister(true); //quits the looper thread as well
                    }

                    break;

                default:
                    super.handleMessage(msg);
            }
        };
    }

    private static class FunctionObserver extends ContentObserver {

        private static final String[] COLUMNS = { DeviceFunctionCallEntry.COLUMN_EVENT_DATA };
        public static final int COL_EVENT_DATA = 0;

        private Function mFunction;
        private ResultCallback mCallback;
        private int mFunctionResult;
        private Handler mHandler;


        public FunctionObserver(Handler h, Function f, ResultCallback r){
            super(h);
            mHandler = h;
            mFunction = f;
            mCallback = r;
            mFunctionResult = -1; // Error prone
        }

        // NB: Will only be called when a function event is expected
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            _log.v("onChange called for " + uri);

            if(!uri.equals(mFunction.getUri())){
                _log.e("The expected URI differs with the function's URI: " +
                        uri.toString() + " vs " + mFunction.getUri().toString() );

                unRegister(true);
                return;
            }

            String result = null;
            Context context = BptApi.getInstance().getContext();
            Cursor c = context.getContentResolver().query(mFunction.getUri(),
                    COLUMNS, null, null, null);

            if(c.moveToFirst()){
                result = c.getString(COL_EVENT_DATA);
            }
            c.close();

            mCallback.onFunctionResult(mFunction, mFunctionResult, result);

            unRegister(true);
        }

        /**
         * The invocation of the callback will be deferred until the event arrives if
         * the function expects it (doReceiveEvents() is true)
         * @param result    The function result code
         */
        public void updateOrSendResult(int result){
            _log.v("updateAndSendResult called " + result + " [receiveEvents=" +
                            mFunction.doReceiveEvents() + "]");

            if(!mFunction.doReceiveEvents()){
                unRegister(true);

                ContentValues v = new ContentValues();
                v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_RETURN, result);
                mContext.getContentResolver().update(mFunction.getUri(), v, null, null);

                mCallback.onFunctionResult(mFunction, result, null);

            }else{
                mFunctionResult = result;  // save result until the event comes in, or a timeout happens: TODO
            }
        }

        public void sendErrorToCallback(String error) {
            mCallback.onFunctionError(mFunction, error);
        }

        public void unRegister(boolean quitLooper){
            mContext.getContentResolver().unregisterContentObserver(this);

            if(quitLooper){
                mHandler.getLooper().quitSafely();
            }
        }
    }

    private BptApi(Context context) {
        mContext = context;
    }

    private Uri createNewFunctionUri(Function function, boolean setUriInFunction){
        ContentValues v = new ContentValues();

        v.put(DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID, function.getDeviceId());
        v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_NAME, function.getName());
        v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_ARGS, function.getArgs());
        v.put(DeviceFunctionCallEntry.COLUMN_PUBLISH_DATE, new Date().getTime());

        Uri uri = mContext.getContentResolver().insert(DeviceFunctionCallEntry.CONTENT_URI, v);

        if(setUriInFunction) {
            function.setUri(uri);
        }

        return uri;
    }

    private static void updateFunctionUri(Uri uri, ParticleEvent event, String eventResult) {
        _log.v("updateFunctionUri called");

        //TODO: tie in the event_id somehow (right now it's null)
        ContentValues v = new ContentValues();
        v.put(DeviceFunctionCallEntry.COLUMN_EVENT_DATA, eventResult);

        mContext.getContentResolver().update(uri, v, null, null);
    }

    private static void subscribeToDeviceEvents(final ParticleCloud cloud) throws IOException {
        synchronized (mLock) {

            if(mCloudSubscriptionId <= 0 && mEventReceiverQueue.size() > 0){

                // only one ParticleEventHandler should be assigned for the class
                mCloudSubscriptionId = cloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                    @Override
                    public void onEvent(String eventName, ParticleEvent particleEvent) {
                        _log.d("onEvent received [thread=" + Thread.currentThread().getName()
                                + "][eventName=" + eventName + "][receiverQueue=" +
                                mEventReceiverQueue.size() + "]");

                        FunctionResultReceiver receiver = mEventReceiverQueue.peek();

                        _log.d("receiver = " + receiver);

                        if(receiver != null){ //could have been unregistered
                            String foundEvent = receiver.receiveEvent(eventName, particleEvent);
                            if (foundEvent != null) {
                                _log.v("!event found !" + eventName + " [data=" + foundEvent + "]");

                                updateFunctionUri(receiver.getUri(), particleEvent, foundEvent);

                                //TODO: change to take?
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
     * Creates an instance of a BPT function.
     *
     * See respective documentation for adding arguments
     *
     * <p>Function.BPT_ACK {@link AckFunction}
     * <p>Function.BPT_STATE {@link StateFunction}
     * <p>Function.BPT_GPS {@link GpsFunction}
     * <p>Function.BPT_STATUS {@link StatusFunction}
     * <p>Function.BPT_RESEST {@link ResetFunction}
     * <p>Function.BPT_REGISTER {@link RegisterFunction}
     * <p>Function.BPT_PROBE {@link ProbeFunction}
     * <p>Function.BPT_TEST {@link TestFunction}
     *
     * @param function
     * @param cloudDeviceId
     * @return
     */
    public static Function createFunction(Firmware.Function function, String cloudDeviceId){
        Function f;

        switch (function){
            case BPT_GPS:
                f = new GpsFunction(cloudDeviceId);
                break;
            case BPT_STATE:
                f = new StateFunction(cloudDeviceId);
                break;
            case BPT_DIAG:
                f = new DiagnosticFunction(cloudDeviceId);
                break;
            case BPT_PROBE:
                f = new ProbeFunction(cloudDeviceId);
                break;
            case BPT_STATUS:
                f = new StatusFunction(cloudDeviceId);
                break;
            case BPT_TEST:
                f = new TestFunction(cloudDeviceId);
                break;
            case BPT_ACK:
                f = new AckFunction(cloudDeviceId);
                break;
            case BPT_REGISTER:
                f = new RegisterFunction(cloudDeviceId);
                break;
            case BPT_RESET:
                f = new ResetFunction(cloudDeviceId);
                break;
            default:
                throw new RuntimeException("Cannot create instance for function "
                        + function.getName() + "- not supported");
        }

        return f;
    }

    private static final TLog _log = TLog.get(BptApi.class);
}



























      /*
        if(funcArgs.length > 0) {
            StringBuffer buf = new StringBuffer(funcArgs.length);
            for (int i = 0; i < funcArgs.length - 1; i++) {

                String a = TextUtils.isEmpty(funcArgs[i]) ? "" : funcArgs[i];
                buf.append(a);
                buf.append(",");
            }
            buf.append(funcArgs[funcArgs.length - 1]);
            funcArgsStr = buf.toString();
        } else {
            funcArgsStr = "";
        }
        */


// listens for data updates on the URI and invokes the callbacks
// new Handler(Looper.getMainLooper())

//Handler h = new Handler();
//
//        Handler h = new Handler(ht.getLooper()) {
//
//
//
//
//
//            public void handleMessage(Message msg) {
//                _log.d("handleMessage " + msg.what + " in " + Thread.currentThread());
//            };
//
//
//
//        };



// final FunctionObserver observer = new FunctionObserver(this.getContext(), h, function, callback);


//this.getContext().getContentResolver().registerContentObserver(uri, false, observer);

/*
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Void>(){

            @Override
            public Void callApi(ParticleCloud cloud) throws ParticleCloudException, IOException {
                ParticleDevice device = cloud.getDevice(deviceId);

                List<String> a = new ArrayList<>(1);
                a.add( funcArgsStr );

                subscribeToDeviceEvents(cloud);

                try {
                    int result = device.callFunction(funcName, a);

                    Message m = Message.obtain(functionHandler);
                    m.what = FunctionHandler.MSG_SEND_FUNCTION_RESULT;
                    m.arg1 = result;

                    m.sendToTarget();

                    //observer.updateAndSendResult(result);

                    //updateFunctionUri(uri, RESULT_SOURCE_FUNCTION, result, null);
                    //callback.onFunctionResult(function, RESULT_SOURCE_FUNCTION, Integer.toString(result));

                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    _log.w("call - " + e.getMessage());


                    Message m = Message.obtain(functionHandler);
                    m.what = FunctionHandler.MSG_SEND_FUNCTION_ERROR;
                    m.getData().putString(FunctionHandler.MSG_DATA_ERROR, e.getMessage());

                    m.sendToTarget();


                    //observer.sendErrorToCallback(e.getMessage());

                    //callback.onFunctionError(function, e.getMessage());

                }

                return null;
            }

            @Override
            public void onSuccess(Void aVoid) { }

            @Override
            public void onFailure(ParticleCloudException e) {
                _log.e("onFailure called - " + e.getBestMessage());

                Message m = Message.obtain(functionHandler);
                m.what = FunctionHandler.MSG_SEND_FUNCTION_ERROR;
                m.getData().putString(FunctionHandler.MSG_DATA_ERROR, e.getMessage());

                m.sendToTarget();

                //observer.sendErrorToCallback(e.getMessage());
            }
        });

        */





//
//    /**
//     * Retrieves data from the function content provider and invokes the callback with the result.
//     */
//    private static class FunctionObserver2 extends ContentObserver {
//
//        private Function mFunction;
//        private Context mContext;
//        private ResultCallback mCallback;
//        private int mFunctionResult;
//
//        private static final String[] COLUMNS = { DeviceFunctionCallEntry.COLUMN_EVENT_DATA };
//        public static final int COL_EVENT_DATA = 0;
//
//        public FunctionObserver2(Context context, Handler handler, Function function, ResultCallback callback){
//            super(handler);
//
//            mFunction = function;
//            mContext = context;
//            mCallback = callback;
//            mFunctionResult = 0;
//        }
//
//        /**
//         * The invocation of the callback will be deferred until the event arrives if
//         * the function expects it (doReceiveEvents() is true)
//         * @param result    The function result code
//         */
//        public void updateAndSendResult(int result){
//            _log.v("updateAndSendResult called " + result + " [receiveEvents=" +
//                    mFunction.doReceiveEvents() + "]");
//
//            if(!mFunction.doReceiveEvents()){
//                unRegister();
//
//                ContentValues v = new ContentValues();
//                v.put(DeviceFunctionCallEntry.COLUMN_FUNCTION_RETURN, result);
//                mContext.getContentResolver().update(mFunction.getUri(), v, null, null);
//
//                mCallback.onFunctionResult(mFunction, result, null);
//
//            }else{
//                // save result until the event comes in, or a timeout happens: TODO
//                mFunctionResult = result;
//            }
//        }
//
//        public void sendErrorToCallback(String error) {
//            mCallback.onFunctionError(mFunction, error);
//            unRegister();
//        }
//
//
//        // Will be called when an function event is expected
//        @Override
//        public void onChange(boolean selfChange, Uri uri) {
//            _log.v("onChange called for " + uri);
//
//            if(!uri.equals(mFunction.getUri())){
//                _log.e("The expected URI differs with the function's URI: " +
//                    uri.toString() + " vs " + mFunction.getUri().toString() );
//
//                unRegister();
//                return;
//            }
//
//            String result = null;
//            Cursor c = mContext.getContentResolver().query(mFunction.getUri(),
//                    COLUMNS, null, null, null);
//
//            if(c.moveToFirst()){
//                result = c.getString(COL_EVENT_DATA);
//            }
//            c.close();
//
//            mCallback.onFunctionResult(mFunction, mFunctionResult, result);
//            unRegister();
//        }
//
//        private void unRegister(){
//            mContext.getContentResolver().unregisterContentObserver(this);
//        }
//    }
