package com.passportsingle.utils;

import java.io.OutputStream;
import java.net.Socket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.passportsingle.PointOfSale;
import com.passportsingle.R;

public class Utils {

	private static Socket clientSocket;
	private static OutputStream out;
	

    private Utils() {
    }
    
    public static String getReceiptType(int receiptOption){
    	String receiptType = "";
    	switch (receiptOption) {
		case 1:
			receiptType = "Print Receipt for Each Sale";
			break;
		case 2:
			receiptType = "Print receipt if total amount > $10";
			break;
		case 3:
			receiptType = "Print receipt if total amount > $20";
			break;
		case 4:
			receiptType = "Print receipt if total amount > $30";
			break;
		
    	}
    	
    	return receiptType;
    }
    
    public static String getApplicationName(Context context){
 	   
 	   PackageInfo pInfo;
 		try {
 			pInfo = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, 0);
 			return ( context.getResources().getString(R.string.app_name)+ " V" +pInfo.versionName);
 		} catch (NameNotFoundException e) {
 			
 			Log.e("Error", "Package Name could not found:" + e.getMessage());
 		} catch (Exception e){
 			
 			Log.e("Error", e.getMessage());
 		}
 	   
 	   
 	   return context.getResources().getString(R.string.app_name);
 	   
    }
    
	public static boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) PointOfSale.me.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}
	
	public static void alertbox(Context context, String title, String mymessage){
		
		new AlertDialog.Builder(context)
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	
	}

}
