package com.example.caro.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPerferences {
    private static final String MY_SHARED_PREFERENCE = "MY_SHARED_PREFERENCE";
    private static final String FIRST_RUNNING = "FIRST_RUNNING";


    public static Boolean isSavedBefore(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCE, context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(FIRST_RUNNING, false);
    }

    public static void setSavedBefore(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_RUNNING, true);
        editor.apply();
    }

    public static String getValue(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCE, context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }


    public static void setValue(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void deleteBefore(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCE, context.MODE_PRIVATE);

        if (!isSavedBefore(context)) {
            //Chua co data
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("name");
        editor.remove("sex");
        editor.remove("imagePath");
        editor.commit();
    }

}
