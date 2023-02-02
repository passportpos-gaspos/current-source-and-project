package com.passportsingle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;



public class RegisterScreen extends AppCompatActivity {

	private TableRow serialRow; 
	private TextView serialEdit;
	private TextView firstNameEdit;
	private TextView lastNameEdit;
	private TextView companyEdit;
	private TextView phoneEdit;
	private TextView emailEdit;
	private TextView websiteEdit;
	private TextView address1Edit;
	private TextView address2Edit;
	private TextView cityEdit;
	private TextView stateEdit;
	private TextView postalEdit;
	private TextView countryEdit;
	private Button submitRegister;
	private Button buyKey;
	private ProgressDialog pd;
	private SharedPreferences mSharedPreferences;
	private boolean registered;
	private boolean licensed;
	private Handler mHandler;
	public String mpsResponse;
	private WebView myWebView ;
	
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_form);

		serialEdit = (TextView) findViewById(R.id.serialEdit);
		firstNameEdit = (TextView) findViewById(R.id.firstNameEdit);
		lastNameEdit = (TextView) findViewById(R.id.lastNameEdit);
		companyEdit = (TextView) findViewById(R.id.companyEdit);
		phoneEdit = (TextView) findViewById(R.id.phoneEdit);
		
		emailEdit = (TextView) findViewById(R.id.emailEdit);
		websiteEdit = (TextView) findViewById(R.id.websiteEdit);
		address1Edit = (TextView) findViewById(R.id.address1Edit);
		address2Edit = (TextView) findViewById(R.id.address2Edit);
		cityEdit = (TextView) findViewById(R.id.cityEdit);
		stateEdit = (TextView) findViewById(R.id.stateEdit);
		postalEdit = (TextView) findViewById(R.id.postalEdit);
		countryEdit = (TextView) findViewById(R.id.countryEdit);

		submitRegister = (Button) findViewById(R.id.submitRegister);
		serialRow = (TableRow) findViewById(R.id.serialRow);
		buyKey = (Button) findViewById(R.id.buySerialKey);
		
		mHandler = new Handler();
		
		String UID = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
		
		buyKey.setVisibility(View.VISIBLE);
		
		
		buyKey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent webIntent =  new Intent(RegisterScreen.this, WebViewActivity.class);
				webIntent.putExtra("url", getString(R.string.single_html));
				startActivity(webIntent);	  
				
			}
		});
			        
		submitRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(serialEdit.getText().toString().trim().equals(""))
				{
					alertbox("Registration Error", "Registration needs a valid Serial Key.");
					return;
				}
				
				if(firstNameEdit.getText().toString().trim().equals(""))
				{
					alertbox("Registration Error", "Registration needs a first name.");
					return;
				}
				
				if(lastNameEdit.getText().toString().trim().equals(""))
				{
					alertbox("Registration Error", "Registration needs a last name.");
					return;
				}
								
				if(emailEdit.getText().toString().trim().equals(""))
				{
					alertbox("Registration Error", "Registration needs an Email Address.");
					return;
				}
								
				pd = ProgressDialog.show(RegisterScreen.this, "", "Sending Registration...", true, false);
				new SendRegistration().execute(""); 
			}
		});
		
		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
		
		registered = mSharedPreferences.getBoolean("APOS_REGISTERED", false);
		licensed = mSharedPreferences.getBoolean("APOS_LICENSED", false);
	}
	
	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
	private class SendRegistration extends AsyncTask<String, Void, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... params) {
			try {
								
				String UID = Secure.getString(getContentResolver(),Secure.ANDROID_ID);
				
				
				
				JSONObject json = new JSONObject();
				
				
				json.put("serial", serialEdit.getText().toString().trim());			
				json.put("firstName", firstNameEdit.getText().toString());
				json.put("lastName", lastNameEdit.getText().toString());
				json.put("company", companyEdit.getText().toString());			
				json.put("phone", phoneEdit.getText().toString());
				json.put("email", emailEdit.getText().toString());
				json.put("website", websiteEdit.getText().toString());
				json.put("address1", address1Edit.getText().toString());
				json.put("address2", address2Edit.getText().toString());
				json.put("city", cityEdit.getText().toString());
				json.put("state", stateEdit.getText().toString());
				json.put("postal", postalEdit.getText().toString());
				json.put("country", countryEdit.getText().toString());
				json.put("UID", UID);
				json.put("make", Build.BRAND);
				json.put("model", Build.MODEL);
				json.put("android", Build.VERSION.RELEASE);
							
				mpsResponse = postData(json);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			// Do something here...
            pd.dismiss();
            			
			if(result!=null && result.contains("success"))
			{
	            alertbox("Registration Success", "This app is succesfully registered. Thank you.");
				Editor e = mSharedPreferences.edit();
				e.putBoolean("APOS_LICENSED", true);
				e.putBoolean("APOS_REGISTERED", true);
				e.putString("APOS_FNAME", firstNameEdit.getText().toString());
				e.putString("APOS_LNAME", lastNameEdit.getText().toString());
				e.putString("APOS_COMPANY", companyEdit.getText().toString());
				e.putString("APOS_PHONE", phoneEdit.getText().toString());
				e.putString("APOS_EMAIL", emailEdit.getText().toString());
				e.putString("APOS_WEBSITE", websiteEdit.getText().toString());
				e.putString("APOS_ADDRESS1", address1Edit.getText().toString());
				e.putString("APOS_ADDRESS2", address2Edit.getText().toString());
				e.putString("APOS_CITY", cityEdit.getText().toString());
				e.putString("APOS_STATE", stateEdit.getText().toString());
				e.putString("APOS_POSTAL", postalEdit.getText().toString());
				e.putString("APOS_COUNTRY", countryEdit.getText().toString());
				e.putString("APOS_LICENSE", serialEdit.getText().toString().trim());
				e.commit();
				finish();
			}
			else if(result!=null && result.contains("failed"))
			{
	            alertbox("Registration Failed", "This app is did not succesfully register. Please try again.");
			}
			else if(result!=null && result.contains("nokey"))
			{
	            alertbox("Serial Invalid", "That Serial Key does not exist. Please try again.");
			}
			else if(result!=null && result.contains("keyused"))
			{
	            alertbox("Serial Invalid", "That Serial Key has previusly been used. Please try again.");
			}
			else
			{
	            alertbox("Registration ERROR", "This app is did not succesfully register. Please try again.");
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public String postData(JSONObject json) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();

		String result = null ;
		try {
			//HttpPost httppost = new HttpPost("http://advantageedge.azurewebsites.net/reg/serialreg.php");
			HttpPost httppost = new HttpPost("http://prioritypos.azurewebsites.net/reg/serialreg.php");
			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			String sendData = Base64.encodeToString(convert_2_unicode(json.toString()).getBytes("UTF-8"), Base64.NO_WRAP);

			nvp.add(new BasicNameValuePair("register", sendData));
			httppost.setEntity(new UrlEncodedFormEntity(nvp, "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();
				String jsonResult = inputStreamToString(is).toString();
				JSONObject object = new JSONObject(jsonResult);
				result  = object.getString("result");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public String convert_2_unicode(String str){

        String string = "";
        try {
            byte[] utf8 = str.getBytes("UTF-8");
            string = new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        } 

        return string;
    }
	
	private StringBuilder inputStreamToString(InputStream is) {
		String rLine = "";
		StringBuilder answer = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		try {
			while ((rLine = rd.readLine()) != null) {
				answer.append(rLine);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}

}
