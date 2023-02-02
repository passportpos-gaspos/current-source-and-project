package com.pos.passport.util;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.StarMicronics.StarIOSDK.PrinterFunctions;
import com.elotouch.paypoint.register.EloTouch;
import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.activity.PaymentActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;

@SuppressLint("NewApi")
public class EscPosDriver {
	public static final int POS_SUCCESS=1000;		//success	
	public static final int ERR_PROCESSING = 1001;	//processing error
	public static final int ERR_PARAM = 1002;		//parameter error
	public static POSSDK pos_usb = null;
	public static boolean sdk_flag = false;
	private static Socket clientSocket;
	private static OutputStream out;
	private static BluetoothSocket mmSocket;
	public static HashMap<BluetoothDevice, BluetoothSocket> mmSocketMap=new HashMap<BluetoothDevice, BluetoothSocket>();
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private static PendingIntent mPermissionIntent;
	private static POSInterfaceAPI interface_usb = null;
	private static int error_code = 0;
	private static UsbDevice display;

    private EscPosDriver() {
    	
    }

    public static String wordWrap(String input, int width) {
        // protect ourselves
        if (input == null) {
            return "";
        }
        else if (width < 5) {
            return input;
        }
        else if (width >= input.length()) {
            return input;
        }

        StringBuilder buf = new StringBuilder(input);
        boolean endOfLine = false;
        int lineStart = 0;

        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == '\n') {
                lineStart = i + 1;
                endOfLine = true;
            }

