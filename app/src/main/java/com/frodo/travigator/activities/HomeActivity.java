package com.frodo.travigator.activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import com.frodo.travigator.db.DbHelper;
import com.frodo.travigator.JSONParser;
import com.frodo.travigator.R;
import com.frodo.travigator.app.trApp;

public class HomeActivity extends Fragment
{
    public static String server_ip = "192.168.0.1";
    public static String server_add = "http://"+ server_ip + "/~durgesh/Nav/public/index.php/app/";

    public static ArrayList<String> cityList, routeNoList, stopList, recentList;
    public static Spinner citySpinner, routeSpinner, stopSpinner, srcStopSpinner, recentSpinner;
    public static String Route="",City="";
    public static int deboardPos = -1;

    private EditText ip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.home, container, false);

		if ( savedInstanceState != null ) {
			cityList = savedInstanceState.getStringArrayList("cityList");
			recentList = savedInstanceState.getStringArrayList("recentList");
			server_ip = savedInstanceState.getString("ip");
		}
		else {
			cityList = new ArrayList<String>();
			recentList = new ArrayList<String>();
			routeNoList = new ArrayList<String>();
			stopList = new ArrayList<String>();

			cityList.add(getString(R.string.selectCity));
			routeNoList.add(getString(R.string.cityFirst));
			stopList.add(getString(R.string.routeFirst));
			recentList.add(getString(R.string.selectRoute));
		}

	citySpinner = (Spinner) rootView.findViewById(R.id.citySpinner);
	routeSpinner = (Spinner) rootView.findViewById(R.id.routeNoSpinner);
	stopSpinner = (Spinner) rootView.findViewById(R.id.stopSpinner);
        srcStopSpinner = (Spinner)rootView.findViewById(R.id.srcStopSpinner);






		ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,cityList);
		cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		citySpinner.setAdapter(cityAdapter);
		citySpinner.setOnItemSelectedListener(cityListener);

		if (cityList.size() == 2) {
			citySpinner.setSelection(1);
		}

		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeNoList);
		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		routeSpinner.setAdapter(routeAdapter);
		routeSpinner.setOnItemSelectedListener(routeListener);

		ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
		stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stopSpinner.setAdapter(stopAdapter);
		stopSpinner.setOnItemSelectedListener(stopListener);


		Button navigate = (Button) rootView.findViewById(R.id.searchNavigate);
		navigate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (City == "") {
					Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
				} else if (Route == "") {
					Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
				} else {
					if(isFavorite(Route)) {
						new JSONParser(server_add + "download.php?city=" + City + "&route=" + Route, getActivity(), 10).execute();
					}
					else {
						favAlert();
					}
				}
			}
		});

		Button viewRoute = (Button) rootView.findViewById(R.id.searchViewRoute);
		viewRoute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( Route != "" ) {
					new JSONParser(server_add+"download.php?city="+City+"&route="+Route, getActivity(), 11).execute();
				}
				else if ( City == "" ) {
					Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
				}
			}
		});

		Button refresh = (Button) rootView.findViewById(R.id.searchRefresh);
		refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cityList.clear();
				cityList.trimToSize();
				cityList.add(getString(R.string.selectCity));
				new JSONParser(server_add+"get_cities",getActivity(),0).execute();
			}
		});



		return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);

	outState.putStringArrayList("cityList",cityList);
	outState.putString("ip",server_ip);
    }

    private OnItemSelectedListener cityListener = new OnItemSelectedListener() {

	@Override
	public void onItemSelected ( AdapterView<?> parent, View view, int pos, long id) {
		routeNoList.clear();
		routeNoList.trimToSize();
	
		if ( pos == 0 ) {
			routeNoList.add(getString(R.string.cityFirst));
			City = "";
			routeSpinner.setEnabled(false);
		}
	
		else {

			routeNoList.add(getString(R.string.selectRoute));

			City = JSONParser.deCapitalize(cityList.get(pos));
			routeSpinner.setEnabled(true);
			new JSONParser(server_add+"get_routes/"+City, getActivity(), 1).execute();
		}

		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeNoList);
        	routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	       	routeSpinner.setAdapter(routeAdapter);

	}

	@Override
	public void onNothingSelected ( AdapterView<?> parent ) {
		routeNoList.clear();
		routeNoList.trimToSize();

		routeNoList.add(getString(R.string.cityFirst));

		City = "";

		routeSpinner.setEnabled(false);

		ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,routeNoList);
        	routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	       	routeSpinner.setAdapter(routeAdapter);

	}
    };

    private OnItemSelectedListener routeListener = new OnItemSelectedListener() {

	@Override
	public void onItemSelected ( AdapterView<?> parent, View view, int pos, long id) {
		stopList.clear();
		stopList.trimToSize();


		final int pos1 = pos;

		if ( pos != 0 ) {
			stopList.add(getString(R.string.selectStop));
			stopSpinner.setEnabled(true);
            srcStopSpinner.setEnabled(true);
            Route = routeNoList.get(pos);
			new JSONParser(server_add+"get_stops/"+JSONParser.deCapitalize(City)+"?route="+Route, getActivity(), 2).execute();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
            ArrayAdapter<String> srcAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, stopList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            srcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			stopSpinner.setAdapter(adapter);
            srcStopSpinner.setAdapter(srcAdapter);
		}
		else {
			stopList.add(getString(R.string.routeFirst));
			stopSpinner.setEnabled(false);
            srcStopSpinner.setEnabled(false);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter<String> srcAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,stopList);
            srcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        	stopSpinner.setAdapter(adapter);
            srcStopSpinner.setAdapter(srcAdapter);
		}
	}

	@Override
	public void onNothingSelected ( AdapterView<?> parent ) {
		Route = "";

		stopList.clear();
		stopList.trimToSize();

		stopList.add(getString(R.string.routeFirst));

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


    public void addFav() {
	if ( Route != "" ) {
		new JSONParser(server_add+"download.php?city="+City+"&route="+Route, getActivity(), 13).execute();
	}
	else if ( City == "" ) {
		Toast.makeText(getActivity(), getString(R.string.selectCity), Toast.LENGTH_SHORT).show();
	}
	else {
		Toast.makeText(getActivity(), getString(R.string.selectRoute), Toast.LENGTH_SHORT).show();
	}
    }

	private boolean isFavorite(String route) {
		DbHelper db = new DbHelper(trApp.getAppContext(),City);
		Cursor c = db.getTables();

		Boolean flag = false;

		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String temp = c.getString(0);
				if (temp.toLowerCase().equals(route.toLowerCase())) {
					flag = true;
					break;
				}
				c.moveToNext();
			}
		}
		db.closeDB();

		return flag;
	}


	public void favAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		// Setting Dialog Title
		alertDialog.setTitle("Add to Favorites?");

		// Setting Dialog Message
		alertDialog.setMessage("Would you like to add this route to your Favorite?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				addFav();
				new JSONParser(server_add + "download.php?city=" + City + "&route=" + Route, getActivity(), 10).execute();
			}
		});

		alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new JSONParser(server_add + "download.php?city=" + City + "&route=" + Route, getActivity(), 10).execute();
			}
		});

		alertDialog.show();
	}
}