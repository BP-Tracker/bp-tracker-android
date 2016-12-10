package com.bptracker.firmware.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Util;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class GpsFunction extends Function {

    private boolean mRequestingCoords;
    private boolean mExpectsEvent;

    public static final Parcelable.Creator<GpsFunction> CREATOR = new Parcelable.Creator<GpsFunction>() {
        public GpsFunction createFromParcel(Parcel in) {
            return new GpsFunction(in);
        }

        public GpsFunction[] newArray(int size) {
            return new GpsFunction[size];
        }
    };

    protected GpsFunction(Parcel in) {
        super(in);
        mRequestingCoords = in.readByte() != 0;
        mExpectsEvent = in.readByte() != 0;
    }

    public GpsFunction(String deviceId) {
        super(Firmware.Function.BPT_GPS.getName(), deviceId);
        mRequestingCoords = true;
        mExpectsEvent = false;
    }

    /**
     *
     * @param argumentId    ARG_LATITUDE_ID or ARG_LONGITUDE_ID
     * @param arg           The argument
     */
    @Override
    public void addArgument(int argumentId, String arg) {

        if (argumentId != BptApi.ARG_LATITUDE && argumentId != BptApi.ARG_LONGITUDE) {
            throw new IllegalArgumentException("Argument " + argumentId + " is not supported");
        }

        if(argumentId == BptApi.ARG_LATITUDE){
            addArgumentAtPos(1, arg);
        }else{
            addArgumentAtPos(2, arg);
        }

        mRequestingCoords = false;
    }

    @Override
    public void addArgument(int argumentId, float arg) {
        addArgument(argumentId, Float.toString(arg));
    }

    @Override
    public boolean doReceiveEvents() {
        return mExpectsEvent;
    }

    // Listen for the bpt:gps or bpt:event NO_GPS_SIGNAL event
    @Nullable
    @Override
    public String receiveEvent(String name, ParticleEvent event) {
        CloudEvent eventName = CloudEvent.fromName(name);

        if (eventName == null || !isMyDevice(event)) {
            return null;
        }

        if(eventName == CloudEvent.BPT_GPS ){
            return event.dataPayload;
        }

        if (eventName == CloudEvent.BPT_EVENT) {
            EventType type = Util.getBptEventType(name, event.dataPayload);

            if (type == EventType.NO_GPS_SIGNAL) {
                return type.name(); //return the NO_GPS_SIGNAL keyword TODO: error prone
            }
        }
        return null;
    }

    @Override
    protected String[] validateArgs(String[] args) {
        if (args.length > 0 && args.length != 2) {
            throw new IllegalArgumentException("GPS function supports 0 to 2 arguments");
        }

        if (mRequestingCoords) {
            mExpectsEvent = true;
        }

        return args;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (mRequestingCoords ? 1 : 0));
        out.writeByte((byte) (mExpectsEvent ? 1 : 0));
    }

}
