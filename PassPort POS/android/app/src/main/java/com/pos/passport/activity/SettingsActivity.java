package com.pos.passport.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pos.passport.BuildConfig;
import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.HardwareFragment;
import com.pos.passport.fragment.OfflineFragment;
import com.pos.passport.fragment.ReceiptsFragment;
import com.pos.passport.fragment.SaleFragment;
import com.pos.passport.fragment.TaxFragment;
import com.pos.passport.interfaces.AsyncTaskListenerData;
import com.pos.passport.interfaces.HardwareInterface;
import com.pos.passport.model.Device;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.task.DeleteHardwareAsyncTask;
import com.pos.passport.task.GetHardwareAsyncTask;
import com.pos.passport.task.SendHardwareAsyncTask;
import com.pos.passport.util.Consts;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/30/15.
 */
public class SettingsActivity extends BaseActivity implements HardwareInterface {
    private TabLayout mSettingsTabLayout;
    private ViewPager mSettingsViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ProductDatabase mDb;
    HardwareFragment mHardwareFragment;
    private static final int mNothingToCall = -1;
    private static final int mCallPrinter = 0;
    private static final int mCallCardReader = 1;
    private static final int mCallBoth = 2;
    private HardwareUpdate receiver_update;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        setContentView(R.layout.activity_settings);
        mDb = ProductDatabase.getInstance(this);
        bindUIElements();
        setUpUIs();
        mHardwareFragment = new HardwareFragment();
        IntentFilter hardwareUpdateIntentFilter = new IntentFilter(HardwareUpdate.PROCESS_RESPONSE_SETTING);
        hardwareUpdateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver_update = new HardwareUpdate();
        registerReceiver(receiver_update, hardwareUpdateIntentFilter);
        onCheckHardwareSetting();
        ViewGroup vg = (ViewGroup) getWindow().getDecorView();
        Utils.setTypeFace(Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf"), vg);
    }

    private void bindUIElements() {
        mSettingsTabLayout = (TabLayout) findViewById(R.id.settings_tab_layout);
        mSettingsViewPager = (ViewPager) findViewById(R.id.settings_view_pager);
    }

    private void setUpUIs() {
        setupViewPager(mSettingsViewPager);
        mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new HardwareFragment(), R.string.txt_hardware);
        mViewPagerAdapter.addFragment(new ReceiptsFragment(), R.string.txt_receipts);
        mViewPagerAdapter.addFragment(new TaxFragment(), R.string.txt_tax);
        mViewPagerAdapter.addFragment(new OfflineFragment(), R.string.txt_offline);
        if (BuildConfig.DEBUG) {
            mViewPagerAdapter.addFragment(new SaleFragment(), R.string.txt_sale);
        }

