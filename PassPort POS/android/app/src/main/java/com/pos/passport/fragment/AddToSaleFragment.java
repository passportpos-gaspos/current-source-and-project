package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 1/18/2016.
 */
public class AddToSaleFragment extends Fragment {

    public static final String CUSTOMER_BUTTON = "customer_button";
    private Button mCustomerButton;
    private Button mNoteButton;
    private Button mDiscountButton;
    private TextView mCloseImageButton;

    private boolean hasCustomer;

    private QueueInterface mCallback;


    private View.OnClickListener mDiscountButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_DISCOUNT);
        }
    };

    private View.OnClickListener mNoteButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_NOTE);
        }
    };

    private View.OnClickListener mCustomerButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(hasCustomer){
                AlertDialogFragment alert = AlertDialogFragment.getInstance(getActivity(), R.string.txt_remove,
                                                        R.string.msg_remove_customer, R.string.txt_yes, R.string.txt_no);
                alert.show(getFragmentManager(), "");
                alert.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        mCallback.onRemoveCustomer();
                    }

                    @Override
                    public void cancel() {}
                });
            }
            mCallback.onChangeFragment(MainActivity.FRAGMENT_SEARCH_CUSTOMER);
        }
    };

    private View.OnClickListener mCloseImageButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
        }
    };

    @Override
    public void onAttach(Context context){
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_to_sale, container, false);
        hasCustomer =  getArguments().getBoolean(CUSTOMER_BUTTON);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUI();
        setUpListeners();
    }

    private void bindUIElements(View v){

        mCustomerButton = (Button) v.findViewById(R.id.sale_customer_button);
        mDiscountButton = (Button) v.findViewById(R.id.sale_discount_button);
        mNoteButton = (Button) v.findViewById(R.id.sale_note_button);
        mCloseImageButton = (TextView) v.findViewById(R.id.back_button);
    }

    private void setUI(){
        if(hasCustomer) {
            mCustomerButton.setText(R.string.txt_remove_customer);
            mCustomerButton.setBackgroundResource(R.color.red);
        }
    }

    private void setUpListeners(){
        mCustomerButton.setOnClickListener(mCustomerButtonListener);
        mDiscountButton.setOnClickListener(mDiscountButtonListener);
        mNoteButton.setOnClickListener(mNoteButtonListener);
        mCloseImageButton.setOnClickListener(mCloseImageButtonListener);
    }
}
