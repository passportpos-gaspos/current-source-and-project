package com.pos.passport.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class ReportCartBackup extends Cart {
    @IntDef({ PROCESS_STATUS_OFFLINE, PROCESS_STATUS_APPROVED, PROCESS_STATUS_DECLINED, PROCESS_STATUS_REVERSED, PROCESS_STATUS_VOID })
    @Retention(RetentionPolicy.SOURCE)
    public @interface  ProcessStatus {}

    public final static int PROCESS_STATUS_OFFLINE = 0;
    public final static int PROCESS_STATUS_APPROVED = 1;
    public final static int PROCESS_STATUS_DECLINED = -1;
    public final static int PROCESS_STATUS_REVERSED = -2;
    public final static int PROCESS_STATUS_VOID = -3;

	public String id;
	public String cartItems;
	private int mCashierId = 0;
    private int mCustomerId = 0;
	public int processed = PROCESS_STATUS_OFFLINE;
	public BigDecimal trans;
	public boolean isReceive;

    public ReportCartBackup(Context context)
	{
        super(context);
    }

	public void setCartItems(String cartItems)
	{
		this.cartItems = cartItems;
	}

	public String getCartItems() {
		return cartItems;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Bitmap getReceipt(FragmentActivity pointOfSale)
	{
    	LinearLayout mainView = new LinearLayout(pointOfSale);
    	mainView.setLayoutParams(new LinearLayout.LayoutParams(576, LayoutParams.WRAP_CONTENT));
    	mainView.setBackgroundColor(Color.WHITE);
    	mainView.setOrientation(LinearLayout.VERTICAL);
    	
    	TableLayout tl = new TableLayout(pointOfSale);
    	mainView.addView(tl);
    	tl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    	
    	//------------Store Name---------------------
    	
		if (!(StoreSetting.getName().equals("")))
		{
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
		if (!(StoreSetting.getAddress1().equals(""))){
			TableRow row = new TableRow(pointOfSale);
	    	tl.addView(row);
	    	   	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					
			TextView tv1 = new TextView(pointOfSale);
	        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
	
			tv1.setText(StoreSetting.getAddress1());
			tv1.setGravity(Gravity.CENTER);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.addView(tv1);
		}
		//---------------Store Number-----------------
		
		if (!(StoreSetting.getPhone().equals(""))){
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
		
		if (!(StoreSetting.getWebsite().equals(""))){
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
		
		if (!(StoreSetting.getEmail().equals(""))){
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
		
		String dateString = DateFormat.getDateTimeInstance().format(new Date(mDate));

		tv1.setText(dateString);
		tv1.setGravity(Gravity.START);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv1.setTypeface(null, Typeface.BOLD);
		tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		row.addView(tv1);
					
		TextView tv2 = new TextView(pointOfSale);

		tv2.setText(getString(R.string.txt_trans_label) + trans);
		tv2.setGravity(Gravity.START);
		tv2.setTypeface(null, Typeface.BOLD);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
		row.addView(tv2);
		
		//----------------Cashier Name-------------------

		if (mCashier != null) {
	    	row = new TableRow(pointOfSale);
	    	tl2.addView(row);
	    	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 5);
					
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
	
			tv1.setText(R.string.txt_cashier_label);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);
	
			tv2.setText(mCashier.name);
			tv2.setGravity(Gravity.START);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv2);
		}
		
		//----------------Customer Name/Email-------------------
		if (hasCustomer()) {
	    	row = new TableRow(pointOfSale);
	    	tl2.addView(row);
	    	
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			row.setPadding(0, 0, 0, 5);
					
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
	
			tv1.setText(getCustomerName());
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);
	
			tv2.setText(getCustomerEmail());
			tv2.setGravity(Gravity.START);
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
		BigDecimal nonDiscountTotal = BigDecimal.ZERO;
		for (int o = 0; o < getProducts().size(); o++) {
			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 0);
			tl3.addView(row);
								
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(getProducts().get(o).name + "\n" + getProducts().get(o).barcode);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f));
			row.addView(tv1);

			BigDecimal price = getProducts().get(o).itemPrice(mDate);
			nonDiscountTotal = nonDiscountTotal.add(getProducts().get(o).itemNonDiscountTotal(mDate));
			
			tv2.setText("" + getProducts().get(o).quantity + " @ " + DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
			tv2.setGravity(Gravity.START);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.175f));
			row.addView(tv2);
		
			price = getProducts().get(o).itemTotal(mDate);

			Product item = getProducts().get(o);
			
			if (item.cat != 0) {
				String cat = mDb.getCatById(item.cat);
				int catPos = mDb.getCatagoryString().indexOf(cat);

				if (catPos > -1) {
					if (mDb.getCatagories().get(catPos).getTaxable1() || mDb.getCatagories().get(catPos).getTaxable2()) {
						tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "T");
					} else{
						tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N");
					}
				} else{
					tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N");
				}
			} else{
				tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N");
			}
			
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.175f));

			row.addView(tv3);
		}
		
		row = new TableRow(pointOfSale);
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		row.setPadding(0, 6, 0, 6);
		tl3.addView(row);
		
		tv1 = new TextView(pointOfSale);
		tv2 = new TextView(pointOfSale);
		tv3 = new TextView(pointOfSale);
		
		tv1.setText(R.string.txt_subtotal);
		
		tv1.setGravity(Gravity.END);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
		row.addView(tv1);
		
		tv2.setText("");
		tv2.setGravity(Gravity.END);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv2);
		
		BigDecimal price = mSubtotal;
		tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
		tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv3.setGravity(Gravity.END);
		tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv3);
		
		if (mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(R.string.txt_discount_label);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText(mSubtotalDiscount.toString() + "%");
			tv2.setGravity(Gravity.END);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
					
			tv3.setText(DecimalFormat.getCurrencyInstance().format(mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED)));
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		if (mTax1Name != null) {
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(R.string.txt_tax_label + mTax1Name);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText(mTax1Percent + "%");
			tv2.setGravity(Gravity.END);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
					
			tv3.setText(DecimalFormat.getCurrencyInstance().format(mTax1.divide(Consts.HUNDRED)));
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		if (mTax2Name != null) {
			row.setPadding(0, 0, 0, 0);
			row = new TableRow(pointOfSale);
			tl3.addView(row);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(getString(R.string.txt_tax_label) + mTax2Name);
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
			
			tv2.setText(mTax2Percent + "%");
			tv2.setGravity(Gravity.END);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);
						
			tv3.setText(DecimalFormat.getCurrencyInstance().format(mTax2.divide(Consts.HUNDRED)));
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}

		row = new TableRow(pointOfSale);
		row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		row.setPadding(0, 0, 0, 6);
		tl3.addView(row);
		
		tv1 = new TextView(pointOfSale);
		tv2 = new TextView(pointOfSale);
		tv3 = new TextView(pointOfSale);
		
		tv1.setText(R.string.txt_total);
		
		tv1.setGravity(Gravity.START);
		tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearanceLarge);
		tv1.setTypeface(null, Typeface.BOLD);
		tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
		row.addView(tv1);
		
		tv2.setText("");
		tv2.setGravity(Gravity.END);
		tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
		tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv2);
		
		tv3.setText(DecimalFormat.getCurrencyInstance().format(mTotal.divide(Consts.HUNDRED)));
		tv3.setGravity(Gravity.END);
		tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearanceLarge);
		tv3.setTypeface(null, Typeface.BOLD);
		tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
		row.addView(tv3);
				
		BigDecimal paymentSum = BigDecimal.ZERO;
		for(int p = 0; p < mPayments.size(); p++) {
			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			tl3.addView(row);
			
			paymentSum = paymentSum.add(mPayments.get(p).paymentAmount);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(R.string.txt_tendered_type);
			
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
				
			tv2.setText(mPayments.get(p).paymentType);
			tv2.setGravity(Gravity.END);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);

			tv3.setText(DecimalFormat.getCurrencyInstance().format(mPayments.get(p).paymentAmount.divide(Consts.HUNDRED)));
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}
		
		if (paymentSum.compareTo(mTotal) > 0) {
			row = new TableRow(pointOfSale);
			row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
			row.setPadding(0, 0, 0, 6);
			tl3.addView(row);
			
			tv1 = new TextView(pointOfSale);
			tv2 = new TextView(pointOfSale);
			tv3 = new TextView(pointOfSale);
			
			tv1.setText(R.string.txt_customer_change);
			
			tv1.setGravity(Gravity.START);
			tv1.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
			row.addView(tv1);
		
		
			tv2.setText("");
			tv2.setGravity(Gravity.END);
			tv2.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv2);

			tv3.setText(DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(mTotal).divide(Consts.HUNDRED)));
			tv3.setGravity(Gravity.END);
			tv3.setTextAppearance(pointOfSale, R.style.receiptLayoutAppearance);
			tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
			row.addView(tv3);
		}
				
		if (!ReceiptSetting.blurb.equals("")) {
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
		mainView.measure(MeasureSpec.makeMeasureSpec(576, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		mainView.layout(0, 0, mainView.getMeasuredWidth(), mainView.getMeasuredHeight()); 
	
		Log.v("Size", "X: " + mainView.getWidth() + " Y:" + mainView.getHeight());
		            
		Bitmap mBitmap = Bitmap.createBitmap(mainView.getWidth(), mainView.getHeight(), Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(mBitmap);
		mainView.draw(canvas);
	
		return mBitmap;
	}

	
	public String getBody() {
		int cols = 40;
    	
		StringBuilder receiptString = new StringBuilder();

		//------------Store Name----------------------
		if (!(StoreSetting.getName().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols + 1)).append('\n');
		}
		
		//------------Store Address----------------------
		if (!(StoreSetting.getAddress1().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress1(), cols+1)).append('\n');
		}

		//---------------Store Number-----------------
		if (!(StoreSetting.getPhone().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
		}
		
		//-----------------Store Website----------------------
		if (!(StoreSetting.getWebsite().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
		}
		
		//-----------------------Store Email-----------------------------
		if (!(StoreSetting.getEmail().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
		}
		
		//-------------------Date------------------------
		if (StoreSetting.getReceipt_header() != null && !(StoreSetting.getReceipt_header().equals(""))){
			receiptString.append('\n');
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_header(), cols+1)).append('\n');
		}

		String date = DateFormat.getDateTimeInstance().format(new Date(mDate));
		receiptString.append('\n');
		receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
		receiptString.append(EscPosDriver.wordWrap(getString(R.string.txt_transaction_label) + " " + trans, cols+1)).append('\n');
		receiptString.append('\n'); 

		if (mCashier != null) {
			receiptString.append(EscPosDriver.wordWrap(getString(R.string.txt_cashier_label) + " " + mCashier.name, cols+1)).append('\n');
			receiptString.append('\n');
		}

		//----------------Customer Name/Email-------------------
		if (!TextUtils.isEmpty(getCustomerName())) {
			receiptString.append(EscPosDriver.wordWrap(getCustomerName(), cols+1)).append('\n');
		}
		if (!TextUtils.isEmpty(getCustomerEmail())) {
			receiptString.append(EscPosDriver.wordWrap(getCustomerEmail(), cols+1)).append('\n');
			receiptString.append('\n');
		}	
		
		//------------------Products-----------------------------------
		
		if (mVoided) {
			StringBuffer message;
			if (cols == 40)
				 message = new StringBuffer("---------------- " + getString(R.string.txt_voided_cap)  + " ----------------".substring(0, cols));
			else
				 message = new StringBuffer("----------- " + getString(R.string.txt_voided_cap) + " -----------".substring(0, cols));
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		BigDecimal nonDiscountTotal = BigDecimal.ZERO;
		for (int o = 0; o < mProducts.size(); o++) {

			BigDecimal price = mProducts.get(o).itemPrice(mDate);
			nonDiscountTotal = nonDiscountTotal.add(mProducts.get(o).itemNonDiscountTotal(mDate));
			
			receiptString.append(EscPosDriver.wordWrap(mProducts.get(o).name, cols+1)).append('\n');
			
			if (!mProducts.get(o).isNote) {
				if (!mProducts.get(o).barcode.isEmpty()) {
					receiptString.append(EscPosDriver.wordWrap(mProducts.get(o).barcode, cols+1)).append('\n');
				}
				
				StringBuffer message = new StringBuffer("                                        ".substring(0, cols));

				String quan = mProducts.get(o).quantity + " @ " + DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED));
				message.replace(0, quan.length(), quan);	
				
				price = mProducts.get(o).itemTotal(mDate);
				Product item = mProducts.get(o);
				
				String TotalPrice = "";
				
				if (item.cat != 0) {
					String cat = mDb.getCatById(item.cat);
					int catPos = mDb.getCatagoryString().indexOf(cat);
	
					if (catPos > -1) {
						if (mDb.getCatagories().get(catPos).getTaxable1() || mDb.getCatagories().get(catPos).getTaxable2()) {
							TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "T";
						} else {
							TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
						}
					} else {
						TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
					}
				} else {
					TotalPrice = DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)) + "N";
				}
				message.replace(message.length()-TotalPrice.length(), cols-1, TotalPrice);	
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			}
			receiptString.append('\n');
		}
		
		if (mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
			StringBuffer message = new StringBuffer((getString(R.string.txt_discount_label) + "                               ").substring(0, cols));

			String discountS = mSubtotalDiscount + "%";
			message.replace(11, 11 + discountS.length(), discountS);

			discountS = DecimalFormat.getCurrencyInstance().format(mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED));
			message.replace(message.length()-discountS.length(), cols-1, discountS);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		StringBuffer message = new StringBuffer((getString(R.string.txt_subtotal) + "                               ").substring(0, cols));
					
		String subprice = DecimalFormat.getCurrencyInstance().format(mSubtotal.divide(Consts.HUNDRED));
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

		if (mTax1Name != null) {
			message = new StringBuffer((getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
			
			String discountS = mTax1Name + " " + mTax1Percent + "%";
			message.replace(6, 6+discountS.length(), discountS);	
							
			String substring = DecimalFormat.getCurrencyInstance().format(mTax1.divide(Consts.HUNDRED));
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}

		if (mTax2Name != null) {
			message = new StringBuffer((getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));

			message.replace(6, 6+(mTax2Name + " " + mTax2Percent + "%").length(), mTax2Name + " " + mTax2Percent + "%");
						
			String substring = DecimalFormat.getCurrencyInstance().format(mTax2.divide(Consts.HUNDRED));
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		if (mTax3Name != null) {
			message = new StringBuffer((getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));

			message.replace(6, 6 + (mTax3Name + " " + mTax3Percent + "%").length(), mTax3Name + " " + mTax3Percent + "%");
						
			String substring = DecimalFormat.getCurrencyInstance().format(mTax3.divide(Consts.HUNDRED));
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
		}
		
		///----------------------
		message = new StringBuffer((getString(R.string.txt_total) + "                                   ").substring(0, cols));

		subprice = DecimalFormat.getCurrencyInstance().format(mTotal.divide(Consts.HUNDRED));
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');

		///----------------------
		if (mVoided) {
			if (cols == 40)
				 message = new StringBuffer("---------------- " + getString(R.string.txt_voided_cap) + " ----------------".substring(0, cols));
			else
				 message = new StringBuffer("----------- " + getString(R.string.txt_voided_cap) + " -----------".substring(0, cols));
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
		}
		
		BigDecimal paymentSum = BigDecimal.ZERO;
		for(int p = 0; p < mPayments.size(); p++) {
			paymentSum = paymentSum.add(mPayments.get(p).paymentAmount);

			///----------------------
			message = new StringBuffer((getString(R.string.txt_tender_type_label) + "                            ").substring(0, cols));

			message.replace(13, 13 + mPayments.get(p).paymentType.length(), mPayments.get(p).paymentType);

			subprice = DecimalFormat.getCurrencyInstance().format(mPayments.get(p).paymentAmount.divide(Consts.HUNDRED));
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
			///----------------------
		}
		
		receiptString.append('\n');

		if (paymentSum.compareTo(mTotal) > 0) {
			///----------------------
			message = new StringBuffer((getString(R.string.txt_customer_change_label) + ":                        ").substring(0, cols));

			subprice = DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(mTotal).divide(Consts.HUNDRED));
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
			///----------------------
		}
		
		if (StoreSetting.getReceipt_footer() != null && !(StoreSetting.getReceipt_footer().equals(""))){
			receiptString.append('\n');
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_footer(), cols+1)).append('\n');
			receiptString.append('\n'); 
		}

		return receiptString.toString();
	}

	public int getCashierId() {
		return mCashierId;
	}

	public void setCashierId(int cashierId) {
		this.mCashierId = cashierId;
		mCashier = mDb.getCashier(mCashierId);
	}

    public void setCustomerId(int customerId){
        this.mCustomerId = customerId;
        mCustomer = mDb.fetchCustomer(mCustomerId);
    }

	public void extractJson()
	{
		try
		{
			JSONObject jsonObject = new JSONObject(cartItems);
			
			if (jsonObject.has("customer")) {
				JSONObject customerData = jsonObject.getJSONObject("customer");

                Customer customer = new Customer();
				customer.id = customerData.optInt("id");
                customer.fName = customerData.optString("fname");
                customer.lName = customerData.optString("lname");
                customer.email = customerData.optString("email");
                setCustomer(customer);
			}
			
			if (jsonObject.has("percentDiscount")) {
				mSubtotalDiscount = new BigDecimal(jsonObject.getDouble("percentDiscount"));
			}
			
			if (jsonObject.has("Payments")) {
				JSONArray paymentArray = jsonObject.getJSONArray("Payments");
				
				for (int i = 0; i < paymentArray.length(); i++) {
					JSONObject paymentData = paymentArray.getJSONObject(i);
												
					Payment payment = new Payment();
					payment.paymentAmount = new BigDecimal(paymentData.optLong("paymentAmount"));
					payment.paymentType = paymentData.optString("paymentType");
					payment.gatewayId = paymentData.optString("gatewayId");
					payment.payload = paymentData.optString("payload");
					mPayments.add(payment);
				}
			}
			
			if (jsonObject.has("Products"))
			{
				JSONArray productArray = jsonObject.getJSONArray("Products");
				
				for (int i = 0; i < productArray.length(); i++)
				{
					JSONObject productData = productArray.getJSONObject(i);
												
					Product product = new Product();
					
					product.name = productData.getString("name");
					product.isNote = productData.getBoolean("isNote");
					product.id = productData.getInt("itemId");
					product.cat = productData.getInt("department");
					product.quantity = productData.getInt("quantity");
					product.price = new BigDecimal(productData.getLong("price"));
					product.cost = new BigDecimal(productData.getLong("cost"));
					product.barcode = productData.getString("barcode");
					product.salePrice = new BigDecimal(productData.getLong("salePrice"));
					product.startSale = productData.getLong("startSale");			
					product.endSale = productData.getLong("endSale");
                    product.discountName = productData.getString("discountName");
                    product.discountAmount = new BigDecimal(productData.getDouble("discountAmount"));
                    product.taxable = productData.getBoolean("taxable");


					if(productData.has("modifiers"))
					{
                        JSONArray modifierArray = productData.getJSONArray("modifiers");

                        for(int j=0; j < modifierArray.length(); j++ )
						{
                            JSONObject modifierData = modifierArray.getJSONObject(j);
                            Product modifier = new Product();
                            modifier.id = modifierData.getInt("id");
                            modifier.name = modifierData.getString("name");
                            modifier.price = new BigDecimal(modifierData.getLong("price"));
                            modifier.cost = new BigDecimal(modifierData.getLong("cost"));
                            modifier.cat = modifierData.getInt("department");

                            int type = modifierData.getInt("type");
                            switch (modifierData.getInt("type"))
							{
                                case Product.MODIFIER_TYPE_ADDON:
                                    modifier.modifierType = Product.MODIFIER_TYPE_ADDON;
                                    break;
                                case Product.MODIFIER_TYPE_DESC:
                                    modifier.modifierType = Product.MODIFIER_TYPE_DESC;
                                    break;
                                case Product.MODIFIER_TYPE_DISCOUNT_AMOUNT:
                                    modifier.modifierType = Product.MODIFIER_TYPE_DISCOUNT_AMOUNT;
                                    break;
                                case Product.MODIFIER_TYPE_DISCOUNT_PERCENT:
                                    modifier.modifierType = Product.MODIFIER_TYPE_DISCOUNT_PERCENT;
                                    break;
                                case Product.MODIFIER_TYPE_NONE:
                                    modifier.modifierType = Product.MODIFIER_TYPE_NONE;
                                    break;
                            }

                            product.modifiers.add(modifier);
                        }
                    }

					mProducts.add(product);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
