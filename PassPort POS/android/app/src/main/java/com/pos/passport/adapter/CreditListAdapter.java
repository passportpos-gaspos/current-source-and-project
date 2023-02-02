package com.pos.passport.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Kareem on 10/15/2016.
 */

public class CreditListAdapter extends CursorAdapter {

    private Context mContext;

    public CreditListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_row_credit, parent, false);
        Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        try{
            if(cursor != null) {
                ((TextView) view.findViewById(R.id.invoice_text_view)).setText(cursor.getString(cursor.getColumnIndex("invoice")));
                ((TextView) view.findViewById(R.id.date_text_view)).setText(convertDateFormat(cursor.getLong(cursor.getColumnIndex("date"))));
                ((TextView) view.findViewById(R.id.amount_text_view)).setText(DecimalFormat.getCurrencyInstance().format(new BigDecimal(cursor.getString(cursor.getColumnIndex("amount")))));
                ((TextView) view.findViewById(R.id.card_type_text_view)).setText(cursor.getString(cursor.getColumnIndex("cardType")));
                ((TextView) view.findViewById(R.id.tip_amount_text_view)).setText(DecimalFormat.getCurrencyInstance().format(new BigDecimal(cursor.getString(cursor.getColumnIndex("tipAmount")))));
            }
        }catch (Exception e){
            Log.e("Error", e.getMessage());
        }
    }

    private String convertDateFormat(long time){
        try {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(time);
            String date = DateFormat.format("MM/dd/yy HH:mm:ss", cal).toString();
            return date;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
