package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.interfaces.ItemOpen;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.model.SectionItem;
import com.pos.passport.util.Utils;

import java.util.ArrayList;

public class OpenOrderAdapter extends ArrayAdapter<ItemOpen>  {


	Context context;
	ArrayList<ItemOpen> Items;
	private LayoutInflater mInflater;
	Activity aa;
	public OpenOrderAdapter(Context context, ArrayList<ItemOpen> Items, Activity aa)
	{
		super(context, R.layout.rowview_openorder, Items);
		this.context = context;
		this.Items = Items;		
		this.aa=aa;
		this.mInflater=(LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		View v=convertView;
		final ItemOpen i = Items.get(position);
		if (i != null) 
		{
			if(i.isSection())
			{
				SectionItem si = (SectionItem)i;
				v = mInflater.inflate(R.layout.sectionview_row, null);
				Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				final TextView sectionView = (TextView) v.findViewById(R.id.txt_date);
				String udata=si.getTitle();
				sectionView.setText(udata);

			}else
			{
				 OpenorderData ei = (OpenorderData)i;
				v = mInflater.inflate(R.layout.rowview_openorder, null);
				Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) convertView);
				TextView name_txt = (TextView) v.findViewById(R.id.name_txt);
				TextView order_id_txt = (TextView) v.findViewById(R.id.order_id_txt);
				TextView time_txt = (TextView) v.findViewById(R.id.time_txt);
				TextView typetxt = (TextView) v.findViewById(R.id.typetxt);
				TextView status_txt = (TextView) v.findViewById(R.id.status_txt);

				name_txt.setText(ei.getCustomerName());
				order_id_txt.setText(ei.getOrderId());
				typetxt.setText(ei.getOrderType());
				status_txt.setText(ei.getOrderStatus());
				time_txt.setText(ei.getOrderPaidDate());
				typetxt.setText(ei.getOrderType());


			}
		}
        
        //return rowView;
		return v;
	}

	/*private class ViewHolder
	{
		public boolean needInflate;
		public TextView name_txt,order_id_txt,time_txt,typetxt,status_txt;//,email_txt;
		//public ImageView bigimg,imgIcon1;
		
	}*/
	/*private void setViewHolder(View rowView) {
		ViewHolder vh = new ViewHolder();
		    vh.name_txt = (TextView) rowView.findViewById(R.id.name_txt);
		vh.order_id_txt = (TextView) rowView.findViewById(R.id.order_id_txt);
		vh.time_txt = (TextView) rowView.findViewById(R.id.time_txt);
		vh.typetxt = (TextView) rowView.findViewById(R.id.typetxt);
		vh.status_txt = (TextView) rowView.findViewById(R.id.status_txt);
	      //  vh.email_txt = (TextView) rowView.findViewById(R.id.email_txt);
	        //vh.date_txt = (TextView) rowView.findViewById(R.id.date_txt);
	    rowView.setTag(vh);
		}*/
	
}
