package com.bptracker.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.data.BptContract.DeviceEntry;
import com.bptracker.data.BptContract.DeviceEventEntry;
import com.bptracker.data.BptContract.DeviceFunctionCallEntry;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */
public class BptProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;
    private static final UriMatcher uriMatcher;
    private static final SQLiteQueryBuilder deviceQueryBuilder;
    private static final SQLiteQueryBuilder bptEventQueryBuilder;
    private static final SQLiteQueryBuilder bptFunctionCallQueryBuilder;


    /* arbitrary ints for URI matcher */
    private static final int MATCHER_DEVICES = 100;
    private static final int MATCHER_BY_DEVICE_ID = 101;
    private static final int MATCHER_BY_CLOUD_DEVICE_ID = 102;

    private static final int MATCHER_EVENTS = 103;
    private static final int MATCHER_FUNCTION_CALLS = 104;
    private static final int MATCHER_BPT_EVENTS = 105;
    private static final int MATCHER_BPT_EVENTS_ID = 106;
    private static final int MATCHER_BPT_FUNCTION_CALLS = 107;
    private static final int MATCHER_BPT_FUNCTION_CALLS_ID = 108;

    private static final int MATCHER_BY_EVENT_ID = 109;
    private static final int MATCHER_BY_FUNCTION_CALLS_ID = 110;


    static{
        deviceQueryBuilder = new SQLiteQueryBuilder();
        deviceQueryBuilder.setTables(DeviceEntry.TABLE_NAME);

        bptEventQueryBuilder = new SQLiteQueryBuilder();
        bptEventQueryBuilder.setTables(
                DeviceEventEntry.TABLE_NAME + " INNER JOIN valid_bpt_function" +
                        " ON " + DeviceEventEntry.TABLE_NAME + "." + DeviceEventEntry.COLUMN_EVENT_NAME +
                        " = valid_bpt_function.function_name");

        bptFunctionCallQueryBuilder = new SQLiteQueryBuilder();
        bptFunctionCallQueryBuilder.setTables(
                DeviceFunctionCallEntry.TABLE_NAME + " INNER JOIN valid_bpt_function" +
                        " ON " + DeviceFunctionCallEntry.TABLE_NAME + "." +
                        DeviceFunctionCallEntry.COLUMN_FUNCTION_NAME +
                        " = valid_bpt_function.function_name");


        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String auth = BptContract.CONTENT_AUTHORITY;

         //devices                         (all devices)
         //events                          (all events)
         //function-calls                  (all function calls)

         //devices/cloud-device-id/*       (device by cloud device id)
         //devices/*/bpt-events            (bpt events by cloud device id)
         //devices/*/bpt-events/*          (specific bpt event by cloud device id)
         //devices/*/bpt-function-calls    (all bpt function calls by cloud device id)
         //devices/*/bpt-function-calls/#  (specific btp function call)

         //devices/*                       (device by internal id)
         //events/*                        (event by id)
         //function-calls/*                (function call by id)

        uriMatcher.addURI(auth, "devices", MATCHER_DEVICES);
        uriMatcher.addURI(auth, "events", MATCHER_EVENTS);
        uriMatcher.addURI(auth, "function-calls", MATCHER_FUNCTION_CALLS);

        uriMatcher.addURI(auth, "devices/cloud-device-id/*", MATCHER_BY_CLOUD_DEVICE_ID);
        uriMatcher.addURI(auth, "devices/*/bpt-events", MATCHER_BPT_EVENTS);
        uriMatcher.addURI(auth, "devices/*/bpt-events/*", MATCHER_BPT_EVENTS_ID);
        uriMatcher.addURI(auth, "devices/*/bpt-function-calls", MATCHER_BPT_FUNCTION_CALLS);
        uriMatcher.addURI(auth, "devices/*/bpt-function-calls/*", MATCHER_BPT_FUNCTION_CALLS_ID);

        uriMatcher.addURI(auth, "devices/*", MATCHER_BY_DEVICE_ID);
        uriMatcher.addURI(auth, "events/*", MATCHER_BY_EVENT_ID);
        uriMatcher.addURI(auth, "function-calls/*", MATCHER_BY_FUNCTION_CALLS_ID);
    }



    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(this.getContext());
        return true;
    }


    public Cursor query(Uri uri, String[] projection) {
        return query(uri, projection, null, null, null);
    }


    // for MATCHER_BPT_FUNCTION_CALLS_ID and MATCHER_BPT_FUNCTION_CALLS URIs
    // and MATCHER_BY_FUNCTION_CALLS_ID
    @Nullable
    private Cursor queryBptFunctionCall(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder){

        int match = uriMatcher.match(uri);

        ArrayList<String> sArgs = selectionArgs != null
                ? new ArrayList<String>(Arrays.asList(selectionArgs)) : new ArrayList<String>();

        StringBuffer buffer = new StringBuffer(selection != null ? selection : "");

        if(!TextUtils.isEmpty(selection)){
            buffer.append(" and ");
        }


        if(match == MATCHER_BPT_FUNCTION_CALLS_ID){
            long id = DeviceFunctionCallEntry.getIdFromUri(uri);

            if(id < 0){
               throw new UnsupportedOperationException("URI is missing function call id:  " + uri);
            }

            String cloudDeviceId = DeviceFunctionCallEntry.getCloudDeviceIdFromUri(uri);
            if ( TextUtils.isEmpty( cloudDeviceId ) ) {
                throw new UnsupportedOperationException("Cannot find cloud device ID from uri: " + uri);
            }

            sArgs.add(Long.toString(id));
            sArgs.add(cloudDeviceId);

            buffer.append(DeviceFunctionCallEntry.TABLE_NAME + "."
                    + DeviceFunctionCallEntry._ID + " = ? and " +
                    DeviceFunctionCallEntry.TABLE_NAME +
                            "." + DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID + " = ? ");

        }else if(match == MATCHER_BPT_FUNCTION_CALLS){

            String cloudDeviceId = DeviceFunctionCallEntry.getCloudDeviceIdFromUri(uri);
            if ( TextUtils.isEmpty( cloudDeviceId ) ) {
                throw new UnsupportedOperationException("Cannot find cloud device ID from uri: " + uri);
            }

            sArgs.add(cloudDeviceId);
            buffer.append(DeviceFunctionCallEntry.TABLE_NAME +
                    "." + DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID + " = ? ");

        }else if(match == MATCHER_BY_FUNCTION_CALLS_ID){

            long id = DeviceFunctionCallEntry.getIdFromUri(uri);

            sArgs.add(Long.toString(id));
            buffer.append(DeviceFunctionCallEntry.TABLE_NAME + "."
                    + DeviceFunctionCallEntry._ID + " = ? ");

        }else{
            throw new UnsupportedOperationException("URI not supported: " + uri);
        }


        Cursor c = bptFunctionCallQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                buffer.toString(),
                sArgs.toArray(new String[sArgs.size()]),
                null,
                null,
                sortOrder
        );
        return c;
    }


    //NB: selection and selectionArgs are not used so they are ignored
    private Cursor queryDevice(Uri uri, String[] projection, String sortOrder){

        Cursor c = null;
        int match = uriMatcher.match(uri);

        if(match != MATCHER_BY_CLOUD_DEVICE_ID && match != MATCHER_BY_DEVICE_ID){
            throw new UnsupportedOperationException("URI not supported: " + uri);
        }

        if(match == MATCHER_BY_CLOUD_DEVICE_ID){

            String cloudDeviceId = DeviceEntry.getCloudDeviceIdFromUri(uri);

            if (cloudDeviceId == null || cloudDeviceId.length() <= 0) {
                throw new UnsupportedOperationException(
                        "Cannot find cloud device ID from uri:" + uri);
            }


            c = deviceQueryBuilder.query(databaseHelper.getReadableDatabase(),
                    projection,
                    DeviceEntry.TABLE_NAME + "." + DeviceEntry.COLUMN_CLOUD_DEVICE_ID + " = ?",
                    new String[]{cloudDeviceId},
                    null,
                    null,
                    sortOrder
            );

            return c;
        }

        // MATCHER_BY_DEVICE_ID

        long deviceId = DeviceEntry.getDeviceIdFromUri(uri);
        c = deviceQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                DeviceEntry.TABLE_NAME + "." + DeviceEntry._ID + " = ?",
                new String[]{Long.toString(deviceId)},
                null,
                null,
                sortOrder
        );
        return c;
    }

    private Cursor queryBptEvent(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder){

        int match = uriMatcher.match(uri);

        if(match != MATCHER_BPT_EVENTS && match != MATCHER_BPT_EVENTS_ID){
            throw new UnsupportedOperationException("URI not supported: " + uri);
        }


        String cloudDeviceId = DeviceEventEntry.getCloudDeviceIdFromUri(uri);
        if ( TextUtils.isEmpty( cloudDeviceId ) ) {
            throw new UnsupportedOperationException(
                    "Cannot find cloud device ID from uri:" + uri);
        }

        ArrayList<String> sArgs = selectionArgs != null
                ? new ArrayList<String>(Arrays.asList(selectionArgs)) : new ArrayList<String>();



        StringBuffer buffer = new StringBuffer(selection);

        if(!TextUtils.isEmpty(selection)){
            buffer.append(" and ");
        }


        if(match == MATCHER_BPT_EVENTS_ID){
            long id = DeviceEventEntry.getIdFromUri(uri);
            if(id < 0){
                throw new UnsupportedOperationException("URI is missing function call id:  " + uri);
            }

            sArgs.add(Long.toString(id));

            buffer.append(" " + DeviceEventEntry.TABLE_NAME + "."
                    + DeviceEventEntry._ID + " = ? and");

        }

        sArgs.add(cloudDeviceId);
        buffer.append(DeviceEventEntry.TABLE_NAME +
                "." + DeviceEventEntry.COLUMN_CLOUD_DEVICE_ID + " = ? ");


        Cursor c = bptEventQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                buffer.toString(),
                sArgs.toArray(new String[sArgs.size()]),
                null,
                null,
                sortOrder
        );

        return c;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c;
        int match = uriMatcher.match(uri);

        _log.d("query on " + uri + " [match=" + match + "]");

        String tableName = getTableNameFromUri(uri);
        if(tableName != null){ // simple URIs

            c = databaseHelper.getReadableDatabase().query(
                    tableName,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }


        if (match == MATCHER_BPT_FUNCTION_CALLS
                || match == MATCHER_BPT_FUNCTION_CALLS_ID
                || match == MATCHER_BY_FUNCTION_CALLS_ID) {

           c = queryBptFunctionCall(uri, projection, selection, selectionArgs, sortOrder);

        }else if(match == MATCHER_BPT_EVENTS
                || match == MATCHER_BPT_EVENTS_ID) {

            c = queryBptEvent(uri, projection, selection, selectionArgs, sortOrder);

        }else if(match == MATCHER_BY_DEVICE_ID
                || match == MATCHER_BY_CLOUD_DEVICE_ID){

            c = queryDevice(uri, projection, sortOrder);

        }else {
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
                return DeviceEntry.CONTENT_TYPE;
            case MATCHER_BY_DEVICE_ID:
                return DeviceEntry.CONTENT_ITEM_TYPE;
            case MATCHER_BY_CLOUD_DEVICE_ID:
                return DeviceEntry.CONTENT_ITEM_TYPE;
            case MATCHER_EVENTS:
                return DeviceEventEntry.CONTENT_TYPE;
            case MATCHER_FUNCTION_CALLS:
                return DeviceFunctionCallEntry.CONTENT_TYPE;
            case MATCHER_BPT_EVENTS:
                return DeviceEventEntry.CONTENT_TYPE;
            case MATCHER_BPT_EVENTS_ID:
                return DeviceEventEntry.CONTENT_ITEM_TYPE;
            case MATCHER_BPT_FUNCTION_CALLS:
                return DeviceFunctionCallEntry.CONTENT_TYPE;
            case MATCHER_BPT_FUNCTION_CALLS_ID:
                return DeviceFunctionCallEntry.CONTENT_ITEM_TYPE;
            case MATCHER_BY_EVENT_ID:
                return DeviceEventEntry.CONTENT_ITEM_TYPE;
            case MATCHER_BY_FUNCTION_CALLS_ID:
                return DeviceFunctionCallEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Cannot getType because uri is unknown: " + uri);
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

                long _id = db.insert(DeviceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DeviceEntry.buildDeviceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MATCHER_EVENTS : {
                long _id = db.insert(DeviceEventEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DeviceEventEntry.buildDeviceEventUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MATCHER_FUNCTION_CALLS: {
                long _id = db.insert(DeviceFunctionCallEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DeviceFunctionCallEntry.buildFunctionCallUri(_id);
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
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        String tableName = getTableNameFromUri(uri);

        if(tableName != null){
            rowsDeleted = db.delete(tableName, selection, selectionArgs);
        }else{
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    public int update(Uri uri, ContentValues values) {  // TODO: useful?
        return update(uri, values, null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int m = uriMatcher.match(uri);
        int rowsUpdated;

        String tableName = getTableNameFromUri(uri);

        if(tableName != null){
            rowsUpdated = db.update(tableName, values, selection, selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }


        switch (m) {
            case MATCHER_BY_FUNCTION_CALLS_ID:

                long id = DeviceFunctionCallEntry.getIdFromUri(uri);

                rowsUpdated = db.update(DeviceFunctionCallEntry.TABLE_NAME, values,
                        DeviceFunctionCallEntry.TABLE_NAME + "."
                                + DeviceFunctionCallEntry._ID + " = ?",
                                new String[]{ Long.toString(id)});

                break;
            case MATCHER_BY_DEVICE_ID:

                long deviceId = DeviceEntry.getDeviceIdFromUri(uri);

                rowsUpdated = db.update(DeviceEntry.TABLE_NAME, values, DeviceEntry.TABLE_NAME +
                                "." + DeviceEntry._ID + " = ?",
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



    // NB: only returns the name of simple URIs
    @Nullable
    private String getTableNameFromUri(Uri uri) {
        final int m = uriMatcher.match(uri);
        String tableName = null;

        switch (m) {
            case MATCHER_DEVICES:
                tableName = DeviceEntry.TABLE_NAME;
                break;
            case MATCHER_EVENTS:
                tableName = DeviceEventEntry.TABLE_NAME;
                break;
            case MATCHER_FUNCTION_CALLS:
                tableName = DeviceFunctionCallEntry.TABLE_NAME;
                break;

        }
        return tableName;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String tableName = getTableNameFromUri(uri);

        if(tableName == null){
            return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
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


    private static final TLog _log = TLog.get(BptProvider.class);
}








