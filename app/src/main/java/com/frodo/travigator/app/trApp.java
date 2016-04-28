package com.frodo.travigator.app;

import android.content.Context;

public class trApp extends android.app.Application {
	private static Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		trApp.mContext = getApplicationContext();
	}

	public static Context getAppContext() {
		return trApp.mContext;
	}
}