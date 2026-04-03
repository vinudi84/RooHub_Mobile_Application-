package com.example.roohub;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME   = "roohub_session";
    private static final String KEY_TOKEN   = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL   = "email";

    // ── Save session — clears old session first ──────────────────────────────
    public static void saveSession(Context context, String token, String userId, String email) {
        SharedPreferences.Editor editor = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();                        // ✅ wipe old session first
        editor.putString(KEY_TOKEN,   token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL,   email);
        editor.apply();

        android.util.Log.d("SESSION", "Session saved for userId: " + userId);
        android.util.Log.d("SESSION", "Email: " + email);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    public static String getUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, null);
    }

    public static String getEmail(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_EMAIL, null);
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }

    // ── Clear session on logout ──────────────────────────────────────────────
    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
        android.util.Log.d("SESSION", "Session cleared");
    }
}