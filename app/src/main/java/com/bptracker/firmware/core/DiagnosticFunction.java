package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class DiagnosticFunction extends BptApi {

    public DiagnosticFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_DIAG.getName());


        this.registerEventReceiver(); // Listen for the bpt:diag event

        this.addArgumentAtPos(1, "1"); // so a bpt:diag event is published from the firmware
    }


    @Override
    public void addArgument(int argumentId, String arg) {
        throw new IllegalArgumentException("This function does not accept additional arguments");
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("This function does not accept additional arguments");
        }
    }
}
