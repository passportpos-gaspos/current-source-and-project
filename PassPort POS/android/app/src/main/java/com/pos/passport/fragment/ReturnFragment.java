package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.ResponseType;
import com.ingenico.framework.iconnecttsi.TsiException;
import com.pos.passport.R;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Payment;
import com.pos.passport.task.IngenicoProcessRefundAsyncTask;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.pos.passport.activity.PayActivity.PAYMENT_TYPE_CREDIT;
import static com.pos.passport.activity.PayActivity.getCurrentTimeStamp;
import static com.pos.passport.activity.PayActivity.getTime;

/**
 * Created by Kareem on 6/6/2016.
 */
public class ReturnFragment extends DialogFragment {

    private static final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private BigDecimal mAmount = BigDecimal.ZERO;
    private Cart mCart;
    private Payment mPayment;
    private Button mCashButton;
    private Button mCardButton;
    private Button mCancelButton;
    private Button mPayButton;
    private EditText mCardNumberEditText;
    private EditText mCardExpMonthEditText;
    private EditText mCardExpYearEditText;
    private TextView mAmountTextView;
    private LinearLayout mCardLinearLayout;

    private QueueInterface mCallback;
    private String mPayload;

    private View.OnClickListener mPayButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            processReturn();
        }
    };

    private View.OnClickListener mCashButtonClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mPayment = new Payment();
            mPayment.paymentType = PayActivity.PAYMENT_TYPE_CASH;
            mPayment.paymentAmount = mAmount;
            AlertDialogFragment fragment = AlertDialogFragment.getInstance(getActivity(),R.string.txt_return, String.format(getString(R.string.msg_return_amount), DecimalFormat.getCurrencyInstance().format(mAmount).toString()));
            fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                @Override
                public void ok() {
                    mCallback.onProcessReturn(mPayment);
                    dismiss();
                }

                @Override
                public void cancel() {}
            });
            fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };

    private View.OnClickListener mCancelButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
    private View.OnClickListener mCardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            processReturn();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAmount = new BigDecimal(getArguments().getString("amount"));
        bindUIElements(view);
        setUpListeners();
        setUpUi();
    }

    private void bindUIElements(View v){
        mAmountTextView = (TextView) v.findViewById(R.id.amount_text_view);
        mCardLinearLayout = (LinearLayout) v.findViewById(R.id.card_number_linear_layout);
        mCardNumberEditText = (EditText) v.findViewById(R.id.card_number_edit_text);
        mCardExpMonthEditText = (EditText) v.findViewById(R.id.card_expiration_month_edit_text);
        mCardExpYearEditText = (EditText) v.findViewById(R.id.card_expiration_year_edit_text);
        mPayButton = (Button) v.findViewById(R.id.pay_button);
        mCashButton = (Button) v.findViewById(R.id.cash_button);
        mCardButton = (Button) v.findViewById(R.id.card_button);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
    }

    private void setUpListeners(){
        mPayButton.setOnClickListener(mPayButtonClickListener);
        mCashButton.setOnClickListener(mCashButtonClickListener);
        mCancelButton.setOnClickListener(mCancelButtonClickListener);
        mCardButton.setOnClickListener(mCardButtonClickListener);
    }

    private void setUpUi(){
        //mAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mAmount.divide(Consts.HUNDRED).multiply(Consts.MINUS_ONE)).toString());
        mAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mAmount.multiply(Consts.MINUS_ONE)).toString());
    }

    private boolean valid(){
        if(TextUtils.isEmpty(mCardNumberEditText.getText().toString()) ) {
            mCardNumberEditText.setError(getString(R.string.msg_invalid_card_number));
            return false;
        }
        if(TextUtils.isEmpty(mCardExpMonthEditText.getText().toString())){
            mCardExpMonthEditText.setError(getString(R.string.msg_invalid_expiration));
            return false;
        }
        if(TextUtils.isEmpty(mCardExpYearEditText.getText().toString()) && mCardExpYearEditText.getText().toString().length() < 4){
            mCardExpYearEditText.setError(getString(R.string.msg_invalid_expiration));
            return false;
        }

        return true;
    }

    private void processReturn(){
        RequestType.Refund refundRequest = new RequestType.Refund(mAmount.multiply(Consts.HUNDRED).intValue());
        refundRequest.setClerkId(1);
        IngenicoProcessRefundAsyncTask refundAsyncTask = new IngenicoProcessRefundAsyncTask(getActivity());
        refundAsyncTask.setListener(new IngenicoProcessRefundAsyncTask.IngenicoListener() {
            @Override
            public void success(ResponseType.Refund response) {
                try {
                    mPayment = new Payment();
                    mPayment.paymentType = PAYMENT_TYPE_CREDIT;
                    mPayment.paymentAmount = mAmount;
                    mPayment.gatewayId = response.getReferenceNo();
                    mPayment.invoiceNo = response.getReferenceNo();
                    mPayment.authCode = response.getReferenceNo();
                    mPayment.cardType = response.getCustomerCardType().toString();
                    mPayment.lastFour = response.getCustomerAccountNo();
                    mPayment.date = String.valueOf(getTime(getCurrentTimeStamp()));
                    ProductDatabase.getInstance(getActivity()).saveCreditSale(mPayment, "Return");
                    AlertDialogFragment fragment = AlertDialogFragment.getInstance(getActivity(), R.string.txt_return, String.format(getString(R.string.msg_credit_return_amount), DecimalFormat.getCurrencyInstance().format(mAmount).toString()));
                    fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                        @Override
                        public void ok() {
                            mCallback.onProcessReturn(mPayment);
                            dismiss();
                        }

                        @Override
                        public void cancel() {}
                    });
                    fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                }catch (TsiException e){
                    Log.d("Return Fragment", e.getMessage());
                }
            }

            @Override
            public void failed() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.alertBox(getActivity(), "Error", "Check Device connection and try again");
                    }
                });
            }
        });

        refundAsyncTask.execute(refundRequest);
    }
    /*private void processReturn() {

        BaseGateway gateway = null;
        try {
            if (BuildConfig.GATEWAY.equals("priority")) {
                gateway = new PriorityGateway(getActivity(), R.raw.gateway_priority);
                gateway.setSandBoxEnabled(true);
                JSONObject cardDetails = new JSONObject();
                cardDetails.put("number", mCardNumberEditText.getText().toString());
                cardDetails.put("expiryMonth", mCardExpMonthEditText.getText().toString());
                cardDetails.put("expiryyear", mCardExpYearEditText.getText().toString());
                cardDetails.put("cvv","");
                cardDetails.put("avsZip", "");
                cardDetails.put("avsStreet", "");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("merchantId", WebSetting.merchantID);
                jsonObject.put("tenderType", "Card");
                jsonObject.put("amount", Utils.formatTotal(mAmount.divide(Consts.HUNDRED).multiply(Consts.MINUS_ONE)));
                mPayload = jsonObject.toString();
            }
            if (gateway == null)
                return;
            gateway.processRefund(null, mPayload , new TransactionListener(){

                @Override
                public void onOffline() {
                    Utils.alertBox(getActivity(), R.string.txt_error, R.string.msg_connection_times_out);
                }

                @Override
                public void onApproved(String id, String gatewayId, String response) {
                    mPayment = new Payment();
                    mPayment.paymentType = PayActivity.PAYMENT_TYPE_CREDIT;
                    mPayment.paymentAmount = mAmount;
                    mPayment.response = response;
                    AlertDialogFragment fragment = AlertDialogFragment.getInstance(getActivity(),R.string.txt_return, String.format(getString(R.string.msg_return_amount), DecimalFormat.getCurrencyInstance().format(mAmount.divide(Consts.HUNDRED)).toString()));
                    fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                        @Override
                        public void ok() {
                            mCallback.onProcessReturn(mPayment);
                        }
                    });
                    fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                }

                @Override
                public void onError(String message) {
                    Utils.alertBox(getActivity(), R.string.txt_error, message);
                }

                @Override
                public void onDeclined(String message) {
                    Utils.alertBox(getActivity(), R.string.txt_error, message);
                }
            });
        } catch (Exception e) {

        }
    }*/
}
