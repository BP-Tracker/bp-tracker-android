package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;

import java.util.List;

/**
 * Author: Derek Benda
 */

public class ResetFunction extends BptApi {

    boolean softwareResetSet;

    public ResetFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_RESET.getName());
        softwareResetSet = false;
    }

    @Override
    public void addArgument(int argumentId, String arg) {

        int v = Integer.parseInt(arg);

        if (v < 0 || v > 1) {
            throw new IllegalArgumentException("Argument only accepts 0 or 1 values");
        }

        addArgument(argumentId, v == 1);
    }

    /**
     * Add arguments to reset
     * @param argumentId    A valid argument id
     * @param arg   A boolean object
     */
    @Override
    public void addArgument(int argumentId, Object arg) {

        if(argumentId != BptApi.ARG_PROPERTY_RESET
                && argumentId != BptApi.ARG_SOFTWARE_RESET){

            throw new IllegalArgumentException("Argument " + argumentId + " is not supported");
        }

        try {

            Boolean b = (Boolean) arg;

            if(argumentId == BptApi.ARG_PROPERTY_RESET){
                addArgumentAtPos(1, b ? "1" : "0" );
            }else{
                addArgumentAtPos(2, b ? "1" : "0" );
                softwareResetSet = true;
            }

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("not a boolean object", e);
        }

    }

    @Override
    protected void validateArgsForCall(String[] args) {

        if(softwareResetSet && args.length == 1){
            // set the props reset to the default (false)
            addArgumentAtPos(1, "0"); //todo will this correctly modify args?
        }

    }
}
