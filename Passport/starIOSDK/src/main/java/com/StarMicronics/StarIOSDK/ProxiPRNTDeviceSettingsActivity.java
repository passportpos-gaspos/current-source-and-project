package com.StarMicronics.StarIOSDK;

import java.util.HashMap;
import java.util.Set;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarProxiPRNTManager;
import com.starmicronics.stario.StarProxiPRNTManagerCallback;


public class ProxiPRNTDeviceSettingsActivity extends Activity {
	private StarProxiPRNTManager m_shareManager;
	private TextView textView_threshold;
	private TextView textView_rssi;
	private SeekBar seekBar_threshold;
	private String bleMacAddress;
	private int samplingNumber = 7;
	private int seekbarMaxAdjustment = 30;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_proxiprnt_settings);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		initializeComponent();
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			m_shareManager.stopScan();
		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		setStarProxiManagerCallback();

		try {
			m_shareManager.startScan(samplingNumber);
		} catch (StarIOPortException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean  dispatchKeyEvent (KeyEvent event) {	
		int count = (-1) *  Integer.valueOf(textView_threshold.getText().toString());
		
	    switch (event.getAction()) {
	    case KeyEvent.ACTION_DOWN:
	        switch (event.getKeyCode()) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
        	            
	        	count--;
	        	if(count <= 30){
	        		count = 30; 
		    	    seekBar_threshold.setProgress(80 - count);
		    	    textView_threshold.setText("-" + count);
	        		return true;
	        	};
	    	    seekBar_threshold.setProgress(80 - count);
	    	    textView_threshold.setText("-" + count);
	    	    return true;
	    	    
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
        	
	        	count++;
	        	if(count >= 80){
	        		count = 80; 
		    	    seekBar_threshold.setProgress(80 - count);
		    	    textView_threshold.setText("-" + count);
	        		return true;
	        	};
	    	    seekBar_threshold.setProgress(80 - count);
	    	    textView_threshold.setText("-" + count);
	            return true;
	             
	        default:
	            break;
	        }
	        break;
	         
	    case KeyEvent.ACTION_UP:
	        switch (event.getKeyCode()) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            return true;
	        default:
	            break;
	        }
	        break;
	         
	    default:
	        break;
	    }

		
		return super.dispatchKeyEvent(event);
		
	}
	
    public void SetRSSIThreshold(View view) {
    	if (!checkClick.isClickEvent()) return;
    	
    	bleMacAddress = ProxiPRNTActivity.getMacAddress();
        
		Builder dialog = new AlertDialog.Builder(ProxiPRNTDeviceSettingsActivity.this);
		dialog.setPositiveButton("Start Calibration",  new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
        	  CalibrationTask calibrationTask = new CalibrationTask(ProxiPRNTDeviceSettingsActivity.this, bleMacAddress, "It's processing", "Please wait a few seconds");
        	  calibrationTask.execute();
          }
      });
		dialog.setNegativeButton("Cancel", null);
		AlertDialog alert = dialog.create();
		alert.setTitle("Calibrate RSSI Threshold");
		alert.setMessage("Please do not move the Android device");
		alert.setCancelable(false);
		alert.show();

    }
    
	public class CalibrationTask extends AsyncTask<Void,Integer,Integer>{

		private Context context = null;
		private ProgressDialog dialog = null;
		private String title;
		private String msg;
		private String dongleMacAddress;
		private String errormessage;

		public CalibrationTask(Context context, String dongleMacAddress, String title,String msg){
			this.context = context;
			this.title = title;
			this.msg = msg;
			this.dongleMacAddress = dongleMacAddress;
			this.errormessage = "";
			
		}

		
		@Override
		protected void onPreExecute(){
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
		protected Integer doInBackground(Void... params) {
			
			int calibrateRSSI = -50;
			try {
				 calibrateRSSI = m_shareManager.calibrateActionArea(dongleMacAddress);
				
			} catch (StarIOPortException e) {
				errormessage = e.getMessage();
			}
			return calibrateRSSI;
		}


		@Override
		protected void onProgressUpdate(Integer... values) {			
			
		}
		
		@Override
		protected void onPostExecute(Integer result){
			dialog.dismiss();
			
			try {
				m_shareManager.startScan(samplingNumber);
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
			
		    int rssi_bar = ((seekBar_threshold.getMax() + seekbarMaxAdjustment) - (-1) * result);
			int rssi_text = (-1) * result;
    	    seekBar_threshold.setProgress(rssi_bar);
    	    textView_threshold.setText("-" + rssi_text);
    	    
			if(errormessage != ""){
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				alert.setTitle("Failure");
				alert.setMessage(errormessage);
				alert.setCancelable(false);
				alert.show();	
			}
		    
		}
		
	}


	private void initializeComponent() {
		ProxiPRNTDeviceSelectActivity.SelectDeviveType selectDeviceType = ProxiPRNTDeviceSelectActivity.getSelectDeviceType();
		String PortName = ProxiPRNTDeviceSelectActivity.getPortName();
		String ModelName = ProxiPRNTDeviceSelectActivity.getModeName();		
		
  	    TextView portNameText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_portname);  	  

  	    EditText portNameEditText = (EditText)findViewById(R.id.editText_proxiprnt_device_settings_portname);   
  	  
  	    EditText nickNameEditText = (EditText)findViewById(R.id.editText_proxiprnt_device_settings_nickname);
  	  
  	    TextView portSettingsText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_portsettigns);
  	    
        Spinner spinner_poprtSettings = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_portsettings);
        ArrayAdapter<String> spinner_ad_poprtSettings_number = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] {"Standard", "9100", "9101", "9102", "9103", "9104", "9105", "9106", "9107", "9108", "9109"});
        spinner_poprtSettings.setAdapter(spinner_ad_poprtSettings_number);
        spinner_ad_poprtSettings_number.setDropDownViewResource(R.layout.spinner_dropdown);
        
        Spinner spinner_bluetooth_communication_type = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_bluetoothsettings);
        ArrayAdapter<String> ad_bluetooth_communication_type = new ArrayAdapter<String>(this, R.layout.spinner, new String[] {"SSP", "PIN Code"});
        spinner_bluetooth_communication_type.setAdapter(ad_bluetooth_communication_type);
        ad_bluetooth_communication_type.setDropDownViewResource(R.layout.spinner_dropdown);

        Spinner spinner_lan_communication_type = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_lansettings);
        ArrayAdapter<String> ad_lan_communication_type = new ArrayAdapter<String>(this, R.layout.spinner, new String[] {"Wired", "Wireless"});
        spinner_lan_communication_type.setAdapter(ad_lan_communication_type);
        ad_lan_communication_type.setDropDownViewResource(R.layout.spinner_dropdown);
        
        
        Spinner spinner_bluetooth_cashdrawerSettings = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_usecashdrawersettings);
        ArrayAdapter<String> spinner_ad_bluetooth_cashdrawerSettings = new ArrayAdapter<String>(this, R.layout.spinner, new String[] {"No Use", "Use", "Use (DK-AirCash)"});
        spinner_bluetooth_cashdrawerSettings.setAdapter(spinner_ad_bluetooth_cashdrawerSettings);
        spinner_ad_bluetooth_cashdrawerSettings.setDropDownViewResource(R.layout.spinner_dropdown);
        
        TextView bluetoothSettingsText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_bluetoothsettigns);
        TextView lanSettingsText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_lanettigns);
              
        TextView portSettingText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_printersettings);
        TextView useCashDrawerText = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_usecashdrawer);
        ToggleButton useCashDrawertoggle = (ToggleButton)findViewById(R.id.toggleButton_proxiprnt_device_settings_usecashdrawer);
		
        boolean isNewSetting = false;
        String newPortName = "";
        String portname = "";
       
        try {
            portname = ProxiPRNTActivity.getPortName();
            String blemacaddress = ProxiPRNTActivity.getMacAddress();
			m_shareManager = StarProxiPRNTManager.getSharedManager();
			
			int size = m_shareManager.getAllSettings().size();
			
			HashMap<String, HashMap<String, Object>> arraySettings;
			try {
				arraySettings = m_shareManager.getAllSettings();
				Set<String> keys = (arraySettings.keySet());//key is portName
							
		        for(String key : keys) {
		        	for(int i=0; i< size; i++){
			        	if (portname.equals(key)) {
			        		newPortName = key;
			        		break;
			        	}	        		
		        	}
		        }				
			} catch (StarIOPortException e1) {
				e1.printStackTrace();
			}
			
			
        	if((size > 0) && (newPortName != "")){
    			if(m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_MACADDRESS).equals(blemacaddress)){

    				isNewSetting = false;
    			}
    			else{
    				isNewSetting = true;
    			}        		
        	}
        	else{
        		isNewSetting = true;
        	}
		} catch (StarIOPortException e1) {
			e1.printStackTrace();
		}
        
        
        if(isNewSetting){//New Setting
    		if (selectDeviceType == ProxiPRNTDeviceSelectActivity.SelectDeviveType.BT_TCP) {
    			if(PortName.startsWith("BT:"))
    			{
    				useCashDrawertoggle.setVisibility(View.GONE);
    				
        			portNameText.setVisibility(View.GONE);
        	  	    portNameEditText.setVisibility(View.GONE);
        	  	    portSettingsText.setVisibility(View.GONE);
        	  	    spinner_poprtSettings.setVisibility(View.GONE);
        	  	    lanSettingsText.setVisibility(View.GONE);
        	  	    spinner_lan_communication_type.setVisibility(View.GONE);
    			}
    			else//TCP
    			{			
    				portNameEditText.setText(portname);
    				bluetoothSettingsText.setVisibility(View.GONE);
    				spinner_bluetooth_communication_type.setVisibility(View.GONE);
    				spinner_bluetooth_cashdrawerSettings.setVisibility(View.GONE);
    				
    				if(ModelName.contains("SAC10"))
    				{
    					portSettingText.setVisibility(View.GONE);
    					useCashDrawerText.setVisibility(View.GONE);
    					useCashDrawertoggle.setVisibility(View.GONE);
    					
    	    			portNameText.setVisibility(View.GONE);
    	    	  	    portNameEditText.setVisibility(View.GONE);
    	    	  	    portSettingsText.setVisibility(View.GONE);
    	    	  	    spinner_poprtSettings.setVisibility(View.GONE);
    				}
    				else
    				{
    					lanSettingsText.setVisibility(View.GONE);
    					spinner_lan_communication_type.setVisibility(View.GONE);
    				}
    			}
    		} 
    		else {//USB_VIA_AIRPORT		
    			portNameEditText.setText("TCP:192.168.192.45");
    			bluetoothSettingsText.setVisibility(View.GONE);
    			lanSettingsText.setVisibility(View.GONE);
    			spinner_bluetooth_communication_type.setVisibility(View.GONE);
    			spinner_bluetooth_cashdrawerSettings.setVisibility(View.GONE);
    			spinner_lan_communication_type.setVisibility(View.GONE);
    		}
    		
            seekBar_threshold = (SeekBar)findViewById(R.id.seekBar_proxiprnt_device_settings_threshold);
            textView_threshold = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_threshold);           
            textView_threshold.setText("-" + ((seekBar_threshold.getMax() + seekbarMaxAdjustment)- seekBar_threshold.getProgress()));
            textView_rssi = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_raw_rssi);
            textView_rssi.setText("-" + ((seekBar_threshold.getMax() + seekbarMaxAdjustment)- seekBar_threshold.getProgress()));

        }else{//Re Settings        	
			try {
				String portsettings = (String)  m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_PORTSETTIGS);
				String nickname     = (String)  m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_NICKNAME);
				int thresholdrssi   = (Integer) m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_THRESHOLDRSSI);
				boolean withdrawer  = (Boolean) m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_WITHDRAWER);
				String deviceType   = (String)  m_shareManager.getSettings(newPortName).get(StarProxiPRNTManager.KEY_STAR_DEVICETYPE);
				
				//Check Port Name
				if(newPortName.startsWith("BT:")){
					useCashDrawertoggle.setVisibility(View.GONE);
	    			portNameText.setVisibility(View.GONE);
	    	  	    portNameEditText.setVisibility(View.GONE);
	    	  	    portSettingsText.setVisibility(View.GONE);
	    	  	    spinner_poprtSettings.setVisibility(View.GONE);
	    	  	    spinner_lan_communication_type.setVisibility(View.GONE);
	    	  	    
					lanSettingsText.setVisibility(View.GONE);
					spinner_lan_communication_type.setVisibility(View.GONE);
				
					nickNameEditText.setText(nickname);
					
					if(portsettings.contains(";p")){//.equalsIgnoreCase(";p")){	
						spinner_bluetooth_communication_type.setSelection(1);//0:SSP/1:PINCODE
					}

					if(withdrawer){
						if(deviceType.equalsIgnoreCase("printer")){
							//POSPrinter
							spinner_bluetooth_cashdrawerSettings.setSelection(1);//0:NoUse/1:Use/2:Use(DK-AirCash)
						}else{//DK-AirCash
							spinner_bluetooth_cashdrawerSettings.setSelection(2);//0:NoUse/1:Use/2:Use(DK-AirCash)
						}						
					}else{
						
					}
				}else{//TCP:				
					if(deviceType.equalsIgnoreCase("printer")){
						if(!portsettings.equalsIgnoreCase("")){												
							char portsettingnumber = portsettings.charAt(3);
							int  portSettingIndex  = Integer.parseInt("" + portsettingnumber) + 1;
							spinner_poprtSettings.setSelection(portSettingIndex);						
						}
						portNameEditText.setText(newPortName);
						
						if(withdrawer){
							useCashDrawertoggle.setChecked(true);
				
						}else{
							useCashDrawertoggle.setChecked(false);								
						}
						
    					lanSettingsText.setVisibility(View.GONE);
    					spinner_lan_communication_type.setVisibility(View.GONE);
						
					}else{//DK-AirCash(SAC10)
						portSettingText.setVisibility(View.GONE);
						useCashDrawerText.setVisibility(View.GONE);
						useCashDrawertoggle.setVisibility(View.GONE);
						
						portNameEditText.setText(newPortName);
		    	  	    portSettingsText.setVisibility(View.GONE);
		    	  	    spinner_poprtSettings.setVisibility(View.GONE);
		    	  	    
						ProxiPRNTDeviceSelectActivity.setModelName("SAC10");
						
						if(portsettings.contains(";wl")){
						spinner_lan_communication_type.setSelection(1);//0:wired/1:wireless
						}

					}
					
					bluetoothSettingsText.setVisibility(View.GONE);
					spinner_bluetooth_communication_type.setVisibility(View.GONE);
					spinner_bluetooth_cashdrawerSettings.setVisibility(View.GONE);
					
					nickNameEditText.setText(nickname);
					
				}

	            seekBar_threshold = (SeekBar)findViewById(R.id.seekBar_proxiprnt_device_settings_threshold);
	            textView_threshold = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_threshold);
	            seekBar_threshold.setProgress((seekBar_threshold.getMax() + seekbarMaxAdjustment) - (-1)*thresholdrssi);
	            textView_threshold.setText("-" + ((seekBar_threshold.getMax() + seekbarMaxAdjustment)- seekBar_threshold.getProgress()));
	            textView_rssi = (TextView)findViewById(R.id.textView_proxiprnt_device_settings_raw_rssi);
	            textView_rssi.setText("-" + ((seekBar_threshold.getMax() + seekbarMaxAdjustment) - seekBar_threshold.getProgress()));
	            
			} catch (StarIOPortException e) {
				e.printStackTrace();
			}
        }
 
        seekBar_threshold.setOnSeekBarChangeListener(
                new OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    	textView_threshold.setText("-" + ((seekBar.getMax() + seekbarMaxAdjustment) - seekBar.getProgress()));
                    	synchronized(seekBar_threshold) {
                    		if (progress > ((seekBar_threshold.getMax() + seekbarMaxAdjustment)- seekBar_threshold.getSecondaryProgress())) {
                        	} else {
                        	}
                    	}
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
 
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
	}
	
	private void setStarProxiManagerCallback() {
		m_shareManager.setCallback(new StarProxiPRNTManagerCallback() {
			public void onClosestCashDrawerFound(String arg0) {
				
			}

			public void onClosestPrinterFound(String arg0) {
			
			}

			public void onPortDiscovered(final String portName, final StarDeviceType deviceType, final String macAddress, final int rssi) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (macAddress.equals(ProxiPRNTActivity.getMacAddress())) {
							synchronized(seekBar_threshold) {
								seekBar_threshold.setSecondaryProgress((seekBar_threshold.getMax() + seekbarMaxAdjustment)- (-1 * rssi));
								
								
								textView_rssi.setText("-" + (-1 * rssi));
	                    		if (seekBar_threshold.getProgress() > ((seekBar_threshold.getMax() + seekbarMaxAdjustment)- (-1 * rssi))) {
	                    		} else {
	                    		}
							}
						}
					}
				});
			}

			public void onStateUpdated(String arg0, String macAddress) {
			}
		});
		
	}

    private String getTCPPortSettings() {
    	String portSettings = "";
    	
		Spinner spinner_tcp_port_number = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_portsettings);
		switch(spinner_tcp_port_number.getSelectedItemPosition())
		{
            case 0:
    	        portSettings = "";
	            break;
	        case 1:
	            portSettings = "9100";
	            break;
	        case 2:
	            portSettings = "9101";
	            break;
	        case 3:
	            portSettings = "9102";
	            break;
	        case 4:
	            portSettings = "9103";
	            break;
	        case 5:
	            portSettings = "9104";
	            break;
	        case 6:
	            portSettings = "9105";
	            break;
	        case 7:
	            portSettings = "9106";
	            break;
	        case 8:
	            portSettings = "9107";
	            break;
	        case 9:
	            portSettings = "9108";
	            break;
	        case 10:
	            portSettings = "9109";
	            break;
		}
    	
    	return portSettings;
    }
    
    private String getBluetoothCommunicationType() {
    	String portSettings = "";
    	
		Spinner spinner_bluetooth_communication_type = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_bluetoothsettings);
		switch(spinner_bluetooth_communication_type.getSelectedItemPosition())
		{
            case 0:
    	        portSettings = "";
	            break;
	        case 1:
	            portSettings = ";p";
	            break;
		}
    	
    	return portSettings;
    }
	
	private String getLANTypeSetting() {
		String lanSetting = "";

		Spinner spinner_drawer_lan_type = (Spinner) findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_lansettings);
		switch (spinner_drawer_lan_type.getSelectedItemPosition()) {
		case 0:// Wired
			lanSetting = "";
			break;
		case 1:// Wireless
			lanSetting = ";wl";
			break;
		}

		return lanSetting;
	}
	enum UseDrawerDeviveType {
		NO_USE,
		USE_PRINTER,
		USE_DKAIRCASH
	}
	
    private UseDrawerDeviveType getbluetoothUseCashDrawerDeviceType() {
    	UseDrawerDeviveType bluetoothUseCashDrawerDeviceType = ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.NO_USE;
    	
		Spinner spinner_bluetooth_use_cashdrawer_device_type = (Spinner)findViewById(R.id.spinner_proxiprnt_proxiprnt_device_settings_usecashdrawersettings);
		switch(spinner_bluetooth_use_cashdrawer_device_type.getSelectedItemPosition())
		{
            case 0:
            	bluetoothUseCashDrawerDeviceType = ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.NO_USE; //No Use
	            break;
	        case 1:
	        	bluetoothUseCashDrawerDeviceType = ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.USE_PRINTER; //Use POS Printer
	            break;
	        case 2:
	        	bluetoothUseCashDrawerDeviceType = ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.USE_DKAIRCASH; //Use DK-AirCash
	            break;	            
	            
		}
    	
    	return bluetoothUseCashDrawerDeviceType;
    }
    
    
	private boolean isUseCashDrawer = false;
	private boolean isDKAirCash = false;
	private String portSetting = "";
	
	public void applyProxiPRNTDeviceSettings(View v) {
		
		String PortName = ProxiPRNTDeviceSelectActivity.getPortName();
		String ModelName = ProxiPRNTDeviceSelectActivity.getModeName();
		ProxiPRNTDeviceSelectActivity.SelectDeviveType selectDeviceType = ProxiPRNTDeviceSelectActivity.getSelectDeviceType();
		
  	    EditText nickNameEditText = (EditText)findViewById(R.id.editText_proxiprnt_device_settings_nickname);
		
  	    String  nickName = nickNameEditText.getText().toString();
  	    ProxiPRNTActivity.setNickName(nickName);
  	    
  	    ProxiPRNTActivity.setPortName(PortName);
  	  
		if (selectDeviceType == ProxiPRNTDeviceSelectActivity.SelectDeviveType.USB_VIA_AIRPORT){
			portSetting = getTCPPortSettings();
			
			EditText portNameEditText = (EditText)findViewById(R.id.editText_proxiprnt_device_settings_portname); 
			String portName = portNameEditText.getText().toString();
			ProxiPRNTActivity.setPortName(portName);
			
		}else if(PortName.startsWith("BT:")){
			portSetting = getBluetoothCommunicationType();
			portSetting += getLANTypeSetting();
		}else{
			portSetting = ""; //USB or TCP
			portSetting += getLANTypeSetting();
			
			EditText portNameEditText = (EditText)findViewById(R.id.editText_proxiprnt_device_settings_portname); 
			String portName = portNameEditText.getText().toString();
			ProxiPRNTActivity.setPortName(portName);
		}
		ToggleButton useCashDrawer = (ToggleButton) findViewById(R.id.toggleButton_proxiprnt_device_settings_usecashdrawer);

		switch(selectDeviceType){
		    case BT_TCP:
		    	if(PortName.startsWith("BT:")){	    		
		    		if(getbluetoothUseCashDrawerDeviceType().equals(ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.NO_USE)){
		    			isUseCashDrawer = false;
		    			isDKAirCash = false;
		    		}
		    		else if(getbluetoothUseCashDrawerDeviceType().equals(ProxiPRNTDeviceSettingsActivity.UseDrawerDeviveType.USE_PRINTER)){
		    			isUseCashDrawer = true;
		    			isDKAirCash = false;
		    		}else{ //USE_DKAIRCASH

		    			isUseCashDrawer = true;
		    			isDKAirCash = true;
		    		}
		    		
		    	}
		    	else{//TCP
		    		if(ModelName.contains("SAC10")){
		    			isUseCashDrawer = true;
		    			isDKAirCash = true;
		    		}
		    		else{//Not DK-AirCash
		    			isUseCashDrawer = useCashDrawer.isChecked();
		    			isDKAirCash = false;
		    		}	    		
		    	}		    	
		    	break;

		    case USB_VIA_AIRPORT:
		    	isUseCashDrawer = useCashDrawer.isChecked();
		    	isDKAirCash = false;
			  break;
			
		    default:
		    	isUseCashDrawer = false;
		    	isDKAirCash = false;
		    	break;
		    	
		}
		
		try {
			m_shareManager.stopScan();

			HashMap<String, Object> apply_setting = m_shareManager.getSettings(ProxiPRNTActivity.getPortName());
			if (apply_setting == null) {
				HashMap<String, HashMap<String, Object>> allSettings = m_shareManager.getAllSettings();
				
				if (allSettings.size() != 0) {
					Set<String> keys = allSettings.keySet();
					int times = 0;
					
					for (final String key : keys) {
						times++;
						HashMap<String, Object> setting = allSettings.get(key);
						
						if (setting.get(StarProxiPRNTManager.KEY_STAR_MACADDRESS).equals(ProxiPRNTActivity.getMacAddress()) == true) {
					        new AlertDialog.Builder(this)
							.setTitle("Confirmation")
							.setMessage("This MAC address is already set by another port name.\n If you push \"OK\", then setting of previous MAC Address will delete.")
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								    try {
										m_shareManager.removeSettingsWithPortName(key);
										if(!isDKAirCash){
											m_shareManager.addSettingForPrinterPortName(ProxiPRNTActivity.getPortName(), portSetting, isUseCashDrawer, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());											
										}
										else {
											m_shareManager.addSettingForDKAirCashPortName(ProxiPRNTActivity.getPortName(), portSetting, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());
										}									

									} catch (NumberFormatException e) {
										e.printStackTrace();
									} catch (StarIOPortException e) {
										e.printStackTrace();
									}

									ProxiPRNTActivity.setDoneFlag(true);

									finish();
								}
							})
							.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							})
							.show();
					        break;
						} else if (times == allSettings.size()) {
							if(!isDKAirCash){
								m_shareManager.addSettingForPrinterPortName(ProxiPRNTActivity.getPortName(), portSetting, isUseCashDrawer, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());								
							
							}else{
								m_shareManager.addSettingForDKAirCashPortName(ProxiPRNTActivity.getPortName(), portSetting, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());							
							}

							ProxiPRNTActivity.setDoneFlag(true);

							finish();
						} else {
                           //
						}
					}
				} else {

					if(!isDKAirCash){
						m_shareManager.addSettingForPrinterPortName(ProxiPRNTActivity.getPortName(), portSetting, isUseCashDrawer, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());						
					
					} else{
						m_shareManager.addSettingForDKAirCashPortName(ProxiPRNTActivity.getPortName(), portSetting, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());
						
					}

					ProxiPRNTActivity.setDoneFlag(true);

					finish();
				}
			} else {
				if (apply_setting.get(StarProxiPRNTManager.KEY_STAR_MACADDRESS).equals(ProxiPRNTActivity.getMacAddress()) == true) {
					m_shareManager.removeSettingsWithPortName(ProxiPRNTActivity.getPortName());
					
					if(!isDKAirCash){
					    m_shareManager.addSettingForPrinterPortName(ProxiPRNTActivity.getPortName(), portSetting, isUseCashDrawer, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());						
					
					} else{
						m_shareManager.addSettingForDKAirCashPortName(ProxiPRNTActivity.getPortName(), portSetting, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());
						
					}
					
					ProxiPRNTActivity.setDoneFlag(true);

					finish();
				} else {
			        new AlertDialog.Builder(this)
					.setTitle("Confirmation")
					.setMessage("This port name is already set by another MAC address.\n If you push \"OK\", then override new settings.")
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						    try {
								m_shareManager.removeSettingsWithPortName(ProxiPRNTActivity.getPortName());

								if(!isDKAirCash){
									m_shareManager.addSettingForPrinterPortName(ProxiPRNTActivity.getPortName(), portSetting, isUseCashDrawer, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());	
								
								} else{
									m_shareManager.addSettingForDKAirCashPortName(ProxiPRNTActivity.getPortName(), portSetting, ProxiPRNTActivity.getMacAddress(), Integer.valueOf((String) textView_threshold.getText()).intValue(), ProxiPRNTActivity.getNickName());	
								}
								
								
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (StarIOPortException e) {
								e.printStackTrace();
							}

							ProxiPRNTActivity.setDoneFlag(true);

							finish();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					})
					.show();
				}
			}
		} catch (NumberFormatException e) {
			showErrorDialog(e.getMessage(), null);
			e.printStackTrace();
			
			try {
				m_shareManager.startScan(samplingNumber);
			} catch (StarIOPortException ex) {
				ex.printStackTrace();
			}
		} catch (StarIOPortException e) {
			showErrorDialog(e.getMessage(), null);
			e.printStackTrace();
			
			try {
				m_shareManager.startScan(samplingNumber);
			} catch (StarIOPortException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void showErrorDialog(String message, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(this)
		.setTitle("Error")
		.setMessage(message)
		.setCancelable(false)
		.setNegativeButton("OK", onClickListener)
		.show();
	}
}
