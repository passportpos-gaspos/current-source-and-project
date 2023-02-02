package com.passportsingle;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Product {

	public String name  = "";
	public String desc = "";
	public int cat;
	public int id;
	public int quantity = 1;
	public int onHand;
	public boolean isNote = false;
	public boolean expandBar = false;

	
	public long price;
	public long salePrice;
	
	public double discount = 0.0f;
	public double tempDiscount = 0.0f;
	public float subdiscount = 0.0f;

	public String barcode = "";
	public long cost = 0;
	private DecimalFormat df;
	
	public long endSale;
	public long lastSold;
	public long lastReceived;
	public int lowAmount;
	public int buttonID;
	public boolean taxable = true;
	public long startSale;
	
	
	public Product(){
		df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(false);
	}
	
	public Product(Product product) {
		df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(false);
		
		name = product.name;
		desc = product.desc;
		isNote = product.isNote;
		cat = product.cat;
		id = product.id;
		cost = product.cost;
		price = product.price;
		salePrice = product.salePrice;
		onHand = product.onHand;
		discount = product.discount;
		quantity = product.quantity;
		subdiscount = product.subdiscount;
		barcode = product.barcode;
		lastSold= product.lastSold;
		endSale= product.endSale;
		startSale= product.startSale;
		lastReceived= product.lastReceived;
		lowAmount= product.lowAmount;
		buttonID= product.buttonID;
		
	}
		
    public String displayPrice(long date)
    {
    	long price;
    	if(date >= this.startSale && date <= this.endSale)
    	{
    		price = this.salePrice;
    	}else{
    		price = this.price;
    	}
    	
		int alt=1;
		if(tempDiscount < 0) alt = -1; else alt = 1;
		if(tempDiscount == 0 && discount < 0) alt = -1;

    	long NewPrice = price - (long)(price * (discount / 100f)+0.5f*alt);
    	
    	if(NewPrice > 0)
    		NewPrice = NewPrice - (long)(NewPrice * (subdiscount / 100f)+0.5f);
    	
        return StoreSetting.getCurrency() + df.format(NewPrice / 100f);
    }

    public String displayTotal(long date)
    {
    	long price;
    	if(date >= this.startSale && date <= this.endSale)
    	{
    		price = this.salePrice;
    	}else{
    		price = this.price;
    	}
    	
		int alt=1;
		if(tempDiscount < 0) alt = -1; else alt = 1;
		if(tempDiscount == 0 && discount < 0) alt = -1;
		
    	long NewTotal = price - (long)(price * (discount / 100f)+0.5f*alt);
    	if(NewTotal > 0)
    		NewTotal = NewTotal - (long)(NewTotal * (subdiscount / 100f)+0.5f);
    	
    	NewTotal *= quantity;
        return StoreSetting.getCurrency() + df.format(NewTotal / 100f);
    }
    
    public long itemPrice(long date)
    {
    	long price;
    	if(date >= this.startSale && date <= this.endSale)
    	{
    		price = this.salePrice;
    	}else{
    		price = this.price;
    	}
    	
		int alt=1;
		if(tempDiscount < 0) alt = -1; else alt = 1;
		if(tempDiscount == 0 && discount < 0) alt = -1;
		
    	long NewPrice = price - (long)(price * (discount / 100f)+0.5f*alt);
    	if(NewPrice > 0)
    		NewPrice = NewPrice - (long)(NewPrice * (subdiscount / 100f)+0.5f);
        return NewPrice;
    }

    public long itemTotal(long date)
    {
    	long price;
    	if(date >= this.startSale && date <= this.endSale)
    	{
    		price = this.salePrice;
    	}else{
    		price = this.price;
    	}
    	
		int alt=1;
		if(tempDiscount < 0) alt = -1; else alt = 1;
		if(tempDiscount == 0 && discount < 0) alt = -1;
    	long NewTotal = price - (long)(price * (discount / 100f)+0.5f*alt);
    	if(NewTotal > 0)
    		NewTotal = NewTotal - (long)(NewTotal * (subdiscount / 100f)+0.5f);
    	
    	NewTotal *= quantity;
        return NewTotal;
    }
    
    public long itemNonDiscountTotal(long date)
    {
    	long price;
    	if(date >= this.startSale && date <= this.endSale)
    	{
    		price = this.salePrice;
    	}else{
    		price = this.price;
    	}
    	
		int alt=1;
		if(tempDiscount < 0) alt = -1; else alt = 1;
		if(tempDiscount == 0 && discount < 0) alt = -1;
		
    	long NewTotal = quantity * (price - (long)(price * (discount / 100f)+0.5f*alt));
        return NewTotal;
    }
}
