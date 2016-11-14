package com.android.videomodel;

import com.android.videoplayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

public class VideoInfo {
    public Bitmap thumb;
    public String path;
    public int id;
    public String title;
    private Context mContext;
    private String duration;
    private boolean isChecked = false;
    public boolean isPlaying = false;
    public boolean isSelected = false;
    public int lastPosition = 0;

    public VideoInfo(Context context) {
        this.mContext = context;
    }

    public Bitmap getThumbImage() {
        if(thumb != null) return thumb;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        this.thumb = MediaStore.Video.Thumbnails.getThumbnail(
                mContext.getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND,
                options);
        return this.thumb;
    }

    public void setDuration(String durationFormDB) {
        long time = Long.valueOf(durationFormDB);
        String format = null;
        if (time / 60 / 60 / 1000 > 0) {
            long s = (time / 1000) % 60;
            long m = (time / (1000 * 60)) % 60;
            long h = (time / (1000 * 60 * 60)) % 24;
            format = String.format(mContext.getString(R.string.text_hours), h, m, s);
        } else if (time / 60 / 1000 > 0) {
            long s = (time / 1000) % 60;
            long m = (time / (1000 * 60)) % 60;
            format = String.format(mContext.getString(R.string.text_mins), m, s);
        } else {
            long s = (time / 1000) % 60;
            format = String.format(mContext.getString(R.string.text_secs), s);
        }
        duration = format;
    }

    public String getDuration() {
        return duration;
    }

    public static String getProgressFromPosition(int position) {
        int time = position;
        String format = null;
        if (time / 60 / 60 / 1000 > 0) {
            long s = (time / 1000) % 60;
            long m = (time / (1000 * 60)) % 60;
            long h = (time / (1000 * 60 * 60)) % 24;
            format = String.format("%02d:%02d:%02d", h, m, s);
        } else if (time / 60 / 1000 > 0) {
            long s = (time / 1000) % 60;
            long m = (time / (1000 * 60)) % 60;
            format = String.format("00:%02d:%02d", m, s);
        } else {
            long s = (time / 1000) % 60;
            format = String.format("00:00:%02d", s);
        }
        return format;
    }

    public void setChecked(boolean b) {
        isChecked = b;
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoInfo info = (VideoInfo) o;

        if (path != null ? !path.equals(info.path) : info.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}