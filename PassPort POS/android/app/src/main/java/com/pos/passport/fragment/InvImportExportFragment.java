package com.pos.passport.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import java.io.File;
import java.io.FilenameFilter;

public class InvImportExportFragment extends Fragment implements Runnable {

	protected String filename;
	protected AlertDialog alertDialog;
	protected ProgressDialog pd;
	private ProductDatabase mDb;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.importexportview, container, false);
		mDb = ProductDatabase.getInstance(getActivity());
        Button mSave = (Button) view.findViewById(R.id.saveInvFile);
        Button mLoad = (Button) view.findViewById(R.id.loadInvFile);

        mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PrefUtils.isLicensed(getActivity())) {
					SaveInventory();
				} else {
					Utils.alertBox(getActivity(), R.string.txt_unlicensed, R.string.msg_get_license_to_export_inventory);
				}
			}
		});

        mLoad.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PrefUtils.isLicensed(getActivity())) {
					LoadInventory();
				} else {
					Utils.alertBox(getActivity(), R.string.txt_unlicensed, R.string.msg_get_license_to_import_inventory);
				}
			}
		});
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		return view;
	}

	protected void LoadInventory() {
    	AlertDialog.Builder builder;

		File sd = Environment.getExternalStorageDirectory();
		
	   	final File loadFile = new File(sd, "/AdvantagePOS/Inventory");
	   	loadFile.mkdirs();

    	builder = new AlertDialog.Builder(getActivity());
    	final FilenameFilter filter =  new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(".csv");
            }
        };

    	builder.setSingleChoiceItems(loadFile.list(filter), -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                filename = loadFile.list(filter)[item];
                Toast.makeText(getActivity(), getString(R.string.txt_selected) + " " + loadFile.list(filter)[item], Toast.LENGTH_SHORT).show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        })
    	.setTitle(R.string.txt_select_file)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_preparing_import_file), true, false);
                Thread thread = new Thread(InvImportExportFragment.this);
                thread.start();
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
            .setTitle(R.string.txt_export_inventory)
            .setPositiveButton(R.string.txt_export, new DialogInterface.OnClickListener() {
                private String name;

                public void onClick(DialogInterface dialog, int id) {

                    if (!TextUtils.isEmpty(nameEdit.getText().toString().trim())){
                        name = nameEdit.getText().toString();
                        mDb.exportinv(name);
                    } else {
                        Utils.alertBox(getActivity(), R.string.txt_insert_item_error, R.string.txt_insert_item_name);
                    }

                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

    	alertDialog = builder.create();
    	alertDialog.show();
    	alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		
    	nameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) { }

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!nameEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
    	if (filename != null){
    		if (mDb.importinv(filename, handler)){
				Message m2 = new Message();
				m2.what = 9;
				handler.sendMessage(m2);
    		} else {
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
    		if (msg.what == 10){
    			pd.dismiss();
    		} else if (msg.what == 11) {
    			pd.setMessage((String) msg.obj);;
    		} else if (msg.what == 9){
    			Utils.alertBox(getActivity(), R.string.txt_import_success, R.string.msg_inventory_imported_successfully);
    		} else if(msg.what == 8){
    			Utils.alertBox(getActivity(), R.string.txt_import_failed, R.string.msg_inventory_import_failed);
    		}
    	}
    };
}
