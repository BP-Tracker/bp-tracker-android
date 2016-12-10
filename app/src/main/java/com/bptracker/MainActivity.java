package com.bptracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.bptracker.data.LocationProvider;
import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.core.BptApi;
import com.bptracker.firmware.core.Function;
import com.bptracker.fragment.DeviceListFragment;
import com.bptracker.service.DeviceEventService;
import com.bptracker.service.LoadDevicesService;
import com.bptracker.service.RunFunctionService;
import com.bptracker.util.IntentUtil;
import com.bptracker.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiActivity;

import io.particle.android.sdk.utils.TLog;

public class MainActivity extends Activity
        implements DeviceListFragment.Callbacks, ActivityCompat.OnRequestPermissionsResultCallback {



    private LocationProvider locationProvider;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        _log.v("onRequestPermissionsResult " + requestCode);
    }

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

        TrackerApplication app = (TrackerApplication) this.getApplicationContext();

        if(!app.hasLocationPermission()){
            app.requestLocationPermission(this);
        }

        //Intent intent = new Intent(this, LoadDevicesService.class);
        //startService(intent);
    }

    @Override
    protected void onResume() {
        _log.v("onResume called");
        super.onResume();
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














     /*
        locationProvider = new LocationProvider(this);


        locationProvider.getLocation(new LocationProvider.Callback() {
            @Override
            public void onLocation(LocationProvider p, Location location) {
                _log.d("onLocation" + location);
            }

            @Override
            public void onLocationError(LocationProvider p, int requestCode, @Nullable ConnectionResult result) {
                _log.d("onLocationError " + requestCode );

                if (result != null) {
                    _log.d("error code = " + result.getErrorCode());
                    switch(result.getErrorCode()){
                        case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                            //GoogleApiActivity.


                    }

                }

            }
        });
    */







/*
    String deviceId = "4200";

    Function f = BptApi.createFunction(Firmware.Function.BPT_STATE, deviceId);
    f.finalizeArguments();

        Intent i = new Intent(this, RunFunctionService.class);
        i.putExtra(IntentUtil.EXTRA_FUNCTION, f);


        IntentFilter filter = new IntentFilter(IntentUtil.ACTION_FUNCTION_RESULT);


    final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
            _log.i("got results");

            Function f = intent.getParcelableExtra(IntentUtil.EXTRA_FUNCTION);
            int r = intent.getIntExtra(IntentUtil.EXTRA_FUNCTION_RESULT, -1);

            _log.i(f.getUri() + " " + r);

            LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(this);
            }
            };


            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

            startService(i);
*/








//private static int REQUEST_PERMISSION_LOCATION = 1;

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        _log.v("onActivityResult called " + requestCode + " " + resultCode);
//
//        if(requestCode == LocationProvider.CONNECTION_FAILED_RESOLUTION_REQUEST){
//
//
//        }
//
//
//    }