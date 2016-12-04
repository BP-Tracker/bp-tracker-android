package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Util;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class GpsFunction extends BptApi {

    boolean mRequestingCoords;


    public GpsFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_GPS.getName());

        mRequestingCoords = true;
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
    protected void validateArgsForCall(String[] args) {
        if (args.length > 0 && args.length != 2) {
            throw new IllegalArgumentException("GPS function supports 0 to 2 arguments");
        }

        if (mRequestingCoords) {

            // Listen for the bpt:gps or bpt:event NO_GPS_SIGNAL event
            this.registerEventReceiver(new EventReceiver() {

                @Override
                public boolean receive(String name, ParticleEvent event) {

                    CloudEvent eventName = CloudEvent.fromName(name);

                    if (eventName == null || !isMyDevice(event)) {
                        return false;
                    }

                    if(eventName == CloudEvent.BPT_GPS ){
                        completeCall(event.dataPayload, event);
                        return true;
                    }

                    if (eventName == CloudEvent.BPT_EVENT) {
                        EventType type = Util.getBptEventType(name, event.dataPayload);

                        if (type == EventType.NO_GPS_SIGNAL) {
                            completeCall(type.name(), event);
                            return true;
                        }
                    }
                    return false;
                }
            });

        }
    }
}
