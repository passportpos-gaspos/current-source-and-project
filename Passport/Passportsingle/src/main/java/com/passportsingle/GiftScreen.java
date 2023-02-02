package com.passportsingle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;


import com.passportsingle.R;
import com.passportsingle.web.WebRequest;
import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;
import com.magtek.mobile.android.scra.ConfigParam;
import com.magtek.mobile.android.scra.MTSCRAException;
import com.magtek.mobile.android.scra.MagTekSCRA;
import com.magtek.mobile.android.scra.ProcessMessageResponse;
import com.magtek.mobile.android.scra.SCRAConfiguration;
import com.magtek.mobile.android.scra.SCRAConfigurationDeviceInfo;
import com.magtek.mobile.android.scra.SCRAConfigurationReaderType;
import com.magtek.mobile.android.scra.StatusCode;

import com.mercurypay.ws.sdk.MPSWebRequest;
//import com.starmicronics.stario.PortInfo;

public class GiftScreen extends AppCompatActivity {

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int STATUS_IDLE = 1;
	public static final int STATUS_PROCESSCARD = 2;

	public static final int SWIPE_STATE_NEEDCONNECT = 1;
	public static final int SWIPE_STATE_CANSWIPE = 2;
	public static final int SWIPE_STATE_PROCESSING = 3;

	public static final int DEVICE_STATUS_CONNECTED_SUCCESS = 0;
	public static final int DEVICE_STATUS_CONNECTED_FAIL = 1;
	public static final int DEVICE_STATUS_CONNECTED_PERMISSION_DENIED = 2;	
	public static final int DEVICE_MESSAGE_CARDDATA_CHANGE =3;	
	public static final int DEVICE_STATUS_CONNECTED = 4;
	public static final int DEVICE_STATUS_DISCONNECTED = 5;

	public String bluetoothAddress = "";
	
	public static final String CONFIG_FILE = "MTSCRADevConfig.cfg";
	private static final int CONFIGWS_READERTYPE = 0;
	private static final String CONFIGWS_USERNAME = "magtek";
	private static final String CONFIGWS_PASSWORD = "p@ssword";
	public static final String CONFIGWS_URL = "https://deviceconfig.magensa.net/service.asmx";//Production URL
	
	private MagTekSCRA mMTSCRA;
	private Handler mSCRADataHandler = new Handler(new SCRAHandlerCallback());
	private Handler mReaderDataHandler = new Handler(new MtHandlerCallback());
	final headSetBroadCastReceiver mHeadsetReceiver = new headSetBroadCastReceiver();
	final NoisyAudioStreamReceiver mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
	public static final int MENU_SCAN = Menu.FIRST;
	private BluetoothAdapter mBtAdapter;
	private MagTeklibDynamag MagTeklibDynamag;
	public int location = 0;

	String TransType = "SALE";

	private String totalAmount = "1.00";
	private String invoice = "124";

	private ProgressDialog pd;

	private long mLongTimerInterval;
	private int mIntCurrentDeviceStatus;
	private int swipeState;

	private int mIntCurrentStatus;
	private Set<BluetoothDevice> pairedDevices;
	private TextView mReaderNameTextView;
	private TextView mChargeAmountTextView;
	private TextView mInvoiceTextView;
	private TextView mReaderStatusTextView;
	private TextView mSwipeStatusTextView;
	private Button reconnectReader;
	private String cashier = "test";
	private TableRow readerStatusColor;

	private String origin = "";
	private String CmdStatus = "";
	private String TextResponse = "";

	private DecimalFormat df;
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private ImageView readerImage;
	private AudioManager mAudioMgr;
	private int mIntCurrentVolume;

	private static final String ACTION_USB_PERMISSION =
		    "com.android.example.USB_PERMISSION";
	
