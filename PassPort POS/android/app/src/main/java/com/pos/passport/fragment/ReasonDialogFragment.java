package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pos.passport.R;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 6/6/2016.
 */
public class ReasonDialogFragment extends DialogFragment {

    private Button mReturnButton;
    private Button mCancelButton;
    private EditText mReasonEditText;

    private QueueInterface mCallback;

    private View.OnClickListener  mReturnButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(mReasonEditText.getText().toString().length() > 0) {
                mCallback.onReturn(mReasonEditText.getText().toString());
                dismiss();
                Utils.dismissKeyboard(v);
                return;
            }
            mReasonEditText.setError(getString(R.string.msg_invalid_reason));
        }
    };

    private View.OnClickListener mCancelButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.dismissKeyboard(v);
            dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_return_dialog, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUI();
        setUpListeners();
    }

    private void bindUIElements(View v){
        mReturnButton = (Button) v.findViewById(R.id.return_button);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mReasonEditText = (EditText) v.findViewById(R.id.reason_edit_view);
    }

    private void setUI(){
    }

    private void setUpListeners(){
        mReturnButton.setOnClickListener(mReturnButtonListener);
        mCancelButton.setOnClickListener(mCancelButtonListener);
    }
}
