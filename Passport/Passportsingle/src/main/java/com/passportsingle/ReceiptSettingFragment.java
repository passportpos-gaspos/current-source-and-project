package com.passportsingle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import POSAPI.POSUSBAPI;
import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;

//import com.StarMicronics.StarIOSDK.R;
//import android.app.AlertDialog.Builder;

public class ReceiptSettingFragment extends Fragment implements
		Runnable {

	protected ProgressDialog pd;
	private static String lock="lockAccess";
	private static CustomPrinter prnDevice;

	private RadioGroup printerGroup;
	private RadioGroup typeGroup;
	private RadioButton TCP;
	private RadioButton BT;
	private RadioButton USB;
	//private RadioButton OFF;
	//private RadioButton STAR;
	//private RadioButton CUSTOM;
	private EditText receiptAddress;
	private TextView printerNameView;
	private EditText footerblurb;
	private Button mSave;
	private Button mTest;
	
	private Button mSearch;
	private CheckBox DK;
	private CheckBox CD;
	private int printerSelected = 0;
	
	private String strInterface;

	private String portName;
	private static final String ACTION_USB_PERMISSION =
		    "com.android.example.USB_PERMISSION";

	private String portSettings;
	//private RadioButton EPSON;
	private Socket clientSocket;
	private OutputStream out;
	private BluetoothSocket mmSocket;
	private RadioButton SIZE2;
	private RadioButton SIZE3;
	private Spinner printerType;
	private Spinner printerNames;
	private Spinner printerModels;
	private List<String> printerNamesList = new ArrayList<String>();
	private List<String> printerModelsList = new ArrayList<String>();
	private ArrayAdapter<String> printerNamesAdapter;
	private ArrayAdapter<String> printerModelsAdapter;
	private static ReceiptSettingFragment me;
	private static PendingIntent mPermissionIntent;

	static final int IMAGE_WIDTH_MAX = 512;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Receipt Settings Fragment");
		
		me = this;
		
		mPermissionIntent = PendingIntent.getBroadcast(me.getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		me.getActivity().registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    me.getActivity().unregisterReceiver(mUsbReceiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.receiptsettings, container, false);

		printerGroup = (RadioGroup) view.findViewById(R.id.PrinterGroup);
		typeGroup = (RadioGroup) view.findViewById(R.id.TypeGroup);
		
		printerType = (Spinner) view.findViewById(R.id.printerType);

		TCP = (RadioButton) view.findViewById(R.id.radioButtonTCP);
		BT = (RadioButton) view.findViewById(R.id.radioButtonBT);
		USB = (RadioButton) view.findViewById(R.id.radioButtonUSB);

		//OFF = (RadioButton) view.findViewById(R.id.RadioOff);
		
		//EPSON = (RadioButton) view.findViewById(R.id.RadioEpson);
		//STAR = (RadioButton) view.findViewById(R.id.RadioStar);
		//CUSTOM = (RadioButton) view.findViewById(R.id.RadioCustom);
		
		SIZE2 = (RadioButton) view.findViewById(R.id.size2);
		SIZE3 = (RadioButton) view.findViewById(R.id.size3);
		
		DK = (CheckBox) view.findViewById(R.id.dkCheckBox);
		CD = (CheckBox) view.findViewById(R.id.cdCheckBox);
		
		receiptAddress = (EditText) view.findViewById(R.id.receiptAddress);
		printerNameView = (TextView) view.findViewById(R.id.printerNameView);
		
		printerNames = (Spinner) view.findViewById(R.id.PrinterName);
		printerModels = (Spinner) view.findViewById(R.id.PrinterModel);
		
		printerNames.setEnabled(false);
		printerModels.setEnabled(false);
		footerblurb = (EditText) view.findViewById(R.id.footerblurb);

		mSave = (Button) view.findViewById(R.id.receipt_save);
		mTest = (Button) view.findViewById(R.id.receipt_test);
		mSearch = (Button) view.findViewById(R.id.searchPrinter);
		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, list);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.printerTypeArray, R.layout.spiner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printerType.setAdapter(adapter);
		printerType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {					
				String type = parent.getItemAtPosition(position).toString();
				printerNames.setEnabled(false);
				printerModels.setEnabled(false);
				if(type.equals("Off")){
					disableFields();
				}else if(type.equals("Star TSP100LAN")){
					disableFields();
					enableStar();
				}else if(type.equals("Custom America")){
					disableFields();
					enableCustom();
				}else if(type.equals("SNBC BTP-R880NP")){
					disableFields();
					enableSNBC();
				}else if(type.equals("Partner Tech PT-6210")){
					disableFields();
					enablePT6210();
				}else if(type.equals("Generic ESC/POS")){
					disableFields();
					enableESCPOS();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		printerNames.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				printerModelsList.clear();
				printerModelsList.add(0,"Select Model");
				if(position > 0){
					Cursor c =ProductDatabase.getPrinterModels(parent.getItemAtPosition(position).toString());						
					while(c.moveToNext()){
						printerModelsList.add(c.getString(c.getColumnIndex("PrinterModel")).trim());
					}
					ReceiptSetting.selectedPrinterIndex = position;
					ArrayAdapter<String> printerModelAdapter = new ArrayAdapter<String>(getActivity() , android.R.layout.simple_spinner_item, printerModelsList);
					printerModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					printerModels.setAdapter(printerModelAdapter);
					
					printerModels.setSelection(ReceiptSetting.selectedModelIndex);
					printerModels.setEnabled(true);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		printerModels.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ReceiptSetting.selectedModelIndex = position;
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});

		/*OFF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(OFF);
			}
		});*/

		/*STAR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(STAR);
			}
		});*/

		/*CUSTOM.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(CUSTOM);
			}
		});*/
		
		/*EPSON.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(EPSON);
			}
		});*/

		BT.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(BT);
			}
		});

		TCP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(TCP);
			}
		});

		USB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(USB);
			}
		});
		
		SIZE2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(SIZE2);
			}
		});
		
		SIZE3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRadioButtonClicked(SIZE3);
			}
		});

		mSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PortDiscovery();
			}
		});

		mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (printerSelected == ReceiptSetting.MAKE_STAR) {
					if (receiptAddress.getText().toString().equals("")) {
						alertbox("Receipt Settings", "Need Printer IP Address");
						return;
					}
					
					ReceiptSetting.enabled = true;
					ReceiptSetting.blurb = footerblurb.getText().toString();
					ReceiptSetting.address = receiptAddress.getText().toString();
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.make = ReceiptSetting.MAKE_STAR;

					if(TCP.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_LAN;
					if(USB.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_USB;
					
					if(SIZE2.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_2;
					if(SIZE3.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_3;	
					
					if(BT.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_BT;
					if(DK.isChecked())
						ReceiptSetting.drawer = true;			
					
					PointOfSale.getShop().insertReceiptSettings();
					mTest.setEnabled(true);
				} else if (printerSelected == ReceiptSetting.MAKE_CUSTOM) {
					if (receiptAddress.getText().toString().equals("")) {
						alertbox("Receipt Settings", "Need Printer IP Address");
						return;
					}
					
					ReceiptSetting.enabled = true;
					ReceiptSetting.blurb = footerblurb.getText().toString();
					ReceiptSetting.address = receiptAddress.getText().toString();
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.make = ReceiptSetting.MAKE_CUSTOM;

					if(TCP.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_LAN;
					if(USB.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_USB;
					if(BT.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_BT;
					
					if(SIZE2.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_2;
					if(SIZE3.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_3;	

					if(DK.isChecked())
						ReceiptSetting.drawer = true;
					if(CD.isChecked())
						ReceiptSetting.display = true;
					
					PointOfSale.getShop().insertReceiptSettings();
					mTest.setEnabled(true);
				}else if (printerSelected == ReceiptSetting.MAKE_ESCPOS) {
					if (receiptAddress.getText().toString().equals("")) {
						alertbox("Receipt Settings", "Need Printer IP Address");
						return;
					}
					
					ReceiptSetting.enabled = true;
					ReceiptSetting.blurb = footerblurb.getText().toString();
					ReceiptSetting.address = receiptAddress.getText().toString();
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.make = ReceiptSetting.MAKE_ESCPOS;
					
					if(printerModels.getSelectedItemPosition() > 0 && printerNames.getSelectedItemPosition() > 0){
						ReceiptSetting.printerModel = printerModels.getSelectedItem().toString();
						ReceiptSetting.printerName = printerNames.getSelectedItem().toString();
						ReceiptSetting.selectedPrinterIndex  = printerNames.getSelectedItemPosition();
						ReceiptSetting.selectedModelIndex = printerModels.getSelectedItemPosition();
						Cursor c = ProductDatabase.getPrinterCodes(printerNames.getSelectedItem().toString(), printerModels.getSelectedItem().toString());
						c.moveToNext();
						ReceiptSetting.drawerCode = (c.getString(c.getColumnIndex("KickCode"))).trim();
						ReceiptSetting.cutCode = c.getString(c.getColumnIndex("CutterCode"));
					}

					if(TCP.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_LAN;
					if(USB.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_USB;
					
					if(SIZE2.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_2;
					if(SIZE3.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_3;	

					if(BT.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_BT;
					if(DK.isChecked())
						ReceiptSetting.drawer = true;
					if(CD.isChecked())
						ReceiptSetting.display = true;
					
					PointOfSale.getShop().insertReceiptSettings();
					mTest.setEnabled(true);
				}else if (printerSelected == ReceiptSetting.MAKE_PT6210) {					
					ReceiptSetting.enabled = true;
					ReceiptSetting.blurb = footerblurb.getText().toString();
					ReceiptSetting.address = receiptAddress.getText().toString();
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.make = ReceiptSetting.MAKE_PT6210;

					if(TCP.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_LAN;
					if(USB.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_USB;
					
					if(SIZE2.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_2;
					if(SIZE3.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_3;	

					if(BT.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_BT;
					if(DK.isChecked())
						ReceiptSetting.drawer = true;
					if(CD.isChecked())
						ReceiptSetting.display = true;
					
					PointOfSale.getShop().insertReceiptSettings();
					mTest.setEnabled(true);
				}else if (printerSelected == ReceiptSetting.MAKE_SNBC) {
					
					ReceiptSetting.enabled = true;
					ReceiptSetting.blurb = footerblurb.getText().toString();
					ReceiptSetting.address = receiptAddress.getText().toString();
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.make = ReceiptSetting.MAKE_SNBC;

					if(TCP.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_LAN;
					if(USB.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_USB;
					
					if(SIZE2.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_2;
					if(SIZE3.isChecked())
						ReceiptSetting.size = ReceiptSetting.SIZE_3;	

					if(BT.isChecked())
						ReceiptSetting.type = ReceiptSetting.TYPE_BT;
					if(DK.isChecked())
						ReceiptSetting.drawer = true;
					if(CD.isChecked())
						ReceiptSetting.display = true;
					
					PointOfSale.getShop().insertReceiptSettings();
					mTest.setEnabled(true);
				}else{
					ReceiptSetting.enabled = false;
					ReceiptSetting.make = 0; 
					ReceiptSetting.blurb = "";
					ReceiptSetting.address = "";
					ReceiptSetting.display = false;
					ReceiptSetting.drawer = false;
					ReceiptSetting.type = 0;
					PointOfSale.getShop().insertReceiptSettings();
				}

				alertbox("Receipt Settings", "Settings Saved");

			}
		});

		mTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				pd = ProgressDialog.show(getActivity(), "",
						"Sending Test Receipt...", true, false);
				Thread thread = new Thread(ReceiptSettingFragment.this);
				thread.start();
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			USB.setEnabled(false);
		}

		if (ReceiptSetting.enabled) {
			disableFields();
			if(ReceiptSetting.make == ReceiptSetting.MAKE_STAR)
			{
				enableStar();
			}
			if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
			{
				enableCustom();
			}
			if(ReceiptSetting.make == ReceiptSetting.MAKE_ESCPOS)
			{
				enableESCPOS();
			}
			if(ReceiptSetting.make == ReceiptSetting.MAKE_SNBC)
			{
				enableSNBC();
			}
			if(ReceiptSetting.make == ReceiptSetting.MAKE_PT6210)
			{
				enablePT6210();
			}
			if( ReceiptSetting.selectedModelIndex > 0 || ReceiptSetting.selectedPrinterIndex > 0){
				printerNames.setEnabled(true);
				printerModels.setEnabled(true);
				addPrintersList();
				printerNames.setSelection(ReceiptSetting.selectedPrinterIndex);
				//addPrinterModelsList(ReceiptSetting.printerName);
				printerModels.setSelection(ReceiptSetting.selectedModelIndex);
				
			}
		} else {
			disableFields();
		}

		return view;
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.RadioOff:
			if (checked)
			{
				disableFields();
			}
			break;
		case R.id.RadioStar:
			if (checked)
			{
				disableFields();
				enableStar();
			}
			break;
		case R.id.RadioCustom:
			if (checked)
			{
				disableFields();
				enableCustom();
			}
			break;
		case R.id.RadioEpson:
			if (checked)
			{
				disableFields();
				enableESCPOS();
			}
			break;
		case R.id.radioButtonTCP:
			if (checked)
			{
				if(printerSelected == ReceiptSetting.MAKE_STAR)
					receiptAddress.setText("TCP:");
				if(printerSelected == ReceiptSetting.MAKE_CUSTOM)
					receiptAddress.setText("");
				receiptAddress.setEnabled(true);
				mSearch.setEnabled(true);
				printerModels.setEnabled(false);
				printerNames.setEnabled(false);
				printerModels.setSelection(0);
				printerNames.setSelection(0);
			}
			break;
		case R.id.radioButtonBT:
			if (checked)
			{
				receiptAddress.setText("BT:");
				receiptAddress.setEnabled(true);
				mSearch.setEnabled(true);
				printerModels.setEnabled(false);
				printerNames.setEnabled(false);
				printerModels.setSelection(0);
				printerNames.setSelection(0);
			}
			break;
		case R.id.radioButtonUSB:
			if (checked)
			{
				receiptAddress.setText("USB:");
				receiptAddress.setEnabled(false);
				mSearch.setEnabled(false);
				
				if(printerSelected == ReceiptSetting.MAKE_SNBC)
				{
					POSUSBAPI interface_usb = new POSUSBAPI(PointOfSale.me);	    		
					int error_code = interface_usb.OpenDevice();
					if(error_code != 1000)
					{
						alertbox("USB SNBC Printer", "No printer found!");
					}
					else
					{
						alertbox("USB SNBC Printer", "Found Printer!");
						interface_usb.CloseDevice();
					}
				}else{
		    		UsbManager mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
	
		    	    boolean foundDevice = false;
	    	    	UsbDevice printer = null;
	
		    	    String devices = "";
		    	    
		    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    	    while(deviceIterator.hasNext()){
		    	    	UsbDevice device = deviceIterator.next();	    		        
	
						if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    	{
		    	    		printer = device;
			    	    	mUsbManager.requestPermission(device, mPermissionIntent);
		    	    		break;
		    	    	}
		    		        
		    	    	if(device.getInterfaceCount() > 0)
		    	    	{
		    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
		    	    		{
		    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    			{
		    	    				printer = device;
		    		    	    	mUsbManager.requestPermission(device, mPermissionIntent);
		    	    				break;
		    	    			}
		    	    		}
		    	    	}   	
		    	    }
		    	    
					if(printer == null)
					{
						alertbox("USB Printer", "No printer found");
					}else{
						if(printerType.getSelectedItemPosition() == 5){
				    	    printerModels.setEnabled(true);
							printerNames.setEnabled(true);
				    	    addPrintersList();
			    	    }
						alertbox("USB Printer", "Found Printer! " + printer.getDeviceName(printer.getDeviceId()));
						
					}
				}
			}
			break;
		}
	}

	private void enableESCPOS() {
		printerSelected = ReceiptSetting.MAKE_ESCPOS;
		printerNames.setEnabled(true);
		printerModels.setEnabled(true);
		printerType.setSelection(5);
		
		footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		CD.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE3.setChecked(true);

		if(ReceiptSetting.display)
			CD.setChecked(true);

		if(ReceiptSetting.drawer)
			DK.setChecked(true);

		TCP.setEnabled(true);
		TCP.setChecked(true);
		USB.setEnabled(true);
		BT.setEnabled(true);
						
		switch (ReceiptSetting.type) {
			case ReceiptSetting.TYPE_LAN:
				TCP.setChecked(true);
			break;
			case ReceiptSetting.TYPE_USB:
				USB.setChecked(true);
			break;
			case ReceiptSetting.TYPE_BT:
				BT.setChecked(true);
			break;
		}
		
		switch (ReceiptSetting.size) {
			case ReceiptSetting.SIZE_2:
				SIZE2.setChecked(true);
			break;
			case ReceiptSetting.SIZE_3:
				SIZE3.setChecked(true);
			break;
		}

		if(TCP.isChecked())
		{
			mSearch.setEnabled(false);
		}
		
		if(USB.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(BT.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(ReceiptSetting.enabled)
			mTest.setEnabled(true);
	}

	private void enableCustom() {
		printerSelected = ReceiptSetting.MAKE_CUSTOM;
		
		printerType.setSelection(2);
		
		footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		CD.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE2.setChecked(true);
		
		if(ReceiptSetting.display)
			CD.setChecked(true);

		if(ReceiptSetting.drawer)
			DK.setChecked(true);

		TCP.setEnabled(true);
		TCP.setChecked(true);
		USB.setEnabled(true);
		BT.setEnabled(false);
						
		switch (ReceiptSetting.type) {
			case ReceiptSetting.TYPE_LAN:
				TCP.setChecked(true);
			break;
			case ReceiptSetting.TYPE_USB:
				USB.setChecked(true);
			break;
			case ReceiptSetting.TYPE_BT:
				BT.setChecked(true);
			break;
		}
		
		switch (ReceiptSetting.size) {
			case ReceiptSetting.SIZE_2:
				SIZE2.setChecked(true);
			break;
			case ReceiptSetting.SIZE_3:
				SIZE3.setChecked(true);
			break;
		}
		
		if(TCP.isChecked())
		{
			mSearch.setEnabled(true);
		}
		
		if(USB.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if(BT.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(ReceiptSetting.enabled)
			mTest.setEnabled(true);
	}
	
	private void enableSNBC() {
		printerSelected = ReceiptSetting.MAKE_SNBC;
		
		printerType.setSelection(3);
		
		footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		CD.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE2.setChecked(true);
		
		if(ReceiptSetting.display)
			CD.setChecked(true);

		if(ReceiptSetting.drawer)
			DK.setChecked(true);

		TCP.setEnabled(false);
		USB.setEnabled(true);
		USB.setChecked(true);
		BT.setEnabled(false);
						
		switch (ReceiptSetting.type) {
			case ReceiptSetting.TYPE_LAN:
				TCP.setChecked(true);
			break;
			case ReceiptSetting.TYPE_USB:
				USB.setChecked(true);
			break;
			case ReceiptSetting.TYPE_BT:
				BT.setChecked(true);
			break;
		}
		
		switch (ReceiptSetting.size) {
			case ReceiptSetting.SIZE_2:
				SIZE2.setChecked(true);
			break;
			case ReceiptSetting.SIZE_3:
				SIZE3.setChecked(true);
			break;
		}
		
		if(TCP.isChecked())
		{
			mSearch.setEnabled(true);
		}
		
		if(USB.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if(BT.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(ReceiptSetting.enabled)
			mTest.setEnabled(true);
	}
	
	private void enablePT6210() {
		printerSelected = ReceiptSetting.MAKE_PT6210;
		
		printerType.setSelection(4);
		
		footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		CD.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(false);
		SIZE2.setChecked(true);
		
		if(ReceiptSetting.display)
			CD.setChecked(true);

		if(ReceiptSetting.drawer)
			DK.setChecked(true);

		TCP.setEnabled(false);
		USB.setEnabled(true);
		USB.setChecked(true);
		BT.setEnabled(false);
						
		switch (ReceiptSetting.type) {
			case ReceiptSetting.TYPE_LAN:
				TCP.setChecked(true);
			break;
			case ReceiptSetting.TYPE_USB:
				USB.setChecked(true);
			break;
			case ReceiptSetting.TYPE_BT:
				BT.setChecked(true);
			break;
		}
		
		//switch (ReceiptSetting.size) {
		//	case ReceiptSetting.SIZE_2:
		//		SIZE2.setChecked(true);
		//	break;
		//	case ReceiptSetting.SIZE_3:
		//		SIZE3.setChecked(true);
		//	break;
		//}
		
		if(TCP.isChecked())
		{
			mSearch.setEnabled(true);
		}
		
		if(USB.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if(BT.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(ReceiptSetting.enabled)
			mTest.setEnabled(true);
	}

	private void enableStar() {	
		printerSelected = ReceiptSetting.MAKE_STAR;
		
		printerType.setSelection(1);
		
		footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE3.setChecked(true);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			TCP.setEnabled(true);
			USB.setEnabled(false);
			BT.setEnabled(false);
		} else {
			TCP.setEnabled(true);
			USB.setEnabled(false);
			BT.setEnabled(false);
		}
				
		switch (ReceiptSetting.type) {
			case ReceiptSetting.TYPE_LAN:
				TCP.setChecked(true);
			break;
			case ReceiptSetting.TYPE_USB:
				USB.setChecked(true);
			break;
			case ReceiptSetting.TYPE_BT:
				BT.setChecked(true);
			break;
		}
		
		switch (ReceiptSetting.size) {
			case ReceiptSetting.SIZE_2:
				SIZE2.setChecked(true);
			break;
			case ReceiptSetting.SIZE_3:
				SIZE3.setChecked(true);
			break;
		}
		
		if(TCP.isChecked())
		{
			mSearch.setEnabled(true);
		}
		
		if(USB.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if(BT.isChecked())
		{
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if(ReceiptSetting.enabled)
			mTest.setEnabled(true);
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setTitle(title)
				.setInverseBackgroundForced(true)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	protected void disableFields() {	
		printerSelected = 0;

		printerType.setSelection(0);
		
		TCP.setEnabled(false);
		BT.setEnabled(false);
		USB.setEnabled(false);
	
		DK.setEnabled(false);
		CD.setEnabled(false);
		
		SIZE2.setEnabled(false);
		SIZE3.setEnabled(false);

		receiptAddress.setEnabled(false);
		printerNameView.setEnabled(false);
		footerblurb.setEnabled(false);

		mTest.setEnabled(false);
		mSearch.setEnabled(false);
	}

	public void PortDiscovery() {
		if(TCP.isChecked())
		{
			getPortDiscovery("LAN");
		}
		
		if(BT.isChecked())
		{
			getPortDiscovery("Bluetooth");
		}
		
		if(USB.isChecked())
		{
			getPortDiscovery("USB");
		}
	}

	@Override
	public void run() {
		Log.v("Thread", "Starting Thread");
		if (issueReceipt()) {
			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
		} else {
			Message m2 = new Message();
			m2.what = 8;
			handler.sendMessage(m2);
		}
	}

	@SuppressLint("NewApi")
	private boolean issueReceipt() {

		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
		
		StringBuilder testString = new StringBuilder();
		
		String textToPrint = "Advantage POS connected to Printer Successfully.";
		
		testString.append('\n').append('\n').append(EscPosDriver.wordWrap(textToPrint, cols+1)).append('\n').append('\n');

		return EscPosDriver.Print(testString.toString());
		
		/*Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTypeface(typeface);
		paint.setTextSize(32);
		TextPaint textpaint = new TextPaint(paint);

		android.text.StaticLayout staticLayout = new StaticLayout(textToPrint,
				textpaint, 576, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
		int height = staticLayout.getHeight();

		Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bitmap);
		c.drawColor(Color.WHITE);
		c.translate(0, 0);
		staticLayout.draw(c);

		portName = ReceiptSetting.address;
		portSettings = "";
		
		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
		
		if(ReceiptSetting.make == ReceiptSetting.MAKE_STAR)
		{
			if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
				portSettings = "MINI";
	
			if (portSettings.toUpperCase().equals("MINI")) {
				MiniPrinterFunctions.PrintBitmap(getActivity(), portName,
						portSettings, bitmap, 576, false, false);
				return true;
			} else {
				return PrinterFunctions.PrintBitmap(getActivity(), portName,
						portSettings, bitmap, 576, false, true);
			}
		}
		
		else if(ReceiptSetting.make == ReceiptSetting.MAKE_EPSON )//|| ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
		{
			try {
	    	    UsbDevice printer = null;
	    	    UsbManager mUsbManager = null;
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
		    	{
		    		mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

		    	    boolean foundDevice = false;

		    	    String devices = "";
		    	    
		    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    	    while(deviceIterator.hasNext()){
		    	    	UsbDevice device = deviceIterator.next();	    		        

		    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    	{
		    	    		printer = device;
		    	    		break;
		    	    	}
		    		        
		    	    	if(device.getInterfaceCount() > 0)
		    	    	{
		    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
		    	    		{
		    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    			{
		    	    				printer = device;
		    	    				break;
		    	    			}
		    	    		}
		    	    	}   		        
		    	    }
		    	    		    		
			    	//clientSocket = new Socket(ReceiptSetting.address, 9100);
			    	//out = clientSocket.getOutputStream();
			    	//return false;
		    	}
				
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
		    	{
			    	clientSocket = new Socket(ReceiptSetting.address, 9100);
			    	out = clientSocket.getOutputStream();
		    	}
		    	
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
		    	{
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (mBluetoothAdapter == null) {
					    return false;
					}
						
					BluetoothDevice BTdevice = null;
					
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
					// If there are paired devices
					if (pairedDevices.size() > 0) {
						for (BluetoothDevice device : pairedDevices) {
							if(device.getAddress().equals(ReceiptSetting.address))
							{
								BTdevice = device;
							}
						}
					}
									
					mBluetoothAdapter.cancelDiscovery();
					
					mmSocket = BTdevice.createRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
			        		        
			        try {
			            mmSocket.connect();
				    	out = mmSocket.getOutputStream();
			        } catch (IOException connectException) {
			            // Unable to connect; close the socket and get out
			        	connectException.printStackTrace();
			            try {
			                mmSocket.close();
			            } catch (IOException closeException) { }
			            return false;
			        }
		    	}

		        				
			    try {
			    	if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
			    	{
			    	    if(printer != null)
			    	    {
			    	    	UsbInterface intf = printer.getInterface(0);
			    	    	
			    	    	UsbEndpoint endpoint = null;
			    	    	for(int o = 0; o < intf.getEndpointCount(); o++)
			    	    	{
			    	    		if(intf.getEndpoint(o).getDirection() == UsbConstants.USB_DIR_OUT)
			    	    		{
			    	    			endpoint = intf.getEndpoint(o);
			    	    		}
			    	    	}
			    	    	
			    	    	if(endpoint != null)
			    	    	{
			    	    		UsbDeviceConnection connection = mUsbManager.openDevice(printer); 
			    	    		connection.claimInterface(intf, true);
			    	    		connection.bulkTransfer(endpoint, PrinterCommands.INIT, PrinterCommands.INIT.length, 500); //do in another thread
			    	    		connection.bulkTransfer(endpoint, EscPosDriver.wordWrap(textToPrint, cols).getBytes(), EscPosDriver.wordWrap(textToPrint, cols).getBytes().length, 500); //do in another thread
			    	    		connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER, PrinterCommands.FEED_PAPER.length, 500); //do in another thread
			    	    		connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER_AND_CUT, PrinterCommands.FEED_PAPER_AND_CUT.length, 500); //do in another thread
			    	    	}
			    	    }
			    	}else{
						out.write(PrinterCommands.INIT);
						
						//out.write(PrinterCommands.SELECT_FONT_A);
	
						out.write(EscPosDriver.wordWrap(textToPrint, cols).getBytes());
					 
						out.write(PrinterCommands.FEED_PAPER);
						out.write(PrinterCommands.FEED_PAPER);
						
						if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
						{
						    out.write(PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT);
						    out.write(PrinterCommands.KICK);
						}else{
						    out.write(PrinterCommands.FEED_PAPER_AND_CUT);
						    out.write(PrinterCommands.KICK);
						}
	
	
				    	out.close();
				    	if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
				    	{
				    		mmSocket.close();
				    	}
			    	}
			    	return true;
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/*try {
				
				if(!InetAddress.getByName(ReceiptSetting.address).isReachable(5000))
				{
					Log.v("error", "not reachable");
					return false;
				}else{
					Log.v("Success", "reachable");
				}
				
			    try {
			    	String hello = "hello";
			    	clientSocket = new Socket(ReceiptSetting.address, 9100);
			    	out = clientSocket.getOutputStream();
					out.write(PrinterCommands.INIT);
					
					out.write(PrinterCommands.SELECT_FONT_A);

					out.write(StringUtils.wordWrap(textToPrint, 40).getBytes());
				 
					out.write(PrinterCommands.FEED_PAPER);
				    out.write(PrinterCommands.FEED_PAPER_AND_CUT);
				    out.write(PrinterCommands.KICK);

			    	out.close();
			    	return true;
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		/*}
		
		if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
		{
		        
			Log.v("Starting Print", "Printing Custom");
			if(OpenDevice())
			{
				synchronized (lock) 
				{
					try
					{
						int[] arrayOfInt = { 16, 20, 1, 3, 5 };
	
						prnDevice.printImage(bitmap,CustomPrinter.IMAGE_ALIGN_TO_CENTER, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
						prnDevice.feed(3);
						prnDevice.cut(CustomPrinter.CUT_TOTAL);	 
						//prnDevice.present(40);
						if(ReceiptSetting.drawer)
							prnDevice.writeData(arrayOfInt);
						return true;
					}
					catch(CustomException e )
					{   
						return false;	      		
					}
					catch(Exception e ) 
					{
						return false;						
					}
				}	
	    	}
		}*/
	}
	
	public static boolean OpenDevice() {	
		Log.v("Printing", "Opening Devices");
		
		if (prnDevice == null) {
			try {
				if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
				{
			        try {
						if(!InetAddress.getByName(ReceiptSetting.address).isReachable(1000))
						{
				    		Log.v("Network Error", "Host not reachable.");
				    		return false;
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			        
					prnDevice = new CustomAndroidAPI().getPrinterDriverETH(ReceiptSetting.address);
				}else{
					UsbDevice printer = null;
		    	    UsbManager mUsbManager = null;
			    	if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
			    	{
			    		mUsbManager = (UsbManager) me.getActivity().getSystemService(Context.USB_SERVICE);

			    	    boolean foundDevice = false;

			    	    String devices = "";
			    	    
			    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			    	    while(deviceIterator.hasNext()){
			    	    	UsbDevice device = deviceIterator.next();	    		        

			    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER)
			    	    	{
			    	    		printer = device;
			    	    		break;
			    	    	}
			    		        
			    	    	if(device.getInterfaceCount() > 0)
			    	    	{
			    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
			    	    		{
			    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
			    	    			{
			    	    				printer = device;
			    	    				break;
			    	    			}
			    	    		}
			    	    	}   		        
			    	    }
					
						if (prnDevice == null && printer != null) 
						{
							try {
								prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(printer, me.getActivity());
								return true;
							} catch (CustomException e) {
								e.printStackTrace();
								return false;
							} catch (Exception e) {
								e.printStackTrace();
								return false;
							}
						}
					}
				}
				return true;
			} catch (CustomException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}  


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			} else if (msg.what == 8) {
				alertbox("Receipt Settings", "Could not send Receipt");
				pd.dismiss();
			}
		}
	};
	public ArrayList<PortInfo> arrayDiscovery;
	public ArrayList<String> arrayPortName;
	public BluetoothDevice[] btDeviceList;
	public ArrayList<BluetoothDevice> mDeviceAdapter;

	private class LongOperation extends AsyncTask<String, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			List<PortInfo> BTPortList;
			List<PortInfo> TCPPortList;

			arrayDiscovery = new ArrayList<PortInfo>();
			arrayPortName = new ArrayList<String>();

			String interfaceName = params[0];
    		ArrayList<String> list = new ArrayList<String>();

    		if(printerSelected == ReceiptSetting.MAKE_ESCPOS)
			{
		        ArrayList<String> mArrayAdapter = new ArrayList<String>();

		        if(BT.isChecked())
		        {
					try {
						BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
						if (mBluetoothAdapter == null) {
						    // Device does not support Bluetooth
						}
								
						Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
						// If there are paired devices
						if (pairedDevices.size() > 0) {
							mDeviceAdapter = new ArrayList<BluetoothDevice>();
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
		        
		        if(USB.isChecked())
		        {
		    		UsbManager mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

		    	    boolean foundDevice = false;

		    	    String devices = "";
		    	    UsbDevice printer = null;
		    	    
		    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    	    while(deviceIterator.hasNext()){
		    	    	UsbDevice device = deviceIterator.next();	    		        

		    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    	{
		    	    		printer = device;
		    	    	}
		    		        
		    	    	if(device.getInterfaceCount() > 0)
		    	    	{
		    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
		    	    		{
		    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    			{
		    	    				printer = device;
		    	    			}
		    	    		}
		    	    	}   
		    	    	
		    	    	// mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("com.android.example.USB_PERMISSION"), 0);
		    	    	//IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
		    	    	//getActivity().registerReceiver(mUsbReceiver, filter);
		    	    	
		    	    	//mUsbManager.requestPermission(device, mPermissionIntent);
		    	    }
		    	    
		        }
				return mArrayAdapter;
			}
    		
			if(printerSelected == ReceiptSetting.MAKE_STAR)
			{
				try {
					if (true == interfaceName.equals("Bluetooth")
							|| true == interfaceName.equals("All")) {
						BTPortList = StarIOPort.searchPrinter("BT:");
	
						for (PortInfo portInfo : BTPortList) {
							arrayDiscovery.add(portInfo);
						}
					}
					if (true == interfaceName.equals("LAN")
							|| true == interfaceName.equals("All")) {
						TCPPortList = StarIOPort.searchPrinter("TCP:");
	
						for (PortInfo portInfo : TCPPortList) {
							arrayDiscovery.add(portInfo);
						}
					}
	
					arrayPortName = new ArrayList<String>();
	
					for (PortInfo discovery : arrayDiscovery) {
						String portName;
	
						portName = discovery.getPortName();
	
						if (discovery.getMacAddress().equals("") == false) {
							portName += "\n - " + discovery.getMacAddress();
							if (discovery.getModelName().equals("") == false) {
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
			
			if(printerSelected == ReceiptSetting.MAKE_SNBC)
			{
				try {
					if (true == interfaceName.equals("Bluetooth")
							|| true == interfaceName.equals("All")) {
						BTPortList = StarIOPort.searchPrinter("BT:");
	
						for (PortInfo portInfo : BTPortList) {
							arrayDiscovery.add(portInfo);
						}
					}
					if (true == interfaceName.equals("LAN")
							|| true == interfaceName.equals("All")) {
						TCPPortList = StarIOPort.searchPrinter("TCP:");
	
						for (PortInfo portInfo : TCPPortList) {
							arrayDiscovery.add(portInfo);
						}
					}
	
					arrayPortName = new ArrayList<String>();
	
					for (PortInfo discovery : arrayDiscovery) {
						String portName;
	
						portName = discovery.getPortName();
	
						if (discovery.getMacAddress().equals("") == false) {
							portName += "\n - " + discovery.getMacAddress();
							if (discovery.getModelName().equals("") == false) {
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

			else if(printerSelected == ReceiptSetting.MAKE_CUSTOM)
			{
				String[] ethDeviceList = null;
				
	    		try
	            {
	            	//Get the list of devices (search for 1.5 seconds)
	    			if(TCP.isChecked())
	    				ethDeviceList = CustomAndroidAPI.EnumEthernetDevices(5000, getActivity());
	    			
	    			if(BT.isChecked())
	    				btDeviceList = CustomAndroidAPI.EnumBluetoothDevices();
	    			
	            	if ((ethDeviceList == null) || (ethDeviceList.length == 0))
	            	{
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
	    		
	    		if(TCP.isChecked())
	    		{
		    		for(int i = 0;i<ethDeviceList.length;i++)
		    		{
		    			list.add(ethDeviceList[i]);
		    		}
	    		}
	    		
    			if(BT.isChecked())
    			{
		    		for(int i = 0;i<btDeviceList.length;i++)
		    		{
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

			new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.checkbox_on_background)
					.setTitle("Please Select IP Address or Input Port Name")
					.setView(editPortName)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int button) {
									receiptAddress.setText(editPortName.getText());
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int button) {
								}
							})
					.setItems(arrayPortName.toArray(new String[0]),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int select) {
									receiptAddress.setText(mDeviceAdapter.get(select).getAddress());
								}
							}).show();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	private void getPortDiscovery(String interfaceName) {
		new LongOperation().execute(interfaceName);
	}

	private final static BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if(device != null){
							//call method to set up device communication
						}
					} 
				}
			}
		}
	};
	
	private void addPrintersList() {
		
		printerNamesList.clear();
		//printerNamesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, printerNamesList);
		//printerNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printerNames.setAdapter(null);
		
		Cursor c = ProductDatabase.getPrinterNames();
		
		while(c.moveToNext()){
			
			printerNamesList.add((c.getString(c.getColumnIndex("PrinterType"))).trim());
			
		}

		printerNamesList.add(0, "Select Printer Name");
		printerNamesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, printerNamesList);
		printerNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printerNames.setAdapter(printerNamesAdapter);
		
		printerNames.setSelection(0);
		
	}
	
	public void addPrinterModelsList(String printerName){
		
		printerModelsList.clear();
		printerModelsList.add(0,"Select Model");
		Cursor c =ProductDatabase.getPrinterModels(printerName);						
		while(c.moveToNext()){
			printerModelsList.add(c.getString(c.getColumnIndex("PrinterModel")).trim());
		}
		ArrayAdapter<String> printerModelAdapter = new ArrayAdapter<String>(getActivity() , android.R.layout.simple_spinner_item, printerModelsList);
		printerModelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printerModels.setAdapter(printerModelAdapter);
		printerModels.setEnabled(true);
		
	}
}
