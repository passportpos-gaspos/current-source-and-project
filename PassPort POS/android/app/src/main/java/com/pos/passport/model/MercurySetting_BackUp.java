package com.pos.passport.model;

import com.pos.passport.util.UrlProvider;

public class MercurySetting_BackUp {
	
	public static String mWSURL = "https://w1.mercurypay.com/ws/ws.asmx"; // Production
	//public static String mWSURL = "https://w1.mercurydev.net/ws/ws.asmx"; // Development
	
	public static String mMANURL = UrlProvider.BASE_URL + "/mhc/open.php"; // Production
	//public static String mMANURL = ProductDatabase.HomeURL+"/mhc_test/open.php"; // Development
	
	public static String mVOIDURL = UrlProvider.BASE_URL + "/mhc/voidsale.php"; // Production
	//public static String mVOIDURL = ProductDatabase.HomeURL+"/mhc_test/voidsale.php"; // Development

	public static boolean enabled;
	public static String merchantID = "";
	public static String webServicePassword = "";
	public static String terminalName = "";
	public static String hostedPass = "";
	public static String hostedMID = "";
	
	public static void clear() {
		enabled = false;
		merchantID = "";
		webServicePassword = "";
		terminalName = ""; 
		hostedPass = "";
		hostedMID = ""; 
	}
}

