package com.passportsingle;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class HelpFragment extends AppCompatActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        WebView browser = (WebView)findViewById(R.id.yourwebview);
        
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);

        int stringId = getApplicationInfo().labelRes;
        String version = "";
		try {
			version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        browser.loadUrl("http://prioritypos.azurewebsites.net/help/pos/endersposhelp.html?version="+version);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
