package com.bptracker.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bptracker.R;
import com.bptracker.TrackerApplication;
import com.bptracker.data.BptContract;
import com.bptracker.data.BptContract.DeviceEntry;
import com.bptracker.data.BptContract.DeviceEventEntry;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Util;
import com.bptracker.util.IntentUtil;

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

    private static final String BPT_PREFIX = "bpt:";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _log.d("onStartCommand called");

        // This is required so the service can continue to listen to cloud events
        // serviceNotification = new ServiceNotification("BP Tracker", "Tracking events from the cloud", MainActivity.class);
        //serviceNotification.sendNotification();

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


        private final String[] DEVICE_COLUMNS = {
                BptContract.DeviceEntry.COLUMN_DEVICE_NAME,
        };

        // These indices are tied to DEVICE_COLUMNS.
        public static final int COL_DEVICE_NAME = 0;


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

                /**
                 * Called when a device event is received from the particle cloud. This method
                 * inserts data for content providers and also broadcasts IntentUtil.ACTION_DEVICE_EVENT
                 * and if applicable IntentUtil.ACTION_BPT_EVENT intents.
                 * @param eventName The event name that was recieved
                 * @param particleEvent The event object
                 */
                @Override
                public void onEvent(String eventName, ParticleEvent particleEvent) {
                    _log.d("onEvent called [eventName=" + eventName
                            + "] [cloudDeviceId=" + particleEvent.deviceId + "] [particleData="
                            + particleEvent.dataPayload + "]");

                    String eventData = particleEvent.dataPayload;
                    String cloudDeviceId = particleEvent.deviceId;

                    ContentValues v = new ContentValues();
                    v.put(DeviceEventEntry.COLUMN_CLOUD_DEVICE_ID, cloudDeviceId);
                    v.put(DeviceEventEntry.COLUMN_PUBLISH_DATE, particleEvent.publishedAt.getTime());
                    v.put(DeviceEventEntry.COLUMN_EVENT_DATA, eventData);
                    v.put(DeviceEventEntry.COLUMN_EVENT_NAME, eventName);

                    Uri uri = mContext.getContentResolver().insert(DeviceEventEntry.CONTENT_URI, v);

                    _log.d("inserted event (" + eventName + "): "  + uri.toString() );


                    Intent i = new Intent(IntentUtil.ACTION_DEVICE_EVENT, uri);
                    i.putExtra(IntentUtil.EXTRA_FROM_BPT_DEVICE, eventName.startsWith(BPT_PREFIX) );

                    i.putExtra(IntentUtil.EXTRA_DEVICE_ID, cloudDeviceId);
                    i.putExtra(IntentUtil.EXTRA_EVENT_NAME, eventName);
                    i.putExtra(IntentUtil.EXTRA_EVENT_DATA, eventData);

                    String deviceName = getDeviceName(cloudDeviceId);

                    if(!TextUtils.isEmpty(deviceName)){
                        i.putExtra(IntentUtil.EXTRA_DEVICE_NAME, deviceName);
                    }else{
                        _log.w("Device name is empty for cloud device ID: " + cloudDeviceId);
                    }

                    mContext.sendBroadcast(i, IntentUtil.PERMISSION_RECEIVE_DEVICE_EVENTS);
                    //LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);

                    if(CloudEvent.fromName(eventName) == CloudEvent.BPT_EVENT){
                       // also broadcast the ACTION_BPT_EVENT intent ...

                        EventType event;
                        String parsedEventData;

                        try {
                            event = Util.getBptEventType(eventName, eventData);
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
                        bptIntent.putExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE, event);

                        //TODO: send local broadcast instead ???
                        //LocalBroadcastManager.getInstance(mContext).sendBroadcast(bptIntent);
                        mContext.sendBroadcast(bptIntent, IntentUtil.PERMISSION_RECEIVE_EVENTS);

                    }

                }
            });
            _log.d("Subscribed to cloud [subscriptionId=" + subscriptionId + "]");

            return null;
        }

        @Nullable
        private String getDeviceName(String cloudDeviceId) {

            String n = null;
            Uri uri = DeviceEntry.buildCloudDeviceUri(cloudDeviceId);

            Cursor c = mContext.getContentResolver().query(uri,
                    DEVICE_COLUMNS, null, null, null);

            if(c.moveToFirst()){
                n = c.getString(COL_DEVICE_NAME);
            }
            c.close();

            return n;
        }

        @Override
        public void onSuccess(Void aVoid) { //TODO?

        }

        @Override
        public void onFailure(ParticleCloudException e) {
            _log.d("onFailure");
            e.printStackTrace();

            // TODO: how to handle UnknownHostException?

            throw new RuntimeException("Cannot subscribe to events from the particle.io cloud: "
                    + e.getBestMessage(), e);

        }

        private Context mContext;
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
    private static final TLog _log = TLog.get(DeviceEventService.class);
}
