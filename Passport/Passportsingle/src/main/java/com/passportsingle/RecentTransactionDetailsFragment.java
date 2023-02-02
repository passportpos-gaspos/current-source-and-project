package com.passportsingle;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecentTransactionDetailsFragment extends Fragment implements Runnable{
	
	private ListView TransactionList;
	private View mylayout;
	private TableLayout tl;
	private DecimalFormat nf;
	private int mActivatedPosition = 0;
	private ArrayList<ReportCart> carts;
	private ReportCart resendEmailCart;
	protected int todo;
	protected ProgressDialog pd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Recent Transaction Details Fragment");
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport();
	}
	
	@Override
	public void onResume() {
		Log.v("Print Transaction", "Resumed");
		super.onResume();
	}
	
	@Override  
    public void onSaveInstanceState(Bundle outState) {   
    	super.onSaveInstanceState(outState);    
    	outState.putInt("curChoice", mActivatedPosition);  
    }
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.reportsview, container, false);
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	protected void buildReport(){
		
		carts = ProductDatabase.getRecentTrasc();

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
				/*if(reprintCart.voided == false)
				{
					String htmlString="<u>Void Sale?</u>";
					Vtv1.setText(Html.fromHtml(htmlString));
				}
				else
				{
					String htmlString="<u>Unvoid Sale?</u>";
					Vtv1.setText(Html.fromHtml(htmlString));
				}*/

				/*Vtv1.setOnClickListener(new OnClickListener() {
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
				*/	
				Vtv1.setGravity(Gravity.LEFT);
				Vtv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				Vtv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(Vtv1);
					
					
				/*if(EmailSetting.isEnabled() && !reprintCart.getCustomerEmail().equals("")){
					String htmlString="<u>Resend Email</u>";
					tv2.setTextColor(Color.BLUE);
					tv2.setText(Html.fromHtml(htmlString));
					tv2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							todo = 1;
							resendEmailCart = reprintCart;
							pd = ProgressDialog
									.show(getSherlockActivity(), "",
											"Sending Email...", true,
											false);
							Thread thread = new Thread(RecentTransactionDetailsFragment.this);
							thread.start();
						}
					});
				}else{
					tv2.setText("");
				}*/	
				
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
								Thread thread = new Thread(RecentTransactionDetailsFragment.this);
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
	public void run() {
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
	
	private boolean Print(Bitmap receiptImage){
		
		/*if(ReceiptSetting.type == ReceiptSetting.TYPE_LAN)
		{	        
	        EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		}else if(ReceiptSetting.type == ReceiptSetting.TYPE_BT)
		{
			EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		}else if(ReceiptSetting.type == ReceiptSetting.TYPE_USB)
		{*/
			EscPosDriver.PrintReceipt(resendEmailCart);
	        return true;
		/*}
		
		return false;*/
		
	}

}
