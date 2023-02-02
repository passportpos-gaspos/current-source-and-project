package com.elotouch.paypoint.register.barcodereader;

public class BarcodeReader {
    public native void setJNIBarCodeReader();

    public void turnOnLaser()
    {
        setJNIBarCodeReader();
    }
}