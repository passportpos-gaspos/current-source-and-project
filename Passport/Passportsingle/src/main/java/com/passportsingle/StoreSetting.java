package com.passportsingle;

public  class StoreSetting {

	private static String name;
	private static String address;
	private static String phone;
	private static String email;
	private static String website;
	private static String currency;
	public static boolean enabled;
	public static boolean clearSale = true;
	
	public static void setName(String name) {
		StoreSetting.name = name;
	}
	public static String getName() {
		return name;
	}

	public static String getAddress() {
		return address;
	}

	public static void setAddress(String address) {
		StoreSetting.address = address;
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

	public static void clear() {
		StoreSetting.name = "";
		StoreSetting.setAddress("");
		StoreSetting.setPhone("");
		StoreSetting.setEmail("");
		StoreSetting.setWebsite("");
		StoreSetting.setCurrency("$");
	}
	

}
