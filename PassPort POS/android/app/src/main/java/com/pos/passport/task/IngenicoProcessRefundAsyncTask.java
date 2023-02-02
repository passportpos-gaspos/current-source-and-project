package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.ingenico.framework.iconnecttsi.IConnectDevice;
import com.ingenico.framework.iconnecttsi.IConnectTcpDevice;
import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.ResponseType;
import com.ingenico.framework.iconnecttsi.TransactionManager;
import com.ingenico.framework.iconnecttsi.TsiException;
import com.ingenico.framework.iconnecttsi.TsiStatus;
import com.ingenico.framework.iconnecttsi.iConnectTsiTypes;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Device;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 10/21/2016.
 */

public class IngenicoProcessRefundAsyncTask extends AsyncTask<RequestType.Refund, String, iConnectTsiTypes.TransactionStatus> implements IConnectDevice.Logger  {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private ProductDatabase mDb;
    private Device mDevice;
    private static String TAG = "ingenico_request_task";
    private IngenicoProcessRefundAsyncTask.IngenicoListener mCallback;
    private ResponseType.Refund mResponse;

    public void setListener(IngenicoProcessRefundAsyncTask.IngenicoListener callback){
        this.mCallback = callback;
    }


    public interface IngenicoListener{
        void success(ResponseType.Refund response);
        void failed();
    }

    public IngenicoProcessRefundAsyncTask(Context context) {
        this.mContext = context;
        mDb = ProductDatabase.getInstance(context);
    }

    private boolean printReceipt(ResponseType.Raw resp, TransactionManager transactionManager) {

        /*publishProgress("Receipt Information");
        for (Map.Entry<Integer, String> tag : resp.tags().entrySet()) {
            publishProgress("Tag " + Integer.toString(tag.getKey()) + ": " + tag.getValue());
        }*/

        RequestType.PrintingStatus req = new RequestType.PrintingStatus(new iConnectTsiTypes.EcrPrintingStatus(iConnectTsiTypes.EcrPrintingStatus.Ok));
        try {
            Log.d("Debug", "Ingenico print: inside try");
            transactionManager.sendRequest(req);
        } catch (TsiException e) {
            Log.d(TAG, e.getMessage());
            Log.d("Debug", "Ingenico print: inside catch");
            //publishProgress("EXCEPTION : " + e.getMessage());
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
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }



    @Override
    protected iConnectTsiTypes.TransactionStatus doInBackground(RequestType.Refund... params) {
        Log.d(TAG, "inside doInBackground");
        TsiStatus ret = new TsiStatus();
        IConnectDevice device = null;
        boolean multiTransaction = false;
        ResponseType.Raw rawResponse = null;
        try{

            device = new IConnectTcpDevice(mDevice.getIpaddress(), mDevice.getPort());
            //pass "this" as an class implementing IConnectDevice.Logger
            device.enableTsiTraces(true, this);
            RequestType.Refund req = params[0];
            TransactionManager transactionManager = new TransactionManager(device);

            device.connect();
            transactionManager.sendRequest(req);
            do{
                rawResponse = transactionManager.receiveResponse();
                publishProgress("Swipe/Insert card for Processing...");
                iConnectTsiTypes.TransactionStatus status = rawResponse.getStatus();
                multiTransaction = rawResponse.isMultiTransactionFlag();
                if (rawResponse.getStatus().getTransactionStatus() == iConnectTsiTypes.TransactionStatus.ReceiptInformation) {
                    multiTransaction = printReceipt(rawResponse, transactionManager);
                }
                Log.d("Debug", "Approved 0:" + status.getTransactionStatus());
                if (status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.Approved) {
                    publishProgress("Approved");
                    Log.d("Debug", "Approved 0: inbackground if success");
                    mResponse = new ResponseType.Refund().validate(rawResponse);
                    mCallback.success(mResponse);
                    Log.d(TAG, mResponse.toString());
                }
            }while(multiTransaction);
            device.dispose();
            return rawResponse.getStatus();

        }catch (TsiException e){
            Log.d(TAG, "TsiException");
            publishProgress(e.getMessage());
            mCallback.failed();
            return null;
        }
    }

    @Override
    protected void onPostExecute(iConnectTsiTypes.TransactionStatus status) {
        super.onPostExecute(status);
        mWakeLock.release();
        try {
            switch(status.getTransactionStatus()){
                case iConnectTsiTypes.TransactionStatus.Approved:
                    Log.d("Debug", "Approved");
                    break;
                case iConnectTsiTypes.TransactionStatus.DeclineByHostOrCard:
                    Log.d("Debug", "OnPost DeclineByHostOrCard");
                    Utils.alertBox(mContext, "Declined", "Card Decline");
                    break;
                case iConnectTsiTypes.TransactionStatus.CancelledByUser:
                    Log.d("Debug", "OnPost CancelledByUser");
                    Utils.alertBox(mContext, "Cancelled", "Cancelled By User");
                    break;
                case iConnectTsiTypes.TransactionStatus.TimeoutOnUserInput:
                    Log.d("Debug", "OnPost TimeoutOnUserInput");
                    Utils.alertBox(mContext, "Timeout", "User Input Time Out");
                    break;
                case iConnectTsiTypes.TransactionStatus.DeclinedByMerchant:
                    Log.d("Debug", "OnPost DeclinedByMerchant");
                    Utils.alertBox(mContext, "Declined", "Transaction Declined");
                    break;
                default:
                    Log.d("Debug", "OnPost default");
                    Utils.alertBox(mContext, "Declined", "Transaction Declined");
                    break;

            }

        }catch (Exception e){
            Log.d("Debug", "OnPost inside catch");
            Log.e(TAG, e.getMessage());
        }finally {
            Log.d("Debug", "OnPost inside finally");
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(mProgressDialog.isShowing())
            mProgressDialog.setMessage(values[0]);
    }

    @Override
    public void onLog(String s) {

    }
}
