package com.passportsingle;

public class TaxSetting {

	private static String tax1name;
	private static String tax2name;
	private static String tax3name;

	private static float tax1;
	private static float tax2;
	private static float tax3;

	public TaxSetting(){
		TaxSetting.tax1name = null;
		TaxSetting.tax2name = null;
		TaxSetting.tax3name = null;

		TaxSetting.tax1 = 0;
		TaxSetting.tax2 = 0;
		TaxSetting.tax3 = 0;

	}
		
	public static void setTax1name(String tax1name) {
		TaxSetting.tax1name = tax1name;
	}
	public static String getTax1name() {
		return tax1name;
	}
	public static void setTax2name(String tax2name) {
		TaxSetting.tax2name = tax2name;
	}
	public static String getTax2name() {
		return tax2name;
	}
	public static void setTax1(float tax1) {
		TaxSetting.tax1 = tax1;
	}
	public static float getTax1() {
		return tax1;
	}
	public static void setTax2(float tax2) {
		TaxSetting.tax2 = tax2;
	}
	public static float getTax2() {
		return tax2;
	}

	public static void clear() {
		TaxSetting.tax1name = null;
		TaxSetting.tax2name = null;
		TaxSetting.tax3name = null;

		TaxSetting.tax1 = 0;
		TaxSetting.tax2 = 0;
		TaxSetting.tax3 = 0;
	}

	public static String getTax3name() {
		return tax3name;
	}

	public static void setTax3name(String tax3name) {
		TaxSetting.tax3name = tax3name;
	}

	public static float getTax3() {
		return tax3;
	}

	public static void setTax3(float tax3) {
		TaxSetting.tax3 = tax3;
	}
	
}
