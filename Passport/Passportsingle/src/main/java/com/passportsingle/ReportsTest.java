package com.passportsingle;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

public class ReportsTest extends AppCompatActivity {

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		if(width < height)
		{
			if(width*160/metrics.densityDpi < 600)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		}else{
			if(height*160/metrics.densityDpi < 600)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}			
		}
		
		setContentView(R.layout.new_reports);
	}

	@Override
	public void onResume() {
		Log.v("ReportTest", "Resumed");
		super.onResume();
	}
}