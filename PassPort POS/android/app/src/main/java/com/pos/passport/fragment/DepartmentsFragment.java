package com.pos.passport.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.adapter.DepartmentAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Category;
import com.pos.passport.model.TaxSetting;
import com.pos.passport.util.Utils;

public class DepartmentsFragment extends Fragment {

	private GridView mDepartmentListView;
	private ProductDatabase mDb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Departments Fragment");
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_item_list, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		mDepartmentListView = (GridView) view.findViewById(R.id.item_list_view);
		DepartmentAdapter itemAdapter = new DepartmentAdapter(getActivity(), mDb.helper.fetchNamedCat(""));
		mDepartmentListView.setAdapter(itemAdapter);
		
		//registerForContextMenu(departmentList);
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.department_spinner);
		departmentFilter.setVisibility(View.GONE);
		return view;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    menu.clear();

        inflater.inflate(R.menu.dep_menu_tab, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setOnQueryTextListener(
                    new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            Log.v("Products", "" + newText);

                            if (mDepartmentListView != null)
                                ((DepartmentAdapter) mDepartmentListView.getAdapter()).changeCursor(mDb.helper.fetchNamedCat(newText));
                            return true;
                        }

                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }
                    });
            searchView.setIconifiedByDefault(false);
        }
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

	private void addDepToDatabase() {
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;

    	Context mContext = getActivity().getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.add_catagory, (ViewGroup) getActivity().findViewById(R.id.mainLayout));

    	final EditText mNameEdit = (EditText) layout.findViewById(R.id.editText1);
    	final CheckBox nTaxBox1 = (CheckBox) layout.findViewById(R.id.taxBox1);
    	final CheckBox nTaxBox2 = (CheckBox) layout.findViewById(R.id.taxBox2);
    	final CheckBox nTaxBox3 = (CheckBox) layout.findViewById(R.id.taxBox3);

    	final TextView nTaxText = (TextView) layout.findViewById(R.id.textView1);
    	
    	if (TaxSetting.getTax1Name() != null){
        	if (!TaxSetting.getTax1Name().equals("")){
        		if (nTaxBox1.getVisibility() == View.GONE){
            		nTaxBox1.setVisibility(View.VISIBLE);
        		}
        		nTaxBox1.setText(TaxSetting.getTax1Name());
        	} else {
        		if (nTaxBox1.getVisibility() == View.VISIBLE){
        			nTaxBox1.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if(nTaxBox1.getVisibility() == View.VISIBLE){
    			nTaxBox1.setVisibility(View.GONE);
    		}
    	}
    	
    	if (TaxSetting.getTax2Name() != null) {
        	if (!TaxSetting.getTax2Name().equals("")) {
        		if (nTaxBox2.getVisibility() == View.GONE) {
            		nTaxBox2.setVisibility(View.VISIBLE);
        		}
        		nTaxBox2.setText(TaxSetting.getTax2Name());
        	} else {
        		if (nTaxBox2.getVisibility() == View.VISIBLE) {
        			nTaxBox2.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if (nTaxBox2.getVisibility() == View.VISIBLE) {
    			nTaxBox2.setVisibility(View.GONE);
    		}
    	}
    	
    	if (TaxSetting.getTax3Name() != null) {
        	if (!TaxSetting.getTax3Name().equals("")) {
        		if (nTaxBox3.getVisibility() == View.GONE) {
            		nTaxBox3.setVisibility(View.VISIBLE);
        		}
        		nTaxBox3.setText(TaxSetting.getTax3Name());
        	} else {
        		if (nTaxBox3.getVisibility() == View.VISIBLE) {
        			nTaxBox3.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if (nTaxBox3.getVisibility() == View.VISIBLE) {
    			nTaxBox3.setVisibility(View.GONE);
    		}
    	}
    	
    	if (nTaxBox1.getVisibility() == View.GONE && nTaxBox2.getVisibility() == View.GONE && nTaxBox3.getVisibility() == View.GONE){
    		nTaxText.setVisibility(View.GONE);
    	} else {
    		nTaxText.setVisibility(View.VISIBLE);
    	}
    	
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(layout)
            .setTitle(R.string.txt_new_department)
            .setPositiveButton(R.string.txt_add_department, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    String name = mNameEdit.getText().toString();

                    name = name.replaceAll(", ", " ");
                    name = name.replaceAll(",", " ");
                    name = name.replaceAll("\"", "");
                    Category newprod = new Category();
                    newprod.setName(name);
                    newprod.setTaxable1(nTaxBox1.isChecked());
                    newprod.setTaxable2(nTaxBox2.isChecked());
                    newprod.setTaxable3(nTaxBox3.isChecked());

                    mDb.insertCat(newprod);
                    ((DepartmentAdapter) mDepartmentListView.getAdapter()).getCursor().requery();

                    dialog.cancel();

                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

    	alertDialog = builder.create();
    	alertDialog.show();
    	
    	if (mNameEdit.getText().toString().equals("")) {
    		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    	}
    	
    	mNameEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mNameEdit.getText().toString().equals("")){
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
    	});
    }
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
            menu.setHeaderTitle(R.string.txt_department_options);
            menu.add(0, v.getId(), 0, getString(R.string.txt_edit_department));
            menu.add(0, v.getId(), 0, getString(R.string.txt_remove_department));
        }
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.txt_edit_department))) { function1(item); }
        else if (item.getTitle().equals(getString(R.string.txt_remove_department))) { function2(item); }
        else { return false; }

        return true;
    }

    public void function1(MenuItem item){
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;

    	Context mContext = getActivity().getApplicationContext();
    	getActivity();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.add_catagory,
    	                               (ViewGroup) getActivity().findViewById(R.id.mainLayout));

    	final EditText mNameEdit = (EditText) layout.findViewById(R.id.editText1);
    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

    	final CheckBox nTaxBox1 = (CheckBox) layout.findViewById(R.id.taxBox1);
    	final CheckBox nTaxBox2 = (CheckBox) layout.findViewById(R.id.taxBox2);
    	final CheckBox nTaxBox3 = (CheckBox) layout.findViewById(R.id.taxBox3);

		Cursor c = (Cursor) mDepartmentListView.getItemAtPosition(info.position);

    	mNameEdit.setText(c.getString(c.getColumnIndex("name")));
    	final int Pid = c.getInt(c.getColumnIndex("_id"));

    	final TextView nTaxText = (TextView) layout.findViewById(R.id.textView1);

    	if (TaxSetting.getTax1Name() != null){
        	if (!TaxSetting.getTax1Name().equals("")){
        		if (nTaxBox1.getVisibility() == View.GONE) {
            		nTaxBox1.setVisibility(View.VISIBLE);
        		}
        		nTaxBox1.setText(TaxSetting.getTax1Name());
        		nTaxBox1.setChecked(c.getInt(c.getColumnIndex("tax1")) != 0);
        	} else {
        		if (nTaxBox1.getVisibility() == View.VISIBLE) {
        			nTaxBox1.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if (nTaxBox1.getVisibility() == View.VISIBLE) {
    			nTaxBox1.setVisibility(View.GONE);
    		}
    	}

    	if (TaxSetting.getTax2Name() != null) {
        	if (!TaxSetting.getTax2Name().equals("")) {
        		if (nTaxBox2.getVisibility() == View.GONE) {
            		nTaxBox2.setVisibility(View.VISIBLE);
        		}
        		nTaxBox2.setText(TaxSetting.getTax2Name());
        		nTaxBox2.setChecked(c.getInt(c.getColumnIndex("tax2")) != 0);
        	} else {
        		if (nTaxBox2.getVisibility() == View.VISIBLE) {
        			nTaxBox2.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if (nTaxBox2.getVisibility() == View.VISIBLE) {
    			nTaxBox2.setVisibility(View.GONE);
    		}
    	}

    	if (TaxSetting.getTax3Name() != null) {
        	if (!TaxSetting.getTax3Name().equals("")) {
        		if (nTaxBox3.getVisibility() == View.GONE) {
            		nTaxBox3.setVisibility(View.VISIBLE);
        		}
        		nTaxBox3.setText(TaxSetting.getTax3Name());
        		nTaxBox3.setChecked(c.getInt(c.getColumnIndex("tax3")) != 0);
        	} else {
        		if (nTaxBox3.getVisibility() == View.VISIBLE) {
        			nTaxBox3.setVisibility(View.GONE);
        		}
        	}
    	} else {
    		if (nTaxBox3.getVisibility() == View.VISIBLE) {
    			nTaxBox3.setVisibility(View.GONE);
    		}
    	}

    	if (nTaxBox1.getVisibility() == View.GONE && nTaxBox2.getVisibility() == View.GONE && nTaxBox3.getVisibility() == View.GONE){
    		nTaxText.setVisibility(View.GONE);
    	} else {
    		nTaxText.setVisibility(View.VISIBLE);
    	}


    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(layout)
            .setTitle(R.string.txt_edit_department)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    String name = mNameEdit.getText().toString();
                    name = name.replaceAll(", ", " ");
                    name = name.replaceAll(",", " ");
                    name = name.replaceAll("\"", "");
                    Category newprod = new Category();
                    newprod.setName(name);
                    newprod.setTaxable1(nTaxBox1.isChecked());
                    newprod.setTaxable2(nTaxBox2.isChecked());
                    newprod.setTaxable3(nTaxBox3.isChecked());

                    newprod.setId(Pid);
                    mDb.setCat(newprod);

                    ((DepartmentAdapter) mDepartmentListView.getAdapter()).getCursor().requery();

                    dialog.cancel();

                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

    	alertDialog = builder.create();
    	alertDialog.show();

    	mNameEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if (mNameEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
    	});
    }

    public void function2(MenuItem item){
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) mDepartmentListView.getItemAtPosition(info.position);
    	int Pid = c.getInt(c.getColumnIndex("_id"));
        mDb.RemoveCatagory(Pid, info.position);
		((DepartmentAdapter) mDepartmentListView.getAdapter()).getCursor().requery();
    }
}

