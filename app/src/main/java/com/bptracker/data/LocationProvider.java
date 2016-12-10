package com.bptracker.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.icu.math.BigDecimal;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bptracker.TrackerApplication;
import com.bptracker.firmware.core.BptApi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 *
 * Gets geo coordinates of the device
 */
public class LocationProvider {

    public static final int CONNECTION_FAILED_RESOLUTION_REQUEST = 1000;
    public static final int CONNECTION_SUSPENDED_REQUEST = 1001;
    public static final int NO_PERMISSION_RESOLUTION_REQUEST = 1002; // TODO


    public interface Callback {
        public void onLocation(LocationProvider p, Location location);
        public void onLocationError(LocationProvider p, int requestCode, @Nullable ConnectionResult result);
    }

    private GoogleApiClient mGoogleApiClient;
    private ApiClientListener mApiClientListener;
    private LocationRequest mLocationRequest;
    private Context mContext;
    private TrackerApplication mApplication;
    private boolean mDisconnectOnLocationReceived;
    private Callback mCallback;
    private Bundle mBundle;


    public LocationProvider(Context context, Bundle bundle){
        init(context);
        mBundle = bundle;
    }

    public LocationProvider(Context context){
        init(context);
    }

    /**
     * the default uses GPS
     */
    public void setHighAccuracy(){
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * "block" level accuracy
     */
    public void setMediumAccuracy(){
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public Bundle getBundle(){
        return mBundle;
    }

    public void setBundle(Bundle bundle) {
        mBundle = bundle;
    }

    /**
     * best guess (low power)
     */
    public void setLowAccuracy(){
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    /**
     * Get the android device's location and return the result in the callback.
     * @param callback  The callback to call once the location has been retrieved or an error has
     *                  occurred
     */
    public void getLocation(Callback callback){
        mCallback = callback;
        processRequest();
    }

    public boolean hasPermissions(){
        return mApplication.hasLocationPermission();
    }

    private void init(Context context){
        mContext = context;
        mApplication = (TrackerApplication) mContext.getApplicationContext();

        mApiClientListener = new ApiClientListener();
        mDisconnectOnLocationReceived = true;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(mApiClientListener)
                .addOnConnectionFailedListener(mApiClientListener)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setInterval(10 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1 * 1000);
    }


    @SuppressWarnings({"MissingPermission"})
    private void processRequest(){

        if (mCallback == null) {
            throw new RuntimeException("callback is null");
        }

        if(!hasPermissions()){
            mCallback.onLocationError(LocationProvider.this, NO_PERMISSION_RESOLUTION_REQUEST, null);
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            processLocationReceived(l);
        }else{

            mGoogleApiClient.connect();
        }
    }

    private void processLocationReceived(Location location){
        //_log.v("processLocation called " + location);

        mCallback.onLocation(this, location);

        if (mDisconnectOnLocationReceived && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }



    private class ApiClientListener implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            processLocationReceived(location);
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            mCallback.onLocationError(LocationProvider.this,
                    CONNECTION_FAILED_RESOLUTION_REQUEST, result);
        }


        /**
         * If the location is null once connected, the class will temporarily subscribe to
         * location requests.
         */
        @Override
        @SuppressWarnings({"MissingPermission"})
        public void onConnected(@Nullable Bundle bundle) {
            _log.v("onConnected called");

            Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (l != null) {
                processLocationReceived(l);
            }else{

                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mCallback.onLocationError(LocationProvider.this, CONNECTION_SUSPENDED_REQUEST, null);
        }
    }

    private static final TLog _log = TLog.get(LocationProvider.class);
}




























//
//public interface ErrorCallback {
//
//}


/**
 * Sends the android device's current location to the particle.io device if it
 * can. Errors are logged to the console.
 *
 * @param cloudDeviceId The particle.io device to send the location to via
 *                      the bpt:ack function
 *
 */


  /*
  *    public static final int FAILED_TO_SEND_TO_FIRMWARE = 1003; //TODO
  * *//**
 * Sends the android device's current location to the particle.io device if it can. If not,
 * the ErrorCallback instance is invoked with the error
 * @param cloudDeviceId     The particle.io device ID
 * @param usingAckFunction  Whether to use bpt:ack function to send the location. If false,
 *                          the bpt:gps function will be called instead
 * @param callback
 *//*
    public void sendLocationToDevice(final String cloudDeviceId, final boolean usingAckFunction,
                                     final ErrorCallback callback) {

        mCallback = new Callback() {
            @Override
            public void onLocation(LocationProvider p, Location location) {

                // mDeviceId = cloudDeviceId;

                float lat = Double.valueOf(location.getLatitude()).floatValue();
                float lon = Double.valueOf(location.getLongitude()).floatValue();

                BptApi f;

                if(usingAckFunction){
                    f = BptApi.createInstance()

                }





            }

            @Override
            public void onLocationError(LocationProvider p, int requestCode, @Nullable ConnectionResult result) {
                callback.onLocationError(p, requestCode, result);
            }
        };

        processRequest();
    }*/





//private String mDeviceId;

//mErrorCallback = (ErrorCallback) callback;
    //private ErrorCallback mErrorCallback;
//private boolean mSendLocationToDevice;
//mSendLocationToDevice = true;
//mSendLocationToDevice = false;
/*
private Location mLastLocation;
mResolutionActivity = null;
    // for services that would want to handle connectivity and permission issues
    private Class<? extends Activity> mResolutionActivity;
    public LocationProvider(Context context, Class<? extends Activity> resolutionActivity){
        init(context);
        mResolutionActivity = resolutionActivity;
    }

*/




        /*
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
        */


 /*
 public static final String EXTRA_CONNECTION_RESULT = "connection_result";
        if (mActivity != null) {
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(mActivity,
                            NO_PERMISSION_RESOLUTION_REQUEST, null, 0 );

            mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    NO_PERMISSION_RESOLUTION_REQUEST, (Intent)null, 0, 0, 0);

        }
        */


/*

    private Activity mActivity;
    public LocationProvider(Activity activity) {
        init(activity);
        mActivity = activity;
    }
 */