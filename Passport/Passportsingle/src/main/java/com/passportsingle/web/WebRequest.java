package com.passportsingle.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;

import android.util.Log;

import com.passportsingle.PrioritySetting;
import com.passportsingle.ProductDatabase;

public class WebRequest {

	private int mTimeout;
	private HashMap<String, String> mWSParameters;
	private String mWebMethodName = "";
	private URL mWebServiceURL;

	public WebRequest(String paramString) throws Exception {
		setWebServiceURL(paramString);
		this.mWebMethodName = "";
		this.mTimeout = 20000;
		this.mWSParameters = new HashMap();
	}

	public void setWebMethodName(String webMethodName) {
		this.mWebMethodName = webMethodName.trim();
	}

	private void setWebServiceURL(String paramString) throws Exception {
		URL param = new URL(paramString.trim());
		if (param.getProtocol().equals("https")) {
			this.mWebServiceURL = param;
			return;
		}
	}

	public void setTimeout(int timeout) throws Exception {
		if (timeout > 0) {
			this.mTimeout = (timeout * 1000);
		} else {
			throw new Exception(String.format("MPSWebRequest Error: %1$s",
					new Object[] { "Timeout value must be greater than 0" }));
		}
	}

	
	public void addParameter(String paramString1, String paramString2)
		    throws Exception
		  {
		    paramString1 = paramString1.trim();
		    paramString2 = paramString2.trim();
		    this.mWSParameters.put(paramString1, paramString2);
		  }
	
	public String voidRequest() {
		try {
			String authString = PrioritySetting.hostedMID.trim() + ":" + PrioritySetting.hostedPass.trim();
			ProductDatabase.insertLog("Void Sale", "Status:" + authString );
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			HttpsURLConnection conn = (HttpsURLConnection) this.mWebServiceURL.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setReadTimeout(this.mTimeout);
			conn.setConnectTimeout(this.mTimeout);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			
			ProductDatabase.insertLog("Void Sale", "Status:" + authStringEnc );
			conn.connect();
			int httpResponseCode =  conn.getResponseCode();
			
			ProductDatabase.insertLog("Void Sale", "Status:" + httpResponseCode );
			
			if(httpResponseCode == 204){
				return "Approved";
			}
			
			return "Declined";
		}catch(Exception e){
			return e.getMessage();
		}
	}
	
	public String validationRequest() throws Exception {
		
		String authString = PrioritySetting.hostedMID.trim() + ":" + PrioritySetting.hostedPass.trim();
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		String buildJson = this.mWSParameters.get("json");
		HttpsURLConnection conn = (HttpsURLConnection) this.mWebServiceURL.openConnection();
		/*conn.setDoOutput(true);
		conn.setDoInput(true);*/
		conn.setRequestMethod("POST");
		conn.setReadTimeout(this.mTimeout);
		conn.setConnectTimeout(this.mTimeout);
		conn.setUseCaches(false);
		conn.setDefaultUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
		
		Log.v("Request Headers", conn.getRequestProperties().toString());
		OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(
				conn.getOutputStream());
		localOutputStreamWriter.write(buildJson);
		Log.v("Soap Response", buildJson);
		localOutputStreamWriter.flush();
		localOutputStreamWriter.close();

		int httpResponseCode =  conn.getResponseCode();
		
		return String.valueOf(httpResponseCode);
		
	}
	

	public String sendRequest() {
		try{
			String responseData = "";
			boolean error = false;
			int k = 0;
			String authString = PrioritySetting.hostedMID.trim() + ":" + PrioritySetting.hostedPass.trim();
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			String buildJson = this.mWSParameters.get("json");
			HttpsURLConnection conn = (HttpsURLConnection) this.mWebServiceURL.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setReadTimeout(this.mTimeout);
			conn.setConnectTimeout(this.mTimeout);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			
			Log.v("Request Headers", conn.getRequestProperties().toString());
			OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(
					conn.getOutputStream());
			localOutputStreamWriter.write(buildJson);
			Log.v("Soap Response", buildJson);
			localOutputStreamWriter.flush();
			localOutputStreamWriter.close();
	
			int httpResponseCode =  conn.getResponseCode();
			BufferedReader rd;
			if (httpResponseCode != 200 && httpResponseCode != 201) {
				rd = new BufferedReader(new InputStreamReader(
						 conn.getErrorStream()));
			} else {
				rd = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
			}
	
			String responseBuffer = "";
			while ((responseBuffer = rd.readLine()) != null) {
				responseData = responseData + responseBuffer;
			}
			rd.close();
	
			int start = 0;
			int end = 0;
			if (httpResponseCode != 200) {
				error = true;
			}
			return responseData;
		}catch(SocketTimeoutException st){
			Log.e("Error", st.getMessage());
			return "{errorCode: 'Timed out, Check your internet connection'}";
		}
		
		catch(Exception e){
			
			Log.e("Error", e.getMessage());
			return "{errorCode: 'Please try again' }";
		}

	}
}
