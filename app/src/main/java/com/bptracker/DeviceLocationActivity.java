package com.bptracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.bptracker.receiver.BptEventReceiver;
import com.bptracker.util.IntentUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.particle.android.sdk.utils.TLog;

public class DeviceLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //mapFragment.

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();

        String deviceName = intent.getStringExtra(IntentUtil.EXTRA_DEVICE_NAME);
        String deviceInfo = intent.getStringExtra(IntentUtil.EXTRA_INFO);
        LatLng device = intent.getParcelableExtra(IntentUtil.EXTRA_LAT_LNG);

        if (!intent.hasExtra(IntentUtil.EXTRA_LAT_LNG)) {
            _log.e("intent is missing required extra: EXTRA_LAT_LNG");
            return;
        }

        boolean hasInfo = !TextUtils.isEmpty(deviceInfo);

        MarkerOptions opts = new MarkerOptions().position(device).title(deviceName);

        if (hasInfo) {
            opts.snippet(deviceInfo);
        }

        Marker marker = mMap.addMarker(opts);

        if (hasInfo) {
            marker.showInfoWindow();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(device, 15));
        //mMap.
    }


    private static final TLog _log = TLog.get(DeviceLocationActivity.class);
}
