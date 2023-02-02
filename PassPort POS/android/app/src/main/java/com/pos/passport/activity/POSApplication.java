package com.pos.passport.activity;

import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kareem on 11/15/2016.
 */

public class POSApplication extends MultiDexApplication {

    private AppCompatActivity mCurrentActivity = null;

    public void onCreate() {
        super.onCreate();
    }

    public AppCompatActivity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(AppCompatActivity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
