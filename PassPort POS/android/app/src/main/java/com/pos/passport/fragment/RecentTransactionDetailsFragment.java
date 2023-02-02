package com.pos.passport.fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.EmailSetting;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReportCart;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecentTransactionDetailsFragment extends Fragment implements Runnable {
	private TableLayout tl;
	private int mActivatedPosition = 0;
	private ArrayList<ReportCart> carts;
	private ReportCart resendEmailCart;
	protected int todo;
	protected ProgressDialog pd;
    private ProductDatabase mDb;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Fragment", "Recent Transaction Details Fragment");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildReport();
	}
	
	@Override
	public void onResume() {
		Log.v("Print Transaction", "Resumed");
		super.onResume();
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);    
    	outState.putInt("curChoice", mActivatedPosition);  
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.reportsview, container, false);
		Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        mDb = ProductDatabase.getInstance(getActivity());
		tl = (TableLayout) view.findViewById(R.id.report);
		return view;
	}
	
	protected void buildReport()
	{
		carts = mDb.getRecentTransactions();

		tl.removeAllViews();
		if (carts.size() > 0) {

			for (int i = 0; i < carts.size(); i++) {
				
				TableRow row = new TableRow(getActivity());
				tl.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				row.setPadding(10, 5, 10, 5);
				
				final TextView header = new TextView(getActivity());
				TextView tv2 = new TextView(getActivity());
				TextView tv3 = new TextView(getActivity());

				long date = carts.get(i).getDate();
				
				String dateString = DateFormat.getDateTimeInstance().format(new Date(date));
				
				if(carts.get(i).isReceive && carts.get(i).mVoided)
					header.setText(String.format(getString(R.string.msg_timestamp_receiving_voided), dateString));
				else if(carts.get(i).mVoided)
					header.setText(dateString + " - " + getString(R.string.txt_voided_cap));
				else if(carts.get(i).isReceive)
					header.setText(dateString + " - " + getString(R.string.txt_receiving_cap));
				else
					header.setText(dateString);

				header.setSingleLine(true);

				header.setGravity(Gravity.START);
				header.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				header.setTypeface(null, Typeface.BOLD);
				header.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.7f));
				row.addView(header);

				tv2.setText(carts.get(i).getCustomerName());
				tv2.setSingleLine(true);

				tv2.setGravity(Gravity.END);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setTypeface(null, Typeface.BOLD);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
				
				
				tv3.setText(String.format(getString(R.string.txt_trans_no), carts.get(i).trans));
				tv3.setGravity(Gravity.START);
				tv3.setSingleLine(true);

				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv3.setTypeface(null, Typeface.BOLD);
				tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv3);
				TextView tv1;
				if (carts.get(i).mCashier != null) {
					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					tl.addView(row);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText(getString(R.string.txt_cashier_label) + " " + carts.get(i).mCashier.name);
					tv1.setSingleLine(true);
					tv1.setGravity(Gravity.START);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .5f));
					row.addView(tv1);
										
					tv2.setText("");
					tv2.setGravity(Gravity.END);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					tv3.setText("");
					tv3.setGravity(Gravity.END);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));

					row.addView(tv3);	
				}

				BigDecimal nonDiscountTotal = BigDecimal.ZERO;
				for (int o = 0; o < carts.get(i).getProducts().size(); o++) {
					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(10, 0, 10, 0);
					tl.addView(row);
					
					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());
					
					tv1.setText(" " + carts.get(i).getProducts().get(o).name);
					tv1.setSingleLine(true);
					tv1.setGravity(Gravity.START);
					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .7f));
					row.addView(tv1);

					Product prod = carts.get(i).getProducts().get(o);
					nonDiscountTotal = nonDiscountTotal.add(prod.itemNonDiscountTotal(carts.get(i).mDate));

					if (!carts.get(i).getProducts().get(o).isNote)
						tv2.setText(" " + prod.quantity + " @ " + prod.displayPrice(carts.get(i).mDate));
					else
						tv2.setText("");

					tv2.setGravity(Gravity.END);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					if (!carts.get(i).getProducts().get(o).isNote)
						tv3.setText(prod.displayTotal(carts.get(i).mDate));
					else
						tv3.setText("");

					tv3.setGravity(Gravity.END);
					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);
					
					if (!carts.get(i).getProducts().get(o).barcode.equals("")) {
						tv2.setText("");
						tv3.setText("");

						row = new TableRow(getActivity());
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);
						tl.addView(row);
						
						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());
						
						tv1.setText(" " + carts.get(i).getProducts().get(o).barcode);
						tv1.setSingleLine(true);
						tv1.setGravity(Gravity.START);
						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, .7f));
						row.addView(tv1);
											
						tv2.setText(" " + prod.quantity + " @ " + prod.displayPrice(carts.get(i).mDate));
						tv2.setGravity(Gravity.END);
						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						tv3.setText(prod.displayTotal(carts.get(i).mDate));
						tv3.setGravity(Gravity.END);
						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}
				}
				
				if (!carts.get(i).isReceive) {
					if (carts.get(i).mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
						row = new TableRow(getActivity());
						tl.addView(row);
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(R.string.txt_discount_label);
						tv1.setGravity(Gravity.START);
						tv1.setSingleLine(true);

						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
						row.addView(tv1);

						tv2.setText("" + carts.get(i).mSubtotalDiscount + "%");
						tv2.setGravity(Gravity.END);
						tv2.setSingleLine(true);

						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(carts.get(i).mSubtotal.subtract(nonDiscountTotal).divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setSingleLine(true);

						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}

					if (carts.get(i).getTax1Name() != null) {
						row = new TableRow(getActivity());
						tl.addView(row);
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax1Name());
						tv1.setGravity(Gravity.START);
						tv1.setSingleLine(true);

						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
						row.addView(tv1);

						tv2.setText("" + carts.get(i).getTax1Percent() + "%");
						tv2.setGravity(Gravity.END);
						tv2.setSingleLine(true);

						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						BigDecimal price = carts.get(i).mTax1.divide(Consts.HUNDRED);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setSingleLine(true);

						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}

					if (carts.get(i).getTax2Name() != null) {
						row = new TableRow(getActivity());
						tl.addView(row);
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax2Name());
						tv1.setGravity(Gravity.START);
						tv1.setSingleLine(true);

						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
						row.addView(tv1);

						tv2.setText("" + carts.get(i).getTax2Percent() +"%");
						tv2.setGravity(Gravity.END);
						tv2.setSingleLine(true);

						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						BigDecimal price = carts.get(i).mTax2.divide(Consts.HUNDRED);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setSingleLine(true);

						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}

					if (carts.get(i).getTax3Name() != null) {
						row = new TableRow(getActivity());
						tl.addView(row);
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(getString(R.string.txt_tax_label) + " " + carts.get(i).getTax3Name());
						tv1.setGravity(Gravity.START);
						tv1.setSingleLine(true);

						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
						row.addView(tv1);

						tv2.setText("" + carts.get(i).getTax3Percent() + "%");
						tv2.setGravity(Gravity.END);
						tv2.setSingleLine(true);

						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv2);

						BigDecimal price = carts.get(i).mTax3.divide(Consts.HUNDRED);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setSingleLine(true);

						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
						row.addView(tv3);
					}

					row = new TableRow(getActivity());
					row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
					row.setPadding(10, 0, 10, 5);
					tl.addView(row);

					tv1 = new TextView(getActivity());
					tv2 = new TextView(getActivity());
					tv3 = new TextView(getActivity());

					tv1.setText(R.string.txt_total);

					tv1.setGravity(Gravity.START);
					tv1.setSingleLine(true);

					tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv1.setTypeface(null, Typeface.BOLD);
					tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.7f));
					row.addView(tv1);

					tv2.setText("");
					tv2.setSingleLine(true);

					tv2.setGravity(Gravity.END);
					tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv2);

					BigDecimal price = carts.get(i).mTotal.divide(Consts.HUNDRED);
					tv3.setText(DecimalFormat.getCurrencyInstance().format(price.divide(Consts.HUNDRED)));
					tv3.setGravity(Gravity.END);
					tv3.setSingleLine(true);

					tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
					tv3.setTypeface(null, Typeface.BOLD);
					tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
					row.addView(tv3);

					BigDecimal paymentSum = BigDecimal.ZERO;
					ArrayList<Payment> payments = carts.get(i).mPayments;
					for (int p = 0; p < carts.get(i).mPayments.size(); p++) {
						row = new TableRow(getActivity());
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 0);
						tl.addView(row);

						paymentSum = paymentSum.add(payments.get(p).paymentAmount);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(R.string.txt_tendered_type);

						tv1.setGravity(Gravity.START);
						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
						row.addView(tv1);

						tv2.setText(payments.get(p).paymentType);
						tv2.setGravity(Gravity.END);
						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
						row.addView(tv2);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(payments.get(p).paymentAmount.divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
						row.addView(tv3);
					}

					if (paymentSum.compareTo(carts.get(i).mTotal) > 0) {
						row = new TableRow(getActivity());
						row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
						row.setPadding(10, 0, 10, 5);
						tl.addView(row);

						tv1 = new TextView(getActivity());
						tv2 = new TextView(getActivity());
						tv3 = new TextView(getActivity());

						tv1.setText(R.string.txt_customer_change);

						tv1.setGravity(Gravity.START);
						tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv1.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.6f));
						row.addView(tv1);

						tv2.setText("");
						tv2.setGravity(Gravity.END);
						tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
						row.addView(tv2);

						tv3.setText(DecimalFormat.getCurrencyInstance().format(paymentSum.subtract(carts.get(i).mTotal).divide(Consts.HUNDRED)));
						tv3.setGravity(Gravity.END);
						tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
						tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
						row.addView(tv3);
					}
				}
				row = new TableRow(getActivity());
				row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
				row.setPadding(10, 0, 10, 5);
				tl.addView(row);

				tv2 = new TextView(getActivity());
				tv3 = new TextView(getActivity());
					
				final ReportCart reprintCart = carts.get(i);	
					
				if (EmailSetting.isEnabled() && !reprintCart.getCustomerEmail().equals("")){
					String htmlString="<u>Resend Email</u>";
					tv2.setTextColor(Color.BLUE);
					tv2.setText(Html.fromHtml(htmlString));
					tv2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

						}
					});
				} else {
					tv2.setText("");
				}	
				
				tv2.setGravity(Gravity.END);
				tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv2);
				
				String htmlString="<u>Reprint</u>";
				tv3.setTextColor(Color.BLUE);
				tv3.setText(Html.fromHtml(htmlString));
				tv3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						todo = 2;
						resendEmailCart = reprintCart;
						pd = ProgressDialog.show(getActivity(), "", getString(R.string.txt_printing_receipt), true, false);
						Thread thread = new Thread(RecentTransactionDetailsFragment.this);
						thread.start();
					}
				});

				tv3.setGravity(Gravity.END);
				tv3.setTextAppearance(getActivity(), R.style.textLayoutAppearance);
				tv3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.15f));
				row.addView(tv3);

				addLine(tl);
			}
		}
	}
	
	private void addLine(TableLayout tl) {
		TableRow line = new TableRow(getActivity());

		tl.addView(line);
		LayoutParams layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		line.setLayoutParams(layoutParams);
		line.setPadding(5, 0, 5, 1);
		line.setBackgroundColor(Color.BLACK);
	}

	@Override
	public void run() {
		if (print(resendEmailCart.getReceipt(getActivity()))) {
			MainActivity.resentReceiptPrintFlag = false;
			Message m = new Message();
			m.what = 11;
			handler.sendMessage(m);
		} else {
			Message m = new Message();
			m.what = 12;
			handler.sendMessage(m);
		}
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
			} else if (msg.what == 9) {
				Toast.makeText(getActivity(), R.string.msg_receipt_sent_successfully, Toast.LENGTH_LONG).show();
			} else if (msg.what == 8) {
				Toast.makeText(getActivity(), R.string.msg_receipt_not_sent, Toast.LENGTH_LONG).show();
			} else if (msg.what == 11) {
				pd.dismiss();
			} else if (msg.what == 12) {
				Toast.makeText(getActivity(), R.string.msg_receipt_not_sent_check_printer_settings, Toast.LENGTH_LONG).show();
				pd.dismiss();
			}
		}
	};
	
	private boolean print(Bitmap receiptImage) {
		MainActivity.resentReceiptPrintFlag = true;
		EscPosDriver.printReceipt(getActivity(), resendEmailCart);
        return true;
	}

}
