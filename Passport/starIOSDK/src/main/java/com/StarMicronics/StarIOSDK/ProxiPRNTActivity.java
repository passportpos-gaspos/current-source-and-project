package com.StarMicronics.StarIOSDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.StarMicronics.StarIOSDK.RasterDocument.RasPageEndMode;
import com.StarMicronics.StarIOSDK.RasterDocument.RasSpeed;
import com.StarMicronics.StarIOSDK.RasterDocument.RasTopMargin;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.stario.StarProxiPRNTManager;
import com.starmicronics.stario.StarProxiPRNTManagerCallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class ProxiPRNTActivity extends Activity {

	private ListView listBLE = null;
	private List<ProxiPRNTData> dataList = new ArrayList<ProxiPRNTData>();
	private static ProxiPRNTAdapter adapter_listProxiPRNT;
	private Integer pos = 0; 
	private List<String> macAddressList = new ArrayList<String>();
	private int  Index_deviceItemList = 0;
	private ArrayList<String> foundPortNameList = new ArrayList<String>();
	private TextView emptyView = null;

	private StarProxiPRNTManager m_shareManager;

	private static String deviceSettings_macAddress = "";
	private static String deviceSettings_portName = "";
	private static String deviceSettings_portSettings = "";
	private static String deviceSettings_nickName = "";
	private static boolean deviceSettings_doneFlag = false;
	
	private byte[] settingsData;
	private String path = "/storage/sdcard0/Starmicronics/";

	private final ScheduledExecutorService scheduledTask = (ScheduledExecutorService) Executors.newSingleThreadScheduledExecutor();
	private final Handler guiThreadHandler = new Handler();

	private int samplingNumber = 7;
	private int seekbarMaxAdjustment = 30;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proxiprnt);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		try {
			m_shareManager = StarProxiPRNTManager.getSharedManager();
			ImportSettings();

		} catch (StarIOPortException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			finish();
		}

		listBLE = (ListView) findViewById(R.id.listView_BLEList);

		emptyView = (TextView) findViewById(R.id.emptyView);
		emptyView.setVisibility(View.INVISIBLE);

		adapter_listProxiPRNT = new ProxiPRNTAdapter();
		listBLE.setAdapter(adapter_listProxiPRNT);
		adapter_listProxiPRNT.notifyDataSetChanged();
	}


	@Override
	protected void onPause() {
		super.onPause();

		try {
			m_shareManager.stopScan();
			dataList.clear();
			macAddressList.clear();
			adapter_listProxiPRNT.notifyDataSetChanged();
			
		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void onResume() {
		super.onResume();

		if (ProxiPRNTActivity.getDoneFlag() == true) {
			ProxiPRNTActivity.setDoneFlag(false);
		}

		try {
			m_shareManager.setCallback(new StarProxiPRNTManagerCallback() {
				public void onPortDiscovered(final String portName, final StarDeviceType deviceType, final String macAddress, final int rssi) {
					runOnUiThread(new Runnable() {
						public void run() {
							String dataList_portName   = ""; 
							String dataList_macAddress = macAddress;
							String dataList_nickName = "";
							int dataList_thresHold = -20;

							try {
								if (portName != null) {

									HashMap<String, HashMap<String, Object>> settings = m_shareManager.getAllSettings();
									Set<String> key = settings.keySet();
									for (String portname : key) {
										if (portName.equals(portname)) {
											dataList_portName = portName;
											dataList_macAddress = (String) m_shareManager.getSettings(dataList_portName).get(StarProxiPRNTManager.KEY_STAR_MACADDRESS);
											dataList_nickName   = (String) m_shareManager.getSettings(dataList_portName).get(StarProxiPRNTManager.KEY_STAR_NICKNAME);
											dataList_thresHold  = (Integer) m_shareManager.getSettings(dataList_portName).get(StarProxiPRNTManager.KEY_STAR_THRESHOLDRSSI);
										}
										else {
											//
										}
									}
								}

								if (macAddressList.size() == 0) {
									Index_deviceItemList = 0;
									macAddressList.add(dataList_macAddress);

									dataList.add(Index_deviceItemList, new ProxiPRNTData(dataList_portName,  (-1 * rssi), dataList_macAddress, dataList_nickName, dataList_thresHold));
								} else if (macAddressList.indexOf(dataList_macAddress) >= 0) {
									Index_deviceItemList = macAddressList.indexOf(dataList_macAddress);

									dataList.set(Index_deviceItemList, new ProxiPRNTData(dataList_portName,  (-1 * rssi), dataList_macAddress, dataList_nickName, dataList_thresHold));
								} else {
									Index_deviceItemList = macAddressList.size();
									macAddressList.add(dataList_macAddress);

									dataList.add(Index_deviceItemList, new ProxiPRNTData(dataList_portName,  (-1 * rssi), dataList_macAddress, dataList_nickName, dataList_thresHold));
								}
							} catch (StarIOPortException e) {
								e.printStackTrace();
							}

							adapter_listProxiPRNT.notifyDataSetChanged();

							emptyView.setVisibility(View.INVISIBLE);
						}
					});
				}

				public void onClosestCashDrawerFound(String arg0) {
				}

				public void onClosestPrinterFound(String arg0) {
				}

				public void onStateUpdated(String arg0, final String macAddress) {

					runOnUiThread(new Runnable() {
						public void run() {
							int listPosition = macAddressList.indexOf(macAddress);

							if (listPosition != -1) {
								dataList.remove(listPosition);
								macAddressList.remove(macAddress);
								adapter_listProxiPRNT.notifyDataSetChanged();//更新
							}
							if (listBLE.getCount() == 0) {
								emptyView.setVisibility(View.VISIBLE);
							}
						}
					});
				}
			});
			m_shareManager.startScan(samplingNumber);

			scheduledTask.schedule(checkBluetoothDevice, 1, TimeUnit.SECONDS); // emptyView.setVisibility(View.VISIBLE);
		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (m_shareManager == null) {
			return;
		}
		
		try {
			m_shareManager.stopScan();
		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
		ExportSettings();
	}


	public void PrintOpenDrawer(View view){
		if (!checkClick.isClickEvent()) {
			return;
		}
		pos = (Integer) view.getTag();

		int PROGRESSBAR_MAX_VALUE = 50;

		if ((PROGRESSBAR_MAX_VALUE - (dataList.get(pos)).getRSSI()) > (PROGRESSBAR_MAX_VALUE - ((-1) * (dataList.get(pos)).getThresHold()))) {
			HashMap<String, Object> settings;
			try {
				String portName = (dataList.get(pos)).getPortName();
				settings = m_shareManager.getSettings(portName);

				String deviceType = (String) settings.get(StarProxiPRNTManager.KEY_STAR_DEVICETYPE);

				dataList.clear();
				macAddressList.clear();
				adapter_listProxiPRNT.notifyDataSetChanged();

				if (deviceType.contains("Printer")) {
					printClosestPrinter(portName);
				} else { // DK-AirCash
					openClosestDrawer(portName);
				}
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}

		} else {
			Toast.makeText(this, "RSSI value does not exceed the threshold value.",Toast.LENGTH_SHORT).show();
			try {
				m_shareManager.startScan(samplingNumber);
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
		}

	}


	private static int printableArea = 576;    // for raster data

	public class printTask  extends AsyncTask<Void,Integer,String> {

		private Context context = null;
		private ProgressDialog dialog = null;
		private String title;
		private String msg;

		private String portName;
		private String portSettings;
		private boolean useDrawer;

		private StarIOPort port = null;
		private Resources resources = null;
		private boolean isErrorMessage = true;

		public printTask(Context context,String portName, String portSettings, boolean useDrawer, Resources res ,String title,String msg) {
			this.context = context;
			this.portName = portName;
			this.portSettings = portSettings;
			this.useDrawer = useDrawer;
			this.resources = res;
			this.title = title;
			this.msg = msg;
			
		}


		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setTitle(title);
			dialog.setMessage(msg);
			dialog.show();

			try {
				m_shareManager.stopScan();
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}

		}


		@Override
		protected String doInBackground(Void... params) {
			String message = "";
			boolean isDrawerOpen = false;

			try {
				//create print data
				ArrayList<Byte> list = new ArrayList<Byte>();
				Byte[] tempList;

				printableArea = 576;    // Printable area in paper is 832(dot)

				RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Standard, 0, 0, 0);				
				byte[] command = rasterDoc.BeginDocumentCommandData();
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				String textToPrint = ("                       Star Clothing Boutique\r\n" +
									  "                             123 Star Road\r\n" +
									  "                           City, State 12345\r\n\r\n" +
									  "Date: MM/DD/YYYY                 Time:HH:MM PM\r\n" +
									  "-----------------------------------------------------------------------\r");
				command = createRasterCommand(textToPrint, 13, 0);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				textToPrint = ("SALE");
				command = createRasterCommand(textToPrint, 13, Typeface.BOLD);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				textToPrint = ("SKU \t\t\t                 Description \t\t                Total\r\n" + 
							   "300678566 \t\t\t      PLAIN T-SHIRT		\t\t    10.99\n" +
							   "300692003 \t\t\t      BLACK DENIM		\t\t    29.99\n" +
							   "300651148 \t\t\t      BLUE DENIM		\t\t       29.99\n" +
							   "300642980 \t\t\t      STRIPED DRESS		\t       49.99\n" +
							   "300638471 \t\t\t      BLACK BOOTS		\t\t       35.99\n\n" +
							   "Subtotal\t\t\t\t                                              156.95\r\n" +
							   "Tax		\t\t\t\t                                                     0.00\r\n" +
							   "-----------------------------------------------------------------------\r\n" +
							   "Total   \t                                                   $156.95\r\n" +
							   "-----------------------------------------------------------------------\r\n\r\n" +
							   "Charge\r\n159.95\r\n" +
							   "Visa XXXX-XXXX-XXXX-0123\r\n");

				command = createRasterCommand(textToPrint, 13, 0);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				textToPrint = ("Refunds and Exchanges");
				command = createRasterCommand(textToPrint, 13, Typeface.BOLD);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				textToPrint = ("Within 30 days with receipt\r\n" + "And tags attached");
				command = createRasterCommand(textToPrint, 13, 0);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.qrcode);
				StarBitmap starbitmap = new StarBitmap(bm, false, 146);
				command = starbitmap.getImageRasterDataForPrinting_Standard(true);
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				tempList = null;

				command = rasterDoc.EndDocumentCommandData();
				tempList = new Byte[command.length];
				CopyArray(command, tempList);
				list.addAll(Arrays.asList(tempList));
				if(useDrawer){
					list.addAll(Arrays.asList(new Byte[]{0x07}));                // Kick cash drawer					
				}

				tempList = null;


				// getPort
				port = StarIOPort.getPort(portName, portSettings, 10000, context);

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}

				// beginCheckedBlock
				StarPrinterStatus status = port.beginCheckedBlock();

				if (status.offline == true) {
					message = "A printer is offline";
				}
				if (useDrawer) {
					if (status.compulsionSwitch == false) {
						message = "Drawer was already opened";
						isDrawerOpen = true;
					}
				}


				if (!isDrawerOpen) {
					// writePort
					byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(list);

					port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);


					// endCheckedBlock
					status = port.endCheckedBlock();

					if (status.coverOpen == true) {
						message = "Printer cover is open";
					} else if (status.receiptPaperEmpty == true) {
						message = "Receipt paper is empty";
					} else if (true == status.offline) {
						message = "Printer is offline";
					}

					if (useDrawer) {
						if (false == status.compulsionSwitch) {
							isErrorMessage = false;
							message = "Drawer was opened";
						}
					}
				}
		
			} catch (StarIOPortException e) {
				e.printStackTrace();
				message = e.getMessage();
			} finally {
				if (port != null) {
					try {
						// releasePort
						StarIOPort.releasePort(port);

						m_shareManager.startScan(samplingNumber);
					} catch (StarIOPortException e) { 
						try {
							m_shareManager.startScan(samplingNumber);
						} catch (StarIOPortException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					try {
						m_shareManager.startScan(samplingNumber);
					} catch (StarIOPortException e) {
						e.printStackTrace();
					}
				}
			}

			return message;
		}


		@Override
		protected void onProgressUpdate(Integer... values) {
		}


		@Override
		protected void onPostExecute(String message) {
			dialog.dismiss();

			if (message != "") {
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				if (isErrorMessage) {
					alert.setTitle("Failure");
				}

				alert.setMessage(message);
				alert.setCancelable(false);
				alert.show();
			}
		}
	}


	public class openDrawerTask extends AsyncTask<Void,Integer,String> {

		private Context context = null;
		private ProgressDialog dialog = null;
		private String title;
		private String msg;
		
		private String portName;
		private String portSettings;
		
		private StarIOPort port = null;
		private boolean isErrorMessage = true;


		public openDrawerTask(Context context,String portName, String portSettings, String title,String msg) {
			this.context = context;
			this.portName = portName;
			this.portSettings = portSettings;
			this.title = title;
			this.msg = msg;
		}


		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setTitle(title);
			dialog.setMessage(msg);
			dialog.show();

			try {
				m_shareManager.stopScan();
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
		}


		@Override
		protected String doInBackground(Void... params) {
			String message = "";
			boolean isDrawerOpen = false;
			
			try {
				//getport
				port = StarIOPort.getPort(portName, portSettings, 10000, context);
				
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}

				// beginCheckedBlock
				StarPrinterStatus status = port.beginCheckedBlock();

				if (status.offline == true) {
					message = "A Drawer is offline";
				}

				if (status.compulsionSwitch == false) {
					message = "Drawer was already opened";
					isDrawerOpen = true;
				}

				if (!isDrawerOpen) {
					// writePort
					byte[] command = new byte[] {0x07};
					port.writePort(command, 0, command.length);

					// endCheckedBlock
					status = port.endCheckedBlock();
					SystemClock.sleep(150);
					status = port.retreiveStatus();

					if (status.offline == true) {
						message = "A Drawer is offline";
					}

					if (status.compulsionSwitch == false) {
						isErrorMessage = false;
						message = "Drawer was opened";
					}
				}

			} catch (StarIOPortException e) {
				e.printStackTrace();
				message = e.getMessage();
			} finally {
				if (port != null) {
					try {
						// releasePort
						StarIOPort.releasePort(port);

						m_shareManager.startScan(samplingNumber);
					} catch (StarIOPortException e) {
						try {
							m_shareManager.startScan(samplingNumber);
						} catch (StarIOPortException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					try {
						m_shareManager.startScan(samplingNumber);
					} catch (StarIOPortException e) {
						e.printStackTrace();
					}
				}
			}

			return message;
		}


		@Override
		protected void onProgressUpdate(Integer... values) {
		}


		@Override
		protected void onPostExecute(String message) {
			dialog.dismiss();

			if (message != "") {
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				if (isErrorMessage) {
					alert.setTitle("Failure");
				}

				alert.setMessage(message);
				alert.setCancelable(false);
				alert.show();
			}
		}
	}


	private void ExportSettings () {
		if (!checkClick.isClickEvent()) {
			return;
		}

		settingsData = m_shareManager.serializedSettings();

		File srcFile = null;
		OutputStream out = null;

		try {
			File dirs = new File(path); 
			if (!dirs.exists()) {
				dirs.mkdirs(); // make folders  
			}

			srcFile = new File(path +"setting.json");
			out = new FileOutputStream(srcFile, false);//file override

			out.write(settingsData, 0, settingsData.length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private void ImportSettings (){

		// Folder Path
		File srcFile = null;
		InputStream in = null;
		byte[] readBytes = null;


		try {
			File dirs = new File(path);
			if (!dirs.exists()) {
			} else {
				srcFile = new File( path + "setting.json");
				in = new FileInputStream(srcFile);

				readBytes = new byte[in.available()];
				in.read(readBytes);

				try {
					m_shareManager.deserializeSetting(readBytes);
				} catch (StarIOPortException e) {
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	private void openClosestDrawer (String portName) {
		Context context = this;

		if(portName != "") {
			try {
				HashMap<String, Object> settings = m_shareManager.getSettings(portName);

				String portSetting = (String) settings.get(StarProxiPRNTManager.KEY_STAR_PORTSETTIGS);

				openDrawerTask opendrawertask = new openDrawerTask(context, portName, portSetting, "It's processing", "Please waiting");
				opendrawertask.execute();

			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
		}
	}


	private void printClosestPrinter (String portName) {
		Context context = this;

		if (portName != "") {
			try {
				HashMap<String, Object> settings = m_shareManager.getSettings(portName);

				String portSetting = (String) settings.get(StarProxiPRNTManager.KEY_STAR_PORTSETTIGS);

				Boolean withDrawer = (Boolean) settings.get(StarProxiPRNTManager.KEY_STAR_WITHDRAWER);

				if (withDrawer) {
					// SampleRecipt + Drawer
					printTask task = new printTask(context, portName, portSetting, withDrawer, getResources(), "It's processing", "Please waiting");
					task.execute();
				} else {
					// Only SampleRecipt
					printTask task = new printTask(context, portName, portSetting, withDrawer,  getResources(), "It's processing", "Please waiting");
					task.execute();
				}

			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
		}
	}


	public void selectDeviceSettingsDialog(Context context){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("ProxiPRNT Device Settings");

		String[] items = {"Edit Device Settings", "Delete Device Setting"};
		builder.setItems(items, m_ItemListener);
		builder.setPositiveButton("Cancel",  new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					m_shareManager.startScan(samplingNumber);
				} catch (StarIOPortException e) {
					e.printStackTrace();
				}
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	DialogInterface.OnClickListener m_ItemListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			final int position = pos;

			if (which == 0) { // Edit Device Settings

				String portName = (dataList.get(position)).getPortName();
				ProxiPRNTDeviceSelectActivity.setPortName(portName);
				Intent myIntent = new Intent(ProxiPRNTActivity.this, ProxiPRNTDeviceSettingsActivity.class);
				startActivityFromChild(ProxiPRNTActivity.this, myIntent, 0);

			}

			if (which == 1) { // Delete Information
				String portName = (dataList.get(position)).getPortName();
				try {
					m_shareManager.removeSettingsWithPortName(portName);
				} catch (StarIOPortException e1) {
					e1.printStackTrace();
				}

				dataList.set(position, new ProxiPRNTData("",  20, "00:00:00:00:00:00", "" , -1));
				adapter_listProxiPRNT.notifyDataSetChanged();
				try {
					m_shareManager.startScan(samplingNumber);
				} catch (StarIOPortException e) {
					e.printStackTrace();
				}
			}
		}
	};


	protected static String getMacAddress() {
		return deviceSettings_macAddress;
	}


	protected static String getPortName() {
		return deviceSettings_portName;
	}


	protected static void setPortName(String portName) {
		deviceSettings_portName = portName;
	}


	protected static String getPortSettings() {
		return deviceSettings_portSettings;
	}


	protected static void setPortSettings(String portSettings) {
		deviceSettings_portSettings = portSettings;
	}


	protected static boolean getDoneFlag() {
		return deviceSettings_doneFlag;
	}


	protected static void setDoneFlag(boolean doneFlag) {
		deviceSettings_doneFlag = doneFlag;
	}


	protected static String getNickName() {
		return deviceSettings_nickName;
	}


	protected static void setNickName(String nickName) {
		deviceSettings_nickName = nickName;
	}


	private class ProxiPRNTAdapter extends BaseAdapter {

		public int getCount() {
		   return dataList.size();
		}


		public Object getItem(int position) {
			return dataList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}


		public View getView(int position, View convertView, ViewGroup parent) {

			TextView textView_port;
			TextView textView_pleaseSelectDevice;
			ProgressBar progressBar_BLE;
			TextView textView_macAdd;
			TextView textView_nickname;
			Button button_DeviceSettings;
			View v = convertView;
			Button button_PrintorCashDrawer;

			if (v == null) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.list_row_text_with_progress, null);
			}

			ProxiPRNTData proxiPRNTData = (ProxiPRNTData)getItem(position);

			if (proxiPRNTData != null) {
				textView_port               = (TextView) v.findViewById(R.id.TextPortName);
				textView_pleaseSelectDevice = (TextView) v.findViewById(R.id.Text_pleaseSelectDevice);
				progressBar_BLE             = (ProgressBar) v.findViewById(R.id.progressBar_BLE);
				textView_macAdd             = (TextView) v.findViewById(R.id.TextMACAddress);
				textView_nickname           = (TextView) v.findViewById(R.id.TextNickName);
				button_DeviceSettings       = (Button) v.findViewById(R.id.buttonDeviceSettings);
				button_PrintorCashDrawer    = (Button) v.findViewById(R.id.buttonPrint_OpenDrawer);

				if (((progressBar_BLE.getMax() + seekbarMaxAdjustment)- proxiPRNTData.rssi) > ((progressBar_BLE.getMax() + seekbarMaxAdjustment)- ((-1)*proxiPRNTData.thresHold))) {
					button_PrintorCashDrawer.setBackgroundColor(Color.YELLOW);
				} else {
					button_PrintorCashDrawer.setBackgroundColor(Color.DKGRAY);
				}

				textView_nickname.setTextColor(Color.WHITE);
				if (proxiPRNTData.portName.equals(foundPortName)) {
					textView_nickname.setTextColor(Color.YELLOW);
				}

				if (proxiPRNTData.portName.length() == 0) {
					button_PrintorCashDrawer.setVisibility(View.INVISIBLE);
					button_PrintorCashDrawer.setBackgroundColor(Color.DKGRAY);
					textView_pleaseSelectDevice.setVisibility(View.VISIBLE);

					Drawable d = getResources().getDrawable(R.xml.unregistered_progressbar);
					progressBar_BLE.setProgressDrawable(d);

				} else {
					button_PrintorCashDrawer.setVisibility(View.VISIBLE);
					textView_pleaseSelectDevice.setVisibility(View.INVISIBLE);

					Drawable d = getResources().getDrawable(R.xml.setting_progressbar_ble);
					progressBar_BLE.setProgressDrawable(d);
				}
	
				textView_port.setText(proxiPRNTData.portName);
				progressBar_BLE.setProgress(((progressBar_BLE.getMax() + seekbarMaxAdjustment)- proxiPRNTData.rssi));
				progressBar_BLE.setSecondaryProgress((progressBar_BLE.getMax() + seekbarMaxAdjustment) - ((-1)*proxiPRNTData.thresHold));
				textView_macAdd.setText(proxiPRNTData.macAd);
				textView_nickname.setText(proxiPRNTData.nickName);

				button_DeviceSettings.setTag(Integer.valueOf(position));
				button_PrintorCashDrawer.setTag(Integer.valueOf(position));
				progressBar_BLE.setTag(Integer.valueOf(position));

				button_DeviceSettings.setOnTouchListener(new View.OnTouchListener() {

					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {

							pos = (Integer) v.getTag();
							deviceSettings_macAddress = (dataList.get(pos)).getMacAdress();
							deviceSettings_portName   = (dataList.get(pos)).getPortName();

							if (deviceSettings_portName == "") {
								Intent myIntent = new Intent(ProxiPRNTActivity.this, ProxiPRNTDeviceSelectActivity.class);
								startActivityFromChild(ProxiPRNTActivity.this, myIntent, 0);
							} else {
								try {
									if (m_shareManager.getSettings(deviceSettings_portName).get(StarProxiPRNTManager.KEY_STAR_MACADDRESS).equals(deviceSettings_macAddress)) {
										m_shareManager.stopScan();
										selectDeviceSettingsDialog(ProxiPRNTActivity.this);
									} else {
										Intent myIntent = new Intent(ProxiPRNTActivity.this, ProxiPRNTDeviceSelectActivity.class);
										startActivityFromChild(ProxiPRNTActivity.this, myIntent, 0);
									}
								} catch (StarIOPortException e) {
									e.printStackTrace();
								}
							}
						}

						return true;
					}
				});

				button_PrintorCashDrawer.setOnTouchListener(new View.OnTouchListener() {

					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_DOWN){
							PrintOpenDrawer(v);
						}
						return true;
					}
				});

			}

			return v;
		}
	}


	public class ProxiPRNTData {  
		String portName;
		int    rssi;
		String macAd;
		String nickName;
		int     thresHold;


		public ProxiPRNTData(String portName, int rssi, String macAd, String nickName, int thresHold) {
			this.portName  = portName;
			this.rssi      = rssi;
			this.macAd     = macAd;
			this.nickName  = nickName;
			this.thresHold = thresHold;
		}


		public String getPortName() {
		    return portName;
		}


		public int getRSSI() {
			return rssi;
		}


		public int getThresHold() {
			return thresHold;
		}


		public String getMacAdress() {
			return macAd;
		}


		public String getNickName() {
			return nickName;
		}


		public void setRSSI(int rssi) {
			this.rssi = rssi;
		}
	}


	private static byte[] createRasterCommand(String printText, int textSize, int bold) {
		byte[] command;

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);

		Typeface typeface;

		try {
			typeface = Typeface.create(Typeface.SERIF, bold);
		} catch (Exception e) {
			typeface = Typeface.create(Typeface.DEFAULT, bold);
		}

		paint.setTypeface(typeface);
		paint.setTextSize(textSize * 2);
		paint.setLinearText(true);

		TextPaint textpaint = new TextPaint(paint);
		textpaint.setLinearText(true);
		android.text.StaticLayout staticLayout =  new StaticLayout(printText, textpaint, printableArea, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
		int height = staticLayout.getHeight();

		Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bitmap);
		c.drawColor(Color.WHITE);
		c.translate(0, 0);
		staticLayout.draw(c);

		StarBitmap starbitmap = new StarBitmap(bitmap, false, printableArea);

		command = starbitmap.getImageRasterDataForPrinting_Standard(true);

		return command;
	}


	private static void CopyArray(byte[] srcArray, Byte[] cpyArray) {
		for (int index = 0; index < cpyArray.length; index++) {
			cpyArray[index] = srcArray[index];
		}
	}


	private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
		byte[] byteArray = new byte[ByteArray.size()];

		for(int index = 0; index < byteArray.length; index++) {
			byteArray[index] = ByteArray.get(index);
		}

		return byteArray;
	}


	private String foundPortName = null;

	public void searchPortName() {
		int count = 0;
		try {
			count = m_shareManager.getAllSettings().size();
		} catch (StarIOPortException e1) {
			e1.printStackTrace();
		}

		HashMap<String, HashMap<String, Object>> arraySettings;

		try {
			arraySettings = m_shareManager.getAllSettings();
			Set<String> keys = (arraySettings.keySet());//key is portName
			foundPortNameList.clear();
			for (String key : keys) {
				HashMap<String, Object> setting = arraySettings.get(key);

				for (int i=0; i< macAddressList.size(); i++) {
					if (macAddressList.get(i).equals(setting.get(StarProxiPRNTManager.KEY_STAR_MACADDRESS))) {

						if ((macAddressList.size() > 1) && (count == 1)) {
							foundPortName = key;
							break;
						} else { // for Threshold Settings List
							String blemacAddressforList = (String) m_shareManager.getSettings(key).get(StarProxiPRNTManager.KEY_STAR_MACADDRESS);
							String nickNameforList      = (String) m_shareManager.getSettings(key).get(StarProxiPRNTManager.KEY_STAR_NICKNAME);

							foundPortNameList.add(key + "\n - " + nickNameforList + "\n - " +blemacAddressforList);
							break;
						}
					}
				}
			}

		} catch (StarIOPortException e1) {
			e1.printStackTrace();
		}
	}



	final Runnable checkBluetoothDevice = new Runnable() {
		public void run() {
			if (listBLE.getCount() == 0) {
				postGUIupdate(true);
			} else {
				postGUIupdate(false);
			}
		};
	};


	/**
	 * To move data from a background thread to the UI thread.
	 * 
	 * @param isVisible
	 *     true: Show a message.
	 *     false: Hide a message.
	 */
	private void postGUIupdate(final boolean isVisible) {
		guiThreadHandler.post(new Runnable() {
			public void run() {
				if (isVisible == true){
					emptyView.setVisibility(View.VISIBLE);
				} else {
					emptyView.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
}
