package com.pos.passport.model;

import com.pos.passport.util.UrlProvider;

public class WebSetting {
	
	  public static String Amount;
	  public static String TransType;
	  public static boolean enabled;
	  public static String hostedMID = "";
	  public static String hostedPass;
	  public static String mMANURL;
	  public static String mVOIDURL;
	  public static String mWSURL = "https://gateway-sb.clearent.net/rest/v2/transactions";
	  public static String merchantID;
	  public static String terminalName;
	  public static String webServicePassword;

	  static {
	    mMANURL = UrlProvider.BASE_URL + "/mhc/open.php";
	    mVOIDURL = UrlProvider.BASE_URL + "/mhc/voidsale.php";
	    merchantID = "";
	    webServicePassword = "";
	    TransType = "";
	    Amount = "";
	    terminalName = "";
	    hostedPass = "";
	  }
	  
	  public static void clear() {
	    enabled = true;
	    merchantID = "";
	    webServicePassword = "";
	    terminalName = "";
	    TransType = "";
	    Amount = "";
	    hostedPass = "";
	    hostedMID = "";
	  }
}
