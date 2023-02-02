package com.passportsingle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;



public class InvDepartmentsFragment extends Fragment {

	private ListView departmentList;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.itemsview, container, false);

		departmentList = (ListView) view.findViewById(R.id.listofitems);
		ProductAdapter itemAdapter = new ProductAdapter(
				ProductDatabase.helper.fetchNamedCat(""));
		departmentList.setAdapter(itemAdapter);
		
		registerForContextMenu(departmentList);
		
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.departmentFilter);
		departmentFilter.setVisibility(View.GONE);
		return view;
	}

	public class ProductAdapter extends CursorAdapter {

		public ProductAdapter(Cursor cursor) {
			super(getActivity(), cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final int itemColumnIndex = cursor.getColumnIndexOrThrow("name");
			final int idColumnIndex = cursor.getColumnIndexOrThrow("_id");

			((TextView) view.findViewById(R.id.cat_name)).setText(cursor
					.getString(itemColumnIndex));
			((TextView) view.findViewById(R.id.prodnum)).setText(cursor
					.getString(idColumnIndex));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			final View view = inflater.inflate(R.layout.listofcats, parent,
					false);
			return view;
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    menu.clear();


			inflater.inflate(R.menu.dep_menu_tab, menu);

		    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			if (searchView!=null)
			{
				searchView.setOnQueryTextListener(
						new SearchView.OnQueryTextListener() {
							@Override
							public boolean onQueryTextChange(String newText) {
								Log.v("Products", "" + newText);

								if(departmentList != null)
									((ProductAdapter) departmentList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedCat(newText));
								return true;
							}

							@Override
							public boolean onQueryTextSubmit(String query) {
								// TODO Auto-generated method stub
								return false;
							}
						});
				searchView.setIconifiedByDefault(false);
			}

		
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences mSharedPreferences;
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			return true;
		case R.id.menu_newitem:
			mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
			
			if(lic){
				addDepToDatabase();
			}else{
				alertbox("Unlicensed", "Please license this app to add new departments. Thank you.");
			}
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
    	
    	if(TaxSetting.getTax1name() != null){
        	if(!TaxSetting.getTax1name().equals("")){
        		if(nTaxBox1.getVisibility() == View.GONE){
            		nTaxBox1.setVisibility(View.VISIBLE);
        		}
        		nTaxBox1.setText(TaxSetting.getTax1name());
        	}else{
        		if(nTaxBox1.getVisibility() == View.VISIBLE){
        			nTaxBox1.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox1.getVisibility() == View.VISIBLE){
    			nTaxBox1.setVisibility(View.GONE);
    		}
    	}
    	
    	if(TaxSetting.getTax2name() != null){
        	if(!TaxSetting.getTax2name().equals("")){
        		if(nTaxBox2.getVisibility() == View.GONE){
            		nTaxBox2.setVisibility(View.VISIBLE);
        		}
        		nTaxBox2.setText(TaxSetting.getTax2name());
        	}else{
        		if(nTaxBox2.getVisibility() == View.VISIBLE){
        			nTaxBox2.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox2.getVisibility() == View.VISIBLE){
    			nTaxBox2.setVisibility(View.GONE);
    		}
    	}
    	
    	if(TaxSetting.getTax3name() != null){
        	if(!TaxSetting.getTax3name().equals("")){
        		if(nTaxBox3.getVisibility() == View.GONE){
            		nTaxBox3.setVisibility(View.VISIBLE);
        		}
        		nTaxBox3.setText(TaxSetting.getTax3name());
        	}else{
        		if(nTaxBox3.getVisibility() == View.VISIBLE){
        			nTaxBox3.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox3.getVisibility() == View.VISIBLE){
    			nTaxBox3.setVisibility(View.GONE);
    		}
    	}
    	
    	if(nTaxBox1.getVisibility() == View.GONE && nTaxBox2.getVisibility() == View.GONE && nTaxBox3.getVisibility() == View.GONE){
    		nTaxText.setVisibility(View.GONE);
    	}else{
    		nTaxText.setVisibility(View.VISIBLE);
    	}
    	
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(layout)
    	.setTitle("New Department")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Add Department", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
	    	    String name = mNameEdit.getText().toString();
	    	    
	        	name = name.replaceAll(", ", " ");
	        	name = name.replaceAll(",", " ");
	        	name = name.replaceAll("\"", "");
	    	    Catagory newprod = new Catagory();
	    	    newprod.setName(name);
	    	    newprod.setTaxable1(nTaxBox1.isChecked());
	    	    newprod.setTaxable2(nTaxBox2.isChecked());
	    	    newprod.setTaxable3(nTaxBox3.isChecked());

	    	    ProductDatabase.insertCat(newprod);
	    		((ProductAdapter) departmentList.getAdapter()).getCursor().requery();

            	dialog.cancel();
            	
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();
    	
    	if(mNameEdit.getText().toString().equals("")){
    		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
    	}
    	
    	mNameEdit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				if(mNameEdit.getText().toString().equals("")){
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
				}else{
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); 
				}
			}
    	});
    	
    }
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Department Options");  
        menu.add(0, v.getId(), 0, "Edit Department");  
        menu.add(0, v.getId(), 0, "Remove Department");  
    }  
	
    @Override  
    public boolean onContextItemSelected(android.view.MenuItem item) {  
        if(item.getTitle()=="Edit Department"){function1(item);}  
        else if(item.getTitle()=="Remove Department"){function2(item);}  
        else {return false;}  
    return true;  
    } 
    
    public void function1(android.view.MenuItem item){  
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

		Cursor c = (Cursor) departmentList.getItemAtPosition(info.position);
    	   	
    	mNameEdit.setText(c.getString(c.getColumnIndex("name")));
    	final int Pid = c.getInt(c.getColumnIndex("_id"));

    	final TextView nTaxText = (TextView) layout.findViewById(R.id.textView1);
    	
    	if(TaxSetting.getTax1name() != null){
        	if(!TaxSetting.getTax1name().equals("")){
        		if(nTaxBox1.getVisibility() == View.GONE){
            		nTaxBox1.setVisibility(View.VISIBLE);
        		}
        		nTaxBox1.setText(TaxSetting.getTax1name());
        		nTaxBox1.setChecked(c.getInt(c.getColumnIndex("tax1")) != 0);
        	}else{
        		if(nTaxBox1.getVisibility() == View.VISIBLE){
        			nTaxBox1.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox1.getVisibility() == View.VISIBLE){
    			nTaxBox1.setVisibility(View.GONE);
    		}
    	}
    	
    	if(TaxSetting.getTax2name() != null){
        	if(!TaxSetting.getTax2name().equals("")){
        		if(nTaxBox2.getVisibility() == View.GONE){
            		nTaxBox2.setVisibility(View.VISIBLE);
        		}
        		nTaxBox2.setText(TaxSetting.getTax2name());
        		nTaxBox2.setChecked(c.getInt(c.getColumnIndex("tax2")) != 0);
        	}else{
        		if(nTaxBox2.getVisibility() == View.VISIBLE){
        			nTaxBox2.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox2.getVisibility() == View.VISIBLE){
    			nTaxBox2.setVisibility(View.GONE);
    		}
    	}
    	
    	if(TaxSetting.getTax3name() != null){
        	if(!TaxSetting.getTax3name().equals("")){
        		if(nTaxBox3.getVisibility() == View.GONE){
            		nTaxBox3.setVisibility(View.VISIBLE);
        		}
        		nTaxBox3.setText(TaxSetting.getTax3name());
        		nTaxBox3.setChecked(c.getInt(c.getColumnIndex("tax3")) != 0);
        	}else{
        		if(nTaxBox3.getVisibility() == View.VISIBLE){
        			nTaxBox3.setVisibility(View.GONE);
        		}
        	}
    	}else{
    		if(nTaxBox3.getVisibility() == View.VISIBLE){
    			nTaxBox3.setVisibility(View.GONE);
    		}
    	}
    	
    	if(nTaxBox1.getVisibility() == View.GONE && nTaxBox2.getVisibility() == View.GONE && nTaxBox3.getVisibility() == View.GONE){
    		nTaxText.setVisibility(View.GONE);
    	}else{
    		nTaxText.setVisibility(View.VISIBLE);
    	}
    	
    	
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(layout)
    	.setTitle("Edit Department")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
	    	    String name = mNameEdit.getText().toString();
	        	name = name.replaceAll(", ", " ");
	        	name = name.replaceAll(",", " ");
	        	name = name.replaceAll("\"", "");
	    	    Catagory newprod = new Catagory();
	    	    newprod.setName(name);
	    	    newprod.setTaxable1(nTaxBox1.isChecked());
	    	    newprod.setTaxable2(nTaxBox2.isChecked());
	    	    newprod.setTaxable3(nTaxBox3.isChecked());

	    	    newprod.setId(Pid);
	    	    ProductDatabase.setCat(newprod);
	    	    
	    		((ProductAdapter) departmentList.getAdapter()).getCursor().requery();

            	dialog.cancel();
            	
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();
    	    	
    	mNameEdit.addTextChangedListener(new TextWatcher(){

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
				
				if(mNameEdit.getText().toString().equals("")){
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); 
				}else{
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true); 
				}
			}
    	});
    } 
    
    public void function2(android.view.MenuItem item){  
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) departmentList.getItemAtPosition(info.position);
    	int Pid = c.getInt(c.getColumnIndex("_id"));
        ProductDatabase.RemoveCatagory(Pid, info.position);
		((ProductAdapter) departmentList.getAdapter()).getCursor().requery();
    }
	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}
}

