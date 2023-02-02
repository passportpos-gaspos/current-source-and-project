package com.pos.passport.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Customer;
import com.pos.passport.task.SendCustomerAsyncTask;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

/**
 * Created by Kareem on 1/21/2016.
 */
@SuppressLint("ValidFragment")
public class CustomerAddFragmentNew extends DialogFragment {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private EditText mPhone;
    private Button mSaveButton;
    private Button mCancelButton;

    private ProductDatabase mDb;

    private QueueInterface mCallback;
    Context mconContext;

    private CallBackAgain callbackListener;
    public interface CallBackAgain {
        void onSetDis();
    }
    public void CallBackAgainListener(CallBackAgain l) {
        callbackListener = l;
    }
    @SuppressLint("ValidFragment")
    public CustomerAddFragmentNew(Context mContext)
    {
       mconContext=mContext;
    }

    private View.OnClickListener mSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            CustomerValidator validator = validate();
            if (validator.message != null) {
                validator.view.setError(validator.message);
                return;
            }

            final Customer customer = new Customer();
            try {
                customer.setEmail(""+mEmail.getText().toString().trim());
                customer.setFName(mFirstName.getText().toString().trim());
                customer.setLName(mLastName.getText().toString().trim());
                customer.setPhone(""+mPhone.getText().toString().trim());
                if (Utils.hasInternet(getActivity())) {
                    SendCustomerAsyncTask task = new SendCustomerAsyncTask(getActivity(), customer) {
                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            try
                            {
                                if(result == null)
                                {
                                    callbackListener.onSetDis();
                                    Toast.makeText(mconContext, "Failed to add Customer to back office", Toast.LENGTH_LONG).show();
                                }
                                else if (result != null && result.contains("customerId"))
                                {
                                    JSONObject data = new JSONObject(result.toString());
                                    int cid=data.getInt("customerId");

                                    Toast.makeText(mconContext, "Customer added successfully to back office", Toast.LENGTH_LONG).show();
                                    mDb.insertCustomernew(customer,cid);
                                    Utils.dismissKeyboard(v);
                                    mCallback.onAssignCustomer(customer);
                                    callbackListener.onSetDis();
                                } else if (result != null && result.contains("error"))
                                {
                                    JSONObject data = new JSONObject(result.toString());
                                    String error=data.getString("error");
                                    Utils.alertBox(mconContext, "Error!", error);
                                    callbackListener.onSetDis();
                                    //Toast.makeText(getActivity(), "Failed to add Customer to back office", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e)
                            {
                               // e.printStackTrace();
                            }
                        }
                    };
                    task.execute();
                    //TODO send request to back office and get id

                }
                else
                {
                    Utils.alertBox(mconContext, "Error!", "The Internet connection appears to be offline. ");
                    callbackListener.onSetDis();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            dismiss();
        }
    };

    private View.OnClickListener mBackImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_SEARCH_CUSTOMER);
            Utils.dismissKeyboard(v);
        }
    };

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
            //mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            Utils.dismissKeyboard(v);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Nullable
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (QueueInterface) context;
            mDb = ProductDatabase.getInstance(context);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUpUI();
        setUpListeners();
    }

    private void bindUIElements(View v) {
        mFirstName = (EditText) v.findViewById(R.id.customer_first_name_edit_text);
        mLastName = (EditText) v.findViewById(R.id.customer_last_name_edit_text);
        mEmail = (EditText) v.findViewById(R.id.customer_email_edit_text);
        mPhone = (EditText) v.findViewById(R.id.customer_phone_edit_text);
        mSaveButton = (Button) v.findViewById(R.id.customer_save_button);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
    }

    private void setUpListeners() {
        mSaveButton.setOnClickListener(mSaveButtonClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
    }

    private void setUpUI() {
    }

    private class CustomerValidator {
        private EditText view;
        String message;
    }

    private CustomerValidator validate() {
        CustomerValidator validator = new CustomerValidator();

        if (TextUtils.isEmpty(mFirstName.getText().toString().trim())) {
            validator.view = mFirstName;
            validator.message = this.getResources().getString(R.string.msg_invalid_first_name);
        } else if (TextUtils.isEmpty(mLastName.getText().toString().trim())) {
            validator.view = mLastName;
            validator.message = this.getResources().getString(R.string.msg_invalid_last_name);
        }
        /*else if (TextUtils.isEmpty(mEmail.getText().toString().trim())) {
            validator.view = mEmail;
            validator.message = this.getResources().getString(R.string.msg_invalid_email);
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString().trim()).matches())
        {
            validator.view = mEmail;
            validator.message = this.getResources().getString(R.string.msg_invalid_email);
        }
        else if(TextUtils.isEmpty(mPhone.getText().toString().trim()))
        {
            validator.view = mPhone;
            validator.message = this.getResources().getString(R.string.msg_invalid_phone);
        }
        else if (!Patterns.PHONE.matcher(mPhone.getText().toString().trim()).matches())
        {
            validator.view = mPhone;
            validator.message = this.getResources().getString(R.string.msg_invalid_phone);
        }*/

        return validator;
    }
}
