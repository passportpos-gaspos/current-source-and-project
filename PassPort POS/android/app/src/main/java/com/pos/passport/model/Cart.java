package com.pos.passport.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.support.annotation.StringRes;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.data.JSONHelper;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.ReceiptDialogFragment;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.MessageHandler;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by karim on 10/9/15.
 */
public class Cart implements Serializable
{
    public static final String PROCESSED = "PROCESSED";//"processed";
    public static final String VOIDED = "VOIDED";//"voided";
    public static final String RETURNED = "RETURNED";//"returned";
    public static final String REFUND = "REFUND";//"refund";

    protected transient Context mContext;
    public ArrayList<Product> mProducts;
    public ArrayList<Payment> mPayments=new ArrayList<>();

    public BigDecimal mTaxable1SubTotal = BigDecimal.ZERO;
    public BigDecimal mTaxable2SubTotal = BigDecimal.ZERO;
    public BigDecimal mTaxable3SubTotal = BigDecimal.ZERO;
    public BigDecimal mSubtotal = BigDecimal.ZERO;
    public BigDecimal mTax1 = BigDecimal.ZERO;
    public BigDecimal mTax2 = BigDecimal.ZERO;
    public BigDecimal mTax3 = BigDecimal.ZERO;

    public BigDecimal mTax1Percent = BigDecimal.ZERO;
    public BigDecimal mTax2Percent = BigDecimal.ZERO;
    public BigDecimal mTax3Percent = BigDecimal.ZERO;

    public BigDecimal mDiscountAmount = BigDecimal.ZERO;
    public BigDecimal mDiscountPercent = BigDecimal.ZERO;
    public String mDiscountName = "";
    public BigDecimal mTotal = BigDecimal.ZERO;
    public BigDecimal mSubtotalDiscount = BigDecimal.ZERO;

    public String mTax1Name;
    public String mTax2Name;
    public String mTax3Name;

    public Cashier mCashier;
    public Customer mCustomer;

    public long mDate;
    public boolean mOnHold = false;
    public String mName = "";

    public boolean mVoided = false;
    public BigDecimal mTrans;
    public int mIsProcessed;
    public boolean mHasTransNumber = false;
    public long mId;
    public boolean mIsReceived;
    public transient ProductDatabase mDb;
    public boolean mEnableTax = true;
    public boolean Alcoholic = false;
    public boolean Tobaco = false;
    public String mStatus = PROCESSED;
    public String mReturnReason = "";
    public ArrayList<TaxArray> taxarray=new ArrayList<>();

    public int cOrder=0;
    public int returnStatus=0;
    public BigDecimal totalReturn = BigDecimal.ZERO;

    public boolean mRecent = false;
    public BigDecimal mChangeAmount=BigDecimal.ZERO;
    public Cart(Context context)
    {
        this.mContext = context;
        mProducts = new ArrayList<>();
        mPayments = new ArrayList<>();
        mDb = ProductDatabase.getInstance(context);
    }

    public Cart() {
    }

    public void setProducts(ArrayList<Product> products) {
        mProducts = products;
    }

    public ArrayList<Product> getProducts() {
        return mProducts;
    }

    public ArrayList<TaxArray> getTaxarray() {
        return taxarray;
    }
    /*public void addTaxarray(TaxArray taxArray)
    {
    taxarray.add(taxArray);
    }
    public void setTaxarray(ArrayList<TaxArray> taxarray) {
        this.taxarray = taxarray;
    }*/

    public int addProduct(Product product) {
        if(product.combo > 0) {
            mProducts.add(product);
            return -1;
        }

        Product sameProduct = findSameProduct(product);
        if (sameProduct == null) {
            mProducts.add(product);
            return -1;
        } else {
            sameProduct.quantity++;
            return mProducts.indexOf(sameProduct);
        }
    }

    private Product findSameProduct(Product product) {
        if (product.id == 0)
            return  null;
        for (Product p : mProducts) {
            if (p.id == product.id && p.modifiers.size() == 0)
                return p;
        }

        return null;
    }

    public void updateProduct(Product product, int position) {
        if (position != -1)
            mProducts.set(position, product);
    }

    public void removeProduct(int index) {
        mProducts.remove(index);
    }

