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

//import com.StarMicronics.StarIOSDK.PrinterFunctions;
import com.passportsingle.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShiftFragment extends Fragment {

	private NumberFormat nf = NumberFormat.getInstance();
	private TableLayout tl;
	
	protected ProgressDialog pd;

	//private long  total;
	private long fromD;
	private long toD;
	private ArrayList<Shift> shifts;
	protected Shift reprintReport;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Shift Fragment");
		
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}

	protected void buildReport(long l, long m) {
		shifts = ProductDatabase.getShifts(l, m);
				
		tl.removeAllViews();
		if (shifts.size() > 0) 
		{
			for (int u = 0; u < shifts.size(); u++) 
			{
				ArrayList<ReportCart> carts = shifts.get(u).carts;
				long totalAmountSum = 0;
				shifts.get(u).total = 0;
				shifts.get(u).subTotal = 0;
				shifts.get(u).nonDiscountTotal = 0;

				TotalsList newtotalf = new TotalsList();
				newtotalf.setDepartment("No Cashier");
				int Totalf = 0;
				newtotalf.setTotal(Totalf);
				shifts.get(u).cashierstotals.add(newtotalf);
				shifts.get(u).cashiers.add("No Cashier");

				for (int i = 0; i < carts.size(); i++)
				{

					if(!carts.get(i).voided)
					{
						for (int o = 0; o < carts.get(i).getProducts().size(); o++) {
							Product prod = carts.get(i).getProducts().get(o);
							String test;
							if(!prod.isNote)
							{
								shifts.get(u).nonDiscountTotal += prod.itemNonDiscountTotal(carts.get(i).date);
	
								if (prod.cat != 0) {
									test = ProductDatabase.getCatById(prod.cat);
									if (test == null || test.equals("")) {
										test = "No Department";
									}
								} else {
									test = "No Department";
								}
								if(shifts.get(u).departments.contains(test)) {
									//float subTotal = carts.get(i).getQuantities().get(o)
									//		* (prod.getPrice() - prod.getPrice()
									//				* (prod.getDiscount() / 100));
									
									long itemTotal = prod.itemTotal(carts.get(i).date);
									
									shifts.get(u).totals.get(shifts.get(u).departments.indexOf(test)).addTotal(itemTotal);
								} else {
									TotalsList newtotal = new TotalsList();
									newtotal.setDepartment(test);
									//float subTotal = carts.get(i).getQuantities().get(o)
									//		* (prod.getPrice() - prod.getPrice()
									//				* (prod.getDiscount() / 100));
									long itemTotal = prod.itemTotal(carts.get(i).date);
			
									newtotal.setTotal(itemTotal);
									shifts.get(u).totals.add(newtotal);
									shifts.get(u).departments.add(test);
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
							
							if (shifts.get(u).tenders.contains(test)) {
								shifts.get(u).tenderstotals.get(shifts.get(u).tenders.indexOf(test)).addTotal(Payments.get(p).paymentAmount);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(test);
								newtotal.setTotal(Payments.get(p).paymentAmount);
								shifts.get(u).	tenderstotals.add(newtotal);
								shifts.get(u).	tenders.add(test);
							}
						}
						
						totalAmountSum += amountSum;
								
						test = carts.get(i).getTaxName1();
						if (test != null && !test.equals("")) {
							if (shifts.get(u).taxes.contains(test)) {
								shifts.get(u).taxtotals.get(shifts.get(u).taxes.indexOf(test)).addTotal(
										carts.get(i).tax1);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(test);
								newtotal.setTotal(carts.get(i).tax1);
								shifts.get(u).taxtotals.add(newtotal);
								shifts.get(u).taxes.add(test);
							}
						}
		
						test = carts.get(i).getTaxName2();
		
						if (test != null && !test.equals("")) {
							if (shifts.get(u).taxes.contains(test)) {
								shifts.get(u).taxtotals.get(shifts.get(u).taxes.indexOf(test)).addTotal(
										carts.get(i).tax2);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(test);
								newtotal.setTotal(carts.get(i).tax2);
								shifts.get(u).taxtotals.add(newtotal);
								shifts.get(u).taxes.add(test);
							}
						}
		
						test = carts.get(i).getTaxName3();
						if (test != null && !test.equals("")) {
							if (shifts.get(u).taxes.contains(test)) {
								shifts.get(u).taxtotals.get(shifts.get(u).taxes.indexOf(test)).addTotal(
										carts.get(i).tax3);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(test);
								newtotal.setTotal(carts.get(i).tax3);
								shifts.get(u).taxtotals.add(newtotal);
								shifts.get(u).taxes.add(test);
							}
						}
						
						shifts.get(u).total = shifts.get(u).total + carts.get(i).total;
						shifts.get(u).subTotal = shifts.get(u).subTotal + carts.get(i).subTotal;

						String name;
						if(carts.get(i).cashier != null)
							name = carts.get(i).cashier.name;
						else
							name = "";
		
						if(!name.equals("")){
							
							if (shifts.get(u).cashiers.contains(name)) {
								long Total = carts.get(i).total;
								shifts.get(u).cashierstotals.get(shifts.get(u).cashiers.indexOf(name)).addTotal(Total);
							} else {
								TotalsList newtotal = new TotalsList();
								newtotal.setDepartment(name);
								long Total = carts.get(i).total;
								newtotal.setTotal(Total);
								shifts.get(u).cashierstotals.add(newtotal);
								shifts.get(u).cashiers.add(name);
							}
							
						}else{
							
							long Total = carts.get(i).total;
							shifts.get(u).cashierstotals.get(shifts.get(u).cashiers.indexOf("No Cashier")).addTotal(Total);	
						}
					}else{
						String test = "Voids";
						
						if (shifts.get(u).voidString.contains(test)) {
							shifts.get(u).voids.get(shifts.get(u).voidString.indexOf(test)).addTotal(carts.get(i).total);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(test);
							newtotal.setTotal(carts.get(i).total);
							shifts.get(u).voids.add(newtotal);
							shifts.get(u).voidString.add(test);
						}
					}
				}
				
				if(totalAmountSum > shifts.get(u).total)
				{
					String change = "Cash";
					long changeAmount = shifts.get(u).total-totalAmountSum;
							
					if (shifts.get(u).tenders.contains(change)) {
						shifts.get(u).tenderstotals.get(shifts.get(u).tenders.indexOf(change)).addTotal(changeAmount);
					} else {
						TotalsList newtotal = new TotalsList();
						newtotal.setDepartment(change);
						newtotal.setTotal(changeAmount);
						shifts.get(u).tenderstotals.add(newtotal);
						shifts.get(u).tenders.add(change);
					}
				}
			
			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());
			String dateString = DateFormat.getDateTimeInstance().format(new Date(shifts.get(u).end));

			tv1.setText("Shift Report ending at " + dateString);
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			TableRow row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 10, 0, 5);
			row.addView(tv1);

			tv2.setText("");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);
			
			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());

			tv1.setText("Departments");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 0);
			row.addView(tv1);

			tv2.setText("Amount");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			android.widget.TableRow.LayoutParams layoutParams;
			for (int i = 0; i < shifts.get(u).totals.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(shifts.get(u).totals.get(i).getDepartment());
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).totals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}

			if (shifts.get(u).subTotal-shifts.get(u).nonDiscountTotal < 0) {
				row = new TableRow(getActivity());
				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
				row.setPadding(0, 5, 0, 0);
				
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				
				tv1.setText("Discounts Total:");
				tv1.setGravity(Gravity.LEFT);
				tv1.setSingleLine(true);

				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setTypeface(null, Typeface.BOLD);

				tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(tv1);
				
				tv2.setText(StoreSetting.getCurrency() + nf.format((shifts.get(u).subTotal-shifts.get(u).nonDiscountTotal)/100f));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setSingleLine(true);

				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);

				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
			}
			
			if( shifts.get(u).taxtotals.size() > 0)
			{
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
	
				for (int i = 0; i < shifts.get(u).taxtotals.size(); i++) {
					row = new TableRow(getActivity());
					TextView tv = new TextView(getActivity());
					row.setPadding(5, -3, 0, 0);

					tl.addView(row);
					layoutParams = new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					row.setLayoutParams(layoutParams);
	
					tv.setText(shifts.get(u).taxtotals.get(i).getDepartment());
					tv.setGravity(Gravity.LEFT);
					tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
	
					tv.setLayoutParams(new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.addView(tv);
	
					tv = new TextView(getActivity());
					tv.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).taxtotals.get(i).getTotal()/100f));
					tv.setGravity(Gravity.RIGHT);
					tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
	
					tv.setLayoutParams(new TableRow.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.addView(tv);
				}
			}

			//addLine(tl);

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
			tv2.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).total/100f));
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);

			//addLine(tl);

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

			for (int i = 0; i < shifts.get(u).tenders.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(shifts.get(u).tenders.get(i));
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).tenderstotals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}
			//addLine(tl);
			
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

			for (int i = 0; i < shifts.get(u).cashiers.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());
				row.setPadding(5, -3, 0, 0);
				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(shifts.get(u).cashiers.get(i));
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).cashierstotals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
			}
			//addLine(tl);
			
			if(shifts.get(u).voids.size() > 0)
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
				row.setPadding(0, 5, 0, 0);
				row.addView(tv1);

				tv2.setText(StoreSetting.getCurrency()  + nf.format(shifts.get(u).voids.get(0).getTotal()/100f));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);

				tv2.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv2);
				
				//addLine(tl);
			}
			
			// ------------------------
			
			row = new TableRow(getActivity());
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 5, 0, 5);
			tl.addView(row);

			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());
				
			final Shift reprintShift = shifts.get(u);
			final int index = u;
				
			if(ReceiptSetting.enabled){
				String htmlString="<u>Reprint</u>";
				tv1.setText(Html.fromHtml(htmlString));
				tv1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						reprintReport = reprintShift;
						pd = ProgressDialog.show(getActivity(), "", "Printing Report...", true, false);
						new PrintOperation().execute("");
					}
				});
			}else{
				tv1.setText("");
			}
			
			tv1.setGravity(Gravity.RIGHT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
			row.addView(tv1);
			
			String htmlString="<u>Save Report</u>";
			tv2.setText(Html.fromHtml(htmlString));
			tv2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					reprintReport = reprintShift;
					exportReport();
				}
			});
			
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.3f));
			row.addView(tv2);

			addLine(tl);
			// -------------------------
			addLine(tl);

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
	
	public void exportReport() {
		
		AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
		String dateString = DateFormat.getDateTimeInstance().format(new Date(reprintReport.end));
		
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setMessage("Do you want to save the Shift Report ending " + dateString+".")
    	.setTitle("Save End of Shift Report")
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
	
	public void printReport() {
		
		StringBuilder reportString = new StringBuilder();
		
		int cols = 40;
		
		if(ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;
		
		if (shifts.size() > 0) 
		{
			int u = 0;
			
			if(reprintReport != null)
				u = shifts.indexOf(reprintReport);

			String dateString = DateFormat.getDateTimeInstance().format(new Date(shifts.get(u).end));
			reportString.append(EscPosDriver.wordWrap("Shift Report ending at " + dateString, cols+1)).append('\n').append('\n');
			                                             
			StringBuffer message = new StringBuffer("Departments                             ".substring(0, cols));													
			String substring = "Amount";
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			for (int i = 0; i < shifts.get(u).totals.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (shifts.get(u).totals.get(i).getDepartment() + ":").length(), shifts.get(u).totals.get(i).getDepartment() + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).totals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}

			reportString.append('\n');
				                            
			if (shifts.get(u).subTotal-shifts.get(u).nonDiscountTotal < 0) {
				message = new StringBuffer("Discounts:                              ".substring(0, cols));													
				substring = StoreSetting.getCurrency()+nf.format((shifts.get(u).subTotal-shifts.get(u).nonDiscountTotal)/100f);                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				reportString.append('\n');
			}
				
			if (shifts.get(u).taxtotals.size() > 0) 
			{					
				message = new StringBuffer("Tax Groups                              ".substring(0, cols));													
				substring = "Amount";                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
				for (int i = 0; i < shifts.get(u).taxtotals.size(); i++) {					
					message = new StringBuffer("                                        ".substring(0, cols));					
					message.replace(0, (shifts.get(u).taxtotals.get(i).getDepartment() + ":").length(), shifts.get(u).taxtotals.get(i).getDepartment() + ":");				
					substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).taxtotals.get(i).getTotal()/100f);
					message.replace(message.length()-substring.length(), cols-1, substring);	
					reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				}
				
				reportString.append('\n');
			}
				                            											   
			message = new StringBuffer("Total                                   ".substring(0, cols));													
			substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).total/100f);                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			reportString.append('\n');
              
			message = new StringBuffer("Tendered Types                          ".substring(0, cols));													
			substring = "Amount";                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			for (int i = 0; i < shifts.get(u).tenders.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (shifts.get(u).tenders.get(i) + ":").length(), shifts.get(u).tenders.get(i) + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).tenderstotals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			}
					
			reportString.append('\n');

			message = new StringBuffer("Cashiers                                ".substring(0, cols));													
			substring = "Amount";                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			for (int i = 0; i < shifts.get(u).cashiers.size(); i++) {
				message = new StringBuffer("                                        ".substring(0, cols));					
				message.replace(0, (shifts.get(u).cashiers.get(i) + ":").length(), shifts.get(u).cashiers.get(i) + ":");				
				substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).cashierstotals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

			}
			
			if(shifts.get(u).voids.size() > 0)
			{
				reportString.append('\n');

				message = new StringBuffer("Voids Total:                            ".substring(0, cols));													
				substring = StoreSetting.getCurrency()  + nf.format(shifts.get(u).voids.get(0).getTotal()/100f);                       
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
					
			reportString.append('\n');
			reportString.append('\n');


			EscPosDriver.Print(reportString.toString());
		}
	}

	public void exportrpt(String filename) {
		
		Shift report = reprintReport;
		
		StringBuilder exportString = new StringBuilder();

		long date1 = report.end;
		String dateString = DateFormat.getDateTimeInstance().format(new Date(date1));

		exportString.append("\"End of Shift Report ending at " + dateString +".\"\n\n");
		
		exportString.append("\"Departments\",,\"Amount\"\n");

		for (int i = 0; i < report.totals.size(); i++) {
			exportString.append("\""+report.totals.get(i).getDepartment() + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(report.totals.get(i).getTotal()/100f) + "\"\n");
		}

		exportString.append(",,\n");
		
		if (report.subTotal-report.nonDiscountTotal < 0) {
			exportString.append("\"Discounts:\",,\"").append(
					StoreSetting.getCurrency() + nf.format((report.subTotal-report.nonDiscountTotal)/100f) + "\"\n");
			exportString.append(",,\n");
		}
		
		if(report.taxtotals.size() > 0)
		{
			exportString.append("\"Tax Groups\",,\"Amount\"\n");
	
			for (int i = 0; i < report.taxtotals.size(); i++) {
				exportString.append("\""+report.taxtotals.get(i).getDepartment() + "\",,\"").append(
						StoreSetting.getCurrency() + nf.format(report.taxtotals.get(i).getTotal()/100f) + "\"\n");
			}

			exportString.append(",,\n");
		}

		exportString.append(",,\n");

		exportString.append("\"Total\",,\"").append(
				StoreSetting.getCurrency()  + nf.format(report.total/100f) + "\"\n");

		exportString.append(",,\n");
                         											   
		exportString.append("\"Tendered Types\",,\"Amount\"\n");

		for (int i = 0; i < report.tenders.size(); i++) {
			exportString.append("\""+report.tenders.get(i) + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(report.tenderstotals.get(i).getTotal()/100f) + "\"\n");
		}

		exportString.append(",,\n");
		
		exportString.append("\"Cashiers\",,\"Amount\"\n");

		for (int i = 0; i < report.cashiers.size(); i++) {
			exportString.append("\""+report.cashiers.get(i) + "\",,\"").append(
					StoreSetting.getCurrency() + nf.format(report.cashierstotals.get(i).getTotal()/100f) + "\"\n");
		}
		
		if(report.voids.size() > 0)
		{
			exportString.append(",,\n");
			exportString.append("\"Voids Total:\",,\"").append(
					StoreSetting.getCurrency() + nf.format(report.voids.get(0).getTotal()/100f) + "\"\n");
		}

		File sd = Environment.getExternalStorageDirectory();
		File dir = new File(sd, "/AdvantagePOS/Reports/EndShifts");

		dir.mkdirs();

        Date date=new Date(report.end);
        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy_HH-mm-ss");
        String dateText = df2.format(date);
        
		File saveFile = new File(sd, "/AdvantagePOS/Reports/EndShifts/End_Of_Shift_" + dateText + ".csv");
		
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
	
	private class PrintOperation extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			printReport();
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

	public void endReport() {
		
		Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Process End of Day?")
				.setMessage("You are about to run an End of Shift report. Do you want to do this?")
				.setInverseBackgroundForced(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {					
							public void onClick(DialogInterface dialog,
									int id) {								
								reprintReport = null;
								
								if(ProductDatabase.tranAfterLastShift())
								{
									ProductDatabase.saveShift();
								}
								
								buildReport(fromD, toD);
								if(ReceiptSetting.enabled)
								{
									pd = ProgressDialog.show(getActivity(), "", "Printing Report...", true, false);
									new PrintOperation().execute("");
								}
							}
						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dialog.cancel();
							}
						});
		
		AlertDialog alertDialog = builder.create();
		alertDialog.show();	
	}
}
