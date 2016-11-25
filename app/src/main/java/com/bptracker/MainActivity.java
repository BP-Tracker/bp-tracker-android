package com.bptracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bptracker.fragment.DeviceListFragment;
import com.bptracker.service.DeviceEventService;
import com.bptracker.service.LoadDevicesService;
import com.bptracker.util.Utils;

import io.particle.android.sdk.utils.TLog;

public class MainActivity extends Activity
        implements DeviceListFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // Async.executeAsync(ParticleCloudSDK.getCloud(),
       //         new LoadDevicesTask(this));

        //TODO: this should probably be executed from an AlarmBroadcastReceiver
        if (!Utils.isServiceRunning(DeviceEventService.class, this)) {
            _log.d("DeviceEventService is not running, launching service");
            Intent i = new Intent(this, DeviceEventService.class);
            startService(i);
        }

        //Intent intent = new Intent(this, LoadDevicesService.class);
        //startService(intent);


    }

    @Override
    protected void onStart() {
        _log.d("onStart called");
        super.onStart();
    }

    //TODO: do I need this?
    @Override
    public void onBackPressed() {
        if (deviceList == null || !deviceList.onBackPressed()) {
            super.onBackPressed();
        }
    }

    // DeviceListFragment.Callbacks
    @Override
    public void onDeviceSelected(Uri deviceUri) {
        _log.d("onDeviceSelected " + deviceUri.toString());
        // TODO: ...

        Intent intent = new Intent(this, DeviceActivity.class).setData(deviceUri);
        startActivity(intent);
    }

    private DeviceListFragment deviceList;

    private static final TLog _log = TLog.get(MainActivity.class);
}
