package com.pos.passport.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import com.pos.passport.R;
import com.pos.passport.adapter.CustomerAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Customer;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;

public class CustomerDetailsFragment extends Fragment {

	private GridView CustomerList;
	private View mylayout;
	private ProductDatabase mDb;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_item_list, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		CustomerList = (GridView) view.findViewById(R.id.item_list_view);
		CustomerAdapter itemAdapter = new CustomerAdapter(mDb.searchCustomers(""), getActivity());
		CustomerList.setAdapter(itemAdapter);
		registerForContextMenu(CustomerList);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.department_spinner);
		departmentFilter.setVisibility(View.GONE);
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		return view;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.clear();

		inflater.inflate(R.menu.cust_menu_tab, menu);
		MenuItem newitemView = menu.findItem(R.id.menu_newcust);

		newitemView.setTitle(R.string.txt_add_customer);
		newitemView.setIcon(R.drawable.person);

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				Log.v("Products", "" + newText);

				if (CustomerList != null)
					((CustomerAdapter) CustomerList.getAdapter()).changeCursor(mDb.searchCustomers(newText));
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
		});
		searchView.setIconifiedByDefault(false);
		searchView.setQuery("", true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			return true;
		case R.id.menu_newcust:
			addCustomerToDatabase();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addCustomerToDatabase() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_customer, (ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);

		builder = new AlertDialog.Builder(getActivity());
		builder.setView(mylayout)
            .setTitle(R.string.txt_add_customer_to_database)
            .setPositiveButton(R.string.txt_add_customer,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String name = mNameEdit.getText().toString();
                        String desc = mDescEdit.getText().toString();

                        name = name.replaceAll(", ", " ");
                        name = name.replaceAll(",", " ");

                        desc = desc.replaceAll(", ", " ");
                        desc = desc.replaceAll(",", " ");

                        Customer newprod = new Customer();
                        newprod.setFName(name);
                        newprod.setEmail(desc);

                        mDb.insertCustomer(newprod);
                        // inventoryList.invalidate();
                        // ((ItemAdaptor)
                        // inventoryList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedProds(""));
                        ((CustomerAdapter) CustomerList.getAdapter())
                                .getCursor().requery();

                        dialog.cancel();
                    }
                })
            .setNegativeButton(android.R.string.cancel,
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
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "Edit Customer") {
			function2(item);
		} else if (item.getTitle() == "Remove Customer") {
			function1(item);
		} else {
			return false;
		}
		return true;
	}

	private void function2(MenuItem item) {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		Cursor c = (Cursor) CustomerList.getItemAtPosition(info.position);

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_customer,
				(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
		final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);

		final Customer customer = new Customer();

		customer.fName = c.getString(c.getColumnIndex("fname"));
		customer.id = c.getInt(c.getColumnIndex("_id"));
		customer.email = c.getString(c.getColumnIndex("email"));
		customer.returns = c.getInt(c.getColumnIndex("numreturns"));
		customer.sales = c.getInt(c.getColumnIndex("numsales"));
		customer.total = new BigDecimal(c.getFloat(c.getColumnIndex("total")));

		mNameEdit.setText(customer.fName);
		mDescEdit.setText(customer.email);

		builder = new AlertDialog.Builder(getActivity());
		builder.setView(mylayout)
            .setTitle(R.string.txt_edit_customer_in_database)
            .setPositiveButton(R.string.txt_finish_editing_customer,
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
                        newprod.setFName(name);
                        newprod.setEmail(email);
                        newprod.returns = customer.returns;
                        newprod.sales = customer.sales;
                        newprod.total = customer.total;

                        mDb.replaceCustomer(newprod);
                        ((CustomerAdapter) CustomerList.getAdapter()).getCursor().requery();
                        dialog.cancel();
                    }
                })
            .setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

		alertDialog = builder.create();
		alertDialog.show();
	}

	private void function1(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) CustomerList.getItemAtPosition(info.position);
		mDb.removeCustomer(c.getInt(c.getColumnIndex("_id")));
		((CustomerAdapter) CustomerList.getAdapter()).getCursor().requery();
	}
}
