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
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class PrinterAdapter extends CursorAdapter {
    private Context mContext;

	public PrinterAdapter(Cursor cursor, Context context) {
        super(context, cursor);
        this.mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
						
        final int blurbIndex = cursor.getColumnIndexOrThrow("blurb");
        String jsonString = cursor.getString(blurbIndex);
        JSONObject object = null;
        		
        try {
			object = new JSONObject(jsonString);

			if (object.getInt("printer") == ReceiptSetting.MAKE_CUSTOM)
				((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_custom_america_t_ten);
			if (object.getInt("printer") == ReceiptSetting.MAKE_ESCPOS)
				((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_generic_esc_pos);
			if (object.getInt("printer") == ReceiptSetting.MAKE_PT6210)
				((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_partner_tech_pt6210);
			if (object.getInt("printer") == ReceiptSetting.MAKE_SNBC)
				((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_snbc);
			if (object.getInt("printer") == ReceiptSetting.MAKE_STAR)
				((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_tsp100_lan);
			if (object.getInt("printer") == ReceiptSetting.MAKE_ELOTOUCH)
                ((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_elo_touch);

			((TextView) view.findViewById(R.id.item_desc_text_view)).setText(object.getString("address"));

			((TextView) view.findViewById(R.id.quantity)).setText("");
			((TextView) view.findViewById(R.id.item_price_text_view)).setText(mContext.getString(R.string.txt_button_type_label) + object.getInt("type"));
			((TextView) view.findViewById(R.id.barcode)).setText(mContext.getString(R.string.txt_cash_drawer_label) + object.getBoolean("cashDrawer"));
			//((TextView) view.findViewById(R.id.receiptPrintOption)).setText(mContext.getString(R.string.txt_receipt_type_label)+ StringUtils.getReceiptType(object.getInt("receiptprintoption")));

			if(object.has("main")) {
				if(object.getBoolean("main")) {
					((TextView) view.findViewById(R.id.catagory)).setText(R.string.txt_main_printer);
				} else if(object.has("openOrderPrinter") && object.getBoolean("openOrderPrinter")){
					((TextView) view.findViewById(R.id.catagory)).setText(R.string.txt_print_open_order);
				}else {
					((TextView) view.findViewById(R.id.catagory)).setText(R.string.txt_secondary_printer);
				}
			} else {
				((TextView) view.findViewById(R.id.catagory)).setText(R.string.txt_main_printer);
			}
		} catch (JSONException e)
		{
			e.printStackTrace();
			((TextView) view.findViewById(R.id.item_name_text_view)).setText(R.string.txt_invalid_printer_settings);
			((TextView) view.findViewById(R.id.item_desc_text_view)).setText(R.string.txt_long_press_to_edit_delete);
			((TextView) view.findViewById(R.id.quantity)).setText("");
			((TextView) view.findViewById(R.id.item_price_text_view)).setText("");
			((TextView) view.findViewById(R.id.barcode)).setText("");
			((TextView) view.findViewById(R.id.catagory)).setText("");
			((TextView) view.findViewById(R.id.receiptPrintOption)).setText("");
		} 
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.itemlist, parent, false);
		Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
	}
}
