package com.dvorkapic.bananiziraj;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class Singleton extends Application {
	private static Context context;
	public static final String TAG = "BANANA";
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int SELECT_PICTURE = 1;
	public static final String IMAGE = "IMAGE";
	public static float scaleFactor = 1;
	public static Rect boundaries = new Rect();
	
	public static SharedPreferences prefs;
	public static String access_token;
	public static long expires;
	
	public static Facebook facebook;
	public static AsyncFacebookRunner mAsyncRunner;
	public static byte[] imgData = null;
	
	public static void initFacebook() {
		Singleton.facebook = new Facebook("284613044970854"); //Added from FB tut.
		Singleton.mAsyncRunner = new AsyncFacebookRunner(Singleton.facebook);
		
		Singleton.access_token = Singleton.prefs.getString("access_token", null);
		Singleton.expires = Singleton.prefs.getLong("access_expires", 0);
		
		if(Singleton.access_token != null) {
			Singleton.facebook.setAccessToken(Singleton.access_token);
		}
		if(Singleton.expires != 0) {
			Singleton.facebook.setAccessExpires(Singleton.expires);
		}
	}
	
	public static void logoutFacebook() {
		SharedPreferences.Editor editor = Singleton.prefs.edit();
		editor.remove("access_token");
		editor.remove("access_expires");
		editor.commit();
	}
	
	public static Context getContext() {
		return context;
	}
	
	public static void setContext(Context c) {
		context = c;
	}
}
