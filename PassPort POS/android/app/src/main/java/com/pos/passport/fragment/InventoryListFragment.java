package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Utils;

public class InventoryListFragment extends ListFragment {
	protected int mActivatedPosition = 0;
    boolean isViewShown = false;

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser)
//    {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(getView()!=null)
//        {
//            isViewShown=true;
//            Log.e("Inventory Main", "setUserVisibleHint");
//            ShowDetails(mActivatedPosition);
//        }
//        else{
//            isViewShown=false;
//        }
//    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Inventory Main", "Create");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.report_list, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
		Log.e("Inventory Main", "CreateView");

		return view;
	}

    /*@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setSelector(R.drawable.list_selector);
    }*/

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setSelector(R.drawable.list_selector);
        String[] values = getResources().getStringArray(R.array.inventory_menu);
        InventoryAdapter adapter = new InventoryAdapter(getActivity(), R.layout.item_list, values);
        setListAdapter(adapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //if (savedInstanceState != null) {
        //    mActivatedPosition = savedInstanceState.getInt("curChoice", 0);
        //}
        //if(! isViewShown )
        //{
            Log.e("Inventory Main", "isViewShown CreateView");
            ShowDetails(mActivatedPosition);
       // }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//    	super.onSaveInstanceState(outState);
//		Log.v("Inventory Main", "SavedSate");
//
//    	outState.putInt("curChoice", mActivatedPosition);
//    }
    
//	@Override
//	public void onResume() {
//		Log.v("Inventory Main", "Resumed");
//		super.onResume();
//	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v("Inventory Main", "Click");

		ShowDetails(position);
	}
	
	
	protected void ShowDetails(int position)
    {

		String item = (String) getListAdapter().getItem(position);
		mActivatedPosition = position;
		
		Fragment fragment = getFragmentManager().findFragmentById(R.id.inventory_details_fragment);
        switch (item) {
            case "Products" :
                if (!(fragment instanceof ProductsFragment)) {
                    ProductsFragment newFragment = new ProductsFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.inventory_details_fragment, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                }
                break;

            case "Departments":
                if (!(fragment instanceof DepartmentsFragment)) {
                    DepartmentsFragment newFragment = new DepartmentsFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.inventory_details_fragment, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                }
                break;

            case "Low Inventory":
                if (!(fragment instanceof LowInventoryFragment)) {
                    LowInventoryFragment newFragment = new LowInventoryFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.inventory_details_fragment, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                }
                break;

            case "Import/Export":
                if (!(fragment instanceof InvImportExportFragment)) {
                    InvImportExportFragment newFragment = new InvImportExportFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.inventory_details_fragment, newFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                }
                break;
		}
	}

    private class InventoryAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private @LayoutRes
        int resource;
        private String[] texts;

        public InventoryAdapter(Context context, int resource, String[] objects) {
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
