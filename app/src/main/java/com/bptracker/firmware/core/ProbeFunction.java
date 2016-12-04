package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.EventType;

import com.bptracker.firmware.Util;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class ProbeFunction extends BptApi {

    public ProbeFunction(Context context, String deviceId) {
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
    }


    @Override
    public void addArgument(int argumentId, String arg) {
        throw new IllegalArgumentException("This function does not accept additional arguments");
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        if (args.length > 0) {
            throw new IllegalArgumentException("This function does not accept additional arguments");
        }
    }
}
