package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Settings;

import com.pos.passport.interfaces.AsyncTaskListenerData;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONObject;


public class UpdateOrderStatusAsyncTask extends AsyncTask<String, String, String>
{
    private Context mContext;
    private String mOrderNumber;
    private String mStatusId;
    //private String mTerminalName;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private boolean mShowMessage = true;
    private AsyncTaskListenerData mCallback;

    public UpdateOrderStatusAsyncTask(Context context, boolean showMessage) {
        this.mContext = context;
        this.mShowMessage = showMessage;
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
            mProgressDialog.setMessage("Updating Order Status...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String[] params)
    {
        try
        {
            mOrderNumber = params[0];
            mStatusId = params[1];
            //mTerminalName = params[2];
            publishProgress("Updating Order Status");
            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            //publishProgress(mContext.getString(R.string.txt_logging_in));
            try {
                PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                version = pinfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();
            json.put("orderStatusId", mStatusId);
            //Log.e("deviceId", credential.getTerminalId());
            String url=UrlProvider.BASE_INNER+"users/"+credential.getUserId()+"/openOrder/"+mOrderNumber+"/"+UrlProvider.UPDATE_STATUS;
            //Log.d("Url","Update order >>"+url);
            String mpsResponse = Utils.GetUpdateStatus(url, "order", json);
            //Log.d("Response","Open order>>>>"+mpsResponse);
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
