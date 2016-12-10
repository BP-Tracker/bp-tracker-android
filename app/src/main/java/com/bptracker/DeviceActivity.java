package com.bptracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bptracker.firmware.Firmware;
import com.bptracker.fragment.DeviceFragment;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;

public class DeviceActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable(DeviceFragment.DEVICE_URI_PARM, getIntent().getData());

            DeviceFragment fragment = new DeviceFragment();
            fragment.setArguments(arguments);


            getFragmentManager().beginTransaction()
                    .add(R.id.device_detail_container, fragment)
                    .commit();
        }


        Uri uri = Uri.parse("content://com.bptracker/devices/42003b000251353337353037/bpt-events/66");
        Intent i = new Intent(IntentUtil.ACTION_BPT_EVENT, uri);
        i.putExtra(IntentUtil.EXTRA_FROM_BPT_DEVICE, true );

        i.putExtra(IntentUtil.EXTRA_DEVICE_ID, "42003b000251353337353037");
        i.putExtra(IntentUtil.EXTRA_EVENT_NAME, "bpt:event");
        i.putExtra(IntentUtil.EXTRA_EVENT_DATA, "0,7,7");
        i.putExtra(IntentUtil.EXTRA_DEVICE_NAME, "Pippy");
        i.putExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE, Firmware.EventType.STATE_CHANGE);

        sendBroadcast(i, IntentUtil.PERMISSION_RECEIVE_EVENTS);

        _log.d("Send broadcast");

    }





    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */




    private static final TLog _log = TLog.get(DeviceActivity.class);
}
