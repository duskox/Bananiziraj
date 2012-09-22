package com.dvorkapic.bananiziraj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class BananizirajActivity extends SherlockActivity implements View.OnTouchListener {
	private Uri fileUri;
	private int xDeltaL, xDeltaR;
	private int yDeltaT, yDeltaB;
	private MyView theView;
	private String picPath;
	
	private Rect canvasRect = new Rect();
	
	ProgressDialog dialog;

	private boolean moveTopRect = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		Singleton.prefs = getPreferences(MODE_PRIVATE);
		Singleton.setContext(this.getApplicationContext());
		Singleton.setActivity(this);
		Singleton.initFacebook(); //Init facebook variables.

		ImageButton saveBtn = (ImageButton)findViewById(R.id.btn_save);
		ImageButton camBtn = (ImageButton)findViewById(R.id.btn_camera);
		ImageButton galBtn = (ImageButton)findViewById(R.id.btn_gallery);
		//HorizontalScrollView horScrlLayout = (HorizontalScrollView)findViewById(R.id.hor_scrl_layout);
		LinearLayout linLayout = (LinearLayout)findViewById(R.id.linear_layout);
		theView = (MyView)findViewById(R.id.the_view);
		theView.setOnTouchListener(this);
		
		if(!Singleton.facebook.isSessionValid()) {
			Singleton.facebook.authorize(this, new String[] {"publish_stream"}, Singleton.fbDialogListenerCallback);
		}
		
		
		int[] tmpArr = new int[14];
		tmpArr[0] = R.drawable.osleep64x64;
		tmpArr[1] = R.drawable.osleep64x64crvena;
		tmpArr[2] = R.drawable.osleep64x64pink;
		tmpArr[3] = R.drawable.osleep64x64plava;
		tmpArr[4] = R.drawable.osleep64x64zelena;
		tmpArr[5] = R.drawable.osleep64x64zuta;
		
		for (int i=0;i<14;i++) {
			ImageView icoImgView = new ImageView(Singleton.getContext());
			icoImgView.setImageResource(tmpArr[i]);
			icoImgView.setId(i);
			linLayout.addView(icoImgView);
			icoImgView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(theView.isBottomLoaded()) {
						//v.setAlpha(0.5f);
						//Toast.makeText(v.getContext(), "ID:" + v.getId(), Toast.LENGTH_SHORT).show();
						theView.setTopImg(v.getId());
					}
				}
			});
		}
		
		camBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				fileUri = getOutputMediaFileUri();
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent, Singleton.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});
		
		galBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, Singleton.SELECT_PICTURE);
			}
		});
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (theView.getBottomRect() != null && theView.getTopRect() != null) {
					Uri tmpUri = getOutputMediaFileUri();
					if (theView.savePic(tmpUri)) {
						//File saved at fileUri
						scanFile(Singleton.getContext(), tmpUri.getPath(), null); //MediaScanner init so file is visible in gallery.
						
						//If facebook session is valid post the img.
						if(Singleton.facebook.isSessionValid()) {
							try {
								//Get byte array from image path.
								Bitmap b = BitmapFactory.decodeFile(tmpUri.getPath());
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
								Singleton.imgData = baos.toByteArray();
							} catch (Exception e) {
								Log.e(Singleton.TAG, "Img 2 byteArray:" + e.getMessage());
							}
							
							if(Singleton.imgData != null) {
								Singleton.mAsyncRunner.request("me", test);
//								try {
//									String response = Singleton.facebook.request("me");
//
//									Bundle params = new Bundle();
//									params.putString(Facebook.TOKEN, Singleton.access_token);
//									params.putByteArray("picture", imgData);
//									params.putString("tag", "@Banana");
//									response = Singleton.facebook.request("me/feed", params, "POST");
//									Log.d(Singleton.TAG, "FB request response:" + response);
//								} catch (Exception e) {
//									Log.e(Singleton.TAG, "FB req. resp. err:" + e.getMessage());
//								}
							}
						} else {
							Toast.makeText(Singleton.getContext(), "You must be logged in to Facebook to complete this action.", Toast.LENGTH_LONG).show();
						}
					} else {
						//Error saving file
					}
				}
			}
		});
	}
	
	private AsyncFacebookRunner.RequestListener test = new AsyncFacebookRunner.RequestListener() {
		public void onComplete(String response, Object state) {
			Bundle params = new Bundle();
			params.putString("method", "photos.upload");
			params.putByteArray("picture", Singleton.imgData);
			//params.putString("tag", "@Banana");
			Singleton.mAsyncRunner.request(null, params, "POST", that, null);
		};
		public void onFacebookError(FacebookError e, Object state) {};
		public void onFileNotFoundException(FileNotFoundException e, Object state) {};
		public void onIOException(IOException e, Object state) {};
		public void onMalformedURLException(MalformedURLException e, Object state) {};
	};
	
	private AsyncFacebookRunner.RequestListener that = new AsyncFacebookRunner.RequestListener() {
		@Override
		public void onComplete(String response, Object state) {
			Log.i(Singleton.TAG, response);
		}
		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.e(Singleton.TAG, "err:" + e.getMessage());
		}
		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.e(Singleton.TAG, "err:" + e.getMessage());
		}
		@Override
		public void onIOException(IOException e, Object state) {
			Log.e(Singleton.TAG, "err:" + e.getMessage());
		}
		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Log.e(Singleton.TAG, "err:" + e.getMessage());
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		Singleton.facebook.extendAccessTokenIfNeeded(this, null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.settings) {
			startActivity(new Intent(Singleton.getContext(), SettingsActivity.class));
		}
		if (item.getItemId() == R.id.undo) {
			theView.resetImages();
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Image from camera, using string path to image.
		if (requestCode == Singleton.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				//Bitmap image = BitmapFactory.decodeFile(data.getExtras().get(MediaStore.Images.Media.TITLE).toString());
				if (data != null) {
					picPath = data.getExtras().get(MediaStore.Images.Media.TITLE).toString();
				}
				if(picPath != "" && picPath != null) {
					theView.setBottomImg(picPath, BananizirajActivity.this);
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Other", Toast.LENGTH_LONG).show();
			}
		}
		//Fotka iz galerije
		if (requestCode == Singleton.SELECT_PICTURE) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Uri selectedImageUri = data.getData();
					String filemanagerstring;
					String selectedImagePath;
					
					//OI FILE Manager
					filemanagerstring = selectedImageUri.getPath();
					
					//MEDIA GALLERY
					selectedImagePath = getPath(selectedImageUri);
					
					//NOW WE HAVE OUR WANTED STRING
					if(selectedImagePath!=null) {
						//Media gallery string
						theView.setBottomImg(selectedImagePath, BananizirajActivity.this);
					} else {
						//File manager string
						theView.setBottomImg(filemanagerstring, BananizirajActivity.this);
					}
				} else {
					Toast.makeText(this, "From gallery - data null", Toast.LENGTH_LONG).show();
				}
			}
		}
		
		Singleton.facebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();
		final Rect rt = theView.getTopRect();
		final Rect rb = theView.getBottomRect();
		int location[] = new int[2];
		boolean multiTouch = false;
		
		if (event.getPointerCount() > 1) multiTouch = true;
		if (multiTouch) {
			
		}
		
		theView.getDrawingRect(canvasRect);
		theView.getLocationOnScreen(location);
		
		if(theView.isBottomLoaded() && theView.isTopLoaded()) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (rt != null) {
					xDeltaL = X - location[0] - rt.left;
					xDeltaR = X - location[0] - rt.right;
					yDeltaT = Y - location[1] - rt.top;
					yDeltaB = Y - location[1] - rt.bottom;
					if (rt.contains(X - location[0], Y - location[1])) {
						moveTopRect = true;
					}
				}
				setTextCoord(X - location[0],Y - location[1],yDeltaT, yDeltaB, xDeltaL, xDeltaR);
				break;
			case MotionEvent.ACTION_UP:
				moveTopRect = false;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_MOVE:
				if (moveTopRect && rb != null) {
					if (rb.contains(X - location[0] - xDeltaL, Y - location[1] - yDeltaT, X - location[0] - xDeltaR, Y - location[1] - yDeltaB)) {
						rt.top = Y - location[1] - yDeltaT;
						rt.left = X - location[0] - xDeltaL;
						rt.bottom = Y - location[1] - yDeltaB;
						rt.right = X - location[0] - xDeltaR;
						theView.setTopPosition(rt);
					}
				}
				setTextCoord(X,Y,rt.top, rt.bottom, rt.left, rt.right);
				break;
			}
		}
		return true;
	}
	
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		//CursorLoader cl = new CursorLoader(Singleton.getContext(), uri, projection, null, null, null);
		//Cursor cursor = cl.loadInBackground();
		if(cursor!=null) {
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}
	
	private Uri getOutputMediaFileUri() {
		return Uri.fromFile(getOutputMediaFile());
	}
	
	private File getOutputMediaFile(){
		File mediaFile = null;
		File mediaStorageDir = null;
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Banana");
			// This location works best if you want the created images to be shared
			// between applications and persist after your app has been uninstalled.
		} else {
			mediaStorageDir = getFilesDir();
		}
		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(Singleton.TAG, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		"IMG_"+ timeStamp + ".jpg");
		
		picPath = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
		return mediaFile;
	}
	
	private void setTextCoord(int x, int y, int rt, int rb, int rl, int rr) {
		TextView tv = (TextView)findViewById(R.id.comment_box);
		//RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) theView.getLayoutParams();
		
		int location[] = new int[2];
		theView.getLocationOnScreen(location);
		//DisplayMetrics om = new DisplayMetrics();
		//getWindowManager().getDefaultDisplay().getMetrics(om);
		//tv.setText("X:" + x + " Y:" + y + " LM:" + lm + " RM:" + rm + " TM:" + tm + " BM:" + bm);
		//tv.setText("LM:" + lm + " RM:" + rm + " TM:" + tm + " BM:" + bm);
		//tv.setText("X:" + x + " Y:" + y + "RT:" + rt + "RB:" + rb + "RL:" + rl + "RR:" + rr);
		if (theView != null) {
			tv.setText("SF:" + theView.getScaleFactor() + " DF:" + theView.getDeltaFactor());
		}
		//tv.setText("X:" + x + " Y:" + y + " RT:" + canvasRect.top + " RB:" + canvasRect.bottom + " RL:" + canvasRect.left + " RR:" + canvasRect.right);
		//tv.setText("X:" + x + " Y:" + y + " H:" + lp.height + " W:" + lp.width + " L0:" + location[0] + " L1:" + location[1]);
	}
	
	public static void scanFile(Context context, String path, String mimeType ) {
		Client client = new Client(path, mimeType);
		MediaScannerConnection connection = new MediaScannerConnection(context, client);
		client.connection = connection;
		connection.connect();
	}

	private static final class Client implements MediaScannerConnectionClient {
		private final String path;
		private final String mimeType;
		MediaScannerConnection connection;

		public Client(String path, String mimeType) {
			this.path = path;
			this.mimeType = mimeType;
		}

		@Override
		public void onMediaScannerConnected() {
			connection.scanFile(path, mimeType);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			connection.disconnect();
		}
	}

}