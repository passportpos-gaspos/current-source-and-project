package com.passportsingle;

public class PrioritySetting {
	
	public static String mWSURL = "https://api.mxmerchant.com/checkout/v3/payment?echo=true"; // Production
	
	//public static String mWSURL = "https://sandbox.mxmerchant.com/checkout/v3/payment?echo=true"; //test
	
	public static String mWSURL_VOID = "https://api.mxmerchant.com/checkout/v3/payment/{id}?force=true";
	
	public static String mMANURL = "https://prioritypos.azurewebsites.net/mhc/open.php"; // Production
	//public static String mMANURL = "https://advantageedge.azurewebsites.net/mhc_test/open.php"; // Development
	
	public static String mVOIDURL = "https://prioritypos.azurewebsites.net/mhc/voidsale.php"; // Production
	//public static String mVOIDURL = "https://advantageedge.azurewebsites.net/mhc_test/voidsale.php"; // Development
	
	//public static String mGWSURL = "https://g1.mercurypay.com/ws/ws.asmx"; // Production
		
	//public static String mGVOIDURL = "https://advantageedge.azurewebsites.net/mhc/voidsale.php"; // Production
	public static String mGVOIDURL = "https://prioritypos.azurewebsites.net/mhc_test/voidsale.php"; // Development

	
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

