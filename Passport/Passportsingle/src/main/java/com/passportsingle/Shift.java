package com.passportsingle;

import java.util.ArrayList;

public class Shift {

	public int id;
	public long start;
	public long end;
	public String note;
	
	public ArrayList<String> voidString = new ArrayList<String>();
	public ArrayList<TotalsList> voids = new ArrayList<TotalsList>();

	public ArrayList<TotalsList> totals = new ArrayList<TotalsList>();
	public ArrayList<String> departments = new ArrayList<String>();

	public ArrayList<String> tenders = new ArrayList<String>();
	public ArrayList<TotalsList> tenderstotals = new ArrayList<TotalsList>();

	public ArrayList<String> taxes = new ArrayList<String>();
	public ArrayList<TotalsList> taxtotals = new ArrayList<TotalsList>();

	public ArrayList<ReportCart> carts;
	public long total;
	public long subTotal;
	public long nonDiscountTotal;
	public ArrayList<String> cashiers = new ArrayList<String>();
	public ArrayList<TotalsList> cashierstotals = new ArrayList<TotalsList>();

}
