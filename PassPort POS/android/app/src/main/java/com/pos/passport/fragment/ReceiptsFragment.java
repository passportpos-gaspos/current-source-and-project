package com.pos.passport.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.EmailSetting;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.StoreReceiptHeader;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 10/30/15.
 */
public class ReceiptsFragment extends Fragment {
    private LinearLayout mPrintOptionsLinearLayout;
    private LinearLayout mPrintOptionsDetailLinearLayout;
    private ImageView mPrintOptionsExpanderImageView;
    private RadioGroup mPrintOptionsRadioGroup;

    private LinearLayout mReceiptHeaderLinearLayout;
    private LinearLayout mReceiptHeaderDetailLinearLayout;
    private LinearLayout mReceiptHeaderDetailLinearLayout2;
    private ImageView mReceiptHeaderExpanderImageView;
    private EditText mStoreNameEditText;
    private EditText mAddressEditText;
    private EditText mPhoneNumberEditText;
    private EditText mEmailEditText;
    private EditText mWebsiteEditText;
    private EditText mStoreNameEditText2;
    private EditText mAddressEditText2;
    private EditText mPhoneNumberEditText2;
    private EditText mEmailEditText2;
    private EditText mWebsiteEditText2;
    private CheckBox mReceiptHeaderCheckBox;
    private Button mReceiptHeaderSaveButton;

    private LinearLayout mEmailReceiptLinearLayout;
    private LinearLayout mEmailReceiptDetailLinearLayout;
    private ImageView mEmailReceiptExpanderImageView;
    private Switch mEmailReceiptSwitch;

    private ProductDatabase mDb;

