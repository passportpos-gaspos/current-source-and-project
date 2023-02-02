package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.util.List;

public class InventorySearchViewAdapter extends BaseAdapter {
    private Context context;
    private List<Product> mItemProduct;
    private ProductDatabase mDb;

    public InventorySearchViewAdapter(Context context, List<Product> mItemProduct) {
        this.context = context;
        this.mItemProduct = mItemProduct;
        mDb = ProductDatabase.getInstance(context);
    }

    @Override
    public int getCount() {
        return mItemProduct.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemProduct.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItemProduct.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = mItemProduct.get(position);
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.view_product_item, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
            holder = new ViewHolder();
            holder.item_name_text_view = (TextView) row.findViewById(R.id.item_name_text_view);
            holder.item_desc_text_view = (TextView) row.findViewById(R.id.item_desc_text_view);
            holder.quantity = (TextView) row.findViewById(R.id.quantity);
            holder.item_price_text_view = (TextView) row.findViewById(R.id.item_price_text_view);
            holder.barcode = (TextView) row.findViewById(R.id.barcode);
            holder.catagory = (TextView) row.findViewById(R.id.catagory);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        String name = product.name.toString().trim();
        holder.item_name_text_view.setText(name);
        holder.item_desc_text_view.setText(product.desc);
        if (product.track)
            holder.quantity.setText("" + product.onHand);
        else
            holder.quantity.setText("---");

        String price = Utils.formatCurrency(product.price);
        holder.item_price_text_view.setText(price);
        String barcode = product.barcode;
        if (barcode.length() > 0)
            holder.barcode.setText(product.barcode);
        else
            holder.barcode.setText("-");

        String cat = mDb.getCatById(product.cat);
        holder.catagory.setText(cat);

        return row;
    }

    static class ViewHolder {
        TextView item_name_text_view, item_desc_text_view, quantity, item_price_text_view, barcode, catagory;
    }
}