package com.bptracker.firmware;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Derek Benda
 */

public class Firmware { /** TODO: double check all codes **/


    // NB: the enum's ordinal will not match the state code
    @SuppressWarnings({"UnusedDeclaration"})
    public enum State {

        OFFLINE(1, false),
        DEACTIVATED(2, false),
        RESET(3, false),
        ARMED(4, false),
        DISARMED(5, false),
        PANIC(6, false),
        PAUSED(7, false),
        RESUMED(8, false),
        ACTIVATED(9, true /* private firmware state */),
        SOFT_PANIC(10, true),
        ONLINE_WAIT(11, true),
        SLEEP(12, true);

        public boolean isPrivate(){
            return this.isPrivate;
        }

        /**
         * Returns the firmware's state number associated with this State
         * @return The state code number
         */
        public int getCode(){
            return this.code;
        }

        /**
         * @param stateCode Firmware state code number
         * @return The unique State enum associated with the number
         */
        public static State getState(int stateCode){
            return map.get(stateCode);
        }

        private int code;
        private boolean isPrivate;

        State(int code, boolean isPrivate){
            this.code = code;
            this.isPrivate = isPrivate;
        }


        private static SparseArray<State> map = new SparseArray<>();

        static {
            for(State state : State.values()){
                map.put(state.code, state);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public enum EventType {

        STATE_CHANGE(1),
        REQUEST_GPS(2),
        BATTERY_LOW(3),
        NO_GPS_SIGNAL(4),
        SOFT_PANIC(5),
        PANIC(6),
        PROBE_CONTROLLER(7),
        TEST(8),
        SERIAL_COMMAND(9),
        ERROR(10),
        HARDWARE_FAULT(11);


        public int getCode(){
            return this.code;
        }

        public static EventType fromCode(int eventCode){
            return map.get(eventCode);
        }

        private int code;

        EventType(int code){
            this.code = code;
        }

        private static SparseArray<EventType> map = new SparseArray<>();

        static {
            for(EventType e : EventType.values()){
                map.put(e.code, e);
            }
        }
    }



    @SuppressWarnings({"UnusedDeclaration"})
    public enum CloudEvent { // BPT events received from the cloud

        BPT_EVENT("bpt:event"),
        BPT_STATE("bpt:state"),
        BPT_GPS("bpt:gps"),
        BPT_STATUS("bpt:status"),
        BPT_DIAG("bpt:diag"); /** TODO: more events might be coming in **/

        public static CloudEvent fromName(String name){
            return map.get(name);
        }

        public String getEventName(){
            return this.eventName;
        }

        private String eventName;

        private CloudEvent(String eventName){
            this.eventName = eventName;
        }

        private static Map<String, CloudEvent> map = new HashMap<String, CloudEvent>();
        static {
            for(CloudEvent e : CloudEvent.values()){
                map.put(e.eventName, e);
            }
        }
    }

    /** TODO is this required ?? **/
    @SuppressWarnings({"UnusedDeclaration"})
    public enum Property {
        CONTROLLER_MODE(1),
        GEOFENCE_RADIUS(2),
        ACCEL_THRESHOLD(3),
        ACK_ENABLED(4),
        SLEEP_WAKEUP_STANDBY(5);


        public static Property getProperty(int propertyCode){
            return map.get(propertyCode);
        }

        public int getPropertyCode(){
            return this.propertyCode;
        }

        private int propertyCode;

        Property(int propertyCode){
            this.propertyCode = propertyCode;
        }

        private static SparseArray<Property> map = new SparseArray<>();

        static {
            for(Property p : Property.values()){
                map.put(p.propertyCode, p);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public enum Function {
        BPT_STATE("bpt:state", true, true),
        BPT_GPS("bpt:gps", true, true),
        BPT_STATUS("bpt:status", true, true),
        BPT_DIAG("bpt:diag", true, true),
        BPT_REGISTER("bpt:register", true, true),
        BPT_ACK("bpt:ack", true, true),
        BPT_PROBE("bpt:probe", true, true),
        BPT_TEST("bpt:test", true, true),
        BPT_RESET("bpt:reset", true, true);

        /**
         * Returns the firmware's function name
         * @return The function name
         */
        public String getName(){
            return this.name;
        }

        /**
         * Returns the Function enum matching the passed in name
         * @param name
         * @return the Function enum
         */
        public static Function getFunction(String name){
            return map.get(name);
        }

        private String name;
        private boolean publishesEvents;
        private boolean acceptsArgs;

        Function(String name, boolean publishesEvents, boolean acceptsArgs){
            this.name = name;
            this.publishesEvents = publishesEvents;
            this.acceptsArgs = acceptsArgs;
        }

        private static Map<String, Function> map = new HashMap<>();
        static {
            for(Function func : Function.values()){
                map.put(func.name, func);
            }
        }
    }

}

