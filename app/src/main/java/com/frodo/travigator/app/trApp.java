package com.frodo.travigator.app;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class trApp extends Application {
    private static final Object TAG = Application.class.getSimpleName();
    private static Context mContext;
	private static RequestQueue mRequestQueue;

	@Override
	public void onCreate() {
		super.onCreate();
		trApp.mContext = getApplicationContext();
	}

	public static Context getAppContext() {
		return trApp.mContext;
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getAppContext());
		}

		return mRequestQueue;
	}

	public static  <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public static  <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}
}