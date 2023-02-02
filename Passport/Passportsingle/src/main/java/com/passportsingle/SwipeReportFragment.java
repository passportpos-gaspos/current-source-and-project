package com.passportsingle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.passportsingle.web.WebRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SwipeReportFragment extends Fragment implements Runnable{

	private TableLayout tl;
	protected ProgressDialog pd;
	private int todo;
	private long fromD;
	private long toD;
	private TotalsList totals;
	private DecimalFormat nf;
	private Payment tempPayment;
	private String mpsResponse;
	public String CmdStatus;
	public String TextResponse;
	public String Authorize;
	public String payAmount;
	public String TranCode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Priority Sales Fragment");
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
	}
	
	@Override
	public void onResume() {
		Log.v("Priority Sales", "Resumed");
		super.onResume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reportsview, container, false);
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public void setDates(long l, long m) {
		this.fromD = l;
		this.toD = m;
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
	
	protected void buildReport(long l, long m) {
		ArrayList<Payment> payments = ProductDatabase.getProritySales(l, m);

		tl.removeAllViews();
		if (payments.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());
			TextView tv3 = new TextView(getActivity());
			TextView tv4 = new TextView(getActivity());
			TextView tv5 = new TextView(getActivity());

			tv1.setText("Date");
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

			tv2.setText("Processed");
			tv2.setGravity(Gravity.LEFT);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv2);
			
			tv3.setText("Invoice");
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv3.setTypeface(null, Typeface.BOLD);

			tv3.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv3);
			
			tv4.setText("Amount");
			tv4.setGravity(Gravity.RIGHT);
			tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv4.setTypeface(null, Typeface.BOLD);

			tv4.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv4);

			tv5.setText("");
			tv5.setGravity(Gravity.RIGHT);
			tv5.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv5.setTypeface(null, Typeface.BOLD);

			tv5.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			
			row.addView(tv5);
			
			totals = new TotalsList();
			totals.setTotal(0);
			
			for (int i = 0; i < payments.size(); i++) {
				final Payment payment = payments.get(i);
				
				long total = (long)(Float.valueOf(payment.payAmount)*100);
				totals.addTotal(total);
				
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				tv3 = new TextView(getActivity());
				tv4 = new TextView(getActivity());
				tv5 = new TextView(getActivity());

				String dateString = "";
				
				if(payment.date != null && !payment.date.equals(""))
					dateString = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(Long.valueOf(payment.date)));
				
				tv1.setText(dateString);
				tv1.setGravity(Gravity.LEFT);
				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv1.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .40f));

				row = new TableRow(getActivity());

				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setPadding(0, 6, 0, 6);
				row.addView(tv1);

				if(payment.processed == 0)
					tv2.setText("PREAUTH");
				else if(payment.processed == 1)
					tv2.setText("APPROVED");
				else if(payment.processed == -1)
					tv2.setText("DELCINED");
				else if(payment.processed == -2)
					tv2.setText("REVERSED");
				else if(payment.processed == -3)
					tv2.setText("VOIDSALE");
				else if(payment.processed == -6)
					tv2.setText("MANUAL");
				else
					tv2.setText("UNKNOWN");
				
				tv2.setGravity(Gravity.LEFT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv2.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv2);
				
				tv3.setText(payment.InvoiceNo);
				tv3.setGravity(Gravity.RIGHT);
				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv3.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv3);
				if(payment.TransCode != null && payment.TransCode.equals("Return"))
				{
					tv4.setText(StoreSetting.getCurrency() +"-"+ payment.payAmount);
				}else{
					tv4.setText(StoreSetting.getCurrency() + payment.payAmount);
				}
				tv4.setGravity(Gravity.RIGHT);
				tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv4.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv4);
				
				tv5.setText("");
				tv5.setGravity(Gravity.RIGHT);
				tv5.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv5.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				Log.v("Date", payment.date);
				Date saleDate = new Date();
				saleDate.setTime(Long.valueOf(payment.date));
				boolean isToday = fmt.format(saleDate).equals(fmt.format(new Date()));
				
				if(isToday)
				{
					if(payment.processed == 1 || payment.processed == -6)
					{
						String htmlString="<u>Void?</u>";
						tv5.setText(Html.fromHtml(htmlString));
	
						tv5.setOnClickListener(new OnClickListener() {
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
									text.setText("Voiding this credit transaction will send a Reversal/VoidSale request to Servers, it will not Void the sale in the History Log.");
									title = "Void Credit Transaction?";
									button = "Void"; 
							    	builder2 = new AlertDialog.Builder(getActivity());
							    	builder2.setView(mylayout)
							    	.setTitle(title)
							    	.setInverseBackgroundForced(true)
							        .setPositiveButton(button, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) { 	
											if(payment.processed == 1)
											{
												if(hasInternet())
												{
													pd = ProgressDialog.show(getActivity(), "", "Processing Reversal", true, false);
													VoidSale = false;
													new ProcessReversal().execute(payment);
												}else{
													alertbox("No Network", "No network connection. Unable to process VoidSale.");
												}
											} else if(payment.processed == -6)
											{
												if(hasInternet())
												{
													pd = ProgressDialog.show(getActivity(), "", "Processing Manual Reversal", true, false);
													VoidSale = false;
													new ProcessManualReversal().execute(payment);
												}else{
													alertbox("No Network", "No network connection. Unable to process VoidSale.");
												}
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
					}
				}

				row.addView(tv5);
			}
		}
	}

	public void clearReport() {
		tl.removeAllViews();
	}

	public void exportReport() {
		
	}
	
	private class ProcessCharge extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) { 
			postData();
			extractXML();
			
			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			
			if (CmdStatus.equals("Declined")) {
				// Error
				tempPayment.processed = -1;
				tempPayment.response = mpsResponse; //"<?xml version=\"1.0\"?><RStream><CmdStatus>Declined</CmdStatus><Authorize>0</Authorize></RStream>";
				ProductDatabase.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				VoidSaleInHistory();
				
			} else if (CmdStatus.equals("Approved")) {
				// Approved
				tempPayment.processed = 1;
				tempPayment.response = mpsResponse; //"<?xml version=\"1.0\"?><RStream><CmdStatus>Approved</CmdStatus><Authorize>"+payAmount+"</Authorize></RStream>";
				ProductDatabase.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				
				JSONObject json = new JSONObject();
				
				SharedPreferences mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
				String license = mSharedPreferences.getString("APOS_LICENSE", "");
				
				try {
					json.put("am", payAmount);			
					json.put("tr", TranCode);
					json.put("id", license);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new SendInfo().execute(json);
				
				endReport();

			} else {
				tempPayment.processed = -4;
				tempPayment.response = mpsResponse; // "<?xml version=\"1.0\"?><RStream><CmdStatus>ERROR</CmdStatus><Authorize>0</Authorize></RStream>";				
				ProductDatabase.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				VoidSaleInHistory();
				alertbox("Error", "Response: " + TextResponse);
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public String postData() {
		try {
			WebRequest mpswr = new WebRequest(PrioritySetting.mWSURL);
			mpswr.addParameter("json", tempPayment.request);
			mpswr.setTimeout(10);

			mpsResponse = mpswr.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mpsResponse;
	}

	public void extractXML() {
		CmdStatus = "Declined";
		if(mpsResponse != null)
		{
			try {
				JSONObject response = new JSONObject(mpsResponse);
				if(response.getString("status") != null &&  response.getString("status").equals("Approved")){
					
					this.CmdStatus = response.getString("status");
					this.TranCode = response.getString("type");
					this.payAmount = response.getString("amount");
				}else {
					
					CmdStatus = response.getString("status");
				}
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			
		}else{
			alertbox("Timed Out", "Connection Times Out.");
		}
	}
	
	/*public class DataHandler extends DefaultHandler {

		// private boolean _inSection, _inArea;

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {

			if (localName.equals("CmdStatus")) {
				CmdStatus = tempVal;
			}

			if (localName.equals("TextResponse")) {
				TextResponse = tempVal;
			}
			
			if (localName.equals("Purchase")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("Authorize")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("TranCode")) {
				TranCode = tempVal;
			}
		}
	}
	*/
	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			}
		}
	};
	public boolean VoidSale;
	
	public void VoidSaleInHistory() {

		new AlertDialog.Builder(getActivity())
				.setMessage(
						"Transaction #" + tempPayment.InvoiceNo + " has returned DECLINED, would you like to void the sale in the History?")
				.setInverseBackgroundForced(true)
				.setTitle("Transaction Declined.")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProductDatabase.voidSale(""+tempPayment.saleID);
								endReport();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						endReport();
					}
				}).show();
	}
	
	
	private class ProcessReversal extends AsyncTask<Payment, Void, Payment> {

		@Override
		protected Payment doInBackground(Payment... payments) {
			Payment payment = payments[0];
			postReversalData(payment);
			//extractReversalXML();
			return payment;
		}

		@Override
		protected void onPostExecute(Payment result) {
			pd.dismiss();	
			if(CmdStatus != null)
			{
				if(CmdStatus.equals("Declined"))
				{

					alertbox("Error", CmdStatus);

				}
				else if(CmdStatus.equals("Approved"))
				{
					
					result.processed = -3;
					
					ProductDatabase.replaceMercurySave(result);
					ProductDatabase.voidSale(String.valueOf(result.saleID));
					buildReport(fromD, toD);
					alertbox("Reversed", CmdStatus);
				}
				else 
				{
					alertbox("Error", CmdStatus);
					VoidSale = false;
				}
			}else 
			{
				alertbox("Error", "Reason: Unknown");
				VoidSale = false;
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
    public String postReversalData(Payment payment) {
    	    	
		try {			
			String url = PrioritySetting.mWSURL_VOID.replace("{id}", payment.RefNo);
			ProductDatabase.insertLog("Void Sale", "RequestUrl:" + url );
			WebRequest mpswr = new WebRequest(url);
			mpswr.setTimeout(100); 
			
			CmdStatus = mpswr.voidRequest();
			ProductDatabase.insertLog("Void Sale", "CmdStatus:" + CmdStatus );
			
		} catch (Exception e) {
			ProductDatabase.insertLog("Void Sale", "in exception:" + e.getMessage() );
			Log.e("Exception", "error occurred while creating xml data");
			e.printStackTrace();
		}
			
		return mpsResponse;
    }

	public void extractReversalXML() {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			ReversalHandler dataHandler = new ReversalHandler();
			xr.setContentHandler(dataHandler);
			
			if(mpsResponse != null)
			{
				Log.v("Resopnse", mpsResponse);			
			
				ByteArrayInputStream in = new ByteArrayInputStream(mpsResponse.getBytes());
				xr.parse(new InputSource(in));
			}
			// data = dataHandler.getData();

		} catch (ParserConfigurationException pce) {
			Log.e("SAX XML", "sax parse error", pce);
		} catch (SAXException se) {
			Log.e("SAX XML", "sax error", se);
		} catch (IOException ioe) {
			Log.e("SAX XML", "sax parse io error", ioe);
		}
	}

	public class ReversalHandler extends DefaultHandler {

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			
		}
		
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
	        tempVal = new String(ch, start, length);
	    }

		@Override
		public void endElement(String namespaceURI, String localName, 
				String qName) throws SAXException {
			
			
			if (localName.equals("CmdStatus")) {
				CmdStatus = tempVal;
			}
			
			if (localName.equals("TextResponse")) {
				TextResponse = tempVal;
			}
			
		}
	}
	
	private boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}

	public void endReport() {
		if(hasInternet())
		{
			ArrayList<Payment> payments = ProductDatabase.getPrioritySale();
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			if(payments.size() >0)
			{
				tempPayment = payments.get(0);
							
				pd = ProgressDialog.show(getActivity(), "", "Processing Invoice #"+tempPayment.InvoiceNo, true, false);
				new ProcessCharge().execute();
			}
		}else{
			alertbox("No Network", "No network connection. Unable to process PreAuths.");
		}
	}
	
	private class ProcessManualReversal extends AsyncTask<Payment, Void, Payment> {

		@Override
		protected Payment doInBackground(Payment... payments) {
			
			Payment payment = payments[0];
			
	    	int stringId = getActivity().getApplicationInfo().labelRes;
		    String version = "";
			try {
				version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
			JSONObject json = new JSONObject();
					
			try {
				json.put("MerchantID", PrioritySetting.hostedMID);	
				json.put("pw", PrioritySetting.hostedPass);
				json.put("PurchaseAmount", payment.payAmount);
				json.put("Invoice", payment.InvoiceNo);
				json.put("RefNo", payment.RefNo);
				json.put("TerminalName", PrioritySetting.terminalName);
				json.put("OperatorID", "Admin");
				json.put("Memo", getString(stringId) + " v"+version);
				json.put("AuthCode", payment.AuthCode);
				json.put("TransCode", payment.TransCode);
				json.put("ProcessData", payment.ProcessData);
				json.put("AcqRefData", payment.AcqRefData);
				json.put("Token", payment.RecordNo);
				json.put("VoidSale", VoidSale);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			postManualData(json);
			//extractXML();
			return payment;
		}

		@Override
		protected void onPostExecute(Payment result) {
			pd.dismiss();		
			
			if(CmdStatus.equals("Declined"))
			{
				if(result.TransCode.equals("Sale"))
				{
					if(VoidSale == false)
					{
						pd = ProgressDialog.show(getActivity(), "", "Processing Manual VoidSale", true, false);
						VoidSale = true;
						new ProcessManualReversal().execute(result);
					}else{
						alertbox("Declined", "Reason: " + TextResponse);
					}
				}else{
					alertbox("Declined", "Reason: " + TextResponse);
				}
				// Error
				//alertbox("Declined", "Reason: " + TextResponse);
			}
			else if(CmdStatus.equals("Approved"))
			{
				if(VoidSale == false)
				{
					result.processed = -2;
				}
				else
				{
					result.processed = -3;
				}
				VoidSale = false;
				ProductDatabase.replaceMercurySave(result);
				buildReport(fromD, toD);
				alertbox("Reversed", TextResponse);
			}
			else 
			{
				// Declined
				alertbox("Error", "Reason: " + TextResponse);
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	
	private StringBuilder inputStreamToString(InputStream is) {
		String rLine = "";
		StringBuilder answer = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		try {
			while ((rLine = rd.readLine()) != null) {
				answer.append(rLine);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}
	
public String postManualData(JSONObject json) {
		
		HttpClient httpclient = new DefaultHttpClient();

		String result = null ;
		try {
			HttpPost httppost = new HttpPost(PrioritySetting.mVOIDURL);

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			Log.v("SEND", json.toString());
			nvp.add(new BasicNameValuePair("json", json.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();
				String jsonResult = inputStreamToString(is).toString();

				Log.v("response", ""+jsonResult);
				
				JSONObject object = new JSONObject(jsonResult);
				
				if(object.has("CreditVoidSaleTokenResult"))
				{
					JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditVoidSaleTokenResult");
					
					if (CreditVoidSaleTokenResult.has("Status")) {
						CmdStatus = CreditVoidSaleTokenResult.getString("Status");
					}
					
					if (CreditVoidSaleTokenResult.has("Message")) {
						TextResponse = CreditVoidSaleTokenResult.getString("Message");
					}
				} else if(object.has("CreditReversalTokenResult"))
				{
					JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditReversalTokenResult");
					
					if (CreditVoidSaleTokenResult.has("Status")) {
						CmdStatus = CreditVoidSaleTokenResult.getString("Status");
					}
					
					if (CreditVoidSaleTokenResult.has("Message")) {
						TextResponse = CreditVoidSaleTokenResult.getString("Message");
					}
				}else if(object.has("CreditVoidReturnTokenResult"))
				{
					JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditVoidReturnTokenResult");
					
					if (CreditVoidSaleTokenResult.has("Status")) {
						CmdStatus = CreditVoidSaleTokenResult.getString("Status");
					}
					
					if (CreditVoidSaleTokenResult.has("Message")) {
						TextResponse = CreditVoidSaleTokenResult.getString("Message");
					}
				}
			
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

private class SendInfo extends AsyncTask<JSONObject, Void, Void> {

	@Override
	protected Void doInBackground(JSONObject... params) {
		JSONObject json = params[0];
		
		HttpParams httpParameters = new BasicHttpParams();
		int timeoutConnection = 4000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = 6000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
										
		HttpClient httpclient = new DefaultHttpClient(httpParameters);

		try {
			HttpPost httppost = new HttpPost("http://prioritypos.azurewebsites.net/approutines/sale.php");

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			Log.v("SEND", json.toString());
			nvp.add(new BasicNameValuePair("send", json.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(nvp));
			httpclient.execute(httppost);


		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(Void... values) {
	}
}
}
