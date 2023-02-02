package com.passportsingle;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.passportsingle.R;

import android.view.Window;

public class KeyInScreen extends AppCompatActivity {

	private String invoice;
	private String cashier;
	private String totalAmount;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.help);
       
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        final WebView browser = (WebView)findViewById(R.id.yourwebview);
        
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);

        Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.getString("InvoiceID") != null) {
				invoice = bundle.getString("InvoiceID");
			}
			if (bundle.getString("Cashier") != null) {
				cashier = bundle.getString("Cashier");
			}
			if (bundle.getString("Amount") != null) {
				totalAmount = bundle.getString("Amount");
			}
		}
		
		String saleType = "";
		String amount = "";
		if (Float.valueOf(totalAmount) > 0) {
			saleType = "Sale";
			amount = totalAmount;
		} else {
			saleType = ("Return");
			amount = ""+(Float.valueOf(totalAmount) * -1);
		}
		
		String terminal = PrioritySetting.terminalName;
		String merchantID = PrioritySetting.hostedMID;
		String webPass = PrioritySetting.hostedPass;
		
		int stringId = getApplicationInfo().labelRes;
		String version = "";
		try {
			version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String memo = getString(stringId) + " v" + version;
		
		/*"MerchantID" =>	$merchantID,
				"Password"	=>	$webPassword,
				"Frequency"	=>	"OneTime",
                "CVV" => "On",
                "Keypad" => "On",
                "CancelButton" => "On",
                "CardEntryMethod" => "Manual",
                "Forcemanualtablet" => "On",
				"ProcessCompleteUrl"	=>	"http://advantageedge.azurewebsites.net/mhc/response.php",
				"ReturnUrl"	=>	"http://advantageedge.azurewebsites.net/mhc/return.php"
				*/
		
		browser.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress)   
            {
             //Make the bar disappear after URL is loaded, and changes string to Loading...
				setTitle("Loading...");
				setProgress(progress * 100); //Make the bar disappear after URL is loaded

             // Return the app name after finish loading
                if(progress > 98)
                	setTitle("Manual Key In");
              }
		});
		
			browser.setWebViewClient(new WebViewClient() {
			@Override
		    public void onPageFinished(WebView view, String url){
		    	String urlHolder;
		        String[] verifExtrctr;
		        Log.v("URL", url);
		        
		        if(url.toLowerCase().contains("https://prioritypos.azurewebsites.net/mhc/response.php"))
		        {
		        
		        Uri uri = Uri.parse(url);
		        Set<String> paramNames = uri.getQueryParameterNames();
		        for (String key: paramNames) {
		            String value = uri.getQueryParameter(key);
		            	            
		            if(key.equals("json"))
		            {
		            	try {
		            		byte[] data = Base64.decode(value, Base64.DEFAULT);
		            		String text = new String(data, "UTF-8");
		            		Log.v("OUTPUT", text);
							JSONObject mpsResponse = new JSONObject(text);
							if(mpsResponse.has("VerifyPaymentResult"))
							{																		
								JSONObject VerifyPaymentResult = mpsResponse.getJSONObject("VerifyPaymentResult");
								if(VerifyPaymentResult.has("Status"))
								{
									String Status = VerifyPaymentResult.getString("Status");
									if (Status.equals("Declined")) {
										// Error
										
										Intent intent = KeyInScreen.this.getIntent();
										KeyInScreen.this.setResult(Activity.RESULT_OK, intent);
																
										String Reason = VerifyPaymentResult.getString("DisplayMessage");
										
										intent.putExtra("TYPE", 2);
										intent.putExtra("STATUS", Status);
										intent.putExtra("MESSAGE", Reason);
										
										finish();
																				
										//alertbox("Declined", "Reason: " + Reason);
									} else if (Status.equals("Approved")) {
										// Approved
										Intent intent = KeyInScreen.this.getIntent();
										KeyInScreen.this.setResult(Activity.RESULT_OK, intent);
										
										
										intent.putExtra("AMOUNT", VerifyPaymentResult.getString("AuthAmount"));									
										intent.putExtra("TYPE", 2);
										intent.putExtra("STATUS", Status);
										intent.putExtra("AUTH_CODE", VerifyPaymentResult.getString("AuthCode"));
										intent.putExtra("AcqRefData", VerifyPaymentResult.getString("AcqRefData"));
										intent.putExtra("RefNo", VerifyPaymentResult.getString("RefNo"));
										intent.putExtra("ProcessData", VerifyPaymentResult.getString("ProcessData"));
										intent.putExtra("InvoiceNo", VerifyPaymentResult.getString("Invoice"));
										intent.putExtra("TranCode", VerifyPaymentResult.getString("TranType"));
										intent.putExtra("RecordNo", VerifyPaymentResult.getString("Token"));
										intent.putExtra("CvvResult", VerifyPaymentResult.getString("CvvResult"));
										finish();
									} else {
										
										Intent intent = KeyInScreen.this.getIntent();
										KeyInScreen.this.setResult(Activity.RESULT_OK, intent);
																
										String Reason = VerifyPaymentResult.getString("DisplayMessage");
										
										intent.putExtra("TYPE", 2);
										intent.putExtra("STATUS", Status);
										intent.putExtra("MESSAGE", Reason);
										
										finish();
										// Declined
										//alertbox("Error", "Reason: " + VerifyPaymentResult.getString("DisplayMessage"));
									}
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		        }
		        
		        }
		        else if(url.toLowerCase().contains("https://prioritypos.azurewebsites.net/mhc/return.php"))
		        {
		        	
		        	
		        	if(url.contains("ReturnCode=102"))
		        	{
		        		Intent intent = KeyInScreen.this.getIntent();
						KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
						
						intent.putExtra("TYPE", 2);
						intent.putExtra("STATUS", "CANCELED");
						
						finish();
		        		Log.v("RETURN","User Cancelled");
		        	}
		        	
		        	else if(url.contains("ReturnCode=103"))
		        	{
		        		Intent intent = KeyInScreen.this.getIntent();
						KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
						
						intent.putExtra("TYPE", 2);
						intent.putExtra("STATUS", "TIMEOUT");
						
						finish();
		        		Log.v("RETURN","Session Timeout");
		        	}
		        	
		        	else if(url.contains("ReturnCode=104"))
		        	{
		        		Intent intent = KeyInScreen.this.getIntent();
						KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
						
						intent.putExtra("TYPE", 2);
						intent.putExtra("STATUS", "UNAVAILABLE");
						
						finish();
		        		Log.v("RETURN","Service Unavailable");
		        	}
		        	else
		        	{
		        		Intent intent = KeyInScreen.this.getIntent();
						KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
						
						intent.putExtra("TYPE", 2);
						intent.putExtra("STATUS", "ERROR");
						
						finish();
		        		Log.v("RETURN","Processing Error");
		        	}
		        }
		        //else    
		        //{
		        //	browser.loadUrl(url);
		        //}
		   }
		});
			
		JSONObject json = new JSONObject();

		try {
			json.put("inv", invoice);			
			json.put("op", cashier);
			json.put("tot", amount);
			json.put("tran", saleType);
			json.put("term", terminal);
			json.put("memo", memo);
			json.put("mid", merchantID);
			json.put("wep", webPass);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String encoded = Base64.encodeToString(json.toString().getBytes(), Base64.DEFAULT);
		String postData = "data="+encoded;
				
		browser.postUrl(PrioritySetting.mMANURL,EncodingUtils.getBytes(postData, "BASE64"));
		
       /* browser.loadUrl("https://advantageedge.azurewebsites.net/mhc/open.php?"+
        "inv="+invoice+
        "&op="+cashier+
        "&tot="+amount+
        "&tran="+saleType+
        "&term="+terminal+
        "&memo="+memo+
        "&mid="+merchantID+
        "&wep="+webPass);*/
        
		ActionBar actionBar = getSupportActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);
		

	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = KeyInScreen.this.getIntent();
				KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
				
				intent.putExtra("TYPE", 2);
				intent.putExtra("STATUS", "CANCELLED");
				intent.putExtra("MESSAGE", "User Cancelled");
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		
		Intent intent = KeyInScreen.this.getIntent();
		KeyInScreen.this.setResult(Activity.RESULT_CANCELED, intent);
		
		intent.putExtra("TYPE", 2);
		intent.putExtra("STATUS", "CANCELLED");
		intent.putExtra("MESSAGE", "User Cancelled");
		finish();
		Log.v("RETURN","User Cancelled");

		return;
	}
}
