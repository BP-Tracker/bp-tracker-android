package com.bptracker;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.bptracker.data.BptContract;

/**
 * Author: Derek Benda
 */
public class BptProvider extends AndroidTestCase {

    public void assetRecordsExistInProvider() {


        Cursor cursor = mContext.getContentResolver().query(
                BptContract.DeviceEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: No records returned", 4, cursor.getCount());
        cursor.close();
    }
}
