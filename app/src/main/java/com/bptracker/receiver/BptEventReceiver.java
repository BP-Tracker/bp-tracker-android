package com.bptracker.receiver;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import io.particle.android.sdk.utils.TLog;

import com.bptracker.R;
import com.bptracker.SelectStateActivity;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.util.IntentUtil;

/**
 * Author: Derek Benda
 */

public class BptEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        EventType type = (EventType) intent.getSerializableExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE);
        String deviceName = intent.getStringExtra(IntentUtil.EXTRA_DEVICE_NAME);
        String deviceId = intent.getStringExtra(IntentUtil.EXTRA_DEVICE_ID);
        String eventData = intent.getStringExtra(IntentUtil.EXTRA_EVENT_DATA);

        _log.d("onReceive called on action: " + intent.getAction()
                + " [eventType=" + type.name() + "] [eventData=" + eventData + "]");

        // ack[,data1[,data2..]]
        String[] data = eventData.split(",");

        EventNotification n = new EventNotification(context, deviceName + " raised a panic alarm",
                "The last update was recorded at 5:30pm", "",  deviceId,  SelectStateActivity.class);

        n.send();




        switch (type){
            case PANIC: //ack, isMoving, something, lat, log



                break;


            case SERIAL_COMMAND:
                _log.i("SERIAL_COMMAND issued on " + data[1] + " [result=" + data[2] + "]");
                break;
        }

    }

    private static class EventNotification {
        private String mTitleMessage;
        private String mTextMessage;
        private String mCloudDeviceId;
        private Class mActivityToCall;
        private Context mContext;
        private String mGeo;
        private static final int NOTIFICATION_ID = 567766;

        public EventNotification(Context context, String titleMessage, String textMessage, String geo, String cloudDeviceId,
                                 Class<? extends Activity> activityToCall) {
            this.mContext = context;
            this.mTitleMessage = titleMessage;
            this.mTextMessage = textMessage;
            this.mActivityToCall = activityToCall;
            this.mCloudDeviceId = cloudDeviceId;
            this.mGeo = geo;
        }

        public void send(){
            NotificationCompat.Builder builder = getNotificationBuilder();


            Intent i = new Intent(mContext, mActivityToCall);
            //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            /*
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addParentStack(mActivityToCall);
            stackBuilder.addNextIntent(i);
            PendingIntent pendingIntent
                    = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    */

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);

            builder.setContentIntent(pendingIntent);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            Uri geoUri = Uri.parse("geo:0,0?q=" + mGeo);

            mapIntent.setData(geoUri);
            PendingIntent mapPendingIntent = PendingIntent.getActivities(
                    mContext, 0, new Intent[]{ mapIntent }, 0);

            builder.addAction(R.drawable.online_dot, "Map", mapPendingIntent);


            Intent stateChooserIntent = new Intent(mContext, SelectStateActivity.class);
            stateChooserIntent.putExtra(IntentUtil.EXTRA_DEVICE_ID, mCloudDeviceId);


            //stateChooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //stateChooserIntent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent setStateIntent = PendingIntent.getActivities(
                    mContext, 0, new Intent[]{stateChooserIntent}, PendingIntent.FLAG_UPDATE_CURRENT
            );



            builder.addAction(R.drawable.offline_dot, "Disarm", setStateIntent);


            NotificationManager notificationManager
                    = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(NOTIFICATION_ID, builder.build() );
        }

        protected NotificationCompat.Builder getNotificationBuilder() {
            final NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(mContext);

            builder.setSmallIcon(R.drawable.device_image_small);
            builder.setContentTitle(mTitleMessage);
            builder.setContentText(mTextMessage);
            //builder.setAutoCancel(true);
            //builder.setContentInfo("Info");

            return builder;
        }
    }



    private static final TLog _log = TLog.get(BptEventReceiver.class);
}