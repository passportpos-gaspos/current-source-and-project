package com.pos.passport.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ingenico.framework.iconnecttsi.RequestType;
import com.ingenico.framework.iconnecttsi.iConnectTsiTypes;
import com.pos.passport.R;
import com.pos.passport.adapter.HardwareAdapter;
import com.pos.passport.adapter.PrinterAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.HardwareInterface;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.IngenicoTestConnection;
import com.pos.passport.util.MagTekDriver;
import com.pos.passport.util.MagTekStripCardParser;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by karim on 10/30/15.
 */
public class HardwareFragment extends Fragment implements Runnable {
    private final static String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private final static String DEBUG_TAG = "hardware_fragment";
    public final static int REQUEST_CODE_UPDATE_PRINTER = 1000;
    public final static int REQUEST_CODE_UPDATE_HARDWAREDEVICE = 2000;
    public LinearLayout mHardWareLinearLayout;
    public Button mAddPrinterButton;
    public ListView mPrinterListView;
    public ListView mHardwareListView;
    public ListView mCreditDeviceListView;
    public Button mCardReaderTestButton;
    public Button mAddHardwareButton;
    public TextView mPrinterTextView;
    public TextView mCardReaderTextView;
    public TextView mHardwareTextView;
    public ProgressDialog mProgressDialog;
    public Typeface mNotoSansBold;
    public MagTekDriver mMagTekDriver;

    public ProductDatabase mDb;
    public PrinterAdapter mPrintAdapter;
    public HardwareAdapter mCreditDeviceAdapter;
    public HardwareInterface mCallback;
    public Context mActivity;


    class MagTekListener implements MagTekDriver.MagStripeListener {

        @Override
        public void OnDeviceDisconnected() {
            mCardReaderTextView.setText(R.string.msg_magtek_disconnected);
        }

        @Override
        public void OnDeviceConnected() {
            mCardReaderTextView.setText(R.string.msg_magtek_connected);
        }

        @Override
        public void OnCardSwiped(String cardData) {
            try {
                MagTekStripCardParser mParser = new MagTekStripCardParser(cardData);
                if(mParser.isDataParse()){
                    if(mParser.hasTrack1()){
                        String accountNo = mParser.getAccountNumber();
                        accountNo = accountNo.replaceAll(accountNo.substring(0, accountNo.length() - 4), "XXXX-XXXX-XXXX-"); //Only last 4 digits of the card is required.
                        Utils.alertBox(getActivity(),R.string.txt_card_number, accountNo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                mProgressDialog.dismiss();
            } else if (msg.what == 8) {
                Utils.alertBox(getActivity(), R.string.txt_receipt_settings, R.string.msg_could_not_send_receipt);
                mProgressDialog.dismiss();
            }
        }
    };

    private View.OnClickListener mAddPrinterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PrinterFragment fragment = PrinterFragment.newInstance();
            fragment.setTargetFragment(HardwareFragment.this, REQUEST_CODE_UPDATE_PRINTER);
            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };

    private View.OnClickListener mCardReaderTestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMagTekDriver.startDevice();
            mCardReaderTextView.setText(R.string.txt_magtek_connected);
        }
    };

