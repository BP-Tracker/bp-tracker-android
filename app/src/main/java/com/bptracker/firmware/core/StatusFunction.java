package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class StatusFunction extends BptApi {

    public StatusFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_STATUS.getName());

        // Listen for the bpt:status event
        this.registerEventReceiver(new EventReceiver() {

            @Override
            public boolean receive(String name, ParticleEvent event) {

                CloudEvent eventName = CloudEvent.fromName(name);

                if (eventName == null || !isMyDevice(event)) {
                    return false;
                }

                if(eventName == CloudEvent.BPT_STATUS ){
                    completeCall(event.dataPayload, event);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void addArgument(int argumentId, String arg) {
        throw new IllegalArgumentException("Function does not support any arguments");
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        if (args.length > 0) {
            throw new IllegalArgumentException("Function does not support any arguments");
        }
    }
}
