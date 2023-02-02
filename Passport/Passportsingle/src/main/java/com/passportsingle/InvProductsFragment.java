package com.passportsingle;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import com.passportsingle.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

public class InvProductsFragment extends Fragment implements OnDateSetListener {

	private ListView inventoryList;
	private PackageManager pm;
	private View mylayout;
	protected String catagory;
	//private EditText mBarcode;
	private Spinner departmentFilter;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Products Fragment");
		setHasOptionsMenu(true);
		pm = getActivity().getPackageManager();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.itemsview, container, false);

		inventoryList = (ListView) view.findViewById(R.id.listofitems);
		ItemAdaptor itemAdapter = new ItemAdaptor(
				ProductDatabase.helper.fetchNamedProds(""), getActivity()
						.getApplicationContext());
		inventoryList.setAdapter(itemAdapter);
		registerForContextMenu(inventoryList);
		
		departmentFilter = (Spinner) view.findViewById(R.id.departmentFilter);

		ArrayList<String> temp = new ArrayList<String>();
		
		temp.add(0, "All Departments");
		temp.addAll(ProductDatabase.getCatagoryString());		
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, temp);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		departmentFilter.setAdapter(adapter);

		departmentFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String search = "";
				if(searchBar != null)
				{
					search = searchBar.getText().toString();
				}else{
					
					search = searchViewText;
				}
				
				if(departmentFilter.getSelectedItemPosition() <= 0)
					
					((ItemAdaptor) inventoryList.getAdapter())
						.changeCursor(ProductDatabase.helper
								.fetchNamedProds(search));
				else
					((ItemAdaptor) inventoryList.getAdapter())
					.changeCursor(ProductDatabase.helper
							.fetchNamedProds(search, (String)departmentFilter.getSelectedItem()));
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
        IntentResult results = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

		//IntentResult results = IntentIntegratorSupportV4.parseActivityResult(requestCode, resultCode, intent);
        Log.d("Results", "scan result: " + results.getContents());

		if (results != null) {
			if (results.getContents() != null) {			
				EditText mBarcode = (EditText) mylayout.findViewById(R.id.barcode);
				mBarcode.setText(results.getContents());
			} else {
				alertbox("Scanner", "Bad scan.");
			}
		}
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(getActivity())
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	@SuppressLint({ "NewApi", "NewApi" })
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
			inflater.inflate(R.menu.inv_menu_tab, menu);

		    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			if (searchView!=null)
			{
				searchView.setOnQueryTextListener(
						new SearchView.OnQueryTextListener() {
							@Override
							public boolean onQueryTextChange(String newText) {
								Log.v("Products", "" + newText);
								searchViewText = newText;
								if(inventoryList != null)
									if(departmentFilter.getSelectedItemPosition() <= 0)
										((ItemAdaptor) inventoryList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedProds(newText));
									else
										((ItemAdaptor) inventoryList.getAdapter())
												.changeCursor(ProductDatabase.helper
														.fetchNamedProds(newText, (String)departmentFilter.getSelectedItem()));

								return true;
							}

							@SuppressLint({ "NewApi", "NewApi" })
							@Override
							public boolean onQueryTextSubmit(String query) {
								// TODO Auto-generated method stub
								return false;
							}
						});
				searchView.setIconifiedByDefault(false);
				searchView.setQuery("", true);
			}

		}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences mSharedPreferences;
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			return true;
		case R.id.menu_newitem:
			mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
			
			if(lic){
				addItemToDatabase(); 
			}else{
				alertbox("Unlicensed", "Please license this app to add new products. Thank you.");
			}
					
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addItemToDatabase() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_item,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

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

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, ProductDatabase.getCatagoryString());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				catagory = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		Button scan = (Button) mylayout.findViewById(R.id.scanButton);
		scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegratorSupportV4 integrator = new IntentIntegratorSupportV4(InvProductsFragment.this);
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
				DialogFragment newFragment = new DatePickerDialogFragment(InvProductsFragment.this);
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
				DialogFragment newFragment = new DatePickerDialogFragment(InvProductsFragment.this);
				newFragment.show(ft, "dialog");	
			}
		});
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setInverseBackgroundForced(true);
		builder.setView(mylayout)
				.setTitle("Insert Item In Database")
				.setPositiveButton("Add Item",
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
								newprod.price = Math.round(price*100f);
								newprod.cost = Math.round(Cost*100f);
								newprod.salePrice = Math.round(salePrice*100f);
								newprod.barcode = barcode;
								newprod.desc = desc;
								newprod.onHand = Quantity;
								newprod.lowAmount = Low;
								newprod.endSale = toDate.getTimeInMillis();
								newprod.startSale = fromDate.getTimeInMillis();
								
								if (catagory != null) {
									int cat = ProductDatabase.getCatId(catagory);
									newprod.cat = cat;
								}

								ProductDatabase.insert(newprod);
								// inventoryList.invalidate();
								// ((ItemAdaptor)
								// inventoryList.getAdapter()).changeCursor(ProductDatabase.helper.fetchNamedProds(""));
								((ItemAdaptor) inventoryList.getAdapter()).getCursor().requery();
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
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
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!mPriceEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}

				if (mNameEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}
			}
		});

		mPriceEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!mNameEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}

				if (mPriceEdit.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}
			}
		});
	}
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Product Options");  
        menu.add(0, v.getId(), 0, "Edit Product");  
        menu.add(0, v.getId(), 0, "Remove Product");  
    }  
	
    @Override  
    public boolean onContextItemSelected(android.view.MenuItem item) {  
        if(item.getTitle()=="Edit Product"){function1(item);}  
        else if(item.getTitle()=="Remove Product"){function2(item);}  
        else {return false;}  
    return true;  
    } 
    
    
    public void function1(android.view.MenuItem item){  
    	
    	AlertDialog.Builder builder;
    	final AlertDialog alertDialog;

    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
		Cursor c = (Cursor) inventoryList.getItemAtPosition(info.position);

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

		product.price = Long.valueOf(c.getString(c.getColumnIndex("price")));
		product.cost = Long.valueOf(c.getString(c.getColumnIndex("cost")));
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
		product.salePrice = c.getLong(c.getColumnIndex("salePrice"));
		product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
		product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));
		
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
				DialogFragment newFragment = new DatePickerDialogFragment(InvProductsFragment.this);
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
				DialogFragment newFragment = new DatePickerDialogFragment(InvProductsFragment.this);
				newFragment.show(ft, "dialog");	
			}
		});

		toDate = new GregorianCalendar(mYear, mMonth, mDay);
		fromDate = new GregorianCalendar(mYear, mMonth, mDay);

		toDate.setTimeInMillis(product.endSale);
		fromDate.setTimeInMillis(product.startSale);
		
    	mNameEdit.setText(product.name);
    	mDescEdit.setText(product.desc);
    	mPriceEdit.setText(""+product.price/100f);
    	mCostEdit.setText(""+product.cost/100f);
    	mQuantityEdit.setText(""+product.onHand);
    	mLowEdit.setText(""+product.lowAmount);
    	mBarcode.setText(product.barcode);
    	
		salePriceEdit.setText(""+product.salePrice/100f);
		saleEndEdit.setText(DateFormat.getDateInstance().format(product.endSale));
		saleStartEdit.setText(DateFormat.getDateInstance().format(product.startSale));

    	final int Pid = product.id;
    	
        final Spinner spinner = (Spinner) mylayout.findViewById(R.id.catagoryselect);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, ProductDatabase.getCatagoryString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        int catIndex = adapter.getPosition(ProductDatabase.getCatById(c.getInt(c.getColumnIndex("catid"))));
        
        if(catIndex > -1){
			spinner.setSelection(catIndex);
		}
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
					catagory = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
        
		Button scan = (Button) mylayout.findViewById(R.id.scanButton);
		scan.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
				IntentIntegratorSupportV4 integrator = new IntentIntegratorSupportV4(InvProductsFragment.this);
				integrator.initiateScan();	
		    }
		});
		Boolean hasFrontCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
		Boolean hasRearCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

		String device = android.os.Build.DEVICE;
			
		if ((hasFrontCamera == false && hasRearCamera == false) || device.contains("PlayBook")) {
			Log.v("Testing Camera", "Faled...");
			View scanRow = (View) mylayout.findViewById(R.id.scanRow);
			scanRow.setVisibility(View.GONE);
			scan.setEnabled(false);
		}
    	
    	builder = new AlertDialog.Builder(getActivity());
    	builder.setView(mylayout)
    	.setTitle("Edit Item In Database")
    	.setInverseBackgroundForced(true)
        .setPositiveButton("Finish Editing Item", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
	    	    String name = mNameEdit.getText().toString();
	    	    String desc = mDescEdit.getText().toString();
	    	    String barcode = mBarcode.getText().toString();
				int Quantity = 0;
				int Low = 0;

				if (!mQuantityEdit.getText().toString().equals(""))
					Quantity = Integer.valueOf(mQuantityEdit.getText().toString());
				
				if (!mLowEdit.getText().toString().equals(""))
					Low = Integer.valueOf(mLowEdit.getText().toString());

				float salePrice = 0;
				if (!salePriceEdit.getText().toString().equals(""))
					salePrice = Float.valueOf(salePriceEdit.getText().toString());
				
				float price = Float.valueOf(mPriceEdit.getText().toString());
	    	    
				float Cost = 0;
				if (!mCostEdit.getText().toString().equals(""))
					Cost = Float.valueOf(mCostEdit.getText().toString());
				
	        	name = name.replaceAll(", ", " ");
	        	name = name.replaceAll(",", " ");
	        	name = name.replaceAll("\"", "");

	        	desc = desc.replaceAll(", ", " ");
	        	desc = desc.replaceAll(",", " ");
	        	desc = desc.replaceAll("\"", "");
	    	    
	    	    Product newprod = new Product();

	    	    newprod.id = Pid;
	    	    newprod.name = name;
	    	    newprod.price = Math.round(price*100f);
	    	    newprod.salePrice = Math.round(salePrice*100f);
	    	    newprod.cost = Math.round(Cost*100f);
	    	    newprod.barcode = barcode;
	    	    newprod.onHand = Quantity;
	    	    newprod.lowAmount = Low;
	    	    newprod.desc = desc;
				newprod.endSale = toDate.getTimeInMillis();
				newprod.startSale = fromDate.getTimeInMillis();

	    	    if(catagory != null){
    	    	    int cat = ProductDatabase.getCatId(catagory);
    	    	    newprod.cat = cat;
	    	    }

	    	    ProductDatabase.replaceItem(newprod);
	    		((ItemAdaptor) inventoryList.getAdapter()).getCursor().requery();
            	dialog.cancel();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });

    	alertDialog = builder.create();
    	alertDialog.show();

    } 

    public void function2(android.view.MenuItem item){  
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 	
		Cursor c = (Cursor) inventoryList.getItemAtPosition(info.position);
		ProductDatabase.RemoveProduct(c.getInt(c.getColumnIndex("_id")));
		((ItemAdaptor) inventoryList.getAdapter()).getCursor().requery();
    } 
    
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
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


}
