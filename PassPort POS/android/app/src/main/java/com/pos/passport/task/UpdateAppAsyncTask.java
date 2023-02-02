package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.util.UrlProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by karim on 10/1/15.
 */
public class UpdateAppAsyncTask extends AsyncTask<Void,String,Void> {
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public UpdateAppAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
        mProgressDialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.txt_downloading_update), true, false);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            URL url = new URL(UrlProvider.BASE_URL + UrlProvider.DOWNLOAD_URL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();

            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            int fileLength = c.getContentLength();

            String PATH = "/mnt/sdcard/Download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "update.apk");
            if(outputFile.exists()){
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            long total = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
                total += len1;
                publishProgress(String.format(mContext.getString(R.string.txt_downloading_update_percent), (total * 100 / fileLength)));
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/Download/update.apk")), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
        mContext.startActivity(intent);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        mProgressDialog.setMessage(values[0]);
    }
}
