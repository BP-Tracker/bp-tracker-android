package com.bptracker.firmware.core;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.firmware.FirmwareException;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */
public class Function implements Parcelable, BptApi.FunctionResultReceiver  {

    public static final int FUNCTION_ARG_CAPACITY = 10;

    public static final Parcelable.Creator<Function> CREATOR = new Parcelable.Creator<Function>() {
        public Function createFromParcel(Parcel in) {
            return new Function(in);
        }

        public Function[] newArray(int size) {
            return new Function[size];
        }
    };

    private String mFunctionName;
    private String[] mFunctionArgs;
    private String mDeviceId;
    private int mLargestFunctionPos;
    private boolean mFinalized;
    private Uri mUri;

    public Function(String name, String deviceId) {
        init(name, deviceId);
    }

    public Function(String name, String[] args, String deviceId){
        init(name, deviceId);
        mFunctionArgs = args;
        mLargestFunctionPos = args.length - 1;
    }


    protected Function(Parcel in) {
        mFunctionName = in.readString();
        mFunctionArgs = in.createStringArray();
        mDeviceId = in.readString();
        mLargestFunctionPos = in.readInt();
        mFinalized = in.readByte() != 0;

        String uri = in.readString();
        mUri = uri == null ? null : Uri.parse(uri);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mFunctionName);
        out.writeStringArray(mFunctionArgs);
        out.writeString(mDeviceId);
        out.writeInt(mLargestFunctionPos);
        out.writeByte((byte) (mFinalized ? 1 : 0));
        String uri = mUri != null ? mUri.toString() : null;
        out.writeString(uri);
    }


    /**
     * Override this method to add arguments to the function and use addArgumentAtPos to do it.
     * @param argumentId    An ID the concrete class understands
     * @param arg           The argument
     *
     * @throws  IllegalArgumentException
     */
    public void addArgument(int argumentId, String arg){
        // by default, use the argumentId as the position of the argument
        this.addArgumentAtPos(argumentId, arg);
    }

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


    public String getDeviceId(){
        return mDeviceId;
    }

    public String getName(){
        return  mFunctionName;
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public void setUri(Uri uri) {
        mUri = uri;
    }

    // used by the api to get the arguments for this function. When
    // invoking this with finalized == true, the arguments cannot be changed afterwards
    public String[] getArgs(boolean finalized){
        String[] args;

        if (finalized) {
            args = finalizeArguments();
        }else{
            args = mFunctionArgs;
        }

        return args;
    }


    //TODO: clean this up
    public String getArgs(){
        String[] funcArgs = this.getArgs(true);

        String funcArgsStr = "";

        if(funcArgs.length > 0) {
            StringBuffer buf = new StringBuffer(funcArgs.length);
            for (int i = 0; i < funcArgs.length - 1; i++) {

                String a = TextUtils.isEmpty(funcArgs[i]) ? "" : funcArgs[i];
                buf.append(a);
                buf.append(",");
            }
            buf.append(funcArgs[funcArgs.length - 1]);
            funcArgsStr = buf.toString();
        }

        return funcArgsStr;
    }


    /**
     * Perform a final validation of all arguments. Call this method
     *
     * @return The final arguments that will be sent to the device
     *
     * @throws IllegalArgumentException when the arguments are not valid
     */
    protected String[] validateArgs(String[] args){
        return args;
    }


    /**
     * Call this to perform final validation of the argument
     * @return
     */
    public String[] finalizeArguments(){
        if(!mFinalized){

            String[] resized = resizeFunctionArgs(); // resize down to the largest position added by addArgumentAtPos

            mFunctionArgs = validateArgs(resized);
            mFinalized = true;
        }

        return mFunctionArgs;
    }

    // called when the function is about to be called
    // if true the receive method will be invoked when events are published from the device
   // Call this method during initialization or validation when the function is expected to
    //* return data in a particle event. The receiver will be enabled once the function is called
    //* and disabled and unregistered when its receive method returns true;
    public boolean doReceiveEvents(){
        return false;
    }



    // NB: this method will only be invoked when doReceiveEvents return true
    // By default: look for an event having the function name
    @Override
    public @Nullable String receiveEvent(String name, ParticleEvent event) {
        if(isMyDevice(event) && name.equals(this.getName())){
            return event.dataPayload;
        }

        return null;
    }


    /**
     * Adds or replaces the argument value at the specified position starting
     * at position 1. Note: only basic validation occurs here.
     * @param pos The position to place the argument at
     * @param arg The argument value
     *
     * @throws FirmwareException if the function has already finalized the arguments
     * @throws IllegalArgumentException if the argument is not valid
     */
    protected void addArgumentAtPos(int pos, String arg) {

        if (mFinalized) {
            throw new FirmwareException("function arguments have already been finalized");
        }

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
     * Is the event coming from this device.
     * @param event The event
     * @return  True if the event came from this device TODO: deprecate this
     */
    protected boolean isMyDevice(ParticleEvent event){
        return this.getDeviceId().equals(event.deviceId);
    }


    private String[] resizeFunctionArgs(){
        String[] resized = new String[mLargestFunctionPos + 1];
        for(int i = 0; i <= mLargestFunctionPos; i++) {
            resized[i] = mFunctionArgs[i];
        }
        return resized;
    }

    //TODO: useful?
    @Nullable
    public <T extends Function> T getSubClass() {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public String toString() { //TODO
        return super.toString();
    }

    @Override
    public void receiveResult(int result){
        //no-op
    }



    private void init(String name, String deviceId){
        mFunctionName = name;
        mFinalized = false;
        mUri = null;
        mDeviceId = deviceId;
        mFunctionArgs = new String[FUNCTION_ARG_CAPACITY];
        mLargestFunctionPos = -1;
    }


}








//protected void setEventReceiver(boolean registered) {
//     mRegisterEventReceiver = registered;
// }
