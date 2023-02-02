package com.pos.passport.util;

import android.content.Context;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.model.Cart;
import com.pos.passport.model.ItemsSold;
import com.pos.passport.model.Payment;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.Summary;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Kareem on 5/29/2016.
 */
public class ReceiptHelper {

    public static boolean print(Context context, Cart cart, int formerchant){
        boolean result;

        for (String t : ReceiptSetting.printers) {
            try {
                JSONObject object = new JSONObject(t);
                Log.e("Receipt Helper", object.toString());
                ReceiptSetting.enabled = true;
                ReceiptSetting.address = object.getString("address");
                ReceiptSetting.make = object.getInt("printer");
                ReceiptSetting.size = object.getInt("size");
                ReceiptSetting.type = object.getInt("type");
                ReceiptSetting.drawer = object.getBoolean("cashDrawer");
                if (object.has("main"))
                    ReceiptSetting.mainPrinter = object.getBoolean("main");
                else
                    ReceiptSetting.mainPrinter = true;

                /*if (ReceiptSetting.mainPrinter) {
                    for(int i = 0; i < cart.mPayments.size(); i++) {
                        if(cart.mPayments.get(i).print && StoreSetting.print_sig) {
                            EscPosDriver.print(context, printCharge(cart.mPayments.get(i)), ReceiptSetting.drawer);
                        }
                    }
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(formerchant == 0)
            result = EscPosDriver.printReceipt(context, cart, Consts.MERCHANT_PRINT_RECEIPT_NO);
        else
            result = EscPosDriver.printReceipt(context, cart, Consts.MERCHANT_PRINT_RECEIPT_YES);
        return result;
    }

    public static String printCharge(Payment payment) {
        int cols = 40;

        if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
            cols = 30;

        StringBuilder receiptString = new StringBuilder();

        //------------Store Name----------------------
        if (!(StoreSetting.getName().equals(""))){
            receiptString.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
        }

        //------------Store Address----------------------
        if (!(StoreSetting.getAddress1().equals(""))){
            receiptString.append(EscPosDriver.wordWrap(StoreSetting.getAddress1(), cols+1)).append('\n');
        }

        //---------------Store Number-----------------
        if (!(StoreSetting.getPhone().equals(""))){
            receiptString.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
        }

        //-----------------Store Website----------------------
        if (!(StoreSetting.getWebsite().equals(""))){
            receiptString.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
        }

        //-----------------------Store Email-----------------------------
        if (!(StoreSetting.getEmail().equals(""))){
            receiptString.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
        }

        //-------------------Date------------------------

        String date = DateFormat.getDateTimeInstance().format(new Date());
        receiptString.append('\n');
        receiptString.append(EscPosDriver.wordWrap(date, cols+1)).append('\n');
        receiptString.append(EscPosDriver.wordWrap("Transaction: " + payment.invoiceNo, cols+1)).append('\n');
        receiptString.append('\n');

        receiptString.append(EscPosDriver.wordWrap("Card # (Last 4): " + payment.printCardNumber, cols+1)).append('\n');
        receiptString.append(EscPosDriver.wordWrap("Card Auth:       " + payment.authCode, cols+1)).append('\n').append('\n');

        if (Float.valueOf(payment.chargeamount) > 0) {
            receiptString.append(EscPosDriver.wordWrap("Trans Type:      Sale", cols+1)).append('\n');
        } else {
            receiptString.append(EscPosDriver.wordWrap("Trans Type:      Return", cols+1)).append('\n');
        }

        receiptString.append(EscPosDriver.wordWrap("Trans Amount:    " + payment.chargeamount, cols+1)).append('\n').append('\n');

        receiptString.append(EscPosDriver.wordWrap("I agree to pay the above amount according to the card issuer agreement.", cols+1)).append('\n');
        receiptString.append(EscPosDriver.wordWrap("Sign below:", cols+1)).append('\n').append('\n');

        StringBuffer message;

        if (cols == 40)
            message = new StringBuffer("X________________________________________".substring(0, cols));
        else
            message = new StringBuffer("X_____________________________".substring(0, cols));

        receiptString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        receiptString.append(EscPosDriver.wordWrap(payment.printCardHolder, cols+1)).append('\n').append('\n');

        return receiptString.toString();
    }

    public static String shiftEndReport(Context context, int cols, Summary summary, String header){
        StringBuilder reportString = new StringBuilder();
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        String fromDate = df.format(new Date(summary.fromDate));
        String toDate = df.format(new Date(summary.toDate));
        reportString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_store_label) + " " + StoreSetting.getName(), cols - 1)).append('\n');
        reportString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_address_label) + " " + StoreSetting.getAddress1(), cols-1)).append('\n').append('\n');
        reportString.append(EscPosDriver.wordWrap( header, cols+1)).append('\n').append('\n');
        reportString.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.msg_shift_end_report), fromDate, toDate), cols+1)).append('\n').append('\n');

        reportString.append(getSummary(context, cols, summary));
        return  reportString.toString();
    }

    public static String summaryReport(Context context, int cols, Summary summary){

        StringBuilder reportString = new StringBuilder();

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        String fromDate = df.format(new Date(summary.fromDate));
        String toDate = df.format(new Date(summary.toDate));

        reportString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_store_label) + " " + StoreSetting.getName(), cols - 1)).append('\n');
        reportString.append(EscPosDriver.wordWrap(context.getString(R.string.txt_address_label) + " " + StoreSetting.getAddress1(), cols-1)).append('\n').append('\n');
        reportString.append(EscPosDriver.wordWrap(String.format(context.getString(R.string.msg_summary_sales_between_dates), fromDate, toDate), cols+1)).append('\n').append('\n');

        reportString.append(getSummary(context,cols,summary));

        return reportString.toString();
    }

    public static String getSummary(Context context, int cols, Summary summary){
        StringBuilder reportString = new StringBuilder();

        StringBuffer message = new StringBuffer((context.getString(R.string.txt_departments) + "                             ").substring(0, cols));
        String substring = context.getString(R.string.txt_amount);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        for (ItemsSold item : summary.departmentsList) {
            message = new StringBuffer("                                        ".substring(0, cols));
            message.replace(0, (item.getName().length() + 1), item.getName() + ":");
            substring = DecimalFormat.getCurrencyInstance().format(item.getPrice());
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        }

        reportString.append('\n');

        message = new StringBuffer((context.getString(R.string.txt_discount) + "                                ").substring(0, cols));
        substring = DecimalFormat.getCurrencyInstance().format(summary.discountTotal);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        reportString.append('\n');


        message = new StringBuffer((context.getString(R.string.txt_tax_groups) + "                              ").substring(0, cols));
        substring = context.getString(R.string.txt_amount);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        for (ItemsSold item : summary.taxList) {
            message = new StringBuffer("                                        ".substring(0, cols));
            message.replace(0, item.getName().length() + 1, item.getName() + ":");
            substring = DecimalFormat.getCurrencyInstance().format(item.getPrice());
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        }

        reportString.append('\n');

        message = new StringBuffer((context.getString(R.string.txt_total) + "                                   ").substring(0, cols));
        substring = DecimalFormat.getCurrencyInstance().format(summary.total);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        reportString.append('\n');

        message = new StringBuffer((context.getString(R.string.txt_tendered_types) + "                          ").substring(0, cols));
        substring = context.getString(R.string.txt_amount);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        for (ItemsSold item : summary.tendersList) {
            message = new StringBuffer("                                        ".substring(0, cols));
            message.replace(0, item.getName().length()+1, item.getName() + ":");
            substring = DecimalFormat.getCurrencyInstance().format(item.getPrice());
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        }

        /*reportString.append('\n');

        if(summary.changeTotal.compareTo(BigDecimal.ZERO) > 0){
            message = new StringBuffer((context.getString(R.string.txt_customer_change) +"                         ").substring(0, cols));
            substring = DecimalFormat.getCurrencyInstance().format(summary.changeTotal);
            message.replace(message.length() - substring.length(), cols - 1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        }*/

        reportString.append('\n');

        message = new StringBuffer((context.getString(R.string.txt_cashiers) + "                                ").substring(0, cols));
        substring = context.getString(R.string.txt_amount);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');


        for (ItemsSold item : summary.cashierList) {
            message = new StringBuffer("                                        ".substring(0, cols));
            message.replace(0, item.getName().length() + 1, item.getName() + ":");
            substring = DecimalFormat.getCurrencyInstance().format(item.getPrice());
            message.replace(message.length()-substring.length(), cols-1, substring);
            reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');
        }

        reportString.append('\n');
        message = new StringBuffer((context.getString(R.string.txt_voids_total_label) + "                            ").substring(0, cols));
        substring = DecimalFormat.getCurrencyInstance().format(summary.voidTotal);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');

        reportString.append('\n');
        message = new StringBuffer((context.getString(R.string.txt_return_total_label) + "                            ").substring(0, cols));
        substring = DecimalFormat.getCurrencyInstance().format(summary.voidTotal);
        message.replace(message.length() - substring.length(), cols - 1, substring);
        reportString.append(EscPosDriver.wordWrap(message.toString(), cols+1)).append('\n');


        reportString.append('\n');
        reportString.append('\n');

        return reportString.toString();
    }
}
