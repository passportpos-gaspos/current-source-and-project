package com.pos.passport.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

public class InventoryActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
		if (Utils.hasSmallerSide(this, Consts.MINIMUM_SIZE_FOR_PORTRAIT_IN_PIXEL)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		setContentView(R.layout.activity_inventory);
	}
	
	@Override
	public void onResume() {
		Log.v("Inventory", "Resumed");
		super.onResume();
	}
}