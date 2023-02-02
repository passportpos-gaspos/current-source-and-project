package com.pos.passport.receiver;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.activity.SettingsActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Device;
import com.pos.passport.model.ReceiptSetting;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Kareem on 11/2/2016.
 */

public class PushReceiver extends BroadcastReceiver {
    private Context context;
    private static final String TAG = "PushReceiver";
    private ProductDatabase mDb;
    private static final int MAINACTIVITY_VIEW = 0;
    private static final int SETTINGACTIVITY_VIEW = 1;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        Bundle dd = intent.getExtras();
        mDb = ProductDatabase.getInstance(context);
        try {
            if (intent.getStringExtra("sync") != null && Boolean.valueOf(intent.getStringExtra("sync"))) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.SyncBroadcastReceiver.PROCESS_SYNC_RESPONSE);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                context.sendBroadcast(broadcastIntent);
            } else if (intent.getStringExtra("type").toString().equalsIgnoreCase("printer")) {
                int updateView = 0;
                if (intent.getStringExtra("action").toString().equalsIgnoreCase("ADD") || intent.getStringExtra("action").toString().equalsIgnoreCase("EDIT")) {
                    setPrintData(new JSONObject(intent.getStringExtra("data")));
                    updateView = 1;
                } else if (intent.getStringExtra("action").toString().equalsIgnoreCase("DELETE")) {
                    JSONObject getData = new JSONObject(intent.getStringExtra("data"));
                    deletePrinter(Integer.parseInt(getData.getString("printerId")));
                    updateView = 1;
                }
                if (updateView == 1) {
                    if (CallClassMethod(SETTINGACTIVITY_VIEW) == 0) {
                        sendNotification("Cumulus", "Hardware settings Updated.Open Settings to configure hardware setting.");
                    } else {
                        setCallbacks(SETTINGACTIVITY_VIEW);
                    }
                }
            } else if (intent.getStringExtra("type").toString().equalsIgnoreCase("cardReader")) {
                int updateView = 0;
                if (intent.getStringExtra("action").toString().equalsIgnoreCase("ADD") || intent.getStringExtra("action").toString().equalsIgnoreCase("EDIT")) {
                    setCardReaderData(new JSONObject(intent.getStringExtra("data")));
                    updateView = 1;

                } else if (intent.getStringExtra("action").toString().equalsIgnoreCase("DELETE")) {
                    JSONObject getData = new JSONObject(intent.getStringExtra("data"));
                    deleteCardDevice(Integer.parseInt(getData.getString("cardReaderId")));
                    updateView = 1;
                }
                if (updateView == 1) {
                    if (CallClassMethod(SETTINGACTIVITY_VIEW) == 0) {
                        sendNotification("Cumulus", "Hardware settings Updated.Open Settings to configure hardware setting.");
                    } else {
                        setCallbacks(SETTINGACTIVITY_VIEW);
                    }
                }
            } else {
                if (CallClassMethod(MAINACTIVITY_VIEW) == 0) {
                    //JSONObject dataget = new JSONObject(intent.getStringExtra("message"));
                    // Log.e("Data get","Noti data>>>"+dataget);
                    sendNotification(intent.getStringExtra("title"), intent.getStringExtra("message"));
                } else {
                    setCallbacks(MAINACTIVITY_VIEW);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.top_icon)
                .setContentTitle(title)//messageBody.optString("title"))
                .setContentText(message)//messageBody.optString("message"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public void setCallbacks(int checkActivity) {
        if (checkActivity == MAINACTIVITY_VIEW) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.OrderUpdate.PROCESS_RESPONSE_1);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("update", "0");
            context.sendBroadcast(broadcastIntent);
        }
        if (checkActivity == SETTINGACTIVITY_VIEW) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(SettingsActivity.HardwareUpdate.PROCESS_RESPONSE_SETTING);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("update", "0");
            context.sendBroadcast(broadcastIntent);
        }
    }

    public int CallClassMethod(int checkActivity) {
        //Log.e("Call method","Callmethod");
        int show_ = 0;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < services.size(); i++) {
            if (checkActivity == MAINACTIVITY_VIEW) {
                if (services.get(i).topActivity.toString().equalsIgnoreCase("ComponentInfo{com.pos.cumulus/com.pos.cumulus.activity.MainActivity}")) {
                    show_ = 1;
                }
            }
            if (checkActivity == SETTINGACTIVITY_VIEW) {
                if (services.get(i).topActivity.toString().equalsIgnoreCase("ComponentInfo{com.pos.cumulus/com.pos.cumulus.activity.SettingsActivity}")) {
                    show_ = 1;
                }
            }

        }
        return show_;
    }

    public void setPrintData(JSONObject getObjects) {
        try {
            JSONObject tempcreate = new JSONObject();
            int serverid = Integer.parseInt(getObjects.optString("printerId"));
            tempcreate.put("serverId", serverid);
            tempcreate.put("printer", GetPrinterType(getObjects.optString("printer")));
            tempcreate.put("type", getObjects.optString("printerType"));
            tempcreate.put("size", getObjects.optString("printerSize"));
            tempcreate.put("address", getObjects.optString("ipAddress"));
            if (getObjects.optString("isKickCashDrawer").equalsIgnoreCase("YES"))
                tempcreate.put("cashDrawer", true);
            else
                tempcreate.put("cashDrawer", false);

            if (getObjects.optString("isPrinterForOpenOrder").equalsIgnoreCase("YES"))
                tempcreate.put("openOrderPrinter", true);
            else
                tempcreate.put("openOrderPrinter", false);

            if (getObjects.optString("isMainPrinter").equalsIgnoreCase("YES"))
                tempcreate.put("main", true);
            else
                tempcreate.put("main", false);
            mDb.insertPrinterSync(tempcreate.toString(), serverid);
        } catch (Exception e) {
            Log.e("PushReceiver", "setPrintData >>" + e.getMessage());
        }
    }

    public void deletePrinter(int printerId) {
        mDb.removePrinter(printerId);
    }

    public void deleteCardDevice(int deviceId) {
        mDb.removeHardwareDevice(deviceId);
    }

    public void setCardReaderData(JSONObject getJsonObjects) {
        Device device = new Device();
        device.setDeviceName(getJsonObjects.optString("name"));
        device.setDeviceType(getJsonObjects.optString("cardReader"));
        device.setIpaddress(getJsonObjects.optString("ipAddress"));
        device.setPort(getJsonObjects.optString("port"));
        int dId = Integer.parseInt(getJsonObjects.optString("cardReaderId"));
        device.setId(dId);
        mDb.saveHardwareDeviceSync(device, dId);
    }

    public int GetPrinterType(String typeValue) {
        int mPrinterType = 0;
        if (context.getResources().getString(R.string.txt_custom_america_t_ten).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_CUSTOM;
        if (context.getResources().getString(R.string.txt_generic_esc_pos).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_ESCPOS;
        if (context.getResources().getString(R.string.txt_partner_tech_pt6210).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_PT6210;
        if (context.getResources().getString(R.string.txt_snbc).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_SNBC;
        if (context.getResources().getString(R.string.txt_tsp100_lan).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_STAR;
        if (context.getResources().getString(R.string.txt_elo_touch).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_ELOTOUCH;

        return mPrinterType;
    }
}
