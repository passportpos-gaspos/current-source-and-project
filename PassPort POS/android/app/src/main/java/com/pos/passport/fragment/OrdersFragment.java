package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.ItemOpen;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/30/15.
 */
public class OrdersFragment extends Fragment {
    private TabLayout mSettingsTabLayout;
    private ViewPager mSettingsViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ImageView mBackImgeView;
    public int mcurent_frag = -1;
    public static final int FRAGMENT_COUNTER = 0;
    public static final int FRAGMENT_OPEN = 1;
    public static final int FRAGMENT_ARCHIVE = 2;
    CounterOpenFragment counterOpenFragment;
    public ImageView imgview;
    public EditText editText;
    public ImageView closeimge;
    public QueueInterface mCallback;
    public ProductDatabase mDb;
    private OpenOrdersFragment fragmentOne;
    private CounterOpenFragment fragmentTwo;
    private CounterArchiveFragment fragmentThree;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static OrdersFragment newInstance(int currentFragment) {
        OrdersFragment f = new OrdersFragment();
        Bundle args = new Bundle();
        args.putInt("currentFragment", currentFragment);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordersall, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        mDb = ProductDatabase.getInstance(getActivity());

        //setUpUIs();

        /*mSettingsViewPager.setAdapter(new CustomAdapter(getChildFragmentManager(), getContext()));
        mSettingsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mcurent_frag = mSettingsViewPager.getCurrentItem();
            }

            @Override
            public void onPageSelected(int position) {
                mcurent_frag = mSettingsViewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mSettingsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mSettingsTabLayout));*/
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mcurent_frag = getArguments().getInt("currentFragment");
        Log.e("mcurent_frag", "mcurent_frag onViewCreated >>>>" + mcurent_frag);
        bindUIElements(view);
        setUpUIs();
        setupTabLayout();
        bindWidgetsWithAnEvent();
    }
    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        //OnloadTab();
        Log.e("mcurent_frag","mcurent_frag onActivityCreated >>>>"+mcurent_frag);
        super.onActivityCreated(savedInstanceState);
    }*/

    public void setParameter(int parameter) {
        mcurent_frag = parameter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }

    private void bindUIElements(View view) {

        mSettingsTabLayout = (TabLayout) view.findViewById(R.id.settings_tab_layout);
        mSettingsViewPager = (ViewPager) view.findViewById(R.id.settings_view_pager);
        mSettingsViewPager.setOffscreenPageLimit(0);
        mBackImgeView = (ImageView) view.findViewById(R.id.back_image_view);
        imgview = (ImageView) view.findViewById(R.id.imgview);
        editText = (EditText) view.findViewById(R.id.search);
        closeimge = (ImageView) view.findViewById(R.id.img_close);
    }


    private void setUpUIs() {
        //setupViewPager(mSettingsViewPager);
        //mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);

        imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setVisibility(View.VISIBLE);
                closeimge.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        mBackImgeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onViewFFFragment(false, new OpenorderData(), false);
                getFragmentManager().popBackStack();
            }
        });
        closeimge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                editText.setVisibility(View.GONE);
                closeimge.setVisibility(View.GONE);
                if (mcurent_frag == FRAGMENT_COUNTER) {
                    List<ItemOpen> mCart = new ArrayList<>();
                    mCart = mDb.getOpenOrdersNew();
                    //OpenOrdersFragment newFragment = new OpenOrdersFragment();//(OpenOrdersFragment) mViewPagerAdapter.getItem(0);
                    fragmentOne.setUpData(mCart, getActivity());
                }
                Utils.dismissKeyboard(view);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (mcurent_frag == FRAGMENT_COUNTER) {
                        if (s.toString().length() > 2) {
                            List<ItemOpen> mCart = new ArrayList<>();
                            mCart = mDb.getSearchOpenOrdersNew(s.toString());
                            //fragmentOne = new OpenOrdersFragment();// mViewPagerAdapter.getItem(0);
                            fragmentOne.setUpData(mCart, getActivity());
                        } else if (s.toString().length() == 0) {
                            List<ItemOpen> mCart = new ArrayList<>();
                            mCart = mDb.getOpenOrdersNew();
                            //OpenOrdersFragment newFragment = new OpenOrdersFragment();// mViewPagerAdapter.getItem(0);
                            fragmentOne.setUpData(mCart, getActivity());
                        }
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setupViewPager(final ViewPager viewPager) {
        mViewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        //mViewPagerAdapter.addFragment(new CounterFragment(), R.string.txt_counter);
        mViewPagerAdapter.addFragment(new OpenOrdersFragment(), R.string.txt_counter);
        if (PrefUtils.getAcceptMobileOrdersInfo(getActivity()).equalsIgnoreCase("YES")) {
            mViewPagerAdapter.addFragment(new CounterOpenFragment(), R.string.txt_open);
            mViewPagerAdapter.addFragment(new CounterArchiveFragment(), R.string.txt_archive);
        }

        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mcurent_frag = viewPager.getCurrentItem();
            }

            @Override
            public void onPageSelected(int position) {
                mcurent_frag = viewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(mcurent_frag);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Integer> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            mcurent_frag = position;
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, @StringRes int titleRes) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(titleRes);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mFragmentTitleList.get(position));
        }
    }

    public void notifyChanges() {
        if (mcurent_frag == FRAGMENT_COUNTER) {

        } else if (mSettingsViewPager.getCurrentItem() == FRAGMENT_OPEN) {
            Log.d("mcurent_frag", "mcurent_frag>>" + FRAGMENT_OPEN);
            CounterOpenFragment counterOpenFragment = (CounterOpenFragment) mViewPagerAdapter.getItem(1);
            counterOpenFragment.notifyChanges();
        } else if (mcurent_frag == FRAGMENT_ARCHIVE) {

        }

    }

    private void setupTabLayout() {
        fragmentOne = new OpenOrdersFragment();
        fragmentTwo = new CounterOpenFragment();
        fragmentThree = new CounterArchiveFragment();
        mSettingsTabLayout.addTab(mSettingsTabLayout.newTab().setText("COUNTER"));
        if (PrefUtils.getAcceptMobileOrdersInfo(getActivity()).equalsIgnoreCase("YES")) {
            mSettingsTabLayout.addTab(mSettingsTabLayout.newTab().setText("OPEN"));
            mSettingsTabLayout.addTab(mSettingsTabLayout.newTab().setText("ARCHIVE"));
        }

    }

    private void bindWidgetsWithAnEvent() {
        mSettingsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setCurrentTabFragment(mcurent_frag);
        mSettingsTabLayout.getTabAt(mcurent_frag).select();
    }

    private void setCurrentTabFragment(int tabPosition) {
        switch (tabPosition) {
            case 0:
                fragmentTwo.mArchivePaging = 1;
                fragmentThree.mArchivePaging = 1;
                mcurent_frag = FRAGMENT_COUNTER;
                replaceFragment(fragmentOne);
                break;
            case 1:
                fragmentTwo.mArchivePaging = 1;
                mcurent_frag = FRAGMENT_OPEN;
                replaceFragment(fragmentTwo);
                break;
            case 2:
                fragmentThree.mArchivePaging = 1;
                mcurent_frag = FRAGMENT_ARCHIVE;
                replaceFragment(fragmentThree);
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }


/*public void OnloadTab()
{
    mSettingsTabLayout.post(new Runnable() {
        @Override
        public void run() {
            mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);
            mSettingsViewPager.setCurrentItem(mcurent_frag);
            mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);
        }
    });

    mSettingsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mSettingsViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            mSettingsViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            mSettingsViewPager.setCurrentItem(tab.getPosition());
        }
    });

    if (ViewCompat.isLaidOut(mSettingsTabLayout)) {
        mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);
    } else {
        mSettingsTabLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mSettingsTabLayout.setupWithViewPager(mSettingsViewPager);
                mSettingsTabLayout.removeOnLayoutChangeListener(this);
            }
        });
    }
}
    //TabLayout and ViewPager class
    private class CustomAdapter extends FragmentPagerAdapter {

        private String fragments[] = {"COUNTER", "OPEN", "ARCHIVE"};

        public CustomAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OpenOrdersFragment();
                case 1:
                    return new CounterOpenFragment();
                case 2:
                    return new CounterArchiveFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }

    }*/

}


