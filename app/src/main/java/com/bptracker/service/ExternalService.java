package com.bptracker.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.data.BptContract;
import com.bptracker.firmware.core.BptApi;
import com.bptracker.firmware.core.Function;
import com.bptracker.firmware.core.SimpleFunction;

import java.util.ArrayList;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 *
 * An external service to retrieve devices and run functions on the particle.io cloud
 * Requires permission IntentUtil.PERMISSION_RUN_DEVICE_FUNCTION
 *
 * See com.bptracker.test.examples.ExternalServiceActivity for an example.
 *
 *
 * TODO: more documentation
 * TODO: best place to stop service
 */
public class ExternalService extends Service {

    /** Get a list of cached devices.
     *
     * Returns a MSG_RETURN_DEVICES or MSG_RETURN_ERROR message
    **/
    public static final int MSG_CALL_GET_DEVICES = 1100;

    /**
     * Invoke a function call on a device
     *
     * Can return MSG_RETURN_FUNCTION, MSG_RETURN_FUNCTION_EVENT and
     * MSG_RETURN_ERROR messages
     */
    public static final int MSG_CALL_FUNCTION = 1200;

    /** Return message types **/
    public static final int MSG_RETURN_DEVICES = 2100;
    public static final int MSG_RETURN_FUNCTION = 2200;
    public static final int MSG_RETURN_SERVICE_ERROR = 3000;

    public static final int STATUS_OK = 1;
    public static final int STATUS_ERROR = 3;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * Process requests from clients
     */
    private class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            _log.v("handleMessage called - " + msg.what);

            try{
                switch (msg.what) {
                    case MSG_CALL_GET_DEVICES: {

                        callGetDevices(msg.getData(), msg.replyTo);
                        break;

                    } case MSG_CALL_FUNCTION: {

                        callFunction(msg.getData(), msg.replyTo);
                        break;

                    } default:
                        super.handleMessage(msg);
                }
            }catch (RemoteException e) {
                _log.e("Cannot return reply - " + e.getMessage());
            }

            super.handleMessage(msg);
        }
    }


    /**
     * Adds active devices into the bundle as an ArrayList of ContentValues
     * Each ContentValues object will have the following keys:
     *  deviceId    The cloud device
     *  deviceName  The device name
     *  deviceType  The type of device (ex. PHOTON, CORE)
     * @param bundle
     * @param replyTo
     */
    private void callGetDevices(Bundle bundle, Messenger replyTo) throws RemoteException {

        Message ret = Message.obtain(null, MSG_RETURN_DEVICES);
        Bundle retBundle = ret.getData();


        Uri uri = BptContract.DeviceEntry.buildDeviceUri();
        Cursor c = getContentResolver().query(uri, DEVICE_COLUMNS,
                BptContract.DeviceEntry.COLUMN_IS_ACTIVE + " = ?",
                new String[]{ "1" }, null);

        ArrayList<ContentValues> devices = new ArrayList<>(c.getCount());

        while (c.moveToNext()) {
            ContentValues v = new ContentValues();
            v.put("deviceId", c.getString(COL_CLOUD_DEVICE_ID) );
            v.put("deviceName", c.getString(COL_DEVICE_NAME));
            v.put("deviceType", c.getString(COL_DEVICE_TYPE));

            devices.add(v);
        }

        c.close();

        retBundle.putParcelableArrayList("devices", devices);
        retBundle.putInt("statusFor", MSG_CALL_GET_DEVICES);
        retBundle.putInt("status", STATUS_OK);
        retBundle.putString("statusMsg", "OK");

        replyTo.send(ret);
    }

    private void callFunction(Bundle bundle, Messenger replyTo) throws RemoteException {

        String deviceId = bundle.getString("deviceId");
        String func = bundle.getString("deviceFunction");
        String args = bundle.getString("deviceArgs");
        String event = bundle.getString("deviceEvent");

        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(func)) {

            returnErrorMessage(replyTo, MSG_CALL_FUNCTION,
                    "deviceId and deviceFunction arguments are required to call a function" );

            return;
        }

        boolean expectsEvent = !TextUtils.isEmpty(event);

        SimpleFunction f = new SimpleFunction(func, deviceId, expectsEvent);

        if(!TextUtils.isEmpty(args)){
            f.addArgument(1, args);
        }

        BptApi.call(f, new FunctionResultCallback(bundle, replyTo) );
    }

    private static class FunctionResultCallback implements BptApi.ResultCallback {

        private Messenger mMessenger;
        private Bundle mBundle;
        private boolean mExpectsEvent;

        public FunctionResultCallback(Bundle clientBundle, Messenger clientMessenger){
            mMessenger = clientMessenger;
            mBundle = clientBundle;
            mExpectsEvent = !TextUtils.isEmpty(mBundle.getString("deviceEvent"));
        }

        @Override
        public void onFunctionResult(Function f, int result, String eventResult) {
            //_log.v("onFunctionResult called - " + source + " " + result);

            try {

                Message m = Message.obtain(null, MSG_RETURN_FUNCTION);
                Bundle b = m.getData();

                b.putString("deviceId", mBundle.getString("deviceId"));
                b.putString("deviceFunction", mBundle.getString("deviceFunction"));
                b.putString("deviceArgs", mBundle.getString("deviceArgs"));
                b.putString("deviceEvent", mBundle.getString("deviceEvent"));

                b.putParcelable("functionUri", f.getUri());
                b.putInt("functionResult", result);

                if(mExpectsEvent){
                    b.putString("functionEventResult", eventResult);
                }

                b.putInt("statusFor", MSG_CALL_FUNCTION);
                b.putInt("status", STATUS_OK);
                b.putString("statusMsg", "OK");

                mMessenger.send(m);

            } catch (RemoteException e) {
                _log.w("could not send error to remote client - " + e.getMessage());
            }
        }

        @Override
        public void onFunctionError(Function f, String reason) {
            sendErrorOverMessenger(reason);
        }

        @Override
        public void onFunctionTimeout(Function f, int source) {
            sendErrorOverMessenger("timeout occurred");
        }


        private void sendErrorOverMessenger(String reason){
            try {
                returnErrorMessage(mMessenger, MSG_CALL_FUNCTION, reason);
            } catch (RemoteException e) {
                _log.w("could not send error to remote client - " + e.getMessage());
            }
        }
    }

    /**
     * Returns an error message to the client via the Messenger
     * @param replyTo   The messenger to pass the error to
     * @param fromWhat  The error is from what originating request
     * @param message   The message
     * @return  true if the message was successfully passed along
     */
    private static void returnErrorMessage(Messenger replyTo, int fromWhat, String message)
            throws RemoteException {

        Message m = Message.obtain(null, MSG_RETURN_SERVICE_ERROR);
        Bundle b = m.getData();

        b.putInt("status", STATUS_ERROR);
        b.putInt("statusFor", fromWhat);
        b.putString("statusMsg", message);
        replyTo.send(m);
    }

    private static final String[] DEVICE_COLUMNS = { // For MSG_RETRIEVE_DEVICES request
            BptContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID,
            BptContract.DeviceEntry.COLUMN_DEVICE_NAME,
            BptContract.DeviceEntry.COLUMN_DEVICE_TYPE,
    };

    private static final int COL_CLOUD_DEVICE_ID = 0;
    private static final int COL_DEVICE_NAME = 1;
    private static final int COL_DEVICE_TYPE = 2;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static final TLog _log = TLog.get(ExternalService.class);
}
