package com.android.videoplayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.android.videocontroler.VideoController;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class VideoPlayActivity extends Activity {
	private static final String TAG = "VideoPlayActivity";
	private final String VIDEO_PATH = "video_path";
	private final String VIDEO_POSITION = "video_position";
	
	public static final String IS_FROM_PLAY_ACTIVITY = "is_from_play_activity"; 
	
	private VideoController mVideoController = null;
	
	private Window mWindow = null;
	private MediaPlayer mMediaPlayer = null;
	private SurfaceHolder mSurfaceHolder = null;

	private SurfaceView mSurfaceView = null;
	private SeekBar mVideoProgress = null;
	private ImageButton mPlayButton = null;
	private View mPlayTopLayout = null;
	private View mPlayButtomLayout = null;
	
	
	private int mLastPlayPosition = 0;
	private String mLastPlayVideoPath = "";
	private String mPlayPath = "";
	private int mCurrentPosition = 0;
	private AudioManager mAudioManager = null;
	private AudioFocuseChangeListener mAudioFocuseChangeListener = null;

	private boolean mIsAudioFocus = false;
	private boolean mIsPauseByUser = false;
	private boolean mIsPlaying = false;
	
	private final int SUPPORT_ERROR = 0x01;
	private final int DEVICE_ERROR = 0x02;
	private final int TIME_TASK_UPDATE_PROGRESS = 0x03;
	private boolean mIsFromExternalApp = false;
	
	private boolean isError = false;
	private boolean isFullScreen = false;
	
	private VideoProgressTimer mVideoProgressTimer = null;
	private Timer mTimer = null;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case SUPPORT_ERROR:
				Toast.makeText(VideoPlayActivity.this, 
						getResources().getString(R.string.do_not_support_this_fromat_video), Toast.LENGTH_SHORT).show();
				VideoPlayActivity.this.finish();
				break;
			case DEVICE_ERROR:
				Toast.makeText(VideoPlayActivity.this, 
						getResources().getString(R.string.do_rm_sdcard), Toast.LENGTH_SHORT).show();
				savePathAndPos(mPlayPath, mCurrentPosition);
				VideoPlayActivity.this.finish();
				break;
			case TIME_TASK_UPDATE_PROGRESS:
				if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
					mVideoProgress.setProgress(mMediaPlayer.getCurrentPosition());
				}else{
					
				}
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play_activity);
		requestAudioFouse();
		mPlayButton = (ImageButton)findViewById(R.id.play_button);
		mVideoProgress = (SeekBar)findViewById(R.id.video_progress);
		mPlayTopLayout = findViewById(R.id.player_top_layout);
		mPlayButtomLayout = findViewById(R.id.player_buttom_layout);
		mVideoProgress.setOnSeekBarChangeListener(new VideoProgressListener());
		mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new surfaceCallback());
		mWindow = getWindow();
		
		initController();
