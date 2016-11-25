package com.bptracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        String version = "?.?.?";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

         /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }*/


        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText("v" + version);

        Button setupBtn  = (Button) findViewById(R.id.set_up_button);
        setupBtn.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }
}
