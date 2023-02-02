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
import com.pos.passport.model.StoreSetting;
import com.pos.passport.util.Utils;

import java.text.NumberFormat;

public class CustomerAdapter extends CursorAdapter {
	
	private NumberFormat nf = NumberFormat.getInstance();

	public CustomerAdapter(Cursor cursor, Context context) {
        super(context, cursor);
        
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
						
        final int nameColumnIndex = cursor.getColumnIndexOrThrow("fname");
        final int emailColumnIndex = cursor.getColumnIndexOrThrow("email");
        final int salesColumnIndex = cursor.getColumnIndexOrThrow("numsales");
        final int returnsColumnIndex = cursor.getColumnIndexOrThrow("numreturns");
        final int totalColumnIndex = cursor.getColumnIndexOrThrow("total");

		((TextView) view.findViewById(R.id.item_name_text_view)).setText(cursor.getString(nameColumnIndex));
		((TextView) view.findViewById(R.id.item_desc_text_view)).setText(cursor.getString(emailColumnIndex));
		
		String price = StoreSetting.getCurrency()+nf.format(cursor.getFloat(totalColumnIndex)/100f);
		((TextView) view.findViewById(R.id.quantity)).setText("Sales: " + cursor.getString(salesColumnIndex));
		((TextView) view.findViewById(R.id.item_price_text_view)).setText("Total Sales: " + price);
		((TextView) view.findViewById(R.id.barcode)).setText("");
		((TextView) view.findViewById(R.id.catagory)).setText("Returns: " + cursor.getString(returnsColumnIndex));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.itemlist, parent, false);
		Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
	}
}
