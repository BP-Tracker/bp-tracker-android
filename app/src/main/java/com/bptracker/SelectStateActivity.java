package com.bptracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.Toast;

import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.State;
import com.bptracker.firmware.core.BptApi;
import com.bptracker.firmware.core.Function;
import com.bptracker.fragment.SelectStateFragment;
import com.bptracker.service.RunFunctionService;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;

public class SelectStateActivity extends Activity
                                implements SelectStateFragment.SelectStateListener {

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

        Function f = BptApi.createFunction(Firmware.Function.BPT_STATE, mCloudDeviceId);
        f.addArgument(BptApi.ARG_STATE, state);
        f.finalizeArguments();

        Intent i = new Intent(this, RunFunctionService.class);
        i.putExtra(IntentUtil.EXTRA_FUNCTION, f);


        IntentFilter filter = new IntentFilter(IntentUtil.ACTION_FUNCTION_RESULT);


        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _log.i("got results");

                Function f = intent.getParcelableExtra(IntentUtil.EXTRA_FUNCTION);
                int r = intent.getIntExtra(IntentUtil.EXTRA_FUNCTION_RESULT, -1);

                _log.i(f.getUri() + " " + r);

                LocalBroadcastManager.getInstance(SelectStateActivity.this).unregisterReceiver(this);
            }
        };


        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        startService(i);


        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    @Override
    public void onStateCancel(DialogFragment dialog) {
        //Toast.makeText(this, "Disarm cancelled", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    private static final TLog _log = TLog.get(SelectStateActivity.class);
}
















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