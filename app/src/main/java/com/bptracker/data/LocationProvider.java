package com.bptracker.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bptracker.TrackerApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 *
 * Gets GPS coordinates of the device
 */
public class LocationProvider implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    public static final int CONNECTION_FAILED_RESOLUTION_REQUEST = 1000;
    public static final int NO_PERMISSION_RESOLUTION_REQUEST = 1001; // TODO
    public static final String EXTRA_CONNECTION_RESULT = "connection_result";


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Context mContext;
    private TrackerApplication mApplication;
    private boolean mConnectionFailed;
    private Location mLastLocation;
    private Activity mActivity;
    private Class<? extends Activity> mResolutionActivity;

    public LocationProvider(Activity activity) {
        init(activity);
        mActivity = activity;
    }

    // does not attempt to resolve connectivity or permission issues
    public LocationProvider(Context context){
        init(context);
    }

    // for services that would want to handle connectivity and permission issues
    public LocationProvider(Context context, Class<? extends Activity> resolutionActivity){
        init(context);
        mResolutionActivity = resolutionActivity;
    }


    private void init(Context context){
        mResolutionActivity = null;
        mActivity = null;
        mConnectionFailed = false;
        mLastLocation = null;

        mContext = context;
        mApplication = (TrackerApplication) mContext.getApplicationContext();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
    }


    public void sendLocationToDevice(String cloudDeviceId, int retries) {

    }

    public void getLastLocation(){
        //Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


    }

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void disconnect(){
        mGoogleApiClient.disconnect();
    }

    public boolean hasPermissions(){
        return mApplication.hasLocationPermission();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        _log.v("onConnectionFailed called");

        mConnectionFailed = true;

        if (result.hasResolution()) {
            try {

                if(mActivity != null){
                    result.startResolutionForResult(mActivity, CONNECTION_FAILED_RESOLUTION_REQUEST);
                } else if (mResolutionActivity != null) {

                    Intent i = new Intent(mContext, mResolutionActivity); //TODO: validate this
                    i.putExtra(EXTRA_CONNECTION_RESULT, result);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(i);
                } else {
                    _log.d("squashing ConnectionResult result resolution");
                }

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                _log.e("Cannot send connection failed resolution request -" + e.getMessage());
            }

        }else{
            _log.e("Location service failed with error - " + result.getErrorMessage() );
        }
    }


    private void processLocation(Location location){
        _log.v("processLocation called " + location);
        mLastLocation = location;

    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(@Nullable Bundle bundle) {
        _log.v("onConnected called");

        if (mApplication.hasLocationPermission()) {
            Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (l != null) {
                processLocation(l);
            }else{


            }

            return;
        }


        // no permissions here

        _log.e("no permissions TODO"); // TODO

        /*
        if (mActivity != null) {
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(mActivity,
                            NO_PERMISSION_RESOLUTION_REQUEST, null, 0 );

            mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    NO_PERMISSION_RESOLUTION_REQUEST, (Intent)null, 0, 0, 0);

        }
        */


    }

    @Override
    public void onConnectionSuspended(int i) {
        _log.v("onConnectionSuspended called");
    }

    private static final TLog _log = TLog.get(LocationProvider.class);
}
