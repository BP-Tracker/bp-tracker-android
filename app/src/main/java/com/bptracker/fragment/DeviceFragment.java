package com.bptracker.fragment;


import android.app.Fragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bptracker.R;
import com.bptracker.data.BptContract;
import com.bptracker.ui.gauges.GradientCircleGauge;

import io.particle.android.sdk.utils.TLog;


public class DeviceFragment extends Fragment {

    private ContentObserver mDeviceEntryObserver;

    private Uri mUri;
    private String mDeviceName;

    public static class ViewHolder {
        final View topLevel;
        final TextView deviceStatus;
        final GradientCircleGauge batteryGauge;
        final GradientCircleGauge signalGauge;

        public ViewHolder(View view) {
            topLevel = view;
            deviceStatus = (TextView) view.findViewById(R.id.tv_device_status);
            batteryGauge = (GradientCircleGauge) view.findViewById(R.id.gcg_battery_gauge);
            signalGauge = (GradientCircleGauge) view.findViewById(R.id.gcg_signal_gauge);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        _log.d("onResume");

        this.getActivity().getContentResolver().registerContentObserver(
            BptContract.DeviceEventEntry.CONTENT_URI, true, mDeviceEntryObserver);

        ViewHolder holder = (ViewHolder) getView().getTag();
        holder.deviceStatus.setText("ARMED");

        //holder.batteryGauge.setGaugeLevel(100, false);
        holder.batteryGauge.setGaugeLevel(87);
        holder.signalGauge.setGaugeLevel(50);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        _log.v("onCreateOptionsMenu called");
        inflater.inflate(R.menu.device_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
//        if(id == R.id.action_refresh){
//            refreshDevicesFromCloud();
//            return true;
//        } else if (id == R.id.action_logout) {
//            logoutAndRedirectToLogin();
//            return true;
//        }

       /* if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();

        _log.d("onPause");

        this.getActivity().getContentResolver().unregisterContentObserver(mDeviceEntryObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DeviceFragment.DEVICE_URI_PARM);
            mDeviceName = arguments.getString(DeviceFragment.DEVICE_NAME);
        }

        mDeviceEntryObserver =
                new ContentObserver(null) {
                    @Override
                    public boolean deliverSelfNotifications() {
                        return super.deliverSelfNotifications();
                    }

                    @Override
                    public void onChange(boolean selfChange) {
                        onChange(selfChange, null);
                    }

                    @Override
                    public void onChange(boolean selfChange, @Nullable Uri uri) {

                        _log.d("URI on change: " + uri.toString());
                    }
            };


        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_device, container, false);

        ViewHolder holder = new ViewHolder(v);
        v.setTag(holder);

        return v;
    }


    public static final String DEVICE_URI_PARM = "DEVICE_URI_PARM";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    private static final TLog _log = TLog.get(DeviceFragment.class);
}
