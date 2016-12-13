package com.bptracker.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.bptracker.data.BptContract;
import com.bptracker.util.IntentUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.TLog;

/**
 * Loads device information from the cloud into a content provider
 *
 * Sends a local broadcast when the devices have been loaded
 */
public class LoadDevicesService extends IntentService {

    public LoadDevicesService() {
        super("load-devices-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        _log.v("onHandleIntent called");

        ParticleCloud cloud = ParticleCloudSDK.getCloud();
        Intent resultIntent = new Intent(IntentUtil.ACTION_LOAD_DEVICES);

        try {
            int devices = loadDevices(cloud);

            resultIntent.putExtra(IntentUtil.EXTRA_INFO, devices + " devices synced");
            resultIntent.putExtra(IntentUtil.EXTRA_ACTION_SUCCESS, true);

        } catch (ParticleCloudException e) {
            _log.e("cannot load devices: " + e.getBestMessage());
            resultIntent.putExtra(IntentUtil.EXTRA_ACTION_SUCCESS, false);
            resultIntent.putExtra(IntentUtil.EXTRA_INFO, e.getBestMessage());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    // returns the number of devices loaded
    private int loadDevices(ParticleCloud cloud) throws ParticleCloudException {

        if(cloud.getAccessToken() == null || cloud.getAccessToken().length() <= 0){
            throw new ParticleCloudException(
                    new Exception("Login access token is missing"));
        }

        List<ParticleDevice> devices = cloud.getDevices();
        LongSparseArray<String> deviceIdMap = new LongSparseArray<String>();

        _log.d("Number of devices: " + devices.size());

        for(ParticleDevice device : devices){
            long deviceId = saveToProvider(device);
            deviceIdMap.put(deviceId, device.getID());
        }

        // 'expire' devices that no longer exist

        ContentValues activeFlagValue = new ContentValues();
        activeFlagValue.put(BptContract.DeviceEntry.COLUMN_IS_ACTIVE, 0);

        Uri uri = BptContract.DeviceEntry.CONTENT_URI;
        Cursor c = this.getContentResolver().query(uri, new String[] {BptContract.DeviceEntry._ID},
                null, null, null);

        while (c.moveToNext()) {
            long savedDeviceId = c.getLong(0);
            if (deviceIdMap.get(savedDeviceId) != null) {
                _log.d("device does not exist, updating is_active flag to false");
                Uri updateUri = BptContract.DeviceEntry.buildDeviceUri(savedDeviceId);
                int rowUpdated
                        = this.getContentResolver().update(uri, activeFlagValue,
                            BptContract.DeviceEntry._ID + " = ?",
                            new String[]{ Long.toString(savedDeviceId)  }
                );
                if (rowUpdated <= 0) {
                    _log.w("No row updated for device ID: " + savedDeviceId);
                }
            }
        }

        c.close();


        return devices.size();
    }


    private long saveToProvider(ParticleDevice device) throws ParticleCloudException {

        _log.v("saveToProvider called on cloud device ID: " + device.getID());

        Uri uri;
        String cloudDeviceId = device.getID();

        ContentValues v = new ContentValues();
        v.put(BptContract.DeviceEntry.COLUMN_DEVICE_NAME, device.getName());
        v.put(BptContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID, cloudDeviceId);
        v.put(BptContract.DeviceEntry.COLUMN_IS_ACTIVE, 1);
        v.put(BptContract.DeviceEntry.COLUMN_IS_CONNECTED, device.isConnected() ? 1 : 0);
        v.put(BptContract.DeviceEntry.COLUMN_SOFTWARE_NAME, ""); // TODO
        v.put(BptContract.DeviceEntry.COLUMN_SOFTWARE_VERSION, ""); // TODO

        String deviceType = PARTICLE_DEVICE_TYPE_MAP.get(device.getDeviceType());
        v.put(BptContract.DeviceEntry.COLUMN_DEVICE_TYPE, deviceType != null ? deviceType : "" );

        Cursor c = this.getContentResolver().query(
                BptContract.DeviceEntry.CONTENT_URI,
                new String[]{ BptContract.DeviceEntry._ID },
                BptContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID + " = ?",
                new String[]{cloudDeviceId},
                null
        );

        if(c.moveToFirst()){ // update record
            long deviceId = c.getLong(0);

            _log.v(device.getID() + " exists, updating record [deviceId=" + deviceId + "]");

            uri = BptContract.DeviceEntry.buildDeviceUri(deviceId);

            int rowsUpdated = this.getContentResolver().update(uri, v, null, null);
            if (rowsUpdated <= 0) {
                _log.w("No row updated for device ID: " + deviceId);
            }

        }else{ // insert new record

            _log.i("inserting device " + device.getID() );
            uri = this.getContentResolver().insert(BptContract.DeviceEntry.CONTENT_URI, v);
        }

        c.close();

        return BptContract.DeviceEntry.getDeviceIdFromUri(uri);
    }

    private static final Map<ParticleDevice.ParticleDeviceType, String> PARTICLE_DEVICE_TYPE_MAP =
            new HashMap<ParticleDevice.ParticleDeviceType, String>();

    static {
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.CORE, "CORE");
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.ELECTRON, "ELECTRON");
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.PHOTON, "PHOTON");
    }

    private static final TLog _log = TLog.get(LoadDevicesService.class);
}