package com.pos.passport.util;


import java.math.BigDecimal;

public class RecentTransactionTag
{

    public static final int TRANSACTION_DEFAULT = 0;
    public static final int TRANSACTION_RETURN = 1;
    public static final int TRANSACTION_PARTIAL = 2;
    public static  BigDecimal TOTAL_AMOUNT = BigDecimal.ZERO;

}
