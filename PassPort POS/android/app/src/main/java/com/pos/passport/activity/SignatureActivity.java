package com.pos.passport.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

public class SignatureActivity extends BaseActivity {

	LinearLayout mContent;
	SignatureView mSignatureView;
	Button mClear, mGetSign, mCancel;
	public static String tempDir;
	public int count = 1;
	public String current = null;
	private Bitmap mBitmap;
	View mView;
	File mypath;
	private String uniqueId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_signature);

		tempDir = Environment.getExternalStorageDirectory() + "/" + getString(R.string.txt_external_dir) + "/";
		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		File directory = cw.getDir(getString(R.string.txt_external_dir), Context.MODE_PRIVATE);

		uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
		current = uniqueId + ".png";
		mypath = new File(directory, current);

		mContent = (LinearLayout) findViewById(R.id.linearLayout);
		mSignatureView = new SignatureView(this, null);
		mSignatureView.setBackgroundColor(Color.WHITE);
		mContent.addView(mSignatureView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mClear = (Button) findViewById(R.id.clear);
		mGetSign = (Button) findViewById(R.id.getsign);
		mGetSign.setEnabled(false);
		mCancel = (Button) findViewById(R.id.cancel);
		mView = mContent;
		ViewGroup vg = (ViewGroup)getWindow().getDecorView();
		Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);

		mClear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v("log_tag", "Panel Cleared");
				mSignatureView.clear();
				mGetSign.setEnabled(false);
			}
		});

		mGetSign.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v("log_tag", "Panel Saved");
				boolean error = captureSignature();
				if (!error) {
					mView.setDrawingCacheEnabled(true);
					String sigImage = mSignatureView.getImage(mView);
					Bundle b = new Bundle();
					b.putString("status", "done");
					b.putString("image", sigImage);
					Intent intent = new Intent();
					intent.putExtras(b);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});

		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.v("log_tag", "Panel Canceled");
				Bundle b = new Bundle();
				b.putString("status", "cancel");
				Intent intent = new Intent();
				intent.putExtras(b);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

	}

	@Override
	protected void onDestroy() {
		Log.w("GetSignature", "onDestroy");
		super.onDestroy();
	}

	private boolean captureSignature() {

		boolean error = false;
		String errorMessage = "";

		if (error) {
			Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 105, 50);
			toast.show();
		}

		return error;
	}

	private String getTodaysDate() {

		final Calendar c = Calendar.getInstance();
		int todaysDate = (c.get(Calendar.YEAR) * 10000)
				+ ((c.get(Calendar.MONTH) + 1) * 100)
				+ (c.get(Calendar.DAY_OF_MONTH));
		Log.w("DATE:", String.valueOf(todaysDate));
		return (String.valueOf(todaysDate));

	}

	private String getCurrentTime() {

		final Calendar c = Calendar.getInstance();
		int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000)
				+ (c.get(Calendar.MINUTE) * 100) + (c.get(Calendar.SECOND));
		Log.w("TIME:", String.valueOf(currentTime));
		return (String.valueOf(currentTime));

	}


	public class SignatureView extends View {
		private static final float STROKE_WIDTH = 5f;
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
		private Paint paint = new Paint();
		private Path path = new Path();

		private float lastTouchX;
		private float lastTouchY;
		private final RectF dirtyRect = new RectF();

		public SignatureView(Context context, AttributeSet attrs) {
			super(context, attrs);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH);
		}

		public String getImage(View v) {
			Log.v("log_tag", "Width: " + v.getWidth());
			Log.v("log_tag", "Height: " + v.getHeight());
			if (mBitmap == null) {
				mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
			}
			Canvas canvas = new Canvas(mBitmap);
			
			try {
				v.draw(canvas);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
	        	byte[] bArray = bos.toByteArray();
	        		        	
	        	String encodedImage = Base64.encodeToString(bArray, Base64.NO_WRAP);
	        	Log.v("IMAGE", encodedImage);
	        	return encodedImage;
			} catch (Exception e) {
				return null;
			}
		}

		public void clear() {
			path.reset();
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawPath(path, paint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float eventX = event.getX();
			float eventY = event.getY();
			mGetSign.setEnabled(true);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				path.moveTo(eventX, eventY);
				lastTouchX = eventX;
				lastTouchY = eventY;
				return true;

			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				resetDirtyRect(eventX, eventY);
				int historySize = event.getHistorySize();
				for (int i = 0; i < historySize; i++) {
					float historicalX = event.getHistoricalX(i);
					float historicalY = event.getHistoricalY(i);
					expandDirtyRect(historicalX, historicalY);
					path.lineTo(historicalX, historicalY);
				}
				path.lineTo(eventX, eventY);
				break;

			default:
				debug("Ignored touch event: " + event.toString());
				return false;
			}

			invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
					(int) (dirtyRect.top - HALF_STROKE_WIDTH),
					(int) (dirtyRect.right + HALF_STROKE_WIDTH),
					(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

			lastTouchX = eventX;
			lastTouchY = eventY;

			return true;
		}

		private void debug(String string) {
		}

		private void expandDirtyRect(float historicalX, float historicalY) {
			if (historicalX < dirtyRect.left) {
				dirtyRect.left = historicalX;
			} else if (historicalX > dirtyRect.right) {
				dirtyRect.right = historicalX;
			}

			if (historicalY < dirtyRect.top) {
				dirtyRect.top = historicalY;
			} else if (historicalY > dirtyRect.bottom) {
				dirtyRect.bottom = historicalY;
			}
		}

		private void resetDirtyRect(float eventX, float eventY) {
			dirtyRect.left = Math.min(lastTouchX, eventX);
			dirtyRect.right = Math.max(lastTouchX, eventX);
			dirtyRect.top = Math.min(lastTouchY, eventY);
			dirtyRect.bottom = Math.max(lastTouchY, eventY);
		}
	}
}