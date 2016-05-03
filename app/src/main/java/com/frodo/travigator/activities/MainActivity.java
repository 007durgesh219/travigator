package com.frodo.travigator.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

import com.frodo.travigator.fragments.Favorite;
import com.frodo.travigator.utils.JSONParser;
import com.frodo.travigator.adapter.MyAdapter;
import com.frodo.travigator.R;

public class MainActivity extends Activity
	implements LocationListener, GpsStatus.Listener, OnInitListener
{
    private AlertDialog aD;

    private LocationManager lM;
    private Location CurrLocation;
    private boolean isGPSFix, isUnav;
    private long mLastLocationMillis;

    private final int MY_DATA_CHECK_CODE = 10;
    private TextToSpeech mTTS;
    private Boolean TTS_READY = false;
	public static Boolean TTS_ENABLE = false;
	public static Boolean TTS_SPEAKALL = true;

    private TextView showLoc, showStop;
    private ListView route;

    private int stopPos = -1;
    private int deboardPos = -1;
    private int oldPos = -1;

    private static final float GEOFENCE_RADIUS = 50;
	private static final float ERROR_MARGIN = 50;

    public static ArrayList<String> stopList, latList, lonList;
    public static ArrayList<Integer> stateList;

    public static int currRes;

    public void onGpsStatusChanged(int event) {
	switch(event) {
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			if (CurrLocation != null)
				isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 5000;
			
			if (!isGPSFix){
				if(CurrLocation == null) {
					if (isUnav == false) {
//						showAlert("Location Unavailable!", "Sorry, we are not able to retrieve your location at the moment...");
						showLoc.setText("Location Unavailable!" + "\n"+"Sorry, we are not able to retrieve your location at the moment...");
						isUnav=true;
					}
				}
				else {
					if (isUnav == false) {
						showAlert("Location lost...","Sorry, we have lost your location.\n Attempting to retrieve your location");
						isUnav=true;
					}
				}
			}
			else {
				isUnav = false;
			}
			break;
		default: break;
	}
    }

    @Override
    public void onLocationChanged(Location loc) {
try {
	CurrLocation = loc;
	if (CurrLocation.getAccuracy()<ERROR_MARGIN ) {
		updateUI();
		geofence();
		if (loc != null)
			mLastLocationMillis = SystemClock.elapsedRealtime();
	}
	if (aD.isShowing())  aD.dismiss();
}
catch (Exception e) {}
    }
	
    @Override
    public void onProviderEnabled(String provider) {
	Toast.makeText(this, getString(R.string.gps_enabled),Toast.LENGTH_SHORT).show();
    }
	
    @Override
    public void onProviderDisabled(String provider) {
	Toast.makeText(this, getString(R.string.gps_disabled),Toast.LENGTH_SHORT).show();
	showSettingsAlert();
    }
	
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	if( provider == LocationManager.GPS_PROVIDER) {
	switch (status) {
	    case LocationProvider.OUT_OF_SERVICE:
		 Toast.makeText(this, getString(R.string.gps_unav),Toast.LENGTH_SHORT).show();
		break;
	    case LocationProvider.TEMPORARILY_UNAVAILABLE:
		Toast.makeText(this, getString(R.string.gps_temp_unav),Toast.LENGTH_SHORT).show();
		break;
	    case LocationProvider.AVAILABLE:
		Toast.makeText(this, getString(R.string.gps_av),Toast.LENGTH_SHORT).show();
		break;
	}
	}
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == MY_DATA_CHECK_CODE) {
	    if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            	mTTS = new TextToSpeech(this, this);
            }
	}
	else {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	String par = getIntent().getStringExtra(getString(R.string.parentKey));

	showLoc = (TextView) findViewById(R.id.coord);
	showStop = (TextView) findViewById(R.id.text);
	route = (ListView) findViewById(R.id.showRoute);

	isUnav = false;

	stateList = new ArrayList<Integer>();

	if (savedInstanceState != null) {
		stopList = savedInstanceState.getStringArrayList("stopList");
		stateList = savedInstanceState.getIntegerArrayList("stateList");
		latList = savedInstanceState.getStringArrayList("latList");
		lonList = savedInstanceState.getStringArrayList("lonList");
		deboardPos = savedInstanceState.getInt("deboard");
	}
	else if ( par.equals("Home") ) {
		stopList = JSONParser.stopList;
		latList = JSONParser.latList;
		lonList = JSONParser.lonList;
		//jc deboardPos = HomeFragment.deboardPos;
	}
	else if ( par.equals("Favorite") ) {
		/*stopList = new ArrayList<String>(Favorite.stopList.subList(1,Favorite.stopList.size()));
		latList = Favorite.latList;
		lonList = Favorite.lonList;
		deboardPos = Favorite.deboardPos;*/
	}
	else {
		stopList = new ArrayList<String>();
		latList = new ArrayList<String>();
		lonList = new ArrayList<String>();
		deboardPos = -1;
	}

	for (int i=0; i<stopList.size(); i++ ) stateList.add(0);

	if (deboardPos != -1) stateList.set(deboardPos,10);

	lM = (LocationManager) getSystemService(LOCATION_SERVICE);
	lM.addGpsStatusListener(this);

	MyAdapter adapter = new MyAdapter(MainActivity.this, stopList);
	route.setAdapter(adapter);

	currRes = R.drawable.neutral;
    }

    @Override
    protected void onStart() {
	super.onStart();
	Intent checkIntent = new Intent();
	checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
	lM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lM.removeUpdates(this);
    }

    @Override
    protected void onStop() {
	super.onStop();
	mTTS.shutdown();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putStringArrayList("stopList",stopList);
	outState.putStringArrayList("latList",latList);
	outState.putStringArrayList("lonList",lonList);
	outState.putInt("deboard",deboardPos);
    }	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu items for use in the action bar
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main_activity_actions, menu);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                MainActivity.openSettings(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
   
    public static void openSettings(final Context context) {
		final String disTTS[] ={"Enable/Disable TalkBack","Enable Text to Speech"};
		final String enTTS[] = {"Enable/Disable TalkBack","Disable Text to Speech", "Change text to speech settings"};
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		View convertView = (View) inflater.inflate(R.layout.list, null);
		alertDialog.setView(convertView);
		alertDialog.setTitle("Settings");

		final ListView lv = (ListView) convertView.findViewById(R.id.dialogList);
		ArrayAdapter<String> adapter;
		if (MainActivity.TTS_ENABLE) {
			adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,enTTS);
		}
		else {
			adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,disTTS);
		}
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("FRODO","YOLO");
				if (MainActivity.TTS_ENABLE){
					switch (position){
						case 0: Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
							context.startActivity(intent);
							break;
						case 1: MainActivity.TTS_ENABLE = false;
							ArrayAdapter<String> ad = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,disTTS);
							lv.setAdapter(ad);
							break;
						case 2:intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
							intent.setAction("com.android.settings.TTS_SETTINGS");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
							break;
						//case 3: break;
						default: break;
					}
				}
				else {
					switch (position){
						case 0:	Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
							context.startActivity(intent);
							break;
						case 1: MainActivity.TTS_ENABLE = true;
							ArrayAdapter<String> ad = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,enTTS);
							lv.setAdapter(ad);
							break;
						default: break;
					}
				}
			}
		});


		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		alertDialog.show();
	}

    public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is disabled!");

		// Setting Dialog Message
		alertDialog.setMessage("Please enable GPS to use this app!");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});

 /*       // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        }); */

		// Showing Alert Message
		alertDialog.show();
    }

    private void showAlert(String title, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle(title);
  
        // Setting Dialog Message
        alertDialog.setMessage(msg);
  
        // On pressing Settings button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
				dialog.dismiss();
            }
        });
  
        // Showing Alert Message
        aD = alertDialog.create();
	aD.show();
    }

    protected void updateUI() {	
	String coord = getResources().getString(R.string.loc_unav);

	if (CurrLocation != null) {
		String lat = String.valueOf(CurrLocation.getLatitude());
		String lon = String.valueOf(CurrLocation.getLongitude());
		String txt = getResources().getString(R.string.loc_av);
		String acc = getResources().getString(R.string.loc_acc);
		String acc_val = String.valueOf(CurrLocation.getAccuracy());
		coord = /*txt + "\n" + lat + " , " + lon + "\n" + */acc + "\n" + acc_val + " m.";
	}

	showLoc.setText(coord);
    }

    protected void updateList() {
	MyAdapter adapter = new MyAdapter(MainActivity.this, stopList);
	route.setAdapter(adapter);
	adapter.notifyDataSetChanged();
    } 

    protected void updateStop(int i) {
	int old = oldPos;

	String thisStop = getString(R.string.thisStop);
	String nextStop = getString(R.string.nextStop);
	String lastStop = getString(R.string.lastStop);

	String newData = "";

	if ( i == -1 && stopPos == -1 ) {
		return;
	}
	else if ( stopPos == stopList.size()-1 ) {
		for(int a=0;a<stopPos;a++) stateList.set(a,-1);
		stateList.set(stopPos,2);
		newData = thisStop + stopList.get(stopPos) + "... \n" + 
			lastStop + "... ";
	}
	else if ( i == -1 ) {
		for(int a=0;a<=stopPos;a++) stateList.set(a,-1);
		stateList.set(stopPos+1,1);
		newData = nextStop + stopList.get(stopPos+1) + "... ";			
	}
	else {
		for (int a=0;a<stopPos;a++) stateList.set(a,-1);
		stateList.set(stopPos,2);
		newData = thisStop + stopList.get(stopPos) + "... \n" +
			nextStop + stopList.get(stopPos+1) + "... " ;
	}

	if (deboardPos == -1 ) {
	}
	else if ( deboardPos == stopPos ) {
		newData += "\nThis is your deboarding stop! ";

	}
	else if ( deboardPos == stopPos+1 ) {
		newData += "\nGet Ready! The next stop is your deboarding stop!";
	}
	else if ( deboardPos < stopPos ) {
		newData += "\nYou missed your deboarding stop!!";
	}
	else if ( deboardPos > stopPos ) {
		newData += "\n" + String.valueOf(deboardPos-stopPos) + " stops left to your deboarding stop!";
	}

	showStop.setText(newData);

	oldPos = stopPos;

	updateList();

	if ( TTS_READY && TTS_ENABLE && old != stopPos) {
		speak(newData);
	}
    }

    private Double toRad(Double value) {
	return value * Math.PI / 180;
    }

    private float distance(String slat1, String slon1, String slat2, String slon2) {
	final int R = 6371000;
	Double lat1 = Double.parseDouble(slat1);
	Double lat2 = Double.parseDouble(slat2);
	Double lon1 = Double.parseDouble(slon1);
	Double lon2 = Double.parseDouble(slon2);

	float result[] = {0,0,0};
/*
	Double delLat = toRad(lat2 - lat1);
	Double delLon = toRad(lat2 - lat1);

        Double a = Math.sin(delLat / 2) * Math.sin(delLat / 2) +
                   Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                   Math.sin(delLon / 2) * Math.sin(delLon / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c; 
*/

	Location.distanceBetween(lat1,lon1,lat2,lon2,result);
	return result[0];
    }

    protected void geofence() { 
	String lat1 = String.valueOf(CurrLocation.getLatitude());
	String lon1 = String.valueOf(CurrLocation.getLongitude());
	float dist;
	if (stopPos >= stopList.size() - 1) {
		return;
	}

	else if (stopPos >= 0 && (dist = distance(lat1,lon1,latList.get(stopPos+1),lonList.get(stopPos+1))) < GEOFENCE_RADIUS) {
		stopPos++;
		updateStop(stopPos);
	}

	else {
		boolean flag=true;
		for ( int i=((stopPos==-1)?0:stopPos); i<stopList.size(); i++) {
			if ((dist=distance(lat1,lon1,latList.get(i),lonList.get(i))) < GEOFENCE_RADIUS) {
				stopPos = i;
				flag=false;
				updateStop(stopPos);
				break;
			}
		}
		if (flag) {
			updateStop(-1);
		}
	}
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            mTTS.setLanguage(Locale.getDefault());
	    TTS_READY = true;
	    speak(showStop.getText().toString());
	}
    }

    private void speak(String text) {
	if (text.trim().length() > 0 ) {
		mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
    }

    public void speakAgain(View view) {
	if ( TTS_READY && TTS_ENABLE) {
		speak(showStop.getText().toString());
	}
	else if (TTS_ENABLE){
		Toast.makeText(this, "Speech not ready!", Toast.LENGTH_SHORT).show();
	}
		else{
		Toast.makeText(this,"Text to speech disabled.", Toast.LENGTH_SHORT).show();
	}
    }		

    public void Exit(View view) {
	Intent intent = new Intent(Intent.ACTION_MAIN);
	intent.addCategory(Intent.CATEGORY_HOME);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(intent);
	android.os.Process.killProcess(android.os.Process.myPid());
	System.exit(0);
    }
}