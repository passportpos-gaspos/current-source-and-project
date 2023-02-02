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
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 2/10/2016.
 */
public class EmailReceiptCheckAsyncTask extends AsyncTask<String, String, String> {

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private boolean mShowMessage = true;
    private AsyncTaskListener mCallback;

    public EmailReceiptCheckAsyncTask(Context context, boolean showMessage){
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
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            try {
                PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                version = pinfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();

            json.put("loginEmail", credential.getEmail());
            json.put("loginPin", credential.getKey());
            json.put("terminalName", credential.getTerminalName());
            json.put("UID", UID);
            json.put("make", Build.BRAND);
            json.put("model", Build.MODEL);
            json.put("version", version);
            json.put("android", Build.VERSION.RELEASE);

            List<RestAgent.Parameter> parameters = new ArrayList<>();
            parameters.add(new RestAgent.Parameter("emailSettings", json.toString()));
            URL url = new URL(UrlProvider.BASE_URL + UrlProvider.EMAIL_RECEIPT_SETTINGS);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");

            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(Utils.joinParameters(parameters, false));
            writer.flush();
            writer.close();
            os.close();

            int length = c.getContentLength();
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            long total = 0;
            StringBuilder data = new StringBuilder();
            while ((len1 = is.read(buffer)) != -1) {
                data.append(new String(buffer, 0, len1));
                total += len1;
                publishProgress(mContext.getString(R.string.txt_check_email_settings));
            }

            return data.toString();

        } catch (Exception e){
            Log.d("debug", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {

        mWakeLock.release();

        if (mShowMessage) {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }

        if(result != null && result.contains("success")){
            mCallback.onSuccess();
            return;
        }
        Utils.alertBox(mContext, R.string.txt_email_configuration, R.string.msg_email_settings);
        mCallback.onFailure();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mShowMessage) {
            mProgressDialog.setMessage(values[0]);
        }
    }
}
