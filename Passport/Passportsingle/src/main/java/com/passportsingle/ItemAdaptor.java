package com.passportsingle;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.passportsingle.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ItemAdaptor extends CursorAdapter {
	
	private NumberFormat nf = NumberFormat.getInstance();
	private DecimalFormat df;
	
	public ItemAdaptor(Cursor cursor, Context context) {
        super(context, cursor);
		df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(false);
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
				
		Product product = new Product();
		
		product.price = Long.valueOf(c.getString(c.getColumnIndex("price")));
		product.cost = Long.valueOf(c.getString(c.getColumnIndex("cost")));
        product.id = c.getInt(c.getColumnIndex("_id"));
        product.barcode = (c.getString(c.getColumnIndex("barcode")));
        product.name = (c.getString(c.getColumnIndex("name")));
        product.desc = (c.getString(c.getColumnIndex("desc")));
        product.onHand = (c.getInt(c.getColumnIndex("quantity")));
        product.cat = (c.getInt(c.getColumnIndex("catid")));
        product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
        product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
        product.lastReceived = (c.getInt(c.getColumnIndex("lastReceived")));
        product.lowAmount = (c.getInt(c.getColumnIndex("lowAmount")));
		product.salePrice = c.getLong(c.getColumnIndex("salePrice"));
		product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
		product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));

		((TextView) view.findViewById(R.id.prod_name)).setText(product.id + ". " +product.name);
		((TextView) view.findViewById(R.id.prod_desc)).setText(product.desc);
		
		String price = StoreSetting.getCurrency()+df.format(product.price/100f);
		((TextView) view.findViewById(R.id.quantity)).setText("Available: " + product.onHand);
		((TextView) view.findViewById(R.id.prod_pricenum)).setText(price);
		((TextView) view.findViewById(R.id.barcode)).setText(product.barcode);
		
		String cat = ProductDatabase.getCatById(product.cat);
		
		((TextView) view.findViewById(R.id.catagory)).setText(cat);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.itemlist, parent, false);
        return view;
	}

}
