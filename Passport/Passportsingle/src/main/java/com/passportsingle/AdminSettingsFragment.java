package com.passportsingle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import java.util.ArrayList;

public class AdminSettingsFragment extends Fragment {

	private CheckBox useAdminPassword;
	private EditText adminPassword;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Admin Password Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.adminsettings, container, false);
		
		useAdminPassword = (CheckBox) view.findViewById(R.id.useAdminPassword);
		
		adminPassword = (EditText) view.findViewById(R.id.adminPassword);

        Button mSave = (Button) view.findViewById(R.id.adminSave);

        useAdminPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (useAdminPassword.isChecked()) {
					enableFields();
					adminPassword.requestFocus();
				} else {
					disableFields(); 
				}
			}
		});
                        
        mSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	if (useAdminPassword.isChecked()) {
			    	AdminSetting.password = adminPassword.getText().toString();
			    	AdminSetting.enabled = true;

			    	PointOfSale.getShop().insertAdminSettings();
		    		alertbox("Admin Account", "Pin Saved. You can now access the admin account from the login screen.");
		    	}else{
		    		ArrayList<Cashier> cashiers = ProductDatabase.getCashiers();
		    		if(cashiers.size() > 0)
		    		{
			    		alertbox("Cashiers Present", "Delete cashiers before removing Admin Account.");
		    		}else{
				    	AdminSetting.password = "";
				    	AdminSetting.hint = "";
				    	AdminSetting.enabled = false;
	
				    	PointOfSale.getShop().insertAdminSettings();
			    		alertbox("Admin Account", "Admin Account has been disabled.");
		    		}
		    	}
		    }
		});
        
		if(AdminSetting.isEnabled()){
			useAdminPassword.setChecked(true);
			enableFields();
		}else{
			disableFields();
		}
		
		return view;
	}
	
	protected void enableFields() {		
		adminPassword.setEnabled(true);
		adminPassword.setText(AdminSetting.password);
	}
	
	protected void disableFields() {
		adminPassword.setEnabled(false);
	}
	
	protected void alertbox(String title, String mymessage) 
    { 
    new AlertDialog.Builder(getActivity()) 
       .setMessage(mymessage) 
       .setInverseBackgroundForced(true)
       .setTitle(title) 
       .setCancelable(true) 
       .setNeutralButton(android.R.string.ok, 
          new DialogInterface.OnClickListener() { 
          public void onClick(DialogInterface dialog, int whichButton){} 
          }) 
       .show(); 
    }
}
