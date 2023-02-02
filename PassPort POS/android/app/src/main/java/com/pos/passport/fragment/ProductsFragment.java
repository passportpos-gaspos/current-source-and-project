package com.pos.passport.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.adapter.ItemAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Product;
import com.pos.passport.util.Consts;
import com.pos.passport.util.IntentIntegrator;
import com.pos.passport.util.IntentIntegratorSupportV4;
import com.pos.passport.util.IntentResult;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ProductsFragment extends Fragment implements OnDateSetListener {
    private final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private GridView mProductListView;
    private PackageManager pm;
    private View mylayout;
    protected String catagory;
    //private EditText mBarcode;
    private Spinner mDepartmentSpinner;
    private EditText searchBar;
    private String searchViewText;
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    private GregorianCalendar date;
    protected Button editedButton;
    protected int getToFrom;
    private GregorianCalendar fromDate;
    private GregorianCalendar toDate;
    private ProductDatabase mDb;
    private Typeface mNotoSansBold;
    private AutoCompleteTextView mSearchView;
    private SimpleCursorAdapter mAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Fragment", "Products Fragment");
        setHasOptionsMenu(true);
        pm = getActivity().getPackageManager();

        mNotoSansBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Bold.ttf");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.view_item_list, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        mProductListView = (GridView) view.findViewById(R.id.item_list_view);
        mSearchView = (AutoCompleteTextView) view.findViewById(R.id.search_edit_text);
        mSearchView.setVisibility(View.VISIBLE);
        ItemAdapter itemAdapter = new ItemAdapter(mDb.helper.fetchNamedProds(""), getActivity());
        mProductListView.setAdapter(itemAdapter);
        registerForContextMenu(mProductListView);

        mDepartmentSpinner = (Spinner) view.findViewById(R.id.department_spinner);
        setUpListeners();
        ArrayList<String> temp = new ArrayList<>();

        temp.add(0, getString(R.string.txt_products));
        temp.addAll(mDb.getCatagoryString());

        DepartmentAdapter adapter = new DepartmentAdapter(getActivity(), R.layout.view_spiner, temp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDepartmentSpinner.setAdapter(adapter);

        mDepartmentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String search = "";
               /* if (searchBar != null) {
                    search = searchBar.getText().toString();
                } else {
                    search = searchViewText;
                }*/

                if (mDepartmentSpinner.getSelectedItemPosition() <= 0)
                    ((ItemAdapter) mProductListView.getAdapter()).changeCursor(mDb.helper.fetchNamedProds(search));
                else
                    ((ItemAdapter) mProductListView.getAdapter()).changeCursor(mDb.helper.fetchNamedProds(search, (String) mDepartmentSpinner.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

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
        return view;
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
            Utils.dismissKeyboard(view);
        }
    };
    public void setSearchData(String searchtext)
    {
        if (mDepartmentSpinner.getSelectedItemPosition() <= 0)
        {
            ((ItemAdapter) mProductListView.getAdapter()).changeCursor(mDb.helper.getProductsData(searchtext));
        } else
        {
            ((ItemAdapter) mProductListView.getAdapter()).changeCursor(mDb.helper.getProductsWithCat(searchtext, (String) mDepartmentSpinner.getSelectedItem()));
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult results = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (results != null) {
            Log.d("Results", "scan result: " + results.getContents());

            if (results.getContents() != null) {
                EditText mBarcode = (EditText) mylayout.findViewById(R.id.barcode);
                mBarcode.setText(results.getContents());
            } else {
                Utils.alertBox(getActivity(), R.string.txt_scanner, R.string.msg_bad_scan);
            }
        }
    }

//    @SuppressLint({"NewApi", "NewApi"})
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
//    {
//        menu.clear();
//        inflater.inflate(R.menu.inv_menu_tab, menu);
//        MenuItem item = menu.findItem(R.id.menu_search);
//
//        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem)
//            {
//                InventorySearchDialogFragment productSearchView = new InventorySearchDialogFragment();
//                productSearchView.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
//                return false;
//            }
//        });
//
//        /*MenuItem searchItem = menu.findItem(R.id.menu_search);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setOnQueryTextListener(
//                new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextChange(String newText) {
//                        Log.v("Products", "" + newText);
//                        searchViewText = newText;
//                        if (mProductListView != null)
//                            if (mDepartmentSpinner.getSelectedItemPosition() <= 0)
//                                ((ItemAdapter) mProductListView.getAdapter()).changeCursor(mDb.helper.fetchNamedProds(newText));
//                            else
//                                ((ItemAdapter) mProductListView.getAdapter())
//                                        .changeCursor(mDb.helper.fetchNamedProds(newText, (String) mDepartmentSpinner.getSelectedItem()));
//
//                        return true;
//                    }
//
//                    @SuppressLint({"NewApi", "NewApi"})
//                    @Override
//                    public boolean onQueryTextSubmit(String query) {
//                        return false;
//                    }
//                }
//        );*/
//        //searchView.setIconifiedByDefault(false);
//        //searchView.setQuery("", true);
//    }

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

    private void addItemToDatabase() {
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mylayout = inflater.inflate(R.layout.add_db_item, (ViewGroup) getActivity().findViewById(R.id.mainLayout));

        final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
        final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);
        final EditText mPriceEdit = (EditText) mylayout.findViewById(R.id.priceEdit);
        final EditText mCostEdit = (EditText) mylayout.findViewById(R.id.costEdit);
        final EditText mQuantityEdit = (EditText) mylayout.findViewById(R.id.quantityEdit);
        final EditText mLowEdit = (EditText) mylayout.findViewById(R.id.lowFilter);
        final EditText mBarcode = (EditText) mylayout.findViewById(R.id.barcode);
        final Spinner spinner = (Spinner) mylayout.findViewById(R.id.catagoryselect);
        final EditText salePriceEdit = (EditText) mylayout.findViewById(R.id.salePriceEdit);
        final Button saleEndEdit = (Button) mylayout.findViewById(R.id.saleEndEdit);
        final Button saleStartEdit = (Button) mylayout.findViewById(R.id.saleStartEdit);

        DepartmentAdapter adapter = new DepartmentAdapter(getActivity(), R.layout.view_spiner, mDb.getCatagoryString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catagory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button scan = (Button) mylayout.findViewById(R.id.scanButton);
        scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegratorSupportV4 integrator = new IntentIntegratorSupportV4(ProductsFragment.this);
                integrator.initiateScan();
            }
        });

        toDate = new GregorianCalendar(mYear, mMonth, mDay);
        fromDate = new GregorianCalendar(mYear, mMonth, mDay);

        toDate.setTimeInMillis(0);
        fromDate.setTimeInMillis(0);

        saleStartEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                c.setTime(fromDate.getTime());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                getToFrom = 0;
                editedButton = saleStartEdit;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = new DatePickerDialogFragment(ProductsFragment.this);
                newFragment.show(ft, "dialog");
            }
        });

        saleEndEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                c.setTime(toDate.getTime());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                getToFrom = 1;
                editedButton = saleEndEdit;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = new DatePickerDialogFragment(ProductsFragment.this);
                newFragment.show(ft, "dialog");
            }
        });

        builder = new AlertDialog.Builder(getActivity());
        builder.setView(mylayout)
                .setTitle(R.string.txt_insert_item_in_database)
                .setPositiveButton(R.string.txt_add_item,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String name = mNameEdit.getText().toString();
                                String desc = mDescEdit.getText().toString();
                                String barcode = mBarcode.getText().toString();

                                float price = Float.valueOf(mPriceEdit.getText().toString());
                                int Quantity = 0;
                                int Low = 0;

                                if (!mQuantityEdit.getText().toString().equals(""))
                                    Quantity = Integer.valueOf(mQuantityEdit.getText().toString());

                                if (!mLowEdit.getText().toString().equals(""))
                                    Low = Integer.valueOf(mLowEdit.getText().toString());

                                float Cost = 0;
                                if (!mCostEdit.getText().toString().equals(""))
                                    Cost = Float.valueOf(mCostEdit.getText().toString());

                                float salePrice = 0;
                                if (!salePriceEdit.getText().toString().equals(""))
                                    salePrice = Float.valueOf(salePriceEdit.getText().toString());


                                name = name.replaceAll(", ", " ");
                                name = name.replaceAll(",", " ");
                                name = name.replaceAll("\"", "");

                                desc = desc.replaceAll(", ", " ");
                                desc = desc.replaceAll(",", " ");
                                desc = desc.replaceAll("\"", "");

                                Product newprod = new Product();
                                newprod.name = name;
                                newprod.price = new BigDecimal(price * 100f);
                                newprod.cost = new BigDecimal(Cost * 100f);
                                newprod.salePrice = new BigDecimal(salePrice * 100f);
                                newprod.barcode = barcode;
                                newprod.desc = desc;
                                newprod.onHand = Quantity;
                                newprod.lowAmount = Low;
                                newprod.endSale = toDate.getTimeInMillis();
                                newprod.startSale = fromDate.getTimeInMillis();

                                if (catagory != null) {
                                    int cat = mDb.getCatId(catagory);
                                    newprod.cat = cat;
                                }

                                mDb.insert(newprod);
                                ((ItemAdapter) mProductListView.getAdapter()).getCursor().requery();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        mNameEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mPriceEdit.getText().toString().equals("")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                if (mNameEdit.getText().toString().equals("")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        mPriceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mNameEdit.getText().toString().equals("")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                if (mPriceEdit.getText().toString().equals("")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.txt_product_options);
        menu.add(0, v.getId(), 0, R.string.txt_view_product);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == getString(R.string.txt_view_product))
        {
            function1(item);
        } else {
            return false;
        }
        return true;
    }

    public void function1(MenuItem item) {
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;

        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        Cursor c = (Cursor) mProductListView.getItemAtPosition(info.position);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mylayout = inflater.inflate(R.layout.add_db_item, (ViewGroup) getActivity().findViewById(R.id.mainLayout));

        final EditText mNameEdit = (EditText) mylayout.findViewById(R.id.nameEdit);
        final EditText mDescEdit = (EditText) mylayout.findViewById(R.id.descEdit);
        final EditText mPriceEdit = (EditText) mylayout.findViewById(R.id.priceEdit);
        final EditText mCostEdit = (EditText) mylayout.findViewById(R.id.costEdit);
        final EditText mQuantityEdit = (EditText) mylayout.findViewById(R.id.quantityEdit);
        final EditText mLowEdit = (EditText) mylayout.findViewById(R.id.lowFilter);
        final EditText mBarcode = (EditText) mylayout.findViewById(R.id.barcode);
        final EditText salePriceEdit = (EditText) mylayout.findViewById(R.id.salePriceEdit);
        final Button saleEndEdit = (Button) mylayout.findViewById(R.id.saleEndEdit);
        final Button saleStartEdit = (Button) mylayout.findViewById(R.id.saleStartEdit);

        final Product product = new Product();

        product.price = new BigDecimal(c.getString(c.getColumnIndex("price")));
        product.cost = new BigDecimal(c.getString(c.getColumnIndex("cost")));
        product.id = c.getInt(c.getColumnIndex("_id"));
        product.barcode = (c.getString(c.getColumnIndex("barcode")));
        product.name = (c.getString(c.getColumnIndex("name")));
        product.desc = (c.getString(c.getColumnIndex("desc")));
        product.onHand = (c.getInt(c.getColumnIndex("quantity")));
        product.cat = (c.getInt(c.getColumnIndex("catid")));
        product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
        product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
        product.lastReceived = (c.getInt(c.getColumnIndex("lastReceived")));
        product.lowAmount = (c.getInt(c.getColumnIndex("lowAmount")));
        product.salePrice = new BigDecimal(c.getLong(c.getColumnIndex("salePrice")));
        product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
        product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));
        product.track = (c.getInt(c.getColumnIndex("track")) != 0);

        saleStartEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                c.setTime(new Date(product.startSale));
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                getToFrom = 0;
                editedButton = saleStartEdit;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = new DatePickerDialogFragment(ProductsFragment.this);
                newFragment.show(ft, "dialog");
            }
        });

        saleEndEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                c.setTime(new Date(product.endSale));
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                getToFrom = 1;
                editedButton = saleEndEdit;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = new DatePickerDialogFragment(ProductsFragment.this);
                newFragment.show(ft, "dialog");
            }
        });

        toDate = new GregorianCalendar(mYear, mMonth, mDay);
        fromDate = new GregorianCalendar(mYear, mMonth, mDay);

        toDate.setTimeInMillis(product.endSale);
        fromDate.setTimeInMillis(product.startSale);

        mNameEdit.setText(product.name);
        mDescEdit.setText(product.desc);
        mPriceEdit.setText("" + product.price.divide(Consts.HUNDRED));
        mCostEdit.setText("" + product.cost.divide(Consts.HUNDRED));
        if (product.track)
            mQuantityEdit.setText(" " + product.onHand);
        else
            mQuantityEdit.setText("---");
        mLowEdit.setText("" + product.lowAmount);
        mBarcode.setText(product.barcode);

        salePriceEdit.setText("" + product.salePrice.divide(Consts.HUNDRED));
        saleEndEdit.setText(DateFormat.getDateInstance().format(product.endSale));
        saleStartEdit.setText(DateFormat.getDateInstance().format(product.startSale));

        final int pid = product.id;

        final Spinner spinner = (Spinner) mylayout.findViewById(R.id.catagoryselect);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.view_spiner, mDb.getCatagoryString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int catIndex = adapter.getPosition(mDb.getCatById(c.getInt(c.getColumnIndex("catid"))));

        if (catIndex > -1) {
            spinner.setSelection(catIndex);
        }
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catagory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Button scan = (Button) mylayout.findViewById(R.id.scanButton);
        scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegratorSupportV4 integrator = new IntentIntegratorSupportV4(ProductsFragment.this);
                integrator.initiateScan();
            }
        });
        Boolean hasFrontCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        Boolean hasRearCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        String device = android.os.Build.DEVICE;

        if ((!hasFrontCamera && !hasRearCamera) || device.contains("PlayBook")) {
            Log.v("Testing Camera", "Faled...");
            View scanRow = (View) mylayout.findViewById(R.id.scanRow);
            scanRow.setVisibility(View.GONE);
            scan.setEnabled(false);
        }

        builder = new AlertDialog.Builder(getActivity());
        builder.setView(mylayout)
                .setTitle(R.string.txt_edit_item_in_database)
                .setPositiveButton(R.string.txt_finish_viewing_item, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void function2(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Cursor c = (Cursor) mProductListView.getItemAtPosition(info.position);
        mDb.RemoveProduct(c.getInt(c.getColumnIndex("_id")));
        ((ItemAdapter) mProductListView.getAdapter()).getCursor().requery();
    }

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
            editedButton.setText(DateFormat.getDateInstance().format(fromDate.getTime()));
        } else {
            toDate = date;
            toDate.set(Calendar.HOUR_OF_DAY, 23);
            toDate.set(Calendar.MINUTE, 59);
            toDate.set(Calendar.SECOND, 59);
            editedButton.setText(DateFormat.getDateInstance().format(toDate.getTime()));
        }
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
