package com.bptracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bptracker.service.DeviceEventService;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    public BootCompletedReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {

            _log.d("onReceive called for Intent ACTION_BOOT_COMPLETED");

            Intent i = new Intent(context, DeviceEventService.class);
            context.startService(i);
        }
    }

    private static final TLog _log = TLog.get(BootCompletedReceiver.class);
}
