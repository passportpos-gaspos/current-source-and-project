package com.pos.passport.fragment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.interfaces.PayInterface;
import com.pos.passport.model.Payment;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by karim on 11/23/15.
 */
public class ApproveFragment extends DialogFragment {
    private static final String BUNDLE_AMOUNT = "bundle_amount";
    private static final String BUNDLE_TENDER_TYPE = "bundle_tender_type";
    private static final String BUNDLE_GATEWAY_ID = "bundle_gateway_id";
    private static final String BUNDLE_IMAGE_SIGN = "bundle_image_sign";
    private static final String BUNDLE_TYPE = "bundle_type";
    private static final String BUNDLE_TIP_AMOUNT = "bundle_tip_amount";
    private static final String BUNDLE_PAYMENT_LIST = "bundle_payment_list";
    private TextView mAmountTextView;
    private TextView mTenderTypeTextView;
    private PayInterface mCallback;
    private String mTenderType;
    private String mImagSign;
    private String mType;
    private LinearLayout mContainerView;
    private RelativeLayout mRelativeLayout;
    private ArrayList<Payment> mPayments = new ArrayList<>();

    public static ApproveFragment newInstance(String amount, String paymentType,ArrayList<Payment> mPayments) {
        ApproveFragment fragment = new ApproveFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putSerializable(BUNDLE_PAYMENT_LIST, mPayments);
        fragment.setArguments(args);

        return fragment;
    }
    public static ApproveFragment newInstance(String amount, String paymentType, String imgSign,String type) {
        ApproveFragment fragment = new ApproveFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putString(BUNDLE_IMAGE_SIGN, imgSign);
        args.putString(BUNDLE_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }
    public static ApproveFragment newInstance(String amount, String paymentType, String gatewayId,ArrayList<Payment> mPayments) {
        ApproveFragment fragment = new ApproveFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putString(BUNDLE_GATEWAY_ID, gatewayId);
        args.putSerializable(BUNDLE_PAYMENT_LIST, mPayments);
        fragment.setArguments(args);

        return fragment;
    }

    public static ApproveFragment newInstance(String amount, String paymentType,String tipAmount, String imgSign, String type) {
        ApproveFragment fragment = new ApproveFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putString(BUNDLE_IMAGE_SIGN, imgSign);
        args.putString(BUNDLE_TYPE, type);
        args.putString(BUNDLE_TIP_AMOUNT, tipAmount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (PayInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PayInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_approve, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        getDialog().setCanceledOnTouchOutside(false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        mTenderType = getArguments().getString(BUNDLE_TENDER_TYPE);
        if (getArguments().getSerializable(BUNDLE_PAYMENT_LIST) != null)
        {
            mPayments = (ArrayList<Payment>) getArguments().getSerializable(BUNDLE_PAYMENT_LIST);
            Log.e("Payment data","Payment>>"+mPayments.size());
        }else
        {
            Log.e("Payment data","Payment>>else part>>>"+mPayments.size());
        }
        setTextView();
        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
                // nothing
            }

            public void onFinish()
            {
                BigDecimal tip = BigDecimal.ZERO;
                if(getArguments().getString(BUNDLE_TIP_AMOUNT) != null)
                    tip = new BigDecimal(getArguments().getString(BUNDLE_TIP_AMOUNT));
                mCallback.onPayCompleted(getArguments().getString(BUNDLE_GATEWAY_ID),
                        getArguments().getString(BUNDLE_IMAGE_SIGN),
                        getArguments().getString(BUNDLE_TENDER_TYPE),
                        tip.toString());
                dismiss();
            }
        }.start();
    }

    private void bindUIElements(View view) {
        mAmountTextView = (TextView)view.findViewById(R.id.amount_text_view);
        mTenderTypeTextView = (TextView)view.findViewById(R.id.tender_type_text_view);
        mRelativeLayout=(RelativeLayout)view.findViewById(R.id.ll_view);
        mRelativeLayout.setVisibility(View.GONE);
        mContainerView = (LinearLayout )view.findViewById(R.id.ll_payment);
        mContainerView.setVisibility(View.VISIBLE);

    }

    private void setTextView() {

        if(mPayments.size()>0){
            setPaymentList();
        }else {
            mAmountTextView.setText(getArguments().getString(BUNDLE_AMOUNT));
            mTenderTypeTextView.setText(mTenderType);
        }
    }
    private void setPaymentList()
    {
        LayoutInflater inflater =(LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int p=0; p<mPayments.size();p++)
        {
            View child = inflater.inflate(R.layout.view_child_payment, null);//child.xml
            mAmountTextView = (TextView)child.findViewById(R.id.amount_text_view);
            mTenderTypeTextView = (TextView)child.findViewById(R.id.tender_type_text_view);
            mTenderTypeTextView.setText(mPayments.get(p).paymentType);
            mAmountTextView.setText(""+mPayments.get(p).paymentAmount.add(mPayments.get(p).tipAmount));
            mContainerView.addView(child);
        }
    }
}
