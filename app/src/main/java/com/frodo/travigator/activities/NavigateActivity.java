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
import com.frodo.travigator.events.MocLocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.Constants;
import com.frodo.travigator.utils.LocationUtil;
import com.frodo.travigator.utils.MocLocSimulator;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class NavigateActivity extends Activity {
    public static final String STOPS = "stops";
    public static final String SRC_STOP = "src_stop";
    public static final String DST_STOP = "dst_stop";

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
        new MocLocSimulator(srcPos, dstPos).simulate(stops, 5000);
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

    @Subscribe
    public void onLocationChangedEvent(LocationChangedEvent event) {
        LatLng latLng = event.getLocation();
        int pos = CommonUtils.getStopPos(stops, latLng);
        CommonUtils.log("Pos:"+pos);
        if (pos == -1 && !isFirstTimeAdjusted){
            isFirstTimeAdjusted = true;
            stopsList.smoothScrollToPosition(CommonUtils.getNearstStop(stops, latLng));
        } else {
            stopsList.smoothScrollToPosition(pos);
            if (infoGivenPos != -1 && infoGivenPos != pos) {
                String message = "You arrived at " + stops[pos].getStop_name()+".";
                if (dstPos == pos) {
                    message = message+". This is you final stop.";
                }
                if (enableSpeech) {
                    trApp.getTTS().speak(message, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            infoGivenPos = pos;
        }
    }

    @Subscribe
    public void onMocLocationChangedEvent(MocLocationChangedEvent event) {
        CommonUtils.log("Event: "+event.getLocation().toString());
        EventBus.getDefault().post(new LocationChangedEvent(event.getLocation()));
    }
}
