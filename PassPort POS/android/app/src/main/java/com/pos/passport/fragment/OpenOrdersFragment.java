package com.pos.passport.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.ItemOpen;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.ReportCartCounter;
import com.pos.passport.model.SectionItem;
import com.pos.passport.util.Utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by karim on 1/25/16.
 */
public class OpenOrdersFragment extends Fragment {
    public TextView mCloseImageView;
    public TextView title_text_view;
    public ListView mOpenOrdersListView;
    private ImageView mBackImageView;
    //private OpenOrdersAdapter mAdapter;
    public OpenOrderAdapter mAdapter;
    public List<ReportCart> mCart_send;
    public List<ItemOpen> mCart;
    public ProductDatabase mDb;
    public QueueInterface mCallback;
    public int mRowSelector = -1;
    private EditText editText;
    private ImageView mClearSearch;
    public View.OnClickListener mCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
        }
    };

    public AdapterView.OnItemClickListener mOrderClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ItemOpen cart = mCart.get(position);
            ReportCartCounter ncart = (ReportCartCounter) cart;
            mCart_send = mDb.getOpenOrdersById("" + ncart.trans);
            if (mCart_send.size() > 0) {
                ReportCart cart1 = mCart_send.get(0);
                //mCallback.onLoadQueueCounter(ncart);
                mCallback.onLoadQueue(cart1);
            }
            mRowSelector = position;
            mAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_orders, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        mRowSelector = -1;
        setUpListView();
        setUpListeners();
        setUpUi();
    }

    public void bindUIElements(View view) {
        //mTitleTextView = (TextView)view.findViewById(R.id.title_text_view);
        mCloseImageView = (TextView) view.findViewById(R.id.back_button);
        title_text_view = (TextView) view.findViewById(R.id.title_text_view);
        mBackImageView = (ImageView) view.findViewById(R.id.back_image_view);
        mOpenOrdersListView = (ListView) view.findViewById(R.id.open_orders_list_view);
        editText = (EditText) view.findViewById(R.id.search);
        mClearSearch = (ImageView) view.findViewById(R.id.img_close);
    }

    public void setUpUi() {
        // mTitleTextView.setText(R.string.txt_open_orders);
        mBackImageView.setVisibility(View.GONE);
        mCloseImageView.setVisibility(View.GONE);
        title_text_view.setVisibility(View.GONE);

    }

    public void setUpListeners() {
        mOpenOrdersListView.setOnItemClickListener(mOrderClickListener);
        mCloseImageView.setOnClickListener(mCloseClickListener);
    }

    public void setUpListView() {
        /*mCart = mDb.getOpenOrders();
        mAdapter = new OpenOrdersAdapter();
        mOpenOrdersListView.setAdapter(mAdapter);*/
        mCart = new ArrayList<>();
        mCart = mDb.getOpenOrdersNew();
        mAdapter = new OpenOrderAdapter(getActivity(), mCart);
        mOpenOrdersListView.setAdapter(mAdapter);

        mClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                //List<ItemOpen> mCart = new ArrayList<>();
               // mCart = mDb.getOpenOrdersNew();
                setUpData(mDb.getOpenOrdersNew(),getActivity());

                Utils.dismissKeyboard(view);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {

                    if (s.toString().length() > 2) {
                        //List<ItemOpen> mCart = new ArrayList<>();
                        //mCart = mDb.getSearchOpenOrdersNew(s.toString());
                        setUpData(mDb.getSearchOpenOrdersNew(s.toString()),getActivity());
                    } else if (s.toString().length() == 0) {
                        //List<ItemOpen> mCart = new ArrayList<>();
                        //mCart = mDb.getOpenOrdersNew();
                        setUpData(mDb.getOpenOrdersNew(),getActivity());
                    }

                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setUpData(List<ItemOpen> mCartnew,Context mcontext) {

        mCart = new ArrayList<>();

        if (mCart.size() > 0)
            mCart.clear();

        mCart = mCartnew;//mDb.getSearchOpenOrdersNew(inputtext);
        mAdapter = new OpenOrderAdapter(mcontext, mCart);
        mOpenOrdersListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
    public void setUpDataNew(List<ItemOpen> mCartnew,Context mcontext) {

        mCart = new ArrayList<>();

        if (mCart.size() > 0)
            mCart.clear();

        mCart = mCartnew;//mDb.getSearchOpenOrdersNew(inputtext);
        mAdapter = new OpenOrderAdapter(mcontext, mCart);
        mOpenOrdersListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

//    private class OpenOrdersAdapter extends BaseAdapter
//    {
//        LayoutInflater inflater;
//
//        public OpenOrdersAdapter() {
//            inflater = LayoutInflater.from(getActivity());
//        }
//
//        @Override
//        public int getCount() {
//            return mCart.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder = null;
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.view_order_item, parent, false);
//
//                holder = new ViewHolder();
//                holder.nameTextView = (TextView)convertView.findViewById(R.id.customer_name_text_view);
//                holder.orderNumberTextView = (TextView)convertView.findViewById(R.id.order_number_text_view);
//                holder.valueTextView = (TextView)convertView.findViewById(R.id.value_text_view);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder)convertView.getTag();
//            }
//            ReportCart cart = mCart.get(position);
//            holder.nameTextView.setText(TextUtils.isEmpty(cart.mName) ? "" : cart.mName);
//            holder.orderNumberTextView.setText(cart.trans.toString());
//            holder.valueTextView.setText(Utils.formatCurrency(cart.mTotal));
//
//            return convertView;
//        }
//
//        class ViewHolder {
//            TextView nameTextView;
//            TextView orderNumberTextView;
//            TextView valueTextView;
//        }
//    }

    public class OpenOrderAdapter extends ArrayAdapter<ItemOpen> {
        Context context;
        List<ItemOpen> Items;
        private LayoutInflater mInflater;
        Activity aa;

        //public OpenOrderAdapter(Context context, ArrayList<ItemOpen> Items, Activity aa)
        public OpenOrderAdapter(Context context, List<ItemOpen> Items) {
            super(context, R.layout.rowview_openorder, Items);
            this.context = context;
            this.Items = Items;
            this.aa = aa;
            this.mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ItemOpen i = Items.get(position);
            if (i != null) {
                if (i.isSection()) {
                    SectionItem si = (SectionItem) i;
                    v = mInflater.inflate(R.layout.sectionview_row, null);
                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);
                    final TextView sectionView = (TextView) v.findViewById(R.id.txt_date);
                    String udata = si.getTitle();
                    //Log.e("tile show",">>>>>"+si.getTitle());
                    sectionView.setText(ShowDate(Long.parseLong(udata)));

                } else {
                    //OpenorderData ei = (OpenorderData)i;
                    ReportCartCounter ei = (ReportCartCounter) i;
                    v = mInflater.inflate(R.layout.rowview_openorder, null);
                    TextView name_txt = (TextView) v.findViewById(R.id.name_txt);
                    TextView order_id_txt = (TextView) v.findViewById(R.id.order_id_txt);
                    TextView time_txt = (TextView) v.findViewById(R.id.time_txt);
                    TextView typetxt = (TextView) v.findViewById(R.id.typetxt);
                    TextView status_txt = (TextView) v.findViewById(R.id.status_txt);

                    name_txt.setText(TextUtils.isEmpty(ei.mName) ? "" : ei.mName);
                    order_id_txt.setText(ei.trans.toString());
                    typetxt.setText("Counter");
                    getDate(ei.getDate());
                    time_txt.setText("" + ShowDateTime(ei.getDate()));
                    status_txt.setText(ei.mStatus);


                    if (position % 2 == 1) {
                        v.setBackgroundColor(getResources().getColor(R.color.row_bg_light_white));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.row_bg_light_gray));

                    }
                    if (mRowSelector != -1) {
                        if (position == mRowSelector)
                            v.setBackgroundColor(getResources().getColor(R.color.row_bg_selector));
                    }

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

    public Date getDate(long time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();//get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        //SimpleDateFormat sdf1 = new SimpleDateFormat(" hh:mm a");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(new Date(time * 1000));
        Date date = new Date();
        try {
            date = sdf.parse(localTime);//get local date

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public enum Days implements Serializable {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday
    }

    public String ShowDate(long timestampget) {
        String dateshow = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampget);
        dateshow = Days.values()[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + (cal.get(Calendar.DATE)) + ", " + cal.get(Calendar.YEAR);
        return dateshow;
    }

    public String ShowDateTime(long timestampget) {
        String timeshow = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampget);
        String ampm = "";
        if (cal.get(Calendar.AM_PM) == 1)
            ampm = "PM";
        else
            ampm = "AM";

        timeshow = "" + String.format("%02d", cal.get(Calendar.HOUR)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE)) + " " + ampm;
        return timeshow;
    }
}
