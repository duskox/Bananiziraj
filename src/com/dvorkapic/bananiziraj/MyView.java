package com.dvorkapic.bananiziraj;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MyView extends View {
	private Bitmap bgBitmap = null; //background pic
	private Bitmap bottomBitmap = null; //bottom image, 800px larger side
	private Bitmap topBitmap = null; //top image
	private Bitmap bottomBitmapScaled = null; //scaled to fit the screen
	private Bitmap topBitmapScaled = null; //scaled to fit the bottomBitmapScaled
	private String bottomBitmapPath = ""; //path passed to this object that represents cam or gallery image
	
	private float scaleFact = 1;
	float deltaFactor = 1.0f;
	Paint p = new Paint();
	Bitmap scaled;
	Rect canvasRect = null;
	
	private boolean bottomLoaded = false;
	private boolean topLoaded = false;
	private Rect topR = null;
	private Rect bottomR = null;
	
	private Activity act;
	
	public MyView(Context context, AttributeSet set) {
		super(context, set);
		
		bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pozadina480x480);
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		canvasRect = new Rect(0,0,getWidth(),getHeight());
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		p.setARGB(100, 255, 0, 0);
		
		//Draw background
		canvas.drawBitmap(bgBitmap,null, canvasRect,null);
		
		//Draw bottom Image if loaded
		if(bottomBitmapScaled != null) {
			canvas.drawBitmap(bottomBitmapScaled, ((getWidth() - bottomBitmapScaled.getWidth())/2), ((getHeight() - bottomBitmapScaled.getHeight())/2), null);
		}
		
		//Draw top Image if loaded
		if(topBitmapScaled != null) {
			if (topR != null) {
				canvas.drawBitmap(topBitmapScaled, null, topR, null);
			}
		}
	}
	
	public void resetImages() {
		bottomBitmapPath = null;
		topBitmap = null;
		topBitmapScaled = null;
		bottomBitmapScaled = null;
		invalidate();
	}
	
	public boolean setBottomImg(String imgPath, Activity a) {
		boolean result = false;
		
		act = a;
		bottomBitmapPath = imgPath;
		
		BottomImageProcess bip = new BottomImageProcess();
		bip.execute(bottomBitmapPath);
		
		if (bottomBitmapScaled != null) {
			result = true;
		}
		invalidate();
		return result;
	}
	
	public boolean setTopImg(int i) {
		boolean result = false;
		int tmpX = 0, tmpY = 0;
		float scaleF = 1;
		//topBitmapPath = imgPath;
		
		int[] tmpArr = new int[14];
		tmpArr[0] = R.drawable.osleep;
		tmpArr[1] = R.drawable.osleepcrvena;
		tmpArr[2] = R.drawable.osleeppink;
		tmpArr[3] = R.drawable.osleepplava;
		tmpArr[4] = R.drawable.osleepzelena;
		tmpArr[5] = R.drawable.osleepzuta;
		
		topBitmap = BitmapFactory.decodeResource(getResources(), tmpArr[i]);
		if(topBitmap != null) {
			result = true;
			if ((Math.round(topBitmap.getWidth()*scaleFact) > (bottomR.right-bottomR.left)) || (Math.round(topBitmap.getHeight()*scaleFact) > (bottomR.bottom-bottomR.top))) {
				if (Math.round(topBitmap.getWidth()*scaleFact) > (bottomR.right-bottomR.left)) {
					tmpX = (bottomR.right-bottomR.left)/2;
					scaleF = tmpX/(float)topBitmap.getWidth();
					tmpY = Math.round(topBitmap.getHeight()*scaleF);
				}
				if (Math.round(topBitmap.getHeight()*scaleFact) > (bottomR.bottom-bottomR.top)) {
					tmpY = (bottomR.bottom-bottomR.top)/2;
					scaleF = tmpY/(float)topBitmap.getHeight();
					tmpX = Math.round(topBitmap.getWidth()*scaleF);
				}
				topBitmapScaled = Bitmap.createScaledBitmap(topBitmap, tmpX, tmpY, true);
			} else {
				topBitmapScaled = Bitmap.createScaledBitmap(topBitmap, Math.round(topBitmap.getWidth() * scaleFact), Math.round(topBitmap.getHeight() * scaleFact), true);
			}
		}
		if (topBitmapScaled != null) {
			topLoaded = true;
			if(topR == null) topR = new Rect();
			topR.left = bottomR.centerX() - topBitmapScaled.getWidth()/2;
			topR.top = bottomR.centerY() - topBitmapScaled.getHeight()/2;
			topR.right = topR.left + topBitmapScaled.getWidth();
			topR.bottom = topR.top + topBitmapScaled.getHeight();
		}
		
		
		
		invalidate();
		return result;
	}
	
	public boolean isBottomLoaded() {
		return bottomLoaded;
	}
	
	public boolean isTopLoaded() {
		return topLoaded;
	}
	
	public Rect getTopRect() {
		return topR;
	}
	
	public Rect getBottomRect() {
		return bottomR;
	}
	
	public void setTopPosition(Rect r) {
		topR = r;
		invalidate();
	}
	
	public float getScaleFactor() {
		return scaleFact;
	}
	
	public float getDeltaFactor() {
		return deltaFactor;
	}
	
	public boolean savePic(Uri fileUri) {
		boolean result = true;
		
		Config c = bottomBitmap.getConfig();
		Bitmap b = Bitmap.createBitmap(bottomBitmap.getWidth(), bottomBitmap.getHeight(),c);
		//BitmapFactory.Options bfo = new BitmapFactory.Options();
		//bfo.inMutable = true;
//		try {
//			b = BitmapFactory.decodeFile(fileUri.getPath(), bfo);
//		} catch (Exception e) {
//			Log.e(Singleton.TAG, e.toString());
//			int i = 1;
//		}
		
		Canvas tmpCanvas = new Canvas(b);
		
		float hs = b.getHeight()/bottomBitmapScaled.getHeight();
		float ws = b.getWidth()/bottomBitmapScaled.getWidth();
		
		tmpCanvas.drawBitmap(bottomBitmap, 0, 0, null);
		//Rect tmpRect = new Rect(Math.round((topR.left)*scaleFact), Math.round((topR.top)*scaleFact), Math.round((topR.right)*scaleFact), Math.round((topR.bottom)*scaleFact));
		Rect tmpRect = new Rect(Math.round((topR.left)*ws), Math.round((topR.top)*hs), Math.round((topR.right)*ws), Math.round((topR.bottom)*hs));
		tmpCanvas.drawBitmap(topBitmap, null, tmpRect, null);
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(fileUri.getPath()));
			b.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.e(Singleton.TAG, e.toString());
			result = false;
		}
		
		return result;
	}
	
	private Bitmap checkOrientation(Bitmap b, String path) {
		ExifInterface exifInt = null;
		Bitmap resultB = null;
		try {
			exifInt = new ExifInterface(path);
		} catch (Exception e) {
			
		}
		if (exifInt != null) {
			Matrix matrix = new Matrix();
			switch (Integer.parseInt(exifInt.getAttribute(ExifInterface.TAG_ORIENTATION))) {
				case 1:
					matrix.reset();
					break;
				case 2:
					matrix.reset();
					break;
				case 3:
					matrix.postRotate(180);
					break;
				case 4:
					matrix.reset();
					break;
				case 5:
					matrix.reset();
					break;
				case 6:
					matrix.postRotate(90);
					break;
				case 7:
					matrix.reset();
					break;
				case 8:
					matrix.postRotate(270);
					break;
				default:
					resultB = b;
					break;
			}
			resultB = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
		}
		return resultB;
	}
	
	protected class BottomImageProcess extends AsyncTask<String, Integer, Boolean> {
		Bitmap b1, b2;
		float scaleFactor = 1f;
		ProgressDialog d;
		
		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			float tmpScaleFactor = 1;
			
			
			b1 = BitmapFactory.decodeFile(params[0]);
			
			//rotate image according to EXIF data
			if(b1 != null) {
				result = true;
				b1 = checkOrientation(b1, params[0]);
			
			
				//resize image to 800px larger side
				if (b1.getHeight() > 800 || b1.getWidth() > 800) {
					if(b1.getHeight() > b1.getWidth()) {
						tmpScaleFactor = 800 / (float)b1.getHeight();
						b1 = Bitmap.createScaledBitmap(b1, Math.round(b1.getWidth()*tmpScaleFactor), 800, true);
					} else if(b1.getHeight() < b1.getWidth()) {
						tmpScaleFactor = 800 / (float)b1.getWidth();
						b1 = Bitmap.createScaledBitmap(b1, 800, Math.round(b1.getHeight()*tmpScaleFactor), true);
					} else {
						b1 = Bitmap.createScaledBitmap(b1, 800, 800, true);
					}
				}
				
			
				if(b1.getHeight() > b1.getWidth()) {
					//Portrait
					scaleFactor = (float)getHeight()/(float)b1.getHeight();
					b2 = Bitmap.createScaledBitmap(b1, Math.round(b1.getWidth() * scaleFactor), getHeight(), true);
				} else if(b1.getHeight() < b1.getWidth()) {
					//Landscape
					scaleFactor = (float)getWidth()/(float)b1.getWidth();
					b2 = Bitmap.createScaledBitmap(b1, getWidth(), Math.round(b1.getHeight() * scaleFactor), true);
				} else {
					//1:1
					if (getHeight() > getWidth()) {
						scaleFactor = (float)getWidth()/(float)b1.getWidth();
						b2 = Bitmap.createScaledBitmap(b1, getWidth(), Math.round(b1.getHeight() * scaleFactor), true);
					} else if (getHeight() < getWidth()) {
						scaleFactor = (float)getHeight()/(float)b1.getHeight();
						b2 = Bitmap.createScaledBitmap(b1, Math.round(b1.getWidth() * scaleFactor), getHeight(), true);
					} else {
						b2 = Bitmap.createScaledBitmap(b1, getWidth(), getHeight(), true);
					}
				}
			}
			return result;
		}
		
		@Override
		protected void onPreExecute() {
			d = ProgressDialog.show(act, "", "Loading...", true);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			bottomBitmap = b1;
			bottomBitmapScaled = b2;
			scaleFact = scaleFactor;
			deltaFactor = bottomBitmap.getHeight() / bottomBitmapScaled.getHeight();
			if (b1 != null && b2 != null) {
				if (bottomR == null) bottomR = new Rect();
				bottomR.left = (getWidth() - bottomBitmapScaled.getWidth())/2;
				bottomR.top = (getHeight() - bottomBitmapScaled.getHeight())/2;
				bottomR.right = bottomR.left + bottomBitmapScaled.getWidth();
				bottomR.bottom = bottomR.top + bottomBitmapScaled.getHeight();
				d.dismiss();
				d = null;
				bottomLoaded = true;
				invalidate();
			}
			super.onPostExecute(result);
		}
	}
}
