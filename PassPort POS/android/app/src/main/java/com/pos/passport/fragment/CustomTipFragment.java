package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.AdminSetting;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Product;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/7/15.
 */
public class CustomTipFragment extends DialogFragment
{


    @IntDef({TEN_PAD_TYPE_ADMIN, TEN_PAD_TYPE_DISCOUNT, TEN_PAD_TYPE_PRICE, TEN_PAD_TYPE_CASH, TEN_PAD_TYPE_LOGIN })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TenPadType {}

    @IntDef({ DISCOUNT_AMOUNT, DISCOUNT_PERCENT })
    @Retention(RetentionPolicy.SOURCE)
    public @interface  DiscountType {}

    public static final String BUNDLE_ATTACH_AS_FRAGMENT = "bundle_attach_as_fragment";

    public static final int TEN_PAD_TYPE_ADMIN = 0;
    public static final int TEN_PAD_TYPE_DISCOUNT = 1;
    public static final int TEN_PAD_TYPE_PRICE = 2;
    public static final int TEN_PAD_TYPE_CASH = 3;
    public static final int TEN_PAD_TYPE_LOGIN = 4;

    public static final int DISCOUNT_AMOUNT = 0;
    public static final int DISCOUNT_PERCENT = 1;

    public static final int MAX_AMOUNT = 100000;   // $1000.00

    private int mType;
    private BigDecimal mMaxValue;
    private BigDecimal mPrice;
    private BigDecimal mValue;
    private int mDiscountType;
    private boolean mAttachAsFragment;
    private LinearLayout mTitleLayout;
    private TextView mTitleTextView;
    private TextView mDisplayTextView;
    private EditText mPasswordEditView;
    private List<Button> mButtons;
    private RadioGroup mDiscountRadioGroup;
    private RadioButton mAmountRadioButton;
    private RadioButton mPercentRadioButton;
    private ImageButton mDeleteImageButton;
    private Button mCancelButton;
    private Button mOkButton;
    private Button mButton0;
    private Button buttondot;
    private LinearLayout mTenPadLayout;
    private TenPadListener mTenPadListener;
    private DiscountListener mDiscountListener;
    private PriceListener mPriceListener;
    private Cashier mCashier;
    //private ImageView mCloseImageView;
    private Typeface mNatoSanBolad;

    private Context mCallback;
    private ImageView mCloseView;
    private LinearLayout mPayLayout;
    private TextView mAmountDueTextView;
    private TextView mTipAmountTextView;
    private TextView mTipAmountPercentTextView;
    private TextView mTipTotalAmountPay;
    private TextView mPayAmountTextView;

    public interface TenPadListener {
        void onAdminAccessGranted();
        void onAdminAccessDenied();
    }

    public interface DiscountListener {
        void onDiscountPrice(BigDecimal amount);
        void onDiscountPercent(BigDecimal percent);
    }

