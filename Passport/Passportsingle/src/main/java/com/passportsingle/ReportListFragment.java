package com.passportsingle;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import android.app.ActionBar;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

public class ReportListFragment extends ListFragment implements
		OnDateSetListener {

	private int mYear;
	private int mMonth;
	private int mDay;
	static final int DATE_DIALOG_ID = 1;

	private int getToFrom;

	private String from = "";
	private String to = "";

	private GregorianCalendar fromDate;
	private GregorianCalendar toDate;

	private TextView dateRange;
	private Button fromButton;
	private Button toButton;
	private int mActivatedPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		from = DateFormat.getDateInstance().format(new Date());
		to = DateFormat.getDateInstance().format(new Date());

		fromDate = (GregorianCalendar) Calendar.getInstance();
		fromDate.set(Calendar.HOUR_OF_DAY, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);
		
		toDate = (GregorianCalendar) Calendar.getInstance();
		toDate.set(Calendar.HOUR_OF_DAY, 23);
		toDate.set(Calendar.MINUTE, 59);
		toDate.set(Calendar.SECOND, 59);
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
		if(PrioritySetting.enabled)
		{
			values = new String[] {"End of Shift", "End of Day", "History", "Items Sold", "Summary", "Margins" ,"Customer", "Priority Auths" };
		}else{
			values = new String[] {"End of Shift", "End of Day", "History", "Items Sold", "Summary", "Margins" ,"Customer", };
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_list, values);
		setListAdapter(adapter);
		
		ActionBar actionBar = getActivity().getActionBar();
		if (actionBar!=null)
			actionBar.setDisplayHomeAsUpEnabled(true);
		
		fromButton = (Button) getActivity().findViewById(R.id.from);
		toButton = (Button) getActivity().findViewById(R.id.to);
		dateRange = (TextView) getActivity().findViewById(R.id.dateRange);

		dateRange.setText(from + " - " + to);

		fromButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar c = Calendar.getInstance();
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);
				getToFrom = 0;

				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				DialogFragment newFragment = new DatePickerDialogFragment(
						ReportListFragment.this);
				newFragment.show(ft, "dialog");
			}
		});

		toButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar c = Calendar.getInstance();
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);
				getToFrom = 1;
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				DialogFragment newFragment = new DatePickerDialogFragment(
						ReportListFragment.this);
				newFragment.show(ft, "dialog");
			}
		});
        
        if (savedInstanceState != null) {             
        	// Restore last state for checked position. 
        	mActivatedPosition = savedInstanceState.getInt("curChoice", 0);      
            ShowDetails(mActivatedPosition);

        }
        else
        {

			ShiftFragment newFragment = new ShiftFragment();
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.report_Container, newFragment);
			transaction.commit();
			
			newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
        }
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ShowDetails(position);
		getActivity().supportInvalidateOptionsMenu();

	}

	private void ShowDetails(int position) {
		String item = (String) getListAdapter().getItem(position);
		mActivatedPosition = position;

		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.report_Container);

		if (item.equals("End of Shift")) {
			if (!(fragment instanceof ShiftFragment)) {
				ShiftFragment newFragment = new ShiftFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("End of Day")) {
			if (!(fragment instanceof DayFragment)) {
				DayFragment newFragment = new DayFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("History")) {
			if (!(fragment instanceof HistoryFragment)) {
				HistoryFragment newFragment = new HistoryFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				transaction.commit();
			}
		} else if (item.equals("Items Sold")) {
			if (!(fragment instanceof ItemsSoldFragment)) {
				ItemsSoldFragment newFragment = new ItemsSoldFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		} else if (item.equals("Summary")) {
			if (!(fragment instanceof SummaryFragment)) {
				SummaryFragment newFragment = new SummaryFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		} else if (item.equals("Customer")) {
			if (!(fragment instanceof CustomerFragment)) {
				CustomerFragment newFragment = new CustomerFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		} else if (item.equals("Margins")) {
			if (!(fragment instanceof ProfitFragment)) {
				ProfitFragment newFragment = new ProfitFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		} else if (item.equals("Cashier")) {
			if (!(fragment instanceof CashierReportFragment)) {
				CashierReportFragment newFragment = new CashierReportFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		} else if (item.equals("Priority Auths")) {
			if (!(fragment instanceof SwipeReportFragment)) {
				SwipeReportFragment newFragment = new SwipeReportFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				transaction.commit();
			}
		}
		
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;

		Fragment fragment = getFragmentManager().findFragmentById(
				R.id.report_Container);
		if (fragment instanceof ShiftFragment) {
			ShiftFragment newFragment = (ShiftFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof DayFragment) {
			DayFragment newFragment = (DayFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof HistoryFragment) {
			HistoryFragment newFragment = (HistoryFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof ItemsSoldFragment) {
			ItemsSoldFragment newFragment = (ItemsSoldFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof SummaryFragment) {
			SummaryFragment newFragment = (SummaryFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof ProfitFragment) {
			ProfitFragment newFragment = (ProfitFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof CustomerFragment) {
			CustomerFragment newFragment = (CustomerFragment) fragment;
			newFragment.clearReport();
		} else if (fragment instanceof SwipeReportFragment) {
			SwipeReportFragment newFragment = (SwipeReportFragment) fragment;
			newFragment.clearReport();
		}

		GregorianCalendar date = new GregorianCalendar(mYear, mMonth, mDay);

		if (getToFrom == 0) {
			fromDate = date;
			fromDate.set(Calendar.HOUR_OF_DAY, 0);
			fromDate.set(Calendar.MINUTE, 0);
			fromDate.set(Calendar.SECOND, 0);
			from = DateFormat.getDateInstance().format(fromDate.getTime());
			dateRange.setText(from + " - " + to);
		} else {
			toDate = date;
			toDate.set(Calendar.HOUR_OF_DAY, 23);
			toDate.set(Calendar.MINUTE, 59);
			toDate.set(Calendar.SECOND, 59);
			to = DateFormat.getDateInstance().format(toDate.getTime());
			dateRange.setText(from + " - " + to); 
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.exportreport, menu);
		
		if(ReceiptSetting.enabled)
			menu.findItem(R.id.printReport).setVisible(true);
		else
			menu.findItem(R.id.printReport).setVisible(false);
		
		menu.findItem(R.id.saveReport).setVisible(true);
		menu.findItem(R.id.endReport).setVisible(false);
		
		if(EmailSetting.isEnabled())
			menu.findItem(R.id.email_report).setVisible(true);
		else
			menu.findItem(R.id.email_report).setVisible(false);
		
		if (mActivatedPosition == 0) {
			menu.findItem(R.id.endReport).setVisible(true);
			menu.findItem(R.id.printReport).setVisible(false);
			menu.findItem(R.id.saveReport).setVisible(false);
			menu.findItem(R.id.email_report).setVisible(false);
			menu.findItem(R.id.endReport).setTitle("End Shift");
		}
		
		if (mActivatedPosition == 1) {
			menu.findItem(R.id.endReport).setVisible(true);
			menu.findItem(R.id.printReport).setVisible(false);
			menu.findItem(R.id.saveReport).setVisible(false);
			menu.findItem(R.id.email_report).setVisible(false);
			menu.findItem(R.id.endReport).setTitle("End Day");
		}
		
		if (mActivatedPosition == 2) {
			menu.findItem(R.id.printReport).setVisible(false);
			menu.findItem(R.id.email_report).setVisible(false);
		}
		
		if (mActivatedPosition == 6) {
			menu.findItem(R.id.printReport).setVisible(false);
			menu.findItem(R.id.email_report).setVisible(false);
		}
		
		if (mActivatedPosition == 7) {
			menu.findItem(R.id.endReport).setVisible(true);
			menu.findItem(R.id.printReport).setVisible(false);
			menu.findItem(R.id.email_report).setVisible(false);
			menu.findItem(R.id.endReport).setTitle("Process Pre-Auths");
			menu.findItem(R.id.saveReport).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				getActivity().finish();
				return true;
			case R.id.saveReport:
				exportReportClicked();
				return true;
			case R.id.endReport:
				endReportClicked();
				return true;
			case R.id.build_report:
				BuildReportClicked();
				return true;
			case R.id.printReport:
				PrintReportClicked();
				return true;
			case R.id.email_report:
				EmailReportClicked();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void endReportClicked() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
		if (fragment instanceof ShiftFragment) {
			ShiftFragment newFragment = (ShiftFragment) fragment;
			newFragment.endReport();
		} else if (fragment instanceof DayFragment) {
			DayFragment newFragment = (DayFragment) fragment;
			newFragment.endReport();
		} else if (fragment instanceof SwipeReportFragment) {
			SwipeReportFragment newFragment = (SwipeReportFragment) fragment;
			newFragment.endReport();
		}
	}

	private void exportReportClicked() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
		if (fragment instanceof ShiftFragment) {
			ShiftFragment newFragment = (ShiftFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof DayFragment) {
			DayFragment newFragment = (DayFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof HistoryFragment) {
			HistoryFragment newFragment = (HistoryFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof ItemsSoldFragment) {
			ItemsSoldFragment newFragment = (ItemsSoldFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof SummaryFragment) {
			SummaryFragment newFragment = (SummaryFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof ProfitFragment) {
			ProfitFragment newFragment = (ProfitFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof CustomerFragment) {
			CustomerFragment newFragment = (CustomerFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof CashierReportFragment) {
			CashierReportFragment newFragment = (CashierReportFragment) fragment;
			newFragment.exportReport();
		} else if (fragment instanceof SwipeReportFragment) {
			SwipeReportFragment newFragment = (SwipeReportFragment) fragment;
			newFragment.exportReport();
		}
	}

	private void BuildReportClicked() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
		if (fragment instanceof ShiftFragment) {
			ShiftFragment newFragment = (ShiftFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof DayFragment) {
			DayFragment newFragment = (DayFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof HistoryFragment) {
			HistoryFragment newFragment = (HistoryFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof ItemsSoldFragment) {
			ItemsSoldFragment newFragment = (ItemsSoldFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		}else if (fragment instanceof SummaryFragment) {
			SummaryFragment newFragment = (SummaryFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof ProfitFragment) {
			ProfitFragment newFragment = (ProfitFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof CustomerFragment) {
			CustomerFragment newFragment = (CustomerFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof CashierReportFragment) {
			CashierReportFragment newFragment = (CashierReportFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		} else if (fragment instanceof SwipeReportFragment) {
			SwipeReportFragment newFragment = (SwipeReportFragment) fragment;
			newFragment.buildReport(fromDate.getTime().getTime(), toDate.getTime().getTime());
		}
	}
	
	private void PrintReportClicked() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
		if (fragment instanceof ItemsSoldFragment) {
			ItemsSoldFragment newFragment = (ItemsSoldFragment) fragment;
			newFragment.printReport(false);
		} else if (fragment instanceof SummaryFragment) {
			SummaryFragment newFragment = (SummaryFragment) fragment;
			newFragment.printReport(false);
		} else if (fragment instanceof ProfitFragment) {
			ProfitFragment newFragment = (ProfitFragment) fragment;
			newFragment.printReport(false);
		} else if (fragment instanceof CustomerFragment) {
			CustomerFragment newFragment = (CustomerFragment) fragment;
			//newFragment.printReport();
		} else if (fragment instanceof CashierReportFragment) {
			CashierReportFragment newFragment = (CashierReportFragment) fragment;
			//newFragment.printReport();
		} 
	}
	
	private void EmailReportClicked() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
		if (fragment instanceof ItemsSoldFragment) {
			ItemsSoldFragment newFragment = (ItemsSoldFragment) fragment;
			newFragment.printReport(true);
		} else if (fragment instanceof SummaryFragment) {
			SummaryFragment newFragment = (SummaryFragment) fragment;
			newFragment.printReport(true);
		} else if (fragment instanceof ProfitFragment) {
			ProfitFragment newFragment = (ProfitFragment) fragment;
			newFragment.printReport(true);
		} 
	}
}
