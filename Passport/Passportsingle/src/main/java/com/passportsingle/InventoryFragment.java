package com.passportsingle;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

public class InventoryFragment extends AppCompatActivity {
	
public static int location;

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
		
		setContentView(R.layout.inventoryfragment);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.getInt("location") != 0) {
				location = 1;
			}else{
				location = 0;
			}
		}else{
			location = 0;
		}
	}
	
	@Override
	public void onResume() {
		Log.v("Inventory", "Resumed");
		super.onResume();
	}
	
	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Fragment myFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.invDetails_Fragment);

		if (myFragment instanceof InvProductsFragment) {
			myFragment.onActivityResult(requestCode, resultCode, intent);
		}
		
	}*/
	
}