	final Handler mUIProcessCardHandler = new Handler();
	private SharedPreferences mSharedPreferences;
	private String bullet;
	private int saleID;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chargescreen);
		
		lockOrientation(this);
		
		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
		bullet = mSharedPreferences.getString("BulletAddress", "");
		
		df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(false);

		mReaderStatusTextView = (TextView) findViewById(R.id.readerStatus);
		mReaderNameTextView = (TextView) findViewById(R.id.readerName);
		mChargeAmountTextView = (TextView) findViewById(R.id.chargeAmount);
		mInvoiceTextView = (TextView) findViewById(R.id.invoiceNumber);
		mSwipeStatusTextView = (TextView) findViewById(R.id.swipeStatus);
		reconnectReader = (Button) findViewById(R.id.reconnectReader);
		readerStatusColor = (TableRow) findViewById(R.id.readerStatusColor);
		readerImage = (ImageView) findViewById(R.id.readerImage);
		readerImage.setVisibility(View.INVISIBLE);
		
		reconnectReader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchingBT = false;
				doDiscovery();
			}
		});
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.getString("InvoiceID") != null) {
				invoice = bundle.getString("InvoiceID");
				mInvoiceTextView.setText("Invoice: #" + invoice);
			}
			if (bundle.getString("Cashier") != null) {
				cashier = bundle.getString("Cashier");
			}
			if (bundle.getString("Amount") != null) {
				totalAmount = bundle.getString("Amount");
				mChargeAmountTextView.setText("Charge: "
						+ StoreSetting.getCurrency() + totalAmount);
			}
			if (bundle.containsKey("saleID") == true) {
				saleID = bundle.getInt("saleID");
			}
		}
		
		boolean foundDevice = false;
		reconnectReader.setVisibility(View.GONE);
		mReaderNameTextView.setText("No Reader Connected");
		
		if(!foundDevice)
		{
			Log.v("DEVICE", "Looking for audio device...");
			setStatus("Looking in audio...", Color.RED);
			
			mMTSCRA = new MagTekSCRA(mSCRADataHandler);
			mAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);	
			InitializeData();
			mAudioMgr.setWiredHeadsetOn(true);
			int origionalVolume = mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
			mIntCurrentVolume = mAudioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
			
			if(mAudioMgr.isWiredHeadsetOn()) {
				setStatus("Audio Device In...", Color.MAGENTA);
				foundDevice = true;
				doDiscovery();
	    	}
		}
		
		if(!foundDevice)
		{
			setStatus("Looking in bluetooth...", Color.RED);
			mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			mMTSCRA = new MagTekSCRA(mSCRADataHandler);
			if(mBtAdapter != null)
			{
				foundDevice = true;
				pairedDevices = mBtAdapter.getBondedDevices();			
				doDiscovery();
			}
			
			if(!mMTSCRA.isDeviceConnected())
			{
				foundDevice = false;
	    	}
		}
		
		if(!foundDevice)
		{
			UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

			mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			//registerReceiver(mUsbReceiver, filter);	
					
			String devices = "";
	    
		    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		    while(deviceIterator.hasNext()){
		        UsbDevice device = deviceIterator.next();
		        
		        if(device.getVendorId() == 2049 && device.getProductId() == 17)
		        {
		    	    mReaderNameTextView.setText("Dynamag HID Card Reader");
		    		readerImage.setVisibility(View.VISIBLE);
		    		readerImage.setImageResource(R.drawable.dynamag);
		    		MagTeklibDynamag = new MagTeklibDynamag(this, mReaderDataHandler);
		    		MagTeklibDynamag.openDevice();
		    		foundDevice = true;
		        }
		    }
	    }
		
		if(!foundDevice)
		{
			setStatus("No compatible devices", Color.RED);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.charge_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_keyin:
								
				Intent i = new Intent(this, KeyInScreen.class);
				i.putExtra("InvoiceID", invoice);
				i.putExtra("Amount", totalAmount);
				i.putExtra("Cashier", cashier);
				startActivityForResult(i, 1021);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setStatus(String string, int lpiColor) {
		Message msg = new Message();
		msg.obj = string;
		StatusUpdateHandler.sendMessage(msg);

		msg = new Message();
		msg.what = lpiColor;
		ColorUpdateHandler.sendMessage(msg);
	}

	private Handler StatusUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (((String) msg.obj).equals("")) {
				if (mMTSCRA.isDeviceConnected()) {
					mReaderStatusTextView.setText("Status: Connected");
				} else {
					mReaderStatusTextView.setText("Status: Disconnected");
				}
			} else {
				mReaderStatusTextView.setText((String) msg.obj);
			}

			if (swipeState == SWIPE_STATE_NEEDCONNECT) {
				mSwipeStatusTextView.setText("Connect to Reader");
			}

			if (swipeState == SWIPE_STATE_CANSWIPE) {
				mSwipeStatusTextView.setText("Swipe Card Now");
			}

			if (swipeState == SWIPE_STATE_PROCESSING) {
				mSwipeStatusTextView.setText("Processing Card");
			}
			mLongTimerInterval = 0;
		}
	};

	private Handler ColorUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			readerStatusColor.setBackgroundColor(msg.what);
		}
	};
	private String KSN;
	private String Track2;
	//private String CardName;
	private String mpsResponse;
	public String Authorize;
	public String AuthCode;
	public String TranCode;
	public String AcqRefData;
	public String RecordNo;
	public String RefNo;
	public String ProcessData;
	public String InvoiceNo;
	private String postXml;
	public String mStringCardDataBuffer;
	public int mIntDataCount;
	private boolean searchingBT;

	public void backPressed(View v) {
		Intent intent = this.getIntent();
		this.setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(mHeadsetReceiver);
		unregisterReceiver(mNoisyAudioStreamReceiver);

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}
		
		if (mMTSCRA != null) {
			if (mMTSCRA.isDeviceConnected())
				mMTSCRA.closeDevice();
		}
		
		if (MagTeklibDynamag != null) {
			if (MagTeklibDynamag.isDeviceConnected()) {
				MagTeklibDynamag.closeDevice();
			}
		}
	}

	private class IssueCard extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			int stringId = getApplicationInfo().labelRes;
			String version = null;
			try {
				version = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String amount = "";
			if (Float.valueOf(totalAmount) > 0) {
				amount = ""+totalAmount;
			} else {
				amount = ""+(df.format(Float.valueOf(totalAmount) * -1));
			}
			
		String xmlPost = "<?xml version=\"1.0\"?>"+
			"<TStream>"+
			 "<Transaction>"+
			  "<IpPort>9100</IpPort>"+ 
			  "<MerchantID>"+PrioritySetting.merchantID+"</MerchantID>"+
			  "<OperatorID>"+cashier+"</OperatorID>"+
			  "<TranType>PrePaid</TranType>"+
			 "<TranCode>Issue</TranCode>"+
			  "<InvoiceNo>"+invoice+"</InvoiceNo>"+
			  "<RefNo>123456</RefNo>"+
			  "<Memo>"+getString(stringId) + " v" + version+"</Memo>"+
			  "<Account>"+
			  "<EncryptedFormat>MagneSafe</EncryptedFormat>"+
			  "<AccountSource>Swiped</AccountSource>"+
			  "<EncryptedBlock>"+Track2+"</EncryptedBlock>"+ 
			  "<EncryptedKey>"+KSN+"</EncryptedKey>"+
			  "</Account>"+
			  "<Amount>"+
			   "<Purchase>"+amount+"</Purchase>"+
			  "</Amount>"+
			 "</Transaction>"+
			"</TStream>";
			
			if (hasInternet()) {
				try {
					WebRequest mpswr = new WebRequest(PrioritySetting.mWSURL);
					mpswr.addParameter("tran", postXml);
					mpswr.addParameter("pw", PrioritySetting.webServicePassword);
					mpswr.setWebMethodName("GiftTransaction");
					mpswr.setTimeout(10);

					mpsResponse = mpswr.sendRequest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			postData();
			if (hasInternet()) {
				extractXML();
			}
			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			pd.dismiss();
			
			if (hasInternet()) {
				Log.v("Response", "" + result);

				if (CmdStatus.equals("Declined")) {
					// Error
					alertbox("Declined", "Reason: " + TextResponse);
				} else if (CmdStatus.equals("Approved")) {
					// Approved
					ProductDatabase.saveMercuryForLater(result, invoice, saleID, true);

					Intent intent = GiftScreen.this.getIntent();
					GiftScreen.this.setResult(Activity.RESULT_OK, intent);
					if (TranCode.equals("Return")) {
						intent.putExtra("AMOUNT", "-" + Authorize);
					} else {
						intent.putExtra("AMOUNT", Authorize);
					}
					intent.putExtra("TYPE", 1);
					intent.putExtra("AUTH_CODE", AuthCode);
					intent.putExtra("AcqRefData", AcqRefData);
					intent.putExtra("RecordNo", RecordNo);
					intent.putExtra("RefNo", RefNo);
					intent.putExtra("ProcessData", ProcessData);
					intent.putExtra("InvoiceNo", InvoiceNo);
					intent.putExtra("TranCode", TranCode);
					
					JSONObject json = new JSONObject();
					SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
					String license = mSharedPreferences.getString("APOS_LICENSE", "");
					
					try {
						json.put("am", Authorize);			
						json.put("tr", TranCode);
						json.put("id", license);
						json.put("in", InvoiceNo);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					new SendInfo().execute(json);
					finish();
				} else {
					// Declined
					alertbox("Error", "Reason: " + TextResponse);
				}
			} else {
				SaveForLaterDialog();
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

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument(null, Boolean.valueOf(true));
			// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
			// true);
			serializer.startTag("", "TStream");
			serializer.startTag("", "Transaction");

			serializer.startTag("", "MerchantID");
			serializer.text(PrioritySetting.merchantID);
			serializer.endTag("", "MerchantID");

			serializer.startTag("", "TranType");
			serializer.text("Credit");
			serializer.endTag("", "TranType");

			if (Float.valueOf(totalAmount) > 0) {
				serializer.startTag("", "TranCode");
				serializer.text("Sale");
				serializer.endTag("", "TranCode");
			} else {
				serializer.startTag("", "TranCode");
				serializer.text("Return");
				serializer.endTag("", "TranCode");
			}

			serializer.startTag("", "InvoiceNo");
			serializer.text(invoice);
			serializer.endTag("", "InvoiceNo");

			serializer.startTag("", "RefNo");
			serializer.text(invoice);
			serializer.endTag("", "RefNo");

			int stringId = getApplicationInfo().labelRes;
			String version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;

			serializer.startTag("", "Memo");
			serializer.text(getString(stringId) + " v" + version);
			serializer.endTag("", "Memo");

			serializer.startTag("", "Frequency");
			serializer.text("OneTime");
			serializer.endTag("", "Frequency");

			serializer.startTag("", "RecordNo");
			serializer.text("RecordNumberRequested");
			serializer.endTag("", "RecordNo");

			serializer.startTag("", "PartialAuth");
			serializer.text("Allow");
			serializer.endTag("", "PartialAuth");

			serializer.startTag("", "Account");

			serializer.startTag("", "EncryptedFormat");
			serializer.text("MagneSafe");
			serializer.endTag("", "EncryptedFormat");

			serializer.startTag("", "AccountSource");
			serializer.text("Swiped");
			serializer.endTag("", "AccountSource");

			serializer.startTag("", "EncryptedBlock");
			serializer.text(Track2);
			serializer.endTag("", "EncryptedBlock");

			serializer.startTag("", "EncryptedKey");
			serializer.text(KSN);
			serializer.endTag("", "EncryptedKey");

			serializer.endTag("", "Account");

			serializer.startTag("", "Amount");
			serializer.startTag("", "Purchase");
			if (Float.valueOf(totalAmount) > 0) {

				serializer.text(totalAmount);
			} else {
				serializer.text(df.format(Float.valueOf(totalAmount) * -1));
			}
			serializer.endTag("", "Purchase");
			serializer.endTag("", "Amount");

			serializer.startTag("", "TerminalName");
			serializer.text(PrioritySetting.terminalName);
			serializer.endTag("", "TerminalName");

			serializer.startTag("", "OperatorID");
			serializer.text(cashier);
			serializer.endTag("", "OperatorID");

			serializer.endTag("", "Transaction");
			serializer.endTag("", "TStream");
			serializer.endDocument();

			postXml = writer.toString();

			Log.v("Request", "" + postXml);

			if (hasInternet()) {
				try {
					MPSWebRequest mpswr = new MPSWebRequest(PrioritySetting.mWSURL);
					mpswr.addParameter("tran", postXml);
					mpswr.addParameter("pw", PrioritySetting.webServicePassword);
					mpswr.setWebMethodName("CreditTransaction");
					mpswr.setTimeout(10);

					mpsResponse = mpswr.sendRequest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			// alertbox("Exception", "error occurred while creating xml data");
			Log.e("Exception", "error occurred while creating xml data");
			e.printStackTrace();
		}

		return mpsResponse;
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
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

	private void doDiscovery() {
		
		swipeState = SWIPE_STATE_NEEDCONNECT;
		if(pairedDevices != null)
		{
		// Indicate scanning in the title
		for (BluetoothDevice device : pairedDevices) {
			if (device.getName().toUpperCase().contains("MAGTEK")) {
				
				reconnectReader.setVisibility(View.VISIBLE);
				readerImage.setVisibility(View.VISIBLE);
				readerImage.setImageResource(R.drawable.bullet);
			    mReaderNameTextView.setText("BulleT BT Card Reader");

				mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_BLUETOOTH);
				mMTSCRA.setDeviceID(device.getAddress());
				mMTSCRA.openDevice();
			}
		}
		}
		
		if(!mMTSCRA.isDeviceConnected())
 	   	{
			if(mbAudioConnected)
            {
				mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_AUDIO);
				openDevice();
            }
 	   }
	}

	private class MtHandlerCallback implements Callback {
        public boolean handleMessage(Message msg) {
        	
        	boolean ret = false;
        	
        	switch (msg.what) {
        	case DEVICE_MESSAGE_CARDDATA_CHANGE: 
        		mStringCardDataBuffer = (String)msg.obj;
        		
        		MagTeklibDynamag.setCardData(mStringCardDataBuffer);

        		KSN = MagTeklibDynamag.getKSN();
        		Track2 = MagTeklibDynamag.getTrack2();
        		           		
        		String status = MagTeklibDynamag.getTrackDecodeStatus();
        		if(status.substring(2, 4).equals("00"))
        		{
        			mSwipeStatusTextView.setText("Done Getting Swipe.");
        			
        			if(!GiftScreen.this.isFinishing())
            		{
        				pd = ProgressDialog.show(GiftScreen.this, "", "Sending Transaction...", true, false);
        				//new ProcessCharge().execute();
            		}
        		}else{
        			mSwipeStatusTextView.setText("Bad swipe. Try again.");
        		}
        		
        		break;
        		
        	case DEVICE_STATUS_CONNECTED:        		
        		if (((Number)msg.obj).intValue() == DEVICE_STATUS_CONNECTED_SUCCESS) {
        			setStatus("USB DynaMag Connected", Color.GREEN);
        			mReaderNameTextView.setText("Dynamag HID Card Reader");
		    		readerImage.setVisibility(View.VISIBLE);
		    		readerImage.setImageResource(R.drawable.dynamag);
        		} else if (((Number)msg.obj).intValue() == DEVICE_STATUS_CONNECTED_FAIL){
        			setStatus("USB DynaMag Connection Failed", Color.RED);
        		} else if (((Number)msg.obj).intValue() == DEVICE_STATUS_CONNECTED_PERMISSION_DENIED){
        			setStatus("Connection Permission Denied", Color.RED);
        		}
        		
        		break;
        		
        	case DEVICE_STATUS_DISCONNECTED:
        		setStatus("USB DynaMag Disconnected", Color.RED);
        		break;
        		
        	default:
        		ret = false;
        		break;
        	
        	}      	
            
            return ret; 
        }
    }

	private class SCRAHandlerCallback implements Callback {
		public boolean handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
					case MagTekSCRA.DEVICE_STATE_CONNECTED:
						mIntCurrentStatus = STATUS_IDLE;
						swipeState = SWIPE_STATE_CANSWIPE;
						mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTED;
						setStatus("Reader Status: Connected", Color.GREEN);
						break;
					case MagTekSCRA.DEVICE_STATE_CONNECTING:
						swipeState = SWIPE_STATE_NEEDCONNECT;
						mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_CONNECTING;
						setStatus("Reader Status: Connecting...", Color.YELLOW);
						break;
					case MagTekSCRA.DEVICE_STATE_DISCONNECTED:
						swipeState = SWIPE_STATE_NEEDCONNECT;
						mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;
						setStatus("Reader Status: Disconnected", Color.RED);
						break;
					}
					break;
				case MagTekSCRA.DEVICE_MESSAGE_DATA_START:
					swipeState = SWIPE_STATE_PROCESSING;
					mSwipeStatusTextView.setText("Getting Swipe Data.");
					return true;
				case MagTekSCRA.DEVICE_MESSAGE_DATA_CHANGE:
					if (msg.obj != null) {
						mSwipeStatusTextView.setText("Done getting swipe.");
						//log.v("Transfer ended", "Transfer ended");
						displayResponseData();
						msg.obj = null;
						return true;
					}
					break;
				case MagTekSCRA.DEVICE_MESSAGE_DATA_ERROR:
					swipeState = SWIPE_STATE_CANSWIPE;
					mSwipeStatusTextView.setText("Bad swipe. Try again.");
					//log.v("ERROR", "Card Swipe Error... Please Swipe Again.");
					return true;
				default:
					if (msg.obj != null) {
						return true;
					}
					break;
				}
				;
 
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return false;
		}
	}

	private void displayResponseData() {
		KSN = mMTSCRA.getKSN();
		Track2 = mMTSCRA.getTrack2();

		String status = mMTSCRA.getTrackDecodeStatus();
		if(status.substring(2, 4).equals("00"))
		{
			if(!GiftScreen.this.isFinishing())
    		{
				pd = ProgressDialog.show(this, "", "Sending Transaction...", true, false);
				//new ProcessCharge().execute();
    		}
		}else{
			mSwipeStatusTextView.setText("Bad swipe. Try again.");
		}
	}

	public void extractXML() {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			DataHandler dataHandler = new DataHandler();
			xr.setContentHandler(dataHandler);
			if(mpsResponse != null)
			{
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

	public class DataHandler extends DefaultHandler {

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

			if (localName.equals("ResponseOrigin")) {
				origin = tempVal;
			}

			if (localName.equals("CmdStatus")) {
				CmdStatus = tempVal;
			}

			if (localName.equals("TextResponse")) {
				TextResponse = tempVal;
			}

			if (localName.equals("Authorize")) {
				Authorize = tempVal;
			}

			if (localName.equals("AuthCode")) {
				AuthCode = tempVal;
			}

			if (localName.equals("AcqRefData")) {
				AcqRefData = tempVal;
			}

			if (localName.equals("RecordNo")) {
				RecordNo = tempVal;
			}

			if (localName.equals("RefNo")) {
				RefNo = tempVal;
			}

			if (localName.equals("ProcessData")) {
				ProcessData = tempVal;
			}

			if (localName.equals("TranCode")) {
				TranCode = tempVal;
			}

			if (localName.equals("InvoiceNo")) {
				InvoiceNo = tempVal;
			}
		}
	}

	private boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}

	private boolean capturing = false;
	private String captureString;
	public boolean mbAudioConnected;
	protected String mStringAudioConfigResult;
	private String mStringLocalConfig;

	public void SaveForLaterDialog() {

		new AlertDialog.Builder(this)
				.setMessage(
						"No internet connection to send transaction swipe. Do you want to save swipe data for later?")
				.setInverseBackgroundForced(true)
				.setTitle("Save Swipe For Later?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ProductDatabase.saveMercuryForLater(postXml, invoice, saleID, false);
								Intent intent = GiftScreen.this.getIntent();
								GiftScreen.this.setResult(Activity.RESULT_OK,
										intent);

								intent.putExtra("AMOUNT", totalAmount);
								intent.putExtra("AUTH_CODE", "PRESALE");
								intent.putExtra("InvoiceNo", invoice);

								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				}).show();
	}
	    	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK)
    	{
			Intent intent = GiftScreen.this.getIntent();
			GiftScreen.this.setResult(Activity.RESULT_CANCELED, intent);
			finish();    		    
			return true;
    	}
    	
		char pressedKey = (char)event.getUnicodeChar();
		
    	if(pressedKey == '%')
    	{
    		MagTeklibDynamag = new MagTeklibDynamag(this, null);
    		capturing = true;
    		captureString = "";
			mSwipeStatusTextView.setText("Processing...0%");

    		captureString = captureString + Character.toString(pressedKey);
    		    		return true;

    	}
                
    	//if(capturing == true && captureString != null && keyCode == 40)
    	if(capturing == true && captureString != null && (captureString.endsWith("||1000") || captureString.endsWith("||0000")))
    	{
    		capturing = false;
    		MagTeklibDynamag.setCardData(captureString);
			mSwipeStatusTextView.setText("Done Getting Swipe.");

    		KSN = MagTeklibDynamag.getKSN();
    		Track2 = MagTeklibDynamag.getTrack2();
    		
    		//log.v("Enter", KSN + " " + Track2 + " " + captureString.length());
    		
    		pd = ProgressDialog.show(this, "", "Sending Transaction...", true,
    				false);
    		//new ProcessCharge().execute(); 
    		return true;
    	}

    	if(capturing == true && keyCode != 59 && keyCode != KeyEvent.KEYCODE_ENTER) 
    	{
    		captureString = captureString + Character.toString(pressedKey);
			mSwipeStatusTextView.setText("Processing..."+(int)(captureString.length()/454f*100)+"%");

    		return true;

    	}

        return false;
    }
    
    private void openDevice()
	{
		if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
		{
			Thread tSetupAudioParams = new Thread() {
				public void run()
				{
					try
					{
						Log.v("Setup", "AUDIO SETUP GOING ON!");
						mStringAudioConfigResult = setupAudioParameters();
					}
					catch(Exception ex)
					{
						mStringAudioConfigResult = "Error:" + ex.getMessage();	
						ex.printStackTrace();
					}
					mUIProcessCardHandler.post(mUISetupAudioParamsResults);
				}
			};
			tSetupAudioParams.start();
			
		}
	}
    
    public class NoisyAudioStreamReceiver extends BroadcastReceiver
    {
    	@Override
    	public void onReceive(Context context, Intent intent)
    	{
    		/* If the device is unplugged, this will immediately detect that action,
    		 * and close the device.
    		 */
    		if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
    		{
            	mbAudioConnected=false;
            	if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
            	{
            		if(mMTSCRA.isDeviceConnected())
            		{
            			mReaderNameTextView.setText("No reader connected");
            			setStatus("Connection Noisy...", Color.RED);
                    	readerImage.setVisibility(View.INVISIBLE);
    		    		readerImage.setImageResource(R.drawable.dynamag);
            			mMTSCRA.closeDevice();
            			//clearScreen();
            		}
            	}
    		}
    	}
    }
    
    public class headSetBroadCastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

        	try
        	{
                String action = intent.getAction();
                //Log.i("Broadcast Receiver", action);
                if( (action.compareTo(Intent.ACTION_HEADSET_PLUG))  == 0)   //if the action match a headset one
                {
                    int headSetState = intent.getIntExtra("state", 0);      //get the headset state property
                    int hasMicrophone = intent.getIntExtra("microphone", 0);//get the headset microphone property
  				    //mCardDataEditText.setText("Headset.Detected=" + headSetState + ",Microphone.Detected=" + hasMicrophone);

                    if( (headSetState == 1) && (hasMicrophone == 1))        //headset was unplugged & has no microphone
                    {
                    	setStatus("Audio Device Connected", Color.YELLOW);
                    	mReaderNameTextView.setText("uDynamo Audio Reader");
                    	readerImage.setVisibility(View.VISIBLE);
    		    		readerImage.setImageResource(R.drawable.udynamo);
                    	reconnectReader.setVisibility(View.VISIBLE);
                    	mbAudioConnected=true;
                    }
                    else 
                    {
                    	if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
                    	{
	                    	setStatus("Disconnected...", Color.RED);
	                    	readerImage.setVisibility(View.INVISIBLE);
	                    	mReaderNameTextView.setText("No reader connected");
	    		    		readerImage.setImageResource(R.drawable.udynamo);
	                    	mbAudioConnected=false ;
                    	
                    		if(mMTSCRA.isDeviceConnected())
                    		{
                    			mMTSCRA.closeDevice();
                    			//clearScreen();
                    		}
                    	}
                    }
                }           
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}

        }
    }	
    
    String setupAudioParameters()throws MTSCRAException {
		mStringLocalConfig="";
		String strResult="OK";
		
		try
		{
            			
			setStatus("Setting up audio config...", Color.YELLOW);
			
			//Option 3
			
			
			
			String strXMLConfig="";
			
			
			//mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
			long login_last = mSharedPreferences.getLong("AUDIO_CHECK", 0);
			
			long now = new Date().getTime();
			if(now > (login_last + 60*60*24*7*1000))
			{
				strXMLConfig = getConfigurationLocal();
			}
			
			if (strXMLConfig.length() <= 0)
			{
				Editor e = mSharedPreferences.edit();
				e.putLong("AUDIO_CHECK", new Date().getTime());
				e.commit();
				
				setStatus("Retrieve Configuration...", Color.YELLOW);
				SCRAConfigurationDeviceInfo pDeviceInfo = new SCRAConfigurationDeviceInfo();
				SCRAConfigurationReaderType pReaderInfo = new SCRAConfigurationReaderType();
				
				pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_PLATFORM,"Android");
				pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_MODEL,Build.MODEL.toUpperCase());
				//pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_MODEL,Build.MODEL.toUpperCase());
				pDeviceInfo.setProperty(SCRAConfigurationDeviceInfo.PROP_DEVICE,Build.DEVICE.toUpperCase());
				
				strXMLConfig = mMTSCRA.getConfigurationXML(CONFIGWS_USERNAME,CONFIGWS_PASSWORD,CONFIGWS_READERTYPE,pDeviceInfo,CONFIGWS_URL,10000);//Call Web Service to retrieve XML
				if (strXMLConfig.length() > 0)
				{
					setStatus("Configuration Received...", Color.YELLOW);
					    //setStatusMessage("Configuration Received From Server\n******************************\n" + strXMLConfig + "\n******************************\n");
					ProcessMessageResponse pResponse = mMTSCRA.getConfigurationResponse(strXMLConfig);
					if(pResponse!=null)
					{
						dumpWebConfigResponse(pResponse);
							//debugMsg("Setting Configuration From Response....");
						mMTSCRA.setConfigurationResponse(pResponse);
						//mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
					}
					mStringLocalConfig=strXMLConfig;
					if(mStringLocalConfig.length() > 0)
	        		{
						setConfigurationLocal(mStringLocalConfig);//optional but can be useful to retrieve from locally and get it from server only certain times
	        		}
						//setStatusMessage("SDK Configuration Was Set Successful.\nPlease Swipe A Card....\n");
					return strResult;
				}//if (strXMLConfig.length() > 0)
				else
				{
					    //setStatusMessage("No Configuration Received, Using Default");
					strResult="Error:" + "No Configuration Received, Using Default";
					setAudioConfigManual();
					return strResult;
						
				}
			}			
		}
		catch(MTSCRAException ex)
		{
			strResult = "Error:" +  ex.getMessage();
			//throw new MTSCRAException(ex.getMessage());
		}
		return strResult;
    }
    
    void dumpWebConfigResponse(ProcessMessageResponse lpMessageResponse)
	{
		String strDisplay="";
		try
		{
            
				if(lpMessageResponse!=null)
				{
					if(lpMessageResponse.Payload!=null)
					{
						if(lpMessageResponse.Payload.StatusCode!= null)
						{
							if(lpMessageResponse.Payload.StatusCode.Number==0)
							{
								if(lpMessageResponse.Payload.SCRAConfigurations.size() > 0)
								{
									for (int i=0; i < lpMessageResponse.Payload.SCRAConfigurations.size();i++)
									{
										SCRAConfiguration tConfig = (SCRAConfiguration) lpMessageResponse.Payload.SCRAConfigurations.elementAt(i);
										strDisplay="********* Config:" + Integer.toString(i+1) + "***********\n";
										
										strDisplay+="DeviceInfo:Model:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_MODEL) + "\n";
										strDisplay+="DeviceInfo:Device:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_DEVICE) + "\n";
										strDisplay+="DeviceInfo:Firmware:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_FIRMWARE) + "\n";
										strDisplay+="DeviceInfo.Platform:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PLATFORM) + "\n";
										strDisplay+="DeviceInfo:Product:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_PRODUCT) + "\n";
										strDisplay+="DeviceInfo:Release:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_RELEASE) + "\n";
										strDisplay+="DeviceInfo:SDK:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_SDK) + "\n";
										strDisplay+="DeviceInfo:Status:" + tConfig.DeviceInfo.getProperty(SCRAConfigurationDeviceInfo.PROP_STATUS)+ "\n";
										//Status = 0 Unknown
										//Status = 1 Tested and Passed 
										//Status = 2 Tested and Failed 
										strDisplay+="ReaderType.Name:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_NAME) + "\n";
										strDisplay+="ReaderType.Type:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_TYPE) + "\n";
										strDisplay+="ReaderType.Version:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_VERSION) + "\n";
										strDisplay+="ReaderType.SDK:" + tConfig.ReaderType.getProperty(SCRAConfigurationReaderType.PROP_SDK) + "\n";
										strDisplay+="StatusCode.Description:" + tConfig.StatusCode.Description + "\n";
										strDisplay+="StatusCode.Number:" + tConfig.StatusCode.Number + "\n";
										strDisplay+="StatusCode.Version:" + tConfig.StatusCode.Version + "\n";
										for (int j=0; j < tConfig.ConfigParams.size();j++)
										{
											strDisplay+="ConfigParam.Name:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Name + "\n";
											strDisplay+="ConfigParam.Type:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Type + "\n";
											strDisplay+="ConfigParam.Value:" + ((ConfigParam)tConfig.ConfigParams.elementAt(j)).Value + "\n";
										}//for (int j=0; j < tConfig.ConfigParams.size();j++)
										strDisplay+="*********  Config:" + Integer.toString(i+1) + "***********\n";
										Log.v("DUMP", strDisplay);
										//debugMsg(strDisplay);
									}//for (int i=0; i < lpMessageResponse.Payload.SCRAConfigurations.size();i++)
									//debugMsg(strDisplay);
								}//if(lpMessageResponse.Payload.SCRAConfigurations.size() > 0)
								
							}//if(lpMessageResponse.Payload.StatusCode.Number==0)
							strDisplay= "Payload.StatusCode.Version:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_VERSION) + "\n";
							strDisplay+="Payload.StatusCode.Number:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_NUMBER) + "\n";
							strDisplay+="Payload.StatusCode.Description:" + lpMessageResponse.Payload.StatusCode.getProperty(StatusCode.PROP_DESCRIPTION) + "\n";
							Log.v("DUMP", strDisplay);
						}//if(lpMessageResponse.Payload.StatusCode!= null)
							
					}//if(lpMessageResponse.Payload!=null)
				}//if(lpMessageResponse!=null)
				else
				{
					Log.v("DUMP","Configuration Not Found");
				}
			
		}
		catch(Exception ex)
		{
			Log.v("DUMP","Exception:" + ex.getMessage());
		}
		
	}
    
    void setAudioConfigManual()throws MTSCRAException
	{
    	String model = android.os.Build.MODEL.toUpperCase();
		try
		{
	    	if(model.contains("DROID RAZR") || model.toUpperCase().contains("XT910"))
	        {
				   mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=48000,");
	        }
	        else if ((model.equals("DROID PRO"))||
	        		 (model.equals("MB508"))||
	        		 (model.equals("DROIDX"))||
	        		 (model.equals("DROID2"))||
	        		 (model.equals("MB525")))
	        {
				  mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=32000,");
	        }    	
	        else if ((model.equals("GT-I9300"))||//S3 GSM Unlocked
	        		 (model.equals("SPH-L710"))||//S3 Sprint
	        		 (model.equals("SGH-T999"))||//S3 T-Mobile
	        		 (model.equals("SCH-I535"))||//S3 Verizon
	        		 (model.equals("SCH-R530"))||//S3 US Cellular
	        		 (model.equals("SAMSUNG-SGH-I747"))||// S3 AT&T
	        		 (model.equals("M532"))||//Fujitsu
	        		 (model.equals("GT-N7100"))||//Notes 2 
	        		 (model.equals("GT-N7105"))||//Notes 2 
	        		 (model.equals("SAMSUNG-SGH-I317"))||// Notes 2
	        		 (model.equals("SCH-I605"))||// Notes 2
	        		 (model.equals("SCH-R950"))||// Notes 2
	        		 (model.equals("SGH-T889"))||// Notes 2
	        		 (model.equals("SPH-L900"))||// Notes 2
	        		 (model.equals("SAMSUNG-SGH-I337"))||// S4
	        		 (model.equals("GT-P3113")))//Galaxy Tab 2, 7.0
	        		
	        {
	        	  mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
	        }
	        else if ((model.equals("XT907")))
	        {

				  mMTSCRA.setConfigurationParams("INPUT_WAVE_FORM=0,");
	        }   
	    	
	    	Log.v("CONFIG", mMTSCRA.getDeviceName());
	        
		}
		catch(MTSCRAException ex)
		{
			throw new MTSCRAException(ex.getMessage());
		}
		
	}
    
    private void InitializeData() 
	{
	    mMTSCRA.clearBuffers();
		mLongTimerInterval = 0;
//		miReadCount=0;
		mbAudioConnected=false;
		mIntCurrentVolume=0;
		mIntCurrentStatus = STATUS_IDLE;
		mIntCurrentDeviceStatus = MagTekSCRA.DEVICE_STATE_DISCONNECTED;
		
		//mStringDebugData ="";
		mStringAudioConfigResult="";
		
	}
    
    @Override
	public synchronized void onResume() {
		super.onResume();
		this.registerReceiver(mHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		this.registerReceiver(mNoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

		doDiscovery();
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
	}
    
    final  Runnable mUISetupAudioParamsResults = new Runnable() {
		public void run() {
			try 
			{
				if(!mStringAudioConfigResult.equalsIgnoreCase("OK"))
				{
					//web configuration failed use local
					//The code below is only needed if configuration needs to be set manually
					//for some reason
					//debugMsg("Setting Configuration Manually....");
					try
					{
						setAudioConfigManual();
						
					}
					catch(MTSCRAException ex)
					{
						//debugMsg("Exception:" + ex.getMessage());
						throw new MTSCRAException(ex.getMessage());
					}
					
				}
				mMTSCRA.openDevice();
			} catch (Exception ex) {

			}

		}
	};
	
	String getConfigurationLocal()
	{
		String strXMLConfig="";
		try
		{
			strXMLConfig = ReadSettings(getApplicationContext(), CONFIG_FILE);
			if(strXMLConfig==null)strXMLConfig="";
		}
		catch (Exception ex)
		{
		}
		
		return strXMLConfig;
	
	}
	
	public static String ReadSettings(Context context, String file) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		String data = null;
		fis = context.openFileInput(file);
		isr = new InputStreamReader(fis);
		char[] inputBuffer = new char[fis.available()];
		isr.read(inputBuffer);
		data = new String(inputBuffer);
		isr.close();
		fis.close();
		return data;
	}
	
	public static void WriteSettings(Context context, String data, String file) throws IOException {
		FileOutputStream fos= null;
		OutputStreamWriter osw = null;
		fos= context.openFileOutput(file,Context.MODE_PRIVATE);
		osw = new OutputStreamWriter(fos);
		osw.write(data);
		osw.close();
		fos.close();
	}
	
	void setConfigurationLocal(String lpstrConfig)
	{
		try
		{
			Log.v("WRITE", lpstrConfig);
			WriteSettings(getApplicationContext(),lpstrConfig,CONFIG_FILE);
		}
		catch (Exception ex)
		{
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private void lockOrientation(Activity activity) {
	    Display display = activity.getWindowManager().getDefaultDisplay();
	    int rotation = display.getRotation();
	    int height;
	    int width;
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
	        height = display.getHeight();
	        width = display.getWidth();
	    } else {
	        Point size = new Point();
	        display.getSize(size);
	        height = size.y;
	        width = size.x;
	    }
	    switch (rotation) {
	    case Surface.ROTATION_90:
	        if (width > height)
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        else
	            activity.setRequestedOrientation(9/* reversePortait */);
	        break;
	    case Surface.ROTATION_180:
	        if (height > width)
	            activity.setRequestedOrientation(9/* reversePortait */);
	        else
	            activity.setRequestedOrientation(8/* reverseLandscape */);
	        break;          
	    case Surface.ROTATION_270:
	        if (width > height)
	            activity.setRequestedOrientation(8/* reverseLandscape */);
	        else
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        break;
	    default :
	        if (height > width)
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        else
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (requestCode == 1021) {
			if(intent != null)
			{
			if (resultCode == Activity.RESULT_OK) {
				
				Bundle bundle = intent.getExtras();
				String status = "";
				String cvvResult = "";
				if (bundle.getString("STATUS") != null) {
					status = bundle.getString("STATUS");
				}
				
				if(status.equals("Approved"))
				{
					if (bundle.getString("CvvResult") != null) {
						cvvResult = bundle.getString("CvvResult");
					}
					
					if(cvvResult.equals("M"))
					{				
						Approved(bundle);
					}else{
						final Bundle sendBundle = bundle;
						new AlertDialog.Builder(this)
						.setMessage(
								"Mercury Transaction #" + bundle.getString("InvoiceNo") + " has returned MISMATCHED CVV/CVV2/CID. Would you like to void this transaction and try again?")
						.setInverseBackgroundForced(true)
						.setTitle("Transaction Mismatch.")
						.setPositiveButton("Reverse",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										pd = ProgressDialog.show(GiftScreen.this, "", "Sending Manual Reversal...", true, false);
										new ProcessManualReversal().execute(sendBundle);										
									}
								})
						.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Approved(sendBundle);
							}
						}).show();
					}
					
					
				} else {
					String message = "";
					
					if (bundle.getString("STATUS") != null) {
						status = bundle.getString("STATUS");
					}
					
					if (bundle.getString("MESSAGE") != null) {
						message = bundle.getString("MESSAGE");
					}
					
					alertbox("Manual Processor", status+": "+message);
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Bundle bundle = intent.getExtras();
				String status = "";
				String message = "";
				
				if (bundle.getString("STATUS") != null) {
					status = bundle.getString("STATUS");
				}
				
				if (bundle.getString("MESSAGE") != null) {
					message = bundle.getString("MESSAGE");
				}
				
				alertbox("Manual Processor", status+": "+message);
			}
			}
		} 
	}
	
	public void Approved(Bundle bundle)
	{
		Intent returnIntent = GiftScreen.this.getIntent();
		GiftScreen.this.setResult(Activity.RESULT_OK, returnIntent);
			
		if (bundle.getString("TranCode").equals("Return")) {
			returnIntent.putExtra("AMOUNT", "-" + bundle.getString("AMOUNT"));
		} else {
			returnIntent.putExtra("AMOUNT", bundle.getString("AMOUNT"));
		}
		
		//returnIntent.putExtra("AMOUNT", bundle.getString("AMOUNT"));
		returnIntent.putExtra("TYPE", "2");
		returnIntent.putExtra("AUTH_CODE", bundle.getString("AUTH_CODE"));
		returnIntent.putExtra("AcqRefData", bundle.getString("AcqRefData"));
		returnIntent.putExtra("RefNo", bundle.getString("RefNo"));
		returnIntent.putExtra("ProcessData", bundle.getString("ProcessData"));
		returnIntent.putExtra("InvoiceNo", bundle.getString("InvoiceNo"));
		returnIntent.putExtra("TranCode", bundle.getString("TranCode"));
		returnIntent.putExtra("RecordNo", bundle.getString("RecordNo"));	
		
		Log.v("SEND TYPE", returnIntent.getExtras().getString("TYPE"));
			
		String resultXML = "<?xml version=\"1.0\"?><RStream>"+
							"<CmdStatus>Approved</CmdStatus>"+
							"<TranCode>"+bundle.getString("TranCode")+"</TranCode>"+
							"<RefNo>"+bundle.getString("RefNo")+"</RefNo>"+
							"<RecordNo>"+bundle.getString("RecordNo")+"</RecordNo>"+
							"<AuthCode>"+bundle.getString("AUTH_CODE")+"</AuthCode>"+
							"<AcqRefData>"+bundle.getString("AcqRefData")+"</AcqRefData>"+
							"<ProcessData>"+bundle.getString("ProcessData")+"</ProcessData>"+
							"<Authorize>"+bundle.getString("AMOUNT")+"</Authorize></RStream>"; 
			
		ProductDatabase.saveMercuryManual(resultXML, invoice);
		
		JSONObject json = new JSONObject();
		SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
		String license = mSharedPreferences.getString("APOS_LICENSE", "");
		
		try {
			json.put("am", bundle.getString("AMOUNT"));			
			json.put("tr", bundle.getString("TranCode"));
			json.put("id", license);
			json.put("in", bundle.getString("InvoiceNo"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new SendInfo().execute(json);
		
		finish();
	}
	
	//public ArrayList<PortInfo> arrayDiscovery;
	public ArrayList<String> arrayPortName;
	public BluetoothDevice[] btDeviceList;
	public ArrayList<BluetoothDevice> mDeviceAdapter;
	public boolean VoidSale;
	public String TransCode;
	
	private class ProcessManualReversal extends AsyncTask<Bundle, Void, Bundle> {

		public ProcessManualReversal() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Bundle doInBackground(Bundle... params) {
			
			Bundle bundle = params[0];
										
	    	int stringId = getApplicationInfo().labelRes;
		    String version = "";
			try {
				version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if (bundle.getString("TranCode") != null) {
				TransCode = bundle.getString("TranCode");
			}
	    	
			JSONObject json = new JSONObject();
					
			try {
				json.put("MerchantID", PrioritySetting.hostedMID);	
				json.put("pw", PrioritySetting.hostedPass);
				json.put("PurchaseAmount", bundle.getString("AMOUNT"));
				json.put("Invoice", bundle.getString("InvoiceNo"));
				json.put("RefNo", bundle.getString("RefNo"));
				json.put("TerminalName", PrioritySetting.terminalName);
				json.put("OperatorID", cashier);
				json.put("Memo", getString(stringId) + " v"+version);
				json.put("AuthCode", bundle.getString("AUTH_CODE"));
				json.put("ProcessData", bundle.getString("ProcessData"));
				json.put("AcqRefData", bundle.getString("AcqRefData"));
				json.put("Token", bundle.getString("RecordNo"));
				json.put("VoidSale", VoidSale);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			postManualData(json);
			//extractXML();
			return bundle;
		}

		@Override
		protected void onPostExecute(Bundle result) {
			pd.dismiss();
			
			if(CmdStatus.equals("Declined"))
			{
				if(TransCode.equals("Sale"))
				{
					if(VoidSale == false)
					{
						pd = ProgressDialog.show(GiftScreen.this, "", "Processing Manual VoidSale", true, false);
						VoidSale = true;
						new ProcessManualReversal().execute(result);
					}else{
						alertbox("Declined", "Reason: " + TextResponse);
					}
				}else{
					alertbox("Declined", "Reason: " + TextResponse);
				}
			}
			else if(CmdStatus.equals("Approved"))
			{
				// Approved
				alertbox("Reversed", "Reason: " + TextResponse);
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
				}
				
				if(object.has("CreditReversalTokenResult"))
				{
					JSONObject CreditVoidSaleTokenResult = object.getJSONObject("CreditReversalTokenResult");
					
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
				Log.v("SEND", json.toString());
				List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
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
