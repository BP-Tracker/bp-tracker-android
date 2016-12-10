package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Firmware;

/**
 * Author: Derek Benda
 *
 * Accepts {@link BptApi#ARG_EVENT_TYPE} and {@link BptApi#ARG_STRING_DATA} arguments
 */
public class AckFunction extends Function {

    public static final Parcelable.Creator<AckFunction> CREATOR = new Parcelable.Creator<AckFunction>() {
        public AckFunction createFromParcel(Parcel in) {
            return new AckFunction(in);
        }

        public AckFunction[] newArray(int size) {
            return new AckFunction[size];
        }
    };


    protected AckFunction(Parcel in) {
        super(in);
    }

    public AckFunction(String deviceId) {
        super(Firmware.Function.BPT_ACK.getName(), deviceId);
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


    /**
     * Adds arguments to this function
     * @param argumentId   Accepts {@link BptApi#ARG_EVENT_TYPE} and {@link BptApi#ARG_STRING_DATA}
     * @param arg          The arguments value
     */
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
        }else if(argumentId == BptApi.ARG_STRING_DATA){

            addArgumentAtPos(2, arg.toString());

        }else{
            throw new IllegalArgumentException("Argument is not supported");
        }
    }

    @Override
    protected String[] validateArgs(String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Function expects two arguments");
        }

        return args;
    }
}
