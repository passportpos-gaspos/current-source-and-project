package com.passportsingle;

import java.io.File;
import java.io.FilenameFilter;


import com.passportsingle.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InvImportExportFragment extends Fragment implements Runnable {

	protected String filename;
	protected AlertDialog alertDialog;
	protected ProgressDialog pd;
	private SharedPreferences mSharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Import/Export Fragment");
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.importexportview, container, false);
		
        Button mSave = (Button) view.findViewById(R.id.saveInvFile);
        Button mLoad = (Button) view.findViewById(R.id.loadInvFile);
        
		mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);

        mSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
				boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
				
				if(lic){
			    	SaveInventory();
				}else{
					alertbox("Unlicensed", "Please license this app to export an inventory CSV file. Thank you.");
				}	
		    }
		});

        mLoad.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
				boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
				
				if(lic){
			    	LoadInventory();
				}else{
					alertbox("Unlicensed", "Please license this app to import and inventory CSV file. Thank you.");
				}	
		    }
		});

		return view;
	}

	protected void LoadInventory() {
    	AlertDialog.Builder builder;

		File sd = Environment.getExternalStorageDirectory();
		
	   	final File loadFile = new File(sd, "/AdvantagePOS/Inventory");
	   	loadFile.mkdirs();
	   	        	   	
	   	// have the object build the directory structure, if needed.
    	builder = new AlertDialog.Builder(getActivity());
    	final FilenameFilter filter =  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(".csv");
            }
    	};

    	builder.setSingleChoiceItems(loadFile.list(filter), -1, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {
    	    	filename  = loadFile.list(filter)[item];
    	        Toast.makeText(getActivity(), "Selected " + loadFile.list(filter)[item], Toast.LENGTH_SHORT).show();
    	    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    	    }
    	})
    	.setTitle("Select File")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
	        	pd = ProgressDialog.show(getActivity(), "", "Preparing import file...", true, false);
		        Thread thread = new Thread(InvImportExportFragment.this);
		        thread.start();
            	
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();
    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
	}

	protected void SaveInventory() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	final View mylayout = inflater.inflate(R.layout.export, (ViewGroup) getActivity().findViewById(R.id.exportmain));
    	
    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Export Inventory")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Export", new DialogInterface.OnClickListener() {
            private String name;

			public void onClick(DialogInterface dialog, int id) {
                        	
            	if(!nameEdit.getText().toString().equals("")){
            		name = nameEdit.getText().toString();
            		ProductDatabase.exportinv(name);
            	}else{
        			alertbox("Insert Item Error", "Insert Item Name");
            	}   
            	
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();   
    	
    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
		
    	nameEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				if(!nameEdit.getText().toString().equals("")){
	            	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); 
				}else{
	            	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
				}
			}
    	});		
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void run() {
		Log.v("Thread", "Starting Thread");
    	if(filename != null){
    					
    		if(ProductDatabase.importinv(filename, handler)){
				Message m2 = new Message();
				m2.what = 9;
				handler.sendMessage(m2);
    		}
    		else{
				Message m2 = new Message();
				m2.what = 8;
				handler.sendMessage(m2);
    		}
    	}
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
    		else if(msg.what == 11){
    			pd.setMessage((String) msg.obj);;
    		}
    		else if(msg.what == 9){
    			alertbox("Import Successful", "Inventory import successful.");
    		}
    		else if(msg.what == 8){
    			//alertbox("Import Failed", (String) msg.obj);

    			alertbox("Import Failed", "Inventory import failed. Check CSV format and try again.");
    		}
    	}
    };
}
