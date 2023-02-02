package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Billing;
import com.pos.passport.model.Shipping;
import com.pos.passport.model.TransactionRequest;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.Utils;
import com.pos.passport.util.WebRequest;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MercuryFragment extends Fragment {
    private int TIMEOUT = 15000;
	private CheckBox useMercury;
	private EditText merchantID;
	private EditText webServicePassword;
	private EditText terminalName;
	private Button mSAVE;
	protected ProgressDialog pd;
	private Button mTEST;
	private ProductDatabase mDb;
	private String mWSURL = "https://w1.mercurypay.net/ws/ws.asmx";
	private final String mCreditTran =
			"<TStream>" +
					"<Transaction>" +
					"<MerchantID>118725340908147</MerchantID>" +
					"<TranType>Credit</TranType>" +
					"<TranCode>Sale</TranCode>" +
					"<InvoiceNo>200</InvoiceNo>" +
					"<RefNo>1</RefNo>" +
					"<Memo>Advantage Mobile POS v0.8.6</Memo>" +
					"<PartialAuth>Allow</PartialAuth>" +
					"<Frequency>OneTime</Frequency>" +
					"<RecordNo>RecordNumberRequested</RecordNo>" +
					"<Account>" +
					"<EncryptedFormat>MagneSafe</EncryptedFormat>" +
					"<AccountSource>Swiped</AccountSource>" +
					"<EncryptedBlock>F40DDBA1F645CC8DB85A6459D45AFF8002C244A0F74402B479ABC9915EC9567C81BE99CE4483AF3D</EncryptedBlock>" +
					"<EncryptedKey>9012090B01C4F200002B</EncryptedKey>" +
					"<Name>MPS TEST</Name>" +
					"</Account>" +
					"<Amount>" +
					"<Purchase>1.00</Purchase>" +
					"</Amount>" +
					"<TerminalName>Android Vizio Tablet</TerminalName>" +
					"<ShiftID>MPS Shift</ShiftID>" +
					"<OperatorID>Brian</OperatorID>" +
					"</Transaction>" +
					"</TStream>";
	private EditText hostedMID;
	private EditText hostedPass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Mercury Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mercury, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		useMercury = (CheckBox) view.findViewById(R.id.useMercury);
		merchantID = (EditText) view.findViewById(R.id.merchantID);
		webServicePassword = (EditText) view.findViewById(R.id.webServicePassword);
		hostedMID = (EditText) view.findViewById(R.id.hostedMID);
		hostedPass = (EditText) view.findViewById(R.id.hostedPass);
		terminalName = (EditText) view.findViewById(R.id.terminal_name_edit_text);

		mSAVE = (Button) view.findViewById(R.id.payanywhere_save);
		mTEST = (Button) view.findViewById(R.id.mercury_test);

		useMercury.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				if (useMercury.isChecked()) {
					enableFields();
				} else {
					disableFields();
				}
			}
		});

		mTEST.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (WebSetting.enabled) {
					//Clearent credit interface does not provide any test charge 
					/*pd = ProgressDialog.show(getActivity(), "", "Sending Test Request...", true,
							false);
					new TestCharge().execute();*/
                    CrearentTestAsyncTask task = new CrearentTestAsyncTask();
                    task.execute();
				} else {
					Utils.alertBox(getActivity(), R.string.txt_mercury_settings, R.string.msg_enable_mercury_settings);
				}
			}
		});

		mSAVE.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (useMercury.isChecked()) {
					if (merchantID.getText().toString().equals("")) {
						Utils.alertBox(getActivity(), R.string.txt_mercury_settings, R.string.msg_need_merchant_id);
						return;
					}

					if (webServicePassword.getText().toString().equals("")) {
						Utils.alertBox(getActivity(), R.string.txt_mercury_settings, R.string.msg_need_web_service_password);
						return;
					}

					if (terminalName.getText().toString().equals("")) {
						Utils.alertBox(getActivity(), R.string.txt_mercury_settings, R.string.msg_need_unique_terminal_name);
						return;
					}

					WebSetting.enabled = true;
					WebSetting.merchantID = merchantID.getText().toString();
					WebSetting.webServicePassword = webServicePassword.getText().toString();
					WebSetting.terminalName = terminalName.getText().toString();
					WebSetting.hostedMID = hostedMID.getText().toString();
					WebSetting.hostedPass = hostedPass.getText().toString();
					mDb.insertMercurySettings();
				} else {
					WebSetting.enabled = false;
					WebSetting.merchantID = "";
					WebSetting.webServicePassword = "";
					WebSetting.terminalName = "";
					WebSetting.hostedMID = "";
					WebSetting.hostedPass = "";
                    mDb.insertMercurySettings();
				}

				Utils.alertBox(getActivity(), R.string.txt_swipe_settings, R.string.txt_settings_saved);
			}
		});

		if (WebSetting.enabled) {
			useMercury.setChecked(true);
			enableFields();
		} else {
			disableFields();
		}

		return view;
	}

	protected void enableFields() {
		merchantID.setEnabled(true);
		webServicePassword.setEnabled(true);
		terminalName.setEnabled(true);
		hostedMID.setEnabled(true);
		hostedPass.setEnabled(true);
		merchantID.setText(WebSetting.merchantID);
		webServicePassword.setText(WebSetting.webServicePassword);
		hostedMID.setText(WebSetting.hostedMID);
		hostedPass.setText(WebSetting.hostedPass);
		terminalName.setText(WebSetting.terminalName);
		merchantID.setEnabled(false);
		webServicePassword.setEnabled(false);
		terminalName.setEnabled(false);
		hostedMID.setEnabled(false);
		hostedPass.setEnabled(false);
	}

	protected void disableFields() {
		merchantID.setText("");
		webServicePassword.setText("");
		terminalName.setText("");
		hostedMID.setText("");
		hostedPass.setText("");

		merchantID.setEnabled(false);
		webServicePassword.setEnabled(false);
		terminalName.setEnabled(false);
		hostedMID.setEnabled(false);
		hostedPass.setEnabled(false);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			}
		}
	};

	public String mpsResponse;
	public String mCmdStatus;
	public String mTextResponse;
	public String mExtData;
	public String mMessage;
	public String mRespMSG;
	public String mResult;
	public String mAuthCode;

	// Clearent does not provide any test charge
	private class TestCharge extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {

			XmlSerializer serializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			try {
				serializer.setOutput(writer);
				serializer.startTag(null, "UserName");
				serializer.text(WebSetting.merchantID);
				serializer.endTag(null, "UserName");
				serializer.startTag(null, "Password");
				serializer.text(WebSetting.webServicePassword);
				serializer.endTag(null, "Password");
				serializer.startTag(null, "TransType");
				serializer.text("StatusCheck");
				serializer.endTag(null, "TransType");
				serializer.startTag(null, "ExtData");
				serializer.text("");
				serializer.endTag(null, "ExtData");
				serializer.endDocument();

				String postXml = writer.toString();

				Log.v("Response", "" + postXml);

				if (Utils.hasInternet(getActivity())) {
					try {
						WebRequest mpswr = new WebRequest(WebSetting.mWSURL);
						mpswr.addParameter("tran", postXml);
						//mpswr.addParameter("pw", WebSetting.webServicePassword);
						mpswr.setWebMethodName("GetInfo");
						mpswr.setTimeout(10);

						mpsResponse = mpswr.sendRequest();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				Log.e("Exception", "error occurred while creating xml data");
				e.printStackTrace();
			}

			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);

			if (Utils.hasInternet(getActivity())) {
				extractXML();

				Log.v("Response", "" + result);
				if (mResult.equals("0")){
					Utils.alertBox(getActivity(), R.string.txt_approved, R.string.txt_approved_transaction);
				} else if (mResult.equals("1")){
					Utils.alertBox(getActivity(), R.string.txt_fail, String.format(getString(R.string.msg_transaction_failed), mResult));
				} else if (mResult.equals("2")){
					Utils.alertBox(getActivity(), R.string.txt_reject, String.format(getString(R.string.msg_transaction_rejected), mResult));
				} else if (mResult.equals("14")){
					Utils.alertBox(getActivity(), R.string.txt_reject, String.format(getString(R.string.msg_transaction_not_supported), mResult));
				} else if (mResult.equals("99")){
					Utils.alertBox(getActivity(), R.string.txt_unknown, String.format(getString(R.string.msg_transaction_unknown_failure), mResult));
				} else if (mResult.equals("1002")){
					Utils.alertBox(getActivity(), R.string.txt_error, String.format(getString(R.string.msg_transaction_error), mResult));
				} else {
					Utils.alertBox(getActivity(), R.string.txt_error, R.string.txt_reason_label + mResult);
				}
			} else {
				Utils.alertBox(getActivity(), R.string.txt_error, R.string.msg_no_internet_connection);
			}


		}

		@Override
		protected void onPreExecute() { }

		@Override
		protected void onProgressUpdate(Void... values) { }
	}

	public void extractXML() {
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
	}

	public class DataHandler extends DefaultHandler {
		// private boolean _inSection, _inArea;
		private String authVal;
		private String tempVal;
		private String respVal;
		private String messageVal;

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

			if (localName.equals("Result")) {
				MercuryFragment.this.mResult = tempVal;
			}
			if (localName.equals("RespMSG")) {
				mRespMSG = respVal;
			}
			if (localName.equals("ExtData")) {
				MercuryFragment.this.mExtData = tempVal;
			}
			if (localName.equals("Message")) {
				mMessage = messageVal;
			}
			if (localName.equals("AuthCode")) {
				mAuthCode = authVal;
			}
		}
	}

    private class CrearentTestAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                TransactionRequest transaction_request = new TransactionRequest();
                transaction_request.api_key = "5ab421b29dff4d8a98ea2b647d30e5e4";
                transaction_request.type = "SALE";
                transaction_request.card = "4111111111111111";
                transaction_request.csc = "123";
                transaction_request.exp_date = "1121";
                transaction_request.amount = "10.00";
                transaction_request.avs_address = "112 N. Orion Court";
                transaction_request.avs_zip = "20210";
                transaction_request.purchase_order = "10";
                transaction_request.invoice = "100";
                transaction_request.email = "bkim@polypay.com";
                transaction_request.customer_id = "25";
                transaction_request.order_number = "1000";
                transaction_request.client_ip = "";
                transaction_request.description = "Cel Phone";
                transaction_request.comments = "Electronic Product";

                transaction_request.billing = new Billing();
                transaction_request.billing.first_name = "Joe";
                transaction_request.billing.last_name = "Smith";
                transaction_request.billing.company = "Company Inc.";
                transaction_request.billing.street = "Street 1";
                transaction_request.billing.street2 = "Street 2";
                transaction_request.billing.city = "Jersey City";
                transaction_request.billing.state = "NJ";
                transaction_request.billing.zip = "07097";
                transaction_request.billing.country = "USA";
                transaction_request.billing.phone = "123456789";

                transaction_request.shipping = new Shipping();
                transaction_request.shipping.first_name = "Joe";
                transaction_request.shipping.last_name = "Smith";
                transaction_request.shipping.company = "Company 2 Inc.";
                transaction_request.shipping.street = "Street 1 2";
                transaction_request.shipping.street2 = "Street 2 2";
                transaction_request.shipping.city = "Colorado City";
                transaction_request.shipping.state = "TX";
                transaction_request.shipping.zip = "79512";
                transaction_request.shipping.country = "USA";
                transaction_request.shipping.phone = "123456789";

                trustAllSSL();

                URL url = new URL("https://sandbox.thesecuregateway.com/rest/v1/transactions");
                URLConnection uc = url.openConnection();
                HttpsURLConnection conn = (HttpsURLConnection) uc;
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");

                String request;
                transaction_request.avs_address=null;
                transaction_request.avs_zip=null;

                Gson gson = new Gson();
                request = gson.toJson(transaction_request);
                PrintWriter pw = new PrintWriter(conn.getOutputStream());
                pw.write(request);
                pw.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = "";
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response += inputLine;
                in.close();

                return response;
            } catch (Exception ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
        }
    };

    public static void trustAllSSL(){
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
