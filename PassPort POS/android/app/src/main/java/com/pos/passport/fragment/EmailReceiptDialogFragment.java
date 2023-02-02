package com.pos.passport.fragment;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.ReceiptDialogFragment.EmailReceiptListener;
import com.pos.passport.model.Customer;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 2/11/2016.
 */
public class EmailReceiptDialogFragment extends DialogFragment {

    public final static String BUNDLE_CUSTOMER = "bundle_customer";
    private AutoCompleteTextView mEmailIdAutoCompleteTextView;
    private TextView mFirstNameText;
    private TextView mLastNameText;
    private TextView mTitleBarTextView;
    private TextView mBackImageView;
    private Button mSendReceiptButton;
    private SimpleCursorAdapter mAdapter;
    private ProductDatabase mDb;
    private Customer mCustomer;
    private EmailReceiptListener mCallback;

    public void setListener(EmailReceiptListener callback){
        this.mCallback = callback;
    }

    private View.OnClickListener mSendReceiptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validate()){
                Customer customer = new Customer();
                customer.setEmail(mEmailIdAutoCompleteTextView.getText().toString().trim());
                customer.setFName(mFirstNameText.getText().toString().trim());
                customer.setLName(mLastNameText.getText().toString().trim());
                mCallback.onSendReceipt(customer);
                dismiss();
            }
        }
    };

    private AdapterView.OnItemClickListener mEmailIdAutoCompleteClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = mAdapter.getCursor();
            Customer customer = new Customer();
            customer.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            customer.setFName(cursor.getString(cursor.getColumnIndex("fname")));
            customer.setLName(cursor.getString(cursor.getColumnIndex("lname")));
            customer.setEmail(cursor.getString(cursor.getColumnIndex("email")));
            mFirstNameText.setText(customer.fName);
            mLastNameText.setText(customer.lName);
            mEmailIdAutoCompleteTextView.setText(customer.email);
            Utils.dismissKeyboard(view);
        }
    };

    private View.OnClickListener mBackImageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getDialog() != null)
            getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fragment_email_receipt_send, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpUI();
        setUpListeners();
    }

    private void bindUIElements(View v){
        mEmailIdAutoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.email_receipt_auto_text);
        mFirstNameText = (TextView) v.findViewById(R.id.customer_first_name_edit_text);
        mLastNameText = (TextView) v.findViewById(R.id.customer_last_name_edit_text);
        mSendReceiptButton = (Button) v.findViewById(R.id.email_receipt_button);
        mTitleBarTextView = (TextView) v.findViewById(R.id.title_text_view);
        mBackImageView = (TextView) v.findViewById(R.id.back_button);
    }

    private void setUpUI(){
        mBackImageView.setVisibility(View.GONE);
        mTitleBarTextView.setText(R.string.txt_email_receipt);

        if(getArguments().get(BUNDLE_CUSTOMER) != null) {
            mCustomer = (Customer) getArguments().get(BUNDLE_CUSTOMER);
            mEmailIdAutoCompleteTextView.setText(mCustomer.email);
            mFirstNameText.setText(mCustomer.fName);
            mLastNameText.setText(mCustomer.lName);
        }

        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, mDb.searchCustomers(""),
                new String[] { "fname", "lname", "email", "phone" }, new int[] { android.R.id.text1 }, 0);
        mEmailIdAutoCompleteTextView.setAdapter(mAdapter);
        mEmailIdAutoCompleteTextView.setThreshold(1);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return mDb.searchCustomers(str.toString());
            }
        });

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (cursor.getCount() > 0)
                {
                    StringBuffer item = new StringBuffer();
                    item.append(cursor.getString(cursor.getColumnIndex("fname"))).append(", ")
                            .append(cursor.getString(cursor.getColumnIndex("lname"))).append("\n")
                            .append(cursor.getString(cursor.getColumnIndex("email"))).append("\n")
                            .append(cursor.getString(cursor.getColumnIndex("phone")));
                    ((TextView) view).setText(item.toString());
                    return true;
                }
                return false;
            }
        });
        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor c) {
                return c.getString(c.getColumnIndexOrThrow("fname"));
            }
        });
    }

    private void setUpListeners(){
        mSendReceiptButton.setOnClickListener(mSendReceiptClickListener);
        mEmailIdAutoCompleteTextView.setOnItemClickListener(mEmailIdAutoCompleteClick);
        mBackImageView.setOnClickListener(mBackImageViewClickListener);
    }

    private boolean validate(){
        if(! android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailIdAutoCompleteTextView.getText().toString().trim()).matches()){
            mEmailIdAutoCompleteTextView.setError(getString(R.string.msg_invalid_email));
            return false;
        }

        return true;
    }
}
