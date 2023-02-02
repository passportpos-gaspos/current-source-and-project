package com.pos.passport.model;

import java.math.BigDecimal;

public class TotalsList {

	private String department;
	private BigDecimal total;
	
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDepartment() {
		return department;
	}
	public void setTotal(BigDecimal l) {
		this.total = l;
	}
	public BigDecimal getTotal() {
		return total;
	}

	public void addTotal(BigDecimal itemTotal){
		this.total = this.total.add(itemTotal);
	}
	
}
