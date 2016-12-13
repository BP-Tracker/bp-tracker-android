package com.bptracker;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.bptracker.firmware.core.BptApi;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.persistance.AppDataStorage;
import io.particle.android.sdk.persistance.SensitiveDataStorage;
import io.particle.android.sdk.utils.TLog;

public class TrackerApplication extends Application {

    public static final int REQUEST_PERMISSION_LOCATION = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        _log.v("onCreate");

        ParticleCloudSDK.init(this);
        BptApi.init(this);
    }


    public boolean hasLocationPermission(){

        //int hasCoarsePermission = ContextCompat.checkSelfPermission(this,
        //        android.Manifest.permission.ACCESS_COARSE_LOCATION);

        int hasFinePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFinePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }


    public boolean logoutAndRedirect(){
        ParticleCloud cloud = ParticleCloudSDK.getCloud();
        cloud.logOut();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);

        return true;
    }

    public void requestLocationPermission(Activity activity) {

        if (!(activity instanceof ActivityCompat.OnRequestPermissionsResultCallback)){
            throw new RuntimeException("activity must implement OnRequestPermissionsResultCallback");
        }

        /*
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {




            //ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
            //        LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION );

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {
        */

        // No explanation needed, we can request the permission.


        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        //}
        // android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ActivityCompat.requestPermissions(activity,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION_LOCATION);

    }


    public boolean hasLoginCredentials(){

       SensitiveDataStorage s = SDKGlobals.getSensitiveDataStorage();

        if((s.getToken() != null && s.getToken().length() > 0 )
                || (s.getUser() != null && s.getUser().length() > 0)){

            return true;
        }

        return false;
    }

    public boolean hasLoginAccessToken(){

        ParticleCloud cloud = ParticleCloudSDK.getCloud();

        if(cloud.getAccessToken() != null && cloud.getAccessToken().length() > 0){
            return true;
        }

        return false;
    }

    public boolean hasClaimedDevices() {
        AppDataStorage s = SDKGlobals.getAppDataStorage();

        return s.getUserHasClaimedDevices();
    }


    private static final TLog _log = TLog.get(TrackerApplication.class);
}
