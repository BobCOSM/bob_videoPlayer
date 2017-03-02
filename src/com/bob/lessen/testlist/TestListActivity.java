package com.bob.lessen.testlist;

import com.android.videoplayer.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class TestListActivity extends Activity {
	
	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_test_layout);
		mListView = (ListView)findViewById(R.id.list_test);
	}
}
