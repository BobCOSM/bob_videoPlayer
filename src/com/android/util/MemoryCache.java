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

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryCache {
    private static final String TAG = "MemoryCache";

    private static Map<String, Bitmap> mCache = Collections
            .synchronizedMap(new LinkedHashMap<String, Bitmap>(10,
                    1.5f, false));

    private long size = 0;

    private long limit = 5 * 1024 * 1024;

    public MemoryCache() {
        // TODO Auto-generated constructor stub
        setLimit(Runtime.getRuntime().maxMemory() / 8);
    }

    public void setLimit(long new_limit) {
        limit = new_limit;
        Log.i(TAG, "MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
    }

    public Bitmap getBitmap(String url) {
        try {
            if (!mCache.containsKey(url)) {
                return null;
            }
            return mCache.get(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void putBitmap(String url, Bitmap bitmap) {
        if (mCache.containsKey(url)) {
            size -= getBitmapSize(mCache.get(url));
        }
        mCache.put(url, bitmap);
        size += getBitmapSize(bitmap);
        checkSize();
    }

    private void checkSize() {
        if (size > limit) {
            Iterator<Entry<String, Bitmap>> iterator = mCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Bitmap> entry = iterator.next();
                iterator.remove();
                size -= getBitmapSize(entry.getValue());
                if (size < limit) {
                    break;
                }
            }
        }
    }

    private long getBitmapSize(Bitmap bitmap) {
        return bitmap == null ? 0 : bitmap.getRowBytes() * bitmap.getHeight();
    }

    public void clearCache() {
        mCache.clear();
    }
}