    private View.OnClickListener mAddCreditDeviceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddHardwareFragment fragment = AddHardwareFragment.newInstance();
            fragment.setTargetFragment(HardwareFragment.this, REQUEST_CODE_UPDATE_HARDWAREDEVICE);
            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hardware, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
        mMagTekDriver = new MagTekDriver(getActivity());
        bindUIElements(view);
        setUpListView();
        updateUI();
        setUpListeners();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (HardwareInterface) context;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_UPDATE_PRINTER && resultCode == Activity.RESULT_OK)
        {
            mPrintAdapter.changeCursor(mDb.getPrinters());
            mPrintAdapter.notifyDataSetChanged();
            updateUI();
        }

        if(requestCode == REQUEST_CODE_UPDATE_HARDWAREDEVICE && resultCode == Activity.RESULT_OK){
            mCreditDeviceAdapter.changeCursor(mDb.getHardwareDevices());
            mCreditDeviceAdapter.notifyDataSetChanged();
            updateUI();
        }
    }
    public void onRefreshData(int requestCode,Context mContext)
    {
        mDb = ProductDatabase.getInstance(mContext);
        if (requestCode == REQUEST_CODE_UPDATE_PRINTER )
        {
            mPrintAdapter.changeCursor(mDb.getPrinters());
            mPrintAdapter.notifyDataSetChanged();
            updateUI();
        }
        if(requestCode == REQUEST_CODE_UPDATE_HARDWAREDEVICE ){
            mCreditDeviceAdapter.changeCursor(mDb.getHardwareDevices());
            mCreditDeviceAdapter.notifyDataSetChanged();
            updateUI();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.equals(mPrinterListView)) {
            menu.setHeaderTitle(R.string.txt_printer_options);
            menu.add(0, v.getId(), 0, R.string.txt_test_printer);
            menu.add(0, v.getId(), 0, R.string.txt_edit_printer);
            menu.add(0, v.getId(), 0, R.string.txt_remove_printer);
        }else if(v.equals(mCreditDeviceListView)){
            menu.setHeaderTitle(R.string.txt_device_options);
            menu.add(0, v.getId(), 0, R.string.txt_test_device);
            menu.add(0, v.getId(), 0, R.string.txt_remove_device);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getTitle() == getString(R.string.txt_test_printer)) { testPrinter(item); }
        else if(item.getTitle() == getString(R.string.txt_edit_printer)){ editPrinter(item); }
        else if(item.getTitle() == getString(R.string.txt_remove_printer)){ removePrinter(item); }
        else if(item.getTitle() == getString(R.string.txt_test_device)){ testDevice(item); }
        else if(item.getTitle() == getString(R.string.txt_remove_device)){ removeDevice(item); }
        return true;
    }

    private void bindUIElements(View view) {
        mHardWareLinearLayout = (LinearLayout)view.findViewById(R.id.hardware_linear_layout);
        mAddPrinterButton = (Button)view.findViewById(R.id.add_printer_button);
        mPrinterListView = (ListView)view.findViewById(R.id.printer_list_view);
        mHardwareListView = (ListView)view.findViewById(R.id.hardware_list_view);
        mCreditDeviceListView = (ListView) view.findViewById(R.id.credit_device_list_view);
        mCardReaderTestButton = (Button)view.findViewById(R.id.card_reader_test_button);
        mAddHardwareButton = (Button)view.findViewById(R.id.add_hardware_button);
        mPrinterTextView = (TextView)view.findViewById(R.id.printer_text_view);
        mCardReaderTextView = (TextView)view.findViewById(R.id.card_reader_text_view);
        mHardwareTextView = (TextView)view.findViewById(R.id.hardware_text_view);
    }

    private void setUpListeners() {
        mAddPrinterButton.setOnClickListener(mAddPrinterClickListener);
        //mCardReaderTestButton.setOnClickListener(mCardReaderTestClickListener);
        //mAddHardwareButton.setOnClickListener(mAddHardwareClickListener);
        mMagTekDriver.registerMagStripeListener(new MagTekListener());
        mCardReaderTestButton.setOnClickListener(mAddCreditDeviceClickListener);
    }

    private void setUpListView() {
        mPrintAdapter = new PrinterAdapter(mDb.getPrinters(), getActivity());
        mPrinterListView.setAdapter(mPrintAdapter);
        registerForContextMenu(mPrinterListView);
        mCreditDeviceAdapter = new HardwareAdapter(mDb.getHardwareDevices(), getActivity());
        mCreditDeviceListView.setAdapter(mCreditDeviceAdapter);
        registerForContextMenu(mCreditDeviceListView);
    }

    private void updateUI() {
        mHardwareListView.setVisibility(View.GONE);
        if (mPrintAdapter.getCount() > 0) {
            mPrinterListView.setVisibility(View.VISIBLE);
            mPrinterTextView.setText("");
        } else {
            mPrinterListView.setVisibility(View.GONE);
            mPrinterTextView.setText(R.string.txt_no_printer_found);
        }
        Log.d(DEBUG_TAG, String.valueOf(mCreditDeviceAdapter.getCount()));
        if (mCreditDeviceAdapter.getCount() > 0) {
            mCreditDeviceListView.setVisibility(View.VISIBLE);
        } else {
            mCreditDeviceListView.setVisibility(View.GONE);
        }

        mAddPrinterButton.setTypeface(mNotoSansBold);
        mAddHardwareButton.setTypeface(mNotoSansBold);
        mCardReaderTestButton.setTypeface(mNotoSansBold);
    }

    private void editPrinter(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mPrinterListView.getItemAtPosition(info.position);
        String json = c.getString(c.getColumnIndex("blurb"));
        int id = c.getInt(c.getColumnIndex("_id"));

        try {
            JSONObject object = new JSONObject(json);

            ReceiptSetting.enabled = true;
            ReceiptSetting.address = object.optString("address");
            ReceiptSetting.make = object.optInt("printer");
            ReceiptSetting.size = object.optInt("size");
            ReceiptSetting.type = object.optInt("type");
            ReceiptSetting.drawer = object.optBoolean("cashDrawer");
            ReceiptSetting.mainPrinter = object.optBoolean("main");
            ReceiptSetting.openOrderPrinter = object.optBoolean("openOrderPrinter");

            PrinterFragment fragment = PrinterFragment.newInstance();
            fragment.setTargetFragment(HardwareFragment.this, REQUEST_CODE_UPDATE_PRINTER);
            Bundle bundle = new Bundle();
            bundle.putInt(Consts.BUNDLE_ID, id);
            fragment.setArguments(bundle);
            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void testPrinter(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mPrinterListView.getItemAtPosition(info.position);

        String json = c.getString(c.getColumnIndex("blurb"));

        try {
            JSONObject object = new JSONObject(json);

            ReceiptSetting.enabled = true;
            ReceiptSetting.address = object.getString("address");
            ReceiptSetting.make = object.getInt("printer");
            ReceiptSetting.size = object.getInt("size");
            ReceiptSetting.type = object.getInt("type");
            ReceiptSetting.drawer = object.getBoolean("cashDrawer");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mProgressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.txt_sending_test_receipt), true, false);
        Thread thread = new Thread(this);
        thread.start();
    }

    private void removePrinter(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mPrinterListView.getItemAtPosition(info.position);
        Log.e("removeDevice","printer id>>>"+c.getInt(c.getColumnIndex("_id")));
       // mDb.removePrinter(c.getInt(c.getColumnIndex("_id")));
        ((PrinterAdapter) mPrinterListView.getAdapter()).getCursor().requery();
        updateUI();
        mCallback.onDelete(c.getInt(c.getColumnIndex("_id")),Consts.DELETEPRINTER_TEXT);
    }

    private void testDevice(MenuItem item){
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mCreditDeviceListView.getItemAtPosition(info.position);
        RequestType.Sale saleReq = new RequestType.Sale(1);
        saleReq.setClerkId(1);
        saleReq.setEcrTenderType(new iConnectTsiTypes.EcrTenderType.Credit());
        IngenicoTestConnection testConnection = new IngenicoTestConnection(getActivity(), c.getString(c.getColumnIndex("ipAddress")), c.getString(c.getColumnIndex("port"))) ;
        testConnection.setListener(new IngenicoTestConnection.IngenicoTestListener() {
            @Override
            public void onSuccess(String title, String message) {
                mCardReaderTextView.setText(R.string.txt_connected);
            }

            @Override
            public void onFailure(final String title, final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.alertBox(getActivity(), title, message);
                        mCardReaderTextView.setText(R.string.txt_not_connected);
                    }
                });
            }
        });
        testConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, saleReq);
    }

    private void removeDevice(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mCreditDeviceListView.getItemAtPosition(info.position);
        //Log.e("removeDevice","id>>>"+c.getInt(c.getColumnIndex("_id")));
        //mDb.removeHardwareDevice(c.getInt(c.getColumnIndex("_id")));
        ((HardwareAdapter) mCreditDeviceListView.getAdapter()).getCursor().requery();
        updateUI();
        mCallback.onDelete(c.getInt(c.getColumnIndex("_id")),Consts.DELETECARDREADER_TEXT);
    }

    @Override
    public void run() {
        Log.v("Thread", "Starting Thread");
        if (issueReceipt()) {
            Message m = new Message();
            m.what = 10;
            mHandler.sendMessage(m);
        } else {
            Message m2 = new Message();
            m2.what = 8;
            mHandler.sendMessage(m2);
        }
    }

    @SuppressLint("NewApi")
    private boolean issueReceipt() {

        int cols = 40;
        if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
            cols = 30;

        StringBuilder testString = new StringBuilder();
        String textToPrint = "Connected to Printer Successfully.";
        testString.append('\n').append('\n').append(EscPosDriver.wordWrap(textToPrint, cols + 1)).append('\n').append('\n');

        return EscPosDriver.print(getActivity(), testString.toString(), ReceiptSetting.drawer);
    }


}
