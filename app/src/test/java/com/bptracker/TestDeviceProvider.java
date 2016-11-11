package com.bptracker;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.bptracker.data.DeviceContract;

/**
 * Author: Derek Benda
 */
public class TestDeviceProvider extends AndroidTestCase {

    public void assetRecordsExistInProvider() {


        Cursor cursor = mContext.getContentResolver().query(
                DeviceContract.DeviceEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: No records returned", 4, cursor.getCount());
        cursor.close();
    }
}
