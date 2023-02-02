package com.pos.passport.ui;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pos.passport.R;

/**
 * Created by Kareem on 4/26/2016.
 */
public class DrawerToggle extends ActionBarDrawerToggle {

    private DrawerLayout mDrawerLayout;
    private Activity mActivity;
    private View mMainLayout;

    public DrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.mDrawerLayout = drawerLayout;
        this.mActivity = activity;
    }

    public DrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.mDrawerLayout = drawerLayout;
        this.mActivity = activity;
        mMainLayout = activity.findViewById(R.id.main_layout);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);
        /*mMainLayout.setTranslationX(-slideOffset * drawerView.getWidth());
        mDrawerLayout.bringChildToFront(drawerView);
        mDrawerLayout.requestLayout();*/
    }
}
