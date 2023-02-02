package com.pos.passport.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.pos.passport.R;
import com.pos.passport.model.OfflineOption;
import com.pos.passport.service.ForwardService;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 1/29/16.
 */
public class BaseActivity extends AppCompatActivity {
    public static final int RESULT_FAILED = 2;
    public static final String ACTION_FORWARD = "com.pos.passport.ACTION_FORWARD";
    protected POSApplication mPOSApplication;

    private BroadcastReceiver mForwardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onForwarded();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPOSApplication = (POSApplication) this.getApplication();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_FORWARD);
        registerReceiver(mForwardReceiver, filter2);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mPOSApplication.setCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mForwardReceiver);
        clearReferences();
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onWifiConnectionChanged(boolean isConnected) {
        if (isConnected) {
            Intent intent = new Intent(this, ForwardService.class);
            startService(intent);
        } else {
            if (Utils.checkTimeSpan(PrefUtils.getLastConnectedTime(this), 24)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.txt_offline_transactions)
                        .setTitle(R.string.msg_offline_for_a_day)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, whichButton) -> startWirelessSettings()).show();
            } else if (Utils.checkTimeSpan(PrefUtils.getLastConnectedTime(this), 4)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.txt_offline_transactions)
                        .setTitle(String.format(getString(R.string.msg_no_internet_for_a_while), 4))
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, whichButton) -> startWirelessSettings()).show();
            }
        }
    }

    protected void onForwarded() {
        OfflineOption option = new OfflineOption();
        option.setShowingMessage(true);
        option.setOffline(false);
        option.setTimestamp(0);
        PrefUtils.setOfflineOption(this, option);
    }

    private void startWirelessSettings() {
        Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private void clearReferences(){
        AppCompatActivity currActivity = mPOSApplication.getCurrentActivity();
        if (this.equals(currActivity))
            mPOSApplication.setCurrentActivity(null);
    }
}
