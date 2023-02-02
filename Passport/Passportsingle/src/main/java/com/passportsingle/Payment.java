package com.passportsingle;

import org.json.JSONObject;

import android.util.Log;

public class Payment {
	String paymentType;
	long paymentAmount;
	
	String AuthCode = "", InvoiceNo = "";
	String AcqRefData = "", RecordNo = "";
	String RefNo = "", ProcessData = "";
	String payAmount = "0";
	String request = "";
	String date = "";
	int processed = 0;
	int preSaleID;
	public String response;
	public String TransCode="";
	public long saleID=0;
	public String chargeamount;
	public String PrintCardHolder;
	public String PrintCardNumber;
	public String PrintCardExpire;
	public boolean Print = false;
	
	public void extractJSON() {
		// sax stuff
		try {
					
			String xml = "";
			if(processed == 0)
			{
				xml = request;
				JSONObject response = new JSONObject(xml);
				
				InvoiceNo = String.valueOf(saleID);
				payAmount = response.getString("amount");
			}
			else 
			{
				xml = response;
				JSONObject response = new JSONObject(xml);
				
				RefNo = response.getString("id");
				InvoiceNo = response.getString("authCode");
				payAmount = response.getString("amount");
			}

		} catch (Exception e) {
			Log.e("error", e.getMessage(),  e);
		} 
	}
	

}

