package com.pos.passport.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Spinner;

import com.pos.passport.R;
import com.pos.passport.adapter.ItemAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.util.Utils;

public class LowInventoryFragment extends Fragment {
	private GridView mInventoryListView;
	protected String catagory;
	private ProductDatabase mDb;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_item_list, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		mInventoryListView = (GridView) view.findViewById(R.id.item_list_view);
		ItemAdapter itemAdapter = new ItemAdapter(mDb.helper.fetchLowProds(""), getActivity());
		mInventoryListView.setAdapter(itemAdapter);
		registerForContextMenu(mInventoryListView);
		Spinner departmentFilter = (Spinner) view.findViewById(R.id.department_spinner);
		departmentFilter.setVisibility(View.GONE);
		return view;
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
}
