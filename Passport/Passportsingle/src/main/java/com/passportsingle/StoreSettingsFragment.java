package com.passportsingle;


import com.passportsingle.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class StoreSettingsFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Store Settings Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.storesettings, container, false);
		
        final EditText mName = (EditText) view.findViewById(R.id.storename);
        final EditText mAddress = (EditText) view.findViewById(R.id.editText1);
        final EditText mPhone = (EditText) view.findViewById(R.id.editText2);
        final EditText mEmail = (EditText) view.findViewById(R.id.editText3);
        final EditText mWebsite = (EditText) view.findViewById(R.id.editText4);
        final EditText mCurrency = (EditText) view.findViewById(R.id.editText5);

        final CheckBox mClearSale = (CheckBox) view.findViewById(R.id.clearSale);
        
        Button mSave = (Button) view.findViewById(R.id.button1);

        if(StoreSetting.getName() != null){
        	mName.setText(StoreSetting.getName());
        }
        if(StoreSetting.getAddress() != null){
        	mAddress.setText(StoreSetting.getAddress());
        }
        if(StoreSetting.getPhone() != null){
        	mPhone.setText(StoreSetting.getPhone());
        }
        if(StoreSetting.getEmail() != null){
        	mEmail.setText(StoreSetting.getEmail());
        }
        if(StoreSetting.getWebsite() != null){
        	mWebsite.setText(StoreSetting.getWebsite());
        }
        if(StoreSetting.getCurrency() != null){
        	mCurrency.setText(StoreSetting.getCurrency());
        }
        
        if(StoreSetting.clearSale){
        	mClearSale.setChecked(true);
        }
                
        mSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	StoreSetting.setName(mName.getText().toString());
		    	StoreSetting.setAddress(mAddress.getText().toString());
		    	StoreSetting.setPhone(mPhone.getText().toString());
		    	StoreSetting.setEmail(mEmail.getText().toString());
		    	StoreSetting.setWebsite(mWebsite.getText().toString());
		    	StoreSetting.setCurrency(mCurrency.getText().toString());
		    	StoreSetting.clearSale = mClearSale.isChecked();
		    	ProductDatabase.insertStoreSettings();
	    		alertbox("Store Settings", "Settings Saved");
		    }
		});
		
		return view;
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
