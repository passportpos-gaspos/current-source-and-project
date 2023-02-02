package com.pos.passport.activity;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.fragment.AccountFragment;
import com.pos.passport.fragment.InventoryFragment;
import com.pos.passport.fragment.QuickButtonFragment;
import com.pos.passport.fragment.ReportsFragment;
import com.pos.passport.interfaces.MenuButtonInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/30/15.
 */
public class MainReportsActivity extends BaseActivity implements MenuButtonInterface {
    private TabLayout mAdminTabLayout;
    private ViewPager mAdminViewPager;
    ViewPagerAdapter adapter;
    private ProgressDialog mProgressDialogall;
 @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainReportsActivity","MainReportsActivity");
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        setContentView(R.layout.activity_admin);
        mProgressDialogall= new ProgressDialog(MainReportsActivity.this);
        mProgressDialogall.setMessage("Loading...");
        mProgressDialogall.setCancelable(false);
        mProgressDialogall.setIndeterminate(true);
     this.runOnUiThread(new Runnable() {
         @Override
         public void run() {
             mProgressDialogall.show();
         }
     });
        bindUIElements();
        ViewGroup vg = (ViewGroup)getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setUpUIs();
    }

    private void bindUIElements() {
        mAdminTabLayout = (TabLayout) findViewById(R.id.admin_tab_layout);
       // mAdminTabLayout.addTab(mAdminTabLayout.newTab().setText("Reports"));
        //mAdminTabLayout.addTab(mAdminTabLayout.newTab().setText("Inventory"));
        //mAdminTabLayout.addTab(mAdminTabLayout.newTab().setText("Keyboard"));
        //mAdminTabLayout.addTab(mAdminTabLayout.newTab().setText("Account Info"));
        mAdminViewPager = (ViewPager) findViewById(R.id.admin_view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
    }

    private void setUpUIs()
    {
        setupViewPager(mAdminViewPager);
    }

    private void setupViewPager(ViewPager viewPager)
    {
       new AsyncTaskRunner().execute();
    }

    @Override
    public void onNotifyQueueChanged() {
        // N/A
    }

    @Override
    public void onAddProduct(Product product) {
        // N/A
    }

    @Override
    public Cart getCart() {
        // N/A
        return null;
    }

    @Override
    public List<Product> getProducts() {
        // N/A
        return null;
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Integer> mFragmentTitleList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, @StringRes int titleRes) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(titleRes);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mFragmentTitleList.get(position));
        }
    }
    private class AsyncTaskRunner extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... voids)
        {
            adapter.addFragment(new ReportsFragment(), R.string.txt_reports);
            adapter.addFragment(new InventoryFragment(), R.string.txt_inventory);
            adapter.addFragment(new QuickButtonFragment(), R.string.txt_keyboard);
            adapter.addFragment(new AccountFragment(), R.string.txt_account_info);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            mAdminViewPager.setAdapter(adapter);
            mAdminViewPager.setCurrentItem(0,true);
            mAdminTabLayout.setupWithViewPager(mAdminViewPager);
            mProgressDialogall.dismiss();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            mProgressDialogall.setMessage(values[0]);
        }
    }

}
