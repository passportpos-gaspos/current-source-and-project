package com.passportsingle;


import com.passportsingle.R;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class CustomerDetailsFragment extends Fragment {

	private ListView CustomerList;
	private View mylayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Customer Details Fragment");
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

		CustomerList = (ListView) view.findViewById(R.id.listofitems);
		CustomerAdaptor itemAdapter = new CustomerAdaptor(
				ProductDatabase.SearchCustomers(""), getActivity()
						.getApplicationContext());
		CustomerList.setAdapter(itemAdapter);
		registerForContextMenu(CustomerList);
		Spinner departmentFilter = (Spinner) view
				.findViewById(R.id.departmentFilter);
		departmentFilter.setVisibility(View.GONE);

		return view;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.clear();

		inflater.inflate(R.menu.inv_menu_tab, menu);

		MenuItem newitemView = menu.findItem(R.id.menu_newitem);

		newitemView.setTitle("Add Customer");
		newitemView.setIcon(R.drawable.person);

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();

		if (searchView!=null)
		{
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String newText) {
					Log.v("Products", "" + newText);

					if (CustomerList != null)
						((CustomerAdaptor) CustomerList.getAdapter())
								.changeCursor(ProductDatabase
										.SearchCustomers(newText));
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
			addCustomerToDatabase();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addCustomerToDatabase() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_customer,
				(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout
				.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout
				.findViewById(R.id.descEdit);

		builder = new AlertDialog.Builder(getActivity());
		builder.setInverseBackgroundForced(true);
		builder.setView(mylayout)
				.setTitle("Add Customer to Database")
				.setPositiveButton("Add Customer",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String name = mNameEdit.getText().toString();
								String desc = mDescEdit.getText().toString();

								name = name.replaceAll(", ", " ");
								name = name.replaceAll(",", " ");

								desc = desc.replaceAll(", ", " ");
								desc = desc.replaceAll(",", " ");

								Customer newprod = new Customer();
								newprod.setName(name);
								newprod.setEmail(desc);

								ProductDatabase.insertCustomer(newprod);
								// inventoryList.invalidate();
								// ((ItemAdaptor)
								// inventoryList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedProds(""));
								((CustomerAdaptor) CustomerList.getAdapter())
										.getCursor().requery();

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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Product Options");
		menu.add(0, v.getId(), 0, "Edit Customer");
		menu.add(0, v.getId(), 0, "Remove Customer");
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getTitle() == "Edit Customer") {
			function2(item);
		} else if (item.getTitle() == "Remove Customer") {
			function1(item);
		} else {
			return false;
		}
		return true;
	}

	private void function2(android.view.MenuItem item) {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		Cursor c = (Cursor) CustomerList.getItemAtPosition(info.position);

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_customer,
				(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout
				.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout
				.findViewById(R.id.descEdit);

		final Customer customer = new Customer();

		customer.name = c.getString(c.getColumnIndex("fname"));
		customer.id = c.getInt(c.getColumnIndex("_id"));
		customer.email = c.getString(c.getColumnIndex("email"));
		customer.returns = c.getInt(c.getColumnIndex("numreturns"));
		customer.sales = c.getInt(c.getColumnIndex("numsales"));
		customer.total = c.getFloat(c.getColumnIndex("total"));

		mNameEdit.setText(customer.name);
		mDescEdit.setText(customer.email);

		builder = new AlertDialog.Builder(getActivity());
		builder.setView(mylayout)
				.setTitle("Edit Customer In Database")
				.setInverseBackgroundForced(true)
				.setPositiveButton("Finish Editing Customer",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String name = mNameEdit.getText().toString();
								String email = mDescEdit.getText().toString();

								name = name.replaceAll(", ", " ");
								name = name.replaceAll(",", " ");

								email = email.replaceAll(", ", " ");
								email = email.replaceAll(",", " ");

								Customer newprod = new Customer();

								newprod.setId(customer.id);
								newprod.setName(name);
								newprod.setEmail(email);
								newprod.returns = customer.returns;
								newprod.sales = customer.sales;
								newprod.total = customer.total;

								ProductDatabase.replaceCustomer(newprod);
								((CustomerAdaptor) CustomerList.getAdapter())
										.getCursor().requery();
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

	private void function1(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor c = (Cursor) CustomerList.getItemAtPosition(info.position);
		ProductDatabase.RemoveCustomer(c.getInt(c.getColumnIndex("_id")));
		((CustomerAdaptor) CustomerList.getAdapter()).getCursor().requery();
	}
	
}
