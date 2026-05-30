package com.cycleproject.b2b.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "auth";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BUSINESS_NAME = "businessName";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLogin(String token, String role, String email, String businessName) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_BUSINESS_NAME, businessName);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return !prefs.getString(KEY_TOKEN, "").isEmpty();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getBusinessName() {
        return prefs.getString(KEY_BUSINESS_NAME, "");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
