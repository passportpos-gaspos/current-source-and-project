package com.pos.passport.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.pos.passport.R;

/**
 * Created by Kareem on 5/29/2016.
 */
public class MessageHandler extends Handler {

    public final static int ERROR = 0;
    public final static int PRINT_SUCCESS = 1;
    public final static int PRINT_FAILED = 2;
    public final static int EMAIL_SUCCESS = 3;
    public final static int EMAIL_FAILED = 4;
    private Context context;

    public MessageHandler(Context context){
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == EMAIL_SUCCESS) {
            Toast.makeText(context, context.getString(R.string.msg_email_receipt_sent_successfully), Toast.LENGTH_LONG).show();
        } else if (msg.what == EMAIL_FAILED) {
            Toast.makeText(context, context.getString(R.string.msg_email_receipt_not_sent), Toast.LENGTH_LONG).show();
        } else if (msg.what == PRINT_SUCCESS) {
            Toast.makeText(context, context.getString(R.string.msg_receipt_sent_to_printer), Toast.LENGTH_LONG).show();
        } else if (msg.what == PRINT_FAILED) {
            Toast.makeText(context, context.getString(R.string.msg_unable_to_print_receipt), Toast.LENGTH_LONG).show();
        }
    }
}
