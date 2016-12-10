package com.bptracker.firmware.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */
public class SimpleFunction extends Function {

    private boolean mExpectsEvent;

    public static final Parcelable.Creator<SimpleFunction> CREATOR = new Parcelable.Creator<SimpleFunction>() {
        public SimpleFunction createFromParcel(Parcel in) {
            return new SimpleFunction(in);
        }

        public SimpleFunction[] newArray(int size) {
            return new SimpleFunction[size];
        }
    };

    protected SimpleFunction(Parcel in) {
        super(in);
        mExpectsEvent = in.readByte() != 0;
    }


    /**
     * Creates a function where the ID of the argument passed into addArgument() is argument position
     * @param deviceId  The cloud device id
     * @param name The name of the function
     * @param expectsEvent  Set to true if the function publishes a particle event with the same
     *                      name as the function and you want to listen for it. Default is false.s
     */
    public SimpleFunction(String name, String deviceId, boolean expectsEvent) {

        super(name, deviceId);

        mExpectsEvent = expectsEvent;
    }


    public SimpleFunction(String name, String deviceId) {
        super(name, deviceId);
        mExpectsEvent = false;
    }


    @Override
    public boolean doReceiveEvents() {
        return mExpectsEvent;
    }


    @Nullable
    @Override
    public String receiveEvent(String name, ParticleEvent event) {
        if(isMyDevice(event) && name.equals(this.getName())){
            return event.dataPayload;
        }

       return null;
    }

    /**
     * This method allows passing arbitrary arguments to the function.
     * @param pos       The position of the argument which must be greater than 0
     * @param arg       The argument
     * @throws IllegalArgumentException
     */
    //@Override
    public void addArgument(int pos, String arg) {
        this.addArgumentAtPos(pos, arg);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (mExpectsEvent ? 1 : 0));
    }

}