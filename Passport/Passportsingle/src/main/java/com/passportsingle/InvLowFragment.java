package com.passportsingle;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;



public class InvLowFragment extends Fragment {
	private ListView inventoryList;
	protected String catagory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Low Products Fragment");
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

		inventoryList = (ListView) view.findViewById(R.id.listofitems);
		ItemAdaptor itemAdapter = new ItemAdaptor(
				ProductDatabase.helper.fetchLowProds(""), getActivity()
						.getApplicationContext());
		inventoryList.setAdapter(itemAdapter);
		registerForContextMenu(inventoryList);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.departmentFilter);
		departmentFilter.setVisibility(View.GONE);
		return view;
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
}
