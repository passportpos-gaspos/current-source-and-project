package com.passportsingle;



import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class InventoryFragmentMain extends ListFragment {
	private int mActivatedPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("Inventory Main", "Create");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.report_list, null);
		Log.v("Inventory Main", "CreateView");

		return view;
	}
	
    @Override  
    public void onSaveInstanceState(Bundle outState) {   
    	super.onSaveInstanceState(outState);  
		Log.v("Inventory Main", "SavedSate");

    	outState.putInt("curChoice", mActivatedPosition);  
    } 
    
	@Override
	public void onResume() {
		Log.v("Inventory Main", "Resumed");
		super.onResume();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v("Inventory Main", "ActivityCreated");

		super.onActivityCreated(savedInstanceState);
		
		
		String[] values = new String[] { "Products", "Departments", "Quick Buttons",
				"Low Inventory", "Import/Export" };
		
        if(InventoryFragment.location == 1)
        {
        	values = new String[] {"Quick Buttons"};
			if (getActivity() != null && getActivity().getActionBar() != null)
        		getActivity().getActionBar().setTitle("Quick Buttons");
        }
        
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_list, values);
		setListAdapter(adapter);

		ActionBar actionBar = getActivity().getActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			    
        if (savedInstanceState != null) {             
        	mActivatedPosition = savedInstanceState.getInt("curChoice", 0);      
        }
        
		Log.v("Inventory Main", "ActivityCreated " + InventoryFragment.location);
        
        ShowDetails(mActivatedPosition);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v("Inventory Main", "Click");

		ShowDetails(position);
	}
	
	
	private void ShowDetails(int position) {

		String item = (String) getListAdapter().getItem(position);

		mActivatedPosition = position;
		
		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.invDetails_Fragment);
		
		if (item.equals("Products")) {
			if (!(fragment instanceof InvProductsFragment)) {
				InvProductsFragment newFragment = new InvProductsFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.invDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Departments")) {
			if (!(fragment instanceof InvDepartmentsFragment)) {
				InvDepartmentsFragment newFragment = new InvDepartmentsFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.invDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Quick Buttons")) {
			if (!(fragment instanceof InvButtonsFragment)) {
				InvButtonsFragment newFragment = new InvButtonsFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.invDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Low Inventory")) {
			if (!(fragment instanceof InvLowFragment)) {
				InvLowFragment newFragment = new InvLowFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.invDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Import/Export")) {
			if (!(fragment instanceof InvImportExportFragment)) {
				InvImportExportFragment newFragment = new InvImportExportFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.replace(R.id.invDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		}
	}
}
