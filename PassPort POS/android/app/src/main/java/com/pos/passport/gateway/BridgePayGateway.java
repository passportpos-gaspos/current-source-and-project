package com.pos.passport.gateway;

import android.content.Context;
import android.support.annotation.RawRes;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by karim on 1/19/16.
 */
public class BridgePayGateway extends BaseGateway {

    private final String DEBUG_TAG = "[BridgePayGateway]";
    private final String PNREF = "PNRef";
    private final String RESPONSE_MESSAGE = "RespMSG";
    private final String RESULT = "Result";
    private String mPNRef;
    private String mRespMSG;
    private String mCode;

    private final String SOAP_ENVELOP = "<?xml version='1.0' encoding='UTF-8'?>" +
            " <soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "+
            " xmlns:xsd='http://www.w3.org/2001/XMLSchema'"+
            " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'> <soap:Body> %s </soap:Body> </soap:Envelope>" ;
    private final String SOAP_ACTION = "http://TPISoft.com/SmartPayments/ProcessCreditCard";

    public BridgePayGateway(Context context, @RawRes int resId) throws JSONException {
        super(context, resId);
    }

    @Override
    public void processRefund(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_SALE, id, payload, l);
    }

    @Override
    public void processSale(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_SALE, id, payload, l);
    }

    @Override
    protected void processResult(@TransactionType String transactionType, String id, String response, TransactionListener l) {
        Log.v(DEBUG_TAG, "response: " + response);
        if (TextUtils.isEmpty(response)) {
            Toast.makeText(mContext, "Empty response", Toast.LENGTH_SHORT).show();
        } else {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(response)));
                mCode = Utils.getNodeValue(RESULT, document.getElementsByTagName("Result"));
                mRespMSG = Utils.getNodeValue(RESPONSE_MESSAGE, document.getElementsByTagName("RespMSG"));
                if (mCode.equals("0")) {
                    if (mRespMSG.equalsIgnoreCase("Approved"))
                        mPNRef = Utils.getNodeValue(PNREF, document.getElementsByTagName("PNRef"));
                        l.onApproved(id, mPNRef, response);
                } else if (mCode.equals("12")) {
                    if (mRespMSG.equalsIgnoreCase("Decline"))
                        l.onDeclined(mRespMSG);
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processVoid(String id, String payload, TransactionListener l) {
        processTransaction(TRANSACTION_SALE, id, payload, l);
    }

    private void processTransaction(@TransactionType String transactionType, String id, String payload, TransactionListener l) {
        try {
            Log.v(DEBUG_TAG, "URL: = " + getBaseUrl() );
            URL url = new URL(getBaseUrl());
            mUrlConnection = url.openConnection();
            if(mUrlConnection instanceof HttpsURLConnection) {
                setRequestParameters(METHOD_POST);
                mUrlConnection.setDoOutput(true);
                mUrlConnection.setRequestProperty("SOAPAction", SOAP_ACTION);
                ((HttpsURLConnection)mUrlConnection).setChunkedStreamingMode(0);
                mUrlConnection.setRequestProperty("Content-Type", mContentType);
                mUrlConnection.setConnectTimeout(mConnectionTimeout);
                mUrlConnection.setReadTimeout(mReadTimeout);
                Log.v(DEBUG_TAG, "Payload:" + String.format(SOAP_ENVELOP, payload));
                RequestAsyncTask task = new RequestAsyncTask(transactionType, id, String.format(SOAP_ENVELOP, payload), l);
                task.execute();
            }
        } catch (MalformedURLException ex) {
            Log.v(DEBUG_TAG, ex.getMessage() + "\nURL: " + getBaseUrl());
        } catch (IOException ex) {
            Log.v(DEBUG_TAG, ex.getMessage());
        }
    }

    @Override
    protected String validate() {
        return null;
    }
}
