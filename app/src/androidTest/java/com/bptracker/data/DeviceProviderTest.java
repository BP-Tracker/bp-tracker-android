package com.bptracker.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;


public class DeviceProviderTest extends ProviderTestCase2<DeviceProvider> {

    private static final String LOG_TAG = DeviceProviderTest.class.getSimpleName();

    MockContentResolver contentResolver;

    public DeviceProviderTest() {
        super(DeviceProvider.class, "com.bptracker");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.d(LOG_TAG, "setUp: ");
        contentResolver = getMockContentResolver();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log.d(LOG_TAG, "tearDown:");
    }


    public void testRecordInProvider() {
        Log.d(LOG_TAG, "testRecordsExistInProvider");

        ContentValues c = createDeviceValues();

        Uri deviceUri = DeviceContract.DeviceEntry.CONTENT_URI;
        Uri recordUri = contentResolver.insert(deviceUri, c);

        Cursor cursor = contentResolver.query(
                deviceUri,
                null,
                null,
                null,
                null
        );
        assertNotNull(cursor);
        assertEquals("Error: No record returned", 1, cursor.getCount());
        cursor.close();
    }

    static ContentValues createDeviceValues() {
        ContentValues v = new ContentValues();
        //weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        v.put(DeviceContract.DeviceEntry.COLUMN_DEVICE_TYPE, "ELECTRON");
        v.put(DeviceContract.DeviceEntry.COLUMN_DEVICE_NAME, "A NAME");
        v.put(DeviceContract.DeviceEntry.COLUMN_IS_ACTIVE, 1);
        v.put(DeviceContract.DeviceEntry.COLUMN_IS_CONNECTED, 0);
        v.put(DeviceContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID, "AADDE34234");

        return v;
    }

}