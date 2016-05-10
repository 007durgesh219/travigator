package com.frodo.travigator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frodo.travigator.R;
import com.frodo.travigator.activities.NavigateActivity;
import com.frodo.travigator.adapter.StopListAdapter;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.events.MocLocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.LocationUtil;
import com.frodo.travigator.utils.MocLocSimulator;
import com.frodo.travigator.utils.PrefManager;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by durgesh on 5/10/16.
 */
public class TextNavigationFragment extends Fragment {

    private ListView stopsList ;
    private Stop[] stops;
    private int srcPos, dstPos;
    private boolean isFirstTimeAdjusted = false;
    private int infoGivenPos = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.text_navigation_fragment, null);
        stopsList = (ListView)rootView.findViewById(R.id.stop_list);
        stopsList.setAdapter(new StopListAdapter(getContext(), stops));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        new MocLocSimulator(srcPos, dstPos).simulate(stops, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stops = (Stop[]) getActivity().getIntent().getSerializableExtra(NavigateActivity.STOPS);
        if (stops == null) {
            getActivity().finish();
        }
        srcPos = getActivity().getIntent().getIntExtra(NavigateActivity.SRC_STOP, -1);
        dstPos = getActivity().getIntent().getIntExtra(NavigateActivity.DST_STOP, -1);

        if (LocationUtil.checkLocationPermission() && LocationUtil.isGPSOn()) {
            trApp.getLocationUtil().startLocationUpdates();
        } else if (!LocationUtil.checkLocationPermission()){
            trApp.getLocationUtil().askLocationPermission(getActivity());
        } else {
            trApp.getLocationUtil().checkLocationSettings(getActivity());
        }
        CommonUtils.toast("Getting your location. Please wait...");
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
                if (PrefManager.isTTSEnabled()) {
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
