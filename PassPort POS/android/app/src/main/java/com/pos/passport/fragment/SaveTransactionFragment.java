package com.pos.passport.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.pos.passport.R;
import com.pos.passport.model.OfflineOption;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 1/28/16.
 */
public class SaveTransactionFragment extends DialogFragment {
    private CheckBox mApplyAllCheckBox;
    private Button mCancelButton;
    private Button mYesButton;
    private SaveTransactionListener mSaveTransactionListener;

    public interface SaveTransactionListener {
        void onSaveSale();
        void onCancelSale();
    }

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSaveTransactionListener.onCancelSale();
            dismiss();
        }
    };

    private View.OnClickListener mYesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mApplyAllCheckBox.isChecked()) {
                OfflineOption option = new OfflineOption();
                option.setShowingMessage(false);
                option.setOffline(true);
                option.setTimestamp(System.currentTimeMillis());
                PrefUtils.setOfflineOption(getActivity(), option);
            }
            mSaveTransactionListener.onSaveSale();
            dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_save_transaction, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUpListeners();
    }

    private void bindUIElements(View view) {
        mApplyAllCheckBox = (CheckBox)view.findViewById(R.id.apply_all_check_box);
        mCancelButton = (Button)view.findViewById(R.id.cancel_sale_button);
        mYesButton = (Button)view.findViewById(R.id.yes_button);
    }

    private void setUpListeners() {
        mCancelButton.setOnClickListener(mCancelClickListener);
        mYesButton.setOnClickListener(mYesClickListener);
    }

    public void setSaveTransactionListener(SaveTransactionListener l) {
        this.mSaveTransactionListener = l;
    }
}
