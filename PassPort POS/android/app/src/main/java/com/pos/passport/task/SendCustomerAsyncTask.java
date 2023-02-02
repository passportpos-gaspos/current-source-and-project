package com.pos.passport.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pos.passport.model.Customer;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.UrlProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 1/27/2016.
 */
public class SendCustomerAsyncTask extends AsyncTask<String, String, String> {

    private Context mContext;
    private Customer mCustomer;
    public int READ_TIMEOUT = 10000;
    public int CONNECTION_TIMEOUT = 15000;
    public SendCustomerAsyncTask(Context context, Customer customer){
        this.mContext = context;
        this.mCustomer = customer;
    }

    @Override
    public String doInBackground(String[] params) {

        try {
            JSONObject json = new JSONObject();
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            json.put("login_email", credential.getEmail());
            json.put("login_pin", credential.getKey());
            json.put("terminal_name", credential.getTerminalName());
            JSONObject customerJson = new JSONObject();
            customerJson.put("firstName",mCustomer.fName);
            customerJson.put("lastName", mCustomer.lName);
            customerJson.put("email", mCustomer.email);
            customerJson.put("phone", mCustomer.phone);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(customerJson);
            json.put("customer", jsonArray);
            List<RestAgent.Parameter> parameters = new ArrayList<>();
            parameters.add(new RestAgent.Parameter("addCustomer", json.toString()));
            //String urldata=UrlProvider.BASE_URL+UrlProvider.Base_inner+"users/"+credential.getUserId()+"/"+ UrlProvider.CUSTOMER_ADD_URL;
            String urldata=UrlProvider.BASE_URL+UrlProvider.BASE_INNER+"users/"+credential.getUserId()+"/"+ UrlProvider.CUSTOMER_ADD_URL;
            /*URL url = new URL(UrlProvider.BASE_URL + UrlProvider.CUSTOMER_ADD_URL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");

            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(Utils.joinParameters(parameters, false));
            writer.flush();
            writer.close();
            os.close();

            c.connect();
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            StringBuilder data = new StringBuilder();
            while ((len1 = is.read(buffer)) != -1) {
                data.append(new String(buffer, 0, len1));
            }

            String jsonResult = data.toString();

            Log.v("result", "" + jsonResult);
            return jsonResult;*/
            Log.e("Coustomer","Coustomer send>>"+urldata);
            URL url = new URL(urldata);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty ("access-key", UrlProvider.Access_key);
           // conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
           // Log.e("Result RestAgent","method>>>>"+mMethod);
            conn.setRequestMethod("POST");

            // if (mParams != null && (POST.equals(mMethod) || PUT.equals(mMethod))) {
            conn.setDoOutput(true);
            //OutputStream os = conn.getOutputStream();
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            Log.e("Result RestAgent","data>>>>"+customerJson.toString());
            //writer.write(send_data.toString());(Utils.joinParameters(mParams, false));
            os.write(customerJson.toString().getBytes("UTF8"));
            writer.flush();
            writer.close();
            os.close();
            // }
            conn.connect();
            int responseCode = conn.getResponseCode();

            String result = null;
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                if (responseCode >= 400 && responseCode <= 499) {
                    Log.e("SendCustomerAsyncTask", "HTTPx Response: " + responseCode + " - " + conn.getResponseMessage());
                    is = new BufferedInputStream(conn.getErrorStream());
                }
                else {
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
                //ex.printStackTrace();
                //Log.e("SendCustomerAsyncTask", "Error reading InputStream");
                result = null;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            Log.e("SendCustomerAsyncTask","result >>>>"+result);
            return result;

        }catch (MalformedURLException e) {
            Log.e("error", "MalformedURLException" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e("error", "UnsupportedEncodingException" +e.getMessage());
        } catch (ProtocolException e) {
            Log.e("error", "ProtocolException" + e.getMessage());
        } catch (IOException e) {
            Log.e("error", "IOException" + e.getMessage());
        } catch (JSONException e) {
            Log.e("error", "JSONException" + e.getMessage());
        }

        return null;
    }

    /*@Override
    protected void onPostExecute(String result) {

        if(result !=null && result.contains("customerId"))
        {
            Toast.makeText(mContext, mContext.getString(R.string.msg_customer_add_success), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(mContext, mContext.getString(R.string.msg_customer_add_failure), Toast.LENGTH_LONG).show();
    }*/




}
