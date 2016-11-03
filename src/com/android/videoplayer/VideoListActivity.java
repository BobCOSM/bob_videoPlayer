package com.android.videoplayer;

import com.android.model.VideoListAdapter;
import com.android.util.ExternalStorageReceiver;
import com.android.videocontroler.VideoController;
import com.android.videoplayer.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class VideoListActivity extends Activity {	
	private static final String TAG = "VideoListActivity";
	
	public static final int LOADING_VIDEO_STARTED = 0x00;
	public static final int LOADING_VIDEO_FINISHED = 0x01;
	public static final int SCAN_VIDEO_STARTED = 0x02;
	public static final int SCAN_VIDEO_FINISHED = 0x03;

	public static final int LOAD_STARTED 	= 0x00;
	public static final int LOAD_FINISHED = 0x01;
	public static final int SCAN_STARTED = 0x02;
	public static final int SCAN_FINISHED = 0x03;
	
	public static boolean canLoad = false;
	
	private Context mAppContext = null;
	private ListView mVideoListView = null;
	private View mLoadingProgress = null;
	private VideoController mVideoController = null;
	private View mNoVideoOption = null;
	
	private VideoListAdapter mVideoListAdapter = null;
	private DeleteMenuSpinnerAdapter spinnerAdapter = null;
	
	private ActionMode mActionMode = null;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case LOAD_STARTED:
				Log.d(TAG, "maxd ---- ++----- receive load started ");
				VideoListActivity.canLoad = true;
			case SCAN_STARTED:
				Log.d(TAG, "maxd ---- ++----- receive scan started ");
				mVideoController.clearList();
				mVideoListAdapter.notifyDataSetChanged();
				showOption();
				break;
			case SCAN_FINISHED:
				Log.d(TAG, "maxd ---- ++----- receive scan finished ");
				mVideoController.loadVideoList();
				break;
			case LOAD_FINISHED:
				Log.d(TAG, "maxd ---- ++----- receive load finished ");
				showOption();
				mVideoListAdapter.notifyDataSetChanged();
				VideoListActivity.canLoad = false;
				break;
			}
			Log.d(TAG, "maxd ---- ++----- video list count: " + mVideoController.getPlayListSize());
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppContext = getApplicationContext();
		setContentView(R.layout.video_list_activity);
		mVideoListView = (ListView)findViewById(R.id.video_list);
		mLoadingProgress = findViewById(R.id.loading_progress);
		mNoVideoOption = findViewById(R.id.no_media);
		setItemClicked();
		initController();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_menus, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case R.id.action_delete:
			if(mActionMode != null 
				|| mVideoController.getPlayListSize() <= 0 
				|| mVideoController.isLoading()){
				return false;
			}
			mActionMode = this.startActionMode(mActionModeCallback);
			break;
		case R.id.action_scanning:
			
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initController(){
		mVideoController = VideoController.getInstance();
		
		mVideoController.setAppContext(mAppContext);
		mVideoController.setListHandler(mHandler);
		setVideoListViewAdapter();
//		Log.d(TAG,"maxd------ mVideoListAdapter " + mVideoListAdapter.toString());
//		Log.d(TAG,"maxd------ mVideoListView " + mVideoListView);
//		Log.d(TAG,"maxd------ mVideoController " + mVideoController);
//		Log.d(TAG,"maxd------ mVideos :" + mVideoController.getVideos());
		mVideoController.loadVideoList();
	}
	
	private void setVideoListViewAdapter(){
		mVideoListAdapter = new VideoListAdapter(mAppContext, mVideoController.getPlayListVideos());
		mVideoListView.setAdapter(mVideoListAdapter);
	}
	
	private void showLoadingProgress(){
		mLoadingProgress.setVisibility(View.VISIBLE);
		mNoVideoOption.setVisibility(View.GONE);
	}
	
	private void showOption(){
		if(ExternalStorageReceiver.isScanning(mAppContext) || mVideoController.isLoading()){
			Log.d(TAG,"showLoadingProgress");
			showLoadingProgress();
		}else{
			Log.d(TAG,"hideLoadingProgress");
			hideLoadingProgress();
			if(mVideoController.getPlayListSize() <=0 ){
				showNoVideoOption();
			}
		}
	}
	
	private void hideLoadingProgress(){
		mLoadingProgress.setVisibility(View.GONE);
	}
	
	private void showNoVideoOption(){
		mLoadingProgress.setVisibility(View.GONE);
		mNoVideoOption.setVisibility(View.VISIBLE);
	}
	
	private void setItemClicked(){
		mVideoListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mVideoController.isDeleteMode()){
					mVideoController.selectVideo(mVideoListAdapter.getItem(position));
					mVideoListAdapter.notifyDataSetChanged();
				}else{
					String videoPath = mVideoListAdapter.getItemPath(position);
					Log.d(TAG,"videoPath :" + videoPath);
					mVideoController.startPlayVideoByPath(VideoListActivity.this, videoPath);
				}
			}
		});
	}
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			mActionMode = null;
			mVideoController.setDeleteMode(false);
			mVideoController.removeFilteListFromPlayList();
			mVideoListView.setAdapter(mVideoListAdapter);
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			MenuInflater menuInflater = mode.getMenuInflater();
			menuInflater.inflate(R.menu.main_activity_delete_menus, menu);
			mVideoController.clearDeleteFilteList();
			mVideoController.setDeleteMode(true);
			DeleteMenuSpinner spinner = new DeleteMenuSpinner(VideoListActivity.this);
			spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
			
			spinnerAdapter = new DeleteMenuSpinnerAdapter(VideoListActivity.this,
					android.R.layout.simple_spinner_item, new CharSequence[]{
							getString(R.string.selectd_all),getString(R.string.selectd_none)});
			spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setItemClickListener(new DeleteMenuSpinnerListener());
			spinner.setAdapter(spinnerAdapter);
			mode.setCustomView(spinner);
			mVideoListView.setAdapter(mVideoListAdapter);
			return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case R.id.action_mode_delete:
				mode.finish();
				return true;
			default:
				break;
			}
			Log.d(TAG,"item id :" + item.getItemId());
			return false;
		}
	};
	
	class DeleteMenuSpinner extends Spinner{
		private DeleteMenuSpinnerListener listener = null;
		public DeleteMenuSpinner(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		public void setSelection(int position){
//			super.setSelection(position);
			if(listener != null){
				Log.d(TAG,"spinner listener :" + listener);
				listener.onItemClick( position);
			}
		}
		
		public void setItemClickListener(DeleteMenuSpinnerListener l){
			listener = l;
		}
	}
	
	class DeleteMenuSpinnerAdapter extends ArrayAdapter<CharSequence>{
		private Context context = null;
		public DeleteMenuSpinnerAdapter(Context context, int resource, CharSequence[] objects) {
			super(context, resource, objects);
			this.context = context;
		}
		
		@Override
		public View getView(int position,View convertView ,ViewGroup parent){
//			super.getView(position, convertView, parent);
			TextView textView = new TextView(context, null, android.R.layout.simple_spinner_item);
			textView.setText(String.format(getString(R.string.label_text),mVideoController.getDeleteFilteSize()));
			return textView;
		}
	}
	
	class DeleteMenuSpinnerListener {
		public void onItemClick( int position) {
			// TODO Auto-generated method stub
			if(position == 0){
				Log.d(TAG,"select all");
				selectAll();
			} else if(position == 1){
				Log.d(TAG,"select none");
				removeAll();
			}
		}
		private void selectAll(){
			mVideoController.addAllVideo();
			mVideoListAdapter.notifyDataSetChanged();
		}
		private void removeAll(){
			mVideoController.removeAll();
			mVideoListAdapter.notifyDataSetChanged();
		}
	}
}
