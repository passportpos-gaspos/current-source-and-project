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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.Shift;
import com.pos.passport.model.Summary;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.ReceiptHelper;
import com.pos.passport.util.Utils;

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
public class ShiftReportFragment extends Fragment {

    private final String DEBUG_TAG = "[ShiftReportFragment]";
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private FragmentActivity mActivity;
    private ProductDatabase mDb;
    private ArrayList<ReportCart> mCarts;
    private Typeface mNotoSans;
    private Typeface mNotoSansBold;

    private long fromD;
    private long toD;

    private ArrayList<Shift> mShifts;
    private ListView mListView;
    private ShiftAdaptor mAdapter;
    private ProgressDialog mProgressDialog;
    //Context mcontext;
    int mSizeText=15;
    //private ProgressDialog prgDialog;

    //    public int ResourceSize()
//    {
//        return mcontext.getResources().getInteger(R.integer.popuptype);
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_list_view, container, false);
        //mcontext=getActivity();
        Log.e("Shiftreport","Shiftreport");
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
        setUpListView();
        SetUpdata();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    private void bindUIElements(View view) {
        mListView = (ListView)view.findViewById(R.id.list_view);
    }
//    public void ShowProgressDialog()
//    {
//        Log.e("Show prog","Show shirft");
//        prgDialog = new ProgressDialog(getActivity());
//        prgDialog.setMessage("Loading...");
//        prgDialog.setCancelable(false);
//        prgDialog.show();
//    }
//    public void DismissDialog()
//    {
//        Log.e("Dismis prog","dismiss p");
//        prgDialog.dismiss();
//    }
    private void setUpListView()
    {
        mShifts = new ArrayList<>();
        mAdapter = new ShiftAdaptor();
        mListView.setAdapter(mAdapter);
        //refresh(fromD, toD);
    }
    public void SetUpdata()
    {
        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                mShifts = mDb.getShifts(fromD, toD);
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
    public void refresh(long l, long m){
        mShifts = mDb.getShifts(l, m);
        if(mShifts.size() <= 0){
            Utils.alertBox(mActivity,R.string.txt_shift_report,R.string.msg_shift_report_not_found_message);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setDates(long l, long m) {
        this.fromD = l;
        this.toD = m;
    }

    public void endShiftClick(){
        AlertDialogFragment fragment = AlertDialogFragment.getInstance(getActivity(), R.string.txt_process_end_of_shift, R.string.msg_process_end_of_shift, R.string.txt_yes, R.string.txt_cancel);
        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
        fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
            @Override
            public void ok()
            {
                if (mDb.tranAfterLastShift())
                {
                    mDb.saveShift();
                    refresh(fromD, toD);
                    if(mShifts.size()> 0)
                    {
                        Summary summary = (new Summary(getActivity()));
                        summary.refresh(mShifts.get(0).carts);
                        String dateString = DateFormat.getDateTimeInstance().format(new Date(mShifts.get(0).end));
                        final String header = String.format(getString(R.string.txt_shift_report_ending_at), dateString);
                        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_report), true, false);
                        new PrintOperation(summary,header).execute("");
                        return;
                    }else
                    {
                        Utils.alertBox(getContext(),R.string.txt_end_shift,R.string.msg_shift_report);
                    }
                }

                Utils.alertBox(getContext(),R.string.txt_end_shift,R.string.msg_shift_report);
            }

            @Override
            public void cancel() {}
        });
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

    private class ShiftAdaptor extends BaseAdapter{

        private LayoutInflater inflater;
        public ShiftAdaptor() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return mShifts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.fragment_shift_report, parent, false);
                Utils.setTypeFace(mNotoSans, (ViewGroup) convertView);
                holder = new ViewHolder();
                holder.shiftDateTextView = (TextView) convertView.findViewById(R.id.end_date_text_view);
                holder.departmentLinearLayout = (LinearLayout) convertView.findViewById(R.id.departments_linear_layout);
                holder.taxLinearLayout = (LinearLayout) convertView.findViewById(R.id.tax_linear_layout);
                holder.tenderTypeLinearLayout = (LinearLayout) convertView.findViewById(R.id.tender_linear_layout);
                holder.cashierLinearLayout = (LinearLayout) convertView.findViewById(R.id.cashier_linear_layout);
                holder.totalAmountTextView = (TextView) convertView.findViewById(R.id.total_amount_text_view);
                holder.changeLinearLayout = (LinearLayout) convertView.findViewById(R.id.change_linear_layout);
                holder.totalChangeTextView = (TextView) convertView.findViewById(R.id.change_amount_text_view);
                holder.printButton = (Button) convertView.findViewById(R.id.print_button);
                holder.voidAmountTextView = (TextView) convertView.findViewById(R.id.void_total_amount_text_view);
                holder.returnAmountTextView = (TextView) convertView.findViewById(R.id.return_total_amount_text_view);
                holder.discountAmountTextView = (TextView) convertView.findViewById(R.id.discount_amount_text_view);
                holder.tipAmountTextView = (TextView) convertView.findViewById(R.id.tip_total_amount_text_view);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            final Shift shift = mShifts.get(position);
            final Summary summary = new Summary(getActivity());
            summary.refresh(shift.carts);
            String dateString = DateFormat.getDateTimeInstance().format(new Date(shift.end));
            final String header = String.format(getString(R.string.txt_shift_report_ending_at), dateString);

            holder.shiftDateTextView.setText(header);
            //holder.totalAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(summary.total.divide(Consts.HUNDRED)));
            holder.totalAmountTextView.setText(Utils.formatCartTotal(summary.total));
            holder.departmentLinearLayout.removeAllViews();
            int dp = com.pos.passport.ui.Utils.setPadding(getActivity(), 5);
            for(ItemsSold item : summary.departmentsList){
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameTextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                nameTextView.setLayoutParams(p);
                nameTextView.setPadding(dp,dp,dp,dp);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                nameTextView.setText(item.getName());
                linearLayout.addView(nameTextView);

                TextView priceTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                priceTextView.setLayoutParams(p);
                nameTextView.setPadding(dp,dp,dp,dp);
                priceTextView.setGravity(Gravity.END);
                priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
                priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
                linearLayout.addView(priceTextView);

                holder.departmentLinearLayout.addView(linearLayout);
            }

            holder.taxLinearLayout.removeAllViews();
            for(ItemsSold item : summary.taxList){
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameTextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                nameTextView.setLayoutParams(p);
                nameTextView.setPadding(dp, dp, dp, dp);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                nameTextView.setText(item.getName());
                linearLayout.addView(nameTextView);

                TextView priceTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                priceTextView.setLayoutParams(p);
                nameTextView.setPadding(dp, dp, dp, dp);
                priceTextView.setGravity(Gravity.END);
                priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
               // priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
                priceTextView.setText(Utils.formatTotal(""+item.getPrice()));
                linearLayout.addView(priceTextView);
                holder.taxLinearLayout.addView(linearLayout);
            }

            holder.tenderTypeLinearLayout.removeAllViews();
            for(ItemsSold item : summary.tendersList){
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameTextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                nameTextView.setLayoutParams(p);
                nameTextView.setPadding(dp,dp,dp,dp);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                nameTextView.setText(item.getName());
                linearLayout.addView(nameTextView);

                TextView priceTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                priceTextView.setLayoutParams(p);
                nameTextView.setPadding(dp, dp, dp, dp);
                priceTextView.setGravity(Gravity.END);
                priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
                priceTextView.setText(Utils.formatTotal(""+item.getPrice()));


                linearLayout.addView(priceTextView);

                holder.tenderTypeLinearLayout.addView(linearLayout);
            }

            holder.cashierLinearLayout.removeAllViews();
            for(ItemsSold item : summary.cashierList){
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameTextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                nameTextView.setLayoutParams(p);
                nameTextView.setPadding(dp,dp,dp,dp);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                nameTextView.setText(item.getName());
                linearLayout.addView(nameTextView);

                TextView priceTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                priceTextView.setLayoutParams(p);
                nameTextView.setPadding(dp, dp, dp, dp);
                priceTextView.setGravity(Gravity.END);
                priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                //priceTextView.setText(DecimalFormat.getCurrencyInstance().format(item.getPrice().divide(Consts.HUNDRED)));
                priceTextView.setText(Utils.formatCartTotal(item.getPrice()));
                linearLayout.addView(priceTextView);

                holder.cashierLinearLayout.addView(linearLayout);
            }

            if(summary.changeTotal.compareTo(summary.total) > 0){
                //holder.changeLinearLayout.setVisibility(View.VISIBLE);
                //holder.totalChangeTextView.setText(DecimalFormat.getCurrencyInstance().format(summary.changeTotal.subtract(summary.total).divide(Consts.HUNDRED)));
                holder.totalChangeTextView.setText(Utils.formatCartTotal(summary.changeTotal.subtract(summary.total)));
            }else{
                holder.changeLinearLayout.setVisibility(View.GONE);
            }

            holder.printButton.setVisibility(View.VISIBLE);
            holder.printButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_report), true, false);
                    new PrintOperation(summary, header).execute("");
                }
            });

            //holder.voidAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(summary.voidTotal.divide(Consts.HUNDRED)));
            holder.voidAmountTextView.setText(Utils.formatCartTotal(summary.voidTotal));
            holder.returnAmountTextView.setText(Utils.formatCartTotal(summary.returnTotal));
            holder.tipAmountTextView.setText(Utils.formatCurrency(summary.tipAmountTotal));
            return convertView;
        }

        class ViewHolder{
            TextView shiftDateTextView;
            LinearLayout departmentLinearLayout;
            LinearLayout taxLinearLayout;
            LinearLayout tenderTypeLinearLayout;
            LinearLayout cashierLinearLayout;
            LinearLayout changeLinearLayout;
            TextView totalAmountTextView;
            TextView totalChangeTextView;
            Button printButton;
            TextView voidAmountTextView;
            TextView returnAmountTextView;
            TextView discountAmountTextView;
            TextView tipAmountTextView;
        }
    }

    private class PrintOperation extends AsyncTask<String, String, String> {

        private Summary mSummary;
        private String mHeader;

        public PrintOperation(Summary summary, String header){
            this.mSummary = summary;
        }

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

                        if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
                            cols = 30;

                        Log.v("Printer Address", ReceiptSetting.address);

                        EscPosDriver.print(getActivity(), ReceiptHelper.shiftEndReport(getActivity(), cols, mSummary, mHeader), ReceiptSetting.drawer);
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
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }
}
