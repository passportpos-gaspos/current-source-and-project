package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ingenico.framework.iconnecttsi.RequestType;
import com.pos.passport.R;
import com.pos.passport.adapter.CreditListAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.task.IngenicoProcessSettlementAsyncTask;
import com.pos.passport.util.Utils;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by Kareem on 10/15/2016.
 */

public class CreditCardReportFragment extends Fragment {

    private FragmentActivity mActivity;
    private GregorianCalendar fromDate;
    private GregorianCalendar toDate;
    private ProductDatabase mDb;
    private Typeface mNotoSansRegular;
    private PackageManager pm;
    private ListView mCreditListView;
    private CreditListAdapter mAdapter;
    private Cursor mCursor = null;
    private long fromD;
    private long toD;
    int mSizeText=15;
    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Fragment", "Credit Card  Fragment");
        setHasOptionsMenu(true);
        pm = getActivity().getPackageManager();
        mNotoSansRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_credit_card, container, false);
        if(Utils.ResourceSize(getActivity()) == 0)
        {
            mSizeText=18;
        }else {
            mSizeText=22;
        }
        mCreditListView = (ListView) view.findViewById(R.id.credit_list_view);
        setUpDates();
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        //mCursor = mDb.getCreditSales(fromD,toD);
        SetUpdata();
       // mAdapter = new CreditListAdapter(getActivity(), mCursor);
       // mCreditListView.setAdapter(mAdapter);
    }

    public void refresh(long l, long m){
        mCursor = mDb.getCreditSales(l, m);
        mAdapter.notifyDataSetChanged();
    }
    public void SetUpdata()
    {
        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.txt_loading), true, false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {

                mCursor = mDb.getCreditSales(fromD,toD);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        mAdapter = new CreditListAdapter(getActivity(), mCursor);
                        mCreditListView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                    }
                });
            }
        });
        thread.start();
    }
    public void setDates(long l, long m) {
        this.fromD = l;
        this.toD = m;
    }

    public void setUpDates(){
        GregorianCalendar fromDate = (GregorianCalendar) Calendar.getInstance();
        fromDate.set(Calendar.HOUR_OF_DAY, 0);
        fromDate.set(Calendar.MINUTE, 0);
        fromDate.set(Calendar.SECOND, 0);

        GregorianCalendar toDate = (GregorianCalendar) Calendar.getInstance();
        toDate.set(Calendar.HOUR_OF_DAY, 23);
        toDate.set(Calendar.MINUTE, 59);
        toDate.set(Calendar.SECOND, 59);

        fromD = fromDate.getTime().getTime();
        toD = toDate.getTime().getTime();
    }

    public void processSettlement(){
        IngenicoProcessSettlementAsyncTask settlementAsyncTask = new IngenicoProcessSettlementAsyncTask(getContext());
        settlementAsyncTask.setListener(new IngenicoProcessSettlementAsyncTask.SettlementListener() {
            @Override
            public void onSuccess(final String title, final String message) {
                float offlineCreditSaleAmount = mDb.getOfflineCreditSales();
                final StringBuffer messageBuffer = new StringBuffer();
                messageBuffer.append(message).append('\n');
                messageBuffer.append("Total Offline Sales Amount: " + String.valueOf(offlineCreditSaleAmount));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.alertBox(getActivity(), title, messageBuffer.toString());
                    }
                });
                mDb.updateOfflineCreditSales();
            }

            @Override
            public void onFailure(final String title, final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.alertBox(getActivity(), title, message);
                    }
                });
            }
        });
        settlementAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new RequestType.Settlement());
    }
}
