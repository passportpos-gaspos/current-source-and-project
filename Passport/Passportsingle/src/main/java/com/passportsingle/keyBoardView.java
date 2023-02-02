package com.passportsingle;

import android.inputmethodservice.InputMethodService;

public class keyBoardView extends InputMethodService {

	@Override
	public boolean onEvaluateInputViewShown() {
	     return true;
	}
}
