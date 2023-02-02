package com.pos.passport.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.pos.passport.R;
import com.pos.passport.adapter.TaxViewAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.TaxSetting;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class TaxFragment extends Fragment {

    TaxViewAdapter mAdapter;
    List<TaxSetting> taxdata = new ArrayList<>();
    ProductDatabase mDb;
    GridView taxdatagrid;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tax, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        taxdatagrid = (GridView) view.findViewById(R.id.taxdatagrid);
       /* final EditText name1Tax = (EditText) view.findViewById(R.id.tax_1_name_edit_text);
        final EditText name2Tax = (EditText) view.findViewById(R.id.tax_2_name_edit_text);
        final EditText name3Tax = (EditText) view.findViewById(R.id.tax_3_name_edit_text);

        final EditText mTax1 = (EditText) view.findViewById(R.id.tax_1_edit_text);
        final EditText mTax2 = (EditText) view.findViewById(R.id.tax_2_edit_text);
        final EditText mTax3 = (EditText) view.findViewById(R.id.tax_3_edit_text);*/

        //Button mSave = (Button) view.findViewById(R.id.save);
        //Button mReset = (Button) view.findViewById(R.id.rset);

       /* if (TaxSetting.getTax1Name() != null){
            name1Tax.setText(TaxSetting.getTax1Name());
        	mTax1.setText(""+TaxSetting.getTax1());
        }
        
        if (TaxSetting.getTax2Name() != null){
            name2Tax.setText(TaxSetting.getTax2Name());
        	mTax2.setText(""+TaxSetting.getTax2());
        }
        
        if (TaxSetting.getTax3Name() != null){
            name3Tax.setText(TaxSetting.getTax3Name());
        	mTax3.setText(""+TaxSetting.getTax3());
        }*/
        mDb = ProductDatabase.getInstance(getActivity());
        taxdata = mDb.findTaxData();
        mAdapter = new TaxViewAdapter(getActivity(), taxdata);
        taxdatagrid.setAdapter(mAdapter);
        /*mSave.setOnClickListener(new OnClickListener() {
            @Override
		    public void onClick(View v) {
		    	
		    	TaxSetting.clear();
		    	
		    	if(!mNameTax1.getText().toString().equals("")){
		    		if(!mTax1.getText().toString().equals("")){
			    		TaxSetting.setTax1name(mNameTax1.getText().toString());
			    		TaxSetting.setTax1(Float.valueOf(mTax1.getText().toString()));
			    	}else{
			    		alertBox("Tax 1 Settings", "Insert Amount for Tax 1: " + mNameTax1.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	if(!mNameTax2.getText().toString().equals("")){
		    		if(!mTax2.getText().toString().equals("")){
			    		TaxSetting.setTax2name(mNameTax2.getText().toString());
			    		TaxSetting.setTax2(Float.valueOf(mTax2.getText().toString()));
			    	}else{
			    		alertBox("Tax 2 Settings", "Insert Amount for Tax 2: " + mNameTax2.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	if(!mNameTax3.getText().toString().equals("")){
		    		if(!mTax3.getText().toString().equals("")){
			    		TaxSetting.setTax3name(mNameTax3.getText().toString());
			    		TaxSetting.setTax3(Float.valueOf(mTax3.getText().toString()));
			    	}else{
			    		alertBox("Tax 3 Settings", "Insert Amount for Tax 3: " + mNameTax3.getText().toString());
			    		return;
			    	}
		    	}
		    	
		    	PointOfSale.getShop().insertTax();
	    		alertBox("Tax Settings", "Settings Saved");

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
	    		alertBox("Tax Settings", "Settings resetted");

		    }
		});*/

        return view;
    }
}
