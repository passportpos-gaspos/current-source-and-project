package com.passportsingle;


import com.passportsingle.R;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

public class RecentTransactionsFragment extends AppCompatActivity {
	
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
		setContentView(R.layout.recenttransactionsfragment);
	}
	
	@Override
	public void onResume() {
		Log.v("recent transaction", "Resumed");
		super.onResume();
	}
	
}
