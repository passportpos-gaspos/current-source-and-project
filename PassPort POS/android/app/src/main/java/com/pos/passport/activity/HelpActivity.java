package com.pos.passport.activity;

import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

public class HelpActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        setContentView(R.layout.activity_help);

        ViewGroup vg = (ViewGroup)getWindow().getDecorView();
		Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
        WebView browser = (WebView)findViewById(R.id.help_web_view);
        
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);

        String version = "";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
        browser.loadUrl(UrlProvider.BASE_URL + "/help/cloud/endersposhelp.html?version=" + version);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
