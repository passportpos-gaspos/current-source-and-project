package com.passportsingle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.passportsingle.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SummaryFragment extends Fragment {
	
	private NumberFormat nf = NumberFormat.getInstance();
	private TableLayout tl;
	private ArrayList<ReportCart> carts; 
	
	private ArrayList<String> voidString = new ArrayList<String>();
	private ArrayList<TotalsList> voids = new ArrayList<TotalsList>();
	
	private ArrayList<TotalsList> totals = new ArrayList<TotalsList>();
	private ArrayList<String> departments = new ArrayList<String>();

	private ArrayList<String> tenders = new ArrayList<String>();
	private ArrayList<TotalsList> tenderstotals = new ArrayList<TotalsList>();

	private ArrayList<String> taxes = new ArrayList<String>();
	private ArrayList<TotalsList> taxtotals = new ArrayList<TotalsList>();
	
	private ArrayList<String> cashiers = new ArrayList<String>();
	private ArrayList<TotalsList> cashierstotals = new ArrayList<TotalsList>();

	private long  total;
	private long fromD;
	private long toD;
	private long subTotal;
	private long nonDiscountTotal;
	private Spinner mPersonName;
	protected Cashier AdminSend;
	protected ProgressDialog pd;
	private AlertDialog adminReportDialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Summary Fragment");
		
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}

	protected void buildReport(long l, long m) {
		carts = ProductDatabase.getReports1(l, m);

		voidString = new ArrayList<String>();
		voids = new ArrayList<TotalsList>();
		
		totals = new ArrayList<TotalsList>();
		departments = new ArrayList<String>();

		tenders = new ArrayList<String>();
		tenderstotals = new ArrayList<TotalsList>();

		taxes = new ArrayList<String>();
		taxtotals = new ArrayList<TotalsList>();
		
		cashiers = new ArrayList<String>();
		cashierstotals = new ArrayList<TotalsList>();
		
		total = 0;
		subTotal = 0;
		nonDiscountTotal = 0;

		tl.removeAllViews();
		if (carts.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());

			tv1.setText("Departments");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			TableRow row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 5, 0, 5);
			row.addView(tv1);

			tv2.setText("Amount");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			long totalAmountSum = 0;

			TotalsList newtotalf = new TotalsList();
			newtotalf.setDepartment("No Cashier");
			int Totalf = 0;
			newtotalf.setTotal(Totalf);
			cashierstotals.add(newtotalf);
			cashiers.add("No Cashier");

			for (int i = 0; i < carts.size(); i++) {
				if(!carts.get(i).voided)
				{
					for (int o = 0; o < carts.get(i).getProducts().size(); o++) {
						Product prod = carts.get(i).getProducts().get(o);
						String test;
						if(!prod.isNote)
						{
							nonDiscountTotal += prod.itemNonDiscountTotal(carts.get(i).date);
							if (prod.cat != 0) {
								test = ProductDatabase.getCatById(prod.cat);
								if (test == null || test.equals("")) {
									test = "No Department";
								}
							} else {
								test = "No Department";
							}
							if (departments.contains(test)) {
								//float subTotal = carts.get(i).getQuantities().get(o)
								//		* (prod.getPrice() - prod.getPrice()
								//				* (prod.getDiscount() / 100));
								
								long itemTotal = prod.itemTotal(carts.get(i).date);
								
								totals.get(departments.indexOf(test)).addTotal(itemTotal);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(test);
								//float subTotal = carts.get(i).getQuantities().get(o)
								//		* (prod.getPrice() - prod.getPrice()
								//				* (prod.getDiscount() / 100));
								long itemTotal = prod.itemTotal(carts.get(i).date);
		
								newtotal.setTotal(itemTotal);
								totals.add(newtotal);
								departments.add(test);
							}
						}
					}
	
					ArrayList<Payment> Payments = carts.get(i).Payments;
					long amountSum = 0;
					String test;
					for(int p = 0; p < Payments.size(); p++)
					{
						test = Payments.get(p).paymentType;
						amountSum += Payments.get(p).paymentAmount;
						
						if (tenders.contains(test)) {
							tenderstotals.get(tenders.indexOf(test)).addTotal(Payments.get(p).paymentAmount);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(test);
							newtotal.setTotal(Payments.get(p).paymentAmount);
							tenderstotals.add(newtotal);
							tenders.add(test);
						}
					}
					
					totalAmountSum += amountSum;
							
					test = carts.get(i).getTaxName1();
					if (test != null && !test.equals("")) {
						if (taxes.contains(test)) {
							taxtotals.get(taxes.indexOf(test)).addTotal(
									carts.get(i).tax1);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(test);
							newtotal.setTotal(carts.get(i).tax1);
							taxtotals.add(newtotal);
							taxes.add(test);
						}
					}
	
					test = carts.get(i).getTaxName2();
					if (test != null && !test.equals("")) {
						if (taxes.contains(test)) {
							taxtotals.get(taxes.indexOf(test)).addTotal(
									carts.get(i).tax2);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(test);
							newtotal.setTotal(carts.get(i).tax2);
							taxtotals.add(newtotal);
							taxes.add(test);
						}
					}
					
					test = carts.get(i).getTaxName3();
					if (test != null && !test.equals("")) {
						if (taxes.contains(test)) {
							taxtotals.get(taxes.indexOf(test)).addTotal(
									carts.get(i).tax3);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(test);
							newtotal.setTotal(carts.get(i).tax3);
							taxtotals.add(newtotal);
							taxes.add(test);
						}
					}
						
					total = total + carts.get(i).total;
					subTotal = subTotal + carts.get(i).subTotal;

					String name;
					if(carts.get(i).cashier != null)
						name = carts.get(i).cashier.name;
					else
						name = "";
	
					if(!name.equals("")){
						
						if (cashiers.contains(name)) {
							long Total = carts.get(i).total;
							cashierstotals.get(cashiers.indexOf(name)).addTotal(Total);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(name);
							long Total = carts.get(i).total;
							newtotal.setTotal(Total);
							cashierstotals.add(newtotal);
							cashiers.add(name);
						}
						
					}else{
						
						long Total = carts.get(i).total;
						cashierstotals.get(cashiers.indexOf("No Cashier")).addTotal(Total);	
					}
				}else{
					String test = "Voids";
					
					if (voidString.contains(test)) {
						voids.get(voidString.indexOf(test)).addTotal(carts.get(i).total);
					} else {
						TotalsList newtotal = new TotalsList();
						newtotal.setDepartment(test);
						newtotal.setTotal(carts.get(i).total);
						voids.add(newtotal);
						voidString.add(test);
					}
				}
			}
			
			if(totalAmountSum > total)
			{
				String change = "Cash";
				long changeAmount = total-totalAmountSum;
						
				if (tenders.contains(change)) {
					tenderstotals.get(tenders.indexOf(change)).addTotal(changeAmount);
				} else {
					TotalsList newtotal = new TotalsList();
					newtotal.setDepartment(change);
					newtotal.setTotal(changeAmount);
					tenderstotals.add(newtotal);
					tenders.add(change);
				}
			}

			android.widget.TableRow.LayoutParams layoutParams;
			for (int i = 0; i < totals.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(totals.get(i).getDepartment());
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(totals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}
			
			if (subTotal-nonDiscountTotal < 0) {
				row = new TableRow(getActivity());
				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
				row.setPadding(0, 5, 0, 0);
				
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				
				tv1.setText("Discounts:");
				tv1.setGravity(Gravity.LEFT);
				tv1.setSingleLine(true);

				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setTypeface(null, Typeface.BOLD);

				tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(tv1);
				
				tv2.setText(StoreSetting.getCurrency() + nf.format((subTotal-nonDiscountTotal)/100f));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setSingleLine(true);

				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
			}
			
			if(taxtotals.size() > 0) {
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
	
				tv1.setText("Tax Groups");
				tv1.setGravity(Gravity.LEFT);
				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setTypeface(null, Typeface.BOLD);
	
				tv1.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
	
				row = new TableRow(getActivity());
	
				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setPadding(0, 5, 0, 0);
				row.addView(tv1);
	
				tv2.setText("Amount");
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);
	
				tv2.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv2);
	
				for (int i = 0; i < taxtotals.size(); i++) {
					row = new TableRow(getActivity());
					TextView tv = new TextView(getActivity());
					row.setPadding(5, -3, 0, 0);
					tl.addView(row);
					layoutParams = new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					row.setLayoutParams(layoutParams);
	
					tv.setText(taxtotals.get(i).getDepartment());
					tv.setGravity(Gravity.LEFT);
					tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
	
					tv.setLayoutParams(new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.addView(tv);
	
					tv = new TextView(getActivity());
					tv.setText(StoreSetting.getCurrency()  + nf.format(taxtotals.get(i).getTotal()/100f));
					tv.setGravity(Gravity.RIGHT);
					tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
	
					tv.setLayoutParams(new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.addView(tv);
				}
			}
			
			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());

			tv1.setText("Total");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 5, 0, 0);
			row.addView(tv1);
			tv2.setText(StoreSetting.getCurrency()  + nf.format(total/100f));
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			row = new TableRow(getActivity());
			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());

			tv1.setText("Tendered Types");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			tl.addView(row);
			layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			row.setLayoutParams(layoutParams);
			row.setPadding(0, 5, 0, 0);
			row.addView(tv1);

			tv2.setText("Amount");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			for (int i = 0; i < tenders.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(tenders.get(i));
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(tenderstotals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}
			
			row = new TableRow(getActivity());
			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());

			tv1.setText("Cashiers");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			tl.addView(row);
			layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			row.setLayoutParams(layoutParams);
			row.setPadding(0, 5, 0, 0);
			row.addView(tv1);

			tv2.setText("Amount");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			for (int i = 0; i < cashiers.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(cashiers.get(i));
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(cashierstotals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}

			if(voids.size() > 0)
			{
				row = new TableRow(getActivity());
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());

				tv1.setText("Voids Total");
				tv1.setGravity(Gravity.LEFT);
				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setTypeface(null, Typeface.BOLD);
				tv1.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);
				row.setPadding(0, 5, 0, 5);
				row.addView(tv1);
				
				tv2.setText(StoreSetting.getCurrency()  + nf.format(voids.get(0).getTotal()/100f));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);
				tv2.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv2);
			}
		}
		
	}
	
	private void addLine(TableLayout tl) {
		TableRow line = new TableRow(getActivity());

		tl.addView(line);
		LayoutParams layoutParams = new TableRow.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		line.setLayoutParams(layoutParams);
		line.setPadding(5, 0, 5, 1);
		line.setBackgroundColor(Color.BLACK);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reportsview, container, false);
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	public static String Join(ArrayList<String> coll, String delimiter) {
		if(coll == null)
			return "";
		if (coll.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (String x : coll)
			sb.append(x + delimiter);
		sb.delete(sb.length() - delimiter.length(), sb.length());
		return sb.toString();
	}

	public void setDates(long l, long m) {
		this.fromD = l;
		this.toD = m;
	}

	public void clearReport() {
		tl.removeAllViews();
	}
	
	public void printReport(boolean email) {
		StringBuilder reportString = new StringBuilder();
    	
		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2 && email == false)
			cols = 30;
		
		if(carts.size() > 0){ 
			long date1 = carts.get(0).getDate();
			long date2 = carts.get(carts.size()-1).getDate();
	
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
			String date1string = df.format(new Date(date1));
			String date2string = df.format(new Date(date2));
			
			reportString.append(EscPosDriver.wordWrap("Store: " + StoreSetting.getName(), cols-1)).append('\n');
			reportString.append(EscPosDriver.wordWrap("Address: " + StoreSetting.getAddress(), cols-1)).append('\n').append('\n');
			
			reportString.append(EscPosDriver.wordWrap("Summary sales report for dates between " + date1string + " - " + date2string, cols+1)).append('\n').append('\n');
				                                             
			StringBuffer message = new StringBuffer("Departments                             ".substring(0, cols));													
			String substring = "Amount";
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		
			for (int i = 0; i < totals.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (totals.get(i).getDepartment() + ":").length(), totals.get(i).getDepartment() + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(totals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
	
			reportString.append('\n');
				
			if (subTotal-nonDiscountTotal < 0) {
				message = new StringBuffer("Discounts:                              ".substring(0, cols));													
				substring = StoreSetting.getCurrency()+nf.format((subTotal-nonDiscountTotal)/100f);                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				reportString.append('\n');
			}
					
			if (taxtotals.size() > 0) 
			{					
				message = new StringBuffer("Tax Groups                              ".substring(0, cols));													
				substring = "Amount";                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		
				for (int i = 0; i < taxtotals.size(); i++) {					
					message = new StringBuffer("                                        ".substring(0, cols));					
					message.replace(0, (taxtotals.get(i).getDepartment() + ":").length(), taxtotals.get(i).getDepartment() + ":");				
					substring = StoreSetting.getCurrency()  + nf.format(taxtotals.get(i).getTotal()/100f);
					message.replace(message.length()-substring.length(), cols-1, substring);	
					reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
					
				reportString.append('\n');
			}
					                            											   
			message = new StringBuffer("Total                                   ".substring(0, cols));													
			substring = StoreSetting.getCurrency()  + nf.format(total/100f);                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
			reportString.append('\n');
					                            
			message = new StringBuffer("Tendered Types                          ".substring(0, cols));													
			substring = "Amount";                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
			for (int i = 0; i < tenders.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (tenders.get(i) + ":").length(), tenders.get(i) + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(tenderstotals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			
			reportString.append('\n');
	
			message = new StringBuffer("Cashiers                                ".substring(0, cols));													
			substring = "Amount";                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
	
			for (int i = 0; i < cashiers.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (cashiers.get(i) + ":").length(), cashiers.get(i) + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(cashierstotals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
				
			if(voids.size() > 0)
			{
				reportString.append('\n');
				
				message = new StringBuffer("Voids Total:                            ".substring(0, cols));													
				substring = StoreSetting.getCurrency()  + nf.format(voids.get(0).getTotal()/100f);                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
						
			reportString.append('\n');
			reportString.append('\n');
	
			if(email == true)
			{
				sendEmailReport(reportString.toString());
			}else{
				EscPosDriver.Print(reportString.toString());
			}
			
			//EscPosDriver.Print(reportString.toString());
		}
	}
	
	private void sendEmailReport(final String report) {
		AlertDialog.Builder builder;

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.admin_select,
				(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		mPersonName = (Spinner) layout
				.findViewById(R.id.adminSpin);
		
		ArrayList<Cashier> admins = ProductDatabase.getAdmins();
		
		ArrayAdapter<Cashier> adminSpinAdapter = new ArrayAdapter<Cashier>(getActivity(), R.layout.spiner, admins);
		adminSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mPersonName.setAdapter(adminSpinAdapter);
		

		builder = new AlertDialog.Builder(getActivity());
		builder.setView(layout)
				.setInverseBackgroundForced(true)
				.setTitle("Email Report to Administrator")
				.setPositiveButton("Send Report",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AdminSend = (Cashier) mPersonName.getSelectedItem();
								pd = ProgressDialog.show(getActivity(), "", "Emailing Report...", true, false);
								new EmailOperation().execute(report);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		adminReportDialog = builder.create();
		adminReportDialog.show();
	}
	
	private boolean issueEmailReport(String report, Cashier admin) {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (EmailSetting.isEnabled()) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
				Mail m = new Mail(EmailSetting.getSmtpUsername(),
						EmailSetting.getSmtpPasword());
				m.setServer(EmailSetting.getSmtpServer(),
						EmailSetting.getSmtpPort());
				m.setSubject(StoreSetting.getName() +" - Date Range Summary Report");
				
				String send = report.replaceAll("\n", "<br>");
				send = send.replaceAll(" ", "&nbsp;");
				
				send = "<P style=\"font-family:courier\">"+send+"</P>";
				m.setBody(send);
				ArrayList<String> sendto = new ArrayList<String>();

				if (admin != null && admin.email != null && admin.email.contains("@"))
				{
					sendto.add(admin.email);
				} else {
					return false;
				}
				
				String[] stockArr = new String[sendto.size()];

				m.setTo(sendto.toArray(stockArr));
				m.setFrom(EmailSetting.getSmtpEmail());
				try {
					return m.send();
				} catch (Exception e) {
					return false;
				}

			} else {
				return false;	
			}
		}

		return false;
	}
	
	private class EmailOperation extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			issueEmailReport(params[0], AdminSend);
			return null;
		}

		@Override
		protected void onPostExecute(String arrayPortName) {
			pd.dismiss();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}
	}
	
	void exportReport() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
		String dateString = DateFormat.getDateInstance().format(new Date(this.fromD)) +" - " +DateFormat.getDateInstance().format(new Date(this.toD));
		
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setMessage("Do you want to save the Summary Report with date range of " + dateString+".")
    	.setTitle("Save Summary Report")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Save Report", new DialogInterface.OnClickListener() {
            private String name;
			public void onClick(DialogInterface dialog, int id) { 	
            		exportrpt(name);
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show(); 
    	
	}
	
	protected void exportrpt(String filename) {

		StringBuilder exportString = new StringBuilder();

		if(carts.size() == 0)
		{
			return;
		}
		
		long date1 = carts.get(0).getDate();
		long date2 = carts.get(carts.size()-1).getDate();

		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		String date1string = df.format(new Date(date1));
		String date2string = df.format(new Date(date2));

		exportString.append("\"Date Range:\",\"" + date1string + "\",\"" + date2string +"\"\n");

		exportString.append("\"Department\",,\"Amount\"\n");

		for (int i = 0; i < totals.size(); i++) {
			exportString.append("\""+totals.get(i).getDepartment() + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f) + "\"\n");
		}
		
		if (subTotal-nonDiscountTotal < 0) {
			exportString.append("\"Discounts:\",,\"").append(
					StoreSetting.getCurrency() + nf.format((subTotal-nonDiscountTotal)/100f) + "\"\n");
			exportString.append(",,\n");
		}

		if( taxtotals.size() > 0) 
		{
			exportString.append("\n\"Tax Groups\",,\"Amount\"\n");
	
			for (int i = 0; i < taxtotals.size(); i++) {
				exportString
						.append("\""+taxtotals.get(i).getDepartment() + "\",,\"")
						.append(StoreSetting.getCurrency() + nf.format(taxtotals.get(i).getTotal()/100f) + "\"\n");
			}
		}
		
		exportString.append("\n\"Total\",,\"" + StoreSetting.getCurrency() + nf.format(total/100f) + "\"\n");

		exportString.append("\n\"Tendered Types\",,\"Amount\"\n");

		for (int i = 0; i < tenders.size(); i++) {
			exportString.append("\""+tenders.get(i) + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(tenderstotals.get(i).getTotal()/100f) + "\"\n");
		}
		
		exportString.append("\n\"Cashiers\",,\"Amount\"\n");

		for (int i = 0; i < cashiers.size(); i++) {
			exportString.append("\""+cashiers.get(i) + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(cashierstotals.get(i).getTotal()/100f) + "\"\n");
		}
		
		if(voids.size() > 0)
		{
			exportString.append(",,\n");
			exportString.append("\"Voids Total:\",,\"").append(
					StoreSetting.getCurrency() + nf.format(voids.get(0).getTotal()/100f) + "\"\n");
		}

		File sd = Environment.getExternalStorageDirectory();
		File dir = new File(sd, "/AdvantagePOS/Reports/Summary");

		dir.mkdirs();

        Date date10=new Date(fromD);
        Date date20=new Date(toD);

        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy");
        String dateText = df2.format(date10)+"-"+df2.format(date20);
        
		File saveFile = new File(sd, "/AdvantagePOS/Reports/Summary/" + dateText + ".csv");
		
        try {   
        	Writer out = new BufferedWriter(new OutputStreamWriter(
        			new FileOutputStream(saveFile), "UTF8"));
   
        	out.append(exportString.toString());
   
        	out.flush();
        	out.close();
        	alertbox("Save Success", "Report Saved Successfully.");
  	    } 
        catch (UnsupportedEncodingException e) 
        {
        	alertbox("Save Failed", "Failed Saving Report.");
        	System.out.println(e.getMessage());
  	   	} 
        catch (IOException e) 
        {
        	alertbox("Save Failed", "Failed Saving Report.");
        	System.out.println(e.getMessage());
  	    }
        catch (Exception e)
        {
        	alertbox("Save Failed", "Failed Saving Report.");
        	System.out.println(e.getMessage());
        } 
	}

	protected void alertbox(String title, String mymessage) 
    { 
    new AlertDialog.Builder(getActivity()) 
       .setMessage(mymessage) 
       .setTitle(title) 
       .setInverseBackgroundForced(true)
       .setCancelable(true) 
       .setNeutralButton(android.R.string.ok, 
          new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int whichButton){} 
          }) 
       .show(); 
    }
}
