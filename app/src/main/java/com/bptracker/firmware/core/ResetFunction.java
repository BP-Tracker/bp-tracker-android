package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bptracker.firmware.Firmware;

import java.util.List;

/**
 * Author: Derek Benda
 */

public class ResetFunction extends Function {

    private boolean mSoftwareResetSet;

    public static final Parcelable.Creator<ResetFunction> CREATOR = new Parcelable.Creator<ResetFunction>() {
        public ResetFunction createFromParcel(Parcel in) {
            return new ResetFunction(in);
        }

        public ResetFunction[] newArray(int size) {
            return new ResetFunction[size];
        }
    };

    protected ResetFunction(Parcel in) {
        super(in);
        mSoftwareResetSet = in.readByte() != 0;
    }

    public ResetFunction(String deviceId) {
        super(Firmware.Function.BPT_RESET.getName(), deviceId);
        mSoftwareResetSet = false;
    }

    @Override
    public void addArgument(int argumentId, String arg) {

        int v = Integer.parseInt(arg);

        if (v < 0 || v > 1) {
            throw new IllegalArgumentException("Argument only accepts 0 or 1 values");
        }

        addArgument(argumentId, v == 1);
    }

    /**
     * Add arguments to reset
     * @param argumentId    A valid argument id
     * @param arg   A boolean object
     */
    @Override
    public void addArgument(int argumentId, Object arg) {

        if(argumentId != BptApi.ARG_PROPERTY_RESET
                && argumentId != BptApi.ARG_SOFTWARE_RESET){

            throw new IllegalArgumentException("Argument " + argumentId + " is not supported");
        }

        try {

            Boolean b = (Boolean) arg;

            if(argumentId == BptApi.ARG_PROPERTY_RESET){
                addArgumentAtPos(1, b ? "1" : "0" );
            }else{
                addArgumentAtPos(2, b ? "1" : "0" );
                mSoftwareResetSet = true;
            }

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("not a boolean object", e);
        }

    }

    @Override
    protected String[] validateArgs(String[] args) {

        if(mSoftwareResetSet && args.length == 1){

            String[] newArgs = new String[2];

            newArgs[0] = args[0];
            newArgs[1] = "0";
            return newArgs; //todo will this correctly modify args?
        }

        return args;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (mSoftwareResetSet ? 1 : 0));
    }
}
