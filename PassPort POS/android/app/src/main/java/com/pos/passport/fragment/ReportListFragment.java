package com.pos.passport.fragment;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ReportListFragment extends ListFragment implements OnDateSetListener {

    private static String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    public static final int SHIFT_FRAGMENT = 0;
    public static final int HISTORY_FRAGMENT = 1;
    public static final int ITEM_SOLD_FRAGMENT = 2;
    public static final int SUMMARY_FRAGMENT = 3;
    public static final int MARGIN_FRAGMENT = 4;
    public static final int CUSTOMER_FRAGMENT = 5;
    public static final int CASHIER_FRAGMENT = 6;
    public static final int CREDIT_CARD_FRAGMENT = 7;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int getToFrom;

	private String from = "";
	private String to = "";

	private GregorianCalendar fromDate;
	private GregorianCalendar toDate;

	private Button mFromButton;
	private Button mToButton;
    private Button mPrintReport;
    private Button mBuildReport;
    private TextView lbl_name;
	private int mActivatedPosition = 0;
    Bundle savedInstanceState = null;


    public ProgressDialog prgDialog;



    private OnClickListener mPrintReportClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
            if (fragment instanceof ItemsSoldReportFragment) {
                ItemsSoldReportFragment newFragment = (ItemsSoldReportFragment) fragment;
                newFragment.printReport();
            }  else if (fragment instanceof MarginReportFragment) {
                MarginReportFragment newFragment = (MarginReportFragment) fragment;
                newFragment.printReport();
            } else if (fragment instanceof CustomerReportFragment) {
                CustomerReportFragment newFragment = (CustomerReportFragment) fragment;
                newFragment.printReport();
            }else if(fragment instanceof  ShiftReportFragment){
                ShiftReportFragment newFragment = (ShiftReportFragment) fragment;
                newFragment.endShiftClick();
            }else if(fragment instanceof CashierReportFragment) {
                CashierReportFragment newFragment = (CashierReportFragment) fragment;
                newFragment.printReport();
            }else if (fragment instanceof CreditCardReportFragment) {
                CreditCardReportFragment newFragment = (CreditCardReportFragment) fragment;
                newFragment.processSettlement();
            }
        }
    };

    private View.OnClickListener mFromButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            getToFrom = 0;

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment newFragment = new DatePickerDialogFragment(ReportListFragment.this);
            newFragment.show(ft, "dialog");
        }
    };

    private View.OnClickListener mToButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            getToFrom = 1;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment newFragment = new DatePickerDialogFragment(ReportListFragment.this);
            newFragment.show(ft, "dialog");
        }
    };

    private OnClickListener mBuildReportClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);
            if (fragment instanceof ShiftReportFragment) {
                ShiftReportFragment newFragment = (ShiftReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            } else if (fragment instanceof RecentTransactionListFragment) {
                RecentTransactionListFragment newFragment = (RecentTransactionListFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            } else if (fragment instanceof ItemsSoldReportFragment) {
                ItemsSoldReportFragment newFragment = (ItemsSoldReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            }else if (fragment instanceof SummaryReportFragment) {
                SummaryReportFragment newFragment = (SummaryReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            } else if (fragment instanceof MarginReportFragment) {
                MarginReportFragment newFragment = (MarginReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            } else if (fragment instanceof CustomerReportFragment) {
                CustomerReportFragment newFragment = (CustomerReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            } else if (fragment instanceof CashierReportFragment) {
                CashierReportFragment newFragment = (CashierReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            }else if (fragment instanceof CreditCardReportFragment) {
                CreditCardReportFragment newFragment = (CreditCardReportFragment) fragment;
                newFragment.refresh(fromDate.getTime().getTime(), toDate.getTime().getTime());
            }
        }
    };

    boolean isViewShownR = false;

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser)
//    {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(getView()!=null)
//        {
//            isViewShownR=true;
//            Log.e("Reportlistfragment", "setUserVisibleHint");
//            new AsyncTaskRunner().execute();
//        }
//        else{
//            isViewShownR=false;
//        }
//    }
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.e("ReportsListFragment","ReportsListFragment");
		getCurrentDate();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
		View view = inflater.inflate(R.layout.report_list, null);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		return view;
	}

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//    	super.onSaveInstanceState(outState);
//    	outState.putInt("curChoice", mActivatedPosition);
//    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
    {
		super.onActivityCreated(savedInstanceState);
        String[] values = getResources().getStringArray(R.array.reports_list);
        ReportAdapter adapter = new ReportAdapter(getActivity(), R.layout.item_list, values);
        setListAdapter(adapter);
        bindUIElements();
        setListeners();
        getListView().setSelector(R.drawable.list_selector);
        setUpUi();
    //    this.savedInstanceState=savedInstanceState;
        /*if (savedInstanceState != null) {
        	mActivatedPosition = savedInstanceState.getInt("curChoice", 0);
            showDetails(mActivatedPosition);
        } else {
			ShiftReportFragment newFragment = new ShiftReportFragment();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.report_Container, newFragment);
			transaction.commit();
			newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
            setButtons();
        }*/
        //if (!isViewShownR)
        //{
            Log.e("Reportlistfragment Main", "isViewShown CreateView");
            new AsyncTaskRunner().execute();
        //}
	}
    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //ShowProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {

            if (savedInstanceState != null) {
                mActivatedPosition = savedInstanceState.getInt("curChoice", 0);
                showDetails(mActivatedPosition);
            } else {
                ShiftReportFragment newFragment = new ShiftReportFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.report_Container, newFragment);
                transaction.commit();
                newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                setButtons();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            //DismissDialog();

        }
    }
//    public void ShowProgressDialog()
//    {
//        Log.e("Show prog","Show p");
//        prgDialog = new ProgressDialog(getActivity());
//        prgDialog.setMessage("Loading...");
//        prgDialog.setCancelable(false);
//        prgDialog.show();
//    }
//    public void DismissDialog()
//    {
//        Log.e("Dismis prog","dismiss p");
//        prgDialog.dismiss();
//    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
       super.onCreateOptionsMenu(menu, inflater);
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

    private void bindUIElements(){
        mFromButton = (Button) getActivity().findViewById(R.id.from_date_text_view);
        mToButton = (Button) getActivity().findViewById(R.id.to_date_text_view);
        mPrintReport = (Button) getActivity().findViewById(R.id.report_print_button);
        mBuildReport = (Button) getActivity().findViewById(R.id.submit_button);
        lbl_name=(TextView)getActivity().findViewById(R.id.lbl_name);
    }

    private void setListeners(){
        mPrintReport.setOnClickListener(mPrintReportClickListener);
        mBuildReport.setOnClickListener(mBuildReportClickListener);
        mFromButton.setOnClickListener(mFromButtonClickListener);
        mToButton.setOnClickListener(mToButtonClickListener);
    }

    private void setUpUi(){
        mFromButton.setText(from);
        mToButton.setText(to);
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(position);
        getActivity().supportInvalidateOptionsMenu();

	}
private void setText(String lbl)
{
    lbl_name.setText(lbl);
}
	private void showDetails(int position)
    {

		String item = (String) getListAdapter().getItem(position);
		mActivatedPosition = position;

		Fragment fragment = getFragmentManager().findFragmentById(R.id.report_Container);

		if (item.equals("End of Shift"))
        {
            setText("End of Shift");
			if (!(fragment instanceof ShiftReportFragment))
            {
                reset();
                ShiftReportFragment newFragment = new ShiftReportFragment();
				newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				transaction.commit();

			}
		}else if (item.equals("History"))
        {
            setText("History");
            if (!(fragment instanceof RecentTransactionListFragment)) {
                reset();
                RecentTransactionListFragment newFragment = new RecentTransactionListFragment();
                newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.report_Container, newFragment);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				transaction.commit();

			}
		} else if (item.equals("Items Sold"))
        {
            setText("Items Sold");
			if (!(fragment instanceof ItemsSoldReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports ){
                    //Log.e("Permission","if >>"+PrefUtils.getCashierInfo(getActivity()).permissionReports);
                    reset();
                    ItemsSoldReportFragment newFragment = new ItemsSoldReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();

                }else
                {
                   // Log.e("Permission","else>>"+PrefUtils.getCashierInfo(getActivity()).permissionReports);

                TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
				f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                    @Override
                    public void onAdminAccessGranted() {
                        reset();
                        ItemsSoldReportFragment newFragment = new ItemsSoldReportFragment();
                        newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.report_Container, newFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                    }

                    @Override
                    public void onAdminAccessDenied() {

                    }
                });
                }
			}
		} else if (item.equals("Summary"))
        {
            setText("Summary");
			if (!(fragment instanceof SummaryReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports ){
                    reset();
                    SummaryReportFragment newFragment = new SummaryReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();

                }else
                {

                    TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                        @Override
                        public void onAdminAccessGranted() {
                            reset();
                            SummaryReportFragment newFragment = new SummaryReportFragment();
                            newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.report_Container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.commit();
                        }

                        @Override
                        public void onAdminAccessDenied() {

                        }
                    });
                }
			}
		} else if (item.equals("Customer"))
        {
            setText("Customer");
			if (!(fragment instanceof CustomerReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports ){
                    reset();
                    CustomerReportFragment newFragment = new CustomerReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();

                }else
                {

                    TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                        @Override
                        public void onAdminAccessGranted() {
                            reset();
                            CustomerReportFragment newFragment = new CustomerReportFragment();
                            newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.report_Container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.commit();
                        }

                        @Override
                        public void onAdminAccessDenied() {

                        }
                    });
                }
			}
		} else if (item.equals("Margins"))
        {
            setText("Margins");
			if (!(fragment instanceof MarginReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports )
                {
                    reset();
                    MarginReportFragment newFragment = new MarginReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();


                }else
                {

                    TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                        @Override
                        public void onAdminAccessGranted() {
                            reset();
                            MarginReportFragment newFragment = new MarginReportFragment();
                            newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.report_Container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.commit();
                        }

                        @Override
                        public void onAdminAccessDenied() {

                        }
                    });
                }
			}
		}else if (item.equals("Cashier"))
        {
            setText("Cashier");
            if (!(fragment instanceof CashierReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports )
                {
                    reset();
                    CashierReportFragment newFragment = new CashierReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();

                }else
                {

                    TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                        @Override
                        public void onAdminAccessGranted() {
                            reset();
                            CashierReportFragment newFragment = new CashierReportFragment();
                            newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.report_Container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.commit();
                        }

                        @Override
                        public void onAdminAccessDenied() {

                        }
                    });
                }
            }
        }else if (item.equals("Credit Card"))
        {
            setText("Credit Card");
			if (!(fragment instanceof MercuryReportFragment))
            {
                if(PrefUtils.getCashierInfo(getActivity()).permissionReports )
                {
                    reset();
                    CreditCardReportFragment newFragment = new CreditCardReportFragment();
                    newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.report_Container, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();

                }else
                {

                    TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                    f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                    f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                        @Override
                        public void onAdminAccessGranted() {
                            reset();
                            CreditCardReportFragment newFragment = new CreditCardReportFragment();
                            newFragment.setDates(fromDate.getTime().getTime(), toDate.getTime().getTime());
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.report_Container, newFragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.commit();
                        }

                        @Override
                        public void onAdminAccessDenied() {

                        }
                    });
                }
			}
		}
	}

    @Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;
		GregorianCalendar date = new GregorianCalendar(mYear, mMonth, mDay);
		if (getToFrom == 0) {
			fromDate = date;
			fromDate.set(Calendar.HOUR_OF_DAY, 0);
			fromDate.set(Calendar.MINUTE, 0);
			fromDate.set(Calendar.SECOND, 0);
			from = DateFormat.getDateInstance().format(fromDate.getTime());
            mFromButton.setText(from);
		} else {
			toDate = date;
			toDate.set(Calendar.HOUR_OF_DAY, 23);
			toDate.set(Calendar.MINUTE, 59);
			toDate.set(Calendar.SECOND, 59);
			to = DateFormat.getDateInstance().format(toDate.getTime());
            mToButton.setText(to);
		}
	}

    private class ReportAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private @LayoutRes int resource;
        private String[] texts;

        public ReportAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.texts = objects;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(resource, parent, false);
            }
            ((TextView)convertView).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"));
            ((TextView)convertView).setText(texts[position]);
            return convertView;
        }
    }

    private void reset(){
        getCurrentDate();
        setButtons();
        setUpUi();
    }

    private void getCurrentDate(){
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
    }

    private void setButtons(){
        mPrintReport.setText(getString(R.string.txt_print));
        mPrintReport.setVisibility(View.VISIBLE);

        switch (mActivatedPosition){
            case SHIFT_FRAGMENT:
                mPrintReport.setText(getString(R.string.txt_end_shift));
                mPrintReport.setVisibility(View.VISIBLE);
                break;
            case HISTORY_FRAGMENT:
                mPrintReport.setVisibility(View.GONE);
                break;
            case ITEM_SOLD_FRAGMENT:
                mPrintReport.setVisibility(View.VISIBLE);
                break;
            case SUMMARY_FRAGMENT:
                mPrintReport.setVisibility(View.GONE);
                break;
            case MARGIN_FRAGMENT:
                mPrintReport.setVisibility(View.VISIBLE);
                break;
            case CUSTOMER_FRAGMENT:
                mPrintReport.setVisibility(View.VISIBLE);
                break;
            case CASHIER_FRAGMENT:
                mPrintReport.setVisibility(View.VISIBLE);
                break;
            case CREDIT_CARD_FRAGMENT:
                mPrintReport.setVisibility(View.VISIBLE);
                mPrintReport.setText(getString(R.string.txt_process_settlement));
                break;
        }
    }
}
