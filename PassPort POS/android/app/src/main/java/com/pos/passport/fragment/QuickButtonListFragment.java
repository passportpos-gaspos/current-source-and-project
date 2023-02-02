package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;

/**
 * Created by karim on 11/3/15.
 */
public class QuickButtonListFragment extends InventoryListFragment {
    private FragmentActivity mActivity;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] values = new String[] {"Quick Buttons"};

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.item_list, values);
        QuickButtonAdapter adapter = new QuickButtonAdapter(getActivity(), R.layout.item_list, values);
        setListAdapter(adapter);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (savedInstanceState != null) {
            mActivatedPosition = savedInstanceState.getInt("curChoice", 0);
        }

        ShowDetails(mActivatedPosition);
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mActivity = getActivity();
    }
    protected void ShowDetails(int position) {
        String item = (String) getListAdapter().getItem(position);
        mActivatedPosition = position;

        Fragment fragment = getFragmentManager().findFragmentById(R.id.inventory_details_fragment);
        switch (item) {
            case "Quick Buttons":
                if (!(fragment instanceof MenuButtonFragment)) {
                    MenuButtonFragment newFragment = new MenuButtonFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.inventory_details_fragment, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                }
                break;
        }
    }

    private class QuickButtonAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private @LayoutRes
        int resource;
        private String[] texts;

        public QuickButtonAdapter(Context context, int resource, String[] objects) {
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
}
