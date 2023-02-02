package com.pos.passport.fragment;

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
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Customer;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 1/21/16.
 */
public class SaveSaleFragment extends DialogFragment {
    public final static String BUNDLE_CUSTOMER = "bundle_customer";
    private EditText mFirstNameEditText;
    private TextView mPrinterErrorText;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mSavePrintButton;
    private QueueInterface mCallback;
    private Customer mCustomer;

    private View.OnClickListener mSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkErrors()) {
                Utils.dismissKeyboard(getActivity().getCurrentFocus());
                mCallback.onSaveQueue(mFirstNameEditText.getText().toString().trim());
                dismiss();
            }
        }
    };

    private View.OnClickListener mSavPrintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.dismissKeyboard(getActivity().getCurrentFocus());
            if(checkErrors() && checkPrinter()) {
                //mCallback.onSavePrintQueue(mFirstNameEditText.getText().toString().trim());
            }
        }
    };

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.dismissKeyboard(getActivity().getCurrentFocus());
            dismiss();
            //mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_save_sale, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }
    /*@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout. fragment_save_sale, null);
        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUpListeners();
        setUpUi();
    }

    private void bindUIElements(View view) {
        mFirstNameEditText = (EditText) view.findViewById(R.id.first_name_edit_text);
        mSavePrintButton = (Button) view.findViewById(R.id.save_print_button);
        mSaveButton = (Button)view.findViewById(R.id.save_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_button);
        mPrinterErrorText = (TextView) view.findViewById(R.id.printer_error_text);
    }

    private void setUpListeners() {
        mSaveButton.setOnClickListener(mSaveClickListener);
        mSavePrintButton.setOnClickListener(mSavPrintClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
    }

    private void setUpUi() {
        mPrinterErrorText.setVisibility(View.GONE);
        if (getArguments() != null) {
            mCustomer = (Customer)getArguments().getSerializable(BUNDLE_CUSTOMER);
            if (mCustomer != null) {
                mFirstNameEditText.setText(String.format("%s %s",mCustomer.fName, mCustomer.lName));
            }
        }
    }

    private Validator validate() {
        Validator validator = new Validator();

        validator.result = true;
        if (TextUtils.isEmpty(mFirstNameEditText.getText().toString().trim())) {
            validator.result = false;
            validator.message = getString(R.string.msg_require_first_name);
            validator.editText = mFirstNameEditText;
        }

        return validator;
    }

    private boolean checkErrors(){
        Validator validator = validate();
        if (!validator.result) {
            validator.editText.setError(validator.message);
            return false;
        }

        return true;
    }

    private boolean checkPrinter(){

        for (String t : ReceiptSetting.printers) {
            try {
                JSONObject object = new JSONObject(t);
                if(object.optBoolean("openOrderPrinter")){
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mPrinterErrorText.setVisibility(View.VISIBLE);
        return false;
    }

    class Validator {
        boolean result;
        String message;
        EditText editText;
    }
}
