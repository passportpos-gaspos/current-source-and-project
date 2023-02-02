package com.passportsingle;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShopCart {
	public ArrayList<Product> Products = new ArrayList<Product>();
	public ArrayList<Payment> Payments = new ArrayList<Payment>();
	
	public long taxable1SubTotal = 0;
	public long taxable2SubTotal = 0;
	public long subTotal = 0;
	public long tax1 = 0;
	public long tax2 = 0;
	public long tax3 = 0;

	public float taxPercent1 = 0.0f;
	public float taxPercent2 = 0.0f;
	public float taxPercent3 = 0.0f;

	public long total = 0;
	public float subtotaldiscount = 0.0f;
	
	public String CustomerName = "";
	public String CustomerEmail = "";
	
	public String taxName1;
	public String taxName2;
	public String taxName3;

	private DecimalFormat nf;
	public Customer Customer;
	public Cashier Cashier;

	public String BluePayAuth;
	public String BluePayTransID;
	public long date;
	public boolean onHold = false;
	public String name = "";
	public long taxable3SubTotal = 0;
	public boolean voided = false;
	public int trans;
	public long id;
	
	public void setProducts(ArrayList<Product> products) {
		Products = products;
	}

	public ArrayList<Product> getProducts() {
		return Products;
	}
	
	public ShopCart(){
		nf = new DecimalFormat("0.00");
		nf.setRoundingMode(RoundingMode.HALF_UP);
		nf.setGroupingUsed(false);
	}

	void AddProduct(Product test1) {
		Products.add(test1);
	}
	
	void RemoveProduct(int index) {
		Products.remove(index);
		//quantities.remove(index);
	}

	//public void setQuantities(ArrayList<Integer> quantities) {
	//	this.quantities = quantities;
	///}

	//public ArrayList<Integer> getQuantities() {
	//	return quantities;
	//}

	public void removeAll() {
		Products.removeAll(Products);
		Payments.removeAll(Payments);
		Customer = null;
		Cashier = null;
		onHold = false;
		voided = false;
		CustomerName = "";
		CustomerEmail = "";
		subtotaldiscount = 0.0f;
		taxable1SubTotal = 0;
		taxable2SubTotal = 0;
		taxable3SubTotal = 0;
		subTotal = 0;
		tax1 = 0;
		tax2 = 0;
		tax3 = 0;
		taxPercent1 = 0.0f;
		taxPercent2 = 0.0f;
		taxPercent3 = 0.0f;
		total = 0;
		id = 0;
		taxName1 = null;
		taxName2 = null;
		taxName3 = null;
	}
	
	public String getDiscountDisplaySubTotal()
	{
    	long NewPrice = (long)(subTotal - subTotal * (subtotaldiscount / 100f));
        return StoreSetting.getCurrency() + nf.format(NewPrice / 100f);
	}
	
	public long getDiscountSubTotal()
	{
    	long NewPrice = (long)(subTotal - subTotal * (subtotaldiscount / 100f));
        return NewPrice;
	}
																																																																																																																																																																																
	public String getContentsforSquare() {

    	nf.setMinimumFractionDigits(2);
    	nf.setMaximumFractionDigits(2);
    	
		String contents = "";
		
        if(Products.size() > 0){    			        
        	int totalItems = 0;
	        for(int i=0;i<Products.size();i++){	
	        	totalItems = totalItems + Products.get(i).quantity;
	        }
	        contents = "Number of items: " + totalItems + ", ";
        	contents = contents + "Sub Total: $" + nf.format(subTotal) + ", ";
	        
	        if(taxName1 != null){
	        	if(!taxName1.equals("")){
	        		contents = contents + taxName1 + " $" + nf.format(tax1) + ", ";
	        	}
	        }
	        
	        if(taxName2 != null){
	        	if(!taxName2.equals("")){
	        		contents = contents + taxName2 + " $" + nf.format(tax2) + ", ";
	        	}
	        }
        	contents = contents + "Total: $" + nf.format(total);
        }else{
        	contents = "No products.";
        }
		
        if(contents.length() > 140){
        	contents = contents.substring(0, 139);
        }
        
		return contents;
	}
	
	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}

	public boolean hasCustomer() {
		if(Customer == null)
		{
			return false;
		}else{
			return true;
		}
	}

	public String getCustomerEmail() {
		return Customer.email;
	}

	public String getCustomerName() {
		return Customer.name;
	}

	public String getBody() {
		
		ShopCart cart = this;
		
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
	    	
		int cols = 40;
			
		StringBuilder receiptString = new StringBuilder();

		//------------Store Name----------------------
		if(!(StoreSetting.getName().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append("<br>");
		}
		
		//------------Store Address----------------------
		if(!(StoreSetting.getAddress().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress(), cols+1)).append("<br>");
		}

		//---------------Store Number-----------------
		if(!(StoreSetting.getPhone().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append("<br>");
		}
		
		//-----------------Store Website----------------------
		if(!(StoreSetting.getWebsite().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append("<br>");
		}
		
		//-----------------------Store Email-----------------------------
		if(!(StoreSetting.getEmail().equals(""))){
			receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append("<br>");
		}
		
		//-------------------Date------------------------	

		String date = DateFormat.getDateTimeInstance().format(new Date(cart.date));
		receiptString.append("<br>");
		receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append("<br>");
		receiptString.append(EscPosDriver.wordWrap("Transaction: " + ProductDatabase.getSaleNumber(), cols+1)).append("<br>");
		receiptString.append("<br>");

		if(cart.Cashier != null)
		{
			if(cart.Cashier.name.equals("Training"))
			{
				StringBuffer message = null;
				if(cols == 40)
					message = new StringBuffer("--------------- TRAINING ---------------".substring(0, cols));					

				else
					message = new StringBuffer("---------- TRAINING ----------".substring(0, cols));					
			
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");
			}else{
				receiptString.append(EscPosDriver.wordWrap("Cashier: " + cart.Cashier.name, cols+1)).append("<br>");
				receiptString.append("<br>");
			}
		}

		//----------------Customer Name/Email-------------------
		if(cart.Customer != null)
		{
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerName(), cols+1)).append("<br>");
			receiptString.append(EscPosDriver.wordWrap(cart.getCustomerEmail(), cols+1)).append("<br>");
			receiptString.append("<br>");
		}	
		
		if (cart.voided) {
            
			StringBuffer message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");
		}
		
		//------------------Products-----------------------------------
		
		long nonDiscountTotal = 0;

		for (int o = 0; o < cart.getProducts().size(); o++) {

			long price = cart.getProducts().get(o).itemPrice(cart.date);
			nonDiscountTotal += cart.getProducts().get(o).itemNonDiscountTotal(cart.date);
			
			receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).name, cols+1)).append("<br>");
			if(!cart.getProducts().get(o).isNote)
			{
				if(!cart.getProducts().get(o).barcode.isEmpty())
				{
					receiptString.append(EscPosDriver.wordWrap(cart.getProducts().get(o).barcode, cols+1)).append("<br>");
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
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
			}
			receiptString.append("<br>");
		}
		
		if(cart.subtotaldiscount > 0)
		{
			StringBuffer message = new StringBuffer("Discount:                               ".substring(0, cols));					
					
			String discountS = (int)cart.subtotaldiscount+"%";
			message.replace(11, 11+discountS.length(), discountS);	
							
			discountS = StoreSetting.getCurrency() + nf.format((cart.subTotal-nonDiscountTotal)/100f);
			message.replace(message.length()-discountS.length(), cols-1, discountS);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
		}
		
		StringBuffer message = new StringBuffer("Sub Total                               ".substring(0, cols));					
					
		String subprice = StoreSetting.getCurrency() + nf.format(cart.subTotal/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");

		if (cart.taxName1 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			String discountS = cart.taxName1 + " " + cart.taxPercent1 + "%";
			message.replace(6, 6+discountS.length(), discountS);	
							
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax1/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
		}

		if (cart.taxName2 != null) {
			                                         
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			message.replace(6, 6+(cart.taxName2 + " " + cart.taxPercent2 + "%").length(), cart.taxName2 + " " + cart.taxPercent2 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax2/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
		}
		
		if (cart.taxName3 != null) {
            
			message = new StringBuffer("TAX:                                    ".substring(0, cols));					
					
			message.replace(6, 6+(cart.taxName3 + " " + cart.taxPercent3 + "%").length(), cart.taxName3 + " " + cart.taxPercent3 + "%");	
						
			String substring = StoreSetting.getCurrency() + nf.format(cart.tax3/100f);
			message.replace(message.length()-substring.length(), cols-1, substring);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
		}
		
		///----------------------
		message = new StringBuffer("Total                                   ".substring(0, cols));					
					
		subprice = StoreSetting.getCurrency() + nf.format(cart.total/100f);
		message.replace(message.length()-subprice.length(), cols-1, subprice);	
		
		receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");

		///----------------------
		
		if (cart.voided) {
            
			message = null;
			if(cols == 40)
				 message = new StringBuffer("---------------- VOIDED ----------------".substring(0, cols));					
			else
				 message = new StringBuffer("----------- VOIDED -----------".substring(0, cols));					
		
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");
		}
		
		if(cart.Cashier != null)
		{
			if(cart.Cashier.name.equals("Training"))
			{
				if(cols == 40)
					message = new StringBuffer("--------------- TRAINING ---------------".substring(0, cols));					

				else
					message = new StringBuffer("---------- TRAINING ----------".substring(0, cols));					
			
				receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");
			}
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
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>");
			///----------------------
		}
		
		receiptString.append("<br>");

		if(paymentSum > cart.total)
		{
			///----------------------
			message = new StringBuffer("Customer Change:                        ".substring(0, cols));					
										
			subprice = StoreSetting.getCurrency() + nf.format((paymentSum-cart.total)/100f);
			message.replace(message.length()-subprice.length(), cols-1, subprice);	
			
			receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append("<br>").append("<br>");

			///----------------------
		}
		
		if(!ReceiptSetting.blurb.equals(""))
		{
			receiptString.append(EscPosDriver.wordWrap(ReceiptSetting.blurb, cols+1)).append("<br>").append("<br>");
		}

		return receiptString.toString();
	}

	public void AddCustomer(Customer customerData) {
		this.setCustomer(customerData);		
	}

	public Customer getCustomer() {
		return Customer;
	}

	public void setCustomer(Customer customer) {
		Customer = customer;
	}

	public void removeCustomer() {
		Customer = null;
	}
}
