package com.passportsingle.web;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.passportsingle.ChargeScreen;
import com.passportsingle.PrioritySetting;
import com.passportsingle.utils.Utils;

public class ManualRequest extends AsyncTask<String, Void, String> {
	
	private String totalAmount;
	private String mpsResponse;
	private String cardNumber;
	private String cvv;
	private String expMonth;
	private String expYear;
	public ChargeScreen delegate = null;
	
	public ManualRequest(String amount, String cardNumber, String cvv, String month, String year){
		
		this.totalAmount = amount;
		this.cardNumber = cardNumber;
		this.cvv = cvv;
		this.expMonth = month;
		this.expYear = year;
	}

	@Override
	protected String doInBackground(String... params) {
		
		delegate.mpsResponse = postManualData();
		delegate.extractJson();
		return mpsResponse;
		
	}
	
	
	@Override
	protected void onPostExecute(String result){
		
		delegate.processPostData(result);
	}
	
	public String postManualData(){
		
		try{
			JSONObject cardDetails = new JSONObject();
			cardDetails.put("number", cardNumber);
			cardDetails.put("expiryMonth", expMonth);
			cardDetails.put("expiryyear", expYear);
			cardDetails.put("cvv", cvv);
			cardDetails.put("avsZip", "");
			cardDetails.put("avsStree", "");
			
			JSONObject postJson = new JSONObject();
			postJson.put("merchantId", PrioritySetting.merchantID);
			postJson.put("tenderType", "Card");
			if(Float.valueOf(totalAmount) > 0){
				postJson.put("amount", totalAmount);
			}else{
				postJson.put("amount", totalAmount);
			}
			postJson.put("cardAccount", cardDetails);
			
			
			if (Utils.hasInternet()) {
				try {
					WebRequest mpswr = new WebRequest(PrioritySetting.mWSURL);
					mpswr.addParameter("json", postJson.toString());
					mpswr.setTimeout(10);

					mpsResponse = mpswr.sendRequest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}catch(Exception e){
			
			Log.e("Error", e.getMessage());
		}
		
		return mpsResponse;
	}
	
}
