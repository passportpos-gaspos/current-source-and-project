package com.elotouch.paypoint.register.cd;

import android.util.Log;

public class CashDrawer {
    public native String getJNIDi();
    public native void setJNICashDrawerOpen();

    private int m_count=0;
    private final int MAX_DI = 1;
    private final int DO_CASHDRAWER=0;

    public boolean isDrawerOpen()
    {
        String di_list = getJNIDi();

        //Log.v("DI", "[KC] DI= " + di_list+ " " + Integer.toString(m_count));
        int value = Integer.valueOf( di_list.substring(0, 1));

        if (value!=0)
            return true;
        else
            return false;
    }

    public void openCashDrawer()
    {
        setJNICashDrawerOpen();
    }
}