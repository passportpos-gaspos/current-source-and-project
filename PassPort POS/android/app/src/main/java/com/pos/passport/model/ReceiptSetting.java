 package com.pos.passport.model;

 import android.support.annotation.IntDef;

 import com.pos.passport.util.EscPosDriver;

 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.util.ArrayList;

 public class ReceiptSetting {
     @IntDef( {RECEIPT_EVERY_TIME, RECEIPT_WHEN_D10, RECEIPT_WHEN_D20, RECEIPT_WHEN_D30 })
     @Retention(RetentionPolicy.SOURCE)
     public @interface PrintOption {}

     public static final int MAKE_STAR = 1;
     public static final int MAKE_CUSTOM = 2;
     public static final int MAKE_ESCPOS = 3;
     public static final int MAKE_SNBC = 4;
     public static final int MAKE_PT6210 = 5;
     public static final int MAKE_ELOTOUCH = 6;

     public static final int TYPE_LAN = 1;
     public static final int TYPE_USB = 2;
     public static final int TYPE_BT = 3;

     public static final int SIZE_2 = 1;
     public static final int SIZE_3 = 2;

     // receipt options
     public static final int RECEIPT_EVERY_TIME = 1;
     public static final int RECEIPT_WHEN_D10 = 2;
     public static final int RECEIPT_WHEN_D20 = 3;
     public static final int RECEIPT_WHEN_D30 = 4;

     public static boolean enabled;
     public static String blurb = "";
     public static String address = "";
     public static String name = "";
     public static int type = 0;
     public static int make = 0;
     public static int size = 0;
     public static boolean drawer = false;
     public static boolean display = false;
     public static int receiptPrintOption = 0;

     public static boolean denabled;
     public static String dblurb = "";
     public static String daddress = "";
     public static String dname = "";
     public static int dtype = 0;
     public static int dmake = 0;
     public static int dsize = 0;
     public static boolean ddrawer = false;
     public static boolean ddisplay = false;

     public static ArrayList<String> printers = new ArrayList<>();
     public static boolean dmainPrinter;
     public static boolean mainPrinter;
     public static boolean openOrderPrinter;
     public static int dreceiptPrintOption = 0;

     public static void clear() {
         denabled = false;
         dblurb = "";
         daddress = "";
         dname = "";
         dtype = 0;
         dmake = 0;
         dsize = 0;
         ddrawer = false;
         ddisplay = false;
         dreceiptPrintOption = 0;

         enabled = false;
         blurb = "";
         address = "";
         name = "";
         type = 0;
         make = 0;
         size = 0;
         drawer = false;
         display = false;
         receiptPrintOption = 0;

         printers.clear();
     }

     public static String getReceiptHeader(int cols){
         StringBuilder header = new StringBuilder();

         //------------Store Name----------------------
         if(!(StoreSetting.getName().equals(""))){
             header.append(EscPosDriver.wordWrap(StoreSetting.getName(), cols+1)).append('\n');
         }

         //------------Store Address----------------------
         if(!(StoreSetting.getAddress1().equals(""))){
             header.append(EscPosDriver.wordWrap(StoreSetting.getAddress1(), cols + 1)).append('\n');
         }

         if(!(StoreSetting.getCity().equals(""))){
             header.append(EscPosDriver.wordWrap(String.format("%1$s, %2$s",StoreSetting.getCity(), StoreSetting.getState()), cols+1)).append('\n');
         }

         //---------------Store Number-----------------
         if(!(StoreSetting.getPhone().equals(""))){
             header.append(EscPosDriver.wordWrap(StoreSetting.getPhone(), cols+1)).append('\n');
         }

         //-----------------Store Website----------------------
         if(!(StoreSetting.getWebsite().equals(""))){
             header.append(EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols+1)).append('\n');
         }

         //-----------------------Store Email-----------------------------
         if(!(StoreSetting.getEmail().equals(""))){
             header.append(EscPosDriver.wordWrap(StoreSetting.getEmail(), cols+1)).append('\n');
         }

         if(StoreSetting.getReceipt_header() != null && !(StoreSetting.getReceipt_header().equals(""))){
             header.append('\n');
             header.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_header(), cols+1)).append('\n');
         }

         return header.toString();
     }

     public static String getReceiptFooter(int cols){
         StringBuilder footer =  new StringBuilder();
         if (StoreSetting.getReceipt_footer() != null && !(StoreSetting.getReceipt_footer().equals(""))){
             footer.append('\n');
             footer.append(EscPosDriver.wordWrap(StoreSetting.getReceipt_footer(), cols+1)).append('\n');
             footer.append('\n');
         }

         return footer.toString();

     }
 }
