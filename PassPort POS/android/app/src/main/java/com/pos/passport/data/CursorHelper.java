package com.pos.passport.data;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.pos.passport.model.Modifier;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.ReportCartCounter;

import java.math.BigDecimal;

/**
 * Created by Kareem on 5/29/2016.
 */
public class CursorHelper {

    public static Modifier parseModifier(Cursor c){

        Modifier m = new Modifier();
        m.id = c.getInt(c.getColumnIndex("_id"));
        m.name = c.getString(c.getColumnIndex("name"));
        m.desc = c.getString(c.getColumnIndex("desc"));
        m.barcode = c.getString(c.getColumnIndex("barcode"));
        m.cat = c.getInt(c.getColumnIndex("catid"));
        m.price = new BigDecimal(c.getString(c.getColumnIndex("price")));
        m.cost = new BigDecimal(c.getString(c.getColumnIndex("cost")));
        m.quantity = c.getInt(c.getColumnIndex("quantity"));

        return m;
    }

    public static ReportCart parseReportCart(ReportCart cartReport, Cursor c)
    {

        cartReport.setCartItems(c.getString(c.getColumnIndex("lineitems")));
        cartReport.extractJson();
        cartReport.mId = c.getLong(c.getColumnIndex("_id"));
        cartReport.setId(c.getString(c.getColumnIndex("_id")));
        cartReport.setDate(c.getLong(c.getColumnIndex("date")));
        //Log.v("Date Range", "" + cartReport.getDate());
        cartReport.setCashierId(c.getInt(c.getColumnIndex("cashierID")));
        cartReport.trans = new BigDecimal(c.getString(c.getColumnIndex("trans")));
        if (cartReport.trans.compareTo(BigDecimal.ZERO) == 0) {
            cartReport.trans = new BigDecimal(cartReport.getId());
        }
        String subTotal = c.getString(c.getColumnIndex("subtotal"));
        String tax1 = c.getString(c.getColumnIndex("tax1"));
        String tax2 = c.getString(c.getColumnIndex("tax2"));
        String tax3 = c.getString(c.getColumnIndex("tax3"));

        String total = c.getString(c.getColumnIndex("total"));

        cartReport.mSubtotal =new BigDecimal(subTotal);
        cartReport.mTax1 = new BigDecimal(tax1);
        cartReport.mTax2 = new BigDecimal(tax2);
        if (!TextUtils.isEmpty(tax3))
            cartReport.mTax3 = new BigDecimal(tax3);

        cartReport.mTotal = new BigDecimal(total);

        cartReport.setTax1Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent1"))));
        cartReport.setTax2Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent2"))));
        cartReport.setTax3Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent3"))));

        cartReport.setTax1Name(c.getString(c.getColumnIndex("taxname1")));
        cartReport.setTax2Name(c.getString(c.getColumnIndex("taxname2")));
        cartReport.setTax3Name(c.getString(c.getColumnIndex("taxname3")));

        cartReport.mVoided = (c.getInt(c.getColumnIndex("voided")) != 0);
        cartReport.isReceive = (c.getInt(c.getColumnIndex("isReceive")) != 0);
        cartReport.mDiscountAmount = new BigDecimal(c.getFloat(c.getColumnIndex("totaldiscountamount")));
        cartReport.mDiscountName = c.getString(c.getColumnIndex("totaldiscountname"));
        cartReport.mStatus = c.getString(c.getColumnIndex("status"));
        cartReport.setCustomerId(c.getInt(c.getColumnIndex("customerID")));
        cartReport.mName = c.getString(c.getColumnIndex("holdName"));
        cartReport.mReturnReason = c.getString(c.getColumnIndex("returnReason"));
        String rtotal = c.getString(c.getColumnIndex("totalReturn"));
        cartReport.totalReturn = new BigDecimal(rtotal);
        if(c.getInt(c.getColumnIndex("onHold")) > 0 )
            cartReport.mOnHold = true;
        cartReport.returnStatus=c.getInt(c.getColumnIndex("returnStatus"));
        return cartReport;
    }
    public static ReportCartCounter parseReportCartCounter(ReportCartCounter cartReport, Cursor c)
    {

        cartReport.setCartItems(c.getString(c.getColumnIndex("lineitems")));
        cartReport.extractJson();

        cartReport.setId(c.getString(c.getColumnIndex("_id")));
        cartReport.setDate(c.getLong(c.getColumnIndex("date")));
        Log.v("Date Range", "" + cartReport.getDate());
        cartReport.setCashierId(c.getInt(c.getColumnIndex("cashierID")));
        cartReport.trans = new BigDecimal(c.getString(c.getColumnIndex("trans")));
        if (cartReport.trans.compareTo(BigDecimal.ZERO) == 0) {
            cartReport.trans = new BigDecimal(cartReport.getId());
        }
        String subTotal = c.getString(c.getColumnIndex("subtotal"));
        String tax1 = c.getString(c.getColumnIndex("tax1"));
        String tax2 = c.getString(c.getColumnIndex("tax2"));
        String tax3 = c.getString(c.getColumnIndex("tax3"));

        String total = c.getString(c.getColumnIndex("total"));

        cartReport.mSubtotal =new BigDecimal(subTotal);
        cartReport.mTax1 = new BigDecimal(tax1);
        cartReport.mTax2 = new BigDecimal(tax2);
        if (!TextUtils.isEmpty(tax3))
            cartReport.mTax3 = new BigDecimal(tax3);

        cartReport.mTotal = new BigDecimal(total);

        cartReport.setTax1Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent1"))));
        cartReport.setTax2Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent2"))));
        cartReport.setTax3Percent(new BigDecimal(c.getFloat(c.getColumnIndex("taxpercent3"))));

        cartReport.setTax1Name(c.getString(c.getColumnIndex("taxname1")));
        cartReport.setTax2Name(c.getString(c.getColumnIndex("taxname2")));
        cartReport.setTax3Name(c.getString(c.getColumnIndex("taxname3")));

        cartReport.mVoided = (c.getInt(c.getColumnIndex("voided")) != 0);
        cartReport.isReceive = (c.getInt(c.getColumnIndex("isReceive")) != 0);
        cartReport.mDiscountAmount = new BigDecimal(c.getFloat(c.getColumnIndex("totaldiscountamount")));
        cartReport.mDiscountName = c.getString(c.getColumnIndex("totaldiscountname"));
        cartReport.mStatus = c.getString(c.getColumnIndex("status"));
        cartReport.setCustomerId(c.getInt(c.getColumnIndex("customerID")));
        cartReport.mName = c.getString(c.getColumnIndex("holdName"));
        if(c.getInt(c.getColumnIndex("onHold")) > 0 )
            cartReport.mOnHold = true;
        cartReport.returnStatus=c.getInt(c.getColumnIndex("returnStatus"));
        String rtotal = c.getString(c.getColumnIndex("totalReturn"));
        cartReport.totalReturn = new BigDecimal(rtotal);
        return cartReport;
    }
}