//		initMediaPlayer();
		mTimer = new Timer();
		mTimer.schedule(new VideoProgressTimer(), 0, 1000);
		Log.d(TAG,"getIntent.getData : " + getIntent().getData());
	}
	
	@Override
	public void onDestroy(){
		if(mMediaPlayer != null){
			mediaStop();
		}
		super.onDestroy();
	}
	
	private void initController(){
		mVideoController = mVideoController.getInstance();
	}
	
	private void requestAudioFouse(){
		if(mAudioManager == null){
			mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		}
		if(mAudioFocuseChangeListener == null){
			mAudioFocuseChangeListener = new AudioFocuseChangeListener();
		}
		if(!mIsAudioFocus){
			mAudioManager.requestAudioFocus(mAudioFocuseChangeListener, 
					AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		}
	}
	
	private void initMediaPlayer(){
		isError = false;
		if(mMediaPlayer == null){
			mMediaPlayer = new MediaPlayer();
		}
		mMediaPlayer.reset();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		setMediaPlayerListener();
		requestAudioFouse();
	}
	
	private void setVideoWindow(){
		int width = mMediaPlayer.getVideoWidth();
		int height = mMediaPlayer.getVideoHeight();
		if(width == 0 || height == 0){
			return;
		}
		LayoutParams lp = (LayoutParams) mSurfaceView.getLayoutParams();
	}
	
	private void scaleScreen(){
		int width = 0;
		int height = 0;
		float ratio = 0;
        int vWidth = mMediaPlayer.getVideoWidth();
        int vHeight = mMediaPlayer.getVideoHeight();
        View playView = findViewById(R.id.root_view);
        int lw = playView.getWidth();
        int lh = playView.getHeight();
        
        Log.d(TAG,"vWidth: " + vWidth + "  vHeight: " + vHeight);
        Log.d(TAG,"lw: " + lw + "  lh: " + lh);
        
        if((vWidth/vHeight) > (lw/lh)){
        	width = lw;
        	ratio = (float)lw/vWidth;
        	height = (int)(vHeight * ratio);
        } else{
        	height = lh;
        	ratio = (float)lh/vHeight;
        	width = (int)(vWidth * ratio);
        }
        Log.d(TAG,"ratio: " + ratio);
        Log.d(TAG,"width: " + width + "  height: " + height);
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(width, height);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(lp);
	}
	
	private void setMediaPlayerListener(){
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mVideoController.setPlayState(mPlayPath);
				mVideoProgress.setMax(mMediaPlayer.getDuration());
				mSurfaceHolder.setFixedSize(mMediaPlayer.getVideoWidth(),
                        mMediaPlayer.getVideoHeight());
				scaleScreen();
				String lastVideoPath = getLastPath();
				if(mPlayPath.equals(lastVideoPath)){
					mLastPlayPosition = getLastPosition();
					mMediaPlayer.seekTo(mLastPlayPosition);
				}
				mVideoProgress.setProgress(mMediaPlayer.getCurrentPosition());
				mediaPlay();
			}
		});
		
		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onError playpath:" + mPlayPath);
				if(mPlayPath == null || !mPlayPath.contains("storage")){
					mHandler.sendEmptyMessage(SUPPORT_ERROR);
				}else{
					File tmpFile = new File(mPlayPath);
					if(tmpFile == null || tmpFile.exists()){
						mHandler.sendEmptyMessage(SUPPORT_ERROR);
					} else {
						mHandler.sendEmptyMessage(DEVICE_ERROR);
					}
				}
				mediaStop();
				isError = true;
				return true;
			}
		});
		
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mPlayPath = null;
				mCurrentPosition = 0;
				mMediaPlayer.seekTo(mCurrentPosition);
				mediaPause();
				removeSharePref();
				mediaNext();
			}
		});
	}
	
	private void playVideo(){
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if( uri != null ){
			mIsFromExternalApp = true;
			playVideo(uri);
		}else{
			String playPath = intent.getStringExtra(VideoController.PLAY_VIDEO_PATH);
			Log.d(TAG,"playPath :" + playPath);
			mIsFromExternalApp = false;
			playVideo(playPath);
		}
	}
	
	private void playVideo(Uri videoUri){
		initMediaPlayer();
		try {
			mSurfaceHolder = mSurfaceView.getHolder();
			mMediaPlayer.setDataSource(this,videoUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(SUPPORT_ERROR);
		}
	}
	
	private void playVideo(String videoPath){
		Log.d(TAG,"play video path : " + videoPath);
		initMediaPlayer();
		mPlayPath = videoPath;
		try {
			mSurfaceHolder = mSurfaceView.getHolder();
			mMediaPlayer.setDataSource(videoPath);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessage(SUPPORT_ERROR);
		}
	}
	
	private void mediaPlay(){		//继续播放
		if(mMediaPlayer != null){
			mMediaPlayer.start();
			mIsPlaying = true;
		}
		changePlayBtnBk(mPlayButton);
	}
	
	private void mediaPause(){		//停止播放
		if(mMediaPlayer != null){
			mMediaPlayer.pause();
			mIsPlaying = false;
		}
		changePlayBtnBk(mPlayButton);
	}
	
	private void mediaStop(){
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mIsPlaying = false;
		}
		mVideoController.resetLastVideoInfo();
		changePlayBtnBk(mPlayButton);
	}
	
	private void mediaNext(){		//播放下一个
		mediaStop();
		playVideo(mVideoController.getNextVideoPath());
		
	}
	
	private void mediaPrevious(){	//播放上一个
		mediaStop();
		playVideo(mVideoController.getPrevVideoPath());
	}

	private void playCurrentVideo(){
		//mPlayPath  mCurrentPosition
	}
	
	private void savePathAndPos(String path,int pos){
		SharedPreferences sharePref = getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, MODE_PRIVATE);
		Editor editor = sharePref.edit();
		editor.putString(VIDEO_PATH, path);
		editor.putInt(VIDEO_POSITION, pos);
		editor.commit();
	}
	
	private String getLastPath(){
		SharedPreferences sharePref = getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, MODE_PRIVATE);
		String playPath = sharePref.getString(VIDEO_PATH, null);
		return playPath;
	}
	
	private int getLastPosition(){
		SharedPreferences sharePref = getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, MODE_PRIVATE);
		int playPosition = sharePref.getInt(VIDEO_POSITION, 0);
		return playPosition;
	}
	
	private void removeSharePref(){
		SharedPreferences sharePref = getSharedPreferences(PlayerApplication.SHARE_PREF_NAME, MODE_PRIVATE);
		Editor editor = sharePref.edit();
		editor.remove(VIDEO_PATH);
		editor.remove(VIDEO_POSITION);
		editor.commit();
	}
	
	public void onBack(View view){
		this.finish();
	}
	
	public void onHome(View view){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}
	
	private void changePlayBtnBk(View view){
		if(mIsPlaying){
			view.setBackground(getResources().getDrawable(R.drawable.pause_selector));
		} else {
			view.setBackground(getResources().getDrawable(R.drawable.play_selector));
		}
	}
	
	public void onPlayOrPause(View view){
		if(mIsPlaying){
			mIsPauseByUser = true;
			mediaPause();
		} else {
			mIsPauseByUser = false;
			mediaPlay();
		}
	}
	
	public void onPlayNext(View view){
		mediaNext();
	}
	
	public void onPlayPrevious(View view){
		mediaPrevious();
	}
	
	public void videoListButtonClicked(View view){
//		Intent intent = new Intent(VideoPlayActivity.this,VideoListActivity.class);
//		intent.putExtra(IS_FROM_PLAY_ACTIVITY, true);
//		startActivity(intent);
		this.finish();
	}
	
	public void fullScreenButtonClicked(View view){
		fullScreen();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			if(isFullScreen){
				showTopAndButtomBar();
			}else{
				fullScreen();
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private void fullScreen(){
		hideNaviBar();
		mPlayButtomLayout.setVisibility(View.GONE);
		mPlayTopLayout.setVisibility(View.GONE);
		isFullScreen = true;
	}
	
	private void showTopAndButtomBar(){
		mPlayButtomLayout.setVisibility(View.VISIBLE);
		mPlayTopLayout.setVisibility(View.VISIBLE);
		isFullScreen = false;
	}
	
	private void hideNaviBar(){
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | 8);
	}
	
	class surfaceCallback implements SurfaceHolder.Callback{

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			hideNaviBar();
			Log.d(TAG,"serfaceCreated");
			playVideo();
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			Log.d(TAG,"surfaceChanged");
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(TAG,"surfaceDestroyed");
			mCurrentPosition = mMediaPlayer == null ? 0 :mMediaPlayer.getCurrentPosition();
			mediaStop();
			if(!isError){
				savePathAndPos(mPlayPath, mCurrentPosition);
			}
		}
	}
	
	class AudioFocuseChangeListener implements OnAudioFocusChangeListener{

		@Override
		public void onAudioFocusChange(int focusChange) {
			// TODO Auto-generated method stub
			switch(focusChange){
			case AudioManager.AUDIOFOCUS_LOSS: 	//失去音频焦点
				mIsAudioFocus = false;
				mediaPause();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:	//获得音频焦点
				mIsAudioFocus = true;
				if(!mIsPauseByUser){
					mediaPlay();
				}
				break;
			}
		}
	}
	
	class VideoProgressListener implements SeekBar.OnSeekBarChangeListener{
		private long lastTime = 0;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			long currentTime = System.currentTimeMillis();
			if(currentTime - lastTime > 200 && fromUser){
				lastTime = currentTime;
				if(mMediaPlayer != null){
					mMediaPlayer.seekTo(progress);
				}
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if(mMediaPlayer != null){
				mediaPause();
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if(mMediaPlayer != null){
				mediaPlay();
				changePlayBtnBk(mPlayButton);
			}
		}
	}
	
	
	class VideoProgressTimer extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mMediaPlayer != null){
				if(!mVideoProgress.isPressed()){
					mHandler.sendEmptyMessage(TIME_TASK_UPDATE_PROGRESS);
				}
			}
		}
	}
}
