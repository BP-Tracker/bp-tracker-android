package com.bptracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.Function;
import com.bptracker.firmware.Firmware.State;
import com.bptracker.firmware.core.BptApi;
import com.bptracker.fragment.SelectStateFragment;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;

public class SelectStateActivity extends Activity
                                implements SelectStateFragment.SelectStateListener,
                                                        BptApi.ResultCallback {

    private ListView mListView;
    private String mCloudDeviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_state);

        mCloudDeviceId = getIntent().getStringExtra(IntentUtil.EXTRA_DEVICE_ID);

        if (TextUtils.isEmpty(mCloudDeviceId)) {
            _log.e("intent is missing EXTRA_DEVICE_ID parameter. Closing activity");

            Toast.makeText(this, "Request abruptly cancelled", Toast.LENGTH_SHORT).show();

            //TODO: add a friendly alert indicating the issue
            finishAndRemoveTask();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) { //TODO: is this necessary??
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment fragment = new SelectStateFragment();
        fragment.show(ft, "dialog");

    }

    @Override
    public void onStateSelect(DialogFragment dialog, State state) {

        //TODO:
        //BptApi f = BptApi.createInstance(this, Function.BPT_STATE, mCloudDeviceId, this);
        //f.addArgument(BptApi.ARG_STATE, State.PAUSED);


        BptApi f = BptApi.createInstance(this, Function.BPT_ACK, mCloudDeviceId, this);

        f.addArgument(BptApi.ARG_EVENT_TYPE, Firmware.EventType.NO_GPS_SIGNAL);
        f.addArgument(BptApi.ARG_STRING_DATA, "1");
        //f.addArgument(BptApi.ARG_TEST_INPUT, Firmware.TestInput.INPUT_ACCEL_INT);
        //f.addArgument(BptApi.ARG_STRING_DATA, "1");
       // f.addArgument(BptApi.ARG_SOFTWARE_RESET, true);
        f.call();

        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    @Override
    public void onStateCancel(DialogFragment dialog) {

        //Toast.makeText(this, "Disarm cancelled", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }



    @Override
    public void onFunctionError(BptApi function, String reason) {
        _log.v("onFunctionError: " + reason);

    }

    @Override
    public void onFunctionTimeout(BptApi function, int source) {
        _log.v("onFunctionTimeout: " + source);

    }

    @Override
    public void onFunctionResult(BptApi function, int source, String extra) {
        _log.v("onFunctionResult [source=" + source + "] " + extra );

    }



    private static final TLog _log = TLog.get(SelectStateActivity.class);
}


































   /*
        mListView = new ListView(this);
        String[] states = {
                "Panic",
                "Offline",
                "Armed"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.select_state_row, R.id.tv_state_item_id, states);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               TextView stateItem = (TextView) view.findViewById(R.id.tv_state_item_id);
               Toast.makeText(SelectStateActivity.this,
                       stateItem.getText(), Toast.LENGTH_LONG).show();

               //startActivityForResult();

           }
       });
       */


/*
    @Override
    protected void onResume() {
        super.onResume();

       //AlertDialog.Builder b = new AlertDialog.Builder(this);
       // b.setCancelable(true);
       // b.setPositiveButton("OK", null);
       // b.setView(mListView);

        //AlertDialog dialog = b.create();
       // dialog.show();

}

 */