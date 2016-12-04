package com.bptracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bptracker.data.BptContract.DeviceEntry;
import com.bptracker.data.BptContract.DeviceFunctionCallEntry;
import com.bptracker.data.BptContract.DeviceEventEntry;

import io.particle.android.sdk.utils.TLog;


public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 13;

    static final String DATABASE_NAME = "bptracker.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //public cloneDatabase()

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_DEVICE_TABLE = "CREATE TABLE " + DeviceEntry.TABLE_NAME + " (" +
                DeviceEntry._ID + " INTEGER PRIMARY KEY, " +
                DeviceEntry.COLUMN_DEVICE_NAME + " TEXT UNIQUE NOT NULL, " +
                DeviceEntry.COLUMN_DEVICE_TYPE + " TEXT NOT NULL, " +
                DeviceEntry.COLUMN_CLOUD_DEVICE_ID + " TEXT UNIQUE NOT NULL, " +
                DeviceEntry.COLUMN_IS_CONNECTED + " INTEGER, " +
                DeviceEntry.COLUMN_SOFTWARE_NAME + " TEXT, " +
                DeviceEntry.COLUMN_SOFTWARE_VERSION + " TEXT, " +
                DeviceEntry.COLUMN_IS_ACTIVE + " INTEGER " +
                " );";
        sqLiteDatabase.execSQL(SQL_DEVICE_TABLE);


        final String SQL_DEVICE_FUNCTION = "CREATE TABLE " + DeviceFunctionCallEntry.TABLE_NAME + " (" +
                DeviceFunctionCallEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID + " TEXT NOT NULL, " +
                DeviceFunctionCallEntry.COLUMN_FUNCTION_NAME + " TEXT NOT NULL, " +
                DeviceFunctionCallEntry.COLUMN_FUNCTION_ARGS + " TEXT, " +
                DeviceFunctionCallEntry.COLUMN_FUNCTION_RETURN + " TEXT, " +
                DeviceFunctionCallEntry.COLUMN_PUBLISH_DATE + " INTEGER, " +
                DeviceFunctionCallEntry.COLUMN_EVENT_ID + " INTEGER, " +
                DeviceFunctionCallEntry.COLUMN_EVENT_DATA + " TEXT, " +
                "FOREIGN KEY (" + DeviceFunctionCallEntry.COLUMN_EVENT_ID + " ) " +
                "REFERENCES " + DeviceEventEntry.TABLE_NAME + " (" + DeviceEventEntry._ID + ") " +
                "FOREIGN KEY (" + DeviceFunctionCallEntry.COLUMN_CLOUD_DEVICE_ID + ") " +
                "REFERENCES " + DeviceEntry.TABLE_NAME + " (" + DeviceEntry.COLUMN_CLOUD_DEVICE_ID + ") );";
        sqLiteDatabase.execSQL(SQL_DEVICE_FUNCTION);


        final String SQL_DEVICE_EVENT = "CREATE TABLE " + DeviceEventEntry.TABLE_NAME + " (" +
                DeviceEventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DeviceEventEntry.COLUMN_CLOUD_DEVICE_ID + " TEXT NOT NULL, " +
                DeviceEventEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, " +
                DeviceEventEntry.COLUMN_EVENT_DATA + " TEXT, " +
                DeviceEventEntry.COLUMN_PUBLISH_DATE + " INTEGER, " +
                "FOREIGN KEY (" + DeviceEventEntry.COLUMN_CLOUD_DEVICE_ID + ") " +
                "REFERENCES " + DeviceEntry.TABLE_NAME + " (" + DeviceEntry.COLUMN_CLOUD_DEVICE_ID + ") );";
        sqLiteDatabase.execSQL(SQL_DEVICE_EVENT);



        final String SQL_VALID_BPT_EVENT = "CREATE TABLE valid_bpt_event (" +
                "event_id INTEGER PRIMARY KEY, " +
                "event_name TEXT UNIQUE NOT NULL, " +
                "is_active INTEGER);";

        final String SQL_VALID_BPT_EVENT_DESC = "CREATE TABLE valid_bpt_event_desc(" +
                "event_id INTEGER, " +
                "language TEXT, " +
                "short_description TEXT, " +
                "description TEXT, " +
                "PRIMARY KEY (event_id, language), " +
                "FOREIGN KEY (event_id) REFERENCES valid_bpt_event (event_id) );";

        sqLiteDatabase.execSQL(SQL_VALID_BPT_EVENT);
        sqLiteDatabase.execSQL(SQL_VALID_BPT_EVENT_DESC);

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (1, 'EVENT_STATE_CHANGE', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(1, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (2, 'EVENT_REQUEST_GPS', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(2, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (3, 'EVENT_BATTERY_LOW', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(3, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (4, 'EVENT_NO_GPS_SIGNAL', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(4, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (5, 'EVENT_SOFT_PANIC', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(5, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (6, 'EVENT_PANIC', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(6, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (7, 'EVENT_PROBE_CONTROLLER', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(7, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (8, 'EVENT_TEST', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(8, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (9, 'EVENT_SERIAL_COMMAND', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(9, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (10, 'EVENT_ERROR', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(10, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event VALUES (11, 'EVENT_HARDWARE_FAULT', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_event_desc VALUES(11, 'en', '', '');");



        final String SQL_VALID_BPT_STATE = "CREATE TABLE valid_bpt_state (" +
                "state_id INTEGER PRIMARY KEY, " +
                "state_name TEXT UNIQUE NOT NULL, " +
                "is_internal INTEGER, " +
                "is_active INTEGER);";

        final String SQL_VALID_BPT_STATE_DESC = "CREATE TABLE valid_bpt_state_desc(" +
                "state_id INTEGER, " +
                "language TEXT, " +
                "short_description TEXT, " +
                "description TEXT, " +
                "PRIMARY KEY (state_id, language), " +
                "FOREIGN KEY (state_id) REFERENCES valid_bpt_state (state_id) );";

        sqLiteDatabase.execSQL(SQL_VALID_BPT_STATE);
        sqLiteDatabase.execSQL(SQL_VALID_BPT_STATE_DESC);

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (1, 'STATE_OFFLINE', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(1, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (2, 'STATE_DEACTIVATED', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(2, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (3, 'STATE_RESET', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(3, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (4, 'STATE_ARMED', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(4, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (5, 'STATE_DISARMED', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(5, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (6, 'STATE_PANIC', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(6, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (7, 'STATE_PAUSED', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(7, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (8, 'STATE_RESUMED', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(8, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (9, 'STATE_ACTIVATED', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(9, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (10, 'STATE_SOFT_PANIC', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(10, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (11, 'STATE_ONLINE_WAIT', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(11, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (12, 'STATE_RESET_WAIT', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(12, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state VALUES (13, 'STATE_SLEEP', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_state_desc VALUES(13, 'en', '', '');");



        final String SQL_VALID_BPT_FUNCTION = "CREATE TABLE valid_bpt_function (" +
                "function_name TEXT PRIMARY KEY, " +
                "callable INTEGER, " + // can the function be called?
                "is_active INTEGER);";

        final String SQL_VALID_BPT_FUNCTION_DESC = "CREATE TABLE valid_bpt_function_desc (" +
                "function_name TEXT, " +
                "language TEXT, " +
                "short_description TEXT, " +
                "description TEXT, " +
                "PRIMARY KEY (function_name, language), " +
                "FOREIGN KEY (function_name) REFERENCES valid_bpt_function (function_name) );";

        sqLiteDatabase.execSQL(SQL_VALID_BPT_FUNCTION);
        sqLiteDatabase.execSQL(SQL_VALID_BPT_FUNCTION_DESC);

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:event', 0, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:event', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:state', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:state', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:gps', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:gps', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:status', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:status', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:diag', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:diag', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:register', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:register', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:ack', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:ack', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:probe', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:probe', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:test', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:test', 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function VALUES ('bpt:reset', 1, 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_function_desc VALUES('bpt:reset', 'en', '', '');");


        final String SQL_VALID_BPT_PROP = "CREATE TABLE valid_bpt_prop ( " +
                "prop_id INTEGER PRIMARY KEY, " +
                "prop_name TEXT UNIQUE NOT NULL, " +
                "is_active INTEGER);";

        final String SQL_VALID_BPT_PROP_DESC = "CREATE TABLE valid_bpt_prop_desc (" +
                "prop_id INTEGER, " +
                "language TEXT, " +
                "short_description TEXT, " +
                "description TEXT, " +
                "PRIMARY KEY (prop_id, language), " +
                "FOREIGN KEY (prop_id) REFERENCES valid_bpt_prop (prop_id) );";

        sqLiteDatabase.execSQL(SQL_VALID_BPT_PROP);
        sqLiteDatabase.execSQL(SQL_VALID_BPT_PROP_DESC);

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop VALUES (1, 'PROP_CONTROLLER_MODE', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop_desc VALUES(1, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop VALUES (2, 'PROP_GEOFENCE_RADIUS', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop_desc VALUES(2, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop VALUES (3, 'PROP_ACCEL_THRESHOLD', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop_desc VALUES(3, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop VALUES (4, 'PROP_ACK_ENABLED', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop_desc VALUES(4, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop VALUES (5, 'PROP_SLEEP_WAKEUP_STANDBY', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_prop_desc VALUES(5, 'en', '', '');");

        // TODO: will this table be used anywhere?
        final String SQL_VALID_BPT_TEST_INPUT = "CREATE TABLE valid_bpt_test_input ( " +
                "test_input_id INTEGER PRIMARY KEY, " +
                "test_input_name TEXT UNIQUE NOT NULL, " +
                "is_active INTEGER);";

        final String SQL_VALID_BPT_TEST_INPUT_DESC = "CREATE TABLE valid_bpt_test_input_desc (" +
                "test_input_id INTEGER, " +
                "language TEXT, " +
                "short_description TEXT, " +
                "description TEXT, " +
                "PRIMARY KEY (test_input_id, language), " +
                "FOREIGN KEY (test_input_id) REFERENCES valid_bpt_test_input (test_input_id) );";

        sqLiteDatabase.execSQL(SQL_VALID_BPT_TEST_INPUT);
        sqLiteDatabase.execSQL(SQL_VALID_BPT_TEST_INPUT_DESC);

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input VALUES (1, 'TEST_INPUT_GPS', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input_desc VALUES(1, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input VALUES (2, 'TEST_INPUT_AUTO_GPS', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input_desc VALUES(2, 'en', '', '');");

        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input VALUES (3, 'TEST_INPUT_ACCEL_INT', 1);");
        sqLiteDatabase.execSQL("INSERT INTO valid_bpt_test_input_desc VALUES(3, 'en', '', '');");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        //TODO: remove later
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BptContract.DeviceEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BptContract.DeviceFunctionCallEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BptContract.DeviceEventEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_test_input_desc");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_test_input");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_prop_desc");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_prop");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_function_desc");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_function");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_state_desc");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_state");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_event_desc");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS valid_bpt_event");

        onCreate(sqLiteDatabase);
    }



    private static final TLog _log = TLog.get(DatabaseHelper.class);
}
