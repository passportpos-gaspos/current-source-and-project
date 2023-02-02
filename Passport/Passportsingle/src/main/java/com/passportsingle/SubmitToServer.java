package com.passportsingle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import android.app.Activity;
import android.provider.Settings.Secure;

public class SubmitToServer {

	public String URL = "http://www.keep-it-secret.co.uk/kawaiiDash/submitscore.php";

	public void SubmitToServerTW(String userName, long time, Activity act) {

		String UID = Secure.getString(act.getContentResolver(),
				Secure.ANDROID_ID);

		try {
			JSONObject json = new JSONObject();
			json.put("UID", UID);
			json.put("TWname", userName);
			json.put("FBname", "");
			json.put("wTime", time);
			postData(json);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void postData(JSONObject json) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();

		try {
			HttpPost httppost = new HttpPost(URL);

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			nvp.add(new BasicNameValuePair("json", json.toString()));
			// httppost.setHeader("Content-type", "application/json");
			httppost.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();
				String jsonResult = inputStreamToString(is).toString();

				JSONObject object = new JSONObject(jsonResult);
			
				String result = object.getString("result");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SubmitToServerFB(String id, Long time, Activity me) {
		String UID = Secure.getString(me.getContentResolver(),
				Secure.ANDROID_ID);

		try {
			JSONObject json = new JSONObject();
			json.put("UID", UID);
			json.put("TWname", "");
			json.put("FBname", id);
			json.put("wTime", time);
			postData(json);

		} catch (JSONException e) {
			e.printStackTrace();
		}
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
