package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.TestInput;

import javax.annotation.Nonnull;

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

        if(argumentId == BptApi.ARG_TEST_INPUT){
            try {
                TestInput input = (TestInput) arg;
                if (input == null) {
                    throw new IllegalArgumentException("Argument is not a valid TestInput object");
                }

                addArgumentAtPos(1, Integer.toString(input.getCode()));

            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Argument is not a TestInput object");
            }

        }else if(argumentId == BptApi.ARG_STRING_DATA){

            addArgumentAtPos(2, arg.toString());

        }else{
            throw new IllegalArgumentException("Argument is not supported");
        }
    }

    @Override
    protected void validateArgsForCall(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Function expects two arguments");
        }
    }
}