    public interface PriceListener {
        void onSetPrice(BigDecimal amount);
    }

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mAttachAsFragment)
                dismiss();
            else
                try {
                    ((QueueInterface) mCallback).onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                }catch (ClassCastException e){
                    e.printStackTrace();
                }
        }
    };

    private View.OnClickListener mTitleBarCloseButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                ((QueueInterface) mCallback).onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mValue == null)
                return;
            if (mValue.toString().length() > 1)
            {
                mValue = new BigDecimal(mValue.toString().substring(0, mValue.toString().length() - 1));
                //showDisplay();
                showTotalPayable();
            }
            /*else if (mValue.toString().length() == 1) {
                if(mType == TEN_PAD_TYPE_LOGIN || mType == TEN_PAD_TYPE_ADMIN){
                    setValue(null);
                }else{
                    setValue(BigDecimal.ZERO);
                }
                showDisplay();
            }*/
        }
    };

    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mType == TEN_PAD_TYPE_ADMIN) {
                if (mValue != null && mValue.toString().equals(AdminSetting.password)) {
                    mTenPadListener.onAdminAccessGranted();
                } else {
                    mDisplayTextView.setError(getString(R.string.txt_invalid_pin));
                    mPasswordEditView.setError(getString(R.string.txt_invalid_pin));
                    return;
                    //mTenPadListener.onAdminAccessDenied();
                }
            } else if(mType == TEN_PAD_TYPE_LOGIN){
                if(mValue != null && mValue.toString().equals(mCashier.pin)){
                    mTenPadListener.onAdminAccessGranted();
                }else{
                    mDisplayTextView.setError(getString(R.string.txt_invalid_pin));
                    mPasswordEditView.setError(getString(R.string.txt_invalid_pin));
                    return;
                    //mTenPadListener.onAdminAccessDenied();
                }
            }else if (mType == TEN_PAD_TYPE_DISCOUNT) {
                if (mValue != null) {
                    if (mAmountRadioButton.isChecked()) {
                        mDiscountListener.onDiscountPrice(mValue);
                    } else {
                        mDiscountListener.onDiscountPercent(mValue);
                    }
                }
            } else {
                if (mValue != null) {
                    if (mType == TEN_PAD_TYPE_CASH || mType == TEN_PAD_TYPE_PRICE) {
                        if(mValue.compareTo(BigDecimal.ZERO) == 0) {
                            mDisplayTextView.setError(getString(R.string.txt_error_valid_amount));
                            return;
                        }
                        mPriceListener.onSetPrice(mValue);
                    }

                }
            }

            if(!mAttachAsFragment)
                dismiss();
        }
    };

    private View.OnClickListener mCloseImageClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
    private RadioGroup.OnCheckedChangeListener mDiscountCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.ten_pad_percent_radio_button) {

                if (mValue.compareTo(Consts.HUNDRED) == 1)
                    setValue(BigDecimal.ZERO);

                mPercentRadioButton.setTextColor(Color.WHITE);
                mAmountRadioButton.setTextColor(Utils.getColor(mCallback, R.color.pos_secondary_blue));
                showDisplay();
                showTotalPayable();
            } else {
                mPercentRadioButton.setTextColor(Utils.getColor(mCallback, R.color.pos_secondary_blue));
                mAmountRadioButton.setTextColor(Color.WHITE);
                showDisplay();
                showTotalPayable();
            }
        }
    };

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = ((Button)v).getText().toString();

            if (mType == TEN_PAD_TYPE_DISCOUNT && mAmountRadioButton.isChecked()) {
                switch (s) {
                    case ".":
                        if (mValue.compareTo(Consts.HUNDRED) == -1)
                            mValue = mValue.multiply(Consts.HUNDRED);
                        break;

                    default:
                        if (mValue.toString().endsWith("00"))
                            mValue = new BigDecimal(mValue.toString().replace("00", s + "0"));
                        else if (mValue.toString().endsWith("0"))
                            mValue = new BigDecimal(mValue.toString().substring(0, mValue.toString().length() - 1) + s);
                        else
                            mValue = new BigDecimal(mValue.toString() + s);
                }
            } else {
                switch (s) {
                    case ".":
                        if (mValue!= null && !mValue.toString().contains(".")) {
                                if (mType == TEN_PAD_TYPE_DISCOUNT) {
                                        mValue = new BigDecimal(mValue + s + "0");
                                } else
                                    mValue = new BigDecimal(mValue + s);
                        }
                        break;

                    case "00":
                        if (mValue!= null && !mValue.toString().endsWith("0"))
                            mValue = new BigDecimal(mValue + s);
                        break;

                    default:
                        if(mValue == null && (mType == TEN_PAD_TYPE_LOGIN || mType == TEN_PAD_TYPE_ADMIN)){
                            mValue = new BigDecimal(s);
                        }else {
                            if (!mValue.toString().equals("0")) {
                                if (mValue.toString().endsWith(".0"))
                                    mValue = new BigDecimal(mValue.toString().replace(".0", ".") + s);
                                else if (getNumberOfFractionDigits(mValue) < 2)
                                    mValue = new BigDecimal(mValue + s);
                            } else
                                mValue = new BigDecimal(s);
                        }
                }
            }

            //showDisplay();
            showTotalPayable();
        }
    };

    public static CustomTipFragment newInstance(@TenPadType int tenPadType) {
        CustomTipFragment f = new CustomTipFragment();

        Bundle args = new Bundle();
        args.putInt("type", tenPadType);
        f.setArguments(args);

        return f;
    }

    public static CustomTipFragment newInstance(@TenPadType int tenPadType, @NonNull Cashier cashier){
        CustomTipFragment f = new CustomTipFragment();

        Bundle args = new Bundle();
        args.putInt("type", tenPadType);
        args.putSerializable("cashier", cashier);
        f.setArguments(args);
        return f;
    }

    public static CustomTipFragment newInstance(@TenPadType int tenPadType, BigDecimal amount) {
        CustomTipFragment f = new CustomTipFragment();

        Bundle args = new Bundle();
        args.putInt("type", tenPadType);
        if (amount != null) {
            if (tenPadType == TEN_PAD_TYPE_DISCOUNT)
                args.putSerializable("maxAmount", amount);
            else if (tenPadType == TEN_PAD_TYPE_PRICE || tenPadType == TEN_PAD_TYPE_CASH)
                args.putSerializable("price", amount);
        }
        f.setArguments(args);

        return f;
    }

    public static CustomTipFragment newInstance(@TenPadType int tenPadType, BigDecimal maxAmount, BigDecimal price) {
        CustomTipFragment f = new CustomTipFragment();

        Bundle args = new Bundle();
        args.putInt("type", tenPadType);
        if (maxAmount != null && price != null) {
            if (tenPadType == TEN_PAD_TYPE_DISCOUNT)
                args.putSerializable("maxAmount", maxAmount);
                args.putSerializable("price", price);
        }
        f.setArguments(args);

        return f;
    }

    public static CustomTipFragment newInstance(@TenPadType int tenPadType, BigDecimal maxAmount, BigDecimal price, @Product.ModifierType int discountType) {
        CustomTipFragment f = new CustomTipFragment();

        Bundle args = new Bundle();
        args.putInt("type", tenPadType);
        args.putSerializable("maxAmount", maxAmount);
        args.putSerializable("price", price);
        args.putInt("discountType", discountType);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mNatoSanBolad = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallback = context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v ;
            v = inflater.inflate(R.layout.fragment_custom_tip, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));
        return v;
    }

   /* @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //return super.onCreateDialog(savedInstanceState);
        //View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_my_dialog, new LinearLayout(getActivity()), false);
        View v ;
        if(getArguments().getBoolean(BUNDLE_ATTACH_AS_FRAGMENT))
            v = getActivity().getLayoutInflater().inflate(R.layout.fragment_ten_pad_title_bar, new LinearLayout(getActivity()), false);
        else v = getActivity().getLayoutInflater().inflate(R.layout.fragment_ten_pad_new, new LinearLayout(getActivity()), false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        Dialog builder = new Dialog(getActivity());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setContentView(v);
        return builder;
    }*/

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mType = getArguments().getInt("type");
        try {
            if (getArguments().getSerializable("maxAmount") != null)
                mMaxValue = (BigDecimal)getArguments().getSerializable("maxAmount");
            else
                mMaxValue = new BigDecimal(MAX_AMOUNT);
            if (getArguments().getSerializable("price") != null)
                if (mType == TEN_PAD_TYPE_CASH)
                    mPrice = (BigDecimal)getArguments().getSerializable("price");
                else
                    mValue = (BigDecimal)getArguments().getSerializable("price");
            mDiscountType = getArguments().getInt("discountType");
            mAttachAsFragment = getArguments().getBoolean(BUNDLE_ATTACH_AS_FRAGMENT);
            if(getArguments().getSerializable("cashier") != null){
                mCashier = (Cashier) getArguments().getSerializable("cashier");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();

            mMaxValue = new BigDecimal(MAX_AMOUNT);
        }
        bindUIElements(view);
        setUI();
        setUpListeners();
    }

    private void bindUIElements(View v) {
        mTenPadLayout = (LinearLayout) v.findViewById(R.id.ten_pad_layout);
        //mCloseImageView = (ImageView) v.findViewById(R.id.close_image_view);
        mTitleLayout =  (LinearLayout) v.findViewById(R.id.ten_pad_title_layout);
        mTitleTextView = (TextView)v.findViewById(R.id.ten_pad_title_text_view);
        mDisplayTextView = (TextView)v.findViewById(R.id.display_text_view);
        mPasswordEditView = (EditText) v.findViewById(R.id.password_edit_text);
        mDiscountRadioGroup = (RadioGroup)v.findViewById(R.id.ten_pad_discount_radio_group);
        mAmountRadioButton = (RadioButton)v.findViewById(R.id.ten_pad_amount_radio_button);
        mPercentRadioButton = (RadioButton)v.findViewById(R.id.ten_pad_percent_radio_button);
        mButtons = new ArrayList<>();
        Button button1 = (Button)v.findViewById(R.id.ten_pad_1_button);
        Button button2 = (Button)v.findViewById(R.id.ten_pad_2_button);
        Button button3 = (Button)v.findViewById(R.id.ten_pad_3_button);
        Button button4 = (Button)v.findViewById(R.id.ten_pad_4_button);
        Button button5 = (Button)v.findViewById(R.id.ten_pad_5_button);
        Button button6 = (Button)v.findViewById(R.id.ten_pad_6_button);
        Button button7 = (Button)v.findViewById(R.id.ten_pad_7_button);
        Button button8 = (Button)v.findViewById(R.id.ten_pad_8_button);
        Button button9 = (Button)v.findViewById(R.id.ten_pad_9_button);
        mButton0 = (Button)v.findViewById(R.id.ten_pad_0_button);
        Button button00 = (Button)v.findViewById(R.id.ten_pad_00_button);
        buttondot = (Button) v.findViewById(R.id.ten_pad_dot_button);
        mButtons.add(button1);
        mButtons.add(button2);
        mButtons.add(button3);
        mButtons.add(button4);
        mButtons.add(button5);
        mButtons.add(button6);
        mButtons.add(button7);
        mButtons.add(button8);
        mButtons.add(button9);
        mButtons.add(mButton0);
        mButtons.add(button00);
        mButtons.add(buttondot);
        mDeleteImageButton = (ImageButton)v.findViewById(R.id.ten_pad_delete_image_button);
        mCancelButton = (Button)v.findViewById(R.id.ten_pad_cancel_button);
        mOkButton = (Button)v.findViewById(R.id.ten_pad_ok_button);
        mCloseView=(ImageView)v.findViewById(R.id.close_view);

        mPayLayout=(LinearLayout)v.findViewById(R.id.pay_ll) ;
        mAmountDueTextView=(TextView)v.findViewById(R.id.amtdue_txt);
        mTipAmountTextView=(TextView)v.findViewById(R.id.tipamt_txt);
        mTipAmountPercentTextView=(TextView)v.findViewById(R.id.tipamtpercent_txt);
        mTipTotalAmountPay=(TextView)v.findViewById(R.id.totalamountpay);
        mPayAmountTextView = (TextView) v.findViewById(R.id.totalpayamount_lbl);


    }
    private void showTotalPayable() {

        if (mAmountRadioButton.isChecked())
        {
            //   if (mValue.compareTo(mMaxValue) == 1)
            //       mValue = mMaxValue;

            mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));
            mTipAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));
            BigDecimal totalAmountPay=mMaxValue.add(mValue.divide(Consts.HUNDRED));
            mTipTotalAmountPay.setText(DecimalFormat.getCurrencyInstance().format(totalAmountPay));
            mPayAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(totalAmountPay));
            if ((mValue.divide(Consts.HUNDRED)).compareTo(BigDecimal.ZERO) > 0)
            {
                mTipAmountPercentTextView.setText(String.format("(%1$s%%)", mValue.divide(mMaxValue, 2, RoundingMode.HALF_EVEN).toString()));
            }else
                mTipAmountPercentTextView.setText("(0%)");

        }
        else
        {
            //if (mValue.compareTo(Consts.HUNDRED) == 1)
            //    mValue = Consts.HUNDRED;
            mDisplayTextView.setText(String.format("%s%%", mValue.toString()));
            BigDecimal totalPercentageAmount=mMaxValue.multiply(mValue).divide(Consts.HUNDRED);
            mTipAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(totalPercentageAmount));
            mTipTotalAmountPay.setText(DecimalFormat.getCurrencyInstance().format(mMaxValue.add(totalPercentageAmount)));
            mTipAmountPercentTextView.setText("("+mValue.toString()+"%)");
        }

    }
    private void setUI()
    {

        if (mType == TEN_PAD_TYPE_DISCOUNT)
        {
            mTitleTextView.setText(R.string.txt_order_discount);
            mDiscountRadioGroup.setVisibility(View.VISIBLE);
            if (mDiscountType == Product.MODIFIER_TYPE_DISCOUNT_AMOUNT)
            {
                mAmountRadioButton.setChecked(true);
                mPercentRadioButton.setVisibility(View.GONE);
                mPercentRadioButton.setTextColor(Color.WHITE);
                mPercentRadioButton.setBackgroundColor(Utils.getColor(mCallback, R.color.pos_secondary_blue));
            }
            else if (mDiscountType == Product.MODIFIER_TYPE_DISCOUNT_PERCENT)
            {
                mPercentRadioButton.setChecked(true);
                mAmountRadioButton.setVisibility(View.GONE);
                mPercentRadioButton.setBackgroundColor(Utils.getColor(mCallback, R.color.pos_secondary_blue));
                mPercentRadioButton.setTextColor(Color.WHITE);
            }
            mOkButton.setText(R.string.txt_add);
        } else if (mType == TEN_PAD_TYPE_ADMIN)
        {
            mTitleTextView.setText(R.string.txt_admin_login);
            mDiscountRadioGroup.setVisibility(View.GONE);
            mDisplayTextView.setVisibility(View.GONE);
            mPasswordEditView.setVisibility(View.VISIBLE);
            buttondot.setVisibility(View.INVISIBLE);
        } else if(mType == TEN_PAD_TYPE_LOGIN)
        {
            mTitleTextView.setText(WordUtils.capitalize(mCashier.name));
            mDiscountRadioGroup.setVisibility(View.GONE);
            mDisplayTextView.setVisibility(View.GONE);
            mPasswordEditView.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.GONE);
            buttondot.setVisibility(View.INVISIBLE);
            mOkButton.setText("Ok");
           // mCloseImageView.setVisibility(View.GONE);
        }else {
            mTitleTextView.setText(R.string.txt_amount);
            mDiscountRadioGroup.setVisibility(View.VISIBLE);
            mPercentRadioButton.setVisibility(View.GONE);
            if (mType == TEN_PAD_TYPE_CASH) {
                setCancelable(false);
                mOkButton.setText(R.string.txt_pay);
            }
        }
        mAmountRadioButton.setTypeface(mNatoSanBolad);
        mPercentRadioButton.setTypeface(mNatoSanBolad);

        mAmountDueTextView.setText(DecimalFormat.getCurrencyInstance().format(mMaxValue));

        initValue();
        //showDisplay();
        showTotalPayable();
    }

    private void setUpListeners() {
        mDeleteImageButton.setOnClickListener(mDeleteClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
        mOkButton.setOnClickListener(mOkClickListener);
        mDiscountRadioGroup.setOnCheckedChangeListener(mDiscountCheckedChangeListener);
        for (Button button : mButtons) {
            button.setOnClickListener(mButtonClickListener);
        }
        mPasswordEditView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });
        mPayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriceListener.onSetPrice(mValue);
                dismiss();
            }
        });
       // mCloseImageView.setOnClickListener(mCloseImageClickListener);
    }

    private void initValue() {
        if (mValue == null || mValue.compareTo(BigDecimal.ZERO) == 0) {
            if (mType == TEN_PAD_TYPE_LOGIN || mType == TEN_PAD_TYPE_ADMIN) {
                mValue = null;
                return;
            }
            mValue = BigDecimal.ZERO;
        }
    }

    private void setValue(BigDecimal value) {
        mValue = value;
    }

    private void showDisplay() {
        if (mType == TEN_PAD_TYPE_DISCOUNT)
        {
           if (mAmountRadioButton.isChecked()) {
               if (mValue.compareTo(mMaxValue) == 1)
                   mValue = mMaxValue;

               mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));
           }
           else
           {
               if (mValue.compareTo(Consts.HUNDRED) == 1)
                   mValue = Consts.HUNDRED;
               mDisplayTextView.setText(String.format("%s%%", mValue.toString()));
           }
        } else if (mType == TEN_PAD_TYPE_PRICE || mType == TEN_PAD_TYPE_CASH) {
            if (mValue.compareTo(mMaxValue) == 1)
                mValue = mMaxValue;
            mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));

        } else if(mType == TEN_PAD_TYPE_LOGIN || mType == TEN_PAD_TYPE_ADMIN){
            mPasswordEditView.setText(mValue == null ? "" : mValue.toString());
        }else{
            mDisplayTextView.setText(mValue.toString());
        }
    }

    private int getNumberOfFractionDigits(BigDecimal number) {
        String digits[] = number.toString().split("\\.");
        if (digits.length < 2)
            return 0;

        return digits[1].length();
    }

    public void setTenPadListener(TenPadListener l) {
        mTenPadListener = l;
    }

    public void setDiscountListener(DiscountListener l) {
        mDiscountListener = l;
    }

    public void setPriceListener(PriceListener l) {
        mPriceListener = l;
    }
}