            // handle splitting at width character
            if (i > lineStart + width - 1) {
                if (!endOfLine) {
                    int limit = i - lineStart - 1;
                    BreakIterator breaks = BreakIterator.getLineInstance(Locale.ENGLISH);
                    breaks.setText(buf.substring(lineStart, i));
                    int end = breaks.last();

                    // if the last character in the search string isn't a space,
                    // we can't split on it (looks bad). Search for a previous
                    // break character
                    if (end == limit + 1) {
                        if (!Character.isWhitespace(buf.charAt(lineStart + end))) {
                            end = breaks.preceding(end - 1);
                        }
                    }

                    // if the last character is a space, replace it with a \n
                    if (end != BreakIterator.DONE && end == limit + 1) {
                        buf.replace(lineStart + end, lineStart + end + 1, "\n");
                        lineStart = lineStart + end;
                    }
                    // otherwise, just insert a \n
                    else if (end != BreakIterator.DONE && end != 0) {
                        buf.insert(lineStart + end, '\n');
                        lineStart = lineStart + end + 1;
                    }
                    else {
                        buf.insert(i, '\n');
                        lineStart = i + 1;
                    }
                }
                else {
                    buf.insert(i, '\n');
                    lineStart = i + 1;
                    endOfLine = false;
                }
            }
        }

        return buf.toString();
    }
    
    public static boolean SendToDisplay(Context context, String DisplayText) {

    	String string = Normalizer.normalize(DisplayText, Normalizer.Form.NFD);
		String message = string.replaceAll("[^\\p{ASCII}]", "");

		byte[] ai = { 27, (byte) 178, 68 };
		byte[] ai1 = { 27, (byte) 178, 80 };
		byte[] ai2 = { 12 };

		try {
			byte ai3[] = new byte[ai.length + ai1.length + ai2.length
					+ message.length()];
			int j = 0;

			for (int i = 0; i < ai.length; i++) {
				int i2 = j + 1;
				ai3[j] = ai[i];
				j = i2;
			}

			for (int i = 0; i < ai2.length; i++) {
				int l1 = j + 1;
				ai3[j] = ai2[i];
				j = l1;
			}

			for (int i = 0; i < message.length(); i++) {
				int k1 = j + 1;
				ai3[j] = message.getBytes()[i];
				j = k1;
			}

			for (int i = 0; i < ai1.length; i++) {
				int j1 = j + 1;
				ai3[j] = ai1[i];
				j = j1;
			}

			if (ReceiptSetting.dmake == ReceiptSetting.MAKE_CUSTOM) {
				if (ReceiptSetting.dtype == ReceiptSetting.TYPE_USB) {
					UsbManager mUsbManager = null;
					if(display == null)
		    	    {
			    		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
			    	    
			    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			    	    while(deviceIterator.hasNext()){
			    	    	UsbDevice device = deviceIterator.next();
	
			    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER)
			    	    	{
			    	    		display = device;
			    	    		break;
			    	    	}
			    		        
			    	    	if(device.getInterfaceCount() > 0)
			    	    	{
			    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
			    	    		{
			    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
			    	    			{
			    	    				display = device;
			    	    				break;
			    	    			}
			    	    		}
			    	    	}   		        
			    	    }
		    	    }
					
		    	    if(display != null) {
			    		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

		    	    	if(!mUsbManager.hasPermission(display)) {
			    			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			context.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(display, mPermissionIntent);
			    	    	return false;
		    	    	} else {
		    	    		UsbInterface intf = null;
			    	    	UsbEndpoint endpoint= null;
			    	    	
		    	    		for(int p = 0; p < display.getInterfaceCount(); p++) {
		    	    			if(display.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
		    	    				intf = display.getInterface(p);
		    	    				break;
		    	    			}
		    	    		}
		    	    		
		    	    		if (intf != null) {
			    	    		for(int p = 0; p < intf.getEndpointCount(); p++) {
			    	    			if(intf.getEndpoint(p).getDirection() == UsbConstants.USB_DIR_OUT) {
			    	    				endpoint = intf.getEndpoint(p);
			    	    				break;
			    	    			}
			    	    		}
		    	    		} else {
		    	    			return false;
		    	    		}
							    	    	
			    	    	if(endpoint != null) {
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(display);
				    	    	if(connection != null) {
					    	    	connection.claimInterface(intf, true);					    	    	
					    	    	connection.bulkTransfer(endpoint, ai3, ai3.length, 1000); //do in another thread
				    	    	} else {
				    	    		return false;
				    	    	}
			    	    	}
		    	    	} 
		    	    	
		    	    	return true;
		    	    } else {
		    	    	return false;
		    	    }
				}
			}
		} catch (Exception localException) {
			return false;
		}

		return true;
    }

	public static boolean print(Context context, String printString, boolean openDrawer) {
        if (ReceiptSetting.make == ReceiptSetting.MAKE_ELOTOUCH && Build.MODEL.contains(Consts.ELO_MODEL)) {
            EloTouch eloTouch = new EloTouch(context);
            if(openDrawer)
            eloTouch.openDrawer();
            return eloTouch.print(printString);
        }

        if (ReceiptSetting.make == ReceiptSetting.MAKE_STAR) {
            Typeface typeface = Typeface.MONOSPACE;

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setTypeface(typeface);
            paint.setTextSize(22);
            TextPaint textpaint = new TextPaint(paint);

            StaticLayout staticLayout = new StaticLayout(printString,
                    textpaint, 576, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
            int height = staticLayout.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height,
                    Bitmap.Config.RGB_565);
            Canvas c = new Canvas(bitmap);
            c.drawColor(Color.WHITE);
            c.translate(0, 0);
            staticLayout.draw(c);

            String portName = ReceiptSetting.address;
            String portSettings = "";

            PrinterFunctions.PrintBitmap(context, portName, portSettings, bitmap, 576, false, openDrawer);


        } else if (ReceiptSetting.make == ReceiptSetting.MAKE_SNBC) {

            interface_usb = new POSUSBAPI(context);

            error_code = interface_usb.OpenDevice();
            if (error_code != POS_SUCCESS) {
                return false;
            } else {
                pos_usb = new POSSDK(interface_usb);
                sdk_flag = true;
                String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
                string = string.replaceAll("[^\\p{ASCII}]", "");
                pos_usb.textPrint(string.getBytes(), string.getBytes().length);
                pos_usb.systemCutPaper(66, 0);
                if (openDrawer) {
                    //interface_usb.WriteBuffer(PrinterCommands.KICK, 0, PrinterCommands.KICK.length, 2000);
                    pos_usb.cashdrawerOpen(0,100,100);
                }
                //pos_usb.cashdrawerOpen(CashdrawerID, PulseOnTimes, PulseOffTimes)
                interface_usb.CloseDevice();
                //Toast.makeText(USBActivity.this, "Open Port OK!.", Toast.LENGTH_LONG).show();
                return true;
            }

        } else if (ReceiptSetting.make == ReceiptSetting.MAKE_PT6210) {

            String string = Normalizer.normalize(printString,
                    Normalizer.Form.NFD);
            string = string.replaceAll("[^\\p{ASCII}]", "");

            writeToFile(PTPRINTER, string);
            if (openDrawer) {
                opencash1();
                opencash2();
            }
            return true;
        } else if (ReceiptSetting.make == ReceiptSetting.MAKE_ESCPOS || ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
            try {
                if (ReceiptSetting.type == ReceiptSetting.TYPE_LAN) {
                    if (!(ReceiptSetting.address.length() > 6)) {
                        return false;
                    }

                    if (!InetAddress.getByName(ReceiptSetting.address).isReachable(2000)) {
                        Log.v("USBPRINT", "FAILED");
                        return false;
                    }
                    try {
                        clientSocket = new Socket(ReceiptSetting.address, 9100);
                        out = clientSocket.getOutputStream();

                        out.write(PrinterCommands.INIT);

                        String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
                        string = string.replaceAll("[^\\p{ASCII}]", "");

                        out.write(string.getBytes("ASCII"));

                        if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
                            out.write(PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT);
                            if (openDrawer) out.write(PrinterCommands.CUSTOM_KICK);
                        } else {
                            out.write(PrinterCommands.FEED_PAPER);
                            out.write(PrinterCommands.FEED_PAPER_AND_CUT);
                            if (openDrawer) out.write(PrinterCommands.KICK);
                        }

                        out.close();
                        return true;
                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and get out
                        connectException.printStackTrace();
                        try {
                            out.close();
                        } catch (IOException closeException) {
                            closeException.printStackTrace();
                        }
                        return false;
                    }
                }

                if (ReceiptSetting.type == ReceiptSetting.TYPE_BT) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        return false;
                    }

                    if (!mBluetoothAdapter.isEnabled()) {
                        return false;
                    }

                    BluetoothDevice BTdevice = null;

                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    // If there are paired devices
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getAddress().equals(ReceiptSetting.address)) {
                                Log.v("BT Printer", "Found Printer: " + device.getAddress());
                                BTdevice = device;

                                if (!mmSocketMap.containsKey(device)) {
                                    Log.v("BT Printer", "New Printer: " + device.getAddress());
                                    mmSocketMap.put(device, null);
                                }
                            }
                        }
                    }

                    mBluetoothAdapter.cancelDiscovery();

                    if (BTdevice == null) {
                        return false;
                    }

                    try {

                        if (mmSocketMap.get(BTdevice) == null) {
                            BluetoothSocket mmSocketin = BTdevice.createInsecureRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
                            mmSocketin.connect();

                            mmSocketMap.put(BTdevice, mmSocketin);
                            mmSocket = mmSocketin;
                        } else {
                            mmSocket = mmSocketMap.get(BTdevice);

                            if (!mmSocket.isConnected()) {
                                mmSocket = BTdevice.createInsecureRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
                                mmSocket.connect();
                            }
                        }

                        //if(mmSocket == null)
                        //{
                        //if(mmSocket != null && !mmSocket.isConnected())
                        //	mmSocket = BTdevice.createInsecureRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
                        //	mmSocket.connect();
                        //}
                        out = mmSocket.getOutputStream();

                        out.write(PrinterCommands.INIT);

                        String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
                        string = string.replaceAll("[^\\p{ASCII}]", "");

                        out.write(string.getBytes("ASCII"));

                        if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
                            out.write(PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT);
                            if (openDrawer) out.write(PrinterCommands.CUSTOM_KICK);
                        } else {
                            out.write(PrinterCommands.FEED_PAPER);
                            out.write(PrinterCommands.FEED_PAPER_AND_CUT);
                            if (openDrawer) out.write(PrinterCommands.KICK);
                        }

                        //out.close();
                        //mmSocket.close();
                        return true;
                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and get out
                        connectException.printStackTrace();
                        try {
                            if (mmSocket != null && mmSocket.isConnected()) mmSocket.close();
                            if (out != null) out.close();
                            mmSocket = null;
                        } catch (IOException closeException) {
                            closeException.printStackTrace();
                        }
                        return false;
                    }
                }

                if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
                    UsbDevice printer = null;
                    UsbManager mUsbManager = null;

                    mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

                    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();

                        if (device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER) {
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

                    if (printer != null) {
                        if (!mUsbManager.hasPermission(printer)) {
                            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                            context.registerReceiver(mUsbReceiver, filter);

                            mUsbManager.requestPermission(printer, mPermissionIntent);
                            return false;
                        } else {
                            UsbInterface intf = null;
                            UsbEndpoint endpoint = null;

                            for (int p = 0; p < printer.getInterfaceCount(); p++) {
                                if (printer.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                                    intf = printer.getInterface(p);
                                    break;
                                }
                            }

                            if (intf != null) {
                                for (int p = 0; p < intf.getEndpointCount(); p++) {
                                    if (intf.getEndpoint(p).getDirection() == UsbConstants.USB_DIR_OUT) {
                                        endpoint = intf.getEndpoint(p);
                                        break;
                                    }
                                }
                            } else {
                                return false;
                            }

                            if (endpoint != null) {
                                UsbDeviceConnection connection = mUsbManager.openDevice(printer);
                                if (connection != null) {
                                    connection.claimInterface(intf, true);
                                    connection.bulkTransfer(endpoint, PrinterCommands.INIT, PrinterCommands.INIT.length, 500); //do in another thread

                                    String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
                                    string = string.replaceAll("[^\\p{ASCII}]", "");

                                    connection.bulkTransfer(endpoint, string.getBytes(), string.getBytes().length, 500); //do in another thread

                                    if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
                                        connection.bulkTransfer(endpoint, PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT, PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT.length, 500); //do in another thread
                                        if (openDrawer)
                                            connection.bulkTransfer(endpoint, PrinterCommands.CUSTOM_KICK, PrinterCommands.CUSTOM_KICK.length, 500); //do in another thread
                                    } else {
                                        connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER, PrinterCommands.FEED_PAPER.length, 500); //do in another thread
                                        connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER_AND_CUT, PrinterCommands.FEED_PAPER_AND_CUT.length, 500); //do in another thread
                                        if (openDrawer)
                                            connection.bulkTransfer(endpoint, PrinterCommands.KICK, PrinterCommands.KICK.length, 500); //do in another thread
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void closeBTConnections() {
		try {
			Iterator myVeryOwnIterator = mmSocketMap.keySet().iterator();
			while(myVeryOwnIterator.hasNext()) {
				BluetoothDevice key=(BluetoothDevice)myVeryOwnIterator.next();
			    BluetoothSocket value=(BluetoothSocket)mmSocketMap.get(key);
			    value.close();
			    value = null;
			}
			
		} catch (IOException closeException) {
        	closeException.printStackTrace();
        }
	}
	
	public static boolean printReceipt(Context context, Cart cart, int mCheckdata)
    {
    	DecimalFormat nf = new DecimalFormat("0.00");
		nf.setGroupingUsed(false);
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	boolean result = false;
    	
    	for (String t : ReceiptSetting.printers) {
			try {
				JSONObject object = new JSONObject(t);

				
				ReceiptSetting.enabled = true;
	        	ReceiptSetting.address = object.optString("address");
	        	ReceiptSetting.make = object.optInt("printer");
	        	ReceiptSetting.size = object.optInt("size");
	        	ReceiptSetting.type = object.optInt("type");
	        	ReceiptSetting.drawer = object.optBoolean("cashDrawer");
	        	//ReceiptSetting.receiptPrintOption = object.getInt("receiptprintoption");
                Log.e("Print option","Print Option"+PrefUtils.getPrintOption(context));
				ReceiptSetting.receiptPrintOption = PrefUtils.getPrintOption(context);
	        	if(object.has("main"))
	        		ReceiptSetting.mainPrinter = object.optBoolean("main");
	        	else
	        		ReceiptSetting.mainPrinter = true;
    	
				int cols = 40;
				
				if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
					cols = 30;
				
				StringBuilder receiptString = new StringBuilder();
		
				//------------Store Name----------------------
				if(!(StoreSetting.getName().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
				}
				
				//------------Store Address----------------------
				if(!(StoreSetting.getAddress1().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress1(), cols+1)).append('\n');
				}
                //------------Store city----------------------
                if(!(StoreSetting.getCity().equals(""))){
                    receiptString.append(EscPosDriver.wordWrap(String.format("%1$s, %2$s",StoreSetting.getCity(), StoreSetting.getState()), cols+1)).append('\n');
                }
                //------------Store state----------------------
                /*if(!(StoreSetting.getState().equals(""))){
                    receiptString.append(EscPosDriver.wordWrap(StoreSetting.getState(), cols+1)).append('\n');
                }*/
				//---------------Store Number-----------------
				if(!(StoreSetting.getPhone().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
				}
				
				//-----------------Store Website----------------------
				if(!(StoreSetting.getWebsite().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
				}
				
				//-----------------------Store Email-----------------------------
				if(!(StoreSetting.getEmail().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
				}
				
				if(StoreSetting.getReceipt_header() != null && !(StoreSetting.getReceipt_header().equals(""))){
					receiptString.append('\n');
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_header(), cols+1)).append('\n');
				}
                Log.e("city state","city"+StoreSetting.getCity()+"<<state>>"+StoreSetting.getState());
				//-------------------Date------------------------	
		        ProductDatabase db = ProductDatabase.getInstance(context);
                Log.e("Get Mid","ESCPOsDri>>>>"+cart.mId);
                Log.e("Get Mtrnas","ESCPOsDri>>>>"+cart.mTrans);
                ReportCart reportCart = db.getSaleById(Integer.parseInt(""+cart.mTrans));
                JSONObject cartJson = new JSONObject(reportCart.cartItems);
                Log.e("Cart Item","CartItem >>>>"+cartJson);
				String date = DateFormat.getDateTimeInstance().format(new Date(reportCart.mDate));
				receiptString.append('\n');
				receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
				receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_transaction_label) + " " + reportCart.trans, cols+1)).append('\n');
				receiptString.append('\n');
		
				if(cart.mCashier != null) {
					if(reportCart.mCashier.name.equals("Training")) {
						StringBuffer message = null;
						if(cols == 40)
							message = new StringBuffer("--------------- " + context.getString(R.string.txt_training_cap) + " ---------------".substring(0, cols));
		
						else
							message = new StringBuffer("---------- " + context.getString(R.string.txt_training_cap) + " ----------".substring(0, cols));
					
						receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
					} else {
						receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_cashier_label) + " " + reportCart.mCashier.name, cols+1)).append('\n');
						receiptString.append('\n');
					}
				}
		
				//----------------Customer Name/Email-------------------
				if (cart.mCustomer != null) {
                    String customerName = cart.getCustomerLastName().length() >0 ? cart.getCustomerLastName() : cart.getCustomerName();
					receiptString.append(EscPosDriver.wordWrap(customerName, cols+1)).append('\n');
					receiptString.append(EscPosDriver.wordWrap(cart.getCustomerEmail(), cols+1)).append('\n');
					receiptString.append('\n');
				}	
				
				if (cart.mVoided) {
					StringBuffer message = null;
					if (cols == 40)
						 message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
					else
						 message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));
				
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
				}

                if(cart.mStatus.equals(Cart.RETURNED)){
                    StringBuffer message = null;
                    if (cols == 40)
                        message = new StringBuffer(("---------------- " + context.getString(R.string.txt_return_cap) + " ----------------").substring(0, cols));
                    else
                        message = new StringBuffer(("----------- " + context.getString(R.string.txt_return_cap) + " -----------").substring(0, cols));

                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                }
				
				//------------------Products-----------------------------------
                Log.e("Product","Product array"+cartJson.getJSONArray("Products"));
                for(int p =0; p<cartJson.getJSONArray("Products").length() ; p++)
                {
                    JSONObject product = cartJson.getJSONArray("Products").optJSONObject(p);
                   // Log.e("Product att loop",">>>"+product);
                    StringBuilder prodBuilder = new StringBuilder(product.getString("quantity"));
                    prodBuilder.append(" ").append(product.getString("name"));
                    while(prodBuilder.length() < 40){
                        prodBuilder.append(" ");
                    }
                    StringBuffer prod = new StringBuffer(prodBuilder.toString().substring(0,cols));
                    String total = Utils.formatCurrency(new BigDecimal(product.optString("total")));
                    Log.d("receipt: prod length", String.valueOf(prod.length()));
                    prod.replace((prod.length()-total.length()), cols - 1, total);
                    receiptString.append(EscPosDriver.wordWrap(prod.toString(), cols+1)).append('\n');

                    /*if(!product.optString("barcode").equals("") || !product.optString("barcode").equals(null))
                        receiptString.append("  " +EscPosDriver.wordWrap(product.optString("barcode"), cols+1)).append('\n');*/
                    /*StringBuffer message = new StringBuffer("                                        ".substring(0, cols));
                    String quan = product.optInt("quantity") + " @ " + DecimalFormat.getCurrencyInstance().format(product.optString("total"));
                    //message.replace(0, quan.length(), quan);
                    message.replace(message.length() - quan.length(), cols - 1, quan);
                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');*/
                    Log.d("receipt", "product: " + prod.toString());
                    if(new BigDecimal(product.optString("discountAmount")).compareTo(BigDecimal.ZERO) > 0){
                        Log.d("receipt:", "discountAmount:" + product.optString("discountAmount"));
                        StringBuffer discountMessage = new StringBuffer((String.format("%-8s",product.optString("discountName")) + "                                ").substring(0, cols));
                        String discount = Utils.formatDiscount(new BigDecimal(product.optString("discountAmount")));
                        discountMessage.replace(discountMessage.length()-discount.length(), cols-1, discount);
                        receiptString.append(EscPosDriver.wordWrap(discountMessage.toString(), cols+1)).append('\n');
                        Log.d("receipt", "Item Discount: " + discountMessage.toString());
                    }
                    Log.d("Modifire","Modifire array>>"+product.getJSONArray("modifiers"));
                    for(int m=0; m<product.getJSONArray("modifiers").length() ; m++)
                    {
                        JSONObject modifier = product.getJSONArray("modifiers").optJSONObject(m);
                        StringBuffer mMessage = new StringBuffer("  ");
                        //StringBuffer mMessage = new StringBuffer("                                        ".substring(0, cols));
                        String modifierPrice = Utils.formatCurrency(new BigDecimal(modifier.optString("price")));
                        String modifierString = modifier.optString("name").concat("(").concat(modifierPrice).concat(")");
                        //mMessage.replace(mMessage.length() - modifierString.length(), cols-1, modifierString);
                        //receiptString.append(EscPosDriver.wordWrap(mMessage.toString(), cols+1)).append('\n');
                        receiptString.append(EscPosDriver.wordWrap(mMessage.append(modifierString).toString(), cols+1)).append('\n');
                        Log.d("receipt", "Modifiers " + mMessage.toString());
                    }

                    for(int c=0; c<product.getJSONArray("comboItems").length(); c++){
                        JSONObject comboItems = product.getJSONArray("comboItems").optJSONObject(c);
                        for(int ci=0; ci< comboItems.getJSONArray("items").length(); ci++){
                            JSONObject items = comboItems.getJSONArray("items").optJSONObject(ci);
                            StringBuffer mMessage = new StringBuffer("  ");
                            //StringBuffer mMessage = new StringBuffer("                                        ".substring(0, cols));
                            //mMessage.replace(mMessage.length() - items.optString("name").length(),cols-1, items.optString("name"));
                            receiptString.append(EscPosDriver.wordWrap(mMessage.append(items.optString("name")).toString(), cols+1)).append('\n');
                            Log.d("receipt", "Combo items : " + mMessage.toString());
                        }
                    }

                }
                receiptString.append('\n');
                StringBuffer subTotalMessage = new StringBuffer((context.getString(R.string.txt_sub_total) + "                               ").substring(0, cols));
                String subTotal = Utils.formatCurrency(cart.mSubtotal);
                subTotalMessage.replace(subTotalMessage.length()-subTotal.length(), cols-1, subTotal);
                receiptString.append(EscPosDriver.wordWrap(subTotalMessage.toString(), cols+1)).append('\n');
                Log.d("receipt", "Sub Total : " + subTotalMessage.toString());

                BigDecimal totalTax = BigDecimal.ZERO;
                for(int tax=0; tax < cartJson.getJSONArray("tax").length(); tax++){
                    JSONObject taxes = cartJson.getJSONArray("tax").optJSONObject(tax);
                    totalTax = totalTax.add(new BigDecimal(taxes.optString("amount")));
                }

                StringBuffer taxMessage = new StringBuffer((context.getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
                String taxAmount = Utils.formatCurrency(totalTax);
                taxMessage.replace(taxMessage.length()-taxAmount.length(), cols-1, taxAmount);
                receiptString.append(EscPosDriver.wordWrap(taxMessage.toString(), cols+1)).append('\n');
                Log.d("receipt:", "tax:" + taxMessage.toString());

                if(cart.mDiscountAmount.compareTo(BigDecimal.ZERO) > 0)
                {
                    StringBuffer discountMessage = new StringBuffer((cart.mDiscountName + "                                ").substring(0, cols));
                    String discount = Utils.formatCurrency(new BigDecimal(cart.mDiscountAmount.toString()));
                    discountMessage.replace(discountMessage.length()-discount.length(), cols-1, discount);
                    receiptString.append(EscPosDriver.wordWrap(discountMessage.toString(), cols+1)).append('\n');
                    Log.d("receipt", "Discount: "+ discountMessage.toString());
                }

                BigDecimal tipAmount =  BigDecimal.ZERO;
                for(int p=0; p < cartJson.getJSONArray("Payments").length(); p++)
                {
                    JSONObject payment = cartJson.getJSONArray("Payments").optJSONObject(p);
                    tipAmount = tipAmount.add(new BigDecimal(payment.optString("tipAmount")));
                }

                if(tipAmount.compareTo(BigDecimal.ZERO) > 0)
                {
                    StringBuffer tipTotalMessage = new StringBuffer((context.getString(R.string.txt_tip_amount) + "                                   ").substring(0, cols));
                    String tipTotalAmount = Utils.formatCurrency(tipAmount);
                    tipTotalMessage.replace(tipTotalMessage.length() - tipTotalAmount.length(), cols - 1, tipTotalAmount);
                    receiptString.append(EscPosDriver.wordWrap(tipTotalMessage.toString(), cols+1)).append('\n');
                    Log.d("receipt", "Tip Total: "+tipTotalMessage.toString());
                }

                StringBuffer totalMessage = new StringBuffer((context.getString(R.string.txt_total) + "                                   ").substring(0, cols));
                String totalAmount = Utils.formatCurrency(cart.mTotal.add(tipAmount));
                totalMessage.replace(totalMessage.length() - totalAmount.length(), cols - 1, totalAmount);
                receiptString.append(EscPosDriver.wordWrap(totalMessage.toString(), cols+1)).append('\n');
                Log.d("receipt", "Total: "+totalMessage.toString());
                StringBuffer message;
                BigDecimal amountReceived = BigDecimal.ZERO;
                StringBuffer paymentStringBuffer = new StringBuffer();
                for(int p=0; p < cartJson.getJSONArray("Payments").length(); p++)
                {
                    JSONObject payment = cartJson.getJSONArray("Payments").optJSONObject(p);
                    message = new StringBuffer((context.getString(R.string.txt_tender_type_label) + "                            ").substring(0, cols));
                    message.replace(13, 13 + payment.optString("paymentType").length(), payment.optString("paymentType"));
                    amountReceived = amountReceived.add(new BigDecimal(payment.optString("paymentAmount")));
                    String amount = Utils.formatCurrency(new BigDecimal(payment.optString("paymentAmount")));
                    message.replace(message.length()-amount.length(), cols-1, amount);
                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
                    Log.d("receipt", "payment: " + message.toString());
                    //tipAmount = tipAmount.add(new BigDecimal(payment.optString("tipAmount")));
                    switch(payment.optString("paymentType"))
                    {
                        case PayActivity.PAYMENT_TYPE_CASH:
                            break;
                        case PayActivity.PAYMENT_TYPE_CHECK:
                            break;
                        case PayActivity.PAYMENT_TYPE_CREDIT:
                            if (cols == 40)
                                message = new StringBuffer(("---------------- " + context.getString(R.string.txt_credit) + " ----------------").substring(0, cols));

                            else
                                message = new StringBuffer(("----------- " + context.getString(R.string.txt_credit) + " -----------").substring(0, cols));

                            paymentStringBuffer.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                            paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_card) , payment.optString("lastFour")),cols+1)).append('\n');
                            paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_card_type), payment.optString("cardType")),cols+1)).append('\n');
                            paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_authorization), payment.optString("authCode")),cols+1)).append('\n');
                            paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_reference), payment.optString("gatewayId")),cols+1)).append('\n');
                            break;

                    }

                }

                StringBuffer changetotalMessage = new StringBuffer((context.getString(R.string.txt_changeamount) + "                                   ").substring(0, cols));
                String changetotalAmount = Utils.formatCurrency(cart.mChangeAmount);
                changetotalMessage.replace(changetotalMessage.length() - changetotalAmount.length(), cols - 1, changetotalAmount);
                receiptString.append(EscPosDriver.wordWrap(changetotalMessage.toString(), cols+1)).append('\n');
                Log.d("receipt", "ChangeAmount: "+changetotalMessage.toString());

				if (cart.mVoided) {
					if(cols == 40)
						 message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
					else
						 message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));
				
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
				}
                if(cart.mStatus.equals(Cart.RETURNED)){
                    if (cols == 40)
                        message = new StringBuffer(("---------------- " + context.getString(R.string.txt_return_cap) + " ----------------").substring(0, cols));
                    else
                        message = new StringBuffer(("----------- " + context.getString(R.string.txt_return_cap) + " -----------").substring(0, cols));

                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                }
				
				if (cart.mCashier != null) {
					if (cart.mCashier.name.equals("Training")) {
						if (cols == 40)
							message = new StringBuffer(("--------------- " + context.getString(R.string.txt_training_cap) + " ---------------").substring(0, cols));
		
						else
							message = new StringBuffer(("---------- " + context.getString(R.string.txt_training_cap) + " ----------").substring(0, cols));
					
						receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
					}
				}

                receiptString.append(paymentStringBuffer);
                /*message = new StringBuffer((context.getString(R.string.txt_customer_change_label) + "                        ").substring(0, cols));
                String change = DecimalFormat.getCurrencyInstance().format(amountReceived.subtract(cart.mTotal));
                message.replace(message.length()-change.length(), cols-1, change);
                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
                Log.d("receipt", "Total: " + message.toString());*/
                if(mCheckdata == Consts.MERCHANT_PRINT_RECEIPT_YES)
                {
                    StringBuffer signature = new StringBuffer(context.getString(R.string.txt_signature));
                    receiptString.append(signature);
                    receiptString.append('\n');
                    receiptString.append('\n');
                    receiptString.append('\n');
                    StringBuffer lineprint=new StringBuffer(("X ______________________________________").substring(0,cols));
                    receiptString.append(EscPosDriver.wordWrap(lineprint.toString(), cols+1)).append('\n');
                    StringBuffer mMessage=new StringBuffer("             Merchant Copy              ");
                    receiptString.append(EscPosDriver.wordWrap(mMessage.toString(), cols+1)).append('\n');
                }
				if (StoreSetting.getReceipt_footer() != null && !(StoreSetting.getReceipt_footer().equals(""))){
					receiptString.append('\n');
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_footer(), cols+1)).append('\n');
					receiptString.append('\n'); 
				}
                receiptString.append('\n').append('\n').append('\n').append('\n');
                Log.d("Receipt", receiptString.toString());
                boolean drawer = ReceiptSetting.drawer ? openDrawer(reportCart,context) : false;
                result = EscPosDriver.print(context, receiptString.toString(), drawer);
			}
            catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return result;
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
	
	public static boolean sendToPT6210Display(Context context, String DisplayText) {
		String string = Normalizer.normalize(DisplayText, Normalizer.Form.NFD);
		String message = string.replaceAll("[^\\p{ASCII}]", "");

		byte[] ai = { 27, (byte) 178, 68 };
		byte[] ai1 = { 27, (byte) 178, 80 };
		byte[] ai2 = { 12 };

		try {
			byte ai3[] = new byte[ai.length + ai2.length + message.length()];
			int j = 0;

			for (int i = 0; i < ai.length; i++) {
				int i2 = j + 1;
				ai3[j] = ai[i];
				j = i2;
			}

			for (int i = 0; i < ai2.length; i++) {
				int l1 = j + 1;
				ai3[j] = ai2[i];
				j = l1;
			}

			for (int i = 0; i < message.length(); i++) {
				int k1 = j + 1;
				ai3[j] = message.getBytes()[i];
				j = k1;
			}

			//if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
			//	if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
					UsbManager mUsbManager = null;
					if(display == null) {
			    		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
			    	    
			    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			    	    while (deviceIterator.hasNext()){
			    	    	UsbDevice device = deviceIterator.next();
	
			    	    	if(device.getDeviceClass() == 0xa) {
			    	    		display = device;
			    	    		break;
			    	    	}
			    		        
			    	    	if (device.getInterfaceCount() > 0) {
			    	    		for (int p = 0; p < device.getInterfaceCount(); p++) {
			    	    			if(device.getInterface(p).getInterfaceClass() == 0xa) {
			    	    				display = device;
			    	    				break;
			    	    			}
			    	    		}
			    	    	}   		        
			    	    }
		    	    }
					
		    	    if(display != null) {
			    		mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);

		    	    	if(!mUsbManager.hasPermission(display)) {
			    			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			context.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(display, mPermissionIntent);
			    	    	return false;
		    	    	} else {
			    	    	UsbInterface intf = display.getInterface(1);
			    	    	UsbEndpoint endpoint = intf.getEndpoint(0);
						    	    	
			    	    	if(endpoint != null) {
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(display);
				    	    	if(connection != null) {
					    	    	connection.claimInterface(intf, true);					    	    	
					    	    	connection.bulkTransfer(endpoint, ai3, ai3.length, 500); //do in another thread
				    	    	} else {
				    	    		return false;
				    	    	}
			    	    	}
		    	    	}
		    	    	return true;
		    	    } else {
		    	    	return false;
		    	    }
			//	}
			//}
		} catch (Exception localException) {
			return false;
		}
    }
    
	public static boolean printReceipt(Context context, ReportCart cart) {
    	DecimalFormat nf = new DecimalFormat("0.00");
		nf.setGroupingUsed(false);
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	
		boolean result = false;
		
		for (String t : ReceiptSetting.printers) {
			try {
				JSONObject object = new JSONObject(t);
				
				ReceiptSetting.enabled = true;
	        	ReceiptSetting.address = object.getString("address");
	        	ReceiptSetting.make = object.getInt("printer");
	        	ReceiptSetting.size = object.getInt("size");
	        	ReceiptSetting.type = object.getInt("type");
	        	ReceiptSetting.drawer = object.getBoolean("cashDrawer");
	        	if(object.has("main"))
	        		ReceiptSetting.mainPrinter = object.getBoolean("main");
	        	else
	        		ReceiptSetting.mainPrinter = true;
    	
				int cols = 40;
				
				if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
					cols = 30;
		    	
				StringBuilder receiptString = new StringBuilder();
		
				//------------Store Name----------------------
				if (!(StoreSetting.getName().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
				}
				
				//------------Store Address----------------------
				if (!(StoreSetting.getAddress1().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress1(), cols+1)).append('\n');
				}
		
				//---------------Store Number-----------------
				if (!(StoreSetting.getPhone().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
				}
				
				//-----------------Store Website----------------------
				if (!(StoreSetting.getWebsite().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
				}
				
				//-----------------------Store Email-----------------------------
				if (!(StoreSetting.getEmail().equals(""))){
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
				}
				
				if (StoreSetting.getReceipt_header() != null && !(StoreSetting.getReceipt_header().equals(""))){
					receiptString.append('\n');
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_header(), cols+1)).append('\n');
				}
				
				//-------------------Date------------------------	
		
				String date = DateFormat.getDateTimeInstance().format(new Date(cart.mDate));
				receiptString.append('\n');
				receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
				receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_transaction_label) + " " + cart.trans, cols + 1)).append('\n');
				receiptString.append('\n'); 
		
				if (cart.mCashier != null) {
					receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_cashier_label) + " " + cart.mCashier.name, cols+1)).append('\n');
					receiptString.append('\n');
				}
		
				//----------------Customer Name/Email-------------------
				if (cart.mCustomer != null) {
                    String name = cart.getCustomerLastName().length()>0 ? cart.getCustomerLastName() : cart.getCustomerLastName();
					receiptString.append(EscPosDriver.wordWrap(name, cols+1)).append('\n');
					receiptString.append(EscPosDriver.wordWrap(cart.getCustomerEmail(), cols+1)).append('\n');
					receiptString.append('\n');
				}	
				
				//------------------Products-----------------------------------
				if (cart.mVoided) {
					StringBuffer message = null;
					if(cols == 40)
						 message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
					else
						 message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));
				
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
				}
				
				StringBuffer message = null;
				if(cols == 40)
					 message = new StringBuffer(("----------------- " + context.getString(R.string.txt_copy_cap) + " -----------------").substring(0, cols));
				else
					 message = new StringBuffer(("------------ " + context.getString(R.string.txt_copy_cap) + " ------------").substring(0, cols));
			
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
				
				BigDecimal nonDiscountTotal = BigDecimal.ZERO;
				for (int o = 0; o < cart.getProducts().size(); o++) {
		
					BigDecimal price = cart.getProducts().get(o).itemPrice(cart.mDate);
					nonDiscountTotal = nonDiscountTotal.add(cart.getProducts().get(o).itemNonDiscountTotal(cart.mDate));
					
					receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).name, cols+1)).append('\n');
					
					if (!cart.getProducts().get(o).isNote) {
						if (!cart.getProducts().get(o).barcode.isEmpty()) {
							receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).barcode, cols+1)).append('\n');
						}
						
						message = new StringBuffer("                                        ".substring(0, cols));
			
						String quan = cart.getProducts().get(o).quantity + " @ " + DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED));
						message.replace(0, quan.length(), quan);	
						
						price = cart.getProducts().get(o).itemTotal(cart.mDate);
						Product item = cart.getProducts().get(o);
						
						String TotalPrice = "";
						ProductDatabase db = ProductDatabase.getInstance(context);
						if (item.cat != 0) {
							String cat = db.getCatById(item.cat);
							int catPos = db.getCatagoryString().indexOf(cat);
			
							if (catPos > -1) {
								if (db.getCatagories().get(catPos).getTaxable1() || db.getCatagories().get(catPos).getTaxable2()) {
									TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED))+"T";
								} else {
									TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
								}
							 }else {
								TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
							}
                        } else {
							TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
						}
						message.replace(message.length()-TotalPrice.length(), cols-1, TotalPrice);	
						receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
					}
				}
				
				receiptString.append('\n');
				
				if(cart.mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
					message = new StringBuffer((context.getString(R.string.txt_discount_label) + "                               ").substring(0, cols));
		
					String discountS = cart.mSubtotalDiscount + "%";
					message.replace(11, 11 + discountS.length(), discountS);

					discountS = DecimalFormat.getCurrencyInstance().format(cart.mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED));
					message.replace(message.length()-discountS.length(), cols-1, discountS);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
				
				message = new StringBuffer((context.getString(R.string.txt_sub_total) + "                               ").substring(0, cols));

				String subprice = DecimalFormat.getCurrencyInstance().format(cart.mSubtotal.divide(Consts.HUNDRED));
				message.replace(message.length()-subprice.length(), cols-1, subprice);	
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		
				if (cart.mTax1Name != null) {
					                                         
					message = new StringBuffer((context.getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
					
					String discountS = cart.mTax1Name + " " + cart.mTax1Percent + "%";
					message.replace(6, 6+discountS.length(), discountS);	
									
					String substring = DecimalFormat.getCurrencyInstance().format(cart.mTax1.divide(Consts.HUNDRED));
					message.replace(message.length()-substring.length(), cols-1, substring);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
		
				if (cart.mTax2Name != null) {
					message = new StringBuffer((context.getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
		
					message.replace(6, 6+(cart.mTax2Name + " " + cart.mTax2Percent + "%").length(), cart.mTax2Name + " " + cart.mTax2Percent + "%");
								
					String substring = DecimalFormat.getCurrencyInstance().format(cart.mTax2.divide(Consts.HUNDRED));
					message.replace(message.length()-substring.length(), cols-1, substring);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
				
				if (cart.mTax3Name != null) {
					message = new StringBuffer((context.getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
		
					message.replace(6, 6+(cart.mTax3Name + " " + cart.mTax3Percent + "%").length(), cart.mTax3Name + " " + cart.mTax3Percent + "%");
								
					String substring = DecimalFormat.getCurrencyInstance().format(cart.mTax2.divide(Consts.HUNDRED));
					message.replace(message.length()-substring.length(), cols-1, substring);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
				
				///----------------------
				message = new StringBuffer((context.getString(R.string.txt_total) + "                                   ").substring(0, cols));
		
				subprice = DecimalFormat.getCurrencyInstance().format(cart.mTotal.divide(Consts.HUNDRED));
				message.replace(message.length()-subprice.length(), cols-1, subprice);	
				
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		
				///----------------------
		
				if (cart.mVoided) {
					if(cols == 40)
						 message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
					else
						 message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));
				
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
				}
				
				BigDecimal paymentSum = BigDecimal.ZERO;
				for (int p = 0; p < cart.mPayments.size(); p++) {
					paymentSum = paymentSum.add(cart.mPayments.get(p).paymentAmount);
		
					///----------------------
					message = new StringBuffer((context.getString(R.string.txt_tender_type_label) + "                            ").substring(0, cols));
		
					message.replace(13, 13 + cart.mPayments.get(p).paymentType.length(), cart.mPayments.get(p).paymentType);

					subprice = DecimalFormat.getCurrencyInstance().format(cart.mPayments.get(p).paymentAmount.divide(Consts.HUNDRED));
					message.replace(message.length()-subprice.length(), cols-1, subprice);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
					///----------------------
				}
						
				if (paymentSum.compareTo(cart.mTotal) > 0) {
					///----------------------
					message = new StringBuffer((context.getString(R.string.txt_customer_change_label) + "                        ").substring(0, cols));

					subprice = DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(cart.mTotal).divide(Consts.HUNDRED));
					message.replace(message.length()-subprice.length(), cols-1, subprice);	
					
					receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
					///----------------------
				}
				
				if (StoreSetting.getReceipt_footer() != null && !(StoreSetting.getReceipt_footer().equals(""))){
					receiptString.append('\n');
					receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_footer(), cols+1)).append('\n');
					receiptString.append('\n'); 
				}
	        	
				receiptString.append('\n'); 
				receiptString.append('\n'); 
				
	        	if (ReceiptSetting.mainPrinter){
	        		if (MainActivity.resentReceiptPrintFlag) {
	        			result = EscPosDriver.print(context, receiptString.toString(), false);
	        		} else {
	        			result = EscPosDriver.print(context, receiptString.toString(), ReceiptSetting.drawer);
	        		}
	        	}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return result;
    }

	private static final String CASH1CDIR = "/sys/class/gpio/gpio35/direction";
    private static final String CASH2CDIR = "/sys/class/gpio/gpio34/direction";
    private static final String CASH1CONTROL = "/sys/class/gpio/gpio35/value";
    private static final String CASH2CONTROL = "/sys/class/gpio/gpio34/value";
    private static final String PTPRINTER = "/dev/usb/lp0";
    private static final String CASHOUT = "out";
    private static final String CASHIN = "in";
    private static final String CASHTH = "1";
    private static final String CASHTL = "0";
    
	private static void opencash1() {
//		writeToFile(GPIOEXPORT,CASH1CGPIO);
		writeToFile(CASH1CDIR,CASHOUT);
		writeToFile(CASH1CONTROL,CASHTH);
		writeToFile(CASH1CONTROL,CASHTL);
		writeToFile(CASH1CONTROL,CASHTH);
	}
	private static void opencash2() {
//		writeToFile(GPIOEXPORT,CASH2CGPIO);
		writeToFile(CASH2CDIR,CASHOUT);
		writeToFile(CASH2CONTROL,CASHTH);
		writeToFile(CASH2CONTROL,CASHTL);
		writeToFile(CASH2CONTROL,CASHTH);
	}

	private static void writeToFile(String FILENAME, String data) {
        try {
        	FileOutputStream output = new FileOutputStream(FILENAME);
        	byte[] b = data.getBytes();
        	output.write(b);
        	output.close();
        }
        catch (IOException e) {
            Log.e("ESCPOS", "File write failed: " + e.toString());
        } 
         
    }
	private Boolean doPrintProcessing() {
		return false;
	}
	
	public static Boolean kickDrawer(Context context) {

        if(ReceiptSetting.make == ReceiptSetting.MAKE_ELOTOUCH && Build.MODEL.contains(Consts.ELO_MODEL)){
            new EloTouch(context).openDrawer();
            return true;
        }

		if (ReceiptSetting.make == ReceiptSetting.MAKE_STAR) {
			Typeface typeface = Typeface.MONOSPACE;

			String portName = ReceiptSetting.address;
			String portSettings = "";

			PrinterFunctions.OpenCashDrawer(context, portName, portSettings);

		
		} else if(ReceiptSetting.make == ReceiptSetting.MAKE_SNBC){
			
    		interface_usb = new POSUSBAPI(context);
    		
			error_code = interface_usb.OpenDevice();
			if(error_code != POS_SUCCESS) {
				return false;
			} else {
                pos_usb = new POSSDK(interface_usb);
				//interface_usb.WriteBuffer(PrinterCommands.KICK, 0, PrinterCommands.KICK.length, 2000);
                pos_usb.cashdrawerOpen(0,100,100);
                interface_usb.CloseDevice();
				return true;
			}
		
		} else if(ReceiptSetting.make == ReceiptSetting.MAKE_PT6210){
			opencash1();
			opencash2();
			return true;
		
		} else if (ReceiptSetting.make == ReceiptSetting.MAKE_ESCPOS || ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
		    try {
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN) {
		    		if(!(ReceiptSetting.address.length() > 6)) {
			    		return false;	
		    		}
		    		
					if(!InetAddress.getByName(ReceiptSetting.address).isReachable(2000)) {
						Log.v("USBPRINT", "FAILED");
			    		return false;
					}
					try {
				    	clientSocket = new Socket(ReceiptSetting.address, 9100);
				    	out = clientSocket.getOutputStream();
				    	
				    	out.write(PrinterCommands.INIT);
						
						
						if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
							out.write(PrinterCommands.CUSTOM_KICK);
						} else{
                            out.write(PrinterCommands.KICK);
						}
						
						out.close();
						return true;
					} catch (IOException connectException) {
			        	connectException.printStackTrace();
			            try {
			                out.close();
			            } catch (IOException closeException) {
			            	closeException.printStackTrace();
			            }
			            return false;
			        }
		    	}
		    	
		    	if (ReceiptSetting.type == ReceiptSetting.TYPE_BT) {
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (mBluetoothAdapter == null) {
					    return false;
					}
						
					if (!mBluetoothAdapter.isEnabled()) {
						return false;
					}
					
					BluetoothDevice BTdevice = null;
					
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
					// If there are paired devices
					if (pairedDevices.size() > 0) {
						for (BluetoothDevice device : pairedDevices) {
							if(device.getAddress().equals(ReceiptSetting.address)) {
								Log.v("BT Printer", "Found Printer: " + device.getAddress());
								BTdevice = device;
								
								if(!mmSocketMap.containsKey(device)) {
									Log.v("BT Printer", "New Printer: " + device.getAddress());
									mmSocketMap.put(device, null);
								}
							}
						}
					}
									
					mBluetoothAdapter.cancelDiscovery();
					
					if(BTdevice == null) {
						return false;
					}
						        
			        try {
			        	if (mmSocketMap.get(BTdevice) == null) {
			        		BluetoothSocket mmSocketin = BTdevice.createInsecureRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
			        		mmSocketin.connect();
			             	
			             	mmSocketMap.put(BTdevice, mmSocketin);
			             	mmSocket = mmSocketin;
			        	} else {
			        		mmSocket = mmSocketMap.get(BTdevice);
			        		
			        		if(!mmSocket.isConnected()) {
			        			mmSocket = BTdevice.createInsecureRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
			        			mmSocket.connect();
			        		}
			        	}
			        	
				    	out = mmSocket.getOutputStream();
				    	
				    	out.write(PrinterCommands.INIT);
						
						if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
						    out.write(PrinterCommands.CUSTOM_KICK);
						} else {
                            out.write(PrinterCommands.KICK);
						}

						return true;
			        } catch (IOException connectException) {
			        	connectException.printStackTrace();
			            try {
			                if(mmSocket != null && mmSocket.isConnected()) mmSocket.close();
			                if(out != null) out.close();
			                mmSocket = null;
			            } catch (IOException closeException) {
			            	closeException.printStackTrace();
			            }
			            return false;
			        }
		    	}
		    	
		    	if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
		    	    UsbDevice printer = null;
		    	    UsbManager mUsbManager = null;

		    		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		    	    
		    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    	    while (deviceIterator.hasNext()) {
		    	    	UsbDevice device = deviceIterator.next();

		    	    	if(device.getDeviceClass() == UsbConstants.USB_CLASS_PRINTER) {
		    	    		printer = device;
		    	    		break;
		    	    	}
		    		        
		    	    	if(device.getInterfaceCount() > 0) {
		    	    		for(int p = 0; p < device.getInterfaceCount(); p++) {
		    	    			if(device.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
		    	    				printer = device;
		    	    				break;
		    	    			}
		    	    		}
		    	    	}   		        
		    	    }
		    	    
		    	    if (printer != null) {
		    	    	if (!mUsbManager.hasPermission(printer)) {
			    			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			context.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(printer, mPermissionIntent);
			    	    	return false;
			    	    	
		    	    	} else {
		    	    		UsbInterface intf = null;
			    	    	UsbEndpoint endpoint= null;
			    	    	
		    	    		for (int p = 0; p < printer.getInterfaceCount(); p++) {
		    	    			if (printer.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
		    	    				intf = printer.getInterface(p);
		    	    				break;
		    	    			}
		    	    		}
		    	    		
		    	    		if (intf != null){
			    	    		for (int p = 0; p < intf.getEndpointCount(); p++) {
			    	    			if (intf.getEndpoint(p).getDirection() == UsbConstants.USB_DIR_OUT) {
			    	    				endpoint = intf.getEndpoint(p);
			    	    				break;
			    	    			}
			    	    		}
		    	    		} else {
		    	    			return false;
		    	    		}
						    	    	
			    	    	if (endpoint != null) {
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(printer);
				    	    	if (connection != null) {
					    	    	connection.claimInterface(intf, true);
					    	    	connection.bulkTransfer(endpoint, PrinterCommands.INIT, PrinterCommands.INIT.length, 500); //do in another thread
					    	    	
					    	    	//String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
									//string = string.replaceAll("[^\\p{ASCII}]", "");
					    	    	
					    	    	//connection.bulkTransfer(endpoint, string.getBytes(), string.getBytes().length, 500); //do in another thread
					    	    	
									if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {

						    	    	connection.bulkTransfer(endpoint, PrinterCommands.CUSTOM_KICK, PrinterCommands.CUSTOM_KICK.length, 500); //do in another thread
									} else {
						    	    	connection.bulkTransfer(endpoint, PrinterCommands.KICK, PrinterCommands.KICK.length, 500); //do in another thread
									}
				    	    	} else {
				    	    		return false;
				    	    	}
			    	    	} else {
			    	    		return false;
			    	    	}
		    	    	} 
		    	    	
		    	    	return true;
		    	    } else {
		    	    	return false;
		    	    }
		    	}
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		}
		return false;
    }
	
	private static boolean processPrintReceipt(int printReceiptOption, BigDecimal subTotalAmount) {
		
		switch (printReceiptOption) {
			
		case 1:
			return true;
		case 2:
			if (subTotalAmount.compareTo(new BigDecimal("1000")) >= 0)
				return true;
			else 
				return false;
		case 3:
			if (subTotalAmount.compareTo(new BigDecimal("2000")) >= 0)
				return true;
			else 
				return false;
		case 4:
			if (subTotalAmount.compareTo(new BigDecimal("3000")) >= 0)
				return true;
			else 
				return false;	
		}
		return true;
	}

    private static boolean openDrawer(ReportCart cart, Context context){
        if(cart.mPayments.size() > 1)
            return true;
        for (Payment payment : cart.mPayments){
            if(payment.paymentType.equals(PaymentActivity.PAYMENT_TYPE_CREDIT)){
                return PrefUtils.openCashDrawer(context);
            }
        }

        return true;
    }
}
