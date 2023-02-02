package com.passportsingle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.database.Cursor;
import android.util.Log;

public class PrintDataHandler extends DefaultHandler{

	// private boolean _inSection, _inArea;

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		Log.v("localName", localName);

		if (localName.equals("Item")) {

			Log.v("Item", atts.getValue("name"));

			int id = Integer.valueOf(atts.getValue("itemId"));
			Cursor c = ProductDatabase.getProdByName("" + id);

			if (c != null) {
				Product product = new Product();

				product.price = Long.valueOf(c.getString(c
						.getColumnIndex("price")));
				product.salePrice = c
						.getLong(c.getColumnIndex("salePrice"));
				product.endSale = c
						.getLong(c.getColumnIndex("saleEndDate"));
				product.startSale = c.getLong(c
						.getColumnIndex("saleStartDate"));
				product.cost = Long.valueOf(c.getString(c
						.getColumnIndex("cost")));
				product.id = c.getInt(c.getColumnIndex("_id"));
				product.barcode = (c.getString(c.getColumnIndex("barcode")));
				product.name = (c.getString(c.getColumnIndex("name")));
				product.desc = (c.getString(c.getColumnIndex("desc")));
				product.onHand = (c.getInt(c.getColumnIndex("quantity")));
				product.cat = (c.getInt(c.getColumnIndex("catid")));
				product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
				product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
				product.lastReceived = (c.getInt(c
						.getColumnIndex("lastReceived")));
				product.lowAmount = (c
						.getInt(c.getColumnIndex("lowAmount")));
				product.discount = Float.valueOf(atts.getValue("discount"));
				product.quantity = Integer.valueOf(atts
						.getValue("quantity"));
				PointOfSale.getCart().AddProduct(product);
				c.close();
			} else {
				Product product = new Product();

				product.name = atts.getValue("name");
				product.desc = atts.getValue("desc");
				if (atts.getValue("isNote") != null)
					product.isNote = Boolean.valueOf(atts
							.getValue("isNote"));
				product.id = Integer.valueOf(atts.getValue("itemId"));
				product.cat = Integer.valueOf(atts.getValue("department"));
				product.quantity = Integer.valueOf(atts
						.getValue("quantity"));
				product.price = Long.valueOf(atts.getValue("price"));
				product.cost = Long.valueOf(atts.getValue("cost"));
				product.discount = Float.valueOf(atts.getValue("discount"));
				product.barcode = atts.getValue("barcode");

				if (atts.getValue("subdiscount") != null)
					product.subdiscount = Float.valueOf(atts
							.getValue("subdiscount"));

				if (atts.getValue("salePrice") != null)
					product.salePrice = Long.valueOf(atts
							.getValue("salePrice"));
				if (atts.getValue("startSale") != null)
					product.startSale = Long.valueOf(atts
							.getValue("startSale"));
				if (atts.getValue("endSale") != null)
					product.endSale = Long
							.valueOf(atts.getValue("endSale"));

				PointOfSale.getCart().AddProduct(product);
			}
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName,
			String qName) throws SAXException {}
}
