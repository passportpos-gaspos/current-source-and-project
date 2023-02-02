package com.pos.passport.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.AsyncTaskListenerData;
import com.pos.passport.interfaces.ItemOpen;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.SectionItem;
import com.pos.passport.task.GetOpenOrderAsyncTask;
import com.pos.passport.task.UpdateOrderStatusAsyncTask;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CounterArchiveFragment extends Fragment {

	public ListView mFFOrdersListView;
	public ImageView mPrePage;
	public ImageView mNextPage;
	public int mRowSelector = -1;
	public OpenOrderAdapter mAdapter;
	public List<ReportCart> mCart_send;
	public List<ItemOpen> mCart = new ArrayList<>();
	public List<ItemOpen> mCartSearch = new ArrayList<>();
	public ProductDatabase mDb;
	public QueueInterface mCallback;
	public int mArchivePaging = 1;
	private EditText editText;
	private ImageView mClearSearch;
	private boolean mIsSearch=false;
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mCallback = (QueueInterface) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement QueueInterface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_counter, container, false);
		Log.e("CounterArchiveFragment","onCreateView CounterArchiveFragment called");
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDb = ProductDatabase.getInstance(getActivity());
		bindUIElements(view);
		setUpListView();
		setUpListeners();
		register();
	}

	public void bindUIElements(View view) {
		mFFOrdersListView = (ListView) view.findViewById(R.id.ff_order_list_view);
		mPrePage=(ImageView)view.findViewById(R.id.prepage);
		mNextPage=(ImageView)view.findViewById(R.id.nextpage);
		editText = (EditText) view.findViewById(R.id.search);
		mClearSearch = (ImageView) view.findViewById(R.id.img_close);
	}

	public void setUpListView() {

		mAdapter = new OpenOrderAdapter(getActivity(), mCart);
		mFFOrdersListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	public void setUpListeners()
	{
		mFFOrdersListView.setOnItemClickListener(mOrderClickListener);
		mPrePage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				if(mArchivePaging > 1)
				{
					mArchivePaging = mArchivePaging-1;
					register();
				}
			}
		});
		mNextPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
					mArchivePaging = mArchivePaging + 1;
					register();
			}
		});
		mClearSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				mIsSearch=false;
				if(editText.getText().length() > 0) {
					editText.setText("");
					Utils.dismissKeyboard(view);
					if (mCart.size() > 0)
						setUpListView();
				}
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
			public void afterTextChanged(Editable s)
			{
				try
				{
					if (s.toString().length() > 2)
					{
						if(mCart.size()>0)
						{
							mRowSelector = -1;
							setUpSearchData(getSearchOrders(s.toString()));
							mPrePage.setVisibility(View.GONE);
							mNextPage.setVisibility(View.GONE);
							mIsSearch=true;
						}
					}
					else if (s.toString().length() == 0)
					{
						mIsSearch=false;
						if(mCart.size()>0)
						{
							mRowSelector = -1;
							setUpListView();
							mCallback.onViewFFFragment(false, new OpenorderData(),true);
							if(mArchivePaging > 1) {
								mPrePage.setVisibility(View.VISIBLE);
								mNextPage.setVisibility(View.VISIBLE);
							}
							else if (mArchivePaging == 1)
							{
								mPrePage.setVisibility(View.GONE);
								mNextPage.setVisibility(View.VISIBLE);
							}
						}
					}
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
			}
		});

	}
	public void setUpSearchData(List<ItemOpen> mCart)
	{
		if(mCartSearch.size()>0)
			mCartSearch.clear();

		mCartSearch=mCart;

		mAdapter = new OpenOrderAdapter(getActivity(), mCartSearch);
		mFFOrdersListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
	public List<ItemOpen> getSearchOrders(String mOrderId)
	{
		List<ItemOpen> mSearchCart = new ArrayList<>();
		try
		{
			for (int p = 0; p < mCart.size(); p++)
			{
				ItemOpen cart = mCart.get(p);
				if(!(cart.isSection())) {
					OpenorderData mOpenData = (OpenorderData) cart;

					if (String.valueOf(mOpenData.getOrderId()).contains(mOrderId))
					{
						mSearchCart = GetSearchLoadData(mOpenData);
					}
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return mSearchCart;
	}
	public List<ItemOpen> GetSearchLoadData(OpenorderData data)
	{
		ArrayList<ItemOpen> cart = new ArrayList<>();
		try
		{
				String sector_show = "";
				String ss=data.getOrderPaidDate();
				String section_name = ConvertDatesString(data.getOrderPaidDate());
				if (sector_show.equalsIgnoreCase(""))
				{
					String section_name1 = ConvertDatesString(data.getOrderPaidDate());
					sector_show = section_name1;
					cart.add(new SectionItem(sector_show));
				}
				else
				{
					String getdateon = ConvertDatesString(data.getOrderPaidDate());
					if (getDate(Long.parseLong(getdateon), Long.parseLong(sector_show))) {
						String section_name1 = ConvertDatesString(data.getOrderPaidDate());
						sector_show = section_name1;
						cart.add(new SectionItem(sector_show));
					} else {
					}
				}
				cart.add(data);

		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return cart;
	}


	public AdapterView.OnItemClickListener mOrderClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			ItemOpen cart;
			if(mIsSearch){
				cart = mCartSearch.get(position);
			}else {
				 cart = mCart.get(position);
			}
			OpenorderData ncart = (OpenorderData) cart;
			//Log.e("Order id", "" + ncart.getOrderId());
			mRowSelector = position;
			mAdapter.notifyDataSetChanged();
			mCallback.onViewFFFragment(true, ncart,false);
		}
	};

	public void notifyChanges()
	{
		register();
	}

	private void register() {
		//Log.e("Open frag","register");
		GetOpenOrderAsyncTask asyncTask = new GetOpenOrderAsyncTask(getActivity(), true, Consts.ARCHIVE_ORDER_TEXT,mArchivePaging);
		asyncTask.setListener(new AsyncTaskListenerData() {
			@Override
			public void onSuccess(String data1) {
				try
				{
					//Log.e("AcchiveData", "AcchiveData >>>>" + data1);
					if (data1 != null)
					{
						JSONArray data = new JSONArray(data1);
						//Log.e("Data", "2>>>>" + data);
						if (data.length() > 0)
						{
							JSONArray getdata = new JSONArray(data.toString());
							if (mCart.size() > 0)
								mCart.clear();

							mCart = getFFOrders(getdata);
							if(mArchivePaging > 1) {
								mPrePage.setVisibility(View.VISIBLE);
								mNextPage.setVisibility(View.VISIBLE);
							}else{
								mPrePage.setVisibility(View.GONE);
								mNextPage.setVisibility(View.VISIBLE);
							}
                            /*for (int p = 0; p < data.length(); p++) {
                                JSONObject gdata = getdata.getJSONObject(p);
                                GetData(gdata);
                            }*/
						}
						else
						{
							if(mArchivePaging > 1) {
								mPrePage.setVisibility(View.VISIBLE);
								mNextPage.setVisibility(View.GONE);
							}
							else {
								mPrePage.setVisibility(View.GONE);
								mNextPage.setVisibility(View.GONE);
							}
							if (mCart.size() > 0)
							{
								mCart.clear();
							}
							mAdapter.notifyDataSetChanged();
							Toast.makeText(getActivity(),"No Records Found",Toast.LENGTH_SHORT).show();
						}
						setUpListView();
					}
					//else
					//{
					//	Toast.makeText(getActivity(),"No Records Found",Toast.LENGTH_SHORT).show();
					//}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure()
			{
			}
		});
		asyncTask.execute("", "", "");
	}


	public List<ItemOpen> getFFOrders(JSONArray data)
	{
		ArrayList<ItemOpen> cart = new ArrayList<>();
		try
		{
			String sector_show = "";
			for (int p = 0; p < data.length(); p++)
			{
				JSONObject gdata = data.getJSONObject(p);
				//GetData(gdata);
				String ss=gdata.getString("orderPaidDate");
				String section_name = ConvertDatesString(gdata.optString("orderPaidDate"));
				if (sector_show.equalsIgnoreCase(""))
				{
					String section_name1 = ConvertDatesString(gdata.optString("orderPaidDate"));
					sector_show = section_name1;
					cart.add(new SectionItem(sector_show));
				}
				else
				{
					String getdateon = ConvertDatesString(gdata.optString("orderPaidDate"));
					if (getDate(Long.parseLong(getdateon), Long.parseLong(sector_show))) {
						String section_name1 = ConvertDatesString(gdata.optString("orderPaidDate"));
						sector_show = section_name1;
						cart.add(new SectionItem(sector_show));
					} else {
					}
				}
				cart.add(SetFFOrderData(gdata));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return cart;
	}
	public String ConvertDatesString (String dateget)
	{
		Date date = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = (Date)formatter.parse(dateget);
			//System.out.println("convert date " +date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ""+date.getTime();
	}
	private boolean getDate(long time,long before_time)
	{
		boolean value_send=false;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		Date date_old = new Date();
		try
		{
			date = sdf.parse(Getdates(time));//get local date
			date_old=sdf.parse(Getdates(before_time));
			//if (date.after(date_old))
			if (date.before(date_old))
			{
				value_send=true;
			} else
			{
				value_send=false;
			}
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		return value_send;
	}
	private String Getdates(long timestamp)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		Date d = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(d);
	}
	public OpenorderData SetFFOrderData(JSONObject gdata)
	{
		OpenorderData data=new OpenorderData();
		try
		{
			data.setOrderId(gdata.getInt("orderId"));
			data.setOrderPaidDate(gdata.optString("orderPaidDate"));
			data.setOrderSubTotal(Float.valueOf(gdata.optString("orderSubTotal")));
			data.setOrderTax(Float.valueOf(gdata.optString("orderTax")));
			data.setOrderTip(Float.valueOf(gdata.optString("orderTip")));
			data.setOrderServiceCharge(Float.valueOf(gdata.optString("orderServiceCharge")));
			data.setOrderTotal(Float.valueOf(gdata.optString("orderTotal")));
			data.setZoneID(gdata.optString("zoneID"));
			data.setPaymentAuth(gdata.optString("paymentAuth"));
			data.setOrderType(gdata.optString("orderType"));

			if(!(gdata.optString("token").equalsIgnoreCase("")))
			data.setToken(Integer.valueOf(gdata.optString("token")));

			data.setPaymentSource(gdata.optString("paymentSource"));
			data.setSection(gdata.optString("section"));
			data.setRowid(gdata.optString("row"));
			data.setSeat(gdata.optString("seat"));
			data.setCustomerName(gdata.optString("customerName"));
			data.setOrderStatus(gdata.optString("orderStatus"));
			data.setOrderStatusId(gdata.optInt("orderStatusId"));
			data.setOrderItems(gdata.optString("orderItems"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return data;
	}
	public void GetData(JSONObject gdata) {
		ContentValues vals = new ContentValues();
		try {
			vals.put("orderId", gdata.getInt("orderId"));
			vals.put("orderPaidDate", gdata.optString("orderPaidDate"));
			vals.put("orderSubTotal", gdata.optString("orderSubTotal"));
			vals.put("orderTax", gdata.optString("orderTax"));
			vals.put("orderTip", gdata.optString("orderTip"));
			vals.put("orderServiceCharge", gdata.optString("orderServiceCharge"));
			vals.put("orderTotal", gdata.optString("orderTotal"));
			vals.put("zoneID", gdata.optString("zoneID"));
			vals.put("paymentAuth", gdata.optString("paymentAuth"));
			vals.put("orderType", gdata.optString("orderType"));
			vals.put("token", gdata.optString("token"));
			vals.put("paymentSource", gdata.optString("paymentSource"));
			vals.put("section", gdata.optString("section"));
			vals.put("row", gdata.optString("row"));
			vals.put("customerName", gdata.optString("customerName"));
			vals.put("seat", gdata.optString("seat"));
			vals.put("orderStatus", gdata.optString("orderStatus"));
			vals.put("orderStatusId", gdata.optString("orderStatusId"));
			vals.put("orderItems", gdata.optString("orderItems"));

			mDb.insertOpenOrder(vals, gdata.getInt("orderId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
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

				} else
				{
					OpenorderData ei = (OpenorderData) i;
					//ReportCartCounter ei = (ReportCartCounter) i;
					v = mInflater.inflate(R.layout.rowview_openorder, null);
					TextView ffname_txt = (TextView) v.findViewById(R.id.name_txt);
					TextView fforder_id_txt = (TextView) v.findViewById(R.id.order_id_txt);
					TextView fftime_txt = (TextView) v.findViewById(R.id.time_txt);
					TextView fftypetxt = (TextView) v.findViewById(R.id.typetxt);
					TextView ffstatus_txt = (TextView) v.findViewById(R.id.status_txt);
					ffstatus_txt.setTag(position);
					ffname_txt.setText(ei.getCustomerName());
					fforder_id_txt.setText("" + ei.getOrderId());
					fftypetxt.setText(""+ei.getOrderType());
					ffstatus_txt.setText("" + ei.getOrderStatus());
					fftime_txt.setText("" + ShowDateTime(ConvertDates(ei.getOrderPaidDate())));

					if (position % 2 == 1) {
						v.setBackgroundColor(getResources().getColor(R.color.row_bg_light_white));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.row_bg_light_gray));
					}
					if (mRowSelector != -1) {
						if (position == mRowSelector)
							v.setBackgroundColor(getResources().getColor(R.color.row_bg_selector));
					}
					ffstatus_txt.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							int clicposition = (int) view.getTag();
							ItemOpen itempos = Items.get(clicposition);
							OpenorderData einew = (OpenorderData) itempos;
							Log.e("Order number", "OrderNumber>>" + einew.getOrderId());
							//UpdateStatus("" + einew.getOrderId(), "3");
						}
					});
				}
			}

			//return rowView;
			return v;
		}
	}

	public void UpdateStatus(String mOrderNumber, String mOrderStatusId) {
		UpdateOrderStatusAsyncTask asyncTask = new UpdateOrderStatusAsyncTask(getActivity(), true) {
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Log.e("Update status", "result >>" + result);
				if (result != null) {
					try {
						JSONObject data = new JSONObject(result);
						if (data.has("success")) {
							Toast.makeText(getActivity(), data.optString("success"), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getActivity(), "Order status updated failed!", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

		asyncTask.execute(mOrderNumber, mOrderStatusId, "");
	}

	public Long ConvertDates(String dateget) {
		Date date = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = (Date) formatter.parse(dateget);
			//System.out.println("convert date " + date.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public Date getDate(long time) {
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();//get your local time zone.
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
		//SimpleDateFormat sdf1 = new SimpleDateFormat(" hh:mm a");
		sdf.setTimeZone(tz);//set time zone.
		String localTime = sdf.format(new Date(time * 1000));
		Date date = new Date();
		try
		{
			date = sdf.parse(localTime);//get local date

		} catch (ParseException e)
		{
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