    public void removeAll() {
        mProducts.clear();
        mPayments.clear();
        taxarray.clear();
        taxarray=new ArrayList<>();
        mCustomer = null;
        mCashier = null;
        mOnHold = false;
        mVoided = false;
        mHasTransNumber = false;
        mIsReceived = false;
        mSubtotalDiscount = BigDecimal.ZERO;
        mTaxable1SubTotal = BigDecimal.ZERO;
        mTaxable2SubTotal= BigDecimal.ZERO;
        mTaxable3SubTotal = BigDecimal.ZERO;
        mDiscountAmount = BigDecimal.ZERO;
        mDiscountPercent = BigDecimal.ZERO;
        mSubtotal = BigDecimal.ZERO;
        mTax1 = BigDecimal.ZERO;
        mTax2= BigDecimal.ZERO;
        mTax3 = BigDecimal.ZERO;
        mTax1Percent = BigDecimal.ZERO;
        mTax2Percent = BigDecimal.ZERO;
        mTax3Percent = BigDecimal.ZERO;
        mTotal = BigDecimal.ZERO;
        mId = 0;
        mTax1Name = null;
        mTax2Name = null;
        mTax3Name = null;
        mEnableTax = true;
        mStatus = PROCESSED;
        mDiscountName = "";
        mReturnReason = "";
        cOrder=0;
        returnStatus=0;
        totalReturn = BigDecimal.ZERO;
        mRecent = false;
        Alcoholic = false;
        Tobaco = false;
        mChangeAmount=BigDecimal.ZERO;
    }

    public String getDiscountDisplaySubTotal() {
        return DecimalFormat.getCurrencyInstance().format(getDiscountSubTotal());
    }

    public BigDecimal getDiscountSubTotal() {
        BigDecimal subTotal = mSubtotal.subtract(mSubtotal.multiply(mSubtotalDiscount.divide(Consts.HUNDRED)));
        return subTotal;
    }

    public String getString(@StringRes int resId) {
        return mContext.getString(resId);
    }

    public Customer getCustomer() {
        return mCustomer;
    }

    public void setCustomer(Customer customer) {
        this.mCustomer = customer;
    }

    public boolean hasCustomer() {
        if (this.mCustomer != null)
            return true;
        else
            return false;
    }

    public String getCustomerName() {
        if (this.mCustomer != null)
            return mCustomer.fName;
        else
            return "";
    }

    public String getCustomerFullName(){
        if (this.mCustomer != null)
            return String.format("%s %s",mCustomer.fName,mCustomer.lName);
        else
            return "";
    }

    public String getCustomerLastName(){
        if (this.mCustomer != null)
            return mCustomer.lName;
        else
            return "";
    }

