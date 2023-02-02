package com.pos.passport.model;

import android.content.Context;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Kareem on 5/31/2016.
 */
public class Summary {

    public BigDecimal total = BigDecimal.ZERO;
    public BigDecimal subTotal = BigDecimal.ZERO;
    public BigDecimal changeTotal = BigDecimal.ZERO;
    public ArrayList<ItemsSold> departmentsList = new ArrayList<>();
    public ArrayList<ItemsSold> taxList = new ArrayList<>();
    public ArrayList<ItemsSold> tendersList = new ArrayList<>();
    public ArrayList<ItemsSold> cashierList = new ArrayList<>();
    public ArrayList<ReportCart> carts = new ArrayList<>();
    public BigDecimal voidTotal = BigDecimal.ZERO;
    public BigDecimal returnTotal = BigDecimal.ZERO;
    public BigDecimal discountTotal = BigDecimal.ZERO;
    public BigDecimal tipAmountTotal = BigDecimal.ZERO;
    public long fromDate;
    public long toDate;

    private Context context;
    private ProductDatabase mDb;

    public Summary(Context context){
        this.context = context;
        this.mDb = ProductDatabase.getInstance(context);
    }

    public void refresh(ArrayList<ReportCart> reportCart){
        carts.clear();
        departmentsList.clear();
        tendersList.clear();
        taxList.clear();
        cashierList.clear();
        total = BigDecimal.ZERO;
        subTotal = BigDecimal.ZERO;
        changeTotal = BigDecimal.ZERO;
        carts = reportCart;
        if(carts.size() > 0) {
            fromDate = carts.get(0).getDate();
            toDate = carts.get(carts.size() - 1).getDate();
        }
        buildReport();
    }

    public void buildReport()
    {
        try {
            for (ReportCart cart : carts) {
                if (!cart.mVoided && !cart.mStatus.equals(Cart.RETURNED)) {
                    total = total.add(cart.mTotal);
                    subTotal = subTotal.add(cart.mSubtotal);
                    long date = cart.mDate;

                    JSONArray taxdata = cart.getTaxdata();
                    for (int t = 0; t < taxdata.length(); t++) {
                        ItemsSold taxItem = new ItemsSold();
                        taxItem.setId(taxdata.getJSONObject(t).getString("name"));
                        taxItem.setName(taxdata.getJSONObject(t).getString("name"));
                        taxItem.setPrice(new BigDecimal(taxdata.getJSONObject(t).getDouble("amount")));
                        addItem(taxList, taxItem);
                    }

//                    if (cart.mTax1.compareTo(BigDecimal.ZERO) > 0) {
//                        ItemsSold taxItem = new ItemsSold();
//                        taxItem.setId(cart.getTax1Name());
//                        taxItem.setName(cart.getTax1Name());
//                        taxItem.setPrice(cart.mTax1);
//                        addItem(taxList, taxItem);
//                    }
//
//                    if (cart.mTax2.compareTo(BigDecimal.ZERO) > 0) {
//                        ItemsSold taxItem = new ItemsSold();
//                        taxItem.setId(cart.getTax2Name());
//                        taxItem.setName(cart.getTax2Name());
//                        taxItem.setPrice(cart.mTax1);
//                        addItem(taxList, taxItem);
//                    }
//
//                    if (cart.mTax3.compareTo(BigDecimal.ZERO) > 0) {
//                        ItemsSold taxItem = new ItemsSold();
//                        taxItem.setId(cart.getTax3Name());
//                        taxItem.setName(cart.getTax3Name());
//                        taxItem.setPrice(cart.mTax1);
//                        addItem(taxList, taxItem);
//                    }


                    for (Payment payment : cart.mPayments) {
                        ItemsSold tenderItem = new ItemsSold();
                        tenderItem.setId(payment.paymentType);
                        tenderItem.setName(payment.paymentType);
                        if(payment.paymentAmount.compareTo(cart.mTotal) >= 0)
                            tenderItem.setPrice(cart.mTotal);
                        else if(payment.paymentAmount.compareTo(cart.mTotal) < 0)
                            tenderItem.setPrice(payment.paymentAmount);
                        addItem(tendersList, tenderItem);
                        changeTotal = changeTotal.add(payment.paymentAmount);
                        tipAmountTotal = tipAmountTotal.add(payment.tipAmount);
                    }

                    if (cart.getCashierId() > 0) {
                        ItemsSold cashierItem = new ItemsSold();
                        cashierItem.setId(String.valueOf(cart.getCashierId()));
                        cashierItem.setName(cart.mCashier.name);
                        cashierItem.setPrice(cart.mTotal);
                        addItem(cashierList, cashierItem);
                    } else {
                        ItemsSold cashierItem = new ItemsSold();
                        cashierItem.setId("0");
                        cashierItem.setName(context.getString(R.string.txt_admin));
                        cashierItem.setPrice(cart.mTotal);
                        addItem(cashierList, cashierItem);
                    }

                    for (Product product : cart.getProducts()) {
                        if (product.discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                            discountTotal = discountTotal.add(product.discountAmount);
                        }
                        ItemsSold deptItem;
                        if (!product.isNote) {
                            Category category = mDb.getDepartmentById(product.cat);
                            if (category != null) {
                                deptItem = new ItemsSold(String.valueOf(category.getId()), category.getName(), product.itemTotal(date));

                            } else {
                                deptItem = new ItemsSold("mis", context.getString(R.string.txt_miscellaneous), product.itemTotal(date));
                            }
                            addItem(departmentsList, deptItem);

                            for (Product modifier : product.modifiers) {
                                category = mDb.getDepartmentById(modifier.cat);
                                if (category != null && modifier.modifierType == Product.MODIFIER_TYPE_ADDON) {
                                    deptItem = new ItemsSold(String.valueOf(category.getId()), category.getName(), modifier.itemTotal(date));
                                    addItem(departmentsList, deptItem);
                                } else if (modifier.itemTotal(date).compareTo(BigDecimal.ZERO) > 0) {
                                    deptItem = new ItemsSold("mis", context.getString(R.string.txt_miscellaneous), modifier.itemTotal(date));
                                    addItem(departmentsList, deptItem);
                                }
                            }
                        } else if (product.isNote && product.itemTotal(date).compareTo(BigDecimal.ZERO) > 0) {
                            deptItem = new ItemsSold("mis", context.getString(R.string.txt_miscellaneous), product.itemTotal(date));
                            addItem(departmentsList, deptItem);
                        }
                    }
                }

                if (cart.mVoided) {
                    voidTotal = voidTotal.add(cart.mTotal);
                }
                if(cart.mStatus.equals(Cart.RETURNED)){
                    returnTotal = returnTotal.add(cart.mTotal);
                }

                discountTotal = discountTotal.add(cart.mDiscountAmount);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addItem(ArrayList<ItemsSold> list, ItemsSold newItem){
        Iterator iterator= list.iterator();
        while(iterator.hasNext()){
            ItemsSold oldItem = (ItemsSold) iterator.next();
            if(newItem.equals(oldItem)){
                oldItem.setPrice(newItem.getPrice().add(oldItem.getPrice()));
                return;
            }
        }

        list.add(newItem);
    }
}
