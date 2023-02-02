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



public class SettingsFragmentMain extends ListFragment {
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private int mActivatedPosition = 0;
	private View detailsFrame;

	public SettingsFragmentMain() {
	}
	
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
		String[] values = null;
		
		String Locale = getResources().getConfiguration().locale.getCountry();

			values = new String[] { "Tax Settings", "Store Settings", "Cashiers",
					"Email Settings", "Receipt Settings", "Admin Password", "Payment Settings",
					"Database Settings", "Registration" };
		
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_list, values);
		setListAdapter(adapter);

		ActionBar actionBar = getActivity().getActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		
	    detailsFrame = getActivity().findViewById(R.id.settingsDetails_Container); 
	    
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
		mActivatedPosition = position;
		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.settingsDetails_Container);
			
		if (item.equals("Tax Settings")) {
			if (!(fragment instanceof TaxSettingsFragment)) {
				TaxSettingsFragment newFragment = new TaxSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Store Settings")) {
			if (!(fragment instanceof StoreSettingsFragment)) {
				StoreSettingsFragment newFragment = new StoreSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Cashiers")) {
			if (!(fragment instanceof CashierSettingsFragment)) {
				CashierSettingsFragment newFragment = new CashierSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Email Settings")) {
			if (!(fragment instanceof EmailSettingsFragment)) {
				EmailSettingsFragment newFragment = new EmailSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Receipt Settings")) {
			if (!(fragment instanceof ReceiptSettingFragment)) {
				ReceiptSettingFragment newFragment = new ReceiptSettingFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Admin Password")) {
			if (!(fragment instanceof AdminSettingsFragment)) {
				AdminSettingsFragment newFragment = new AdminSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Payment Settings")) {
			if (!(fragment instanceof SwipeFragment)) {
				SwipeFragment newFragment = new SwipeFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Database Settings")) {
			if (!(fragment instanceof DatabaseSettingsFragment)) {
				DatabaseSettingsFragment newFragment = new DatabaseSettingsFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Registration")) {
			if (!(fragment instanceof RegistrationFragment)) {
				RegistrationFragment newFragment = new RegistrationFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.settingsDetails_Container, newFragment);
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
