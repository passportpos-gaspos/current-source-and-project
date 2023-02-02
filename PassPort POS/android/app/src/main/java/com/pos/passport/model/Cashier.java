package com.pos.passport.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Cashier implements Serializable {
	
	public String name;
	public String email="";
	public int sales = 0;
	public int returns = 0;
	public BigDecimal total = BigDecimal.ZERO;
	public int id;
	public String pin = "";
	public boolean deleted = false;
	public boolean permissionReturn = false;
	public boolean permissionPriceModify = false;
	public boolean permissionReports = false;
	public boolean permissionInventory = false;
	public boolean permissionSettings = false;
	public boolean permissionVoideSale = false;
	public boolean permissionProcessTender = false;


	public int admin=0;

	public String toString(){
		return name;
	}

	public Cashier clone() {
        Cashier cashier = new Cashier();
        cashier.name = this.name;
        cashier.email = this.email;
        cashier.sales = this.sales;
        cashier.returns = this.returns;
        cashier.total = this.total;
        cashier.id = this.id;
        cashier.pin = this.pin;
        cashier.deleted = this.deleted;
        cashier.permissionReturn = this.permissionReturn;
        cashier.permissionPriceModify = this.permissionPriceModify;
        cashier.permissionReports = this.permissionReports;
        cashier.permissionInventory = this.permissionInventory;
        cashier.permissionSettings = this.permissionSettings;
		cashier.permissionVoideSale = this.permissionVoideSale;
		cashier.permissionProcessTender = this.permissionProcessTender;

        return cashier;
    }
}
