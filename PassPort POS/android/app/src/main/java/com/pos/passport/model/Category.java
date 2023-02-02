package com.pos.passport.model;

import java.io.Serializable;

public class Category implements Serializable {

	private String name;
	private boolean taxable1;
	private boolean taxable2;
	private boolean taxable3;
	public boolean deleted;
	public int tax;
	public String taxarray;
	
	private int id;
	private int numofprods=0;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(int products) {
		this.id = products;
	}

	public int getId() {
		return id;
	}

	public void setNumofprods(int numofprods) {
		this.numofprods = numofprods;
	}

	public int getNumofprods() {
		return numofprods;
	}

	public void setTaxable1(boolean b) {
		this.taxable1 = b;
	}

	public boolean getTaxable1() {
		return taxable1;
	}

	public void setTaxable2(boolean taxable2) {
		this.taxable2 = taxable2;
	}

	public boolean getTaxable2() {
		return taxable2;
	}

	public boolean getTaxable3() {
		return taxable3;
	}

	public void setTaxable3(boolean taxable3) {
		this.taxable3 = taxable3;
	}

	public int getTax() {
		return tax;
	}

	public void setTax(int tax) {
		this.tax = tax;
	}

	public String getTaxarray() {
		return taxarray;
	}

	public void setTaxarray(String taxarray) {
		this.taxarray = taxarray;
	}
}
