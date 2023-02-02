package com.passportsingle;

public class TotalsList {

	private String department;
	private long total;
	
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getDepartment() {
		return department;
	}
	public void setTotal(long l) {
		this.total = l;
	}
	public long getTotal() {
		return total;
	}
	
	public void addTotal(long itemTotal){
		this.total = this.total + itemTotal;
	}
	
}
