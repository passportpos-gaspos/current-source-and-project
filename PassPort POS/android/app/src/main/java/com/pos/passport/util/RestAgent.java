package com.pos.passport.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by karim on 9/25/15.
 */
public class RestAgent {
    private final static String DEBUG_TAG = "[RestAgent]";

    protected final static String UTF8 = "UTF-8";
    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";
    public int READ_TIMEOUT = 10000;
    public int CONNECTION_TIMEOUT = 15000;
    public String mRelativeUrl;
    public String mMethod;
    public List<Parameter> mParams;
    public String send_data;

    public RestAgent(String relativeUrl, String method, List<Parameter> params) {
        this.mRelativeUrl = relativeUrl;
        this.mMethod = method;
        this.mParams = params;
    }
    public RestAgent(String relativeUrl, String method, String data) {
        this.mRelativeUrl = relativeUrl;
        this.mMethod = method;
        this.send_data = data;
    }
    public String send() throws IOException {
        String strUrl = UrlProvider.BASE_URL + mRelativeUrl;
        if (GET.equals(mMethod) && mParams != null) {
            strUrl += "?" + Utils.joinParameters(mParams, true);
        }
        //Log.e("submit url","send() method >>"+strUrl);
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty ("access-key", UrlProvider.Access_key);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setRequestMethod(mMethod);

        if (mParams != null && (POST.equals(mMethod) || PUT.equals(mMethod)))
        {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
            writer.write(Utils.joinParameters(mParams, false));
            writer.flush();
            writer.close();
            os.close();
        }
        conn.connect();

        String result = null;
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine + "\n");
            }
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(DEBUG_TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        //Log.e("Result RestAgent",">>>>"+result);
        return result;
    }
    public String sendSubmit() throws IOException {
        String strUrl = UrlProvider.BASE_URL + mRelativeUrl;

        //Log.e("submit url","sendSubmit() method >>"+strUrl);
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty ("access-key", UrlProvider.Access_key);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        //Log.e("Result RestAgent","method>>>>"+mMethod);
        conn.setRequestMethod(mMethod);

        // if (mParams != null && (POST.equals(mMethod) || PUT.equals(mMethod))) {
        conn.setDoOutput(true);
        //OutputStream os = conn.getOutputStream();
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
        //Log.e("senddata RestAgent","data>>>>"+send_data);
        //writer.write(send_data.toString());//(Utils.joinParameters(mParams, false));
        //String jsonFormattedString = send_data.replaceAll("\\\\", "");
        //String temp=URLEncoder.encode(jsonFormattedString, "UTF-8");
        //Log.e("formated",""+jsonFormattedString);
        os.write(send_data.toString().getBytes("UTF-8"));
        //os.write(send_data.toString().getBytes("UTF-8"));
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
            ex.printStackTrace();
            Log.e(DEBUG_TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        //Log.e("Result RestAgent",">>>>"+result);
        return result;
    }
    public String sendNew() throws IOException
    {
        String strUrl = UrlProvider.BASE_URL + mRelativeUrl;

        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty ("access-key", UrlProvider.Access_key);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setRequestMethod(mMethod);
       // if (mParams != null && (POST.equals(mMethod) || PUT.equals(mMethod))) {
        conn.setDoOutput(true);
            //OutputStream os = conn.getOutputStream();
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
        //Log.e("Result RestAgent","data>>>>"+send_data.toString());
        //writer.write(send_data.toString());//(Utils.joinParameters(mParams, false));
            os.write(send_data.toString().getBytes("UTF-8"));
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
            ex.printStackTrace();
            Log.e(DEBUG_TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        Log.e("sendNew","Method>>>"+result);
        return result;
    }

    public String sendNewOrder() throws IOException {
        String strUrl = UrlProvider.BASE_URL + mRelativeUrl;
        //String strUrl = UrlProvider.BASE_URL_two + mRelativeUrl;
        Log.e("Url call","Call>>"+strUrl);
        // if (GET.equals(mMethod) && mParams != null) {
        //     strUrl += "?" + Utils.joinParameters(mParams, true);
        // }
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty ("access-key", UrlProvider.Access_key);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        //Log.e("Result RestAgent","method>>>>"+mMethod);
        conn.setRequestMethod(mMethod);
       /* conn.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF8));
        //Log.e("Result RestAgent","data>>>>"+send_data.toString());
        //os.write(send_data.toString().getBytes("UTF-8"));
        writer.flush();
        writer.close();
        os.close();*/
        // }
        conn.connect();
        int responseCode = conn.getResponseCode();
        String result = null;
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            //is = new BufferedInputStream(conn.getInputStream());

            if (responseCode >= 400 && responseCode <= 499)
            {
                Log.e("NewOrder", "HTTPx Response: " + responseCode + " - " + conn.getResponseMessage());
                is = new BufferedInputStream(conn.getErrorStream());
            }
            else
            {
                is = new BufferedInputStream(conn.getInputStream());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine + "\n");
            }
            result = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(DEBUG_TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
       // Log.e("Result RestAgent",">>>>"+result);
        return result;
    }

    public static class Parameter {
        private String name;
        private String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", name, value);
        }

        public String toEncodedString() throws UnsupportedEncodingException {
            return String.format("%s=%s", URLEncoder.encode(name, UTF8), URLEncoder.encode(value, UTF8));
        }
    }
}
