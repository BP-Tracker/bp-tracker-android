package com.bptracker.service;

import android.content.Intent;
import android.os.Bundle;

import com.bptracker.util.IntentUtil;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.utils.TLog;

/**
 * Receive bpt events from Google Cloud Messaging
 * NB: requires a webhook on particle.io
 *
 * Message Format:
     "message": {
        "coreid": "{{PARTICLE_DEVICE_ID}}",
        "ttl": "60",
        "data": "{{PARTICLE_EVENT_VALUE}}",
        "published_at":"{{PARTICLE_PUBLISHED_AT}}",
        "name": "{{PARTICLE_EVENT_NAME}}"
     }
 */
public class GcmPushService extends GcmListenerService {

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *
     *
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        _log.v("[from=" + from + "] message:" + message);

        try{
            JSONObject payload = new JSONObject(message);

            String eventName = payload.getString("name");
            String dataPayload = payload.getString("data");
            String deviceId = payload.getString("coreid");
            int timeToLive = Integer.parseInt(payload.getString("ttl"));

            String dateStr = payload.getString("published_at"); //ex: 2016-12-20T20:50:19.222Z
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

            Date publishedAt = dateFormat.parse(dateStr);

            ParticleEvent e = new ParticleEvent(deviceId, dataPayload, publishedAt, timeToLive);

            Intent intent = new Intent(getApplicationContext(), DeviceEventService.class);
            intent.putExtra(IntentUtil.EXTRA_PARTICLE_EVENT, e);
            intent.putExtra(IntentUtil.EXTRA_EVENT_NAME, eventName);

            startService(intent);

        } catch (JSONException|ParseException e) {
            _log.e("Cannot parse GCM message: " + e.getMessage());
            e.printStackTrace();
            //TODO: better error handling
        }
    }

    private static final TLog _log = TLog.get(GcmPushService.class);
}


