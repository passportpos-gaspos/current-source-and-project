package com.passportsingle;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Date;

public class CashierReportFragment extends Fragment {
	
	private NumberFormat nf = NumberFormat.getInstance();
	private TableLayout tl;
	private ArrayList<ReportCart> carts;
	private float total;
	private ArrayList<String> cashiers = new ArrayList<String>();
	private ArrayList<String> emails = new ArrayList<String>();

	private ArrayList<TotalsList> totals = new ArrayList<TotalsList>();
	private long fromD;
	private long toD;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("Fragment", "Cashier Report Fragment");
		
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
		cashiers = new ArrayList<String>();

		total = 0;

		tl.removeAllViews();
		
		if (carts.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());

			tv1.setText("Cashier");
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
			newtotalf.setDepartment("No Cashier");
			int Totalf = 0;
			newtotalf.setTotal(Totalf);
			totals.add(newtotalf);
			cashiers.add("No Cashier");
			emails.add("");
			
			for (int i = 0; i < carts.size(); i++) {
				if(!carts.get(i).voided)
				{
					String name;
					if(carts.get(i).cashier != null)
						name = carts.get(i).cashier.name;
					else
						name = "";
	
					if(!name.equals("")){
						
						if (cashiers.contains(name)) {
							long Total = carts.get(i).total;
							totals.get(cashiers.indexOf(name)).addTotal(Total);
						} else {
							TotalsList newtotal = new TotalsList();
							newtotal.setDepartment(name);
							long Total = carts.get(i).total;
							newtotal.setTotal(Total);
							totals.add(newtotal);
							cashiers.add(name);
						}
						
					}else{
						
						long Total = carts.get(i).total;
						totals.get(cashiers.indexOf("No Cashier")).addTotal(Total);	
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
    	
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	final View mylayout = inflater.inflate(R.layout.export, (ViewGroup) getActivity().findViewById(R.id.exportmain));
    	
    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
    	final TextView text = (TextView) mylayout.findViewById(R.id.textView1);
    	
    	text.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
    	text.setText("Enter file name. It will save in .csv format and be place in Enders POS directory.");

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Save Customer Report")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Save Report", new DialogInterface.OnClickListener() {
            private String name;
			public void onClick(DialogInterface dialog, int id) { 	
            	if(!nameEdit.getText().toString().equals("")){
            		name = nameEdit.getText().toString();
            		exportrpt(name);
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
    	});		
	}

	protected void exportrpt(String filename) {

		StringBuilder exportString = new StringBuilder();

		long date1 = carts.get(0).getDate();
		long date2 = carts.get(carts.size()-1).getDate();

		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		String date1string = df.format(new Date(date1));
		String date2string = df.format(new Date(date2));
		
		exportString.append("\"Date Range:\",\"" + date1string + "\",\"" + date2string +"\"\n");

		exportString.append("\"Cashier\",\"Amount\"\n");

		for (int i = 0; i < totals.size(); i++) {
			exportString
					.append("\""+totals.get(i).getDepartment() + "\",\"")
					.append(StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f) + "\"\n");
		}

		File sd = Environment.getExternalStorageDirectory();
		File dir = new File(sd, "/AdvantagePOS/Reports");

		dir.mkdirs();

		File saveFile = new File(sd, "/AdvantagePOS/Reports/" + filename + ".csv");
		
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
