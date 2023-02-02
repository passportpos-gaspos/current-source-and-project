package com.passportsingle;

public class AdminSetting {
	public static boolean enabled;
	public static String password = "";
	public static String hint = "";
	protected static String override = "2003";
	
	public static boolean isEnabled() {
		return enabled;
	}

	public static void clear() {
		enabled = false;
		password = "";
		hint = "";
	}
}
