package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.CloudEvent;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleEvent;

/**
 * Author: Derek Benda
 */

public class DiagnosticFunction extends Function {

    public static final Parcelable.Creator<DiagnosticFunction> CREATOR = new Parcelable.Creator<DiagnosticFunction>() {
        public DiagnosticFunction createFromParcel(Parcel in) {
            return new DiagnosticFunction(in);
        }

        public DiagnosticFunction[] newArray(int size) {
            return new DiagnosticFunction[size];
        }
    };

    protected DiagnosticFunction(Parcel in) {
        super(in);
    }

    public DiagnosticFunction(String deviceId) {
        super(Firmware.Function.BPT_DIAG.getName(), deviceId);


        this.addArgumentAtPos(1, "1"); // so a bpt:diag event is published from the firmware
    }

    @Override
    public boolean doReceiveEvents() { // Listen for the default event (bpt:diag)
        return true;
    }

    @Override
    public void addArgument(int argumentId, String arg) {
        throw new IllegalArgumentException("This function does not accept additional arguments");
    }

    @Override
    protected String[] validateArgs(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("This function does not accept additional arguments");
        }

        return args;
    }
}
