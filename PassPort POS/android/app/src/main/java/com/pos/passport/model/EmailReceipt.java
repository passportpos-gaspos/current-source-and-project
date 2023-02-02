package com.pos.passport.model;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Kareem on 2/12/2016.
 */
public class EmailReceipt {

    private Cart mCart;
    private Customer mCustomer;

    public String recipient;
    public String recipient_first_name;
    public String recipient_last_name;
    public long timestamp;
    public BigDecimal transaction_number;
    public String terminal_name ="";
    public String cashier_name;
    public ArrayList<Item> items;
    public BigDecimal discount_percent;
    public BigDecimal discount_amount;
    public String tender_type;
    public ArrayList<Tax> taxes;
    public BigDecimal total;
    public BigDecimal change = BigDecimal.ZERO;
    public ArrayList<Payment> payments;

    public EmailReceipt(Cart cart, Customer customer){
        this.mCart = cart;
        this.mCustomer = customer;
        init();
    }

    private void init(){
        if(mCustomer != null){
            this.recipient = mCustomer.email;
            this.recipient_first_name = mCustomer.fName;
            this.recipient_last_name = mCustomer.lName;
        }else if(mCart.hasCustomer()){
            this.recipient = mCart.getCustomer().email;
            this.recipient_first_name = mCart.getCustomer().fName;
            this.recipient_last_name = mCart.getCustomer().lName;
        }

        this.timestamp = mCart.getDate();
        this.transaction_number = mCart.mTrans;
        this.cashier_name = mCart.mCashier.name;
        this.items= new ArrayList<>();
        for(Product p : mCart.mProducts){
            Item item = new Item();
            item.name = p.name;
            item.quantity = p.quantity;
            item.price = p.price;
            item.discount = p.discount;
            items.add(item);
        }

        this.discount_percent = mCart.mDiscountPercent;
        this.discount_amount = mCart.mDiscountAmount;
        this.tender_type = mCart.mPayments.get(0).paymentType;

        this.taxes = new ArrayList<>();
        this.taxes.add(new Tax(mCart.getTax1Name(), mCart.mTaxable1SubTotal));
        this.taxes.add(new Tax(mCart.getTax2Name(), mCart.mTaxable2SubTotal));
        this.taxes.add(new Tax(mCart.getTax3Name(), mCart.mTaxable3SubTotal));
        BigDecimal paymentSum = BigDecimal.ZERO;

        for (int p = 0; p < mCart.mPayments.size(); p++) {
            paymentSum = paymentSum.add(mCart.mPayments.get(p).paymentAmount);
        }
        this.change = paymentSum.subtract(mCart.mTotal);
        this.payments = mCart.mPayments;
    }
}


class Item{
    public String name;
    public int quantity;
    public BigDecimal price;
    public BigDecimal discount;
}

class Tax{
    public String name;
    public BigDecimal amount;

    public Tax(String name, BigDecimal amount){
        this.name = name;
        this.amount = amount;
    }
}
