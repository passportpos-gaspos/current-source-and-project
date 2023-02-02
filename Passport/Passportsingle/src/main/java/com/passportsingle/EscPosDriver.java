package com.passportsingle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

//import com.StarMicronics.StarIOSDK.PrinterFunctions;
//import com.StarMicronics.StarIOSDK.PrinterFunctions.RasterCommand;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;
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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

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
	private static final String ACTION_USB_PERMISSION =
		    "com.android.example.USB_PERMISSION";
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
    
    public static boolean SendToDisplay(String DisplayText)
    {
    	String string = Normalizer.normalize(DisplayText,
				Normalizer.Form.NFD);
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

			if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
				if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
					UsbManager mUsbManager = null;
					if(display == null)
		    	    {
			    		mUsbManager = (UsbManager) PointOfSale.me.getSystemService(Context.USB_SERVICE);
			    	    
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
					
		    	    if(display != null)
		    	    {
			    		mUsbManager = (UsbManager) PointOfSale.me.getSystemService(Context.USB_SERVICE);

		    	    	if(!mUsbManager.hasPermission(display))
		    	    	{
			    			mPermissionIntent = PendingIntent.getBroadcast(PointOfSale.me, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			PointOfSale.me.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(display, mPermissionIntent);
			    	    	return false;
			    	    	
		    	    	}else{ 	    	 
		    	    		
		    	    		UsbInterface intf = null;
			    	    	UsbEndpoint endpoint= null;
			    	    	
		    	    		for(int p = 0; p < display.getInterfaceCount(); p++)
		    	    		{
		    	    			if(display.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    			{
		    	    				intf = display.getInterface(p);
		    	    				break;
		    	    			}
		    	    		}
		    	    		
		    	    		if(intf != null){
			    	    		for(int p = 0; p < intf.getEndpointCount(); p++)
			    	    		{
			    	    			if(intf.getEndpoint(p).getDirection() == UsbConstants.USB_DIR_OUT)
			    	    			{
			    	    				endpoint = intf.getEndpoint(p);
			    	    				break;
			    	    			}
			    	    		}
		    	    		}else{
		    	    			return false;
		    	    		}
							    	    	
			    	    	if(endpoint != null)
			    	    	{			    	    	
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(display); 
				    	    	if(connection != null) 
				    	    	{
					    	    	connection.claimInterface(intf, true);					    	    	
					    	    	connection.bulkTransfer(endpoint, ai3, ai3.length, 1000); //do in another thread
				    	    	}else{
				    	    		return false;
				    	    	}
			    	    	}
		    	    	} 
		    	    	
		    	    	return true;
		    	    }else{
		    	    	return false;
		    	    }
				}
			 else if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
	    	{
	    		if(!(ReceiptSetting.address.length() > 6))
	    		{
		    		return false;	
	    		}
	    		
				if(!InetAddress.getByName(ReceiptSetting.address).isReachable(2000))
				{
					Log.v("USBPRINT", "FAILED");
		    		return false;
				}
				
		    	clientSocket = new Socket(ReceiptSetting.address, 9100);
		    	out = clientSocket.getOutputStream();
		    	
		    	try {											
				    out.write(ai3);
	    		} catch (IOException closeException) {
	            	closeException.printStackTrace();
	            }
	    		
		    	out.close();		    		
		    			    	
		    	return true;
	    	}
			}
		} catch (Exception localException) {
			return false;
		}

		return true;
    }

	public static boolean Print(String printString)
    {    
		if(ReceiptSetting.make == ReceiptSetting.MAKE_STAR)
		{
			Typeface typeface = Typeface.MONOSPACE;

			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.BLACK);
			paint.setAntiAlias(true);
			paint.setTypeface(typeface);
			paint.setTextSize(22);
			TextPaint textpaint = new TextPaint(paint);

			android.text.StaticLayout staticLayout = new StaticLayout(printString.toString(),
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
						
			Context context;
			//PrinterFunctions.PrintBitmap(PointOfSale.me, portName, portSettings, bitmap, 576, true, false);
			//PrinterFunctions.PrintBitmap(PointOfSale.me, portName, portSettings, bitmap, 576, false, RasterCommand.Standard);
										
		
		}else if(ReceiptSetting.make == ReceiptSetting.MAKE_SNBC){	
			
    		interface_usb = new POSUSBAPI(PointOfSale.me);
    		
			error_code = interface_usb.OpenDevice();
			if(error_code != POS_SUCCESS)
			{
				return false;
			}
			else
			{
				pos_usb = new POSSDK(interface_usb);
				sdk_flag = true;
				String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
				string = string.replaceAll("[^\\p{ASCII}]", "");
				pos_usb.textPrint(string.getBytes(), string.getBytes().length);
				pos_usb.systemCutPaper(66, 0);
				interface_usb.WriteBuffer(PrinterCommands.KICK, 0, PrinterCommands.KICK.length, 2000);
				//pos_usb.cashdrawerOpen(CashdrawerID, PulseOnTimes, PulseOffTimes)
				interface_usb.CloseDevice();
				//Toast.makeText(USBActivity.this, "Open Port OK!.", Toast.LENGTH_LONG).show();
				return true;
			}
		
		}else if(ReceiptSetting.make == ReceiptSetting.MAKE_PT6210){	
			
			String string = Normalizer.normalize(printString,
										Normalizer.Form.NFD);
			string = string.replaceAll("[^\\p{ASCII}]", "");
						
			writeToFile(PTPRINTER,string);
						
			opencash1();
			opencash2();
																					
			return true;
		
		}else if(ReceiptSetting.make == ReceiptSetting.MAKE_ESCPOS || ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM){	
		    try {
		    			    	
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
		    	{
		    		if(!(ReceiptSetting.address.length() > 6))
		    		{
			    		return false;	
		    		}
		    		
					if(!InetAddress.getByName(ReceiptSetting.address).isReachable(2000))
					{
						Log.v("USBPRINT", "FAILED");
			    		return false;
					}
					
			    	clientSocket = new Socket(ReceiptSetting.address, 9100);
			    	out = clientSocket.getOutputStream();
		    	}
		    	
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
		    	{
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
							if(device.getAddress().equals(ReceiptSetting.address))
							{
								Log.v("BT Printer", "Found Printer: " + device.getAddress());
								BTdevice = device;
							}
						}
					}
									
					mBluetoothAdapter.cancelDiscovery();
					
					if(BTdevice == null)
					{
						return false;
					}
						        
			        try {
			        	if(mmSocket == null || !mmSocket.isConnected())
			        	{
			        		mmSocket = BTdevice.createRfcommSocketToServiceRecord(BTdevice.getUuids()[0].getUuid());
			             	mmSocket.connect();
			        	}
				    	out = mmSocket.getOutputStream();
			        } catch (IOException connectException) {
			            // Unable to connect; close the socket and get out
			        	connectException.printStackTrace();
			            try {
			            	if(mmSocket != null)
			            		mmSocket.close();
			            } catch (IOException closeException) {
			            	closeException.printStackTrace();
			            }
			            return false;
			        }
		    	}
		    	
		    	if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
		    	{				
		    	    UsbDevice printer = null;
		    	    UsbManager mUsbManager = null;

		    		mUsbManager = (UsbManager) PointOfSale.me.getSystemService(Context.USB_SERVICE);
		    	    
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
		    	    
		    	    if(printer != null)
		    	    {
		    	    	if(!mUsbManager.hasPermission(printer))
		    	    	{
			    			mPermissionIntent = PendingIntent.getBroadcast(PointOfSale.me, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			PointOfSale.me.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(printer, mPermissionIntent);
			    	    	return false;
			    	    	
		    	    	}else{ 	    	 
		    	    		
		    	    		UsbInterface intf = null;
			    	    	UsbEndpoint endpoint= null;
			    	    	
		    	    		for(int p = 0; p < printer.getInterfaceCount(); p++)
		    	    		{
		    	    			if(printer.getInterface(p).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER)
		    	    			{
		    	    				intf = printer.getInterface(p);
		    	    				break;
		    	    			}
		    	    		}
		    	    		
		    	    		if(intf != null){
			    	    		for(int p = 0; p < intf.getEndpointCount(); p++)
			    	    		{
			    	    			if(intf.getEndpoint(p).getDirection() == UsbConstants.USB_DIR_OUT)
			    	    			{
			    	    				endpoint = intf.getEndpoint(p);
			    	    				break;
			    	    			}
			    	    		}
		    	    		}else{
		    	    			return false;
		    	    		}
		    	    		
			    	    	if(endpoint != null)
			    	    	{			    	    	
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(printer); 
				    	    	if(connection != null) 
				    	    	{
					    	    	connection.claimInterface(intf, true);
					    	    	connection.bulkTransfer(endpoint, PrinterCommands.INIT, PrinterCommands.INIT.length, 500); //do in another thread
					    	    	
					    	    	String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
									string = string.replaceAll("[^\\p{ASCII}]", "");
					    	    	
					    	    	connection.bulkTransfer(endpoint, string.getBytes(), string.getBytes().length, 500); //do in another thread
					    	    	
					    	    	byte [] kickCode ;
					    	    	
					    	    	if( ReceiptSetting.drawerCode != null){
					    	    		String[] splitCodes = (ReceiptSetting.drawerCode).split(",");
					    	    		kickCode = new byte[splitCodes.length];
					    	    		int i = 0;
					    	    		for(String code: splitCodes){
					    	    			kickCode[i] = new Byte(code.trim());
					    	    			i++;
					    	    		}
					    	    	}else {
										
					    	    		kickCode = PrinterCommands.KICK;
									}
					    	    	
									if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
									{
						    	    	connection.bulkTransfer(endpoint, PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT, PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT.length, 500); //do in another thread
						    	    	connection.bulkTransfer(endpoint, PrinterCommands.CUSTOM_KICK, PrinterCommands.CUSTOM_KICK.length, 500); //do in another thread
									}else{
						    	    	connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER, PrinterCommands.FEED_PAPER.length, 500); //do in another thread
						    	    	connection.bulkTransfer(endpoint, PrinterCommands.FEED_PAPER_AND_CUT, PrinterCommands.FEED_PAPER_AND_CUT.length, 500); //do in another thread
						    	    	connection.bulkTransfer(endpoint, kickCode, PrinterCommands.KICK.length, 500); //do in another thread
									}
				    	    	}else{
				    	    		return false;
				    	    	}
			    	    	}else{
			    	    		return false;
			    	    	}
		    	    	} 
		    	    	
		    	    	return true;
		    	    }else{
		    	    	return false;
		    	    }
		    	}else{
		    		try {
						out.write(PrinterCommands.INIT);
						
						String string = Normalizer.normalize(printString, Normalizer.Form.NFD);
						string = string.replaceAll("[^\\p{ASCII}]", "");
												
					    out.write(string.getBytes("ASCII"));
						
						if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
						{
						    out.write(PrinterCommands.CUSTOM_FEED_PAPER_AND_CUT);
						    out.write(PrinterCommands.CUSTOM_KICK);
						}else{
							out.write(PrinterCommands.FEED_PAPER);
						    out.write(PrinterCommands.FEED_PAPER_AND_CUT);
						    out.write(PrinterCommands.KICK);
						}
		    		} catch (IOException closeException) {
		            	closeException.printStackTrace();
		            }
		    		
			    	if(ReceiptSetting.type != ReceiptSetting.TYPE_BT)
			    	{
			    		out.close();		    		
			    	}
			    	
			    	return true;
		    	}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		return false;
    }	
	
	public static void closeBTConnections()
	{		
		try {
			if(mmSocket != null && mmSocket.isConnected())
			{
				mmSocket.close();
				mmSocket = null;
			}
        } catch (IOException closeException) {
        	closeException.printStackTrace();
        }
	}
	
	public static boolean PrintReceipt(ShopCart cart)
    {    	
    	DecimalFormat nf = new DecimalFormat("0.00");
		nf.setGroupingUsed(false);
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	
		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
		
		StringBuilder receiptString = new StringBuilder();

		//------------Store Name----------------------
		if(!(StoreSetting.getName().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
		}
		
		//------------Store Address----------------------
		if(!(StoreSetting.getAddress().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress(), cols+1)).append('\n');
		}

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
		
		//-------------------Date------------------------	

		String date = DateFormat.getDateTimeInstance().format(new Date(cart.date));
		receiptString.append('\n');
		receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
		receiptString.append(EscPosDriver.wordWrap("Transaction: " + ProductDatabase.getSaleNumber(), cols+1)).append('\n');
		receiptString.append('\n');

		if(cart.Cashier != null)
		{
			if(cart.Cashier.name.equals("Training"))
			{
				StringBuffer message = null;
				if(cols == 40)
					message = new StringBuffer("--------------- TRAINING ---------------".substring(0, cols));					

				else
					message = new StringBuffer("---------- TRAINING ----------".substring(0, cols));					
			
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
			}else{
				receiptString.append(EscPosDriver.wordWrap("Cashier: " + cart.Cashier.name, cols+1)).append('\n');
				receiptString.append('\n');
			}
		}

		//----------------Customer Name/Email-------------------
		if(cart.Customer != null)
		{
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerName(), cols+1)).append('\n');
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerEmail(), cols+1)).append('\n');
			receiptString.append('\n');
		}	
		
		if (cart.voided) {
            
			StringBuffer message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		//------------------Products-----------------------------------
		
		long nonDiscountTotal = 0;

		for (int o = 0; o < cart.getProducts().size(); o++) {

			long price = cart.getProducts().get(o).itemPrice(cart.date);
			nonDiscountTotal += cart.getProducts().get(o).itemNonDiscountTotal(cart.date);
			
			receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).name, cols+1)).append('\n');
			if(!cart.getProducts().get(o).isNote)
			{
				if(!cart.getProducts().get(o).barcode.isEmpty())
				{
					receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).barcode, cols+1)).append('\n');
				}
				
				StringBuffer message = new StringBuffer("                                        ".substring(0, cols));	
				String quan = cart.getProducts().get(o).quantity + " @ "+StoreSetting.getCurrency() + nf.format(price/100f);
				message.replace(0, quan.length(), quan);	
				
				price = cart.getProducts().get(o).itemTotal(cart.date);
				Product item = cart.getProducts().get(o);
				
				String TotalPrice = "";
				
				if (item.cat != 0) {
					String cat = ProductDatabase.getCatById(item.cat);
					int catPos = ProductDatabase.getCatagoryString().indexOf(cat);
	
					if (catPos > -1) {
						if (ProductDatabase.getCatagories().get(catPos).getTaxable1() || ProductDatabase.getCatagories().get(catPos).getTaxable2()) {
							TotalPrice = StoreSetting.getCurrency() + nf.format(price/100f)+"T";
						}else{
							TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
						}
					}else{
						TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
					}
				}else{
					TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
				}
				message.replace(message.length()-TotalPrice.length(), cols-1, TotalPrice);	
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			receiptString.append('\n');
		}
		
		if(cart.subtotaldiscount > 0)
		{
			StringBuffer message = new StringBuffer("Discount:                               ".substring(0, cols));					
					
			String discountS = (int)cart.subtotaldiscount+"%";
			message.replace(11, 11+discountS.length(), discountS);	
							
			discountS = StoreSetting.getCurrency() + nf.format((cart.subTotal-nonDiscountTotal)/100f);
			message.replace(message.length()-discountS.length(), cols-1, discountS);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		StringBuffer message = new StringBuffer("Sub Total                               ".substring(0, cols));					
					
		String subprice = StoreSetting.getCurrency() + nf.format(cart.subTotal/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

		if (cart.taxName1 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			String discountS = cart.taxName1 + " " + cart.taxPercent1 + "%";
			message.replace(6, 6+discountS.length(), discountS);	
							
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax1/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}

		if (cart.taxName2 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			message.replace(6, 6+(cart.taxName2 + " " + cart.taxPercent2 + "%").length(), cart.taxName2 + " " + cart.taxPercent2 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax2/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		if (cart.taxName3 != null) {
            
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			message.replace(6, 6+(cart.taxName3 + " " + cart.taxPercent3 + "%").length(), cart.taxName3 + " " + cart.taxPercent3 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax3/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		///----------------------
		message = new StringBuffer("Total                                   ".substring(0, cols));					
					
		subprice = StoreSetting.getCurrency() + nf.format(cart.total/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

		///----------------------
		
		if (cart.voided) {
            
			message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		if(cart.Cashier != null)
		{
			if(cart.Cashier.name.equals("Training"))
			{
				if(cols == 40)
					message = new StringBuffer("--------------- TRAINING ---------------".substring(0, cols));					

				else
					message = new StringBuffer("---------- TRAINING ----------".substring(0, cols));					
			
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
			}
		}

		long paymentSum = 0;
		for(int p = 0; p < cart.Payments.size(); p++)
		{
			paymentSum += cart.Payments.get(p).paymentAmount;

			///----------------------
			message = new StringBuffer("Tender Type:                            ".substring(0, cols));					
						
			message.replace(13, 13+cart.Payments.get(p).paymentType.length(), cart.Payments.get(p).paymentType);	
			
			subprice = StoreSetting.getCurrency() + nf.format(cart.Payments.get(p).paymentAmount/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			if((cart.Payments.get(p).InvoiceNo).length() > 1){
				
				message = new StringBuffer("Invoice Number: #                       ".substring(0, cols));
				message.replace(17, 17+cart.Payments.get(p).InvoiceNo.length(), cart.Payments.get(p).InvoiceNo);
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			///----------------------
		}
		
		receiptString.append('\n');

		if(paymentSum > cart.total)
		{
			///----------------------
			message = new StringBuffer("Customer Change:                        ".substring(0, cols));					
										
			subprice = StoreSetting.getCurrency() + nf.format((paymentSum-cart.total)/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

			///----------------------
		}
		
		if(!ReceiptSetting.blurb.equals(""))
		{
			receiptString.append(EscPosDriver.wordWrap(ReceiptSetting.blurb, cols+1)).append('\n').append('\n');
		}

		return Print(receiptString.toString());
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
	
	public static boolean SendToPT6210Display(String DisplayText)
    {
		String string = Normalizer.normalize(DisplayText,
				Normalizer.Form.NFD);
		String message = string.replaceAll("[^\\p{ASCII}]", "");
		
		byte[] ai = { 27, (byte) 178, 68 };
		byte[] ai1 = { 27, (byte) 178, 80 };
		byte[] ai2 = { 12 };

		try {
			byte ai3[] = new byte[ai.length + ai2.length
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

			//if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM) {
			//	if (ReceiptSetting.type == ReceiptSetting.TYPE_USB) {
					UsbManager mUsbManager = null;
					if(display == null)
		    	    {
			    		mUsbManager = (UsbManager) PointOfSale.me.getSystemService(Context.USB_SERVICE);
			    	    
			    	    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			    	    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			    	    while(deviceIterator.hasNext()){
			    	    	UsbDevice device = deviceIterator.next();	    		        
	
			    	    	if(device.getDeviceClass() == 0xa)
			    	    	{
			    	    		display = device;
			    	    		break;
			    	    	}
			    		        
			    	    	if(device.getInterfaceCount() > 0)
			    	    	{
			    	    		for(int p = 0; p < device.getInterfaceCount(); p++)
			    	    		{
			    	    			if(device.getInterface(p).getInterfaceClass() == 0xa)
			    	    			{
			    	    				display = device;
			    	    				break;
			    	    			}
			    	    		}
			    	    	}   		        
			    	    }
		    	    }
					
		    	    if(display != null)
		    	    {
			    		mUsbManager = (UsbManager) PointOfSale.me.getSystemService(Context.USB_SERVICE);

		    	    	if(!mUsbManager.hasPermission(display))
		    	    	{
			    			mPermissionIntent = PendingIntent.getBroadcast(PointOfSale.me, 0, new Intent(ACTION_USB_PERMISSION), 0);
			    			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			    			PointOfSale.me.registerReceiver(mUsbReceiver, filter);
			    			
			    	    	mUsbManager.requestPermission(display, mPermissionIntent);
			    	    	return false;
			    	    	
		    	    	}else{ 	    	 
			    	    	UsbInterface intf = display.getInterface(1);
			    	    	UsbEndpoint endpoint = intf.getEndpoint(0);
						    	    	
			    	    	if(endpoint != null)
			    	    	{			    	    	
				    	    	UsbDeviceConnection connection = mUsbManager.openDevice(display); 
				    	    	if(connection != null) 
				    	    	{
					    	    	connection.claimInterface(intf, true);					    	    	
					    	    	connection.bulkTransfer(endpoint, ai3, ai3.length, 500); //do in another thread
				    	    	}else{
				    	    		return false;
				    	    	}
			    	    	}
		    	    	} 
		    	    	
		    	    	return true;
		    	    }else{
		    	    	return false;
		    	    }
			//	}
			//}
		} catch (Exception localException) {
			return false;
		}
    }
    
	public static boolean PrintReceipt(ReportCart cart)
    {    	
    	DecimalFormat nf = new DecimalFormat("0.00");
		nf.setGroupingUsed(false);
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	
		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
    	
		StringBuilder receiptString = new StringBuilder();

		//------------Store Name----------------------
		if(!(StoreSetting.getName().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
		}
		
		//------------Store Address----------------------
		if(!(StoreSetting.getAddress().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress(), cols+1)).append('\n');
		}

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
		
		//-------------------Date------------------------	

		String date = DateFormat.getDateTimeInstance().format(new Date(cart.date));
		receiptString.append('\n');
		receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
		receiptString.append(EscPosDriver.wordWrap("Transaction: " + cart.trans, cols+1)).append('\n');
		receiptString.append('\n'); 

		if(cart.cashier != null)
		{
			receiptString.append(EscPosDriver.wordWrap("Cashier: " + cart.cashier.name, cols+1)).append('\n');
			receiptString.append('\n');
		}

		//----------------Customer Name/Email-------------------
		if(cart.Customer != null)
		{
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerName(), cols+1)).append('\n');
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerEmail(), cols+1)).append('\n');
			receiptString.append('\n');
		}	
		
		//------------------Products-----------------------------------
		
		if (cart.voided) {
            
			StringBuffer message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		long nonDiscountTotal = 0;

		for (int o = 0; o < cart.getProducts().size(); o++) {

			long price = cart.getProducts().get(o).itemPrice(cart.date);
			nonDiscountTotal += cart.getProducts().get(o).itemNonDiscountTotal(cart.date);
			
			receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).name, cols+1)).append('\n');
			
			if(!cart.getProducts().get(o).isNote)
			{
				if(!cart.getProducts().get(o).barcode.isEmpty())
				{
					receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).barcode, cols+1)).append('\n');
				}
				
				StringBuffer message = new StringBuffer("                                        ".substring(0, cols));	
	
				String quan = cart.getProducts().get(o).quantity + " @ "+StoreSetting.getCurrency() + nf.format(price/100f);
				message.replace(0, quan.length(), quan);	
				
				price = cart.getProducts().get(o).itemTotal(cart.date);
				Product item = cart.getProducts().get(o);
				
				String TotalPrice = "";
				
				if (item.cat != 0) {
					String cat = ProductDatabase.getCatById(item.cat);
					int catPos = ProductDatabase.getCatagoryString().indexOf(cat);
	
					if (catPos > -1) {
						if (ProductDatabase.getCatagories().get(catPos).getTaxable1() || ProductDatabase.getCatagories().get(catPos).getTaxable2()) {
							TotalPrice = StoreSetting.getCurrency() + nf.format(price/100f)+"T";
						}else{
							TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
						}
					}else{
						TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
					}
				}else{
					TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
				}
				message.replace(message.length()-TotalPrice.length(), cols-1, TotalPrice);	
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			receiptString.append('\n');
		}
		
		if(cart.subtotaldiscount > 0)
		{
			StringBuffer message = new StringBuffer("Discount:                               ".substring(0, cols));					

			String discountS = (int)cart.subtotaldiscount+"%";
			message.replace(11, 11+discountS.length(), discountS);	
							
			discountS = StoreSetting.getCurrency() + nf.format((cart.subTotal-nonDiscountTotal)/100f);
			message.replace(message.length()-discountS.length(), cols-1, discountS);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		StringBuffer message = new StringBuffer("Sub Total                               ".substring(0, cols));					
					
		String subprice = StoreSetting.getCurrency() + nf.format(cart.subTotal/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

		if (cart.taxName1 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
			
			String discountS = cart.taxName1 + " " + cart.taxPercent1 + "%";
			message.replace(6, 6+discountS.length(), discountS);	
							
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax1/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}

		if (cart.taxName2 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					

			message.replace(6, 6+(cart.taxName2 + " " + cart.taxPercent2 + "%").length(), cart.taxName2 + " " + cart.taxPercent2 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax2/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		if (cart.taxName3 != null) {
            
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					

			message.replace(6, 6+(cart.taxName3 + " " + cart.taxPercent3 + "%").length(), cart.taxName3 + " " + cart.taxPercent3 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax3/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		///----------------------
		message = new StringBuffer("Total                                   ".substring(0, cols));					

		subprice = StoreSetting.getCurrency() + nf.format(cart.total/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

		///----------------------

		if (cart.voided) {
            
			message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		long paymentSum = 0;
		for(int p = 0; p < cart.Payments.size(); p++)
		{
			paymentSum += cart.Payments.get(p).paymentAmount;

			///----------------------
			message = new StringBuffer("Tender Type:                            ".substring(0, cols));					

			message.replace(13, 13+cart.Payments.get(p).paymentType.length(), cart.Payments.get(p).paymentType);	
			
			subprice = StoreSetting.getCurrency() + nf.format(cart.Payments.get(p).paymentAmount/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			///----------------------
		}
		
		receiptString.append('\n');

		if(paymentSum > cart.total)
		{
			///----------------------
			message = new StringBuffer("Customer Change:                        ".substring(0, cols));					

			subprice = StoreSetting.getCurrency() + nf.format((paymentSum-cart.total)/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

			///----------------------
		}
		
		if(!ReceiptSetting.blurb.equals(""))
		{
			receiptString.append(EscPosDriver.wordWrap(ReceiptSetting.blurb, cols+1)).append('\n').append('\n');
		}

		return Print(receiptString.toString());
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
    
	private static void opencash1()
	{
//		writeToFile(GPIOEXPORT,CASH1CGPIO);
		writeToFile(CASH1CDIR,CASHOUT);
		writeToFile(CASH1CONTROL,CASHTH);
		writeToFile(CASH1CONTROL,CASHTL);
		writeToFile(CASH1CONTROL,CASHTH);
	}
	private static void opencash2()
	{
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
        	output.flush();
        	output.close();
        }
        catch (IOException e) {
            Log.e("ESCPOS", "File write failed: " + e.toString());
        } 
         
    }
}
