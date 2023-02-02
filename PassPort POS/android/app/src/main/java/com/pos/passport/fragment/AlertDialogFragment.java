package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 1/26/2016.
 */
public class AlertDialogFragment extends DialogFragment {

    private String mMessage;
    private String mOkText;
    private String mCancelText;
    private String mTitleText;

    private Button mokButton;
    private Button mCancelButton;
    private TextView mMessageTextView;
    private TextView mTitleView;

    private AlertListener mAlertListener;

    public interface AlertListener{
        void ok();
        void cancel();
    }

    public void setAlertListener(AlertListener l){
        mAlertListener = l;
    }

    private View.OnClickListener  mokButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mAlertListener.ok();
            dismiss();
        }
    };

    private View.OnClickListener mCancelButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mAlertListener.cancel();
            dismiss();
        }
    };

    public static AlertDialogFragment getInstance(Context context,@StringRes int title, @StringRes int message, @StringRes int okText, @StringRes int cancelText){
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.mMessage = context.getString(message);
        fragment.mTitleText = context.getString(title);
        fragment.mOkText = context.getString(okText);
        fragment.mCancelText = context.getString(cancelText);

        return fragment;
    }

    public static AlertDialogFragment getInstance(Context context, @StringRes int title, @StringRes int message){
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.mMessage = context.getString(message);
        fragment.mTitleText = context.getString(title);
        fragment.mOkText = context.getString(R.string.txt_ok);
        return fragment;
    }

    public static AlertDialogFragment getInstance(Context context, @StringRes int title, String message){
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.mMessage = message;
        fragment.mTitleText = context.getString(title);
        fragment.mOkText = context.getString(R.string.txt_ok);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alert_dialog, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUI();
        setUpListeners();
    }

    private void bindUIElements(View v){
        mokButton = (Button) v.findViewById(R.id.alert_ok_button);
        mCancelButton = (Button) v.findViewById(R.id.alert_cancel_button);
        mMessageTextView = (TextView) v.findViewById(R.id.alert_message_text_view);
        mTitleView = (TextView) v.findViewById(R.id.alert_title_text_view);
    }

    private void setUI(){
        mokButton.setText(mOkText);
        if(mCancelText != null)
            mCancelButton.setText(mCancelText);
        if(mOkText != null)
            mMessageTextView.setText(mMessage);
        if(mTitleText != null)
            mTitleView.setText(mTitleText);
    }

    private void setUpListeners(){
        mokButton.setOnClickListener(mokButtonListener);
        mCancelButton.setOnClickListener(mCancelButtonListener);
    }

    public void hideCancelButton(){
        mCancelButton.setVisibility(View.GONE);
    }
}
