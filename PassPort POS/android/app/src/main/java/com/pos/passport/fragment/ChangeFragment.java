package com.pos.passport.fragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pos.passport.R;
import com.pos.passport.interfaces.PayInterface;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by karim on 12/9/15.
 */
public class ChangeFragment extends DialogFragment {
    private static final String BUNDLE_AMOUNT = "bundle_amount";
    private static final String BUNDLE_CHANGE = "bundle_change";
    private static final String BUNDLE_PAYMENT_TYPE = "bundle_payment_type";
    private static final String BUNDLE_TIP_AMOUNT = "bundle_payment_tip_amount";
    private TextView mChangeTextView;
    private Button mOkButton;
    private PayInterface mCallback;
    private String mAmount;
    private String mChange;
    private TextView mAmountTextView;
    private TextView mTenderTypeTextView;

    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Change")
                    .putCustomAttribute("Button", "Ok"));
            mCallback.onPayCompleted(null);
            dismiss();
        }
    };

    public static ChangeFragment newInstance(String amount, String change) {
        ChangeFragment fragment = new ChangeFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_CHANGE, change);
        fragment.setArguments(args);

        return fragment;
    }

    public static ChangeFragment newInstance(String amount, String change, String paymentType){
        ChangeFragment fragment = new ChangeFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_CHANGE, change);
        args.putString(BUNDLE_PAYMENT_TYPE , paymentType);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAmount = getArguments().getString(BUNDLE_AMOUNT);
        mChange = getArguments().getString(BUNDLE_CHANGE);
        bindUIElements(view);
        setUpListeners();
        setUpUIs();

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                // nothing
            }

            public void onFinish() {
                mCallback.onPayCompleted(null);
                dismiss();
            }
        }.start();

    }

    private void bindUIElements(View v) {
        mAmountTextView = (TextView)v.findViewById(R.id.amount_text_view);
        mChangeTextView = (TextView)v.findViewById(R.id.change_text_view);
        mTenderTypeTextView = (TextView) v.findViewById(R.id.tender_type_text_view);
        mOkButton = (Button)v.findViewById(R.id.change_ok_button);
    }

    private void setUpListeners() {
        mOkButton.setOnClickListener(mOkClickListener);
    }

    private void setUpUIs() {
        mAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(new BigDecimal(mAmount)));
        mChangeTextView.setText(DecimalFormat.getCurrencyInstance().format(new BigDecimal(mChange)));
        if(getArguments().getString(BUNDLE_PAYMENT_TYPE) != null)
            mTenderTypeTextView.setText(getArguments().getString(BUNDLE_PAYMENT_TYPE));
    }
}
