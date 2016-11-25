package com.bptracker;

import android.app.Application;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.persistance.AppDataStorage;
import io.particle.android.sdk.persistance.SensitiveDataStorage;
import io.particle.android.sdk.utils.TLog;

public class TrackerApplication extends Application {

    @Override
    public void onCreate() {
        _log.d("onCreate");

        super.onCreate();

        ParticleCloudSDK.init(this);
        //ParticleDeviceSetupLibrary.init(this, MainActivity.class);
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
