package com.elotouch.paypoint.register.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class PrinterAPI 
{
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;

	/**
	 * Set Printer to assigned SerialPort
	 * 
	 * @param serialPort 
	 */
	public PrinterAPI(SerialPort serialPort) {
		mSerialPort = serialPort;
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
	}

	/***
	 * Print & Line Feed
	 */
	public boolean print_feed()
	{
		boolean isPrinted = true;
		try
		{
			if(!isPaperAvailable())
			{
				isPrinted = false;
			}
			ArrayList<Byte> byteList = new ArrayList<Byte>();
			for(int i=0;i<=10;i++){
				byteList.add(Byte.valueOf((byte)10));
			}
			sendCommand(byteList);
			mInputStream.close();
			mOutputStream.close();
			return isPrinted;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return isPrinted;
		}
		
	}

	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray)
	{
		byte[] byteArray = new byte[ByteArray.size()];
		for(int index = 0; index < byteArray.length; index++)
		{
			byteArray[index] = ByteArray.get(index);
		}
		ByteArray.clear();
		ByteArray = null;
		return byteArray;
	}

	/***
	 * Send Command To Printer
	 * @param byteList
	 */
	public void sendCommand(ArrayList<Byte> byteList)
	{
		byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);

		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e) {e.printStackTrace(); }

		if (mSerialPort != null) 
		{
			try {
				if (mOutputStream != null) {
					mOutputStream.write(commandToSendToPrinter, 0, commandToSendToPrinter.length);
					mOutputStream.flush();
				} else {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		commandToSendToPrinter = null;
	}

	private ArrayList<Byte> toByteArray(String string)
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		if (string!=null) {
			for (byte byt : string.getBytes()) 
			{
				list.add(Byte.valueOf(byt));
			}
		}
		// line feed
		list.add(Byte.valueOf((byte) 0x0A));
		list.add(Byte.valueOf((byte) 0x0D));
		return list;
	}
	private ArrayList<Byte> addBytesToList(byte[] b)
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (int i=0; i<b.length;i++) {
			list.add(b[i]);
		}
		// line feed
		list.add(Byte.valueOf((byte) 0x0A));
		list.add(Byte.valueOf((byte) 0x0D));
		return list;
	}

	public ArrayList<Byte> PrintLF(int lines)
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));//Print & Line Feed
		list.add(Byte.valueOf((byte) 100));
		list.add(Byte.valueOf((byte) 2));

		list.add(Byte.valueOf((byte) 13));

		return list;

	}
	public ArrayList<Byte> Print()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 10));
		list.add(Byte.valueOf((byte) 13));
		return list;
	}

	/***
	 * Left
	 * @return
	 */
	public ArrayList<Byte> AlignLeft()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 97));
		list.add(Byte.valueOf((byte) 0));
		return list;
	}

	/***
	 * Center
	 * @return
	 */
	public ArrayList<Byte> AlignCenter()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 97));
		list.add(Byte.valueOf((byte) 1));
		return list;
	}
	/***
	 * Right
	 * @return
	 */
	public ArrayList<Byte> AlignRight()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 97));
		list.add(Byte.valueOf((byte) 2));
		return list;
	}
	/***
	 * Bold
	 * @return
	 */
	public ArrayList<Byte> SetBold()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 69));
		list.add(Byte.valueOf((byte) 1));
		return list;
	}
	/***
	 * Set Language
	 * @return
	 */
	public ArrayList<Byte> SetLanguage(int langNo)
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 82));
		list.add(Byte.valueOf((byte) langNo));
		return list;
	}
	/***
	 * Set Chinese Mode
	 * @return
	 */
	public ArrayList<Byte> SetChineseMode()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 28));
		list.add(Byte.valueOf((byte) 38));
		return list;
	}
	/**
	 * Normal
	 * @return
	 */
	public ArrayList<Byte> SetNormal()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 27));
		list.add(Byte.valueOf((byte) 69));
		list.add(Byte.valueOf((byte) 0));
		return list;
	}
	/**
	 * Bar Code
	 * @return
	 */
	public ArrayList<Byte> setBarcode()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();

		return list;
	}
	/***
	 * Print english
	 * @param
	 */
	public boolean print_test()
	{
		boolean isPrinted = true;
		try
		{
			if(!isPaperAvailable())
			{
				isPrinted =  false;
			}
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();

			Calendar c = Calendar.getInstance();
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);

			String timeString = timeFormat.format(c.getTime());
			String dateString = dateFormat.format(c.getTime());

			ArrayList<Byte> printReceipt = new ArrayList<Byte>();

			printReceipt.addAll(SetLanguage(0));
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(SetBold());
			printReceipt.addAll(toByteArray("Hello World - Bold   !!!"));
			printReceipt.addAll(SetNormal());
			printReceipt.addAll(toByteArray("Hello World - Normal !!!"));
			printReceipt.addAll(toByteArray("Date: "+dateString));
			printReceipt.addAll(toByteArray("Time: "+timeString));
			printReceipt.addAll(toByteArray("------------------------------"));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(toByteArray("Hello - Center"));
			printReceipt.addAll(AlignLeft());
			printReceipt.addAll(toByteArray("Hello - Left"));
			printReceipt.addAll(AlignRight());
			printReceipt.addAll(toByteArray("Hello - Right"));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(GetBarCode("12345"));
			printReceipt.addAll(toByteArray("12345"));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			sendCommand(printReceipt);
			mInputStream.close();
			mOutputStream.close();
			return isPrinted;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return isPrinted;
		}
		

	}
	
	/***
	 * Print Chinese - Traditional 
	 * @param
	 */
	public boolean print_traditional_chinese_test()
	{
		boolean isPrinted = true;
		try
		{
			if(!isPaperAvailable())
			{
				isPrinted =  false;
			}
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();

			Calendar c = Calendar.getInstance();
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);

			String timeString = timeFormat.format(c.getTime());
			String dateString = dateFormat.format(c.getTime());

			ArrayList<Byte> printReceipt = new ArrayList<Byte>();
				
			String s = "輸入您的選擇"; // Traditional Chinese
			
			byte[] s_bytes = s.getBytes("GBK");
						
			printReceipt.addAll(SetLanguage(15));
			printReceipt.addAll(SetChineseMode());			
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(SetBold());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(SetNormal());
			printReceipt.addAll(addBytesToList(s_bytes));
			
			printReceipt.addAll(toByteArray("Date: "+dateString));
			printReceipt.addAll(toByteArray("Time: "+timeString));
			printReceipt.addAll(toByteArray("------------------------------"));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(AlignLeft());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(AlignRight());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(GetBarCode("12345"));
			printReceipt.addAll(toByteArray("12345"));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			sendCommand(printReceipt);
			mInputStream.close();
			mOutputStream.close();
			return isPrinted;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return isPrinted;
		}
	}
	/***
	 * Print Chinese - Simplified 
	 * @param
	 */
	public boolean print_simplified_chinese_test()
	{
		boolean isPrinted = true;
		try
		{
			if(!isPaperAvailable())
			{
				isPrinted =  false;
			}
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();

			Calendar c = Calendar.getInstance();
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);

			String timeString = timeFormat.format(c.getTime());
			String dateString = dateFormat.format(c.getTime());

			ArrayList<Byte> printReceipt = new ArrayList<Byte>();
			
			String s = "输入您的选择"; // Simplied Chinese - Mainland
			
			byte[] s_bytes = s.getBytes("GBK");
			
			printReceipt.addAll(SetLanguage(15));
			printReceipt.addAll(SetChineseMode());			
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(SetBold());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(SetNormal());
			printReceipt.addAll(addBytesToList(s_bytes));
			
			printReceipt.addAll(toByteArray("Date: "+dateString));
			printReceipt.addAll(toByteArray("Time: "+timeString));
			printReceipt.addAll(toByteArray("------------------------------"));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(AlignLeft());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(AlignRight());
			printReceipt.addAll(addBytesToList(s_bytes));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(GetBarCode("12345"));
			printReceipt.addAll(toByteArray("12345"));
			printReceipt.addAll(addLines(2));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			sendCommand(printReceipt);
			mInputStream.close();
			mOutputStream.close();
			return isPrinted;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return isPrinted;
		}
	}
	
	/***
	 * Print Barcode 
	 * @param Code
	 */
	public ArrayList<Byte> GetBarCode(String Code)
	{

		String barcode = "A"+Code+"B";
		byte[] codeData =  barcode.getBytes();
		ArrayList<Byte> command = new ArrayList<Byte>();

		//Barcode Width
		command.add(Byte.valueOf((byte) 0x1D));
		command.add(Byte.valueOf((byte) 0x77));
		command.add(Byte.valueOf((byte) 0x03));

		//Height
		command.add(Byte.valueOf((byte) 0x1D));
		command.add(Byte.valueOf((byte) 0x68));
		command.add(Byte.valueOf((byte) 0x32));

		//Position
		command.add(Byte.valueOf((byte) 0x1D));
		command.add(Byte.valueOf((byte) 0x48));
		command.add(Byte.valueOf((byte) 0x00));

		//Barcode Type CODABAR
		command.add(Byte.valueOf((byte) 0x1D));
		command.add(Byte.valueOf((byte) 0x6B));
		command.add(Byte.valueOf((byte) 0x06));
		for (byte byt : codeData) {
			command.add(Byte.valueOf(byt));
		}
		command.add(Byte.valueOf((byte) 0x00));
		return command;

	}
	public ArrayList<Byte> LineFeed()
	{
		ArrayList<Byte> list = new ArrayList<Byte>();
		list.add(Byte.valueOf((byte) 0x0A));
		list.add(Byte.valueOf((byte) 0x0D));
		return list;

	}
	
	public ArrayList<Byte> addLines(int lines) {
		ArrayList<Byte> list = new ArrayList<Byte>();

		list.add(Byte.valueOf((byte) 27));//Print & Line Feed
		list.add(Byte.valueOf((byte) 100));
		list.add(Byte.valueOf((byte) lines));
		list.add(Byte.valueOf((byte) 0x0A));
		list.add(Byte.valueOf((byte) 0x0D));

		return list;
	}
	
	/***
	 * setFontSize (height,width)
	 * 
	 * Height Value =>  0,1,2,3,4,5,6,7
	 * Width  Value =>  0,16,32,48,64,80,96,112
	 */
	public ArrayList<Byte> SetFontSize(int height, int width) {
        Byte hVal = (byte) (0xFF & height);
        Byte wVal  = (byte) (0xFF & width);
 
        // First 4 bits height & Second 4 bits for width
        Byte val = (byte) (wVal|hVal);
 
        ArrayList<Byte> list = new ArrayList<Byte>();
        list.add(Byte.valueOf((byte) 29));
        list.add(Byte.valueOf((byte) 33));
        list.add(Byte.valueOf((byte) val));
        return list;
	}     

	/*
	 * Checks Availability of paper in Printer
	 */

	public boolean isPaperAvailable() throws IOException
	{
		ArrayList<Byte> byteArray = new ArrayList<Byte>();
		byteArray.add(Byte.valueOf((byte) 0x10));
		byteArray.add(Byte.valueOf((byte) 0x04));
		byteArray.add(Byte.valueOf((byte) 0x04));
		sendCommand(byteArray);
		String result = Integer.toHexString(mInputStream.read());
		return result.contains("12");
	}
	
	
	/***
	 * print_image(Context, Image Resource Id)
	 * @param void
	 */
	public boolean print_image(Context context, int resId) {
		boolean isPrinted = true;
		try {
			if(!isPaperAvailable()) {
				isPrinted =  false;
			}
			ArrayList<Byte> printReceipt = new ArrayList<Byte>();
			printReceipt.addAll(SetLanguage(0));
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(printImage(context,resId));
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(AlignCenter());
			printReceipt.addAll(SetFontSize(1,16));
			printReceipt.addAll(toByteArray("Set Font Size !!"));
			printReceipt.addAll(SetFontSize(0,0));
			printReceipt.addAll(AlignLeft());
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			printReceipt.addAll(LineFeed());
			sendCommand(printReceipt);
			mInputStream.close();
			mOutputStream.close();
			return isPrinted;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return isPrinted;
		}
	}
		
	public ArrayList<Byte> printImage(Context context, int resId) {
		ArrayList<Byte> list = new ArrayList<Byte>();
		// Set the Image Resource Id as argument
		int[][] pixels = getPixelsArray(context,resId);
		// Get Byte Array
		list = getPrintImageBytes(pixels);
		return list;
	}
	
	private int[][] getPixelsArray(Context context, int resId) {
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),resId);
        int[][] result = new int[bmp.getWidth()][bmp.getHeight()];  
	    for (int row = 0; row < bmp.getWidth(); row++) {
	        for (int col = 0; col < bmp.getHeight(); col++) {
	            result[row][col] = bmp.getPixel(row, col);
	        }
	    }
	    return result;
	}
	
	private ArrayList<Byte> getPrintImageBytes(int[][] pixels) {
		final char ESC_CHAR = 0x1B;
		final byte[] LINE_FEED = new byte[]{0x0A};
		final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
		final byte[] SET_LINE_SPACE_24 = new byte[]{ESC_CHAR, 0x33, 24};
		final byte[] SET_LINE_SPACE_30 = new byte[]{ESC_CHAR, 0x33, 30};
		
		ArrayList<Byte> list = new ArrayList<Byte>();
		
		for (byte b : SET_LINE_SPACE_24) {
			list.add(Byte.valueOf((byte) b));
		}
		
		for (int y = 0; y < pixels.length; y += 24) {
			// Set Image Mode
			for (byte b : SELECT_BIT_IMAGE_MODE) {
				list.add(Byte.valueOf((byte) b));
			}
			
			// Set Pixel Length
			byte[] nLnH = new byte[] {
					(byte) (0x00ff & pixels[y].length),
					(byte) ((0xff00 & pixels[y].length) >> 8) };
			
			for (byte b : nLnH) {
				list.add(Byte.valueOf((byte) b));
			}

			// Set Horizontal pixel
			for (int x = 0; x < pixels[y].length; x++) {
				byte[] sliceArray = recollectSlice(y, x, pixels);
				for (byte b : sliceArray) {
					list.add(Byte.valueOf((byte) b));
				}
			}

			// Go to next line
			for (byte b : LINE_FEED) {
				list.add(Byte.valueOf((byte) b));
			}
			
		}
		
		for (byte b : SET_LINE_SPACE_30) {
			list.add(Byte.valueOf((byte) b));
		}
		
		return list;
	}

	private byte[] recollectSlice(int y, int x, int[][] img) {
		byte[] slices = new byte[] { 0, 0, 0 };
		for (int yy = y, i = 0; yy < y + 24 && i < 3; yy += 8, i++) {
			byte slice = 0;
			for (int b = 0; b < 8; b++) {
				int yyy = yy + b;
				if (yyy >= img.length) {
					continue;
				}
				int col = img[yyy][x];
				boolean v = shouldPrintColor(col);
				slice |= (byte) ((v ? 1 : 0) << (7 - b));
			}
			slices[i] = slice;
		}
		return slices;
	}

	private boolean shouldPrintColor(int col) {
		final int threshold = 127;
		int a, r, g, b, luminance;
		a = (col >> 24) & 0xff;
		if (a != 0xff) {// Ignore transparencies
			return false;
		}
		r = (col >> 16) & 0xff;
		g = (col >> 8) & 0xff;
		b = col & 0xff;
		luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
		return luminance < threshold;
	}
}