    public String getCustomerEmail() {
        if (this.mCustomer != null)
            return mCustomer.email;
        else
            return null;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public long getDate() {
        return mDate;
    }

    public void setTax1Name(String tax1Name) {
        this.mTax1Name = tax1Name;
    }

    public String getTax1Name() {
        return mTax1Name;
    }

    public void setTax2Name(String tax2Name) {
        this.mTax2Name = tax2Name;
    }

    public String getTax2Name() {
        return mTax2Name;
    }

    public void setTax3Name(String tax3Name) {
        this.mTax3Name = mTax3Name;
    }

    public String getTax3Name() {
        return this.mTax3Name;
    }

    public void setTax1Percent(BigDecimal tax1Percent) {
        this.mTax1Percent = tax1Percent;
    }

    public BigDecimal getTax1Percent() {
        return mTax2Percent;
    }

    public void setTax2Percent(BigDecimal tax2Percent) {
        this.mTax2Percent = tax2Percent;
    }

    public BigDecimal getTax2Percent() {
        return mTax2Percent;
    }

    public void setTax3Percent(BigDecimal tax3Percent) {
        this.mTax3Percent = tax3Percent;
    }

    public BigDecimal getTax3Percent() {
        return mTax3Percent;
    }
    
    public Cart clone() {
        Cart cart = new Cart(this.mContext);
        cart.mProducts.addAll(this.mProducts);
        cart.mPayments.addAll(this.mPayments);
        
        cart.mTaxable1SubTotal = this.mTaxable1SubTotal;
        cart.mTaxable2SubTotal = this.mTaxable2SubTotal;
        cart.mTaxable3SubTotal = this.mTaxable3SubTotal;
        cart.mSubtotal = this.mSubtotal;
        cart.mTax1 = this.mTax1;
        cart.mTax2 = this.mTax2;
        cart.mTax3 = this.mTax3;

        cart.mTax1Percent = this.mTax1Percent;
        cart.mTax2Percent = this.mTax2Percent;
        cart.mTax3Percent = this.mTax3Percent;

        cart.mTotal = this.mTotal;
        cart.mSubtotalDiscount = this.mSubtotalDiscount;

        cart.mTax1Name = this.mTax1Name;
        cart.mTax2Name = this.mTax2Name;
        cart.mTax3Name = this.mTax3Name;

        if (this.mCashier != null)
            cart.mCashier = this.mCashier.clone();
        if (this.mCustomer != null)
            cart.mCustomer = this.mCustomer.clone();

        cart.mDate = mDate;
        cart.mOnHold = this.mOnHold;
        cart.mName = this.mName;

        cart.mHasTransNumber = this.mHasTransNumber;
        cart.mVoided = this.mVoided;
        cart.mTrans = this.mTrans;
        cart.mIsProcessed = this.mIsProcessed;
        cart.mId = this.mId;
        cart.mIsReceived = this.mIsReceived;
        cart.mDb = ProductDatabase.getInstance(this.mContext);

        return cart;
    }

    public boolean hasDiscountAmount(){
        return mDiscountAmount.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean hasDiscountPercent(){
        return mDiscountPercent.compareTo(BigDecimal.ZERO) != 0;
    }

    public String getCartReceipt(Context context, int cols){
        StringBuilder receiptString =  new StringBuilder();
        try {
            JSONObject cartJson = new JSONObject();
            JSONArray jsonPayments = JSONHelper.toJSONPaymentArray(context, this, "");
            cartJson.put("Payments", jsonPayments);

            JSONArray jsonProducts = JSONHelper.toJSONProductArray(context, this);
            cartJson.put("Products", jsonProducts);

            JSONObject jsoncustomer = JSONHelper.toJSONcustomer(context, this);
            cartJson.put("customer", jsoncustomer);

            JSONArray jsontax = JSONHelper.toJSONTaxArray(context, this);
            cartJson.put("tax", jsontax);

            receiptString.append(ReceiptSetting.getReceiptHeader(cols));

            String date = DateFormat.getDateTimeInstance().format(new Date(mDate));
            receiptString.append('\n');
            receiptString.append(EscPosDriver.wordWrap(date, cols + 1)).append('\n');
            receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_transaction_label) + " " + mTrans, cols + 1)).append('\n');
            receiptString.append('\n');

            if(mCashier != null) {
                if(mCashier.name.equals("Training")) {
                    StringBuffer message = null;
                    if(cols == 40)
                        message = new StringBuffer("--------------- " + context.getString(R.string.txt_training_cap) + " ---------------".substring(0, cols));

                    else
                        message = new StringBuffer("---------- " + context.getString(R.string.txt_training_cap) + " ----------".substring(0, cols));

                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                } else {
                    receiptString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_cashier_label) + " " + mCashier.name, cols+1)).append('\n');
                    receiptString.append('\n');
                }
            }

            if (mVoided) {
                StringBuffer message = null;
                if (cols == 40)
                    message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
                else
                    message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));

                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
            }

            if(mStatus.equals(Cart.RETURNED)){
                StringBuffer message = null;
                if (cols == 40)
                    message = new StringBuffer(("---------------- " + context.getString(R.string.txt_return_cap) + " ----------------").substring(0, cols));
                else
                    message = new StringBuffer(("----------- " + context.getString(R.string.txt_return_cap) + " -----------").substring(0, cols));

                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
            }

            for(int p =0; p<cartJson.getJSONArray("Products").length() ; p++){
                JSONObject product = cartJson.getJSONArray("Products").optJSONObject(p);
                StringBuilder prodBuilder = new StringBuilder(product.getString("quantity"));
                prodBuilder.append(" ").append(product.getString("name"));
                while(prodBuilder.length() < 40){
                    prodBuilder.append(" ");
                }
                StringBuffer prod = new StringBuffer(prodBuilder.toString().substring(0,cols));
                String total = Utils.formatCurrency(new BigDecimal(product.optString("total")));
                prod.replace(prod.length() - total.length(), cols - 1, total);
                receiptString.append(EscPosDriver.wordWrap(prod.toString(), cols+1)).append('\n');

                /*if(!product.optString("barcode").equals("") || !product.optString("barcode").equals(null))
                    receiptString.append(EscPosDriver.wordWrap(product.optString("barcode"), cols+1)).append('\n');*/
                    /*StringBuffer message = new StringBuffer("                                        ".substring(0, cols));
                    String quan = product.optInt("quantity") + " @ " + DecimalFormat.getCurrencyInstance().format(product.optString("total"));
                    //message.replace(0, quan.length(), quan);
                    message.replace(message.length() - quan.length(), cols - 1, quan);
                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');*/
                Log.d("receipt", "product: " + prod.toString());
                if(new BigDecimal(product.optString("discountAmount")).compareTo(BigDecimal.ZERO) > 0){
                    Log.d("receipt:", "discountAmount:" + product.optString("discountAmount"));
                    StringBuffer discountMessage = new StringBuffer((product.optString("discountName") + "                                ").substring(0, cols));
                    String discount = Utils.formatDiscount(new BigDecimal(product.optString("discountAmount")));
                    discountMessage.replace(discountMessage.length()-discount.length(), cols-1, discount);
                    receiptString.append(EscPosDriver.wordWrap(discountMessage.toString(), cols+1)).append('\n');
                    Log.d("receipt", "Item Discount: " + discountMessage.toString());
                }

                for(int m=0; m<product.getJSONArray("modifiers").length() ; m++){
                    JSONObject modifier = product.getJSONArray("modifiers").optJSONObject(m);
                    StringBuffer mMessage = new StringBuffer("  ");
                    //StringBuffer mMessage = new StringBuffer("                                        ".substring(0, cols));
                    String modifierPrice = Utils.formatCurrency(new BigDecimal(modifier.optString("price")));
                    String modifierString = modifier.optString("name").concat("(").concat(modifierPrice).concat(")");
                    //mMessage.replace(mMessage.length() - modifierString.length(), cols-1, modifierString);
                    //receiptString.append(EscPosDriver.wordWrap(mMessage.toString(), cols+1)).append('\n');
                    receiptString.append(EscPosDriver.wordWrap(mMessage.append(modifierString).toString(), cols+1)).append('\n');
                    Log.d("receipt", "Modifiers " + mMessage.toString());
                }

                for(int c=0; c<product.getJSONArray("comboItems").length(); c++){
                    JSONObject comboItems = product.getJSONArray("comboItems").optJSONObject(c);
                    for(int ci=0; ci< comboItems.getJSONArray("items").length(); ci++){
                        JSONObject items = comboItems.getJSONArray("items").optJSONObject(ci);
                        StringBuffer mMessage = new StringBuffer("  ");
                        //StringBuffer mMessage = new StringBuffer("                                        ".substring(0, cols));
                        //mMessage.replace(mMessage.length() - items.optString("name").length(),cols-1, items.optString("name"));
                        receiptString.append(EscPosDriver.wordWrap(mMessage.append(items.optString("name")).toString(), cols+1)).append('\n');
                        Log.d("receipt", "Combo items : " + mMessage.toString());
                    }
                }

            }

            StringBuffer subTotalMessage = new StringBuffer((context.getString(R.string.txt_sub_total) + "                               ").substring(0, cols));
            String subTotal = Utils.formatCurrency(mSubtotal);
            subTotalMessage.replace(subTotalMessage.length()-subTotal.length(), cols-1, subTotal);
            receiptString.append(EscPosDriver.wordWrap(subTotalMessage.toString(), cols+1)).append('\n');
            Log.d("receipt", "Sub Total : " + subTotalMessage.toString());

            BigDecimal totalTax = BigDecimal.ZERO;
            for(int tax=0; tax < cartJson.getJSONArray("tax").length(); tax++){
                JSONObject taxes = cartJson.getJSONArray("tax").optJSONObject(tax);
                totalTax = totalTax.add(new BigDecimal(taxes.optString("amount")));
            }

            StringBuffer taxMessage = new StringBuffer((context.getString(R.string.txt_tax_label) + "                                    ").substring(0, cols));
            String taxAmount = Utils.formatCurrency(totalTax);
            taxMessage.replace(taxMessage.length()-taxAmount.length(), cols-1, taxAmount);
            receiptString.append(EscPosDriver.wordWrap(taxMessage.toString(), cols+1)).append('\n');
            Log.d("receipt:", "tax:" + taxMessage.toString());

            if(mDiscountAmount.compareTo(BigDecimal.ZERO) > 0){
                StringBuffer discountMessage = new StringBuffer((mDiscountName + "                                ").substring(0, cols));
                String discount = Utils.formatCurrency(new BigDecimal(mDiscountAmount.toString()));
                discountMessage.replace(discountMessage.length()-discount.length(), cols-1, discount);
                receiptString.append(EscPosDriver.wordWrap(discountMessage.toString(), cols+1)).append('\n');
                Log.d("receipt", "Discount: "+ discountMessage.toString());
            }


            BigDecimal tipAmount =  BigDecimal.ZERO;
            for(int p=0; p < cartJson.getJSONArray("Payments").length(); p++)
            {
                JSONObject payment = cartJson.getJSONArray("Payments").optJSONObject(p);
                tipAmount = tipAmount.add(new BigDecimal(payment.optString("tipAmount")));
            }
            if(tipAmount.compareTo(BigDecimal.ZERO) > 0){
                StringBuffer tipTotalMessage = new StringBuffer((context.getString(R.string.txt_tip_amount) + "                                   ").substring(0, cols));
                String tipTotalAmount = Utils.formatCurrency(tipAmount);
                tipTotalMessage.replace(tipTotalMessage.length() - tipTotalAmount.length(), cols - 1, tipTotalAmount);
                receiptString.append(EscPosDriver.wordWrap(tipTotalMessage.toString(), cols+1)).append('\n');
                Log.d("receipt", "Tip Total: "+tipTotalMessage.toString());
            }

            StringBuffer totalMessage = new StringBuffer((context.getString(R.string.txt_total) + "                                   ").substring(0, cols));
            String totalAmount = Utils.formatCurrency(mTotal.add(tipAmount));
            totalMessage.replace(totalMessage.length() - totalAmount.length(), cols - 1, totalAmount);
            receiptString.append(EscPosDriver.wordWrap(totalMessage.toString(), cols+1)).append('\n');
            Log.d("receipt", "Total: "+totalMessage.toString());

            StringBuffer message;
            BigDecimal amountReceived = BigDecimal.ZERO;

            ArrayList<Byte> signImage = null;
            StringBuffer paymentStringBuffer = new StringBuffer();
            for(int p=0; p < cartJson.getJSONArray("Payments").length(); p++){
                JSONObject payment = cartJson.getJSONArray("Payments").optJSONObject(p);
                message = new StringBuffer((context.getString(R.string.txt_tender_type_label) + "                            ").substring(0, cols));
                message.replace(13, 13 + payment.optString("paymentType").length(), payment.optString("paymentType"));
                amountReceived = amountReceived.add(new BigDecimal(payment.optString("paymentAmount")));
                String amount = Utils.formatCurrency(new BigDecimal(payment.optString("paymentAmount")));
                message.replace(message.length()-amount.length(), cols-1, amount);
                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
                Log.d("receipt", "payment: " + message.toString());
                //tipAmount = tipAmount.add(new BigDecimal(payment.optString("tipAmount")));
                switch(payment.optString("paymentType")){
                    case PayActivity.PAYMENT_TYPE_CASH:
                        break;
                    case PayActivity.PAYMENT_TYPE_CHECK:
                        break;
                    case PayActivity.PAYMENT_TYPE_CREDIT:
                        if (cols == 40)
                            message = new StringBuffer(("---------------- " + context.getString(R.string.txt_credit) + " ----------------").substring(0, cols));

                        else
                            message = new StringBuffer(("----------- " + context.getString(R.string.txt_credit) + " -----------").substring(0, cols));
                       // signImage = Utils.printImage(Utils.convertBase64_2_Bitmap(payment.optString("signImage")));
                        paymentStringBuffer.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                        paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_card) , payment.optString("lastFour")),cols+1)).append('\n');
                        paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_card_type), payment.optString("cardType")),cols+1)).append('\n');
                        paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_authorization), payment.optString("authCode")),cols+1)).append('\n');
                        paymentStringBuffer.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.txt_reference), payment.optString("gatewayId")),cols+1)).append('\n');
                        break;

                }

            }

            StringBuffer changetotalMessage = new StringBuffer((context.getString(R.string.txt_changeamount) + "                                   ").substring(0, cols));
            String changetotalAmount = Utils.formatCurrency(mChangeAmount);
            changetotalMessage.replace(changetotalMessage.length() - changetotalAmount.length(), cols - 1, changetotalAmount);
            receiptString.append(EscPosDriver.wordWrap(changetotalMessage.toString(), cols+1)).append('\n');
            Log.d("receipt", "ChangeAmount: "+changetotalMessage.toString());



            if (mVoided)
            {
                if(cols == 40)
                    message = new StringBuffer(("---------------- " + context.getString(R.string.txt_voided_cap) + " ----------------").substring(0, cols));
                else
                    message = new StringBuffer(("----------- " + context.getString(R.string.txt_voided_cap) + " -----------").substring(0, cols));

                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
            }
            if(mStatus.equals(Cart.RETURNED)){
                if (cols == 40)
                    message = new StringBuffer(("---------------- " + context.getString(R.string.txt_return_cap) + " ----------------").substring(0, cols));
                else
                    message = new StringBuffer(("----------- " + context.getString(R.string.txt_return_cap) + " -----------").substring(0, cols));

                receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
            }

            if (mCashier != null) {
                if (mCashier.name.equals("Training")) {
                    if (cols == 40)
                        message = new StringBuffer(("--------------- " + context.getString(R.string.txt_training_cap) + " ---------------").substring(0, cols));

                    else
                        message = new StringBuffer(("---------- " + context.getString(R.string.txt_training_cap) + " ----------").substring(0, cols));

                    receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n').append('\n');
                }
            }

            receiptString.append(paymentStringBuffer);

            //receiptString.append(String.valueOf(signImage));

            if (StoreSetting.getReceipt_footer() != null && !(StoreSetting.getReceipt_footer().equals(""))){
                receiptString.append('\n');
                receiptString.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_footer(), cols+1)).append('\n');
                receiptString.append('\n');
            }

            receiptString.append('\n').append('\n').append('\n').append('\n');
        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        return receiptString.toString();
    }

    public void printCart(final Context context){
        final MessageHandler handler = new MessageHandler(context);
        final Message m = new Message();
        final ProgressDialog pd = ProgressDialog.show(context, "", getString(R.string.txt_printing_receipt), true, false);
        Thread receiptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String t : ReceiptSetting.printers) {
                        JSONObject object = new JSONObject(t);

                        ReceiptSetting.enabled = true;
                        ReceiptSetting.address = object.getString("address");
                        ReceiptSetting.make = object.getInt("printer");
                        ReceiptSetting.size = object.getInt("size");
                        ReceiptSetting.type = object.getInt("type");
                        ReceiptSetting.drawer = object.getBoolean("cashDrawer");
                        //ReceiptSetting.receiptPrintOption = object.getInt("receiptprintoption");
                        ReceiptSetting.receiptPrintOption = PrefUtils.getPrintOption(context);
                        if (object.has("main"))
                            ReceiptSetting.mainPrinter = object.getBoolean("main");
                        else
                            ReceiptSetting.mainPrinter = true;

                        int cols = 40;

                        if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
                            cols = 30;

                        if (EscPosDriver.print(context, getCartReceipt(context, cols).toString(), false))
                            m.what = ReceiptDialogFragment.PRINT_SUCCESS;
                        else
                            m.what = ReceiptDialogFragment.PRINT_FAILED;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Print Cart", e.getMessage());
                    m.what = ReceiptDialogFragment.PRINT_FAILED;
                } finally {
                    handler.sendMessage(m);
                    pd.dismiss();
                }
            }
        });
        receiptThread.start();
        try {
            receiptThread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }
    }
}
