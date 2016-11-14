package com.android.videoplayer;

import java.io.IOException;

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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VideoPlayActivity extends Activity {
	private static final String TAG = "VideoPlayActivity";
	private final String VIDEO_PATH = "video_path";
	private final String VIDEO_POSITION = "video_position";
	
	private VideoController mVideoController = null;
	
	private MediaPlayer mMediaPlayer = null;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	
	private int mLastPlayPosition = 0;
	private String mLastPlayVideoPath = "";
	private String mPlayPath = "";
	private int mCurrentPosition = 0;
	private AudioManager mAudioManager = null;
	private AudioFocuseChangeListener mAudioFocuseChangeListener = null;

	private boolean mIsAudioFocus = false;
	private boolean mIsPauseByUser = false;
	private boolean mIsPlaying = false;
	
	private boolean mIsFromExternalApp = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play_activity);
		requestAudioFouse();
		mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new surfaceCallback());
		initController();
		initMediaPlayer();
		Log.d(TAG,"getIntent.getData : " + getIntent().getData());
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
		if(mMediaPlayer == null){
			mMediaPlayer = new MediaPlayer();
		}
		mMediaPlayer.reset();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		setMediaPlayerListener();
		requestAudioFouse();
	}
	
	private void setMediaPlayerListener(){
		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mVideoController.setPlayState(mPlayPath);
				String lastVideoPath = getLastPath();
				if(mPlayPath.equals(lastVideoPath)){
					mLastPlayPosition = getLastPosition();
					mMediaPlayer.seekTo(mLastPlayPosition);
				}
				mediaPlay();
			}
		});
		
		mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				
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
			String playPath = mVideoController.playVideoPath(intent);
			Log.d(TAG,"playPath :" + playPath);
			playVideo(playPath);
		}
	}
	
	private void playVideo(Uri videoUri){
		String videoStr = videoUri.getPath();
		playVideo(videoStr);
	}
	
	private void playVideo(String videoPath){
		initMediaPlayer();
		mPlayPath = videoPath;
		try {
			mSurfaceHolder = mSurfaceView.getHolder();
			mMediaPlayer.setDataSource(videoPath);
			mMediaPlayer.setDisplay(mSurfaceHolder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setMediaPlayerListener();
		mMediaPlayer.prepareAsync();
	}
	
	private void mediaPlay(){		//继续播放
		if(mMediaPlayer != null){
			mMediaPlayer.start();
		}
	}
	
	private void mediaPause(){		//停止播放
		if(mMediaPlayer != null){
			mMediaPlayer.pause();
		}
	}
	
	private void mediaStop(){
		if(mMediaPlayer != null){
			mCurrentPosition = mMediaPlayer.getCurrentPosition();
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	private void mediaNext(){		//播放下一个
	}
	
	private void mediaPrevious(){	//播放上一个
		
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
	
	public void onBack(View view){
		this.finish();
	}
	
	public void onHome(View view){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}
	
	public void onPlayOrPause(View view){
		if(mIsPlaying){
			mIsPauseByUser = true;
			mIsPlaying = false;
			mediaPause();
		} else {
			mIsPauseByUser = false;
			mIsPlaying = false;
			mediaPlay();
		}
	}
	
	public void onPlayNext(View view){
		
	}
	
	public void onPlayPrevious(View view){
		
	}
	
	public void videoListButtonClicked(View view){
		
	}
	
	public void fullScreenButtonClicked(View view){
		
	}
	
	class surfaceCallback implements SurfaceHolder.Callback{

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
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
			String path = mPlayPath;
			int pos = mMediaPlayer == null ? 0 :mMediaPlayer.getCurrentPosition();
			savePathAndPos(path, pos);
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
}
