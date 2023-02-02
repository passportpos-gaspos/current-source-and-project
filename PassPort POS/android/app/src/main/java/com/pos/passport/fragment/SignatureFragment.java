package com.pos.passport.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.interfaces.PayInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

/**
 * Created by Kareem on 5/25/2016.
 */
public class SignatureFragment extends DialogFragment {

    private PayInterface mCallback;
    private LinearLayout mContent;
    private SignatureView mSignatureView;
    private Button mClear, mGetSign, mCancel;
    private static String tempDir;
    private int count = 1;
    private String current = null;
    private Bitmap mBitmap;
    private View mView;
    private File mypath;
    private String uniqueId;
    private TextView mAmountTextView;
    private TextView mTenderTypeTextView;
    private TextView mTipAmountTextView;
    private String mTenderType;

    private static final String BUNDLE_AMOUNT = "bundle_amount";
    private static final String BUNDLE_TENDER_TYPE = "bundle_tender_type";
    private static final String BUNDLE_GATEWAY_ID = "bundle_gateway_id";
    private static final String BUNDLE_TIP_AMOUNT = "bundle_tip_amount";

    public static SignatureFragment newInstance(String amount, String paymentType, String gatewayId) {
        SignatureFragment fragment = new SignatureFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putString(BUNDLE_GATEWAY_ID, gatewayId);
        fragment.setArguments(args);

        return fragment;
    }

    public static SignatureFragment newInstance(String amount, String paymentType, String gatewayId, String tipAmount) {
        SignatureFragment fragment = new SignatureFragment();
        fragment.setStyle(STYLE_NO_TITLE, 0);
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(BUNDLE_AMOUNT, amount);
        args.putString(BUNDLE_TENDER_TYPE, paymentType);
        args.putString(BUNDLE_GATEWAY_ID, gatewayId);
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
        View v = inflater.inflate(R.layout.activity_signature, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        mTenderType = getArguments().getString(BUNDLE_TENDER_TYPE);
        setTextView();
        setUpListeners();
    }

    private void bindUIElements(View view){

        tempDir = Environment.getExternalStorageDirectory() + "/" + getString(R.string.txt_external_dir) + "/";
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir(getString(R.string.txt_external_dir), Context.MODE_PRIVATE);

        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath = new File(directory, current);
        mContent = (LinearLayout) view.findViewById(R.id.linearLayout);
        mSignatureView = new SignatureView(getActivity(), null);
        mSignatureView.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignatureView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button) view.findViewById(R.id.clear);
        mGetSign = (Button) view.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) view.findViewById(R.id.cancel);
        mAmountTextView = (TextView)view.findViewById(R.id.amount_text_view);
        mTenderTypeTextView = (TextView)view.findViewById(R.id.tender_type_text_view);
        mTipAmountTextView = (TextView) view.findViewById(R.id.tip_amount_text_view);
        mView = mContent;
        mCancel.setVisibility(View.GONE);
    }

    private void setUpListeners(){

        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Cleared");
                mSignatureView.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Saved");
                boolean error = captureSignature();
                if (!error) {
                    mView.setDrawingCacheEnabled(true);
                    String sigImage = mSignatureView.getImage(mView);
                    Log.e("sigImage","sigImage>>>>"+sigImage);
                    mCallback.onSignCompleted(getArguments().getString(BUNDLE_GATEWAY_ID), sigImage);
                    dismiss();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Canceled");
                Bundle b = new Bundle();
                b.putString("status", "cancel");
                Intent intent = new Intent();
                intent.putExtras(b);
                //mCallback.onSignCompleted();
                dismiss();
            }
        });
    }

    private void setTextView() {
        mAmountTextView.setText(getArguments().getString(BUNDLE_AMOUNT));
        if(getArguments().getString(BUNDLE_TIP_AMOUNT) != null)
            mTipAmountTextView.setText(getArguments().getString(BUNDLE_TIP_AMOUNT));
        mTenderTypeTextView.setText(mTenderType);
    }

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
                Log.v("IMAGE", encodedImage);
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
            mGetSign.setEnabled(true);

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
}
