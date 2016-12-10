package com.bptracker.firmware.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Util;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */
public class ProbeFunction extends Function {

    public static final Parcelable.Creator<ProbeFunction> CREATOR = new Parcelable.Creator<ProbeFunction>() {
        public ProbeFunction createFromParcel(Parcel in) {
            return new ProbeFunction(in);
        }

        public ProbeFunction[] newArray(int size) {
            return new ProbeFunction[size];
        }
    };

    protected ProbeFunction(Parcel in) {
        super(in);
    }

    public ProbeFunction(String deviceId) {
        super(Firmware.Function.BPT_PROBE.getName(), deviceId);
    }

    // Listen for the bpt:event PROBE_CONTROLLER event
    @Nullable
    @Override
    public String receiveEvent(String name, ParticleEvent event) {

        if(isMyDevice(event) && Util.isBptEvent(name)){

            EventType type = Util.getBptEventType(name, event.dataPayload);

            if (type == EventType.PROBE_CONTROLLER) {
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
        throw new IllegalArgumentException("This function does not accept additional arguments");
    }

    @Override
    protected String[] validateArgs(String[] args) {
        if (args.length > 0) {
            throw new IllegalArgumentException("This function does not accept additional arguments");
        }

        return args;
    }
}







        /*
        super(context, deviceId, Firmware.Function.BPT_PROBE.getName());

        // Listen for the bpt:event PROBE_CONTROLLER event
        this.registerEventReceiver(new EventReceiver() {

            @Override
            public boolean receive(String name, ParticleEvent event) {

                if(isMyDevice(event) && Util.isBptEvent(name)){

                    EventType type = Util.getBptEventType(name, event.dataPayload);

                    if (type == EventType.PROBE_CONTROLLER) {
                        String data = Util.getBptEventData(name, event.dataPayload);
                        completeCall(data, event);
                        return true;
                    }
                }

                return false;
            }
        });
        */
