package com.pos.passport.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.HardwareInterface;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import POSAPI.POSUSBAPI;
import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;

/**
 * Created by karim on 11/2/15.
 */
public class PrinterFragment extends DialogFragment {
    public final static int MAKE_STAR = 1;
    public final static int MAKE_CUSTOM = 2;
    public final static int MAKE_ESCPOS = 3;
    public final static int MAKE_SNBC = 4;
    public final static int MAKE_PT6210 = 5;
    public final static int MAKE_ELOTOUCH = 6;
    private final static String ACTION_USB_PERMISSION = "com.passport.permission.USB_PERMISSION";

    private int mId;
    private int mPrinterType;
    private Spinner mPrinterTypeSpinner;
    private RadioGroup mPrinterConnectionTypeRadioGroup;
    private RadioButton mConnectionTypeTCPRadioButton;
    private RadioButton mConnectionTypeBluetoothRadioButton;
    private RadioButton mConnectionTypeUSBRadioButton;
    private RadioGroup mPrinterSizeRadioGroup;
    private RadioButton mSize2InchRadioButton;
    private RadioButton mSize3InchRadioButton;
    private CheckBox mKickCashDrawerCheckBox;
    private CheckBox mMainPrinterCheckBox;
    private CheckBox mPrintOpenOrderCheckBox;
    private EditText mIPAddressEditText;
    private Button mSearchPrinterButton;

    public ProductDatabase mDb;
    private PendingIntent mPermissionIntent;

    public ArrayList<PortInfo> arrayDiscovery;
    public ArrayList<String> arrayPortName;
    public BluetoothDevice[] btDeviceList;
    public ArrayList<BluetoothDevice> mDeviceAdapter;

    public boolean mTCPChecked;
    public boolean mBluetoothChecked;
    public boolean mUSBChecked;
    public HardwareInterface mCallback;
    private RadioGroup.OnCheckedChangeListener mRadioCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.connection_type_tcp_radio_button:
                    mIPAddressEditText.setEnabled(true);
                    mSearchPrinterButton.setEnabled(true);
                    mTCPChecked = true;
                    mBluetoothChecked = false;
                    mUSBChecked = false;
                    break;

                case R.id.connection_type_bluetooth_radio_button:
                    mIPAddressEditText.setEnabled(true);
                    mSearchPrinterButton.setEnabled(true);
                    mTCPChecked = false;
                    mBluetoothChecked = true;
                    mUSBChecked = false;
                    break;

