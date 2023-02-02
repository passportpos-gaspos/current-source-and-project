package com.passportsingle;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.passportsingle.R;

public class RecentTransactionsFragmentMain  extends ListFragment {
	
	private int mActivatedPosition = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.report_list, null);
		return view;
	}
	
	@Override  
    public void onSaveInstanceState(Bundle outState) {   
    	super.onSaveInstanceState(outState);    
    	outState.putInt("curChoice", mActivatedPosition);  
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] values = new String[] { "Recent Transactions"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_list, values);
		setListAdapter(adapter);

		ActionBar actionBar = getActivity().getActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);


		if (savedInstanceState != null) {             
        	// Restore last state for checked position. 
        	mActivatedPosition = savedInstanceState.getInt("curChoice", 0);      
        	
        }
        ShowDetails(mActivatedPosition);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ShowDetails(position);
	}
	
	private void ShowDetails(int position) {
		String item = (String) getListAdapter().getItem(position);

		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.trnsDetails_Fragment);
		
		if (item.equals("Recent Transactions")) {
			if (!(fragment instanceof RecentTransactionDetailsFragment)) {
				RecentTransactionDetailsFragment newFragment = new RecentTransactionDetailsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.trnsDetails_Fragment, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} 
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
