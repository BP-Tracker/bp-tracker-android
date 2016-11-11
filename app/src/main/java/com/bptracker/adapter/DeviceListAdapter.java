package com.bptracker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bptracker.R;
import com.bptracker.data.DeviceContract;
import com.bptracker.fragment.DeviceListFragment;

import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.TLog;

import static io.particle.android.sdk.utils.Py.truthy;

/**
 * Author: Derek Benda
 */
public class DeviceListAdapter extends CursorAdapter {


    /*//TODO: refactor
    public static ParticleDevice getParticleDevice(Cursor cursor){

        int type = cursor.getInt(DeviceListFragment.COL_DEVICE_TYPE);

        ParticleDevice.ParticleDeviceType deviceType
                = ParticleDevice.ParticleDeviceType.fromInt(type);

        String deviceId = cursor.getString(DeviceListFragment.COL_DEVICE_ID);
        String deviceName = cursor.getString(DeviceListFragment.COL_DEVICE_NAME);
    }*/

    public static class ViewHolder {
        final View topLevel;
        final TextView modelName;
        final ImageView productImage;
        final TextView deviceName;
        final TextView statusTextWithIcon;
        final TextView productId;
        final ImageView overflowMenuIcon;

        public ViewHolder(View view) {
            topLevel = view;
            modelName = (TextView) view.findViewById(R.id.product_model_name);
            productImage = (ImageView) view.findViewById(R.id.product_image);
            deviceName = (TextView) view.findViewById(R.id.product_name);
            statusTextWithIcon = (TextView) view.findViewById(R.id.online_status);
            productId = (TextView) view.findViewById(R.id.product_id);
            overflowMenuIcon = (ImageView) view.findViewById(R.id.context_menu);
        }
    }

    public DeviceListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.device_list_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        _log.d("bindView called");

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int position = cursor.getPosition();

        if (defaultBackground == null) {
            defaultBackground = viewHolder.topLevel.getBackground();
        }

        if (position % 2 == 0) {
            viewHolder.topLevel.setBackgroundResource(R.color.shaded_background);
        } else {
            viewHolder.topLevel.setBackground(defaultBackground);
        }

        String deviceType = cursor.getString(DeviceListFragment.COL_DEVICE_TYPE);
        viewHolder.modelName.setText(deviceType);

        /*
        ParticleDevice.ParticleDeviceType deviceType
                = ParticleDevice.ParticleDeviceType.fromInt(type);
        if(deviceType == ParticleDevice.ParticleDeviceType.CORE){
            viewHolder.modelName.setText("Core");

        }else if(deviceType == ParticleDevice.ParticleDeviceType.ELECTRON){
            viewHolder.modelName.setText("Electron");

        }else if(deviceType == ParticleDevice.ParticleDeviceType.PHOTON){
            viewHolder.modelName.setText("Photon");

        }else{
            viewHolder.modelName.setText("N/A");
        }
        */

        int dot;
        String connected_status = "";

        //TODO: turn COL into boolean
        if(cursor.getInt(DeviceListFragment.COL_IS_CONNECTED) > 0){
            dot = R.drawable.online_dot;
            connected_status = "Online";
        }else{
            dot = R.drawable.offline_dot;
            connected_status = "Offline";
        }

        viewHolder.statusTextWithIcon.setText(connected_status);
        viewHolder.statusTextWithIcon.setCompoundDrawablesWithIntrinsicBounds(0, 0, dot, 0);

        String deviceId = cursor.getString(DeviceListFragment.COL_CLOUD_DEVICE_ID);
        viewHolder.productId.setText(deviceId.toUpperCase());

        Context ctx = viewHolder.topLevel.getContext();
        String deviceName = cursor.getString(DeviceListFragment.COL_DEVICE_NAME);

        viewHolder.deviceName.setText(
                deviceName != null && deviceName.length() > 0
                        ? deviceName : ctx.getString(R.string.unnamed_device) );

        final int deviceEntryId = cursor.getInt(DeviceListFragment.COL_DEVICE_ENTRY_ID);
        viewHolder.overflowMenuIcon.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenu(view, deviceEntryId);
                }
            }
        );


    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    private void showMenu(View v, int deviceEntryId) {
        _log.d("Show menu called on device entry id: " + deviceEntryId);
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        //popup.inflate(R.menu.context_device_row);
        //popup.setOnMenuItemClickListener(DeviceActionsHelper.buildPopupMenuHelper(activity, device));
        //popup.show();
    }

    private Drawable defaultBackground;
    private static final int VIEW_TYPE_COUNT = 1;
    private static final TLog _log = TLog.get(DeviceListAdapter.class);
}
