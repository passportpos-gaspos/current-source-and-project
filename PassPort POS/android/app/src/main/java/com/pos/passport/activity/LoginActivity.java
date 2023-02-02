package com.pos.passport.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.TenPadDialogFragment;
import com.pos.passport.model.Cashier;
import com.pos.passport.task.SendSyncAsyncTask;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by karim on 2/8/16.
 */
public class LoginActivity extends BaseActivity {

    public static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";

    private Spinner mCashierSpinner;
    private Button mLoginButton;
    private ProductDatabase mDb;
    private ArrayList<Cashier> mCashiers = new ArrayList();
    private ArrayAdapter<Cashier> mCashierSpinAdapter;
    private Cashier mCashier;

    private View.OnClickListener mLoginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCashier = (Cashier)mCashierSpinner.getSelectedItem();
            TenPadDialogFragment fragment =  TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_LOGIN, mCashier);
            fragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                @Override
                public void onAdminAccessGranted()
                {
                    Intent data = new Intent();
                    PrefUtils.saveCashierInfo(LoginActivity.this, mCashier);
                    setResult(RESULT_OK, data);
                    finish();
                }

                @Override
                public void onAdminAccessDenied()
                {
                    Utils.alertBox(LoginActivity.this, String.format(getString(R.string.txt_cashier_login), mCashier.name), R.string.msg_cashier_login_failed);
                }
            });

            fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };


    private Spinner.OnItemSelectedListener mCashierSpinnerItemListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCashier = (Cashier) mCashierSpinner.getSelectedItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.d("Debug", "Nothing selected");
        }
    };

    private Spinner.OnClickListener mCashierClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCashiers = mDb.getCashiers();
            mCashierSpinAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_new);
        mDb = ProductDatabase.getInstance(this);
        bindUIElements();
        setUpUIs();
        setUpListeners();
        ViewGroup vg = (ViewGroup)getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
    }

    private void bindUIElements(){
        mCashierSpinner = (Spinner) findViewById(R.id.cashier_spinner);
        mLoginButton = (Button) findViewById(R.id.login_button);
    }

    private void setUpUIs() {
        mCashiers = mDb.getCashiers();
        //mDb.findAdminSettings();
        //mCashiers.add(0,AdminSetting.getAdminPermissions(this));
        //mCashierSpinAdapter = new ArrayAdapter<>(this, R.layout.view_spiner, mCashiers);
        mCashierSpinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCashiers);
        mCashierSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCashierSpinner.setAdapter(mCashierSpinAdapter);
    }

    private void setUpListeners(){
        mLoginButton.setOnClickListener(mLoginButtonClickListener);
        mCashierSpinner.setOnItemSelectedListener(mCashierSpinnerItemListener);
    }

    @Override
    protected  void onResume(){
        super.onResume();

    }

    @Override
    public void onBackPressed(){
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.menu_sync){
            if(Utils.hasInternet(LoginActivity.this)) {
                SendSyncAsyncTask sendSyncAsyncTask = new SendSyncAsyncTask(LoginActivity.this, true, true, true){
                    @Override
                    protected void onPostExecute(String result){
                        super.onPostExecute(result);
                        mCashiers.clear();
                        mCashiers.addAll(mDb.getCashiers());
                        mCashierSpinAdapter.notifyDataSetChanged();
                    }
                };
                sendSyncAsyncTask.setListener(new SendSyncAsyncTask.SendSyncListener() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onFailure(JSONObject error) {
                        if(!error.optString("tag").isEmpty()){
                                PrefUtils.removeCashier(LoginActivity.this);
                                PrefUtils.removeall(LoginActivity.this);
                                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                        }
                    }
                });
                sendSyncAsyncTask.execute();
            }else{
                Utils.alertBox(LoginActivity.this, R.string.txt_sync_failed,R.string.msg_no_internet_connection );
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
