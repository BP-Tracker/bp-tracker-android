package com.bptracker.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.iid.InstanceIDListenerService;

import io.particle.android.sdk.utils.TLog;

// TODO: required?
public class GcmTokenService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        _log.v("onTokenRefresh called");
    }

    private static final TLog _log = TLog.get(GcmTokenService.class);
}
