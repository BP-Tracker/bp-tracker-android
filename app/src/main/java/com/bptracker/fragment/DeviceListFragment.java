package com.bptracker.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bptracker.R;
import com.bptracker.SettingsActivity;
import com.bptracker.TrackerApplication;
import com.bptracker.adapter.DeviceListAdapter;
import com.bptracker.data.BptContract;
import com.bptracker.service.LoadDevicesService;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;


public class DeviceListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {


    public interface Callbacks {

        /**
         * Called when a device is selected from the list
         *
         * @param deviceUri URI with the format: com.bptracker/devices/cloud-device-id/*
         * @param deviceName The name of the device
         */
        void onDeviceSelected(Uri deviceUri, String deviceName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        _log.v("onCreateView called");

        View rootView = inflater.inflate(R.layout.fragment_device_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        adapter = new DeviceListAdapter(getActivity(), null, 0);

        listView = (ListView) rootView.findViewById(R.id.device_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                _log.v("onItemClick for device [position=" + position + "]");

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                String cloudDeviceId = cursor.getString(COL_CLOUD_DEVICE_ID);

                String name = cursor.getString(COL_DEVICE_NAME);

                _log.v("Device entry ID = " + cursor.getLong(COL_DEVICE_ENTRY_ID));

                // URI format: com.bptracker/devices/cloud-device-id/*
                ((Callbacks) getActivity())
                        .onDeviceSelected(
                                BptContract.DeviceEntry.buildCloudDeviceUri(cloudDeviceId), name);

            }
        });

        return rootView;
    }

    // SwipeRefreshLayout.OnRefreshListener
    @Override
    public void onRefresh() {
        _log.v("onRefresh called");

        if(mIsRefreshing){
            return;
        }

        refreshDevicesFromCloud();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        _log.d("onActivityCreated called");
        getLoaderManager().initLoader(DEVICE_LOADER_ID, null, this);

        /*
        if(!getLoaderManager().getLoader(DEVICE_LOADER_ID).isReset()) {
            _log.d("Loader was not reset");
            getLoaderManager().restartLoader(DEVICE_LOADER_ID, null, this);
        }
        */

        super.onActivityCreated(savedInstanceState);
    }

    // TODO: is this needed?
    public boolean onBackPressed() {
        return false;
    }

    // LoaderManager.LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        _log.d("onCreateLoader called");

        return new CursorLoader(getActivity(),
                BptContract.DeviceEntry.buildDeviceUri(),
                DEVICE_COLUMNS,
                null,
                null,
                BptContract.DeviceEntry.COLUMN_DEVICE_NAME + " ASC");
    }

    // LoaderManager.LoaderCallbacks
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    // LoaderManager.LoaderCallbacks
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        _log.v("onCreateOptionsMenu called");
        inflater.inflate(R.menu.device_list_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_refresh){
            refreshDevicesFromCloud();
            return true;
        } else if (id == R.id.action_logout) {
            logoutAndRedirectToLogin();
            return true;
        } else if (id == R.id.action_general_settings) {

            Intent i = new Intent(this.getActivity(), SettingsActivity.class);
            startActivity(i);

            return true;

        }

       /* if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void logoutAndRedirectToLogin(){ // TODO: add confirmation dialog
        _log.v("logoutAndRedirectToLogin called");

        TrackerApplication tracker = (TrackerApplication) getActivity().getApplicationContext();

        getActivity().finishAndRemoveTask();
        tracker.logoutAndRedirect();
    }


    private void refreshDevicesFromCloud(){
        _log.v("refreshDevicesFromCloud called");

        if (mIsRefreshing) {
            _log.w("refresh is already is in progress");
            return;
        }

        mIsRefreshing = true;

        Intent intent = new Intent(getActivity(), LoadDevicesService.class);
        getActivity().startService(intent);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsRefreshing = false;
                mSwipeRefreshLayout.setRefreshing(false);

                if(!intent.getBooleanExtra(IntentUtil.EXTRA_ACTION_SUCCESS, false)){
                    _log.w("cannot refresh devices: " + intent.getStringExtra(IntentUtil.EXTRA_ACTION_ERROR));
                }

                LocalBroadcastManager.getInstance(DeviceListFragment.this.getActivity())
                        .unregisterReceiver(this);
            }
        };

        IntentFilter filter = new IntentFilter(IntentUtil.ACTION_LOAD_DEVICES);
        LocalBroadcastManager.getInstance(this.getActivity())
                .registerReceiver(receiver, filter);

        //Intent intent = new Intent(getActivity(), LoadDevicesService.class);
        //getActivity().startService(intent);
    }


    // Specify the columns we need.
    private static final String[] DEVICE_COLUMNS = {
            BptContract.DeviceEntry._ID,
            BptContract.DeviceEntry.COLUMN_DEVICE_NAME,
            BptContract.DeviceEntry.COLUMN_DEVICE_TYPE,
            BptContract.DeviceEntry.COLUMN_CLOUD_DEVICE_ID,
            BptContract.DeviceEntry.COLUMN_IS_CONNECTED,
            BptContract.DeviceEntry.COLUMN_SOFTWARE_NAME,
            BptContract.DeviceEntry.COLUMN_SOFTWARE_VERSION,
    };

    // These indices are tied to DEVICE_COLUMNS.  If DEVICE_COLUMNS changes, these
    // must change.
    public static final int COL_DEVICE_ENTRY_ID = 0;
    public static final int COL_DEVICE_NAME = 1;
    public static final int COL_DEVICE_TYPE = 2;
    public static final int COL_CLOUD_DEVICE_ID = 3;
    public static final int COL_IS_CONNECTED = 4;
    public static final int COL_SOFTWARE_NAME = 5;
    public static final int COL_SOFTWARE_VERSION = 6;

    // for LoaderManager
    private static final int DEVICE_LOADER_ID = 0;


    private DeviceListAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    private boolean mIsRefreshing;
    private static final TLog _log = TLog.get(DeviceListFragment.class);
}

























/*
//RecyclerView rv = (RecyclerView) v.findViewById(R.id.device_list);
 @SuppressLint("InflateParams")
        View myHeader = inflater.inflate(R.layout.device_list_header, null);
        myHeader.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


 progressBar = (ProgressBar) inflater.inflate(R.layout.progress_bar, null);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

 */