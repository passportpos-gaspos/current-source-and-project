package com.pos.passport.model;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Shift {

	public int id;
	public long start;
	public long end;
	public String note;
	
	public ArrayList<String> voidString = new ArrayList<>();
	public ArrayList<TotalsList> voids = new ArrayList<>();

	public ArrayList<TotalsList> totals = new ArrayList<>();
	public ArrayList<String> departments = new ArrayList<>();

	public ArrayList<String> tenders = new ArrayList<>();
	public ArrayList<TotalsList> tenderstotals = new ArrayList<>();

	public ArrayList<String> taxes = new ArrayList<>();
	public ArrayList<TotalsList> taxtotals = new ArrayList<>();

	public ArrayList<ReportCart> carts;
	public BigDecimal total;
	public BigDecimal subTotal;
	public BigDecimal nonDiscountTotal;
	public ArrayList<String> cashiers = new ArrayList<>();
	public ArrayList<TotalsList> cashierstotals = new ArrayList<>();

}
