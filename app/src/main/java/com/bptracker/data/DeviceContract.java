package com.bptracker.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;


public class DeviceContract {


    public static final String CONTENT_AUTHORITY = "com.bptracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DEVICES = "devices";




    public static final class DeviceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DEVICES).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEVICES;

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


        public static Uri buildDeviceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getDeviceIdFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        // com.bptracker/devices/cloud_device_id/AFF21323123ACCBCEDFF
        public static String getCloudDeviceIdFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }


        public static Uri buildDevicesUri(){
            return CONTENT_URI;
        }
    }
}
