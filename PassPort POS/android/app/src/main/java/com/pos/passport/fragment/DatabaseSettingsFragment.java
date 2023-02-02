package com.pos.passport.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
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

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.CartInterface;
import com.pos.passport.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;

public class DatabaseSettingsFragment extends Fragment {

	protected String filename;
	protected AlertDialog alertDialog;
	private ProductDatabase mDb;
	private CartInterface mCallback;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CartInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CartInterface");
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_database_settings, container, false);
        mDb = ProductDatabase.getInstance(getActivity());

        Button mSave = (Button) view.findViewById(R.id.savedb);
        Button mRestore = (Button) view.findViewById(R.id.restoredb);
        Button mReset = (Button) view.findViewById(R.id.resetdb);
		
        mSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveDatabase();
			}
		});

        mRestore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				importdatabase();
			}
		});
        
        mReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean success = mDb.delete();
				if (success) {
					Utils.alertBox(getActivity(), R.string.txt_database_settings, R.string.msg_database_deleted);
                    mCallback.onReset();
					//MainActivity.resetShop(true);
				} else {
					Utils.alertBox(getActivity(), R.string.txt_database_settings, R.string.msg_database_not_deleted);
				}

			}
		});
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		return view;
	}
	
	protected void SaveDatabase() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;
    	
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	final View mylayout = inflater.inflate(R.layout.export, (ViewGroup) getActivity().findViewById(R.id.exportmain));
    	
    	final EditText nameEdit = (EditText) mylayout.findViewById(R.id.editText1);
    	final TextView text = (TextView) mylayout.findViewById(R.id.textView1);
    	
    	text.setText(R.string.msg_enter_db_filename);

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
            .setTitle(R.string.txt_export_database)
            .setPositiveButton(R.string.txt_export, new DialogInterface.OnClickListener() {
                private String name;

                public void onClick(DialogInterface dialog, int id) {

                    if (!nameEdit.getText().toString().equals("")) {
                        name = nameEdit.getText().toString();
                        try {
                            File sd = Environment.getExternalStorageDirectory();

                            File loadFile = new File(sd, "/AdvantagePOS/Database");
                            loadFile.mkdirs();

                            if (sd.canWrite()) {
                                String currentDBPath = mDb.getDb().getPath();

                                File currentDB = new File(currentDBPath);
                                File backupDB = new File(sd, "/AdvantagePOS/Database/" + name + ".db");

                                if (currentDB.exists()) {
                                    mDb.close();

                                    FileChannel src = new FileInputStream(currentDB).getChannel();
                                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                                    dst.transferFrom(src, 0, src.size());
                                    src.close();
                                    dst.close();

                                    mCallback.onReset();
                                    //MainActivity.resetShop(true);
                                }

                                Utils.alertBox(getActivity(), R.string.txt_database_settings, R.string.msg_database_saved);
                            }
                        } catch (Exception e) {
                            Utils.alertBox(getActivity(), R.string.txt_error, R.string.msg_failed_saving_database);
                        }

                    } else {
                        Utils.alertBox(getActivity(), R.string.txt_error, R.string.msg_insert_database_name);
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
				filename = loadFile.list(filter)[item];
				Toast.makeText(getActivity(), getString(R.string.txt_selected) + " " + loadFile.list(filter)[item], Toast.LENGTH_SHORT).show();
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
			}
		})
    	.setTitle(R.string.msg_select_database_file)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (filename != null) {
					try {
						if (sd.canRead()) {

							String currentDBPath = mDb.getDb().getPath();

							File currentDB = new File(currentDBPath);
							File backupDB = new File(sd, "/AdvantagePOS/Database/" + filename);

							if (backupDB.exists()) {
                                mDb.close();

								FileChannel src = new FileInputStream(backupDB).getChannel();
								FileChannel dst = new FileOutputStream(currentDB).getChannel();
								dst.transferFrom(src, 0, src.size());
								src.close();
								dst.close();

                                mCallback.onReset();
								//MainActivity.resetShop(true);
							}

							Utils.alertBox(getActivity(), R.string.txt_database_settings, R.string.msg_database_restored);
						}
					} catch (Exception e) {
						Log.v("Database Error", "" + e.getMessage());
						Utils.alertBox(getActivity(), R.string.msg_database_error, R.string.msg_restore_failed);
					}
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
	}
}