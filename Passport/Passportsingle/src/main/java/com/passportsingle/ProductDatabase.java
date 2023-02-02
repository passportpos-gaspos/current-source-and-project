package com.passportsingle;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ProductDatabase {

	private static String[] ProductString; 
	
	private static ArrayList<Catagory> Catagories;
	private static ArrayList<String> CatagoryString;
	static ProductDatabaseHelper helper;

    private static final String TAG = "ProductDatabase";
    
    private static final String PRODUCT_TABLE = "products";
    private static final String CATAGORY_TABLE = "catagories";
    private static final String SALES_TABLE = "sales";
    private static final String TAX_TABLE = "taxes";
    private static final String ADMIN_TABLE = "adminsettings";
    private static final String STORE_TABLE = "storesettings";
    private static final String EMAIL_TABLE = "emailsettings";
    private static final String RECEIPT_TABLE = "receiptsettings";
    private static final String CUSTOMER_TABLE = "customers";
    private static final String CASHIER_TABLE = "cashiers";
    private static final String BUTTON_TABLE = "buttons";
    private static final String PRIORITY_TABLE = "priority";
    private static final String CCSALES_TABLE = "ccsales";
    private static final String SHIFT_TABLE = "endshift";
    private static final String DAY_TABLE = "endday";
    private static final String PRINTER_TYPES_TABLES = "printers";
    private static final String LOGGER_TABLE = "logger";

    private static final String DATABASE_NAME = "advantagePOSFinal.db";
    private static final int DATABASE_VERSION = 3;
	
    private static SQLiteDatabase db;
	private static Context context;
	private static boolean inDepartment;
	private static boolean inProducts;
    
	public ProductDatabase(Context context){

		ProductDatabase.context = context;
		
        helper = new ProductDatabaseHelper(context);
        
        if(getDb() == null){
        	setDb(helper.getWritableDatabase());
        }else{
        	if(!getDb().isOpen())
        		setDb(helper.getWritableDatabase());
        }
             
	    findCats();
	    buildCatString();
	}

	private static void buildCatString() {
		setCatagoryString(new ArrayList<String>());
    	for(int i=0; i<getCatagories().size(); i++){
    		Catagory item = getCatagories().get(i);
    		getCatagoryString().add(item.getName());
    	}		
	}

	private static void findCats() {
		setCatagories(new ArrayList<Catagory>());

        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
        	Catagory product = new Catagory();

            product.setName(c.getString(c.getColumnIndex("name")));
            product.setId(c.getInt(c.getColumnIndex("_id")));
            product.setTaxable1(c.getInt(c.getColumnIndex("tax1")) != 0);
            product.setTaxable2(c.getInt(c.getColumnIndex("tax2")) != 0);
            product.setTaxable3(c.getInt(c.getColumnIndex("tax3")) != 0);
            getCatagories().add(product);

            c.moveToNext();

            Log.d(TAG, "product.name = " + product.getName());
            Log.d(TAG, "product.id = " + product.getId());

        }

        c.close();
	}
	
    public static boolean insert(Product product) {
        ContentValues vals = new ContentValues();       
        
        vals.put("name", product.name);
        vals.put("desc", product.desc);
        vals.put("barcode", product.barcode);
        vals.put("price", product.price);
        vals.put("salePrice", product.salePrice);
        vals.put("saleEndDate", product.endSale);
        vals.put("saleStartDate", product.startSale);
        vals.put("cost", product.cost);
        vals.put("catid", product.cat);
        vals.put("quantity", product.onHand);
        vals.put("buttonID", product.buttonID);
        vals.put("quantity", product.onHand);
        vals.put("lastSold", product.lastSold);
        vals.put("lastReceived", product.lastReceived);
        vals.put("lowAmount", product.lowAmount);

        int at = (int) getDb().insert(PRODUCT_TABLE, null, vals);
        product.id = at;
                
        return true;
    }
    
   /* public static Cursor getProds(String[] fields) {
    	
        Cursor data = getDb().query("names", fields,
        		null, null, null, null, null);
    	
		return null;
    }*/
	
    /*public static ArrayList<Product> findAll() {
    	Products = new ArrayList<Product>();

        Cursor c = db.rawQuery("select * from " + PRODUCT_TABLE, null);
        c.moveToFirst();
        
        while (!c.isAfterLast()) {
        	Product product = new Product();

            product.setBarcode(c.getString(c.getColumnIndex("barcode")));
            product.setName(c.getString(c.getColumnIndex("name")));
            product.setDesc(c.getString(c.getColumnIndex("desc")));
            product.setPrice(c.getFloat(c.getColumnIndex("price")));
            product.setCat(c.getInt(c.getColumnIndex("catid")));
            product.setId(c.getInt(c.getColumnIndex("_id")));

            Products.add(product);
            c.moveToNext();

            Log.d(TAG, "product.barcode = " + product.getBarcode());
            Log.d(TAG, "product.name = " + product.getName());
            Log.d(TAG, "product.desc = " + product.getDesc());
            Log.d(TAG, "product.price = " + product.getPrice());
            Log.d(TAG, "product.catid = " + product.getCat());
        }

        c.close();
        return Products;
    }*/
    
    public Product findByBarcode(String barcode) {
        String[] cols = new String[] { "*"};  
    	Log.v("FIND", barcode);
        //Cursor c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE barcode LIKE '%"+barcode+"%'", null);
        //Cursor c = getDb().query(PRODUCT_TABLE, cols, "barcode LIKE '?", new String[] { barcode+"%" }, null, null, null);
        Cursor c = getDb().query(true, PRODUCT_TABLE, new String[] {"*"}, "barcode" + " like \"%" + barcode + "%\"", null, null, null, null, null);
	            
        if (!c.moveToFirst()) {
        	c.close();
            return null; // product not found
        }

        Product product = new Product();
                
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

        c.close();       
        return product;
    }
	
    static class ProductDatabaseHelper extends SQLiteOpenHelper {

        public ProductDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder sql = new StringBuilder();
            // @formatter:off
			sql.append("create table ").append(PRODUCT_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   barcode text,")
			.append("   version integer,")
			.append("   name text,")
			.append("   desc text,")
			.append("   catid integer,")
			.append("   isNote integer,")
			.append("   buttonID integer,")
			.append("   price integer,")
			.append("   salePrice integer,")
			.append("   saleEndDate integer,")
			.append("   saleStartDate integer,")
			.append("   cost integer,")
			.append("   lastSold integer,")
			.append("   lastReceived integer,")
			.append("   lowAmount integer,")
			.append("   quantity integer")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(CATAGORY_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   name text,")
			.append("   desc text,")
			.append("   tax1 integer,")
			.append("   tax2 integer,")
			.append("   tax3 integer")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(SALES_TABLE).append("(  ")
				.append("   _id integer primary key,")
				.append("   date text,")
				.append("   customer text,")
				.append("   cashierID integer,")
            	.append("   lineitems text,")
            	.append("   subtotal integer,")
            	.append("   trans integer,")
             	.append("   voided integer,")
                .append("   tax1 integer,")
                .append("   tax2 integer,")
                .append("   tax3 integer,")
                .append("   taxpercent1 float,")
                .append("   taxpercent2 float,")
                .append("   taxpercent3 float,")
                .append("   taxname1 text,")
                .append("   taxname2 text,")
                .append("   taxname3 text,")
                .append("   total integer,")
                .append("   paymentype text,")
                .append("   onHold integer,")
                .append("   holdName text")
                .append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(RECEIPT_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   enabled integer,")
			.append("   make integer,")
			.append("   type integer,")
			.append("   drawer integer,")
			.append("   name string,")
			.append("   display integer,")
			.append("   address text,")
			.append("   cutcode text,")
			.append("   drawercode text,")
			.append("   size text,")
			.append("   printername text,")
			.append("   printermodel text,")
			.append("   blurb text")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(TAX_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   name1 text,").append("   name2 text,").append("   name3 text,")
			.append("   tax1 float,").append("   tax2 float,").append("   tax3 float")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(STORE_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   name text,")
			.append("   address text,")
			.append("   phone text,")
			.append("   email text,")
			.append("   website text,")
			.append("   clearsale int,")
			.append("   currency text")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(BUTTON_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   type int,")
			.append("   orderBy int,")
			.append("   parent int,")
			.append("   productID int,")
			.append("   departID int,")
			.append("   folderName text,")
			.append("   link text,")
			.append("   image blob")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());
	
	String insert = "INSERT INTO buttons (type, orderBy, parent) VALUES ('-1','-1','-1')";
	db.execSQL(insert);
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(ADMIN_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   enabled integer,").append("   password text,")
			.append("   hint text").append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(EMAIL_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   enabled integer,")
			.append("   bookkeeper integer,")
			.append("   smtpserver text,")
			.append("   smtpport integer,").append("   smtpuser text,")
			.append("   smtppass text,").append("   smtpemail text,")
			.append("   smtpsubject text,").append("   blurb text")
			.append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(CUSTOMER_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   fname text,").append("   lname text,")
			.append("   phone text,").append("   street text,")
			.append("   city text,").append("   email text,")
			.append("   region text,").append("   postal text,")
			.append("   numsales integer,")
			.append("   numreturns integer,")
			.append("   total integer").append(")  ");

	// @formatter:on
	db.execSQL(sql.toString());

	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(PRIORITY_TABLE)
            	.append("(  ")
            	.append("   _id integer primary key,")
            	.append("   enabled integer,")
            	.append("   merchantID text,")
            	.append("   webServicePassword text,")
            	.append("   hostedMID text,")
            	.append("   hostedPass text,")
            	.append("   terminalName text")
            	.append(")  ");

	// @formatter:on
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(CCSALES_TABLE)
            	.append("(  ")
            	.append("   _id integer primary key,")
            	.append("   processed integer,")
            	.append("   saleid integer,")
            	.append("   request text,")
            	.append("   response text,")
            	.append("   invoice text,")
            	.append("   date text")
            	.append(")  ");

	// @formatter:on
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(SHIFT_TABLE)
            	.append("(  ")
            	.append("   _id integer primary key,")
            	.append("   start integer,")
            	.append("   end integer,")
            	.append("   note text")
            	.append(")  ");

	// @formatter:on
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	
	sql.append("create table ").append(LOGGER_TABLE)
			.append("( ")
			.append(" _id integer primary key ,")
			.append(" type text, ")
			.append(" log text )");
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(DAY_TABLE)
            	.append("(  ")
            	.append("   _id integer primary key,")
            	.append("   start integer,")
            	.append("   end integer,")
            	.append("   note text")
            	.append(")  ");

	// @formatter:on
	db.execSQL(sql.toString());
	
	sql = new StringBuilder();
	// @formatter:off
	sql.append("create table ").append(CASHIER_TABLE).append("(  ")
			.append("   _id integer primary key,")
			.append("   fname text,")
			.append("   lname text,")
			.append("   phone text,")
			.append("   street text,")
			.append("   city text,")
			.append("   email text,")
			.append("   region text,")
			.append("   postal text,")
			.append("   numsales integer,")
			.append("   numreturns integer,")
			.append("   pin text,")
			.append("   permissionReturn integer,")
			.append("   permissionPriceModify integer,")
			.append("   permissionReports integer,")
			.append("   permissionInventory integer,")
			.append("   permissionSettings integer,")
			.append("   total integer").append(")  ");
	// @formatter:on
	db.execSQL(sql.toString());	
	
	sql = new StringBuilder();
	sql.append("create table ").append(PRINTER_TYPES_TABLES).append("(  ")
			.append("   _id integer primary key,")
			.append("   PrinterType text,")
			.append("   PrinterModel text,")
			.append("   KickCode text,")
			.append("   CutterCode text").append(")  ");
	db.execSQL(sql.toString());
	
	insert = "INSERT INTO taxes (name1, tax1) VALUES ('TAX', 10)";
	db.execSQL(insert);
	
	insert = "INSERT INTO catagories (name, tax1) VALUES ('Dept 1', 1)";
	db.execSQL(insert);
	
	insert = "INSERT INTO catagories (name, tax1) VALUES ('Dept 2', 1)";
	db.execSQL(insert);
	
	insert = "INSERT INTO catagories (name) VALUES ('Dept 3')";
	db.execSQL(insert);
	
	insert = "INSERT INTO products (name, desc, barcode, price, cost, catid, quantity, lowAmount) VALUES ('Item 1', 'Item 1 in database', '123456', 250, 100, 1, 5, 2)";
	db.execSQL(insert);
	
	insert = "INSERT INTO products (name, desc, barcode, price, cost, catid, quantity, lowAmount) VALUES ('Item 2', 'Item 2 in database', '100001', 500, 200, 2, 5, 2)";
	db.execSQL(insert);
	
	insert = "INSERT INTO products (name, desc, barcode, price, cost, catid, quantity, lowAmount) VALUES ('Item 3', 'Item 3 in database', '', 750, 250, 3, 5, 2)";
	db.execSQL(insert);
	
	insert = "INSERT INTO products (name, desc, barcode, price, cost, catid, quantity, lowAmount) VALUES ('Item 4', 'Item 4 in database', '4567821904', 399, 125, 2, 5, 2)";
	db.execSQL(insert);
	
	insert = "INSERT INTO products (name, desc, barcode, price, cost, catid, quantity, lowAmount) VALUES ('Item 5', 'Item 5 in database', 'abcdefg', 2499, 1300, 1, 5, 2)";
	db.execSQL(insert);
		
	insert = "INSERT INTO buttons (type, orderBy, parent, productID) VALUES ('2','1','0','1')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, productID) VALUES ('2','1','0','2')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, productID) VALUES ('2','1','0','3')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, departID) VALUES ('3','2','0','1')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, departID) VALUES ('3','2','0','2')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, departID) VALUES ('3','2','0','3')";
	db.execSQL(insert);
		
	insert = "INSERT INTO buttons (type, orderBy, parent, folderName) VALUES ('4','99','0','Cash')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, folderName) VALUES ('4','99','0','Check')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, folderName) VALUES ('4','99','0','Credit Card')";
	db.execSQL(insert);
	
	insert = "INSERT INTO buttons (type, orderBy, parent, folderName) VALUES('6', '99', '0', 'Upgrade')";
	db.execSQL(insert);
	
	
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if(oldVersion < 2){
	            try {
	                db.execSQL("ALTER TABLE " + PRIORITY_TABLE + " ADD COLUMN hostedPass");
	            } catch (SQLException e) {
	                Log.i("ADD COLUMN cutcode", "hostedPass already exists");
	            }
	            
	            try {
	                db.execSQL("ALTER TABLE " + PRIORITY_TABLE + " ADD COLUMN hostedMID");
	            } catch (SQLException e) {
	                Log.i("ADD COLUMN drawercode", "hostedMID already exists");
	            }
        	}
        	
        	if(oldVersion < 3){
	            try {
	                db.execSQL("ALTER TABLE " + CCSALES_TABLE + " ADD COLUMN saleid");
	            } catch (SQLException e) {
	                Log.i("ADD COLUMN cutcode", "saleid already exists");
	            }
        	}
        }
        
        public Cursor fetchItemsQuantity(String inputText) throws SQLException{
        	
        	Log.v("product text", inputText);
        	Cursor mCursor = null;
        	if(inputText.length() > 2){
        		
        		mCursor = getDb().query(true, PRODUCT_TABLE, new String[] {"name","quantity"}, "name like '" + inputText + "%' OR barcode like '"+ inputText + "%'", null,
	                    null, null,"name", null);
        		if (mCursor != null) {
        			mCursor.moveToNext();
        		}
        		
        	}
        	return mCursor;
        }
        
        public String[] fetchItemsByName(String inputText) throws SQLException {
        	
        	String formatedText = inputText.replaceAll("'", "''"); 

        	Log.v("text", formatedText);
        	
        	
        	if(formatedText.length() > 1)
        	{
	            Cursor mCursor = getDb().query(true, PRODUCT_TABLE, new String[] {"*"}, "name" + " like '%" + formatedText + "%' OR barcode like '%"+ formatedText + "%'", null,
	                    null, null, null, null);
	            if (mCursor != null) {
	                mCursor.moveToFirst();
	                
	                ProductString = new String[mCursor.getCount()];
	                int i = 0;
	                while (!mCursor.isAfterLast()) {	
	                	
	                	if(mCursor.getString(mCursor.getColumnIndex("barcode")).equals(""))
	                	{
	                		ProductString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " + mCursor.getString(mCursor.getColumnIndex("name")));
	                	}else{
	                		ProductString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " + mCursor.getString(mCursor.getColumnIndex("name")) + ", " + mCursor.getString(mCursor.getColumnIndex("barcode")));
	                	}
	                	i++;
	                	mCursor.moveToNext();
	                }
	                
	                mCursor.close();
	            }else{
	            	return null;
	            }
        	}else{
        		return null;
        	}
            
            return ProductString;
        }

		public Cursor fetchProds() throws SQLException {
			Cursor mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}
		
		public Cursor fetchNamedProds(String inputText) throws SQLException {
			Cursor mCursor = null;
						
			if(inputText != null && inputText.length() > 0){
	        	String formatedText = inputText.replaceAll("'", "''"); 

	            mCursor = getDb().query(true, PRODUCT_TABLE, new String[] {"*"}, "name" + " like '%" + formatedText + "%'", null,
	                    null, null, null, null);
			}else{            
	            mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE, null);
			}

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

		public Cursor fetchNamedCat(String inputText) {
			Cursor mCursor = null;
						
			if(inputText != null && inputText.length() > 0){
	        	String formatedText = inputText.replaceAll("'", "''"); 

	            mCursor = getDb().query(true, CATAGORY_TABLE, new String[] {"*"}, "name" + " like '%" + formatedText + "%'", null,
	                    null, null, null, null);
			}else{
	            mCursor = getDb().rawQuery("select * from " + CATAGORY_TABLE, null);
			}

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

		public Cursor fetchLowProds(String inputText) {
			Cursor mCursor = null;
			
			Log.v("Products", inputText);
			
	        mCursor = getDb().query(true, PRODUCT_TABLE, new String[] {"*"}, "quantity <= lowAmount" , null,
	                    null, null, null, null);

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			
			return mCursor;
		}

		public Cursor fetchNamedProds(String inputText, String selectedItem) {
			Cursor mCursor = null;
					
			int depID = getCatId(selectedItem);
			
			if(inputText.length()<1){
				mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE + " where catid =" + depID, null);
			}else{
	        	String formatedText = inputText.replaceAll("'", "''"); 

	            mCursor = getDb().query(true, PRODUCT_TABLE, new String[] {"*"}, "name" + " like '%" + formatedText + "%' AND catid = " + depID, null,
	                    null, null, null, null);
			}

			if (mCursor != null) {
				mCursor.moveToFirst(); 
			}
			
			return mCursor;
		}


    }
    
	public static Cursor fetchCustomers(String inputText) {
		Cursor mCursor = null;
		
		Log.v("Customers", inputText);
		
		if(inputText.length()<1){
			mCursor = getDb().rawQuery("select * from " + CUSTOMER_TABLE, null);
		}else{
        	String formatedText = inputText.replaceAll("'", "''"); 
        	
            mCursor = getDb().query(true, CUSTOMER_TABLE, new String[] {"*"}, "_id = "+formatedText, null,
                    null, null, null, null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public static void RemoveProduct(int position) {
		getDb().delete(PRODUCT_TABLE, "_id=" + position, null);
		getDb().delete(BUTTON_TABLE, "productID=" + position, null);
	}

	public static void setCatagoryString(ArrayList<String> catagoryString) {
		CatagoryString = catagoryString;
	}

	public static ArrayList<String> getCatagoryString() {
		return CatagoryString;
	}

	public static void setCatagories(ArrayList<Catagory> catagories) {
		Catagories = catagories;
	}

	public static ArrayList<Catagory> getCatagories() {
		return Catagories;
	}

	public static boolean insertCat(Catagory newprod) {
        ContentValues vals = new ContentValues();
        vals.put("name", newprod.getName());
        vals.put("tax1", newprod.getTaxable1());
        vals.put("tax2", newprod.getTaxable2());
        vals.put("tax3", newprod.getTaxable3());

        newprod.setId((int) getDb().insert(CATAGORY_TABLE, null, vals));
        Catagories.add(newprod);
        CatagoryString.add(newprod.getName());
        return true;		
	}
	
	public boolean insertTax() {
        ContentValues vals = new ContentValues();
        
        vals.put("name1", TaxSetting.getTax1name());
        vals.put("name2", TaxSetting.getTax2name());
        vals.put("name3", TaxSetting.getTax3name());

        vals.put("tax1", TaxSetting.getTax1());
        vals.put("tax2", TaxSetting.getTax2());
        vals.put("tax3", TaxSetting.getTax3());

        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE, null);
        
		if(c.getCount()>0){
	        getDb().update(TAX_TABLE, vals, "_id=1", null);
		}else{
	        getDb().insert(TAX_TABLE, null, vals);
		}
        
		c.close();
        return true;		
	}
	
    public void findTax() {
    	TaxSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE, null);
        c.moveToFirst();
        
        while (!c.isAfterLast()) {

            TaxSetting.setTax1name(c.getString(c.getColumnIndex("name1")));
            TaxSetting.setTax2name(c.getString(c.getColumnIndex("name2")));
            TaxSetting.setTax3name(c.getString(c.getColumnIndex("name3")));

            TaxSetting.setTax1(c.getFloat(c.getColumnIndex("tax1")));
            TaxSetting.setTax2(c.getFloat(c.getColumnIndex("tax2")));
            TaxSetting.setTax3(c.getFloat(c.getColumnIndex("tax3")));

            c.moveToNext();
        }
        c.close();
    }
	
	public int getProdId(String prod_name) {
        
		int result = 0;
        String[] cols = new String[] {"_id", "name"};        
        Cursor c = getDb().query(PRODUCT_TABLE, cols, "name = ?", new String[] { prod_name }, null, null, null);
        if(c.moveToFirst()){
        	result =  c.getInt(c.getColumnIndex("_id"));
        }
        c.close(); 
		
		return result;
	}

	public static int getCatId(String catagory) {
        
		int result = 0;
        String[] cols = new String[] { "_id", "name" };        
        Cursor c = getDb().query(CATAGORY_TABLE, cols, "name = ?", new String[] { catagory }, null, null, null);
        if(c.moveToFirst()){
        	result =  c.getInt(c.getColumnIndex("_id"));
        }
        c.close(); 
		
		return result;
	}

	public static String getCatById(int cat) {
		String result = null;
        String[] cols = new String[] { "_id", "name" };        
        Cursor c = getDb().query(CATAGORY_TABLE, cols, "_id = ?", new String[] { ""+cat }, null, null, null);
        if(c.moveToFirst()){
        	result =  c.getString(c.getColumnIndex("name"));
        }
        c.close(); 	
         
        if(result == null)
        	result = "No Department";
        return result;
	}

	public static void RemoveCatagory(int position, int position2) {
		getDb().delete(CATAGORY_TABLE, "_id=" + position, null);
	    findCats();
	    buildCatString();	
	}

	public static void setCat(Catagory newprod) {
		
        ContentValues vals = new ContentValues();
        vals.put("name", newprod.getName());
        vals.put("tax1", newprod.getTaxable1());
        vals.put("tax2", newprod.getTaxable2());
        vals.put("tax3", newprod.getTaxable3());

        getDb().update(CATAGORY_TABLE, vals, "_id=" + newprod.getId(), null);
        
	    findCats();
	    buildCatString();		
	}

	public static void replaceItem(Product newprod) {
        ContentValues vals = new ContentValues();
        vals.put("name", newprod.name);
        vals.put("desc", newprod.desc);
        vals.put("barcode", newprod.barcode);
        vals.put("price", newprod.price);
        vals.put("salePrice", newprod.salePrice);
        vals.put("saleEndDate", newprod.endSale);
        vals.put("saleStartDate", newprod.startSale);
        vals.put("cost", newprod.cost);
        vals.put("catid", newprod.cat);
        vals.put("quantity", newprod.onHand);
        vals.put("buttonID", newprod.buttonID);
        vals.put("quantity", newprod.onHand);
        vals.put("lastSold", newprod.lastSold);
        vals.put("lastReceived", newprod.lastReceived);
        vals.put("lowAmount", newprod.lowAmount);

        getDb().update(PRODUCT_TABLE, vals, "_id=" + newprod.id, null);
	}
	
	public void insertSale(ShopCart cart, String result) {
        ContentValues vals = new ContentValues();
        
        long currentDateTime = new Date().getTime();
        cart.date = currentDateTime;
        
        vals.put("date", cart.date);
        
        vals.put("trans", cart.trans);

        vals.put("lineitems", result);
        vals.put("subtotal", cart.subTotal);
        vals.put("tax1", cart.tax1);
        vals.put("tax2", cart.tax2);
        vals.put("tax3", cart.tax3);
        
        if(cart.voided)
        	vals.put("voided", 1);
        else
        	vals.put("voided", 0);

        vals.put("holdName", cart.name); 

        if(cart.onHold)
            vals.put("onHold", 1);
        else
            vals.put("onHold", 0);

        if(cart.Cashier != null)
        	vals.put("cashierID", cart.Cashier.id);
        else
        	vals.put("cashierID", 0);
        
        vals.put("taxpercent1", cart.taxPercent1);
        vals.put("taxpercent2", cart.taxPercent2);
        vals.put("taxpercent3", cart.taxPercent3);
        vals.put("taxname1", cart.taxName1);
        vals.put("taxname2", cart.taxName2);
        vals.put("taxname3", cart.taxName3);
        vals.put("total", cart.total);
        
        if(cart.hasCustomer())
        	vals.put("customer", cart.getCustomer().id);
        else
        	vals.put("customer", 0);

        Cursor c = getDb().rawQuery("select date from " + SALES_TABLE +" WHERE _id ="+cart.id, null);
        int count = c.getCount();
        c.close();
        
		if(count>0){
	        getDb().update(SALES_TABLE, vals, "_id="+cart.id, null);
		}else{
			cart.id = getDb().insert(SALES_TABLE, null, vals);
		}	
	}

	public static ArrayList<ReportCart> getReports1(long l, long m) {
		ArrayList<ReportCart> cart = new ArrayList<ReportCart>();

		String[] cols = new String[] { "*" };
		final String whereClause = "date >= " + l + " and date <= " + m
				+ " and onHold != 1";

		Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			ReportCart cartReport = new ReportCart();

			cartReport.setCartItems(c.getString(c.getColumnIndex("lineitems")));
			cartReport.extractXML();

			cartReport.setId(c.getString(c.getColumnIndex("_id")));
			cartReport.setDate(c.getLong(c.getColumnIndex("date")));

			cartReport.setCashierID(c.getInt(c.getColumnIndex("cashierID")));

			cartReport.trans = (c.getInt(c.getColumnIndex("trans")));
			
			if(cartReport.trans == 0)
			{
				cartReport.trans = Integer.valueOf(cartReport.getId());
			}

			String subTotal = c.getString(c.getColumnIndex("subtotal"));
			String tax1 = c.getString(c.getColumnIndex("tax1"));
			String tax2 = c.getString(c.getColumnIndex("tax2"));
			String tax3 = c.getString(c.getColumnIndex("tax3"));

			String total = c.getString(c.getColumnIndex("total"));

			cartReport.subTotal = Long.valueOf(subTotal);
			cartReport.tax1 = Long.valueOf(tax1);
			cartReport.tax2 = Long.valueOf(tax2);
			if(tax3 != null)
				cartReport.tax3 = Long.valueOf(tax3);

			cartReport.total = Long.valueOf(total);

			cartReport.setTaxPercent1(c.getFloat(c.getColumnIndex("taxpercent1")));
			cartReport.setTaxPercent2(c.getFloat(c.getColumnIndex("taxpercent2")));
			cartReport.setTaxPercent3(c.getFloat(c.getColumnIndex("taxpercent3")));

			cartReport.setTaxName1(c.getString(c.getColumnIndex("taxname1")));
			cartReport.setTaxName2(c.getString(c.getColumnIndex("taxname2")));
			cartReport.setTaxName3(c.getString(c.getColumnIndex("taxname3")));

			cartReport.voided = (c.getInt(c.getColumnIndex("voided")) != 0);

			cart.add(cartReport);

			c.moveToNext();
		}

		c.close();
		return cart;
	}

	public static ArrayList<ReportCart> getRecentTrasc(){
		ArrayList<ReportCart> cart = new ArrayList<ReportCart>();

		String[] cols = new String[] { "*" };
		final String whereClause = "onHold != 1";
		String order = "date DESC";

		Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
				null, order, "10");
		c.moveToFirst();

		while (!c.isAfterLast()) {
			ReportCart cartReport = new ReportCart();

			cartReport.setCartItems(c.getString(c.getColumnIndex("lineitems")));
			cartReport.extractXML();

			cartReport.setId(c.getString(c.getColumnIndex("_id")));
			cartReport.setDate(c.getLong(c.getColumnIndex("date")));

			cartReport.setCashierID(c.getInt(c.getColumnIndex("cashierID")));

			cartReport.trans = (c.getInt(c.getColumnIndex("trans")));
			
			if(cartReport.trans == 0)
			{
				cartReport.trans = Integer.valueOf(cartReport.getId());
			}

			String subTotal = c.getString(c.getColumnIndex("subtotal"));
			String tax1 = c.getString(c.getColumnIndex("tax1"));
			String tax2 = c.getString(c.getColumnIndex("tax2"));
			String tax3 = c.getString(c.getColumnIndex("tax3"));

			String total = c.getString(c.getColumnIndex("total"));

			cartReport.subTotal = Long.valueOf(subTotal);
			cartReport.tax1 = Long.valueOf(tax1);
			cartReport.tax2 = Long.valueOf(tax2);
			if(tax3 != null)
				cartReport.tax3 = Long.valueOf(tax3);

			cartReport.total = Long.valueOf(total);

			cartReport.setTaxPercent1(c.getFloat(c.getColumnIndex("taxpercent1")));
			cartReport.setTaxPercent2(c.getFloat(c.getColumnIndex("taxpercent2")));
			cartReport.setTaxPercent3(c.getFloat(c.getColumnIndex("taxpercent3")));

			cartReport.setTaxName1(c.getString(c.getColumnIndex("taxname1")));
			cartReport.setTaxName2(c.getString(c.getColumnIndex("taxname2")));
			cartReport.setTaxName3(c.getString(c.getColumnIndex("taxname3")));

			cartReport.voided = (c.getInt(c.getColumnIndex("voided")) != 0);

			cart.add(cartReport);

			c.moveToNext();
		}

		c.close();
		return cart;
	}
	public static int getSaleNumber() {
        Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " where date >= " +getLastDayShift(), null);
        int trans = 0;
        
        if(c.getCount() > 0)
        {
        	c.moveToLast();
        	trans = c.getInt(c.getColumnIndex("trans"));
        }
        
        c.close();
		return trans;
	}
	
	public static int getSaleIDNumber() {
        Cursor c = getDb().rawQuery("select _id from " + SALES_TABLE, null);
        int trans = 0;
        
        if(c.getCount() > 0)
        {
        	c.moveToLast();
        	trans = c.getInt(c.getColumnIndex("_id"));
        }
        c.close();
		return trans;
	}

	public static void insertStoreSettings() {
        ContentValues vals = new ContentValues();
        
        vals.put("name", StoreSetting.getName());
        vals.put("address", StoreSetting.getAddress());
        vals.put("phone", StoreSetting.getPhone());
        vals.put("email", StoreSetting.getEmail());
        vals.put("website", StoreSetting.getWebsite());
        vals.put("currency", StoreSetting.getCurrency());
        vals.put("clearsale", StoreSetting.clearSale);

        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE, null);
        
        
		if(c.getCount()>0){
	       getDb().update(STORE_TABLE, vals, "_id=1", null);

		}else{
	       getDb().insert(STORE_TABLE, null, vals);
		}		
		c.close();
	}

    public void findStoreSettings() {
    	StoreSetting.clear();
    	
        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE, null);
        c.moveToFirst();
        
        while (!c.isAfterLast()) {
        	
        	StoreSetting.setName(c.getString(c.getColumnIndex("name")));
        	StoreSetting.setAddress(c.getString(c.getColumnIndex("address")));
        	StoreSetting.setPhone(c.getString(c.getColumnIndex("phone")));
        	StoreSetting.setEmail(c.getString(c.getColumnIndex("email")));
        	StoreSetting.setWebsite(c.getString(c.getColumnIndex("website")));
        	StoreSetting.setCurrency(c.getString(c.getColumnIndex("currency")));
        	StoreSetting.clearSale =(c.getInt(c.getColumnIndex("clearsale")) != 0);

        	if(StoreSetting.getCurrency() == null){
        		StoreSetting.setCurrency("$");
        	}

            c.moveToNext();
        }
        c.close();
    }

	public static String getDatabaseName() {
		return DATABASE_NAME;
	}

	public void resetAll() {
		getDb().close();
		getDb().releaseReference();
        ProductDatabaseHelper helper = new ProductDatabaseHelper(context);
        setDb(helper.getWritableDatabase());
	    
	    findCats();
	    buildCatString();		
	}

	public static void close() {
		db.close();
	}

	public static boolean delete() {
		return context.deleteDatabase(DATABASE_NAME);		
	}

	public static void exportinv(String name) {
		StringBuilder exportString = new StringBuilder();
		
		exportString.append("\"[DEPARTMENT]\"\n");
		
		exportString.append("\"_id\",").append("\"name\",").append("\"tax1\",").append("\"tax2\",").append("\"tax3\"\n");
		
        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
        	exportString.append("\""+c.getString(c.getColumnIndex("_id"))+"\",");
        	exportString.append("\""+c.getString(c.getColumnIndex("name"))+"\",");
        	exportString.append("\""+c.getString(c.getColumnIndex("tax1"))+"\",");
        	exportString.append("\""+c.getString(c.getColumnIndex("tax2"))+"\",");
        	if(c.getString(c.getColumnIndex("tax3")) != null)
        		exportString.append("\""+c.getString(c.getColumnIndex("tax3"))+"\"\n");
        	else
        		exportString.append("\"0\"\n");

            c.moveToNext();
        }

        c.close();
		
		exportString.append("\"[/DEPARTMENT]\"\n");
		
		
		exportString.append("\"[PRODUCTS]\"\n");
		
		exportString.append("\"_id\",")
				.append("\"name\",")
				.append("\"desc\",")
				.append("\"price\",")
				.append("\"barcode\",")
				.append("\"catid\",")
				.append("\"quantity\",")
				.append("\"cost\",")
				.append("\"lowAmount\",")
				.append("\"salePrice\",")
				.append("\"saleStart\",")
				.append("\"saleEnd\"\n");
		
        c = getDb().rawQuery("select * from " + PRODUCT_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
    		Product product = new Product();
    		
    		product.price = Long.valueOf(c.getString(c.getColumnIndex("price")));
    		product.salePrice = c.getLong(c.getColumnIndex("salePrice"));
    		product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
    		product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));

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
        	    		    		
            
	    	exportString.append("\""+product.id+"\",");
        	exportString.append("\""+product.name.replaceAll("\"", "")+"\",");
        	exportString.append("\""+product.desc.replaceAll("\"", "")+"\",");
        	exportString.append("\""+(product.price/100f)+"\",");
    		exportString.append("\""+product.barcode+"\",");
        	exportString.append("\""+product.cat+"\",");
        	exportString.append("\""+product.onHand+"\",");
        	exportString.append("\""+(product.cost/100f)+"\",");
        	exportString.append("\""+product.lowAmount+"\",");
        	exportString.append("\""+(product.salePrice/100f)+"\",");
        	
        	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        	exportString.append("\""+formatter.format(new Date(product.startSale))+"\",");
        	exportString.append("\""+formatter.format(new Date(product.endSale))+"\"\n");
        	
            c.moveToNext();
        }

        c.close();
		
		exportString.append("\"[/PRODUCTS]\"\n");
		        
		File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/AdvantagePOS/Inventory");
        
        dir.mkdirs();
        
        File saveFile = new File(sd, "/AdvantagePOS/Inventory/" +name+".csv");

        FileWriter writer;
		try {
			writer = new FileWriter(saveFile);
	        writer.append(exportString.toString());
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static boolean importinv(String filename, Handler handler) {
		
		File sd = Environment.getExternalStorageDirectory();
		
	   	final File loadFile = new File(sd, "/AdvantagePOS/Inventory/"+filename);
	   	
		Log.v("Import" , filename);

        try {
            BufferedReader input =  new BufferedReader(new FileReader(loadFile));
            try {
            	String line = null;
            	while (( line = input.readLine()) != null){
        			Log.v("ImportInv", line);

            		if(inDepartment){
                		if(line.contains("[/DEPARTMENT]")){
                			inDepartment = false;
                		}
                		else if(!line.contains("_id")){
                			StringTokenizer st = new StringTokenizer(line, ",");
            				
            				String id = st.nextToken().replaceAll("\"", "");
            				
            		        ContentValues vals = new ContentValues();
            		        vals.put("name", st.nextToken().replaceAll("\"", ""));
            		        vals.put("tax1", Float.valueOf(st.nextToken().replaceAll("\"", "")));
            		        vals.put("tax2", Float.valueOf(st.nextToken().replaceAll("\"", "")));
            		        if(st.hasMoreTokens())
            		        {
                		        vals.put("tax3", Float.valueOf(st.nextToken().replaceAll("\"", "")));
            		        }
            		        if(getDb().update(CATAGORY_TABLE, vals, "_id=" + id, null) == 0){
            		        	getDb().insert(CATAGORY_TABLE, null, vals);
            		        }
            			}
            		}
            		
            		else if(inProducts){
                		if(line.contains("[/PRODUCTS]")){
                			inProducts = false;
                		}
                		else if(!line.contains("_id")){
                			
                			String[] RowData = line.split(",");
            				
            				String id = RowData[0].replaceAll("\"", "");
            				           						
            		        ContentValues vals = new ContentValues();
            		        vals.put("name", RowData[1].replaceAll("\"", ""));
            		        vals.put("desc", RowData[2].replaceAll("\"", ""));
            		        if(!RowData[3].equals(""))
            		        {
            		        	String desc = RowData[3].replaceAll("\"", "");
            		        	if(desc.length() > 6) desc = desc.substring(desc.length()-6, desc.length());
            		        	vals.put("price", (int)(Float.valueOf(desc)*100f));
            		        }
            		        vals.put("barcode", RowData[4].replaceAll("\"", ""));
            		        if(!RowData[5].equals(""))
            		        	vals.put("catid", Integer.valueOf(RowData[5].replaceAll("\"", "")));
            		        if(RowData.length > 6){
	            		        if(!RowData[6].equals(""))
	            		        	vals.put("quantity", Integer.valueOf(RowData[6].replaceAll("\"", "")));
            		        }else{
            		        	vals.put("quantity", 0);
            		        }
            		        
            		        if(RowData.length > 7){
	            		        if(!RowData[7].equals(""))
	            		        {
	            		        	String desc = RowData[7].replaceAll("\"", "");
	            		        	if(desc.length() > 6) desc = desc.substring(desc.length()-6, desc.length());
	            		        	vals.put("cost", (int)(Float.valueOf(desc)*100f));
	            		        }else{
	            		        	vals.put("cost", 0);
	            		        }
	            		    }else{
            		        	vals.put("cost", 0);
            		        }
            		                    		        
            		        if(RowData.length > 8){
	            		        if(!RowData[8].equals(""))
	            		        {
	            		        	String desc = RowData[8].replaceAll("\"", "");
	            		        	if(desc.length() > 6) desc = desc.substring(desc.length()-6, desc.length());
	            		        	vals.put("lowAmount", Integer.valueOf(desc));
	            		        }else{
	            		        	vals.put("lowAmount", 0);
	            		        }
	            		    }else{
            		        	vals.put("lowAmount", 0);
            		        }
            		        
            		        if(RowData.length > 9){
	            		        if(!RowData[9].equals(""))
	            		        {
	            		        	String desc = RowData[9].replaceAll("\"", "");
	            		        	if(desc.length() > 6) desc = desc.substring(desc.length()-6, desc.length());
	            		        	vals.put("salePrice", (int)(Float.valueOf(desc)*100f));
	            		        }else{
	            		        	vals.put("salePrice", 0);
	            		        }
	            		    }else{
            		        	vals.put("salePrice", 0);
            		        }
            		        
            		        if(RowData.length > 10){
	            		        if(!RowData[10].equals(""))
	            		        {
	            		        	String desc = RowData[10].replaceAll("\"", "");
	            		        	
	            		        	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
	            		        	ParsePosition pos = new ParsePosition(0);
	            		        	Date date = formatter.parse(desc, pos);
	            		        	
	            		        	vals.put("saleStartDate", date.getTime());
	            		        }else{
	            		        	vals.put("saleStartDate", 0);
	            		        }
	            		    }else{
            		        	vals.put("saleStartDate", 0);
            		        }
            		        
            		        if(RowData.length > 11){
	            		        if(!RowData[11].equals(""))
	            		        {
	            		        	String desc = RowData[11].replaceAll("\"", "");
	            		        	
	            		        	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
	            		        	ParsePosition pos = new ParsePosition(0);
	            		        	Date date = formatter.parse(desc, pos);
	            		        	
	            		        	vals.put("saleEndDate", date.getTime());
	            		        }else{
	            		        	vals.put("saleEndDate", 0);
	            		        }
	            		    }else{
            		        	vals.put("saleEndDate", 0);
            		        }
            		        
            				Message m = new Message();
            				m.what = 11;
            				m.obj = RowData[0].replaceAll("\"", "") + " - " + RowData[1].replaceAll("\"", ""); 
            				handler.sendMessage(m);

            		        if(getDb().update(PRODUCT_TABLE, vals, "_id=" + id, null) == 0){
            		        	getDb().insert(PRODUCT_TABLE, null, vals);
            		        }
            			}
            		}
            		
            		if(line.contains("[DEPARTMENT]")){
            			inDepartment = true;
            		}        		
            		if(line.contains("[PRODUCTS]")){
            			inProducts = true;
            		}
            	}
            }
            finally {
              input.close();
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
        	    
	    findCats();
	    buildCatString();
	    
	    return true;
	}

	public static Cursor getProdByName(String inputText) {
        String[] cols = new String[] {"*"};        
        Cursor c = getDb().query(PRODUCT_TABLE, cols, "_id = ?", new String[] { inputText }, null, null, null);
        if(c.moveToFirst()){
        	return c;
        }
        c.close();      
        return null;
	}

	public void findEmailSettings() {
		EmailSetting.clear();
		
        Cursor c = getDb().rawQuery("select * from " + EMAIL_TABLE, null);
        c.moveToFirst();
                
        while (!c.isAfterLast()) {
        	
        	EmailSetting.setEnabled(c.getInt(c.getColumnIndex("enabled")) != 0);
        	EmailSetting.bookkeeper = (c.getInt(c.getColumnIndex("bookkeeper")) != 0);
        	EmailSetting.setSmtpServer(c.getString(c.getColumnIndex("smtpserver")));
        	EmailSetting.setSmtpPort(c.getInt(c.getColumnIndex("smtpport")));
        	EmailSetting.setSmtpUsername(c.getString(c.getColumnIndex("smtpuser")));
        	EmailSetting.setSmtpPasword(c.getString(c.getColumnIndex("smtppass")));
        	EmailSetting.setSmtpEmail(c.getString(c.getColumnIndex("smtpemail")));
        	EmailSetting.setSmtpSubject(c.getString(c.getColumnIndex("smtpsubject")));
        	EmailSetting.blurb = c.getString(c.getColumnIndex("blurb"));

            c.moveToNext();
        }
        c.close();
	}
	
	public static void saveButton(Button button) {
        ContentValues vals = new ContentValues();
                
        vals.put("type", button.type);
        vals.put("orderBy", button.order);
        vals.put("parent", button.parent);
        vals.put("productID", button.productID);
        vals.put("departID", button.departID);
        vals.put("folderName", button.folderName);
        vals.put("link", button.link);

        Bitmap photo = button.image;
        
        if(photo != null)
        {
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        	photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
        	byte[] bArray = bos.toByteArray();
            vals.put("image", bArray);
        }
        
		if(button.id > 0){
	        getDb().update(BUTTON_TABLE, vals, "_id="+button.id, null);
		}else{
	        getDb().insert(BUTTON_TABLE, null, vals);
		}		
	}

	public static void insertEmailSettings() {
        ContentValues vals = new ContentValues();
        
        vals.put("enabled", EmailSetting.isEnabled());
        vals.put("bookkeeper", EmailSetting.bookkeeper);
        vals.put("smtpserver", EmailSetting.getSmtpServer());
        vals.put("smtpport", EmailSetting.getSmtpPort()); 
        vals.put("smtpuser", EmailSetting.getSmtpUsername());
        vals.put("smtppass", EmailSetting.getSmtpPasword());
        vals.put("smtpemail", EmailSetting.getSmtpEmail());
        vals.put("smtpsubject", EmailSetting.getSmtpSubject());
        vals.put("blurb", EmailSetting.blurb);

        Cursor c = getDb().rawQuery("select * from " + EMAIL_TABLE, null);
        
		if(c.getCount()>0){
	        getDb().update(EMAIL_TABLE, vals, "_id=1", null);
		}else{
	        getDb().insert(EMAIL_TABLE, null, vals);
		}		
		
		c.close();
	}

	public void insertReceiptSettings() {
        ContentValues vals = new ContentValues();
                
        vals.put("enabled", ReceiptSetting.enabled);
        vals.put("blurb", ReceiptSetting.blurb);
        vals.put("address", ReceiptSetting.address);
        vals.put("name", ReceiptSetting.name);
        vals.put("make", ReceiptSetting.make);
        vals.put("size", ReceiptSetting.size);
        vals.put("type", ReceiptSetting.type);
        vals.put("drawer", ReceiptSetting.drawer);
        vals.put("display", ReceiptSetting.display);
        vals.put("printermodel", ReceiptSetting.printerModel);
        vals.put("printername", ReceiptSetting.printerName);
        vals.put("drawercode", ReceiptSetting.drawerCode);
        vals.put("cutcode", ReceiptSetting.cutCode);

        Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);
        
		if(c.getCount()>0){
	        getDb().update(RECEIPT_TABLE, vals, "_id=1", null);
		}else{
	        getDb().insert(RECEIPT_TABLE, null, vals);
		}		
		
		c.close();		
	}

	public void findReceiptSettings() {
		ReceiptSetting.clear();
		
        Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);
        c.moveToFirst();
                
        while (!c.isAfterLast()) {
        	
        	ReceiptSetting.enabled = c.getInt(c.getColumnIndex("enabled")) != 0;
        	ReceiptSetting.blurb = c.getString(c.getColumnIndex("blurb"));
        	ReceiptSetting.address = c.getString(c.getColumnIndex("address"));
        	ReceiptSetting.name = c.getString(c.getColumnIndex("name"));
        	ReceiptSetting.make = c.getInt(c.getColumnIndex("make"));
        	ReceiptSetting.size = c.getInt(c.getColumnIndex("size"));
        	ReceiptSetting.type = c.getInt(c.getColumnIndex("type"));
        	ReceiptSetting.drawer = c.getInt(c.getColumnIndex("drawer")) != 0;
        	ReceiptSetting.display = c.getInt(c.getColumnIndex("display")) != 0;
        
            c.moveToNext();
        }
        c.close();
	}

	public static SQLiteDatabase getDb() {
		return db;
	}

	public static void setDb(SQLiteDatabase db) {
		ProductDatabase.db = db;
	}

	public static int insertCustomer(Customer customer) {
        ContentValues vals = new ContentValues();
        vals.put("fName", customer.name);
        vals.put("email", customer.email);
        vals.put("numreturns", customer.returns);
        vals.put("numsales", customer.sales);
        vals.put("total", customer.total);

        int at = (int) getDb().insert(CUSTOMER_TABLE, null, vals);
        customer.setId(at);
        return at;
    }
	
	public static void insertLog(String type, String log){
		
		ContentValues vals = new ContentValues();
        vals.put("log", log);
        vals.put("type", type);
        
        getDb().insert(LOGGER_TABLE, null, vals);
	}

	public static void RemoveCustomer(int position) {
		getDb().delete(CUSTOMER_TABLE, "_id=" + position, null);
	}

	public static void replaceCustomer(Customer newprod) {
        ContentValues vals = new ContentValues();
                
        vals.put("fName", newprod.name);
        vals.put("email", newprod.email);
        vals.put("numreturns", newprod.returns);
        vals.put("numsales", newprod.sales);
        vals.put("total", newprod.total);
        
        Log.v("VALS", newprod.id+" "+newprod.name + " " +newprod.total);

        getDb().update(CUSTOMER_TABLE, vals, "_id=" + newprod.id, null);
	}
	
	public static Cursor SearchCustomers(String string) {
       	String formatedText = string.replaceAll("'", "''"); 
		
		Cursor mCursor;
		
		if(formatedText.length()<1){
			mCursor = getDb().rawQuery("select * from " + CUSTOMER_TABLE, null);
		}else{
       	
            mCursor = getDb().query(true, CUSTOMER_TABLE, new String[] {"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%"+ formatedText + "%'", null,
                    null, null, null, null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public static String[] fetchCustomersName(String string) {
       	String formatedText = string.replaceAll("'", "''"); 

        Cursor mCursor = getDb().query(true, CUSTOMER_TABLE, new String[] {"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%"+ formatedText + "%'", null,
                null, null, null, null);
        String[] CustomerString;
		if (mCursor != null) {
            mCursor.moveToFirst();
            
            CustomerString = new String[mCursor.getCount()];
            int i = 0;
            while (!mCursor.isAfterLast()) {	
            	if(mCursor.getString(mCursor.getColumnIndex("email")).equals(""))
            	{
            		CustomerString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " +  mCursor.getString(mCursor.getColumnIndex("fname")));
            	}else{
            		CustomerString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " +  mCursor.getString(mCursor.getColumnIndex("fname")) + ", " +  mCursor.getString(mCursor.getColumnIndex("email")));
            	}
            	i++;
            	mCursor.moveToNext();
            }
            
            mCursor.close();
        }else{
        	return null;
        }
        
        return CustomerString;
	}

	public static int insertCashier(Cashier cashier) {
        ContentValues vals = new ContentValues();
        vals.put("fname", cashier.name);
        vals.put("email", cashier.email);
        vals.put("numreturns", cashier.returns);
        vals.put("numsales", cashier.sales);
        vals.put("total", cashier.total);        
        vals.put("pin", cashier.pin);
        vals.put("permissionReturn", cashier.permissionReturn);
        vals.put("permissionPriceModify", cashier.permissionPriceModify);
        vals.put("permissionReports", cashier.permissionReports);
        vals.put("permissionInventory", cashier.permissionInventory);
        vals.put("permissionSettings", cashier.permissionSettings);

        int at = (int) getDb().insert(CASHIER_TABLE, null, vals);
        cashier.id = at;
        return at;
	}

	public static void replaceCashier(Cashier cashier) {
        ContentValues vals = new ContentValues();
        
        vals.put("fname", cashier.name);
        vals.put("email", cashier.email);
        vals.put("numreturns", cashier.returns);
        vals.put("numsales", cashier.sales);
        vals.put("total", cashier.total);
        vals.put("pin", cashier.pin);
        vals.put("permissionReturn", cashier.permissionReturn);
        vals.put("permissionPriceModify", cashier.permissionPriceModify);
        vals.put("permissionReports", cashier.permissionReports);
        vals.put("permissionInventory", cashier.permissionInventory);
        vals.put("permissionSettings", cashier.permissionSettings);
        
        getDb().update(CASHIER_TABLE, vals, "_id=" + cashier.id, null);

	}

	public static void RemoveCashier(int position) {
		getDb().delete(CASHIER_TABLE, "_id=" + position, null);
	}

	public void insertAdminSettings() {
        ContentValues vals = new ContentValues();
        
        vals.put("enabled", AdminSetting.enabled);
        vals.put("password", AdminSetting.password);
        vals.put("hint", AdminSetting.hint);

        Cursor c = getDb().rawQuery("select * from " + ADMIN_TABLE, null);
        
		if(c.getCount()>0){
	        getDb().update(ADMIN_TABLE, vals, "_id=1", null);
		}else{
	        getDb().insert(ADMIN_TABLE, null, vals);
		}		
		
		c.close();
	}

	public void findAdminSettings() {
		AdminSetting.clear();
		
        Cursor c = getDb().rawQuery("select * from " + ADMIN_TABLE, null);
        c.moveToFirst();
                
        while (!c.isAfterLast()) {
        	
        	AdminSetting.enabled = c.getInt(c.getColumnIndex("enabled")) != 0;
            AdminSetting.password = c.getString(c.getColumnIndex("password"));
            AdminSetting.hint =c.getString(c.getColumnIndex("hint"));

            c.moveToNext();
        }
        c.close();
	}

	public static ArrayList<Cashier> getCashiers() {
        Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE, null);
        c.moveToFirst();
        
        ArrayList<Cashier> cashiers = new ArrayList<Cashier>();

        while (!c.isAfterLast()) {
        	final Cashier cashier = new Cashier();
        	
        	cashier.name = c.getString(c.getColumnIndex("fname"));
        	cashier.id = c.getInt(c.getColumnIndex("_id"));
        	cashier.email = c.getString(c.getColumnIndex("email"));
        	cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
        	cashier.sales = c.getInt(c.getColumnIndex("numsales"));
        	cashier.total = c.getFloat(c.getColumnIndex("total"));
           
        	cashier.pin = c.getString(c.getColumnIndex("pin"));
        	cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
        	cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
        	cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
        	cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
        	cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;

        	cashiers.add(cashier);

            c.moveToNext();
        }

        c.close();	
        
        return cashiers;
	}

	public static Cursor SearchCashiers(String string) {
       	String formatedText = string.replaceAll("'", "''"); 
		
		Cursor mCursor;
		
		if(formatedText.length()<1){
			mCursor = getDb().rawQuery("select * from " + CASHIER_TABLE, null);
		}else{
       	
            mCursor = getDb().query(true, CASHIER_TABLE, new String[] {"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%"+ formatedText + "%'", null,
                    null, null, null, null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public static Cashier getCashier(int cashierID) {

		Cashier cashier = null;
        Cursor c = getDb().query(CASHIER_TABLE, new String[] { "*" }, "_id = ?", new String[] { ""+cashierID }, null, null, null);
        if(c.moveToFirst()){
        	cashier = new Cashier();
        	       	
        	cashier.name = c.getString(c.getColumnIndex("fname"));
        	cashier.id = c.getInt(c.getColumnIndex("_id"));
        	cashier.email = c.getString(c.getColumnIndex("email"));
        	cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
        	cashier.sales = c.getInt(c.getColumnIndex("numsales"));
        	cashier.total = c.getFloat(c.getColumnIndex("total"));
           
        	cashier.pin = c.getString(c.getColumnIndex("pin"));
        	cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
        	cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
        	cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
        	cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
        	cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;
        }
        c.close();
        return cashier;
	}

	public static void replaceSale(ReportCart cart) {
	       ContentValues vals = new ContentValues();
	        vals.put("_id", cart.getId());

	        vals.put("trans", cart.trans);

	        vals.put("date", cart.getDate());
	        vals.put("lineitems", cart.getCartItems());
	        vals.put("subtotal", cart.subTotal);
	        vals.put("tax1", cart.tax1);
	        vals.put("tax2", cart.tax2);
	        vals.put("tax3", cart.tax3);

	        if(cart.cashier != null)
	        	vals.put("cashierID", cart.cashier.id);
	        else
	        	vals.put("cashierID", 0);
	        
	        vals.put("taxpercent1", cart.getTaxPercent1());
	        vals.put("taxpercent2", cart.getTaxPercent2());
	        vals.put("taxpercent3", cart.getTaxPercent3());

	        vals.put("taxname1", cart.getTaxName1());
	        vals.put("taxname2", cart.getTaxName2());
	        vals.put("taxname3", cart.getTaxName3());

	        vals.put("total", cart.total);
	        if(cart.Customer != null)
	        	vals.put("customer", cart.Customer.id);
	        else
	        	vals.put("customer", 0);	
	        vals.put("voided", cart.voided);
	        
	        getDb().update(SALES_TABLE, vals, "_id=" + cart.getId(), null);

	}

	public static Cursor getButtons(int parent) {
		Cursor mCursor = null;
				
		if(parent > 0){
			mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where parent="+parent+" or type = -1 order by orderBy", null);
		}else{
			mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where parent="+parent+" order by orderBy", null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public static ArrayList<FolderList> getDepartments() {
		ArrayList<FolderList> folders = new ArrayList<FolderList>();
		FolderList home = new FolderList();
		home.id = 0;
		home.name = "No Department";
		
		folders.add(home);
		Cursor c = null;

		c = getDb().rawQuery("select * from " + CATAGORY_TABLE, null);
		c.moveToFirst();

        while (!c.isAfterLast()) {
        	String name = c.getString(c.getColumnIndex("name"));
        	long id = c.getLong(c.getColumnIndex("_id"));
        	
        	if(name!= null && !name.trim().equals(""))
        	{	home = new FolderList();
	    		home.id = id;
	    		home.name = name;
	    		
	    		folders.add(home);
        	}
        	else
        	{	
        		home = new FolderList();
	    		home.id = 0;
	    		home.name = "Null";
	    		
	    		folders.add(home);
        	}

            c.moveToNext();
        }

        c.close();
        return folders;	
	}
	
	public static ArrayList<FolderList> getFolders() {
		ArrayList<FolderList> folders = new ArrayList<FolderList>();
		FolderList home = new FolderList();
		home.id = 0;
		home.name = "Home";
		
		folders.add(home);
		Cursor c = null;

		c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where type="+Button.TYPE_FOLDER, null);
		c.moveToFirst();

        while (!c.isAfterLast()) {
        	String name = c.getString(c.getColumnIndex("folderName"));
        	long id = c.getLong(c.getColumnIndex("_id"));
        	
        	if(name!= null && !name.trim().equals(""))
        	{	home = new FolderList();
	    		home.id = id;
	    		home.name = name;
	    		
	    		folders.add(home);
        	}
        	else
        	{	
        		home = new FolderList();
	    		home.id = 0;
	    		home.name = "Null";
	    		
	    		folders.add(home);
        	}

            c.moveToNext();
        }

        c.close();
        return folders;
	}

	public static int getFolderID(String selectedItem) {
		Cursor c = null;
		int id = 0;
		c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where folderName='"+selectedItem+"'", null);
		c.moveToFirst();

        while (!c.isAfterLast()) {
        	id = ((c.getInt(c.getColumnIndex("_id"))));
            c.moveToNext();
        }

        c.close();
        return id;
	}
	
	public static void deleteButton(int position) {
		if(position > 1)
			getDb().delete(BUTTON_TABLE, "_id=" + position, null);
	}

	public static Button getButtonByID(int newParent) {
		Cursor c = null;

		c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where _id="+newParent, null);
		c.moveToFirst();
		
		Button button = new Button();	
		
        if (!c.isAfterLast()) {
			button.id = c.getInt(c.getColumnIndex("_id"));
			button.type = c.getInt(c.getColumnIndex("type"));
			button.order = c.getInt(c.getColumnIndex("orderBy"));
			button.parent = c.getInt(c.getColumnIndex("parent"));
			button.productID = c.getInt(c.getColumnIndex("productID"));
			button.departID = c.getInt(c.getColumnIndex("departID"));
			button.folderName = c.getString(c.getColumnIndex("folderName"));	
			button.link = c.getString(c.getColumnIndex("link"));	

	        final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));
	        
	        if(imageBlob != null)
	        {       	
	        	Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 
	        	        0,imageBlob.length);
	        	button.image = image;
	        }
        }
		c.close();
		
		return button;
	}

	public static boolean hasChildren(int parent) {
		Cursor mCursor = null;
		
		mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where parent="+parent+" order by orderBy", null);

		mCursor.moveToFirst();

        if (mCursor.isAfterLast()) {
        	mCursor.close();
        	return false;
        }
        mCursor.close();	
        return true;
	}

	public static Cursor getOnHoldSales() {
        String[] cols = new String[] { "*" };        
        String whereClause = "onHold = 1"; 
        String orderBy = "date"; 
        Log.v("Find Sales", "Finding...");
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, orderBy, null);
		
        if(c!=null)
        {
            Log.v("Find Sales", "Found: "+c.getCount());

			c.moveToFirst();
        }
        else
        {
            Log.v("Find Sales", "None Found.");
        }
   
        return c;
	}

	public static void deleteSale(int saleID) {
		getDb().delete(SALES_TABLE, "_id=" + saleID, null);		
	}

	public void insertMercurySettings() {
        ContentValues vals = new ContentValues();
        
        vals.put("enabled", PrioritySetting.enabled);
        vals.put("merchantID", PrioritySetting.merchantID);
        vals.put("webServicePassword", PrioritySetting.webServicePassword);
        vals.put("terminalName", PrioritySetting.terminalName);
        vals.put("hostedMID", PrioritySetting.hostedMID);
        vals.put("hostedPass", PrioritySetting.hostedPass);
        Cursor c = getDb().rawQuery("select * from " + PRIORITY_TABLE, null);
        
		if(c.getCount()>0){
	        getDb().update(PRIORITY_TABLE, vals, "_id=1", null);
		}else{
	        getDb().insert(PRIORITY_TABLE, null, vals);
		}		
		
		c.close();
	}
	
	public void findMercurySettings() {
		PrioritySetting.clear();
		
        Cursor c = getDb().rawQuery("select * from " + PRIORITY_TABLE, null);
        c.moveToFirst();
                
        while (!c.isAfterLast()) {
        	
        	PrioritySetting.enabled = c.getInt(c.getColumnIndex("enabled")) != 0;
        	PrioritySetting.merchantID = c.getString(c.getColumnIndex("merchantID"));
        	PrioritySetting.webServicePassword =c.getString(c.getColumnIndex("webServicePassword"));
        	PrioritySetting.terminalName =c.getString(c.getColumnIndex("terminalName"));
        	PrioritySetting.hostedPass =c.getString(c.getColumnIndex("hostedPass"));
        	PrioritySetting.hostedMID =c.getString(c.getColumnIndex("hostedMID"));
        	
            c.moveToNext();
        }
        c.close();
	}

	public static void saveMercuryForLater(String postXml, String invoice, int saleid, boolean processed) {
        ContentValues vals = new ContentValues();
            	
        vals.put("processed", processed);
        
        if(processed)
        {
            vals.put("response", postXml);
        	vals.put("request", "");
        }else{
        	vals.put("request", postXml);
            vals.put("response", "");
        }
        
        vals.put("invoice", invoice);
        vals.put("saleid", saleid);
        
        long currentDateTime = new Date().getTime();
      
        vals.put("date", ""+currentDateTime);

	    getDb().insert(CCSALES_TABLE, null, vals);
	}
	
	public static void saveMercuryManual(String postXml, String invoice) {
        ContentValues vals = new ContentValues();
            	
        vals.put("processed", -6);
        vals.put("response", postXml);
        vals.put("invoice", invoice);
        
        long currentDateTime = new Date().getTime();
      
        vals.put("date", ""+currentDateTime);

	    getDb().insert(CCSALES_TABLE, null, vals);
	}
	
	public static void replaceMercurySave(Payment ccPayment) {
        ContentValues vals = new ContentValues();
            	
        vals.put("processed", ccPayment.processed);
        vals.put("request", "");
        vals.put("invoice", ccPayment.InvoiceNo);
        vals.put("response", ccPayment.response);
        if(ccPayment.date != null)
        	vals.put("date", ccPayment.date);
        else
        	vals.put("date", "");

        getDb().update(CCSALES_TABLE, vals, "_id="+ccPayment.preSaleID, null);
	}
	
	public static void removePreCreditPayment(Payment payment) {
		getDb().delete(CCSALES_TABLE, "saleid=" + payment.saleID, null);	
	}

	public static ArrayList<Payment> getProritySales(long l, long m) {
		ArrayList<Payment> payments = new ArrayList<Payment>();

		String[] cols = new String[] { "*" };
		final String whereClause = "date >= " + l + " and date <= " + m;
		
		//final String whereClause = "processed != 1";

		Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();
		
		while (!c.isAfterLast()) {
			Payment payment = new Payment();
	
			payment.request = c.getString(c.getColumnIndex("request"));
			payment.response = c.getString(c.getColumnIndex("response"));

			payment.date = c.getString(c.getColumnIndex("date"));
         	payment.preSaleID = c.getInt(c.getColumnIndex("_id"));
         	payment.processed = c.getInt(c.getColumnIndex("processed"));
         	payment.saleID = c.getInt(c.getColumnIndex("saleid"));
         	payment.extractJSON();
         	
         	payments.add(payment);
         	
         	c.moveToNext();
		}
		
		c.close();
		return payments;
	}
	
	public static ArrayList<Payment> getPrioritySale() {
		ArrayList<Payment> payments = new ArrayList<Payment>();

		String[] cols = new String[] { "*" };
		
		final String whereClause = "processed = 0";

		Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		
		if(c.getCount() > 0)
		{
			c.moveToFirst();
		
			Payment payment = new Payment();
	
			payment.InvoiceNo = c.getString(c.getColumnIndex("invoice"));
			payment.request = c.getString(c.getColumnIndex("request"));
			payment.date = c.getString(c.getColumnIndex("date"));
         	payment.preSaleID = c.getInt(c.getColumnIndex("_id"));
         	payment.processed = c.getInt(c.getColumnIndex("processed"));
         	payment.saleID = c.getInt(c.getColumnIndex("saleid"));
         	payment.extractJSON();

         	payments.add(payment);
         	
		}
		
		c.close();
		return payments;
	}

	public static void voidSale(String substring) {
        ContentValues vals = new ContentValues();
    	
        vals.put("voided", 1);

        getDb().update(SALES_TABLE, vals, "_id="+substring, null);
	}

	public static void replaceMercuryPartial(Payment ccPayment) {
        ContentValues vals = new ContentValues();
    	
        vals.put("processed", ccPayment.processed);
        vals.put("request", ccPayment.request);
        vals.put("invoice", ccPayment.InvoiceNo);
        vals.put("response", ccPayment.response);
        if(ccPayment.date != null)
        	vals.put("date", ccPayment.date);
        else
        	vals.put("date", "");

        getDb().update(CCSALES_TABLE, vals, "invoice="+ccPayment.InvoiceNo, null);
	}

	public static ArrayList<Shift> getShifts(long l, long m) {
		ArrayList<Shift> shifts = new ArrayList<Shift>();

		String[] cols = new String[] { "*" };
		final String whereClause = "end >= " + l + " and end <= " + m;

		Cursor c = getDb().query(SHIFT_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			Shift shift = new Shift();

			shift.id = c.getInt(c.getColumnIndex("_id"));
			String start = c.getString(c.getColumnIndex("start"));
			String end = c.getString(c.getColumnIndex("end"));
			String note = c.getString(c.getColumnIndex("note"));

			shift.start = Long.valueOf(start);
			shift.end = Long.valueOf(end);
			shift.note = note;
			shift.carts = getReports1(shift.start, shift.end);

			shifts.add(0, shift);
			c.moveToNext();
		}

		c.close();
		return shifts;
	}

	public static void saveShift() {
        ContentValues vals = new ContentValues();
        
        long currentDateTime = new Date().getTime();
        
        Shift shift = new Shift();
        
        shift.end = currentDateTime;
        shift.start = getLastShift();
                
        vals.put("end", shift.end);
        vals.put("start", shift.start);

        getDb().insert(SHIFT_TABLE, null, vals);		
	}

	private static long getLastShift() {
		String[] cols = new String[] { "*" };

		Cursor c = getDb().query(SHIFT_TABLE, cols, null, null, null,
				null, null, null);
		c.moveToLast();

		while (!c.isAfterLast()) {
			Shift shift = new Shift();

			shift.id = c.getInt(c.getColumnIndex("_id"));
			String start = c.getString(c.getColumnIndex("start"));
			String end = c.getString(c.getColumnIndex("end"));
			String note = c.getString(c.getColumnIndex("note"));

			shift.start = Long.valueOf(start);
			shift.end = Long.valueOf(end);
			shift.note = note;
			shift.carts = getReports1(shift.start, shift.end);

			c.close();
			return shift.end;
		}

		c.close();
		return 0;	
	}

	public static ArrayList<Shift> getDayShifts(long l, long m) {
		ArrayList<Shift> shifts = new ArrayList<Shift>();

		String[] cols = new String[] { "*" };
		final String whereClause = "end >= " + l + " and end <= " + m;

		Cursor c = getDb().query(DAY_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			Shift shift = new Shift();

			shift.id = c.getInt(c.getColumnIndex("_id"));
			String start = c.getString(c.getColumnIndex("start"));
			String end = c.getString(c.getColumnIndex("end"));
			String note = c.getString(c.getColumnIndex("note"));

			shift.start = Long.valueOf(start);
			shift.end = Long.valueOf(end);
			shift.note = note;
			shift.carts = getReports1(shift.start, shift.end);

			shifts.add(0, shift);

			c.moveToNext();
		}

		c.close();
		return shifts;
	}

	public static void saveDayShift() {
		
		if(tranAfterLastShift())
		{
			saveShift();
		}
		
        ContentValues vals = new ContentValues();
        
        long currentDateTime = new Date().getTime();
        
        Shift shift = new Shift();
        
        shift.end = currentDateTime;
        shift.start = getLastDayShift();
                
        vals.put("end", shift.end);
        vals.put("start", shift.start);

        getDb().insert(DAY_TABLE, null, vals);		
	}

	public static boolean tranAfterLastShift() {

		long lastShift = getLastShift();
		
		String[] cols = new String[] { "*" };
		final String whereClause = "date >= " + lastShift;

		Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			c.close();
			return true;
		}
		
		c.close();
		return false;
	}

	private static long getLastDayShift() {
		String[] cols = new String[] { "*" };

		Cursor c = getDb().query(DAY_TABLE, cols, null, null, null,
				null, null, null);
		c.moveToLast();

		while (!c.isAfterLast()) {
			Shift shift = new Shift();

			shift.id = c.getInt(c.getColumnIndex("_id"));
			String start = c.getString(c.getColumnIndex("start"));
			String end = c.getString(c.getColumnIndex("end"));
			String note = c.getString(c.getColumnIndex("note"));

			shift.start = Long.valueOf(start);
			shift.end = Long.valueOf(end);
			shift.note = note;
			//shift.carts = getReports1(shift.start, shift.end);

			c.close();			
			return shift.end;
		}

		c.close();
		return 0;	
	}

	public static boolean tranAfterLastDayShift() {
		long lastShift = getLastDayShift();
		
		String[] cols = new String[] { "*" };
		final String whereClause = "date >= " + lastShift;

		Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		c.moveToFirst();

		while (!c.isAfterLast()) {
			c.close();
			return true;
		}
		c.close();
		return false;	
	}
	
	public static void backupDelete()
	{
		try {
            File sd = Environment.getExternalStorageDirectory();
            
    	   	File loadFile = new File(sd, "/AdvantagePOS/Database");
    	   	loadFile.mkdirs();
    	   	
            if (sd.canWrite()) 
            {    		            
                String currentDBPath = ProductDatabase.getDb().getPath();
                
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, "/AdvantagePOS/Database/unlicensed-backup.db");
                
                if (currentDB.exists()) {
                	ProductDatabase.close();

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    
                }
                
                ProductDatabase.delete();
                PointOfSale.resetShop();
            }
            
        } catch (Exception e) {
			
        }
	}

	public static ArrayList<Cashier> getAdmins() {
		Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE + " WHERE permissionReports='1'", null);
        c.moveToFirst();
        
        ArrayList<Cashier> cashiers = new ArrayList<Cashier>();

        while (!c.isAfterLast()) {
        	final Cashier cashier = new Cashier();
        	
        	cashier.name = c.getString(c.getColumnIndex("fname"));
        	cashier.id = c.getInt(c.getColumnIndex("_id"));
        	cashier.email = c.getString(c.getColumnIndex("email"));
        	cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
        	cashier.sales = c.getInt(c.getColumnIndex("numsales"));
        	cashier.total = c.getFloat(c.getColumnIndex("total"));
           
        	cashier.pin = c.getString(c.getColumnIndex("pin"));
        	cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
        	cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
        	cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
        	cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
        	cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;

        	cashiers.add(cashier);

            c.moveToNext();
        }

        c.close();	
        
        return cashiers;
	}

	public static int preAuthCount() {
		String[] cols = new String[] { "*" };
		
		final String whereClause = "processed = 0";

		Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null,
				null, null, null);
		
		int count = c.getCount();
		c.close();
			
		return count;
	}
	
	public static void insertRowsInPrinterTable(InputStream inputStream){
		try {		
			getDb().delete(PRINTER_TYPES_TABLES, null, null);
			Workbook workbook = Workbook.getWorkbook(inputStream);
			Sheet sheet = workbook.getSheet(0);
			for(int j=1; j< sheet.getRows(); j++){
				ContentValues values = new ContentValues();
				values.put("_id", j);
			
				for(int i=0; i< sheet.getColumns(); i++){
					
					Cell cell = sheet.getCell(i,j);
					values.put((sheet.getCell(i,0).getContents()).trim() , (cell.getContents()).trim());
					
				}
				long id = getDb().insert(PRINTER_TYPES_TABLES, null, values);
				//db.close();
			}
			
		} catch (Exception e) {
			Log.v("error", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static Cursor getPrintersAndModels(){
		
		Cursor c = getDb().rawQuery("select * from " + PRINTER_TYPES_TABLES + " order by PrinterType, PrinterModel ", null);
		return c;
	}
	
	public static Cursor getPrinterModels(String name){
		
		Cursor c = getDb().rawQuery("select PrinterModel from "+ PRINTER_TYPES_TABLES +" where PrinterType = ? order by PrinterModel", new String[]{name});
		
		return c;
	}
	
	public static Cursor getPrinterNames(){
		
		Cursor c = getDb().rawQuery("Select DISTINCT PrinterType from " + PRINTER_TYPES_TABLES + " order by PrinterType ", null);
		
		return c;
	}
	
	public static Cursor getPrinterCodes(String printerName, String printerModel){
		
		Cursor c = getDb().rawQuery("select * from "+ PRINTER_TYPES_TABLES + " where PrinterType=? AND PrinterModel = ? ", 
									new String[]{printerName, printerModel});
		
		return c;
	}
}
