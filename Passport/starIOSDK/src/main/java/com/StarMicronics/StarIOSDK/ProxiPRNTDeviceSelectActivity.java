package com.StarMicronics.StarIOSDK;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

public class ProxiPRNTDeviceSelectActivity extends Activity {

	private ListView listDiscoveryDevice;
	private ListView listUSBPrinter;
	private List<DiscoveredData> dataList = new ArrayList<DiscoveredData>();
	private static DiscoveredDeviceAdapter adapter_list_discoveredDevice;
	private static SelectDeviveType selectDeviceType = SelectDeviveType.BT_TCP;
	private static String setPortName = "";
	private static String modelName = "";
	private  ProgressDialog m_ProgressDialog = null;

	enum SelectDeviveType {
		BT_TCP,
		USB_VIA_AIRPORT
	}

	
    private void showProgressDialog() {
        if (m_ProgressDialog == null) {
        	m_ProgressDialog = new ProgressDialog(ProxiPRNTDeviceSelectActivity.this);
        	m_ProgressDialog.setMessage("Loading. Please wait...");
//        	m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	m_ProgressDialog.setIndeterminate(false);
        	m_ProgressDialog.setCancelable(false);
        }
        m_ProgressDialog.show();
    }
	
    private void dismissProgressDialog() {
        if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
        	m_ProgressDialog.dismiss();
        }
    }

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_proxiprnt_select_device);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					
		class AsyncTaskProgressDiscovery extends AsyncTask<Void, Void, ArrayList<String>>{
	
		    @Override
		    protected void onPreExecute() {
		    	
		    	showProgressDialog();	    	

		    	return;
		    }
		    
			@Override
			protected ArrayList<String> doInBackground(Void... params) {
				ArrayList<String> itemList = getPortDiscovery("All");
				return itemList;
			}
			
			@Override
			protected void onPostExecute(ArrayList<String> itemList) {

				dismissProgressDialog();
								  
				String[] itemMember_discovery = itemList.toArray(new String[itemList.size()]);
				listDiscoveryDevice = (ListView) findViewById(R.id.listView_discover_deviced_proxiprnt);
		        adapter_list_discoveredDevice = new DiscoveredDeviceAdapter();
				listDiscoveryDevice.setAdapter(adapter_list_discoveredDevice);
		        
				listDiscoveryDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			        	selectDeviceType = ProxiPRNTDeviceSelectActivity.SelectDeviveType.BT_TCP;

		            	ProxiPRNTActivity.setPortName((dataList.get(position)).getPortName());
		            	setPortName = (dataList.get(position)).getPortName();
		            	modelName = (dataList.get(position)).getPortInformation();
		        		ProxiPRNTActivity.setPortSettings("");
		        		ProxiPRNTActivity.setNickName("Sample Nick Name");

		        		Intent myIntent = new Intent(ProxiPRNTDeviceSelectActivity.this, ProxiPRNTDeviceSettingsActivity.class);
			        	startActivityFromChild(ProxiPRNTDeviceSelectActivity.this, myIntent, 0);
			        }
			    });
				
				
				for (String deviceName : itemMember_discovery) {	
				    dataList.add(new DiscoveredData(deviceName));
				}
				
			}
			
		}
        new AsyncTaskProgressDiscovery().execute();
		
		String[] itemMember_usb = {"USB Printer via AirPort"};
		listUSBPrinter = (ListView) findViewById(R.id.listView_usb_printer_proxiprnt);
		listUSBPrinter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	        	if (position == 0) {
	            	selectDeviceType = ProxiPRNTDeviceSelectActivity.SelectDeviveType.USB_VIA_AIRPORT;
	            	setPortName = ProxiPRNTDeviceSelectActivity.SelectDeviveType.USB_VIA_AIRPORT.toString();
	            	ProxiPRNTActivity.setPortName("TCP:192.168.192.45");
	        		ProxiPRNTActivity.setPortSettings("9100");
	        		ProxiPRNTActivity.setNickName("Sample Nick Name");
	            } else {
	            	//
	            }
	        	Intent myIntent = new Intent(ProxiPRNTDeviceSelectActivity.this, ProxiPRNTDeviceSettingsActivity.class);
	        	startActivityFromChild(ProxiPRNTDeviceSelectActivity.this, myIntent, 0);
	        }
	    });

		ArrayAdapter<String> adapter_USBPrinter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, itemMember_usb);
		listUSBPrinter.setAdapter(adapter_USBPrinter);
	}

	private ArrayList<String> getPortDiscovery(String interfaceName) {
    	List<PortInfo> BTPortList;
    	List<PortInfo> TCPPortList;

    	final ArrayList<PortInfo> arrayDiscovery;
    	ArrayList<String> arrayPortName;

		arrayDiscovery = new ArrayList<PortInfo>();
		arrayPortName = new ArrayList<String>();
		
    	try {
    		if (true == interfaceName.equals("Bluetooth") || true == interfaceName.equals("All")) {
    			BTPortList  = StarIOPort.searchPrinter("BT:");   
    	    	
    			for (PortInfo portInfo : BTPortList) {
  	    		    arrayDiscovery.add(portInfo);
  	    	    }
    		}
    		if (true == interfaceName.equals("LAN") || true == interfaceName.equals("All")) {
    			TCPPortList = StarIOPort.searchPrinter("TCP:");
    			
    	    	for (PortInfo portInfo : TCPPortList) {
    	    		arrayDiscovery.add(portInfo);
    	    	}
    		}
    		
    		arrayPortName = new ArrayList<String>();

		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
    	
		for(PortInfo discovery : arrayDiscovery)
		{
			String portName;

			portName = discovery.getPortName();

			if(discovery.getMacAddress().equals("") == false)
			{
				portName += "\n - " + discovery.getMacAddress();
				if(discovery.getModelName().equals("") == false)
				{
					portName += "\n - " + discovery.getModelName();
				}
			}

			arrayPortName.add(portName);
		}
    	
    	return arrayPortName;
	}

	protected static SelectDeviveType getSelectDeviceType() {
		return selectDeviceType;
	}

	protected static String getPortName() {
		return setPortName;
	}
	
	protected static void setPortName(String PortName){
		setPortName = PortName;
	}
	
	
	protected static void setModelName(String ModelName){
		
	    modelName = ModelName;
	}
	
	protected static String getModeName() {
		
		String modelName1 = "";
		String delimiter = "\n - ";
			
		  if (modelName.lastIndexOf(delimiter) != -1) {
			  int delimiterLastIndex = modelName.lastIndexOf(delimiter);
	   	
			  modelName1 = modelName.substring(delimiterLastIndex + delimiter.length(), modelName.length());
		  }
		  else{
			  if(modelName.equalsIgnoreCase("SAC10")){
				  modelName1 = modelName;
			  }else{
				  modelName1 = "";  
			  }

		  }
			
		return modelName1;
	}	
	
	private class DiscoveredDeviceAdapter extends BaseAdapter {
 
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
	        TextView textView_discovered_device;
	        View v = convertView;

	        if (v == null) {
		        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        v = inflater.inflate(R.layout.list_row_discovered_device, null);
	        }

	        DiscoveredData m_discoveredData = (DiscoveredData) getItem(position);
	        if (m_discoveredData != null) {
	        	textView_discovered_device = (TextView) v.findViewById(R.id.textView_discovered_device);

	        	textView_discovered_device.setText(m_discoveredData.getPortInformation());
		    }
		    return v;
		}
    }
	 
	public class DiscoveredData {  
		String portInformation;
	  
		public DiscoveredData(String portInformation){
		    this.portInformation = portInformation;
		}
	
		public String getPortInformation(){
		    return portInformation;
		}
		
		public String getPortName(){
			String portName = "";
			  String delimiter = "\n - ";
				
			  if (portInformation.indexOf(delimiter) != -1) {
				  int delimiterIndex = portInformation.indexOf(delimiter);
		   	
				  portName = portInformation.substring(0, delimiterIndex);
			  }
			  else{
				  portName = portInformation;
			  }
			
			return portName;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (ProxiPRNTActivity.getDoneFlag() == true) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		dismissProgressDialog();

		super.onDestroy();
		if (ProxiPRNTActivity.getDoneFlag() == true) {
			finish();
		}
	}
	
}
