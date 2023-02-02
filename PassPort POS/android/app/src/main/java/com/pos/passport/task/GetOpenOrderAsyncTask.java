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
import com.pos.passport.util.Consts;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

/**
 * Created by Kareem on 3/25/2016.
 */
public class GetOpenOrderAsyncTask extends AsyncTask<String, String, String> {

    private Context mContext;
   // private String token;
    //private String mPin;
    //private String mTerminalName;
    private boolean mShowMessage = true;
    private String mUrl = "";
    private AsyncTaskListenerData mCallback;
    LoginCredential credential;
    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    public GetOpenOrderAsyncTask(Context context, boolean showMessage,String mFFOrdertype,int pagecounter)
    {
        this.mContext = context;
        this.mShowMessage = showMessage;
        credential = PrefUtils.getLoginCredential(mContext);

        if(mFFOrdertype.equalsIgnoreCase(Consts.OPEN_ORDER_TEXT))
            mUrl=UrlProvider.BASE_INNER+"users/"+credential.getUserId()+"/"+UrlProvider.OPEN_ORDER+"/"+"page/"+pagecounter;
        else
            mUrl=UrlProvider.BASE_INNER+"users/"+credential.getUserId()+"/"+UrlProvider.ARCHIVE_ORDER+"/"+"page/"+pagecounter;

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
            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            try {
                PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                version = pinfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            //LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();
            json.put("deviceId", credential.getTerminalId());
            //String url=UrlProvider.BASE_INNER+"users/"+credential.getUserId()+"/"+UrlProvider.OPEN_ORDER;
           // Log.d("Url","Open order >>"+mUrl);
            String mpsResponse = Utils.GetDataOrder(mUrl, "order", json);
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
