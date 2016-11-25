package com.bptracker.util;

/**
 * Author: Derek Benda
 */

public class IntentUtil {

    /**
     * This local broadcast action forwards incoming device events from the particle.io cloud
     *
     * Extras: EXTRA_FROM_BPT_DEVICE, EVENT_NAME, EXTRA_EVENT_DATA, EXTRA_DEVICE_NAME
     *
     * URI format: content://com.bptracker/events/[event_id]
     */
    public static final String ACTION_DEVICE_EVENT = "com.bptracker.intent.action.DEVICE_EVENT";


    /**
     * This broadcast action forwards incoming btp:event events from the particle.io cloud
     *
     * Extras: EXTRA_BPT_EVENT_TYPE, EVENT_NAME, EXTRA_EVENT_DATA, EXTRA_DEVICE_NAME
     *
     * Requires permission: PERMISSION_RECEIVE_BPT_EVENTS
     *
     * URI format: content://devices/[cloud_device_id]/bpt-events/[event_id]
     */
    public static final String ACTION_BPT_EVENT = "com.bptracker.intent.action.BPT_EVENT";


    /********************************************************
       Extras for the above actions
     *******************************************************/

    // the name of the event
    public static final String EXTRA_EVENT_NAME = "com.bptracker.intent.extra.EVENT_NAME";

    // the data from the event
    public static final String EXTRA_EVENT_DATA = "com.bptracker.intent.extra.EVENT_DATA";


    // the device name (if available)
    public static final String EXTRA_DEVICE_NAME = "com.bpt.intent.extra.DEVICE_NAME";

    // the particle.io assigned device ID
    public static final String EXTRA_DEVICE_ID = "com.bpt.intent.extra.DEVICE_ID";

    // Is the event originating from the device loaded with the BPT firmware?
    public static final String EXTRA_FROM_BPT_DEVICE = "com.bpt.intent.extra.FROM_BPT_DEVICE";

    // Holds the Firmware.Event type (if applicable)
    public static final String EXTRA_BPT_EVENT_TYPE = "com.bpt.intent.extra.BPT_EVENT_TYPE";


    /********************************************************
     Intent permissions
     *******************************************************/

    //NB: this string is also hard-coded in the manifest
    public static final String PERMISSION_RECEIVE_EVENTS = "com.bptracker.permission.RECEIVE_EVENTS";

}
