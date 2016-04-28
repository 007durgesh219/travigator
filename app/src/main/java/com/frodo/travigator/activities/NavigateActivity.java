package com.frodo.travigator.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.frodo.travigator.R;
import com.frodo.travigator.adapter.StopListAdapter;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class NavigateActivity extends Activity {
    public static final String STOPS = "stops";
    public static final String SRC_STOP = "src_stop";
    public static final String DST_STOP = "dst_stop";

    private static int ERROR_RADIUS = 50;

    private ListView stopsList ;
    private Stop[] stops;
    private int srcPos, dstPos;
    private boolean isFirstTimeAdjusted = false;
    private int infoGivenPos = -1;

    private boolean enableSpeech = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        stopsList = (ListView)findViewById(R.id.stop_list);
        stops = (Stop[]) getIntent().getSerializableExtra(STOPS);
        if (stops == null) {
            finish();
        }
        srcPos = getIntent().getIntExtra(SRC_STOP, -1);
        dstPos = getIntent().getIntExtra(DST_STOP, -1);

        stopsList.setAdapter(new StopListAdapter(this, stops));

        if (LocationUtil.checkLocationPermission() && LocationUtil.isGPSOn()) {
            trApp.getLocationUtil().startLocationUpdates();
        } else if (!LocationUtil.checkLocationPermission()){
            trApp.getLocationUtil().askLocationPermission(this);
        } else {
            trApp.getLocationUtil().checkLocationSettings(this);
        }
        CommonUtils.toast("Getting your location. Please wait...");

        CheckBox checkBox = (CheckBox)findViewById(R.id.enable_speech_cb);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableSpeech = isChecked;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LocationUtil.REQ_PERMISSIONS_REQUEST_ACCESS_FILE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    trApp.getLocationUtil().checkLocationSettings(this);
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationUtil.REQUEST_CHECK_SETTINGS:
                trApp.getLocationUtil().dialogClosed();
        }
    }

    private int getStopPos(LatLng latLng) {
        for (int i = 0 ; i < stops.length ; i++) {
            Stop stop = stops[i];
            float[] result = new float[2];
            Location.distanceBetween(latLng.latitude, latLng.longitude, stop.getStop_lat(), stop.getStop_lon(), result);
            if (result[0] < ERROR_RADIUS)
                return i;
        }
        return -1;
    }

    private int getNearstStop(LatLng latLng) {
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
        }
        return res;
    }

    @Subscribe
    public void onLocationChangedEvent(LocationChangedEvent event) {
        LatLng latLng = event.getLocation();
        int pos = getStopPos(latLng);
        if (pos == -1 && !isFirstTimeAdjusted){
            isFirstTimeAdjusted = true;
            stopsList.smoothScrollToPosition(getNearstStop(latLng));
        } else {
            stopsList.smoothScrollToPosition(pos);
            if (infoGivenPos != -1 && infoGivenPos != pos) {
                String message = "You are arrived at " + stops[pos].getStop_name()+".";
                if (dstPos == pos) {
                    message = "This is our final stop.";
                }
                if (enableSpeech)
                    trApp.getTTS().speak(message, TextToSpeech.QUEUE_FLUSH, null);
                infoGivenPos = pos;
            }
        }
    }
}
