package com.pos.passport.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Product;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;

public class PrintDataHandler extends DefaultHandler {
    private ProductDatabase mDb;
    private Cart mCart;

    public PrintDataHandler(Context context, Cart cart) {
        mDb = ProductDatabase.getInstance(context);
        this.mCart = cart;
    }
    // private boolean _inSection, _inArea;
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        Log.v("localName", localName);
        if (localName.equals("Item")) {

            Log.v("Item", atts.getValue("name"));

            int id = Integer.valueOf(atts.getValue("itemId"));
            Cursor c = mDb.getProdById(id);

            if (c != null) {
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
                product.discount = new BigDecimal(Float.valueOf(atts.getValue("discount")));
                product.quantity = Integer.valueOf(atts.getValue("quantity"));
                mCart.addProduct(product);
                c.close();
            } else {
                Product product = new Product();

                product.name = atts.getValue("name");
                product.desc = atts.getValue("desc");
                if (atts.getValue("isNote") != null)
                    product.isNote = Boolean.valueOf(atts.getValue("isNote"));
                product.id = Integer.valueOf(atts.getValue("itemId"));
                product.cat = Integer.valueOf(atts.getValue("department"));
                product.quantity = Integer.valueOf(atts.getValue("quantity"));
                product.price = new BigDecimal(atts.getValue("price"));
                product.cost = new BigDecimal(atts.getValue("cost"));
                product.discount = new BigDecimal(Float.valueOf(atts.getValue("discount")));
                product.barcode = atts.getValue("barcode");

                if (atts.getValue("subdiscount") != null)
                    product.subDiscount = new BigDecimal(Float.valueOf(atts.getValue("subdiscount")));

                if (atts.getValue("salePrice") != null)
                    product.salePrice = new BigDecimal(Long.valueOf(atts.getValue("salePrice")));
                if (atts.getValue("startSale") != null)
                    product.startSale = Long.valueOf(atts.getValue("startSale"));
                if (atts.getValue("endSale") != null)
                    product.endSale = Long.valueOf(atts.getValue("endSale"));

                mCart.addProduct(product);
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    }
}
