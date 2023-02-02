package com.pos.passport.interfaces;

/**
 * Created by karim on 11/23/15.
 */
public interface PayInterface {
    void onPayCompleted(String gatewayId);
    void onPayCompleted(String gatewayId, String imageSign, String paymentType);
    void onPayCompleted(String gatewayId, String imageSign, String paymentType, String tipAmount);
    void onPayFailed();
    void onSignCompleted(String gatewayId, String sigImage);
}
