package com.bptracker.firmware;

/**
 * Author: Derek Benda
 */

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.EventType;

public class Util {


    public static boolean isBptEvent(String eventName){
        return CloudEvent.fromName(eventName) == CloudEvent.BPT_EVENT;
    }


    // this method strips the event_code from eventData and returns the rest
    // eventData format: event_code,ack_required[,data1[,data2..]]
    public static String getBptEventData(String eventName, String eventData)
        throws DataTypeException {

        String r = "";

        if(!isBptEvent(eventName)){
            throw new DataTypeException(
                    "getBptEventData can only be called on a BPT_EVENT events: " + eventName);
        }

        int sep = eventData.indexOf(",");

        if(sep > 0){
            return eventData.substring(sep + 1);
        }

        return r;
    }

    // eventData format: event_code[,data1[,data2..]]
    public static EventType getBptEventType(String eventName, String eventData)
        throws DataTypeException {

        if(!isBptEvent(eventName)){
            throw new DataTypeException(
                    "getBptEventType can only be called on a BPT_EVENT events: " + eventName);
        }

        if(TextUtils.isEmpty(eventData)){
            throw new DataTypeException(eventName + " contains missing or malformed data");
        }

        int sep = eventData.indexOf(",");

        String code = sep > 0 ? eventData.substring(0, sep) : eventData;
        EventType e = EventType.fromCode(Integer.parseInt(code));

        if (e == null) {
            throw new DataTypeException("bpt event code " + code + " is unknown");
        }

        return e;
    }

    @Nullable
    public static String[] getDataElements(EventType eventType, String eventData) { //TODO

        switch (eventType) {
            case PANIC:
                return eventData.split(",");


        }

        return null;
    }


}
