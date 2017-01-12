package com.bptracker.persistance;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.particle.android.sdk.utils.TLog;

/**
 * The preference keys are defined in xml/pref_*.xml
 */
public class AppPreferences {

    /**
     * The name of the shared preferences store for application preferences
     */
    public static final String PREFERENCES_STORE = "app-preferences";

    private static final String KEY_IS_VISITED = "isVisited";

    private final SharedPreferences prefs;

    public AppPreferences(Context ctx) {
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREFERENCES_STORE, Context.MODE_PRIVATE);
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }


    public boolean isGcmEnabled(){
        return prefs.getBoolean("pref_gcm_sync_enabled", false);
    }

    //  Google Project Number
    public String getGcmSenderId(){
        return prefs.getString("pref_gcm_project_reg_number", null);
    }


    public boolean isFirstVisit() {
        return !prefs.getBoolean(KEY_IS_VISITED, false);
    }

    public void setVisited(boolean isVisited) {
        prefs.edit().putBoolean(KEY_IS_VISITED, isVisited).apply();
    }




    private static final TLog _log = TLog.get(AppPreferences.class);
}




