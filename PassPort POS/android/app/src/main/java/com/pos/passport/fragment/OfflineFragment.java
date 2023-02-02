package com.pos.passport.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.OfflineStats;
import com.pos.passport.service.ForwardService;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by karim on 10/30/15.
 */
public class OfflineFragment extends Fragment {
    private TextView mTransactionsTextView;
    private TextView mTotalTextView;
    private TextView mOfflineTimeTextView;
    private TextView mCashTransactionTextView;
    private TextView mCashTotalTextView;
    private Button mSendBatchButton;
    private ProductDatabase mDb;

    private View.OnClickListener mSendBatchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ForwardService.class);
            getActivity().startService(intent);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offline, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup)view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void bindUIElements(View view) {
        mTransactionsTextView = (TextView)view.findViewById(R.id.transactions_text_view);
        mTotalTextView = (TextView)view.findViewById(R.id.dollar_value_text_view);
        mOfflineTimeTextView = (TextView) view.findViewById(R.id.time_offline_text_view);
        mCashTotalTextView = (TextView) view.findViewById(R.id.cash_dollar_value_text_view);
        mCashTransactionTextView = (TextView) view.findViewById(R.id.cash_transactions_text_view);
        mSendBatchButton = (Button)view.findViewById(R.id.send_batch_button);
    }

    private void setUpListeners() {
        mSendBatchButton.setOnClickListener(mSendBatchClickListener);
    }

    public void refresh() {
        OfflineStats stats = mDb.getOfflineStatistics();
        OfflineStats cashStats = mDb.getOfflineCashStatistics();
        mTransactionsTextView.setText(String.format("%d", stats.getNumOfTransactions()));
        mTotalTextView.setText(Utils.formatCurrency(new BigDecimal(stats.getTotal())));
        mCashTransactionTextView.setText(String.format("%d", cashStats.getNumOfTransactions()));
        mCashTotalTextView.setText(Utils.formatCurrency(new BigDecimal(cashStats.getTotal())));
        if(PrefUtils.getOfflineOption(getActivity()).isOffline()) {
            Map<String, Long> time = Utils.timeDifferenceInHours(PrefUtils.getOfflineOption(getActivity()).getTimestamp());
            mOfflineTimeTextView.setText(String.format("%d : %d : %d", time.get("hours"), time.get("minutes"),time.get("seconds")));
        }
    }
}
