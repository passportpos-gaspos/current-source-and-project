package com.pos.passport.gateway;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.model.WebSetting;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Kareem on 5/7/2016.
 */
public class PriorityGateway extends BaseGateway {

    private final String DEBUG_TAG = "[PriorityGateway]";
    private String mAuthorization;

    public PriorityGateway(Context context, @RawRes int resId) throws JSONException {
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
        Log.v(DEBUG_TAG, "parseConfigs in PriorityGateway");
    }

    @Override
    public void processSale(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_SALE, id, payload, l);
    }

    @Override
    protected void processResult(@TransactionType String transactionType, String id, String response, TransactionListener l) {
        Log.v(DEBUG_TAG, "response: " + response);
        if (TextUtils.isEmpty(response)) {
            l.onOffline();
        }else {
            try{
                JSONObject resultJSONObject = new JSONObject(response);
                String status = resultJSONObject.optString("status");
                if(status.equals("Approved")){
                    String gatewayId = resultJSONObject.optString("id");
                    l.onApproved(id, gatewayId, response);
                }else if(status.equals("Decline")){
                    l.onDeclined(response);
                }else {
                    l.onError(resultJSONObject.optString("message"));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void processRefund(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_RETURN, id, payload, l);
    }

    @Override
    public void processVoid(String id, String payload, TransactionListener l) {

    }

    @Override
    protected String validate() {
        return null;
    }

    public void processTransaction(@TransactionType String transactionType, String id, String payload, TransactionListener l) {
        try{
            Log.v(DEBUG_TAG, "URL: = " + getBaseUrl());
            URL url = new URL(getBaseUrl());
            mAuthorization = String.format("%1s:%2s", WebSetting.hostedMID.trim(), WebSetting.hostedPass.trim());
            byte[] authEncBytes = Base64.encodeBase64(mAuthorization.getBytes());
            String authStringEnc = new String(authEncBytes);
            mUrlConnection = url.openConnection();
            if (mUrlConnection instanceof HttpsURLConnection) {
                setRequestParameters(METHOD_POST);
                mUrlConnection.setDoOutput(true);
                ((HttpsURLConnection)mUrlConnection).setChunkedStreamingMode(0);
                mUrlConnection.setConnectTimeout(mConnectionTimeout);
                mUrlConnection.setReadTimeout(mReadTimeout);
                mUrlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            } else if (mUrlConnection instanceof HttpURLConnection) {
                setRequestParameters(METHOD_POST);
                mUrlConnection.setDoOutput(true);
                ((HttpURLConnection)mUrlConnection).setChunkedStreamingMode(0);
                mUrlConnection.setConnectTimeout(mConnectionTimeout);
                mUrlConnection.setReadTimeout(mReadTimeout);
                mUrlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }

            Log.v(DEBUG_TAG, "url: " + url.toString());
            Log.v(DEBUG_TAG, "connection timeout: " + mConnectionTimeout);
            Log.v(DEBUG_TAG, "read timeout: " + mReadTimeout);
            Log.v(DEBUG_TAG, "payload: " + payload);
            RequestAsyncTask task = new RequestAsyncTask(transactionType, id, payload, l);
            task.execute();

        }catch (ConnectException ex) {
            Toast.makeText(mContext, "ConnectException", Toast.LENGTH_SHORT).show();
            Log.v(DEBUG_TAG, ex.getMessage() + "\nURL: " + getBaseUrl());
            l.onOffline();
        } catch (IOException ex) {
            Toast.makeText(mContext, "IOException", Toast.LENGTH_SHORT).show();
            Log.v(DEBUG_TAG, ex.getMessage());
            l.onOffline();
        }
    }
}
