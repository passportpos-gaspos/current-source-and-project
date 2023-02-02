package com.pos.passport.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.ResponseType;
import com.ingenico.framework.iconnecttsi.TsiException;
import com.ingenico.framework.iconnecttsi.iConnectTsiTypes;
import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;
import com.pos.passport.BuildConfig;
import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.AlertDialogFragment;
import com.pos.passport.fragment.ApproveFragment;
import com.pos.passport.fragment.ChangeFragment;
import com.pos.passport.fragment.SaveTransactionFragment;
import com.pos.passport.fragment.SignatureFragment;
import com.pos.passport.fragment.TenPadDialogFragment;
import com.pos.passport.fragment.TipPadDialogFragment;
import com.pos.passport.gateway.BaseGateway;
import com.pos.passport.gateway.BridgePayGateway;
import com.pos.passport.gateway.ClearentGateway;
import com.pos.passport.gateway.PriorityGateway;
import com.pos.passport.interfaces.PayInterface;
import com.pos.passport.interfaces.TransactionListener;
import com.pos.passport.model.OfflineOption;
import com.pos.passport.model.Payment;
import com.pos.passport.model.ProcessCreditCard;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.WebSetting;
import com.pos.passport.task.IngenicoProcessRequestAsyncTask;
import com.pos.passport.util.Consts;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kareem on 10/27/2016.
 */

public class PaymentActivity extends BaseActivity implements PayInterface {

    private final String DEBUG_TAG = "[PayActivity]";
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    public static final String BUNDLE_AMOUNT = "bundle_amount";
    public static final String BUNDLE_PAYMENT_TYPE = "bundle_payment_type";
    public static final String BUNDLE_PAYMENT_AMOUNT = "bundle_payment_amount";
    public static final String BUNDLE_CREDIT_PAYMENT_REF = "bundle_credit_payment_ref";
    public static final String BUNDLE_GATEWAY_ID = "bundle_gateway_id";
    public static final String BUNDLE_PAYLOAD = "bundle_payload";
    public static final String BUNDLE_RESPONSE = "bundle_response";
    public static final String BUNDLE_PAYMENTS = "bundle_payments";

    public static final String PAYMENT_TYPE_CASH = "Cash";
    public static final String PAYMENT_TYPE_CREDIT = "Card";
    public static final String PAYMENT_TYPE_CHECK = "Check";
    //public static final String PAYMENT_TYPE_CHECK = "CHEQUE";
    public static final String PAYMENT_TYPE_OTHER = "Other";
    public static final String PAYMENT_TYPE_SPLIT = "Split";

    public static final int DEVICE_STATUS_CONNECTED_SUCCESS = 0;
    public static final int DEVICE_STATUS_CONNECTED_FAIL = 1;
    public static final int DEVICE_STATUS_CONNECTED_PERMISSION_DENIED = 2;
    public static final int DEVICE_MESSAGE_CARDDATA_CHANGE = 3;
    public static final int DEVICE_STATUS_CONNECTED = 4;
    public static final int DEVICE_STATUS_DISCONNECTED = 5;

    private ImageView mSwipeStatusImageView;
    private TextView mSwipeTextView;
    private TextView mAmountTextView;

    private Button mAmount1Button;
    private Button mAmount2Button;
    private Button mAmount3Button;
    private Button mAmountOtherButton;
    private Button mCheckButton;
    private Button mOtherButton;
    private Button mCreditPayButton;

    private String mTotal;
    private BigDecimal mAmount1;
    private BigDecimal mAmount2;
    private BigDecimal mAmount3;
    private BigDecimal mAmountOther;

    private MagTeklibDynamag mMagTeklibDynamag;
    private Handler mReaderDataHandler = new Handler(new PaymentActivity.MtHandlerCallback());
    private String mStringCardDataBuffer;
    private String mPaymentMethod = "";
    private String mCreditPaymentReference = "";
    private String mPayload;
    private String mResponse;
    private BigDecimal mAmountDue;
    private ArrayList<Payment> mPayments = new ArrayList<>();

    //Tenpad elements
    private RadioGroup mDiscountRadioGroup;
    private RadioButton mAmountRadioButton;
    private RadioButton mPercentRadioButton;
    private ImageButton mDeleteImageButton;
    private Button mCancelButton;
    private Button mOkButton;
    private Button mButton0;
    private Button buttondot;
    private List<Button> mButtons;
    private LinearLayout mTenPadLayout;
    private LinearLayout mTitleLayout;
    private TextView mTitleTextView;
    private TextView mDisplayTextView;
    private TextView mPasswordEditView;
    private BigDecimal mValue;

    //End Tenpad elements

    private String mFragmentType="Approved";
    public static final String FRAGMENT_APPROVED = "Approved";
    public static final String FRAGMENT_CHANGED = "Changed";

    private ProductDatabase mDb;

