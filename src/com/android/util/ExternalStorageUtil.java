package com.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class ExternalStorageUtil {
	
	public final static int MOUNTED = 1;
	public final static int UNMOUNTED = 2;
	private static final String TAG = "ExternalStrorageUtil ";
	public static int getSDExterState() {
		return SDExterState;
	}
	
	private static int SDExterState = 0;
	private static int miLastSDState = 0;

	private static int USBExterState = 0;
	private static int miLastUSBState = 0;
	
	public static boolean checkMediaStoreState() {
		String state1 = getSDExterStatus();
		String state2 = getUSBExterStatus();
		if(Environment.MEDIA_REMOVED == state1 || Environment.MEDIA_BAD_REMOVAL == state1){
			SDExterState = UNMOUNTED;
		}else if(Environment.MEDIA_MOUNTED == state1){
			SDExterState = MOUNTED;
		}
		
		if(Environment.MEDIA_REMOVED == state2 || Environment.MEDIA_BAD_REMOVAL == state2){
			USBExterState = UNMOUNTED;
		}else if(Environment.MEDIA_MOUNTED == state2){
			USBExterState = MOUNTED;
		}
		
		if ((state1.equals(Environment.MEDIA_REMOVED) || state1.equals(Environment.MEDIA_BAD_REMOVAL)) 
         		&& (state2.equals(Environment.MEDIA_REMOVED) || state2.equals(Environment.MEDIA_BAD_REMOVAL))) {
             return false;
        }
		return true;
     }

	public static String getSDExterStatus(){
		String sdStatus = Environment.MEDIA_REMOVED;
		String sdPath = getSDExterPath();
		if(sdPath != null){
			sdStatus = Environment.getStorageState(new File(sdPath));
			Log.d(TAG,"sdStatus : " + sdStatus);
		}
//		Log.d("SDCardUtil","sd status : " + sdStatus);
		return sdStatus;
	}
	
	public static String getUSBExterStatus(){
		String usbStatus = Environment.MEDIA_REMOVED;
		String usbPath = getUSBExterPath();
		if(usbPath != null){
			usbStatus = Environment.getStorageState(new File(usbPath));
			Log.d(TAG,"usbStatus : " + usbStatus);
		}
//		Log.d("SDCardUtil","usb status : " + usbStatus);
		return usbStatus;
	}
	
	public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }
    
    public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
            	if (selection == null) {
            		selection = MediaStore.Audio.Media.DURATION + " > 15000 ";
            	} else {
            		selection = selection + " AND " + MediaStore.Audio.Media.DURATION + " > 15000 ";
            	}
			}
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
         } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

 	public static String getSDExterPath(){
 		List<String> paths = getAllExterSdcardPath();

 		if (paths.size() >= 2) {

 			for (String path : paths) {
 				if (path != null && !path.equals(getFirstExterPath()) && path.contains("sdcard")) {
// 					Log.d("SDCardUtil","exterstorage SD " + path);
 					return path;
 				}
 			}

 			return null;

 		} else {
 			return null;
 		}
 	}
 	
 	public static boolean isSDCardExist(){
 		return getSDExterPath() != null;
 	}
 	
 	public static boolean isUSBExist(){
 		return getUSBExterPath() != null;
 	}
 	
 	public static String getUSBExterPath(){
 		List<String> paths = getAllExterSdcardPath();

 		if (paths.size() >= 2) {

 			for (String path : paths) {
// 				Log.d("SDCardUtil","exterstorage " + path);
 				if (path != null && !path.equals(getFirstExterPath()) && (path.contains("usb") || path.contains("udisk"))) {
// 					Log.d("SDCardUtil","exterstorage USB " + path);
 					return path;
 				}
 			}
 			return null;
 		} else {
 			return null;
 		}
 	}
 	
 	public static String getFirstExterPath() {
 		return Environment.getExternalStorageDirectory().getPath();
 	}

 	public static List<String> getAllExterSdcardPath() {
 		List<String> SdList = new ArrayList<String>();

 		String firstPath = getFirstExterPath();

 		// 得到路径
 		try {
 			Runtime runtime = Runtime.getRuntime();
 			Process proc = runtime.exec("mount");
 			InputStream is = proc.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			String line;
 			BufferedReader br = new BufferedReader(isr);
 			while ((line = br.readLine()) != null) {
 				// 将常见的linux分区过滤掉
 				if (line.contains("secure"))
 					continue;
 				if (line.contains("asec"))
 					continue;
 				if (line.contains("media"))
 					continue;
 				if (line.contains("system") || line.contains("cache")
 						|| line.contains("sys") || line.contains("data")
 						|| line.contains("tmpfs") || line.contains("shell")
 						|| line.contains("root") || line.contains("acct")
 						|| line.contains("proc") || line.contains("misc")
 						|| line.contains("obb")) {
 					continue;
 				}

 				if (line.contains("fat") || line.contains("fuse") || (line
 						.contains("ntfs"))) {
 					
 					String columns[] = line.split(" ");
 					if (columns != null && columns.length > 1) {
 						String path = columns[1];
 						if (path!=null&&!SdList.contains(path)&&(path.contains("sd") || path.contains("usb") || path.contains("udisk")))
 							SdList.add(columns[1]);
 					}
 				}
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}

 		if (!SdList.contains(firstPath)) {
 			SdList.add(firstPath);
 		}

 		return SdList;
 	}
    
 	/**
 	 * 
 	 * @param 针对4.4版本uri不能直接获取文件绝对路径的问题，只对本地文件有效 
 	 * @param uri
 	 * @return 返回uri对应的路径
 	 */
 	
 	public static String getDataColumn(Context context, Uri uri) {    
        
        Cursor cursor = null;    
        final String column = "_data";    
        final String[] projection = {    
                column    
        };
        try {    
            cursor = context.getContentResolver().query(uri, projection, null, null, null);    
            if (cursor != null && cursor.moveToFirst()) {    
                final int column_index = cursor.getColumnIndexOrThrow(column);    
                return cursor.getString(column_index);    
            }    
        } catch(Exception ex){
        	ex.printStackTrace();
        } finally {
            if (cursor != null)    
                cursor.close();    
        }    
        return null;    
    }   
}
