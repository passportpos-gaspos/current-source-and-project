package com.pos.passport.activity;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.interfaces.CartInterface;
import com.pos.passport.task.RefreshTokenAsyncTask;
import com.pos.passport.task.RegistrationAsyncTask;
import com.pos.passport.util.Utils;

public class RegisterActivity extends BaseActivity implements CartInterface {
	private EditText mEmailEditText;
	private EditText mKeyEditText;
	private EditText mTerminalNameEditText;
	private Button mLoginButton;
	private ProgressDialog pd;
	public String mpsResponse;
    private static final String DEBUG_TAG ="[RegisterActivity]";
	private View.OnClickListener mLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Register")
                    .putCustomAttribute("Button", "Login"));

            if (Utils.hasInternet(RegisterActivity.this)) {
                if (TextUtils.isEmpty(mEmailEditText.getText().toString().trim())) {
                    Utils.alertBox(RegisterActivity.this, R.string.txt_login_error, R.string.msg_require_email);
                    return;
                }

                if (TextUtils.isEmpty(mKeyEditText.getText().toString().trim())) {
                    Utils.alertBox(RegisterActivity.this, R.string.txt_login_error, R.string.msg_require_key);
                    return;
                }

                if (TextUtils.isEmpty(mTerminalNameEditText.getText().toString().trim())) {
                    Utils.alertBox(RegisterActivity.this, R.string.txt_login_error, R.string.msg_require_terminal_name);
                    return;
                }

				register();

            } else {
                Utils.alertBox(RegisterActivity.this, R.string.txt_no_network, R.string.msg_unable_to_login);
            }
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));

        Log.d(DEBUG_TAG,"oncreate registerActivity");
//        Log.d(DEBUG_TAG,"Inet devive gride "+this.getResources().getInteger(R.integer.int_grid_num_columns));
//        Log.d(DEBUG_TAG,"Inet devive reso"+this.getResources().getString(R.string.txt_reso_inet));
		setContentView(R.layout.activity_register);

        bindUIElements();
        setUpListeners();

        ViewGroup vg = (ViewGroup)getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
	}

    private void bindUIElements() {
        mEmailEditText = (EditText) findViewById(R.id.login_email_edit_text);
        mKeyEditText = (EditText) findViewById(R.id.login_key_edit_text);
        mTerminalNameEditText = (EditText) findViewById(R.id.terminal_name_edit_text);
        mLoginButton = (Button) findViewById(R.id.login_button);
    }

    private void setUpListeners() {
        mLoginButton.setOnClickListener(mLoginClickListener);
    }

    @Override
    public void onBackPressed(){
        return;
    }
	private void register()
    {
        RegistrationAsyncTask asyncTask = new RegistrationAsyncTask(RegisterActivity.this, true);
        asyncTask.setListener(new AsyncTaskListener()
        {

            @Override
            public void onSuccess()
            {
                RefreshTokenAsyncTask refreshTokenAsyncTask = new RefreshTokenAsyncTask(RegisterActivity.this, false);
                refreshTokenAsyncTask.execute();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure() {
                Log.i("tag","Error");
            }
        });

        asyncTask.execute(mEmailEditText.getText().toString().trim(), mKeyEditText.getText().toString().trim(),
                mTerminalNameEditText.getText().toString().trim());
	}

    @Override
    public void onReset() {
        setResult(RESULT_OK);
    }


}
