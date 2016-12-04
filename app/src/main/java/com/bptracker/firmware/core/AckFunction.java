package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Firmware.Function;

/**
 * Author: Derek Benda
 */

public class AckFunction extends BptApi {

    public AckFunction(Context context, String deviceId) {
        super(context, deviceId, Function.BPT_ACK.getName());
    }

    @Override
    public void addArgument(int argumentId, String arg) {

        if (argumentId == BptApi.ARG_EVENT_TYPE) {

            EventType type = EventType.fromCode(Integer.parseInt(arg));
            addArgument(argumentId, type);
        } else{
            addArgument(argumentId, (Object) arg);
        }
    }

    @Override
    public void addArgument(int argumentId, Object arg) {

        if (argumentId == BptApi.ARG_EVENT_TYPE) {
            try{
                EventType type = (EventType) arg;

                if(type == null){
                    throw new IllegalArgumentException("Argument is not a valid EventType object");
                }

                addArgumentAtPos(1, Integer.toString(type.getCode()));

            }catch (ClassCastException e) {
                throw new IllegalArgumentException("Argument is not a EventType object");
            }
        }else if(argumentId == ARG_STRING_DATA){

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
