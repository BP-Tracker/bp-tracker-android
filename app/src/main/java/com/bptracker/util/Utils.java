package com.bptracker.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;


public class Utils {

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo s : m.getRunningServices(Integer.MAX_VALUE)) {
            //Log.d("Utils", serviceClass.getName() + "--" + s.service.getClassName());
            if (serviceClass.getName().equals(s.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
