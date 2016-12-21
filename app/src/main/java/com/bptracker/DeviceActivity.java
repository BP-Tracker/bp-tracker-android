package com.bptracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toolbar;

import com.bptracker.firmware.Firmware;
import com.bptracker.fragment.DeviceFragment;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;

public class DeviceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);


        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_device);
        setActionBar(toolbar);

        this.getActionBar().setDisplayShowTitleEnabled(false);


        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            String deviceName = getIntent().getStringExtra(IntentUtil.EXTRA_DEVICE_NAME);
            Uri deviceUri = getIntent().getData();

            Bundle arguments = new Bundle(); //TODO: refactor
            arguments.putParcelable(DeviceFragment.DEVICE_URI_PARM, deviceUri);
            arguments.putString(DeviceFragment.DEVICE_NAME, deviceName);

            DeviceFragment fragment = new DeviceFragment();
            fragment.setArguments(arguments);

            toolbar.setTitle(deviceName);


            getFragmentManager().beginTransaction()
                    .add(R.id.fl_device_fragment_container, fragment)
                    .commit();
        }
    }

    private static final TLog _log = TLog.get(DeviceActivity.class);
}















/*

    Uri uri = Uri.parse("content://com.bptracker/devices/343434/bpt-events/66");
    Intent i = new Intent(IntentUtil.ACTION_BPT_EVENT, uri);
i.putExtra(IntentUtil.EXTRA_FROM_BPT_DEVICE, true );

        i.putExtra(IntentUtil.EXTRA_DEVICE_ID, "3434");
        i.putExtra(IntentUtil.EXTRA_EVENT_NAME, "bpt:event");
        i.putExtra(IntentUtil.EXTRA_EVENT_DATA, "0,7,7");
        i.putExtra(IntentUtil.EXTRA_DEVICE_NAME, "Pippy");
        i.putExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE, Firmware.EventType.STATE_CHANGE);

        sendBroadcast(i, IntentUtil.PERMISSION_RECEIVE_EVENTS);*/
