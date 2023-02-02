package com.pos.passport.task;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Customer;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailReceiptSendAsync extends AsyncTask<String, String, String> {

    private Context mContext;
    private Cart mCart;
    private Customer mRecipient;
    private AsyncTaskListener mCallback;
    public final static String POST = "POST";
    protected final static String UTF8 = "UTF-8";
    public int READ_TIMEOUT = 10000;
    public int CONNECTION_TIMEOUT = 15000;
    public BigDecimal transid;
    public BigDecimal changeamount;

    public EmailReceiptSendAsync(Context context, Cart cart, Customer recipient, BigDecimal transid, BigDecimal changeamount)
    {
        this.mContext = context;
        this.mCart = cart;
        this.mRecipient = recipient;
        this.transid=transid;
        this.changeamount=changeamount;
    }

    public void setListener(AsyncTaskListener callback)
    {
        this.mCallback = callback;
    }
    @Override
    public String doInBackground(String... params)
    {
        String result = null;

        try {
            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            try {
                version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();
            JSONObject cust_json = new JSONObject();
            json.put("userId", credential.getUserId());
            json.put("deviceId", credential.getTerminalId());
            json.put("UID", UID);
            json.put("transId", transid);
            cust_json.put("firstName", "" + mRecipient.fName);
            cust_json.put("lastName", "" + mRecipient.lName);
            cust_json.put("email", "" + mRecipient.email);
            cust_json.put("changeamount", changeamount);
            json.put("emailReceipt", cust_json);

            String strUrl = UrlProvider.BASE_URL + UrlProvider.getBase_inner() + UrlProvider.CUSTOMER_EMAIL_SEND;

            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("access-key", UrlProvider.Access_key);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod(POST);
            conn.setDoOutput(true);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
            os.write(json.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            int responseCode = conn.getResponseCode();

            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {

                if (responseCode >= 400 && responseCode <= 499) {
                    Log.e("EmailReceiptSendAsync", "HTTPx Response: " + responseCode + " - " + conn.getResponseMessage());
                    is = new BufferedInputStream(conn.getErrorStream());
                } else {
                    is = new BufferedInputStream(conn.getInputStream());
                }
                //is = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine + "\n");
                }
                result = sb.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("EmailReceiptSendAsync", "Error reading InputStream");
                result = null;
            } finally {
                if (is != null) {
                    is.close();
                }
            }


        } catch (Exception e) {
            Log.d("debug", "error sending receipt to back office");
            result = null;
        }
        return result;
    }

    @Override
    public void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject getresult = new JSONObject(result.toString());
                if (getresult.has("success"))
                    mCallback.onSuccess();
                else
                    mCallback.onFailure();

                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mCallback.onFailure();
    }

}
