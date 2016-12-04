package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;

/**
 * Author: Derek Benda
 */

public class AckFunction extends BptApi {

    public AckFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_ACK.getName());

    }

    @Override
    public void addArgument(int argumentId, String arg) {

    }

    @Override
    protected void validateArgsForCall(String[] args) {

    }
}
