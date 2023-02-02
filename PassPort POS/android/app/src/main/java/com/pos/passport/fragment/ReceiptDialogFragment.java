package com.pos.passport.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pos.passport.R;
import com.pos.passport.activity.PaymentActivity;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Customer;
import com.pos.passport.model.Payment;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.task.EmailReceiptSendAsync;
import com.pos.passport.util.Consts;
import com.pos.passport.util.MessageHandler;
import com.pos.passport.util.ReceiptHelper;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;

/**
 * Created by Kareem on 2/10/2016.
 */
public class ReceiptDialogFragment extends DialogFragment implements Runnable {

    public final static String BUNDLE_CART = "bundle_cart";
    public final static int PRINT_RECEIPT = 0;
    public final static int EMAIL_RECEIPT = 1;
    public final static int NO_RECEIPT = 2;
    public final static int PRINT_RETURN_RECEIPT = 3;

    public final static int ERROR = 0;
    public final static int PRINT_SUCCESS = 1;
    public final static int PRINT_FAILED = 2;
    public final static int EMAIL_SUCCESS = 3;
    public final static int EMAIL_FAILED = 4;

    public final static String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    private Button mPrintReceipt;
    private Button mEmailReceipt;
    private Button mNoReceipt;
    private Cart mCart;
    private int todo = 0;
    private int merchantPrint = 0;
    private ProgressDialog pd;
    private QueueInterface mCallback;
    private Customer mRecipient;
    private MessageHandler mHandler;
    private BigDecimal transid=BigDecimal.ZERO;
    private BigDecimal ChangeAmount=BigDecimal.ZERO;
    private boolean hasCreditPayment = false;
    private Context mcontext;
    private ProgressDialog mProgressDialog;

    public interface EmailReceiptListener{
        void onSendReceipt(Customer customer);
    }

    private View.OnClickListener mPrintReceiptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendReceipt(PRINT_RECEIPT, R.string.txt_printing_receipt);
        }
    };

    private View.OnClickListener mEmailReceiptClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(Utils.hasInternet(getContext()))
            {
                if(mCart.hasCustomer())
                {
                    mRecipient = mCart.getCustomer();
                    if(mRecipient.email.equalsIgnoreCase("") || mRecipient.email.length() < 0)
                    {
                        ShowEmailDialogFragment();
                    }else
                    {
                        sendReceipt(EMAIL_RECEIPT, R.string.txt_email_receipt_to_customer);
                    }
                }else
                {
                    ShowEmailDialogFragment();
                }
            }else{
                Utils.alertBox(getActivity(),R.string.txt_error ,R.string.msg_no_internet_connection);
            }
        }
    };
