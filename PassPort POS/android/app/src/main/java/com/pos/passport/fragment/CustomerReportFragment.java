package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Customer;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * Created by Kareem on 6/1/2016.
 */
public class CustomerReportFragment extends Fragment {

    private final String DEBUG_TAG = "[CustomerReportFragment]";
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

    private LinearLayout mItemsLinearLayout;
    int mSizeText=15;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_report, container, false);
        if(Utils.ResourceSize(getActivity()) == 0)
        {
            mSizeText=18;
        }else {
            mSizeText=22;
        }
        mNotoSans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf");
        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        setUpDates();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        mCarts = new ArrayList<>();
        bindUIElements(view);
       // refresh(fromD, toD);
        SetUpdata();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    private void bindUIElements(View v){
        mItemsLinearLayout = (LinearLayout) v.findViewById(R.id.item_linear_layout);
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayReport();
            }
        });
    }
    public void SetUpdata()
    {
        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                refresh(fromD, toD);

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
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
            long date = cart.mDate;
            if (!cart.mVoided) {
                if(cart.hasCustomer()){
                    Customer customer = cart.getCustomer();
                    ItemsSold item = new ItemsSold();
                    item.setId(String.valueOf(customer.getId()));
                    item.setName(customer.getFullName());
                    item.setPrice(cart.mTotal);
                    addNewItem(item);
                }else{
                    ItemsSold item = new ItemsSold();
                    item.setId("others");
                    item.setName(getString(R.string.txt_no_customer));
                    item.setPrice(cart.mTotal);
                    addNewItem(item);
                }
            }
        }
    }

    private void addNewItem(ItemsSold newItem){

        Iterator iterator= mItemsSoldList.iterator();
        while(iterator.hasNext()){
            ItemsSold oldItem = (ItemsSold) iterator.next();
            if(newItem.equals(oldItem)){
                oldItem.setQuantity(newItem.getQuantity().add(oldItem.getQuantity()));
                oldItem.setPrice(newItem.getPrice().add(oldItem.getPrice()));
                oldItem.setCost(newItem.getCost().add(oldItem.getCost()));
                return;
            }
        }

        mItemsSoldList.add(newItem);
    }

    private void displayReport(){
        mItemsLinearLayout.removeAllViews();
        for(ItemsSold item : mItemsSoldList){
            LinearLayout linearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(getActivity());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
            nameTextView.setLayoutParams(p);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(item.getName());
            linearLayout.addView(nameTextView);

            TextView salePriceTextView = new TextView(getActivity());
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            salePriceTextView.setLayoutParams(p);
            salePriceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            salePriceTextView.setGravity(Gravity.END);
            //salePriceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            salePriceTextView.setText(Utils.formatCartTotal(item.getPrice()));
            linearLayout.addView(salePriceTextView);

            mItemsLinearLayout.addView(linearLayout);
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
            reportString.append(EscPosDriver.wordWrap(String.format(getString(R.string.msg_customer_report_between_dates), date1string, date2string), cols + 1)).append('\n').append('\n');

            StringBuffer message;
            for (ItemsSold item : mItemsSoldList) {
                message = new StringBuffer((item.getName() + "                                   ").substring(0, cols));
                //String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));
                String substring = Utils.formatCartTotal(item.getPrice());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
            }

            reportString.append('\n');
            reportString.append('\n');
        }

        return reportString.toString();
    }
}
