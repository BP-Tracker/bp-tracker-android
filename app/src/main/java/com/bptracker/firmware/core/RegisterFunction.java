package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bptracker.firmware.Firmware.Function;

/**
 * Author: Derek Benda
 */
public class RegisterFunction extends SimpleFunction { //TODO

    public static final Parcelable.Creator<RegisterFunction> CREATOR = new Parcelable.Creator<RegisterFunction>() {
        public RegisterFunction createFromParcel(Parcel in) {
            return new RegisterFunction(in);
        }

        public RegisterFunction[] newArray(int size) {
            return new RegisterFunction[size];
        }
    };

    protected RegisterFunction(Parcel in) {
        super(in);
    }

    public RegisterFunction(String deviceId) {
        super(deviceId, Function.BPT_REGISTER.getName());
    }
}
