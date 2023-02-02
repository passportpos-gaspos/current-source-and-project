package com.passportsingle;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity{
	
	private WebView myWebView;
	private String url;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		url = bundle.getString("url");
		
		setContentView(R.layout.priority_webview);
		myWebView = (WebView) findViewById(R.id.webview);
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.getSettings().setJavaScriptEnabled(true);
		
		myWebView.loadUrl(url);
		
	}
	
	@Override
	public void onResume(){
		
		super.onResume();
	}

}
