package com.pos.passport.fragment;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.activity.PaymentActivity;
import com.pos.passport.interfaces.PayInterface;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class TipPadDialogFragment extends DialogFragment
{
    private Typeface mNatoSanBolad;
    //private Context mCallback;
    private BigDecimal mMaxValue;
    private LinearLayout mNoTipLayout;
    private LinearLayout mTipFiftenLayout;
    private LinearLayout mTipTwentyLayout;
    private LinearLayout mTipTwentyFiveLayout;
    private LinearLayout mTipCustomLayout;
    private LinearLayout mClearSignLayout;
    private LinearLayout mAcceptSignLayout;
    private TextView mNoTipTextView;
    private TextView mTipFiftenTextView;
    private TextView mTipTwentyTextView;
    private TextView mTipTwentyFiveTextView;
    private TextView mTipCustomTextView;
    private TextView mAmountTextView;
    private ImageView mCloseImageView;
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private final int TIP_AMOUNT_PERCENT_ONE = 15;
    private final int TIP_AMOUNT_PERCENT_TWO = 20;
    private final int TIP_AMOUNT_PERCENT_THREE = 25;


    private LinearLayout mContent;
    private SignatureView mSignatureView;
    private static String tempDir;
    private int count = 1;
    private String current = null;
    private Bitmap mBitmap;
    private View mView;
    private File mypath;
    private String uniqueId;
    private ArrayList<LinearLayout> mTipLinearLayoutButtons;
    public String PAYMENT_TYPE = "Cash";
    public String FRAGMENT_TYPE = "";
    public String amountReceived;
    private PayInterface mCallback;
    private TipPriceListener mPriceListener;
    PaymentActivity paymentActivity;
    private BigDecimal mTipAmount = BigDecimal.ZERO;

    public interface TipPriceListener {
        void onSetPrice(BigDecimal amount,String paymentType);
        void onSetTipZeroPrice(BigDecimal amount,String paymentType);
        void onSetSignImage(String imageSign ,String paymentType);
        void onCreditPayment(String signImage, BigDecimal tipAmount);
        void onComplete(String signImage, BigDecimal tipAmount, String paymentType);
    }

    public void setTipPriceListener(TipPriceListener l) {
        mPriceListener = l;
    }

    public static TipPadDialogFragment newInstance(BigDecimal amount,String paymentType,String mfragmentType,String amountReceived)
    {
        TipPadDialogFragment f = new TipPadDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("maxAmount", amount);
        args.putSerializable("paymentType", paymentType);
        args.putSerializable("fragmentType", mfragmentType);
        args.putSerializable("amountReceived", amountReceived);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mNatoSanBolad = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
        paymentActivity=new PaymentActivity();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        //mCallback = context;

        try {
            mCallback = (PayInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PayInterface");
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v ;
        v = inflater.inflate(R.layout.fragment_tip_pad_frame, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().getWindow().setBackgroundDrawable( new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            if (getArguments().getSerializable("maxAmount") != null)
                mMaxValue = (BigDecimal)getArguments().getSerializable("maxAmount");
            else
                mMaxValue = BigDecimal.ZERO;

            if (getArguments().getSerializable("paymentType") != null)
                PAYMENT_TYPE = (String) getArguments().getSerializable("paymentType");

            if (getArguments().getSerializable("fragmentType") != null)
                FRAGMENT_TYPE = (String) getArguments().getSerializable("fragmentType");

            amountReceived = (String) getArguments().getSerializable("amountReceived");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            mMaxValue = BigDecimal.ZERO;
        }
        bindUIElements(view);
        setUI();
        setUpListeners();
    }

    private void bindUIElements(View v)
    {

        mNoTipLayout = (LinearLayout) v.findViewById(R.id.no_tip_ll);
        mTipFiftenLayout = (LinearLayout) v.findViewById(R.id.tip_one_ll);
        mTipTwentyLayout = (LinearLayout) v.findViewById(R.id.tip_two_ll);
        mTipTwentyFiveLayout = (LinearLayout) v.findViewById(R.id.tip_three_ll);
        mTipCustomLayout = (LinearLayout) v.findViewById(R.id.tip_custom_ll);
        mClearSignLayout = (LinearLayout) v.findViewById(R.id.clear_sign);
        mAcceptSignLayout = (LinearLayout) v.findViewById(R.id.accept_sign);
        mNoTipTextView = (TextView) v.findViewById(R.id.notip_amount);
        mTipFiftenTextView = (TextView) v.findViewById(R.id.tip_one_amount);
        mTipTwentyTextView = (TextView) v.findViewById(R.id.tip_two_amount);
        mTipTwentyFiveTextView = (TextView) v.findViewById(R.id.tip_three_amount);
        mTipCustomTextView = (TextView) v.findViewById(R.id.tip_custom_amount);
        mAmountTextView = (TextView) v.findViewById(R.id.amount_text_view);
        mCloseImageView = (ImageView) v.findViewById(R.id.close_image_view);
        tempDir = Environment.getExternalStorageDirectory() + "/" + getString(R.string.txt_external_dir) + "/";
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir(getString(R.string.txt_external_dir), Context.MODE_PRIVATE);

        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath = new File(directory, current);
        mContent = (LinearLayout) v.findViewById(R.id.linearLayout);
        mSignatureView = new SignatureView(getActivity(), null);
        mSignatureView.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignatureView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mAcceptSignLayout.setEnabled(false);
        mView = mContent;
        mTipLinearLayoutButtons = new ArrayList<>();
        mTipLinearLayoutButtons.add(mNoTipLayout);
        mTipLinearLayoutButtons.add(mTipFiftenLayout);
        mTipLinearLayoutButtons.add(mTipTwentyLayout);
        mTipLinearLayoutButtons.add(mTipTwentyFiveLayout);
        mTipLinearLayoutButtons.add(mTipCustomLayout);

    }
    private void setUI()
    {
        mAmountTextView.setText(String.valueOf(mMaxValue));
        mTipFiftenTextView.setText(mFindPercentage(TIP_AMOUNT_PERCENT_ONE));
        mTipTwentyTextView.setText(mFindPercentage(TIP_AMOUNT_PERCENT_TWO));
        mTipTwentyFiveTextView.setText(mFindPercentage(TIP_AMOUNT_PERCENT_THREE));
    }
    private String mFindPercentage(int percent)
    {

        return Utils.formatCartTotal(mMaxValue.multiply(new BigDecimal(percent)).divide(Consts.HUNDRED));
    }
    private void  setUpListeners()
    {
        mNoTipLayout.setOnClickListener(mLlClickListener);
        mTipFiftenLayout.setOnClickListener(mLlClickListener);
        mTipTwentyLayout.setOnClickListener(mLlClickListener);
        mTipTwentyFiveLayout.setOnClickListener(mLlClickListener);
        mTipCustomLayout.setOnClickListener(mLlClickListener);
        mCloseImageView.setOnClickListener(mCloseClickListener);
        mClearSignLayout.setOnClickListener(mSignClickListener);
        mAcceptSignLayout.setOnClickListener(mSignClickListener);

    }

    public View.OnClickListener mCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    public View.OnClickListener mLlClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (v.equals(mNoTipLayout))
            {
                //mPriceListener.onSetTipZeroPrice(new BigDecimal(BigInteger.ZERO),PAYMENT_TYPE);
                refreshButtons(mNoTipLayout);
                mTipCustomTextView.setText(R.string.txt_tip_amount);
                mTipAmount = BigDecimal.ZERO;
                mAmountTextView.setText(String.valueOf(mMaxValue));
            }
            else if (v.equals(mTipFiftenLayout))
            {
                //mPriceListener.onSetPrice(new BigDecimal(mTipFiftenTextView.getText().toString()),PAYMENT_TYPE);
                refreshButtons(mTipFiftenLayout);
                mTipCustomTextView.setText(R.string.txt_tip_amount);
                mTipAmount = new BigDecimal(mTipFiftenTextView.getText().toString());
                mAmountTextView.setText(String.valueOf(mMaxValue.add(mTipAmount)));
            }
            else if (v.equals(mTipTwentyLayout))
            {
               // mPriceListener.onSetPrice(new BigDecimal(mTipTwentyTextView.getText().toString()),PAYMENT_TYPE);
                refreshButtons(mTipTwentyLayout);
                mTipCustomTextView.setText(R.string.txt_tip_amount);
                mTipAmount = new BigDecimal(mTipTwentyTextView.getText().toString());
                mAmountTextView.setText(String.valueOf(mMaxValue.add(mTipAmount)));
            }
            else if (v.equals(mTipTwentyFiveLayout))
            {
                //mPriceListener.onSetPrice(new BigDecimal(mTipTwentyFiveTextView.getText().toString()),PAYMENT_TYPE);
                refreshButtons(mTipTwentyFiveLayout);
                mTipCustomTextView.setText(R.string.txt_tip_amount);
                mTipAmount = new BigDecimal(mTipTwentyFiveTextView.getText().toString());
                mAmountTextView.setText(String.valueOf(mMaxValue.add(mTipAmount)));
            }
            else if (v.equals(mTipCustomLayout))
            {

                    CustomTipFragment fragment = CustomTipFragment.newInstance(CustomTipFragment.TEN_PAD_TYPE_DISCOUNT, mMaxValue);
                    fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    fragment.setPriceListener(new CustomTipFragment.PriceListener() {
                        @Override
                        public void onSetPrice(BigDecimal amount)
                        {
                         /*   mAmountOther = amount.divide(Consts.HUNDRED);
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
                            }*/
                            refreshButtons(mTipCustomLayout);
                            mTipCustomTextView.setText(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)));
                            //mPriceListener.onSetPrice(amount.divide(Consts.HUNDRED),PAYMENT_TYPE);
                            mTipAmount = amount.divide(Consts.HUNDRED);
                            mAmountTextView.setText(String.valueOf(mMaxValue.add(mTipAmount)));
                        }
                    });
            }
        }
    };

    public View.OnClickListener mSignClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (v.equals(mClearSignLayout))
            {
                mSignatureView.clear();
            }
            else if (v.equals(mAcceptSignLayout))
            {
                boolean error = captureSignature();
                if (!error) {
                    mView.setDrawingCacheEnabled(true);
                    String signImage = mSignatureView.getImage(mView);
                    Log.e("sigImage","sigImage>>>>"+signImage);
                    //mPriceListener.onSetSignImage(signImage,PAYMENT_TYPE);

                    if(PAYMENT_TYPE.equals(PaymentActivity.PAYMENT_TYPE_CREDIT)){
                        mPriceListener.onCreditPayment(signImage, mTipAmount);
                        dismiss();
                        return;
                    }

                    mPriceListener.onComplete(signImage, mTipAmount, PAYMENT_TYPE);

                    BigDecimal amountRec = new BigDecimal(amountReceived);
                    if(amountRec.compareTo(mMaxValue.add(mTipAmount)) > 0){
                        BigDecimal amoutget = amountRec.subtract(mMaxValue.add(mTipAmount));
                        ChangeFragment fragment = ChangeFragment.newInstance(amountReceived.toString(), amoutget.toString(), PAYMENT_TYPE);
                        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    } else{
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mMaxValue.add(mTipAmount)), PAYMENT_TYPE, mTipAmount.toString(), signImage, "");
                        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }
                    /*if(FRAGMENT_TYPE.equalsIgnoreCase(PaymentActivity.FRAGMENT_CHANGED)) {
                        BigDecimal amoutget=(new BigDecimal(amountReceived)).subtract(mMaxValue.add(mTipAmount));
                        ChangeFragment fragment = ChangeFragment.newInstance(amountReceived.toString(), amoutget.toString(), PAYMENT_TYPE);
                        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }
                    else {
                        Log.e("Fragment Approve","else condition Fragment Approve");
                        ApproveFragment fragment = ApproveFragment.newInstance(DecimalFormat.getCurrencyInstance().format(mMaxValue.add(mTipAmount)), PAYMENT_TYPE, mTipAmount.toString(), signImage, "");
                        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }*/
                    dismiss();
                }
            }

        }
    };
    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";

        if (error) {
            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate = (c.get(Calendar.YEAR) * 10000)
                + ((c.get(Calendar.MONTH) + 1) * 100)
                + (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:", String.valueOf(todaysDate));
        return (String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000)
                + (c.get(Calendar.MINUTE) * 100) + (c.get(Calendar.SECOND));
        Log.w("TIME:", String.valueOf(currentTime));
        return (String.valueOf(currentTime));

    }
    public class SignatureView extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public SignatureView(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public String getImage(View v) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(mBitmap);

            try {
                v.draw(canvas);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bArray = bos.toByteArray();

                String encodedImage = Base64.encodeToString(bArray, Base64.NO_WRAP);
                //Log.v("IMAGE", encodedImage);
                return encodedImage;
            } catch (Exception e) {
                return null;
            }
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mAcceptSignLayout.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    private void refreshButtons(LinearLayout linearLayout){
        for(LinearLayout llButtons : mTipLinearLayoutButtons){
            GradientDrawable bgNoTipShape = (GradientDrawable)llButtons.getBackground();
            if(llButtons.equals(linearLayout)){
                bgNoTipShape.setColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.pos_secondary_blue));
            }else{
                bgNoTipShape.setColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondary_color));
            }
        }
    }
}
