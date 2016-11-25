package com.bptracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import io.particle.android.sdk.utils.TLog;

public class LauncherActivity extends Activity {

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
