package com.bptracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.TLog;

/**
 * Author: Derek Benda
 * Loads device information from the cloud into a content provider
 *
 * Usage: Async.executeAsync(cloud, new LoadDevicesTask());
 */
public class LoadDevicesTask extends Async.ApiWork<ParticleCloud, Void>{

    public LoadDevicesTask(Context context){
        this.context = context;
    }


    @Override
    public Void callApi(ParticleCloud cloud)
                    throws ParticleCloudException, IOException {

        if(cloud.getAccessToken() == null || cloud.getAccessToken().length() <= 0){
            throw new ParticleCloudException(
                    new Exception("Login access token is missing"));
        }

        List<ParticleDevice> devices = cloud.getDevices();
        Map<Long, String> deviceIdMap = new HashMap<Long, String>();

        _log.d("Number of devices: " + devices.size());

        for(ParticleDevice device : devices){
            long deviceId = saveToProvider(device);
            deviceIdMap.put(deviceId, device.getID());
        }

        // 'expire' devices that no longer exist

        ContentValues activeFlagValue = new ContentValues();
        activeFlagValue.put(DeviceContract.DeviceEntry.COLUMN_IS_ACTIVE, 0);

        Uri uri = DeviceContract.DeviceEntry.CONTENT_URI;
        Cursor c = this.context.getContentResolver().query(uri,
                new String[] {DeviceContract.DeviceEntry._ID},
                null, null, null);

        while (c.moveToNext()) {
            long savedDeviceId = c.getLong(0);
            if (!deviceIdMap.containsKey(savedDeviceId)) {
                _log.d("device does not exist, updating is_active flag to false");
                Uri updateUri = DeviceContract.DeviceEntry.buildDeviceUri(savedDeviceId);
                int rowUpdated
                        = this.context.getContentResolver().update(uri, activeFlagValue,
                            DeviceContract.DeviceEntry._ID + " = ?",
                            new String[]{ Long.toString(savedDeviceId)  }
                            );
                if (rowUpdated <= 0) {
                    _log.w("No row updated for device ID: " + savedDeviceId);
                }
            }
        }

        c.close();
        return null;
    }

    @Override
    public void onSuccess(Void result) {
        _log.d("onSuccess called");
    }

    @Override
    public void onFailure(ParticleCloudException exception) {
        _log.e("onFailure: " + exception.getBestMessage());
        // TODO: ??
    }

    private long saveToProvider(ParticleDevice device)
            throws ParticleCloudException {

        _log.d("saveToProvider called on cloud device ID: " + device.getID());

        Uri uri;
        String cloudDeviceId = device.getID().toUpperCase();

        ContentValues v = new ContentValues();
        v.put(DeviceContract.DeviceEntry.COLUMN_DEVICE_NAME, device.getName());
        v.put(DeviceContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID, cloudDeviceId);
        v.put(DeviceContract.DeviceEntry.COLUMN_IS_ACTIVE, 1);
        v.put(DeviceContract.DeviceEntry.COLUMN_IS_CONNECTED, device.isConnected() ? 1 : 0);
        v.put(DeviceContract.DeviceEntry.COLUMN_SOFTWARE_NAME, ""); // TODO
        v.put(DeviceContract.DeviceEntry.COLUMN_SOFTWARE_VERSION, ""); // TODO

        String deviceType = PARTICLE_DEVICE_TYPE_MAP.get(device.getDeviceType());
        v.put(DeviceContract.DeviceEntry.COLUMN_DEVICE_TYPE, deviceType != null ? deviceType : "" );

        Cursor c = this.context.getContentResolver().query(
                DeviceContract.DeviceEntry.CONTENT_URI,
                new String[]{ DeviceContract.DeviceEntry._ID },
                DeviceContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID + " = ?",
                new String[]{cloudDeviceId},
                null
        );

        if(c.moveToFirst()){ // update record
            long deviceId = c.getLong(0);

            _log.d(device.getID() + " exists, updating record [deviceId=" + deviceId + "]");

            uri = DeviceContract.DeviceEntry.buildDeviceUri(deviceId);

            int rowsUpdated = context.getContentResolver().update(uri, v, null, null);
            if (rowsUpdated <= 0) {
                _log.w("No row updated for device ID: " + deviceId);
            }

        }else{ // insert new record

            _log.d("inserting device " + device.getID() );
            uri = context.getContentResolver().insert(
                    DeviceContract.DeviceEntry.CONTENT_URI, v);
        }

        c.close();

        return DeviceContract.DeviceEntry.getDeviceIdFromUri(uri);
    }

    private static final Map<ParticleDevice.ParticleDeviceType, String> PARTICLE_DEVICE_TYPE_MAP =
        new HashMap<ParticleDevice.ParticleDeviceType, String>();

    static {
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.CORE, "CORE");
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.ELECTRON, "ELECTRON");
        PARTICLE_DEVICE_TYPE_MAP.put(ParticleDevice.ParticleDeviceType.PHOTON, "PHOTON");
    }

    private Context context;
    private static final TLog _log = TLog.get(LoadDevicesTask.class);
}