public void ShowEmailDialogFragment()
{
    EmailReceiptDialogFragment fragment = new EmailReceiptDialogFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(EmailReceiptDialogFragment.BUNDLE_CUSTOMER, mCart.getCustomer());
    fragment.setArguments(bundle);
    fragment.setListener(new EmailReceiptListener() {

        @Override
        public void onSendReceipt(Customer customer)
        {
            mRecipient = customer;
            sendReceipt(EMAIL_RECEIPT, R.string.txt_email_receipt_to_customer);
        }
    });
    fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
}
    private  View.OnClickListener mNoReceiptClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mCallback.onSaleDone();
            dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try
        {
            mcontext=context;
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getDialog() != null)
            getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fragment_receipt_dialog, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        mCart = (Cart) getArguments().getSerializable(BUNDLE_CART);
        transid = mCart.mTrans;
        ChangeAmount = mCart.mChangeAmount;
        for(Payment p : mCart.mPayments){
            if(p.paymentType.equalsIgnoreCase(PaymentActivity.PAYMENT_TYPE_CREDIT))
                hasCreditPayment = true;
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new MessageHandler(getActivity());
        bindUIElements(view);
        setUpListeners();
    }

    private void bindUIElements(View v){
        mPrintReceipt = (Button) v.findViewById(R.id.print_receipt_button);
        mEmailReceipt = (Button) v.findViewById(R.id.email_receipt_button);
        mNoReceipt = (Button) v.findViewById(R.id.no_receipt_button);
    }
    ;
    private void setUpListeners(){
        mPrintReceipt.setOnClickListener(mPrintReceiptClickListener);
        mEmailReceipt.setOnClickListener(mEmailReceiptClickListener);
        mNoReceipt.setOnClickListener(mNoReceiptClickListener);
    }

    private void sendReceipt(int receiptType, int resId)
    {
        pd = ProgressDialog.show(getActivity(), "", getString(resId), true, false);
        this.todo = receiptType;
        Log.e("type","todo>>"+todo);
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        final Message m = new Message();
        switch (todo){
            case PRINT_RECEIPT:
                if(!StoreSetting.capture_sig && hasCreditPayment){
                    if(ReceiptHelper.print(mcontext, mCart, Consts.MERCHANT_PRINT_RECEIPT_YES)){
                        m.what = PRINT_SUCCESS;
                        AlertDialogFragment fragment = AlertDialogFragment.getInstance(mcontext,R.string.txt_customer_copy, R.string.msg_customer_copy,
                                                                                        R.string.txt_yes, R.string.txt_no);
                        fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                            @Override
                            public void ok() {
                                ReceiptHelper.print(mcontext, mCart, Consts.MERCHANT_PRINT_RECEIPT_NO);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
                        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    }else
                        m.what = PRINT_FAILED;
                } else {
                    if(ReceiptHelper.print(getActivity(), mCart, 0)){
                        m.what = PRINT_SUCCESS;
                    }else{
                        m.what = PRINT_FAILED;
                    }
                }
                mHandler.sendMessage(m);
                /*if(ReceiptHelper.print(getActivity(), mCart, 0)) {
                    m.what = PRINT_SUCCESS;
                    if (!StoreSetting.capture_sig)
                        merchantPrint = 1;
                }
                else
                    m.what = PRINT_FAILED;
                mHandler.sendMessage(m);*/
                break;
            case EMAIL_RECEIPT:
                if(mRecipient != null)
                {
                    try {

//                        EmailReceiptSendAsync emailReceiptAsyncTask = new EmailReceiptSendAsync(getActivity(), mCart, mRecipient,mCart.mTrans);
//                        emailReceiptAsyncTask.setListener(new AsyncTaskListener() {
//                            @Override
//                            public void onSuccess() {
//                                Log.e("onSuccess", "onSuccess");
//                                m.what = EMAIL_SUCCESS;
//                                mHandler.sendMessage(m);
//                            }
//
//                            @Override
//                            public void onFailure() {
//                                Log.e("EMAIL_FAILED", "EMAIL_FAILED");
//                                m.what = EMAIL_FAILED;
//                                mHandler.sendMessage(m);
//                            }
//                        });
//                        emailReceiptAsyncTask.execute("");
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            case NO_RECEIPT:
                break;
            default:
                m.what = ERROR;
                mHandler.sendMessage(m);
                break;
        }



        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(merchantPrint == 1 && hasCreditPayment)
                {
                    //alertBox(getActivity());
                    alertBox(mcontext);
                }
                else
                    mCallback.onSaleDone();

                dismiss();
                if(pd != null && pd.isShowing()) pd.dismiss();

                SednEmailRec();
            }
        });

    }
    public void SednEmailRec()
    {
        final Message m = new Message();
        if(mRecipient != null) {

            EmailReceiptSendAsync emailReceiptAsyncTask = new EmailReceiptSendAsync(getActivity(), mCart, mRecipient, transid, ChangeAmount);
            emailReceiptAsyncTask.setListener(new AsyncTaskListener() {
                @Override
                public void onSuccess() {
                    Log.e("onSuccess", "onSuccess");
                    m.what = EMAIL_SUCCESS;
                    mHandler.sendMessage(m);
                }

                @Override
                public void onFailure() {
                    Log.e("EMAIL_FAILED", "EMAIL_FAILED");
                    m.what = EMAIL_FAILED;
                    mHandler.sendMessage(m);
                }
            });
            emailReceiptAsyncTask.execute("");
        }else
        {
            m.what = ERROR;
            mHandler.sendMessage(m);
        }
    }

    public void alertBox(final Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Print merchant receipt ?")
                .setTitle("Merchant Receipt")
                .setCancelable(true)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCallback.onSaleDone();
                    }
                })
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                printReceipt(context);

                            }
                        }).show();
    }
    public void printReceipt(Context mcontext) {
        Message mMessage = new Message();
        if(ReceiptHelper.print(mcontext, mCart, Consts.MERCHANT_PRINT_RECEIPT_YES))
            mMessage.what = MessageHandler.PRINT_SUCCESS;
        else
            mMessage.what = MessageHandler.PRINT_FAILED;

        mHandler.sendMessage(mMessage);
        mCallback.onSaleDone();
//        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_printing_receipt), true, false);
//
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(mProgressDialog.isShowing())
//                            mProgressDialog.dismiss();
//                    }
//                });
//            }
//        });
//        thread.start();
    }
}
