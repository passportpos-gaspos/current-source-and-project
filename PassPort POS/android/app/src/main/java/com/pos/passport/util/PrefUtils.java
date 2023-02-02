package com.pos.passport.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.model.OfflineOption;
import com.pos.passport.model.ReceiptSetting;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by karim on 9/24/15.
 */
public class PrefUtils {
    public static boolean hasLoginInfo(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String loginEmail = pref.getString(Consts.APOS_LOGIN_EMAIL, "");
        String loginKey = pref.getString(Consts.APOS_LOGIN_KEY, "");
        String loginTerminal = pref.getString(Consts.APOS_TERMINAL, "");
        String terminalid =pref.getString(Consts.APOS_TerminalId, "");
       String userid = pref.getString(Consts.APOS_Userid, "");
        if (!loginEmail.equals("") && !loginKey.equals("") && !loginTerminal.equals(""))
            return true;
        else
            return false;
    }

    public static long getLoginLast(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getLong(Consts.APOS_LASTLOG, 0);
    }

    public static void updateLoginLast(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(Consts.APOS_LASTLOG, new Date().getTime());
        editor.apply();
    }

    public static void saveLoginInfo(Context context, String email, String key, String terminalName,String tid,String userid) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.APOS_LOGIN_EMAIL, email);
        editor.putString(Consts.APOS_LOGIN_KEY, key);
        editor.putString(Consts.APOS_TERMINAL, terminalName);
        editor.putString(Consts.APOS_TerminalId, tid);
        editor.putString(Consts.APOS_Userid, userid);
        editor.putLong(Consts.APOS_LASTLOG, new Date().getTime());
        editor.apply();
    }

    public static LoginCredential getLoginCredential(Context context) {
        LoginCredential credential = new LoginCredential();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        credential.setEmail(pref.getString(Consts.APOS_LOGIN_EMAIL, ""));
        credential.setKey(pref.getString(Consts.APOS_LOGIN_KEY, ""));
        credential.setTerminalName(pref.getString(Consts.APOS_TERMINAL, ""));
        credential.setTerminalId(pref.getString(Consts.APOS_TerminalId, ""));
        credential.setUserId(pref.getString(Consts.APOS_Userid, ""));
        return credential;
    }

    public static boolean isLicensed(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getBoolean(Consts.APOS_LICENSED, false);
    }

    public static void savePrintOption(Context context, @ReceiptSetting.PrintOption int printOption) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Consts.RECEIPT_PRINT_OPTION, printOption);
        editor.apply();
    }

    public static int getPrintOption(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getInt(Consts.RECEIPT_PRINT_OPTION, 1);
    }

    public static void saveEmailReceiptOption(Context context, boolean emailOption){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Consts.RECEIPT_EMAIL_OPTION, emailOption);
        editor.apply();
    }

    public static boolean getEmailReceiptOption(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getBoolean(Consts.RECEIPT_EMAIL_OPTION, true);
    }

    public static void setOfflineOption(Context context, OfflineOption option) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Consts.OFFLINE_SHOWING_MESSAGE, option.isShowingMessage());
        editor.putBoolean(Consts.IS_OFFLINE, option.isOffline());
        editor.putLong(Consts.OFFLINE_TIMESTAMP, option.getTimestamp());
        editor.apply();
    }

    public static OfflineOption getOfflineOption(Context context) {
        OfflineOption option = new OfflineOption();
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        option.setShowingMessage(pref.getBoolean(Consts.OFFLINE_SHOWING_MESSAGE, true));
        option.setOffline(pref.getBoolean(Consts.IS_OFFLINE, false));
        option.setTimestamp(pref.getLong(Consts.OFFLINE_TIMESTAMP, 0));
        return option;
    }

    public static void setLastConnectedTime(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(Consts.LAST_CONNECTED_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public static long getLastConnectedTime(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getLong(Consts.LAST_CONNECTED_TIME, 0);
    }

    public static void saveCashierInfo(Context context, Cashier cashier)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cashier);
        editor.putString(Consts.LOGIN_CASHIER_INFO, json);
        editor.apply();
    }

    public static Cashier getCashierInfo(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.LOGIN_CASHIER_INFO, null);
        Gson gson = new Gson();
        return gson.fromJson(json, Cashier.class);
    }

    public static void removeCashier(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.LOGIN_CASHIER_INFO, null);
        editor.apply();
    }

    public static void saveNavigationInfo(Context context, String nvalue)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.NAVIGATIONPANE_INFO, nvalue);
        editor.apply();
    }
    public static void saveCaptureSignInfo(Context context, String nvalue)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.CAPTURESIGN, nvalue);
        editor.apply();
    }
    public static void saveAutoSyncInfo(Context context, String nvalue) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.AUTOSYNC_INFO, nvalue);
        editor.apply();
    }
    public static String getAutoSyncInfo(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.AUTOSYNC_INFO, "ON");
        return json;
    }
    public static String getNavigationInfo(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.NAVIGATIONPANE_INFO, "OFF");
        return json;
    }
    public static String getCaptureSignInfo(Context context)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.CAPTURESIGN, "NO");
        return json;
    }

    public static void saveCashDrawerSyncInfo(Context context, String nvalue) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.CASH_DRAWER_OPEN_CREDIT, nvalue);
        editor.apply();
    }
    public static boolean openCashDrawer(Context context)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String openDrawer = pref.getString(Consts.CASH_DRAWER_OPEN_CREDIT, "NO");
        if(openDrawer.equalsIgnoreCase("NO"))
            return false;
        return true;
    }

    public static void saveAcceptTipsSyncInfo(Context context, String nvalue) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.ACCEPT_TIPS, nvalue);
        editor.apply();
    }
    public static String getAcceptTipsInfo(Context context)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.ACCEPT_TIPS, "NO");
        return json;
    }

    public static void saveAcceptMobileOrdersSyncInfo(Context context, String nvalue) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.ACCEPT_MOBILE_ORDERS, nvalue);
        editor.apply();
    }
    public static String getAcceptMobileOrdersInfo(Context context)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.ACCEPT_MOBILE_ORDERS, "NO");
        return json;
    }
    public static void removeall(Context context)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    public static boolean hasCashier(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        String json = pref.getString(Consts.LOGIN_CASHIER_INFO, null);
        try{
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.has("name");
        }catch (Exception e){
            return false;
        }
    }

    public static void saveReceiptHeaderType(Context context, int type) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Consts.RECEIPT_HEADER_TYPE, type);
        editor.apply();
    }

    public static int getReceiptHeaderType(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getInt(Consts.RECEIPT_HEADER_TYPE, 1);
    }

    public static void updateTransNumber(Context context, String transId){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.TRANSACTION_NUMBER, transId);
        editor.apply();
    }

    public static String getCurrentTrans(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        return pref.getString(Consts.TRANSACTION_NUMBER, String.valueOf(Consts.TRANSACTION_START_NUMBER));
    }

    public static void resetTransNumber(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Consts.MY_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Consts.TRANSACTION_NUMBER, String.valueOf(Consts.TRANSACTION_START_NUMBER));
        editor.apply();
    }

}
