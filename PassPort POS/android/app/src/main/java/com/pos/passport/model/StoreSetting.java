package com.pos.passport.model;

public  class StoreSetting {

    public static int BACK_OFFICE_HEADER = 1;
    public static int TERMINAL_HEADER = 2;

	private static String name;
	private static String address1;
	private static String address2;
	private static String phone;
	private static String email;
	private static String website;
	private static String currency;
	private static String receipt_header = "";
	private static String receipt_footer = "";
	public static boolean enabled;
	private static String city;
	private static String state;
	//functionality is removed in latest code 
	//public static boolean clearSale = false;
	public static boolean clearSale = true;
	public static boolean capture_sig = true;
	public static boolean print_sig = false;
	public static int header_type = BACK_OFFICE_HEADER;

	public static void setName(String name) {
		StoreSetting.name = name;
	}
	public static String getName() {
		return name;
	}

	public static String getAddress1() {
		return address1;
	}

	public static void setAddress1(String address) {
		StoreSetting.address1 = address;
	}
	
	public static String getAddress2() {
		return address2;
	}

	public static void setAddress2(String address) {
		StoreSetting.address2 = address;
	}

	public static String getPhone() {
		return phone;
	}

	public static void setPhone(String phone) {
		StoreSetting.phone = phone;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		StoreSetting.email = email;
	}

	public static String getWebsite() {
		return website;
	}

	public static void setWebsite(String website) {
		StoreSetting.website = website;
	}

	public static String getCurrency() {
		return currency;
	}

	public static void setCurrency(String currency) {
		StoreSetting.currency = currency;
	}

	public static String getCity() {
		return city;
	}

	public static void setCity(String city) {
		StoreSetting.city = city;
	}

	public static String getState() {
		return state;
	}

	public static void setState(String state) {
		StoreSetting.state = state;
	}

	public static void clear() {
		StoreSetting.name = "";
		StoreSetting.setAddress1("");
		StoreSetting.setPhone("");
		StoreSetting.setEmail("");
		StoreSetting.setWebsite("");
		StoreSetting.setReceipt_footer("");
		StoreSetting.setReceipt_header("");
		StoreSetting.setCurrency("$");
		StoreSetting.setCity("");
		StoreSetting.setState("");
	}
	
	public static String getReceipt_footer() {
		return receipt_footer;
	}
	public static void setReceipt_footer(String receipt_footer) {
		StoreSetting.receipt_footer = receipt_footer;
	}
	public static String getReceipt_header() {
		return receipt_header;
	}
	public static void setReceipt_header(String receipt_header) {
		StoreSetting.receipt_header = receipt_header;
	}
	

}
