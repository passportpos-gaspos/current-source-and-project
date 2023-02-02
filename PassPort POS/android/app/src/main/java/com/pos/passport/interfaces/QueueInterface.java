package com.pos.passport.interfaces;

import android.os.Bundle;
import android.support.annotation.StringRes;

import com.pos.passport.model.Customer;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReportCart;

/**
 * Created by karim on 10/12/15.
 */
public interface QueueInterface {
    void onSendingMessage(int todo, @StringRes int resId);
    void onSaleDone();
    void onChangeFragment(int which);
    void onChangeFragment(int which, Bundle bundle);
    void onSaveQueue(String firstName);
    void onSavePrintQueue(String firstName);
    void onLoadQueue(ReportCart cart);
    void onAssignCustomer(Customer customer);
    void onRemoveCustomer();
    String onPrintCharge(Payment payment);
    void onEditItem(Product product, int id);
    void onReturn(String returnMsg);
    void onProcessReturn(Payment payment);
    void onOrderStatusGet();
    void onLoadQueueRecent(ReportCart cart);
    void onViewFFFragment(boolean show, OpenorderData data, boolean clear);

}
