package com.elotouch.paypoint.register;

import android.content.Context;
import android.widget.Toast;

import com.elotouch.paypoint.register.printer.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 5/18/2016.
 */
public class Print {

    private Context mContext;

    public Print(Context context){
        this.mContext = context;
    }

    private boolean print(ArrayList<Byte> byteList,OutputStream mOutputStream)
    {
        byte[] commandToSendToPrinter = List2Array(byteList);

        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {e.printStackTrace(); }

        try {
            if (mOutputStream != null) {
                mOutputStream.write(commandToSendToPrinter, 0, commandToSendToPrinter.length);
                mOutputStream.flush();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        commandToSendToPrinter = null;
        return false;
    }

    private static byte[] List2Array(List<Byte> ByteArray)
    {
        byte[] byteArray = new byte[ByteArray.size()];
        for(int index = 0; index < byteArray.length; index++)
        {
            byteArray[index] = ByteArray.get(index);
        }
        ByteArray.clear();
        ByteArray = null;
        return byteArray;
    }

    private boolean isPaperAvailable(OutputStream oStream,InputStream iStream) throws IOException {
        ArrayList<Byte> byteArray = new ArrayList<Byte>();
        byteArray.add(Byte.valueOf((byte) 0x10));
        byteArray.add(Byte.valueOf((byte) 0x04));
        byteArray.add(Byte.valueOf((byte) 0x04));
        print(byteArray, oStream);
        String result = Integer.toHexString(iStream.read());
        return result.contains("12");
    }

    private ArrayList<Byte> toByteArray(String string)
    {
        ArrayList<Byte> list = new ArrayList<Byte>();
        for (byte byt : string.getBytes())
        {
            list.add(Byte.valueOf(byt));
        }
        // line feed
        list.add(Byte.valueOf((byte) 0x0A));
        list.add(Byte.valueOf((byte) 0x0D));
        return list;
    }

    public synchronized boolean printReceipt(final String receipt){
        boolean result = false;
            try{
                SerialPort port = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
                OutputStream stream = port.getOutputStream();
                InputStream iStream = port.getInputStream();
                if (!isPaperAvailable(stream, iStream)) {
                    Toast.makeText(mContext, "Printer out of paper!", Toast.LENGTH_LONG).show();
                    return false;
                }else{
                    ArrayList<Byte> dataArray = new ArrayList<Byte>();
                    dataArray.addAll(toByteArray(receipt)); //Prints the Text

                    /*String barData = "\n***BARCODE***\n";
                    dataArray.addAll(toByteArray(barData));
                    long number = (long) Math.floor(Math.random() * 900000000L) + 100000000L;
                    dataArray.addAll(getBarCode(String.valueOf(number))); //Prints Barcode for corresponding random number.
                    dataArray.addAll(toByteArray(String.valueOf(number)+"\n\n\n")); //Prints the random number.*/

                    result = print(dataArray,stream);
                }
                stream.close();
                iStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        return result;
    }

    private ArrayList<Byte> AlignCenter()
    {
        ArrayList<Byte> list = new ArrayList<Byte>();
        list.add(Byte.valueOf((byte) 27));
        list.add(Byte.valueOf((byte) 97));
        list.add(Byte.valueOf((byte) 1));
        return list;
    }

    private ArrayList<Byte> getBarCode(String Code)
    {

        String barcode = "A"+Code+"B";
        byte[] codeData =  barcode.getBytes();
        ArrayList<Byte> command = new ArrayList<Byte>();

        //Barcode Width
        command.add(Byte.valueOf((byte) 0x1D));
        command.add(Byte.valueOf((byte) 0x77));
        command.add(Byte.valueOf((byte) 0x03));

        //Height
        command.add(Byte.valueOf((byte) 0x1D));
        command.add(Byte.valueOf((byte) 0x68));
        command.add(Byte.valueOf((byte) 100));

        //Position
        command.add(Byte.valueOf((byte) 0x1D));
        command.add(Byte.valueOf((byte) 0x48));
        command.add(Byte.valueOf((byte) 0x00));

        //Barcode Type CODABAR
        command.add(Byte.valueOf((byte) 0x1D));
        command.add(Byte.valueOf((byte) 0x6B));
        command.add(Byte.valueOf((byte) 0x06));
        for (byte byt : codeData) {
            command.add(Byte.valueOf(byt));
        }
        command.add(Byte.valueOf((byte) 0x00));
        return command;

    }
}
