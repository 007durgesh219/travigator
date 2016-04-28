package com.frodo.travigator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.frodo.travigator.activities.HomeActivity;

public class ShowRoute extends Activity
{
    private ArrayList<String> routeData;
    private TextView dir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.showroute);

	dir = (TextView) findViewById(R.id.dir);
	ListView route = (ListView) findViewById(R.id.showRoute);

	String par = getIntent().getStringExtra(getString(R.string.parentKey));
	
	if (savedInstanceState != null) {	
		routeData = savedInstanceState.getStringArrayList("routeData");
		dir.setText(savedInstanceState.getString("dir"));
	}
	else if (par.equals("Home")) {
		routeData = new ArrayList<String> ();
		
		for (int i=0; i<JSONParser.stopList.size(); i++) {
			String temp = String.valueOf(i+1)+". ";
			temp += JSONParser.stopList.get(i)+" (";
			temp += JSONParser.latList.get(i)+",";
			temp += JSONParser.lonList.get(i)+") (Hits: ";
			temp += JSONParser.hitList.get(i)+") ";

			routeData.add(temp);
		}

		dir.setText(HomeActivity.Route + " (" + HomeActivity.City + ")" );
	}
	else if (par.equals("Favorite")) {
		routeData = new ArrayList<String>();

		for (int i=1; i<Favorite.stopList.size(); i++) {
			String temp = String.valueOf(i)+". ";
			temp += Favorite.stopList.get(i)+" (";
			temp += Favorite.latList.get(i-1)+",";
			temp += Favorite.lonList.get(i-1)+") ";

			routeData.add(temp);
		}

		dir.setText(Favorite.Route + " (" + Favorite.City + ")" );
	}
	else {
		routeData = new ArrayList<String>();
	}

	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,routeData);
	route.setAdapter(adapter);
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
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
   
    protected void openSettings() {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putStringArrayList("routeData", routeData);
	outState.putString("dir", dir.getText().toString());
    }
}