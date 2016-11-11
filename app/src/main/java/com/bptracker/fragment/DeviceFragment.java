package com.bptracker.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bptracker.R;

import io.particle.android.sdk.utils.TLog;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends Fragment {


    public DeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device, container, false);
    }


    public static final String DEVICE_URI_PARM = "DEVICE_URI_PARM";
    private static final TLog _log = TLog.get(DeviceFragment.class);
}
