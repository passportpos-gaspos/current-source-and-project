package com.pos.passport.model;

import android.support.annotation.IntDef;

import com.pos.passport.util.Consts;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class Product implements Serializable {
    @IntDef({ MODIFIER_TYPE_DESC, MODIFIER_TYPE_ADDON, MODIFIER_TYPE_DISCOUNT_AMOUNT, MODIFIER_TYPE_DISCOUNT_PERCENT, MODIFIER_TYPE_NONE, PRODUCT_TYPE_ITEM })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ModifierType {}

    public final static int MODIFIER_TYPE_DESC = 4;
	public final static int MODIFIER_TYPE_ADDON = 3;
    public final static int MODIFIER_TYPE_DISCOUNT_AMOUNT = 2;
    public final static int MODIFIER_TYPE_DISCOUNT_PERCENT = 1;
    public final static int MODIFIER_TYPE_NONE = 0;
    public final static int PRODUCT_TYPE_ITEM = 5;

	public String name  = "";
	public String desc = "";
	public int cat;
	public int id;
	public int quantity = 1;
	public int onHand;
	public boolean isNote = false;
	public boolean expandBar = false;
	public boolean deleted = false;
    public @ModifierType int modifierType;

    public BigDecimal maxPrice = BigDecimal.ZERO;
	public BigDecimal price = BigDecimal.ZERO;
	public BigDecimal salePrice = BigDecimal.ZERO;

    public BigDecimal discountPercent = BigDecimal.ZERO;
    public BigDecimal discountAmount = BigDecimal.ZERO;
	public BigDecimal discount = BigDecimal.ZERO;
	public BigDecimal tempDiscount = BigDecimal.ZERO;
	public BigDecimal subDiscount = BigDecimal.ZERO;
    public String discountName = "";

	public String barcode = "";
	public BigDecimal cost = BigDecimal.ZERO;

	public long endSale;
	public long lastSold;
	public long lastReceived;
	public int lowAmount;
	public int buttonID;
	public boolean taxable = true;
	public long startSale;
	public boolean track = true;
    public String comboItems;
    public String modi_data="";
    public String image="";
    public int combo;
    public int type_check=0;
    public int combo_id;
    public String comboname="";
    public ArrayList<Product> modifiers;
    public ArrayList<Product> combofiers;
    public boolean isAlcoholic = false;
    public boolean isTobaco = false;
    public BigDecimal total = BigDecimal.ZERO;
	
	public Product(){
        modifiers = new ArrayList<>();
        combofiers = new ArrayList<>();
	}



    public Product(Product product) {
		name = product.name;
		desc = product.desc;
		isNote = product.isNote;
		cat = product.cat;
		id = product.id;
		cost = product.cost;
        maxPrice = product.maxPrice;
		price = product.price;
		salePrice = product.salePrice;
		onHand = product.onHand;
		discount = product.discount;
		quantity = product.quantity;
		subDiscount = product.subDiscount;
		barcode = product.barcode;
		lastSold= product.lastSold;
		endSale= product.endSale;
		startSale= product.startSale;
		lastReceived= product.lastReceived;
		lowAmount= product.lowAmount;
		buttonID= product.buttonID;
		track= product.track;
        comboItems=product.comboItems;
        combo=product.combo;
        modi_data=product.modi_data;
        image=product.image;
        modifiers = new ArrayList<>();
        combofiers = new ArrayList<>();
	}
		
    public String displayPrice(long date) {
    	BigDecimal price;
        if (date >= this.startSale && date <= this.endSale)
        {
        	price = new BigDecimal(this.salePrice.toString());
    	} else
        {
        	price = new BigDecimal(this.price.toString());
    	}
    	
		int alt;
		if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;
		
		BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newPrice = price.subtract(dp);
    	if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }
        return DecimalFormat.getCurrencyInstance().format(newPrice.multiply(new BigDecimal(quantity)));
        //return DecimalFormat.getCurrencyInstance().format(newPrice.divide(Consts.HUNDRED).multiply(new BigDecimal(quantity)));
    }
    public BigDecimal displayPriceNew(long date)
    {
        BigDecimal price;
        BigDecimal m_total= BigDecimal.ZERO;
        if (date >= this.startSale && date <= this.endSale) {
            price = new BigDecimal(this.salePrice.toString());
        } else {
            price = new BigDecimal(this.price.toString());
        }

        int alt;
                if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
        if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newPrice = price.subtract(dp);
        if (newPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }

        newPrice = newPrice.multiply(new BigDecimal(quantity));
        for (int i = 0; i < modifiers.size(); i++)
        {

            if(modifiers.get(i).modifierType != Product.MODIFIER_TYPE_DESC && modifiers.get(i).modifierType != Product.MODIFIER_TYPE_ADDON)
            {
                BigDecimal sum1=modifiers.get(i).price();//.multiply( new BigDecimal(quantity));
                m_total = m_total.add(sum1);
            }else if(modifiers.get(i).modifierType == Product.MODIFIER_TYPE_DESC || modifiers.get(i).modifierType == Product.MODIFIER_TYPE_ADDON)
            {
                BigDecimal sum1=modifiers.get(i).price();
                m_total = m_total.add(sum1);
            }
        }
        //Log.e("Total m",">>"+m_total);
        //Log.e("total","with qunt>>>"+m_total.multiply(new BigDecimal(quantity)));
        newPrice=newPrice.add(m_total.multiply(new BigDecimal(quantity)));
        return newPrice;
        //return newPrice.divide(Consts.HUNDRED);
    }
    public BigDecimal displayPriceTax(long date) {
        BigDecimal price;
        if (date >= this.startSale && date <= this.endSale) {
            price = new BigDecimal(this.salePrice.toString());
        } else {
            price = new BigDecimal(this.price.toString());
        }

        int alt;
        if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
        if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newPrice = price.subtract(dp);
        if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }
        return newPrice.multiply(new BigDecimal(quantity));
        //return DecimalFormat.getCurrencyInstance().format(newPrice.divide(Consts.HUNDRED).multiply(new BigDecimal(quantity)));
    }
    public BigDecimal totalPrice(long date)
    {
        BigDecimal price;
        if (date >= this.startSale && date <= this.endSale) {
            price = new BigDecimal(this.salePrice.toString());
        } else {
            price = new BigDecimal(this.price.toString());
        }

        int alt;
        if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
        if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newPrice = price.subtract(dp);
        if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }

        newPrice = newPrice.multiply(new BigDecimal(quantity));
        return newPrice;
        //return newPrice.divide(Consts.HUNDRED);
    }

    public BigDecimal totalPriceNew(long date)
    {
        BigDecimal price;
        if (date >= this.startSale && date <= this.endSale) {
            price = new BigDecimal(this.salePrice.toString());
        } else {
            price = new BigDecimal(this.price.toString());
        }

        int alt;
        if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
        if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newPrice = price.subtract(dp);
        if (newPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }

        newPrice = newPrice.multiply(new BigDecimal(quantity));
        for (int i = 0; i < modifiers.size(); i++)
        {

            if(modifiers.get(i).modifierType != Product.MODIFIER_TYPE_DESC && modifiers.get(i).modifierType != Product.MODIFIER_TYPE_ADDON)
            {
                BigDecimal sum1=modifiers.get(i).price().multiply( new BigDecimal(quantity));
                newPrice = newPrice.add(sum1);
            }else if(modifiers.get(i).modifierType == Product.MODIFIER_TYPE_DESC || modifiers.get(i).modifierType == Product.MODIFIER_TYPE_ADDON)
            {
                BigDecimal sum1=modifiers.get(i).price();
                newPrice = newPrice.add(sum1);
            }
        }
        return newPrice;
        //return newPrice.divide(Consts.HUNDRED);
    }
    public String displayTotal(long date) {
    	BigDecimal price;
    	if (date >= this.startSale && date <= this.endSale) {
    		price = new BigDecimal(this.salePrice.toString());
    	} else {
    		price = new BigDecimal(this.price.toString());
    	}
    	
		int alt=1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
    	BigDecimal newTotal = price.subtract(dp);
    	if (newTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sd = newTotal.multiply(subDiscount.divide(Consts.HUNDRED));
            newTotal = newTotal.subtract(sd);
        }
    	
    	newTotal = newTotal.multiply(new BigDecimal(quantity));
        return DecimalFormat.getCurrencyInstance().format(newTotal);
        //return DecimalFormat.getCurrencyInstance().format(newTotal.divide(Consts.HUNDRED));
    }
    
    public BigDecimal itemPrice(long date){
    	BigDecimal price;
    	if (date >= this.startSale && date <= this.endSale) {
    		price = new BigDecimal(this.salePrice.toString());
    	} else {
    		price = new BigDecimal(this.price.toString());
    	}
    	
		int alt=1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
    	BigDecimal newPrice = price.subtract(dp);
    	if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sd = newPrice.multiply(subDiscount.divide(Consts.HUNDRED));
            newPrice = newPrice.subtract(sd);
        }
        return newPrice;
    }

    public BigDecimal itemTotal(long date) {
    	BigDecimal price;
    	if (date >= this.startSale && date <= this.endSale) {
    		price = this.salePrice;
    	} else {
    		price = this.price;
    	}
    	
		int alt=1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
    	BigDecimal newTotal = price.subtract(dp);
    	if (newTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sd = newTotal.multiply(subDiscount.divide(Consts.HUNDRED));
            newTotal = newTotal.subtract(sd);
        }
    	
    	newTotal = newTotal.multiply(new BigDecimal(quantity));
        newTotal = newTotal.subtract(this.discountAmount);

        return newTotal;
    }
    
    public BigDecimal itemNonDiscountTotal(long date) {
    	BigDecimal price;
    	if (date >= this.startSale && date <= this.endSale) {
    		price = new BigDecimal(this.salePrice.toString());
    	} else {
    		price = new BigDecimal(this.price.toString());
    	}
    	
		int alt;
		if (tempDiscount.compareTo(BigDecimal.ZERO) < 0) alt = -1; else alt = 1;
		if (tempDiscount.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) < 0) alt = -1;

        BigDecimal dp = price.multiply(discount.divide(Consts.HUNDRED));
        BigDecimal newTotal = new BigDecimal(quantity).multiply(price.subtract(dp));
        return newTotal;
    }

    public void addModifier(Product product)
    {
        int position = getPositionForModifier(product);
        //Log.e("Position",""+position);
        modifiers.add(position, product);
    }
    public ArrayList<Product> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<Product> modifiers) {
        this.modifiers = modifiers;
    }
    public void removeModifier(Product product) {
        modifiers.remove(product);
    }

    public int getPositionForModifier(Product product)
    {
        //Log.e("getPositionForModifier","getPositionForModifier>>"+modifiers.size());
        for (int i = 0; i < modifiers.size(); i++)
        {
            Product modifier = modifiers.get(i);
            if (product.modifierType > modifier.modifierType)
                return i;
        }
        //Log.e("getPositionForModifier","getPositionForModifier after>>"+modifiers.size());
        return modifiers.size();
    }

    public BigDecimal price() {
        long now = new Date().getTime();
        BigDecimal p;
        if (now >= this.startSale && now <= this.endSale)
            p = salePrice;
        else
            p = price;

        if (discount.compareTo(BigDecimal.ZERO) == 0)
            return p;
        else
            return p.multiply(discount.divide(Consts.HUNDRED).multiply(Consts.MINUS_ONE));
    }

    public BigDecimal total()
    {
        long now = new Date().getTime();
        BigDecimal sum;
        if (now >= this.startSale && now <= this.endSale)
            sum = salePrice;
        else
            sum = price;
        sum = sum.multiply(new BigDecimal(quantity));

        for (int i = 0; i < modifiers.size(); i++)
        {
            //sum = sum.add(modifiers.get(i).price());
            BigDecimal sum1=modifiers.get(i).price().multiply( new BigDecimal(quantity));
            sum = sum.add(sum1);
        }
        if(this.modifierType == MODIFIER_TYPE_DISCOUNT_PERCENT || this.modifierType == MODIFIER_TYPE_DISCOUNT_AMOUNT)
        {
            sum = sum.subtract(this.discountAmount);
        }
        return sum;
    }

    public BigDecimal totalBefore(Product modifier) {
        long now = new Date().getTime();
        BigDecimal sum;
        if (now >= this.startSale && now <= this.endSale)
            sum = salePrice;
        else
            sum = price;

        int position = modifiers.indexOf(modifier);
        if (position > 0) {
            for (int i = 0; i < position; i++) {
                sum = sum.add(modifiers.get(i).price());
            }
        }

        return sum;
    }

}
