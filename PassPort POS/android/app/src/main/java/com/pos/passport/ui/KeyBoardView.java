package com.pos.passport.ui;

import android.inputmethodservice.InputMethodService;

public class KeyBoardView extends InputMethodService {

	@Override
	public boolean onEvaluateInputViewShown() {
	     return true;
	}
}
