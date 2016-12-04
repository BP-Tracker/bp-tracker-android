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


    /**
     * Strips the event code from data in a bpt:event event and returns the rest.
     * Firmware result format: event_code,ack_required[,data1[,data2..]].
     * @param eventName Event name
     * @param eventData Event data in its raw form
     * @throws IllegalArgumentException
     * @return The parsed data
     */
    public static String getBptEventData(String eventName, String eventData) {

        String r = "";

        if(!isBptEvent(eventName)){
            throw new IllegalArgumentException(
                    "getBptEventData can only be called on a BPT_EVENT events: " + eventName);
        }

        int sep = eventData.indexOf(",");

        if(sep > 0){
            return eventData.substring(sep + 1);
        }

        return r;
    }

    // eventData format: event_code[,data1[,data2..]]
    public static EventType getBptEventType(String eventName, String eventData) {

        if(!isBptEvent(eventName)){
            throw new IllegalArgumentException(
                    "getBptEventType can only be called on a BPT_EVENT events: " + eventName);
        }

        if(TextUtils.isEmpty(eventData)){
            throw new IllegalArgumentException(eventName + " contains missing or malformed data");
        }

        int sep = eventData.indexOf(",");

        String code = sep > 0 ? eventData.substring(0, sep) : eventData;
        EventType e = EventType.fromCode(Integer.parseInt(code));

        if (e == null) {
            throw new IllegalArgumentException("bpt event code " + code + " is unknown");
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
