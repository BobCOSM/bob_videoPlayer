package com.android.videomodel;

import java.util.ArrayList;

import android.util.Log;

public class PlayList {
	private static final String TAG = "VideoPlayer PlayList";
	private ArrayList<VideoInfo> videos = null;	//用于加载
	private ArrayList<VideoInfo> playListVideos = null; //用于列表显示
	private static PlayList instance = null;
	public boolean isEditMode = false;
	
	private VideoInfo currentPlayInfo = null;
	private int currentIndex = -1;
	
	public ArrayList<VideoInfo> getPlayListVideos(){
		return playListVideos;
	}
	
	public int getPlayListSize(){
		return playListVideos.size();
	}
	
	public void clearList(){
		videos.clear();
		playListVideos.clear();
	}
	
	private PlayList(){
		videos = new ArrayList<VideoInfo>();
		playListVideos = new ArrayList<VideoInfo>();
	}
	
	public static PlayList getInstance(){
		if(instance == null){
			instance = new PlayList();
		}
		return instance;
	}
	
	public void removeFilteList(ArrayList<VideoInfo> filteList){
		playListVideos.removeAll(filteList);
	}
	
	public void freshPlayListAfterLoad(){
		playListVideos.addAll(videos);
	}
	
	//加入列表 返回 true 未加入列表返回false
	public boolean addVideo(ArrayList<String> fliterList,VideoInfo videoInfo){
		if(fliterList != null){
			if(fliterList.contains(videoInfo.path)){	//过滤列表中存在要加入的视频
//				Log.d(TAG,TAG+" maxd====++++ in fliterlist");
				return false;
			}
		}
		if( !videos.contains(videoInfo) ){
			videos.add(videoInfo);
//			Log.d(TAG,TAG+" maxd====++++ success");
			return true;
		}
//		Log.d(TAG,TAG+" maxd====++++ already in playlist");
		return false;
	}
	
	public boolean setPlayState(int position){
		boolean res = false;
		VideoInfo videoInfo = null;
		resetLastVideoInfo();
		if(position < 0 ){
			return res;
		}
		videoInfo = playListVideos.get(position);
		if(videoInfo != null){
			videoInfo.isPlaying = true;
			videoInfo.isSelected = true;
			currentIndex = position;
			res = true;
		}
		return res;
	}
	
	public boolean setPlayState(String videoPath){
		boolean res = false;
		resetLastVideoInfo();
		if(videoPath == null || "".equals(videoPath)){
			return res;
		}
		for(VideoInfo videoInfo : playListVideos){
			if(videoInfo.path.equals(videoPath)){
				videoInfo.isPlaying = true;
				videoInfo.isSelected = true;
				currentIndex = playListVideos.indexOf(videoInfo);
				res = true;
			}
		}
		return res;
	}
	
	public VideoInfo getNextVideo(){
		VideoInfo videoInfo = null;
		if(currentIndex < playListVideos.size() - 1){
			videoInfo = playListVideos.get(currentIndex + 1);
		}else{
			videoInfo = playListVideos.get(0);
		}
		return videoInfo;
	}

	public VideoInfo getPrevVideo(){
		VideoInfo videoInfo = null;
		if(currentIndex > 0){
			videoInfo = playListVideos.get(currentIndex - 1);
		}else{
			videoInfo = playListVideos.get(playListVideos.size() -1);
		}
		return videoInfo;
	}
	
	private void resetLastVideoInfo(){
		if(currentIndex < 0 || currentIndex >= playListVideos.size()){
			return;
		}
		VideoInfo videoInfo = playListVideos.get(currentIndex);
		videoInfo.isPlaying = false;
		videoInfo.isSelected = false;
	}
	
	public int getCurrentPosition(){
		return currentIndex;
	}
}
