package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware.Function;

/**
 * Author: Derek Benda
 */

public class RegisterFunction extends SimpleFunction { //TODO

    public RegisterFunction(Context context, String deviceId) {
        super(context, deviceId, Function.BPT_ACK.getName());
    }
}
