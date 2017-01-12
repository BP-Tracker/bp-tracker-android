package com.bptracker.persistance;

import android.content.Context;

/**
 * Author: Derek Benda
 */

public class DevicePreferences {

    private Context mContext;
    private String mcloudDeviceId;


    public DevicePreferences(Context context, String cloudDeviceId) {
        mContext = context;
        mcloudDeviceId = cloudDeviceId;
    }




}
