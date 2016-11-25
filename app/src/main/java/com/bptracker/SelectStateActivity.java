package com.bptracker;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.bptracker.firmware.Firmware.State;
import com.bptracker.fragment.SelectStateFragment;

public class SelectStateActivity extends Activity
                                implements SelectStateFragment.SelectStateListener {
    @Override
    public void onStateSelect(DialogFragment dialog, State state) {

        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    @Override
    public void onStateCancel(DialogFragment dialog) {

        Toast.makeText(this, "Disarm cancelled", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_state);


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        if (prev != null) { //TODO: is this necessary??
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment fragment = new SelectStateFragment();
        fragment.show(ft, "dialog");



    }


}


































   /*
        mListView = new ListView(this);
        String[] states = {
                "Panic",
                "Offline",
                "Armed"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.select_state_row, R.id.tv_state_item_id, states);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               TextView stateItem = (TextView) view.findViewById(R.id.tv_state_item_id);
               Toast.makeText(SelectStateActivity.this,
                       stateItem.getText(), Toast.LENGTH_LONG).show();

               //startActivityForResult();

           }
       });
       */


/*
    @Override
    protected void onResume() {
        super.onResume();

       //AlertDialog.Builder b = new AlertDialog.Builder(this);
       // b.setCancelable(true);
       // b.setPositiveButton("OK", null);
       // b.setView(mListView);

        //AlertDialog dialog = b.create();
       // dialog.show();

}

 */