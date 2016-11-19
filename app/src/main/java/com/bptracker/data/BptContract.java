package com.bptracker.data;

/**
 * Author: Derek Benda
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

public class BptContract {


    public static final String CONTENT_AUTHORITY = "com.bptracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DEVICES = "devices";
    public static final String PATH_EVENTS = "events";
    public static final String PATH_FUNCTION_CALLS = "function-calls";


    /** see also valid_bpt_function **/
    // TODO: test for integrity
    public static final String EVENT_BPT_EVENT      = "btp:event";
    public static final String EVENT_BPT_STATE      = "btp:state";
    public static final String EVENT_BPT_GPS        = "btp:gps";
    public static final String EVENT_BPT_STATUS     = "btp:status";
    public static final String EVENT_BPT_DIAG       = "btp:diag";
    public static final String EVENT_BPT_REGISTER   = "btp:register";
    public static final String EVENT_BPT_ACK        = "btp:ack";
    public static final String EVENT_BPT_PROBE      = "btp:probe";
    public static final String EVENT_BPT_TEST       = "bpt:test";
    public static final String EVENT_BPT_RESET      = "btp:reset";


    // See all valid_bpt_state
    // TODO: test for integrity
    public static final int STATE_OFFLINE               = 1;
    public static final int STATE_DEACTIVATED           = 2;
    public static final int STATE_RESET                 = 3;
    public static final int STATE_ARMED                 = 4;
    public static final int STATE_DISARMED              = 5;
    public static final int STATE_PANIC                 = 6;
    public static final int STATE_PAUSED                = 7;
    public static final int STATE_RESUMED               = 8;
    public static final int STATE_INTERNAL_ACTIVATED    = 9;
    public static final int STATE_INTERNAL_SOFT_PANIC   = 10;
    public static final int STATE_INTERNAL_ONLINE_WAIT  = 11;
    public static final int STATE_INTERNAL_SLEEP        = 12;


    public static final class DeviceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DEVICES).build();


        // multiple item MIME-type
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEVICES;

        // single item MIME-type
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEVICES;


        public static final String TABLE_NAME = "device";


        public static final String COLUMN_DEVICE_NAME = "device_name";
        public static final String COLUMN_DEVICE_TYPE = "device_type";
        public static final String COLUMN_CLOUD_DEVICE_ID = "device_id";
        public static final String COLUMN_IS_CONNECTED = "is_connected"; // 0 = false, 1 = true
        public static final String COLUMN_SOFTWARE_VERSION = "software_version";
        public static final String COLUMN_SOFTWARE_NAME = "software_name";
        public static final String COLUMN_IS_ACTIVE = "is_active"; // 0 = false, 1 = true




        public static long getDeviceIdFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        // com.bptracker/devices/cloud-device-id/AFF21323123ACCBCEDFF
        public static String getCloudDeviceIdFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }

        public static Uri buildDeviceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        //devices
        public static Uri buildDeviceUri(){
            return CONTENT_URI;
        }
    }


    // logs functions called by this application
    public static final class DeviceFunctionCallEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FUNCTION_CALLS).build();


        // multiple item MIME-type
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FUNCTION_CALLS;

        // single item MIME-type
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FUNCTION_CALLS;


        public static final String TABLE_NAME = "device_function_call";

        public static final String COLUMN_CLOUD_DEVICE_ID = "device_id";
        public static final String COLUMN_FUNCTION_NAME = "function_name";
        public static final String COLUMN_FUNCTION_ARGS = "function_args";
        public static final String COLUMN_FUNCTION_RETURN = "function_return";
        public static final String COLUMN_PUBLISH_DATE = "published";


        //devices/*/bpt-function-calls
        public static Uri buildBptFunctionCallUri(String cloudDeviceId) {
            Uri uri = BASE_CONTENT_URI.buildUpon().appendPath("devices")
                    .appendPath(cloudDeviceId).appendPath("bpt-function-calls").build();

            return uri;
        }


        //devices/*/bpt-function-calls/#
        public static String getCloudDeviceIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        //devices/*/bpt-function-calls/#
        public static Uri buildBptFunctionCallUri(String cloudDeviceId, long functionCallId) {
            Uri uri = buildBptFunctionCallUri(cloudDeviceId);
            return ContentUris.withAppendedId(uri, functionCallId);
        }

        //function-calls/#"
        public static Uri buildFunctionCallUri(long functionCallId) {
            return ContentUris.withAppendedId(CONTENT_URI, functionCallId);
        }

        //function-calls
        public static Uri buildFunctionCallUri(){
            return CONTENT_URI;
        }


        //devices/*/bpt-function-calls/#
        //function-calls/#
        public static long getIdFromUri(Uri uri){

            String id = uri.getLastPathSegment();  //TODO: error prone
            if(StringUtils.isNumeric(id)) {
                return Long.parseLong(id);
            }

            return -1;
        }
    }


    // stores private device events
    public static final class DeviceEventEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();


        // multiple item MIME-type
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;

        // single item MIME-type
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVENTS;


        public static final String TABLE_NAME = "device_event";

        public static final String COLUMN_CLOUD_DEVICE_ID = "device_id";
        public static final String COLUMN_EVENT_NAME = "event_name";
        public static final String COLUMN_EVENT_DATA = "event_data";
        public static final String COLUMN_PUBLISH_DATE = "published";


        //devices/*/bpt-events
        public static String getCloudDeviceIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        //devices/*/bpt-events
        public static Uri buildBptDeviceEventUri(String cloudDeviceId) {

            Uri uri = BASE_CONTENT_URI.buildUpon().appendPath("devices")
                    .appendPath(cloudDeviceId).appendPath("bpt-events").build();

            return uri;
        }

        //devices/*/bpt-events/#
        public static Uri buildBptDeviceEventUri(String cloudDeviceId, long deviceEventId) {

            Uri uri = buildBptDeviceEventUri(cloudDeviceId);
            return ContentUris.withAppendedId(uri, deviceEventId);
        }

        //devices/#
        public static Uri buildDeviceEventUri(long deviceEventId) {
            return ContentUris.withAppendedId(CONTENT_URI, deviceEventId);
        }

        //events
        public static Uri buildDeviceEventUri(){
            return CONTENT_URI;
        }

        //devices/*/bpt-events/#
        //events/#
        public static long getIdFromUri(Uri uri){

            String id = uri.getLastPathSegment();  //TODO: error prone
            if(StringUtils.isNumeric(id)) {
                return Long.parseLong(id);
            }

            return -1;
        }
    }
}