    private View.OnClickListener mReceiptHeaderSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mReceiptHeaderCheckBox.isChecked() && validateReceiptHeaders()){
                StoreReceiptHeader storeHeader = new StoreReceiptHeader();
                storeHeader.setName(mStoreNameEditText2.getText().toString().trim());
                storeHeader.setAddress1(mAddressEditText2.getText().toString().trim());
                storeHeader.setEmail(mEmailEditText2.getText().toString().trim());
                storeHeader.setPhone(mPhoneNumberEditText2.getText().toString().trim());
                storeHeader.setWebsite(mWebsiteEditText2.getText().toString().trim());
                storeHeader.setCurrency(StoreSetting.getCurrency());
                storeHeader.setHeader_type(StoreSetting.TERMINAL_HEADER);
                mDb.insertStoreSettings(storeHeader);

                StoreSetting.header_type = StoreSetting.TERMINAL_HEADER;
                PrefUtils.saveReceiptHeaderType(getContext(), StoreSetting.TERMINAL_HEADER);
                mDb.findStoreSettings(StoreSetting.header_type);
                Utils.alertBox(getActivity(), R.string.txt_store_settings, R.string.msg_settings_saved);

                return;
            }

            StoreSetting.header_type = StoreSetting.BACK_OFFICE_HEADER;
            PrefUtils.saveReceiptHeaderType(getContext(), StoreSetting.BACK_OFFICE_HEADER);
            mDb.findStoreSettings(StoreSetting.header_type);
        }
    };

    private View.OnClickListener mPrinterOptionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPrintOptionsDetailLinearLayout.getVisibility() == View.GONE) {
                expand(mPrintOptionsDetailLinearLayout);
                mPrintOptionsExpanderImageView.setImageResource(R.drawable.ic_expand_less_48dp);
            } else {
                collapse(mPrintOptionsDetailLinearLayout);
                mPrintOptionsExpanderImageView.setImageResource(R.drawable.ic_expand_more_48dp);
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mPrintOptionCheckedChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            @ReceiptSetting.PrintOption int printOption;
            switch (checkedId) {
                case R.id.option_10_dollars:
                    printOption = ReceiptSetting.RECEIPT_WHEN_D10;
                    break;

                case R.id.option_20_dollars:
                    printOption = ReceiptSetting.RECEIPT_WHEN_D20;
                    break;

                case R.id.option_30_dollars:
                    printOption = ReceiptSetting.RECEIPT_WHEN_D30;
                    break;

                default:
                    printOption = ReceiptSetting.RECEIPT_EVERY_TIME;
                    break;
            }
            PrefUtils.savePrintOption(getActivity(), printOption);
        }
    };

    private View.OnClickListener mReceiptHeaderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mReceiptHeaderDetailLinearLayout.getVisibility() == View.GONE) {
                expand(mReceiptHeaderDetailLinearLayout);
                mReceiptHeaderExpanderImageView.setImageResource(R.drawable.ic_expand_more_48dp);
            } else {
                collapse(mReceiptHeaderDetailLinearLayout);
                mReceiptHeaderExpanderImageView.setImageResource(R.drawable.ic_expand_less_48dp);
            }
        }
    };

    private View.OnClickListener mEmailReceiptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEmailReceiptDetailLinearLayout.getVisibility() == View.GONE) {
                expand(mEmailReceiptDetailLinearLayout);
                mEmailReceiptExpanderImageView.setImageResource(R.drawable.ic_expand_more_48dp);
            } else {
                collapse(mEmailReceiptDetailLinearLayout);
                mEmailReceiptExpanderImageView.setImageResource(R.drawable.ic_expand_less_48dp);
            }
        }
    };

    private View.OnClickListener mEmailReceiptSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEmailReceiptSwitch.isChecked()) {
                /*EmailReceiptCheckAsyncTask checkAsyncTask = new EmailReceiptCheckAsyncTask(getContext(), true);
                checkAsyncTask.setListener(new AsyncTaskListener() {
                    @Override
                    public void onSuccess() {
                        mEmailReceiptSwitch.setText(R.string.txt_on);
                        EmailSetting.setEnabled(true);
                    }

                    @Override
                    public void onFailure() {
                        mEmailReceiptSwitch.setChecked(false);
                    }
                });
                checkAsyncTask.execute();*/
                mEmailReceiptSwitch.setText(R.string.txt_on);
                EmailSetting.setEnabled(true);
                PrefUtils.saveEmailReceiptOption(getActivity(), true);

            } else {
                mEmailReceiptSwitch.setText(R.string.txt_off);
                EmailSetting.setEnabled(false);
                PrefUtils.saveEmailReceiptOption(getActivity(), false);
            }
            //mDb.insertEmailSettings();
        }
    };

    private  View.OnClickListener mReceiptHeaderCheckListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mReceiptHeaderCheckBox.isChecked()){
                mReceiptHeaderDetailLinearLayout2.setVisibility(View.VISIBLE);
                expand(mReceiptHeaderDetailLinearLayout);
            }else{
                mReceiptHeaderDetailLinearLayout2.setVisibility(View.GONE);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setValues();
        setUpListeners();
    }

    private void bindUIElements(View view) {
        mPrintOptionsLinearLayout = (LinearLayout)view.findViewById(R.id.print_options_linear_layout);
        mPrintOptionsDetailLinearLayout = (LinearLayout)view.findViewById(R.id.print_options_detail_linear_layout);
        mPrintOptionsExpanderImageView = (ImageView)view.findViewById(R.id.print_options_expander_image_view);
        mPrintOptionsRadioGroup = (RadioGroup)view.findViewById(R.id.print_options_radio_group);

        mReceiptHeaderLinearLayout = (LinearLayout)view.findViewById(R.id.receipt_header_linear_layout);
        mReceiptHeaderDetailLinearLayout = (LinearLayout)view.findViewById(R.id.receipt_header_detail_linear_layout);
        mReceiptHeaderDetailLinearLayout2 = (LinearLayout) view.findViewById(R.id.receipt_header_detail_linear_layout2);
        mReceiptHeaderExpanderImageView = (ImageView)view.findViewById(R.id.receipt_header_expander_image_view);
        mStoreNameEditText = (EditText)view.findViewById(R.id.store_name_edit_text);
        mAddressEditText = (EditText)view.findViewById(R.id.address_edit_text);
        mPhoneNumberEditText = (EditText)view.findViewById(R.id.phone_number_edit_text);
        mEmailEditText = (EditText)view.findViewById(R.id.email_edit_text);
        mWebsiteEditText = (EditText)view.findViewById(R.id.website_edit_text);
        mReceiptHeaderCheckBox = (CheckBox)view.findViewById(R.id.receipt_header_type_check_box);

        mStoreNameEditText2 = (EditText)view.findViewById(R.id.store_name_edit_text2);
        mAddressEditText2 = (EditText)view.findViewById(R.id.address_edit_text2);
        mPhoneNumberEditText2 = (EditText)view.findViewById(R.id.phone_number_edit_text2);
        mEmailEditText2 = (EditText)view.findViewById(R.id.email_edit_text2);
        mWebsiteEditText2 = (EditText)view.findViewById(R.id.website_edit_text2);

        mReceiptHeaderSaveButton = (Button)view.findViewById(R.id.receipt_header_save_button);

        mEmailReceiptLinearLayout = (LinearLayout)view.findViewById(R.id.email_receipt_linear_layout);
        mEmailReceiptDetailLinearLayout = (LinearLayout)view.findViewById(R.id.email_receipt_detail_linear_layout);
        mEmailReceiptExpanderImageView = (ImageView)view.findViewById(R.id.email_receipt_expander_image_view);
        mEmailReceiptSwitch = (Switch)view.findViewById(R.id.email_receipt_switch);
    }

    private void setUpListeners() {
        mPrintOptionsLinearLayout.setOnClickListener(mPrinterOptionsClickListener);
        mPrintOptionsRadioGroup.setOnCheckedChangeListener(mPrintOptionCheckedChangedListener);
        mReceiptHeaderLinearLayout.setOnClickListener(mReceiptHeaderClickListener);
        mReceiptHeaderSaveButton.setOnClickListener(mReceiptHeaderSaveClickListener);
        mEmailReceiptLinearLayout.setOnClickListener(mEmailReceiptClickListener);
        mEmailReceiptSwitch.setOnClickListener(mEmailReceiptSwitchClickListener);
        mReceiptHeaderCheckBox.setOnClickListener(mReceiptHeaderCheckListener);
    }

    private void setValues() {
        int printOption = PrefUtils.getPrintOption(getActivity());
        switch (printOption) {
            case ReceiptSetting.RECEIPT_WHEN_D10:
                mPrintOptionsRadioGroup.check(R.id.option_10_dollars);
                break;

            case ReceiptSetting.RECEIPT_WHEN_D20:
                mPrintOptionsRadioGroup.check(R.id.option_20_dollars);
                break;

            case ReceiptSetting.RECEIPT_WHEN_D30:
                mPrintOptionsRadioGroup.check(R.id.option_30_dollars);
                break;

            default:
                mPrintOptionsRadioGroup.check(R.id.option_always);
                break;
        }

        mEmailReceiptSwitch.setChecked(PrefUtils.getEmailReceiptOption(getActivity()));

        mStoreNameEditText.setText(TextUtils.isEmpty(StoreSetting.getName()) ? "" : StoreSetting.getName());
        mAddressEditText.setText(TextUtils.isEmpty(StoreSetting.getAddress1()) ? "" : StoreSetting.getAddress1());
        mPhoneNumberEditText.setText(TextUtils.isEmpty(StoreSetting.getPhone()) ? "" : StoreSetting.getPhone());
        mEmailEditText.setText(TextUtils.isEmpty(StoreSetting.getEmail()) ? "" : StoreSetting.getEmail());
        mWebsiteEditText.setText(TextUtils.isEmpty(StoreSetting.getWebsite()) ? "" : StoreSetting.getWebsite());
        mReceiptHeaderCheckBox.setChecked(PrefUtils.getReceiptHeaderType(getContext()) == StoreSetting.TERMINAL_HEADER);
        StoreReceiptHeader header = mDb.getStoreSettings(StoreSetting.TERMINAL_HEADER);
        mStoreNameEditText2.setText(TextUtils.isEmpty(header.getName()) ? "" : header.getName());
        mAddressEditText2.setText(TextUtils.isEmpty(header.getAddress1()) ? "" : header.getAddress1());
        mPhoneNumberEditText2.setText(TextUtils.isEmpty(header.getPhone()) ? "" : header.getPhone());
        mEmailEditText2.setText(TextUtils.isEmpty(header.getEmail()) ? "" : header.getEmail());
        mWebsiteEditText2.setText(TextUtils.isEmpty(header.getWebsite()) ? "" : header.getWebsite());
        if(mReceiptHeaderCheckBox.isChecked())
            mReceiptHeaderDetailLinearLayout2.setVisibility(View.VISIBLE);

    }

    private void expand(LinearLayout l) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        ValueAnimator animator = slideAnimator(l, 0, l.getMeasuredHeight());
        animator.start();
    }

    private void collapse(final LinearLayout l) {
        int finalHeight = l.getHeight();

        ValueAnimator animator = slideAnimator(l, finalHeight, 0);
        animator.start();
    }

    private ValueAnimator slideAnimator(final LinearLayout l, final int start, final int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = l.getLayoutParams();
                layoutParams.height = value;
                l.setLayoutParams(layoutParams);
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (end == 0)
                    l.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animator;
    }

    private boolean validateReceiptHeaders(){
        boolean isValid = true;
        if(mReceiptHeaderCheckBox.isChecked()){
            if(TextUtils.isEmpty(mStoreNameEditText2.getText().toString().trim())) {
                mStoreNameEditText2.setError(getResources().getString(R.string.txt_error));
                isValid = false;
            }
            if (TextUtils.isEmpty(mAddressEditText2.getText().toString().trim())) {
                mAddressEditText2.setError(getResources().getString(R.string.txt_error));
                isValid = false;
            }
            if(!Utils.validateEmail(mEmailEditText2.getText().toString().trim())){
                mEmailEditText2.setError(getResources().getString(R.string.txt_error));
                isValid = false;
            }

            if(TextUtils.isEmpty(mPhoneNumberEditText2.getText().toString().trim())){
                mPhoneNumberEditText2.setError(getResources().getString(R.string.txt_error));
                isValid = false;
            }
        }

        return isValid;
    }
}
