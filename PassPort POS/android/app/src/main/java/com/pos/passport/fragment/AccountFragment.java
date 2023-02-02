package com.pos.passport.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.pos.passport.R;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.task.RegistrationAsyncTask;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

/**
 * Created by Kareem on 3/24/2016.
 */
public class AccountFragment extends Fragment {

    private EditText mDeviceKeyEditText;
    private EditText mTerminalNameEditText;
    private EditText mEmailEditText;
    private Button mSaveButton;

    private View.OnClickListener mSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(mDeviceKeyEditText.getText().toString().trim())) {
                Utils.alertBox(getActivity(), R.string.txt_login_error, R.string.msg_require_key);
                return;
            }

            if (TextUtils.isEmpty(mTerminalNameEditText.getText().toString().trim())) {
                Utils.alertBox(getActivity(), R.string.txt_login_error, R.string.msg_require_terminal_name);
                return;
            }

            RegistrationAsyncTask asyncTask = new RegistrationAsyncTask(getActivity(), true);
            asyncTask.setListener(new AsyncTaskListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {

                }
            });
            asyncTask.execute(mEmailEditText.getText().toString().trim(), mDeviceKeyEditText.getText().toString().trim(),
                    mTerminalNameEditText.getText().toString().trim());

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account_info, container, false);
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
        mDeviceKeyEditText = (EditText)v.findViewById(R.id.device_key_edit_text);
        mTerminalNameEditText = (EditText) v.findViewById(R.id.terminal_name_edit_text);
        mEmailEditText = (EditText) v.findViewById(R.id.account_email_edit_text);
        mSaveButton = (Button) v.findViewById(R.id.save_button);
    }

    private void setUI(){
        LoginCredential credential = PrefUtils.getLoginCredential(getActivity());
        mDeviceKeyEditText.setText(credential.getKey());
        mTerminalNameEditText.setText(credential.getTerminalName());
        mEmailEditText.setText(credential.getEmail());
        mSaveButton.setEnabled(false);
    }

    private void setUpListeners(){
        mSaveButton.setOnClickListener(mSaveButtonClickListener);
        mDeviceKeyEditText.addTextChangedListener(TextChangeWatcher);
        mTerminalNameEditText.addTextChangedListener(TextChangeWatcher);
    }

    private final TextWatcher TextChangeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mSaveButton.setEnabled(true);
        }
    };
}
