package com.bptracker.fragment;


import android.app.Fragment;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bptracker.R;
import com.bptracker.data.BptContract.DeviceEntry;
import com.bptracker.data.BptContract.DeviceEventEntry;
import com.bptracker.firmware.Firmware.CloudEvent;
import com.bptracker.firmware.Firmware.ControllerMode;
import com.bptracker.firmware.Firmware.EventType;
import com.bptracker.firmware.Firmware.State;
import com.bptracker.firmware.Util;
import com.bptracker.ui.gauges.GradientCircleGauge;

import io.particle.android.sdk.utils.TLog;


public class DeviceFragment extends Fragment {

    private ContentObserver mDeviceEventObserver;
    private String mDeviceName;
    private Uri mDeviceUri;
    private Uri mBptEventEventUri;

    public static class ViewHolder {
        final View topLevel;
        final TextView deviceStatus;
        final GradientCircleGauge batteryGauge;
        final GradientCircleGauge trackerGauge;

        public ViewHolder(View view) {
            topLevel = view;
            deviceStatus = (TextView) view.findViewById(R.id.tv_device_status);
            batteryGauge = (GradientCircleGauge) view.findViewById(R.id.gcg_battery_gauge);
            trackerGauge = (GradientCircleGauge) view.findViewById(R.id.gcg_tracker_gauge);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        _log.v("onResume");

        getActivity().getContentResolver().registerContentObserver(
                mBptEventEventUri, true, mDeviceEventObserver);


        ViewHolder holder = (ViewHolder) getView().getTag();
        holder.deviceStatus.setText("ARMED");

        //holder.batteryGauge.setGaugeLevel(100, false);
        holder.batteryGauge.setGaugeLevel(87);
        holder.trackerGauge.setGaugeLevel(50);
        //holder.trackerGauge.setAdditionalValue("NO");

        holder.trackerGauge.setAdditionalLabel("MONITORING");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        _log.v("onCreateOptionsMenu called");
        inflater.inflate(R.menu.device_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
//        if(id == R.id.action_refresh){
//            refreshDevicesFromCloud();
//            return true;
//        } else if (id == R.id.action_logout) {
//            logoutAndRedirectToLogin();
//            return true;
//        }

       /* if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();
        _log.v("onPause");

        getActivity().getContentResolver().unregisterContentObserver(mDeviceEventObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mDeviceUri = arguments.getParcelable(DeviceFragment.DEVICE_URI_PARM);

            String cloudDeviceId = DeviceEntry.getCloudDeviceIdFromUri(mDeviceUri);

            mBptEventEventUri = DeviceEventEntry.buildDeviceEventUri(
                    cloudDeviceId, CloudEvent.BPT_EVENT.getEventName());

            mDeviceName = arguments.getString(DeviceFragment.DEVICE_NAME);
        }

        View v = inflater.inflate(R.layout.fragment_device, container, false);
        ViewHolder holder = new ViewHolder(v);
        v.setTag(holder);

        Handler h = new Handler(Looper.getMainLooper());
        mDeviceEventObserver = new BptEventObserver(h, getActivity(), holder);

        return v;
    }


    private static class BptEventObserver extends ContentObserver {

        private static final String[] COLUMNS = {
                DeviceEventEntry.COLUMN_EVENT_NAME,
                DeviceEventEntry.COLUMN_EVENT_DATA,
                DeviceEventEntry.COLUMN_PUBLISH_DATE
        };

        public static final int COL_EVENT_NAME = 0;
        public static final int COL_EVENT_DATA = 1;
        public static final int COL_PUBLISH_DATE = 2;

        private ViewHolder mViewHolder;
        private Context mContext;

        public BptEventObserver(Handler handler, Context context, ViewHolder viewHolder) {
            super(handler);

            mViewHolder = viewHolder;
            mContext = context;
        }

        /**
         * Called when a bpt:event or bpt:status event is received from the device
         *
         * @param selfChange
         * @param uri   URI format: content://com.bptracker/events/[cloud_device_id]/bpt%3Aevent/[event_id]
         */
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            _log.v("BptEventObserver onChange URI: " + uri.toString());

            Cursor c = mContext.getContentResolver().query(uri, COLUMNS, null, null, null);
            if(c != null && c.moveToFirst()){

                String eventData = c.getString(COL_EVENT_DATA);
                String eventName = c.getString(COL_EVENT_NAME);
                int publishDate = c.getInt(COL_PUBLISH_DATE);
                c.close();

                if(!Util.isBptEvent(eventName)){
                    _log.e("URI " + uri.toString() + " is not supported");
                    return;
                }

                _log.v("BptEventObserver [name=" + eventName + "][data=" + eventData + "]");

                EventType type = Util.getBptEventType(eventName, eventData);
                String data = Util.getBptEventData(eventName, eventData);
                String[] tokens = data.split(",");

                switch (type) {
                    case STATUS_UPDATE: // format: ack,controller_mode,state,batt(%),signal(%),is_armed

                        ControllerMode mode = ControllerMode.fromMode(Integer.parseInt(tokens[1]));
                        State state = State.getState(Integer.parseInt(tokens[2]));
                        int batteryLevel = (int) Float.parseFloat(tokens[3]);
                        int signalLevel = (int) Float.parseFloat(tokens[4]);
                        String isMonitoring = (int) Float.parseFloat(tokens[5]) == 1
                                ? "MONITORING" : "DISABLED";

                        mViewHolder.deviceStatus.setText( state.name() );
                        mViewHolder.batteryGauge.setGaugeLevel( batteryLevel );
                        mViewHolder.trackerGauge.setGaugeLevel( signalLevel );
                        mViewHolder.trackerGauge.setAdditionalLabel( isMonitoring );

                        break;
                    case BATTERY_LOW: // in %  //TODO:

                        break;

                    case STATE_CHANGE:
                        State newState = State.getState(Integer.parseInt(tokens[2]));

                        mViewHolder.deviceStatus.setText( newState.name() );
                        break;
                }

            }
        }

    }

