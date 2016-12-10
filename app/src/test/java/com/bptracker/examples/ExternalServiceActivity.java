package com.bptracker.examples;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.bptracker.service.ExternalService;

import java.util.ArrayList;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */

public class ExternalServiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        _log.d("onResume");
        super.onResume();
        doBindService();
    }

    @Override
    protected void onPause() {
        _log.d("onPause");
        super.onPause();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void doBindService(){

        mMessenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                _log.v("received message from service " + msg.what);

                Bundle b = msg.getData();
                switch (msg.what) {

                    case ExternalService.MSG_RETURN_DEVICES:

                        ArrayList<ContentValues> devices = b.getParcelableArrayList("devices");

                        for (ContentValues v : devices) {
                            _log.v(v.getAsString("deviceId") + " - "
                                    + v.getAsString("deviceName") + " - "
                                    + v.getAsString("deviceType"));

                            if (v.getAsString("deviceType").equals("ELECTRON")) {
                                callFunction(v.getAsString("deviceId"), "bpt:state", "", "bpt:state");
                            }
                        }

                        break;
                    case ExternalService.MSG_RETURN_FUNCTION:

                        String deviceId = b.getString("deviceId");
                        String result = b.getString("functionResult");
                        String eventResult = b.getString("functionEventResult");
                        Uri uri = b.getParcelable("functionUri");

                        _log.i("Device " + deviceId + " returned function result " + result
                                + " with event data " + eventResult + " (" + uri + ")");

                        break;

                    case ExternalService.MSG_RETURN_SERVICE_ERROR:

                        int fromWhat = b.getInt("statusFor");
                        int statusCode = b.getInt("status");
                        String statusMsg = b.getString("statusMsg");

                        _log.e("error " + statusCode + " occurred on " + fromWhat + ": " + statusMsg);


                    default:
                        super.handleMessage(msg);
                }
            }
        });

        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {

                mService = new Messenger(service);


                try {
                    Message msg = Message.obtain(null, ExternalService.MSG_CALL_GET_DEVICES);
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                } catch (RemoteException e) {
                    _log.e("Remote service exception - " + e.getMessage());
                }

            }

            public void onServiceDisconnected(ComponentName className) {
                _log.v("onServiceDisconnected called");
                mService = null;
            }
        };

        bindService(new Intent(this, ExternalService.class), mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;

    }

    private void callFunction(String deviceId, String name, String args, String event){

        try {
            Message newMsg = Message.obtain(null, ExternalService.MSG_CALL_FUNCTION);
            Bundle newBundle = newMsg.getData();

            newBundle.putString("deviceId", deviceId);
            newBundle.putString("deviceFunction", name);
            newBundle.putString("deviceArgs", args);
            newBundle.putString("deviceEvent", event);

            newMsg.replyTo = mMessenger;
            mService.send(newMsg);
        } catch (RemoteException e) {
            _log.e("Remote service exception - " + e.getMessage());
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private Messenger mMessenger;
    private Messenger mService = null;
    private ServiceConnection mConnection;
    boolean mIsBound;

    private static final TLog _log = TLog.get(ExternalServiceActivity.class);
}
