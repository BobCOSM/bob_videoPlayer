package com.android.videoplayer;

import android.app.Activity;
import android.os.Bundle;

public class VideoPlayActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play_activity);
	}
}
