package com.bptracker.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bptracker.TrackerApplication;
import com.bptracker.data.BptContract;
import com.bptracker.data.BptContract.DeviceEntry;
import com.bptracker.data.BptContract.DeviceEventEntry;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Util;
import com.bptracker.util.IntentUtil;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.Date;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.TLog;


/**
 * Processes particle cloud events
 *
 * <p>Accepts intent extras: {@link IntentUtil#EXTRA_PARTICLE_EVENT}, {@link IntentUtil#ACTION_DEVICE_EVENT}
 *
 * <p>Broadcasts: {@link IntentUtil#ACTION_DEVICE_EVENT} and {@link IntentUtil#ACTION_BPT_EVENT}
 * intents
 */
public class DeviceEventService extends Service {

    private static final String BPT_PREFIX = "bpt:";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _log.v("onStartCommand called");

        TrackerApplication app = (TrackerApplication) this.getApplicationContext();

        if (intent.hasExtra(IntentUtil.EXTRA_PARTICLE_EVENT)) {

            ParticleEvent event = intent.getParcelableExtra(IntentUtil.EXTRA_PARTICLE_EVENT);
            String eventName = intent.getStringExtra(IntentUtil.EXTRA_EVENT_NAME);

            processCloudEvent(this, eventName, event);
            return START_STICKY;
        }

        _log.w("not implemented");
        return START_STICKY;

        /*

        if(! app.hasLoginAccessToken()){
            _log.i("Deferring listening to Particle cloud events because login token is missing");
            return START_NOT_STICKY;
        }

        Async.executeAsync(ParticleCloudSDK.getCloud(), new SubscribeToEventsTask(this));
        return START_STICKY;

        */
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        _log.v("onBind called");
        return null;
    }


    @Override
    public void onDestroy() {
        _log.v("onDestroy called");
        super.onDestroy();
    }


    /*
    * This method inserts data to content providers and also broadcasts
    * IntentUtil.ACTION_DEVICE_EVENT and if applicable, IntentUtil.ACTION_BPT_EVENT intents.
    *
    * @param context Context
    * @param eventName The event name that was received
    * @param particleEvent The event object
    */
    private static void processCloudEvent(Context context, String eventName, ParticleEvent event ){

        String cloudDeviceId = event.deviceId;
        Date publishedAt = event.publishedAt;
        String eventData = event.dataPayload;

        _log.v("processCloudEvent [eventName=" + eventName + "] [cloudDeviceId="
                + cloudDeviceId + "] [particleData=" + eventData + "]");


        ContentValues v = new ContentValues();
        v.put(DeviceEventEntry.COLUMN_CLOUD_DEVICE_ID, cloudDeviceId);
        v.put(DeviceEventEntry.COLUMN_PUBLISH_DATE, publishedAt.getTime());
        v.put(DeviceEventEntry.COLUMN_EVENT_DATA, eventData);
        v.put(DeviceEventEntry.COLUMN_EVENT_NAME, eventName);

        Uri uri = context.getContentResolver().insert(DeviceEventEntry.CONTENT_URI, v);

        _log.d("inserted event (" + eventName + "): "  + uri.toString() );


        Intent i = new Intent(IntentUtil.ACTION_DEVICE_EVENT, uri);
        i.putExtra(IntentUtil.EXTRA_FROM_BPT_DEVICE, eventName.startsWith(BPT_PREFIX) );

        i.putExtra(IntentUtil.EXTRA_DEVICE_ID, cloudDeviceId);
        i.putExtra(IntentUtil.EXTRA_EVENT_NAME, eventName);
        i.putExtra(IntentUtil.EXTRA_EVENT_DATA, eventData);

        String deviceName = getDeviceName(context, cloudDeviceId);

        if(!TextUtils.isEmpty(deviceName)){
            i.putExtra(IntentUtil.EXTRA_DEVICE_NAME, deviceName);
        }else{
            _log.w("Device name is empty for cloud device ID: " + cloudDeviceId);
        }

        context.sendBroadcast(i, IntentUtil.PERMISSION_RECEIVE_DEVICE_EVENTS);

        if(CloudEvent.fromName(eventName) == CloudEvent.BPT_EVENT){
            // also broadcast the ACTION_BPT_EVENT intent ...

            EventType eventType;
            String parsedEventData;

            try {
                eventType = Util.getBptEventType(eventName, eventData);
                parsedEventData = Util.getBptEventData(eventName, eventData);
            }catch (IllegalArgumentException e){
                _log.e("Cannot send ACTION_BPT_EVENT broadcast: " + e.getMessage());
                return;
            }


            long id = DeviceEventEntry.getIdFromUri(uri);
            Uri bptEntryUri = DeviceEventEntry.buildBptDeviceEventUri(cloudDeviceId, id);


            Intent bptIntent = new Intent(IntentUtil.ACTION_BPT_EVENT, bptEntryUri);
            bptIntent.setAction(IntentUtil.ACTION_BPT_EVENT);
            bptIntent.putExtras(i);

            bptIntent.putExtra(IntentUtil.EXTRA_EVENT_NAME, parsedEventData); // event_code is parsed out from the event data
            bptIntent.putExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE, eventType);

            context.sendBroadcast(bptIntent, IntentUtil.PERMISSION_RECEIVE_EVENTS);
        }
    }

    @Nullable
    private static String getDeviceName(Context context, String cloudDeviceId) {

        String n = null;
        Uri uri = DeviceEntry.buildCloudDeviceUri(cloudDeviceId);

        Cursor c = context.getContentResolver().query(uri,
                DEVICE_COLUMNS, null, null, null);

        if(c.moveToFirst()){
            n = c.getString(COL_DEVICE_NAME);
        }
        c.close();

        return n;
    }

    /**
     * Subscribe to events using particle.io's Server-Sent-Events service
     */
    private class SubscribeToEventsTask extends Async.ApiWork<ParticleCloud, Void>{
        public SubscribeToEventsTask(Context context){
            this.mContext = context;
        }

        @Override
        public Void callApi(ParticleCloud cloud) throws ParticleCloudException, IOException {

            long subscriptionId = cloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                @Override
                public void onEventError(Exception e) {
                    _log.d("onEventError called"); //TODO
                }

                @Override
                public void onEvent(String eventName, ParticleEvent event) {
                    DeviceEventService.processCloudEvent(mContext, eventName, event);
                }
            });
            _log.d("Subscribed to cloud [subscriptionId=" + subscriptionId + "]");

            return null;
        }

        @Override
        public void onSuccess(Void aVoid) { }

        @Override
        public void onFailure(ParticleCloudException e) {
            _log.v("onFailure");
            e.printStackTrace();

            // TODO: how to handle UnknownHostException?

            throw new RuntimeException("Cannot subscribe to events from the particle.io cloud: "
                    + e.getBestMessage(), e);

        }

        private Context mContext;
    }

    private static final String[] DEVICE_COLUMNS = { BptContract.DeviceEntry.COLUMN_DEVICE_NAME };
    public static final int COL_DEVICE_NAME = 0;

    private static final TLog _log = TLog.get(DeviceEventService.class);
}




















// This is required so the service can continue to listen to cloud events
// serviceNotification = new ServiceNotification("BP Tracker", "Tracking events from the cloud", MainActivity.class);
//serviceNotification.sendNotification();

/*

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

 */