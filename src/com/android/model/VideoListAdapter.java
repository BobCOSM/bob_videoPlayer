package com.android.model;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.android.videoplayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private ArrayList<VideoInfo> mVideoList = null;
	private LayoutInflater mInflater = null;
	private Context mAppContext = null;
	public VideoListAdapter(Context appContext,ArrayList<VideoInfo> videoList){
		mAppContext = appContext;
		mInflater = (LayoutInflater)appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mVideoList = videoList;
		Log.d("VideoListAdapter","maxd-----------VideoListAdapter ==== "+this.toString());
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
		ViewHolder viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.video_list_cell, null);
			viewHolder.videoThumb = (ImageView)convertView.findViewById(R.id.video_thumb);
			viewHolder.videoTitle = (TextView)convertView.findViewById(R.id.video_titile);
			viewHolder.videoCheck = (ImageView)convertView.findViewById(R.id.video_checked);
			
			viewHolder.videoCheck.setVisibility(
					PlayList.getInstance().isEditMode ? View.VISIBLE : View.GONE);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		
		VideoInfo videoInfo = mVideoList.get(position);
		Bitmap videoThumb = ThumbnailUtils.createVideoThumbnail(videoInfo.path, Images.Thumbnails.MICRO_KIND);
		
		viewHolder.videoThumb.setImageBitmap(videoThumb);
		
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
