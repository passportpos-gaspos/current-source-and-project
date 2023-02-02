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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.Summary;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.ReceiptHelper;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Kareem on 5/31/2016.
 */
public class SummaryReportFragment extends Fragment {

    private final String DEBUG_TAG = "[SummaryReportFragment]";
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    private ProductDatabase mDb;
    private ArrayList<ReportCart> mCarts;
    private Typeface mNotoSans;
    private Typeface mNotoSansBold;
    private long fromD;
    private long toD;
    private ProgressDialog mProgressDialog;
    private FragmentActivity mActivity;
    private LinearLayout mDepartmentLinearLayout;
    private LinearLayout mTaxLinearLayout;
    private LinearLayout mTenderTypeLinearLayout;
    private LinearLayout mCashierLinearLayout;
    private LinearLayout mChangeLinearLayout;
    private TextView mTotalAmountTextView;
    private TextView mTotalChangeTextView;
    private TextView mVoidTextView;
    private TextView mReturnTextView;
    private TextView mDiscountTextView;
    private TextView mTipTextView;
    private Button mPrintButton;
    private Summary mSummary;
    int mSizeText = 15;
    private ProgressDialog mProgDialog;
    private View.OnClickListener mPrintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_report), true, false);
            new PrintOperation().execute("");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summarty_report, container, false);
        Log.e("Summary rep", "Summary rep");

        if (Utils.ResourceSize(getActivity()) == 0) {
            mSizeText = 18;
        } else {
            mSizeText = 22;
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
        setUpListeners();

        //refresh(fromD, toD);

        CallRefresh();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    private void bindUIElements(View v) {
        mDepartmentLinearLayout = (LinearLayout) v.findViewById(R.id.departments_linear_layout);
        mTaxLinearLayout = (LinearLayout) v.findViewById(R.id.tax_linear_layout);
        mTenderTypeLinearLayout = (LinearLayout) v.findViewById(R.id.tender_linear_layout);
        mCashierLinearLayout = (LinearLayout) v.findViewById(R.id.cashier_linear_layout);
        mTotalAmountTextView = (TextView) v.findViewById(R.id.total_amount_text_view);
        mChangeLinearLayout = (LinearLayout) v.findViewById(R.id.change_linear_layout);
        mTotalChangeTextView = (TextView) v.findViewById(R.id.change_amount_text_view);
        mPrintButton = (Button) v.findViewById(R.id.print_button);
        mVoidTextView = (TextView) v.findViewById(R.id.void_total_amount_text_view);
        mReturnTextView = (TextView) v.findViewById(R.id.return_total_amount_text_view);
        mDiscountTextView = (TextView) v.findViewById(R.id.discount_amount_text_view);
        mTipTextView = (TextView) v.findViewById(R.id.tip_total_amount_text_view);
    }

    private void setUpListeners() {
        mPrintButton.setOnClickListener(mPrintClickListener);
    }

    private void setUpDates() {
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


    public void CallRefresh() {
        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                refresh(fromD, toD);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                    }
                });
            }
        });
        thread.start();
    }
    public void refresh(long l, long m) {
        mCarts.clear();
        mCarts = mDb.getReports1(l, m);
        mSummary = new Summary(mActivity);
        mSummary.refresh(mCarts);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayReport();
            }
        });
    }

    private void displayReport() {

        //mTotalAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mSummary.total.divide(Consts.HUNDRED)));
        mTotalAmountTextView.setText(Utils.formatCartTotal(mSummary.total));

        mDepartmentLinearLayout.removeAllViews();

        int dp = com.pos.passport.ui.Utils.setPadding(mActivity, 10);
        for (ItemsSold item : mSummary.departmentsList) {
            LinearLayout linearLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(mActivity);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(WordUtils.capitalize(item.getName()));
            linearLayout.addView(nameTextView);

            TextView priceTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            priceTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            priceTextView.setGravity(Gravity.END);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            // priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
            linearLayout.addView(priceTextView);

            mDepartmentLinearLayout.addView(linearLayout);
        }

        mTaxLinearLayout.removeAllViews();
        for (ItemsSold item : mSummary.taxList) {
            LinearLayout linearLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(mActivity);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(item.getName());
            linearLayout.addView(nameTextView);

            TextView priceTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            priceTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            priceTextView.setGravity(Gravity.END);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            priceTextView.setText(Utils.formatCartTotal(item.getPrice()));

            linearLayout.addView(priceTextView);

            mTaxLinearLayout.addView(linearLayout);
        }

        mTenderTypeLinearLayout.removeAllViews();
        for (ItemsSold item : mSummary.tendersList) {
            LinearLayout linearLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(mActivity);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(item.getName());
            linearLayout.addView(nameTextView);

            TextView priceTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            priceTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            priceTextView.setGravity(Gravity.END);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
            linearLayout.addView(priceTextView);

            mTenderTypeLinearLayout.addView(linearLayout);
        }

        mCashierLinearLayout.removeAllViews();
        for (ItemsSold item : mSummary.cashierList) {
            LinearLayout linearLayout = new LinearLayout(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTextView = new TextView(mActivity);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            nameTextView.setText(WordUtils.capitalize(item.getName()));
            linearLayout.addView(nameTextView);

            TextView priceTextView = new TextView(mActivity);
            p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            priceTextView.setLayoutParams(p);
            nameTextView.setPadding(dp, dp, dp, dp);
            priceTextView.setGravity(Gravity.END);
            priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
            //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
            priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
            linearLayout.addView(priceTextView);

            mCashierLinearLayout.addView(linearLayout);
        }

        if (mSummary.changeTotal.compareTo(mSummary.total) > 0) {
            //mChangeLinearLayout.setVisibility(View.VISIBLE);
            //mTotalChangeTextView.setText(DecimalFormat.getCurrencyInstance().format(mSummary.changeTotal.subtract(mSummary.total).divide(Consts.HUNDRED)));
            mTotalChangeTextView.setText(Utils.formatCartTotal(mSummary.changeTotal.subtract(mSummary.total)));
        } else {
            mChangeLinearLayout.setVisibility(View.GONE);
        }
        mVoidTextView.setText(Utils.formatCartTotal(mSummary.voidTotal));
        mDiscountTextView.setText(Utils.formatCartTotal(mSummary.discountTotal));
        mReturnTextView.setText(Utils.formatCartTotal(mSummary.returnTotal));
        mTipTextView.setText(Utils.formatCartTotal(mSummary.tipAmountTotal));


    }

    private class PrintOperation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

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

                        if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
                            cols = 30;

                        Log.v("Printer Address", ReceiptSetting.address);

                        EscPosDriver.print(getActivity(), ReceiptHelper.summaryReport(getActivity(), cols, mSummary), ReceiptSetting.drawer);
                        return null;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String arrayPortName) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }

}
