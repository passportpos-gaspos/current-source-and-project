package com.passportsingle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CreditCardType {
	
	private static final String MASTERCARD = "^5[1-5][0-9]{5,}$";
	private static final String DISCOVER = "^6(?:011|5[0-9]{2})[0-9]{3,}$";
	private static final String AMEX  = "^3[47][0-9]{5,}$";
	private static final String VISA = "^4[0-9]{6,}$";
	private static final String DINERS_CLUB = "^3(?:0[0-5]|[68][0-9])[0-9]{4,}$";
	private static final String JCB = "^(?:2131|1800|35[0-9]{3})[0-9]{3,}$";
	
	public static Map<String, String> cardTypes = new HashMap<String, String>();
	
	static {
		cardTypes.put("VISA", VISA);
		cardTypes.put("MASTERCARD", MASTERCARD);
		cardTypes.put("DISCOVER", DISCOVER);
		cardTypes.put("AMEX", AMEX);
		cardTypes.put("DINERS CLUB", DINERS_CLUB);
		cardTypes.put("JCB", JCB);
	}
	
	public static String getCardType(String cardNumber){
		
		for (Entry<String, String> card : cardTypes.entrySet()) {
			
			if(cardNumber.matches(card.getValue())){
				
				return card.getKey();
			}
		}
		
		return "Credit Card";
		
	}

}
