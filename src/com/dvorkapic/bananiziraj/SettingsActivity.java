package com.dvorkapic.bananiziraj;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;


public class SettingsActivity extends SherlockActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		final Button fbLogout = (Button)findViewById(R.id.fb_logout_btn);
		if(Singleton.facebook.isSessionValid()) {
			fbLogout.setText("Logout");
		} else {
			fbLogout.setText("Login");
		}
		
		fbLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if(Singleton.facebook.isSessionValid()) {
//					//If it is valid then only option is to logout
//					Singleton.mAsyncRunner.logout(Singleton.getContext(), new AsyncFacebookRunner.RequestListener() {
//						@Override
//						public void onMalformedURLException(MalformedURLException e, Object state) {
//							Log.e(Singleton.TAG, "Logout-onMalformedURLException:" + e.getMessage());
//						}
//						@Override
//						public void onIOException(IOException e, Object state) {
//							Log.e(Singleton.TAG, "Logout-onIOException:" + e.getMessage());
//						}
//						@Override
//						public void onFileNotFoundException(FileNotFoundException e, Object state) {
//							Log.e(Singleton.TAG, "Logout-onFileNotFoundException:" + e.getMessage());
//						}
//						@Override
//						public void onFacebookError(FacebookError e, Object state) {
//							Log.e(Singleton.TAG, "Logout-onFacebookError:" + e.getMessage());
//						}
//						@Override
//						public void onComplete(String response, Object state) {
//							Log.i(Singleton.TAG, "Logout-onComplete:" + response);
//							Singleton.logoutFacebook();
//							
//						}
//					});
//					fbLogout.setText("Login");
//				} else {
//					//Otherwise login again
//					Singleton.initFacebook(); //Init facebook variables.
//					if(!Singleton.facebook.isSessionValid()) {
//						Singleton.facebook.authorize(Singleton.getActivity() , new String[] {"publish_stream"}, Singleton.fbDialogListenerCallback);
//					}
//				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		
		return super.onOptionsItemSelected(item);
	}
}
