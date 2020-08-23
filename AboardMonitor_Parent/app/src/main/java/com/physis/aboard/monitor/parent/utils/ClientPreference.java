package com.physis.aboard.monitor.parent.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ClientPreference {

    private static final String XML_FILE_NAME = "ABOARD_PARENT";
    private static final String KEY_PUSH_TOKEN = "FCM_Token";
    private static final String KEY_CLIENT_NO = "Client_NO";

    public static void setPushToken(Context context, String token){
        SharedPreferences pref = context.getSharedPreferences(XML_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PUSH_TOKEN, token);
        editor.apply();
    }

    public static String getPushToken(Context context){
        SharedPreferences pref = context.getSharedPreferences(XML_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PUSH_TOKEN, null);
    }

    public static void setClientID(Context context, String id){
        SharedPreferences pref = context.getSharedPreferences(XML_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_CLIENT_NO, id);
        editor.apply();
    }

    public static String getClientID(Context context){
        SharedPreferences pref = context.getSharedPreferences(XML_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CLIENT_NO, null);
    }
}
