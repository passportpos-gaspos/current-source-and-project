package com.pos.passport.interfaces;

/**
 * Created by karim on 11/23/15.
 */
public interface TransactionListener {
    void onOffline();
    void onApproved(String id, String gatewayId, String response);
    void onError(String message);
    void onDeclined(String message);
}
