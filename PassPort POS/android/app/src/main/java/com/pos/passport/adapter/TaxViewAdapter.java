package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.model.TaxSetting;
import com.pos.passport.util.Utils;

import java.util.List;

public class TaxViewAdapter extends BaseAdapter {

    private Context context;
    private int layoutResourceId;
    private List<TaxSetting> mItemButtons;

    public TaxViewAdapter(Context context, List<TaxSetting> itemButtons) {
        this.context = context;
        this.mItemButtons = itemButtons;
    }

    @Override
    public int getCount() {
        return mItemButtons.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemButtons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItemButtons.get(position).getTaxId();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //TaxSetting itemButton = mItemButtons.get(position);
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.taxitemlayout, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
            holder = new ViewHolder();
            holder.txttitle_lbl = (TextView) row.findViewById(R.id.txttitle_lbl);
            holder.tax_1_name_edit_text= (EditText) row.findViewById(R.id.tax_1_name_edit_text);
            holder.tax_1_edit_text= (EditText) row.findViewById(R.id.tax_1_edit_text);
            //holder.image = (ImageView) row.findViewById(R.id.image);


            row.setTag(holder);
        } else
        {
            holder = (ViewHolder) row.getTag();
        }
        holder.txttitle_lbl.setText("Tax Rate "+ (position + 1));
        holder.tax_1_name_edit_text.setText(mItemButtons.get(position).getTaxname().toString().trim());
        holder.tax_1_edit_text.setText(""+mItemButtons.get(position).getTaxpercent());

        /*ImageLoaderImage imLoader = ImageLoaderImage
                .getImLoader(context);
        imLoader.DisplayImageBackground(itemButton.link, holder.image);*/

        return row;
    }

    static class ViewHolder {
        TextView txttitle_lbl;
        EditText tax_1_name_edit_text,tax_1_edit_text;
        ImageView image;
    }
}