package com.njuguna.dailyselfie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.njuguna.dailyselfie.profile.entity.User;

public class Preferences {
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static final String SIGNED_IN = "signedIn";
    private static final String LAST_USER = "lastUser";
    private static final String LAST_RCVD_FB_ACCESS_TOKEN = "lastReceivedFbAccessToken";
    private static final String LAST_SYNC_GATEWAY_SESSION_ID = "lastSyncGatewaySessionID";

    public static final String KEY_PREF_SYNC_ONLY_ON_WIFI = "pref_sync_only_on_wifi";
    public static final String KEY_PREF_REMIDER_TIME = "pref_remider_time";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public Boolean getSyncOnlyOnWiFi() {
        return sharedPreferences.getBoolean(KEY_PREF_SYNC_ONLY_ON_WIFI, true);
    }

    public Long getReminderTime() {
        return sharedPreferences.getLong(KEY_PREF_REMIDER_TIME, 0);
    }


    public String getRegistrationId() {
        return sharedPreferences.getString(PROPERTY_REG_ID, "");
    }

    public void setRegistrationId(String id) {
        if (id != null) {
            sharedPreferences.edit().putString(PROPERTY_REG_ID, id).apply();
        } else {
            sharedPreferences.edit().remove(PROPERTY_REG_ID).apply();
        }
    }

    public User getLastUser() {
        Gson gson = new Gson();
        String userJSON = sharedPreferences.getString(LAST_USER, "");
        return userJSON.isEmpty() ? null: gson.fromJson(userJSON, User.class);
    }

    public void setLastUser(User user) {
        if (user != null) {
            Gson gson = new Gson();
            String userJSON = gson.toJson(user);
            sharedPreferences.edit().putString(LAST_USER, userJSON).apply();
        } else {
            sharedPreferences.edit().remove(LAST_USER).apply();
        }
    }

    public void setLastReceivedFbAccessToken(String fbAccessToken) {
        if (fbAccessToken != null) {
            sharedPreferences.edit().putString(LAST_RCVD_FB_ACCESS_TOKEN, fbAccessToken).apply();
        } else {
            sharedPreferences.edit().remove(LAST_RCVD_FB_ACCESS_TOKEN).apply();
        }
    }

    public String getLastReceivedFbAccessToken() {
        return sharedPreferences.getString(LAST_RCVD_FB_ACCESS_TOKEN, "");
    }

    public String getLastSyncGatewaySessionId() {
        return sharedPreferences.getString(LAST_SYNC_GATEWAY_SESSION_ID, "");
    }

    public void setLastSyncGatewaySessionId(String lastSyncGatewaySessionId) {
        if (lastSyncGatewaySessionId != null) {
            sharedPreferences.edit().putString(LAST_SYNC_GATEWAY_SESSION_ID, lastSyncGatewaySessionId).apply();
        } else {
            sharedPreferences.edit().remove(LAST_SYNC_GATEWAY_SESSION_ID).apply();
        }
    }

    public Integer getAppVersion() {
        return sharedPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }

    public void setAppVersion(Integer appVersion) {
        if (appVersion != null) {
            sharedPreferences.edit().putInt(PROPERTY_APP_VERSION, appVersion).apply();
        } else {
            sharedPreferences.edit().remove(PROPERTY_APP_VERSION).apply();
        }
    }

    public void setSignedIn(Boolean signedIn) {
        if (signedIn != null) {
            sharedPreferences.edit().putBoolean(SIGNED_IN, signedIn).apply();
        } else {
            sharedPreferences.edit().remove(SIGNED_IN).apply();
        }
    }

    public Boolean getSignedIn() {
        return sharedPreferences.getBoolean(SIGNED_IN, false);
    }

}
