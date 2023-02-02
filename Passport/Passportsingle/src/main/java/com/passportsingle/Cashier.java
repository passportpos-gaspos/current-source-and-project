package com.passportsingle;

public class Cashier {
	
	public String name;
	public String email;
	public int sales = 0;
	public int returns = 0;
	public float total = 0.0f;
	public int id;
	public String pin = "";

	public boolean permissionReturn = false;
	public boolean permissionPriceModify = false;
	public boolean permissionReports = false;
	public boolean permissionInventory = false;
	public boolean permissionSettings = false;

	public String toString(){
		return name;
	}
}
