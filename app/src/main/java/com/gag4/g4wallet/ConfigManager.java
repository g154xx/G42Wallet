package com.gag4.g4wallet.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    public static void loadConfig(Context context, Uri uri) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        InputStream is = resolver.openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JSONObject json = new JSONObject(sb.toString());

        SharedPreferences prefs = context.getSharedPreferences("G4WalletPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pan", json.optString("pan", "1234567890123456"));
        editor.putString("expiry", json.optString("expiry", "2612"));
        editor.putString("service_code", json.optString("service_code", "101"));
        editor.putString("country", json.optString("country", "0840"));
        editor.putString("amount", json.optString("amount", "000000000500"));
        editor.putString("aid", json.optString("aid", "A0000000041010"));
        editor.putString("emv_key_hex", json.optString("emv_key_hex", "0123456789ABCDEF0123456789ABCDEF"));
        editor.apply();
    }

    public static String getCardInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("G4WalletPrefs", Context.MODE_PRIVATE);
        return "PAN: " + prefs.getString("pan", "N/A") +
                ", Expiry: " + prefs.getString("expiry", "N/A") +
                ", Service: " + prefs.getString("service_code", "N/A");
    }
}