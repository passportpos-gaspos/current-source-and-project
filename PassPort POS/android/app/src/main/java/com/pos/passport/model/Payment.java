package com.pos.passport.model;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Payment implements Serializable
{
	public static final int OFFLINE_CREDIT_SALE = 1;
	public static final int ONLINE_CREDIT_SALE = 0;
	public String paymentType ="";
	public BigDecimal paymentAmount;

	public String gatewayId = "";
	public String authCode = "";
	public String invoiceNo = "";
	public String acqRefData = "", recordNo = "";
	public String refNo = "", processData = "";
	public String payAmount = "0";
	public String request = "";
	public String date = "";
	public int processed = 0;
	public int preSaleID;
	public String response = "";
	public String transCode = "";
	public long saleID;
	public String chargeamount;
	public String printCardHolder;
	public String printCardNumber;
	public String printCardExpire;
	public boolean print = false;
	public String signImage = "";
	public String payload = "";
	public String cardType = "";
	public String lastFour = "";
    public BigDecimal tipAmount = BigDecimal.ZERO;

	public void extractXML() {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			DataHandler dataHandler = new DataHandler();
			xr.setContentHandler(dataHandler);
			
			String xml = "";
			if(processed == 0)
			{
				xml = request;
			}
			else 
			{
				xml = response;
			}
			
			ByteArrayInputStream in = new ByteArrayInputStream(
					xml.getBytes());
			xr.parse(new InputSource(in));

			// data = dataHandler.getData();

		} catch (ParserConfigurationException pce) {
			Log.e("SAX XML", "sax parse error", pce);
		} catch (SAXException se) {
			Log.e("SAX XML", "sax error", se);
		} catch (IOException ioe) {
			Log.e("SAX XML", "sax parse io error", ioe);
		}
	}

	public class DataHandler extends DefaultHandler {

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {

			if (localName.equals("Purchase")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("Authorize")) {
				payAmount = tempVal;
			}
			
			if (localName.equals("AuthCode")) {
				authCode = tempVal;
			}

			if (localName.equals("AcqRefData")) {
				acqRefData = tempVal;
			}

			if (localName.equals("RecordNo")) {
				recordNo = tempVal;
			}

			if (localName.equals("RefNo")) {
				refNo = tempVal;
			}

			if (localName.equals("ProcessData")) {
				processData = tempVal;
			}

			if (localName.equals("InvoiceNo")) {
				invoiceNo = tempVal;
			}
			
			if (localName.equals("TranCode")) {
				transCode = tempVal;
			}
		}
		
	}

}

