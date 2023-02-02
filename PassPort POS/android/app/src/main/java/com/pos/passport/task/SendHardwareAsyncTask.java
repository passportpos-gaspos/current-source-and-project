package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.pos.passport.interfaces.AsyncTaskListenerData;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.Consts;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

/**
 * Created by Kareem on 3/25/2016.
 */
public class SendHardwareAsyncTask extends AsyncTask<String, String, String>
{
    private Context mContext;
    private boolean mShowMessage = true;
    private String mUrl = "";
    private String mSendType = "";
    private AsyncTaskListenerData mCallback;
    LoginCredential credential;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    JSONObject postdata=new JSONObject();


    public SendHardwareAsyncTask(Context context, boolean showMessage, String type, JSONObject sendata,String method)
    {
        this.mContext = context;
        this.mShowMessage = showMessage;
        credential = PrefUtils.getLoginCredential(mContext);
        postdata=sendata;
        mSendType=method;
        if(type.equalsIgnoreCase(Consts.ADDPRINTER_TEXT))
            mUrl=UrlProvider.BASE_INNER+"/"+UrlProvider.ADD_PRINTER;
        else
            mUrl=UrlProvider.BASE_INNER+"/"+UrlProvider.ADD_CARD_READER;

    }

    public void setListener(AsyncTaskListenerData callback){
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute()
    {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();

        if (mShowMessage) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Loading Data...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String[] params) {

        try
        {
            postdata.put("userId", credential.getUserId());
            postdata.put("deviceId", credential.getTerminalId());
            Log.d("postdata","Postdata >>>>"+postdata);
            String mpsResponse = Utils.SendPrinterOrCardData(mUrl, postdata, mSendType);
            return mpsResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();

        if (mShowMessage) {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
        if (result != null)
        {
            try
            {
                //JSONObject data=new JSONObject(result);
                if(mCallback != null)
                    mCallback.onSuccess(result);
                return;
            } catch (Exception e)
            {
                mCallback.onFailure();
                return;
            }
        }
        mCallback.onFailure();
    }

    @Override
    protected void onProgressUpdate(String... values) {
    }
}
