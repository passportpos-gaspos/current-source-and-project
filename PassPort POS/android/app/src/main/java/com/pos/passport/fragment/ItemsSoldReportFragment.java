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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Cart;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Kareem on 5/30/2016.
 */
public class ItemsSoldReportFragment extends Fragment {

    private final String DEBUG_TAG = "[ItemsSoldReportFragment]";
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private FragmentActivity mActivity;
    private ProductDatabase mDb;
    private ArrayList<ReportCart> mCarts;
    private HashSet<ItemsSold> mItemsSoldSet = new HashSet<>();
    private ArrayList<ItemsSold> mItemsSoldList = new ArrayList<>();
    private BigDecimal mTotalPrice = BigDecimal.ZERO;
    private BigDecimal mTotalCost = BigDecimal.ZERO;

    private Typeface mNotoSans;
    private Typeface mNotoSansBold;
    private long fromD;
    private long toD;
    private ProgressDialog mProgressDialog;

    private LinearLayout mItemsLinearLayout;
    private TextView mTotalPriceTextView;
    private TextView mTotalMarginTextView;
    int mSizeText=15;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_sold_report, container, false);
        Log.e("Items sold rep","Items sold rep");
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
        //refresh(fromD, toD);
        SetUpdata();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    private void bindUIElements(View v){
        mItemsLinearLayout = (LinearLayout) v.findViewById(R.id.item_linear_layout);
        mTotalPriceTextView = (TextView) v.findViewById(R.id.total_sales_text_view);
        mTotalMarginTextView = (TextView) v.findViewById(R.id.total_margin_text_view);
    }

    public void refresh(long l, long m){
        mCarts.clear();
        mItemsSoldSet.clear();
        mItemsSoldList.clear();
        mTotalCost = BigDecimal.ZERO;
        mTotalPrice = BigDecimal.ZERO;
        mCarts = mDb.getReports1(l,m);
        buildReport(mCarts);
        mItemsSoldList = new ArrayList<>(mItemsSoldSet);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayReport();
            }
        });
    }
    public void SetUpdata() {
        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run(){
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
    private void displayReport(){
        mItemsLinearLayout.removeAllViews();
        for(ItemsSold item : mItemsSoldList)
        {
            LinearLayout linearLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(mActivity);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
            nameTextView.setLayoutParams(p);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(item.getName());
            nameTextView.setPadding(5,5,5,5);
            linearLayout.addView(nameTextView);

            TextView quantityTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            quantityTextView.setLayoutParams(p);
            quantityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            quantityTextView.setText(item.getQuantity().toString());
            quantityTextView.setPadding(5,5,5,5);
            linearLayout.addView(quantityTextView);

            TextView priceTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            priceTextView.setLayoutParams(p);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
            priceTextView.setPadding(5,5,5,5);
            linearLayout.addView(priceTextView);

            TextView marginTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            marginTextView.setLayoutParams(p);
            marginTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
           // marginTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getMargin().divide(Consts.HUNDRED)));
            marginTextView.setText(Utils.formatCartTotal(item.getMargin()));
            marginTextView.setPadding(5,5,5,5);
            linearLayout.addView(marginTextView);

            mItemsLinearLayout.addView(linearLayout);
        }

        //mTotalPriceTextView.setText(DecimalFormat.getCurrencyInstance().format(mTotalPrice.divide(Consts.HUNDRED)));
        mTotalPriceTextView.setText(Utils.formatCartTotal(mTotalPrice));
        for(ItemsSold itemsSold : mItemsSoldList){
            mTotalCost = mTotalCost.add(itemsSold.getMargin());
        }
        //mTotalMarginTextView.setText(DecimalFormat.getCurrencyInstance().format(mTotalCost.divide(Consts.HUNDRED)));
        mTotalMarginTextView.setText(Utils.formatCartTotal(mTotalCost));
    }

    private void buildReport(ArrayList<ReportCart> mCarts){
        for(ReportCart cart : mCarts){
            long date = cart.mDate;
            if(!cart.mVoided && !cart.mStatus.equals(Cart.RETURNED)) {
                mTotalPrice = mTotalPrice.add(cart.mSubtotal);
                for(Product product : cart.getProducts()) {
                    if (!product.isNote) {
                        ItemsSold newItem = new ItemsSold();
                        newItem.setId(String.format("i%d", product.id));
                        newItem.setName(product.name);
                        int quantity = product.quantity;
                        newItem.setQuantity(new BigDecimal(quantity));
                        BigDecimal price = product.itemTotal(date);
                        newItem.setPrice(price);
                        newItem.setCost(product.cost.multiply(new BigDecimal(quantity)));
                        addNewItem(newItem);
                        for (Product modifier : product.modifiers) {
                            if(modifier.modifierType == Product.MODIFIER_TYPE_ADDON) {
                                ItemsSold newModifier = new ItemsSold();
                                newModifier.setId(String.format("m%d", modifier.id));
                                newModifier.setName(modifier.name);
                                int modifierQuantity = modifier.quantity;
                                newModifier.setQuantity(new BigDecimal(modifierQuantity));
                                BigDecimal modifierPrice = modifier.itemTotal(date);
                                newModifier.setPrice(modifierPrice);
                                newModifier.setCost(modifier.cost.multiply(new BigDecimal(modifierQuantity)));
                                addNewItem(newModifier);
                            }else if(modifier.itemTotal(date).compareTo(BigDecimal.ZERO) > 0){
                                ItemsSold newModifier = new ItemsSold();
                                newModifier.setId("0");
                                newModifier.setName(getString(R.string.txt_miscellaneous));
                                int modifierQuantity = modifier.quantity;
                                newModifier.setQuantity(new BigDecimal(modifierQuantity));
                                BigDecimal modifierPrice = modifier.itemTotal(date);
                                newModifier.setPrice(modifierPrice);
                                newModifier.setCost(modifier.cost.multiply(new BigDecimal(modifierQuantity)));
                                addNewItem(newModifier);
                            }
                        }

                    }else if(product.isNote && product.itemTotal(date).compareTo(BigDecimal.ZERO)>0){
                        ItemsSold newItem = new ItemsSold();
                        newItem.setId("0");
                        newItem.setName(getString(R.string.txt_miscellaneous));
                        int quantity = product.quantity;
                        newItem.setQuantity(new BigDecimal(quantity));
                        BigDecimal price = product.itemTotal(date);
                        newItem.setPrice(price);
                        newItem.setCost(product.cost.multiply(new BigDecimal(quantity)));
                        addNewItem(newItem);
                    }
                }
            }
        }
    }

    private void addNewItem(ItemsSold newItem){

        Iterator iterator= mItemsSoldSet.iterator();
        while(iterator.hasNext()){
            ItemsSold oldItem = (ItemsSold) iterator.next();
            if(newItem.equals(oldItem)){
                oldItem.setQuantity(newItem.getQuantity().add(oldItem.getQuantity()));
                oldItem.setPrice(newItem.getPrice().add(oldItem.getPrice()));
                oldItem.setCost(newItem.getCost().add(oldItem.getCost()));
                return;
            }
        }

        mItemsSoldSet.add(newItem);
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

    public void setDates(long l, long m) {
        this.fromD = l;
        this.toD = m;
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

    public String getReportString(int size) {
        StringBuilder reportString = new StringBuilder();

        int cols = size;

        if (mCarts.size() > 0) {
            long date1 = mCarts.get(0).getDate();
            long date2 = mCarts.get(mCarts.size()-1).getDate();

            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            String date1string = df.format(new Date(date1));
            String date2string = df.format(new Date(date2));

            reportString.append(EscPosDriver.wordWrap(getString(R.string.txt_store_label) + StoreSetting.getName(), cols - 1)).append('\n');
            reportString.append(EscPosDriver.wordWrap(getString(R.string.txt_address_label) + StoreSetting.getAddress1(), cols-1)).append('\n').append('\n');

            reportString.append(EscPosDriver.wordWrap(String.format(getString(R.string.msg_item_sold_between_dates), date1string, date2string), cols+1)).append('\n').append('\n');

            StringBuffer message;
            for (ItemsSold item : mItemsSoldList)
            {
                reportString.append(EscPosDriver.wordWrap(item.getQuantity()+ "x "+item.getName(), cols+1)).append('\n');

                message = new StringBuffer((getString(R.string.txt_total) + "                                   ").substring(0, cols));
                //String substring = DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED));

                String substring = Utils.formatCartTotal(item.getPrice());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

                message = new StringBuffer((getString(R.string.txt_profit) + "                                  ").substring(0, cols));
                //substring = DecimalFormat.getCurrencyInstance().format(item.getMargin().divide(Consts.HUNDRED));
                substring = Utils.formatCartTotal(item.getMargin());
                message.replace(message.length() - substring.length(), cols - 1, substring);
                reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
                reportString.append('\n');
            }

            message = new StringBuffer((getString(R.string.txt_total_sales) + "                             ").substring(0, cols));
            //String substring = DecimalFormat.getCurrencyInstance().format(mTotalPrice.divide(Consts.HUNDRED));
            String substring = Utils.formatCartTotal(mTotalPrice);
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

            message = new StringBuffer((getString(R.string.txt_total_profit) + "                            ").substring(0, cols));
            //substring = DecimalFormat.getCurrencyInstance().format(mTotalCost.divide(Consts.HUNDRED));
            substring = Utils.formatCartTotal(mTotalCost);
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

            reportString.append('\n');
            reportString.append('\n');
        }

        return reportString.toString();
    }
}
