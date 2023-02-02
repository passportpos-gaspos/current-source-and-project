package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kareem on 3/25/2016.
 */
public class RefreshTokenAsyncTask extends AsyncTask<Void, String, String> {

    private Context mContext;
    private boolean mShowMessage = true;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private AsyncTaskListener mCallback;

    public RefreshTokenAsyncTask(Context context, boolean showMessage) {
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
    protected String doInBackground(Void... arg0) {

        try {
            //String refreshedToken = Pushy.register(mContext);
            String refreshedToken ="d3f5cd670c60757ccf21de";

            Log.e("Refreshed token", "Refreshed token: " + refreshedToken);
            publishProgress(mContext.getString(R.string.txt_logging_in));
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();
            json.put("deviceId", credential.getTerminalId());
            json.put("userId", credential.getUserId());
            json.put("token", refreshedToken);
            String mpsResponse = Utils.GetData(UrlProvider.getBase_inner()+""+UrlProvider.Token_URL, "token", json);
            Log.e("Tag",""+mpsResponse);
            return mpsResponse;

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
            Log.e("tag",e.getMessage());
            //e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        mWakeLock.release();

        if (mShowMessage)
        {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }

        if (result != null)
        {
            try {
                JSONObject data=new JSONObject(result);
                if(mCallback != null)
                    mCallback.onSuccess();
                return;
            } catch (JSONException e)
            {
                mCallback.onFailure();
                return;

            }

        }

        mCallback.onFailure();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mShowMessage) {
            mProgressDialog.setMessage(values[0]);
        }
    }
}
