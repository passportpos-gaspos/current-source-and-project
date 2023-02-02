package com.pos.passport.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Customer implements Serializable {

	public String fName;
	public String lName ="";
	public String email;
	public String phone="";
	public int sales = 0;
	public int returns = 0;
	public BigDecimal total = BigDecimal.ZERO;
	public int id;
	public int cid=0;

	public void setLName(String lName) {
		this.lName = lName;
	}

	public void setFName(String fName){
		this.fName = fName;
	}

	public void setEmail(String email) {
		this.email = email; 
	}

	public void setId(int at) {
		this.id = at;
	}

	public int getId(){
		return this.id;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getFullName() {
        return String.format("%s %s", fName, lName);
    }

	public Customer clone() {
        Customer customer = new Customer();
        customer.lName = this.lName;
		customer.fName = this.fName;
        customer.email = this.email;
        customer.sales = this.sales;
        customer.returns = this.returns;
        customer.total = this.total;
        customer.id = this.id;
		customer.phone = this.phone;
		customer.cid = this.cid;

        return customer;
    }
}
