package com.bptracker.firmware.core;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.State;
import com.bptracker.firmware.Util;

import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */

public class StateFunction extends Function {

    private boolean mGetStateMode;

    public static final Parcelable.Creator<StateFunction> CREATOR = new Parcelable.Creator<StateFunction>() {
        public StateFunction createFromParcel(Parcel in) {
            return new StateFunction(in);
        }

        public StateFunction[] newArray(int size) {
            return new StateFunction[size];
        }
    };


    protected StateFunction(Parcel in) {
        super(in);
        mGetStateMode = in.readByte() != 0;
    }


    public StateFunction(String deviceId) {
        super(Firmware.Function.BPT_STATE.getName(), deviceId);
        mGetStateMode = true;
    }

    @Override
    public boolean doReceiveEvents() {
        return true;
    }

    //
    @Override
    public @Nullable String receiveEvent(String eventName, ParticleEvent event) {
        _log.v("receiverEvent called [eventName=" + eventName + "] " + isMyDevice(event) + "][" + Util.isBptEvent(eventName));
        if(isMyDevice(event)){

            if(mGetStateMode){

                if( eventName.equals(this.getName()) ){ // found event
                    return event.dataPayload;
                }

            }else{

                if(Util.isBptEvent(eventName)) {  // Listen for the bpt:event STATE_CHANGE event
                    EventType type = Util.getBptEventType(eventName, event.dataPayload);

                    if (type == EventType.STATE_CHANGE) {

                        String data = Util.getBptEventData(eventName, event.dataPayload);
                        _log.v("Found event [" + data + "]");
                        return data;
                    }
                }
            }
        }
        return null;
    }


    @Override
    public void addArgument(int argumentId, String arg) {

        // NB: accepts one argument only (change state)
        State state = State.getState(Integer.getInteger(arg));

        if (state == null) {
            throw new IllegalArgumentException(arg + " is not a valid State");
        }

        this.addArgument(argumentId, state);
    }


    /**
     * Coerces arg into a Firmware.State and uses the associated code. Note
     * the firmware permits changing the state to one of the public states only
     * @param argumentId    Accepts BptApi.ARG_STATE
     * @param arg           A Firmware.State object
     */
    @Override
    public void addArgument(int argumentId, Object arg) {

        if(argumentId != BptApi.ARG_STATE){
            throw new IllegalArgumentException("Argument id " + argumentId + " is not supported");
        }

        try {
            State state = (State) arg;

            if (state == null || state.isPrivate()) {
                throw new IllegalArgumentException(state.name() + " is not a public state");
            }

            mGetStateMode = false;
            this.addArgumentAtPos(1, Integer.toString(state.getCode()));

        }catch (ClassCastException e){
            throw new IllegalArgumentException("not a state object", e);
        }
    }

    @Override
    protected String[] validateArgs(String[] args) {
        if(args.length > 1){
            throw new IllegalArgumentException("The state function only accepts zero or one argument");
        }
        return args;
    }


    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (mGetStateMode ? 1 : 0));
    }

    private static final TLog _log = TLog.get(StateFunction.class);
}