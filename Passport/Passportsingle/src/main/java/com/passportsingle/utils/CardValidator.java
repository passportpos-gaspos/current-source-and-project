package com.passportsingle.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.passportsingle.CreditCardType;
import com.passportsingle.R;

public class CardValidator implements TextWatcher{
	
	private EditText mEditText;
	
	public CardValidator(EditText editText){
		
		this.mEditText = editText;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		
		if(mEditText.getText().hashCode() == s.hashCode()  && s.toString().length() > 8 ){
			
			String cardType = CreditCardType.getCardType(s.toString());
			switch (cardType) {
			case "VISA":
				mEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visa, 0);
				break;
			case "MASTERCARD":
				mEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mastercard, 0);
				break;
			
			case "DISCOVER":
				mEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.discover, 0);
				break;
			
			case "AMEX":
				mEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.amex, 0);
				break;

			default:
				mEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cards, 0);
				break;
			}
					
		}
	}
	
	public static boolean cardValidation(EditText mCard, EditText mCvv, EditText mMonth,EditText mYear){
		
		boolean result = true;
		
		if(mCard.getText().toString().length() < 10) {
			mCard.setError("Enter Valid Card Number" );
			result = false;
			
		}
		if(mCvv.getText().toString().length() < 3){
			mCvv.setError("Enter Valid CVV");
			result = false;
		}
		if(mMonth.getText().toString().length() < 2){
			mMonth.setError("Enter Valid Expire Month");
			result = false;
		}else if(mMonth.getText().toString().length() == 2){
			
			if(Integer.valueOf(mMonth.getText().toString()) > 12){
				
				mMonth.setError("Enter Valid Expire Month");
				result = false;
			}
				
		}
		if(mYear.getText().toString().length() < 4){
			mYear.setError("Enter Valid Expire Date");
			result = false;
		}
		
		return result;
		
	}

}
