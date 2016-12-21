package com.bptracker.receiver;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bptracker.R;
import com.bptracker.SelectStateActivity;
import com.bptracker.data.LocationProvider;
import com.bptracker.firmware.Firmware;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.core.BptApi;
import com.bptracker.firmware.core.Function;
import com.bptracker.service.RunFunctionService;
import com.bptracker.util.EventNotification;
import com.bptracker.util.IntentUtil;
import com.google.android.gms.common.ConnectionResult;

import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 *
 * Processes bpt:events such as REQUEST_GPS and PANIC
 */
public class BptEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        EventType type = (EventType) intent.getSerializableExtra(IntentUtil.EXTRA_BPT_EVENT_TYPE);
        String deviceName = intent.getStringExtra(IntentUtil.EXTRA_DEVICE_NAME);
        final String deviceId = intent.getStringExtra(IntentUtil.EXTRA_DEVICE_ID);
        String eventData = intent.getStringExtra(IntentUtil.EXTRA_EVENT_DATA);

        _log.v("onReceive called on action: " + intent.getAction()
                + " [eventType=" + type.name() + "] [eventData=" + eventData + "]");

        String[] data = eventData.split(",");

        // ack[,data1[,data2..]]
        switch (type){
            case PANIC: //ack, isMoving, something, lat, log

                double lat = Double.valueOf(data[1]);
                double lon = Double.valueOf(data[2]);

                String title = deviceName + " raised a panic alarm";
                String message = "The last update was recorded at 5:30pm";

                EventNotification n = new EventNotification(context, title, message);
                n.sendPanic(deviceId, deviceName, lat, lon);

                break;
            case REQUEST_GPS:

                //TODO: ensure device is permitted to receive coordinates

                // send the location to the requesting device
                LocationProvider p = new LocationProvider(context);
                p.getLocation(new LocationProvider.Callback() {

                    @Override
                    public void onLocation(LocationProvider p, Location location) {

                        float lat = Double.valueOf(location.getLatitude()).floatValue();
                        float lon = Double.valueOf(location.getLongitude()).floatValue();

                        Function f = BptApi.createFunction(Firmware.Function.BPT_ACK, deviceId);
                        f.addArgument(BptApi.ARG_EVENT_TYPE, EventType.REQUEST_GPS);
                        f.addArgument(BptApi.ARG_STRING_DATA, String.format("%f,%f", lat, lon));

                        Intent i = new Intent(context, RunFunctionService.class);
                        i.putExtra(IntentUtil.EXTRA_FUNCTION, f);

                        context.startService(i);
                    }

                    @Override
                    public void onLocationError(LocationProvider p, int requestCode, @Nullable ConnectionResult result) {
                        _log.e("onLocationError " + requestCode); //TODO
                    }
                });

                break;

            case SERIAL_COMMAND:
                _log.i("SERIAL_COMMAND issued on " + data[2] + " [result=" + data[1] + "]");
                break;
        }
    }

    private static final TLog _log = TLog.get(BptEventReceiver.class);
}