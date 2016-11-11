package com.bptracker.data;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothClass;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */
public class DeviceProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;
    private static final UriMatcher uriMatcher;
    private static final SQLiteQueryBuilder queryBuilder;


    /* arbitrary ints for URI matcher */
    private static final int MATCHER_DEVICES = 100;
    private static final int MATCHER_BY_DEVICE_ID = 101;
    private static final int MATCHER_BY_CLOUD_DEVICE_ID = 102;

    static{
        queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DeviceContract.DeviceEntry.TABLE_NAME);


        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String auth = DeviceContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        uriMatcher.addURI(auth, DeviceContract.PATH_DEVICES, MATCHER_DEVICES);
        uriMatcher.addURI(auth, DeviceContract.PATH_DEVICES + "/cloud_device_id/*",
                MATCHER_BY_CLOUD_DEVICE_ID);
        uriMatcher.addURI(auth,  DeviceContract.PATH_DEVICES + "/*", MATCHER_BY_DEVICE_ID);
    }



    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(this.getContext());
        return true;
    }


    public Cursor query(Uri uri, String[] projection) {
        return query(uri, projection, null, null, null);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c;

        int match = uriMatcher.match(uri);
        switch (match) {
            case MATCHER_DEVICES:

                c = databaseHelper.getReadableDatabase().query(
                        DeviceContract.DeviceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MATCHER_BY_DEVICE_ID:

                long deviceId = DeviceContract.DeviceEntry.getDeviceIdFromUri(uri);
                c = queryBuilder.query(databaseHelper.getReadableDatabase(),
                        projection,
                        DeviceContract.DeviceEntry.TABLE_NAME +
                                "." + DeviceContract.DeviceEntry._ID + " = ?",
                        new String[]{ Long.toString(deviceId) },
                        null,
                        null,
                        sortOrder
                );
            case MATCHER_BY_CLOUD_DEVICE_ID:

                String cloudDeviceId = DeviceContract.DeviceEntry.getCloudDeviceIdFromUri(uri);
                if (cloudDeviceId == null || cloudDeviceId.length() <= 0) {
                    _log.w("Cannot find cloud device ID from uri: " + uri.toString());
                }

                c = queryBuilder.query(databaseHelper.getReadableDatabase(),
                        projection,
                        DeviceContract.DeviceEntry.TABLE_NAME +
                                "." + DeviceContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID + " = ?",
                        new String[]{ cloudDeviceId },
                        null,
                        null,
                        sortOrder
                );

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match) {
            case MATCHER_DEVICES:
                return DeviceContract.DeviceEntry.CONTENT_TYPE;
            case MATCHER_BY_DEVICE_ID:
                return DeviceContract.DeviceEntry.CONTENT_ITEM_TYPE;
            case MATCHER_BY_CLOUD_DEVICE_ID:
                return DeviceContract.DeviceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int m = uriMatcher.match(uri);
        Uri returnUri;

        switch (m) {
            case MATCHER_DEVICES: {

                long _id = db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DeviceContract.DeviceEntry.buildDeviceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int m = uriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (m) {
            case MATCHER_DEVICES:
                rowsDeleted = db.delete(
                        DeviceContract.DeviceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // TODO: useful?
    public int update(Uri uri, ContentValues values) {
        return update(uri, values, null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int m = uriMatcher.match(uri);
        int rowsUpdated;

        switch (m) {
            case MATCHER_DEVICES:

                rowsUpdated = db.update(DeviceContract.DeviceEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case MATCHER_BY_DEVICE_ID:

                long deviceId = DeviceContract.DeviceEntry.getDeviceIdFromUri(uri);

                rowsUpdated = db.update(DeviceContract.DeviceEntry.TABLE_NAME, values,
                    DeviceContract.DeviceEntry.TABLE_NAME +
                        "." + DeviceContract.DeviceEntry._ID + " = ?",
                        new String[]{ Long.toString(deviceId) });

                break;
            default:
                _log.e("Unknown URI for update");
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int m = uriMatcher.match(uri);

        switch (m) {
            case MATCHER_DEVICES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        databaseHelper.close();
        super.shutdown();
    }


    private static final TLog _log = TLog.get(DeviceProvider.class);
}







































/*
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
 */
