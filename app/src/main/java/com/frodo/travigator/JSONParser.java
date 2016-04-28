package com.frodo.travigator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;


import android.util.Log;

import com.frodo.travigator.activities.HomeActivity;
import com.frodo.travigator.activities.MainActivity;
import com.frodo.travigator.db.DbHelper;

public class JSONParser extends AsyncTask<String,Void,String> {

    private ProgressDialog pD;
    private Context context;
    private String link;
    private int flag;

    public static ArrayList<String> stopList, latList, lonList, hitList;

    //flag 0 means get and 1 means post.(By default it is get.) Removed this for now. Trying only with GET method
    public JSONParser(String url,Context context, int flag) {
        this.context = context;

        this.link=url;

	this.flag = flag;
    }

    public static String capitalize(String text) {
	char[] ch = text.toLowerCase().toCharArray();
	boolean found = true;

	for ( int i=0; i<ch.length; i++ ) {
		if ( found && Character.isLetter(ch[i]) ) {
			ch[i] = Character.toUpperCase(ch[i]);
			found = false;
		}
		else if ( Character.isWhitespace(ch[i]) || ch[i] == '_' ) {
			found = true;
		}
	}

	return String.valueOf(ch).replaceAll("_"," ").trim();
    }

	public static String deCapitalize(String text) {
		text = text.toLowerCase();
		text = text.replace(' ', '_');
		return text;
	}

    protected void onPreExecute(){
	String message = "";
	if (flag == 0) message = context.getString(R.string.loadingCity);
	else if (flag == 1) message = context.getString(R.string.loadingRoute);
	else if (flag == 2) message = context.getString(R.string.loadingStop);
	else if (flag == 10 || flag == 11) message = context.getString(R.string.loadingData);
 	else if (flag == 13) message = context.getString(R.string.addingFav); 
	pD = ProgressDialog.show(context, context.getString(R.string.Loading), message, true);
		Log.i("url", this.link);
    }

    @Override
    protected String doInBackground(String... arg0) {
	try{

		URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
		String line = "";

		while ( (line = in.readLine()) != null ) {
			if ( line .trim().length() > 0 ) {
				sb.append(line);
			}
		}

                in.close();

                String result =  sb.toString();
		Log.i("Result", result);
		JSONArray jArray = new JSONArray(result);
		JSONObject obj = null;

		if (flag == 0) {
			for (int i = 0; i<jArray.length(); i++) {
				obj = jArray.getJSONObject(i);
				HomeActivity.cityList.add(capitalize(obj.getString("name")));
			}

			return "Cities loaded!";
		}
		else if (flag == 1) {
			for (int i = 0; i<jArray.length(); i++) {
				obj = jArray.getJSONObject(i);
				String temp = obj.getString("route");
				HomeActivity.routeNoList.add(temp);
			}
			return "Routes loaded!";
		}
		else if (flag == 2) {
			for (int i = 0; i<jArray.length(); i++) {
				obj = jArray.getJSONObject(i);
				HomeActivity.stopList.add(obj.getString("stop_name"));
			}

			return "Stops loaded!";
		}
		else if (flag == 10 || flag == 11) {
			stopList = new ArrayList<String>();
			latList = new ArrayList<String>();
			lonList = new ArrayList<String>();
			hitList = new ArrayList<String>();

			for (int i = 0; i<jArray.length(); i++) {
				obj = jArray.getJSONObject(i);
				stopList.add(obj.getString("StopName"));
				latList.add(obj.getString("Latitude"));
				lonList.add(obj.getString("Longitude"));
				hitList.add(obj.getString("Hits"));
			}

			return "Route loaded!";
		}
		else if (flag == 13) {
			DbHelper db = new DbHelper(context, HomeActivity.City, HomeActivity.Route);
			Log.v("FRODO",HomeActivity.Route);
			stopList = new ArrayList<String>();
			latList = new ArrayList<String>();
			lonList = new ArrayList<String>();
			for (int i = 0; i<jArray.length(); i++) {
				obj = jArray.getJSONObject(i);
				stopList.add(obj.getString("StopName"));
				latList.add(obj.getString("Latitude"));
				lonList.add(obj.getString("Longitude"));
			}

			db.setTable(stopList, latList, lonList, 0, stopList.size());
			db.closeDB();		
			return "Favorite Added!";
		}
		else return "Download Failed!";
        }

        catch(Exception e){
		Log.e("FRODO","ERROR",e);
            return new String("Exception: " + e.getMessage());
	}
            
    }

    @Override
    protected void onPostExecute(String result){
	pD.dismiss();
Log.e("FRODO",result); 
	if (result.contains("Exception: ")) {
		Toast.makeText(context, "Connection Failed!", Toast.LENGTH_SHORT).show();
		return;
	}
	else if (result.contains("<") && result.contains(">")) {
		Toast.makeText(context, "PHP Error!", Toast.LENGTH_SHORT).show();
		return;
	}
	else {
		Toast.makeText(context,result,Toast.LENGTH_SHORT).show();
	}

	if (flag == 0) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,HomeActivity.cityList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	HomeActivity.citySpinner.setAdapter(adapter);
	}
	else if (flag == 1) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,HomeActivity.routeNoList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	HomeActivity.routeSpinner.setAdapter(adapter);
	}
	else if (flag == 2) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,HomeActivity.stopList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	HomeActivity.stopSpinner.setAdapter(adapter);
	}
	else if (flag == 10) {
		Intent i = new Intent(context, MainActivity.class);
		i.putExtra(context.getString(R.string.parentKey), "Home");
		context.startActivity(i);
	}
	else if (flag == 11) {
		Intent i = new Intent(context, ShowRoute.class);
		i.putExtra(context.getString(R.string.parentKey), "Home");
		context.startActivity(i);
	}
	else if (flag == 13) {
	}
    }
}