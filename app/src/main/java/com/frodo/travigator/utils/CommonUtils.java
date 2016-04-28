package com.frodo.travigator.utils;

import android.util.Log;
import android.widget.Toast;

import com.frodo.travigator.app.trApp;

/**
 * Created by durgesh on 4/28/16.
 */
public class CommonUtils {
    public static void log(String message) {
        Log.i("Log", message);
    }

    public static void logStatus(String key, String message) {
        Log.i(key, message);
    }

    public static void toast(String message) {
        Toast.makeText(trApp.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String message) {
        Toast.makeText(trApp.getAppContext(), message, Toast.LENGTH_LONG).show();
    }

    public static String capitalize(String text) {
        char[] ch = text.toLowerCase().toCharArray();
        boolean found = true;

        for (int i = 0; i < ch.length; i++) {
            if (found && Character.isLetter(ch[i])) {
                ch[i] = Character.toUpperCase(ch[i]);
                found = false;
            } else if (Character.isWhitespace(ch[i]) || ch[i] == '_') {
                found = true;
            }
        }

        return String.valueOf(ch).replaceAll("_", " ").trim();
    }

    public static String deCapitalize(String text) {
        text = text.toLowerCase();
        text = text.replace(' ', '_');
        return text;
    }
}
