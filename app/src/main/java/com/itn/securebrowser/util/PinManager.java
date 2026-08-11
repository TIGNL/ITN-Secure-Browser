package com.itn.securebrowser.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;

public class PinManager {

    private static final String PREFS = "itn_pin";
    private static final String KEY_PIN = "pin_hash";

    public static boolean hasPin(Context ctx) {
        return prefs(ctx).getString(KEY_PIN, null) != null;
    }

    public static void savePin(Context ctx, String pin) {
        prefs(ctx).edit().putString(KEY_PIN, hash(pin)).apply();
    }

    public static boolean verify(Context ctx, String pin) {
        String stored = prefs(ctx).getString(KEY_PIN, null);
        return stored != null && stored.equals(hash(pin));
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_PIN).apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