    // URI format must be com.bptracker/devices/cloud-device-id/*
    public static final String DEVICE_URI_PARM = "DEVICE_URI_PARM";

    public static final String DEVICE_NAME = "DEVICE_NAME";
    private static final TLog _log = TLog.get(DeviceFragment.class);
}




 /*

  private void processBptStatus(String eventData, int publishDate){
            _log.v("processBptStatus: " + eventData);

            String[] tokens = eventData.split(",");

            ControllerMode mode = ControllerMode.fromMode(Integer.parseInt(tokens[0]));
            State state = State.getState(Integer.parseInt(tokens[1]));
            int batteryLevel = (int) Float.parseFloat(tokens[2]);
            int satellites = Integer.parseInt(tokens[3]);
            float lat = Float.parseFloat(tokens[4]);
            float lon = Float.parseFloat(tokens[5]);

            mViewHolder.deviceStatus.setText( state.name() );
            mViewHolder.batteryGauge.setGaugeLevel( batteryLevel );
            //mViewHolder.signalGauge.setGaugeLevel(); //TODO

        }

  // bpt:event data format: ...
        private void processBptEvent(EventType type, String eventData, int publishDate){
            _log.v("processBptEvent: " + eventData);
            String[] tokens = eventData.split(",");

            switch(type){

                case BATTERY_LOW: // in %

                    //TODO:

                    break;

                case STATE_CHANGE:
                    State cState = State.getState(Integer.parseInt(tokens[1]));

                    mViewHolder.deviceStatus.setText( cState.name() );
                    break;
            }

        }

                CloudEvent event = CloudEvent.fromName(eventName);
                switch (event){

                    case BPT_STATUS:
                        processBptStatus(eventData, publishDate);
                        break;

                    case BPT_EVENT:

                        String data = Util.getBptEventData(eventName, eventData );
                        EventType type = Util.getBptEventType(eventName, eventData);

                        processBptEvent(type, data, publishDate);
                        break;

                    default:
                        _log.e("URI " + uri.toString() + " is not supported");
                }
                */