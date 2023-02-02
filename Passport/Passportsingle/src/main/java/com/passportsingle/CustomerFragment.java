package com.passportsingle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

public class CustomerFragment extends Fragment {
	
	private NumberFormat nf = NumberFormat.getInstance();
	private TableLayout tl;
	private ArrayList<ReportCart> carts;
	private float total;
	private ArrayList<String> customers = new ArrayList<String>();
	private ArrayList<String> emails = new ArrayList<String>();

	private ArrayList<TotalsList> totals = new ArrayList<TotalsList>();
	private long fromD;
	private long toD;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("Fragment", "Customer Fragment");
		
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reportsview, container, false);
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	protected void buildReport(long l, long m) {
		carts = ProductDatabase.getReports1(l, m);
		totals = new ArrayList<TotalsList>();
		customers = new ArrayList<String>();
		emails = new ArrayList<String>();

		total = 0;

		tl.removeAllViews();
		
		if (carts.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());

			tv1.setText("Customer");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));

			TableRow row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 6);
			row.addView(tv1);

			tv2.setText("Amount");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv2);
			
			TotalsList newtotalf = new TotalsList();
			newtotalf.setDepartment("No Customer");
			int Totalf = 0;
			newtotalf.setTotal(Totalf);
			totals.add(newtotalf);
			customers.add("No Customer");
			emails.add("");
			
			for (int i = 0; i < carts.size(); i++) {
				if(!carts.get(i).voided)
				{
					String name = carts.get(i).getCustomerName();
					String email = carts.get(i).getCustomerEmail();
	
					if(!name.equals("")){
						
						if (customers.contains(name)) {
							long Total = carts.get(i).total;
							totals.get(customers.indexOf(name)).addTotal(Total);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(name);
							long Total = carts.get(i).total;
							newtotal.setTotal(Total);
							totals.add(newtotal);
							customers.add(name);
							emails.add(email);
						}
						
					}else{
						
						long Total = carts.get(i).total;
						totals.get(customers.indexOf("No Customer")).addTotal(Total);	
					}
	
					total = total + carts.get(i).total;
				}
			}
			
			android.widget.TableRow.LayoutParams layoutParams;
			for (int i = 0; i < totals.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());

				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(totals.get(i).getDepartment());
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv);
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
    	builder.setMessage("Do you want to save the Customer Report with date range of " + dateString+".")
    	.setTitle("Save Customer Report")
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

		if(carts.size() > 0){
			long date1 = carts.get(0).getDate();
			long date2 = carts.get(carts.size()-1).getDate();
	
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			String date1string = df.format(new Date(date1));
			String date2string = df.format(new Date(date2));
			
			exportString.append("\"Date Range:\",\"" + date1string + "\",\"" + date2string +"\"\n");
	
			exportString.append("\"Customer\",\"Amount\",\"Email\"\n");
	
			for (int i = 0; i < totals.size(); i++) {
				exportString
						.append("\""+totals.get(i).getDepartment() + "\",\"")
						.append(StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f) + "\",")
						.append("\""+emails.get(i) + "\"\n");
			}
		}else{
			alertbox("Save Error", "No data to save.");
			return;
	    }
		File sd = Environment.getExternalStorageDirectory();
		File dir = new File(sd, "/AdvantagePOS/Reports/Customers");

		dir.mkdirs();
		
        Date date10=new Date(fromD);
        Date date20=new Date(toD);

        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy");
        String dateText = df2.format(date10)+"-"+df2.format(date20);

		File saveFile = new File(sd, "/AdvantagePOS/Reports/Customers/Customers_" + dateText + ".csv");
		
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