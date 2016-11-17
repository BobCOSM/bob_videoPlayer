package com.android.videocontroler;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.android.util.ExternalStorageReceiver;
import com.android.videomodel.PlayList;
import com.android.videomodel.VideoInfo;
import com.android.videomodel.VideoListAdapter;
import com.android.videoplayer.PlayerApplication;
import com.android.videoplayer.VideoListActivity;
import com.android.videoplayer.VideoPlayActivity;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class VideoController {

	private static final String TAG = "VideoPlayer VideoController";
	
	public static final String PLAY_VIDEO_PATH = "play_video_path";
	
	public static final String FILTE_VIDEO_LIST = "filte_video_list"; 
	
	private static VideoController instance = null;
	private Context mAppContext = null;
	private Handler mListHandler = null;
	private ArrayList<VideoInfo> mDeleteFilteList = new ArrayList<VideoInfo>();
	
	private PlayList mPlayList = null;
	private boolean isLoading = false;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			case 0:
				mPlayList.freshPlayListAfterLoad();
				sendMessageToListActivity(VideoListActivity.LOADING_VIDEO_FINISHED);
				break;
			}
		}
	};
	
	public boolean isLoading(){
		return isLoading;
	}
	
	public void setDeleteMode(boolean isDeleteMode){
		mPlayList.isEditMode = isDeleteMode;
		saveDeleteFliterList();
	}
	
	public boolean isDeleteMode(){
		return mPlayList.isEditMode;
	}
	
	private void addVideoToFilteList(VideoInfo videoInfo){
		videoInfo.setChecked(true);
		if(!mDeleteFilteList.contains(videoInfo)){
			mDeleteFilteList.add(videoInfo);
		}
	}
	
	private void removeVideoFromFilteList(VideoInfo videoInfo){
		videoInfo.setChecked(false);
		if(mDeleteFilteList.contains(videoInfo)){
			mDeleteFilteList.remove(videoInfo);
		}
	}
	
	public void selectVideoToFilteList(Object videoInfo){		// 列表移除视频模式时 选中视频
		VideoInfo filteVideo = (VideoInfo)videoInfo;
		if(filteVideo.isChecked()){
			removeVideoFromFilteList(filteVideo);
		}else{
			addVideoToFilteList(filteVideo);
		}
	}
	
	public void addAllVideotoFilteList(){
		removeAll();
		for (VideoInfo filteVideo : mPlayList.getPlayListVideos()) {
			filteVideo.setChecked(true);
			addVideoToFilteList(filteVideo);
		}
	}
	
	public void removeAll(){
		for (VideoInfo fliteVideo : mPlayList.getPlayListVideos()) {
			fliteVideo.setChecked(false);
			removeVideoFromFilteList(fliteVideo);
		}
	}
	
	public int getDeleteFilteSize(){
		return mDeleteFilteList.size();
	}
	
	
	public void clearDeleteFilteList(){
		mDeleteFilteList.clear();
	}

	private ArrayList<String> getFilteList(){
		ArrayList<String> filteList = new ArrayList<String>();
		SharedPreferences sharePref = mAppContext.getSharedPreferences(
				PlayerApplication.SHARE_PREF_NAME, Context.MODE_PRIVATE);
		String jsonStr = sharePref.getString(FILTE_VIDEO_LIST, null);
		if(jsonStr != null){
			try {
				JSONArray jsonArray = new JSONArray(jsonStr);
				for(int i = 0; i<jsonArray.length(); i++){
					String filtePath = jsonArray.optString(i);
					Log.d(TAG,"filtePath : " + filtePath); 
					filteList.add(filtePath);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return filteList;
	}
	
	private void saveShareFilteList(ArrayList<String> filteList){
		SharedPreferences sharePref = mAppContext.getSharedPreferences(
				PlayerApplication.SHARE_PREF_NAME, Context.MODE_PRIVATE);
		JSONArray jsonArray = new JSONArray();
		for(String filteStr : filteList){
			jsonArray.put(filteStr);
		}
		
		if(jsonArray.length() > 0){
			Editor editor = sharePref.edit();
			editor.putString(FILTE_VIDEO_LIST, jsonArray.toString());
			editor.commit();
		}
	}
	
	private void removeShareFilteList(){
		SharedPreferences sharePref = mAppContext.getSharedPreferences(
				PlayerApplication.SHARE_PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = sharePref.edit();
		editor.remove(FILTE_VIDEO_LIST);
		editor.commit();
	}
	
	private void saveDeleteFliterList(){
		//存储在sharePreference里
		ArrayList<String> filteList = getFilteList();
		for(VideoInfo videoInfo : mDeleteFilteList){
			if(!filteList.contains(videoInfo.path)){
				filteList.add(videoInfo.path);
			}
		}
		saveShareFilteList(filteList);
	}
	
	public void removeFilteListFromPlayList(){
		mPlayList.removeFilteList(mDeleteFilteList);
		clearDeleteFilteList();
	}
	
	public ArrayList<VideoInfo> getPlayListVideos(){
		return mPlayList.getPlayListVideos();
	} 
	
	public void clearList(){
		mPlayList.clearList();
	}
	
	public int getPlayListSize(){
		int playListSize = mPlayList.getPlayListSize();
		return playListSize;
	}
	
	public int getCurrentPosition(){
		return mPlayList.getCurrentPosition();
	}
	
	
	private void sendMessageToListActivity(int msg){
		if(mListHandler != null){
			Log.d(TAG,"maxd ----======= send msg:" + msg);
			try {
				mListHandler.sendEmptyMessage(msg);	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private VideoController(){
		mPlayList = PlayList.getInstance();
	}
	
	public static VideoController getInstance(){
		if(instance == null){
			instance = new VideoController();
		}
		return instance;
	}
	
	public void setAppContext(Context context){
		mAppContext = context;
	}
	
	public void setListHandler(Handler listHandler){
		mListHandler = listHandler;
		ExternalStorageReceiver.setVideoListActivityHandler(mListHandler);
	}
	
	public void loadVideoList(){
//		mListHandler.sendEmptyMessage(LOAD_STARTED);
		Log.d(TAG,"maxd --- loadVideoList");
		sendMessageToListActivity(VideoListActivity.LOADING_VIDEO_STARTED);
		
		if(!isLoading && !ExternalStorageReceiver.isScanning(mAppContext)){
			isLoading = true;
			new Thread(new LoadVideoThread()).start();
//			loadVideos(); 
		}else{
			sendMessageToListActivity(VideoListActivity.LOADING_VIDEO_FINISHED);
		}
	}
	
	public void reloadVideoList(){
		removeShareFilteList();
		loadVideoList();
	}
	
	public void startPlayVideoByPath(Context context,String videoPath){
		Intent intent = new Intent(context,VideoPlayActivity.class);
		intent.putExtra(PLAY_VIDEO_PATH, videoPath);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	//启动VideoPlayActivity的Context 不是Activity类的Context要加上这句
		mAppContext.startActivity(intent);
	}
	
//	public String playVideoPath(Intent intent){
//		return intent.getStringExtra(PLAY_VIDEO_PATH);
//	}
	
	public boolean setPlayState(int position){
		return mPlayList.setPlayState(position);
	}
	public boolean setPlayState(String videoPath){
		return mPlayList.setPlayState(videoPath);
	}
	
	public boolean resetLastVideoInfo(){		//重置视频信息
		return mPlayList.setPlayState(null);
	}
	
	public String getNextVideoPath(){
		return mPlayList.getNextVideo().path;
	}
	public String getPrevVideoPath(){
		return mPlayList.getPrevVideo().path;
	}
	
	private VideoInfo createVideoInfo(Cursor cursor){
		VideoInfo video = new VideoInfo(mAppContext);
		int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
        String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
        video.id = id;
        video.path = path;
        video.title = title;
        if(duration != null){
            video.setDuration(duration);	
        }else{
        	Log.d(TAG,"duration is null path : " + path);
        }
		return video;
	}

	private Cursor getVideoCursor(){
		Cursor curVideos = null;
		try {
			ContentResolver mcr = mAppContext.getContentResolver();
			curVideos = MediaStore.Video.query(mcr, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, generateQueryArgs());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return curVideos;
	}

	private String[] generateQueryArgs() {
        return new String[] {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.MIME_TYPE
            };
    }

	private boolean addVideoInfoToPlayList(VideoInfo videoInfo,ArrayList<String> filteList){
		return mPlayList.addVideo(filteList, videoInfo);
	}
	
	private void loadVideos(){
		//加载音频文件
		ArrayList<String> filteList = getFilteList();
		Cursor curVideo = getVideoCursor();
		if(curVideo != null && curVideo.moveToFirst()){
			Log.d(TAG,"start load video");
			do{
				VideoInfo videoInfo = createVideoInfo(curVideo);
				addVideoInfoToPlayList(videoInfo,filteList);
			}while(curVideo.moveToNext());
		}
		isLoading = false;
		Log.d(TAG,"end load video");
	}
	
	class LoadVideoThread implements Runnable{
		@Override
		public void run(){
			while(!VideoListActivity.canLoad){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d(TAG,"started load video");
			loadVideos();
			Log.d(TAG,"finished load video");
			mHandler.sendEmptyMessage(0);
		}
	}
}
