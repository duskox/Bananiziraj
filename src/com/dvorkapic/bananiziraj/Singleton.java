package com.dvorkapic.bananiziraj;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class Singleton extends Application {
	
	private static Context context;
	private static Activity activity;
	
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
		Singleton.facebook = new Facebook("284613044970854"); //Added from FB tut. this is my APP ID
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
	
	public static Facebook.DialogListener fbDialogListenerCallback = new Facebook.DialogListener() {
		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			Log.i(Singleton.TAG, "Completed");
			SharedPreferences.Editor editor = Singleton.prefs.edit();
			editor.putString("access_token", Singleton.facebook.getAccessToken());
			editor.putLong("access_expires", Singleton.facebook.getAccessExpires());
			editor.commit();
//			User revoked access to your app:
//			{"error":{"type":"OAuthException","message":"Error validating access token: User 1053947411 has not authorized application 157111564357680."}}
//
//			OR when password changed:
//			{"error":{"type":"OAuthException","message":"Error validating access token: The session is invalid because the user logged out."}}
		}
		
		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			Log.e(Singleton.TAG, e.getMessage());
		}
		
		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Log.e(Singleton.TAG, e.getMessage());
		}
		
		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			Log.i(Singleton.TAG, "Canceled");
		}	
	};
	
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
	
	public static Activity getActivity() {
		return activity;
	}
	
	public static void setActivity(Activity a) {
		activity = a;
	}
}
