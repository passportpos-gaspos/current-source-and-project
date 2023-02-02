package com.pos.passport.util;

import java.math.BigDecimal;

/**
 * Created by karim on 9/24/15.
 */
public class Consts {
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final BigDecimal MINUS_ONE = new BigDecimal("-1");
    public static final int MINIMUM_SIZE_FOR_PORTRAIT_IN_PIXEL = 600;
    public static final String MY_PREF = "MyPref";
    public static final String APOS_LOGIN_EMAIL = "APOS_LOGIN_EMAIL";
    public static final String APOS_LOGIN_KEY = "APOS_LOGIN_KEY";
    public static final String APOS_TERMINAL = "APOS_TERMINAL";
    public static final String APOS_Userid = "APOS_Userid";
    public static final String APOS_TerminalId = "APOS_TerminalId";
    public static final String APOS_LASTLOG = "APOS_LASTLOG";
    public static final String APOS_LICENSED = "APOS_LICENSED";
    public static final String RECEIPT_PRINT_OPTION = "RECEIPT_PRINT_OPTION";
    public static final String RECEIPT_EMAIL_OPTION = "RECCEIPT_EMAIL_OPTION";
    public static final String OFFLINE_SHOWING_MESSAGE = "OFFLINE_SHOWING_MESSGE";
    public static final String OFFLINE_TIMESTAMP = "OFFLINE_TIMESTAMP";
    public static final String IS_OFFLINE = "IS_OFFLINE";
    public static final String LAST_CONNECTED_TIME = "LAST_CONNECTED_TIME";
    public static final String LOGIN_CASHIER_INFO = "LOGIN_CASHIER_INFO";
    public static final String RECEIPT_HEADER_TYPE = "RECEIPT_HEADER_TYPE";
    public static final String TRANSACTION_NUMBER = "TRANSACTION_NUMBER";
    public static final String NAVIGATIONPANE_INFO = "NAVIGATIONPANE_INFO";
    public static final String CAPTURESIGN = "CAPTURESIGH";
    public static final String AUTOSYNC_INFO = "AUTOSYNC_INFO";
    public static final String CASH_DRAWER_OPEN_CREDIT="CASHDRAWER_OPEN";
    public static final String ACCEPT_TIPS="ACCEPT_TIPS";
    public static final String ACCEPT_MOBILE_ORDERS="ACCEPT_MOBILE_ORDERS";


    public static final String CONFIG_BASE_URL = "base_url";
    public static final String CONFIG_ACCESS_KEY = "access_key";
    public static final String CONFIG_BASE_INNER = "base_inner";
    public static final String BUNDLE_ID = "bundle_id";

    public static final int REQUEST_REGISTER = 1001;
    public static final int REQUEST_PAY = 1002;
    public static final int REQUEST_LOGIN = 1003;

    public static final int TRANSACTION_START_NUMBER = 111;
    public static final String ELO_MODEL = "PayPoint ESY13P1";

    public static final String CLEARENT_SANDBOX_API_KEY = "fc594b6c6fd4493db932d7ae50f8c56e";
    public static final String CLEARENT_API_KEY = "";

    public static final String NO_KEY = "NoKey";
    public static final String NO_USER = "NoUser";
    public static final String EXPIRED = "Expired";
    public static final String KEY_USED = "KeyUsed";
    public static final String INVALID_USER = "InvalidUser";

    public static final String COMM_TYPE_INGENICO ="Ingenico ICT220";

    public static final int MERCHANT_PRINT_RECEIPT_NO = 0;
    public static final int MERCHANT_PRINT_RECEIPT_YES = 1;

    public static final String OPEN_ORDER_TEXT ="open";
    public static final String ARCHIVE_ORDER_TEXT ="archive";


    public static final String ADDPRINTER_TEXT ="addprint";
    public static final String DELETEPRINTER_TEXT ="deleteprint";
    public static final String GET_PRINTER_TEXT ="getprint";
    public static final String ADDCARDREADER_TEXT ="addcard";
    public static final String GET_CARDREADER_TEXT ="getcard";
    public static final String DELETECARDREADER_TEXT ="deletecard";

}
