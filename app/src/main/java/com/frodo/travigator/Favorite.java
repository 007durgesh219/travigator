package com.frodo.travigator;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.frodo.travigator.activities.MainActivity;
import com.frodo.travigator.activities.ShowRoute;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.db.DbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Favorite extends Fragment
{
    private ArrayList<String> cityList, routeList;
    public static ArrayList<String> stopList, latList, lonList;
    public static String City="", Route="";
    public static int deboardPos = -1;

    private Spinner citySpinner, routeSpinner, stopSpinner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View rootView = inflater.inflate(R.layout.favorite, container, false);
		if (savedInstanceState != null) {
			cityList = savedInstanceState.getStringArrayList("cityList");
			routeList = savedInstanceState.getStringArrayList("routeList");
			stopList = savedInstanceState.getStringArrayList("stopList");
			latList = savedInstanceState.getStringArrayList("latList");
			lonList = savedInstanceState.getStringArrayList("lonList");
		}
		else {
			stopList = new ArrayList<String>();
			routeList = new ArrayList<String>();
			cityList = new ArrayList<String>();
			latList = new ArrayList<String>();
			lonList = new ArrayList<String>();

			stopList.add(getString(R.string.routeFirst));
			routeList.add(getString(R.string.cityFirst));
			cityList.add(getString(R.string.selectCity));

			File dir = new File(DbHelper.DATABASE_PATH);
			File files[] = dir.listFiles();
			int l=0;
			if (files != null) l=files.length;

			for ( int i = 0; i<l; i++) {
				String name = files[i].getName();
				name.replaceAll("_"," ");
				if(name.contains("journal")==false) {
					cityList.add(name);
				}
			}
		}

		citySpinner = (Spinner) rootView.findViewById(R.id.cityFav);
		routeSpinner = (Spinner) rootView.findViewById(R.id.routeFav);
		stopSpinner = (Spinner) rootView.findViewById(R.id.stopFav);

		ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,cityList);
		cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		citySpinner.setAdapter(cityAdapter);
		citySpinner.setOnItemSelectedListener(cityListener);

		if (cityList.size() == 2) {
			citySpinner.setSelection(1);
		}

		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeList);
		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		routeSpinner.setAdapter(routeAdapter);
		routeSpinner.setOnItemSelectedListener(routeListener);

		ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
		stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stopSpinner.setAdapter(stopAdapter);
		stopSpinner.setOnItemSelectedListener(stopListener);

		Button navigate = (Button) rootView.findViewById(R.id.buttonNavigate);
		navigate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( City == "" ) {
					Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
				}
				else if ( Route == "" ) {
					Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
				}
				else {
						Intent i = new Intent(getActivity(), MainActivity.class);
						i.putExtra(getString(R.string.parentKey), "Favorite");
						startActivity(i);
						getActivity().finish();
					}
				}

		});

		Button viewRoute = (Button) rootView.findViewById(R.id.buttonViewRoute);
		viewRoute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( Route != "" ) {
					Intent i = new Intent(getActivity(), ShowRoute.class);
					i.putExtra(getString(R.string.parentKey), "Favorite");
					startActivity(i);
					getActivity().finish();

			}
			else if ( City == "" ) {
				Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
			}
			}
		});

		Button remFav = (Button) rootView.findViewById(R.id.buttonRemFav);
		remFav.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Route != "") {
					DbHelper db = new DbHelper(getActivity(), City, Route);
					db.delTable();
					db.closeDB();
					citySpinner.setSelection(0);
				} else if (City == "") {
					Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
				}
			}
		});

		return rootView;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.favorite);

	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		//MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				MainActivity.openSettings(getContext());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
   
    protected void openSettings() {}

    @Override
    public void onSaveInstanceState(Bundle outstate) {
	super.onSaveInstanceState(outstate);
	
	outstate.putStringArrayList("cityList",cityList);
	outstate.putStringArrayList("routeList",routeList);
	outstate.putStringArrayList("stopList",stopList);
	outstate.putStringArrayList("latList",latList);
	outstate.putStringArrayList("lonList",lonList);
    }

    private OnItemSelectedListener cityListener = new OnItemSelectedListener() {

	@Override
	public void onItemSelected ( AdapterView<?> parent, View view, int pos, long id) { 
		routeList.clear();
		routeList.trimToSize();
	
		if ( pos == 0 ) {
			routeList.add(getString(R.string.cityFirst));
			City = "";
			routeSpinner.setEnabled(false);
		}
	
		else {
			routeList.add(getString(R.string.selectRoute));

			City = cityList.get(pos);

			DbHelper db = new DbHelper(trApp.getAppContext(),City);
			Cursor c = db.getTables();

			if (c != null && c.getCount()>0) {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					String temp = c.getString(0);

					if (temp.contains("Route")) {
						String temp2 = temp.substring(6).replaceAll("_"," ");
						routeList.add(temp2);
					}
					c.moveToNext();
				}
			}
			db.closeDB();

			routeSpinner.setEnabled(true);
		}
	
		Collections.sort(routeList.subList(1,routeList.size()));
/*
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("GPS is disabled!");
  	        alertDialog.setMessage("Please enable GPS to use this app!");
  

	        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
        	    public void onClick(DialogInterface dialog,int which) {
                	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                startActivity(intent);
        	    }
	        });
  
         	alertDialog.show();
*/

		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeList);
        	routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	       	routeSpinner.setAdapter(routeAdapter);

	}

	@Override
	public void onNothingSelected ( AdapterView<?> parent ) {
		routeList.clear();
		routeList.trimToSize();

		routeList.add(getString(R.string.cityFirst));

		City = "";

		routeSpinner.setEnabled(false);
		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeList);
        	routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	       	routeSpinner.setAdapter(routeAdapter);

	}
    };

    private OnItemSelectedListener routeListener = new OnItemSelectedListener() {

	@Override
	public void onItemSelected ( AdapterView<?> parent, View view, int pos, long id) {
		stopList.clear();
		stopList.trimToSize();

		latList.clear();
		latList.trimToSize();
		
		lonList.clear();
		lonList.trimToSize();

		Route = "";

		if ( pos != 0 ) {
			Route = "Route_" + routeList.get(pos).trim().replaceAll(" ","_");
			stopList.add(getString(R.string.selectStop));

			DbHelper db = new DbHelper(trApp.getAppContext(), City, Route);
			Cursor c = db.showTable();
		
			if ( c != null && c.getCount() > 0 ) {
				c.moveToFirst();
				while( !c.isAfterLast() ) {
					stopList.add(c.getString(1));
					latList.add(c.getString(2));
					lonList.add(c.getString(3));
					c.moveToNext();
				}
			}
			db.closeDB();
			stopSpinner.setEnabled(true);
		}
		else {
			stopList.add(getString(R.string.routeFirst));
			stopSpinner.setEnabled(false);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	stopSpinner.setAdapter(adapter);
	}

	@Override
	public void onNothingSelected ( AdapterView<?> parent ) {
		Route = "";

		stopList.clear();
		stopList.trimToSize();

		latList.clear();
		latList.trimToSize();
		
		lonList.clear();
		lonList.trimToSize();
		stopList.add(getString(R.string.routeFirst));

		stopSpinner.setEnabled(false);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	stopSpinner.setAdapter(adapter);
	}
    };

    private OnItemSelectedListener stopListener = new OnItemSelectedListener() {

	@Override
	public void onItemSelected ( AdapterView<?> parent, View view, int pos, long id) {
		deboardPos = pos-1;
	}

	@Override
	public void onNothingSelected ( AdapterView<?> parent ) {
		deboardPos = -1;
	}
    };




}