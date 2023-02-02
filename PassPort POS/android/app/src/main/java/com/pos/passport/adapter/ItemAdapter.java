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
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;

public class ItemAdapter extends CursorAdapter {
	private Context mContext;
	private ProductDatabase mDb;
	
	public ItemAdapter(Cursor cursor, Context context) {
        super(context, cursor);
		this.mContext = context;
        mDb = ProductDatabase.getInstance(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {
				
		Product product = new Product();
		product.price = new BigDecimal(c.getString(c.getColumnIndex("price")));
		product.cost = new BigDecimal(c.getString(c.getColumnIndex("cost")));
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
		product.salePrice = new BigDecimal(c.getLong(c.getColumnIndex("salePrice")));
		product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
		product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));
		product.track = (c.getInt(c.getColumnIndex("track")) != 0);
		//Log.e("Name data",">>"+product.name);
		//Log.e("Desc data",">>"+product.desc);
		String name=product.name.toString().trim();
		String desc=product.desc.toString().trim();
		((TextView) view.findViewById(R.id.item_name_text_view)).setText(name);
		((TextView) view.findViewById(R.id.item_desc_text_view)).setText(product.desc);

		//((TextView) view.findViewById(R.id.quantity)).setText(String.format(mContext.getString(R.string.txt_available_placeholder), product.onHand));
		if (product.track)
			((TextView) view.findViewById(R.id.quantity)).setText(""+product.onHand);
		else
			((TextView) view.findViewById(R.id.quantity)).setText("---");

		String price = Utils.formatCurrency(product.price);
		((TextView) view.findViewById(R.id.item_price_text_view)).setText(price);

		String barcode=product.barcode;
		//Log.e("Barcode","barcode value>>>"+barcode);
		if(barcode.length()>0)
		((TextView) view.findViewById(R.id.barcode)).setText(product.barcode);
		else
			((TextView) view.findViewById(R.id.barcode)).setText("-");
		
		String cat = mDb.getCatById(product.cat);
		((TextView) view.findViewById(R.id.catagory)).setText(cat);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.view_product_item, parent, false);
		Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
	}

}
