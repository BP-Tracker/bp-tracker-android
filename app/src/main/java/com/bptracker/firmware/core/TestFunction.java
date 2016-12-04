package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.TestInput;

/**
 * Author: Derek Benda
 */

public class TestFunction extends BptApi {

    // NB: not events are returned from this function
    public TestFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_TEST.getName());
    }

    @Override
    public void addArgument(int argumentId, String arg) {

        if(argumentId == BptApi.ARG_TEST_INPUT){
            TestInput input = TestInput.fromCode(Integer.parseInt(arg));

            if (input == null) {
                throw new IllegalArgumentException(arg + " is not a valid TestInput code");
            }
            addArgument(argumentId, input);
        }else{
            addArgument(argumentId, (Object) arg);
        }
    }

    @Override
    public void addArgument(int argumentId, Object arg) {

        if(argumentId != BptApi.ARG_TEST_INPUT && argumentId != BptApi.ARG_TEST_INPUT_STRING){
            throw new IllegalArgumentException("Argument " + argumentId + " is not supported");
        }

        if(argumentId == BptApi.ARG_TEST_INPUT){
            try {
                TestInput input = (TestInput) arg;
                addArgumentAtPos(1, Integer.toString(input.getCode()));

            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Argument is not a Firmware.TestInput object");
            }

        }else{
            addArgumentAtPos(2, arg.toString());
        }
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Function expects two arguments");
        }
    }
}
