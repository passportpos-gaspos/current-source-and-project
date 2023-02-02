package com.passportsingle;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.passportsingle.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends Fragment implements Runnable{

	private static CustomPrinter prnDevice;
	private ArrayList<ReportCart> carts;
	private ReportCart resendEmailCart;
	//private NumberFormat nf = NumberFormat.getInstance();
	private DecimalFormat nf;
	private TableLayout tl;
	protected ProgressDialog pd;
	private int todo;
	private long fromD;
	private long toD;
	private boolean enteredPassword = false;
	private static String lock="lockAccess";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "History Fragment");
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
		enteredPassword = false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}
	
	@Override
	public void onResume() {
		Log.v("History", "Resumed");
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reportsview, container, false);
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}

	protected void buildReport(long l, long m) {
		carts = ProductDatabase.getReports1(l, m);

		tl.removeAllViews();
		if (carts.size() > 0) {

			for (int i = 0; i < carts.size(); i++) {
				
				TableRow row = new TableRow(getActivity());
				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setPadding(0, 5, 0, 0);
				
				final TextView header = new TextView(getActivity());
				TextView tv2 = new TextView(getActivity());
				TextView tv3 = new TextView(getActivity());

				long date = carts.get(i).getDate();
				
				String dateString = DateFormat.getDateTimeInstance().format(new Date(date));
				
				if(carts.get(i).voided)
					header.setText(dateString + " - VOIDED");
				else
					header.setText(dateString);

				header.setSingleLine(true);

				header.setGravity(Gravity.LEFT);
				header.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				header.setTypeface(null, Typeface.BOLD);
				header.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(header);

				tv2.setText(carts.get(i).getCustomerName());
				tv2.setSingleLine(true);

				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
				
				
				tv3.setText("Trans: #"+carts.get(i).trans);
				tv3.setGravity(Gravity.RIGHT);
				tv3.setSingleLine(true);

				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv3.setTypeface(null, Typeface.BOLD);
				tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv3);
				TextView tv1;
				if(carts.get(i).cashier!=null)
				{
					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					tl.addView(row);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("Cashier: "+carts.get(i).cashier.name);
					tv1.setSingleLine(true);
					tv1.setGravity(Gravity.LEFT);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, .5f));
					row.addView(tv1);
										
					tv2.setText("");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					tv3.setText("");
					tv3.setGravity(Gravity.RIGHT);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));

					row.addView(tv3);	
				}
				long nonDiscountTotal = 0;

				for (int o = 0; o < carts.get(i).getProducts().size(); o++) {

					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					tl.addView(row);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText(" "+carts.get(i).getProducts().get(o).name);
					tv1.setSingleLine(true);
					tv1.setGravity(Gravity.LEFT);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, .7f));
					row.addView(tv1);

					Product prod = carts.get(i).getProducts().get(o);
					nonDiscountTotal += prod.itemNonDiscountTotal(carts.get(i).date);

					if(!carts.get(i).getProducts().get(o).isNote)
						tv2.setText(" " + prod.quantity + " @ "+prod.displayPrice(carts.get(i).date));
					else
						tv2.setText("");

					tv2.setGravity(Gravity.RIGHT);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					if(!carts.get(i).getProducts().get(o).isNote)
						tv3.setText(prod.displayTotal(carts.get(i).date));
					else
						tv3.setText("");

					tv3.setGravity(Gravity.RIGHT);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
					
					if(!carts.get(i).getProducts().get(o).barcode.equals(""))
					{
						tv2.setText("");
						tv3.setText("");

						row = new TableRow(getActivity());
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(8, 0, 0, 0);
						tl.addView(row);
						
						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());
						
						tv1.setText(" "+carts.get(i).getProducts().get(o).barcode);
						tv1.setSingleLine(true);
						tv1.setGravity(Gravity.LEFT);
						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, .7f));
						row.addView(tv1);
											
						tv2.setText(" " + prod.quantity + " @ "+prod.displayPrice(carts.get(i).date));
						tv2.setGravity(Gravity.RIGHT);
						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						tv3.setText(prod.displayTotal(carts.get(i).date));
						tv3.setGravity(Gravity.RIGHT);
						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}
				}
				
				if (carts.get(i).subtotaldiscount > 0) {
					row = new TableRow(getActivity());
					tl.addView(row);
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("Discount:");
					tv1.setGravity(Gravity.LEFT);
					tv1.setSingleLine(true);

					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
					row.addView(tv1);
					
					tv2.setText("" + (int)carts.get(i).subtotaldiscount+"%");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setSingleLine(true);

					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					tv3.setText(StoreSetting.getCurrency() + nf.format((carts.get(i).subTotal-nonDiscountTotal)/100f));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setSingleLine(true);

					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
				}

				if (carts.get(i).getTaxName1() != null) {
					row = new TableRow(getActivity());
					tl.addView(row);
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("TAX: " + carts.get(i).getTaxName1());
					tv1.setGravity(Gravity.LEFT);
					tv1.setSingleLine(true);

					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
					row.addView(tv1);
					
					tv2.setText("" + carts.get(i).getTaxPercent1() + "%");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setSingleLine(true);

					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					float price = carts.get(i).tax1/100f;
					
					tv3.setText(StoreSetting.getCurrency() + nf.format(price));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setSingleLine(true);

					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
				}

				if (carts.get(i).getTaxName2() != null) {
					row = new TableRow(getActivity());
					tl.addView(row);
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("TAX: " + carts.get(i).getTaxName2());
					tv1.setGravity(Gravity.LEFT);
					tv1.setSingleLine(true);

					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
					row.addView(tv1);
					
					tv2.setText("" + carts.get(i).getTaxPercent2() +"%");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setSingleLine(true);

					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					float price = carts.get(i).tax2/100f;
					
					tv3.setText(StoreSetting.getCurrency() + nf.format(price));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setSingleLine(true);

					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
				}

				if (carts.get(i).getTaxName3() != null) {
					row = new TableRow(getActivity());
					tl.addView(row);
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("TAX: " + carts.get(i).getTaxName3());
					tv1.setGravity(Gravity.LEFT);
					tv1.setSingleLine(true);

					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.7f));
					row.addView(tv1);
					
					tv2.setText("" + carts.get(i).getTaxPercent3() +"%");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setSingleLine(true);

					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					float price = carts.get(i).tax3/100f;
					
					tv3.setText(StoreSetting.getCurrency() + nf.format(price));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setSingleLine(true);

					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
				}

				row = new TableRow(getActivity());
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.setPadding(8, 0, 0, 6);
				tl.addView(row);

				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				tv3 = new TextView(getActivity());
				
				tv1.setText("Total");

				tv1.setGravity(Gravity.LEFT);
				tv1.setSingleLine(true);

				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setTypeface(null, Typeface.BOLD);
				tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(tv1);
				
				tv2.setText("");
				tv2.setSingleLine(true);

				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);

				float price = carts.get(i).total/100f;
				tv3.setText(StoreSetting.getCurrency() + nf.format(price));
				tv3.setGravity(Gravity.RIGHT);
				tv3.setSingleLine(true);

				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv3.setTypeface(null, Typeface.BOLD);
				tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv3);
				
				long paymentSum = 0;
				ArrayList<Payment> Payments = carts.get(i).Payments;
				for(int p = 0; p < carts.get(i).Payments.size(); p++)
				{
					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 0);
					tl.addView(row);
					
					paymentSum += Payments.get(p).paymentAmount;
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("Tendered Type");
					
					tv1.setGravity(Gravity.LEFT);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
					row.addView(tv1);
						
					tv2.setText(Payments.get(p).paymentType);
					tv2.setGravity(Gravity.RIGHT);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
					row.addView(tv2);
				
					tv3.setText(StoreSetting.getCurrency() + nf.format(Payments.get(p).paymentAmount/100f));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
					row.addView(tv3);
				}
				
				if(paymentSum > carts.get(i).total)
				{
					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(8, 0, 0, 6);
					tl.addView(row);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText("Customer Change");
					
					tv1.setGravity(Gravity.LEFT);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
					row.addView(tv1);
				
					tv2.setText("");
					tv2.setGravity(Gravity.RIGHT);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
					row.addView(tv2);
				
					tv3.setText(StoreSetting.getCurrency() + nf.format((paymentSum-carts.get(i).total)/100f));
					tv3.setGravity(Gravity.RIGHT);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
					row.addView(tv3);
				}
				
				//if(ReceiptSetting.isEnabled() || EmailSetting.isEnabled()){
				row = new TableRow(getActivity());
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.setPadding(8, 0, 0, 6);
				tl.addView(row);

				final TextView Vtv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				tv3 = new TextView(getActivity());
					
				final ReportCart reprintCart = carts.get(i);
				final int index = i;
				if(reprintCart.voided == false)
				{
					String htmlString="<u>Void Sale?</u>";
					Vtv1.setText(Html.fromHtml(htmlString));
				}
				else
				{
					String htmlString="<u>Unvoid Sale?</u>";
					Vtv1.setText(Html.fromHtml(htmlString));
				}

				Vtv1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
					    	AlertDialog.Builder builder2;
					    	final AlertDialog alertDialog2;
					    	
					    	LayoutInflater inflater2 = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
					    	final View mylayout = inflater2.inflate(R.layout.adminpass, (ViewGroup) getActivity().findViewById(R.id.adminpassmain));
					    	
					    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
					    	final TextView text = (TextView) mylayout.findViewById(R.id.textView1);
					    	
					    	text.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
					    	String title = "";
					    	String button = "";
					    	nameEdit.setVisibility(View.GONE);
					    	if(reprintCart.voided == false)
					    	{
								text.setText("Voiding a sale will remove any calculations from the reports and will not issue any form of refund.");
								title = "Void Sale?";
								button = "Void Sale";
					    	}else{
								text.setText("Unvoiding a sale will replace any calculations back in the reports.");
								title = "Unvoid Sale?";
								button = "Unvoid Sale";
					    	}
					    	builder2 = new AlertDialog.Builder(getActivity());
					    	builder2.setView(mylayout)
					    	.setTitle(title)
					    	.setInverseBackgroundForced(true)
					        .setPositiveButton(button, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) { 	
									if(reprintCart.voided == false)
									{
										reprintCart.voided = true;
										ProductDatabase.replaceSale(reprintCart);
										carts.set(index, reprintCart);
										String htmlString="<u>Unvoid Sale?</u>";
										Vtv1.setText(Html.fromHtml(htmlString));
										long datef = reprintCart.getDate();
										String dateString = DateFormat.getDateTimeInstance().format(new Date(datef));
										header.setText(dateString + " - VOIDED");
									}
									else
									{
										reprintCart.voided = false;
										ProductDatabase.replaceSale(reprintCart);
										carts.set(index, reprintCart);
										String htmlString="<u>Void Sale?</u>";
										Vtv1.setText(Html.fromHtml(htmlString));
										long datef = reprintCart.getDate();
										String dateString = DateFormat.getDateTimeInstance().format(new Date(datef));
										header.setText(dateString);
									}	 
					            }
					        })
					        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int id) {
					            	dialog.cancel();
					            }
					        });
				
					    	alertDialog2 = builder2.create();
					    	alertDialog2.show();   							
						}
				});
					
				Vtv1.setGravity(Gravity.LEFT);
				Vtv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				Vtv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(Vtv1);
					
					
				if(EmailSetting.isEnabled() && !reprintCart.getCustomerEmail().equals("")){
					String htmlString="<u>Resend Email</u>";
					tv2.setTextColor(Color.BLUE);
					tv2.setText(Html.fromHtml(htmlString));
					tv2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							todo = 1;
							resendEmailCart = reprintCart;
							pd = ProgressDialog
									.show(getActivity(), "",
											"Sending Email...", true,
											false);
							Thread thread = new Thread(HistoryFragment.this);
							thread.start();
						}
					});
				}else{
					tv2.setText("");
				}	
				
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
				
					if(ReceiptSetting.enabled){
						String htmlString="<u>Reprint</u>";
						tv3.setTextColor(Color.BLUE);
						tv3.setText(Html.fromHtml(htmlString));
						tv3.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								todo = 2;
								resendEmailCart = reprintCart;
								pd = ProgressDialog.show(
										getActivity(), "",
										"Printing Receipt...", true,
										false);
								Thread thread = new Thread(HistoryFragment.this);
								thread.start();
							}
					});
					}else{
						tv3.setText("");
					}
					tv3.setGravity(Gravity.RIGHT);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
					
					addLine(tl);
				//}
			}
		}
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
	
	void exportReport() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
		String dateString = DateFormat.getDateInstance().format(new Date(this.fromD)) +" - " +DateFormat.getDateInstance().format(new Date(this.toD));
		
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setMessage("Do you want to save the History Report with date range of " + dateString+".")
    	.setTitle("Save History Report")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Save Report", new DialogInterface.OnClickListener() {
            private String name;
			public void onClick(DialogInterface dialog, int id) { 	
            		exportrpt(carts, name);
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();  
    	  	
    	/*LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	final View mylayout = inflater.inflate(R.layout.export, (ViewGroup) getActivity().findViewById(R.id.exportmain));
    	
    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
    	final TextView text = (TextView) mylayout.findViewById(R.id.textView1);
    	
    	text.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
    	text.setText("Enter file name. It will save in .csv format and be place in Enders POS directory.");

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Save History Report")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Save Report", new DialogInterface.OnClickListener() {
            private String name;
			public void onClick(DialogInterface dialog, int id) { 	
            	if(!nameEdit.getText().toString().equals("")){
            		name = nameEdit.getText().toString();
            		exportrpt(carts, name);
            	}else{
        			alertbox("File Name Error", "Insert report name.");
            	}   
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();   
    	
    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
		
    	nameEdit.addTextChangedListener(new TextWatcher(){

			@Override public void afterTextChanged(Editable s) {}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				if(!nameEdit.getText().toString().equals("")){
	            	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); 
				}else{
	            	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
				}
			}
    	});	*/	
	}
	
	protected void exportrpt(ArrayList<ReportCart> cart, String filename) {
		
		StringBuilder exportString = new StringBuilder();
		
        if(cart.size() > 0){    
			long date1 = carts.get(0).getDate();
			long date2 = carts.get(carts.size()-1).getDate();

			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			String date1string = df.format(new Date(date1));
			String date2string = df.format(new Date(date2));
			
    		exportString.append("\"Date Range:\",\"" + date1string + "\",\"" + date2string +"\"\n");

	        for(int i=0;i<cart.size();i++){	
	        	ReportCart tempCart = cart.get(i);
				long date = tempCart.getDate();
				
				String dateString = DateFormat.getDateTimeInstance().format(new Date(date));

				
	        	exportString.append("\n\"Transaction: "+tempCart.trans+"\",\"").append(dateString+"\",\"").append(tempCart.getCustomerName()+"\"\n");
	        	if(tempCart.cashier != null)
	        		exportString.append("\"Cashier:\",\""+tempCart.cashier.name+"\"\n");
	        	exportString.append("\"Name\",\"").append("Quantity\",\"").append("Price\",\"").append("Total Price\"\n");
	    		long nonDiscountTotal = 0;

	    		for(int o = 0; o < tempCart.getProducts().size(); o++){
	        		Product tempProd = tempCart.getProducts().get(o);
	        		
		            //int price = tempProd.itemPrice();
		            //int total = tempProd.itemTotal();
	    			nonDiscountTotal += tempProd.itemNonDiscountTotal(tempCart.date);

	        		exportString.append("\""+tempProd.name+"\",\"").append(tempProd.quantity+"\",\"")
	        			.append("@ "+tempProd.displayPrice(tempCart.date)+"\",\"")
	        			.append(tempProd.displayTotal(tempCart.date)+"\"\n");
	        	} 
	    		
	        	if (carts.get(i).subtotaldiscount > 0) {
		        	exportString.append("\"Discount:\",,\"").append((int)carts.get(i).subtotaldiscount+"%\",\"").append(StoreSetting.getCurrency() + nf.format((carts.get(i).subTotal-nonDiscountTotal)/100f)+"\"\n");
	        	}
	        	
	        	if (carts.get(i).getTaxName1() != null) {
		        	exportString.append("\"TAX: "+ tempCart.getTaxName1()+"\",,\"").append(tempCart.getTaxPercent1()+"%\",\"").append(StoreSetting.getCurrency() + nf.format(tempCart.tax1/100f)+"\"\n");
	        	}
	        	
	        	if (carts.get(i).getTaxName2() != null) {
		        	exportString.append("\"TAX: "+ tempCart.getTaxName2()+"\",,\"").append(tempCart.getTaxPercent2()+"%\",\"").append(StoreSetting.getCurrency() + nf.format(tempCart.tax2/100f)+"\"\n");
	        	}
	        	
	        	if (carts.get(i).getTaxName3() != null) {
		        	exportString.append("\"TAX: "+ tempCart.getTaxName3()+"\",,\"").append(tempCart.getTaxPercent3()+"%\",\"").append(StoreSetting.getCurrency() + nf.format(tempCart.tax3/100f)+"\"\n");
	        	}
	        	
	    		long paymentSum = 0;
	    		ArrayList<Payment> Payments = carts.get(i).Payments;
	    		for(int p = 0; p < Payments.size(); p++)
	    		{
	    			paymentSum += Payments.get(p).paymentAmount;
	        		exportString.append("\"Tender Type\",,\""+ Payments.get(p).paymentType+"\",\"").append(StoreSetting.getCurrency() + nf.format(Payments.get(p).paymentAmount/100f)+"\"\n");
	    		}
	    		
	    		if(paymentSum > carts.get(i).total)
	    		{
	        		exportString.append("\"Customer Change\",,,\"").append(StoreSetting.getCurrency() + nf.format((paymentSum-carts.get(i).total)/100f)+"\"\n");
	    		}
	        	
	        	if(!tempCart.voided)
	        		exportString.append("\"Total\",,,\"").append(StoreSetting.getCurrency() + nf.format(tempCart.total/100f)+"\"\n");
	        	else
	        		exportString.append("\"Total\",,\"VOIDED\",\"").append(StoreSetting.getCurrency() + nf.format(tempCart.total/100f)+"\"\n");
	       }   
        }else{
			alertbox("Save Error", "No data to save.");
			return;
        }
		
		File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/AdvantagePOS/Reports/HistoryReports");
        
        dir.mkdirs();
        
        Date date1=new Date(fromD);
        Date date2=new Date(toD);

        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy");
        String dateText = df2.format(date1)+"-"+df2.format(date2);
        
        File saveFile = new File(sd, "/AdvantagePOS/Reports/HistoryReports/History_" +dateText+".csv");
        
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

        
		/*try {
			
			writer = new FileWriter(saveFile);
	        writer.append(exportString.toString());
	        writer.flush();
	        writer.close();
	        alertbox("Save Success", "Report Saved Successfully.");
		} catch (IOException e) {
	        
			e.printStackTrace();
		}*/
		
	}

	
	protected void alertbox(String title, String mymessage) 
    { 
    new AlertDialog.Builder(getActivity()) 
       .setMessage(mymessage) 
       .setInverseBackgroundForced(true)
       .setTitle(title) 
       .setCancelable(true) 
       .setNeutralButton(android.R.string.ok, 
          new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int whichButton){} 
          }) 
       .show(); 
    }
	
	private boolean Print(Bitmap receiptImage) {
		
		if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
		{	        
	        EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		}else if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
		{
			EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		}else if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
		{
			EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		}

		
//		String portName = ReceiptSetting.address;
//		String portSettings = "";
//		
//		if(!hasInternet())
//		{
//			if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
//			{
//				if (prnDevice != null) {
//					prnDevice = null;
//				}
//			}
//			return false;
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_STAR)
//		{
//			if(ReceiptSetting.address.contains("TCP"))
//			{	
//		        try {
//					if(!InetAddress.getByName(ReceiptSetting.address.substring(4)).isReachable(10))
//					{
//			    		Log.v("Not Open", "Not Open");
//			    		return false;
//					}
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			if (portSettings.toUpperCase().equals("MINI")) {
//				MiniPrinterFunctions.PrintBitmap(getActivity(), portName, portSettings,
//						receiptImage, 576, false, false);
//			} else {
//				return PrinterFunctions.PrintBitmap(getActivity(), portName, portSettings,
//						receiptImage, 576, false, false);
//			}
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_EPSON)
//		{
//			if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
//			{ 
//				try {
//					if(!InetAddress.getByName(ReceiptSetting.address).isReachable(3000))
//					{
//			    		return false;
//					}
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			
//	        EscPosDriver.PrintReceipt(resendEmailCart);
//	        return true;
//		}
//		
//		if(ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
//		{
//	        try {
//				if(!InetAddress.getByName(ReceiptSetting.address).isReachable(10))
//				{
//		    		Log.v("Not Open", "Not Open");
//		    		return false;
//				}
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//	        
//			if(OpenDevice())
//			{
//				Log.v("Custom", "Found Device");
//				synchronized (lock) 
//				{
//			    	try
//			        {
//					    int[] arrayOfInt = { 16, 20, 1, 3, 5 };
//						Log.v("Custom", "Try Print");
//
//			    		prnDevice.printImage(receiptImage,CustomPrinter.IMAGE_ALIGN_TO_CENTER, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
//						prnDevice.feed(3);
//			    		prnDevice.cut(CustomPrinter.CUT_TOTAL);	 
//			    		//prnDevice.present(40);
//				    	prnDevice.writeData(arrayOfInt);
//				    	return true;
//			        }
//			    	catch(CustomException e )
//		            {            	
//			    		Log.e("CA ERROR", e.getMessage());
//			    		e.printStackTrace();
//			    		return false;
//		            }
//					catch(Exception e ) 
//			        {
//			    		e.printStackTrace();
//				    	return false;
//			        }
//				}
//			}
//		}

		
		//if(portSettings.toUpperCase().equals("MINI"))
		//{
		//	MiniPrinterFunctions.PrintBitmap(getActivity(), portName, portSettings, receiptImage, 576, false, false);
		//} 
		//else
		//{
		//	PrinterFunctions.PrintBitmap(getActivity(), portName, portSettings, receiptImage, 576, false, false);
		//}
		return false;
	}
	
	private void issueEmailReceipt(ReportCart cart) {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (EmailSetting.isEnabled()) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null
					&& activeNetwork.isConnectedOrConnecting()) {
				if (cart.hasCustomer()) {
					String email = cart.getCustomerEmail();
					if (email.contains("@")) {
						Mail m = new Mail(EmailSetting.getSmtpUsername(),
								EmailSetting.getSmtpPasword());
						m.setServer(EmailSetting.getSmtpServer(),
								EmailSetting.getSmtpPort());
						m.setSubject(EmailSetting.getSmtpSubject());
						String toSend = cart.getBody();
						
						String send = toSend.replaceAll(" ", "&nbsp;");
						send = send.replaceAll("\n", "<br>");
						send = "<P style=\"font-family:courier\">"+send+"</P>";
						Log.v("EMAIL", send);
						m.setBody(send);
						m.setTo(cart.getCustomerEmail());
						m.setFrom(EmailSetting.getSmtpEmail());
						try {

							if (m.send()) {
								Message m2 = new Message();
								m2.what = 9;
								handler.sendMessage(m2);
							} else {
								Message m2 = new Message();
								m2.what = 8;
								handler.sendMessage(m2);

							}
						} catch (Exception e) {
							Log.e("MailApp", "Could not send email", e);
							Message m2 = new Message();
							m2.what = 8;
							handler.sendMessage(m2);
						}
					}
				}
			} else {
				if (cart.hasCustomer()) {
					String email = cart.getCustomerEmail();
					if (email.contains("@")) {
						Message m2 = new Message();
						m2.what = 8;
						handler.sendMessage(m2);
					}
				}
			}
		}
	}
	
	@Override
	public void run() {
		if(todo == 1){
			issueEmailReceipt(resendEmailCart);
			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
		}else if(todo == 2){
			if(Print(resendEmailCart.getReceipt(getActivity()))){
				Message m = new Message();
				m.what = 11;
				handler.sendMessage(m);
			}else{
				Message m = new Message();
				m.what = 12;
				handler.sendMessage(m);
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

	private boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}
	
	public static boolean OpenDevice() {	
        
		if (prnDevice == null) {
			try {
				prnDevice = new CustomAndroidAPI().getPrinterDriverETH(ReceiptSetting.address);
				return true;
			} catch (CustomException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
 			}
		} else {
			Log.v("Custom", "Already Open: " + prnDevice.getPrinterName());
			return true;
		}
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			} else if (msg.what == 9) {
				Toast.makeText(getActivity(),
						"Receipt was sent successfully.", Toast.LENGTH_LONG)
						.show();
			} else if (msg.what == 8) {
				Toast.makeText(getActivity(), "Receipt was not sent.",
						Toast.LENGTH_LONG).show();
			}
			else if (msg.what == 11) {
				pd.dismiss();
			}
			else if (msg.what == 12) {
				Toast.makeText(getActivity(), "Receipt was not sent. Check printer settings.",
						Toast.LENGTH_LONG).show();
				pd.dismiss();
			}
		}
	};
}
