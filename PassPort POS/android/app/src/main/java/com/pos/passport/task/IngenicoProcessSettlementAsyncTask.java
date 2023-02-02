package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.ingenico.framework.iconnecttsi.IConnectDevice;
import com.ingenico.framework.iconnecttsi.IConnectTcpDevice;
import com.ingenico.framework.iconnecttsi.RecordType;
import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.RequestType.Settlement;
import com.ingenico.framework.iconnecttsi.ResponseType;
import com.ingenico.framework.iconnecttsi.TransactionManager;
import com.ingenico.framework.iconnecttsi.TsiException;
import com.ingenico.framework.iconnecttsi.Utility;
import com.ingenico.framework.iconnecttsi.iConnectTsiTypes;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Device;
import com.pos.passport.util.Consts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kareem on 10/15/2016.
 */

public class IngenicoProcessSettlementAsyncTask extends AsyncTask<RequestType.Settlement, String, iConnectTsiTypes.TransactionStatus> implements IConnectDevice.Logger{

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private ProductDatabase mDb;
    private Device mDevice;
    private static String TAG = "ingenico_request_task";
    private SettlementListener mCallback;

    public interface SettlementListener{
        void onSuccess(String title, String message);
        void onFailure(String title, String message);
    }

    public void setListener(SettlementListener callback){
        mCallback = callback;
    }

    public IngenicoProcessSettlementAsyncTask(Context context) {
        this.mContext = context;
        mDb = ProductDatabase.getInstance(context);
    }

    private boolean printReceipt(ResponseType.Raw resp, TransactionManager transactionManager) {

        for (Map.Entry<Integer, String> tag : resp.tags().entrySet()) {
            publishProgress("Tag " + Integer.toString(tag.getKey()) + ": " + tag.getValue());
        }

        RequestType.PrintingStatus req = new RequestType.PrintingStatus(new iConnectTsiTypes.EcrPrintingStatus(iConnectTsiTypes.EcrPrintingStatus.Ok));
        try {
            transactionManager.sendRequest(req);
        } catch (TsiException e) {
            Log.d(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        Cursor cursor = mDb.getIngencioInfo();
        Log.d(TAG, "inside preExecute");
        mDevice = new Device();
        if (cursor.getCount()>0){
            mDevice.setIpaddress(cursor.getString(cursor.getColumnIndex("ipAddress")));
            mDevice.setPort(cursor.getString(cursor.getColumnIndex("port")));
        }
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading..");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected iConnectTsiTypes.TransactionStatus doInBackground(RequestType.Settlement... params) {

        IConnectDevice device = null;
        try{
            device = new IConnectTcpDevice(mDevice.getIpaddress(), mDevice.getPort());
            device.enableTsiTraces(true, this);
            Settlement req = params[0];
            TransactionManager transactionManager = new TransactionManager(device);
            publishProgress("Processing...");
            device.connect();
            transactionManager.sendRequest(req);
            publishProgress("Sending settlement request...  ");

            boolean multiTransaction = false;
            do {
                publishProgress("Waiting for a response...");
                ResponseType.Raw raw = transactionManager.receiveResponse();
                iConnectTsiTypes.TransactionStatus status = raw.getStatus();
                publishProgress("Status: " + status);
                multiTransaction = raw.isMultiTransactionFlag();
                if (status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.ReceiptInformation) {
                    multiTransaction = printReceipt(raw, transactionManager);
                } else if (status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.Approved) {
                    ResponseType.Settlement setResp = new ResponseType.Settlement().validate(raw);
                    if (setResp == null) {
                        mCallback.onFailure("ERROR", "Failed to construct settlement response from a response. Reported type: " + raw.getType());
                        break;
                    }else{
                        multiTransaction = setResp.isMultiTransactionFlag();
                        publishProgress("Settlement is successful. Date: " + setResp.getTransactionDate() + ". Time: " + setResp.getTransactionTime());
                        ArrayList<RecordType.Raw> recs = setResp.getRecords();
                        for (RecordType.Raw rec : recs) {
                            if (rec.getType().getRecordType() == iConnectTsiTypes.RecordType.TerminalTotal) {
                                RecordType.TerminalTotal terminalTotal = new RecordType.TerminalTotal().validate(rec);
                                if (terminalTotal == null) {
                                    mCallback.onFailure("ERROR", "Unknown error. Cannot construct TerminalTotal from record");
                                    break;
                                }
                                BigDecimal batchTotal = new BigDecimal(terminalTotal.getBatchTotalAmount());
                                mCallback.onSuccess("Batch Total", "Batch total amount: " + batchTotal.divide(Consts.HUNDRED).toString());
                            }else if(rec.getType().getRecordType() == iConnectTsiTypes.RecordType.HostTotal){
                                RecordType.HostTotal hostTotal = new RecordType.HostTotal().validate(rec);
                                Log.d("Settlement ", String.valueOf(hostTotal.getBatchSaleCount()));
                                Log.d("Settlement ", String.valueOf(hostTotal.getBatchSaleAmount()));
                                Log.d("Settlement ", String.valueOf(hostTotal.getBatchRefundAmount()));
                                Log.d("Settlement ", String.valueOf(hostTotal.getBatchVoidAmount()));
                            }
                            else {
                                for (Map.Entry<Integer, String> tag : rec.tags().entrySet()) {
                                    Log.d("Settlement ", Utility.TagToString(tag.getKey()) + ": " + tag.getValue());
                                    System.out.print(Utility.TagToString(tag.getKey()) + ": " + tag.getValue());
                                    publishProgress("Tag " + Utility.TagToString(tag.getKey()) + ": " + tag.getValue());
                                }
                            }
                        }
                    }
                }else {
                    switch(status.getTransactionStatus()){
                        case iConnectTsiTypes.TransactionStatus.DeclineByHostOrCard:
                            mCallback.onFailure("Declined", "Card Decline");
                            break;
                        case iConnectTsiTypes.TransactionStatus.CancelledByUser:
                            mCallback.onFailure("Declined", "Cancelled By User");
                            break;
                        case iConnectTsiTypes.TransactionStatus.TimeoutOnUserInput:
                            mCallback.onFailure("Declined", "User Input Time Out");
                            break;
                        case iConnectTsiTypes.TransactionStatus.DeclinedByMerchant:
                            mCallback.onFailure("Declined", "Transaction Declined");
                            break;
                        case iConnectTsiTypes.TransactionStatus.BatchEmpty:
                            mCallback.onFailure("Error", "Batch Empty");
                            break;
                        default:
                            mCallback.onFailure("Declined","Transaction Declined with error " + status.getTransactionStatus());
                            break;
                    }
                }
            }while(multiTransaction);


        }catch (TsiException e){
            mCallback.onFailure(mContext.getResources().getString(R.string.txt_error), e.getMessage());
            Log.d("Debug",e.getMessage());
        }
        return null;
    }

    @Override
    public void onLog(String s) {

    }

    @Override
    protected void onPostExecute(iConnectTsiTypes.TransactionStatus status) {
        super.onPostExecute(status);
        mWakeLock.release();
        if(mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mProgressDialog.setMessage(values[0]);
    }
}
