package com.bptracker.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bptracker.DeviceActivity;
import com.bptracker.DeviceLocationActivity;
import com.bptracker.MainActivity;
import com.bptracker.R;
import com.bptracker.SelectStateActivity;
import com.bptracker.data.BptContract;
import com.google.android.gms.maps.model.LatLng;


public class EventNotification {
    private String mTitleMessage;
    private String mTextMessage;
    private Class mActivityToCall;
    private Context mContext;
    private static final int PANIC_NOTIFICATION_ID = 567766;

    public EventNotification(Context context, String title, String message) {
        init(context, title, message);
        mActivityToCall = MainActivity.class;
    }

    public EventNotification(Context context, String title, String message, Class<? extends Activity> activityToCall) {
        init(context, title, message);
        mActivityToCall = activityToCall;
    }

    private void init(Context context, String title, String message) {
        mContext = context;
        mTitleMessage = title;
        mTextMessage = message;
    }

    public void sendPanic(String cloudDeviceId, String deviceName, double lat, double lon){


        NotificationCompat.Builder builder = getNotificationBuilder();


        Intent i = new Intent(mContext, mActivityToCall);

        if (mActivityToCall == DeviceActivity.class) {
            i.setData(BptContract.DeviceEntry.buildCloudDeviceUri(cloudDeviceId));
            i.putExtra(IntentUtil.EXTRA_DEVICE_NAME, deviceName);
        }


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

        Intent mapIntent = new Intent(mContext, DeviceLocationActivity.class);



        mapIntent.putExtra(IntentUtil.EXTRA_LAT_LNG, new LatLng(lat, lon));
        mapIntent.putExtra(IntentUtil.EXTRA_DEVICE_ID, cloudDeviceId);
        mapIntent.putExtra(IntentUtil.EXTRA_DEVICE_NAME, deviceName);
        mapIntent.putExtra(IntentUtil.EXTRA_INFO, "More Info"); //TODO
        //mapIntent.
        //Uri geoUri = Uri.parse("geo:0,0?q=" + mGeo);
        //mapIntent.setData(geoUri);

        PendingIntent mapPendingIntent = PendingIntent.getActivities(
                mContext, 0, new Intent[]{ mapIntent }, 0);

        builder.addAction(R.drawable.online_dot, "Location", mapPendingIntent);


        Intent stateChooserIntent = new Intent(mContext, SelectStateActivity.class);
        stateChooserIntent.putExtra(IntentUtil.EXTRA_DEVICE_ID, cloudDeviceId);


        //stateChooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //stateChooserIntent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent setStateIntent = PendingIntent.getActivities(
                mContext, 0, new Intent[]{stateChooserIntent}, PendingIntent.FLAG_UPDATE_CURRENT
        );



        builder.addAction(R.drawable.offline_dot, "Disarm", setStateIntent);


        NotificationManager notificationManager
                = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(PANIC_NOTIFICATION_ID, builder.build() );
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