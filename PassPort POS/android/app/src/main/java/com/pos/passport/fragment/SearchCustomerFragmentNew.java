package com.pos.passport.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Customer;
import com.pos.passport.util.Utils;

/**
 * Created by karim on 1/25/16.
 */
public class SearchCustomerFragmentNew extends DialogFragment {
    private AutoCompleteTextView mCustomerAutoCompleteTextView;
    private ImageView mClearImageView;
    private Button mNewCustomerButton;
    private Button mCancelButton;
    private SimpleCursorAdapter mAdapter;
    private ProductDatabase mDb;
    private QueueInterface mCallback;
    private static final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    Context mcontext;
    private AdapterView.OnItemClickListener mCustomerClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = mAdapter.getCursor();
            Customer customer = new Customer();
            customer.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            customer.setFName(cursor.getString(cursor.getColumnIndex("fname")));
            customer.setLName(cursor.getString(cursor.getColumnIndex("lname")));
            customer.setEmail(cursor.getString(cursor.getColumnIndex("email")));
            customer.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
            dismiss();
            Utils.dismissKeyboard(view);
            mCallback.onAssignCustomer(customer);
        }
    };

    private TextWatcher mCustomerTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString().trim())) {
                mClearImageView.setVisibility(View.GONE);
            } else {
                mClearImageView.setVisibility(View.VISIBLE);
            }

        }
    };

    private View.OnClickListener mClearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCustomerAutoCompleteTextView.setText("");
        }
    };

    private View.OnClickListener mCancelClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            dismiss();
            Utils.dismissKeyboard(v);
        }
    };

    private View.OnClickListener mNewCustomerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            CustomerAddFragmentNew newFragment1 = new CustomerAddFragmentNew(mcontext);
            newFragment1.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
            newFragment1.CallBackAgainListener(new CustomerAddFragmentNew.CallBackAgain(){

                @Override
                public void onSetDis() {
                    dismiss();
                }
            });
            //dismiss();
           // mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_NEW_CUSTOMER);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext=getActivity();
       // setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_customer, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpListeners();
        setUpUi();
    }

    private void bindUIElements(View view) {
        mCustomerAutoCompleteTextView = (AutoCompleteTextView)view.findViewById(R.id.customer_autocomplete_text_view);
        mClearImageView = (ImageView)view.findViewById(R.id.clear_image_view);
        mNewCustomerButton = (Button)view.findViewById(R.id.add_customer_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_button);
    }

    private void setUpListeners() {
        mCustomerAutoCompleteTextView.addTextChangedListener(mCustomerTextWatcher);
        mCustomerAutoCompleteTextView.setOnItemClickListener(mCustomerClickListener);
        mClearImageView.setOnClickListener(mClearClickListener);
        mNewCustomerButton.setOnClickListener(mNewCustomerClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
    }

    private void setUpUi() {
        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, mDb.searchCustomers(""),
                new String[] { "fname", "lname", "email", "phone" }, new int[] { android.R.id.text1 }, 0);
        mCustomerAutoCompleteTextView.setAdapter(mAdapter);
        mCustomerAutoCompleteTextView.setThreshold(1);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider()
        {
            public Cursor runQuery(CharSequence str)
            {
                return mDb.searchCustomers(str.toString());
            }
        });

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (cursor.getCount() > 0) {
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
        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter()
        {
            @Override
            public CharSequence convertToString(Cursor c)
            {
                return c.getString(c.getColumnIndexOrThrow("fname"));
            }
        });
    }
}
