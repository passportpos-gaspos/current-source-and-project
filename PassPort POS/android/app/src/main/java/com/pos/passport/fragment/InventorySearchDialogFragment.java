package com.pos.passport.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.adapter.InventorySearchViewAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 2/11/2016.
 */
public class InventorySearchDialogFragment extends DialogFragment {
    private ProductDatabase mDb;
    private GridView mProductListView;
    private Spinner mDepartmentSpinner;
    private AutoCompleteTextView mSearchView;
    private InventorySearchViewAdapter itemAdapter;
    private List<Product> mItemProduct = new ArrayList<>();
    private SimpleCursorAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null)
            getDialog().setCanceledOnTouchOutside(true);
        View v = inflater.inflate(R.layout.inventory_view_item_list, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpListeners();
    }

    private void bindUIElements(View view) {
        mProductListView = (GridView) view.findViewById(R.id.item_list_view);
        mSearchView = (AutoCompleteTextView) view.findViewById(R.id.search_edit_text);
        mDepartmentSpinner = (Spinner) view.findViewById(R.id.department_spinner);
        mItemProduct = new ArrayList<>();
        mItemProduct = mDb.helper.getNamedProds("");
        itemAdapter = new InventorySearchViewAdapter(getActivity(), mItemProduct);
        mProductListView.setAdapter(itemAdapter);
        //registerForContextMenu(mProductListView);
        ArrayList<String> temp = new ArrayList<>();
        temp.add(0, getString(R.string.txt_products));
        temp.addAll(mDb.getCatagoryString());
        DepartmentAdapter adapter = new DepartmentAdapter(getContext(), R.layout.view_spiner, temp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDepartmentSpinner.setAdapter(adapter);
        mDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String search = "";
                if (mSearchView.getText().toString().length() > 2)
                {
                    search = mSearchView.getText().toString();
                }
                setSearchData(search);
                /*if (mDepartmentSpinner.getSelectedItemPosition() <= 0) {
                    if (mItemProduct.size() > 0)
                        mItemProduct.clear();

                    mItemProduct = mDb.helper.getNamedProds(search);
                    notifyList(mItemProduct);
                } else {
                    if (mItemProduct.size() > 0)
                        mItemProduct.clear();

                    mItemProduct = mDb.helper.getProdsNamesWithCat(search, (String) mDepartmentSpinner.getSelectedItem());
                    notifyList(mItemProduct);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        /*mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {

                    if (s.toString().length() > 2) {
                        setSearchData(s.toString());
                    } else if (s.toString().length() == 0) {
                        setSearchData("");
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });*/

        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, mDb.helper.fetchNamedProds(""),
                new String[]{"name", "barcode", "_id", "quantity"}, new int[]{android.R.id.text1}, 0);
        mSearchView.setAdapter(mAdapter);
        mSearchView.setThreshold(1);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                if (str != null && str.length() > 2)
                    return setSearchAutoComp(str.toString());
                return null;

                //return mDb.searchCustomers(str.toString());
            }
        });

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (cursor.getCount() > 0) {
                    StringBuffer item = new StringBuffer();
                    item.append(cursor.getString(cursor.getColumnIndex("name"))).append(", ")
                            .append(cursor.getString(cursor.getColumnIndex("barcode"))).append(", ")
                            .append(cursor.getString(cursor.getColumnIndex("quantity")));
                    ((TextView) view).setText(item.toString());
                    return true;
                }
                return false;
            }
        });
        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor c) {
                return c.getString(c.getColumnIndexOrThrow("name"));
            }
        });

    }

    private void setUpListeners() {
        mSearchView.setOnItemClickListener(mSearchViewIdAutoCompleteClick);

    }

    private AdapterView.OnItemClickListener mSearchViewIdAutoCompleteClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Cursor cursor = mAdapter.getCursor();
            Cursor productC = (Cursor) mSearchView.getAdapter().getItem(position);
            if (productC != null)
            {
                mSearchView.setText("" + productC.getString(productC.getColumnIndex("name")));
                setSearchData(""+productC.getString(productC.getColumnIndex("_id")));
            }

//            Customer customer = new Customer();
            /*customer.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            customer.setFName(cursor.getString(cursor.getColumnIndex("fname")));
            customer.setLName(cursor.getString(cursor.getColumnIndex("lname")));
            customer.setEmail(cursor.getString(cursor.getColumnIndex("email")));
            mFirstNameText.setText(customer.fName);
            mLastNameText.setText(customer.lName);
            mEmailIdAutoCompleteTextView.setText(customer.email);*/

            Utils.dismissKeyboard(view);
        }
    };

    public void setSearchData(String searchtext) {
        if (mItemProduct.size() > 0)
            mItemProduct.clear();

        if (mDepartmentSpinner.getSelectedItemPosition() <= 0)
        {
            mItemProduct = mDb.helper.getNamedProds(searchtext);
            notifyList(mItemProduct);
        } else
        {
            mItemProduct = mDb.helper.getProdsNamesWithCat(searchtext, (String) mDepartmentSpinner.getSelectedItem());
            notifyList(mItemProduct);
        }
    }

    public Cursor setSearchAutoComp(String searchtext) {
        Cursor mCursor = null;
        if (mDepartmentSpinner.getSelectedItemPosition() <= 0) {
            mCursor = mDb.helper.fetchNamedProdsLimit(searchtext);
            // notifyList(mItemProduct);
        } else {
            mCursor = mDb.helper.fetchNamedProdsLimit(searchtext, (String) mDepartmentSpinner.getSelectedItem());
            // notifyList(mItemProduct);
        }
        Log.e("Cursor count","setSearchAutoComp>>>"+mCursor.getCount());
        return mCursor;
    }

    public void notifyList(List<Product> mItemProduct) {
        itemAdapter = new InventorySearchViewAdapter(getActivity(), mItemProduct);
        mProductListView.setAdapter(itemAdapter);
        itemAdapter.notifyDataSetChanged();
    }

    private class DepartmentAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;
        private
        @LayoutRes
        int resource;
        private List<String> texts;

        public DepartmentAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.texts = objects;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"));
            //view.setPadding(10, 10, 10, 10);
            return view;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(resource, parent, false);
            }
            ((TextView) convertView).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"));
            ((TextView) convertView).setText(texts.get(position));


            return convertView;
        }
    }
}
