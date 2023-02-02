package com.passportsingle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationFragment extends Fragment {

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
	private ProgressDialog pd;
	private SharedPreferences mSharedPreferences;
	private boolean registered;
	private boolean licensed;
	public String mpsResponse;
	private String license;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Registration Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.register_form, container, false);

		serialEdit = (TextView) view.findViewById(R.id.serialEdit);
		firstNameEdit = (TextView) view.findViewById(R.id.firstNameEdit);
		lastNameEdit = (TextView) view.findViewById(R.id.lastNameEdit);
		companyEdit = (TextView) view.findViewById(R.id.companyEdit);
		phoneEdit = (TextView) view.findViewById(R.id.phoneEdit);
		
		emailEdit = (TextView) view.findViewById(R.id.emailEdit);
		websiteEdit = (TextView) view.findViewById(R.id.websiteEdit);
		address1Edit = (TextView) view.findViewById(R.id.address1Edit);
		address2Edit = (TextView) view.findViewById(R.id.address2Edit);
		cityEdit = (TextView) view.findViewById(R.id.cityEdit);
		stateEdit = (TextView) view.findViewById(R.id.stateEdit);
		postalEdit = (TextView) view.findViewById(R.id.postalEdit);
		countryEdit = (TextView) view.findViewById(R.id.countryEdit);

		submitRegister = (Button) view.findViewById(R.id.submitRegister);
		serialRow = (TableRow) view.findViewById(R.id.serialRow);
				
		String UID = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID); 
			        
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
				
				pd = ProgressDialog.show(getActivity(), "", "Updating Registration...", true, false);
				new UpdateRegistration().execute(""); 
			}
		});
		
		mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
		
		registered = mSharedPreferences.getBoolean("APOS_REGISTERED", false);
		licensed = mSharedPreferences.getBoolean("APOS_LICENSED", false);
		license = mSharedPreferences.getString("APOS_LICENSE", "");
		
		serialEdit.setText(license);
		serialEdit.setFocusable(true);

		
		firstNameEdit.setText(mSharedPreferences.getString("APOS_FNAME", ""));
		lastNameEdit.setText(mSharedPreferences.getString("APOS_LNAME", ""));
		companyEdit.setText(mSharedPreferences.getString("APOS_COMPANY", ""));
		phoneEdit.setText(mSharedPreferences.getString("APOS_PHONE", ""));
		emailEdit.setText(mSharedPreferences.getString("APOS_EMAIL", ""));
		websiteEdit.setText(mSharedPreferences.getString("APOS_WEBSITE", ""));
		address1Edit.setText(mSharedPreferences.getString("APOS_ADDRESS1", ""));
		address2Edit.setText(mSharedPreferences.getString("APOS_ADDRESS2", ""));
		cityEdit.setText(mSharedPreferences.getString("APOS_CITY", ""));
		stateEdit.setText(mSharedPreferences.getString("APOS_STATE", ""));
		postalEdit.setText(mSharedPreferences.getString("APOS_POSTAL", ""));
		countryEdit.setText(mSharedPreferences.getString("APOS_COUNTRY", ""));
				
		return view;
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
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
	
	private class UpdateRegistration extends AsyncTask<String, Void, String> {

		@SuppressLint("WrongThread")
		@Override
		protected String doInBackground(String... params) {
			try {
								
				String UID = Secure.getString(getActivity().getContentResolver(),Secure.ANDROID_ID);
								
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
	            alertbox("Update Registration Success", "You have succesfully updated your registered information. Thank you.");
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
				e.commit();
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
			HttpPost httppost = new HttpPost("http://polypay.azurewebsites.net/reg/serialreg.php");
			
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