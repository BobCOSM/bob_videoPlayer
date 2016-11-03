package com.android.util;

import com.android.videoplayer.PlayerApplication;
import com.android.videoplayer.VideoListActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

public class ExternalStorageReceiver extends BroadcastReceiver {
	private static final String IS_SCANNING = "ExternalStorageReveiver.IS_SCANNING";
//	private static boolean isScanning = false;

	private static final String TAG = "BroadcastReceiver";
	
	private static Handler videoListActivityHandler = null;
	public static boolean isScanning(Context appContext){
		boolean scanState = false;
		SharedPreferences sharePref = appContext.getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, appContext.MODE_PRIVATE);
		scanState = sharePref.getBoolean(IS_SCANNING, false);
		return scanState;
	}
	
	public static void setVideoListActivityHandler(Handler handler){
		videoListActivityHandler = handler;
	}
	
	private void sendMessageToVideoListActivity(int msg){
		if(videoListActivityHandler != null){
			videoListActivityHandler.sendEmptyMessage(msg);
		}else{
			Log.d(TAG,"videoListActivityHandler is null");
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d("VideoPlayer ExternalStorageReveiver","appcontext context:" + context.getApplicationContext().toString());
		if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
			Log.d("VideoPlayer ExternalStorageReveiver","ACTION_MEDIA_SCANNER_STARTED");
			setScanState(context.getApplicationContext(), true);
			sendMessageToVideoListActivity(VideoListActivity.SCAN_STARTED);
		} else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			Log.d("VideoPlayer ExternalStorageReveiver","ACTION_MEDIA_SCANNER_FINISHED");
			setScanState(context.getApplicationContext(), false);
			sendMessageToVideoListActivity(VideoListActivity.SCAN_FINISHED);
		}
	}
	
	private void setScanState(Context appContext,boolean isScanning){
		SharedPreferences sharePref = appContext.getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, appContext.MODE_PRIVATE);
		Editor editor = sharePref.edit();
		editor.putBoolean(IS_SCANNING, isScanning);
		editor.commit();
	}
}
