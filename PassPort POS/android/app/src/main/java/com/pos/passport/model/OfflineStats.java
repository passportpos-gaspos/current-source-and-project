package com.pos.passport.model;

/**
 * Created by karim on 2/8/16.
 */
public class OfflineStats {
    private int numOfTransactions;
    private int total;

    public int getNumOfTransactions() {
        return numOfTransactions;
    }

    public void setNumOfTransactions(int numOfTransactions) {
        this.numOfTransactions = numOfTransactions;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
