package com.android.util;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import java.io.ByteArrayOutputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitmapLoader {

    private static final String TAG = "BitmapLoader";

    private static final String URI_KEY = "uri";

    private static ExecutorService pool;
    private LoaderListener mListener;

    static MemoryCache memoryCache;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            mListener.onLoadFinish((Bitmap) msg.obj, bundle.getString(URI_KEY));
        };
    };

    public interface LoaderListener {
        void onLoadFinish(Bitmap bitmap, String uri);
    }

    public BitmapLoader(Context context, LoaderListener listener) {
        memoryCache = new MemoryCache();
        pool = Executors.newFixedThreadPool(2);
        mListener = listener;
    }

    public Bitmap loadBitmap(String uri) {
        Bitmap bitmap = null;
        long startTime = System.currentTimeMillis();
        bitmap = memoryCache.getBitmap(uri);
        if (bitmap != null) {
            // get bitmap from memory cache
            Log.i(TAG, "get bitmap from memory cache, uri:" + uri + "  time:"
                    + (System.currentTimeMillis() - startTime));
            return bitmap;
        }
        queueBitmap(uri);
        return null;
    }

    private void queueBitmap(String uri) {
        LoadBitmapJob loadBitmapJob = new LoadBitmapJob(uri);
        pool.submit(loadBitmapJob);
    }

    public static class BytesBuffer {
        public byte[] data;
        public int offset;
        public int length;

        public BytesBuffer(int capacity) {
            this.data = new byte[capacity];
        }
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    class LoadBitmapJob implements Runnable {

        private String mUri;

        public LoadBitmapJob(String uri) {
            mUri = uri;
        }

        @Override
        public void run() {
            Bitmap bitmap = null;
            try {
                long startTime = System.currentTimeMillis();

                bitmap = ThumbnailUtils.createVideoThumbnail(mUri.toString(),
                        MediaStore.Video.Thumbnails.MINI_KIND);
                if (bitmap == null) return;
                Log.i(TAG, "create new bitmap, uri:" + mUri + "  time:"
                        + (System.currentTimeMillis() - startTime));

                compressToBytes(bitmap, 90);
                memoryCache.putBitmap(mUri, bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Message message = mHandler.obtainMessage(0, bitmap);
            Bundle bundle = new Bundle();
            bundle.putString(URI_KEY, mUri);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }
}
