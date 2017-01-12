package com.bptracker.util;

import android.support.annotation.RequiresPermission;

import com.bptracker.Manifest;

/**
 * Author: Derek Benda
 */

public class IntentUtil {

    /**
     * This broadcast action forwards incoming device events from the particle.io cloud or
     * push notifications from GCM
     *
     * Extras: EXTRA_FROM_BPT_DEVICE, EXTRA_DEVICE_ID, EVENT_NAME, EXTRA_EVENT_DATA,
     *         EXTRA_DEVICE_NAME
     *
     *
     * URI format: content://com.bptracker/events/[event_id] TODO: change
     */
    @RequiresPermission(Manifest.permission.RECEIVE_DEVICE_EVENTS)
    public static final String ACTION_DEVICE_EVENT = "com.bptracker.intent.action.DEVICE_EVENT";


    /**
     * This broadcast action forwards incoming btp:event events from the particle.io cloud
     *
     * Extras: EXTRA_BPT_EVENT_TYPE, EXTRA_DEVICE_ID, EVENT_NAME, EXTRA_EVENT_DATA,
     *         EXTRA_DEVICE_NAME
     *
     *
     * URI format: content://devices/[cloud_device_id]/bpt-events/[event_id]
     */
    @RequiresPermission(Manifest.permission.RECEIVE_EVENTS)
    public static final String ACTION_BPT_EVENT = "com.bptracker.intent.action.BPT_EVENT";


    /**
     * This is for local broadcasts. Receives function call results from a call to RunFunctionService
     *
     * Extras: EXTRA_FUNCTION, EXTRA_FUNCTION_RESULT, EXTRA_FUNCTION_EVENT_RESULT (if available),
     *         EXTRA_FUNCTION_ERROR (if an error occurred)
     */
    public static final String ACTION_FUNCTION_RESULT = "com.bptracker.intent.action.FUNCTION_RESULT";


    /**
     * A local service to load and sync devices from the particle cloud. The service send
     * a local broadcast with this action when the data has been synced successfully or an error
     * occurred.
     *
     * Extras: EXTRA_ACTION_SUCCESS, EXTRA_ACTION_ERROR, EXTRA_INFO
     */
    public static final String ACTION_LOAD_DEVICES = "com.bptracker.intent.action.LOAD_DEVICES";


    /**
     * Service action for ExternalService`
     *
     */
    @RequiresPermission(Manifest.permission.RUN_DEVICE_FUNCTION)
    public static final String ACTION_RUN_DEVICE_FUNCTION = "com.bptracker.intent.action.RUN_DEVICE_FUNCTION";


    /********************************************************
       Extras for the above actions
       All data types are String unless documented otherwise
     *******************************************************/

    // the name of the event
    public static final String EXTRA_EVENT_NAME = "com.bptracker.intent.extra.EVENT_NAME";

    // the data from the event
    public static final String EXTRA_EVENT_DATA = "com.bptracker.intent.extra.EVENT_DATA";


    // the device name (if available)
    public static final String EXTRA_DEVICE_NAME = "com.bpt.intent.extra.DEVICE_NAME";

    // LatLng data type
    public static final String EXTRA_LAT_LNG = "com.bpt.intent.extra.LAT_LNG";

    public static final String EXTRA_INFO = "com.bpt.intent.extra.INFO";

    // The BptApi Function
    public static final String EXTRA_FUNCTION = "com.bpt.intent.extra.FUNCTION";


    // Parcelable type io.particle.android.sdk.cloud.ParticleEvent
    public static final String EXTRA_PARTICLE_EVENT  = "com.bpt.intent.extra.PARTICLE_EVENT";

    // int datatype? TODO
    public static final String EXTRA_FUNCTION_RESULT = "com.bpt.intent.extra.FUNCTION_RESULT";

    public static final String EXTRA_FUNCTION_EVENT_RESULT = "com.bpt.intent.extra.FUNCTION_EVENT_RESULT";

    // boolean indicating the request was performed successfully
    // if it, false the reason will be in EXTRA_ACTION_ERROR
    public static final String EXTRA_ACTION_SUCCESS = "com.bpt.intent.extra.ACTION_SUCCESS";

    public static final String EXTRA_ACTION_ERROR = "com.bpt.intent.extra.ACTION_ERROR";

    // TODO: deprecate this in favor of EXTRA_REQUEST_ERROR
    public static final String EXTRA_ERROR = "com.bpt.intent.extra.ERROR";

    // the particle.io assigned device ID
    public static final String EXTRA_DEVICE_ID = "com.bpt.intent.extra.DEVICE_ID";

    // Is the event originating from the device loaded with the BPT firmware? TODO: boolean?
    public static final String EXTRA_FROM_BPT_DEVICE = "com.bpt.intent.extra.FROM_BPT_DEVICE";

    // Holds the Firmware.Event type (if applicable)
    public static final String EXTRA_BPT_EVENT_TYPE = "com.bpt.intent.extra.BPT_EVENT_TYPE";


    /********************************************************
     Intent permissions
     *******************************************************/

    public static final String PERMISSION_RECEIVE_EVENTS = Manifest.permission.RECEIVE_EVENTS;

    public static final String PERMISSION_RECEIVE_DEVICE_EVENTS
                                            = Manifest.permission.RECEIVE_DEVICE_EVENTS;

    public static final String PERMISSION_RUN_DEVICE_FUNCTION
                                            = Manifest.permission.RUN_DEVICE_FUNCTION;

}
