package com.passportsingle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;

public class DatabaseSettingsFragment extends Fragment {

	protected String filename;
	protected AlertDialog alertDialog;
	private SharedPreferences mSharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Database Settings Fragment");
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.databasesettings, container, false);
		
        Button mSave = (Button) view.findViewById(R.id.savedb);
        Button mRestore = (Button) view.findViewById(R.id.restoredb);
        Button mReset = (Button) view.findViewById(R.id.resetdb);
        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        mSave.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
				
				if(lic){
					SaveDatabase();
				}else{
					alertbox("Unlicensed", "Please license this app to save a database file. Thank you.");
				}
		    }
		});

        mRestore.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
				
				if(lic){
					importdatabase();
				}else{
					alertbox("Unlicensed", "Please license this app to restore a database file. Thank you.");
				}
		    }
		});
        
        mReset.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		    	boolean success = false;
		    	
		        success = ProductDatabase.delete();

		        if(success){
		        	alertbox("Database Settings", "Database Deleted. Restart application to reinitalize application settings.");
		        	PointOfSale.resetShop();
		        }else{
		    		alertbox("Database Settings", "Database not deleted.");
		        }

		    }
		});
        
		return view;
	}
	
	protected void SaveDatabase() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	final View mylayout = inflater.inflate(R.layout.export, (ViewGroup) getActivity().findViewById(R.id.exportmain));
    	
    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
    	final TextView text = (TextView) mylayout.findViewById(R.id.textView1);
    	
    	text.setText("Enter the file name here. It will end in .db and be place in AdvantagePOS/Database directory.");

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Export Database")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Export", new DialogInterface.OnClickListener() {
            private String name;

			public void onClick(DialogInterface dialog, int id) {
                        	
            	if(!nameEdit.getText().toString().equals("")){
            		name = nameEdit.getText().toString();
    		        try {
    		            File sd = Environment.getExternalStorageDirectory();
    		            
    		    	   	File loadFile = new File(sd, "/AdvantagePOS/Database");
    		    	   	loadFile.mkdirs();
    		    	   	
    		            if (sd.canWrite()) 
    		            {    		            
    		                String currentDBPath = ProductDatabase.getDb().getPath();
    		                
    		                File currentDB = new File(currentDBPath);
    		                File backupDB = new File(sd, "/AdvantagePOS/Database/"+name+".db");
    		                
    		                if (currentDB.exists()) {
    		                	ProductDatabase.close();

    		                    FileChannel src = new FileInputStream(currentDB).getChannel();
    		                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
    		                    dst.transferFrom(src, 0, src.size());
    		                    src.close();
    		                    dst.close();
    		                    
    		                    PointOfSale.resetShop();
    		                }
    		                
    	    	    		alertbox("Database Settings", "Database Saved");
    		            }
    		        } catch (Exception e) {
            			alertbox("Error", "Failed Saving Database.");
    		        }

            	}else{
        			alertbox("Error", "Insert Database Name");
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
	
	private void importdatabase() {
    	AlertDialog.Builder builder;
    	
		final File sd = Environment.getExternalStorageDirectory();
        
	   	final File loadFile = new File(sd, "/AdvantagePOS/Database");
	   	loadFile.mkdirs();
	   		   	
	   	// have the object build the directory structure, if needed.
    	builder = new AlertDialog.Builder(getActivity());
    	final FilenameFilter filter =  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(".db");
            }
    	};

    	builder.setSingleChoiceItems(loadFile.list(filter), -1, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	    	filename  = loadFile.list(filter)[item];
    	        Toast.makeText(getActivity(), "Selected " + loadFile.list(filter)[item], Toast.LENGTH_SHORT).show();
    	    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    	    }
    	})
    	.setTitle("Select Database File")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	if(filename != null){
    		        try {
    		            if (sd.canRead()) {
    		            	
    		                String currentDBPath = ProductDatabase.getDb().getPath();
    		                
    		                File currentDB = new File(currentDBPath);
    		                File backupDB = new File(sd, "/AdvantagePOS/Database/"+filename);

    		                if (backupDB.exists()) {
    		                	ProductDatabase.close();
    		                	
    		                    FileChannel src = new FileInputStream(backupDB).getChannel();
    		                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
    		                    dst.transferFrom(src, 0, src.size());
    		                    src.close();
    		                    dst.close();
    		                    
    		                    PointOfSale.resetShop();
    		                }
    		                
    	    	    		alertbox("Database Settings", "Database Restored");
    		            }
    		        } catch (Exception e) {
    		        	Log.v("Database Error", ""+e.getMessage());
	    	    		alertbox("Database Error", "Restore Failed");
    		        }                 
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