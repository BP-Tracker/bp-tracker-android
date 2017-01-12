package com.bptracker;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.Preference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.bptracker.firmware.core.BptApi;
import com.bptracker.persistance.AppPreferences;
import com.bptracker.persistance.DevicePreferences;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.persistance.AppDataStorage;
import io.particle.android.sdk.persistance.SensitiveDataStorage;
import io.particle.android.sdk.utils.TLog;

public class TrackerApplication extends Application {

    public static final int REQUEST_PERMISSION_LOCATION = 1;
    private boolean mHasRegisteredGcm;
    private AppPreferences mAppPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        _log.v("onCreate");

        ParticleCloudSDK.init(this);
        BptApi.init(this);

        mAppPreferences = new AppPreferences(this);

        _log.d("is GCM enabled: " + mAppPreferences.isGcmEnabled());

        if (mAppPreferences.isGcmEnabled()) {
            registerGcmService(false);
        }

    }

    //TODO: is this the right place to register for GCM notifications?
    public boolean registerGcmService(boolean reRegister){
        _log.i("Registering GCM service");

        if (!mHasRegisteredGcm || reRegister) {
            RegisterGcmTask task = new RegisterGcmTask();
            task.execute(this);
            mHasRegisteredGcm = true;
        }
        return true;
    }

    public void deregisterGcmService(){
        _log.i("De-registering GCM service");
        if (!mHasRegisteredGcm) {
            return;
        }

        //TODO
        mHasRegisteredGcm = false;
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

    public Preference getDevicePreference(String cloudDeviceId, String key) {

        //TODO
        return null;
    }


    public AppPreferences getPreferences(){
        return mAppPreferences;
    }


    public DevicePreferences getDevicePreferences(String cloudDeviceId) {
        if (TextUtils.isEmpty(cloudDeviceId)) {
            throw new IllegalArgumentException("cloudDeviceId is null or an empty string");
        }

        return new DevicePreferences(this, cloudDeviceId);
    }

    /*
    public Preference getPreference(String key){

        //TODO
        return null;
    }

    public void setPreference(String key, String value) { //TODO

    }

    public void setDevicePreference(String cloudDeviceId, String key, String value) {

    }
    */

    //TODO: error handling
    private static class RegisterGcmTask extends AsyncTask<Context, Void, Void> {

        @Override
        protected Void doInBackground(Context... ctx) {

            Context context = ctx[0];
            TrackerApplication app = (TrackerApplication) context.getApplicationContext();

            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            try {
                InstanceID instanceID = InstanceID.getInstance(context);

                // http://stackoverflow.com/questions/26718115/gcm-error-not-registered
                //instanceID.deleteInstanceID();
                //instanceID = InstanceID.getInstance(MainActivity.this);

                String senderId = app.getPreferences().getGcmSenderId();
                String regId = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                GcmPubSub pubSub = GcmPubSub.getInstance(context);
                pubSub.subscribe(regId, "/topics/bpt", null);


                _log.i("GCM registration token = " + regId);
            } catch (IOException e) {
                _log.e("Could not register with GCM: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {}

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    private static final TLog _log = TLog.get(TrackerApplication.class);
}
