package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Util;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * TODO: support optional arguments
 */

public class StatusFunction extends Function {

    public static final Parcelable.Creator<StatusFunction> CREATOR = new Parcelable.Creator<StatusFunction>() {
        public StatusFunction createFromParcel(Parcel in) {
            return new StatusFunction(in);
        }

        public StatusFunction[] newArray(int size) {
            return new StatusFunction[size];
        }
    };

    protected StatusFunction(Parcel in) {
        super(in);
    }

    public StatusFunction(String deviceId) {
        super(Firmware.Function.BPT_STATUS.getName(), deviceId);
    }

    @Nullable
    @Override
    public String receiveEvent(String name, ParticleEvent event) {
        CloudEvent eventName = CloudEvent.fromName(name);

        if (eventName == null || !isMyDevice(event)) {
            return null;
        }

        if(Util.isBptEvent(name)) {  // Listen for the bpt:event STATUS_UPDATE event
            Firmware.EventType type = Util.getBptEventType(name, event.dataPayload);

            if (type == Firmware.EventType.STATUS_UPDATE) {

                String data = Util.getBptEventData(name, event.dataPayload);
                return data;
            }
        }

        return null;
    }

    @Override
    public boolean doReceiveEvents() {
        return true;
    }

    @Override
    public void addArgument(int argumentId, String arg) {
        throw new IllegalArgumentException("Function does not support any arguments");
    }

    @Override
    protected String[] validateArgs(String[] args) {
        if (args.length > 0) {
            throw new IllegalArgumentException("Function does not support any arguments");
        }

        return args;
    }
}