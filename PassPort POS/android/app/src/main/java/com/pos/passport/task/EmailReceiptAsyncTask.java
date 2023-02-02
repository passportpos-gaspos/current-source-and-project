package com.pos.passport.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Customer;
import com.pos.passport.model.EmailReceipt;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 2/12/2016.
 */
public class EmailReceiptAsyncTask extends AsyncTask<String, String, String>{

    private Context mContext;
    private Cart mCart;
    private Customer mRecipient;
    private AsyncTaskListener mCallback;

    public EmailReceiptAsyncTask(Context context, Cart cart, Customer recipient){
        this.mContext = context;
        this.mCart = cart;
        this.mRecipient = recipient;
    }

    public void setListener(AsyncTaskListener callback){
        this.mCallback = callback;
    }


    @Override
    protected String doInBackground(String... params) {
        try{
            EmailReceipt receipt = new EmailReceipt(mCart, mRecipient);
            receipt.terminal_name = PrefUtils.getLoginCredential(mContext).getTerminalName();
            Gson gson = new Gson();
            String json = gson.toJson(receipt);

            List<RestAgent.Parameter> parameters = new ArrayList<>();
            parameters.add(new RestAgent.Parameter("emailReceipt", json));
            URL url = new URL(UrlProvider.BASE_URL + UrlProvider.CUSTOMER_EMAIL_RECEIPT);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");

            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(Utils.joinParameters(parameters, false));
            writer.flush();
            writer.close();
            os.close();

            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            StringBuilder data = new StringBuilder();
            while ((len1 = is.read(buffer)) != -1) {
                data.append(new String(buffer, 0, len1));
            }

            return data.toString();

        }catch (Exception e){
            Log.d("debug", "error sending receipt to back office");
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null && result.contains("success")){
            mCallback.onSuccess();
            return;
        }

        mCallback.onFailure();
    }

}
