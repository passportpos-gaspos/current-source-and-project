package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class CashierReportFragment extends Fragment {

	private final String DEBUG_TAG = "[CashierReportFragment]";
	private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private FragmentActivity mActivity;
	private ProductDatabase mDb;
	private ArrayList<ReportCart> mCarts;
	private ArrayList<ItemsSold> mItemsSoldList = new ArrayList<>();

	private Typeface mNotoSans;
	private Typeface mNotoSansBold;
	private long fromD;
	private long toD;
	private ProgressDialog mProgressDialog;

    private ListView mListView;
    private CashierAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_list_view, container, false);
        mNotoSans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf");
        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        setUpDates();
        return view;
    }

	@Override
	public void onAttach(Context context){
		super.onAttach(context);
        this.mActivity = getActivity();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        setUpDates();
		mDb = ProductDatabase.getInstance(getActivity());
		mCarts = new ArrayList<>();
        bindUIElements(view);
        setUpListView();
	}

    private void bindUIElements(View view) {
        mListView = (ListView)view.findViewById(R.id.list_view);
    }

    private void setUpListView(){
        mAdapter = new CashierAdapter();
        mListView.setAdapter(mAdapter);
        //refresh(fromD, toD);
		SetUpdata();
    }

	private void setUpDates(){
		String from = DateFormat.getDateInstance().format(new Date());
		String to = DateFormat.getDateInstance().format(new Date());

		GregorianCalendar fromDate = (GregorianCalendar) Calendar.getInstance();
		fromDate.set(Calendar.HOUR_OF_DAY, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);

		GregorianCalendar toDate = (GregorianCalendar) Calendar.getInstance();
        toDate.set(Calendar.HOUR_OF_DAY, 23);
        toDate.set(Calendar.MINUTE, 59);
		toDate.set(Calendar.SECOND, 59);

		fromD = fromDate.getTime().getTime();
		toD = toDate.getTime().getTime();
	}

	public void refresh(long l, long m){
		mCarts.clear();
		mItemsSoldList.clear();
		mCarts = mDb.getReports1(l, m);
		buildReport();
        mAdapter.notifyDataSetChanged();
	}
	public void SetUpdata()
	{
		mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run()
			{

				mCarts.clear();
				mItemsSoldList.clear();
				mCarts = mDb.getReports1(fromD, toD);
				buildReport();
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run()
					{
						mAdapter.notifyDataSetChanged();
						if(mProgressDialog.isShowing())
							mProgressDialog.dismiss();
					}
				});
			}
		});
		thread.start();
	}
	public void setDates(long l, long m) {
		this.fromD = l;
		this.toD = m;
	}

	private void buildReport(){
		for(ReportCart cart : mCarts) {
            ItemsSold item = new ItemsSold();
            if(cart.getCashierId() > 0){
                Cashier cashier = mDb.getCashier(cart.getCashierId());
                item.setId(String.valueOf(cashier.id));
                item.setName(cashier.name);
            }else{
                item.setId("admin");
                item.setName(getString(R.string.txt_administrator));
            }

            if(cart.mStatus.equals(Cart.VOIDED)) item.setVoidAmount(cart.mTotal);
            else if(cart.mStatus.equals(Cart.RETURNED)) item.setReturnAmount(cart.mTotal);
			else item.setPrice(cart.mTotal);
				item.setQuantity(BigDecimal.ONE);
            addNewItem(item);
		}
	}

	private void addNewItem(ItemsSold newItem){

		Iterator iterator= mItemsSoldList.iterator();
		while(iterator.hasNext()){
			ItemsSold oldItem = (ItemsSold) iterator.next();
			if(newItem.equals(oldItem)){
				oldItem.setQuantity(newItem.getQuantity().add(oldItem.getQuantity()));
				oldItem.setPrice(newItem.getPrice().add(oldItem.getPrice()));
				oldItem.setVoidAmount(newItem.getCost().add(oldItem.getVoidAmount()));
                oldItem.setReturnAmount(newItem.getCost().add(oldItem.getReturnAmount()));
				return;
			}
		}

		mItemsSoldList.add(newItem);
	}

	private class CashierAdapter extends BaseAdapter{

        private LayoutInflater inflater;
        public CashierAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return mItemsSoldList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.fragment_cashier_report, parent, false);
                Utils.setTypeFace(mNotoSans, (ViewGroup) convertView);
                holder = new ViewHolder();
                holder.cashierName = (TextView) convertView.findViewById(R.id.cashier_name_text_view);
                holder.totalSaleCount = (TextView) convertView.findViewById(R.id.total_sales_count_text_view);
                holder.totalSale = (TextView) convertView.findViewById(R.id.total_sales_text_view);
                holder.totalReturn = (TextView) convertView.findViewById(R.id.total_refund_text_view);
                holder.totalVoid = (TextView) convertView.findViewById(R.id.total_void_text_view);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

			ItemsSold item = mItemsSoldList.get(position);
			holder.cashierName.setText(item.getName());
			holder.totalSaleCount.setText(item.getQuantity().toString());
			//holder.totalSale.setText(DecimalFormat.getCurrencyInstance().format((item.getPrice()).divide(Consts.HUNDRED)));
			//holder.totalVoid.setText(DecimalFormat.getCurrencyInstance().format((item.getVoidAmount()).divide(Consts.HUNDRED)));
			//holder.totalReturn.setText(DecimalFormat.getCurrencyInstance().format((item.getReturnAmount()).divide(Consts.HUNDRED)));

			holder.totalSale.setText(Utils.formatCartTotal((item.getPrice())));
			holder.totalVoid.setText(Utils.formatCartTotal((item.getVoidAmount())));
			holder.totalReturn.setText(Utils.formatCartTotal((item.getReturnAmount())));


            return convertView;
        }


        class ViewHolder{
            TextView cashierName;
            TextView totalSaleCount;
            TextView totalVoid;
            TextView totalSale;
            TextView totalReturn;

        }
    }

	public void printReport(){
		mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_report), true, false);
		new PrintOperation().execute();
	}


	private class PrintOperation extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			boolean result;

			for (String t : ReceiptSetting.printers) {
				Log.v("Printer", t);

				try {
					JSONObject object = new JSONObject(t);

					ReceiptSetting.enabled = true;
					ReceiptSetting.address = object.getString("address");
					ReceiptSetting.make = object.getInt("printer");
					ReceiptSetting.size = object.getInt("size");
					ReceiptSetting.type = object.getInt("type");
					ReceiptSetting.drawer = object.getBoolean("cashDrawer");
					if (object.has("main"))
						ReceiptSetting.mainPrinter = object.getBoolean("main");
					else
						ReceiptSetting.mainPrinter = true;

					if (ReceiptSetting.mainPrinter) {
						int cols = 40;

						if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
							cols = 30;

						EscPosDriver.print(getActivity(), getReportString(cols), ReceiptSetting.drawer);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String arrayPortName) {
			if(mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}
	}

	private String getReportString(int cols){
		StringBuffer reportString = new StringBuffer();

		if (mCarts.size() > 0) {
			long from = mCarts.get(0).getDate();
			long to = mCarts.get(mCarts.size() - 1).getDate();

			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			String date1string = df.format(new Date(from));
			String date2string = df.format(new Date(to));

			reportString.append(EscPosDriver.wordWrap(getString(R.string.txt_store_label) + StoreSetting.getName(), cols - 1)).append('\n');
			reportString.append(EscPosDriver.wordWrap(getString(R.string.txt_address_label) + StoreSetting.getAddress1(), cols - 1)).append('\n').append('\n');
			reportString.append(EscPosDriver.wordWrap(String.format(getString(R.string.msg_cashier_report_between_dates), date1string, date2string), cols + 1)).append('\n').append('\n');

			StringBuffer message;
			for (ItemsSold item : mItemsSoldList) {
                reportString.append(EscPosDriver.wordWrap(WordUtils.capitalize(item.getName()), cols+1)).append('\n');
				message = new StringBuffer((getString(R.string.txt_total_number_of_sales) + "                                        ").substring(0, cols));
				//String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));
				String substring = item.getQuantity().toString();
				message.replace(message.length() - substring.length(), cols - 1, substring);
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

                message = new StringBuffer((getString(R.string.txt_total_sales) + "                                        ").substring(0, cols));
                //String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));
                substring = Utils.formatCartTotal(item.getPrice());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

                message = new StringBuffer((getString(R.string.txt_total_void) + "                                        ").substring(0, cols));
                //String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));
                substring = Utils.formatCartTotal(item.getVoidAmount());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

                message = new StringBuffer((getString(R.string.txt_total_returns) + "                                        ").substring(0, cols));
                //String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));
                substring = Utils.formatCartTotal(item.getReturnAmount());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
                reportString.append('\n').append('\n');
			}

			reportString.append('\n');
			reportString.append('\n');
		}

		return reportString.toString();
	}
}
