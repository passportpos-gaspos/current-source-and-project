package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pos.passport.BuildConfig;
import com.pos.passport.R;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.gateway.BaseGateway;
import com.pos.passport.gateway.BridgePayGateway;
import com.pos.passport.gateway.ClearentGateway;
import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Payment;
import com.pos.passport.model.ProcessCreditCard;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.MessageHandler;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.ReceiptHelper;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by karim on 1/13/16.
 */
public class RecentTransactionListFragment extends Fragment{
    private final String DEBUG_TAG = "[RecentTransaction]";
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    private FragmentActivity mActivity;
    @IntDef({VOID, REFUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProcessType{};
    private static final int VOID = 1;
    private static final int REFUND = 2;

    private ListView mListView;
    private ProductDatabase mDb;
    private List<ReportCart> mCarts;
    private RecentTransactionAdapter mAdapter;

    private long fromD;
    private long toD;
    private Typeface mNotoSans;
    private Typeface mNotoSansBold;

    private ProgressDialog mProgressDialog;
    private MessageHandler mHandler;
    int mSizeText=15;
    int mSizeText_tax=15;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.view_list_view, container, false);
     Log.e("Rec Trans","Rec. Trans");
        if(Utils.ResourceSize(getActivity()) == 0)
        {
            mSizeText=18;
            mSizeText_tax=20;
        }else {
            mSizeText=22;
            mSizeText_tax=24;
        }
        mNotoSans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf");
        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        setUpDates();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        mCarts = new ArrayList<>();
        bindUIElements(view);
        setUpListView();
        setUpListeners();
        SetUpdata();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recent_transaction_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                refresh(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_search:
                Toast.makeText(getActivity(), "Search Test", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    private void bindUIElements(View view) {
        mListView = (ListView)view.findViewById(R.id.list_view);
    }

    private void setUpListView()
    {

        mAdapter = new RecentTransactionAdapter();
        mListView.setAdapter(mAdapter);
       // refresh(fromD,toD);
    }

    private void setUpListeners() {

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

    public void refresh(String transNo)
    {
        mCarts.clear();
        if (TextUtils.isEmpty(transNo))
            mCarts = mDb.getReports1(fromD, toD);
        else
            mCarts = mDb.getRecentTransactions(transNo);
        mAdapter.notifyDataSetChanged();
    }

    public void refresh(long l, long m){
        mCarts.clear();
        mCarts = mDb.getReports1(l,m);
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
                mCarts = mDb.getReports1(fromD, toD);

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

    public void clearReport() {

    }

    private void setProcessType(@ProcessType int type, ReportCart cart){
        String paymentType = "";
        String gatewayId = "";
        List<Payment> payments = cart.mPayments;
        if (payments != null) {
            for (int i = 0; i < payments.size(); i++) {
                Payment payment = payments.get(i);
                paymentType = payment.paymentType;
                gatewayId = payment.gatewayId;
            }
        }

        switch (type){
            case VOID:
                if (paymentType.equals(PayActivity.PAYMENT_TYPE_CREDIT)) {
                    processVoid(cart.id, gatewayId);
                }
                break;
            case REFUND:
                if (paymentType.equals(PayActivity.PAYMENT_TYPE_CREDIT)) {
                    //processRefund(cart.id, gatewayId, cart.mTotal.divide(Consts.HUNDRED).toString());
                    processRefund(cart.id, gatewayId, cart.mTotal.toString());
                }
                if(paymentType.equals(PayActivity.PAYMENT_TYPE_CASH) || paymentType.equals(PayActivity.PAYMENT_TYPE_CHECK)){
                    cart.mStatus = Cart.REFUND;
                    cart.mIsProcessed = ReportCart.PROCESS_STATUS_OFFLINE;
                    mDb.replaceSale(cart);
                }
                break;
        }
    }

    private void processRefund(String id, String gatewayId, String amount) {
        try {
            BaseGateway gateway = null;
            String payload = null;
            if (BuildConfig.GATEWAY.equals("clearent")) {
                gateway = new ClearentGateway(getActivity(), R.raw.gateway_clearent);
                gateway.setSandBoxEnabled(true);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", BaseGateway.TRANSACTION_REFUND);
                jsonObject.put("amount", amount);
                jsonObject.put("id", gatewayId);
                payload = jsonObject.toString();
            }else if (BuildConfig.GATEWAY.equals("bridgepay")) {
                gateway = new BridgePayGateway(getActivity(), R.raw.gateway_bridgepay);
                gateway.setSandBoxEnabled(true);
                ProcessCreditCard cProcess = new ProcessCreditCard();
                cProcess.setUserName(WebSetting.merchantID);
                cProcess.setTransType(BaseGateway.TRANSACTION_RETURN);
                cProcess.setPassword(WebSetting.webServicePassword);
                cProcess.setMerchantID(WebSetting.hostedMID);
                cProcess.setPNRef(gatewayId);
                payload = cProcess.generateXmlString();
            }

            if (gateway == null)
                return;
            Log.v(DEBUG_TAG, "payload: " + payload);
            gateway.processRefund(id, payload, new TransactionListener() {
                @Override
                public void onOffline() {
                    // do nothing
                }

                @Override
                public void onApproved(String id, String gatewayId, String payload) {
                    //mDb.updateSaleStatus(id, );
                    // update local database
                }

                @Override
                public void onError(String message) {
                    Utils.alertBox(getActivity(), R.string.txt_error, message);
                }

                @Override
                public void onDeclined(String message) {
                    Utils.alertBox(getActivity(), R.string.txt_declined, message);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
    }

    private void processVoid(String id, String gatewayId) {
        try {
            BaseGateway gateway = null;
            String payload = null;
            if (BuildConfig.GATEWAY.equals("clearent")) {
                gateway = new ClearentGateway(getActivity(), R.raw.gateway_clearent);
                gateway.setSandBoxEnabled(true);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", BaseGateway.TRANSACTION_VOID);
                jsonObject.put("id", gatewayId);
                payload = jsonObject.toString();
            }else if (BuildConfig.GATEWAY.equals("bridgepay")) {
                gateway = new BridgePayGateway(getActivity(), R.raw.gateway_bridgepay);
                gateway.setSandBoxEnabled(true);
                ProcessCreditCard cProcess = new ProcessCreditCard();
                cProcess.setUserName(WebSetting.merchantID);
                cProcess.setTransType("Void");
                cProcess.setPassword(WebSetting.webServicePassword);
                cProcess.setMerchantID(WebSetting.hostedMID);
                cProcess.setPNRef(gatewayId);
                payload = cProcess.generateXmlString();
            }

            if (gateway == null)
                return;
            Log.v(DEBUG_TAG, "payload: " + payload);
            gateway.processVoid(id, payload, new TransactionListener() {
                @Override
                public void onOffline () {
                    // do nothing
                }

                @Override
                public void onApproved (String id, String gatewayId, String payload){
                    Utils.alertBox(getActivity(), R.string.txt_void, "Success");
                }

                @Override
                public void onError (String message){
                    Utils.alertBox(getActivity(), R.string.txt_error, message);
                }

                @Override
                public void onDeclined (String message){
                    Utils.alertBox(getActivity(), R.string.txt_declined, message);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
    }

    private class RecentTransactionAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public RecentTransactionAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return mCarts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_recent_transactions_item, parent, false);
                Utils.setTypeFace(mNotoSans,(ViewGroup)convertView);
                holder = new ViewHolder();
                holder.rootRelativeLayout = (RelativeLayout)convertView.findViewById(R.id.root_relative_layout);
                holder.timeTextView = (TextView)convertView.findViewById(R.id.sale_time_text_view);
                holder.transactionNumberTextView = (TextView)convertView.findViewById(R.id.sale_transaction_number_text_view);
                holder.saleItemLinearLayout = (LinearLayout)convertView.findViewById(R.id.sale_items_linear_layout);
                holder.subTotalTextView = (TextView) convertView.findViewById(R.id.sale_subtotal_amount_text_view);
                holder.discountAmountTextView = (TextView) convertView.findViewById(R.id.sale_discount_amount_text_view);
                holder.discountLabelTextView = (TextView) convertView.findViewById(R.id.sale_discount_label_text_view);
                holder.taxLinearLayout = (LinearLayout) convertView.findViewById(R.id.sale_tax_linear_layout);
                holder.totalTextView = (TextView)convertView.findViewById(R.id.sale_total_amount_text_view);
                holder.paymentTypeTextView = (TextView)convertView.findViewById(R.id.sale_payment_type_text_view);
                holder.reprintButton = (Button)convertView.findViewById(R.id.reprint_button);
                holder.refundButton = (Button)convertView.findViewById(R.id.refund_button);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final ReportCart cart = mCarts.get(position);

            String dateString = DateFormat.getDateTimeInstance().format(new Date(cart.getDate()));
            if(cart.mStatus.equals(Cart.VOIDED))
                holder.timeTextView.setText(String.format(getString(R.string.msg_timestamp_voided), dateString));
            else if(cart.mStatus.equals(Cart.RETURNED))
                holder.timeTextView.setText(String.format(getString(R.string.msg_timestamp_returned), dateString));
            else {
                holder.timeTextView.setText(dateString);
                holder.timeTextView.setTypeface(mNotoSansBold, Typeface.BOLD);
                //holder.refundButton.setVisibility(View.VISIBLE);
            }
            holder.transactionNumberTextView.setText(String.format(getString(R.string.txt_trans_no), cart.trans));
            holder.transactionNumberTextView.setTypeface(mNotoSansBold, Typeface.BOLD);
           // holder.totalTextView.setText(DecimalFormat.getCurrencyInstance().format(cart.mTotal.divide(Consts.HUNDRED)));
            holder.totalTextView.setText(Utils.formatCartTotal(cart.mTotal));
            //holder.subTotalTextView.setText(DecimalFormat.getCurrencyInstance().format(cart.mSubtotal.divide(Consts.HUNDRED)));
            holder.subTotalTextView.setText(Utils.formatCartTotal(cart.mSubtotal));
            if(cart.mDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                holder.discountLabelTextView.setVisibility(View.VISIBLE);
                holder.discountAmountTextView.setVisibility(View.VISIBLE);
                holder.discountAmountTextView.setText(Utils.formatCartTotal(cart.mDiscountAmount));
                holder.discountLabelTextView.setText(cart.mDiscountName);
            }
            Log.e("payment size","size>>>"+cart.mPayments.size());
            if(!cart.mStatus.equals(Cart.VOIDED)) {
                if (cart.mPayments.size() > 1) {
                    holder.paymentTypeTextView.setText("Split");
                } else {
                    holder.paymentTypeTextView.setText(cart.mPayments.get(0).paymentType);
                }
            }else{
                holder.paymentTypeTextView.setText("");
            }
            holder.taxLinearLayout.removeAllViews();

            /*if(cart.mTax1.compareTo(BigDecimal.ZERO) > 0 )
            {
                LinearLayout tax1LinearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tax1LinearLayout.setLayoutParams(params);
                tax1LinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView tax1TextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tax1TextView.setLayoutParams(p);
                tax1TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax1TextView.setText(cart.mTax1Name);
                tax1TextView.setGravity(Gravity.START);
                tax1LinearLayout.addView(tax1TextView);

                TextView tax1AmountTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                tax1AmountTextView.setLayoutParams(p);
                tax1AmountTextView.setGravity(Gravity.END);
                tax1AmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax1AmountTextView.setText(DecimalFormat.getCurrencyInstance().format(cart.mTax1.divide(Consts.HUNDRED)));
                tax1LinearLayout.addView(tax1AmountTextView);
                holder.taxLinearLayout.addView(tax1LinearLayout);
                holder.taxLinearLayout.setVisibility(View.VISIBLE);
            }

            if(cart.mTax2.compareTo(BigDecimal.ZERO) > 0 ){
                LinearLayout tax2LinearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tax2LinearLayout.setLayoutParams(params);
                tax2LinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView tax2TextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tax2TextView.setLayoutParams(p);
                tax2TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax2TextView.setGravity(Gravity.START);
                tax2TextView.setText(cart.mTax2Name);
                tax2LinearLayout.addView(tax2TextView);

                TextView tax2AmountTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                tax2AmountTextView.setLayoutParams(p);
                tax2AmountTextView.setGravity(Gravity.END);
                tax2AmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax2AmountTextView.setText(DecimalFormat.getCurrencyInstance().format(cart.mTax2.divide(Consts.HUNDRED)));
                tax2LinearLayout.addView(tax2AmountTextView);
                holder.taxLinearLayout.addView(tax2LinearLayout);
                holder.taxLinearLayout.setVisibility(View.VISIBLE);
            }

            if(cart.mTax3.compareTo(BigDecimal.ZERO) > 0 ){
                LinearLayout tax3LinearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tax3LinearLayout.setLayoutParams(params);
                tax3LinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView tax3TextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tax3TextView.setLayoutParams(p);
                tax3TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax3TextView.setGravity(Gravity.START);
                tax3TextView.setText(cart.mTax3Name);
                tax3LinearLayout.addView(tax3TextView);

                TextView tax3AmountTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                tax3AmountTextView.setLayoutParams(p);
                tax3AmountTextView.setGravity(Gravity.END);
                tax3AmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tax3AmountTextView.setText(DecimalFormat.getCurrencyInstance().format(cart.mTax3.divide(Consts.HUNDRED)));
                tax3LinearLayout.addView(tax3AmountTextView);
                holder.taxLinearLayout.addView(tax3LinearLayout);
                holder.taxLinearLayout.setVisibility(View.VISIBLE);
            }*/
            try {


                JSONArray taxdata=cart.getTaxdata();
                for (int ti = 0; ti < taxdata.length(); ti++)
                {
                    JSONObject taxone=taxdata.getJSONObject(ti);
                    LinearLayout tax3LinearLayout = new LinearLayout(getActivity());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tax3LinearLayout.setLayoutParams(params);
                    tax3LinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView tax3TextView = new TextView(getActivity());
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    tax3TextView.setLayoutParams(p);
                    tax3TextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText_tax);
                    tax3TextView.setGravity(Gravity.START);
                    tax3TextView.setText(taxone.getString("name"));
                    tax3LinearLayout.addView(tax3TextView);

                    TextView tax3AmountTextView = new TextView(getActivity());
                    p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                    tax3AmountTextView.setLayoutParams(p);
                    tax3AmountTextView.setGravity(Gravity.END);
                    tax3AmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText_tax);
                    tax3AmountTextView.setText(""+Utils.formatCartTotal(new BigDecimal(taxone.getString("amount"))));//DecimalFormat.getCurrencyInstance().format(cart.mTax3.divide(Consts.HUNDRED)));
                    tax3LinearLayout.addView(tax3AmountTextView);
                    holder.taxLinearLayout.addView(tax3LinearLayout);
                    holder.taxLinearLayout.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){}
            holder.saleItemLinearLayout.removeAllViews();
            for (int i = 0; i < cart.mProducts.size(); i++) {
                Product product = cart.mProducts.get(i);
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);


                TextView nameTextView = new TextView(getActivity());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                nameTextView.setLayoutParams(p);
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                nameTextView.setText(product.name);
                linearLayout.addView(nameTextView);

                TextView quantityTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                quantityTextView.setLayoutParams(p);
                quantityTextView.setGravity(Gravity.END);
                quantityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                quantityTextView.setText(String.format("%d", product.quantity));
                linearLayout.addView(quantityTextView);

                TextView amountTextView = new TextView(getActivity());
                p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                amountTextView.setGravity(Gravity.END);
                amountTextView.setLayoutParams(p);
                amountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                //amountTextView.setText(Utils.formatCurrency(product.itemTotal(cart.mDate)));
                //Log.e("Item price","<Item price>"+product.itemTotal(cart.mDate));
                //amountTextView.setText(""+Utils.formatTotal(product.itemTotal(cart.mDate)));
                amountTextView.setText(""+Utils.formatTotal(product.total));
                linearLayout.addView(amountTextView);
                holder.saleItemLinearLayout.addView(linearLayout);

                if(product.discountAmount.compareTo(BigDecimal.ZERO) > 0){
                    LinearLayout discountLinearLayout = new LinearLayout(getActivity());
                    LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    discountLinearLayout.setLayoutParams(mParams);
                    discountLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView discountNameTextView = new TextView(getActivity());
                    LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    mp.setMargins(30, 0, 0, 0);
                    discountNameTextView.setLayoutParams(mp);
                    discountNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                    discountNameTextView.setText(product.discountName);
                    discountLinearLayout.addView(discountNameTextView);

                    TextView discountAmountTextView = new TextView(getActivity());
                    p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                    discountAmountTextView.setGravity(Gravity.END);
                    discountAmountTextView.setLayoutParams(p);
                    discountAmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                   // discountAmountTextView.setText(Utils.formatDiscount(product.discountAmount));
                    discountAmountTextView.setText(Utils.formatCartTotal(product.discountAmount));
                    discountLinearLayout.addView(discountAmountTextView);

                    holder.saleItemLinearLayout.addView(discountLinearLayout);
                }

                for(int j=0; j<product.modifiers.size() ; j++)
                {
                    LinearLayout modifierLinearLayout = new LinearLayout(getActivity());
                    LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    modifierLinearLayout.setLayoutParams(mParams);
                    modifierLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView modifierNameTextView = new TextView(getActivity());
                    LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    mp.setMargins(30, 0, 0, 0);
                    modifierNameTextView.setLayoutParams(mp);
                    modifierNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                    modifierNameTextView.setText(product.modifiers.get(j).name);
                    modifierLinearLayout.addView(modifierNameTextView);

                    TextView modifierAmountTextView = new TextView(getActivity());
                    p = new LinearLayout.LayoutParams(Utils.dpToPixel(getActivity(), 100), LinearLayout.LayoutParams.WRAP_CONTENT);
                    modifierAmountTextView.setGravity(Gravity.END);
                    modifierAmountTextView.setLayoutParams(p);
                    modifierAmountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSizeText);
                    //modifierAmountTextView.setText(Utils.formatCurrency(product.modifiers.get(j).price()));
                    modifierAmountTextView.setText(Utils.formatCartTotal(product.modifiers.get(j).price()));
                    modifierLinearLayout.addView(modifierAmountTextView);

                    holder.saleItemLinearLayout.addView(modifierLinearLayout);
                }

            }

            holder.reprintButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler = new MessageHandler(getActivity());
                    printReceipt(cart);
                }
            });
            holder.refundButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Answers.getInstance().logCustom(new CustomEvent("RecentTransaction")
                            .putCustomAttribute("button", "refund"));
                    if(PrefUtils.getCashierInfo(getActivity()).permissionReturn ){
                        setProcessType(REFUND, cart);
                        return;
                    }
                    TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                    fragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {
                        @Override
                        public void onAdminAccessGranted() {
                            setProcessType(REFUND, cart);
                        }

                        @Override
                        public void onAdminAccessDenied() {
                            Utils.alertBox(getActivity(), R.string.txt_admin_login, R.string.msg_admin_login_failed);
                        }
                    });
                }
            });

            return convertView;
        }

        class ViewHolder {
            RelativeLayout rootRelativeLayout;
            TextView timeTextView;
            TextView transactionNumberTextView;
            LinearLayout saleItemLinearLayout;
            TextView subTotalTextView;
            LinearLayout taxLinearLayout;
            TextView discountAmountTextView;
            TextView discountLabelTextView;
            TextView totalTextView;
            TextView paymentTypeTextView;
            Button reprintButton;
            Button refundButton;
        }
    }


    public void printReceipt(final ReportCart cart) {

        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_printing_receipt), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message mMessage = new Message();
                if(ReceiptHelper.print(getActivity(), cart, 0))
                    mMessage.what = MessageHandler.PRINT_SUCCESS;
                else
                    mMessage.what = MessageHandler.PRINT_FAILED;

                mHandler.sendMessage(mMessage);
                getActivity().runOnUiThread(new Runnable() {
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
}