                case R.id.connection_type_usb_radio_button:
                    mIPAddressEditText.setEnabled(false);
                    mSearchPrinterButton.setEnabled(false);
                    mTCPChecked = false;
                    mBluetoothChecked = false;
                    mUSBChecked = true;
                    checkUSBPrinter();
                    break;
            }
        }
    };

    private AdapterView.OnItemSelectedListener mPrinterTypeSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: // Generic ESC/POS
                    mPrinterType = MAKE_ESCPOS;
                    disableFields();
                    enableESCPOS();
                    break;

                case 1: // Star TSP100LAN
                    mPrinterType = MAKE_STAR;
                    disableFields();
                    enableStar();
                    break;

                case 2: // SNBC
                    mPrinterType = MAKE_SNBC;
                    disableFields();
                    enableSNBC();
                    break;

                case 3: // Custom America T-Ten
                    mPrinterType = MAKE_CUSTOM;
                    disableFields();
                    enableCustom();
                    break;

                case 4: // Partner Tech PT-6210
                    mPrinterType = MAKE_PT6210;
                    disableFields();
                    enablePT6210();
                    break;
                case 5: //ELo
                    mPrinterType = MAKE_ELOTOUCH;
                    disableFields();
                    enableElo();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    public static PrinterFragment newInstance() {
        PrinterFragment fragment = new PrinterFragment();
        fragment.setCancelable(false);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.txt_add_printer)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                savePrinter(mId);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        if (getArguments() != null)
            mId = getArguments().getInt(Consts.BUNDLE_ID, 0);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_add_printer, null, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        mDb = ProductDatabase.getInstance(getActivity());
        mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().registerReceiver(mUsbReceiver, filter);
        bindUIElements(view);
        setUpUI();
        setUpListeners();
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        getActivity().unregisterReceiver(mUsbReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (HardwareInterface) context;
    }

    private void bindUIElements(View view) {
        mPrinterTypeSpinner = (Spinner)view.findViewById(R.id.printer_type_spinner);
        mPrinterConnectionTypeRadioGroup = (RadioGroup)view.findViewById(R.id.printer_connection_type_radio_group);
        mConnectionTypeTCPRadioButton = (RadioButton)view.findViewById(R.id.connection_type_tcp_radio_button);
        mConnectionTypeBluetoothRadioButton = (RadioButton)view.findViewById(R.id.connection_type_bluetooth_radio_button);
        mConnectionTypeUSBRadioButton = (RadioButton)view.findViewById(R.id.connection_type_usb_radio_button);
        mPrinterSizeRadioGroup = (RadioGroup)view.findViewById(R.id.printer_size_radio_group);
        mSize2InchRadioButton = (RadioButton)view.findViewById(R.id.size_2_inch_radio_button);
        mSize3InchRadioButton = (RadioButton)view.findViewById(R.id.size_3_inch_radio_button);
        mKickCashDrawerCheckBox = (CheckBox)view.findViewById(R.id.kick_cash_drawer_check_box);
        mMainPrinterCheckBox = (CheckBox)view.findViewById(R.id.main_printer_check_box);
        mIPAddressEditText = (EditText)view.findViewById(R.id.ip_address_edit_text);
        mSearchPrinterButton = (Button)view.findViewById(R.id.search_printer_button);
        mPrintOpenOrderCheckBox =  (CheckBox) view.findViewById(R.id.print_open_order_check_box);
    }

    private void setUpUI() {
        ArrayAdapter<CharSequence> printerTypeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.printer_types, R.layout.view_printer_type_item);
        mPrinterTypeSpinner.setAdapter(printerTypeAdapter);
        if (mId > 0) {
            switch (ReceiptSetting.make) {
                case MAKE_ESCPOS:
                    mPrinterTypeSpinner.setSelection(0);
                    break;

                case MAKE_SNBC:
                    mPrinterTypeSpinner.setSelection(2);
                    break;

                case MAKE_STAR:
                    mPrinterTypeSpinner.setSelection(1);
                    break;

                case MAKE_PT6210:
                    mPrinterTypeSpinner.setSelection(4);
                    break;

                case MAKE_CUSTOM:
                    mPrinterTypeSpinner.setSelection(3);
                    break;
                case MAKE_ELOTOUCH:
                    mPrinterTypeSpinner.setSelection(5);
                    break;
            }

            switch (ReceiptSetting.type) {
                case ReceiptSetting.TYPE_LAN:
                    mPrinterConnectionTypeRadioGroup.check(R.id.connection_type_tcp_radio_button);
                    break;

                case ReceiptSetting.TYPE_USB:
                    mPrinterConnectionTypeRadioGroup.check(R.id.connection_type_usb_radio_button);
                    break;

                case ReceiptSetting.TYPE_BT:
                    mPrinterConnectionTypeRadioGroup.check(R.id.connection_type_bluetooth_radio_button);
                    break;
            }
            if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
                mPrinterSizeRadioGroup.check(R.id.size_2_inch_radio_button);
            else if (ReceiptSetting.size == ReceiptSetting.SIZE_3)
                mPrinterSizeRadioGroup.check(R.id.size_3_inch_radio_button);
            mKickCashDrawerCheckBox.setChecked(ReceiptSetting.drawer);
            mMainPrinterCheckBox.setChecked(ReceiptSetting.mainPrinter);
        }
    }

    private void setUpListeners() {
        mPrinterTypeSpinner.setOnItemSelectedListener(mPrinterTypeSelectedListener);
        mPrinterConnectionTypeRadioGroup.setOnCheckedChangeListener(mRadioCheckedChangeListener);
        mPrinterSizeRadioGroup.setOnCheckedChangeListener(mRadioCheckedChangeListener);
    }

    private void disableFields() {
        mConnectionTypeTCPRadioButton.setEnabled(false);
        mConnectionTypeBluetoothRadioButton.setEnabled(false);
        mConnectionTypeUSBRadioButton.setEnabled(false);

        mSize2InchRadioButton.setEnabled(false);
        mSize3InchRadioButton.setEnabled(false);

        mKickCashDrawerCheckBox.setEnabled(false);
        mIPAddressEditText.setEnabled(false);
        mSearchPrinterButton.setEnabled(false);
    }

    private void enableStar() {
        mIPAddressEditText.setEnabled(true);
        mSize2InchRadioButton.setEnabled(true);
        mSize3InchRadioButton.setEnabled(true);

        mConnectionTypeTCPRadioButton.setEnabled(true);
    }

    private void enableSNBC() {
        mIPAddressEditText.setEnabled(true);
        mKickCashDrawerCheckBox.setEnabled(true);
        mSize2InchRadioButton.setEnabled(true);
        mSize3InchRadioButton.setEnabled(true);

        mConnectionTypeUSBRadioButton.setEnabled(true);

        mConnectionTypeUSBRadioButton.setChecked(true);
    }

    private void enableESCPOS() {
        mIPAddressEditText.setEnabled(true);
        mKickCashDrawerCheckBox.setEnabled(true);
        mSize2InchRadioButton.setEnabled(true);
        mSize3InchRadioButton.setEnabled(true);

        mConnectionTypeTCPRadioButton.setEnabled(true);
        mConnectionTypeUSBRadioButton.setEnabled(true);
        mConnectionTypeBluetoothRadioButton.setEnabled(true);

        mConnectionTypeTCPRadioButton.setChecked(true);
    }

    private void enablePT6210() {
        mIPAddressEditText.setEnabled(true);
        mKickCashDrawerCheckBox.setEnabled(true);
        mSize2InchRadioButton.setEnabled(true);
        mSize3InchRadioButton.setEnabled(true);

        mConnectionTypeUSBRadioButton.setEnabled(true);

        mConnectionTypeUSBRadioButton.setChecked(true);
    }

    private void enableElo(){
        mKickCashDrawerCheckBox.setChecked(true);
        mSize2InchRadioButton.setChecked(true);
        mConnectionTypeUSBRadioButton.setChecked(true);
        mIPAddressEditText.setText("0.0.0.0");
    }

    private void enableCustom() {
        mIPAddressEditText.setEnabled(true);
        mKickCashDrawerCheckBox.setEnabled(true);
        mSize2InchRadioButton.setEnabled(true);
        mSize3InchRadioButton.setEnabled(true);

        mConnectionTypeTCPRadioButton.setEnabled(true);
        mConnectionTypeUSBRadioButton.setEnabled(true);

        mConnectionTypeTCPRadioButton.setChecked(true);
    }

    private void savePrinter(int id) {
        int type = 0;
        String mTypeText="";
        String mSizeTypeText="";
        if (mConnectionTypeTCPRadioButton.isChecked())
        {
            type = 1;
            mTypeText= (String) mConnectionTypeTCPRadioButton.getText();
        }
        if (mConnectionTypeUSBRadioButton.isChecked()) {
            type = 2;
            mTypeText= (String) mConnectionTypeUSBRadioButton.getText();
        }
        if (mConnectionTypeBluetoothRadioButton.isChecked()) {
            type = 3;
            mTypeText= (String) mConnectionTypeBluetoothRadioButton.getText();
        }
        int size = 0;
        if (mSize2InchRadioButton.isChecked()) {
            size = 1;
            mSizeTypeText= (String) mSize2InchRadioButton.getText();
        }
        if (mSize3InchRadioButton.isChecked()) {
            size = 2;
            mSizeTypeText = (String) mSize3InchRadioButton.getText();
        }

        JSONObject json = new JSONObject();
        JSONObject jsonsend = new JSONObject();
        try {

            json.put("printer", mPrinterType);
            json.put("type", type);
            json.put("size", size);
            json.put("address", mIPAddressEditText.getText().toString().trim());
            json.put("cashDrawer", mKickCashDrawerCheckBox.isChecked());
            json.put("openOrderPrinter", mPrintOpenOrderCheckBox.isChecked());
            json.put("main", mMainPrinterCheckBox.isChecked());


            jsonsend.put("printer", GetPrinterType(mPrinterType));
            jsonsend.put("printerType", mTypeText);
            jsonsend.put("printerSize", mSizeTypeText);
            jsonsend.put("ipAddress", mIPAddressEditText.getText().toString().trim());

            if(mKickCashDrawerCheckBox.isChecked())
                jsonsend.put("isKickCashDrawer", "YES");
            else
                jsonsend.put("isKickCashDrawer", "NO");

            if(mPrintOpenOrderCheckBox.isChecked())
                jsonsend.put("isPrinterForOpenOrder", "YES");
            else
                jsonsend.put("isPrinterForOpenOrder", "NO");

            if(mMainPrinterCheckBox.isChecked())
                jsonsend.put("isMainPrinter", "YES");
            else
                jsonsend.put("isMainPrinter", "NO");

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        //int rowId = mDb.insertReceiptSettings(json.toString(), id);
        //Log.e("rowId Printer","Rowid>>>>"+rowId);
        mCallback.onAdd(json,jsonsend,id);
        //AddPrinterSync(jsonsend,rowId);
    }
    public String GetPrinterType(int typeValue)
    {
        String mPrinterType="";
        if (typeValue == ReceiptSetting.MAKE_CUSTOM)
            mPrinterType = getActivity().getResources().getString(R.string.txt_custom_america_t_ten);
        if (typeValue == ReceiptSetting.MAKE_ESCPOS)
            mPrinterType = getActivity().getResources().getString(R.string.txt_generic_esc_pos);
        if (typeValue == ReceiptSetting.MAKE_PT6210)
            mPrinterType = getActivity().getResources().getString(R.string.txt_partner_tech_pt6210);
        if (typeValue == ReceiptSetting.MAKE_SNBC)
            mPrinterType = getActivity().getResources().getString(R.string.txt_snbc);
        if (typeValue == ReceiptSetting.MAKE_STAR)
            mPrinterType = getActivity().getResources().getString(R.string.txt_tsp100_lan);
        if (typeValue == ReceiptSetting.MAKE_ELOTOUCH)
            mPrinterType = getActivity().getResources().getString(R.string.txt_elo_touch);

        return mPrinterType;

    }
    /*private void AddPrinterSync(JSONObject data,int rowId) {
        //Log.e("Open frag","register");
        SendHardwareAsyncTask asyncTask = new SendHardwareAsyncTask(getActivity(), true, Consts.ADDPRINTER_TEXT,data);
        asyncTask.setListener(new AsyncTaskListenerData() {
            @Override
            public void onSuccess(String data1) {
                try {
                    Log.e("Add printer", "Result >>>>" + data1);
                    if (data1 != null)
                    {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure()
            {
            }
        });
        asyncTask.execute("", "", "");
    }*/
    private void checkUSBPrinter() {
        if(mPrinterType == MAKE_ELOTOUCH){
            return;
        }
        if(mPrinterType == MAKE_SNBC) {
            POSUSBAPI interface_usb = new POSUSBAPI(getActivity());
            int error_code = interface_usb.OpenDevice();
            if (error_code != 1000) {
                Utils.alertBox(getActivity(), R.string.txt_usb_snbc_printer, R.string.msg_no_printer_found);
            } else {
                Utils.alertBox(getActivity(), R.string.txt_usb_snbc_printer, R.string.msg_found_printer);
                interface_usb.CloseDevice();
            }
        } else {
            UsbManager mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

            boolean foundDevice = false;
            UsbDevice printer = null;

            String devices = "";

            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if (device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    printer = device;
                    mUsbManager.requestPermission(device, mPermissionIntent);
                    break;
                }

                if (device.getInterfaceCount() > 0) {
                    for (int p = 0; p < device.getInterfaceCount(); p++) {
                        if (device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                            printer = device;
                            mUsbManager.requestPermission(device, mPermissionIntent);
                            break;
                        }
                    }
                }
            }

            if (printer == null) {
                Utils.alertBox(getActivity(), R.string.txt_usb_printer, R.string.msg_no_printer_found);
            } else {
                Utils.alertBox(getActivity(), R.string.txt_usb_printer, getString(R.string.msg_found_printer) + printer.getDeviceName(printer.getDeviceId()));
            }
        }
    }

    private class SearchPrinterAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            List<PortInfo> BTPortList;
            List<PortInfo> TCPPortList;

            arrayDiscovery = new ArrayList<PortInfo>();
            arrayPortName = new ArrayList<String>();

            String interfaceName = params[0];
            ArrayList<String> list = new ArrayList<String>();

            if (mPrinterType == MAKE_ESCPOS) {
                ArrayList<String> mArrayAdapter = new ArrayList<String>();

                if (mBluetoothChecked) {
                    try {
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            // Device does not support Bluetooth
                        }

                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        // If there are paired devices
                        if (pairedDevices.size() > 0) {
                            mDeviceAdapter = new ArrayList<>();
                            // Loop through paired devices
                            for (BluetoothDevice device : pairedDevices) {
                                // Add the name and address to an array adapter to show in a ListView
                                Log.v("DEVICE", device.getName() + "\n" + device.getAddress());
                                mDeviceAdapter.add(device);
                                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (mUSBChecked) {
                    UsbManager mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

                    boolean foundDevice = false;

                    String devices = "";
                    UsbDevice printer = null;

                    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while (deviceIterator.hasNext()){
                        UsbDevice device = deviceIterator.next();

                        if (device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER) {
                            printer = device;
                        }

                        if (device.getInterfaceCount() > 0) {
                            for (int p = 0; p < device.getInterfaceCount(); p++) {
                                if (device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                                    printer = device;
                                }
                            }
                        }
                    }

                }
                return mArrayAdapter;
            }

            if (mPrinterType == MAKE_STAR) {
                try {
                    if (interfaceName.equals("Bluetooth") || interfaceName.equals("All")) {
                        BTPortList = StarIOPort.searchPrinter("BT:");

                        for (PortInfo portInfo : BTPortList) {
                            arrayDiscovery.add(portInfo);
                        }
                    }
                    if (interfaceName.equals("LAN") || interfaceName.equals("All")) {
                        TCPPortList = StarIOPort.searchPrinter("TCP:");

                        for (PortInfo portInfo : TCPPortList) {
                            arrayDiscovery.add(portInfo);
                        }
                    }

                    arrayPortName = new ArrayList<String>();

                    for (PortInfo discovery : arrayDiscovery) {
                        String portName;

                        portName = discovery.getPortName();

                        if (!discovery.getMacAddress().equals("")) {
                            portName += "\n - " + discovery.getMacAddress();
                            if (!discovery.getModelName().equals("")) {
                                portName += "\n - " + discovery.getModelName();
                            }
                        }

                        arrayPortName.add(portName);
                    }
                } catch (StarIOPortException e) {
                    e.printStackTrace();
                }

                return arrayPortName;
            }

            if (mPrinterType == MAKE_SNBC) {
                try {
                    if (interfaceName.equals("Bluetooth") || interfaceName.equals("All")) {
                        BTPortList = StarIOPort.searchPrinter("BT:");

                        for (PortInfo portInfo : BTPortList) {
                            arrayDiscovery.add(portInfo);
                        }
                    }
                    if (interfaceName.equals("LAN") || interfaceName.equals("All")) {
                        TCPPortList = StarIOPort.searchPrinter("TCP:");

                        for (PortInfo portInfo : TCPPortList) {
                            arrayDiscovery.add(portInfo);
                        }
                    }

                    arrayPortName = new ArrayList<>();

                    for (PortInfo discovery : arrayDiscovery) {
                        String portName;

                        portName = discovery.getPortName();

                        if (!discovery.getMacAddress().equals("")) {
                            portName += "\n - " + discovery.getMacAddress();
                            if (!discovery.getModelName().equals("")) {
                                portName += "\n - " + discovery.getModelName();
                            }
                        }

                        arrayPortName.add(portName);
                    }
                } catch (StarIOPortException e) {
                    e.printStackTrace();
                }

                return arrayPortName;
            } else if(mPrinterType == MAKE_CUSTOM) {
                String[] ethDeviceList = null;

                try {
                    //Get the list of devices (search for 1.5 seconds)
                    if(mTCPChecked)
                        ethDeviceList = CustomAndroidAPI.EnumEthernetDevices(5000, getActivity());

                    if(mBluetoothChecked)
                        btDeviceList = CustomAndroidAPI.EnumBluetoothDevices();

                    if ((ethDeviceList == null) || (ethDeviceList.length == 0)) {
                        return list;
                    }
                }
                catch(CustomException e )
                {
                    e.printStackTrace();
                    return list;
                }
                catch(Exception e )
                {
                    e.printStackTrace();
                    return list;
                }

                if (mTCPChecked) {
                    for (int i = 0; i < ethDeviceList.length; i++) {
                        list.add(ethDeviceList[i]);
                    }
                }

                if(mBluetoothChecked) {
                    for (int i = 0; i < btDeviceList.length; i++) {
                        list.add(btDeviceList[i].getAddress());
                    }
                }
                return list;
            }

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> arrayPortName) {
            final EditText editPortName;

            editPortName = new EditText(getActivity());

            new android.app.AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.checkbox_on_background)
                    .setTitle(R.string.msg_select_ip_address_or_input_port_name)
                    .setView(editPortName)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int button) {
                                    mIPAddressEditText.setText(editPortName.getText());
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int button) {
                                }
                            })
                    .setItems(arrayPortName.toArray(new String[0]),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int select) {
                                    mIPAddressEditText.setText(mDeviceAdapter.get(select).getAddress());
                                }
                            }).show();
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                }
            }
        }
    };
}
