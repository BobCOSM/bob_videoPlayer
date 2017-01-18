package com.bob.lessen;

import com.android.videoplayer.R;
import com.android.videoplayer.VideoListActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInsaceStatus){
		super.onCreate(savedInsaceStatus);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
	}
	public void onToVP(View view){
		Intent intent = new Intent(this,VideoListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
