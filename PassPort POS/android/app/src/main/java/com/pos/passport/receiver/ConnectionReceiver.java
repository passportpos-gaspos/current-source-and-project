package com.pos.passport.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pos.passport.service.ForwardService;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 2/15/16.
 */
public class ConnectionReceiver extends BroadcastReceiver {
    private String DEBUG_TAG = "[ConnectionReceiver]";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(DEBUG_TAG, "onReceive");
        if (Utils.isConnected(context)) {
            Intent service = new Intent(context, ForwardService.class);
            context.startService(service);
        }
    }
}
