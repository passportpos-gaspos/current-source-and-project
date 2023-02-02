package com.pos.passport.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.ingenico.framework.iconnecttsi.IConnectDevice;
import com.ingenico.framework.iconnecttsi.IConnectTcpDevice;
import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.ResponseType;
import com.ingenico.framework.iconnecttsi.TransactionManager;
import com.ingenico.framework.iconnecttsi.TsiException;
import com.ingenico.framework.iconnecttsi.TsiStatus;
import com.ingenico.framework.iconnecttsi.iConnectTsiTypes;

/**
 * Created by Kareem on 10/11/2016.
 */
public class IngenicoTestConnection extends AsyncTask<RequestType.Sale, String, TsiStatus> implements IConnectDevice.Logger{

    private Context context;
    private String ipAddress;
    private String port;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private IngenicoTestListener mCallback;

    public interface IngenicoTestListener{
        void onSuccess(String title, String message);
        void onFailure(String title, String message);
    }

    public void setListener(IngenicoTestListener callback){
        mCallback = callback;
    }

    public IngenicoTestConnection(Context context, String ipAddress, String port) {
        this.context = context;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    protected void onPreExecute() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    protected TsiStatus doInBackground(RequestType.Sale... params) {

        TsiStatus ret = new TsiStatus();
        IConnectDevice device = null;
        try {
            device = new IConnectTcpDevice(ipAddress, port);
            publishProgress("Connecting...");
            RequestType.Sale req = params[0];
            device.enableTsiTraces(true, this);
            TransactionManager transactionManager = new TransactionManager(device);
            device.connect();
            publishProgress("Device Connected");
            transactionManager.sendRequest(req);
            ResponseType.Raw resp = transactionManager.receiveResponse();
            iConnectTsiTypes.TransactionStatus status = resp.getStatus();
            publishProgress("Processing...");
            String refNo = null;
            if (status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.Approved) {
                ResponseType.Sale saleResp = new ResponseType.Sale().validate(resp);
                refNo = saleResp.getReferenceNo();
            }else if (status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.CancelledByUser ||
                    status.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.TimeoutOnUserInput) {
                mCallback.onFailure("Cancelled", "Cancelled by User or Time out on User Input");
            }

            if(refNo !=null){
                device.disconnect();
                device.connect();
                publishProgress("Void Transaction...");
                RequestType.VoidRequest voidReq = new RequestType.VoidRequest();
                voidReq.setReferenceNo(refNo);
                transactionManager.sendRequest(voidReq);
                ResponseType.Raw raw = transactionManager.receiveResponse();
                iConnectTsiTypes.TransactionStatus voidStatus = raw.getStatus();
                if (voidStatus.getTransactionStatus() == iConnectTsiTypes.TransactionStatus.Approved) {
                    ResponseType.VoidResponse voidResp = new ResponseType.VoidResponse().validate(raw);
                    if (voidResp == null) {
                        publishProgress("ERROR: Cannot construct void response object from raw. Reported type: " + raw.getType());
                    }
                    publishProgress("Successfully voided");
                }
            }

        }catch (TsiException e){
            mCallback.onFailure("Error",e.getMessage());
        }finally {
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
        return null;
    }

    @Override
    public void onLog(String s) {

    }

    @Override
    protected void onProgressUpdate(String... values) {
        mProgressDialog.setMessage(values[0]);
    }
}
