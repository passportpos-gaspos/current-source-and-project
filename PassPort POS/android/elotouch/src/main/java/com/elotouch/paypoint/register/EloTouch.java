package com.elotouch.paypoint.register;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.elotouch.paypoint.register.barcodereader.BarcodeReader;
import com.elotouch.paypoint.register.cd.CashDrawer;
import com.elotouch.paypoint.register.cfd.CFD;

/**
 * Created by Kareem on 5/11/2016.
 */
public class EloTouch {

    private BarcodeReader barcodeReader ;
    private CashDrawer cashDrawer;
    private CFD cfd ;
    //private Timer mCFDShiftTimer;
   // private ShiftTimerTask mCFDShiftTask;
    private Context mContext;
    private Print mPrint;

    static {
        try {
            System.loadLibrary("cashdrawerjni");
            System.loadLibrary("cfdjni");
            System.loadLibrary("barcodereaderjni");
            System.loadLibrary("serial_port");
        }catch (Exception e){
            Log.e("error", "loading native libraries" + e.getMessage());
        }
    }

    public EloTouch(Context context){
        this.mContext = context;
        cfd =  new CFD();
        cashDrawer = new CashDrawer();
        barcodeReader = new BarcodeReader();
        //mCFDShiftTimer = new Timer();
        //mCFDShiftTask = new ShiftTimerTask();
        //mCFDShiftTimer.schedule(mCFDShiftTask, 0, 1000);
        mPrint = new Print(context);
    }

    public void turnOnBarcodeLaser(){
        barcodeReader.turnOnLaser();
    }

    public boolean isEloDrawerOpen(){
        return cashDrawer.isDrawerOpen();
    }

    public void setBacklight(boolean isOn){
        cfd.setBacklight(isOn);
    }

    public synchronized void clearEloDisplay(){
        cfd.clearDisplay();
    }

    public synchronized void setEloDisplayLine1(String line1){
        cfd.setLine1(line1);
    }

    public synchronized void setEloDisplayLine2(String line2){
        cfd.setLine2(line2);
    }

    /*private class ShiftTimerTask extends TimerTask {

        @Override
        public void run() {
            cfd.shiftDisplay(2);
        }
    }*/

    /*public void stopShiftTimer(){
        if(mCFDShiftTimer != null){
            mCFDShiftTask.cancel();
        }
    }*/

    public boolean print(String receipt){
        return mPrint.printReceipt(receipt);
    }

    public void openDrawer(){
        if(cashDrawer.isDrawerOpen()){
            Toast.makeText(mContext, "The Cash Drawer is already open !", Toast.LENGTH_SHORT).show();
        }else{
            cashDrawer.openCashDrawer();
        }
    }
}
