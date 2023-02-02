package com.pos.passport.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pos.passport.fragment.AccountFragment;
import com.pos.passport.fragment.InventoryFragment;
import com.pos.passport.fragment.QuickButtonFragment;
import com.pos.passport.fragment.ReportsFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ReportsFragment tab1 = new ReportsFragment();
                return tab1;
            case 1:
                InventoryFragment tab2 = new InventoryFragment();
                return tab2;
            case 2:
                QuickButtonFragment tab3 = new QuickButtonFragment();
                return tab3;
            case 3:
                AccountFragment tab4 = new AccountFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
