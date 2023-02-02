package com.pos.passport.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.EmailSetting;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;

public class HistoryFragment extends Fragment implements Runnable {

    private static CustomPrinter prnDevice;
    private ArrayList<ReportCart> carts;
    private ReportCart resendEmailCart;
    //private NumberFormat nf = NumberFormat.getInstance();
    private DecimalFormat nf;
    private TableLayout tl;
    protected ProgressDialog pd;
    private int todo;
    private long fromD;
    private long toD;
    private static String lock="lockAccess";
    private ProductDatabase mDb;
    private View mRootView;
    private Typeface mNotoSans;
    private Typeface mNotoSansBold;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Fragment", "History Fragment");
        nf = new DecimalFormat("0.00");
        nf.setRoundingMode(RoundingMode.HALF_UP);
        nf.setGroupingUsed(false);

        mNotoSans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf");
        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buildReport(fromD, toD);
    }

    @Override
    public void onResume() {
        Log.v("History", "Resumed");
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.reportsview, container, false);
        tl = (TableLayout) mRootView.findViewById(R.id.report);
        tl.setPadding(10, 10, 10, 10);
        mDb = ProductDatabase.getInstance(getActivity());
        return mRootView;
    }

    protected void buildReport(long l, long m) {
        carts = mDb.getReports1(l, m);

        tl.removeAllViews();
        if (carts.size() > 0) {

            for (int i = 0; i < carts.size(); i++) {

                TableRow row = new TableRow(getActivity());
                tl.addView(row);
                row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                row.setPadding(0, 5, 0, 0);

                final TextView header = new TextView(getActivity());
                TextView tv2 = new TextView(getActivity());
                TextView tv3 = new TextView(getActivity());

                long date = carts.get(i).getDate();

                String dateString = DateFormat.getDateTimeInstance().format(new Date(date));

                if (carts.get(i).isReceive && carts.get(i).mVoided)
                    header.setText(dateString + " - " + getString(R.string.txt_receiving_cap) + " - " + getString(R.string.txt_voided_cap));
                else if (carts.get(i).mVoided)
                    header.setText(dateString + " - " + getString(R.string.txt_voided_cap));
                else if (carts.get(i).isReceive)
                    header.setText(dateString + " - " + getString(R.string.txt_receiving_cap));
                else
                    header.setText(dateString);

                header.setSingleLine(true);

                header.setGravity(Gravity.START);
                header.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                header.setTypeface(mNotoSansBold, Typeface.BOLD);
                header.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.7f));
                row.addView(header);

                tv2.setText(carts.get(i).getCustomerName());
                tv2.setSingleLine(true);

                tv2.setGravity(Gravity.END);
                tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                tv2.setTypeface(mNotoSansBold, Typeface.BOLD);
                tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
                row.addView(tv2);


                tv3.setText(String.format(getString(R.string.txt_trans_no), carts.get(i).trans));
                tv3.setGravity(Gravity.END);
                tv3.setSingleLine(true);

                tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                tv3.setTypeface(mNotoSansBold, Typeface.BOLD);
                tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
                row.addView(tv3);
                TextView tv1;
                if(carts.get(i).mCashier != null) {
                    row = new TableRow(getActivity());
                    row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                    tl.addView(row);

                    tv1 = new TextView(getActivity());
                    tv2 = new TextView(getActivity());
                    tv3 = new TextView(getActivity());

                    tv1.setText(getString(R.string.txt_cashier_label) + " " + carts.get(i).mCashier.name);
                    tv1.setSingleLine(true);
                    tv1.setGravity(Gravity.START);
                    tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv1.setTypeface(mNotoSans);
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .5f));
                    row.addView(tv1);

                    tv2.setText("");
                    tv2.setGravity(Gravity.END);
                    tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv2.setTypeface(mNotoSans);
                    tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv2);

                    tv3.setText("");
                    tv3.setGravity(Gravity.END);
                    tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv3.setTypeface(mNotoSans);
                    tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));

                    row.addView(tv3);
                }
                BigDecimal nonDiscountTotal = BigDecimal.ZERO;

                for (int o = 0; o < carts.get(i).getProducts().size(); o++) {
                    row = new TableRow(getActivity());
                    row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                    row.setPadding(8, 0, 0, 0);
                    tl.addView(row);

                    tv1 = new TextView(getActivity());
                    tv2 = new TextView(getActivity());
                    tv3 = new TextView(getActivity());

                    tv1.setText("  " + carts.get(i).getProducts().get(o).name);
                    tv1.setSingleLine(true);
                    tv1.setGravity(Gravity.START);
                    tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv1.setTypeface(mNotoSans);
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .7f));
                    row.addView(tv1);

                    Product prod = carts.get(i).getProducts().get(o);
                    nonDiscountTotal = nonDiscountTotal.add(prod.itemNonDiscountTotal(carts.get(i).mDate));

                    if(!carts.get(i).getProducts().get(o).isNote)
                        tv2.setText(" " + prod.quantity + " @ " + prod.displayPrice(carts.get(i).mDate));
                    else
                        tv2.setText("");

                    tv2.setGravity(Gravity.END);
                    tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv2.setTypeface(mNotoSans);
                    tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv2);

                    if(!carts.get(i).getProducts().get(o).isNote)
                        tv3.setText(prod.displayTotal(carts.get(i).mDate));
                    else
                        tv3.setText("");

                    tv3.setGravity(Gravity.END);
                    tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv3.setTypeface(mNotoSans);
                    tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv3);

                    if (!carts.get(i).getProducts().get(o).barcode.equals("")) {
                        tv2.setText("");
                        tv3.setText("");

                        row = new TableRow(getActivity());
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);
                        tl.addView(row);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(" "+carts.get(i).getProducts().get(o).barcode);
                        tv1.setSingleLine(true);
                        tv1.setGravity(Gravity.START);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .7f));
                        row.addView(tv1);

                        tv2.setText(" " + prod.quantity + " @ " + prod.displayPrice(carts.get(i).mDate));
                        tv2.setGravity(Gravity.END);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv2);

                        tv3.setText(prod.displayTotal(carts.get(i).mDate));
                        tv3.setGravity(Gravity.END);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv3);
                    }
                }

                if(!carts.get(i).isReceive) {
                    if (carts.get(i).mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        row = new TableRow(getActivity());
                        tl.addView(row);
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(R.string.txt_discount_label);
                        tv1.setGravity(Gravity.START);
                        tv1.setSingleLine(true);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                        row.addView(tv1);

                        tv2.setText("" + carts.get(i).mSubtotalDiscount + "%");
                        tv2.setGravity(Gravity.END);
                        tv2.setSingleLine(true);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv2);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(carts.get(i).mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setSingleLine(true);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv3);
                    }

                    if (carts.get(i).getTax1Name() != null) {
                        row = new TableRow(getActivity());
                        tl.addView(row);
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax1Name());
                        tv1.setGravity(Gravity.START);
                        tv1.setSingleLine(true);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                        row.addView(tv1);

                        tv2.setText("" + carts.get(i).getTax1Percent() + "%");
                        tv2.setGravity(Gravity.END);
                        tv2.setSingleLine(true);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv2);

                        BigDecimal price = carts.get(i).mTax1.divide(Consts.HUNDRED);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setSingleLine(true);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv3);
                    }

                    if (carts.get(i).getTax2Name() != null) {
                        row = new TableRow(getActivity());
                        tl.addView(row);
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax2Name());
                        tv1.setGravity(Gravity.START);
                        tv1.setSingleLine(true);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                        row.addView(tv1);

                        tv2.setText("" + carts.get(i).getTax2Percent() +"%");
                        tv2.setGravity(Gravity.END);
                        tv2.setSingleLine(true);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv2);

                        BigDecimal price = carts.get(i).mTax2.divide(Consts.HUNDRED);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setSingleLine(true);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv3);
                    }

                    if (carts.get(i).getTax3Name() != null) {
                        row = new TableRow(getActivity());
                        tl.addView(row);
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax3Name());
                        tv1.setGravity(Gravity.END);
                        tv1.setSingleLine(true);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                        row.addView(tv1);

                        tv2.setText("" + carts.get(i).getTax3Percent() +"%");
                        tv2.setGravity(Gravity.END);
                        tv2.setSingleLine(true);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv2);

                        BigDecimal price = carts.get(i).mTax3.divide(Consts.HUNDRED);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setSingleLine(true);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                        row.addView(tv3);
                    }

                    row = new TableRow(getActivity());
                    row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                    row.setPadding(8, 0, 0, 6);
                    tl.addView(row);

                    tv1 = new TextView(getActivity());
                    tv2 = new TextView(getActivity());
                    tv3 = new TextView(getActivity());

                    tv1.setText(R.string.txt_total);

                    tv1.setGravity(Gravity.START);
                    tv1.setSingleLine(true);
                    tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv1.setTypeface(mNotoSans, Typeface.BOLD);
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                    row.addView(tv1);

                    tv2.setText("");
                    tv2.setSingleLine(true);
                    tv2.setGravity(Gravity.END);
                    tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv2.setTypeface(mNotoSans);
                    tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv2);

                    BigDecimal price = carts.get(i).mTotal.divide(Consts.HUNDRED);
                    tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
                    tv3.setGravity(Gravity.END);
                    tv3.setSingleLine(true);
                    tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                    tv3.setTypeface(mNotoSans, Typeface.BOLD);
                    tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                    row.addView(tv3);

                    BigDecimal paymentSum = BigDecimal.ZERO;
                    ArrayList<Payment> payments = carts.get(i).mPayments;

                    for(int p = 0; p < carts.get(i).mPayments.size(); p++) {
                        row = new TableRow(getActivity());
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 0);
                        tl.addView(row);

                        paymentSum = paymentSum.add(payments.get(p).paymentAmount);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(R.string.txt_tendered_type);
                        tv1.setGravity(Gravity.START);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
                        row.addView(tv1);

                        tv2.setText(payments.get(p).paymentType);
                        tv2.setGravity(Gravity.END);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
                        row.addView(tv2);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(payments.get(p).paymentAmount.divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
                        row.addView(tv3);
                    }

                    if (paymentSum.compareTo(carts.get(i).mTotal) > 0) {
                        row = new TableRow(getActivity());
                        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                        row.setPadding(8, 0, 0, 6);
                        tl.addView(row);

                        tv1 = new TextView(getActivity());
                        tv2 = new TextView(getActivity());
                        tv3 = new TextView(getActivity());

                        tv1.setText(R.string.txt_customer_change);
                        tv1.setGravity(Gravity.START);
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv1.setTypeface(mNotoSans);
                        tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
                        row.addView(tv1);

                        tv2.setText("");
                        tv2.setGravity(Gravity.END);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv2.setTypeface(mNotoSans);
                        tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
                        row.addView(tv2);

                        tv3.setText(DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(carts.get(i).mTotal).divide(Consts.HUNDRED)));
                        tv3.setGravity(Gravity.END);
                        tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                        tv3.setTypeface(mNotoSans);
                        tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
                        row.addView(tv3);
                    }
                }
                //if(ReceiptSetting.isEnabled() || EmailSetting.isEnabled()){
                row = new TableRow(getActivity());
                row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
                row.setPadding(8, 0, 0, 6);
                tl.addView(row);

                final TextView Vtv1 = new TextView(getActivity());
                tv2 = new TextView(getActivity());
                tv3 = new TextView(getActivity());

                final ReportCart reprintCart = carts.get(i);
                final int index = i;
                if (!reprintCart.mVoided) {
                    String htmlString="<u>Void Sale?</u>";
                    Vtv1.setText(Html.fromHtml(htmlString));
                } else {
                    String htmlString="<u>Unvoid Sale?</u>";
                    Vtv1.setText(Html.fromHtml(htmlString));
                }

                Vtv1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder2;
                        final AlertDialog alertDialog2;

                        LayoutInflater inflater2 = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                        final View mylayout = inflater2.inflate(R.layout.adminpass, (ViewGroup) getActivity().findViewById(R.id.adminpassmain));

                        final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
                        final TextView text = (TextView) mylayout.findViewById(R.id.textView1);

                        text.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
                        String title = "";
                        String button = "";
                        nameEdit.setVisibility(View.GONE);
                        if (!reprintCart.mVoided) {
                            text.setText(R.string.msg_voiding_a_sale);
                            title = getString(R.string.txt_void_sale_question_mark);
                            button = getString(R.string.txt_void_sale);
                        } else {
                            text.setText(R.string.msg_unvoiding_a_sale);
                            title = getString(R.string.txt_unvoid_sale_question_mark);
                            button = getString(R.string.txt_unvoid_sale);
                        }
                        builder2 = new AlertDialog.Builder(getActivity());
                        builder2.setView(mylayout)
                            .setTitle(title)
                                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (!reprintCart.mVoided) {
                                            reprintCart.mVoided = true;
                                            mDb.replaceSale(reprintCart);
                                            carts.set(index, reprintCart);
                                            String htmlString = "<u>Unvoid Sale?</u>";
                                            Vtv1.setText(Html.fromHtml(htmlString));
                                            long datef = reprintCart.getDate();
                                            String dateString = DateFormat.getDateTimeInstance().format(new Date(datef));
                                            header.setText(dateString + " - " + getString(R.string.txt_voided_cap));
                                        } else {
                                            reprintCart.mVoided = false;
                                            mDb.replaceSale(reprintCart);
                                            carts.set(index, reprintCart);
                                            String htmlString = "<u>Void Sale?</u>";
                                            Vtv1.setText(Html.fromHtml(htmlString));
                                            long datef = reprintCart.getDate();
                                            String dateString = DateFormat.getDateTimeInstance().format(new Date(datef));
                                            header.setText(dateString);
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        alertDialog2 = builder2.create();
                        alertDialog2.show();
                    }
                });

                Vtv1.setGravity(Gravity.START);
                Vtv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                Vtv1.setTypeface(mNotoSansBold);
                Vtv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
                row.addView(Vtv1);


                if (EmailSetting.isEnabled() && !reprintCart.getCustomerEmail().equals("")) {
                    String htmlString="<u>Resend Email</u>";
                    tv2.setTextColor(Color.BLUE);
                    tv2.setText(Html.fromHtml(htmlString));
                    tv2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            todo = 1;
                            resendEmailCart = reprintCart;
                            pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_sending_email), true, false);
                            Thread thread = new Thread(HistoryFragment.this);
                            thread.start();
                        }
                    });
                } else {
                    tv2.setText("");
                }

                tv2.setGravity(Gravity.END);
                tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                tv2.setTypeface(mNotoSans);
                tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                row.addView(tv2);

                if(ReceiptSetting.enabled){
                    String htmlString="<u>Reprint</u>";
                    tv3.setTextColor(Color.BLUE);
                    tv3.setText(Html.fromHtml(htmlString));
                    tv3.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            todo = 2;
                            resendEmailCart = reprintCart;
                            pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_receipt), true, false);
                            Thread thread = new Thread(HistoryFragment.this);
                            thread.start();
                        }
                    });
                } else {
                    tv3.setText("");
                }
                tv3.setGravity(Gravity.END);
                tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
                tv3.setTypeface(mNotoSans);
                tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
                row.addView(tv3);

                addLine(tl);
                //}
            }
        }
    }

    public void setDates(long l, long m) {
        this.fromD = l;
        this.toD = m;
    }

    public void clearReport() {
        tl.removeAllViews();
    }

    void exportReport() {
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;

        String dateString = DateFormat.getDateInstance().format(new Date(this.fromD)) +" - " + DateFormat.getDateInstance().format(new Date(this.toD));

        builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getString(R.string.msg_save_history_report_with_date_range), dateString))
            .setTitle(R.string.txt_save_history_report)
            .setPositiveButton(R.string.txt_save_report, new DialogInterface.OnClickListener() {
                private String name;

                public void onClick(DialogInterface dialog, int id) {
                    exportrpt(carts, name);
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        alertDialog = builder.create();
        alertDialog.show();
    }

    protected void exportrpt(ArrayList<ReportCart> cart, String filename) {

        StringBuilder exportString = new StringBuilder();

        if(cart.size() > 0){
            long date1 = carts.get(0).getDate();
            long date2 = carts.get(carts.size()-1).getDate();

            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
            String date1string = df.format(new Date(date1));
            String date2string = df.format(new Date(date2));

            exportString.append("\"Date Range:\",\"" + date1string + "\",\"" + date2string +"\"\n");

            for(int i=0;i<cart.size();i++){
                ReportCart tempCart = cart.get(i);
                long date = tempCart.getDate();

                String dateString = DateFormat.getDateTimeInstance().format(new Date(date));


                exportString.append("\n\"Transaction: "+tempCart.trans+"\",\"").append(dateString+"\",\"").append(tempCart.getCustomerName()+"\"\n");
                if(tempCart.mCashier != null)
                    exportString.append("\"Cashier:\",\""+tempCart.mCashier.name+"\"\n");
                exportString.append("\"Name\",\"").append("Quantity\",\"").append("Price\",\"").append("Total Price\"\n");

                BigDecimal nonDiscountTotal = BigDecimal.ZERO;
                for(int o = 0; o < tempCart.getProducts().size(); o++){
                    Product tempProd = tempCart.getProducts().get(o);

                    //int price = tempProd.itemPrice();
                    //int total = tempProd.itemTotal();
                    nonDiscountTotal = nonDiscountTotal.add(tempProd.itemNonDiscountTotal(tempCart.mDate));

                    exportString.append("\""+tempProd.name+"\",\"").append(tempProd.quantity+"\",\"")
                            .append("@ "+tempProd.displayPrice(tempCart.mDate)+"\",\"")
                            .append(tempProd.displayTotal(tempCart.mDate)+"\"\n");
                }

                if (carts.get(i).mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    exportString.append("\"Discount:\",,\"").append(carts.get(i).mSubtotalDiscount+"%\",\"").append(
                            DecimalFormat.getCurrencyInstance().format(carts.get(i).mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED)) + "\"\n");
                }

                if (carts.get(i).getTax1Name() != null) {
                    exportString.append("\"TAX: "+ tempCart.getTax1Name()+"\",,\"").append(tempCart.getTax1Percent()+"%\",\"").append(
                            DecimalFormat.getCurrencyInstance().format(tempCart.mTax1.divide(Consts.HUNDRED)) + "\"\n");
                }

                if (carts.get(i).getTax2Name() != null) {
                    exportString.append("\"TAX: "+ tempCart.getTax2Name()+"\",,\"").append(tempCart.getTax2Percent()+"%\",\"").append(
                            DecimalFormat.getCurrencyInstance().format(tempCart.mTax2.divide(Consts.HUNDRED)) + "\"\n");
                }

                if (carts.get(i).getTax3Name() != null) {
                    exportString.append("\"TAX: "+ tempCart.getTax3Name()+"\",,\"").append(tempCart.getTax3Percent()+"%\",\"").append(
                            DecimalFormat.getCurrencyInstance().format(tempCart.mTax3.divide(Consts.HUNDRED)) + "\"\n");
                }

                BigDecimal paymentSum = BigDecimal.ZERO;
                ArrayList<Payment> payments = carts.get(i).mPayments;
                for (int p = 0; p < payments.size(); p++) {
                    paymentSum = paymentSum.add(payments.get(p).paymentAmount);
                    exportString.append("\"Tender Type\",,\""+ payments.get(p).paymentType+"\",\"").append(
                        DecimalFormat.getCurrencyInstance().format(payments.get(p).paymentAmount.divide(Consts.HUNDRED)) + "\"\n");
                }

                if (paymentSum.compareTo(carts.get(i).mTotal) > 0) {
                    exportString.append("\"Customer Change\",,,\"").append(
                        DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(carts.get(i).mTotal).divide(Consts.HUNDRED)) + "\"\n");
                }

                if (!tempCart.mVoided)
                    exportString.append("\"Total\",,,\"").append(
                        DecimalFormat.getCurrencyInstance().format(tempCart.mTotal.divide(Consts.HUNDRED)) + "\"\n");
                else
                    exportString.append("\"Total\",,\"VOIDED\",\"").append(
                        DecimalFormat.getCurrencyInstance().format(tempCart.mTotal.divide(Consts.HUNDRED)) + "\"\n");
            }
        } else {
            Utils.alertBox(getActivity(), R.string.txt_save_error, R.string.msg_no_data_to_save);
            return;
        }

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/AdvantagePOS/Reports/HistoryReports");

        dir.mkdirs();

        Date date1=new Date(fromD);
        Date date2=new Date(toD);

        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy");
        String dateText = df2.format(date1)+"-"+df2.format(date2);

        File saveFile = new File(sd, "/AdvantagePOS/Reports/HistoryReports/History_" +dateText+".csv");

        try {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), "UTF8"));
            out.append(exportString.toString());

            out.flush();
            out.close();
            Utils.alertBox(getActivity(), R.string.txt_save_success, R.string.msg_report_saved);
        } catch (Exception ex) {
            Utils.alertBox(getActivity(), R.string.txt_save_failed, R.string.msg_report_save_failed);
            ex.printStackTrace();
        }
        
		/*try {
			
			writer = new FileWriter(saveFile);
	        writer.append(exportString.toString());
	        writer.flush();
	        writer.close();
	        alertBox("Save Success", "Report Saved Successfully.");
		} catch (IOException e) {
	        
			e.printStackTrace();
		}*/

    }

    private boolean print(Bitmap receiptImage) {
        if (ReceiptSetting.type == ReceiptSetting.TYPE_LAN) {
            EscPosDriver.printReceipt(getActivity(), resendEmailCart);
            return true;
        } else if(ReceiptSetting.type == ReceiptSetting.TYPE_BT) {
            EscPosDriver.printReceipt(getActivity(), resendEmailCart);
            return true;
        } else if(ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
            EscPosDriver.printReceipt(getActivity(), resendEmailCart);
            return true;
        }

//		String portName = ReceiptSetting.address;
//		String portSettings = "";
//		
//		if(!hasInternet())
//		{
//			if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
//			{
//				if (prnDevice != null) {
//					prnDevice = null;
//				}
//			}
//			return false;
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_STAR)
//		{
//			if(ReceiptSetting.address.contains("TCP"))
//			{	
//		        try {
//					if(!InetAddress.getByName(ReceiptSetting.address.substring(4)).isReachable(10))
//					{
//			    		Log.v("Not Open", "Not Open");
//			    		return false;
//					}
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			if (portSettings.toUpperCase().equals("MINI")) {
//				MiniPrinterFunctions.PrintBitmap(getActivity(), portName, portSettings,
//						receiptImage, 576, false, false);
//			} else {
//				return PrinterFunctions.PrintBitmap(getActivity(), portName, portSettings,
//						receiptImage, 576, false, false);
//			}
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_EPSON)
//		{
//			if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
//			{ 
//				try {
//					if(!InetAddress.getByName(ReceiptSetting.address).isReachable(3000))
//					{
//			    		return false;
//					}
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			
//	        EscPosDriver.PrintReceipt(resendEmailCart);
//	        return true;
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
//		{
//	        try {
//				if(!InetAddress.getByName(ReceiptSetting.address).isReachable(10))
//				{
//		    		Log.v("Not Open", "Not Open");
//		    		return false;
//				}
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//	        
//			if(OpenDevice())
//			{
//				Log.v("Custom", "Found Device");
//				synchronized (lock) 
//				{
//			    	try
//			        {
//					    int[] arrayOfInt = { 16, 20, 1, 3, 5 };
//						Log.v("Custom", "Try Print");
//
//			    		prnDevice.printImage(receiptImage,CustomPrinter.IMAGE_ALIGN_TO_CENTER, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
//						prnDevice.feed(3);
//			    		prnDevice.cut(CustomPrinter.CUT_TOTAL);	 
//			    		//prnDevice.present(40);
//				    	prnDevice.writeData(arrayOfInt);
//				    	return true;
//			        }
//			    	catch(CustomException e )
//		            {            	
//			    		Log.e("CA ERROR", e.getMessage());
//			    		e.printStackTrace();
//			    		return false;
//		            }
//					catch(Exception e ) 
//			        {
//			    		e.printStackTrace();
//				    	return false;
//			        }
//				}
//			}
//		}


        //if(portSettings.toUpperCase().equals("MINI"))
        //{
        //	MiniPrinterFunctions.PrintBitmap(getActivity(), portName, portSettings, receiptImage, 576, false, false);
        //}
        //else
        //{
        //	PrinterFunctions.PrintBitmap(getActivity(), portName, portSettings, receiptImage, 576, false, false);
        //}
        return false;
    }

    /*private void issueEmailReceipt(ReportCart cart) {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (EmailSetting.isEnabled()) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting()) {
                if (cart.hasCustomer()) {
                    String email = cart.getCustomerEmail();
                    if (email.contains("@")) {
                        Mail m = new Mail(EmailSetting.getSmtpUsername(), EmailSetting.getSmtpPasword());
                        m.setServer(EmailSetting.getSmtpServer(), EmailSetting.getSmtpPort());
                        m.setSubject(EmailSetting.getSmtpSubject());
                        m.setBody(cart.getBody());
                        m.setTo(cart.getCustomerEmail());
                        m.setFrom(EmailSetting.getSmtpEmail());

                        try {
                            if (m.send()) {
                                Message m2 = new Message();
                                m2.what = 9;
                                handler.sendMessage(m2);
                            } else {
                                Message m2 = new Message();
                                m2.what = 8;
                                handler.sendMessage(m2);

                            }
                        } catch (Exception e) {
                            Log.e("MailApp", "Could not send email", e);
                            Message m2 = new Message();
                            m2.what = 8;
                            handler.sendMessage(m2);
                        }
                    }
                }
            } else {
                if (cart.hasCustomer()) {
                    String email = cart.getCustomerEmail();
                    if (email.contains("@")) {
                        Message m2 = new Message();
                        m2.what = 8;
                        handler.sendMessage(m2);
                    }
                }
            }
        }
    }
*/
    @Override
    public void run() {
        if(todo == 2){
            if(print(resendEmailCart.getReceipt(getActivity()))){
                Message m = new Message();
                m.what = 11;
                handler.sendMessage(m);
            } else {
                Message m = new Message();
                m.what = 12;
                handler.sendMessage(m);
            }
        }
    }

    private void addLine(TableLayout tl) {
        TableRow line = new TableRow(getActivity());

        tl.addView(line);
        LayoutParams layoutParams = new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        line.setLayoutParams(layoutParams);
        line.setPadding(5, 0, 5, 1);
        line.setBackgroundColor(Color.BLACK);
    }

    public static boolean OpenDevice() {

        if (prnDevice == null) {
            try {
                prnDevice = new CustomAndroidAPI().getPrinterDriverETH(ReceiptSetting.address);
                return true;
            } catch (CustomException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.v("Custom", "Already Open: " + prnDevice.getPrinterName());
            return true;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                pd.dismiss();
            } else if (msg.what == 9) {
                Toast.makeText(getActivity(), R.string.msg_receipt_sent_successfully, Toast.LENGTH_LONG).show();
            } else if (msg.what == 8) {
                Toast.makeText(getActivity(), R.string.msg_receipt_not_sent, Toast.LENGTH_LONG).show();
            }
            else if (msg.what == 11) {
                pd.dismiss();
            }
            else if (msg.what == 12) {
                Toast.makeText(getActivity(), R.string.msg_receipt_not_sent_check_printer_settings, Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        }
    };
}
