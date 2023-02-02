package com.passportsingle;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class EmailSettingsFragment extends Fragment implements Runnable {

	private CheckBox smtpUseEmail;
	private CheckBox useBookkeeper;
	private EditText smtpServer;
	private EditText smtpPort;
	private EditText smtpUsername;
	private EditText smtpPassword;
	private EditText smtpEmail;
	private EditText smtpSubject;
	private Button smtpSave;
	private Button smtpTest;
	protected ProgressDialog pd;
	private EditText emailFooter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Email Settings Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.emailsettings, container, false);

		smtpUseEmail = (CheckBox) view.findViewById(R.id.useEmail);

		smtpServer = (EditText) view.findViewById(R.id.smtp_server);
		smtpPort = (EditText) view.findViewById(R.id.smtp_port);
		smtpUsername = (EditText) view.findViewById(R.id.smtp_username);
		smtpPassword = (EditText) view.findViewById(R.id.smtp_password);
		smtpEmail = (EditText) view.findViewById(R.id.smtp_email);
		smtpSubject = (EditText) view.findViewById(R.id.smtp_subject);
		emailFooter = (EditText) view.findViewById(R.id.emailfooter);
		useBookkeeper = (CheckBox) view.findViewById(R.id.bookkeeper);
		
		smtpSave = (Button) view.findViewById(R.id.smtp_save);
		smtpTest = (Button) view.findViewById(R.id.smtp_test);

		smtpUseEmail.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (smtpUseEmail.isChecked()) {
					enableFields();
					smtpServer.requestFocus();
				} else {
					disableFields();
				}
			}
		});
		
		smtpSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	if (smtpUseEmail.isChecked()) {
			    	if(smtpServer.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need SMTP Server");
			    		return;
			    	}
			    	
			    	if(smtpPort.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need SMTP Port");
			    		return;
			    	}
			    	
			    	if(smtpUsername.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need Username");
			    		return;
			    	}
	
			    	if(smtpPassword.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need Password");
			    		return;
			    	}
			    	
			    	if(smtpEmail.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need Sent From Email");
			    		return;
			    	}
			    	
			    	if(smtpSubject.getText().toString().equals("")){
			    		alertbox("Email Settings", "Need Email Subject");
			    		return;
			    	}
			    	
			    	EmailSetting.setEnabled(smtpUseEmail.isChecked());
			    	EmailSetting.setSmtpServer(smtpServer.getText().toString());
			    	EmailSetting.setSmtpPort(Integer.valueOf(smtpPort.getText().toString()));
			    	EmailSetting.setSmtpUsername(smtpUsername.getText().toString());
			    	EmailSetting.setSmtpPasword(smtpPassword.getText().toString());
			    	EmailSetting.setSmtpEmail(smtpEmail.getText().toString());
			    	EmailSetting.setSmtpSubject(smtpSubject.getText().toString());
			    	EmailSetting.blurb = emailFooter.getText().toString();
			    	EmailSetting.bookkeeper = useBookkeeper.isChecked();
			    	smtpTest.setEnabled(true);
			    	
		    	}else{
		    		
			    	EmailSetting.setEnabled(false);
			    	EmailSetting.setSmtpServer("");
			    	EmailSetting.setSmtpPort(-1);
			    	EmailSetting.setSmtpUsername("");
			    	EmailSetting.setSmtpPasword("");
			    	EmailSetting.setSmtpEmail("");
			    	EmailSetting.setSmtpSubject("");
			    	EmailSetting.bookkeeper = false;
			    	EmailSetting.blurb = "";
			    }
		    	    	
		    	ProductDatabase.insertEmailSettings();
		    	
	    		alertbox("Email Settings", "Settings Saved");
		    }
		});
				
		smtpTest.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
				if(EmailSetting.isEnabled()){
		        	pd = ProgressDialog.show(getActivity(), "", "Sending Test Email...", true, false);
		        	Thread thread = new Thread(EmailSettingsFragment.this);
		        	thread.start();
				}else{

				}
		    }
		});
				
		if(EmailSetting.isEnabled()){
			smtpUseEmail.setChecked(true);
			enableFields();
		}

		return view;
	}

	protected void disableFields() {
		smtpServer.setText("");
		smtpPort.setText("");
		smtpUsername.setText("");
		smtpPassword.setText("");
		smtpEmail.setText("");
		smtpSubject.setText("");
		emailFooter.setText("");
		useBookkeeper.setChecked(false);
		smtpServer.setEnabled(false);
		smtpPort.setEnabled(false);
		smtpUsername.setEnabled(false);
		smtpPassword.setEnabled(false);
		smtpEmail.setEnabled(false);
		smtpSubject.setEnabled(false);
		smtpTest.setEnabled(false);
		emailFooter.setEnabled(false);
		useBookkeeper.setEnabled(false);

	}

	protected void enableFields() {	
		smtpServer.setEnabled(true);
		smtpPort.setEnabled(true);
		smtpUsername.setEnabled(true);
		smtpPassword.setEnabled(true);
		smtpEmail.setEnabled(true);
		smtpSubject.setEnabled(true);
		emailFooter.setEnabled(true);
		useBookkeeper.setEnabled(true);

		smtpServer.setText(EmailSetting.getSmtpServer());
		if(EmailSetting.getSmtpPort() == -1)
			smtpPort.setText("");
		else
			smtpPort.setText(""+EmailSetting.getSmtpPort());
		smtpUsername.setText(EmailSetting.getSmtpUsername());
		smtpPassword.setText(EmailSetting.getSmtpPasword());
		smtpEmail.setText(EmailSetting.getSmtpEmail());
		smtpSubject.setText(EmailSetting.getSmtpSubject());
		emailFooter.setText(EmailSetting.blurb);

		if(EmailSetting.bookkeeper)
			useBookkeeper.setChecked(true);
		
    	if(smtpServer.getText().toString().equals("")){
    		return;
    	}
    	
    	if(smtpPort.getText().toString().equals("")){
    		return;
    	}
    	
    	if(smtpUsername.getText().toString().equals("")){
    		return;
    	}

    	if(smtpPassword.getText().toString().equals("")){
    		return;
    	}
    	
    	if(smtpEmail.getText().toString().equals("")){
    		return;
    	}
    	
    	if(smtpSubject.getText().toString().equals("")){
    		return;
    	}
    	
		smtpTest.setEnabled(true);
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}
	
	private void issueEmailReceipt() {
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (EmailSetting.isEnabled()) {	          
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
				String email = EmailSetting.getSmtpEmail();
				if (email.contains("@")) {
					Mail m = new Mail(EmailSetting.getSmtpUsername(), EmailSetting.getSmtpPasword());
					m.setServer(EmailSetting.getSmtpServer(), EmailSetting.getSmtpPort());
					m.setSubject(EmailSetting.getSmtpSubject());
					m.setBody("Test Email from " + email);
					m.setTo(email);
					m.setFrom(email);
					try {

						if (m.send()) {
							Message m2 = new Message();
							m2.what = 9;
							handler.sendMessage(m2);
						} else {		
							Message m2 = new Message();
							m2.what = 8;
							handler.sendMessage(m2);
						}
					} catch (Exception e) {
						Log.e("MailApp", "Could not send email", e);
						Message m2 = new Message();
						m2.what = 8;
						handler.sendMessage(m2);
					}
				}
			} else {
				Message m2 = new Message();
				m2.what = 8;
				handler.sendMessage(m2);
			}
		}
	}
	
	@Override
	public void run() {
		Log.v("Thread", "Starting Thread");
		issueEmailReceipt();
		Message m = new Message();
		m.what = 10;
		handler.sendMessage(m);
	}
	
	 
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		if(msg.what == 10){
    			pd.dismiss();
    		}
    		else if(msg.what == 9){
    			Toast.makeText(getActivity(),
    					"Email Test was sent successfully.",
    					Toast.LENGTH_LONG).show();
    		}
    		else if(msg.what == 8){
				Toast.makeText(getActivity(),
						"Email Test was not sent.",
						Toast.LENGTH_LONG).show();
    		}
    	}
    };

}
