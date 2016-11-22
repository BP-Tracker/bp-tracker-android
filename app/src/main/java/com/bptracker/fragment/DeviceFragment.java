package com.bptracker.fragment;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bptracker.R;
import com.bptracker.data.BptContract;
import com.bptracker.firmware.DataType;

import io.particle.android.sdk.utils.Funcy;
import io.particle.android.sdk.utils.TLog;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends Fragment {

    private ContentObserver mDeviceEntryObserver;

    @Override
    public void onResume() {
        super.onResume();

        _log.d("onResume");

        this.getActivity().getContentResolver().registerContentObserver(
            BptContract.DeviceEventEntry.CONTENT_URI, true, mDeviceEntryObserver);

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
        return inflater.inflate(R.layout.fragment_device, container, false);
    }


    public static final String DEVICE_URI_PARM = "DEVICE_URI_PARM";
    private static final TLog _log = TLog.get(DeviceFragment.class);
}
