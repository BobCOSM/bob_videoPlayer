package com.android.videoplayer;

import java.io.IOException;

import com.android.videocontroler.VideoController;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VideoPlayActivity extends Activity {
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_play_activity);
		mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new surfaceCallback());
		initController();
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
		mMediaPlayer.setDisplay(mSurfaceHolder);
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
	
	private void playVideo(Uri videoUri){
		String videoStr = videoUri.getPath();
		playVideo(videoStr);
	}
	
	private void playVideo(String videoPath){
		mPlayPath = videoPath;
		try {
			mMediaPlayer.setDataSource(videoPath);
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mMediaPlayer.prepareAsync();
	}
	
	private void play(){		//继续播放
		mMediaPlayer.start();
	}
	
	private void pause(){		//停止播放 
		mMediaPlayer.pause();
	}
	
	private void next(){		//播放下一个
	}
	
	private void previous(){	//播放上一个
		
	}

	private void playCurrentVideo(){
		//mPlayPath  mCurrentPosition
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
			pause();
		} else {
			mIsPauseByUser = false;
			mIsPlaying = false;
			play();
		}
	}
	
	public void onPlayNext(View view){
		
	}
	
	public void onPlayPrevious(View view){
		
	}
	class surfaceCallback implements SurfaceHolder.Callback{

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
	}
	class AudioFocuseChangeListener implements OnAudioFocusChangeListener{

		@Override
		public void onAudioFocusChange(int focusChange) {
			// TODO Auto-generated method stub
			switch(focusChange){
			case AudioManager.AUDIOFOCUS_LOSS: 	//失去音频焦点
				mIsAudioFocus = false;
				pause();
				break;
			case AudioManager.AUDIOFOCUS_GAIN:	//获得音频焦点
				mIsAudioFocus = true;
				if(!mIsPauseByUser){
					play();
				}
				break;
			}
		}
	}
}
