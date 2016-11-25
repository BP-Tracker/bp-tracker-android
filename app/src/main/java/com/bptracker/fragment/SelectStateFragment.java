package com.bptracker.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bptracker.LoginActivity;
import com.bptracker.R;
import com.bptracker.firmware.Firmware;

import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.utils.TLog;


public class SelectStateFragment extends DialogFragment {


    public interface SelectStateListener {
        public void onStateSelect(DialogFragment dialog, Firmware.State state);
        public void onStateCancel(DialogFragment dialog);
    }

    ArrayAdapter<Pair<Firmware.State, String>> mAdapter;
    SelectStateListener mListener;



    // NB: for API 23+
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        _log.d("onAttach called");

        try {
            mListener = (SelectStateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SelectStateListener");
        }
    }


    // NB: for API 21-22
    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (SelectStateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectStateListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _log.d("onCreate called");

        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);

        List<Pair<Firmware.State, String>> states = new ArrayList<>(2);
        states.add(new Pair<Firmware.State, String>(Firmware.State.DISARMED, "Temporarily"));
        states.add(new Pair<Firmware.State, String>(Firmware.State.DEACTIVATED, "Until manual reactivation"));


        mAdapter = new StateAdapter(this.getActivity(),
                R.layout.select_state_row, R.id.tv_select_state_item, states);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

            //new CharSequence[]{"Temporaily", "Until resumed"}
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Disarm");

        builder.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pair<Firmware.State, String> item = mAdapter.getItem(which);
                mListener.onStateSelect(SelectStateFragment.this, item.first );
            }
        });


        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mListener.onStateCancel(SelectStateFragment.this);
    }


    private static class StateAdapter extends ArrayAdapter<Pair<Firmware.State, String>> {

        private int mResource;
        private int mTextViewResourceId;
        private int mDropDownResource;
        private LayoutInflater mInflater;

        public StateAdapter(Context context, int resource, int textViewResourceId,
                            List<Pair<Firmware.State, String>> objects) {

            super(context, resource, textViewResourceId, objects);
            mResource = resource;
            mDropDownResource = resource;
            mTextViewResourceId = textViewResourceId;
            mInflater = LayoutInflater.from(context);

        }

        // NB: logic borrowed from superclass
        private @NonNull View createViewFromResource(@NonNull LayoutInflater inflater,
                                                     int position,
                                                     @Nullable View convertView,
                                                     @NonNull ViewGroup parent, int resource) {
            final View view;
            final TextView text;

            if (convertView == null) {
                view = mInflater.inflate(mResource, parent, false);

            }else{
                view = convertView;
            }

            text = (TextView) view.findViewById(mTextViewResourceId);

            Pair<Firmware.State, String> item = getItem(position);
            if (item != null) {
                text.setText((CharSequence) item.second);
            }

            return view;
        }

        public void setDropDownViewResource(@LayoutRes int resource) {
            this.mDropDownResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return createViewFromResource(mInflater,
                    position, convertView, parent, mResource);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = mInflater;

            return createViewFromResource(inflater,
                    position, convertView, parent, mDropDownResource);
        }
    }

    private static final TLog _log = TLog.get(SelectStateFragment.class);

}



































//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mListener.onStateCancel(SelectStateFragment.this);
//            }
//        });
//ArrayAdapter<String> mAdapter;
//List<String> t = new ArrayList<>();
//t.add("TRST");

// mAdapter = new ArrayAdapter<String>(this.getActivity(),
//         R.layout.select_state_row, R.id.tv_select_state_item, t);

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_select_state, container, false);
        getDialog().setTitle("Disarm");

        ListView listView = (ListView) v.findViewById(R.id.lv_select_state);

        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TextView stateItem = (TextView) view.findViewById(R.id.tv_select_state_item);

                Toast.makeText(SelectStateFragment.this.getActivity(),
                        "Request sent", Toast.LENGTH_SHORT).show();

                //startActivityForResult();

            }
        });


        return v;
    }
    */



//        builder.setItems(android.R.array.emailAddressTypes,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });


//        mAdapter = new ArrayAdapter<String>(this.getActivity(),
//                R.layout.select_state_row, R.id.tv_select_state_item, states);