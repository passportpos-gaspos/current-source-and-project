package com.passportsingle;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

public class DatePickerDialogFragment extends DialogFragment {
	private Fragment mFragment;
	
	private int sYear;

	private int sMonth;

	private int sDay;

	public DatePickerDialogFragment(Fragment callback) {
		mFragment = callback;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final Calendar c = Calendar.getInstance();
		sYear = c.get(Calendar.YEAR);
		sMonth = c.get(Calendar.MONTH);
		sDay = c.get(Calendar.DAY_OF_MONTH);

		return new DatePickerDialog(getActivity(), (OnDateSetListener) mFragment, sYear, sMonth, sDay);
	}
}
