package com.frodo.travigator.utils;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.frodo.travigator.app.trApp;
import com.frodo.travigator.models.Stop;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

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

    public static List<String> getStringArray(Stop[] stops) {
        List<String> stopList = new ArrayList<>();
        for (Stop stop : stops) {
            stopList.add(stop.getStop_name());
        }
        return stopList;
    }

    public static int getStopPos(Stop[] stops, LatLng latLng) {
        int pos = CommonUtils.getNearstStop(stops, latLng);
        Stop stop = stops[pos];
        float[] result = new float[2];
        Location.distanceBetween(latLng.latitude, latLng.longitude, stop.getStop_lat(), stop.getStop_lon(), result);
        if (result[0] < Constants.ERROR_RADIUS)
            return pos;
        return -1;
    }

    public static int getNearstStop(Stop[] stops, LatLng latLng) {
        int res = 0;
        float distance = Float.MAX_VALUE;
        for (int i = 0 ; i < stops.length ; i++) {
            Stop stop = stops[i];
            float[] result = new float[2];
            Location.distanceBetween(latLng.latitude, latLng.longitude, stop.getStop_lat(), stop.getStop_lon(), result);
            if (distance > result[0]){
                distance = result[0];
                res = i;
            }
            //CommonUtils.log("Distance:"+i+":"+result[0]+"Pos:"+res);
        }
        return res;
    }
}
