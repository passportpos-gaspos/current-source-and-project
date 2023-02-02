package com.passportsingle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.passportsingle.R;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ReportCart {

	private ArrayList<Product> Products = new ArrayList<Product>();
	public ArrayList<Payment> Payments = new ArrayList<Payment>();

	public long subTotal = 0;
	public long tax1 = 0;
	public long tax2 = 0;
	public long tax3 = 0;

	public long total = 0; 

	public String id;

	public String taxName1;
	public String taxName2;
	public String taxName3;

	public long date;
	public String cartItems;

	public String CustomerName = "";
	public String CustomerEmail = "";
	public float taxPercent1;
	public float taxPercent2;
	public float taxPercent3;

	public float subtotaldiscount = 0.0f;
	
	private DecimalFormat nf;
	Customer Customer;
	private int CashierID;
	
	public Integer CartVersion = 0;
	public boolean voided = false;
	boolean hasCreditPayment = false;
	
	public Cashier cashier;
	public int trans;
	
	public ReportCart(){
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
	}

	public void setProducts(ArrayList<Product> products) {
		Products = products;
	}

	public ArrayList<Product> getProducts() {
		return Products;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getDate() {
		return date;
	}

	public void setCartItems(String cartItems) {
		this.cartItems = cartItems;
	}

	public String getCartItems() {
		return cartItems;
	}

	public void extractXML() {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			DataHandler dataHandler = new DataHandler();
			xr.setContentHandler(dataHandler);
			ByteArrayInputStream in = new ByteArrayInputStream(
					cartItems.getBytes());
			xr.parse(new InputSource(in));

			// data = dataHandler.getData();

		} catch (ParserConfigurationException pce) {
			Log.e("SAX XML", "sax parse error", pce);
		} catch (SAXException se) {
			Log.e("SAX XML", "sax error", se);
		} catch (IOException ioe) {
			Log.e("SAX XML", "sax parse io error", ioe);
		}
	}

	public void setTaxName3(String taxName3) {
		this.taxName3 = taxName3;
	}

	public String getTaxName3() {
		return taxName3;
	}
	
	public void setTaxName2(String taxName2) {
		this.taxName2 = taxName2;
	}

	public String getTaxName2() {
		return taxName2;
	}

	public void setTaxName1(String taxName1) {
		this.taxName1 = taxName1;
	}

	public String getTaxName1() {
		return taxName1;
	}

	public class DataHandler extends DefaultHandler {

		// private boolean _inSection, _inArea;


		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			
			if (localName.equals("Customer")) {
				CustomerName = atts.getValue("name");
				CustomerEmail = atts.getValue("email");
			}
			
			if (localName.equals("SubDiscount")) {
				subtotaldiscount = Float.valueOf(atts.getValue("percent"));
			}

			if (localName.equals("Payment")) {
				Payment payment = new Payment();
				payment.paymentAmount = Long.valueOf(atts.getValue("paymentAmount"));
				payment.paymentType = atts.getValue("paymentType");
				
				Payments.add(payment);
			}

			if (localName.equals("Item")) {
				Log.v("Item", atts.getValue("name"));
				
				Product product = new Product();
				
				product.name = atts.getValue("name");
				if(atts.getValue("isNote") != null)
					product.isNote = Boolean.valueOf(atts.getValue("isNote"));
				product.id = Integer.valueOf(atts.getValue("itemId"));
				product.cat = Integer.valueOf(atts.getValue("department"));
				product.quantity = (Integer.valueOf(atts.getValue("quantity")));
				product.price = Long.valueOf(atts.getValue("price"));
				product.cost = Long.valueOf(atts.getValue("cost"));
				product.discount = Float.valueOf(atts.getValue("discount"));
				if(atts.getValue("subdiscount") != null)
					product.subdiscount = Float.valueOf(atts.getValue("subdiscount"));

				product.barcode = atts.getValue("barcode");
				
				if(atts.getValue("salePrice") != null)
					product.salePrice = Long.valueOf(atts.getValue("salePrice"));				
				if(atts.getValue("startSale") != null)
					product.startSale = Long.valueOf(atts.getValue("startSale"));			
				if(atts.getValue("endSale") != null)
					product.endSale = Long.valueOf(atts.getValue("endSale"));

				Products.add(product);
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName, 
				String qName) throws SAXException {}
	}

	public boolean hasCustomer() {
		if (CustomerName.equals("")) {
			if (CustomerEmail.equals("")) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	public boolean hasCustomerEmail() {
		if (CustomerEmail.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public void setCustomerEmail(String customerEmail) {
		CustomerEmail = customerEmail;
	}

	public String getCustomerEmail() {
		return CustomerEmail;
	}

	public void setCustomerName(String customerName) {
		CustomerName = customerName;
	}

	public String getCustomerName() {
		return CustomerName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTaxPercent1(float tax1) {
		this.taxPercent1 = tax1;
	}

	public void setTaxPercent2(float tax2) {
		this.taxPercent2 = tax2;
	}

	public void setTaxPercent3(float tax3) {
		this.taxPercent3 = tax3;
	}
	
	public float getTaxPercent1() {
		return taxPercent1;
	}

	public float getTaxPercent2() {
		return taxPercent2;
	}

	public float getTaxPercent3() {
		return taxPercent3;
	}
	
	public Bitmap getReceipt(FragmentActivity pointOfSale) {
		
    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	
    	LinearLayout mainView = new LinearLayout(pointOfSale);
    	mainView.setLayoutParams(new LinearLayout.LayoutParams(576, LayoutParams.WRAP_CONTENT));
    	mainView.setBackgroundColor(Color.WHITE);
    	mainView.setOrientation(LinearLayout.VERTICAL);
    	
    	TableLayout tl = new TableLayout(pointOfSale);
    	mainView.addView(tl);
    	tl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	
    	//------------Store Name---------------------
    	
		if(!(StoreSetting.getName().equals(""))){
	    	TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
			row.setPadding(0, 5, 0, 2);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	
			tv1.setText(StoreSetting.getName());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearanceLarge);
			tv1.setTypeface(null, Typeface.BOLD);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		//------------Store Address----------------------
		if(!(StoreSetting.getAddress().equals(""))){
			TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
	
			tv1.setText(StoreSetting.getAddress());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		//---------------Store Number-----------------
		
		if(!(StoreSetting.getPhone().equals(""))){
			TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
	
			tv1.setText(StoreSetting.getPhone());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		
		//-----------------Store Website----------------------
		
		if(!(StoreSetting.getWebsite().equals(""))){
			TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
	
			tv1.setText(StoreSetting.getWebsite());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		
		//-----------------------Store Email-----------------------------
		
		if(!(StoreSetting.getEmail().equals(""))){
			TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
	
			tv1.setText(StoreSetting.getEmail());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		
		//-------------------Date------------------------
		
    	TableLayout tl2 = new TableLayout(pointOfSale);
    	mainView.addView(tl2);
    	tl2.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));		
		
    	TableRow row = new TableRow(pointOfSale);
    	tl2.addView(row);
    	   	
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		row.setPadding(0, 5, 0, 0);

		TextView tv1 = new TextView(pointOfSale);
        //String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
		
		String dateString = DateFormat.getDateTimeInstance().format(new Date(date));

		tv1.setText(dateString);
		tv1.setGravity(Gravity.LEFT);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv1.setTypeface(null, Typeface.BOLD);
		tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		row.addView(tv1);
					
		TextView tv2 = new TextView(pointOfSale);

		tv2.setText("Trans: " + trans);
		tv2.setGravity(Gravity.RIGHT);
		tv2.setTypeface(null, Typeface.BOLD);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		row.addView(tv2);
		
		//----------------Cashier Name-------------------

		if(cashier != null)
		{
	    	row = new TableRow(pointOfSale);
	    	tl2.addView(row);
	    	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 5);
					
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
	
			tv1.setText("Cashier:");
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);
	
			tv2.setText(cashier.name);
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv2);
		}
		
		//----------------Customer Name/Email-------------------
		if(hasCustomer())
		{
	    	row = new TableRow(pointOfSale);
	    	tl2.addView(row);
	    	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 5);
					
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
	
			tv1.setText(getCustomerName());
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);
	
			tv2.setText(getCustomerEmail());
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv2);
		}	
		//------------------Products-----------------------------------
		
    	TableLayout tl3 = new TableLayout(pointOfSale);
    	mainView.addView(tl3);
    	tl3.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	tl3.setBackgroundColor(Color.WHITE);
		
		TextView tv3;
		long nonDiscountTotal = 0;
		for (int o = 0; o < getProducts().size(); o++) {

			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 0);
			tl3.addView(row);
								
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(getProducts().get(o).name + "\n" + getProducts().get(o).barcode);
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);

			//float price = (getProducts().get(o).getPrice() - getProducts().get(o).getPrice()
			//				* (getProducts().get(o).getDiscount() / 100));

			long price = getProducts().get(o).itemPrice(date);
			nonDiscountTotal += getProducts().get(o).itemNonDiscountTotal(date);
			
			tv2.setText("" + getProducts().get(o).quantity + " @ "+StoreSetting.getCurrency() + nf.format(price/100f));
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.175f));
			row.addView(tv2);
		
			price = getProducts().get(o).itemTotal(date);

			Product item = getProducts().get(o);
			
			if (item.cat != 0) {
				String cat = ProductDatabase.getCatById(item.cat);
				int catPos = ProductDatabase.getCatagoryString().indexOf(cat);

				if (catPos > -1) {
					if (ProductDatabase.getCatagories().get(catPos).getTaxable1() || ProductDatabase.getCatagories().get(catPos).getTaxable2()) {
						tv3.setText(StoreSetting.getCurrency() + nf.format(price/100f)+"T");
					}else{
						tv3.setText(StoreSetting.getCurrency() + nf.format(price/100f)+"N");
					}
				}else{
					tv3.setText(StoreSetting.getCurrency() + nf.format(price/100f)+"N");
				}
			}else{
				tv3.setText(StoreSetting.getCurrency() + nf.format(price/100f)+"N");
			}
			
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.175f));

			row.addView(tv3);
		}
		
		row = new TableRow(pointOfSale);
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		row.setPadding(0, 6, 0, 6);
		tl3.addView(row);
		
		tv1 = new TextView(pointOfSale);
		tv2 = new TextView(pointOfSale);
		tv3 = new TextView(pointOfSale);
		
		tv1.setText("Sub Total");
		
		tv1.setGravity(Gravity.LEFT);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
		row.addView(tv1);
		
		tv2.setText("");
		tv2.setGravity(Gravity.RIGHT);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv2);
		
		float price = subTotal;
		tv3.setText(StoreSetting.getCurrency() + nf.format(price/100f));
		tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv3.setGravity(Gravity.RIGHT);
		tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv3);
		
		if(subtotaldiscount > 0)
		{
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText("Discount:" );
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText((int)subtotaldiscount+"%");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
					
			tv3.setText(StoreSetting.getCurrency() + nf.format((subTotal-nonDiscountTotal)/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		if (this.taxName1 != null) {
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText("TAX: " + this.taxName1);
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText(this.taxPercent1 + "%");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
					
			tv3.setText(StoreSetting.getCurrency() + nf.format( this.tax1/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		if (this.taxName2 != null) {
			row.setPadding(0, 0, 0, 0);
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText("TAX: " + this.taxName2 );
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText("" + this.taxPercent2 +"%");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
						
			tv3.setText(StoreSetting.getCurrency() + nf.format(this.tax2/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		row = new TableRow(pointOfSale);
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		row.setPadding(0, 0, 0, 6);
		tl3.addView(row);
		
		tv1 = new TextView(pointOfSale);
		tv2 = new TextView(pointOfSale);
		tv3 = new TextView(pointOfSale);
		
		tv1.setText("Total");
		
		tv1.setGravity(Gravity.LEFT);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearanceLarge);
		tv1.setTypeface(null, Typeface.BOLD);
		tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
		row.addView(tv1);
		
		tv2.setText("");
		tv2.setGravity(Gravity.RIGHT);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv2);
		
		tv3.setText(StoreSetting.getCurrency() + nf.format(total/100f));
		tv3.setGravity(Gravity.RIGHT);
		tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearanceLarge);
		tv3.setTypeface(null, Typeface.BOLD);
		tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv3);
				
		long paymentSum = 0;
		
		for(int p = 0; p < Payments.size(); p++)
		{
			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			tl3.addView(row);
			
			paymentSum += Payments.get(p).paymentAmount;
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText("Tendered Type");
			
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
				
			tv2.setText(Payments.get(p).paymentType);
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
		
			tv3.setText(StoreSetting.getCurrency() + nf.format(Payments.get(p).paymentAmount/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}
		
		if(paymentSum > total)
		{
			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			tl3.addView(row);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText("Customer Change");
			
			tv1.setGravity(Gravity.LEFT);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
		
		
			tv2.setText("");
			tv2.setGravity(Gravity.RIGHT);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
		
			tv3.setText(StoreSetting.getCurrency() + nf.format((paymentSum-total)/100f));
			tv3.setGravity(Gravity.RIGHT);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}
				
		if(!ReceiptSetting.blurb.equals(""))
		{
	    	tl2 = new TableLayout(pointOfSale);
	    	mainView.addView(tl2);
	    	tl2.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));		
			
	    	row = new TableRow(pointOfSale);
	    	tl2.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 10, 0, 0);

			tv1 = new TextView(pointOfSale);

			tv1.setText(ReceiptSetting.blurb);
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			//tv1.setTypeface(null, Typeface.BOLD);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);
		}
		// this is the important code :)  
		// Without it the view will have a dimension of 0,0 and the bitmap will be null          
		mainView.measure(MeasureSpec.makeMeasureSpec(576, MeasureSpec.EXACTLY), 
		            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mainView.layout(0, 0, mainView.getMeasuredWidth(), mainView.getMeasuredHeight()); 
	
		Log.v("Size", "X: "+ mainView.getWidth() + " Y:"+ mainView.getHeight());
		            
		Bitmap mBitmap = Bitmap.createBitmap(mainView.getWidth(), mainView.getHeight(),
		        Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(mBitmap);
		mainView.draw(canvas);
	
		return mBitmap;
	}

	
	public String getBody() {

		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		
		ReportCart cart = this;
	
		int cols = 40;
    	
		StringBuilder receiptString = new StringBuilder();

		//------------Store Name----------------------
		if(!(StoreSetting.getName().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
		}
		
		//------------Store Address----------------------
		if(!(StoreSetting.getAddress().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress(), cols+1)).append('\n');
		}

		//---------------Store Number-----------------
		if(!(StoreSetting.getPhone().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
		}
		
		//-----------------Store Website----------------------
		if(!(StoreSetting.getWebsite().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
		}
		
		//-----------------------Store Email-----------------------------
		if(!(StoreSetting.getEmail().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
		}
		
		//-------------------Date------------------------	

		String date = DateFormat.getDateTimeInstance().format(new Date(cart.date));
		receiptString.append('\n');
		receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
		receiptString.append(EscPosDriver.wordWrap("Transaction: " + cart.trans, cols+1)).append('\n');
		receiptString.append('\n'); 

		if(cart.cashier != null)
		{
			receiptString.append(EscPosDriver.wordWrap("Cashier: " + cart.cashier.name, cols+1)).append('\n');
			receiptString.append('\n');
		}

		//----------------Customer Name/Email-------------------
		if(CustomerName != null && !CustomerName.isEmpty())
		{
			receiptString.append(EscPosDriver.wordWrap(CustomerName, cols+1)).append('\n');
		}
		if(CustomerName != null && !CustomerEmail.isEmpty())
		{
			receiptString.append(EscPosDriver.wordWrap(CustomerEmail, cols+1)).append('\n');
			receiptString.append('\n');
		}	
		
		//------------------Products-----------------------------------
		
		if (cart.voided) {
            
			StringBuffer message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		long nonDiscountTotal = 0;

		for (int o = 0; o < cart.getProducts().size(); o++) {

			long price = cart.getProducts().get(o).itemPrice(cart.date);
			nonDiscountTotal += cart.getProducts().get(o).itemNonDiscountTotal(cart.date);
			
			receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).name, cols+1)).append('\n');
			
			if(!cart.getProducts().get(o).isNote)
			{
				if(!cart.getProducts().get(o).barcode.isEmpty())
				{
					receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).barcode, cols+1)).append('\n');
				}
				
				StringBuffer message = new StringBuffer("                                        ".substring(0, cols));	
	
				String quan = cart.getProducts().get(o).quantity + " @ "+StoreSetting.getCurrency() + nf.format(price/100f);
				message.replace(0, quan.length(), quan);	
				
				price = cart.getProducts().get(o).itemTotal(cart.date);
				Product item = cart.getProducts().get(o);
				
				String TotalPrice = "";
				
				if (item.cat != 0) {
					String cat = ProductDatabase.getCatById(item.cat);
					int catPos = ProductDatabase.getCatagoryString().indexOf(cat);
	
					if (catPos > -1) {
						if (ProductDatabase.getCatagories().get(catPos).getTaxable1() || ProductDatabase.getCatagories().get(catPos).getTaxable2()) {
							TotalPrice = StoreSetting.getCurrency() + nf.format(price/100f)+"T";
						}else{
							TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
						}
					}else{
						TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
					}
				}else{
					TotalPrice =StoreSetting.getCurrency() + nf.format(price/100f)+"N";
				}
				message.replace(message.length()-TotalPrice.length(), cols-1, TotalPrice);	
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			receiptString.append('\n');
		}
		
		if(cart.subtotaldiscount > 0)
		{
			StringBuffer message = new StringBuffer("Discount:                               ".substring(0, cols));					

			String discountS = (int)cart.subtotaldiscount+"%";
			message.replace(11, 11+discountS.length(), discountS);	
							
			discountS = StoreSetting.getCurrency() + nf.format((cart.subTotal-nonDiscountTotal)/100f);
			message.replace(message.length()-discountS.length(), cols-1, discountS);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		StringBuffer message = new StringBuffer("Sub Total                               ".substring(0, cols));					
					
		String subprice = StoreSetting.getCurrency() + nf.format(cart.subTotal/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

		if (cart.taxName1 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
			
			String discountS = cart.taxName1 + " " + cart.taxPercent1 + "%";
			message.replace(6, 6+discountS.length(), discountS);	
							
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax1/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}

		if (cart.taxName2 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					

			message.replace(6, 6+(cart.taxName2 + " " + cart.taxPercent2 + "%").length(), cart.taxName2 + " " + cart.taxPercent2 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax2/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		if (cart.taxName3 != null) {
            
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					

			message.replace(6, 6+(cart.taxName3 + " " + cart.taxPercent3 + "%").length(), cart.taxName3 + " " + cart.taxPercent3 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax3/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		///----------------------
		message = new StringBuffer("Total                                   ".substring(0, cols));					

		subprice = StoreSetting.getCurrency() + nf.format(cart.total/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

		///----------------------

		if (cart.voided) {
            
			message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		long paymentSum = 0;
		for(int p = 0; p < cart.Payments.size(); p++)
		{
			paymentSum += cart.Payments.get(p).paymentAmount;

			///----------------------
			message = new StringBuffer("Tender Type:                            ".substring(0, cols));					

			message.replace(13, 13+cart.Payments.get(p).paymentType.length(), cart.Payments.get(p).paymentType);	
			
			subprice = StoreSetting.getCurrency() + nf.format(cart.Payments.get(p).paymentAmount/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			///----------------------
		}
		
		receiptString.append('\n');

		if(paymentSum > cart.total)
		{
			///----------------------
			message = new StringBuffer("Customer Change:                        ".substring(0, cols));					

			subprice = StoreSetting.getCurrency() + nf.format((paymentSum-cart.total)/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

			///----------------------
		}
		
		if(!ReceiptSetting.blurb.equals(""))
		{
			receiptString.append(EscPosDriver.wordWrap(ReceiptSetting.blurb, cols+1)).append('\n').append('\n');
		}

		return receiptString.toString();
	}

	public int getCashierID() {
		return CashierID;
	}

	public void setCashierID(int cashierID) {
		CashierID = cashierID;
		cashier = ProductDatabase.getCashier(cashierID);
	}

}
