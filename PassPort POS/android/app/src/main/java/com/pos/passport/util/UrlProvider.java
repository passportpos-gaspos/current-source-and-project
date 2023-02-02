package com.pos.passport.util;

/**
 * Created by karim on 9/24/15.
 */
public class UrlProvider {

//    public static String Access_key = "497Lq10pwjJwlk480PYxV62wlm3B7O4p";
//    public static String BASE_URL = "http://demo.webfirminfotech.com/";
;
//    public static String Base_inner="pos_api/v1/api/";
//
//    //public static final String LOGIN_URL = "approutines/login.php";
//    public static final String LOGIN_URL = Base_inner+"register";
//    public static final String Token_URL = Base_inner+"refreshToken";
//
//    public static final String DOWNLOAD_URL = "download/upgrade.php";
//
//    //public static final String SYNC_URL = "approutines/sync.php";
//    public static final String SYNC_URL = "sync";
//    public static final String SYNC_STORE = "store";
//    public static final String OPEN_ORDER = "openOrder";
//
//
//    public static final String CLOUD_SALE_URL = "approutines/cloudsale.php";
//    //public static final String SUBMIT_SALE_URL = "approutines/submitSale.php";
//    public static final String SUBMIT_SALE_URL = Base_inner+"submitSale";
//    public static final String TRANSACTION_NUMBER_URL = "approutines/getTranNumber.php";
//   // public static final String CUSTOMER_ADD_URL = "approutines/addCustomer.php";
//    public static final String CUSTOMER_ADD_URL = "customers";
//    public static final String EMAIL_RECEIPT_SETTINGS = "approutines/checkEmailSettings.php";
//    public static final String CUSTOMER_EMAIL_RECEIPT = "approutines/EmailRoutine.php";
//
//    public static String getBaseUrl() {
//        return BASE_URL;
//    }
//
//    public static void setBaseUrl(String baseUrl) {
//        BASE_URL = baseUrl;
//    }


    //public static String BASE_URL = "http://falcorpos.azurewebsites.net/";
    public static String Access_key = "";
    public static String BASE_URL = "";
    public static String BASE_INNER="";

    //public static final String LOGIN_URL = "approutines/login.php";
    //public static final String LOGIN_URL = BASE_INNER+"register";
    public static final String LOGIN_URL = "register";
    //public static final String Token_URL = BASE_INNER+"refreshToken";
    public static final String Token_URL = "refreshToken";

    public static final String DOWNLOAD_URL = "download/upgrade.php";

    //public static final String SYNC_URL = "approutines/sync.php";
    public static final String SYNC_URL = "sync";
    public static final String SYNC_STORE = "store";
    public static final String OPEN_ORDER = "openOrder";
    public static final String UPDATE_STATUS = "updateStatus";
    public static final String ARCHIVE_ORDER = "archiveOrder";
    public static final String ADD_PRINTER = "printer";
    public static final String ADD_CARD_READER = "cardReader";
    public static final String DELETE_PRINTER = "deletePrinter";
    public static final String DELETE_CARD_READER = "deleteCardReader";

    public static final String GET_PRINTER = "getPrinter";
    public static final String GET_CARD_READER = "getCardReader";

    public static final String CLOUD_SALE_URL = "approutines/cloudsale.php";
    //public static final String SUBMIT_SALE_URL = "approutines/submitSale.php";
    //public static final String SUBMIT_SALE_URL = BASE_INNER+"submitSale";
    public static final String SUBMIT_SALE_URL = "submitSale";
    public static final String TRANSACTION_NUMBER_URL = "approutines/getTranNumber.php";
    // public static final String CUSTOMER_ADD_URL = "approutines/addCustomer.php";
    public static final String CUSTOMER_ADD_URL = "customers";
    public static final String EMAIL_RECEIPT_SETTINGS = "approutines/checkEmailSettings.php";
    public static final String CUSTOMER_EMAIL_RECEIPT = "approutines/EmailRoutine.php";
    public static final String CUSTOMER_EMAIL_SEND = "emailReceipt";
    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }

    public static String getAccess_key() {
        return Access_key;
    }

    public static void setAccess_key(String access_key) {
        Access_key = access_key;
    }

    public static String getBase_inner() {
        return BASE_INNER;
    }

    public static void setBase_inner(String base_inner) {
        BASE_INNER = base_inner;
    }


}
