package com.pos.passport.gateway;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pos.passport.interfaces.TransactionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by karim on 11/19/15.
 */
public class ClearentGateway extends BaseGateway {
    private final String DEBUG_TAG = "[ClearentGateway]";
    private final String TRANSACTION_URL = "rest/v2/transactions";

    public ClearentGateway(Context context, @RawRes int resId) throws JSONException {
        super(context, resId);
    }

    @Override
    protected void setRequestParameters(@HttpMethod String method) throws ProtocolException {
        super.setRequestParameters(method);
        mUrlConnection.setRequestProperty("api-key", getApiKey());
    }

    @Override
    protected void parseConfigs() throws JSONException {
        super.parseConfigs();
        Log.v(DEBUG_TAG, "parseConfigs in ClearentGateway");
    }

    @Override
    public void processSale(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_SALE, id, payload, l);
    }

    @Override
    public void processRefund(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_REFUND, id, payload, l);
    }

    @Override
    public void processVoid(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_VOID, id, payload, l);
    }

    private void processTransaction(@TransactionType String transactionType, String id, String payload, TransactionListener l) {
        try {
            Log.v(DEBUG_TAG, "URL: = " + getBaseUrl() + TRANSACTION_URL);
            URL url = new URL(getBaseUrl() + TRANSACTION_URL);
            mUrlConnection = url.openConnection();
            if (mUrlConnection instanceof HttpsURLConnection) {
                setRequestParameters(METHOD_POST);
                mUrlConnection.setDoOutput(true);
                ((HttpsURLConnection)mUrlConnection).setChunkedStreamingMode(0);
                mUrlConnection.setConnectTimeout(mConnectionTimeout);
                mUrlConnection.setReadTimeout(mReadTimeout);
            } else if (mUrlConnection instanceof HttpURLConnection) {
                setRequestParameters(METHOD_POST);
                mUrlConnection.setDoOutput(true);
                ((HttpURLConnection)mUrlConnection).setChunkedStreamingMode(0);
                mUrlConnection.setConnectTimeout(mConnectionTimeout);
                mUrlConnection.setReadTimeout(mReadTimeout);
            }

            Log.v(DEBUG_TAG, "url: " + url.toString());
            Log.v(DEBUG_TAG, "connection timeout: " + mConnectionTimeout);
            Log.v(DEBUG_TAG, "read timeout: " + mReadTimeout);
            Log.v(DEBUG_TAG, "payload: " + payload);
            RequestAsyncTask task = new RequestAsyncTask(transactionType, id, payload, l);
            task.execute();
        } catch (MalformedURLException ex) {
            Toast.makeText(mContext, "MalformedURLException", Toast.LENGTH_SHORT).show();
            Log.v(DEBUG_TAG, ex.getMessage() + "\nURL: " + getBaseUrl() + TRANSACTION_URL);
            l.onOffline();
        } catch (IOException ex) {
            Toast.makeText(mContext, "IOException", Toast.LENGTH_SHORT).show();
            Log.v(DEBUG_TAG, ex.getMessage());
            l.onOffline();
        }
    }

    @Override
    protected String validate() {
        return null;
    }

    @Override
    protected void processResult(String transactionType, String id, String response, TransactionListener l) {
        Log.v(DEBUG_TAG, "response: " + response);
        if (TextUtils.isEmpty(response)) {
            l.onOffline();
        } else {
            try {
                JSONObject resultJSONObject = new JSONObject(response);
                JSONObject payloadJSONObject = resultJSONObject.optJSONObject("payload");
                String code = resultJSONObject.optString("code");
                if (code.equals("200")) {
                    if (payloadJSONObject != null) {
                        JSONObject transactionJSONObject = payloadJSONObject.optJSONObject("transaction");
                        if (transactionJSONObject != null) {
                            String res = transactionJSONObject.optString("result");
                            if (res.equalsIgnoreCase("Approved") || res.equalsIgnoreCase("ADVICE_ACCEPTED")) {
                                l.onApproved(id, transactionJSONObject.optString("id"), response);
                            }
                        }
                    }
                } else if (code.equals("400")) {
                    if (payloadJSONObject != null) {
                        JSONObject errorJSONObject = payloadJSONObject.optJSONObject("error");
                        if (errorJSONObject != null) {
                            String message = errorJSONObject.optString("error-message");
                            l.onError(message);
                        }
                    }
                } else if (code.equals("402")) {
                    if (payloadJSONObject != null) {
                        JSONObject transactionJSONObject = payloadJSONObject.optJSONObject("transaction");
                        if (transactionJSONObject != null) {
                            l.onDeclined(transactionJSONObject.optString("display-message"));
                        }
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
}
