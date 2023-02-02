package com.pos.passport.model;

public class TaxSetting {

	private static String tax1Name;
	private static String tax2Name;
	private static String tax3Name;

	private static float tax1;
	private static float tax2;
	private static float tax3;

	 String taxname;
	 int taxId;
	float taxpercent;


	public TaxSetting(){
		TaxSetting.tax1Name = null;
		TaxSetting.tax2Name = null;
		TaxSetting.tax3Name = null;

		TaxSetting.tax1 = 0;
		TaxSetting.tax2 = 0;
		TaxSetting.tax3 = 0;

		this.taxname=null;
		this.taxId=0;
		this.taxpercent=0;
	}
	public TaxSetting(String taxname,int taxId,float taxpercent){

		this.taxname=taxname;
		this.taxId=taxId;
		this.taxpercent=taxpercent;
	}
	public static void setTax1Name(String tax1Name) {
		TaxSetting.tax1Name = tax1Name;
	}
	public static String getTax1Name() {
		return tax1Name;
	}
	public static void setTax2Name(String tax2Name) {
		TaxSetting.tax2Name = tax2Name;
	}
	public static String getTax2Name() {
		return tax2Name;
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
		TaxSetting.tax1Name = null;
		TaxSetting.tax2Name = null;
		TaxSetting.tax3Name = null;

		TaxSetting.tax1 = 0;
		TaxSetting.tax2 = 0;
		TaxSetting.tax3 = 0;

		/*TaxSetting.taxname=null;
		TaxSetting.taxId=0;
		TaxSetting.taxpercent=0;*/
	}

	public static String getTax3Name() {
		return tax3Name;
	}

	public static void setTax3Name(String tax3Name) {
		TaxSetting.tax3Name = tax3Name;
	}

	public static float getTax3() {
		return tax3;
	}

	public static void setTax3(float tax3) {
		TaxSetting.tax3 = tax3;
	}

	public String getTaxname() {
		return taxname;
	}

	public void setTaxname(String taxname) {
		this.taxname = taxname;
	}

	public int getTaxId() {
		return taxId;
	}

	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}

	public float getTaxpercent() {
		return taxpercent;
	}

	public void setTaxpercent(float taxpercent) {
		this.taxpercent = taxpercent;
	}

	/*public static String getTaxname() {
		return taxname;
	}

	public static void setTaxname(String taxname) {
		this.taxname = taxname;
	}

	public static int getTaxId() {
		return taxId;
	}

	public static void setTaxId(int taxId) {
		TaxSetting.taxId = taxId;
	}

	public static float getTaxpercent() {
		return taxpercent;
	}

	public static void setTaxpercent(float taxpercent) {
		TaxSetting.taxpercent = taxpercent;
	}*/
}
