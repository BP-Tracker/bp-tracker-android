package com.bptracker.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bptracker.MainActivity;
import com.bptracker.R;
import com.bptracker.TrackerApplication;
import com.bptracker.receiver.BootCompletedReceiver;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.TLog;

// Monitors particle cloud events
public class DeviceEventService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _log.d("onStartCommand called");

        // This is required so the service can continue to listen to cloud events
        serviceNotification = new ServiceNotification("title", "message", MainActivity.class);
        serviceNotification.sendNotification();

        TrackerApplication app = (TrackerApplication) this.getApplicationContext();

        if(! app.hasLoginAccessToken()){
            _log.i("Deferring listening to Particle cloud events because login token is missing");
            return START_NOT_STICKY;
        }

        Async.executeAsync(ParticleCloudSDK.getCloud(), new SubscribeToEventsTask(this));
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        _log.d("onBind called");
        return null;
    }


    @Override
    public void onDestroy() {
        _log.d("onDestroy called");
        super.onDestroy();
    }

    private class SubscribeToEventsTask extends Async.ApiWork<ParticleCloud, Void>{

        public SubscribeToEventsTask(Context context){
            this.context = context;
        }

        @Override
        public Void callApi(ParticleCloud cloud) throws ParticleCloudException, IOException {

            long subscriptionId = cloud.subscribeToMyDevicesEvents("te", new ParticleEventHandler() {
                @Override
                public void onEventError(Exception e) {
                    _log.d("onEventError called");

                }

                @Override
                public void onEvent(String eventName, ParticleEvent particleEvent) {
                    _log.d("onEvent called [eventName=" + eventName
                            + "] [cloudDeviceId=" + particleEvent.deviceId + "]");
                }
            });
            _log.d("Subscribed to cloud [subscriptionId= " + subscriptionId + "]");

            return null;
        }

        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(ParticleCloudException e) {
            _log.d("onFailure");
            e.printStackTrace();
            throw new RuntimeException("Cannot subscribe to events from the Particle cloud: "
                    + e.getBestMessage(), e);

        }

        private Context context;
    }

    private class ServiceNotification {
        private String titleMessage;
        private String textMessage;
        private Class activityToCall;

        public ServiceNotification(String titleMessage, String textMessage,
                                   Class<? extends Activity> activityToCall) {
            this.textMessage = textMessage;
            this.titleMessage = titleMessage;
            this.activityToCall = activityToCall;
        }

        public void sendNotification(){
            NotificationCompat.Builder builder = getNotificationBuilder();


            Intent i = new Intent(getApplicationContext(), this.activityToCall);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent
                    = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

            builder.setContentIntent(contentIntent);

            startForeground(NOTIFICATION_ID, builder.build());
        }

        protected NotificationCompat.Builder getNotificationBuilder() {
            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext());

            builder.setSmallIcon(R.drawable.device_image_small);
            builder.setContentTitle(this.titleMessage);
            builder.setContentText(this.textMessage);
            builder.setContentInfo("Info");

            return builder;
        }
    }

    protected Integer NOTIFICATION_ID = 4566566; // Random int
    private ServiceNotification serviceNotification;
    private static final TLog _log = TLog.get(BootCompletedReceiver.class);
}































/*public DeviceEventService() {
        super("monitor-events-service");
    }*/
   /* @Override
    public void onStart(Intent intent, int startId) {
        _log.d("onStart called");
        super.onStart(intent, startId);
    }*/

