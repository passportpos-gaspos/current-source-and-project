package com.pos.passport.model;

import android.content.Context;

import com.pos.passport.R;

public class AdminSetting {
	public static boolean enabled;
	public static String password = "0";
	public static String userid = "0";
	public static String hint = "";
	protected static String override = "2003";

	public static boolean isEnabled() {
		return enabled;
	}

	public static void clear() {
		enabled = false;
		password = "0";
		hint = "";
		userid="";
	}

	public static Cashier getAdminPermissions(Context context)
	{
        Cashier cashier = new Cashier();
        cashier.name = context.getString(R.string.txt_administrator);
        cashier.pin = "1234";
	    cashier.permissionInventory = true;
        cashier.permissionPriceModify = true;
        cashier.permissionReports = true;
        cashier.permissionReturn = true;
        cashier.permissionSettings = true;

        return cashier;
    }
}
