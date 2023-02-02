package com.pos.passport.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.ReportCart;
import com.pos.passport.util.Utils;

import java.util.List;

/**
 * Created by karim on 2/10/16.
 */
public class SaleFragment extends Fragment {
    private ListView mListView;
    private SaleAdapter mAdapter;
    private List<ReportCart> mCarts;
    private ProductDatabase mDb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sale, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        mCarts = mDb.getAllTransactions();
        bindUIElements(view);
        setUpListView();
        refresh();
    }

    private void bindUIElements(View view)
    {
        mListView = (ListView)view.findViewById(R.id.sale_list_view);
    }

    private void setUpListView()
    {
        mAdapter = new SaleAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void refresh()
    {
        mCarts = mDb.getAllTransactions();
        mAdapter.notifyDataSetChanged();
    }

    private class SaleAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public SaleAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return mCarts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_stored_sale_item, parent, false);
                holder = new ViewHolder();
                holder.transactionNumberTextView = (TextView)convertView.findViewById(R.id.transaction_number_text_view);
                holder.totalTextView = (TextView)convertView.findViewById(R.id.total_text_view);
                holder.processedTextView = (TextView)convertView.findViewById(R.id.processed_text_view);
                holder.holdTextView = (TextView)convertView.findViewById(R.id.hold_text_view);
                holder.lineItemTextView = (TextView)convertView.findViewById(R.id.line_item_text_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            ReportCart cart = mCarts.get(position);
            holder.transactionNumberTextView.setText("" + cart.trans);
            holder.totalTextView.setText(Utils.formatCurrency(cart.mTotal));
            holder.processedTextView.setText("" + cart.processed);
            holder.holdTextView.setText("" + cart.mOnHold);
            holder.lineItemTextView.setText(cart.cartItems);
            return convertView;
        }

        class ViewHolder {
            TextView transactionNumberTextView;
            TextView totalTextView;
            TextView processedTextView;
            TextView holdTextView;
            TextView lineItemTextView;
        }
    }
}
