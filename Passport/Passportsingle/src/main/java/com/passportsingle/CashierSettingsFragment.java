package com.passportsingle;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;


import com.passportsingle.R;

public class CashierSettingsFragment extends Fragment {

	private ListView CashierList;
	private View mylayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Cashier Details Fragment");
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

		CashierList = (ListView) view.findViewById(R.id.listofitems);
		CustomerAdaptor itemAdapter = new CustomerAdaptor(ProductDatabase.SearchCashiers(""), getActivity().getApplicationContext());
		CashierList.setAdapter(itemAdapter);
		registerForContextMenu(CashierList);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.departmentFilter);
		departmentFilter.setVisibility(View.GONE);
				
		return view;
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
	    menu.clear();

			inflater.inflate(R.menu.inv_menu_tab, menu);
			
			MenuItem newitemView = menu.findItem(R.id.menu_newitem);

			newitemView.setTitle("Add Cashier");
			newitemView.setIcon(R.drawable.person);

		    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			if (searchView!=null)
			{
				searchView.setOnQueryTextListener(
						new SearchView.OnQueryTextListener() {
							@Override
							public boolean onQueryTextChange(String newText) {
								if(CashierList != null)
									((CustomerAdaptor) CashierList.getAdapter()).changeCursor(ProductDatabase.SearchCashiers(newText));
								return true;
							}

							@Override
							public boolean onQueryTextSubmit(String query) {
								// TODO Auto-generated method stub
								return false;
							}
						});
				searchView.setIconifiedByDefault(false);
				searchView.setQuery("", true);
			}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish(); 
			return true;
		case R.id.menu_newitem:
			if(!AdminSetting.enabled)
			{
				alertbox("Admin Account Needed", "Before adding cashiers, please create an Admin Account.");
				return true;
			}
			addCashierToDatabase();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void addCashierToDatabase() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_cashier,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);
		final EditText mPinEdit = (EditText) mylayout.findViewById(R.id.pinEdit);
		
		final CheckBox permissionSettings = (CheckBox) mylayout.findViewById(R.id.cashierEditSetting);
		final CheckBox permissionInventory = (CheckBox) mylayout.findViewById(R.id.cashierEditInventory);
		final CheckBox permissionReports = (CheckBox) mylayout.findViewById(R.id.cashierReports);
		final CheckBox permissionReturn = (CheckBox) mylayout.findViewById(R.id.cashierReturns);
		final CheckBox permissionPriceModify = (CheckBox) mylayout.findViewById(R.id.cashierModify);

		final CheckBox permissionTraining = (CheckBox) mylayout.findViewById(R.id.cashierTraining);

		permissionTraining.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(permissionTraining.isChecked())
				{
					permissionSettings.setChecked(false);
					permissionInventory.setChecked(false);
					permissionReports.setChecked(false);
					permissionReturn.setChecked(false);
					permissionPriceModify.setChecked(false);
					
					permissionSettings.setEnabled(false);
					permissionInventory.setEnabled(false);
					permissionReports.setEnabled(false);
					permissionReturn.setEnabled(false);
					permissionPriceModify.setEnabled(false);
					
					mNameEdit.setText(getResources().getString(R.string.training_cash));
					mNameEdit.setEnabled(false);
				}else{
					permissionSettings.setEnabled(true);
					permissionInventory.setEnabled(true);
					permissionReports.setEnabled(true);
					permissionReturn.setEnabled(true);
					permissionPriceModify.setEnabled(true);
					
					mNameEdit.setEnabled(true);
					mNameEdit.setText("");
				}
			}
		});
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setInverseBackgroundForced(true);
		builder.setView(mylayout)
				.setTitle("Add Cashier to Database")
				.setPositiveButton("Add Cashier",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String name = mNameEdit.getText().toString();
								String desc = mDescEdit.getText().toString();
								String pin = mPinEdit.getText().toString();

					        	name = name.replaceAll(", ", " ");
					        	name = name.replaceAll(",", " ");
					        	
					        	desc = desc.replaceAll(", ", " ");
					        	desc = desc.replaceAll(",", " ");
					        	
					        	Cashier cashier = new Cashier();
					        	cashier.name = (name);
					        	cashier.email = (desc);
					        	cashier.pin = (pin);
								cashier.permissionInventory = permissionInventory.isChecked();
								cashier.permissionSettings = permissionSettings.isChecked();
								cashier.permissionReports = permissionReports.isChecked();
								cashier.permissionReturn = permissionReturn.isChecked();
								cashier.permissionPriceModify = permissionPriceModify.isChecked();

								ProductDatabase.insertCashier(cashier);
								// inventoryList.invalidate();
								// ((ItemAdaptor)
								// inventoryList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedProds(""));
								((CustomerAdaptor) CashierList.getAdapter()).getCursor().requery();
								
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

    	alertDialog = builder.create();
    	alertDialog.show();
	}
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Cashier Options");  
        menu.add(0, v.getId(), 0, "Edit Cashier");  
        menu.add(0, v.getId(), 0, "Remove Cashier");  
    }  
	
    @Override  
    public boolean onContextItemSelected(android.view.MenuItem item) {  
        if(item.getTitle()=="Edit Cashier"){function2(item);}  
        else if(item.getTitle()=="Remove Cashier"){function1(item);}  
        else {return false;}  
    return true;  
    }

	private void function2(android.view.MenuItem item) {
		AlertDialog.Builder builder;
    	final AlertDialog alertDialog;

    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
		Cursor c = (Cursor) CashierList.getItemAtPosition(info.position);

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_cashier,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);
		final EditText mPinEdit = (EditText) mylayout.findViewById(R.id.pinEdit);
		
		final CheckBox permissionSettings = (CheckBox) mylayout.findViewById(R.id.cashierEditSetting);
		final CheckBox permissionInventory = (CheckBox) mylayout.findViewById(R.id.cashierEditInventory);
		final CheckBox permissionReports = (CheckBox) mylayout.findViewById(R.id.cashierReports);
		final CheckBox permissionReturn = (CheckBox) mylayout.findViewById(R.id.cashierReturns);
		final CheckBox permissionPriceModify = (CheckBox) mylayout.findViewById(R.id.cashierModify);
    	
    	final Cashier cashier = new Cashier();
    	
    	cashier.name = c.getString(c.getColumnIndex("fname"));
    	cashier.id = c.getInt(c.getColumnIndex("_id"));
    	cashier.email = c.getString(c.getColumnIndex("email"));
    	cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
    	cashier.sales = c.getInt(c.getColumnIndex("numsales"));
    	cashier.total = c.getFloat(c.getColumnIndex("total"));
       
    	cashier.pin = c.getString(c.getColumnIndex("pin"));
    	cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
    	cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
    	cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
    	cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
    	cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;

    	mNameEdit.setText(cashier.name);
    	mDescEdit.setText(cashier.email);
    	mPinEdit.setText(cashier.pin);

    	permissionReturn.setChecked(cashier.permissionReturn);
    	permissionPriceModify.setChecked(cashier.permissionPriceModify);
    	permissionReports.setChecked(cashier.permissionReports);
    	permissionInventory.setChecked(cashier.permissionInventory);
    	permissionSettings.setChecked(cashier.permissionSettings);
    	
		final CheckBox permissionTraining = (CheckBox) mylayout.findViewById(R.id.cashierTraining);

		permissionTraining.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(permissionTraining.isChecked())
				{
					permissionSettings.setChecked(false);
					permissionInventory.setChecked(false);
					permissionReports.setChecked(false);
					permissionReturn.setChecked(false);
					permissionPriceModify.setChecked(false);
					
					permissionSettings.setEnabled(false);
					permissionInventory.setEnabled(false);
					permissionReports.setEnabled(false);
					permissionReturn.setEnabled(false);
					permissionPriceModify.setEnabled(false);
					
					mNameEdit.setText(getResources().getString(R.string.training_cash));
					mNameEdit.setEnabled(false);
				}else{
					permissionSettings.setEnabled(true);
					permissionInventory.setEnabled(true);
					permissionReports.setEnabled(true);
					permissionReturn.setEnabled(true);
					permissionPriceModify.setEnabled(true);
					
					mNameEdit.setEnabled(true);
					mNameEdit.setText("");
				}
			}
		});

		if(cashier.name.equals(getResources().getString(R.string.training_cash)))
		{
			permissionSettings.setChecked(false);
			permissionInventory.setChecked(false);
			permissionReports.setChecked(false);
			permissionReturn.setChecked(false);
			permissionPriceModify.setChecked(false);
			
			permissionSettings.setEnabled(false);
			permissionInventory.setEnabled(false);
			permissionReports.setEnabled(false);
			permissionReturn.setEnabled(false);
			permissionPriceModify.setEnabled(false);
			
			permissionTraining.setChecked(true);
			
			mNameEdit.setText(getResources().getString(R.string.training_cash));
			
			mNameEdit.setEnabled(false);
		}

    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Edit Cashier In Database")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Finish Editing Cashier", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
				String name = mNameEdit.getText().toString();
				String desc = mDescEdit.getText().toString();
				String pin = mPinEdit.getText().toString();

	        	name = name.replaceAll(", ", " ");
	        	name = name.replaceAll(",", " ");
	        	
	        	desc = desc.replaceAll(", ", " ");
	        	desc = desc.replaceAll(",", " "); 
	        	
	        	Cashier newCashier = new Cashier();

	        	newCashier.name = (name);
	        	newCashier.email = (desc);
	        	newCashier.pin = (pin);
	        	newCashier.permissionInventory = permissionInventory.isChecked();
	        	newCashier.permissionSettings = permissionSettings.isChecked();
	        	newCashier.permissionReports = permissionReports.isChecked();
	        	newCashier.permissionReturn = permissionReturn.isChecked();
	        	newCashier.permissionPriceModify = permissionPriceModify.isChecked();

	        	newCashier.id = cashier.id;
	        	newCashier.returns = cashier.returns;
	        	newCashier.sales = cashier.sales;
	        	newCashier.total = cashier.total;
	    	    
	    	    ProductDatabase.replaceCashier(newCashier);
	    		((CustomerAdaptor) CashierList.getAdapter()).getCursor().requery();
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
	}

	private void function1(android.view.MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 	
		Cursor c = (Cursor) CashierList.getItemAtPosition(info.position);
		ProductDatabase.RemoveCashier(c.getInt(c.getColumnIndex("_id")));
		((CustomerAdaptor) CashierList.getAdapter()).getCursor().requery();
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
