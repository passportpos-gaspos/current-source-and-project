package com.pos.passport.fragment;

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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.pos.passport.R;
import com.pos.passport.adapter.PrinterAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import POSAPI.POSUSBAPI;
import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;

public class ReceiptSettingFragment extends Fragment implements Runnable {

	protected ProgressDialog pd;
	private static String lock="lockAccess";
	private static CustomPrinter prnDevice;

	private RadioGroup printerGroup;
	private RadioGroup typeGroup;
	private RadioButton TCP;
	private RadioButton BT;
	private RadioButton USB;
	private EditText receiptAddress;

	private Button mSearch;
	private CheckBox DK;
	private int printerSelected = 0;
	
	private String strInterface;

	private String portName;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private String portSettings;
	//private RadioButton EPSON;
	private Socket clientSocket;
	private OutputStream out;
	private BluetoothSocket mmSocket;
	private RadioButton SIZE2;
	private RadioButton SIZE3;
	private Spinner printerType;
	private View mylayout;
	private GridView PrinterList;
	private EditText prefixCommand;
	private EditText suffixCommand;
	private int editID;
	private CheckBox mainPrinter;
	private static ReceiptSettingFragment me;
	private static PendingIntent mPermissionIntent;
	private ProductDatabase mDb;
	
	//kareem printing 
	private RadioButton receiptEveryTime;
	private RadioButton receiptFor10;
	private RadioButton receiptFor20;
	private RadioButton receiptFor30;

	static final int IMAGE_WIDTH_MAX = 512;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Receipt Settings Fragment");
		
		me = this;
		
