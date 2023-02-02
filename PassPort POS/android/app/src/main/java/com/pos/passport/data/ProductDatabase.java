package com.pos.passport.data;

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

import com.pos.passport.interfaces.ItemOpen;
import com.pos.passport.model.AdminSetting;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Category;
import com.pos.passport.model.Customer;
import com.pos.passport.model.Device;
import com.pos.passport.model.EmailSetting;
import com.pos.passport.model.ItemButton;
import com.pos.passport.model.Modifier;
import com.pos.passport.model.OfflineStats;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.ReportCartCounter;
import com.pos.passport.model.SectionItem;
import com.pos.passport.model.Shift;
import com.pos.passport.model.StoreReceiptHeader;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.TaxSetting;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.Consts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class ProductDatabase {
    private String[] productString;

    private ArrayList<Category> Catagories;
    private ArrayList<String> CatagoryString;
    public ProductDatabaseHelper helper;

    private final String TAG = "ProductDatabase";

    private final String PRODUCT_TABLE = "products";
    private final String MODIFIER_TABLE = "modifiers";
    private final String CATAGORY_TABLE = "catagories";
    private final String SALES_TABLE = "sales";
    private final String OPEN_SALES_TABLE = "opensales";
    private final String TAX_TABLE = "taxes";
    private final String ADMIN_TABLE = "adminsettings";
    private final String STORE_TABLE = "storesettings";
    private final String EMAIL_TABLE = "emailsettings";
    private final String RECEIPT_TABLE = "receiptsettings";
    private final String CUSTOMER_TABLE = "customers";
    private final String CASHIER_TABLE = "cashiers";
    private final String BUTTON_TABLE = "buttons";
    private final String Ostatus_TABLE = "orderstatus";
    private final String MERCURY_TABLE = "mercury";
    private final String CCSALES_TABLE = "ccsales";
    private final String SHIFT_TABLE = "endshift";
    private final String DAY_TABLE = "endday";
    private final String DEVICE_TABLE = "hardwaredevice";
    private final String CREDIT_SALE_TABLE = "creditsale";

    private String DATABASE_NAME = "passport.db";
    private final int DATABASE_VERSION = 8;

    private SQLiteDatabase db;
    private Context context;
    private boolean inDepartment;
    private boolean inProducts;

    private static ProductDatabase mProductDataBase;

    public void clearTable() {
        getDb().delete(BUTTON_TABLE, null, null);
        getDb().delete(STORE_TABLE, null, null);
        getDb().delete(CATAGORY_TABLE, null, null);
        getDb().delete(TAX_TABLE, null, null);
        getDb().delete(MERCURY_TABLE, null, null);
        getDb().delete(CATAGORY_TABLE, null, null);
        getDb().delete(ADMIN_TABLE, null, null);
        getDb().delete(CASHIER_TABLE, null, null);
        getDb().delete(PRODUCT_TABLE, null, null);
        getDb().delete(BUTTON_TABLE, null, null);
        getDb().delete(DAY_TABLE, null, null);
        getDb().delete(Ostatus_TABLE, null, null);
        getDb().delete(CUSTOMER_TABLE, null, null);

    }

    private ProductDatabase(Context context) {
        this.context = context;
        helper = new ProductDatabaseHelper(context);
        if (getDb() == null) {
            setDb(helper.getWritableDatabase());
        } else {
            if (!getDb().isOpen())
                setDb(helper.getWritableDatabase());
        }

        findCats();
        buildCatString();
    }

    public static ProductDatabase getInstance(Context context) {
        if (mProductDataBase == null)
            mProductDataBase = new ProductDatabase(context.getApplicationContext());

        return mProductDataBase;
    }

    private void buildCatString() {
        setCatagoryString(new ArrayList<String>());
        for (int i = 0; i < getCatagories().size(); i++) {
            Category item = getCatagories().get(i);
            getCatagoryString().add(item.getName());
        }
    }

    public void findCats() {
        setCatagories(new ArrayList<Category>());

        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE + " WHERE deleted='0'", null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Category product = new Category();

            product.setName(c.getString(c.getColumnIndex("name")));
            product.setId(c.getInt(c.getColumnIndex("_id")));
            product.setTaxable1(c.getInt(c.getColumnIndex("tax1")) != 0);
            product.setTaxable2(c.getInt(c.getColumnIndex("tax2")) != 0);
            product.setTaxable3(c.getInt(c.getColumnIndex("tax3")) != 0);
            product.setTaxarray(c.getString(c.getColumnIndex("taxarray")));
            getCatagories().add(product);

            c.moveToNext();
        }

        c.close();
    }

    public ArrayList<Category> getCats() {

        ArrayList<Category> categories = new ArrayList<>();

        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE + " WHERE deleted='0' order by name", null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Category product = new Category();

            product.setName(c.getString(c.getColumnIndex("name")));
            product.setId(c.getInt(c.getColumnIndex("_id")));
            product.setTaxable1(c.getInt(c.getColumnIndex("tax1")) != 0);
            product.setTaxable2(c.getInt(c.getColumnIndex("tax2")) != 0);
            product.setTaxable3(c.getInt(c.getColumnIndex("tax3")) != 0);
            product.setTax(c.getInt(c.getColumnIndex("tax")));
            product.setTaxarray(c.getString(c.getColumnIndex("taxarray")));
            categories.add(product);

            c.moveToNext();
        }

        c.close();

        return categories;
    }

    public boolean insertModifiers(Modifier modifier) {
        ContentValues vals = new ContentValues();

        vals.put("_id", modifier.id);
        vals.put("name", modifier.name);
        vals.put("desc", modifier.desc);
        vals.put("barcode", modifier.barcode);
        vals.put("price", Integer.parseInt(modifier.price.toString()));
        vals.put("cost", Integer.parseInt(modifier.cost.toString()));
        vals.put("catid", modifier.cat);
        vals.put("deleted", modifier.deleted);
        vals.put("quantity", modifier.quantity);

        Cursor c = getDb().rawQuery("select name from " + MODIFIER_TABLE + " WHERE _id =" + modifier.id, null);
        int count = c.getCount();
        c.close();

        if (count > 0) {
            getDb().update(MODIFIER_TABLE, vals, "_id=" + modifier.id, null);
        } else {
            getDb().insert(MODIFIER_TABLE, null, vals);
        }

        return true;
    }

    public boolean insert(Product product) {
        ContentValues vals = new ContentValues();

        vals.put("_id", product.id);
        vals.put("name", product.name);
        vals.put("desc", product.desc);
        vals.put("barcode", product.barcode);
        vals.put("price", product.price.toString());//Integer.parseInt(product.price.toString()));
        vals.put("salePrice", product.salePrice.toString());//Integer.parseInt(product.salePrice.toString()));
        vals.put("saleEndDate", product.endSale);
        vals.put("saleStartDate", product.startSale);
        vals.put("cost", product.cost.toString());//Integer.parseInt(product.cost.toString()));
        vals.put("catid", product.cat);
        vals.put("quantity", product.onHand);
        vals.put("buttonID", product.buttonID);
        vals.put("quantity", product.onHand);
        vals.put("lastSold", product.lastSold);
        vals.put("lastReceived", product.lastReceived);
        vals.put("lowAmount", product.lowAmount);
        vals.put("track", product.track);
        vals.put("deleted", product.deleted);
        vals.put("combo", product.combo);
        vals.put("comboItems", product.comboItems);
        vals.put("modifiers", product.modi_data);
        vals.put("image", product.image);

        Cursor c = getDb().rawQuery("select name from " + PRODUCT_TABLE + " WHERE _id =" + product.id, null);
        int count = c.getCount();
        c.close();

        if (count > 0) {
            getDb().update(PRODUCT_TABLE, vals, "_id=" + product.id, null);
        } else {
            getDb().insert(PRODUCT_TABLE, null, vals);
        }

        return true;
    }

    public boolean insertsync(Product product, int trackable, int reco, int taxable, int isAlcoholic, int isTobaco) {
        ContentValues vals = new ContentValues();
        vals.put("_id", product.id);
        vals.put("name", product.name);
        vals.put("desc", product.desc);
        vals.put("barcode", product.barcode);
        vals.put("price", product.price.toString());//Integer.parseInt(product.price.toString()));
        //Log.e("Insert sale parice",">>>>>"+product.salePrice.toString());
        vals.put("salePrice", "" + product.salePrice);//Integer.parseInt(product.salePrice.toString()));
        vals.put("saleEndDate", product.endSale);
        vals.put("saleStartDate", product.startSale);
        vals.put("cost", product.cost.toString());//Integer.parseInt(product.cost.toString()));
        vals.put("catid", product.cat);
        vals.put("quantity", product.onHand);
        vals.put("buttonID", product.buttonID);
        vals.put("quantity", product.onHand);
        vals.put("lastSold", product.lastSold);
        vals.put("lastReceived", product.lastReceived);
        vals.put("lowAmount", product.lowAmount);
        vals.put("track", product.track);
        vals.put("deleted", product.deleted);
        vals.put("combo", product.combo);
        vals.put("comboItems", product.comboItems);
        vals.put("modifiers", product.modi_data);
        vals.put("image", product.image);
        vals.put("isTrackable", trackable);
        vals.put("reorderLevel", reco);
        vals.put("taxable", taxable);
        vals.put("isAlcoholic", isAlcoholic);
        vals.put("isTobaco", isTobaco);
        Cursor c = getDb().rawQuery("select name from " + PRODUCT_TABLE + " WHERE _id =" + product.id, null);
        int count = c.getCount();
        c.close();

        if (count > 0) {
            getDb().update(PRODUCT_TABLE, vals, "_id=" + product.id, null);
        } else {
            getDb().insert(PRODUCT_TABLE, null, vals);
        }
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
        String[] cols = new String[]{"*"};
        Cursor c = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "barcode" + " like \"%" + barcode + "%\"", null, null, null, null, null);

        if (!c.moveToFirst()) {
            c.close();
            return null; // product not found
        }

        Product product = new Product();

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
        product.modi_data = (c.getString(c.getColumnIndex("modifiers")));
        product.comboItems = (c.getString(c.getColumnIndex("comboItems")));
        product.combo = (c.getInt(c.getColumnIndex("combo")));
        int taxone = c.getInt(c.getColumnIndex("taxable"));
        if (taxone == 0)
            product.taxable = false;
        else
            product.taxable = true;
        c.close();
        return product;
    }

    public class ProductDatabaseHelper extends SQLiteOpenHelper {

        public ProductDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(PRODUCT_TABLE).append("(  ")
                    .append("   _id integer,")
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
                    .append("   quantity integer,")
                    .append("   track integer default 1,")
                    .append("   deleted integer,")
                    .append("   combo integer default 0,")//new
                    .append("   comboItems text,")//new
                    .append("   modifiers text,")//new
                    .append("   image text,")//new
                    .append("   isTrackable integer,")//new
                    .append("   reorderLevel integer,")//new
                    .append("   taxable integer,")//new
                    .append("   isAlcoholic integer,")//new
                    .append("   isTobaco integer")//new
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("create table ").append(MODIFIER_TABLE).append("(  ")
                    .append("   _id integer,")
                    .append("   barcode text,")
                    .append("   version integer,")
                    .append("   name text,")
                    .append("   desc text,")
                    .append("   catid integer,")
                    .append("   price integer,")
                    .append("   cost integer,")
                    .append("   quantity integer,")
                    .append("   deleted integer")
                    .append(")  ");

            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(CATAGORY_TABLE).append("(  ")
                    .append("   _id integer,")
                    .append("   name text,")
                    .append("   desc text,")
                    .append("   tax1 integer,")
                    .append("   tax2 integer,")
                    .append("   tax3 integer,")
                    .append("   deleted integer default 0,")
                    .append("   tax integer,")//new
                    .append("   taxarray text")//new
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(SALES_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   date text,")
                    .append("   customerID integer,")
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
                    .append("   isReceive integer,")
                    .append("   processed integer,")
                    .append("   holdName text,")
                    .append("   status text,")
                    .append("   totaldiscountname text,")
                    .append("   totaldiscountamount float,")
                    .append("   returnReason text,")
                    .append("   corder integer default 0,")
                    .append("   returnStatus integer default 0,")
                    .append("   totalReturn integer")
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());
            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(OPEN_SALES_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   orderId integer,")
                    .append("   orderPaidDate text,")
                    .append("   orderSubTotal float,")
                    .append("   orderTax float,")
                    .append("   orderTip float,")
                    .append("   orderServiceCharge float,")
                    .append("   orderTotal float,")
                    .append("   zoneID text,")
                    .append("   paymentAuth text,")
                    .append("   orderType integer,")
                    .append("   token integer,")
                    .append("   paymentSource text,")
                    .append("   section text,")
                    .append("   row text,")
                    .append("   customerName text,")
                    .append("   seat text,")
                    .append("   orderStatus text,")
                    .append("   orderStatusId integer,")
                    .append("   orderItems text")
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
                    .append("   receiptprintoption integer,")
                    .append("   serverId integer,")
                    .append("   blurb text")
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(TAX_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   name1 text,").append("   name2 text,").append("   name3 text,")
                    .append("   tax1 float,").append("   tax2 float,").append("   tax3 float,")
                    .append("   taxId integer,").append("   taxName text,").append("   taxPercent float")
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(STORE_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   name text,")
                    .append("   address1 text,")
                    .append("   address2 text,")
                    .append("   phone text,")
                    .append("   email text,")
                    .append("   website text,")
                    .append("   receipt_header text,")
                    .append("   receipt_footer text,")
                    .append("   clearsale int,")
                    .append("   print_sig int,")
                    .append("   capture_sig int,")
                    .append("   currency text,")
                    .append("   header_type int,")
                    .append("   image text,")
                    .append("   city text,")
                    .append("   state text")
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());
            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(BUTTON_TABLE).append("(  ")
                    .append("   _id integer,")
                    .append("   type int,")
                    .append("   orderBy int,")
                    .append("   parent int,")
                    .append("   productID int,")
                    .append("   departID int,")
                    .append("   folderName text,")
                    .append("   link text,")
                    .append("   image blob,")
                    .append("   price text,")
                    .append("   deleted integer,")
                    .append("   saleStartDate text,")
                    .append("   saleEndDate text,")
                    .append("   salePrice text,")
                    .append("   isTrackable integer,")
                    .append("   reorderLevel integer")
                    .append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            String insert = "INSERT INTO buttons (_id, type, orderBy, parent) VALUES ('1', '-1','-1','-1')";
            db.execSQL(insert);

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(ADMIN_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   enabled integer,").append("   password text,")
                    .append("   hint text,")
                    .append("   userid text").append(")  ");
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

            sql = new StringBuilder();//.append("   _id integer primary key,")
            // @formatter:off
            sql.append("create table ").append(CUSTOMER_TABLE).append("(  ")
                    .append("   _id integer primary key,")
                    .append("   fname text,").append("   lname text,")
                    .append("   phone text,").append("   street text,")
                    .append("   city text,").append("   email text,")
                    .append("   region text,").append("   postal text,")
                    .append("   numsales integer,")
                    .append("   numreturns integer,")
                    .append("   deleted integer,")
                    .append("   total integer,")
                    .append("   id integer")
                    .append(")  ");

            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(MERCURY_TABLE)
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
            // @formatter:off
            sql.append("create table ").append(DAY_TABLE)
                    .append("(  ")
                    .append("   _id integer primary key,")
                    .append("   day_id integer,")
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
                    .append("   deleted integer default 0,")
                    .append("   permissionReturn integer,")
                    .append("   permissionPriceModify integer,")
                    .append("   permissionReports integer,")
                    .append("   permissionInventory integer,")
                    .append("   permissionSettings integer,")
                    .append("   permissionVoideSale integer,")
                    .append("   permissionProcessTender integer,")
                    .append("   total integer,")
                    .append("   admin integer").append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(Ostatus_TABLE).append("(  ")
                    .append("   id integer,")
                    .append("   status text").append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            // @formatter:off
            sql.append("create table ").append(DEVICE_TABLE).append("(  ")
                    .append("   _id integer,")
                    .append("   type text,")
                    .append("   name text,")
                    .append("   ipAddress text,")
                    .append("   port text").append(")  ");
            // @formatter:on
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("create table ").append(CREDIT_SALE_TABLE).append("( ")
                    .append("  _id integer,")
                    .append("  cardType text,")
                    .append("  date text,")
                    .append("  lastFour text,")
                    .append("  invoice text,")
                    .append("  amount float,")
                    .append("  saleType text,")
                    .append("  tipAmount text,")
                    .append("  offline integer,")
                    .append("  response text").append(" )");

            db.execSQL(sql.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (oldVersion < 2) {
                try {
                    db.execSQL("ALTER TABLE " + DAY_TABLE + " ADD COLUMN day_id");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN day_id", "day_id already exists");
                }
            }

            if (oldVersion < 3) {
                try {
                    db.execSQL("ALTER TABLE " + MERCURY_TABLE + " ADD COLUMN hostedPass");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN cutcode", "hostedPass already exists");
                }

                try {
                    db.execSQL("ALTER TABLE " + MERCURY_TABLE + " ADD COLUMN hostedMID");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN drawercode", "hostedMID already exists");
                }

                try {
                    db.execSQL("ALTER TABLE " + CCSALES_TABLE + " ADD COLUMN saleid");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN cutcode", "saleid already exists");
                }
            }

            if (oldVersion < 4) {
                try {
                    db.execSQL("ALTER TABLE " + PRODUCT_TABLE + " ADD COLUMN track DEFAULT 1");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN track", "track already exists");
                }
            }

            if (oldVersion < 5) {
                try {
                    db.execSQL("ALTER TABLE " + SALES_TABLE + " ADD COLUMN isReceive DEFAULT 0");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN isReceive", "isReceive already exists");
                }
            }

            if (oldVersion < 6) {
                try {
                    db.execSQL("ALTER TABLE " + STORE_TABLE + " ADD COLUMN print_sig DEFAULT 0");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN print_sig", "print_sig already exists");
                }

                try {
                    db.execSQL("ALTER TABLE " + STORE_TABLE + " ADD COLUMN capture_sig DEFAULT 0");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN capture_sig", "capture_sig already exists");
                }
            }
            if (oldVersion < 7) {
                try {
                    db.execSQL("ALTER TABLE " + CASHIER_TABLE + " ADD COLUMN permissionVoideSale DEFAULT 0");
                    db.execSQL("ALTER TABLE " + CASHIER_TABLE + " ADD COLUMN permissionProcessTender DEFAULT 0");
                    db.execSQL("ALTER TABLE " + CREDIT_SALE_TABLE + " ADD COLUMN offline DEFAULT 0");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN Permission", "Permissions already exists");
                }
            }
            if (oldVersion < 8) {
                try {
                    db.execSQL("ALTER TABLE " + RECEIPT_TABLE + " ADD COLUMN serverId DEFAULT 0");
                } catch (SQLException e) {
                    Log.i("ADD COLUMN Permission", "Permissions already exists");
                }
            }
        }

        public Cursor fetchItemsQuantity(String inputText) throws SQLException {
            Log.v("product text", inputText);
            Cursor mCursor = null;
            if (inputText.length() > 2) {

                mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"name", "quantity"}, "deleted=0 AND (name" + " like '" + inputText + "%' OR barcode like '" + inputText + "%')", null,
                        null, null, "name", null);
                if (mCursor != null) {
                    mCursor.moveToNext();
                }

            }
            return mCursor;
        }

        public Cursor fetchProds() throws SQLException {
            Cursor mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0", null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
            return mCursor;
        }

        public Cursor fetchNamedProds(String inputText) throws SQLException {
            Cursor mCursor = null;

            if (inputText != null && inputText.length() > 0) {
                String formatedText = inputText.replaceAll("'", "''");
                mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 ", null,
                        null, null, null, null);
            } else {
                mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0", null);
            }

            if (mCursor != null) {
                mCursor.moveToFirst();
            }
            return mCursor;
        }
        public Cursor fetchNamedProdsLimit(String inputText) throws SQLException {
            Cursor mCursor = null;

            if (inputText != null && inputText.length() > 0) {
                String formatedText = inputText.replaceAll("'", "''");
                mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"name", "barcode", "_id", "quantity"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 LIMIT 10", null,
                        null, null, null, null);
            } else {
                mCursor = getDb().rawQuery("select name, barcode, _id, quantity from " + PRODUCT_TABLE + " WHERE deleted=0", null);
            }

            if (mCursor != null) {
                mCursor.moveToFirst();
            }
            return mCursor;
        }
        public Cursor fetchNamedCat(String inputText) {
            Cursor mCursor = null;

            if (inputText != null && inputText.length() > 0) {
                String formatedText = inputText.replaceAll("'", "''");
                mCursor = getDb().rawQuery("select * from " + CATAGORY_TABLE + " WHERE deleted=0 AND name like '%" + formatedText + "%'", null);
            } else {
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

            mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "deleted = 0 AND quantity <= reorderLevel AND track = 1", null,
                    null, null, null, null);

            if (mCursor != null) {
                mCursor.moveToFirst();
            }

            return mCursor;
        }

        public Cursor fetchNamedProds(String inputText, String selectedItem) {
            Cursor mCursor = null;

            int depID = getCatId(selectedItem);

            if (inputText.length() < 1) {
                mCursor = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0 AND catid =" + depID, null);
            } else {
                String formatedText = inputText.replaceAll("'", "''");

                mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 AND catid = " + depID, null,
                        null, null, null, null);
            }

            if (mCursor != null) {
                mCursor.moveToFirst();
            }

            return mCursor;
        }
        public Cursor fetchNamedProdsLimit(String inputText, String selectedItem) {
            Cursor mCursor = null;

            int depID = getCatId(selectedItem);

            if (inputText.length() < 1) {
                mCursor = getDb().rawQuery("select name, barcode, _id, quantity from " + PRODUCT_TABLE + " WHERE deleted=0 AND catid =" + depID+" LIMIT 10", null);
            } else {
                String formatedText = inputText.replaceAll("'", "''");

                mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"name", "barcode", "_id", "quantity"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 AND catid = " + depID+" LIMIT 10", null,
                        null, null, null, null);
            }

            if (mCursor != null) {
                mCursor.moveToFirst();
            }

            return mCursor;
        }
        public List<Product> getNamedProds(String inputText) throws SQLException
        {
            List<Product> mProductsData=new ArrayList<>();
            Cursor c = null;

//            if (inputText != null && inputText.length() > 0) {
//                String formatedText = inputText.replaceAll("'", "''");
//                c = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0", null,
//                        null, null, null, null);
//            } else {
//                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0", null);
//            }
            if(inputText != null && inputText.length()>0)
            {
                int _id=Integer.parseInt(inputText);
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0 AND _id="+_id, null);
            }else{
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0", null);
            }

            if (c != null)
            {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Product product = new Product();
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
                    mProductsData.add(product);
                    c.moveToNext();
                }
            }
            return mProductsData;
        }
        public Cursor getProductsData(String inputText) throws SQLException
        {
            Cursor c = null;
           if(inputText != null && inputText.length()>0)
            {
                int _id=Integer.parseInt(inputText);
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0 AND _id="+_id, null);
            }else{
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0", null);
            }

            if (c != null)
            {
                c.moveToFirst();

            }
            return c;
        }
        public List<Product> getProdsNamesWithCat(String inputText, String selectedItem)
        {
            List<Product> mProductsData=new ArrayList<>();
            Cursor c = null;

            int depID = getCatId(selectedItem);

            if (inputText.length() < 1) {
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0 AND catid =" + depID, null);
            } else {
                String formatedText = inputText.replaceAll("'", "''");

                c = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 AND catid = " + depID, null,
                        null, null, null, null);
            }

            if (c != null)
            {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Product product = new Product();
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
                    mProductsData.add(product);
                    c.moveToNext();
                }
            }
            return mProductsData;
        }
        public Cursor getProductsWithCat(String inputText, String selectedItem)
        {
            Cursor c = null;

            int depID = getCatId(selectedItem);
              if (inputText.length() < 1) {
                c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE deleted=0 AND catid =" + depID, null);
            } else {
                String formatedText = inputText.replaceAll("'", "''");

                c = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "(name like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%') AND deleted=0 AND catid = " + depID, null,
                        null, null, null, null);
            }

            if (c != null)
            {
                c.moveToFirst();
            }
            return c;
        }
    }

    public Cursor fetchCustomers(String inputText) {
        Cursor mCursor = null;

        Log.v("Customers", inputText);

        if (inputText.length() < 1) {
            mCursor = getDb().rawQuery("select * from " + CUSTOMER_TABLE, null);
        } else {
            String formatedText = inputText.replaceAll("'", "''");

            mCursor = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, "_id = " + formatedText, null,
                    null, null, null, null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Customer fetchCustomer(int id) {
        Customer customer = null;
        String inputText = String.valueOf(id);
        Log.e("Customers", inputText);

        String formatedText = inputText.replaceAll("'", "''");
        Cursor mCursor = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, "_id = " + formatedText, null,
                null, null, null, null);
        mCursor.moveToFirst();
        while (!mCursor.isAfterLast()) {
            customer = new Customer();
            customer.setEmail(mCursor.getString(mCursor.getColumnIndex("email")));
            customer.setLName(mCursor.getString(mCursor.getColumnIndex("lname")));
            customer.setFName(mCursor.getString(mCursor.getColumnIndex("fname")));
            customer.setId(mCursor.getInt(mCursor.getColumnIndex("_id")));
            mCursor.moveToNext();
        }
        mCursor.close();
        return customer;
    }

    public void RemoveProduct(int position) {
        getDb().delete(PRODUCT_TABLE, "_id=" + position, null);
        getDb().delete(BUTTON_TABLE, "productID=" + position, null);
    }

    public void setCatagoryString(ArrayList<String> catagoryString) {
        CatagoryString = catagoryString;
    }

    public ArrayList<String> getCatagoryString() {
        return CatagoryString;
    }

    public void setCatagories(ArrayList<Category> catagories) {
        Catagories = catagories;
    }

    public ArrayList<Category> getCatagories() {
        return Catagories;
    }

    public boolean insertCat(Category newprod) {
        ContentValues vals = new ContentValues();
        vals.put("_id", newprod.getId());
        vals.put("name", newprod.getName());
        vals.put("tax1", newprod.getTaxable1());
        vals.put("tax2", newprod.getTaxable2());
        vals.put("tax3", newprod.getTaxable3());
        vals.put("taxarray", newprod.getTaxarray());
        vals.put("tax", newprod.getTax());
        vals.put("deleted", newprod.deleted);

        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE + " WHERE _id =" + newprod.getId(), null);

        if (c.getCount() > 0) {
            getDb().update(CATAGORY_TABLE, vals, "_id=" + newprod.getId(), null);
        } else {
            getDb().insert(CATAGORY_TABLE, null, vals);
        }

        c.close();

        return true;
    }

    public boolean insertOpenOrder(ContentValues vals, int id) {
        Cursor c = getDb().rawQuery("select * from " + OPEN_SALES_TABLE + " WHERE orderId =" + id, null);

        if (c.getCount() > 0) {
            getDb().update(OPEN_SALES_TABLE, vals, "orderId=" + id, null);
        } else {
            getDb().insert(OPEN_SALES_TABLE, null, vals);
        }

        c.close();

        return true;
    }

    public boolean insertTax(int taxid, String taxname, float taxpercent) {
        ContentValues vals = new ContentValues();

        /*vals.put("name1", TaxSetting.getTax1Name());
        vals.put("name2", TaxSetting.getTax2Name());
        vals.put("name3", TaxSetting.getTax3Name());

        vals.put("tax1", TaxSetting.getTax1());
        vals.put("tax2", TaxSetting.getTax2());
        vals.put("tax3", TaxSetting.getTax3());*/
        //.append("   taxId text,").append("   taxName text,").append("   taxPercent float")
        //vals.put("taxId", TaxSetting.getTaxId());
        //vals.put("taxName", TaxSetting.getTaxname());
        //vals.put("taxPercent", TaxSetting.getTaxpercent());
        vals.put("taxId", taxid);
        vals.put("taxName", taxname);
        vals.put("taxPercent", taxpercent);
        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE + " WHERE taxId =" + taxid, null);
        //Log.e("Count tax","Getcount>>>"+c.getCount());
        if (c.getCount() > 0) {
            getDb().update(TAX_TABLE, vals, "taxId =" + taxid, null);
        } else {
            getDb().insert(TAX_TABLE, null, vals);
        }
        c.close();
        return true;
    }

    /*public boolean insertTax() {
        ContentValues vals = new ContentValues();

        *//*vals.put("name1", TaxSetting.getTax1Name());
        vals.put("name2", TaxSetting.getTax2Name());
        vals.put("name3", TaxSetting.getTax3Name());

        vals.put("tax1", TaxSetting.getTax1());
        vals.put("tax2", TaxSetting.getTax2());
        vals.put("tax3", TaxSetting.getTax3());*//*
        //.append("   taxId text,").append("   taxName text,").append("   taxPercent float")
        vals.put("taxId", TaxSetting.getTaxId());
        vals.put("taxName", TaxSetting.getTaxname());
        vals.put("taxPercent", TaxSetting.getTaxpercent());
        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE, null);
        if (c.getCount() > 0)
        {
            getDb().update(TAX_TABLE, vals, "taxId="+TaxSetting.getTaxId(), null);
        } else
        {
            getDb().insert(TAX_TABLE, null, vals);
        }
        c.close();
        return true;
    }*/
    public void findTax() {
        TaxSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            /*TaxSetting.setTax1Name(c.getString(c.getColumnIndex("name1")));
            TaxSetting.setTax2Name(c.getString(c.getColumnIndex("name2")));
            TaxSetting.setTax3Name(c.getString(c.getColumnIndex("name3")));

            TaxSetting.setTax1(c.getFloat(c.getColumnIndex("tax1")));
            TaxSetting.setTax2(c.getFloat(c.getColumnIndex("tax2")));
            TaxSetting.setTax3(c.getFloat(c.getColumnIndex("tax3")));*/

            //TaxSetting.setTaxname(c.getString(c.getColumnIndex("taxName")));
            //TaxSetting.setTaxId(c.getInt(c.getColumnIndex("taxId")));
            //TaxSetting.setTaxpercent(c.getFloat(c.getColumnIndex("taxPercent")));
            c.moveToNext();
        }
        c.close();
    }

    public List<TaxSetting> findTaxData() {
        List<TaxSetting> taxdata = new ArrayList<>();
        TaxSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + TAX_TABLE, null);
        c.moveToFirst();

        do //while (!c.isAfterLast())
        {
            /*TaxSetting.setTax1Name(c.getString(c.getColumnIndex("name1")));
            TaxSetting.setTax2Name(c.getString(c.getColumnIndex("name2")));
            TaxSetting.setTax3Name(c.getString(c.getColumnIndex("name3")));

            TaxSetting.setTax1(c.getFloat(c.getColumnIndex("tax1")));
            TaxSetting.setTax2(c.getFloat(c.getColumnIndex("tax2")));
            TaxSetting.setTax3(c.getFloat(c.getColumnIndex("tax3")));*/

            //TaxSetting.setTaxname(c.getString(c.getColumnIndex("taxName")));
            //TaxSetting.setTaxId(c.getInt(c.getColumnIndex("taxId")));
            //TaxSetting.setTaxpercent(c.getFloat(c.getColumnIndex("taxPercent")));
            taxdata.add(new TaxSetting(c.getString(c.getColumnIndex("taxName")), c.getInt(c.getColumnIndex("taxId")), c.getFloat(c.getColumnIndex("taxPercent"))));
            //c.moveToNext();
        } while (c.moveToNext());
        c.close();

        return taxdata;
    }
	/*public int getProdId(String prod_name) {
        
		int result = 0;
        String[] cols = new String[] {"_id", "name"};        
        Cursor c = getDb().query(PRODUCT_TABLE, cols, "name = ?", new String[] { prod_name }, null, null, null);
        if(c.moveToFirst()){
        	result =  c.getInt(c.getColumnIndex("_id"));
        }
        c.close(); 
		
		return result;
	}*/

    public int getCatId(String catagory) {

        int result = 0;
        String[] cols = new String[]{"_id", "name"};
        Cursor c = getDb().query(CATAGORY_TABLE, cols, "name = ?", new String[]{catagory}, null, null, null);
        if (c.moveToFirst()) {
            result = c.getInt(c.getColumnIndex("_id"));
        }
        c.close();

        return result;
    }

    public String getCatById(int cat) {
        String result = null;
        String[] cols = new String[]{"_id", "name"};
        Cursor c = getDb().query(CATAGORY_TABLE, cols, "_id = ?", new String[]{"" + cat}, null, null, null);
        if (c.moveToFirst()) {
            result = c.getString(c.getColumnIndex("name"));
        }
        c.close();

        if (result == null)
            result = "No Department";
        return result;
    }

    public Category getDepartmentById(int id) {
        Category category = null;
        String[] cols = new String[]{"_id", "name"};
        Cursor c = getDb().query(CATAGORY_TABLE, cols, "_id = ?", new String[]{"" + id}, null, null, null);
        if (c.moveToFirst()) {
            category = new Category();
            category.setId(c.getInt(c.getColumnIndex("_id")));
            category.setName(c.getString(c.getColumnIndex("name")));
            return category;
        }
        c.close();
        return category;
    }

    public void RemoveCatagory(int position, int position2) {
        getDb().delete(CATAGORY_TABLE, "_id=" + position, null);
        findCats();
        buildCatString();
    }

    public void setCat(Category newprod) {

        ContentValues vals = new ContentValues();
        vals.put("name", newprod.getName());
        vals.put("tax1", newprod.getTaxable1());
        vals.put("tax2", newprod.getTaxable2());
        vals.put("tax3", newprod.getTaxable3());

        getDb().update(CATAGORY_TABLE, vals, "_id=" + newprod.getId(), null);

        findCats();
        buildCatString();
    }

    public void replaceItem(Product newprod) {
        ContentValues vals = new ContentValues();
        vals.put("name", newprod.name);
        vals.put("desc", newprod.desc);
        vals.put("barcode", newprod.barcode);
        //vals.put("price", Integer.parseInt(newprod.price.toString()));
        vals.put("price", newprod.price.toString());
        // vals.put("salePrice", Integer.parseInt(newprod.salePrice.toString()));
        vals.put("salePrice", newprod.salePrice.toString());
        vals.put("saleEndDate", newprod.endSale);
        vals.put("saleStartDate", newprod.startSale);
        // vals.put("cost", Integer.parseInt(newprod.cost.toString()));
        vals.put("cost", newprod.cost.toString());
        vals.put("catid", newprod.cat);
        vals.put("quantity", newprod.onHand);
        vals.put("buttonID", newprod.buttonID);
        vals.put("quantity", newprod.onHand);
        vals.put("lastSold", newprod.lastSold);
        vals.put("lastReceived", newprod.lastReceived);
        vals.put("lowAmount", newprod.lowAmount);
        vals.put("track", newprod.track);

        getDb().update(PRODUCT_TABLE, vals, "_id=" + newprod.id, null);
    }

    public void replaceSale(ReportCart cart) {
        ContentValues vals = new ContentValues();
        vals.put("_id", cart.getId());
        vals.put("date", cart.getDate());
        vals.put("trans", cart.trans.toString());
        vals.put("lineitems", cart.getCartItems());
        vals.put("subtotal", cart.mSubtotal.toString());
        vals.put("tax1", cart.mTax1.toString());
        vals.put("tax2", cart.mTax2.toString());
        vals.put("tax3", cart.mTax3.toString());
        if (cart.mVoided)
            vals.put("voided", 1);
        else
            vals.put("voided", 0);
        if (cart.mOnHold)
            vals.put("onHold", 1);
        else
            vals.put("onHold", 0);
        if (cart.mIsReceived)
            vals.put("isReceive", 1);
        else
            vals.put("isReceive", 0);
        if (cart.mCashier != null)
            vals.put("cashierID", cart.mCashier.id);
        else
            vals.put("cashierID", 0);
        vals.put("processed", cart.mIsProcessed);
        vals.put("holdName", cart.mName);
        vals.put("taxpercent1", cart.getTax1Percent().toString());
        vals.put("taxpercent2", cart.getTax2Percent().toString());
        vals.put("taxpercent3", cart.getTax3Percent().toString());
        vals.put("taxname1", cart.getTax1Name());
        vals.put("taxname2", cart.getTax2Name());
        vals.put("taxname3", cart.getTax3Name());
        vals.put("processed", cart.processed);
        vals.put("total", cart.mTotal.toString());
        vals.put("status", cart.mStatus);
        vals.put("totaldiscountname", cart.mDiscountName);
        vals.put("totaldiscountamount", cart.mDiscountAmount.toString());
        vals.put("corder", cart.cOrder);
        if (cart.hasCustomer())
            vals.put("customerID", cart.getCustomer().id);
        else
            vals.put("customerID", 0);

        getDb().update(SALES_TABLE, vals, "_id=" + cart.getId(), null);
    }

    public long insertSale(Cart cart, String result, int cid, int checkRecent) {
        ContentValues vals = new ContentValues();
        long currentDateTime = new Date().getTime();
        cart.mDate = currentDateTime;
        vals.put("date", cart.mDate);
        vals.put("trans", cart.mTrans.toString());
        vals.put("lineitems", result);
        vals.put("subtotal", cart.mSubtotal.toString());
        vals.put("tax1", cart.mTax1.toString());
        vals.put("tax2", cart.mTax2.toString());
        vals.put("tax3", cart.mTax3.toString());
        if (cart.mVoided)
            vals.put("voided", 1);
        else
            vals.put("voided", 0);
        if (cart.mOnHold)
            vals.put("onHold", 1);
        else
            vals.put("onHold", 0);
        if (cart.mIsReceived)
            vals.put("isReceive", 1);
        else
            vals.put("isReceive", 0);
        if (cart.mCashier != null)
            vals.put("cashierID", cart.mCashier.id);
        else
            vals.put("cashierID", cid);

        //vals.put("cashierID", 0);

        vals.put("processed", cart.mIsProcessed);
        vals.put("holdName", cart.mName);
        vals.put("taxpercent1", cart.mTax1Percent.toString());
        vals.put("taxpercent2", cart.mTax2Percent.toString());
        vals.put("taxpercent3", cart.mTax3Percent.toString());
        vals.put("taxname1", cart.mTax1Name);
        vals.put("taxname2", cart.mTax2Name);
        vals.put("taxname3", cart.mTax3Name);
        vals.put("total", cart.mTotal.toString());
        vals.put("status", cart.mStatus);
        vals.put("totaldiscountname", cart.mDiscountName);
        vals.put("totaldiscountamount", cart.mDiscountAmount.toString());
        vals.put("returnReason", cart.mReturnReason);
        vals.put("corder", cart.cOrder);
        vals.put("returnStatus", cart.returnStatus);
        vals.put("totalReturn", cart.mTotal.toString());


        if (cart.hasCustomer())
            vals.put("customerID", cart.getCustomer().id);
        else
            vals.put("customerID", 0);

        if (checkRecent == 1) {
            cart.mId = getDb().insert(SALES_TABLE, null, vals);
        } else {
            Cursor c = getDb().rawQuery("select date from " + SALES_TABLE + " WHERE _id =" + cart.mId, null);
            int count = c.getCount();
            c.close();

            if (count > 0) {
                getDb().update(SALES_TABLE, vals, "_id=" + cart.mId, null);
            } else {
                cart.mId = getDb().insert(SALES_TABLE, null, vals);
            }
        }
        return cart.mId;
    }

    public String UpdateRecentSaleStatus(String transid, int returnstatus, String totalreturn) {
        ContentValues vals = new ContentValues();
        vals.put("totalReturn", totalreturn);
        vals.put("returnStatus", returnstatus);
        //int updatekey= getDb().update(SALES_TABLE, vals, "trans='" + transid +"'", null);
        int updatekey = getDb().update(SALES_TABLE, vals, "trans=" + transid, null);
        //Log.e("Update key","update key >>>>"+updatekey);
        return transid;
    }

    public Cursor getModifierByID(int id) {

        Modifier modifier = new Modifier();
        String[] cols = new String[]{"*"};
        String whereClause = "deleted = 0 AND _id=" + id;
        String order = "name";
        Cursor c = getDb().query(MODIFIER_TABLE, cols, whereClause, null, null, null, order, null);
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            return c;
        }
        return null;
    }

    public ArrayList<Modifier> getModifiers() {
        ArrayList<Modifier> modifiers = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "deleted = 0";
        String order = "name";
        Cursor c = getDb().query(MODIFIER_TABLE, cols, whereClause, null, null, null, order, null);
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();

        while (!c.isAfterLast()) {
            modifiers.add(CursorHelper.parseModifier(c));
            c.moveToNext();
        }
        c.close();
        return modifiers;
    }

    public ArrayList<ReportCart> getRecentTransactions() {
        ArrayList<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "onHold != 1 AND processed = 1";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, order, "10");
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));

            c.moveToNext();
        }

        c.close();

        return cart;
    }

    public ArrayList<ReportCart> getRecentTransactionsById(String transid) {
        ArrayList<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 0 AND trans =" + transid;
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, order);
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));

            c.moveToNext();
        }

        c.close();

        return cart;
    }

    public List<ItemOpen> getRecentTransactionsNew() {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 0";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, order, "10");
        Log.v("Count", "" + c.getCount());
        String sector_show = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // ReportCart cartReport = new ReportCart(context);
            String section_name = c.getString(c.getColumnIndex("date"));

            if (sector_show.equalsIgnoreCase("")) {
                //sector_show = c.getString(c.getColumnIndex("date"));
                String section_name1 = c.getString(c.getColumnIndex("date"));
                sector_show = section_name1;
                cart.add(new SectionItem(sector_show));
            } else {
                if (getDate(Long.parseLong(c.getString(c.getColumnIndex("date"))), Long.parseLong(sector_show))) {
                    String section_name1 = c.getString(c.getColumnIndex("date"));
                    sector_show = section_name1;
                    cart.add(new SectionItem(sector_show));
                } else {

                }
            }

            ReportCartCounter cartReport = new ReportCartCounter(context);
            cart.add(CursorHelper.parseReportCartCounter(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public List<ItemOpen> getSearchRecentTransactions(String input) {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String formatedText = input.replaceAll("'", "''");
        Log.e("text", formatedText);
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 0 AND ( trans like '%" + formatedText + "%')";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, order, "10");
        Log.e("Count", "Search count" + c.getCount());
        String sector_show = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // ReportCart cartReport = new ReportCart(context);
            String section_name = c.getString(c.getColumnIndex("date"));

            if (sector_show.equalsIgnoreCase("")) {
                //sector_show = c.getString(c.getColumnIndex("date"));
                String section_name1 = c.getString(c.getColumnIndex("date"));
                sector_show = section_name1;
                cart.add(new SectionItem(sector_show));
            } else {
                if (getDate(Long.parseLong(c.getString(c.getColumnIndex("date"))), Long.parseLong(sector_show))) {
                    String section_name1 = c.getString(c.getColumnIndex("date"));
                    sector_show = section_name1;
                    cart.add(new SectionItem(sector_show));
                } else {

                }
            }
            ReportCartCounter cartReport = new ReportCartCounter(context);
            cart.add(CursorHelper.parseReportCartCounter(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public ArrayList<ReportCart> getRecentTransactions(String transNo) {
        ArrayList<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "onHold !=1 AND processed = 1 AND trans = " + transNo;
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, order, "10");
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public ReportCart getTransactionById(String id) {
        String[] cols = new String[]{"*"};
        String whereClause = "_id = " + id;
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, null, null);
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        ReportCart cartReport = new ReportCart(context);
        cartReport = CursorHelper.parseReportCart(cartReport, c);
        c.close();

        return cartReport;
    }

    public List<ReportCart> getAllTransactions() {
        List<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "onHold !=1";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, order, "10");
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));
            c.moveToNext();
        }

        c.close();

        return cart;
    }

    public void updateSaleStatus(String id, int status) {
        ContentValues vals = new ContentValues();
        vals.put("processed", status);

        getDb().update(SALES_TABLE, vals, "_id=" + id, null);
    }

    public void updateSaleStatus(String id, int status, String lineItems) {
        ContentValues vals = new ContentValues();
        vals.put("processed", status);
        vals.put("lineitems", lineItems);

        getDb().update(SALES_TABLE, vals, "_id=" + id, null);
    }

    public List<ReportCart> getOpenOrders() {
        ArrayList<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "onHold = 1";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, order);
        Log.v("Count", "" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public List<ReportCart> getOpenOrdersById(String transid) {
        ArrayList<ReportCart> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 1 AND trans = " + transid;
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, order);
        Log.e("Count", "get count>>" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public List<ItemOpen> getOpenOrdersNew() {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 1";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, order);
        //Log.e("Count", "" + c.getCount());
        String sector_show = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // ReportCart cartReport = new ReportCart(context);
            //Log.e("Date get","order data"+c.getString(c.getColumnIndex("date")));
            String section_name = c.getString(c.getColumnIndex("date"));

            if (sector_show.equalsIgnoreCase("")) {
                //sector_show = c.getString(c.getColumnIndex("date"));
                String section_name1 = c.getString(c.getColumnIndex("date"));
                sector_show = section_name1;
                cart.add(new SectionItem(sector_show));
            } else {
                if (getDate(Long.parseLong(c.getString(c.getColumnIndex("date"))), Long.parseLong(sector_show))) {
                    String section_name1 = c.getString(c.getColumnIndex("date"));
                    sector_show = section_name1;
                    cart.add(new SectionItem(sector_show));
                } else {

                }
            }

            /*if(sector_show.equalsIgnoreCase(section_name))
            {

            }
            else
            {
                String section_name1=c.getString(c.getColumnIndex("date"));
                sector_show=section_name1;
                cart.add(new SectionItem(sector_show));
            }*/
            ReportCartCounter cartReport = new ReportCartCounter(context);
            cart.add(CursorHelper.parseReportCartCounter(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public List<ItemOpen> getFFOrders() {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        //String whereClause = "corder = 1";
        String order = "orderPaidDate DESC";
        Cursor c = getDb().query(OPEN_SALES_TABLE, cols, null, null, null,
                null, order);
        Log.e("Count ff order", ">>" + c.getCount());
        String sector_show = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // ReportCart cartReport = new ReportCart(context);
            String section_name = ConvertDates(c.getString(c.getColumnIndex("orderPaidDate")));
            if (sector_show.equalsIgnoreCase("")) {
                //sector_show = c.getString(c.getColumnIndex("date"));
                String section_name1 = ConvertDates(c.getString(c.getColumnIndex("orderPaidDate")));
                sector_show = section_name1;
                cart.add(new SectionItem(sector_show));
            } else {
                String getdateon = ConvertDates(c.getString(c.getColumnIndex("orderPaidDate")));
                Log.e("Date print", "Frist date>>" + getdateon);
                Log.e("date print two", "sector show date>>" + sector_show);
                if (getDate(Long.parseLong(getdateon), Long.parseLong(sector_show))) {
                    String section_name1 = ConvertDates(c.getString(c.getColumnIndex("orderPaidDate")));
                    sector_show = section_name1;
                    cart.add(new SectionItem(sector_show));
                } else {

                }
            }
            cart.add(SetFFOrderData(c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public List<ItemOpen> getFFOrderById(String transid) {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String[] cols = new String[]{"*"};
        String whereClause = "orderId = " + transid;
        String order = "orderPaidDate DESC";
        Cursor c = getDb().query(OPEN_SALES_TABLE, cols, whereClause, null, null,
                null, order);
        Log.e("FF By ID", ">>" + c.getCount());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            cart.add(SetFFOrderData(c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    public String ConvertDates(String dateget) {
        Date date = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = (Date) formatter.parse(dateget);
            System.out.println("convert date " + date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "" + date.getTime();
    }

    public OpenorderData SetFFOrderData(Cursor c) {
        OpenorderData data = new OpenorderData();
        data.setOrderId(c.getInt(c.getColumnIndex("orderId")));
        data.setCustomerName(c.getString(c.getColumnIndex("customerName")));
        data.setOrderPaidDate(c.getString(c.getColumnIndex("orderPaidDate")));
        data.setOrderStatus(c.getString(c.getColumnIndex("orderStatus")));
        data.setOrderStatusId(c.getInt(c.getColumnIndex("orderStatusId")));
        return data;
    }

    public List<ItemOpen> getSearchOpenOrdersNew(String input) {
        ArrayList<ItemOpen> cart = new ArrayList<>();
        String formatedText = input.replaceAll("'", "''");
        // Log.e("text", formatedText);
        String[] cols = new String[]{"*"};
        String whereClause = "corder = 1 AND (holdName" + " like '%" + formatedText + "%' OR trans like '%" + formatedText + "%')";
        String order = "date DESC";
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, order);
        //Log.e("Count", "Search count" + c.getCount());
        String sector_show = "";
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // ReportCart cartReport = new ReportCart(context);
            String section_name = c.getString(c.getColumnIndex("date"));

            if (sector_show.equalsIgnoreCase("")) {
                //sector_show = c.getString(c.getColumnIndex("date"));
                String section_name1 = c.getString(c.getColumnIndex("date"));
                sector_show = section_name1;
                cart.add(new SectionItem(sector_show));
            } else {
                if (getDate(Long.parseLong(c.getString(c.getColumnIndex("date"))), Long.parseLong(sector_show))) {
                    String section_name1 = c.getString(c.getColumnIndex("date"));
                    sector_show = section_name1;
                    cart.add(new SectionItem(sector_show));
                } else {

                }
            }
            ReportCartCounter cartReport = new ReportCartCounter(context);
            cart.add(CursorHelper.parseReportCartCounter(cartReport, c));
            c.moveToNext();
        }
        c.close();
        return cart;
    }

    private boolean getDate(long time, long before_time) {
        boolean value_send = false;
        Calendar cal = Calendar.getInstance();
        //TimeZone tz = cal.getTimeZone();//get your local time zone.

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        //sdf.setTimeZone(tz);//set time zone.
        //String localTime = sdf.format(new Date(time * 1000));
        //Log.e("Local time","String show>>>"+localTime);


        Date date = new Date();
        Date date_old = new Date();
        try {
            date = sdf.parse(Getdates(time));//get local date
            date_old = sdf.parse(Getdates(before_time));

            //if (date.after(date_old))
            if (date.before(date_old)) {
                value_send = true;
            } else {
                value_send = false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Log.e("Value send",""+value_send);
        return value_send;
    }

    private Date getDateNew(long before_time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();//get your local time zone.
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        //SimpleDateFormat sdf1 = new SimpleDateFormat(" hh:mm a");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(new Date(before_time * 1000));
        Date date = new Date();
        try {
            date = sdf.parse(localTime);//get local date
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private String Getdates(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(d);
    }

    public ArrayList<ReportCart> getReports1(long l, long m) {
        ArrayList<ReportCart> cart = new ArrayList<ReportCart>();

        String[] cols = new String[]{"*"};
        final String whereClause = "date >= '" + l + "' and date <= '" + m
                + "' and onHold != '1'";
        String orderBy = "date DESC";
        // Log.v("Date Range", whereClause);
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null,
                null, orderBy, null);
        // Log.v("Count", "" + c.getCount());
        c.moveToFirst();

        while (!c.isAfterLast()) {
            ReportCart cartReport = new ReportCart(context);
            cart.add(CursorHelper.parseReportCart(cartReport, c));
            c.moveToNext();
        }

        c.close();
        return cart;
    }

    public int getSaleNumber() {
        Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " where date >= " + getLastDayShift(), null);
        int trans = 0;

        if (c.getCount() > 0) {
            c.moveToLast();
            trans = c.getInt(c.getColumnIndex("trans"));
        }

        c.close();
        return trans;
    }

    public void insertStoreSettings(StoreReceiptHeader header) {
        ContentValues vals = new ContentValues();

        vals.put("name", header.getName());
        vals.put("address1", header.getAddress1());
        vals.put("phone", header.getPhone());
        vals.put("email", header.getEmail());
        vals.put("website", header.getWebsite());
        vals.put("currency", header.getCurrency());
        vals.put("clearsale", StoreSetting.clearSale);
        vals.put("print_sig", header.isPrint_sig());
        vals.put("capture_sig", header.isCapture_sig());
        vals.put("receipt_header", header.getReceipt_header());
        vals.put("receipt_footer", header.getReceipt_footer());
        vals.put("header_type", header.getHeader_type());
        vals.put("city", header.getCity());
        vals.put("state", header.getState());

        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE + " where header_type=" + header.getHeader_type(), null);

        if (c.getCount() > 0) {
            getDb().update(STORE_TABLE, vals, "_id=" + header.getHeader_type(), null);

        } else {
            getDb().insert(STORE_TABLE, null, vals);
        }
        c.close();
    }

    public void insertStoreSettings() {
        ContentValues vals = new ContentValues();

        vals.put("name", StoreSetting.getName());
        vals.put("address1", StoreSetting.getAddress1());
        vals.put("phone", StoreSetting.getPhone());
        vals.put("email", StoreSetting.getEmail());
        vals.put("website", StoreSetting.getWebsite());
        vals.put("currency", StoreSetting.getCurrency());
        vals.put("clearsale", StoreSetting.clearSale);
        vals.put("print_sig", StoreSetting.print_sig);
        vals.put("capture_sig", StoreSetting.capture_sig);
        vals.put("receipt_header", StoreSetting.getReceipt_header());
        vals.put("receipt_footer", StoreSetting.getReceipt_footer());

        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE, null);

        if (c.getCount() > 0) {
            getDb().update(STORE_TABLE, vals, "_id=1", null);

        } else {
            getDb().insert(STORE_TABLE, null, vals);
        }
        c.close();
    }

    public StoreReceiptHeader getStoreSettings(int headerType) {

        StoreReceiptHeader header = new StoreReceiptHeader();

        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE + " where header_type =" + headerType, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {

            header.setName(c.getString(c.getColumnIndex("name")));
            header.setAddress1(c.getString(c.getColumnIndex("address1")));
            header.setPhone(c.getString(c.getColumnIndex("phone")));
            header.setEmail(c.getString(c.getColumnIndex("email")));
            header.setWebsite(c.getString(c.getColumnIndex("website")));
            header.setCurrency(c.getString(c.getColumnIndex("currency")));
            header.setCity(c.getString(c.getColumnIndex("city")));
            header.setState(c.getString(c.getColumnIndex("state")));
            Log.e("getStoreSettings", "getStoreSettings city >>>" + header.getCity() + "<<state>>" + header.getState());
            header.setPrint_sig((c.getInt(c.getColumnIndex("print_sig")) != 0));
            header.setCapture_sig((c.getInt(c.getColumnIndex("capture_sig")) != 0));
            header.setReceipt_header(c.getString(c.getColumnIndex("receipt_header")));
            header.setReceipt_footer(c.getString(c.getColumnIndex("receipt_footer")));

            if (header.getCurrency() == null) {
                header.setCurrency("$");
            }

            c.moveToNext();
        }
        c.close();
        return header;
    }

    public void findStoreSettings(int headerType) {
        StoreSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + STORE_TABLE + " where header_type =" + headerType, null);
        if (c.getCount() > 0) {
            c.moveToFirst();

            while (!c.isAfterLast()) {
                StoreSetting.setName(c.getString(c.getColumnIndex("name")));
                StoreSetting.setAddress1(c.getString(c.getColumnIndex("address1")));
                StoreSetting.setPhone(c.getString(c.getColumnIndex("phone")));
                StoreSetting.setEmail(c.getString(c.getColumnIndex("email")));
                StoreSetting.setWebsite(c.getString(c.getColumnIndex("website")));
                StoreSetting.setCurrency(c.getString(c.getColumnIndex("currency")));

                StoreSetting.setCity(c.getString(c.getColumnIndex("city")));
                StoreSetting.setState(c.getString(c.getColumnIndex("state")));
                //Log.e("findStoreSettings","findStoreSettings city >>>"+StoreSetting.getCity()+"<<Get state>>"+StoreSetting.getState());
                StoreSetting.clearSale = (c.getInt(c.getColumnIndex("clearsale")) != 0);

                StoreSetting.print_sig = (c.getInt(c.getColumnIndex("print_sig")) != 0);
                StoreSetting.capture_sig = (c.getInt(c.getColumnIndex("capture_sig")) != 0);
                StoreSetting.setReceipt_header(c.getString(c.getColumnIndex("receipt_header")));
                StoreSetting.setReceipt_footer(c.getString(c.getColumnIndex("receipt_footer")));

                if (StoreSetting.getCurrency() == null) {
                    StoreSetting.setCurrency("$");
                }

                c.moveToNext();
            }
        }
        c.close();

    }

    public String getDatabaseName() {
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

    public void close() {
        db.close();
    }

    public boolean delete() {
        return context.deleteDatabase(DATABASE_NAME);
    }

    public void exportinv(String name) {
        StringBuilder exportString = new StringBuilder();

        exportString.append("\"[DEPARTMENT]\"\n");

        exportString.append("\"_id\",").append("\"name\",").append("\"tax1\",").append("\"tax2\",").append("\"tax3\"\n");

        Cursor c = getDb().rawQuery("select * from " + CATAGORY_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            exportString.append("\"" + c.getString(c.getColumnIndex("_id")) + "\",");
            exportString.append("\"" + c.getString(c.getColumnIndex("name")) + "\",");
            exportString.append("\"" + c.getString(c.getColumnIndex("tax1")) + "\",");
            exportString.append("\"" + c.getString(c.getColumnIndex("tax2")) + "\",");
            if (c.getString(c.getColumnIndex("tax3")) != null)
                exportString.append("\"" + c.getString(c.getColumnIndex("tax3")) + "\"\n");
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
                .append("\"saleEnd\",")
                .append("\"track\"\n");
        c = getDb().rawQuery("select * from " + PRODUCT_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Product product = new Product();

            product.price = new BigDecimal(c.getString(c.getColumnIndex("price")));
            product.salePrice = new BigDecimal(c.getLong(c.getColumnIndex("salePrice")));
            product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
            product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));

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
            product.track = (c.getInt(c.getColumnIndex("track")) != 0);

            exportString.append("\"" + product.id + "\",");
            exportString.append("\"" + product.name.replaceAll("\"", "") + "\",");
            exportString.append("\"" + product.desc.replaceAll("\"", "") + "\",");
            exportString.append("\"" + (product.price.divide(Consts.HUNDRED).toString()) + "\",");
            exportString.append("\"" + product.barcode + "\",");
            exportString.append("\"" + product.cat + "\",");
            exportString.append("\"" + product.onHand + "\",");
            exportString.append("\"" + (product.cost.divide(Consts.HUNDRED)) + "\",");
            exportString.append("\"" + product.lowAmount + "\",");
            exportString.append("\"" + (product.salePrice.divide(Consts.HUNDRED).toString()) + "\",");

            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            exportString.append("\"" + formatter.format(new Date(product.startSale)) + "\",");
            exportString.append("\"" + formatter.format(new Date(product.endSale)) + "\",");
            int track = 1;
            if (product.track == false) track = 0;
            exportString.append("\"" + track + "\"\n");

            c.moveToNext();
        }

        c.close();

        exportString.append("\"[/PRODUCTS]\"\n");

        File sd = Environment.getExternalStorageDirectory();
        File dir = new File(sd, "/AdvantagePOS/Inventory");

        dir.mkdirs();

        File saveFile = new File(sd, "/AdvantagePOS/Inventory/" + name + ".csv");

        FileWriter writer;
        try {
            writer = new FileWriter(saveFile);
            writer.append(exportString.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean importinv(String filename, Handler handler) {

        File sd = Environment.getExternalStorageDirectory();

        final File loadFile = new File(sd, "/AdvantagePOS/Inventory/" + filename);

        Log.v("Import", filename);

        try {
            BufferedReader input = new BufferedReader(new FileReader(loadFile));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    Log.v("ImportInv", line);

                    if (inDepartment) {
                        if (line.contains("[/DEPARTMENT]")) {
                            inDepartment = false;
                        } else if (!line.contains("_id")) {
                            StringTokenizer st = new StringTokenizer(line, ",");

                            String id = st.nextToken().replaceAll("\"", "");

                            ContentValues vals = new ContentValues();
                            vals.put("name", st.nextToken().replaceAll("\"", ""));
                            vals.put("tax1", Float.valueOf(st.nextToken().replaceAll("\"", "")));
                            vals.put("tax2", Float.valueOf(st.nextToken().replaceAll("\"", "")));
                            if (st.hasMoreTokens()) {
                                vals.put("tax3", Float.valueOf(st.nextToken().replaceAll("\"", "")));
                            }
                            if (getDb().update(CATAGORY_TABLE, vals, "_id=" + id, null) == 0) {
                                getDb().insert(CATAGORY_TABLE, null, vals);
                            }
                        }
                    } else if (inProducts) {
                        if (line.contains("[/PRODUCTS]")) {
                            inProducts = false;
                        } else if (!line.contains("_id")) {

                            String[] RowData = line.split(",");

                            String id = RowData[0].replaceAll("\"", "");

                            ContentValues vals = new ContentValues();
                            vals.put("name", RowData[1].replaceAll("\"", ""));
                            vals.put("desc", RowData[2].replaceAll("\"", ""));
                            if (!RowData[3].equals("")) {
                                String desc = RowData[3].replaceAll("\"", "");
                                if (desc.length() > 6)
                                    desc = desc.substring(desc.length() - 6, desc.length());
                                vals.put("price", (int) (Float.valueOf(desc) * 100f));
                            }
                            vals.put("barcode", RowData[4].replaceAll("\"", ""));
                            if (!RowData[5].equals(""))
                                vals.put("catid", Integer.valueOf(RowData[5].replaceAll("\"", "")));
                            if (RowData.length > 6) {
                                if (!RowData[6].equals(""))
                                    vals.put("quantity", Integer.valueOf(RowData[6].replaceAll("\"", "")));
                            } else {
                                vals.put("quantity", 0);
                            }

                            if (RowData.length > 7) {
                                if (!RowData[7].equals("")) {
                                    String desc = RowData[7].replaceAll("\"", "");
                                    if (desc.length() > 6)
                                        desc = desc.substring(desc.length() - 6, desc.length());
                                    vals.put("cost", (int) (Float.valueOf(desc) * 100f));
                                } else {
                                    vals.put("cost", 0);
                                }
                            } else {
                                vals.put("cost", 0);
                            }

                            if (RowData.length > 8) {
                                if (!RowData[8].equals("")) {
                                    String desc = RowData[8].replaceAll("\"", "");
                                    if (desc.length() > 6)
                                        desc = desc.substring(desc.length() - 6, desc.length());
                                    vals.put("lowAmount", Integer.valueOf(desc));
                                } else {
                                    vals.put("lowAmount", 0);
                                }
                            } else {
                                vals.put("lowAmount", 0);
                            }

                            if (RowData.length > 9) {
                                if (!RowData[9].equals("")) {
                                    String desc = RowData[9].replaceAll("\"", "");
                                    if (desc.length() > 6)
                                        desc = desc.substring(desc.length() - 6, desc.length());
                                    vals.put("salePrice", (int) (Float.valueOf(desc) * 100f));
                                } else {
                                    vals.put("salePrice", 0);
                                }
                            } else {
                                vals.put("salePrice", 0);
                            }

                            if (RowData.length > 10) {
                                if (!RowData[10].equals("")) {
                                    String desc = RowData[10].replaceAll("\"", "");

                                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                                    ParsePosition pos = new ParsePosition(0);
                                    Date date = formatter.parse(desc, pos);

                                    vals.put("saleStartDate", date.getTime());
                                } else {
                                    vals.put("saleStartDate", 0);
                                }
                            } else {
                                vals.put("saleStartDate", 0);
                            }

                            if (RowData.length > 11) {
                                if (!RowData[11].equals("")) {
                                    String desc = RowData[11].replaceAll("\"", "");

                                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                                    ParsePosition pos = new ParsePosition(0);
                                    Date date = formatter.parse(desc, pos);

                                    vals.put("saleEndDate", date.getTime());
                                } else {
                                    vals.put("saleEndDate", 0);
                                }
                            } else {
                                vals.put("saleEndDate", 0);
                            }


                            Message m = new Message();
                            m.what = 11;
                            m.obj = RowData[0].replaceAll("\"", "") + " - " + RowData[1].replaceAll("\"", "");
                            handler.sendMessage(m);

                            if (getDb().update(PRODUCT_TABLE, vals, "_id=" + id, null) == 0) {
                                getDb().insert(PRODUCT_TABLE, null, vals);
                            }
                        }
                    }

                    if (line.contains("[DEPARTMENT]")) {
                        inDepartment = true;
                    }
                    if (line.contains("[PRODUCTS]")) {
                        inProducts = true;
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        findCats();
        buildCatString();

        return true;
    }

    public Cursor getProdById(int id) {
        //Log.e("In query","Product id"+id);
        //String[] cols = new String[] {"*"};     
        Cursor c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE _id = " + id, null);
        //Cursor c = getDb().query(PRODUCT_TABLE, cols, "_id = ?", new String[] { inputText }, null, null, null);
        //Log.e("Cursor count",""+c.getCount());
        if (c.moveToFirst()) {
            return c;
        }
        c.close();
        return null;
    }

    public String getProductNameById(int id) {
        Cursor c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE _id=" + id, null);
        if (c.moveToFirst()) {
            return c.getString(c.getColumnIndex("name"));
        }
        c.close();
        return null;
    }

    public int getProductQuantity(int id) {
        int qunati = 0;
        Cursor c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE _id=" + id, null);
        if (c.moveToFirst()) {
            qunati = c.getInt(c.getColumnIndex("quantity"));
        }
        c.close();
        return qunati;
    }

    public Cursor getProductByCategory(int id) {
        Cursor c = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE catid=" + id + " and deleted=0", null);
        return c;
    }

    public int getProductQuantityCategory(int id) {
        int qunati = 0;
        Cursor c1 = getDb().rawQuery("select * from " + PRODUCT_TABLE + " WHERE catid=" + id + " and deleted=0", null);
        if (c1.moveToFirst()) {
            qunati = c1.getInt(c1.getColumnIndex("quantity"));
        }

        return qunati;
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

    public void saveButton(ItemButton itemButton) {
        ContentValues vals = new ContentValues();

        vals.put("_id", itemButton.id);
        vals.put("type", itemButton.type);
        vals.put("orderBy", itemButton.order);
        vals.put("parent", itemButton.parent);
        vals.put("productID", itemButton.productID);
        vals.put("departID", itemButton.departID);
        vals.put("folderName", itemButton.folderName);
        vals.put("link", itemButton.link);
        vals.put("deleted", itemButton.deleted);
        vals.put("price", itemButton.price);

        vals.put("saleStartDate", itemButton.startdate);
        vals.put("saleEndDate", itemButton.enddate);
        vals.put("salePrice", itemButton.saleprice);
        vals.put("isTrackable", itemButton.trackable);
        vals.put("reorderLevel", itemButton.reorderLevel);


        Bitmap photo = itemButton.image;

        if (photo != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] bArray = bos.toByteArray();
            vals.put("image", bArray);
        } else {
            vals.put("image", "");
        }

        Cursor c = getDb().rawQuery("select type from " + BUTTON_TABLE + " WHERE _id =" + itemButton.id, null);

        if (c.getCount() > 0) {
            getDb().update(BUTTON_TABLE, vals, "_id=" + itemButton.id, null);
        } else {
            getDb().insert(BUTTON_TABLE, null, vals);
        }
        c.close();
    }

    public void saveOrderStatus(JSONObject statusdata) {
        try {
            int id = Integer.parseInt(statusdata.getString("id"));
            ContentValues vals = new ContentValues();

            vals.put("id", id);
            vals.put("status", statusdata.getString("status"));


            Cursor c = getDb().rawQuery("select * from " + Ostatus_TABLE + " WHERE id =" + id, null);

            if (c.getCount() > 0) {
                getDb().update(Ostatus_TABLE, vals, "id=" + id, null);
            } else {
                getDb().insert(Ostatus_TABLE, null, vals);
            }
            c.close();
        } catch (Exception e) {
            Log.e("saveOrderStatus Method", "Error>>" + e.getMessage());
        }
    }

    public void insertEmailSettings() {
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

        if (c.getCount() > 0) {
            getDb().update(EMAIL_TABLE, vals, "_id=1", null);
        } else {
            getDb().insert(EMAIL_TABLE, null, vals);
        }

        c.close();
    }

    public int insertReceiptSettings(String string, int editID)
    {
        int rowId = 0;
        ContentValues vals = new ContentValues();
        //vals.put("enabled", ReceiptSetting.enabled);
        vals.put("blurb", string);
        //vals.put("address", ReceiptSetting.address);
        //vals.put("name", ReceiptSetting.name);
        //vals.put("make", ReceiptSetting.make);
        //vals.put("size", ReceiptSetting.size);
        //vals.put("type", ReceiptSetting.type);
        //vals.put("drawer", ReceiptSetting.drawer);
        //vals.put("display", ReceiptSetting.display);
        //Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);
        //if(c.getCount()>0){
        //    getDb().update(RECEIPT_TABLE, vals, "_id=1", null);
        //}else{
        //getDb().insert(RECEIPT_TABLE, null, vals);
        //}
        if (editID > 0) {
            rowId = getDb().update(RECEIPT_TABLE, vals, "_id=" + editID, null);
        } else {
            rowId = (int) getDb().insert(RECEIPT_TABLE, null, vals);
        }
        findReceiptSettings();
        //c.close();
        return rowId;
    }
    public int insertPrinterSettings(String string, int editID, int serverId) {
        int rowId = 0;
        ContentValues vals = new ContentValues();

        //vals.put("enabled", ReceiptSetting.enabled);
        vals.put("blurb", string);
        vals.put("serverId", serverId);
        //vals.put("address", ReceiptSetting.address);
        //vals.put("name", ReceiptSetting.name);
        //vals.put("make", ReceiptSetting.make);
        //vals.put("size", ReceiptSetting.size);
        //vals.put("type", ReceiptSetting.type);
        //vals.put("drawer", ReceiptSetting.drawer);
        //vals.put("display", ReceiptSetting.display);

        //Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);

        //if(c.getCount()>0){
        //    getDb().update(RECEIPT_TABLE, vals, "_id=1", null);
        //}else{
        //getDb().insert(RECEIPT_TABLE, null, vals);
        //}

        if (editID > 0) {
            rowId = getDb().update(RECEIPT_TABLE, vals, "_id=" + editID, null);
        } else {
            rowId = (int) getDb().insert(RECEIPT_TABLE, null, vals);
        }

        findReceiptSettings();
        //c.close();
        return rowId;
    }

    public void insertPrinterSync(String string, int serverId)
    {
        int rowId = 0;
        ContentValues vals = new ContentValues();
        vals.put("blurb", string);
        vals.put("serverId", serverId);
        Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE + " WHERE serverId =" + serverId, null);
        if (c.getCount() > 0)
        {
            getDb().update(RECEIPT_TABLE, vals, "serverId = " + serverId, null);
        }
        else
        {
            getDb().insert(RECEIPT_TABLE, null, vals);
        }
        c.close();
    }

    public int UpdatePrintSettings(int string, int editID) {
        //Log.e("UpdatePrintSettings","update Id>>> "+editID);
        int rowId = 0;
        ContentValues vals = new ContentValues();
        vals.put("serverId", string);
        rowId = getDb().update(RECEIPT_TABLE, vals, "_id =" + editID, null);
        //Log.e("update row","row id>>"+rowId);
        return rowId;
    }

    public int findPrinterServerId(int pId) {
        int serverId = 0;
        try {
            Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE + " WHERE _id =" + pId, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                serverId = c.getInt(c.getColumnIndex("serverId"));
                //Log.e("Serverid",">>>"+serverId);
            }
            c.close();
        } catch (Exception e) {
            Log.e("PrinterServerId Method", "Error>>" + e.getMessage());
        }
        return serverId;
    }

    public void findReceiptSettings() {
        ReceiptSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {

            //ReceiptSetting.enabled = c.getInt(c.getColumnIndex("enabled")) != 0;
            //ReceiptSetting.blurb = c.getString(c.getColumnIndex("blurb"));
            //ReceiptSetting.address = c.getString(c.getColumnIndex("address"));
            //ReceiptSetting.name = c.getString(c.getColumnIndex("name"));
            //ReceiptSetting.make = c.getInt(c.getColumnIndex("make"));
            //ReceiptSetting.size = c.getInt(c.getColumnIndex("size"));
            //ReceiptSetting.type = c.getInt(c.getColumnIndex("type"));
            //ReceiptSetting.drawer = c.getInt(c.getColumnIndex("drawer")) != 0;
            //ReceiptSetting.display = c.getInt(c.getColumnIndex("display")) != 0;

            String json = c.getString(c.getColumnIndex("blurb"));

            boolean good = false;
            try {
                JSONObject object = new JSONObject(json);
                good = true;
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (good) {
                ReceiptSetting.printers.add(json);
                ReceiptSetting.enabled = true;
            }
            c.moveToNext();
        }
        c.close();
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    public int insertCustomer(Customer customer) {
        ContentValues vals = new ContentValues();
        vals.put("fname", customer.fName);
        vals.put("lname", customer.lName);
        vals.put("email", customer.email);
        vals.put("numreturns", customer.returns);
        vals.put("numsales", customer.sales);
        vals.put("total", customer.total.toString());
        vals.put("phone", customer.phone.toString());
        vals.put("id", customer.cid);

        int at = (int) getDb().insert(CUSTOMER_TABLE, null, vals);
        customer.setId(at);

        return at;
    }

    public int insertCustomernew(Customer customer, int id) {
        ContentValues vals = new ContentValues();
        vals.put("fname", customer.fName);
        vals.put("lname", customer.lName);
        vals.put("email", customer.email);
        vals.put("numreturns", customer.returns);
        vals.put("numsales", customer.sales);
        vals.put("total", customer.total.toString());
        vals.put("phone", customer.phone.toString());
        vals.put("id", id);

        int at = (int) getDb().insert(CUSTOMER_TABLE, null, vals);
        customer.setId(at);
        customer.setCid(id);
        return at;
    }

    public void removeCustomer(int position) {
        getDb().delete(CUSTOMER_TABLE, "_id=" + position, null);
    }

    public void replaceCustomer(Customer newprod) {
        ContentValues vals = new ContentValues();

        vals.put("fname", newprod.fName);
        vals.put("lname", newprod.lName);
        vals.put("email", newprod.email);
        vals.put("numreturns", newprod.returns);
        vals.put("numsales", newprod.sales);
        vals.put("total", newprod.total.toString());

        getDb().update(CUSTOMER_TABLE, vals, "_id=" + newprod.id, null);
    }

    public Cursor searchCustomers(String string) {
        String formatedText = string.replaceAll("'", "''");

        Cursor mCursor;

        if (formatedText.length() < 1) {
            mCursor = getDb().rawQuery("select * from " + CUSTOMER_TABLE, null);
        } else {
            //mCursor = getDb().query(true, CUSTOMER_TABLE, new String[] {"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%"+ formatedText + "%'", null,
            //        null, null, null, null);

            mCursor = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%" + formatedText + "%'" + " OR lname like '%" + formatedText + "%'" + " OR phone like '%" + formatedText + "%'", null,
                    null, null, null, null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchItemsByName(String inputText) {
        String formatedText = inputText.replaceAll("'", "''");
        Log.v("text", formatedText);
        Cursor mCursor;
        mCursor = getDb().query(true, PRODUCT_TABLE, new String[]{"*"}, "deleted=0 AND (name" + " like '%" + formatedText + "%' OR barcode like '%" + formatedText + "%')", null,
                null, null, null, null);


        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public ArrayList<Customer> fetchCustomerByEmail(String emailId) {
        ArrayList<Customer> customers = new ArrayList<Customer>();
        Cursor c;
        if (emailId == null) {
            c = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, null, null,
                    null, null, null, null);
        } else {
            c = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, "email " + "like '%" + emailId + "%'", null,
                    null, null, "email", null);
        }
        if (c != null) {
            c.moveToNext();
            int i = 0;
            while (!c.isAfterLast()) {
                Customer customer = new Customer();
                customer.lName = c.getString(c.getColumnIndex("lname"));
                customer.id = c.getInt(c.getColumnIndex("_id"));
                customer.fName = c.getString(c.getColumnIndex("fname"));
                customer.email = c.getString(c.getColumnIndex("email"));
                customers.add(customer);
                c.moveToNext();
            }

            c.close();
        }
        return customers;
    }

    public String[] fetchCustomersName(String string) {
        String formatedText = string.replaceAll("'", "''");

        Cursor mCursor = getDb().query(true, CUSTOMER_TABLE, new String[]{"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%" + formatedText + "%'", null,
                null, null, null, null);
        String[] CustomerString;
        if (mCursor != null) {
            mCursor.moveToFirst();

            CustomerString = new String[mCursor.getCount()];
            int i = 0;
            while (!mCursor.isAfterLast()) {
                if (mCursor.getString(mCursor.getColumnIndex("email")).equals("")) {
                    CustomerString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " + mCursor.getString(mCursor.getColumnIndex("fname")));
                } else {
                    CustomerString[i] = new String(mCursor.getString(mCursor.getColumnIndex("_id")) + ", " + mCursor.getString(mCursor.getColumnIndex("fname")) + ", " + mCursor.getString(mCursor.getColumnIndex("email")));
                }
                i++;
                mCursor.moveToNext();
            }

            mCursor.close();
        } else {
            return null;
        }

        return CustomerString;
    }

    public int insertCashier(Cashier cashier) {
        ContentValues vals = new ContentValues();
        vals.put("_id", cashier.id);
        vals.put("fname", cashier.name);
        vals.put("email", cashier.email);
        vals.put("numreturns", cashier.returns);
        vals.put("numsales", cashier.sales);
        vals.put("total", Long.parseLong(cashier.total.toString()));
        vals.put("pin", cashier.pin);
        vals.put("permissionReturn", cashier.permissionReturn);
        vals.put("permissionPriceModify", cashier.permissionPriceModify);
        vals.put("permissionReports", cashier.permissionReports);
        vals.put("permissionInventory", cashier.permissionInventory);
        vals.put("permissionSettings", cashier.permissionSettings);
        vals.put("permissionVoideSale", cashier.permissionVoideSale);
        vals.put("permissionProcessTender", cashier.permissionProcessTender);
        vals.put("deleted", cashier.deleted);
        vals.put("admin", cashier.admin);

        Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE + " WHERE _id =" + cashier.id, null);

        if (c.getCount() > 0) {
            getDb().update(CASHIER_TABLE, vals, "_id=" + cashier.id, null);
        } else {
            getDb().insert(CASHIER_TABLE, null, vals);
        }
        c.close();
        return cashier.id;
    }

    public void replaceCashier(Cashier cashier) {
        ContentValues vals = new ContentValues();

        vals.put("fname", cashier.name);
        vals.put("email", cashier.email);
        vals.put("numreturns", cashier.returns);
        vals.put("numsales", cashier.sales);
        vals.put("total", cashier.total.toString());//Long.parseLong(cashier.total.toString()));
        vals.put("pin", cashier.pin);
        vals.put("permissionReturn", cashier.permissionReturn);
        vals.put("permissionPriceModify", cashier.permissionPriceModify);
        vals.put("permissionReports", cashier.permissionReports);
        vals.put("permissionInventory", cashier.permissionInventory);
        vals.put("permissionSettings", cashier.permissionSettings);

        getDb().update(CASHIER_TABLE, vals, "_id=" + cashier.id, null);

    }

    public void RemoveCashier(int position) {
        getDb().delete(CASHIER_TABLE, "_id=" + position, null);
    }

    public void insertAdminSettings() {
        ContentValues vals = new ContentValues();

        vals.put("enabled", AdminSetting.enabled);
        vals.put("password", AdminSetting.password);
        vals.put("hint", AdminSetting.hint);
        vals.put("userid", AdminSetting.userid);

        Cursor c = getDb().rawQuery("select * from " + ADMIN_TABLE, null);

        if (c.getCount() > 0) {
            getDb().update(ADMIN_TABLE, vals, "_id=1", null);
        } else {
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
            AdminSetting.hint = c.getString(c.getColumnIndex("hint"));

            c.moveToNext();
        }
        c.close();
    }

    public ArrayList<Cashier> getCashiers() {
        //Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE + " WHERE deleted = 0 AND admin = 0", null);
        Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE + " WHERE deleted = 0", null);
        c.moveToFirst();

        ArrayList<Cashier> cashiers = new ArrayList<Cashier>();

        while (!c.isAfterLast()) {
            final Cashier cashier = new Cashier();

            cashier.name = c.getString(c.getColumnIndex("fname"));
            cashier.id = c.getInt(c.getColumnIndex("_id"));
            cashier.email = c.getString(c.getColumnIndex("email"));
            cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
            cashier.sales = c.getInt(c.getColumnIndex("numsales"));
            cashier.total = new BigDecimal(c.getFloat(c.getColumnIndex("total")));

            cashier.pin = c.getString(c.getColumnIndex("pin"));
            cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
            cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
            cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
            cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
            cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;
            cashier.permissionVoideSale = c.getInt(c.getColumnIndex("permissionVoideSale")) != 0;
            cashier.permissionProcessTender = c.getInt(c.getColumnIndex("permissionProcessTender")) != 0;
            cashiers.add(cashier);

            c.moveToNext();
        }

        c.close();

        return cashiers;
    }

    public int getCashiersAdminId() {
        int adminid = 0;
        Cursor c = getDb().rawQuery("select * from " + CASHIER_TABLE + " WHERE deleted = 0 AND admin = 1", null);
        c.moveToFirst();
        adminid = c.getInt(c.getColumnIndex("_id"));
        c.close();
        //Log.e("getCashiersAdminId","Method adminid"+adminid);
        return adminid;
    }

    public Cursor getPrinters() {
        Cursor mCursor;

        mCursor = getDb().rawQuery("select * from " + RECEIPT_TABLE, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public Cursor SearchCashiers(String string) {
        String formatedText = string.replaceAll("'", "''");

        Cursor mCursor;

        if (formatedText.length() < 1) {
            mCursor = getDb().rawQuery("select * from " + CASHIER_TABLE, null);
        } else {
            mCursor = getDb().query(true, CASHIER_TABLE, new String[]{"*"}, "fname" + " like '%" + formatedText + "%' OR email like '%" + formatedText + "%'", null,
                    null, null, null, null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cashier getCashier(int cashierID) {
        Cashier cashier = null;
        Cursor c = getDb().query(CASHIER_TABLE, new String[]{"*"}, "_id = ?", new String[]{"" + cashierID}, null, null, null);
        if (c.moveToFirst()) {
            cashier = new Cashier();

            cashier.name = c.getString(c.getColumnIndex("fname"));
            cashier.id = c.getInt(c.getColumnIndex("_id"));
            cashier.email = c.getString(c.getColumnIndex("email"));
            cashier.returns = c.getInt(c.getColumnIndex("numreturns"));
            cashier.sales = c.getInt(c.getColumnIndex("numsales"));
            cashier.total = new BigDecimal(c.getFloat(c.getColumnIndex("total")));

            cashier.pin = c.getString(c.getColumnIndex("pin"));
            cashier.permissionReturn = c.getInt(c.getColumnIndex("permissionReturn")) != 0;
            cashier.permissionPriceModify = c.getInt(c.getColumnIndex("permissionPriceModify")) != 0;
            cashier.permissionReports = c.getInt(c.getColumnIndex("permissionReports")) != 0;
            cashier.permissionInventory = c.getInt(c.getColumnIndex("permissionInventory")) != 0;
            cashier.permissionSettings = c.getInt(c.getColumnIndex("permissionSettings")) != 0;
        }

        return cashier;
    }

    public OfflineStats getOfflineStatistics() {
        Cursor cursor = getDb().rawQuery("SELECT count(*), sum(total) FROM sales WHERE onHold != 1 AND processed = 0 and lineitems like '%credit card%'", null);

        OfflineStats stats = new OfflineStats();
        if (cursor != null) {
            cursor.moveToFirst();
            stats.setNumOfTransactions(cursor.getInt(0));
            stats.setTotal(cursor.getInt(1));
        }

        return stats;
    }

    public OfflineStats getOfflineCashStatistics() {
        Cursor cursor = getDb().rawQuery("SELECT count(*), sum(total) FROM sales WHERE onHold != 1 AND processed = 0 and lineitems not like '%credit card%'", null);

        OfflineStats stats = new OfflineStats();
        if (cursor != null) {
            cursor.moveToFirst();
            stats.setNumOfTransactions(cursor.getInt(0));
            stats.setTotal(cursor.getInt(1));
        }

        return stats;
    }

    public Cursor getButtons(int parent) {
        Cursor mCursor = null;

        if (parent > 0) {
            mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where deleted=0 AND (parent=" + parent + " or type = -1) order by orderBy", null);
        } else {
            mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where deleted=0 AND parent=" + parent + " order by orderBy", null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public ArrayList<String> getFolders() {
        ArrayList<String> folders = new ArrayList<>();
        folders.add("Home");
        Cursor c = null;

        c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where deleted=0 AND type=" + ItemButton.TYPE_FOLDER, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndex("folderName"));

            if (name != null && !name.trim().equals(""))
                folders.add(name);
            else
                folders.add("Null");

            c.moveToNext();
        }

        c.close();
        return folders;
    }

    public int getFolderID(String selectedItem) {
        Cursor c = null;
        int id = 0;
        c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where folderName='" + selectedItem + "'", null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            id = ((c.getInt(c.getColumnIndex("_id"))));
            c.moveToNext();
        }

        c.close();
        return id;
    }

    public void deleteButton(int position) {
        if (position > 1)
            getDb().delete(BUTTON_TABLE, "_id=" + position, null);
    }

    public ItemButton getButtonByID(int newParent) {
        Cursor c = null;

        c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where _id=" + newParent, null);
        c.moveToFirst();

        ItemButton itemButton = new ItemButton();

        if (!c.isAfterLast()) {
            itemButton.id = c.getInt(c.getColumnIndex("_id"));
            itemButton.type = c.getInt(c.getColumnIndex("type"));
            itemButton.order = c.getInt(c.getColumnIndex("orderBy"));
            itemButton.parent = c.getInt(c.getColumnIndex("parent"));
            itemButton.productID = c.getInt(c.getColumnIndex("productID"));
            itemButton.departID = c.getInt(c.getColumnIndex("departID"));
            itemButton.folderName = c.getString(c.getColumnIndex("folderName"));
            itemButton.link = c.getString(c.getColumnIndex("link"));
            itemButton.price = c.getString(c.getColumnIndex("price"));

            final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));

            if (imageBlob != null) {
                Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                itemButton.image = image;
            }
        }
        c.close();

        return itemButton;
    }

    /*public ItemButton getButtonByID(int newParent) {
        Cursor c = null;

        c = getDb().rawQuery("select * from " + BUTTON_TABLE + " where _id="+newParent, null);
        c.moveToFirst();

        ItemButton itemButton = new ItemButton();

        if (!c.isAfterLast()) {
            itemButton.id = c.getInt(c.getColumnIndex("_id"));
            itemButton.type = c.getInt(c.getColumnIndex("type"));
            itemButton.order = c.getInt(c.getColumnIndex("orderBy"));
            itemButton.parent = c.getInt(c.getColumnIndex("parent"));
            itemButton.productID = c.getInt(c.getColumnIndex("productID"));
            itemButton.departID = c.getInt(c.getColumnIndex("departID"));
            itemButton.folderName = c.getString(c.getColumnIndex("folderName"));
            itemButton.link = c.getString(c.getColumnIndex("link"));

            final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));

            if(imageBlob != null) {
                Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                itemButton.image = image;
            }
        }
        c.close();

        return itemButton;
    }*/
    public boolean hasChildren(int parent) {
        Cursor mCursor = null;

        mCursor = getDb().rawQuery("select * from " + BUTTON_TABLE + " where parent=" + parent + " order by orderBy", null);

        mCursor.moveToFirst();

        if (mCursor.isAfterLast()) {
            return false;
        }

        return true;
    }

    public Cursor getPrevious10() {
        String[] cols = new String[]{"*"};
        String whereClause = "onHold = 0";
        String orderBy = "date DESC";

        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, orderBy, null);

        if (c != null) {
            Log.v("Find Sales", "Found: " + c.getCount());
            c.moveToFirst();
        } else {
            Log.v("Find Sales", "None Found.");
        }

        return c;
    }

    public Cursor getOnHoldSales() {
        String[] cols = new String[]{"*"};
        String whereClause = "onHold = 1";
        String orderBy = "date";
        Log.v("Find Sales", "Finding...");
        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, orderBy, null);

        if (c != null) {
            Log.v("Find Sales", "Found: " + c.getCount());
            c.moveToFirst();
        } else {
            Log.v("Find Sales", "None Found.");
        }

        return c;
    }

    public void deleteSale(int saleID) {
        getDb().delete(SALES_TABLE, "_id=" + saleID, null);
    }

    public void insertMercurySettings() {
        ContentValues vals = new ContentValues();

        vals.put("enabled", WebSetting.enabled);
        vals.put("merchantID", WebSetting.merchantID);
        vals.put("webServicePassword", WebSetting.webServicePassword);
        vals.put("terminalName", WebSetting.terminalName);
        vals.put("hostedMID", WebSetting.hostedMID);
        vals.put("hostedPass", WebSetting.hostedPass);
        Cursor c = getDb().rawQuery("select * from " + MERCURY_TABLE, null);

        if (c.getCount() > 0) {
            getDb().update(MERCURY_TABLE, vals, "_id=1", null);
        } else {
            getDb().insert(MERCURY_TABLE, null, vals);
        }

        c.close();
    }

    public void findMercurySettings() {
        WebSetting.clear();

        Cursor c = getDb().rawQuery("select * from " + MERCURY_TABLE, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            WebSetting.enabled = c.getInt(c.getColumnIndex("enabled")) != 0;
            WebSetting.merchantID = c.getString(c.getColumnIndex("merchantID"));
            WebSetting.webServicePassword = c.getString(c.getColumnIndex("webServicePassword"));
            WebSetting.terminalName = c.getString(c.getColumnIndex("terminalName"));
            WebSetting.hostedPass = c.getString(c.getColumnIndex("hostedPass"));
            WebSetting.hostedMID = c.getString(c.getColumnIndex("hostedMID"));

            c.moveToNext();
        }
        c.close();
    }

    public void saveOfflineCreditSales(String postXml, String invoice, int saleid, boolean processed) {
        ContentValues vals = new ContentValues();

        vals.put("processed", processed);

        if (processed) {
            vals.put("response", postXml);
            vals.put("request", "");
        } else {
            vals.put("request", postXml);
            vals.put("response", "");
        }

        vals.put("invoice", invoice);
        vals.put("saleid", saleid);

        long currentDateTime = new Date().getTime();

        vals.put("date", "" + currentDateTime);

        getDb().insert(CCSALES_TABLE, null, vals);
    }

    public void replaceMercurySave(Payment ccPayment) {
        ContentValues vals = new ContentValues();

        vals.put("processed", ccPayment.processed);
        vals.put("request", "");
        vals.put("invoice", ccPayment.invoiceNo);
        vals.put("response", ccPayment.response);
        if (ccPayment.date != null)
            vals.put("date", ccPayment.date);
        else
            vals.put("date", "");

        getDb().update(CCSALES_TABLE, vals, "_id=" + ccPayment.preSaleID, null);
    }

    public void removePreCreditPayment(Payment payment) {
        getDb().delete(CCSALES_TABLE, "saleid=" + payment.saleID, null);
    }

    public ArrayList<Payment> getMercurySales(long l, long m) {
        ArrayList<Payment> payments = new ArrayList<>();

        String[] cols = new String[]{"*"};
        final String whereClause = "date >= " + l + " and date <= " + m;

        //final String whereClause = "processed != 1";

        Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Payment payment = new Payment();

            payment.invoiceNo = c.getString(c.getColumnIndex("invoice"));
            payment.request = c.getString(c.getColumnIndex("request"));
            payment.response = c.getString(c.getColumnIndex("response"));

            payment.date = c.getString(c.getColumnIndex("date"));
            payment.preSaleID = c.getInt(c.getColumnIndex("_id"));
            payment.processed = c.getInt(c.getColumnIndex("processed"));
            payment.saleID = c.getInt(c.getColumnIndex("saleid"));
            payment.extractXML();

            payments.add(payment);

            c.moveToNext();
        }

        return payments;
    }

    public ArrayList<Payment> getMercurySale() {
        ArrayList<Payment> payments = new ArrayList<>();

        String[] cols = new String[]{"*"};

        final String whereClause = "processed = 0";

        Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null,
                null, null, null);

        if (c.getCount() > 0) {
            c.moveToFirst();

            Payment payment = new Payment();

            payment.invoiceNo = c.getString(c.getColumnIndex("invoice"));
            payment.request = c.getString(c.getColumnIndex("request"));
            payment.date = c.getString(c.getColumnIndex("date"));
            payment.preSaleID = c.getInt(c.getColumnIndex("_id"));
            payment.processed = c.getInt(c.getColumnIndex("processed"));
            payment.saleID = c.getInt(c.getColumnIndex("saleid"));
            payment.extractXML();

            payments.add(payment);
        }

        return payments;
    }

    public int getUnsentCount() {
        ReportCart cartReport = new ReportCart(context);

        String[] cols = new String[]{"*"};

        //final String whereCl ause = "onhold = 0 AND processed = 0";
        Log.v("getUnsentSale", "find unsent");
        Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " WHERE onhold = '0' AND processed = '0'", null);
        int unsent = c.getCount();
        c.close();

        return unsent;
    }

    public ReportCart getSaleById(int id) {
        ReportCart cartReport = new ReportCart(context);
        //Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " WHERE _id = " + id, null);
        Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " WHERE trans = " + id, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            cartReport = CursorHelper.parseReportCart(cartReport, c);
            c.close();
            return cartReport;
        }
        c.close();
        return null;
    }

    public ReportCart getUnsentSale() {
        ReportCart cartReport = new ReportCart(context);
        Log.v("getUnsentSale", "find unsent");
        Cursor c = getDb().rawQuery("select * from " + SALES_TABLE + " WHERE onhold = '0' AND processed = '0' order by _id desc", null);

        if (c.getCount() > 0) {
            Log.v("getUnsentSale", "find " + c.getCount());
            c.moveToFirst();
            cartReport = CursorHelper.parseReportCart(cartReport, c);
            c.close();
            return cartReport;
        }
        c.close();
        return null;
    }

    public List<ReportCart> getUnsentSales() {
        List<ReportCart> carts = new ArrayList<>();

        Log.v("getUnsentSale", "find unsent");
        Cursor c = getDb().rawQuery("select * from sales WHERE onHold != 1 AND processed = 0 order by _id desc", null);

        while (c.moveToNext()) {
            ReportCart cart = new ReportCart(context);
            carts.add(CursorHelper.parseReportCart(cart, c));
        }
        c.close();

        return carts;
    }

    public void voidSale(String substring) {
        ContentValues vals = new ContentValues();

        vals.put("voided", 1);

        getDb().update(SALES_TABLE, vals, "_id=" + substring, null);
    }

    public void replaceMercuryPartial(Payment ccPayment) {
        ContentValues vals = new ContentValues();

        vals.put("processed", ccPayment.processed);
        vals.put("request", ccPayment.request);
        vals.put("invoice", ccPayment.invoiceNo);
        vals.put("response", ccPayment.response);
        if (ccPayment.date != null)
            vals.put("date", ccPayment.date);
        else
            vals.put("date", "");

        getDb().update(CCSALES_TABLE, vals, "invoice=" + ccPayment.invoiceNo, null);

    }

    public ArrayList<Shift> getShifts(long l, long m) {
        ArrayList<Shift> shifts = new ArrayList<>();

        String[] cols = new String[]{"*"};
        final String whereClause = "end >= " + l + " and end <= " + m;

        Cursor c = getDb().query(SHIFT_TABLE, cols, whereClause, null, null, null, null, null);
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

    public void saveShift() {
        ContentValues vals = new ContentValues();

        long currentDateTime = new Date().getTime();

        Shift shift = new Shift();

        shift.end = currentDateTime;
        shift.start = getLastShift();

        vals.put("end", shift.end);
        vals.put("start", shift.start);

        getDb().insert(SHIFT_TABLE, null, vals);
    }

    private long getLastShift() {
        String[] cols = new String[]{"*"};

        Cursor c = getDb().query(SHIFT_TABLE, cols, null, null, null, null, null, null);
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

    public ArrayList<Shift> getDayShifts(long l, long m) {
        ArrayList<Shift> shifts = new ArrayList<>();

        String[] cols = new String[]{"*"};
        final String whereClause = "end >= " + l + " and end <= " + m;

        Cursor c = getDb().query(DAY_TABLE, cols, whereClause, null, null, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            Shift shift = new Shift();

            shift.id = c.getInt(c.getColumnIndex("day_id"));
            String start = c.getString(c.getColumnIndex("start"));
            String end = c.getString(c.getColumnIndex("end"));
            String note = c.getString(c.getColumnIndex("note"));

            shift.start = Long.valueOf(start);
            shift.end = Long.valueOf(end);
            shift.note = note;
            shift.carts = getReports1(shift.start, shift.end);
            Log.v("carts", "" + shift.carts.size());
            shifts.add(0, shift);

            c.moveToNext();
        }

        c.close();

        return shifts;
    }

    public void saveDayShift() {

        if (tranAfterLastShift()) {
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

    public boolean tranAfterLastShift() {

        long lastShift = getLastShift();

        String[] cols = new String[]{"*"};
        final String whereClause = "date >= " + lastShift;

        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            return true;
        }

        return false;
    }

    private long getLastDayShift() {
        String[] cols = new String[]{"*"};

        Cursor c = getDb().query(DAY_TABLE, cols, null, null, null, null, null, null);
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

    public boolean tranAfterLastDayShift() {
        long lastShift = getLastDayShift();

        String[] cols = new String[]{"*"};
        final String whereClause = "date >= " + lastShift;

        Cursor c = getDb().query(SALES_TABLE, cols, whereClause, null, null, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            return true;
        }

        return false;
    }

    public void insertDayShift(Shift shift) {
        Log.v("END DAY", "" + shift.id);
        ContentValues vals = new ContentValues();
        vals.put("day_id", shift.id);
        Calendar cal = Calendar.getInstance();

        long z = (long) cal.getTimeZone().getOffset(new Date().getTime());
        Log.v("Time", (shift.start * 1000 - z) + " - " + (shift.end * 1000 - z));

        if (shift.end != 0) {
            vals.put("end", shift.end * 1000 - z);
        } else {
            vals.put("end", 0);
        }

        if (shift.start != 0) {
            vals.put("start", shift.start * 1000 - z);
        } else {
            vals.put("start", 0);
        }

        Cursor c = getDb().rawQuery("select * from " + DAY_TABLE + " WHERE day_id ='" + shift.id + "'", null);

        if (c.getCount() > 0) {
            getDb().update(DAY_TABLE, vals, "day_id=" + shift.id, null);
        } else {
            getDb().insert(DAY_TABLE, null, vals);
        }
        c.close();
    }

    public void saveMercuryManual(String postXml, String invoice) {
        ContentValues vals = new ContentValues();

        vals.put("processed", -6);
        vals.put("response", postXml);
        vals.put("invoice", invoice);

        long currentDateTime = new Date().getTime();

        vals.put("date", "" + currentDateTime);

        getDb().insert(CCSALES_TABLE, null, vals);
    }

    public int getSaleIDNumber() {
        Cursor c = getDb().rawQuery("select _id from " + SALES_TABLE, null);
        int trans = 0;

        if (c.getCount() > 0) {
            c.moveToLast();
            trans = c.getInt(c.getColumnIndex("_id"));
        }
        c.close();
        return trans;
    }

    public int preAuthCount() {
        String[] cols = new String[]{"*"};
        final String whereClause = "processed = 0";

        Cursor c = getDb().query(CCSALES_TABLE, cols, whereClause, null, null,
                null, null, null);

        int count = c.getCount();
        c.close();

        return count;
    }

    public ArrayList<Cashier> getAdmins() {
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
            cashier.total = new BigDecimal(c.getFloat(c.getColumnIndex("total")));

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

    public void removePrinter(int position) {
        getDb().delete(RECEIPT_TABLE, "serverId=" + position, null);
        findReceiptSettings();
    }

    public Cursor getHardwareDevices() {

        Cursor mCursor = null;

        mCursor = getDb().rawQuery("select * from " + DEVICE_TABLE, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public Cursor getIngencioInfo() {
        Cursor mCursor = null;

        mCursor = getDb().rawQuery("select * from " + DEVICE_TABLE + " where type = '" + Consts.COMM_TYPE_INGENICO + "'", null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public void saveHardwareDevice(Device device, int id)
    {
        ContentValues vals = new ContentValues();

        vals.put("type", device.getDeviceType());
        vals.put("name", device.getDeviceName());
        vals.put("ipAddress", device.getIpaddress());
        vals.put("port", device.getPort());
        vals.put("_id", device.getId());

        if (id > 0) {
            int rowid = getDb().update(DEVICE_TABLE, vals, "_id=" + id, null);
            Log.e("Card update", "Rowid>>" + rowid);
        } else {
            int rowid = (int) getDb().insert(DEVICE_TABLE, null, vals);
            Log.e("Card insert", "Rowid>>" + rowid);
        }
    }

    public void removeHardwareDevice(int id) {
        getDb().delete(DEVICE_TABLE, "_id=" + id, null);
    }
    public void saveHardwareDeviceSync(Device device, int id)
    {
        ContentValues vals = new ContentValues();
        vals.put("type", device.getDeviceType());
        vals.put("name", device.getDeviceName());
        vals.put("ipAddress", device.getIpaddress());
        vals.put("port", device.getPort());
        vals.put("_id", device.getId());
        Cursor c = getDb().rawQuery("select * from " + DEVICE_TABLE + " WHERE _id =" + id, null);
        if (c.getCount() > 0)
        {
            int rowid = getDb().update(DEVICE_TABLE, vals, "_id=" + id, null);
            Log.e("Card update", "Rowid>>" + rowid);
        } else
        {
            int rowid = (int) getDb().insert(DEVICE_TABLE, null, vals);
            Log.e("Card insert", "Rowid>>" + rowid);
        }
    }

    public long saveCreditSale(Payment payment, String saleType) {
        ContentValues vals = new ContentValues();

        vals.put("cardType", payment.cardType);
        vals.put("date", payment.date);
        vals.put("lastFour", payment.lastFour);
        vals.put("invoice", payment.invoiceNo);
        vals.put("amount", payment.paymentAmount.toString());
        vals.put("saleType", saleType);
        vals.put("tipAmount", payment.tipAmount.toString());
        vals.put("response", payment.response);
        if (payment.invoiceNo.isEmpty())
            vals.put("offline", Payment.OFFLINE_CREDIT_SALE);
        else
            vals.put("offline", Payment.ONLINE_CREDIT_SALE);

        return (getDb().insert(CREDIT_SALE_TABLE, null, vals));
    }

    public Cursor getCreditSales(long l, long m) {

        Cursor mCursor = null;
        String whereClause = "date >= '" + l + "' and date <= '" + m + "'";
        mCursor = getDb().rawQuery("select * from " + CREDIT_SALE_TABLE + " WHERE " + whereClause, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public float getOfflineCreditSales() {
        Cursor mCursor = null;
        String whereClause = "offline = " + Payment.OFFLINE_CREDIT_SALE;
        mCursor = getDb().rawQuery("select SUM(amount) from " + CREDIT_SALE_TABLE + " WHERE " + whereClause, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            return mCursor.getFloat(0);
        }
        return 0;
    }

    public void updateOfflineCreditSales() {
        ContentValues newValues = new ContentValues();
        newValues.put("offline", Payment.OFFLINE_CREDIT_SALE);
        getDb().update(CREDIT_SALE_TABLE, newValues, "offline=" + Payment.ONLINE_CREDIT_SALE, null);
    }


}
