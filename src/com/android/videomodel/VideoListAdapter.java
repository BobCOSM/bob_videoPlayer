package com.android.videomodel;

import java.util.ArrayList;

import com.android.util.BitmapLoader;
import com.android.videoplayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private ArrayList<VideoInfo> mVideoList = null;
	private LayoutInflater mInflater = null;
	private Context mAppContext = null;
	private ListView mListView = null;
	private BitmapLoader mLoader = null;
	
	public VideoListAdapter(Context appContext,ArrayList<VideoInfo> videoList){
		mAppContext = appContext;
		mInflater = (LayoutInflater)appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mVideoList = videoList;
		Log.d("VideoListAdapter","maxd-----------VideoListAdapter ==== "+this.toString());
	
		mLoader = new BitmapLoader(appContext, new BitmapLoader.LoaderListener() {
			
			@Override
			public void onLoadFinish(Bitmap bitmap, String uri) {
				ImageView imageView;
				imageView = (ImageView) mListView.findViewWithTag(uri);
				if (imageView != null && bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		});
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mVideoList == null ? 0 : mVideoList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mVideoList == null ? null : mVideoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getItemPath(int position){
		return mVideoList == null? null : 
			mVideoList.get(position) == null ? null : mVideoList.get(position).path; 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(mListView == null){
			mListView = (ListView) parent;
		}
		ViewHolder viewHolder;
		VideoInfo videoInfo = mVideoList.get(position);
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.video_list_cell, null);
			viewHolder.videoThumb = (ImageView)convertView.findViewById(R.id.video_thumb);
			viewHolder.videoTitle = (TextView)convertView.findViewById(R.id.video_titile);
			viewHolder.videoCheck = (ImageView)convertView.findViewById(R.id.video_checked);
			
			viewHolder.videoCheck.setVisibility(
					PlayList.getInstance().isEditMode ? View.VISIBLE : View.GONE);
			convertView.setTag(viewHolder);
			viewHolder.videoThumb.setTag(videoInfo.path);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.videoThumb.setTag(videoInfo.path);
		}

//		viewHolder.videoThumb.setImageBitmap(videoInfo.getThumbImage());
		Bitmap thumb = mLoader.loadBitmap(videoInfo.path);
		if(thumb != null){
			viewHolder.videoThumb.setImageBitmap(thumb);
		}else{
			viewHolder.videoThumb.setImageResource(R.drawable.icon_video_thumb);
		}
		viewHolder.videoTitle.setText(videoInfo.title);
		viewHolder.videoCheck.setImageResource( 
				videoInfo.isChecked() ? R.drawable.check_box_checked : R.drawable.check_box_normal);
		
		convertView.setBackgroundColor(mAppContext.getResources().getColor(
				videoInfo.isSelected ? R.color.video_list_cell_sel_bg : R.color.video_list_cell_nor_bg));
		return convertView;
	}
	
	class ViewHolder{
		ImageView videoThumb = null;
		TextView videoTitle = null;
		ImageView videoCheck = null;
	}
}
