package com.passportsingle;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class InvButtonsFragment extends Fragment {
	private GridView buttonList;
	protected String catagory;
	private View mylayout;
	private AutoCompleteTextView autoProduct;
	private Bitmap Pimage;
	private int backParent;
	protected com.passportsingle.Button currentFolder;
	private ImageView image;
	private ArrayList<Integer> multiItems;
	private ArrayList<Product> multiProducts;
	private TableLayout itemTable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Fragment", "Buttons Fragment");
		setHasOptionsMenu(true);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.inv_buttons_fragment, container,
				false);

		buttonList = (GridView) view.findViewById(R.id.inv_buttons);
		ButtonAdaptor itemAdapter = new ButtonAdaptor(
				ProductDatabase.getButtons(0), getActivity());
		buttonList.setAdapter(itemAdapter);
		registerForContextMenu(buttonList);

		buttonList.setClickable(true);
		buttonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Cursor c = (Cursor) buttonList.getItemAtPosition(position);
				int newType = c.getInt(c.getColumnIndex("type"));
				int newParent = c.getInt(c.getColumnIndex("_id"));

				if(newType == 1)
				{
					currentFolder = ProductDatabase.getButtonByID(newParent);
					backParent = c.getInt(c.getColumnIndex("parent"));
					((ButtonAdaptor) buttonList.getAdapter()).changeCursor(ProductDatabase.getButtons(newParent));
				}
				
				if(newType == -1){
					((ButtonAdaptor) buttonList.getAdapter()).changeCursor(ProductDatabase.getButtons(currentFolder.parent));
					currentFolder = ProductDatabase.getButtonByID(currentFolder.parent);
				}
			}
		});
		
		return view;
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.button_menu, menu);	
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences mSharedPreferences;
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			return true;
		case R.id.menu_newbutton:
			mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
			
			if(lic){
				addButtonToDatabase();
			}else{
				alertbox("Unlicensed", "Please license this app to add new buttons. Thank you.");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);  
        menu.setHeaderTitle("Product Options");  
        menu.add(0, v.getId(), 0, "Edit Button");  
        menu.add(0, v.getId(), 0, "Delete Button");  
    } 
    
    @Override  
    public boolean onContextItemSelected(android.view.MenuItem item) {  
        if(item.getTitle()=="Edit Button"){
    		SharedPreferences mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
			
			if(lic){
				editButton(item);
			}else{
				alertbox("Unlicensed", "Please license this app to edit/delete buttons. Thank you.");
			}
        } 
        else if(item.getTitle()=="Delete Button")
        {
    		SharedPreferences mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
			boolean lic = mSharedPreferences.getBoolean("APOS_LICENSED", false); 
			
			if(lic){
				deleteButton(item);
			}else{
				alertbox("Unlicensed", "Please license this app to edit/delete buttons. Thank you.");
			}
        	
        }  
        else {return false;}  
    return true;  
    } 

	private void deleteButton(android.view.MenuItem item) {		
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 	
		Cursor c = (Cursor) buttonList.getItemAtPosition(info.position);
		int id = c.getInt(c.getColumnIndex("_id"));
		int type = c.getInt(c.getColumnIndex("type"));

		if(type == -1 || ProductDatabase.hasChildren(id))
		{
			if(type == -1)
			{
				alertbox("Can not delete", "Can not delete back button");
			}
			else
			{
				alertbox("Can not delete", "The button has children. Delete those first");
			}
		}
		
		else if(type == 6 && c.getString(c.getColumnIndex("folderName")).equals("Cloud Upgrade")){
			
			alertbox("Can not delete", "Can not delete the upgrade button");
		}
		else
		{
			ProductDatabase.deleteButton(id);
			((ButtonAdaptor) buttonList.getAdapter()).getCursor().requery();
		}
	}

	private void editButton(android.view.MenuItem item) {
		
    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Cursor c = (Cursor) buttonList.getItemAtPosition(info.position);
		
		final com.passportsingle.Button button = ProductDatabase.getButtonByID(c.getInt(c.getColumnIndex("_id")));
		
		if(button.folderName != null && button.folderName.equals(R.string.upgrade) && button.type == 6){
			
			alertbox("Can not", "Can not edit the upgrade button");
			return;
		}
		
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_button,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final Spinner type = (Spinner) mylayout.findViewById(R.id.typeSelect);
		final Spinner parent = (Spinner) mylayout.findViewById(R.id.parentSelect);
		final Spinner department = (Spinner) mylayout.findViewById(R.id.departmentSelect);
		final Spinner tender = (Spinner) mylayout.findViewById(R.id.tenderSelect);

		final EditText folder = (EditText) mylayout.findViewById(R.id.folderEdit);
		final EditText order = (EditText) mylayout.findViewById(R.id.orderByEdit);
		final TextView folderName = (TextView) mylayout.findViewById(R.id.folderName);

		image = (ImageView) mylayout.findViewById(R.id.button_image);
		itemTable = (TableLayout) mylayout.findViewById(R.id.itemTable);

		final TableRow folderNameRow = (TableRow) mylayout.findViewById(R.id.folderNameRow);
		final TableRow productSearchRow = (TableRow) mylayout.findViewById(R.id.productSearchRow);
		final TableRow departmentRow = (TableRow) mylayout.findViewById(R.id.departmentRow);
		final TableRow tenderTypeRow = (TableRow) mylayout.findViewById(R.id.tenderTypeRow);
		final TableRow multiItemRow = (TableRow) mylayout.findViewById(R.id.multiItemRow);

		autoProduct = (AutoCompleteTextView) mylayout.findViewById(R.id.productSearch);
		
        ItemAutoTextAdapter Searchadapter = this.new ItemAutoTextAdapter(getActivity());
        autoProduct.setAdapter(Searchadapter);
        autoProduct.setOnItemClickListener(Searchadapter);
		
        ArrayList<FolderList> departmentList = ProductDatabase.getDepartments();		
		
		ArrayList<String> tenders = new ArrayList<String>();
		
		tenders.add("Cash");
		tenders.add("Credit Card");
		tenders.add("Check");
		tenders.add("Custom");

		ArrayAdapter<String> tenderAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, tenders);
		tenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tender.setAdapter(tenderAdapter);
		
		tender.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {					
				String type = parent.getItemAtPosition(position).toString();
				if(type.equals("Custom")){
					folderNameRow.setVisibility(View.VISIBLE);
					folderName.setText("Tender Name:");
					folder.setHint("Tender Name:");

				}else{
					folderNameRow.setVisibility(View.GONE);
					folderName.setText("Folder Name:");	
					folder.setHint("Folder Name:");

		        	if(type.equals("Cash"))
		        		image.setImageResource(R.drawable.bill);
		        	else if(type.equals("Credit Card"))
		        		image.setImageResource(R.drawable.credit_card);
		        	else if(type.equals("Check"))
		        		image.setImageResource(R.drawable.check);
		        	else 
		        		image.setImageResource(0);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		
		ArrayAdapter<FolderList> departAdapter = new ArrayAdapter<FolderList>(getActivity(), R.layout.spiner, departmentList);
		departAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		department.setAdapter(departAdapter);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("Folder");
		list.add("Product");
		list.add("Multi Products");
		list.add("Department");
		list.add("Tender");
		list.add("External App");
		list.add("Text");

		ArrayList<FolderList> folders = ProductDatabase.getFolders();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		type.setAdapter(adapter);

		ArrayAdapter<FolderList> folderAdapter = new ArrayAdapter<FolderList>(getActivity(), R.layout.spiner, folders);
		folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		parent.setAdapter(folderAdapter);
		
		image.setClickable(true);
		image.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v)
	        {
	        	Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        	startActivityForResult(i, 3);
	        }
		});
		
		if(button.type == -1)
		{
			return;
		}
		
		if(button.type == 1){
			productSearchRow.setVisibility(View.GONE);
			folderNameRow.setVisibility(View.VISIBLE); 
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.GONE);
			multiItemRow.setVisibility(View.GONE);
			folderName.setText("Folder Name:");
			folder.setHint("Folder Name:");
			type.setSelection(0);
			image.setBackgroundResource(R.drawable.button_yellow_back);	
		}else if(button.type == 2){
			productSearchRow.setVisibility(View.VISIBLE);
			folderNameRow.setVisibility(View.GONE);
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.GONE);
			multiItemRow.setVisibility(View.GONE);
			type.setSelection(1);
			image.setBackgroundResource(R.drawable.button_gray_back);	
		}else if(button.type == 3){
			productSearchRow.setVisibility(View.GONE);
			folderNameRow.setVisibility(View.GONE); 
			departmentRow.setVisibility(View.VISIBLE);
			tenderTypeRow.setVisibility(View.GONE); 
			multiItemRow.setVisibility(View.GONE);
			type.setSelection(3);
			department.setSelection(0);
			
			for(int foID = 0; foID < department.getCount(); foID++)
			{
				FolderList idCheck = (FolderList) department.getItemAtPosition(foID);
				
				if(button.departID == idCheck.id)
				{
					department.setSelection(departAdapter.getPosition(idCheck));
					break;
				}
			}
			image.setBackgroundResource(R.drawable.button_blue_back);	
		}else if(button.type == 4){
			productSearchRow.setVisibility(View.GONE);
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.VISIBLE);
			multiItemRow.setVisibility(View.GONE);
			if(tenders.contains(button.folderName))
			{
				folderNameRow.setVisibility(View.GONE); 
				if(tenders.indexOf(button.folderName) > -1)
					tender.setSelection(tenders.indexOf(button.folderName));
				else
					tender.setSelection(0);
				
	        	if(button.folderName.equals("Cash"))
	        		image.setImageResource(R.drawable.bill);
	        	else if(button.folderName.equals("Credit Card"))
	        		image.setImageResource(R.drawable.credit_card);
	        	else if(button.folderName.equals("Check"))
	        		image.setImageResource(R.drawable.check);
			}else{
				tender.setSelection(3);
				folderNameRow.setVisibility(View.VISIBLE); 
				folderName.setText("Tender Name:");
				folder.setHint("Tender Name:");
			}
			type.setSelection(4);
			image.setBackgroundResource(R.drawable.button_green_back);	
		}else if(button.type == 5){
			productSearchRow.setVisibility(View.VISIBLE);
			multiItemRow.setVisibility(View.VISIBLE);
			folderNameRow.setVisibility(View.GONE);
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.GONE); 
			type.setSelection(2);
			folderName.setText("Display Name:");
			folder.setHint("Display Name:");
			folder.setText(button.folderName);
			multiProducts = getItemList(button.link);
			updateItems();
			image.setBackgroundResource(R.drawable.button_gray_back);	
		}else if(button.type == 6){
			productSearchRow.setVisibility(View.GONE);
			multiItemRow.setVisibility(View.GONE);
			folderNameRow.setVisibility(View.VISIBLE);
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.GONE); 
			folderName.setText("External App:");
			folder.setHint("Package Name: <com.example.app>");
			type.setSelection(5);
			image.setBackgroundResource(R.drawable.button_yellow_back);		
		}else if(button.type == 7){
			productSearchRow.setVisibility(View.GONE);
			folderNameRow.setVisibility(View.VISIBLE); 
			departmentRow.setVisibility(View.GONE);
			tenderTypeRow.setVisibility(View.GONE);
			multiItemRow.setVisibility(View.GONE);
			folderName.setText("Text:");
			folder.setHint("Text:");
			type.setSelection(6);
			image.setBackgroundResource(R.drawable.button_yellow_back);	
		}else{
			productSearchRow.setVisibility(View.GONE);
			folderNameRow.setVisibility(View.GONE);
			departmentRow.setVisibility(View.GONE);
			type.setSelection(1);
			image.setBackgroundResource(R.drawable.button_red_back);	
		}
		
		type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {					
				String type = parent.getItemAtPosition(position).toString();
				if(type.equals("Folder")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);
					folderName.setText("Folder Name:");
					folder.setHint("Folder Name:");
					image.setBackgroundResource(R.drawable.button_yellow_back);
				}else if(type.equals("Department")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.VISIBLE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					image.setBackgroundResource(R.drawable.button_blue_back);
				}else if(type.equals("Product")){
					productSearchRow.setVisibility(View.VISIBLE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					image.setBackgroundResource(R.drawable.button_gray_back);
				}else if(type.equals("Tender")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.VISIBLE);
					multiItemRow.setVisibility(View.GONE);
					folderName.setText("Tender Name:");
					folder.setHint("Tender Name:");
					image.setBackgroundResource(R.drawable.button_green_back);
				}else if(type.equals("Multi Products")){
					productSearchRow.setVisibility(View.VISIBLE);
					multiItemRow.setVisibility(View.VISIBLE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					folderName.setText("Display Name:");
					folder.setHint("Display Name:");
					multiProducts = getItemList(button.link);
					updateItems();
					image.setBackgroundResource(R.drawable.button_gray_back);
				}else if(type.equals("External App")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);
					folderName.setText("External App:");
					folder.setHint("Package Name: <com.example.app>");
					image.setBackgroundResource(R.drawable.button_yellow_back);		
				}else if(type.equals("Text")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);
					folderName.setText("Text:");
					folder.setHint("Text:");
					image.setBackgroundResource(R.drawable.button_yellow_back);
				}else{
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					image.setBackgroundResource(R.drawable.button_red_back);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		if(button.parent > 0)
		{	
			parent.setSelection(0);
		
			for(int foID = 0; foID < parent.getCount(); foID++)
			{
				FolderList idCheck = (FolderList) parent.getItemAtPosition(foID);
				
				if(button.parent == idCheck.id)
				{
					parent.setSelection(folderAdapter.getPosition(idCheck));
					break;
				}
			}
		}else{
			parent.setSelection(0);
		}
		
		order.setText(""+button.order);
		
		if(button.productID > 0)
		{
			autoProduct.setText(""+button.productID);
		}
		
		if(button.departID > 0)
		{
			department.setSelection(0);
			
			for(int foID = 0; foID < department.getCount(); foID++)
			{
				FolderList idCheck = (FolderList) department.getItemAtPosition(foID);
				
				if(button.departID == idCheck.id)
				{
					department.setSelection(departAdapter.getPosition(idCheck));
					break;
				}
			}
			//departmentFilter.setSelection(temp.indexOf(ProductDatabase.getCatById(button.departID)));
		}else{
			department.setSelection(0);
		}
		
		if(button.folderName != null && !button.folderName.equals(""))
		{
			folder.setText(button.folderName);
		}
				 
		if(button.image != null)
		{
			image.setImageBitmap(button.image);
		}
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setInverseBackgroundForced(true);
		builder.setView(mylayout)
				.setTitle("Edit Button In Database")
				.setPositiveButton("Edit Button",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String name = (String) type.getSelectedItem();
								int orderBy = Integer.valueOf(order.getText().toString());
								int buttontype = 0;
								int parentID = 0;
								int productID = 0;
								int departID = 0;
								int orderBBy = 0;
								String folderName = "";
								String Link = "";

								if(name.equals("Folder")){
									buttontype = 1;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("Product")){
									buttontype = 2;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									try {
										productID = Integer.valueOf(autoProduct
												.getText().toString());
									} catch (NumberFormatException nfe) {
										alertbox(
												"Insert Button Error",
												"Product field must be the product ID. When doing a search, click on the product in the dropdown.");
										return;
									}
								}else if(name.equals("Department")){
									buttontype = 3;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									departID = (int) ((FolderList)department.getSelectedItem()).id;
								}else if(name.equals("Tender")){
									buttontype = 4;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									if(((String)tender.getSelectedItem()).equals("Custom"))
									{
										folderName = folder.getText().toString();
										folderName = folderName.replaceAll(", ", " ");
										folderName = folderName.replaceAll(",", " ");
										folderName = folderName.replaceAll("\"", "");
									}else{
										folderName = (String)tender.getSelectedItem();
									}
								}else if(name.equals("Multi Products")){
									buttontype = 5;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									multiItems = new ArrayList<Integer>();
									for(int p = 0;p<multiProducts.size();p++){
										multiItems.add(new Integer(multiProducts.get(p).id));
									}
									Link = multiItems.toString();
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("External App")){
									buttontype = 6;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("Text")){
									buttontype = 7;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else{
									return;
								}
																					        	
								button.type = buttontype;
								button.parent = parentID;
								button.productID = productID;
								button.departID = departID;
								button.order = orderBy;
								button.folderName = folderName;
								button.link = Link;
								if(image.getDrawable() != null)
									button.image = ((BitmapDrawable)image.getDrawable()).getBitmap();
								
								ProductDatabase.saveButton(button);
								((ButtonAdaptor) buttonList.getAdapter()).getCursor().requery();
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

	}

	private void addButtonToDatabase() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;
		multiProducts = new ArrayList<Product>();
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mylayout = inflater.inflate(R.layout.add_db_button,(ViewGroup) getActivity().findViewById(R.id.mainLayout));

		final Spinner type = (Spinner) mylayout.findViewById(R.id.typeSelect);
		final Spinner parent = (Spinner) mylayout.findViewById(R.id.parentSelect);
		final Spinner department = (Spinner) mylayout.findViewById(R.id.departmentSelect);
		final Spinner tender = (Spinner) mylayout.findViewById(R.id.tenderSelect);

		final EditText folder = (EditText) mylayout.findViewById(R.id.folderEdit);
		final EditText order = (EditText) mylayout.findViewById(R.id.orderByEdit);
		final TextView folderName = (TextView) mylayout.findViewById(R.id.folderName);

		autoProduct = (AutoCompleteTextView) mylayout.findViewById(R.id.productSearch);
		
        ItemAutoTextAdapter Searchadapter = this.new ItemAutoTextAdapter(getActivity());
        autoProduct.setAdapter(Searchadapter);
        autoProduct.setOnItemClickListener(Searchadapter);

		final TableRow folderNameRow = (TableRow) mylayout.findViewById(R.id.folderNameRow);
		final TableRow productSearchRow = (TableRow) mylayout.findViewById(R.id.productSearchRow);
		final TableRow departmentRow = (TableRow) mylayout.findViewById(R.id.departmentRow);
		final TableRow tenderTypeRow = (TableRow) mylayout.findViewById(R.id.tenderTypeRow);
		final TableRow multiItemRow = (TableRow) mylayout.findViewById(R.id.multiItemRow);

		final AutoCompleteTextView product = (AutoCompleteTextView) mylayout.findViewById(R.id.productSearch);
		image = (ImageView) mylayout.findViewById(R.id.button_image);
		itemTable = (TableLayout) mylayout.findViewById(R.id.itemTable);

		ArrayList<String> list = new ArrayList<String>();
		list.add("Product");
		list.add("Folder");
		list.add("Multi Products");
		list.add("Department");
		list.add("Tender");
		list.add("External App");
		list.add("Text");

		ArrayList<FolderList> folders = ProductDatabase.getFolders();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		type.setAdapter(adapter);

		ArrayAdapter<FolderList> folderAdapter = new ArrayAdapter<FolderList>(getActivity(), R.layout.spiner, folders);
		folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		parent.setAdapter(folderAdapter);
		
		image.setClickable(true);
		image.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v)
	        {
	        	Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        	startActivityForResult(i, 3);
	        }
		});
		
		type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {					
				String type = parent.getItemAtPosition(position).toString();
				if(type.equals("Folder")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					folderName.setText("Folder Name:");
					folder.setHint("Folder Name:");
					image.setBackgroundResource(R.drawable.button_yellow_back);
				}else if(type.equals("Department")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.VISIBLE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					image.setBackgroundResource(R.drawable.button_blue_back);
				}else if(type.equals("Product")){
					productSearchRow.setVisibility(View.VISIBLE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					image.setBackgroundResource(R.drawable.button_gray_back);
				}else if(type.equals("Tender")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.VISIBLE);
					multiItemRow.setVisibility(View.GONE);

					image.setBackgroundResource(R.drawable.button_green_back);
					String tenderType = (String) tender.getSelectedItem();
					
		        	if(tenderType.equals("Cash"))
		        		image.setImageResource(R.drawable.bill);
		        	else if(tenderType.equals("Credit Card"))
		        		image.setImageResource(R.drawable.credit_card);
		        	else if(tenderType.equals("Check"))
		        		image.setImageResource(R.drawable.check);
		        	else 
		        		image.setImageResource(0); 
				}else if(type.equals("Multi Products")){
					productSearchRow.setVisibility(View.VISIBLE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.VISIBLE);
					folderName.setText("Display Name:");
					folder.setHint("Display Name:");
					updateItems();
					image.setBackgroundResource(R.drawable.button_gray_back);
				}else if(type.equals("External App")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);
					folderName.setText("External App:");
					folder.setHint("Package Name: <com.example.app>");
					image.setBackgroundResource(R.drawable.button_yellow_back);		
				}else if(type.equals("Text")){
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.VISIBLE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					multiItemRow.setVisibility(View.GONE);

					folderName.setText("Text:");
					folder.setHint("Enter Text:");
					image.setBackgroundResource(R.drawable.button_yellow_back);
				}else{
					productSearchRow.setVisibility(View.GONE);
					folderNameRow.setVisibility(View.GONE);
					departmentRow.setVisibility(View.GONE);
					tenderTypeRow.setVisibility(View.GONE);
					folderName.setText("Tender Name:");
					folder.setHint("Tender Name:");
					image.setBackgroundResource(R.drawable.button_red_back);	
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		ArrayList<FolderList> departmentList = ProductDatabase.getDepartments();
		
		ArrayAdapter<FolderList> departAdapter = new ArrayAdapter<FolderList>(getActivity(), R.layout.spiner, departmentList);
		departAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		department.setAdapter(departAdapter);
		
		ArrayList<String> tenders = new ArrayList<String>();
		
		tenders.add("Cash");
		tenders.add("Credit Card");
		tenders.add("Check");
		tenders.add("Custom");

		ArrayAdapter<String> tenderAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spiner, tenders);
		tenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tender.setAdapter(tenderAdapter);
		
		tender.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {					
				if(((String)type.getSelectedItem()).equals("Tender"))
				{
					String tenderType = parent.getItemAtPosition(position).toString();
					if(tenderType.equals("Custom")){
						folderNameRow.setVisibility(View.VISIBLE);
						folderName.setText("Tender Name:");
						folder.setHint("Tender Name:");
						image.setImageResource(0);
					}else{
						folderNameRow.setVisibility(View.GONE);
						folderName.setText("Folder Name:");
						folder.setHint("Folder Name:");
			        	if(tenderType.equals("Cash"))
			        		image.setImageResource(R.drawable.bill);
			        	else if(tenderType.equals("Credit Card"))
			        		image.setImageResource(R.drawable.credit_card);
			        	else if(tenderType.equals("Check"))
			        		image.setImageResource(R.drawable.check);
			        	else 
			        		image.setImageResource(0);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});


		if(currentFolder != null)
		{
			for(int foID = 0; foID < parent.getCount(); foID++)
			{
				FolderList idCheck = (FolderList) parent.getItemAtPosition(foID);
				
				if(currentFolder.folderName.equals(idCheck.name))
				{
					parent.setSelection(folderAdapter.getPosition(idCheck));
					break;
				}
			}
		} 
		
		builder = new AlertDialog.Builder(getActivity());
		builder.setInverseBackgroundForced(true);
		builder.setView(mylayout)
				.setTitle("Insert Button In Database")
				.setPositiveButton("Add Button",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String name = (String) type.getSelectedItem();
								int orderBy = Integer.valueOf(order.getText().toString());
								int buttontype = 0;
								int parentID = 0;
								int departID = 0;
								int productID = 0;
								String folderName = "";
								String Link = "";
								
								if(name.equals("Folder")){
									buttontype = 1;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("Product")){
									buttontype = 2;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									try {
										productID = Integer.valueOf(autoProduct
												.getText().toString());
									} catch (NumberFormatException nfe) {
										alertbox(
												"Insert Button Error",
												"Product field must be the product ID. When doing a search, click on the product in the dropdown.");
										return;
									}
								}else if(name.equals("Department")){
									buttontype = 3;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									departID = (int) ((FolderList)department.getSelectedItem()).id;
								}else if(name.equals("Tender")){
									buttontype = 4;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									if(((String)tender.getSelectedItem()).equals("Custom"))
									{
										folderName = folder.getText().toString();
										folderName = folderName.replaceAll(", ", " ");
										folderName = folderName.replaceAll(",", " ");
										folderName = folderName.replaceAll("\"", "");
									}else{
										folderName = (String)tender.getSelectedItem();
									}
								}else if(name.equals("Multi Products")){
									buttontype = 5;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									multiItems = new ArrayList<Integer>();
									for(int p = 0;p<multiProducts.size();p++){
										multiItems.add(new Integer(multiProducts.get(p).id));
									}
									Link = multiItems.toString();
									Log.v("items",Link);
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("External App")){
									buttontype = 6;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else if(name.equals("Text")){
									buttontype = 7;
									parentID = (int) ((FolderList)parent.getSelectedItem()).id;
									folderName = folder.getText().toString();
									productID = -1;
									folderName = folderName.replaceAll(", ", " ");
									folderName = folderName.replaceAll(",", " ");
									folderName = folderName.replaceAll("\"", "");					
								}else{
									return;
								}
													        	
								com.passportsingle.Button newbutton = new com.passportsingle.Button();
								newbutton.type = buttontype;
								newbutton.parent = parentID;
								newbutton.productID = productID;
								newbutton.departID = departID;
								newbutton.order = orderBy;
								newbutton.folderName = folderName;
								newbutton.link = Link;

								if(image.getDrawable() != null)
									newbutton.image = ((BitmapDrawable)image.getDrawable()).getBitmap();
								
								ProductDatabase.saveButton(newbutton);
								((ButtonAdaptor) buttonList.getAdapter()).getCursor().requery();
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
		
		order.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(((String) type.getSelectedItem()).equals("Folder"))
				{
					if (folder.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}
				}else if(((String) type.getSelectedItem()).equals("Product")){
					if (autoProduct.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}
				}else{
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(true);
				}
				
				if (!order.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
		});
		
		folder.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!folder.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}

				if (order.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
		});

		autoProduct.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!autoProduct.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}

				if (order.getText().toString().equals("")) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
							.setEnabled(false);
				}
				
				if(((String) type.getSelectedItem()).equals("Multi Products"))
				{
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == getActivity().RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            try
            {
            	DisplayMetrics metrics = getResources().getDisplayMetrics();
	        	
	        	int size = (int) Math.ceil(getResources().getDimension(R.dimen.button_corners));
	        	size = (int) (size / metrics.density);
	        	
            	Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeFile(picturePath), size);
                image.setImageBitmap(bitmap);
            }
            catch (OutOfMemoryError e)
            {
            	alertbox("Error", "Out of memory error. Image may be to large.");
            }
        }
    }
	
	class ItemAutoTextAdapter extends CursorAdapter
	implements android.widget.AdapterView.OnItemClickListener {

	    /**
	     * Constructor. Note that no cursor is needed when we create the
	     * adapter. Instead, cursors are created on demand when completions are
	     * needed for the field. (see
	     * {@link ItemAutoTextAdapter#runQueryOnBackgroundThread(CharSequence)}.)
	     *
	     * @param dbHelper
	     *            The AutoCompleteDbAdapter in use by the outer class
	     *            object.
	     */
	    public ItemAutoTextAdapter(Activity activity) {
	        super(activity, null);
	    }

	    /**
	     * Invoked by the AutoCompleteTextView field to get completions for the
	     * current input.
	     *
	     * NOTE: If this method either throws an exception or returns null, the
	     * Filter class that invokes it will log an error with the traceback,
	     * but otherwise ignore the problem. No choice list will be displayed.
	     * Watch those error logs!
	     *
	     * @param constraint
	     *            The input entered thus far. The resulting query will
	     *            search for Items whose description begins with this string.
	     * @return A Cursor that is positioned to the first row (if one exists)
	     *         and managed by the activity.
	     */
	    @Override
	    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
	        if (getFilterQueryProvider() != null) {
	            return getFilterQueryProvider().runQuery(constraint);
	        }

	        Cursor cursor = ProductDatabase.helper.fetchNamedProds(
	                (constraint != null ? constraint.toString() : "@@@@"));

	        return cursor;
	    }

	    /**
	     * Called by the AutoCompleteTextView field to get the text that will be
	     * entered in the field after a choice has been made.
	     *
	     * @param
	     *             cursor, positioned to a particular row in the list.
	     * @return A String representing the row's text value. (Note that this
	     *         specializes the base class return value for this method,
	     *         which is {@link CharSequence}.)
	     */
	    @Override
	    public String convertToString(Cursor cursor) {
	        final int id = cursor.getColumnIndexOrThrow("_id");
	        final int columnIndex = cursor.getColumnIndexOrThrow("name");
	        final String str = (cursor.getString(id) + cursor.getString(columnIndex));
	        return str;
	    }

	    /**
	     * Called by the ListView for the AutoCompleteTextView field to display
	     * the text for a particular choice in the list.
	     *
	     * @param view
	     *            The TextView used by the ListView to display a particular
	     *            choice.
	     * @param context
	     *            The context (Activity) to which this form belongs;
	     * @param cursor
	     *            The cursor for the list of choices, positioned to a
	     *            particular row.
	     */
	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	        //final String text = convertToString(cursor);
	        //((TextView) view).setText(text);
	        final int itemColumnIndex = cursor.getColumnIndexOrThrow("name");
	        final int descColumnIndex = cursor.getColumnIndexOrThrow("desc");
	        TextView text1 = (TextView) view.findViewById(R.id.text1);
	        text1.setText(cursor.getString(itemColumnIndex));
	        TextView text2 = (TextView) view.findViewById(R.id.text2);
	        text2.setText(cursor.getString(descColumnIndex));
	    }

	    /**
	     * Called by the AutoCompleteTextView field to display the text for a
	     * particular choice in the list.
	     *
	     * @param context
	     *            The context (Activity) to which this form belongs;
	      * @param cursor
	     *            The cursor for the list of choices, positioned to a
	     *            particular row.
	     * @param parent
	     *            The ListView that contains the list of choices.
	     *
	     * @return A new View (really, a TextView) to hold a particular choice.
	     */
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        final LayoutInflater inflater = LayoutInflater.from(context);
	        final View view = inflater.inflate(R.layout.product_button_listview, parent, false);
	        return view;
	    }

	    /**
	     * Called by the AutoCompleteTextView field when a choice has been made
	     * by the user.
	     *
	     * @param listView
	     *            The ListView containing the choices that were displayed to
	     *            the user.
	     * @param view
	     *            The field representing the selected choice
	     * @param position
	     *            The position of the choice within the list (0-based)
	     * @param id
	     *            The id of the row that was chosen (as provided by the _id
	     *            column in the cursor.)
	     */
	    @Override
	    public void onItemClick(AdapterView<?> listView, View view, int position, long id) 
	    {
	        Cursor cursor = (Cursor) listView.getItemAtPosition(position);
	        String itemNumber = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
	        String itemName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			Spinner type = (Spinner) mylayout.findViewById(R.id.typeSelect);

	        if(type.getSelectedItem().equals("Product"))
	        {
	        	autoProduct.setText(itemNumber);
	        }
	        else if(type.getSelectedItem().equals("Multi Products"))
	        {
	        	autoProduct.setText("");
	        	Product product = new Product();
	        	product.id = Integer.valueOf(itemNumber);
	        	product.name = itemName;       
	        	if(multiProducts == null)
	        		multiProducts = new ArrayList<Product>();
	        			
	        	multiProducts.add(product);
	        	updateItems();
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
	
	public void updateItems() {
		itemTable.removeAllViews();
		
		if(multiProducts.size()>0)
		{
			for(int p = 0; p < multiProducts.size(); p++)
			{
		    	TableRow row = new TableRow(getActivity());
		    	itemTable.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
						
				TextView tv1 = new TextView(getActivity());
				TextView tv2 = new TextView(getActivity());

				tv1.setText(multiProducts.get(p).name);
				tv1.setGravity(Gravity.LEFT);
				tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.5f));
				row.addView(tv1);
				
				String htmlString="<u>Remove Item</u>";
				tv2.setText(Html.fromHtml(htmlString));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
				final Product product = multiProducts.get(p);
				tv2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						multiProducts.remove(product);
						updateItems();
					}
				});
				
				row.addView(tv2);
			}
		}
	}

	public ArrayList<Product> getItemList(String itemString)
	{
		Log.v("items",itemString);
		String[] items = itemString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

		ArrayList<Product> results = new ArrayList<Product>();

		for (int i = 0; i < items.length; i++) {
			Log.v("items",items[i]);

			Cursor c = ProductDatabase.getProdByName(items[i]);

			if (c != null) {
				Product product = new Product();

				product.id = c.getInt(c.getColumnIndex("_id"));
				product.name = c.getString(c.getColumnIndex("name"));

				results.add(product);
			}
		}
		
		return results;
	}
	
	protected boolean isAppInstalled(String packageName) {
        Intent mIntent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }
}
