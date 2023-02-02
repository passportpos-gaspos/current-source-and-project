package com.pos.passport.gateway;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.RawRes;
import android.support.annotation.StringDef;
import android.util.Log;

import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by karim on 11/19/15.
 */
public abstract class BaseGateway {
    private final String DEBUG_TAG = "[BaseGateway]";

    @StringDef({ METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_HEAD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpMethod {}

    @StringDef({ APPLICATION_JSON, APPLICATION_XML })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplicationType {}

    @StringDef({ TRANSACTION_SALE, TRANSACTION_REFUND, TRANSACTION_VOID, TRANSACTION_RETURN })
    @Retention(RetentionPolicy.SOURCE)
    public @interface  TransactionType {}

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_PUT = "PUT";

    public static final String TRANSACTION_SALE = "SALE";
    public static final String TRANSACTION_REFUND = "REFUND";
    public static final String TRANSACTION_VOID = "VOID";
    public static final String TRANSACTION_RETURN="RETURN";

    protected Context mContext;
    protected URLConnection mUrlConnection;

    protected JSONObject mConfigJSONObject;
    protected boolean mSandBoxEnabled;
    protected String mName;
    protected String mBaseUrl;
    protected String mSandboxBaseUrl;
    protected String mApiKey;
    protected String mSandBoxApiKey;
    protected String mApplicationType;
    protected int mConnectionTimeout;
    protected int mReadTimeout;
    protected String mContentType;

    public BaseGateway(Context context, @RawRes int resId) throws JSONException {
        mContext = context;
        String configs = Utils.getJsonStringFromResource(context, resId);
        mConfigJSONObject = new JSONObject(configs);
        parseConfigs();
    }

    public String getName() {
        return mName;
    }

    public String getBaseUrl() {
        if (mSandBoxEnabled)
            return mSandboxBaseUrl;
        else
            return mBaseUrl;
    }

    public String getApiKey() {
        if (mSandBoxEnabled)
            return mSandBoxApiKey;
        else
            return mApiKey;
    }

    public boolean isSandBoxEnabled() {
        return mSandBoxEnabled;
    }

    public void setSandBoxEnabled(boolean enabled) {
        this.mSandBoxEnabled = enabled;
    }

    protected void setRequestParameters(@HttpMethod String method) throws ProtocolException {
        if (mUrlConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection)mUrlConnection).setRequestMethod(method);
            mUrlConnection.setRequestProperty("Content-Type", mApplicationType);
            mUrlConnection.setRequestProperty("Accept", mApplicationType);
        } else if (mUrlConnection instanceof HttpURLConnection) {
            ((HttpURLConnection)mUrlConnection).setRequestMethod(method);
            mUrlConnection.setRequestProperty("Content-Type", mApplicationType);
            mUrlConnection.setRequestProperty("Accept", mApplicationType);
        }
    }

    protected void sendRequest(final String requestBody, final OutputStream outputStream) throws IOException{
        outputStream.write(requestBody.getBytes());
        outputStream.flush();
    }

    protected String getResponse(final InputStream inputStream) throws IOException {
        final StringBuilder response = new StringBuilder();
        BufferedReader bufferedReader;

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String output;
        while ((output = bufferedReader.readLine()) != null) {
            response.append(output);
        }

        return response.toString();
    }

    protected void parseConfigs() throws JSONException {
        mName = mConfigJSONObject.optString("name");
        mBaseUrl = mConfigJSONObject.optString("baseUrl");
        mSandboxBaseUrl = mConfigJSONObject.optString("sandBoxBaseUrl");
        mApiKey = mConfigJSONObject.optString("apiKey");
        mSandBoxApiKey = mConfigJSONObject.optString("sandBoxApiKey");
        mApplicationType = mConfigJSONObject.optString("applicationType");
        mContentType = mConfigJSONObject.optString("contentType");
        mConnectionTimeout = mConfigJSONObject.optInt("connectionTimeout");
        mReadTimeout = mConfigJSONObject.optInt("readTimeout");
        Log.v(DEBUG_TAG, "parseConfigs in BaseGateway");
    };

    public abstract void processSale(String id, String payload, TransactionListener l);
    protected abstract void processResult(@TransactionType String transactionType, String id, String response, TransactionListener l);
    public abstract void processRefund(String id, String payload, TransactionListener l);
    public abstract void processVoid(String id, String payload, TransactionListener l);
    protected abstract String validate();

    protected class RequestAsyncTask extends AsyncTask<Void, Void, String> {
        private String transactionType;
        private String id;
        private String payload;
        private TransactionListener listener;


        public RequestAsyncTask(@TransactionType String transactionType, String id, String payload, TransactionListener l) {
            this.transactionType = transactionType;
            this.id = id;
            this.payload = payload;
            this.listener = l;
        }
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (!Utils.isConnected(mContext))
                    return null;

                if (mUrlConnection instanceof HttpsURLConnection) {
                    sendRequest(payload, mUrlConnection.getOutputStream());
                    String response = "";
                    if (((HttpsURLConnection)mUrlConnection).getResponseCode() < 300)
                        response = getResponse(mUrlConnection.getInputStream());
                    else
                        response = getResponse(((HttpsURLConnection)mUrlConnection).getErrorStream());
                    return response;
                } else if (mUrlConnection instanceof HttpURLConnection){
                    sendRequest(payload, mUrlConnection.getOutputStream());
                    String response = "";
                    if (((HttpURLConnection)mUrlConnection).getResponseCode() < 300)
                        response = getResponse(mUrlConnection.getInputStream());
                    else
                        response = getResponse(((HttpURLConnection)mUrlConnection).getErrorStream());
                    return response;
                }
                return null;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            processResult(transactionType, id, response, listener);
        }
    }
}
