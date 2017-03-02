package com.bob.lessen;

import java.util.ArrayList;
import java.util.List;

import com.android.videoplayer.R;
import com.android.videoplayer.VideoListActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class MainActivity extends Activity{
	@Override
	public void onCreate(Bundle savedInsaceStatus){
		super.onCreate(savedInsaceStatus);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
	}
	
	public List<View> getAllChildViews(View view){
		List<View> allchildren = new ArrayList<View>();
		if(view instanceof ViewGroup){
			ViewGroup vp = (ViewGroup)view;
			for(int i = 0; i< vp.getChildCount(); i++){
				View viewchild = vp.getChildAt(i);
				allchildren.add(viewchild);
				allchildren.addAll(getAllChildViews(viewchild));
			}
		}
		return allchildren;
	}
	
	public void onToVP(View view){
		Intent intent = new Intent(this,VideoListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	public boolean onKeyDown(int keyCode,KeyEvent event){
		
		return super.onKeyDown(keyCode, event);
	}
}
