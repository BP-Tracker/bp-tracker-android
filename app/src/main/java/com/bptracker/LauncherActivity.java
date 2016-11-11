package com.bptracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.SDKGlobals;
import io.particle.android.sdk.persistance.AppDataStorage;
import io.particle.android.sdk.utils.TLog;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _log.d("onCreate called");

        setContentView(R.layout.activity_launcher);

        TrackerApplication app = (TrackerApplication) this.getApplicationContext();
        Intent intent;


        if(app.hasClaimedDevices()){
            _log.d("Has claimed devices");

            if(app.hasLoginAccessToken()){
                _log.d("Has login access token");

                intent = new Intent(this, MainActivity.class);

            }else {

                intent = new Intent(this, LoginActivity.class);
            }

        }else{

            _log.d("No devices have been claimed");

            intent = new Intent(this, IntroActivity.class);
        }


        _log.d("Launching activity: " + intent);
        startActivity(intent);
        finish();
    }

    private static final TLog _log = TLog.get(LauncherActivity.class);
}
