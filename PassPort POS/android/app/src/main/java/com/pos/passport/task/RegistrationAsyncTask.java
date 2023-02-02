package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kareem on 3/25/2016.
 */
public class RegistrationAsyncTask extends AsyncTask<String, String, String> {

    private Context mContext;
    private String mEmail;
    private String mPin;
    private String mTerminalName;
    private boolean mShowMessage = true;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private AsyncTaskListener mCallback;

    public RegistrationAsyncTask(Context context, boolean showMessage) {
        this.mContext = context;
        this.mShowMessage = showMessage;
    }

    public void setListener(AsyncTaskListener callback){
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();

        if (mShowMessage) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String[] params) {

        try {
            mEmail = params[0];
            mPin = params[1];
            mTerminalName = params[2];

            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            publishProgress(mContext.getString(R.string.txt_logging_in));
            try {
                PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                version = pinfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            JSONObject json = new JSONObject();
            json.put("loginEmail", mEmail);
            json.put("deviceKey", mPin);
            json.put("terminalName", mTerminalName);
            json.put("UID", UID);
            json.put("make", Build.BRAND);
            json.put("model", Build.MODEL);
            json.put("appVersion", version);
            json.put("androidVersion", Build.VERSION.RELEASE);

            String mpsResponse = Utils.GetData(UrlProvider.getBase_inner()+""+UrlProvider.LOGIN_URL, "login", json);
            return mpsResponse;

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
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
            try {
                JSONObject data=new JSONObject(result);
                String transidget=data.optString("transId");
                PrefUtils.saveLoginInfo(mContext, mEmail, mPin, mTerminalName,data.optString("terminaId"),data.optString("userId"));
                Log.e("trans id",""+transidget);
                Log.e("terminal id",""+data.optString("terminaId"));
                Log.e("Userid",""+data.optString("userId"));
                int index=data.optString("terminaId").length();
                String sub1 = transidget.substring(index);
                PrefUtils.updateTransNumber(mContext, sub1);
                String tag = data.optString("tag");
                if(tag != null && !tag.isEmpty()){
                    Utils.alertBox(mContext, R.string.txt_login_failed, data.optString("error"));
                    mCallback.onFailure();
                    return;
                }

                mCallback.onSuccess();
            } catch (JSONException e) {
                mCallback.onFailure();
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mShowMessage) {
            mProgressDialog.setMessage(values[0]);
        }
    }
}
