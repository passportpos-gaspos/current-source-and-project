package com.pos.passport.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pos.passport.BuildConfig;
import com.pos.passport.R;
import com.pos.passport.activity.BaseActivity;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.gateway.BaseGateway;
import com.pos.passport.gateway.BridgePayGateway;
import com.pos.passport.gateway.ClearentGateway;
import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.model.Payment;
import com.pos.passport.model.ProcessCreditCard;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * Created by karim on 1/25/16.
 */
public class ForwardService extends Service {
    public final String DEBUG_TAG = "ForwardService";
    private ProductDatabase mDb;
    private int mTotalOfflineSales = 0;
    private int mTotalCreditSales = 0;
    private int mTotalCashSales = 0;
    private int mTotalApprovedSales = 0;
    private int mTotalCreditApproved = 0;
    private int mTotalCashApproved = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mDb = ProductDatabase.getInstance(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ReportCart> carts = mDb.getUnsentSales();
                mTotalOfflineSales = carts.size();
                try {
                    Log.v(DEBUG_TAG, "before for loop. cart size = " + carts.size());
                    for (int i = 0; i < carts.size(); i++) {
                        Log.v(DEBUG_TAG, "before process: " + i);
                        /*for(Payment payment : carts.get(i).mPayments) {
                            if(payment.paymentType.equalsIgnoreCase(PayActivity.PAYMENT_TYPE_CREDIT))
                                mTotalCreditSales++;
                                processTransaction(payment, carts.get(i));
                        }*/
                        new SendSale().execute(carts.get(i));
                        Log.v(DEBUG_TAG, "after process: " + i);
                    }
                    mTotalCashSales = mTotalOfflineSales - mTotalCreditSales;
                    Log.v(DEBUG_TAG, "after for loop");
                    displayResults();
                    stopSelf();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        return Service.START_NOT_STICKY;
    }

    private void processTransaction(Payment payment, ReportCart cart) {
        BaseGateway gateway = null;
        String payload = null;
        try {
            if (BuildConfig.GATEWAY.equals("bridgepay")) {
                gateway = new BridgePayGateway(this, R.raw.gateway_bridgepay);
                ProcessCreditCard  cProcess = new ProcessCreditCard();
                gateway.setSandBoxEnabled(true);
                cProcess.setAmount("mTotal");
                //cProcess.setExtData("mStringCardDataBuffer");
                cProcess.setUserName(WebSetting.merchantID);
                cProcess.setTransType(BaseGateway.TRANSACTION_SALE);
                cProcess.setPassword(WebSetting.webServicePassword);
                cProcess.setMerchantID(WebSetting.hostedMID);
                payload = cProcess.generateXmlString();
            } else if (BuildConfig.GATEWAY.equals("clearent")) {
                Log.v(DEBUG_TAG, "clearent");
                gateway = new ClearentGateway(this, R.raw.gateway_clearent);
                gateway.setSandBoxEnabled(true);
                payload = payment.payload;
                Log.v(DEBUG_TAG, "payload: " + payload);
            }
            if (gateway == null)
                return;

            gateway.processSale(cart.id, payload, new TransactionListener() {
                @Override
                public void onOffline() {
                    // do nothing
                    Utils.alertBox(getApplicationContext(), R.string.txt_no_network, R.string.msg_no_network_sale_will_be_uploaded_when_available);
                    Log.v(DEBUG_TAG, "onOffline");
                }

                @Override
                public void onApproved(String id, String gatewayId, String response) {
                    Log.v(DEBUG_TAG, "onApproved() id: " + id + ", gatewayId: " + gatewayId + ", response: " + response);
                    try {
                        ReportCart cart = mDb.getTransactionById(id);
                        JSONObject lineItemsJSONObject = new JSONObject(cart.cartItems);
                        JSONArray paymentsJSONArray = lineItemsJSONObject.getJSONArray("Payments");
                        if (paymentsJSONArray != null) {
                            JSONObject paymentJSONObject = paymentsJSONArray.getJSONObject(0);
                            paymentJSONObject.put("gatewayId", gatewayId);
                            paymentJSONObject.put("response", response);
                        }
                        mDb.updateSaleStatus(id, ReportCart.PROCESS_STATUS_APPROVED, lineItemsJSONObject.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    mTotalCreditApproved++;
                    Intent i = new Intent(BaseActivity.ACTION_FORWARD);
                    sendBroadcast(i);
                }

                @Override
                public void onError(String message) {
                    Log.v(DEBUG_TAG, "onError: " + message);
                }

                @Override
                public void onDeclined(String message) {
                    Utils.alertBox(getApplicationContext(),R.string.txt_declined, message);
                    Log.v(DEBUG_TAG, "onDeclined: " + message);
                }
            });
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private class SendSale extends AsyncTask<ReportCart, String, String> {

        @Override
        protected String doInBackground(ReportCart... params) {
            try {
                String UID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String version = "";
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                JSONObject json = new JSONObject();
                LoginCredential credential = PrefUtils.getLoginCredential(getBaseContext());
                json.put("loginEmail", credential.getEmail());
                json.put("loginPin", credential.getKey());
                json.put("terminalName", credential.getTerminalName());
                json.put("userId", credential.getUserId());
                json.put("deviceId", credential.getTerminalId());
                json.put("UID", UID);
                json.put("make", Build.BRAND);
                json.put("model", Build.MODEL);
                json.put("version", version);
                json.put("android", Build.VERSION.RELEASE);
                json.put("sync", false);
                ReportCart cart = params[0];
                json.put("sale_id", cart.id);
                json.put("trans_id", cart.trans);
                Calendar c = Calendar.getInstance();
                long z = (long) c.getTimeZone().getOffset(cart.mDate);
                json.put("saleDate", (cart.mDate + z) / 1000);
                json.put("subtotal", cart.mSubtotal);
                int cashiid = mDb.getCashiersAdminId();
                json.put("cashier_id", cashiid);// cart.getCashierId());
                json.put("total", Utils.formatCartTotal(cart.mTotal));
                json.put("tax1", cart.mTax1);
                json.put("tax2", cart.mTax2);
                json.put("tax3", cart.mTax3);
                json.put("taxpercent1", cart.mTax1Percent);
                json.put("taxpercent2", cart.mTax2Percent);
                json.put("taxpercent3", cart.mTax3Percent);
                json.put("taxname1", cart.mTax1Name);
                json.put("taxname2", cart.mTax2Name);
                json.put("taxname3", cart.mTax3Name);
                if (cart.mVoided) json.put("voided", 1);
                else json.put("voided", 0);
                if (cart.isReceive) json.put("isReceive", 1);
                else json.put("isReceive", 0);
                json.put("status", cart.mStatus);
                json.put("total_discount_name", cart.mDiscountName);
                json.put("total_discount_amount", cart.mDiscountAmount.toString());
                Log.e("Return reson", ">>>>" + cart.mReturnReason);
                json.put("return_reason", cart.mReturnReason);
                JSONObject cartitemdata = new JSONObject(cart.cartItems);
                if (cartitemdata.has("Payments")) {
                    JSONArray payment = cartitemdata.getJSONArray("Payments");
                    if (payment.length() > 0) {
                        if (cartitemdata.getJSONArray("Payments").length() > 1) {
                            json.put("tenderType", "SPLIT");
                        } else {
                            String typeshow = "CASH";
                            String typeget = cartitemdata.getJSONArray("Payments").getJSONObject(0).optString("paymentType");
                            if (typeget.equalsIgnoreCase(PayActivity.PAYMENT_TYPE_CREDIT)) {
                                typeshow = "CARD";
                            } else if (typeget.equalsIgnoreCase(PayActivity.PAYMENT_TYPE_OTHER)) {
                                typeshow = "OTHER";
                            } else {
                                typeshow = typeget.toUpperCase();
                            }
                            json.put("tenderType", typeshow);
                        }
                    } else {
                        json.put("tenderType", "OTHER");
                    }
                } else {
                    json.put("tenderType", "OTHER");
                }
                json.put("tax", cartitemdata.optJSONArray("tax"));
                json.put("customer", cartitemdata.optJSONObject("customer"));
                JSONObject botharray = new JSONObject();
                botharray.put("payments", cartitemdata.getJSONArray("Payments"));
                botharray.put("products", cartitemdata.getJSONArray("Products"));
                json.put("saledata", botharray);
                Log.d("Sale send data", "Sale send>>>>" + json.toString());
                JSONObject response = Utils.postData(UrlProvider.getBase_inner() + "" + UrlProvider.SUBMIT_SALE_URL, "submitSale", json.toString());// Utils.convert_2_unicode(json.toString()));
                String result = "null";
                Log.d("Result get", "Result>>>>" + response);
                if (response != null) {
                    if (response.has("transaction_id")) {
                        cart.processed = ReportCart.PROCESS_STATUS_APPROVED;
                        cart.cOrder = 0;
                        mDb.replaceSale(cart);
                        result = "success";
                    }
                }
                return result;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "FAILED";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result!=null && result.contains("success")) {
                mTotalCashApproved++;
                Intent i = new Intent(BaseActivity.ACTION_FORWARD);
                sendBroadcast(i);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    private void displayResults(){
        mTotalApprovedSales = mTotalCreditApproved + mTotalCashApproved;

    }
}
