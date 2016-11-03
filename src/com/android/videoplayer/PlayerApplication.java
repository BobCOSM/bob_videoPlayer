package com.android.videoplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class PlayerApplication extends Application {
	private static final String TAG = "VideoPlayer PlayerApplication";
	public static String SHARE_PREF_NAME = "com.android.videoplayer";
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.d(TAG,"getApplicationContext: " + getApplicationContext().toString());
	}
	public SharedPreferences getSharedPref(){
		return this.getApplicationContext().getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE);
	}
}