        viewPager.setAdapter(mViewPagerAdapter);
    }

    private void onCheckHardwareSetting()
    {
        Cursor mDbPrinters = mDb.getPrinters();
        Cursor mDbHardwareDevices = mDb.getHardwareDevices();
        //Log.e("mDbPrinters","mDbPrinters>>>"+mDbPrinters.getCount());
        //Log.e("mDbHardwareDevices","mDbHardwareDevices>>>"+mDbHardwareDevices.getCount());
        if (mDbPrinters.getCount() == 0 && mDbHardwareDevices.getCount() == 0)
            alertBox(this, R.string.txt_hardware_title, R.string.msg_hardware_setting, mCallBoth);
        else if (mDbPrinters.getCount() == 0 && mDbHardwareDevices.getCount() > 0)
            alertBox(this, R.string.txt_hardware_title_printer, R.string.msg_hardware_setting_printer, mCallPrinter);
        else if (mDbPrinters.getCount() > 0 && mDbHardwareDevices.getCount() == 0)
            alertBox(this, R.string.txt_hardware_title_card, R.string.msg_hardware_setting_card, mCallCardReader);
    }

    @Override
    protected void onForwarded() {
        if (mSettingsViewPager.getCurrentItem() == 3) {
            Fragment fragment = mViewPagerAdapter.getItem(3);
            if (fragment instanceof OfflineFragment) {
                ((OfflineFragment) fragment).refresh();
            }
        } else if (mSettingsViewPager.getCurrentItem() == 4) {
            Fragment fragment = mViewPagerAdapter.getItem(4);
            if (fragment instanceof SaleFragment) {
                ((SaleFragment) fragment).refresh();
            }
        }
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
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_right, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
            item.setTitle(spanString);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cumulus_txt:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onAdd(JSONObject insertdata, JSONObject data, int checkId) {
        try {
            if (Utils.hasInternet(SettingsActivity.this)) {
                JSONObject datasend = data;
                String method = RestAgent.POST;
                if (checkId > 0) {
                    int serverId = mDb.findPrinterServerId(checkId);
                    datasend.put("printerId", serverId);
                    method = RestAgent.PUT;
                    int rowId = mDb.insertReceiptSettings(insertdata.toString(), checkId);
                }
                AddHardwareSync(insertdata, datasend, checkId, method);
            } else {
                Toast.makeText(SettingsActivity.this, "No internet connection. Connect and try again", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("Error", "Error>>" + e.getMessage());
        }

    }

    @Override
    public void onDelete(int rowid, String type) {
        try {
            if (Utils.hasInternet(SettingsActivity.this)) {
                if (type.equalsIgnoreCase(Consts.DELETEPRINTER_TEXT)) {
                    int serverId = mDb.findPrinterServerId(rowid);
                    JSONObject deletedata = new JSONObject();
                    deletedata.put("printerId", serverId);
                    DeleteHardwareSync(deletedata, Consts.DELETEPRINTER_TEXT);
                } else {

                    JSONObject deletedata = new JSONObject();
                    deletedata.put("cardReaderId", rowid);
                    DeleteHardwareSync(deletedata, Consts.DELETECARDREADER_TEXT);
                }
            } else {
                Toast.makeText(SettingsActivity.this, "No internet connection. Connect and try again", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddCardReader(JSONObject data, Device device) {
        if (Utils.hasInternet(SettingsActivity.this)) {
            AddCardReaderSync(data, device);
        } else {
            Toast.makeText(SettingsActivity.this, "No internet connection. Connect and try again", Toast.LENGTH_LONG).show();
        }
    }


    private void AddHardwareSync(final JSONObject insertdata, JSONObject data, final int checkId, String method) {
        SendHardwareAsyncTask asyncTask = new SendHardwareAsyncTask(this, true, Consts.ADDPRINTER_TEXT, data, method);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1) {
                try {
                    //Log.e(" printer", "Result >>>>" + data1);
                    if (data1 != null) {
                        JSONObject data = new JSONObject(data1);
                        if (checkId > 0) {
                            if (data.has("status"))
                                Toast.makeText(SettingsActivity.this, "Successfully Edit.", Toast.LENGTH_LONG).show();

                            mSettingsViewPager.getAdapter().notifyDataSetChanged();
                            // mDb.UpdatePrintSettings("",checkId);
                        } else {
                            int printerId = data.getInt("printerId");
                            insertdata.put("serverId", printerId);
                            int rowId = mDb.insertReceiptSettings(insertdata.toString(), 0);
                            mDb.UpdatePrintSettings(printerId, rowId);
                            mSettingsViewPager.getAdapter().notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
            }
        });
        asyncTask.execute("", "", "");
    }

    private void AddCardReaderSync(JSONObject data, final Device device) {
        //Log.e("Open frag","register");
        SendHardwareAsyncTask asyncTask = new SendHardwareAsyncTask(this, true, Consts.ADDCARDREADER_TEXT, data, RestAgent.POST);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1)
            {
                try
                {
                    //Log.e("Add Card", "Result >>>>" + data1);
                    if (data1 != null) {
                        JSONObject data = new JSONObject(data1);
                        if (data.has("cardReaderId")) {
                            int cardReaderId = Integer.parseInt(data.getString("cardReaderId"));
                            device.setId(cardReaderId);
                            mDb.saveHardwareDevice(device, 0);
                            mSettingsViewPager.getAdapter().notifyDataSetChanged();
                            // mHardwareFragment.onRefreshData(mHardwareFragment.REQUEST_CODE_UPDATE_HARDWAREDEVICE,SettingsActivity.this);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
            }
        });
        asyncTask.execute("", "", "");
    }

    private void DeleteHardwareSync(final JSONObject data, final String type) {
        //Toast toast = Toast.makeText(SettingsActivity.this, "Json:" + data.toString(),Toast.LENGTH_SHORT);
        //toast.show();
        DeleteHardwareAsyncTask asyncTask = new DeleteHardwareAsyncTask(this, true, type, data);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1)
            {
                try
                {
                    //Log.d("onsuccess",">>>"+data1);
                    if (data1 != null)
                    {
                        JSONObject result = new JSONObject(data1);
                        /*if (result.has("error"))
                        {
                            Toast.makeText(SettingsActivity.this, "" + result.optString("error"), Toast.LENGTH_LONG).show();
                        }
                        else
                        {*/
                            if (type.equalsIgnoreCase(Consts.DELETECARDREADER_TEXT)) {
                                int id = data.getInt("cardReaderId");
                                mDb.removeHardwareDevice(id);
                                mSettingsViewPager.getAdapter().notifyDataSetChanged();
                                //mHardwareFragment.onRefreshData(mHardwareFragment.REQUEST_CODE_UPDATE_HARDWAREDEVICE,SettingsActivity.this);
                            }
                            if (type.equalsIgnoreCase(Consts.DELETEPRINTER_TEXT))
                            {
                                int id = data.getInt("printerId");
                                mDb.removePrinter(id);
                                mSettingsViewPager.getAdapter().notifyDataSetChanged();
                            }
                        //}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
                Toast toast = Toast.makeText(SettingsActivity.this, "Failed to delete", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        asyncTask.execute("", "", "");
    }

    public void alertBox(Context context, @StringRes int titleId, @StringRes int messageId, final int mCheckCallAsync) {
        new AlertDialog.Builder(context)
                .setMessage(messageId)
                .setTitle(titleId)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (mCheckCallAsync == mCallBoth)
                                {
                                    CallSync(Consts.GET_PRINTER_TEXT,"Loading Printer Data...",mCallBoth);
                                } else if (mCheckCallAsync == mCallPrinter)
                                {
                                    CallSync(Consts.GET_PRINTER_TEXT,"Loading Printer Data...",mCallPrinter);
                                } else if (mCheckCallAsync == mCallCardReader)
                                {
                                    CallSync(Consts.GET_CARDREADER_TEXT,"Loading Card Reader Data...",mCallCardReader);
                                }

                            }
                        })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    private void CallSync(String type, String msg,final int mCheckCallAsync)
    {

        GetHardwareAsyncTask asyncTask = new GetHardwareAsyncTask(this, true, type, msg, RestAgent.POST);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1)
            {
                try
                {
                    //Log.e("data", "Result >>>>" + data1);
                    if (data1 != null)
                    {
                        if(mCheckCallAsync == mCallBoth || mCheckCallAsync == mCallPrinter)
                        {
                            JSONArray devicedata=new JSONArray(data1);
                            if(devicedata.length()>0) {
                                for (int d = 0; d < devicedata.length(); d++) {
                                    JSONObject tempcreate = new JSONObject();
                                    JSONObject getObjects = devicedata.getJSONObject(d);
                                    int serverid = Integer.parseInt(getObjects.optString("printerId"));
                                    tempcreate.put("serverId", serverid);
                                    tempcreate.put("printer", GetPrinterType(getObjects.optString("printer")));
                                    tempcreate.put("type", getObjects.optString("printerType"));
                                    tempcreate.put("size", getObjects.optString("printerSize"));
                                    tempcreate.put("address", getObjects.optString("ipAddress"));
                                    if (getObjects.optString("isKickCashDrawer").equalsIgnoreCase("YES"))
                                        tempcreate.put("cashDrawer", true);
                                    else
                                        tempcreate.put("cashDrawer", false);

                                    if (getObjects.optString("isPrinterForOpenOrder").equalsIgnoreCase("YES"))
                                        tempcreate.put("openOrderPrinter", true);
                                    else
                                        tempcreate.put("openOrderPrinter", false);

                                    if (getObjects.optString("isMainPrinter").equalsIgnoreCase("YES"))
                                        tempcreate.put("main", true);
                                    else
                                        tempcreate.put("main", false);
                                    mDb.insertPrinterSettings(tempcreate.toString(), 0, serverid);
                                }
                            }
                        }
                        if(mCheckCallAsync == mCallCardReader)
                        {
                            JSONArray devicedata=new JSONArray(data1);
                            if(devicedata.length()>0) {
                                for (int d = 0; d < devicedata.length(); d++) {
                                    JSONObject getobjeJsonObjects = devicedata.getJSONObject(d);
                                    Device device = new Device();
                                    device.setDeviceName(getobjeJsonObjects.optString("name"));
                                    device.setDeviceType(getobjeJsonObjects.optString("cardReader"));
                                    device.setIpaddress(getobjeJsonObjects.optString("ipAddress"));
                                    device.setPort(getobjeJsonObjects.optString("port"));
                                    device.setId(Integer.parseInt(getobjeJsonObjects.optString("cardReaderId")));
                                    mDb.saveHardwareDevice(device, 0);
                                }
                            }
                        }
                    }
                    if (mCheckCallAsync == mCallBoth)
                    {
                        CallSync(Consts.GET_CARDREADER_TEXT,"Loading Card Reader Data...",mCallCardReader);
                    }

                    mSettingsViewPager.getAdapter().notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure() {
            }
        });
        asyncTask.execute("", "", "");
    }
    public int GetPrinterType(String typeValue)
    {
        int mPrinterType=0;
        if (this.getResources().getString(R.string.txt_custom_america_t_ten).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_CUSTOM;
        if (this.getResources().getString(R.string.txt_generic_esc_pos).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_ESCPOS;
        if (this.getResources().getString(R.string.txt_partner_tech_pt6210).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_PT6210;
        if (this.getResources().getString(R.string.txt_snbc).equalsIgnoreCase(typeValue))
            mPrinterType  = ReceiptSetting.MAKE_SNBC;
        if (this.getResources().getString(R.string.txt_tsp100_lan).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_STAR;
        if (this.getResources().getString(R.string.txt_elo_touch).equalsIgnoreCase(typeValue))
            mPrinterType = ReceiptSetting.MAKE_ELOTOUCH;

        return mPrinterType;

    }


    public class HardwareUpdate extends BroadcastReceiver
    {
        public static final String PROCESS_RESPONSE_SETTING = "com.pos.cumulus.intent.action.PROCESS_RESPONSE_PROCESS_RESPONSE_SETTING";

        @Override
        public void onReceive(Context context, Intent intent)
        {
            try {
                if (mSettingsViewPager.getCurrentItem() == 0)
                {
                    Log.d("Call brod", "Getting brod if con");
                    mSettingsViewPager.getAdapter().notifyDataSetChanged();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