    @Override
    public void onPayCompleted(String gatewayId) {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
        data.putExtra(BUNDLE_GATEWAY_ID, gatewayId);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onPayCompleted(String gatewayId, String imageSign,String paymentType)
    {
        Log.e("onPayCompleted",">>"+imageSign);
        Log.e("onPayCompleted",">>"+paymentType);
        for (Payment payment : mPayments)
        {
            if (payment.paymentType.equals(paymentType))
            {
                payment.gatewayId = gatewayId;
                payment.signImage = imageSign;
            }
        }
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
        data.putExtra(BUNDLE_GATEWAY_ID, gatewayId);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onPayCompleted(String gatewayId, String imageSign, String paymentType, String tipAmount)
    {
        for (Payment payment : mPayments)
        {
            if (payment.paymentType.equals(paymentType))
            {
                payment.paymentAmount = (payment.paymentAmount).add(new BigDecimal(tipAmount));
            }
        }
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
        data.putExtra(BUNDLE_GATEWAY_ID, gatewayId);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onPayFailed() {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
        data.putExtras(bundle);
        setResult(RESULT_FAILED, data);
        finish();
    }

    @Override
    public void onSignCompleted(String gatewayId, String sigImage)
    {
        for (Payment payment : mPayments)
        {
            if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT))
            {
                payment.gatewayId = gatewayId;
                payment.signImage = sigImage;
            }
        }
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
        data.putExtra(BUNDLE_GATEWAY_ID, gatewayId);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onSetDueAmount()
    {
        mAmountDue = getAmountDue();
        setUpUIs();
        setUpChanges();
    }
    //Tenpad Listeners

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mValue.toString().length() > 1) {
                mValue = new BigDecimal(mValue.toString().substring(0, mValue.toString().length() - 1));
                showDisplay();
            } else if (mValue.toString().length() == 1) {
                setValue(BigDecimal.ZERO);
                showDisplay();
            }
        }
    };

    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mValue != null) {
                if (mValue.compareTo(BigDecimal.ZERO) == 0) {
                    mDisplayTextView.setError(getString(R.string.txt_error_valid_amount));
                    return;
                } else {
                    mAmountOther = mValue.divide(Consts.HUNDRED);
                    Answers.getInstance().logCustom(new CustomEvent("Pay")
                            .putCustomAttribute("Type", "Cash")
                            .putCustomAttribute("Button", "ButtonOther")
                            .putCustomAttribute("Amount", mAmountOther));
                    if (Utils.formatTotal(mAmountDue).compareTo(Utils.formatTotal(mAmountOther)) > 0) {
                        addPayment(PAYMENT_TYPE_CASH, mValue, "");
                        mAmountDue = getAmountDue();
                        setUpUIs();
                        setUpChanges();
                        setValue(BigDecimal.ZERO);
                        showDisplay();
                    } else {
                        addPayment(PAYMENT_TYPE_CASH, mValue, "");
                        showChangeFragment(PAYMENT_TYPE_CASH);
                    }
                }//mPriceListener.onSetPrice(mValue);

            }
        }
    };

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = ((Button) v).getText().toString();

            switch (s) {
                case ".":
                    if (mValue != null && !mValue.toString().contains(".")) {
                        mValue = new BigDecimal(mValue + s);
                    }
                    break;

                case "00":
                    if (mValue != null && !mValue.toString().endsWith("0"))
                        mValue = new BigDecimal(mValue + s);
                    break;

                default:
                    if (!mValue.toString().equals("0")) {
                        if (mValue.toString().endsWith(".0"))
                            mValue = new BigDecimal(mValue.toString().replace(".0", ".") + s);
                        else if (getNumberOfFractionDigits(mValue) < 2)
                            mValue = new BigDecimal(mValue + s);
                    } else
                        mValue = new BigDecimal(s);
            }

            showDisplay();
        }
    };

    //End Tendpad Listeneres

    private View.OnClickListener mCheckClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mPaymentMethod = PAYMENT_TYPE_CHECK;
            Answers.getInstance().logCustom(new CustomEvent("Pay")
                    .putCustomAttribute("Type", "Check")
                    .putCustomAttribute("Button", "Check")
                    .putCustomAttribute("Amount", mAmountDue));
            TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_CASH, mAmountDue.multiply(Consts.HUNDRED));
            fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
                @Override
                public void onSetPrice(BigDecimal amount) {
                    addPayment(PAYMENT_TYPE_CHECK, amount, "");
                    mAmountDue = getAmountDue();
                    BigDecimal amountReceived = BigDecimal.ZERO;
                    setUpUIs();
                    setUpChanges();
                    for (Payment payment : mPayments) {
                        //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
                        amountReceived = amountReceived.add(payment.paymentAmount);
                    }
                    if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)), mPaymentMethod,mPayments);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    } else if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
                        showChangeFragment(PAYMENT_TYPE_CHECK);
                    }
                }
            });
        }
    };

    public View.OnClickListener mCashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            mPaymentMethod = PAYMENT_TYPE_CASH;
            if (v.equals(mAmount1Button))
            {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button1")
                        .putCustomAttribute("Amount", mAmount1));
                addPayment(PAYMENT_TYPE_CASH, mAmount1.multiply(Consts.HUNDRED), "");
                ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(new BigDecimal(mTotal)), mPaymentMethod, mPayments);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            } else if (v.equals(mAmount2Button)) {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button2")
                        .putCustomAttribute("Amount", mAmount2));
                addPayment(PAYMENT_TYPE_CASH, mAmount2.multiply(Consts.HUNDRED), "");
                BigDecimal amountReceived = BigDecimal.ZERO;
                for (Payment payment : mPayments) {
                    amountReceived = amountReceived.add(payment.paymentAmount);
                }
                if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                    ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmount2), mPaymentMethod, mPayments);
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                }else if(Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
                    showChangeFragment(PAYMENT_TYPE_CASH);
                }else{
                    mAmountDue = getAmountDue();
                    setUpUIs();
                    setUpChanges();
                }
            } else if (v.equals(mAmount3Button)) {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button3")
                        .putCustomAttribute("Amount", mAmount3));
                addPayment(PAYMENT_TYPE_CASH, mAmount3.multiply(Consts.HUNDRED),"");
                BigDecimal amountReceived = BigDecimal.ZERO;
                for (Payment payment : mPayments) {
                    amountReceived = amountReceived.add(payment.paymentAmount);
                }
                if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                    ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmount3), mPaymentMethod, mPayments);
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                }else if(Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
                    showChangeFragment(PAYMENT_TYPE_CASH);
                }else{
                    mAmountDue = getAmountDue();
                    setUpUIs();
                    setUpChanges();
                }
            } else if (v.equals(mAmountOtherButton)) {
                if (mAmountOtherButton.getText().toString().equals(getString(R.string.txt_cash_tender))) {
                    TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_CASH, new BigDecimal(mTotal).multiply(Consts.HUNDRED));
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
                        @Override
                        public void onSetPrice(BigDecimal amount) {
                            mAmountOther = amount.divide(Consts.HUNDRED);
                            Answers.getInstance().logCustom(new CustomEvent("Pay")
                                    .putCustomAttribute("Type", "Cash")
                                    .putCustomAttribute("Button", "ButtonOther")
                                    .putCustomAttribute("Amount", mAmountOther));
                            if (Utils.formatTotal(mAmountDue).compareTo(Utils.formatTotal(mAmountOther)) > 0)
                            {
                                addPayment(PAYMENT_TYPE_CASH, amount, "");
                                mAmountDue = getAmountDue();
                                setUpUIs();
                                setUpChanges();
                            }
                            else
                            {
                                addPayment(PAYMENT_TYPE_CASH, amount, "");
                                showChangeFragment(PAYMENT_TYPE_CASH);
                            }
                        }
                    });
                }
            }
        }
    };

    private View.OnClickListener mOtherClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (PrefUtils.getCashierInfo(PaymentActivity.this).permissionProcessTender)
            {
//                mPaymentMethod = PAYMENT_TYPE_OTHER;
//                Answers.getInstance().logCustom(new CustomEvent("Pay")
//                        .putCustomAttribute("Type", "Check")
//                        .putCustomAttribute("Button", "Check")
//                        .putCustomAttribute("Amount", mAmountDue));
//                TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_CASH, mAmountDue.multiply(Consts.HUNDRED));
//                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
//                fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
//                    @Override
//                    public void onSetPrice(BigDecimal amount) {
//                        addPayment(PAYMENT_TYPE_OTHER, amount);
//                        mAmountDue = getAmountDue();
//                        BigDecimal amountReceived = BigDecimal.ZERO;
//                        setUpUIs();
//                        setUpChanges();
//                        for (Payment payment : mPayments) {
//                            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
//                            amountReceived = amountReceived.add(payment.paymentAmount);
//                        }
//                        if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
//                            ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)), mPaymentMethod);
//                            fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
//                        } else if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
//                            showChangeFragment(PAYMENT_TYPE_OTHER);
//                        }
//                    }
//                });


                callOtherPayment();
            } else {
                TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                f.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {
                    @Override
                    public void onAdminAccessGranted() {
                        callOtherPayment();

                    }

                    @Override
                    public void onAdminAccessDenied() {

                    }
                });
            }
        }
    };

    private View.OnClickListener mCreditPayClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            Cursor cursor = mDb.getIngencioInfo();
            if(cursor.getCount() == 0)
            {
                AlertDialogFragment fragment = AlertDialogFragment.getInstance(PaymentActivity.this,
                        R.string.txt_card_reader_title, R.string.msg_card_reader, R.string.txt_continue, R.string.txt_cancel);
                fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        mPaymentMethod = PAYMENT_TYPE_CREDIT;
                        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED), "");
                        ApproveFragment approveFragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, mPayments);
                        approveFragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }

                    @Override
                    public void cancel() {

                    }
                });

                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                return;
            }
            if (PrefUtils.getAcceptTipsInfo(PaymentActivity.this).equalsIgnoreCase("YES")) {
                AlertDialogFragment fragment = AlertDialogFragment.getInstance(PaymentActivity.this, R.string.txt_tip_add, R.string.msg_tip_add, R.string.txt_yes, R.string.txt_no);
                fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        callTipPadFragment(PAYMENT_TYPE_CREDIT, getAmountDue().multiply(Consts.HUNDRED));
                    }

                    @Override
                    public void cancel() {
                        processIngenicoTransaction("", BigDecimal.ZERO, false);
                    }
                });
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            }else {
                processIngenicoTransaction("", BigDecimal.ZERO, false);
            }
            //Start Test Credit Card
            /*try{
                mPaymentMethod = PAYMENT_TYPE_CREDIT;
                addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));
                for(Payment payment : mPayments){
                    if(payment.paymentType.equals(PAYMENT_TYPE_CREDIT)) {
                        payment.gatewayId = "2";
                        payment.invoiceNo = "12345";
                        payment.authCode = "12345";
                        payment.cardType = "Visa";
                        payment.lastFour = "2345";
                        payment.tipAmount = payment.tipAmount.add(new BigDecimal(1).divide(Consts.HUNDRED));
                        //YYMMDD HHMMSS
                        payment.date = String.valueOf(getTime(getCurrentTimeStamp()));
                        ProductDatabase.getInstance(PaymentActivity.this).saveCreditSale(payment,PAYMENT_TYPE_CREDIT);
                    }
                }
                mFragmentType = FRAGMENT_APPROVED;
                callTipPadFragment(mPaymentMethod);
                return;*/
                /*if (StoreSetting.capture_sig) {
                    SignatureFragment fragment = SignatureFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, "2",
                            DecimalFormat.getCurrencyInstance().format(new BigDecimal(1).divide(Consts.HUNDRED)));
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    return;
                }
                ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, "2");
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);*/
            /*}catch (Exception e){
                Log.d(DEBUG_TAG, e.getMessage());
            }*/
            //End Text Credit Card
            /*RequestType.Force force = new RequestType.Force(getAmountDue().multiply(Consts.HUNDRED).toBigInteger().intValue());
            force.setEcrTenderType(new iConnectTsiTypes.EcrTenderType.Credit());*/

        }
    };

    private class MtHandlerCallback implements Handler.Callback {
        public boolean handleMessage(Message msg) {

            boolean ret = false;

            switch (msg.what) {
                case DEVICE_MESSAGE_CARDDATA_CHANGE:
                    Log.v(DEBUG_TAG, "DEVICE_MESSAGE_CARDDATA_CHANGE");
                    mStringCardDataBuffer = (String) msg.obj;
                    Log.v(DEBUG_TAG, "DataBuffer: " + mStringCardDataBuffer);
                    mMagTeklibDynamag.setCardData(mStringCardDataBuffer);
                    mPaymentMethod = PAYMENT_TYPE_CREDIT;
                    processMagTekTransaction();
                    break;

                case DEVICE_STATUS_CONNECTED:
                    Log.v(DEBUG_TAG, "DEVICE_STATUS_CONNECTED");
                    Toast.makeText(PaymentActivity.this, "device connected", Toast.LENGTH_LONG).show();
                    setSwipeStatus(true);
                    break;

                case DEVICE_STATUS_DISCONNECTED:
                    Log.v(DEBUG_TAG, "DEVICE_STATUS_DISCONNECTED");
                    setSwipeStatus(false);
                    break;

                default:
                    Log.v(DEBUG_TAG, "DEFAULT: what - " + msg.what);
                    ret = false;
                    break;
            }

            return ret;
        }
    }

    public void callOtherPayment() {
        mPaymentMethod = PAYMENT_TYPE_OTHER;
        Answers.getInstance().logCustom(new CustomEvent("Pay")
                .putCustomAttribute("Type", "Check")
                .putCustomAttribute("Button", "Check")
                .putCustomAttribute("Amount", mAmountDue));
        TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_CASH, mAmountDue.multiply(Consts.HUNDRED));
        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
        getSupportFragmentManager().executePendingTransactions();
        if(fragment.getDialog().isShowing())
            fragment.setAmount(mAmountDue.multiply(Consts.HUNDRED));
        fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
            @Override
            public void onSetPrice(BigDecimal amount) {
                addPayment(PAYMENT_TYPE_OTHER, amount, "");
                mAmountDue = getAmountDue();
                BigDecimal amountReceived = BigDecimal.ZERO;
                setUpUIs();
                setUpChanges();
                for (Payment payment : mPayments) {
                    //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
                    amountReceived = amountReceived.add(payment.paymentAmount);
                }
                if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                    //mFragmentType = FRAGMENT_APPROVED;
                    //callTipPadFragment(PAYMENT_TYPE_OTHER);
                    ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)), mPaymentMethod, mPayments);
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                } else if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
                    //mFragmentType = FRAGMENT_CHANGED;
                    //callTipPadFragment(PAYMENT_TYPE_OTHER);
                    showChangeFragment(PAYMENT_TYPE_OTHER);
                }
            }
        });
    }

    public void callTipPadFragment(String paymentType, final BigDecimal amount) {
        String amountReceived = new BigDecimal(getAmountReceived(paymentType)).add(amount.divide(Consts.HUNDRED)).toString();

        TipPadDialogFragment fragmentTip = TipPadDialogFragment.newInstance(new BigDecimal(mTotal), paymentType, mFragmentType, amountReceived);
        fragmentTip.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
        fragmentTip.setTipPriceListener(new TipPadDialogFragment.TipPriceListener()
        {
            @Override
            public void onSetPrice(BigDecimal amount, String paymentType)
            {
                if(paymentType.equals(PAYMENT_TYPE_CREDIT)){
                    return;
                    /*if(mPayments.size() == 0)
                        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));*/
                }
                for (Payment payment : mPayments)
                {
                    if (payment.paymentType.equals(paymentType))
                    {
                        payment.tipAmount = payment.tipAmount.add(amount);
                    }
                }
            }

            @Override
            public void onSetTipZeroPrice(BigDecimal amount, String paymentType) {
                if(paymentType.equals(PAYMENT_TYPE_CREDIT))
                {
                    return;
                    /*if(mPayments.size() == 0)
                        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));*/
                }
                for (Payment payment : mPayments)
                {
                    if (payment.paymentType.equals(paymentType))
                    {
                        payment.tipAmount = BigDecimal.ZERO;
                        Log.d("onSetTipZeroPrice","Total Tipamount after"+payment.tipAmount);
                    }
                }
            }

            @Override
            public void onSetSignImage(String imageSign, String paymentType) {
                Log.e("onPayCompleted",">>"+imageSign);
                Log.e("onPayCompleted",">>"+paymentType);
                if(paymentType.equals(PAYMENT_TYPE_CREDIT))
                {
                    return;
                    /*if(mPayments.size() == 0)
                        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));*/
                }
                for (Payment payment : mPayments)
                {
                    if (payment.paymentType.equals(paymentType))
                    {
                        payment.signImage = imageSign;
                    }
                }
            }

            @Override
            public void onCreditPayment(String signImage, BigDecimal tipAmount) {
                processIngenicoTransaction(signImage, tipAmount, true);
            }

            @Override
            public void onComplete(String signImage, BigDecimal tipAmount, String paymentType) {
                addPayment(paymentType, amount , signImage);
                for (Payment payment : mPayments) {
                    if (payment.paymentType.equals(paymentType)) {
                        payment.tipAmount = tipAmount;
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        Log.e(DEBUG_TAG, "onCreate");
        setContentView(R.layout.activity_payment);
        ViewGroup vg = (ViewGroup) getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
        try {

            mTotal = getIntent().getStringExtra(BUNDLE_AMOUNT);
            mAmountDue = new BigDecimal(mTotal);
            Bundle bundle = getIntent().getExtras();
            //mTotal = (String) bundle.getSerializable(BUNDLE_AMOUNT);

            //mAmountDue = new BigDecimal(mTotal);

            mPayments = (ArrayList<Payment>) bundle.getSerializable(BUNDLE_PAYMENTS);


            if (mPayments.size() > 0)
            {
                mAmountDue = getAmountDue();
            }

            mPayload = "";

            bindUIElements();
            bindTenPadUIElements();
            setUpListeners();
            setUpTenPadListeners();
            setUpUIs();
            setUpTenPadUI();
            setUpChanges();

            mMagTeklibDynamag = new MagTeklibDynamag(this, mReaderDataHandler);
            mMagTeklibDynamag.clearCardData();
            mDb = ProductDatabase.getInstance(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(BUNDLE_PAYMENTS, mPayments);
                data.putExtras(bundle);
                setResult(RESULT_CANCELED, data);
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("start", "start");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.e("resume", "on resume");
        if (!mMagTeklibDynamag.isDeviceConnected()) {
            mMagTeklibDynamag.openDevice();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e("pause", "pasuse");
        if (mMagTeklibDynamag != null) {
            mMagTeklibDynamag.closeDevice();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMagTeklibDynamag != null) {
            if (mMagTeklibDynamag.isDeviceConnected()) {
                mMagTeklibDynamag.closeDevice();
            }
        }
    }

    private void bindUIElements() {
        mSwipeStatusImageView = (ImageView) findViewById(R.id.swipe_status_image_view);
        mSwipeTextView = (TextView) findViewById(R.id.swipe_text_view);
        mAmount1Button = (Button) findViewById(R.id.amount_1_button);
        mAmount2Button = (Button) findViewById(R.id.amount_2_button);
        mAmount3Button = (Button) findViewById(R.id.amount_3_button);
        mAmountOtherButton = (Button) findViewById(R.id.amount_other_button);
        mCheckButton = (Button) findViewById(R.id.check_button);
        mAmountTextView = (TextView) findViewById(R.id.amount_text_view);
        mOtherButton = (Button) findViewById(R.id.other_button);
        mCreditPayButton = (Button) findViewById(R.id.credit_button);
    }

    private void bindTenPadUIElements() {
        mTenPadLayout = (LinearLayout) findViewById(R.id.ten_pad_layout);
        mTitleLayout = (LinearLayout) findViewById(R.id.ten_pad_title_layout);
        mTitleTextView = (TextView) findViewById(R.id.ten_pad_title_text_view);
        mDisplayTextView = (TextView) findViewById(R.id.display_text_view);
        mPasswordEditView = (EditText) findViewById(R.id.password_edit_text);
        mDiscountRadioGroup = (RadioGroup) findViewById(R.id.ten_pad_discount_radio_group);
        mAmountRadioButton = (RadioButton) findViewById(R.id.ten_pad_amount_radio_button);
        mPercentRadioButton = (RadioButton) findViewById(R.id.ten_pad_percent_radio_button);
        mButtons = new ArrayList<>();
        Button button1 = (Button) findViewById(R.id.ten_pad_1_button);
        Button button2 = (Button) findViewById(R.id.ten_pad_2_button);
        Button button3 = (Button) findViewById(R.id.ten_pad_3_button);
        Button button4 = (Button) findViewById(R.id.ten_pad_4_button);
        Button button5 = (Button) findViewById(R.id.ten_pad_5_button);
        Button button6 = (Button) findViewById(R.id.ten_pad_6_button);
        Button button7 = (Button) findViewById(R.id.ten_pad_7_button);
        Button button8 = (Button) findViewById(R.id.ten_pad_8_button);
        Button button9 = (Button) findViewById(R.id.ten_pad_9_button);
        mButton0 = (Button) findViewById(R.id.ten_pad_0_button);
        Button button00 = (Button) findViewById(R.id.ten_pad_00_button);
        buttondot = (Button) findViewById(R.id.ten_pad_dot_button);
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
        mDeleteImageButton = (ImageButton) findViewById(R.id.ten_pad_delete_image_button);
        mCancelButton = (Button) findViewById(R.id.ten_pad_cancel_button);
        mOkButton = (Button) findViewById(R.id.ten_pad_ok_button);
    }

    private void setUpListeners() {
        mAmount1Button.setOnClickListener(mCashClickListener);
        mAmount2Button.setOnClickListener(mCashClickListener);
        mAmount3Button.setOnClickListener(mCashClickListener);
        mCheckButton.setOnClickListener(mCheckClickListener);
        mOtherButton.setOnClickListener(mOtherClickListener);
        mCreditPayButton.setOnClickListener(mCreditPayClickListener);
    }

    private void setUpTenPadListeners() {
        mDeleteImageButton.setOnClickListener(mDeleteClickListener);
        mOkButton.setOnClickListener(mOkClickListener);
        for (Button button : mButtons) {
            button.setOnClickListener(mButtonClickListener);
        }
    }

    public void setUpUIs() {
        mAmount1Button.setText(DecimalFormat.getCurrencyInstance().format(mAmountDue));
        mAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mAmountDue));
    }

    public void setUpChanges() {
        mAmount1 = mAmountDue;

        if (mAmount1.compareTo(new BigDecimal(5)) < 0) {
            mAmount2 = new BigDecimal(5);
            mAmount3 = new BigDecimal(10);
        } else if (mAmount1.compareTo(new BigDecimal(10)) < 0) {
            mAmount2 = new BigDecimal(10);
            mAmount3 = new BigDecimal(20);
        } else if (mAmount1.compareTo(new BigDecimal(20)) < 0) {
            mAmount2 = new BigDecimal(20);
            mAmount3 = new BigDecimal(50);
        } else if (mAmount1.compareTo(new BigDecimal(50)) < 0) {
            mAmount2 = new BigDecimal(50);
            mAmount3 = new BigDecimal(100);
        } else {
            mAmount2 = new BigDecimal(100);
            mAmount3 = new BigDecimal(200);
        }
        mAmount2Button.setText(DecimalFormat.getCurrencyInstance().format(mAmount2));
        mAmount3Button.setText(DecimalFormat.getCurrencyInstance().format(mAmount3));
    }

    private void setUpTenPadUI() {
        mTitleTextView.setText(getString(R.string.txt_cash));
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleTextView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"));
        mTitleTextView.setGravity(Gravity.RIGHT);
        mPercentRadioButton.setVisibility(View.GONE);
        mPasswordEditView.setVisibility(View.GONE);
        mOkButton.setText(getString(R.string.txt_pay));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(175, 75);
        mOkButton.setLayoutParams(params);
        int color = com.pos.passport.ui.Utils.getColor(PaymentActivity.this, android.R.color.black);
        mOkButton.setTextColor(color);
        mOkButton.setBackgroundResource(R.drawable.border_blue_gray_back);
        mTenPadLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary_color));
        initValue();
        showDisplay();
    }

    private void processIngenicoTransaction(final String signImage, final BigDecimal tipAmount, final boolean hasSignature){

        //START TEST
        /*try {
            mPaymentMethod = PAYMENT_TYPE_CREDIT;
            addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED), signImage);
            for (Payment payment : mPayments)
            {
                if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT)) {
                    payment.gatewayId = "123";
                    payment.cardType = "VISA";
                    payment.tipAmount = tipAmount;
                    payment.signImage = signImage;
                    payment.lastFour = "34567";
                    if (true) {
                        payment.tipAmount = payment.tipAmount.add(new BigDecimal(0).divide(Consts.HUNDRED));
                    }
                    //YYMMDD HHMMSS
                    payment.date = String.valueOf(getTime(getCurrentTimeStamp()));
                    ProductDatabase.getInstance(PaymentActivity.this).saveCreditSale(payment, PAYMENT_TYPE_CREDIT);
                }
            }


            if (!hasSignature && StoreSetting.capture_sig) {
                SignatureFragment fragment = SignatureFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, "123");
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            }else{
                ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, "123", mPayments);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            }
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.getMessage());
        } */
        //END TEST
        //PRODUCTION START
        RequestType.Sale saleReq = new RequestType.Sale((getAmountDue().add(tipAmount)).multiply(Consts.HUNDRED).toBigInteger().intValue());
        saleReq.setClerkId(1);
        saleReq.setEcrTenderType(new iConnectTsiTypes.EcrTenderType.Credit());

        IngenicoProcessRequestAsyncTask requestAsyncTask = new IngenicoProcessRequestAsyncTask(PaymentActivity.this);

        requestAsyncTask.setListener(new IngenicoProcessRequestAsyncTask.IngenicoListener() {
            @Override
            public void success(ResponseType.Sale response) {
                try {
                    mPaymentMethod = PAYMENT_TYPE_CREDIT;
                    addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED), signImage);
                    for (Payment payment : mPayments)
                    {
                        if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT)) {
                            payment.gatewayId = response.getReferenceNo();
                            if(response.containsAuthorizationNo()) {
                                payment.invoiceNo = response.getAuthorizationNo();
                                payment.authCode = response.getAuthorizationNo();
                            }
                            payment.cardType = response.getCustomerCardType().toString();
                            payment.tipAmount = tipAmount;
                            payment.signImage = signImage;
                            payment.lastFour = (response.getCustomerAccountNo()).substring((response.getCustomerAccountNo()).length() - 4);
                            if (response.containsTipAmount()) {
                                payment.tipAmount = payment.tipAmount.add(new BigDecimal(response.getTipAmount()).divide(Consts.HUNDRED));
                            }
                            //YYMMDD HHMMSS
                            payment.date = String.valueOf(getTime(getCurrentTimeStamp()));
                            ProductDatabase.getInstance(PaymentActivity.this).saveCreditSale(payment, PAYMENT_TYPE_CREDIT);
                        }
                    }
                    String tipAmount = getString(R.string.txt_price_0);
                    if (response.containsTipAmount())
                    {
                        tipAmount = DecimalFormat.getCurrencyInstance().format(new BigDecimal(response.getTipAmount()).divide(Consts.HUNDRED));
                    }

                    if (!hasSignature && StoreSetting.capture_sig) {
                        SignatureFragment fragment = SignatureFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, response.getReferenceNo());
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }else{
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, response.getReferenceNo(), mPayments);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }
                } catch (TsiException e) {
                    Log.d(DEBUG_TAG, e.getMessage());
                }
            }

            @Override
            public void failed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.alertBox(PaymentActivity.this, "Error", "Check Device connection and try again");
                    }
                });

            }
        });

        requestAsyncTask.execute(saleReq);

        //PRODUCTION END
    }

    private void processMagTekTransaction() {
        try {
            String data = mMagTeklibDynamag.getTrack1Masked();
            String track1 = mMagTeklibDynamag.getTrack1();
            String track2 = mMagTeklibDynamag.getTrack2();
            String ksn = mMagTeklibDynamag.getKSN();
            String[] separated = data.split("\\^");
            String expYear = separated[2].substring(0, 2);
            String expMonth = separated[2].substring(2, 4);

            BaseGateway gateway = null;
            if (BuildConfig.GATEWAY.equals("clearent")) {
                gateway = new ClearentGateway(PaymentActivity.this, R.raw.gateway_clearent);
                gateway.setSandBoxEnabled(true);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "SALE");
                jsonObject.put("amount", Utils.formatTotal(mAmountDue.toString()));
                jsonObject.put("type", BaseGateway.TRANSACTION_SALE);
                jsonObject.put("exp-date", expMonth + expYear);
                jsonObject.put("track-format", "MAGTEK");
                jsonObject.put("encrypted-track-data", mStringCardDataBuffer);
                mPayload = jsonObject.toString();
            } else if (BuildConfig.GATEWAY.equals("bridgepay")) {
                gateway = new BridgePayGateway(PaymentActivity.this, R.raw.gateway_bridgepay);
                ProcessCreditCard cProcess = new ProcessCreditCard();
                gateway.setSandBoxEnabled(true);
                cProcess.setExtData(ksn, track1, track2);
                cProcess.setAmount(Utils.formatTotal(mAmountDue.toString()));
                cProcess.setUserName(WebSetting.merchantID);
                cProcess.setTransType(BaseGateway.TRANSACTION_SALE);
                cProcess.setPassword(WebSetting.webServicePassword);
                cProcess.setMerchantID(WebSetting.hostedMID);
                mPayload = cProcess.generateXmlString();
            } else if (BuildConfig.GATEWAY.equals("priority")) {
                gateway = new PriorityGateway(PaymentActivity.this, R.raw.gateway_priority);
                gateway.setSandBoxEnabled(true);
                JSONObject cardDetails = new JSONObject();
                cardDetails.put("magstripe", mStringCardDataBuffer);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("merchantId", WebSetting.merchantID);
                jsonObject.put("tenderType", "Card");
                jsonObject.put("cardAccount", cardDetails);
                mPayload = jsonObject.toString();
            }

            if (gateway == null)
                return;
            gateway.processSale(null, mPayload, new TransactionListener() {
                @Override
                public void onOffline() {
                    OfflineOption option = PrefUtils.getOfflineOption(PaymentActivity.this);
                    if (option.isShowingMessage() || Utils.checkTimeSpan(option.getTimestamp(), 12)) {
                        SaveTransactionFragment fragment = new SaveTransactionFragment();
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                        fragment.setSaveTransactionListener(new SaveTransactionFragment.SaveTransactionListener() {
                            @Override
                            public void onSaveSale() {
                                onPayFailed();
                            }

                            @Override
                            public void onCancelSale() {

                            }
                        });
                    } else {
                        onPayFailed();
                    }
                }

                @Override
                public void onApproved(String id, String gatewayId, String response) {
                    mResponse = response;
                    if (StoreSetting.capture_sig) {
                        SignatureFragment fragment = SignatureFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, gatewayId);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                        return;
                    }
                    addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED), "");
                    for (Payment payment : mPayments) {
                        if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT))
                            payment.gatewayId = gatewayId;
                    }

                    ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, gatewayId,mPayments);
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                }

                @Override
                public void onError(String message) {
                    Utils.alertBox(PaymentActivity.this, R.string.txt_error, message);
                }

                @Override
                public void onDeclined(String message) {
                    Utils.alertBox(PaymentActivity.this, R.string.txt_declined, message);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            clearMessageBuffer();
        }
    }

    private void clearMessageBuffer() {
        mStringCardDataBuffer = "";
    }

    private void setSwipeStatus(boolean connected) {
        if (connected) {
            mSwipeTextView.setEnabled(true);
            mSwipeStatusImageView.setImageResource(R.drawable.ic_circle_green_24dp);
        } else {
            mSwipeTextView.setEnabled(false);
            mSwipeStatusImageView.setImageResource(R.drawable.ic_circle_red_24dp);
        }
    }

    private void addPayment(String paymentType, BigDecimal payAmount, String sign) {
        payAmount = payAmount.divide(Consts.HUNDRED);

        if (mPayments.size() > 0) {
            for (Payment payment : mPayments) {
                if (payment.paymentType.equals(paymentType)) {
                    payment.paymentAmount = payAmount.add(payment.paymentAmount);
                    payment.signImage = sign;
                    return;
                }
            }
        }
        Payment payment = new Payment();
        payment.paymentType = paymentType;
        payment.paymentAmount = payAmount;
        payment.payload = mPayload;
        payment.response = mResponse;
        payment.signImage = sign;
        mPayments.add(payment);
        Log.e("Payment size", "" + mPayments.size());

    }

    public BigDecimal getAmountDue()
    {
        BigDecimal amountReceived = BigDecimal.ZERO;
        BigDecimal amountReceivedTip = BigDecimal.ZERO;
        BigDecimal CartTotal = new BigDecimal(mTotal);
        for (Payment payment : mPayments) {
            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
            amountReceived = amountReceived.add(payment.paymentAmount);
            amountReceivedTip = amountReceivedTip.add(payment.tipAmount);
        }
        return CartTotal.subtract(amountReceived).add(amountReceivedTip);
    }

    public void showChangeFragment(String paymentType)
    {
        BigDecimal CartTotal = new BigDecimal(mTotal);
        BigDecimal amountReceived = BigDecimal.ZERO;
        for (Payment payment : mPayments)
        {
            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
            amountReceived = amountReceived.add(payment.paymentAmount);
        }
        if (mPayments.size() > 1)
            paymentType = PAYMENT_TYPE_SPLIT;
        ChangeFragment fragment = ChangeFragment.newInstance(amountReceived.toString(), amountReceived.subtract(CartTotal).toString(), paymentType);
        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
    }

    public String getAmountReceived(String paymentType){
        BigDecimal CartTotal = new BigDecimal(mTotal);
        BigDecimal amountReceived = BigDecimal.ZERO;
        for (Payment payment : mPayments)
        {
            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
            amountReceived = amountReceived.add(payment.paymentAmount);
        }
        if (mPayments.size() > 1)
            paymentType = PAYMENT_TYPE_SPLIT;

        return amountReceived.toString();
       // ChangeFragment fragment = ChangeFragment.newInstance(amountReceived.toString(), amountReceived.subtract(CartTotal).toString(), paymentType);
        //fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
    }
    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd HHmmss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static long getTime(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyMMdd HHmmss");
            Date dateParser = (Date) formatter.parse(date);
            return dateParser.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void initValue() {
        mValue = BigDecimal.ZERO;
    }

    private void setValue(BigDecimal value) {
        mValue = value;
    }

    private void showDisplay() {
        mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));
    }

    private int getNumberOfFractionDigits(BigDecimal number) {
        String digits[] = number.toString().split("\\.");
        if (digits.length < 2)
            return 0;

        return digits[1].length();
    }
}
