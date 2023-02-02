package com.pos.passport.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 9/17/15.
 */
public class DepartmentAdapter extends CursorAdapter {

    public DepartmentAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int itemColumnIndex = cursor.getColumnIndexOrThrow("name");
        final int idColumnIndex = cursor.getColumnIndexOrThrow("_id");

        ((TextView) view.findViewById(R.id.name_text_view)).setText(cursor.getString(itemColumnIndex));
        ((TextView) view.findViewById(R.id.id_text_view)).setText(cursor.getString(idColumnIndex));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.view_item_item, parent, false);
        Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
    }
}