		mPermissionIntent = PendingIntent.getBroadcast(me.getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		me.getActivity().registerReceiver(mUsbReceiver, filter);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    menu.clear();
		inflater.inflate(R.menu.receipt_menu, menu);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    me.getActivity().unregisterReceiver(mUsbReceiver);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish(); 
			return true;
		case R.id.menu_addprinter:
			addPrinter();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle(R.string.txt_printer_options);
        menu.add(0, v.getId(), 0, R.string.txt_test_printer);
        menu.add(0, v.getId(), 0, R.string.txt_edit_printer);
        menu.add(0, v.getId(), 0, R.string.txt_remove_printer);
    }
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle() == getString(R.string.txt_test_printer)) { testPrinter(item); }
        else if(item.getTitle() == getString(R.string.txt_edit_printer)){ editPrinter(item); }
        else if(item.getTitle() == getString(R.string.txt_remove_printer)){ removePrinter(item); }
        //else {return false;}
		return true;
    }

	private void editPrinter(MenuItem item) {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.fragment_receipt_settings,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		printerGroup = (RadioGroup) mylayout.findViewById(R.id.PrinterGroup);
		typeGroup = (RadioGroup) mylayout.findViewById(R.id.TypeGroup);

		printerType = (Spinner) mylayout.findViewById(R.id.printer_type_spinner);

		TCP = (RadioButton) mylayout.findViewById(R.id.radioButtonTCP);
		BT = (RadioButton) mylayout.findViewById(R.id.radioButtonBT);
		USB = (RadioButton) mylayout.findViewById(R.id.radioButtonUSB);

		SIZE2 = (RadioButton) mylayout.findViewById(R.id.size2);
		SIZE3 = (RadioButton) mylayout.findViewById(R.id.size3);

		DK = (CheckBox) mylayout.findViewById(R.id.drawerKick);
		mainPrinter = (CheckBox) mylayout.findViewById(R.id.mainPrinter);

		receiptAddress = (EditText) mylayout.findViewById(R.id.ip_address_edit_text);
		prefixCommand = (EditText) mylayout.findViewById(R.id.precommands);
		suffixCommand = (EditText) mylayout.findViewById(R.id.subcommands);

		mSearch = (Button) mylayout.findViewById(R.id.search_printer_button);

		receiptEveryTime = (RadioButton) mylayout.findViewById(R.id.receiptEveryTime);
		receiptFor10 = (RadioButton) mylayout.findViewById(R.id.receiptIf10);
		receiptFor20 = (RadioButton) mylayout.findViewById(R.id.receiptIf20);
		receiptFor30 = (RadioButton) mylayout.findViewById(R.id.receiptIf30);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.printer_types, R.layout.view_spiner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		printerType.setAdapter(adapter);

		printerType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String type = parent.getItemAtPosition(position).toString();

				if(type.equals("Star TSP100LAN")){
					disableFields();
					enableStar();
				}else if(type.equals("Custom America T-Ten")){
					disableFields();
					enableCustom();
				}else if(type.equals("SNBC")){
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
			}
		});

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

		receiptEveryTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiptEveryTime.setChecked(true);
			}
		});
		receiptFor10.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiptFor10.setChecked(true);
			}
		});
		receiptFor20.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiptFor20.setChecked(true);
			}
		});
		receiptFor30.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				receiptFor30.setChecked(true);
			}
		});
		mSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PortDiscovery();
			}
		});

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) PrinterList.getItemAtPosition(info.position);

		String json = c.getString(c.getColumnIndex("blurb"));
		editID = c.getInt(c.getColumnIndex("_id"));

		try {
			JSONObject object = new JSONObject(json);

			ReceiptSetting.enabled = true;
        	ReceiptSetting.address = object.getString("address");
        	ReceiptSetting.make = object.getInt("printer");
        	ReceiptSetting.size = object.getInt("size");
        	ReceiptSetting.type = object.getInt("type");
        	ReceiptSetting.drawer = object.getBoolean("cashDrawer");
        	ReceiptSetting.receiptPrintOption = object.getInt("receiptprintoption");

        	if(object.has("main"))
        		ReceiptSetting.mainPrinter = object.getBoolean("main");
        	else
        		ReceiptSetting.mainPrinter = true;

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (ReceiptSetting.enabled) {
			disableFields();
			if (ReceiptSetting.make == ReceiptSetting.MAKE_STAR) {
				enableStar();
			} if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
				enableCustom();
			} if(ReceiptSetting.make == ReceiptSetting.MAKE_ESCPOS) {
				enableESCPOS();
			} if(ReceiptSetting.make == ReceiptSetting.MAKE_SNBC) {
				enableSNBC();
			} if(ReceiptSetting.make == ReceiptSetting.MAKE_PT6210) {
				enablePT6210();
			}
		} else {
			disableFields();
		}

		enablePrintReceiptOption(ReceiptSetting.receiptPrintOption);

		builder = new AlertDialog.Builder(getActivity());
		builder.setView(mylayout)
            .setTitle(R.string.txt_edit_printer)
            .setPositiveButton(R.string.txt_save_printer,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            savePrinter(editID);
                            dialog.cancel();
                        }
                    })
            .setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

    	alertDialog = builder.create();
    	alertDialog.show();
	}

	private void enablePrintReceiptOption(int printOption){

		switch (printOption) {
		case 1:
			receiptEveryTime.setChecked(true);
			break;
		case 2:
			receiptFor10.setChecked(true);
			break;
		case 3:
			receiptFor20.setChecked(true);
			break;
		case 4:
			receiptFor30.setChecked(true);
			break;
		default:
			break;
		}
	}
	private void removePrinter(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) PrinterList.getItemAtPosition(info.position);
		mDb.removePrinter(c.getInt(c.getColumnIndex("_id")));
		((PrinterAdapter) PrinterList.getAdapter()).getCursor().requery();
	}

	private void testPrinter(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) PrinterList.getItemAtPosition(info.position);
		
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
		
		pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_sending_test_receipt), true, false);
		Thread thread = new Thread(ReceiptSettingFragment.this);
		thread.start();
	}

	private void addPrinter() {

			AlertDialog.Builder builder;
			final AlertDialog alertDialog;

			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mylayout = inflater.inflate(R.layout.fragment_receipt_settings,(ViewGroup) getActivity().findViewById(R.id.mainLayout));
			
			printerGroup = (RadioGroup) mylayout.findViewById(R.id.PrinterGroup);
			typeGroup = (RadioGroup) mylayout.findViewById(R.id.TypeGroup);
			
			printerType = (Spinner) mylayout.findViewById(R.id.printer_type_spinner);

			TCP = (RadioButton) mylayout.findViewById(R.id.radioButtonTCP);
			BT = (RadioButton) mylayout.findViewById(R.id.radioButtonBT);
			USB = (RadioButton) mylayout.findViewById(R.id.radioButtonUSB);
			
			SIZE2 = (RadioButton) mylayout.findViewById(R.id.size2);
			SIZE3 = (RadioButton) mylayout.findViewById(R.id.size3);
			
			DK = (CheckBox) mylayout.findViewById(R.id.drawerKick);
			mainPrinter = (CheckBox) mylayout.findViewById(R.id.mainPrinter);
			
			receiptAddress = (EditText) mylayout.findViewById(R.id.ip_address_edit_text);
			prefixCommand = (EditText) mylayout.findViewById(R.id.precommands);
			suffixCommand = (EditText) mylayout.findViewById(R.id.subcommands);
			
			mSearch = (Button) mylayout.findViewById(R.id.search_printer_button);
			
			receiptEveryTime = (RadioButton) mylayout.findViewById(R.id.receiptEveryTime);
			receiptFor10 = (RadioButton) mylayout.findViewById(R.id.receiptIf10);
			receiptFor20 = (RadioButton) mylayout.findViewById(R.id.receiptIf20);
			receiptFor30 = (RadioButton) mylayout.findViewById(R.id.receiptIf30);
			
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.printer_types, R.layout.view_spiner);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			printerType.setAdapter(adapter);
			
			printerType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@SuppressLint("NewApi")
				@SuppressWarnings("deprecation")
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {					
					String type = parent.getItemAtPosition(position).toString();
								
					if(type.equals("Star TSP100LAN")){
						disableFields();
						enableStar();
					}else if(type.equals("Custom America T-Ten")){
						disableFields();
						enableCustom();
					}else if(type.equals("SNBC")){
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
				}
			});
			
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
			receiptEveryTime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					receiptEveryTime.setChecked(true);					
				}
			});
			receiptFor10.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					receiptFor10.setChecked(true);						
				}
			});
			receiptFor20.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					receiptFor20.setChecked(true);					
				}
			});
			receiptFor30.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					receiptFor30.setChecked(true);				
				}
			});
			mSearch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PortDiscovery();
				}
			});
			
			builder = new AlertDialog.Builder(getActivity());
			builder.setView(mylayout)
                .setTitle(R.string.txt_add_printer)
                .setPositiveButton(R.string.txt_add_printer,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                savePrinter(0);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

	    	alertDialog = builder.create();
	    	alertDialog.show();
		}
	

	protected void savePrinter(int editID2) {
		int type = 0;
		if(TCP.isChecked()) type = ReceiptSetting.TYPE_LAN;
		if(USB.isChecked()) type = ReceiptSetting.TYPE_USB;
		if(BT.isChecked())  type = ReceiptSetting.TYPE_BT;
		
		int size = 0;
		if(SIZE2.isChecked()) size = ReceiptSetting.SIZE_2;
		if(SIZE3.isChecked()) size = ReceiptSetting.SIZE_3;
		
		int receiptPrint = 0;
		if(receiptEveryTime.isChecked()) receiptPrint = ReceiptSetting.RECEIPT_EVERY_TIME;
		if(receiptFor10.isChecked()) receiptPrint = ReceiptSetting.RECEIPT_WHEN_D10;
		if(receiptFor20.isChecked()) receiptPrint = ReceiptSetting.RECEIPT_WHEN_D20;
		if(receiptFor30.isChecked()) receiptPrint = ReceiptSetting.RECEIPT_WHEN_D30;
		
		JSONObject json = new JSONObject();
		
		try {
		
			json.put("printer", printerSelected);
			json.put("type", type);
			json.put("size", size);			
			json.put("address", receiptAddress.getText().toString().trim());
			json.put("prefix", prefixCommand.getText().toString().trim());
			json.put("suffix", suffixCommand.getText().toString().trim());
			json.put("cashDrawer", DK.isChecked());
			json.put("main", mainPrinter.isChecked());
			json.put("receiptprintoption", receiptPrint);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ReceiptSetting.enabled = false;
		ReceiptSetting.make = 0; 
		ReceiptSetting.blurb = "";
		ReceiptSetting.address = "";
		ReceiptSetting.display = false;
		ReceiptSetting.drawer = false;
		ReceiptSetting.type = 0;
		ReceiptSetting.receiptPrintOption = 0;
				
		mDb.insertReceiptSettings(json.toString(), editID2);

		Utils.alertBox(getActivity(), R.string.txt_receipt_settings, R.string.txt_settings_saved);
		
		((PrinterAdapter) PrinterList.getAdapter()).getCursor().requery();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.view_item_list, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		PrinterList = (GridView) view.findViewById(R.id.item_list_view);
		PrinterAdapter itemAdapter = new PrinterAdapter(mDb.getPrinters(), getActivity().getApplicationContext());
		PrinterList.setAdapter(itemAdapter);
		registerForContextMenu(PrinterList);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.department_spinner);
		departmentFilter.setVisibility(View.GONE);

		return view;
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.RadioOff:
			if (checked) {
				disableFields();
			}
			break;
		case R.id.RadioStar:
			if (checked) {
				disableFields();
				enableStar();
			}
			break;
		case R.id.RadioCustom:
			if (checked) {
				disableFields();
				enableCustom();
			}
			break;
		case R.id.RadioEpson:
			if (checked) {
				disableFields();
				enableESCPOS();
			}
			break;
		case R.id.radioButtonTCP:
			if (checked) {
				if(printerSelected == ReceiptSetting.MAKE_STAR)
					receiptAddress.setText("TCP:");
				if(printerSelected == ReceiptSetting.MAKE_CUSTOM)
					receiptAddress.setText("");
				receiptAddress.setEnabled(true);
				mSearch.setEnabled(true);
			}
			break;
		case R.id.radioButtonBT:
			if (checked) {
				receiptAddress.setText("BT:");
				receiptAddress.setEnabled(true);
				mSearch.setEnabled(true);
			}
			break;
		case R.id.radioButtonUSB:
			if (checked) {
				receiptAddress.setText(R.string.txt_usb_label);
				receiptAddress.setEnabled(false);
				mSearch.setEnabled(false);
				
				if(printerSelected == ReceiptSetting.MAKE_SNBC) {
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
		    	    while (deviceIterator.hasNext()){
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
						Utils.alertBox(getActivity(), R.string.txt_usb_printer, getString(R.string.msg_found_printer) + " " + printer.getDeviceName(printer.getDeviceId()));
					}
				}
			}
			break;
		}
	}

	private void enableESCPOS() {
		printerSelected = ReceiptSetting.MAKE_ESCPOS;

		printerType.setSelection(0);
		
		//footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		//footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE3.setChecked(true);


		if (ReceiptSetting.drawer)
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

		if (TCP.isChecked()) {
			mSearch.setEnabled(false);
		}
		
		if (USB.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (BT.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (ReceiptSetting.mainPrinter)
			mainPrinter.setChecked(true);
	}

	private void enableCustom() {
		printerSelected = ReceiptSetting.MAKE_CUSTOM;
	
		printerType.setSelection(3);
		
		//footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		//footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE2.setChecked(true);
		
		if (ReceiptSetting.drawer)
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
		
		if (TCP.isChecked()) {
			mSearch.setEnabled(true);
		}
		
		if (USB.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if (BT.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (ReceiptSetting.mainPrinter)
			mainPrinter.setChecked(true);
	}
	
	private void enableSNBC() {
		printerSelected = ReceiptSetting.MAKE_SNBC;

		printerType.setSelection(2);
		
		//footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		//footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(true);
		SIZE2.setChecked(true);
		
		if (ReceiptSetting.drawer)
			DK.setChecked(true);

		TCP.setEnabled(true);
		USB.setEnabled(true);
		USB.setChecked(true);
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
		
		if (TCP.isChecked()) {
			mSearch.setEnabled(true);
		}
		
		if (USB.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if (BT.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (ReceiptSetting.mainPrinter)
			mainPrinter.setChecked(true);
	}
	
	private void enablePT6210() {
		printerSelected = ReceiptSetting.MAKE_PT6210;
		
		printerType.setSelection(4);
		
		//footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		//footerblurb.setText(ReceiptSetting.blurb);
		receiptAddress.setText(ReceiptSetting.address);
		
		DK.setEnabled(true);
		
		SIZE2.setEnabled(true);
		SIZE3.setEnabled(false);
		SIZE2.setChecked(true);
		
		if (ReceiptSetting.drawer)
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
		
		if (TCP.isChecked()) {
			mSearch.setEnabled(true);
		}
		
		if (USB.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if (BT.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (ReceiptSetting.mainPrinter)
			mainPrinter.setChecked(true);
	}

	private void enableStar() {	
		printerSelected = ReceiptSetting.MAKE_STAR;
		
		printerType.setSelection(1);
		
		//footerblurb.setEnabled(true);
		receiptAddress.setEnabled(true);

		//footerblurb.setText(ReceiptSetting.blurb);
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
		
		if (TCP.isChecked()) {
			mSearch.setEnabled(true);
		}
		
		if (USB.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(false);
		}
		
		if (BT.isChecked()) {
			receiptAddress.setEnabled(false);
			mSearch.setEnabled(true);
		}
		
		if (ReceiptSetting.drawer)
			DK.setChecked(true);
		
		if (ReceiptSetting.mainPrinter)
			mainPrinter.setChecked(true);
	}

	protected void disableFields() {	
		printerSelected = 0;

		printerType.setSelection(0);
		
		TCP.setEnabled(false);
		BT.setEnabled(false);
		USB.setEnabled(false);
	
		DK.setEnabled(false);
				
		SIZE2.setEnabled(false);
		SIZE3.setEnabled(false);

		receiptAddress.setEnabled(false);
		prefixCommand.setEnabled(false);
		suffixCommand.setEnabled(false);
		
		//mTest.setEnabled(false);
		mSearch.setEnabled(false);
		
		receiptEveryTime.setEnabled(true);
		receiptFor10.setEnabled(true);
		receiptFor20.setEnabled(true);
		receiptFor30.setEnabled(true);
	}

	public void PortDiscovery() {
		if (TCP.isChecked()) {
			getPortDiscovery("LAN");
		}
		
		if(BT.isChecked()) {
			getPortDiscovery("Bluetooth");
		}
		
		if (USB.isChecked()) {
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
		if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
		
		StringBuilder testString = new StringBuilder();
		String textToPrint = "Connected to Printer Successfully.";
		testString.append('\n').append('\n').append(EscPosDriver.wordWrap(textToPrint, cols+1)).append('\n').append('\n');

		return EscPosDriver.print(getActivity(), testString.toString(), ReceiptSetting.drawer);
	}
	
	public static boolean OpenDevice() {	
		Log.v("Printing", "Opening Devices");
		
		if (prnDevice == null) {
			try {
				if (ReceiptSetting.type == ReceiptSetting.TYPE_LAN) {
			        try {
						if(!InetAddress.getByName(ReceiptSetting.address).isReachable(1000)) {
				    		Log.v("Network Error", "Host not reachable.");
				    		return false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        
					prnDevice = new CustomAndroidAPI().getPrinterDriverETH(ReceiptSetting.address);
				} else {
					UsbDevice printer = null;
		    	    UsbManager mUsbManager = null;
			    	if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
			    		mUsbManager = (UsbManager) me.getActivity().getSystemService(Context.USB_SERVICE);

			    	    boolean foundDevice = false;

			    	    String devices = "";
			    	    
			    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			    	    while (deviceIterator.hasNext()) {
			    	    	UsbDevice device = deviceIterator.next();

			    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER) {
			    	    		printer = device;
			    	    		break;
			    	    	}
			    		        
			    	    	if (device.getInterfaceCount() > 0) {
			    	    		for (int p = 0; p < device.getInterfaceCount(); p++) {
			    	    			if (device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
			    	    				printer = device;
			    	    				break;
			    	    			}
			    	    		}
			    	    	}   		        
			    	    }
					
						if (prnDevice == null && printer != null) {
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
				Utils.alertBox(getActivity(), R.string.txt_receipt_settings, R.string.msg_could_not_send_receipt);
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

    		if (printerSelected == ReceiptSetting.MAKE_ESCPOS) {
		        ArrayList<String> mArrayAdapter = new ArrayList<>();

		        if (BT.isChecked()) {
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
		        
		        if (USB.isChecked()) {
		    		UsbManager mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

		    	    boolean foundDevice = false;

		    	    String devices = "";
		    	    UsbDevice printer = null;
		    	    
		    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    	    while (deviceIterator.hasNext()) {
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
		    	    	
		    	    	// mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("com.android.example.USB_PERMISSION"), 0);
		    	    	//IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
		    	    	//getActivity().registerReceiver(mUsbReceiver, filter);
		    	    	
		    	    	//mUsbManager.requestPermission(device, mPermissionIntent);
		    	    }
		    	    
		        }
				return mArrayAdapter;
			}
    		
			if (printerSelected == ReceiptSetting.MAKE_STAR) {
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
			
			if (printerSelected == ReceiptSetting.MAKE_SNBC) {
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
			} else if(printerSelected == ReceiptSetting.MAKE_CUSTOM) {
				String[] ethDeviceList = null;
				
	    		try {
	            	//Get the list of devices (search for 1.5 seconds)
	    			if (TCP.isChecked())
	    				ethDeviceList = CustomAndroidAPI.EnumEthernetDevices(5000, getActivity());
	    			
	    			if (BT.isChecked())
	    				btDeviceList = CustomAndroidAPI.EnumBluetoothDevices();
	    			
	            	if ((ethDeviceList == null) || (ethDeviceList.length == 0)) {
		    			return list;
	            	}                               	
	            } catch(CustomException e ) {
	            	e.printStackTrace();
	    			return list;
	            } catch(Exception e ) {
	            	e.printStackTrace();
	    			return list;
	            } 
	    		
	    		if (TCP.isChecked()) {
		    		for (int i = 0;i<ethDeviceList.length;i++) {
		    			list.add(ethDeviceList[i]);
		    		}
	    		}
	    		
    			if (BT.isChecked()) {
		    		for (int i = 0;i<btDeviceList.length;i++) {
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
					.setTitle(R.string.msg_select_ip_address_or_input_port_name)
					.setView(editPortName)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int button) {
									receiptAddress.setText(editPortName.getText());
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
									receiptAddress.setText(mDeviceAdapter.get(select).getAddress());
								}
							}).show();
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
}
