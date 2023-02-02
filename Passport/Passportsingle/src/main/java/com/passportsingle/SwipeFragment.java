package com.passportsingle;

import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;



import com.passportsingle.R;
import com.passportsingle.web.WebRequest;

public class SwipeFragment extends Fragment {

	private CheckBox useMercury;
	private EditText merchantID;
	private EditText webServicePassword;
	private EditText terminalName;
	private Button mSAVE;
	protected ProgressDialog pd;
	private Button mTEST;

	private EditText hostedMID;
	private EditText hostedPass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Mercury Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mercury_settings, container,
				false);

		useMercury = (CheckBox) view.findViewById(R.id.useMercury);

		merchantID = (EditText) view.findViewById(R.id.merchantID);
		webServicePassword = (EditText) view.findViewById(R.id.webServicePassword);
		hostedMID = (EditText) view.findViewById(R.id.hostedMID);
		hostedPass = (EditText) view.findViewById(R.id.hostedPass);
		terminalName = (EditText) view.findViewById(R.id.terminalName);

		mSAVE = (Button) view.findViewById(R.id.payanywhere_save);
		mTEST = (Button) view.findViewById(R.id.mercury_test);

		useMercury.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (useMercury.isChecked()) {
					enableFields();
				} else {
					disableFields();
				}
			}
		});

		mTEST.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(PrioritySetting.enabled)
				{
					pd = ProgressDialog.show(getActivity(), "", "Sending Test Request...", true,
							false);
					new TestCharge().execute();
				}
				else
				{
					alertbox("Payment Settings", "Enable and Save Payment Settings before testing");
				}
			}
		});

		mSAVE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (useMercury.isChecked()) {
					if (merchantID.getText().toString().equals("")) {
						alertbox("Priority Settings", "Need Merchant ID.");
						return;
					}

					if (terminalName.getText().toString().equals("")) {
						alertbox("Priority Settings",
								"Need Unique Terminal Name");
						return;
					}
					
					if(hostedMID.getText().toString().equals("")){
						alertbox("Priority Settings",
								"Need host Merchant ID.");
						return;
					}
					
					if(hostedPass.getText().toString().equals("")){
						alertbox("Priority Settings",
								"Need host Merchant Password.");
						return;
					}

					PrioritySetting.enabled = true;
					PrioritySetting.merchantID = merchantID.getText().toString();
					PrioritySetting.webServicePassword = webServicePassword.getText().toString();
					PrioritySetting.terminalName = terminalName.getText().toString();
					PrioritySetting.hostedMID = hostedMID.getText().toString();
					PrioritySetting.hostedPass = hostedPass.getText().toString();
					PointOfSale.getShop().insertMercurySettings();
				} else {
					PrioritySetting.enabled = false;
					PrioritySetting.merchantID = "";
					PrioritySetting.webServicePassword = "";
					PrioritySetting.terminalName = "";
					PrioritySetting.hostedMID = "";
					PrioritySetting.hostedPass = "";
					PointOfSale.getShop().insertMercurySettings();
				}

				alertbox("Payment Settings", "Settings Saved");

			}
		});

		if (PrioritySetting.enabled) {
			useMercury.setChecked(true);
			enableFields();
		} else {
			disableFields();
		}

		return view;
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setTitle(title)
				.setInverseBackgroundForced(true)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	protected void enableFields() {
		merchantID.setEnabled(true);
		webServicePassword.setEnabled(true);
		terminalName.setEnabled(true);
		hostedMID.setEnabled(true);
		hostedPass.setEnabled(true);
		merchantID.setText(PrioritySetting.merchantID);
		webServicePassword.setText(PrioritySetting.webServicePassword);
		hostedMID.setText(PrioritySetting.hostedMID);
		hostedPass.setText(PrioritySetting.hostedPass);
		terminalName.setText(PrioritySetting.terminalName);
	}

	protected void disableFields() {
		merchantID.setText("");
		webServicePassword.setText("");
		terminalName.setText("");
		hostedMID.setText("");
		hostedPass.setText("");
		
		merchantID.setEnabled(false);
		webServicePassword.setEnabled(false);
		terminalName.setEnabled(false);
		hostedMID.setEnabled(false);
		hostedPass.setEnabled(false);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			}
		}
	};
	public String mpsResponse;
	public String CmdStatus;
	public String TextResponse = "Error";
	
	private class TestCharge extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			try {
				JSONObject carddetails = new JSONObject();
				
				carddetails.put("number", "");
				carddetails.put("expiryMonth", "08");
				carddetails.put("expiryyear", "2017");
				carddetails.put("cvv", "");
				
				JSONObject postJson = new JSONObject();
				postJson.put("merchantId", PrioritySetting.merchantID.trim());
				postJson.put("tenderType", "Card");
				postJson.put("amount", "0");
				postJson.put("cardAccount", carddetails);
				String postXml = postJson.toString();
				
				Log.v("Response", "" + postXml);

				if (hasInternet()) {
					try {
						WebRequest mpswr = new WebRequest(PrioritySetting.mWSURL);
						mpswr.addParameter("json", postXml);
						mpswr.setTimeout(10);
						mpsResponse = mpswr.validationRequest();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				// alertbox("Exception", "error occurred while creating xml data");
				Log.e("Exception", "error occurred while creating xml data");
				e.printStackTrace();
			}

			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
			
			if (hasInternet()) {
				extractJson();
				
				Log.v("Response", "" + result);

				if (CmdStatus.equals("Declined")) {
					// Error
					alertbox("Declined", "Reason: " + TextResponse);
				} else if (CmdStatus.equals("Approved")) {
					// Approved
					alertbox("Success", "Success");

				} else {
					// Declined
					alertbox("Error", "Reason: " + TextResponse);
				}
			}else{
				alertbox("Error", "No internet connection. Connect and try again");
			}
			
			
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
	private boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}

	 public void extractJson(){
		 
		 if(mpsResponse != null){
				try {
					if(Integer.valueOf(mpsResponse) > 400){
						CmdStatus = "Declined";
					switch(Integer.valueOf(mpsResponse)){
					
						case 401:
							TextResponse = "Consumer key/consumer secret were incorrect or not recognized";
							break;
						case 403:
							TextResponse = "Please verify your login credentials";
							break;
						case 404:
							TextResponse = "Request not found";
							break;
						case 500:
							TextResponse = "Internal Server Error";
							break;
						case 501:
							TextResponse = "Service Unavailable";
							break;
						default:
							TextResponse = "Verify your login credentials and try again";
							break;
					}
						
					}else{
						CmdStatus = "Approved";
					}
				}catch(Exception e){
					
					CmdStatus = "";
					Log.e("error", e.getMessage());
				}
		 }
	}
	
	public class DataHandler extends DefaultHandler {

		// private boolean _inSection, _inArea;

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {

			if (localName.equals("CmdStatus")) {
				CmdStatus = tempVal;
			}
			
			if (localName.equals("TextResponse")) {
				TextResponse = tempVal;
			}
		}
	}

}
