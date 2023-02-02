package com.pos.passport.util;

import android.content.Context;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.AsyncTaskListenerData;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.task.SendHardwareAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kareem on 5/17/2016.
 */
public class EloTouchHelper {

    private ProductDatabase mDb;
    private Context mContext;

    public EloTouchHelper(Context context) {
        mDb = ProductDatabase.getInstance(context);
        mContext = context;
    }

    public void setUpPrinter()
    {

        JSONObject json = new JSONObject();
        try {
            json.put("printer", ReceiptSetting.MAKE_ELOTOUCH);
            json.put("type", ReceiptSetting.TYPE_USB);
            json.put("size", ReceiptSetting.SIZE_2);
            json.put("address", "0.0.0.0");
            json.put("cashDrawer", true);
            json.put("openOrderPrinter", false);
            json.put("main", true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        syncPrinter(json);
        //mDb.insertReceiptSettings(json.toString(), 0);
    }

    public void syncPrinter(JSONObject insertData) {
        JSONObject jsonsend = new JSONObject();
        try {
            jsonsend.put("printer", GetPrinterType(ReceiptSetting.MAKE_ELOTOUCH));
            jsonsend.put("printerType", "USB: (OS 3.0+)");
            jsonsend.put("printerSize", "2\\\" Printer");
            jsonsend.put("ipAddress", "0.0.0.0");
            jsonsend.put("isKickCashDrawer", "YES");
            jsonsend.put("isPrinterForOpenOrder", "NO");
            jsonsend.put("isMainPrinter", "YES");
            AddHardwareSync(insertData,jsonsend,RestAgent.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String GetPrinterType(int typeValue) {
        String mPrinterType = "";
        if (typeValue == ReceiptSetting.MAKE_CUSTOM)
            mPrinterType = mContext.getResources().getString(R.string.txt_custom_america_t_ten);
        if (typeValue == ReceiptSetting.MAKE_ESCPOS)
            mPrinterType = mContext.getResources().getString(R.string.txt_generic_esc_pos);
        if (typeValue == ReceiptSetting.MAKE_PT6210)
            mPrinterType = mContext.getResources().getString(R.string.txt_partner_tech_pt6210);
        if (typeValue == ReceiptSetting.MAKE_SNBC)
            mPrinterType = mContext.getResources().getString(R.string.txt_snbc);
        if (typeValue == ReceiptSetting.MAKE_STAR)
            mPrinterType = mContext.getResources().getString(R.string.txt_tsp100_lan);
        if (typeValue == ReceiptSetting.MAKE_ELOTOUCH)
            mPrinterType = mContext.getResources().getString(R.string.txt_elo_touch);

        return mPrinterType;

    }
    private void AddHardwareSync(final JSONObject insertdata, JSONObject data, String method) {
        SendHardwareAsyncTask asyncTask = new SendHardwareAsyncTask(mContext, true, Consts.ADDPRINTER_TEXT, data, method);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1) {
                try {
                    //Log.e(" printer", "Result >>>>" + data1);
                    if (data1 != null)
                    {
                            JSONObject data = new JSONObject(data1);
                            int printerId = data.getInt("printerId");
                            insertdata.put("serverId", printerId);
                            int rowId = mDb.insertReceiptSettings(insertdata.toString(), 0);
                            mDb.UpdatePrintSettings(printerId, rowId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
            }
        });
        asyncTask.execute("", "", "");
    }

    public static String getDisplayCharacters(String message){
        if (message.length() == 16) {
            return message;
        } else if (message.length() > 16) {
            return message.substring(message.length() - 16);
        } else {
            return message;
        }
    }
}
