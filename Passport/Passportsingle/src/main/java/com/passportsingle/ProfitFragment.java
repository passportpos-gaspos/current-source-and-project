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

public class ProfitFragment extends Fragment {
	private NumberFormat nf = NumberFormat.getInstance();
	private TableLayout tl;
	private ArrayList<ReportCart> carts;
	
	private ArrayList<TotalsList> totals = new ArrayList<TotalsList>();
	private ArrayList<String> departments = new ArrayList<String>();
	
	private float total;
	private ArrayList<TotalsList> margins;
	private float margin;
	private long fromD;
	private long toD;
	private Spinner mPersonName;
	protected Cashier AdminSend;
	protected ProgressDialog pd;
	private AlertDialog adminReportDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Margins Fragment");
		
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

		totals = new ArrayList<TotalsList>();
		margins = new ArrayList<TotalsList>();
		departments = new ArrayList<String>();

		total = 0;
		margin = 0.0f;
		
		tl.removeAllViews();
		if (carts.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());
			TextView tv3 = new TextView(getActivity());
			TextView tv4 = new TextView(getActivity());

			tv1.setText("Departments");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .4f));

			TableRow row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 6);
			row.addView(tv1);

			tv2.setText("Sales");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv2);
			
			tv3.setText("Margin");
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv3.setTypeface(null, Typeface.BOLD);

			tv3.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv3);
			
			tv4.setText("Percent");
			tv4.setGravity(Gravity.RIGHT);
			tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv4.setTypeface(null, Typeface.BOLD);

			tv4.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv4);

			for (int i = 0; i < carts.size(); i++) {
				if(!carts.get(i).voided)
				{
					for (int o = 0; o < carts.get(i).getProducts().size(); o++) {
						Product prod = carts.get(i).getProducts().get(o);
						String test;
						if(!prod.isNote)
						{
							if (prod.cat != 0) {
								test = ProductDatabase.getCatById(prod.cat);
								if (test == null || test.equals("")) {
									test = "No Department";
								}
							} else {
								test = "No Department";
							}
							if (departments.contains(test)) {
								long cost = prod.quantity * prod.cost;
								long itemTotal = prod.itemTotal(carts.get(i).date);
								
								totals.get(departments.indexOf(test)).addTotal(itemTotal);
								
								if(itemTotal < 0){
									margins.get(departments.indexOf(test)).addTotal(itemTotal);
									margin = margin+itemTotal;
								}else{
									margins.get(departments.indexOf(test)).addTotal(itemTotal-cost);
									margin = margin+itemTotal-cost;
								}
								
							} else {
								TotalsList newtotal = new TotalsList();
								TotalsList newcost = new TotalsList();
		
								newtotal.setDepartment(test);
								newcost.setDepartment(test);
								long cost = prod.quantity * prod.cost;
								long itemTotal = prod.itemTotal(carts.get(i).date);
								
								if(itemTotal < 0){
									newcost.setTotal(itemTotal);
									margin = margin+itemTotal;
								}else{
									newcost.setTotal(itemTotal-cost);
									margin = margin+itemTotal-cost;
								}
								
								newtotal.setTotal(itemTotal);
								totals.add(newtotal);
								margins.add(newcost);
								departments.add(test);
							}
						}
					}
	
					//String test = carts.get(i).getPaymentType();
	
					//test = carts.get(i).getTaxName2();
	
					total = total + carts.get(i).subTotal;
				}
			}

			android.widget.TableRow.LayoutParams layoutParams;
			
			for (int i = 0; i < totals.size(); i++) {
				row = new TableRow(getActivity());
				TextView tv = new TextView(getActivity());

				tl.addView(row);
				layoutParams = new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				row.setLayoutParams(layoutParams);

				tv.setText(totals.get(i).getDepartment());
				tv.setGravity(Gravity.LEFT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .4f));
				row.addView(tv);

				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(totals.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
				row.addView(tv);
				
				tv = new TextView(getActivity());
				tv.setText(StoreSetting.getCurrency()  + nf.format(margins.get(i).getTotal()/100f));
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
				row.addView(tv);
				
				tv = new TextView(getActivity());
				if(totals.get(i).getTotal() != 0)
				{
					tv.setText(nf.format((float)margins.get(i).getTotal()/(float)totals.get(i).getTotal()*100) + "%");
				}else{
					tv.setText(nf.format(0)+"%");
				}
				tv.setGravity(Gravity.RIGHT);
				tv.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
				row.addView(tv);
			}

			row.setPadding(0, 0, 0, 5);

			addLine(tl);
			addLine(tl);

			tv1 = new TextView(getActivity());
			tv2 = new TextView(getActivity());
			tv3 = new TextView(getActivity());
			tv4 = new TextView(getActivity());

			tv1.setText("Total");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .4f));

			row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 5, 0, 5);
			row.addView(tv1);
			tv2.setText(StoreSetting.getCurrency()  + nf.format(total/100f));
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv2);
			
			tv3.setText(StoreSetting.getCurrency()  + nf.format(margin/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv3.setTypeface(null, Typeface.BOLD);

			tv3.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv3);

			if(total != 0)
			{
				tv4.setText(nf.format((float)margin/(float)total*100f)+"%");
			}else{
				tv4.setText(nf.format(0)+"%");
			}
			
			tv4.setGravity(Gravity.RIGHT);
			tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv4.setTypeface(null, Typeface.BOLD);

			tv4.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .2f));
			row.addView(tv4);
		}
		
	}
	
	private void addLine(TableLayout tl) {
		TableRow line = new TableRow(getActivity());

		tl.addView(line);
		LayoutParams layoutParams = new TableRow.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
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
	
	public void printReport(Boolean email) {
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
						
			reportString.append(EscPosDriver.wordWrap("Department Profit report for dates between " + date1string + " - " + date2string, cols+1)).append('\n').append('\n');
				                                      		
			StringBuffer message = new StringBuffer("                                        ".substring(0, cols));													
		
			for (int i = 0; i < totals.size(); i++) {	
				reportString.append(EscPosDriver.wordWrap(totals.get(i).getDepartment(), cols+1)).append('\n');
			
				message = new StringBuffer("Total                                   ".substring(0, cols));					
				String substring = StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
				message = new StringBuffer("Margin                                  ".substring(0, cols));					
				substring = StoreSetting.getCurrency() + nf.format(margins.get(i).getTotal()/100f);
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				
				message = new StringBuffer("Percent                                 ".substring(0, cols));
				if(totals.get(i).getTotal() != 0)
				{
					substring = nf.format((float)margins.get(i).getTotal()/(float)totals.get(i).getTotal()*100f)+ "%";
				}else{
					substring = nf.format(0)+"%";
				}
				message.replace(message.length()-substring.length(), cols-1, substring);	
				reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
				reportString.append('\n');
			}
								                            											   
			message = new StringBuffer("Total Sales                             ".substring(0, cols));													
			String substring = StoreSetting.getCurrency()  + nf.format(total/100f);                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
	
			message = new StringBuffer("Total Margin                            ".substring(0, cols));													
			substring = StoreSetting.getCurrency()  + nf.format(total/100f);                       
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			
			message = new StringBuffer("Total Percent                            ".substring(0, cols));	
			if(total != 0)
			{
				substring = nf.format((float)margin/(float)total*100f)+"%";                       
			}else{
				substring = nf.format(0)+"%";                    
			}
			message.replace(message.length()-substring.length(), cols-1, substring);	
			reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
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
				m.setSubject(StoreSetting.getName() +" - Profit Margin Report");
				
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
    	builder.setMessage("Do you want to save the Profit Report with date range of " + dateString+".")
    	.setTitle("Save Profit Report")
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
	
			exportString.append("\"Department\",,\"Amount\",\"Margin\",\"Percent\"\n");
	
			for (int i = 0; i < totals.size(); i++) {
				exportString.append("\""+totals.get(i).getDepartment() + "\",,\"")
							.append(StoreSetting.getCurrency() + nf.format(totals.get(i).getTotal()/100f) + "\",\"")
							.append(StoreSetting.getCurrency() + nf.format(margins.get(i).getTotal()/100f) + "\",\"")
							.append(StoreSetting.getCurrency() + nf.format((float)margins.get(i).getTotal()/(float)totals.get(i).getTotal()*100f) + "%\"\n");
			}
	
			if(total != 0)
			{
				exportString.append("\n\"Total\",,\"" + StoreSetting.getCurrency() + nf.format(total/100f) + "\",\"")
					.append(StoreSetting.getCurrency() + nf.format(margin/100f) + "\",\"")
					.append(StoreSetting.getCurrency() + nf.format((float)margin/(float)total*100f) + "%\"\n");
			}else{
				exportString.append("\n\"Total\",,\"" + StoreSetting.getCurrency() + nf.format(total/100f) + "\",\"")
					.append(StoreSetting.getCurrency() + nf.format(margin/100f) + "\",\"")
					.append(StoreSetting.getCurrency() + nf.format(0) + "%\"\n");
			}
		
		}else{
			alertbox("Save Error", "No data to save.");
			return;
	    }
		
		File sd = Environment.getExternalStorageDirectory();
		File dir = new File(sd, "/AdvantagePOS/Reports/Profit");

		dir.mkdirs();

        Date date10=new Date(fromD);
        Date date20=new Date(toD);

        SimpleDateFormat df2 = new SimpleDateFormat("ddMMyy");
        String dateText = df2.format(date10)+"-"+df2.format(date20);

        
		File saveFile = new File(sd, "/AdvantagePOS/Reports/Profit/Profit_" + dateText + ".csv");
		
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
        
		/*FileWriter writer;
		try {
			writer = new FileWriter(saveFile);
			writer.append(exportString.toString());
			writer.flush();
			writer.close();
	        alertbox("Save Success", "Report Saved Successfully.");
		} catch (IOException e) {
	        alertbox("Save Failed", "Failed Saving Report.");
			e.printStackTrace();
		}*/
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
