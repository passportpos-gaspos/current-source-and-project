package com.pos.passport.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
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

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.model.Payment;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.TotalsList;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.Consts;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;
import com.pos.passport.util.WebRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MercuryReportFragment extends Fragment implements Runnable {
    private final static String DEBUG_TAG = "[MercuryReportFragment]";
	private TableLayout tl;
	protected ProgressDialog pd;
	private int todo;
	private long fromD;
	private long toD;
	private TotalsList totals;
	private DecimalFormat nf;
	private Payment tempPayment;
	private String mpsResponse;
	public String mCmdStatus;
	public String mTextResponse;
	public String mAuthorize;
	public String payAmount;
	public String mTranCode;
	private ProductDatabase mDb;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Mercury Sales Fragment");
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
	}
	
	@Override
	public void onResume() {
		Log.v("Mercury Sales", "Resumed");
		super.onResume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport(fromD, toD);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.reportsview, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	@Override
	public void run() {
		
	}

	public void setDates(long l, long m) {
		this.fromD = l;
		this.toD = m;
	}
	
	private void addLine(TableLayout tl) {
		TableRow line = new TableRow(getActivity());

		tl.addView(line);
		LayoutParams layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		line.setLayoutParams(layoutParams);
		line.setPadding(5, 0, 5, 1);
		line.setBackgroundColor(Color.BLACK);
	}
	
	protected void buildReport(long l, long m) {
		ArrayList<Payment> payments = mDb.getMercurySales(l, m);

		tl.removeAllViews();
		if (payments.size() > 0) {

			TextView tv1 = new TextView(getActivity());
			TextView tv2 = new TextView(getActivity());
			TextView tv3 = new TextView(getActivity());
			TextView tv4 = new TextView(getActivity());
			TextView tv5 = new TextView(getActivity());

			tv1.setText(R.string.txt_date);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv1.setTypeface(null, Typeface.BOLD);

			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .4f));

			TableRow row = new TableRow(getActivity());

			tl.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 6);
			row.addView(tv1);

			tv2.setText(R.string.txt_processed);
			tv2.setGravity(Gravity.START);
			tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv2.setTypeface(null, Typeface.BOLD);

			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv2);
			
			tv3.setText(R.string.txt_invoice);
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv3.setTypeface(null, Typeface.BOLD);

			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv3);
			
			tv4.setText(R.string.txt_amount);
			tv4.setGravity(Gravity.END);
			tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv4.setTypeface(null, Typeface.BOLD);

			tv4.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			row.addView(tv4);

			tv5.setText("");
			tv5.setGravity(Gravity.END);
			tv5.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
			tv5.setTypeface(null, Typeface.BOLD);

			tv5.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
			
			row.addView(tv5);
			
			totals = new TotalsList();
			totals.setTotal(BigDecimal.ZERO);
			
			for (int i = 0; i < payments.size(); i++) {
				final Payment payment = payments.get(i);
				
				BigDecimal total = new BigDecimal(payment.payAmount).multiply(Consts.HUNDRED);
				totals.addTotal(total);
				
				tv1 = new TextView(getActivity());
				tv2 = new TextView(getActivity());
				tv3 = new TextView(getActivity());
				tv4 = new TextView(getActivity());
				tv5 = new TextView(getActivity());

				String dateString = "";
				
				if (payment.date != null && !payment.date.equals(""))
					dateString = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(Long.valueOf(payment.date)));
				
				tv1.setText(dateString);
				tv1.setGravity(Gravity.START);
				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .40f));

				row = new TableRow(getActivity());

				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setPadding(0, 6, 0, 6);
				row.addView(tv1);

				if(payment.processed == 0)
					tv2.setText(R.string.txt_pre_auth_cap);
				else if(payment.processed == 1)
					tv2.setText(R.string.txt_approved_cap);
				else if(payment.processed == -1)
					tv2.setText(R.string.txt_declined_cap);
				else if(payment.processed == -2)
					tv2.setText(R.string.txt_reversed_cap);
				else if(payment.processed == -3)
					tv2.setText(R.string.txt_void_sale_cap);
				else if(payment.processed == -6)
					tv2.setText(R.string.txt_manual_cap);
				else
					tv2.setText(R.string.txt_unknown_cap);
				
				tv2.setGravity(Gravity.START);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv2);
				
				tv3.setText(payment.invoiceNo.substring(0, payment.invoiceNo.length()-1)+"-"+payment.invoiceNo.substring(payment.invoiceNo.length()-1,payment.invoiceNo.length()));
				tv3.setGravity(Gravity.END);
				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv3);
				if (payment.transCode != null && payment.transCode.equals(getString(R.string.txt_return)))  {
					tv4.setText(StoreSetting.getCurrency() + "-" + payment.payAmount);
				} else {
					tv4.setText(StoreSetting.getCurrency() + payment.payAmount);
				}
				tv4.setGravity(Gravity.END);
				tv4.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv4.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				row.addView(tv4);
				
				tv5.setText("");
				tv5.setGravity(Gravity.END);
				tv5.setTextAppearance(getActivity(), R.style.textLayoutAppearance);

				tv5.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, .15f));
				
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				Log.v("Date", payment.date);
				Date saleDate = new Date();
				saleDate.setTime(Long.valueOf(payment.date));
				boolean isToday = fmt.format(saleDate).equals(fmt.format(new Date()));
				
				if (isToday) {
					if (payment.processed == 1 || payment.processed == -6) {
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
								text.setText(R.string.msg_send_void_sale_to_mercury);
								title = getString(R.string.txt_void_credit_transaction);
								button = getString(R.string.txt_void);
								builder2 = new AlertDialog.Builder(getActivity());
								builder2.setView(mylayout)
                                    .setTitle(title)
                                    .setPositiveButton(button, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (payment.processed == 1) {
                                                if (Utils.hasInternet(getActivity())) {
                                                    pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_processing_reversal), true, false);
                                                    mVoidSale = false;
                                                    new ProcessReversal().execute(payment);
                                                } else {
                                                    Utils.alertBox(getActivity(), R.string.txt_no_network, R.string.msg_unable_to_void_sale);
                                                }
                                            } else if (payment.processed == -6) {
                                                if (Utils.hasInternet(getActivity())) {
                                                    pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_processing_reversal), true, false);
                                                    mVoidSale = false;
                                                    new ProcessManualReversal().execute(payment);
                                                } else {
                                                    Utils.alertBox(getActivity(), R.string.txt_no_network, R.string.msg_unable_to_void_sale);
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
			
			if (mCmdStatus.equals(getString(R.string.txt_declined))) {
				// Error
				tempPayment.processed = -1;
				tempPayment.response = mpsResponse; //"<?xml version=\"1.0\"?><RStream><CmdStatus>Declined</CmdStatus><Authorize>0</Authorize></RStream>";
                mDb.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				VoidSaleInHistory();
				
			} else if (mCmdStatus.equals(getString(R.string.txt_approved))) {
				// Approved
				tempPayment.processed = 1;
				tempPayment.response = mpsResponse; //"<?xml version=\"1.0\"?><RStream><CmdStatus>Approved</CmdStatus><Authorize>"+payAmount+"</Authorize></RStream>";
                mDb.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				
				JSONObject json = new JSONObject();

				LoginCredential credential = PrefUtils.getLoginCredential(getActivity());
				String UID = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID);
				
				try {
					json.put("am", payAmount);			
					json.put("tr", mTranCode);
					json.put("id", credential.getEmail());
					json.put("key", credential.getKey());
					json.put("user", UID);
					json.put("in", tempPayment.invoiceNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				new SendInfo().execute(json);
				
				endReport();

			} else {
				tempPayment.processed = -4;
				tempPayment.response = mpsResponse; // "<?xml version=\"1.0\"?><RStream><CmdStatus>ERROR</CmdStatus><Authorize>0</Authorize></RStream>";				
                mDb.replaceMercurySave(tempPayment);
				buildReport(fromD, toD);
				VoidSaleInHistory();
				Utils.alertBox(getActivity(), R.string.txt_error, R.string.txt_response_label + mTextResponse);
			}
		}
	}

	public String postData() {
		try {
			WebRequest mpswr = new WebRequest(WebSetting.mWSURL);
			mpswr.addParameter("tran", tempPayment.request);
			mpswr.addParameter("pw", WebSetting.webServicePassword);
			mpswr.setWebMethodName("CreditTransaction");
			mpswr.setTimeout(10);

			mpsResponse = mpswr.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mpsResponse;
	}

	public void extractXML() {
		if (mpsResponse != null) {
		// sax stuff
			try {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
	
				XMLReader xr = sp.getXMLReader();
	
				DataHandler dataHandler = new DataHandler();
				xr.setContentHandler(dataHandler);
				ByteArrayInputStream in = new ByteArrayInputStream(
						mpsResponse.getBytes());
				xr.parse(new InputSource(in));
	
				// data = dataHandler.getData();
			} catch (ParserConfigurationException pce) {
				Log.e("SAX XML", "sax parse error", pce);
			} catch (SAXException se) {
				Log.e("SAX XML", "sax error", se);
			} catch (IOException ioe) {
				Log.e("SAX XML", "sax parse io error", ioe);
			}
		} else {
			Utils.alertBox(getActivity(), R.string.txt_timed_out, R.string.msg_connection_times_out);
		}
	}
	
	public class DataHandler extends DefaultHandler {

		// private boolean _inSection, _inArea;

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {

			if (localName.equals("CmdStatus")) {
				mCmdStatus = tempVal;
			}

			if (localName.equals("TextResponse")) {
				mTextResponse = tempVal;
			}
			
			if (localName.equals("Purchase")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("Authorize")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("TranCode")) {
				mTranCode = tempVal;
			}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			}
		}
	};
	public boolean mVoidSale;
	
	public void VoidSaleInHistory() {

		new AlertDialog.Builder(getActivity())
			.setMessage(String.format(getString(R.string.msg_mercury_declined_transaction), tempPayment.invoiceNo))
            .setTitle(R.string.msg_transaction_declined)
            .setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mDb.voidSale(tempPayment.invoiceNo.substring(0, tempPayment.invoiceNo.length() - 1));
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
			extractReversalXML();
			return payment;
		}

		@Override
		protected void onPostExecute(Payment result) {
			pd.dismiss();	
			if (mCmdStatus != null) {
				if (mCmdStatus.equals(getString(R.string.txt_declined))) {
					if (result.transCode.equals(getString(R.string.txt_sale))) {
						if (!mVoidSale) {
							pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_processing_void_sale), true, false);
							mVoidSale = true;
							new ProcessReversal().execute(result);
						} else {
							Utils.alertBox(getActivity(), R.string.txt_declined, getString(R.string.txt_reason_label) + mTextResponse);
						}
					}else{
						Utils.alertBox(getActivity(), R.string.txt_declined, getString(R.string.txt_reason_label) + mTextResponse);
					}
				} else if(mCmdStatus.equals("Approved")) {
					if (!mVoidSale) {
						result.processed = -2;
					} else {
						result.processed = -3;
					}
					mVoidSale = false;
					mDb.replaceMercurySave(result);
					buildReport(fromD, toD);
					Utils.alertBox(getActivity(), R.string.txt_reversed, mTextResponse);
				} else {
					Utils.alertBox(getActivity(), R.string.txt_error, getString(R.string.txt_reason_label) + mTextResponse);
					mVoidSale = false;
				}
			} else {
				Utils.alertBox(getActivity(), R.string.txt_error, R.string.txt_reason_unknown);
				mVoidSale = false;
			}
		}
	}
	
    public String postReversalData(Payment payment) {
    	    	
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument(null, true);
			//serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			serializer.startTag("", "TStream");
			serializer.startTag("", "Transaction");

			serializer.startTag("", "MerchantID");
            serializer.text(WebSetting.merchantID);
			serializer.endTag("", "MerchantID");
			
			serializer.startTag("", "TranType");
            serializer.text("Credit");
			serializer.endTag("", "TranType");

			if (payment.transCode.equals("Return")) {
				serializer.startTag("", "TranCode");
                serializer.text("VoidReturnByRecordNo");
				serializer.endTag("", "TranCode");
			} else {
				serializer.startTag("", "TranCode");
                serializer.text("VoidSaleByRecordNo");
				serializer.endTag("", "TranCode");
			}
			
			serializer.startTag("", "InvoiceNo");
            serializer.text(payment.invoiceNo);
			serializer.endTag("", "InvoiceNo");
		
			serializer.startTag("", "RefNo");
            serializer.text(payment.invoiceNo);
			serializer.endTag("", "RefNo");
			
		    int stringId = getActivity().getApplicationInfo().labelRes;
		    String version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		    
			serializer.startTag("", "Memo");
            serializer.text(getString(stringId) + " v"+version);
			serializer.endTag("", "Memo");
			
			serializer.startTag("", "RecordNo");
            serializer.text(payment.recordNo);
			serializer.endTag("", "RecordNo");
				
			serializer.startTag("", "Frequency");
            serializer.text("OneTime");
			serializer.endTag("", "Frequency");
		
			serializer.startTag("", "Amount");
            serializer.startTag("", "Purchase");
            serializer.text(payment.payAmount);
            serializer.endTag("", "Purchase");
			serializer.endTag("", "Amount");
		

			serializer.startTag("", "TranInfo");
            serializer.startTag("", "AuthCode");
            serializer.text(payment.authCode);
            serializer.endTag("", "AuthCode");
            if (!payment.transCode.equals("Return") && !mVoidSale) {
				serializer.startTag("", "AcqRefData");
                serializer.text(payment.acqRefData);
				serializer.endTag("", "AcqRefData");
				
				serializer.startTag("", "ProcessData");
                serializer.text(payment.processData);
				serializer.endTag("", "ProcessData");
            }
			serializer.endTag("", "TranInfo");
	
			serializer.startTag("", "TerminalName");
            serializer.text(WebSetting.terminalName);
			serializer.endTag("", "TerminalName");
			
			serializer.startTag("", "OperatorID");
            serializer.text("Admin");
			serializer.endTag("", "OperatorID");

			serializer.endTag("", "Transaction");
			serializer.endTag("", "TStream");
			serializer.endDocument();

			String result = writer.toString();

			Log.v("Request", "" + result);

			try {
				WebRequest mpswr = new WebRequest(WebSetting.mWSURL);
				mpswr.addParameter("tran", result); 
				mpswr.addParameter("pw", WebSetting.webServicePassword); 
				mpswr.setWebMethodName("CreditTransaction"); 
				mpswr.setTimeout(10); 

				mpsResponse = mpswr.sendRequest();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
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
			
			if (mpsResponse != null) {
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
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			
		}
		
	    public void characters(char[] ch, int start, int length) throws SAXException {
	        tempVal = new String(ch, start, length);
	    }

		@Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			if (localName.equals("CmdStatus")) {
				mCmdStatus = tempVal;
			}
			
			if (localName.equals("TextResponse")) {
				mTextResponse = tempVal;
			}
		}
	}

	public void endReport() {
		if(Utils.hasInternet(getActivity())) {
			ArrayList<Payment> payments = mDb.getMercurySale();
			
			if (pd != null && pd.isShowing())
				pd.cancel();
			
			if (payments.size() >0) {
				tempPayment = payments.get(0);
							
				pd = ProgressDialog.show(getActivity(), "", String.format(getString(R.string.msg_processing_invoice), tempPayment.invoiceNo), true, false);
				new ProcessCharge().execute();
			}
		} else {
			Utils.alertBox(getActivity(), R.string.txt_no_network, R.string.txt_unable_to_process_pre_auth);
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
				e1.printStackTrace();
			}
	    	
			JSONObject json = new JSONObject();
					
			try {
				json.put("MerchantID", WebSetting.hostedMID);	
				json.put("pw", WebSetting.hostedPass);
				json.put("PurchaseAmount", payment.payAmount);
				json.put("Invoice", payment.invoiceNo);
				json.put("RefNo", payment.refNo);
				json.put("TerminalName", WebSetting.terminalName);
				json.put("OperatorID", "Admin");
				json.put("Memo", getString(stringId) + " v"+version);
				json.put("AuthCode", payment.authCode);
				json.put("TransCode", payment.transCode);
				json.put("ProcessData", payment.processData);
				json.put("AcqRefData", payment.acqRefData);
				json.put("Token", payment.recordNo);
				json.put("VoidSale", mVoidSale);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			postManualData(json);
			return payment;
		}

		@Override
		protected void onPostExecute(Payment result) {
			pd.dismiss();		
			
			if (mCmdStatus.equals(getString(R.string.txt_declined))) {
				if (result.transCode.equals(getString(R.string.txt_sale))) {
					if (!mVoidSale) {
						pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_processing_manual_void_sale), true, false);
						mVoidSale = true;
						new ProcessManualReversal().execute(result);
					} else {
						Utils.alertBox(getActivity(), R.string.txt_declined, getString(R.string.txt_reason_label) + mTextResponse);
					}
				} else {
					Utils.alertBox(getActivity(), R.string.txt_declined, getString(R.string.txt_reason_label) + mTextResponse);
				}
			} else if(mCmdStatus.equals("Approved")) {
				if(!mVoidSale) {
					result.processed = -2;
				} else {
					result.processed = -3;
				}
				mVoidSale = false;
				mDb.replaceMercurySave(result);
				buildReport(fromD, toD);
				Utils.alertBox(getActivity(), R.string.txt_reversed, mTextResponse);
			} else {
				// Declined
				Utils.alertBox(getActivity(), R.string.txt_error, getString(R.string.txt_reason_label) + mTextResponse);
			}
		}
	}

	public String postManualData(JSONObject json) {
		try {
            List<RestAgent.Parameter> parameters = new ArrayList<>();
            parameters.add(new RestAgent.Parameter("json", json.toString()));
            URL url = new URL(WebSetting.mVOIDURL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setConnectTimeout(4000);
            c.setReadTimeout(6000);

            c.setDoOutput(true);
            OutputStream os = c.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(Utils.joinParameters(parameters, false));
            writer.flush();
            writer.close();
            os.close();

            c.connect();

            String jsonResult = null;
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                is = new BufferedInputStream(c.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine + "\n");
                }
                jsonResult = sb.toString();

                JSONObject object = new JSONObject(jsonResult);

                if (object.has("CreditVoidSaleTokenResult")) {
                    JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditVoidSaleTokenResult");

                    if (CreditVoidSaleTokenResult.has("Status")) {
                        mCmdStatus = CreditVoidSaleTokenResult.getString("Status");
                    }

                    if (CreditVoidSaleTokenResult.has("Message")) {
                        mTextResponse = CreditVoidSaleTokenResult.getString("Message");
                    }
                } else if (object.has("CreditReversalTokenResult")) {
                    JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditReversalTokenResult");

                    if (CreditVoidSaleTokenResult.has("Status")) {
                        mCmdStatus = CreditVoidSaleTokenResult.getString("Status");
                    }

                    if (CreditVoidSaleTokenResult.has("Message")) {
                        mTextResponse = CreditVoidSaleTokenResult.getString("Message");
                    }
                } else if (object.has("CreditVoidReturnTokenResult")) {
                    JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditVoidReturnTokenResult");

                    if (CreditVoidSaleTokenResult.has("Status")) {
                        mCmdStatus = CreditVoidSaleTokenResult.getString("Status");
                    }

                    if (CreditVoidSaleTokenResult.has("Message")) {
                        mTextResponse = CreditVoidSaleTokenResult.getString("Message");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.v(DEBUG_TAG, "Error reading InputStream");
                jsonResult = null;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

		return null;
	}

	private class SendInfo extends AsyncTask<JSONObject, Void, Void> {
		@Override
		protected Void doInBackground(JSONObject... params) {
            JSONObject json = params[0];

            try {
                List<RestAgent.Parameter> parameters = new ArrayList<>();
                parameters.add(new RestAgent.Parameter("send", json.toString()));
                URL url = new URL(UrlProvider.BASE_URL + UrlProvider.CLOUD_SALE_URL);
                HttpURLConnection c = (HttpURLConnection)url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(4000);
                c.setReadTimeout(6000);

                c.setDoOutput(true);
                OutputStream os = c.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(Utils.joinParameters(parameters, false));
                writer.flush();
                writer.close();
                os.close();

                c.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
		}
	}
}
