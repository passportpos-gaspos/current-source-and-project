package com.passportsingle;



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
import android.widget.EditText;

public class TaxSettingsFragment extends Fragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Tax Settings Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.taxsettings, container, false);
		
        final EditText mNameTax1 = (EditText) view.findViewById(R.id.taxName1);
        final EditText mNameTax2 = (EditText) view.findViewById(R.id.taxName2);
        final EditText mNameTax3 = (EditText) view.findViewById(R.id.taxName3);

        final EditText mTax1 = (EditText) view.findViewById(R.id.tax1);
        final EditText mTax2 = (EditText) view.findViewById(R.id.tax2);
        final EditText mTax3 = (EditText) view.findViewById(R.id.tax3);

        Button mSave = (Button) view.findViewById(R.id.save);
        Button mReset = (Button) view.findViewById(R.id.rset);

        if(TaxSetting.getTax1name() != null){
			mNameTax1.setText(TaxSetting.getTax1name());
        	mTax1.setText(""+TaxSetting.getTax1());
        }
        
        if(TaxSetting.getTax2name() != null){
        	mNameTax2.setText(TaxSetting.getTax2name());
        	mTax2.setText(""+TaxSetting.getTax2());
        }
        
        if(TaxSetting.getTax3name() != null){
        	mNameTax3.setText(TaxSetting.getTax3name());
        	mTax3.setText(""+TaxSetting.getTax3());
        }
        
        mSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	TaxSetting.clear();
		    	
		    	if(!mNameTax1.getText().toString().equals("")){
		    		if(!mTax1.getText().toString().equals("")){
			    		TaxSetting.setTax1name(mNameTax1.getText().toString());
			    		TaxSetting.setTax1(Float.valueOf(mTax1.getText().toString()));
			    	}else{
			    		alertbox("Tax 1 Settings", "Insert Amount for Tax 1: " + mNameTax1.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	if(!mNameTax2.getText().toString().equals("")){
		    		if(!mTax2.getText().toString().equals("")){
			    		TaxSetting.setTax2name(mNameTax2.getText().toString());
			    		TaxSetting.setTax2(Float.valueOf(mTax2.getText().toString()));
			    	}else{
			    		alertbox("Tax 2 Settings", "Insert Amount for Tax 2: " + mNameTax2.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	if(!mNameTax3.getText().toString().equals("")){
		    		if(!mTax3.getText().toString().equals("")){
			    		TaxSetting.setTax3name(mNameTax3.getText().toString());
			    		TaxSetting.setTax3(Float.valueOf(mTax3.getText().toString()));
			    	}else{
			    		alertbox("Tax 3 Settings", "Insert Amount for Tax 3: " + mNameTax3.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	PointOfSale.getShop().insertTax();
	    		alertbox("Tax Settings", "Settings Saved");

		    }
		});
        
        mReset.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	mNameTax1.setText("");
		    	mTax1.setText("");
		    	
		    	TaxSetting.setTax1name("");
		    	TaxSetting.setTax1(0.0f);
		    	
		    	mNameTax2.setText("");
		    	mTax2.setText("");
		    	
		    	TaxSetting.setTax2name("");
		    	TaxSetting.setTax2(0.0f);
		    	
		    	mNameTax3.setText("");
		    	mTax3.setText("");
		    	
		    	TaxSetting.setTax3name("");
		    	TaxSetting.setTax3(0.0f);
		    	
		    	PointOfSale.getShop().insertTax();
	    		alertbox("Tax Settings", "Settings resetted");

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
