package com.bptracker.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bptracker.R;
import com.bptracker.data.LoadDevicesTask;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.TLog;


/**
 * Author: Derek Benda
 */
public class LoadDevicesService extends IntentService {

    public LoadDevicesService() {
        super("load-devices-service");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        _log.d("onStart called");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        _log.d("onDestroy called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        _log.d("onHandleIntent called");
        Async.executeAsync(ParticleCloudSDK.getCloud(), new LoadDevicesTask(this));
    }

    private static final TLog _log = TLog.get(LoadDevicesService.class);
}



























    /*
    public static class TestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Notification n = new Notification.Builder(context)
                    .setContentTitle("Title")
                    .setSmallIcon(R.drawable.device_image_small)
                    .setContentText("Content").build();

            NotificationManager m = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            m.notify(0, n);
        }
    }
    */
