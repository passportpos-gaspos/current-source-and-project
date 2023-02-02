package com.pos.passport.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 10/11/2016.
 */
public class HardwareAdapter extends CursorAdapter {

    private String TAG = "Hardware_Adapter";

    public HardwareAdapter(Cursor cursor, Context context){

        super(context,cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_device_list, parent, false);
        Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        try{
            Log.d(TAG,cursor.getString(cursor.getColumnIndex("type")));
            Log.d(TAG, cursor.getString(cursor.getColumnIndex("name")));
            Log.d(TAG, cursor.getString(cursor.getColumnIndex("port")));
            ((TextView) view.findViewById(R.id.type_text_view)).setText(cursor.getString(cursor.getColumnIndex("type")));
            ((TextView) view.findViewById(R.id.name_text_view)).setText(cursor.getString(cursor.getColumnIndex("name")));
            ((TextView) view.findViewById(R.id.ip_address_text_view)).setText(cursor.getString(cursor.getColumnIndex("ipAddress")));
            ((TextView) view.findViewById(R.id.port_text_view)).setText(cursor.getString(cursor.getColumnIndex("port")));
        }catch (Exception e){
            ((TextView) view.findViewById(R.id.type_text_view)).setText("");
            ((TextView) view.findViewById(R.id.name_text_view)).setText("");
            ((TextView) view.findViewById(R.id.ip_address_text_view)).setText("");
            ((TextView) view.findViewById(R.id.port_text_view)).setText("");
        }
    }
}
