package com.bptracker.firmware.core;

import android.content.Context;

import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.State;
import com.bptracker.firmware.Util;

import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */

public class StateFunction extends BptApi {

    public StateFunction(Context context, String deviceId) {
        super(context, deviceId, Firmware.Function.BPT_STATE.getName());

        // Listen for the bpt:event STATE_CHANGE event
        this.registerEventReceiver(new EventReceiver() {

            @Override
            public boolean receive(String name, ParticleEvent event) {

                if(isMyDevice(event) && Util.isBptEvent(name)){

                    EventType type = Util.getBptEventType(name, event.dataPayload);

                    if (type == EventType.STATE_CHANGE) {
                        //_log.d("Found event");
                        String data = Util.getBptEventData(name, event.dataPayload);
                        completeCall(data, event);
                        return true;
                    }
                }

                return false;
            }
        });
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

            this.addArgumentAtPos(1, Integer.toString(state.getCode()));

        }catch (ClassCastException e){
            throw new IllegalArgumentException("not a state object", e);
        }
    }

    @Override
    protected void validateArgsForCall(String[] args) {

        if(args.length > 1){
            throw new IllegalArgumentException("The state function only accepts zero or one argument");
        }
    }

    private static final TLog _log = TLog.get(StateFunction.class);
}