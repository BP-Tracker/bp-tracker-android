package com.bptracker.firmware.core;

/**
 * Author: Derek Benda
 */

import android.content.Context;


public class SimpleFunction extends BptApi {


    public SimpleFunction(Context context, String deviceId, String mFunctionName) {
        super(context, deviceId, mFunctionName);
    }

    /**
     * This method allows passing arbitrary arguments to the function.
     * @param pos       The position of the argument which must be greater than 0
     * @param arg       The argument
     * @throws IllegalArgumentException
     */
    @Override
    public void addArgument(int pos, String arg) {
        this.addArgumentAtPos(pos, arg);
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        return; //no-op
    }
}
