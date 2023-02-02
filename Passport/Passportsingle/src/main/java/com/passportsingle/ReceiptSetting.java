package com.passportsingle;

public class ReceiptSetting {
		
	public static final int MAKE_STAR = 1;
	public static final int MAKE_CUSTOM = 2;
	public static final int MAKE_ESCPOS = 3;
	public static final int MAKE_SNBC = 4;
	public static final int MAKE_PT6210 = 5;
	
	public static final int TYPE_LAN = 1;
	public static final int TYPE_USB = 2;
	public static final int TYPE_BT = 3;
	
	public static final int SIZE_2 = 1;
	public static final int SIZE_3 = 2;

	public static boolean enabled;
	public static String blurb = "";
	public static String address = "";
	public static String name = "";
	public static int type = 0;
	public static int make = 0;
	public static int size = 0;
	public static boolean drawer = false;
	public static boolean display = false;
	
	public static String printerName;
	public static String printerModel;
	public static String drawerCode;
	public static String cutCode;
	
	public static int selectedPrinterIndex = 0;
	public static int selectedModelIndex = 0;

	
	public static void clear() {
		enabled = false;
		blurb = "";
		address = "";
		name = "";
		type = 0;
		make = 0;
		size = 0;
		drawer = false;
		display = false;
	}
}
