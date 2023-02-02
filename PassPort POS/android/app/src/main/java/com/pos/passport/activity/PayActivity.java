package com.pos.passport.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.pos.passport.fragment.ApproveFragment;
import com.pos.passport.fragment.ChangeFragment;
import com.pos.passport.fragment.SaveTransactionFragment;
import com.pos.passport.fragment.SignatureFragment;
import com.pos.passport.fragment.TenPadDialogFragment;
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

/**
 * Created by karim on 11/24/15.
 */
public class PayActivity extends BaseActivity implements PayInterface {
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
    public static final int DEVICE_MESSAGE_CARDDATA_CHANGE =3;
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
    private Handler mReaderDataHandler = new Handler(new MtHandlerCallback());
    private String mStringCardDataBuffer;
    private String mPaymentMethod = "";
    private String mCreditPaymentReference= "";
    private String mPayload;
    private String mResponse;
    private BigDecimal mAmountDue;
    private ArrayList<Payment> mPayments = new ArrayList<>();

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
    public void onPayCompleted(String gatewayId, String imageSign, String paymentType) {

    }

    @Override
    public void onPayCompleted(String gatewayId, String imageSign, String paymentType, String tipAmount) {

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
    public void onSignCompleted(String gatewayId, String sigImage) {
        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));
        for (Payment payment : mPayments) {
            if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT)) {
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
                    addPayment(PAYMENT_TYPE_CHECK, amount);
                    mAmountDue = getAmountDue();
                    BigDecimal amountReceived = BigDecimal.ZERO;
                    setUpUIs();
                    setUpChanges();
                    for (Payment payment : mPayments) {
                        //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
                        amountReceived = amountReceived.add(payment.paymentAmount);
                    }
                    if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)), mPaymentMethod, mPayments);
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
        public void onClick(View v) {
            mPaymentMethod = PAYMENT_TYPE_CASH;
            if (v.equals(mAmount1Button)) {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button1")
                        .putCustomAttribute("Amount", mAmount1));
                addPayment(PAYMENT_TYPE_CASH, mAmount1.multiply(Consts.HUNDRED));
                ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(new BigDecimal(mTotal)), mPaymentMethod, mPayments);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            } else if (v.equals(mAmount2Button)) {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button2")
                        .putCustomAttribute("Amount", mAmount2));
                addPayment(PAYMENT_TYPE_CASH, mAmount2.multiply(Consts.HUNDRED));
                showChangeFragment(PAYMENT_TYPE_CASH);
            } else if (v.equals(mAmount3Button)) {
                Answers.getInstance().logCustom(new CustomEvent("Pay")
                        .putCustomAttribute("Type", "Cash")
                        .putCustomAttribute("Button", "Button3")
                        .putCustomAttribute("Amount", mAmount3));
                addPayment(PAYMENT_TYPE_CASH, mAmount3.multiply(Consts.HUNDRED));
                showChangeFragment(PAYMENT_TYPE_CASH);
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
                            if (Utils.formatTotal(mAmountDue).compareTo(Utils.formatTotal(mAmountOther)) > 0) {
                                addPayment(PAYMENT_TYPE_CASH, amount);
                                mAmountDue = getAmountDue();
                                setUpUIs();
                                setUpChanges();
                            } else {
                                addPayment(PAYMENT_TYPE_CASH, amount);
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
            mPaymentMethod = PAYMENT_TYPE_OTHER;
            Answers.getInstance().logCustom(new CustomEvent("Pay")
                    .putCustomAttribute("Type", "Check")
                    .putCustomAttribute("Button", "Check")
                    .putCustomAttribute("Amount", mAmountDue));
            TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_CASH, mAmountDue.multiply(Consts.HUNDRED));
            fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
            fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
                @Override
                public void onSetPrice(BigDecimal amount) {
                    addPayment(PAYMENT_TYPE_OTHER, amount);
                    mAmountDue = getAmountDue();
                    BigDecimal amountReceived = BigDecimal.ZERO;
                    setUpUIs();
                    setUpChanges();
                    for (Payment payment : mPayments) {
                        //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
                        amountReceived = amountReceived.add(payment.paymentAmount);
                    }
                    if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) == 0) {
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)), mPaymentMethod, mPayments);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    } else if (Utils.formatTotal(amountReceived).compareTo(Utils.formatTotal(new BigDecimal(mTotal))) > 0) {
                        showChangeFragment(PAYMENT_TYPE_OTHER);
                    }
                }
            });
        }
    };

    private View.OnClickListener mCreditPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RequestType.Sale saleReq = new RequestType.Sale(getAmountDue().multiply(Consts.HUNDRED).toBigInteger().intValue());
            saleReq.setClerkId(1);
            saleReq.setEcrTenderType(new iConnectTsiTypes.EcrTenderType.Credit());

            IngenicoProcessRequestAsyncTask requestAsyncTask = new IngenicoProcessRequestAsyncTask(PayActivity.this);

            requestAsyncTask.setListener(new IngenicoProcessRequestAsyncTask.IngenicoListener() {
                @Override
                public void success(ResponseType.Sale response) {
                    try{
                        addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));
                        for(Payment payment : mPayments){
                            if(payment.paymentType.equals(PAYMENT_TYPE_CREDIT)) {
                                payment.gatewayId = response.getReferenceNo();
                                payment.invoiceNo = response.getAuthorizationNo();
                                payment.authCode = response.getAuthorizationNo();
                                payment.cardType = response.getCustomerCardType().toString();
                                payment.lastFour = response.getCustomerAccountNo();
                                //YYMMDD HHMMSS
                                payment.date = String.valueOf(getTime(getCurrentTimeStamp()));
                                ProductDatabase.getInstance(PayActivity.this).saveCreditSale(payment,PAYMENT_TYPE_CREDIT);
                            }
                        }
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, response.getReferenceNo(),mPayments);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }catch (TsiException e){
                        Log.d(DEBUG_TAG, e.getMessage());
                    }
                }

                @Override
                public void failed() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.alertBox(PayActivity.this, "Error", "Check Device connection and try again");
                        }
                    });

                }
            });

            requestAsyncTask.execute(saleReq);
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
                    processTransaction();
                    break;

                case DEVICE_STATUS_CONNECTED:
                    Log.v(DEBUG_TAG, "DEVICE_STATUS_CONNECTED");
                    Toast.makeText(PayActivity.this, "device connected", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        Log.e(DEBUG_TAG, "onCreate");
        setContentView(R.layout.activity_pay);
        ViewGroup vg = (ViewGroup)getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
        try {

            mTotal = getIntent().getStringExtra(BUNDLE_AMOUNT);
            mAmountDue = new BigDecimal(mTotal);
            Bundle bundle = getIntent().getExtras();
            //mTotal = (String) bundle.getSerializable(BUNDLE_AMOUNT);

            //mAmountDue = new BigDecimal(mTotal);

            mPayments = (ArrayList<Payment>) bundle.getSerializable(BUNDLE_PAYMENTS);


            if (mPayments.size() > 0) {

                mAmountDue = getAmountDue();
            }

            mPayload = "";

            bindUIElements();

            setUpListeners();

            setUpUIs();

            setUpChanges();

            mMagTeklibDynamag = new MagTeklibDynamag(this, mReaderDataHandler);

            mMagTeklibDynamag.clearCardData();

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

    private void setUpListeners() {
        mAmount1Button.setOnClickListener(mCashClickListener);
        mAmount2Button.setOnClickListener(mCashClickListener);
        mAmount3Button.setOnClickListener(mCashClickListener);
        mAmountOtherButton.setOnClickListener(mCashClickListener);
        mCheckButton.setOnClickListener(mCheckClickListener);
        mOtherButton.setOnClickListener(mOtherClickListener);
        mCreditPayButton.setOnClickListener(mCreditPayClickListener);
    }

    private void setUpUIs() {
        mAmount1Button.setText(DecimalFormat.getCurrencyInstance().format(mAmountDue));
        mAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mAmountDue));
    }

    private void setUpChanges() {
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

    private void processTransaction() {
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
                gateway = new ClearentGateway(PayActivity.this, R.raw.gateway_clearent);
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
                gateway = new BridgePayGateway(PayActivity.this, R.raw.gateway_bridgepay);
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
                gateway = new PriorityGateway(PayActivity.this, R.raw.gateway_priority);
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
                    OfflineOption option = PrefUtils.getOfflineOption(PayActivity.this);
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
                    addPayment(PAYMENT_TYPE_CREDIT, mAmountDue.multiply(Consts.HUNDRED));
                    for (Payment payment : mPayments) {
                        if (payment.paymentType.equals(PAYMENT_TYPE_CREDIT))
                            payment.gatewayId = gatewayId;
                    }

                    ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mAmountDue), mPaymentMethod, gatewayId, mPayments);
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                }

                @Override
                public void onError(String message) {
                    Utils.alertBox(PayActivity.this, R.string.txt_error, message);
                }

                @Override
                public void onDeclined(String message) {
                    Utils.alertBox(PayActivity.this, R.string.txt_declined, message);
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

    private void addPayment(String paymentType, BigDecimal payAmount) {
        payAmount = payAmount.divide(Consts.HUNDRED);

        if (mPayments.size() > 0) {
            for (Payment payment : mPayments) {
                if (payment.paymentType.equals(paymentType)) {
                    payment.paymentAmount = payAmount.add(payment.paymentAmount);
                    return;
                }
            }
        }
        Payment payment = new Payment();
        payment.paymentType = paymentType;
        payment.paymentAmount = payAmount;
        payment.payload = mPayload;
        payment.response = mResponse;
        mPayments.add(payment);
        Log.e("Payment size", "" + mPayments.size());

    }

    private BigDecimal getAmountDue() {
        BigDecimal amountReceived = BigDecimal.ZERO;
        BigDecimal CartTotal = new BigDecimal(mTotal);
        for (Payment payment : mPayments) {
            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
            amountReceived = amountReceived.add(payment.paymentAmount);
        }
        return CartTotal.subtract(amountReceived);
    }

    private void showChangeFragment(String paymentType) {
        BigDecimal CartTotal = new BigDecimal(mTotal);
        BigDecimal amountReceived = BigDecimal.ZERO;
        for (Payment payment : mPayments) {
            //amountReceived = amountReceived.add(payment.paymentAmount.divide(Consts.HUNDRED));
            amountReceived = amountReceived.add(payment.paymentAmount);
        }
        if (mPayments.size() > 1)
            paymentType = PAYMENT_TYPE_SPLIT;
        ChangeFragment fragment = ChangeFragment.newInstance(amountReceived.toString(), amountReceived.subtract(CartTotal).toString(), paymentType);
        fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd HHmmss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static long getTime(String date){
        try {
            DateFormat formatter = new SimpleDateFormat("yyMMdd HHmmss");
            Date dateParser = (Date) formatter.parse(date);
            return dateParser.getTime();
        }catch (ParseException e){
            e.printStackTrace();
        }

        return 0;
    }
}
