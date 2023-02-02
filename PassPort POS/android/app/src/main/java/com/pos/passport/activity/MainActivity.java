package com.pos.passport.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;
import com.elotouch.paypoint.register.EloTouch;
import com.pos.passport.BuildConfig;
import com.pos.passport.Exceptions.ExceptionHandler;
import com.pos.passport.R;
import com.pos.passport.adapter.NavRightListAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.fragment.AddToSaleFragment;
import com.pos.passport.fragment.AlertDialogFragment;
import com.pos.passport.fragment.DepartmentButtonFragment;
import com.pos.passport.fragment.EditItemFragment;
import com.pos.passport.fragment.FFQueueFragment;
import com.pos.passport.fragment.MenuButtonFragment;
import com.pos.passport.fragment.NoteFragment;
import com.pos.passport.fragment.OpenOrdersFragment;
import com.pos.passport.fragment.OrdersFragment;
import com.pos.passport.fragment.QueueFragment;
import com.pos.passport.fragment.ReasonDialogFragment;
import com.pos.passport.fragment.RecentFragmentView;
import com.pos.passport.fragment.ReturnFragment;
import com.pos.passport.fragment.SearchCustomerFragment;
import com.pos.passport.fragment.SearchCustomerFragmentNew;
import com.pos.passport.fragment.TenPadDialogFragment;
import com.pos.passport.interfaces.AsyncTaskListener;
import com.pos.passport.interfaces.MenuButtonInterface;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Category;
import com.pos.passport.model.Customer;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.model.NavMenu;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.task.RefreshTokenAsyncTask;
import com.pos.passport.task.SendSyncAsyncTask;
import com.pos.passport.task.UpdateAppAsyncTask;
import com.pos.passport.ui.DrawerToggle;
import com.pos.passport.ui.HideAnimator;
import com.pos.passport.util.Configurations;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EloTouchHelper;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.ReceiptHelper;
import com.pos.passport.util.RecentTransactionTag;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;
import me.pushy.sdk.Pushy;

import static com.pos.passport.R.id.sync;

public class MainActivity extends BaseActivity implements QueueInterface, MenuButtonInterface, Runnable {
    private static final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    private static final String TAG_FRAGMENT_BUTTON = "tag_fragment_button";
    private static final String DEBUG_TAG = "[MainActivity]";
    public static boolean resentReceiptPrintFlag = false;
    public static final int FRAGMENT_BUTTONS = 0;
    public static final int FRAGMENT_OPEN_ORDERS = 1;
    public static final int FRAGMENT_SAVE_SALE = 2;
    public static final int FRAGMENT_SEARCH_CUSTOMER = 3;
    public static final int FRAGMENT_ADD_TO_SALE = 4;
    public static final int FRAGMENT_ADD_NOTE = 5;
    public static final int FRAGMENT_ADD_DISCOUNT = 6;
    public static final int FRAGMENT_ADD_NEW_CUSTOMER = 7;
    public static final int FRAGMENT_DEPT_PROD_BUTTONS = 8;
    public static final int FRAGMENT_EDIT_ITEM = 9;
    public static final int FRAGMENT_RETURN = 10;
    public static final int FRAGMENT_PROCESS_RETURN = 11;
    public static final int FRAGMENT_ORDERS = 12;
    public static final int RECENT_TRANSACTIONS = 13;
    private ArrayAdapter<String> mAutoAdapter;

    public Cashier mCashier;
    public boolean loggedIn;
    private boolean mCashierEnabled = false;
    private StringBuffer mScannedBarcode;

    private int todo;
    protected int enterType;

    private NavigationView mNavigationView;
    private Button mCashierNameButton;

    private ProgressDialog pd;
    private AutoCompleteTextView textView;
    protected AlertDialog alertDialog;
    private Button loginButton;
    private LinearLayout mDateTimeLinearLayout;
    private TextView dateTimeText;
    private TextView invoiceText;
    private LinearLayout buttonScreen;
    private TableRow previewItemView;

    public String keyAmount = "";

    private AsyncTask asyncTask;
    private boolean mIsLogged;
    private boolean mIsSynced;
    private boolean showPD;
    private Timer mTimer;
    private TimerTask checkTask;
    private String mpsResponse;

    public ReportCart savedCart;
    private QueueFragment mQueueFragment;
    private FFQueueFragment mFFFragment;
    private ProductDatabase mDb;
    private ExpandableListView mNavExpandableListView;
    private ExpandableListView mRightNavListView;
    private DrawerLayout mDrawerLayout;
    private DrawerToggle mDrawerToggle;
    private TextView mPassportTextView;
    private TextView mEmailTextView;

    private Typeface mNotoSans;
    private Typeface mNotoSansBold;
    private Typeface mUbuntuRegular;
    private Typeface mUbuntuLight;
    private int mCurrentFragment;
    private Menu mMenu;
    private Cart tempCart;

    private ArrayAdapter<CharSequence> mMenuAdapter;
    //Spinner mMenuSpinner;
    ListView mMenuSpinner;

    private SimpleCursorAdapter mItemSearchAdaptor;
    private SearchView searchView;
    private NavRightListAdapter mNavRightMenuAdapter;
    private View mRightNavLayout;
    private Button mRightPanelBackButton;
    private TextView mCashierToolbarName;
    private TextView mToolbarAppTitle;
    private ArrayList<Category> mCatogoryList = new ArrayList<>();
    private EloTouch mEloTouch;
    private Context mcontext;
    private OrdersFragment ordersFragment;
    private OrderUpdate receiver_update;
    private SyncBroadcastReceiver syncBroadcastReceiver;
    private Configurations mRemoteConfig;
    private ImageView locButton;
    private Dialog dialog;
    private int mFragmentCase=0;
    private SearchView.OnSuggestionListener mSuggestionClickListner = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            return true;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            Cursor productC = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            if (productC != null) {
                searchView.clearFocus();
                searchView.setQuery(productC.getString(productC.getColumnIndex("name")), false);
                searchView.setIconified(true);
                productC.moveToPosition(position);
                long quan = 1;

                Product product = new Product();
                //Log.e("price", "price>>>" + productC.getColumnIndex("price"));
                product.price = new BigDecimal(productC.getString(productC.getColumnIndex("price")));
                //Log.e("sale price", "sprice before>>>" + productC.getString(productC.getColumnIndex("salePrice")));
                product.salePrice = new BigDecimal(productC.getString(productC.getColumnIndex("salePrice")));
                product.endSale = productC.getLong(productC.getColumnIndex("saleEndDate"));
                product.startSale = productC.getLong(productC.getColumnIndex("saleStartDate"));
                product.cost = new BigDecimal(productC.getString(productC.getColumnIndex("cost")));
                //Log.e("id get","id>>>>"+productC.getInt(productC.getColumnIndex("_id")));
                product.id = productC.getInt(productC.getColumnIndex("_id"));
                product.barcode = (productC.getString(productC.getColumnIndex("barcode")));

                product.name = (productC.getString(productC.getColumnIndex("name")));

                product.desc = (productC.getString(productC.getColumnIndex("desc")));
                product.onHand = (productC.getInt(productC.getColumnIndex("quantity")));
                product.cat = (productC.getInt(productC.getColumnIndex("catid")));
                product.quantity = (int) quan;
                product.buttonID = (productC.getInt(productC.getColumnIndex("buttonID")));
                product.lastSold = (productC.getInt(productC.getColumnIndex("lastSold")));
                product.lastReceived = (productC.getInt(productC.getColumnIndex("lastReceived")));
                product.lowAmount = (productC.getInt(productC.getColumnIndex("lowAmount")));
                product.track = (productC.getInt(productC.getColumnIndex("track")) != 0);
                product.modi_data = (productC.getString(productC.getColumnIndex("modifiers")));
                product.comboItems = (productC.getString(productC.getColumnIndex("comboItems")));
                product.combo = (productC.getInt(productC.getColumnIndex("combo")));
                int taxone = (productC.getInt(productC.getColumnIndex("taxable")));
                if (taxone == 0)
                    product.taxable = false;
                else
                    product.taxable = true;
                int isAlcoholic = (productC.getInt(productC.getColumnIndex("isAlcoholic")));
                if (isAlcoholic == 0)
                    product.isAlcoholic = false;
                else
                    product.isAlcoholic = true;
                int isTobaco = (productC.getInt(productC.getColumnIndex("isTobaco")));
                if (isTobaco == 0)
                    product.isTobaco = false;
                else
                    product.isTobaco = true;
                onAddProduct(product);

                productC.close();
            }

            return true;
        }
    };



    public class SyncBroadcastReceiver extends BroadcastReceiver {
        public static final String PROCESS_SYNC_RESPONSE = "com.pos.cumulus.intent.action.PROCESS_SYNC_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            processSync();
        }
    }

    public class OrderUpdate extends BroadcastReceiver
    {
        public static final String PROCESS_RESPONSE_1 = "com.pos.cumulus.intent.action.PROCESS_RESPONSE_1";

        @Override
        public void onReceive(Context context, Intent intent) {
//            String u_key = intent.getStringExtra("updatekey");
//            String m_key=intent.getStringExtra("mkey");
            Log.d("Call brod", "Getting brod");
            Log.d("mCurrentFragment", "mCurrentFragment>>>>" + mCurrentFragment);
            Log.d("mCurrentFragment", "FRAGMENT_ORDERS>>>>" + FRAGMENT_ORDERS);
            try {
                if (mCurrentFragment == FRAGMENT_ORDERS) {
                    Log.d("Call brod", "Getting brod if con");
                    ordersFragment.notifyChanges();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled (true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Pushy.listen(this);
        Log.e(DEBUG_TAG, "onCreate");
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this, MainActivity.class));
        mcontext = this;

        mRemoteConfig = new Configurations(this);
        mRemoteConfig.setDefaults();
         Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        sendRegistrationToServer();
        Fabric.with(this, crashlyticsKit);
        Fabric.with(this, new Answers(), new Crashlytics());
        IntentFilter orderIntentFilter = new IntentFilter(OrderUpdate.PROCESS_RESPONSE_1);
        orderIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver_update = new OrderUpdate();
        registerReceiver(receiver_update, orderIntentFilter);
        IntentFilter syncIntentFilter = new IntentFilter(SyncBroadcastReceiver.PROCESS_SYNC_RESPONSE);
        syncIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        syncBroadcastReceiver = new SyncBroadcastReceiver();
        registerReceiver(syncBroadcastReceiver, syncIntentFilter);

        if (savedInstanceState == null) {
            Fragment newFragment = new MenuButtonFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_fragment, newFragment, TAG_FRAGMENT_BUTTON).commit();
            mCurrentFragment = FRAGMENT_BUTTONS;
        }

       setUpLayout();
         ordersFragment = new OrdersFragment();
        ViewGroup vg = (ViewGroup) getWindow().getDecorView();
        mNotoSans = Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Regular.ttf");
        mNotoSansBold = Typeface.createFromAsset(getAssets(), "fonts/NotoSans-Bold.ttf");
        mUbuntuRegular = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-R.ttf");
        mUbuntuLight = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Light.ttf");
        Utils.setTypeFace(mNotoSansBold, vg);

        mDb = ProductDatabase.getInstance(this);
        Log.e(DEBUG_TAG, "Database Name:" + mDb.getDatabaseName());
        mTimer = new Timer();
        readDatabase();
        bindUIElements();
        mToolbarAppTitle.setTypeface(mUbuntuRegular);
        mCashierToolbarName.setTypeface(mUbuntuLight);
        mAutoAdapter = new ArrayAdapter<>(this, R.layout.item_list);
        mAutoAdapter.setNotifyOnChange(true);
        textView.setAdapter(mAutoAdapter);
        setUpDeptList();
        setUpNavMenu();
        setUpListeners();

        StringBuffer message = new StringBuffer("                                        ");
        String welcome1 = getString(R.string.txt_welcome_to);
        String store = StoreSetting.getName();
        if (store.equals("")) {
            store = getString(R.string.txt_our_store);
        }

        if (welcome1.length() > 20)
            welcome1 = welcome1.substring(0, 19);

        int start = 9 - welcome1.length() / 2;
        message.replace(start, start + welcome1.length() - 1, welcome1);

        if (store.length() > 20)
            store = store.substring(0, 19);

        start = 29 - store.length() / 2;
        message.replace(start, start + store.length() - 1, store);

        if (PrefUtils.hasLoginInfo(this)) {
            loginCashier();
        } else {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivityForResult(intent, Consts.REQUEST_REGISTER);
        }

        if (onRetainNonConfigurationInstance() != null) {
            asyncTask = (AsyncTask) onRetainNonConfigurationInstance();
            if (!(asyncTask.getStatus().equals(AsyncTask.Status.FINISHED))) {
                if (showPD) pd.show();
            }
        }
        mScannedBarcode = new StringBuffer();
        if (Build.MODEL.contains(Consts.ELO_MODEL)) {
            mEloTouch = new EloTouch(MainActivity.this);
            mEloTouch.turnOnBarcodeLaser();
            mEloTouch.setBacklight(true);
        }
    }

    private void sendRegistrationToServer() {
        LoginCredential credential = PrefUtils.getLoginCredential(this);
        if (!credential.getUserId().equalsIgnoreCase("")) {
            RefreshTokenAsyncTask asyncTask = new RefreshTokenAsyncTask(this, false);
            asyncTask.setListener(new AsyncTaskListener() {

                @Override
                public void onSuccess() {
                    Log.e("update token", "call onsuccess");

                }

                @Override
                public void onFailure() {
                    //Log.e("update token", "call onFailure");
                }
            });

            asyncTask.execute();
        }
    }

    @Override
    protected void onPause() {
        if (checkTask != null) {
            Log.e("TIMER", "timer canceled");
            checkTask.cancel();
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
       mQueueFragment.notifyChanges();
        buttonDataChange();
        mQueueFragment.updateTotals();

        if (PrefUtils.getAutoSyncInfo(MainActivity.this).equalsIgnoreCase("ON")) {
            enableAutoSyncTask();
        }


        super.onResume();
        // ScreenSharingWrapper.getInstance().setRunningStateListener(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver_update);
        unregisterReceiver(syncBroadcastReceiver);
        if (pd != null && pd.isShowing()) pd.dismiss();
        EscPosDriver.closeBTConnections();
        if (checkTask != null) {
            Log.d("TIMER", "timer canceled");
            checkTask.cancel();
        }
        mTimer.cancel();
        /*if (mEloTouch != null)
            mEloTouch.stopShiftTimer();*/
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_right, menu);
        this.mMenu = menu;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        if (v != null) {
            v.setImageResource(R.drawable.search_icon_view);
        }


        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mItemSearchAdaptor = new SimpleCursorAdapter(MainActivity.this, R.layout.view_suggestion_list, null,
                new String[]{"name", "barcode"}, new int[]{android.R.id.text1}, 0);
        mItemSearchAdaptor.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str)
            {
                if (str != null && str.length() > 2)
                    return mDb.fetchItemsByName(str.toString());
                return null;
            }
        });

        mItemSearchAdaptor.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (cursor.getCount() > 0) {
                    StringBuffer item = new StringBuffer();
                    item.append(cursor.getString(cursor.getColumnIndex("name"))).append(" ")
                            .append(cursor.getString(cursor.getColumnIndex("barcode")));
                    ((TextView) view).setText(item.toString());
                    return true;
                }
                return false;
            }
        });
        mItemSearchAdaptor.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor c) {
                return c.getString(c.getColumnIndexOrThrow("name"));
            }
        });
        searchView.setSuggestionsAdapter(mItemSearchAdaptor);
        searchView.setOnSuggestionListener(mSuggestionClickListner);
        MenuItem item = mMenu.findItem(R.id.open_new_rec);

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                View menuItemView = MainActivity.this.findViewById(R.id.open_new_rec);
                showPopup(menuItemView);
                return false;
            }
        });


        return true;
    }

    private void showPopup(View v1) {
        try {

            dialog = new Dialog(mcontext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_layout_menu);
            // dialog.setTitle("Custom Dialog");
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            int popuptype = getResources().getInteger(R.integer.popuptype);
            if (popuptype == 0)
                wmlp.width = 370;
            else
                wmlp.width = 500;

            wmlp.gravity = Gravity.TOP | Gravity.RIGHT;
            wmlp.x = 0;   //x position
            wmlp.y = 100;
            mMenuSpinner = (ListView) dialog.findViewById(R.id.spinner);
            String[] menus;
            if(PrefUtils.getAcceptMobileOrdersInfo(MainActivity.this).equalsIgnoreCase("YES"))
                menus = getResources().getStringArray(R.array.queue_menu);
            else
                menus = getResources().getStringArray(R.array.queue_menu_counter);
            ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menus));
            mMenuAdapter = new MenuArrayAdapter(mcontext, R.layout.view_queue_spinner_item, menuList);
            mMenuAdapter.setDropDownViewResource(R.layout.view_queue_spinner_dropdown_item);
            //mMenuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mMenuSpinner.setAdapter(mMenuAdapter);
            mMenuSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (i) {
                        case 0: // Open Orders
                            dialog.cancel();
                            mFragmentCase=0;
                            onChangeFragment(MainActivity.FRAGMENT_ORDERS);
                            break;
                        case 1: // Open Orders
                            dialog.cancel();
                            mFragmentCase=1;
                            onChangeFragment(MainActivity.FRAGMENT_ORDERS);
                            break;
                        case 2: // Open Orders
                            dialog.cancel();
                            mFragmentCase=2;
                            onChangeFragment(MainActivity.FRAGMENT_ORDERS);
                            break;
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//return popup;
    }

    private class MenuArrayAdapter extends ArrayAdapter {
        private Typeface raleway;

        public MenuArrayAdapter(Context context, @LayoutRes int textViewResourceId, @NonNull List<String> objects) {
            super(context, textViewResourceId, objects);
            raleway = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        }

        public TextView getView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setTextColor(Color.BLACK);
            Utils.setTypeFace(raleway, parent);
            //v.setVisibility(View.GONE);
            return v;
        }

        public TextView getDropDownView(final int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            v.setBackgroundColor(Color.BLACK);
            Utils.setTypeFace(raleway, parent);

            return v;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            /*case R.id.item_navigation_left_panel:
                HideAnimator.showView(mRightNavLayout);
                break;*/
            case R.id.new_order:
                if (mQueueFragment.getCart().mProducts.size() > 0) {
                    mQueueFragment.mClearImageButton.performClick();
                }
                break;
            case R.id.menu_cashier:
                //onChangeFragment(MainActivity.FRAGMENT_SEARCH_CUSTOMER);
                SearchCustomerFragmentNew newFragment1 = new SearchCustomerFragmentNew();
                newFragment1.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                break;
            case R.id.item_add_product:
                NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ADD_ITEM, mCatogoryList);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setNoteListener(new NoteFragment.NoteListener() {
                    @Override
                    public void onNote(int position, String note, BigDecimal amount) {
                        Log.e("Shoe position", "temp>>>" + position);
                        if (position == -1) {
                            Log.e("if condition", "postion get" + position);
                            BigDecimal amount1 = amount.divide(Consts.HUNDRED);
                            Product product = new Product();
                            product.price = amount1;
                            product.name = note;
                            product.isNote = true;
                            //product.cat = mCatogoryList.get(position + 1).getId();
                            onAddProduct(product);
                        } else {
                            BigDecimal amount1 = amount.divide(Consts.HUNDRED);
                            Product product = new Product();
                            product.price = amount1;
                            product.name = note;
                            product.cat = mCatogoryList.get(position + 1).getId();
                            onAddProduct(product);
                        }
                    }

                    @Override
                    public void onDelete() {

                    }
                });
                break;
              /*case R.id.open_new_rec:
                  showPopup();
                  break;*/
            /*case R.id.help:
                LoginCredential credential = PrefUtils.getLoginCredential(MainActivity.this);
                String serviceCaseName ="DeviceId : "+credential.getTerminalId();
                String serviceCaseDescription = "UserId : "+credential.getUserId()+"\n"+" Cashier : "+ WordUtils.capitalize(PrefUtils.getCashierInfo(MainActivity.this).name);
                */

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*if(PrefUtils.getCashierInfo(MainActivity.this) != null)
            menu.findItem(R.id.menu_cashier).setTitle(PrefUtils.getCashierInfo(MainActivity.this).name);*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Consts.REQUEST_REGISTER:
                if (resultCode == RESULT_OK)
                {
                    Log.e(DEBUG_TAG, "onActivityResult");
                    if (Utils.hasInternet(MainActivity.this))
                    {
                        SendSyncAsyncTask sendSyncTask = new SendSyncAsyncTask(MainActivity.this, true)
                        {
                            @Override
                            protected void onPostExecute(String result)
                            {
                                super.onPostExecute(result);
                                buttonDataChange();
                                setUpDeptList();
                                resetShop(false);
                                if (Build.MODEL.contains(Consts.ELO_MODEL))
                                {
                                    new EloTouchHelper(MainActivity.this).setUpPrinter();
                                    mEloTouch.clearEloDisplay();
                                    mEloTouch.setEloDisplayLine1(getResources().getString(R.string.txt_welcome_to));
                                    mEloTouch.setEloDisplayLine2(StoreSetting.getName());
                                }
                                if (result != null && result.contains("success"))
                                    loginCashier();
                            }
                        };
                        sendSyncTask.execute();
                    }
                }
                break;

            case Consts.REQUEST_PAY:
                if (data != null) {
                    ArrayList<Payment> payments = (ArrayList<Payment>) data.getExtras().getSerializable(PayActivity.BUNDLE_PAYMENTS);
                    mQueueFragment.setPayment(payments);
                    BigDecimal tip = BigDecimal.ZERO;
                    for (Payment payment : payments) {
                        tip = tip.add(payment.tipAmount);
                    }
                    mQueueFragment.getCart().mTotal = getCart().mTotal.add(tip);

                    if (resultCode == RESULT_OK)
                    {
                        transNumberIncrement();
                        mQueueFragment.saveSale(data.getStringExtra(PayActivity.BUNDLE_GATEWAY_ID), 0);
                        mQueueFragment.printSale();
                    } else if (resultCode == RESULT_FAILED){
                        mQueueFragment.saveSale(null, 0);
                        mQueueFragment.printSale();
                    } else if (resultCode == RESULT_CANCELED)
                    {
                        mQueueFragment.updateTotals();
                    }
                }
                break;
            case Consts.REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    try {
                        Log.d("debug", PrefUtils.getCashierInfo(this).toString());
                        loginCashier();
                        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        setSupportActionBar(toolbar);
                        Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
                        f.setAccessible(true);
                        invalidateOptionsMenu();
                        TextView titleTextView = (TextView) f.get(toolbar);
                        titleTextView.setVisibility(View.GONE);
                        mCashierToolbarName.setText(WordUtils.capitalize(PrefUtils.getCashierInfo(this).name));
                    /*Utils.setToolbarTitleFont(mcontext, toolbar);*/
                        Cashier cashier = PrefUtils.getCashierInfo(this);
                        if (cashier != null && !PrefUtils.getCashierInfo(this).email.isEmpty()) {
                            mEmailTextView.setVisibility(View.VISIBLE);
                            mEmailTextView.setText(PrefUtils.getCashierInfo(this).email);
                        } else
                            mEmailTextView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.d(DEBUG_TAG, e.getMessage());
                    }
                }
        }
    }

    public void transNumberIncrement() {
            BigDecimal currentTrans = new BigDecimal(PrefUtils.getCurrentTrans(this));
            PrefUtils.updateTransNumber(this, String.valueOf(currentTrans.add(BigDecimal.ONE)));
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            return;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                onBackPressed();
            } else if (event.getAction() == KeyEvent.ACTION_UP && this.getCurrentFocus() != null && !(this.getCurrentFocus() instanceof EditText)) {
                char pressedKey = (char) event.getUnicodeChar();
                if (event.getKeyCode() != 59)
                    mScannedBarcode.append(Character.toString(pressedKey));
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
                    r.play();
                    Product item = mDb.findByBarcode(mScannedBarcode.toString().trim());
                    if (item == null) {
                        Utils.alertBox(MainActivity.this, R.string.txt_not_found,
                                String.format("%1$s - %2$s", getResources().getString(R.string.txt_product_not_found), mScannedBarcode.toString()));
                        mScannedBarcode.setLength(0);
                        return super.dispatchKeyEvent(event);
                    }
                    onAddProduct(item);
                    mScannedBarcode.setLength(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
            *//*default:
                if (this.getCurrentFocus() != null && !(this.getCurrentFocus() instanceof EditText)) {
                    char pressedKey = (char) event.getUnicodeChar();
                    if (keyCode != 59)
                        mScannedBarcode.append(Character.toString(pressedKey));
                    if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB || pressedKey == '\n'){
                        Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
                        r.play();
                        Product item = mDb.findByBarcode(mScannedBarcode.toString().trim());
                        if (item == null) {
                            Utils.alertBox(MainActivity.this, R.string.txt_not_found,
                                    String.format("%1$s - %2$s", getResources().getString(R.string.txt_product_not_found), mScannedBarcode.toString()));
                            mScannedBarcode.setLength(0);
                            return super.onKeyDown(keyCode, event);
                        }
                        onAddProduct(item);
                        mScannedBarcode.setLength(0);
                    }
                }

                break;*//*
        }

        return super.onKeyDown(keyCode, event);
    }*/

    private void onNavigationMenuSelected(NavMenu menu) {
        switch (menu.getId()) {
            case R.id.recent_transactions:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Reprint receipt"));
                //startActivity(new Intent(this, RecentTransactionsActivity.class));
                closeDrawer();
                onChangeFragment(MainActivity.RECENT_TRANSACTIONS);

                break;

            case R.id.kick_cash_drawer:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Kick cash drawer"));
                for (String t : ReceiptSetting.printers) {
                    try {
                        JSONObject object = new JSONObject(t);

                        ReceiptSetting.denabled = true;
                        ReceiptSetting.daddress = object.getString("address");
                        ReceiptSetting.make = object.getInt("printer");
                        ReceiptSetting.ddrawer = object.getBoolean("cashDrawer");
                        if (object.has("main"))
                            ReceiptSetting.dmainPrinter = object.getBoolean("main");
                        else
                            ReceiptSetting.dmainPrinter = true;

                        if (ReceiptSetting.ddrawer)
                            EscPosDriver.kickDrawer(MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.reports:
                closeDrawer();
                //Answers.getInstance().logCustom(new CustomEvent("NavigationView").putCustomAttribute("submenu", "Admin"));
                startActivity(new Intent(MainActivity.this, MainReportsActivity.class));
                break;

            case R.id.settings:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Settings"));
                if (mCashier.permissionSettings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return;
                }
                TenPadDialogFragment f = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                f.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                f.setTenPadListener(new TenPadDialogFragment.TenPadListener() {
                    @Override
                    public void onAdminAccessGranted() {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }

                    @Override
                    public void onAdminAccessDenied() {
                        Utils.alertBox(MainActivity.this, R.string.txt_admin_login, R.string.msg_admin_login_failed);
                    }
                });

                break;

            case sync:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Sync"));
                if (getCart().mProducts.size() > 0) {
                    AlertDialogFragment fragment = AlertDialogFragment.getInstance(MainActivity.this, R.string.txt_sync, R.string.msg_transaction_complete, R.string.txt_ok, R.string.txt_cancel);
                    fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                        @Override
                        public void ok() {
                            processSync();
                        }

                        @Override
                        public void cancel() {}
                    });
                    fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                } else if (getCart().mProducts.size() == 0)
                    processSync();
                break;

            case R.id.help:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Help"));
                startActivity(new Intent(this, HelpActivity.class));
                break;

            case R.id.logout:
                Answers.getInstance().logCustom(new CustomEvent("NavigationView")
                        .putCustomAttribute("submenu", "Logout"));
                logoutCashier();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, Consts.REQUEST_LOGIN);
                break;
        }
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setUpLayout() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            TextView titleTextView = (TextView) f.get(toolbar);
            titleTextView.setVisibility(View.GONE);
            //Utils.setToolbarTitleFont(this, toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new DrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mNavigationView.getLayoutParams();
            params.width = getResources().getDisplayMetrics().widthPixels / 3;
            mNavigationView.setLayoutParams(params);
            mRightNavLayout = (View) findViewById(R.id.view_nav_departments);
            mRightPanelBackButton = (Button) findViewById(R.id.exp_list_back_button);

            if (Utils.hasSmallerSide(this, Consts.MINIMUM_SIZE_FOR_PORTRAIT_IN_PIXEL)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readDatabase() {
        mDb.findTax();
        mDb.findStoreSettings(PrefUtils.getReceiptHeaderType(this));
        mDb.findEmailSettings();
        mDb.findReceiptSettings();
        mDb.findAdminSettings();
        mDb.findMercurySettings();
    }

    private void bindUIElements() {
        mQueueFragment = (QueueFragment) getSupportFragmentManager().findFragmentById(R.id.queue_fragment);
        mFFFragment = (FFQueueFragment) getSupportFragmentManager().findFragmentById(R.id.ff_fragment);
        mFFFragment.getView().setVisibility(View.GONE);
        mDateTimeLinearLayout = (LinearLayout) findViewById(R.id.date_time_linear_layout);

        dateTimeText = (TextView) findViewById(R.id.date_time_text_view);
        invoiceText = (TextView) findViewById(R.id.invoice_text_view);
        textView = (AutoCompleteTextView) findViewById(R.id.search_autocomplete_text_view);
        mNavExpandableListView = (ExpandableListView) findViewById(R.id.nav_expandable_list_view);
        mRightNavListView = (ExpandableListView) findViewById(R.id.nav_expandable_list_view_right);
        mCashierNameButton = (Button) findViewById(R.id.cashier_name_button);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mCashierToolbarName = (TextView) findViewById(R.id.toolbar_cashier_text_view);
        mToolbarAppTitle = (TextView) findViewById(R.id.toolbar_app_title);
    }

    private void setUpNavMenu() {
        List<NavMenu> menus = new ArrayList<>();
        NavMenu registerMenu = new NavMenu(R.id.register, R.drawable.ic_laptop_24dp, R.string.txt_register);
        registerMenu.getSubMenus().add(new NavMenu(R.id.recent_transactions, 0, R.string.txt_recent_transactions));
        registerMenu.getSubMenus().add(new NavMenu(R.id.kick_cash_drawer, 0, R.string.txt_kick_cash_drawer));
        menus.add(registerMenu);
        menus.add(new NavMenu(R.id.reports, R.drawable.ic_receipt_24dp, R.string.txt_reports));
        menus.add(new NavMenu(R.id.settings, R.drawable.ic_settings_24dp, R.string.txt_settings));
        menus.add(new NavMenu(sync, R.drawable.ic_sync_24dp, R.string.txt_sync));
        menus.add(new NavMenu(R.id.help, R.drawable.ic_help_24dp, R.string.txt_help));
        menus.add(new NavMenu(R.id.logout, R.drawable.ic_exit_24dp, R.string.txt_logout));

        NavExpandableListAdapter adapter = new NavExpandableListAdapter(this, menus);
        mNavExpandableListView.setAdapter(adapter);

        View headerLayout = mNavigationView.getHeaderView(0);
        mPassportTextView = (TextView) headerLayout.findViewById(R.id.cumulus_text_view);
        ((TextView) headerLayout.findViewById(R.id.version_text_view)).setText(String.format("V-%1$s", Utils.getVersionName(MainActivity.this)));
        mPassportTextView.setTypeface(mNotoSans);
    }

    private void setUpListeners() {
        textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        mNavExpandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);
        mRightPanelBackButton.setOnClickListener(v -> HideAnimator.slideToRight(mRightNavLayout));
    }

    private void setUpDeptList() {
        if (mCatogoryList.size() > 0)
            mCatogoryList.clear();

        if (PrefUtils.getNavigationInfo(this).equalsIgnoreCase("ON"))
            mRightNavLayout.setVisibility(View.VISIBLE);
        else
            mRightNavLayout.setVisibility(View.GONE);
        mCatogoryList = mDb.getCats();

        Category product = new Category();
        //product.setName("All Items");
        product.setName(getString(R.string.txt_menu));

        product.setId(0);
        product.setTaxable1(false);
        product.setTaxable2(false);
        product.setTaxable3(false);
        product.setTax(0);
        product.setTaxarray("");
        mCatogoryList.add(0, product);
        mNavRightMenuAdapter = new NavRightListAdapter(this, mCatogoryList, R.layout.view_textview,getColorArray(mCatogoryList.size()));
        mRightNavListView.setAdapter(mNavRightMenuAdapter);
        mNavRightMenuAdapter.setListener((id, posg) -> {
            mNavRightMenuAdapter.notifyDataSetChanged();
            if (mQueueFragment.CheckRecent()) {
                clearRecentQueue();
            }
            if (posg == 0) {
                onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            } else {
                //Log.e("MainActivity", "setUpDeptList()>>> onitemclick");
                onChangeFragment(MainActivity.FRAGMENT_DEPT_PROD_BUTTONS);
            }
            onViewFFFragment(false,new OpenorderData(),false);
        });
    }
    public int[] getColorArray(int len) {
        int[] colors = new int[len];
        int[] androidColors = this.getResources().getIntArray(R.array.androidcolors);
        for(int c=0; c<len; c++)
        {
            if(c >= androidColors.length)
                colors[c]=Utils.getRandomColor();
            else
                colors[c]=androidColors[c];
        }
        return colors;
    }
    private void clearRecentQueue() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        if (currentFragment.getTag().equalsIgnoreCase(RecentFragmentView.TAG)) {
            resetShop(true);
            getCart().mRecent = false;
            mQueueFragment.setCart(getCart());
            mQueueFragment.updateTotals();
        }
    }

    public void resetShop(boolean clearCart) {
        if (clearCart)
            mQueueFragment.removeAll();
        mDb.resetAll();

        mDb.findTax();
        mDb.findStoreSettings(PrefUtils.getReceiptHeaderType(this));
        mDb.findEmailSettings();
        mDb.findReceiptSettings();
        mDb.findAdminSettings();
        mDb.findMercurySettings();
        mDb.findCats();
    }

    private void loginCashier() {
        if (!PrefUtils.hasCashier(this)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, Consts.REQUEST_LOGIN);
            return;
        }
        keyAmount = "";
        enterType = 0;
        mCashier = PrefUtils.getCashierInfo(this);
        /*mCashierNameButton.setText(Html.fromHtml("<b><small>" + "Login: " + "</small></b>" +
                mCashier.name.toUpperCase()));*/
        mCashierNameButton.setText(WordUtils.capitalize(mCashier.name));
        invalidateOptionsMenu();
        loggedIn = true;
        onChangeFragment(FRAGMENT_BUTTONS);
        mQueueFragment.notifyChanges();
    }

    private void logoutCashier() {
        mCashier = null;
        PrefUtils.removeCashier(this);
        supportInvalidateOptionsMenu();
        loggedIn = false;
    }

    protected void resentReprintReceipts() {
        if (ReceiptSetting.enabled) {
            resentReceiptPrintFlag = true;
            todo = 2;
            pd = ProgressDialog.show(this, "", getString(R.string.txt_reprint_receipt_ellipsis), true, false);
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    protected void saleDone() {
        BigDecimal paymentSum = BigDecimal.ZERO;

        for (int p = 0; p < mQueueFragment.getCart().mPayments.size(); p++) {
            paymentSum = paymentSum.add(mQueueFragment.getCart().mPayments.get(p).paymentAmount);
            //paymentSum += cart.mPayments.get(p).paymentAmount;
        }

        if (StoreSetting.clearSale) {
            finalizeSale();
        }
    }

    public void finalizeSale() {
        if (previewItemView != null) {
            previewItemView.setVisibility(View.GONE);
        }

        mQueueFragment.removeAll();
        mQueueFragment.notifyChanges();
        mQueueFragment.updateTotals();

        sendSaleToServer();
    }

    private synchronized void sendSaleToServer() {
        if (Utils.hasInternet(this)) {
            ReportCart cartToSend = mDb.getUnsentSale();

            if (cartToSend != null) {
                new SendSale().execute(cartToSend);
            }
        } else {
            Utils.alertBox(MainActivity.this, getString(R.string.txt_no_network), getString(R.string.msg_no_network_sale_will_be_uploaded_when_available));
        }
    }

    private boolean printOpenOrder() {

        for (String t : ReceiptSetting.printers) {
            try {
                JSONObject object = new JSONObject(t);
                if (object.optBoolean("openOrderPrinter")) {
                    return EscPosDriver.printReceipt(this, tempCart, Consts.MERCHANT_PRINT_RECEIPT_NO);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean print() {
        boolean result;

        for (String t : ReceiptSetting.printers) {
            try {
                JSONObject object = new JSONObject(t);

                ReceiptSetting.enabled = true;
                ReceiptSetting.address = object.getString("address");
                ReceiptSetting.make = object.getInt("printer");
                ReceiptSetting.size = object.getInt("size");
                ReceiptSetting.type = object.getInt("type");
                ReceiptSetting.drawer = object.getBoolean("cashDrawer");
                if (object.has("main"))
                    ReceiptSetting.mainPrinter = object.getBoolean("main");
                else
                    ReceiptSetting.mainPrinter = true;

                if (ReceiptSetting.mainPrinter) {
                    for (int i = 0; i < mQueueFragment.getCart().mPayments.size(); i++) {
                        if (mQueueFragment.getCart().mPayments.get(i).print && StoreSetting.print_sig) {
                            EscPosDriver.print(this, ReceiptHelper.printCharge(mQueueFragment.getCart().mPayments.get(i)), ReceiptSetting.drawer);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        result = EscPosDriver.printReceipt(this, mQueueFragment.getCart(), Consts.MERCHANT_PRINT_RECEIPT_NO);

        return result;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                if (pd != null && pd.isShowing()) pd.dismiss();
                saleDone();
            } else if (msg.what == 9) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_email_receipt_sent_successfully), Toast.LENGTH_LONG).show();
            } else if (msg.what == 8) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_email_receipt_not_sent), Toast.LENGTH_LONG).show();
            } else if (msg.what == 11) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_receipt_sent_to_printer), Toast.LENGTH_LONG).show();
            } else if (msg.what == 12) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_unable_to_print_receipt), Toast.LENGTH_LONG).show();
            } else if (msg.what == 20) {
                if (pd != null && pd.isShowing()) pd.dismiss();

            }
        }
    };

    @Override
    public void run() {
        if (todo == 2) {
            if (print()) {
                resentReceiptPrintFlag = false;
                Message m = new Message();
                m.what = 11;
                handler.sendMessage(m);
            } else {
                resentReceiptPrintFlag = false;
                Message m = new Message();
                m.what = 12;
                handler.sendMessage(m);
            }

            Message m = new Message();
            m.what = 10;
            handler.sendMessage(m);
        } else if (todo == 3) {
            if (print()) {
                Message m = new Message();
                m.what = 11;
                handler.sendMessage(m);
            } else {
                Message m = new Message();
                m.what = 12;
                handler.sendMessage(m);
            }

            Message m = new Message();
            m.what = 10;
            handler.sendMessage(m);
        } else if (todo == 4) {
            if (print()) {
                resentReceiptPrintFlag = false;
                Message m = new Message();
                m.what = 11;
                handler.sendMessage(m);
                mQueueFragment.getCart().removeAll();
            } else {
                resentReceiptPrintFlag = false;
                Message m = new Message();
                m.what = 12;
                handler.sendMessage(m);
                mQueueFragment.getCart().removeAll();
            }

            Message m = new Message();
            m.what = 20;
            handler.sendMessage(m);
        } else if (todo == 5) {
            if (printOpenOrder()) {
                Message m = new Message();
                m.what = 11;
                handler.sendMessage(m);
            } else {
                Message m = new Message();
                m.what = 12;
                handler.sendMessage(m);
            }
        }
    }

    private void checkForServer() {
        if (Utils.hasInternet(this)) {
            if (!mIsLogged) {
                mIsLogged = true;
                if (showPD)
                    pd = ProgressDialog.show(this, "", getString(R.string.txt_logging_in), true, false);
                asyncTask = new SendLogin().execute();
            }
        } else {
            Utils.alertBox(MainActivity.this, R.string.txt_no_network, R.string.msg_sales_will_sync_later);
        }
    }


    private void checkForNewContent() {
        if (Utils.hasInternet(this)) {
            if (!mIsSynced) {
                mIsSynced = true;
                SendSyncAsyncTask task = new SendSyncAsyncTask(this, false) {
                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        mIsSynced = false;
                        if (result != null && result.contains("success")) {
                            resetShop(false);
                            buttonDataChange();
                            onResume();
                            sendSaleToServer();
                        }
                    }
                };
                task.setListener(new SendSyncAsyncTask.SendSyncListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(JSONObject error) {
                        if (error.optString("tag").equalsIgnoreCase(Consts.EXPIRED)) {
                            PrefUtils.removeCashier(MainActivity.this);
                            PrefUtils.removeall(MainActivity.this);
                            CallRegActivity();
                        }
                    }
                });
                task.execute();
            } else {
                Utils.alertBox(MainActivity.this, R.string.txt_no_network, R.string.msg_sales_will_sync_later);
            }
        }
    }

    @Override
    public void onSendingMessage(int todo, @StringRes int resId) {
        this.todo = todo;
        pd = ProgressDialog.show(this, "", getString(resId), true, false);
        Thread thread = new Thread(MainActivity.this);
        thread.start();
    }

    @Override
    public void onSaleDone() {
        resetShop(true);
        mQueueFragment.updateTotals();
        getCart().mRecent = false;
        mQueueFragment.setCartView();
        saleDone();
    }

    @Override
    public void onChangeFragment(int which, Bundle bundle) {
        Fragment newFragment = null;
        switch (which) {
            case FRAGMENT_EDIT_ITEM:
                newFragment = new EditItemFragment();
                newFragment.setArguments(bundle);
                break;
        }
        mCurrentFragment = which;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.content_fragment, newFragment);
        if (mCurrentFragment == FRAGMENT_BUTTONS)
            ft.addToBackStack(TAG_FRAGMENT_BUTTON);
        ft.addToBackStack(null);

        ft.commit();
    }

    @Override
    public void onChangeFragment(int which) {
        Fragment newFragment = null;
        String fragmentTag = "";
        Bundle bundle;
        /*if (mCurrentFragment == which && mCurrentFragment != FRAGMENT_DEPT_PROD_BUTTONS)
            return;*/

        switch (which) {
            case FRAGMENT_BUTTONS:
                newFragment = new MenuButtonFragment();
                if (mQueueFragment.CheckRecent()) {
                    clearRecentQueue();
                }
                break;

            case FRAGMENT_OPEN_ORDERS:
                newFragment = new OpenOrdersFragment();
                break;

            case FRAGMENT_SEARCH_CUSTOMER:
                newFragment = new SearchCustomerFragment();
                break;
            case FRAGMENT_ADD_NEW_CUSTOMER:
                newFragment = mQueueFragment.addNewCustomer();
                break;
            case FRAGMENT_ADD_TO_SALE:
                newFragment = new AddToSaleFragment();
                Bundle args = new Bundle();
                args.putBoolean(AddToSaleFragment.CUSTOMER_BUTTON, getCart().hasCustomer());
                newFragment.setArguments(args);
                break;
            case FRAGMENT_ADD_NOTE:
                newFragment = mQueueFragment.addNoteToSale();
                break;
            case FRAGMENT_ADD_DISCOUNT:
                newFragment = mQueueFragment.addDiscountToSale();

                break;
            case FRAGMENT_DEPT_PROD_BUTTONS:
                newFragment = new DepartmentButtonFragment();
                bundle = new Bundle();
                bundle.putInt("id", mNavRightMenuAdapter.getItemId());
                bundle.putString("name", mNavRightMenuAdapter.getItemName());
                newFragment.setArguments(bundle);
                break;
            case FRAGMENT_RETURN:
                newFragment = new ReasonDialogFragment();
                break;
            case FRAGMENT_ORDERS:
                newFragment = new OrdersFragment();
                newFragment =  OrdersFragment.newInstance(mFragmentCase);
               // ordersFragment.setParameter(mFragmentCase);
                break;
            case RECENT_TRANSACTIONS:
                newFragment = new RecentFragmentView();
                fragmentTag = RecentFragmentView.TAG;
                break;
        }
        mCurrentFragment = which;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        /*if(mCurrentFragment == FRAGMENT_ORDERS)
            ft.replace(R.id.content_fragment, newFragment, "MY_FRAGMENT");
        else*/
        ft.replace(R.id.content_fragment, newFragment, fragmentTag);

        if (mCurrentFragment == FRAGMENT_BUTTONS)
            ft.addToBackStack(TAG_FRAGMENT_BUTTON);
        ft.addToBackStack(null);

        ft.commit();
    }

    @Override
    public void onEditItem(Product product, int id) {
        mQueueFragment.saveItemEditChanges(product, id);
    }

    @Override
    public void onReturn(String returnMsg) {
        mQueueFragment.getCart().mReturnReason = returnMsg;
        ReturnFragment newFragment = new ReturnFragment();
        Bundle bundle = new Bundle();
        bundle.putString("amount", new BigDecimal(Utils.formatCartTotal(getCart().mTotal)).toString());
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
    }

    @Override
    public void onProcessReturn(Payment payment) {
        //Log.e("Payment","Payment amount>>"+payment.paymentAmount+"<< format >>"+Utils.formatTotal(payment.paymentAmount));
        //Log.e("total","total amount>>"+mQueueFragment.getCart().mTotal+"<< format >>"+Utils.formatTotal(mQueueFragment.getCart().mTotal));
        int res;
        int rstatus = RecentTransactionTag.TRANSACTION_DEFAULT;
        //Log.e(" Rec total","Recent total amount>>"+RecentTransactionTag.TOTAL_AMOUNT+"<< format >>"+Utils.formatTotal(RecentTransactionTag.TOTAL_AMOUNT));
        //res = Utils.formatTotal(mQueueFragment.getCart().mTotal).compareTo(Utils.formatTotal(payment.paymentAmount));
        res = Utils.formatTotal(RecentTransactionTag.TOTAL_AMOUNT).compareTo(Utils.formatTotal(mQueueFragment.getCart().mTotal));
//        Log.e("res","res>>"+res);
        if (res == 0)
            rstatus = RecentTransactionTag.TRANSACTION_RETURN;
        else if (res == 1)
            rstatus = RecentTransactionTag.TRANSACTION_PARTIAL;

//        Log.e("Trans old","before"+mQueueFragment.getCart().mTrans);
        BigDecimal remainingTotal = Utils.formatTotal(RecentTransactionTag.TOTAL_AMOUNT).subtract(Utils.formatTotal(payment.paymentAmount));
//        Log.e("remaining total","remaining total>>>"+remainingTotal.toString());
        if (remainingTotal.compareTo(BigDecimal.ZERO) < 0) {
            rstatus = RecentTransactionTag.TRANSACTION_RETURN;
        }
        String transid = mDb.UpdateRecentSaleStatus(mQueueFragment.getCart().mTrans.toString(), rstatus, remainingTotal.toString());
//        Log.e("Old trans id","trans id>>>"+transid);
        mQueueFragment.getCart().mStatus = Cart.RETURNED;
        mQueueFragment.getCart().mPayments.clear();
        mQueueFragment.getCart().mPayments.add(payment);
        //Log.e("Trans old","before"+mQueueFragment.getCart().mTrans);
        mQueueFragment.getCart().mTrans = mQueueFragment.setReturnTransNumber();
        //Log.e("Trans new","New trans num>>>"+mQueueFragment.getCart().mTrans);
        mQueueFragment.getCart().returnStatus = rstatus;
        mQueueFragment.saveSale(null, 1);
        onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
        mQueueFragment.printSale();
    }

    @Override
    public void onSaveQueue(String name) {
        mQueueFragment.saveCurrentSaleNew(name);
        mQueueFragment.notifyChanges();
        mQueueFragment.updateTotals();
        mQueueFragment.removeAll();
    }

    @Override
    public void onSavePrintQueue(String name) {
        tempCart = mQueueFragment.getCart().clone();
        onSendingMessage(5, R.string.txt_print_receipt);
        onSaveQueue(name);
    }

    @Override
    public void onLoadQueue(ReportCart reportCart) {

        Cart cart = (Cart) reportCart;
        cart.mId = Integer.valueOf(reportCart.getId());
        cart.mHasTransNumber = true;
        cart.mTrans = reportCart.trans;
        cart.mOnHold = ((Cart) reportCart).mOnHold;
        cart.mIsProcessed = ReportCart.PROCESS_STATUS_OFFLINE;
        cart.mIsReceived = reportCart.isReceive;
        cart.mStatus = ((Cart) reportCart).mStatus;
        cart.cOrder = 1;
        cart.mPayments = ((Cart) reportCart).mPayments;

        mQueueFragment.removeAll();
        mQueueFragment.setCart(cart);
        mQueueFragment.assignOpenOrder(cart.mName);
        mQueueFragment.setProduct();
        mQueueFragment.updateTotals();
        mQueueFragment.notifyChanges();
        onChangeFragment(FRAGMENT_BUTTONS);
        onViewFFFragment(false,new OpenorderData(),false);

    }

    @Override
    public void onLoadQueueRecent(ReportCart reportCart) {
        RecentTransactionTag.TOTAL_AMOUNT = BigDecimal.ZERO;
        Cart cart = (Cart) reportCart;
        cart.mId = Integer.valueOf(reportCart.getId());
        cart.mHasTransNumber = true;
        cart.mTrans = reportCart.trans;
        cart.mOnHold = ((Cart) reportCart).mOnHold;
        cart.mIsProcessed = ReportCart.PROCESS_STATUS_OFFLINE;
        cart.mIsReceived = reportCart.isReceive;
        cart.mStatus = ((Cart) reportCart).mStatus;
        cart.cOrder = 0;
        cart.mPayments = ((Cart) reportCart).mPayments;
        cart.mDiscountAmount = ((Cart) reportCart).mDiscountAmount;
        cart.mDiscountName = ((Cart) reportCart).mDiscountName;

        cart.mRecent = true;
        cart.returnStatus = ((Cart) reportCart).returnStatus;
        cart.mChangeAmount = ((Cart) reportCart).mChangeAmount;
        //RecentTransactionTag.TOTAL_AMOUNT=((Cart) reportCart).mTotal;
        RecentTransactionTag.TOTAL_AMOUNT = ((Cart) reportCart).totalReturn;
        mQueueFragment.removeAll();
        mQueueFragment.setCart(cart);
        mQueueFragment.assignOpenOrder(cart.mName);
        mQueueFragment.setProduct();
        mQueueFragment.updateTotals();
        mQueueFragment.notifyChanges();
        onViewFFFragment(false,new OpenorderData(),false);

    }

    @Override
    public void onViewFFFragment(boolean show, OpenorderData data, boolean clear)
    {
        if (clear)
        {
            mFFFragment.ClearCart();
        }
        else if(show)
        {

                mFFFragment.getView().setVisibility(View.VISIBLE);
                mQueueFragment.getView().setVisibility(View.GONE);

            /*FragmentManager ff = getSupportFragmentManager();
            ff.beginTransaction()
                    .hide(mQueueFragment)
                    .commit();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .show(mFFFragment)
                    .commit();*/
                mFFFragment.SetDataCart(data);

        }else
        {
            if(mFFFragment.getView().getVisibility() == View.VISIBLE)
            {
                mFFFragment.ClearCart();
                mFFFragment.getView().setVisibility(View.GONE);
                mQueueFragment.getView().setVisibility(View.VISIBLE);

            /*FragmentManager ff = getSupportFragmentManager();
            ff.beginTransaction()
                    .hide(mFFFragment)
                    .commit();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .show(mQueueFragment)
                    .commit();*/
            }

        }
    }


    @Override
    public void onAssignCustomer(Customer customer) {
        mQueueFragment.assignCustomer(customer);
    }

    @Override
    public void onRemoveCustomer() {
        mQueueFragment.removeCustomer();
    }

    @Override
    public void onOrderStatusGet() {
        if (mCurrentFragment == FRAGMENT_ORDERS) {
            ordersFragment.notifyChanges();
        }
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    public void onNotifyQueueChanged() {
        mQueueFragment.notifyChanges();
        mQueueFragment.updateTotals();
    }

    @Override
    public String onPrintCharge(Payment payment) {
        return ReceiptHelper.printCharge(payment);
    }

    @Override
    public void onAddProduct(Product product) {
        if (mQueueFragment.CheckRecent()) {
            Log.e("Check recent", "In recent");
        } else {
            mQueueFragment.addProduct(product);
        }
    }

    @Override
    public Cart getCart() {
        return mQueueFragment.getCart();
    }

    @Override
    public List<Product> getProducts() {
        return mQueueFragment.getProducts();
    }

    private class SendLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                LoginCredential credential = PrefUtils.getLoginCredential(MainActivity.this);
                String UID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String version = "";
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                JSONObject json = new JSONObject();

                json.put("loginEmail", credential.getEmail());
                //json.put("loginPin", credential.getKey());
                json.put("deviceKey", credential.getKey());
                json.put("terminalName", credential.getTerminalName());
                json.put("UID", UID);
                json.put("make", Build.BRAND);
                json.put("model", Build.MODEL);
                //  json.put("version", version);
                json.put("appVersion", version);
                //json.put("android", Build.VERSION.RELEASE);
                json.put("androidVersion", Build.VERSION.RELEASE);

                mpsResponse = Utils.postData(UrlProvider.getBase_inner() + "" + UrlProvider.LOGIN_URL, "login", json);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mpsResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            AlertDialogFragment fragment;
            if (pd != null && pd.isShowing()) pd.dismiss();
            mIsLogged = false;
            if (result != null && result.contains("success")) {
                checkForNewContent();
                PrefUtils.updateLoginLast(MainActivity.this);
            } else if (result != null && result.contains("keyused")) {
                fragment = AlertDialogFragment.getInstance(MainActivity.this, R.string.txt_login_failed, R.string.msg_key_used);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivityForResult(intent, Consts.REQUEST_REGISTER);
                    }

                    @Override
                    public void cancel() {}
                });

            } else if (result != null && result.contains("upgrade")) {
                fragment = AlertDialogFragment.getInstance(MainActivity.this, R.string.txt_update_available, R.string.msg_update_available);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        UpdateAppAsyncTask task = new UpdateAppAsyncTask(MainActivity.this);
                        task.execute();
                    }

                    @Override
                    public void cancel() {}
                });

            } else {
                fragment = AlertDialogFragment.getInstance(MainActivity.this, R.string.txt_login_error, R.string.msg_login_error);
                fragment.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setAlertListener(new AlertDialogFragment.AlertListener() {
                    @Override
                    public void ok() {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivityForResult(intent, Consts.REQUEST_REGISTER);
                    }

                    @Override
                    public void cancel() {}
                });
            }
        }
    }

    private class SendSale extends AsyncTask<ReportCart, String, String> {

        @Override
        protected String doInBackground(ReportCart... params) {
            try {
                String UID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String version = "";
                try {
                    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                JSONObject json = new JSONObject();
                LoginCredential credential = PrefUtils.getLoginCredential(MainActivity.this);
                json.put("loginEmail", credential.getEmail());
                json.put("loginPin", credential.getKey());
                json.put("terminalName", credential.getTerminalName());
                json.put("userId", credential.getUserId());
                json.put("deviceId", credential.getTerminalId());
                json.put("UID", UID);
                json.put("make", Build.BRAND);
                json.put("model", Build.MODEL);
                json.put("version", version);
                json.put("android", Build.VERSION.RELEASE);
                json.put("sync", false);
                ReportCart cart = params[0];
                savedCart = cart;
                json.put("sale_id", cart.id);
                json.put("trans_id", cart.trans);
                Calendar c = Calendar.getInstance();
                long z = (long) c.getTimeZone().getOffset(cart.mDate);
                json.put("saleDate", (cart.mDate + z) / 1000);
                json.put("subtotal", cart.mSubtotal);
                int cashiid = mDb.getCashiersAdminId();
                json.put("cashier_id", cashiid);// cart.getCashierId());
                json.put("total", Utils.formatCartTotal(cart.mTotal));
                json.put("tax1", cart.mTax1);
                json.put("tax2", cart.mTax2);
                json.put("tax3", cart.mTax3);
                json.put("taxpercent1", cart.mTax1Percent);
                json.put("taxpercent2", cart.mTax2Percent);
                json.put("taxpercent3", cart.mTax3Percent);
                json.put("taxname1", cart.mTax1Name);
                json.put("taxname2", cart.mTax2Name);
                json.put("taxname3", cart.mTax3Name);
                if (cart.mVoided) json.put("voided", 1);
                else json.put("voided", 0);
                if (cart.isReceive) json.put("isReceive", 1);
                else json.put("isReceive", 0);
                json.put("status", cart.mStatus);
                json.put("total_discount_name", cart.mDiscountName);
                json.put("total_discount_amount", cart.mDiscountAmount.toString());
                Log.e("Return reson", ">>>>" + cart.mReturnReason);
                json.put("return_reason", cart.mReturnReason);
                JSONObject cartitemdata = new JSONObject(cart.cartItems);
                if (cartitemdata.has("Payments")) {
                    JSONArray payment = cartitemdata.getJSONArray("Payments");
                    if (payment.length() > 0) {
                        if (cartitemdata.getJSONArray("Payments").length() > 1) {
                            json.put("tenderType", "SPLIT");
                        } else {
                            String typeshow = "CASH";
                            String typeget = cartitemdata.getJSONArray("Payments").getJSONObject(0).optString("paymentType");
                            if (typeget.equalsIgnoreCase(PayActivity.PAYMENT_TYPE_CREDIT)) {
                                typeshow = "CARD";
                            } else if (typeget.equalsIgnoreCase(PayActivity.PAYMENT_TYPE_OTHER)) {
                                typeshow = "OTHER";
                            } else {
                                typeshow = typeget.toUpperCase();
                            }
                            json.put("tenderType", typeshow);
                        }
                    } else {
                        json.put("tenderType", "OTHER");
                    }
                } else {
                    json.put("tenderType", "OTHER");
                }
                json.put("tax", cartitemdata.optJSONArray("tax"));
                json.put("customer", cartitemdata.optJSONObject("customer"));
                JSONObject botharray = new JSONObject();
                botharray.put("payments", cartitemdata.getJSONArray("Payments"));
                botharray.put("products", cartitemdata.getJSONArray("Products"));
                json.put("saledata", botharray);
                Log.d("Sale send data", "Sale send>>>>" + json.toString());
                JSONObject response = Utils.postData(UrlProvider.getBase_inner() + "" + UrlProvider.SUBMIT_SALE_URL, "submitSale", json.toString());// Utils.convert_2_unicode(json.toString()));
                String result = "null";
                Log.d("Result get", "Result>>>>" + response);
                if (response != null) {
                    if (response.has("transaction_id")) {
                        cart.processed = ReportCart.PROCESS_STATUS_APPROVED;
                        cart.cOrder = 0;
                        mDb.replaceSale(cart);
                        result = "success";
                    } else if (response.has("error")) {
                        if (response.has("tag")) {
                            String tagget = response.getString("tag");
                            String errorshow = response.getString("error");
                            if (tagget.equalsIgnoreCase("NoKey")) {
                                Utils.alertBox(MainActivity.this, getString(R.string.txt_sync_failed), getString(R.string.msg_sync_not_registered_device));
                                result = "Logout";
                            } else if (tagget.equalsIgnoreCase("NoUser")) {
                                Utils.alertBox(MainActivity.this, getString(R.string.txt_sync_failed), getString(R.string.msg_sync_no_user));
                                result = "Logout";
                            } else if (tagget.equalsIgnoreCase("Expired")) {
                                Utils.alertBox(MainActivity.this, getString(R.string.txt_sync_failed), getString(R.string.msg_sync_account_expired));
                                result = "Logout";
                            } else if (tagget.equalsIgnoreCase("KeyUsed")) {
                                Utils.alertBox(MainActivity.this, getString(R.string.txt_sync_failed), errorshow);
                                result = "Logout";
                            } else if (tagget.equalsIgnoreCase("InvalidUser")) {
                                Utils.alertBox(MainActivity.this, getString(R.string.txt_sync_failed), errorshow);
                                result = "Logout";
                            }
                        }

                    } else {
                        savedCart = cart;
                    }
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "FAILED";
        }

        @Override
        protected void onPostExecute(String result) {
            if (pd != null && pd.isShowing()) pd.dismiss();

            if (result != null && result.contains("success")) {
                sendSaleToServer();
            } else if (result != null && result.contains("Logout")) {
                PrefUtils.removeCashier(MainActivity.this);
                PrefUtils.removeall(MainActivity.this);
                CallRegActivity();
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            pd.setMessage(values[0]);
        }
    }

    public void CallRegActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private class NavExpandableListAdapter extends BaseExpandableListAdapter {
        private List<NavMenu> menus;
        private LayoutInflater inflater;

        public NavExpandableListAdapter(Context context, List<NavMenu> menus) {
            this.menus = menus;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return menus.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return menus.get(groupPosition).getSubMenus().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return menus.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return menus.get(groupPosition).getSubMenus().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return menus.get(groupPosition).getIcon();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return menus.get(groupPosition).getSubMenus().get(childPosition).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            final NavMenu navMenu = menus.get(groupPosition);
            if (convertView == null) {

                convertView = inflater.inflate(R.layout.view_nav_menu, null);
            }
            LinearLayout rootLinearLayout = (LinearLayout) convertView.findViewById(R.id.nav_root_linear_layout);
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.nav_icon_image_view);
            iconImageView.setImageResource(navMenu.getIcon());
            TextView nameTextView = (TextView) convertView.findViewById(R.id.nav_text_view);
            mEmailTextView = (TextView) convertView.findViewById(R.id.nav_email_text_view);
            nameTextView.setTypeface(mNotoSans);
            nameTextView.setText(navMenu.getName());
            ImageView indicatorImageView = (ImageView) convertView.findViewById(R.id.nav_group_indicator);
            //int color = Color.parseColor(getResources().getString(R.color.dark_gray));
            //indicatorImageView.setColorFilter(color);
            indicatorImageView.setImageResource(isExpanded ? R.drawable.ic_keyboard_arrow_up_24dp : R.drawable.ic_keyboard_arrow_down_24dp);
            Cashier cashier = PrefUtils.getCashierInfo(MainActivity.this);
            if (navMenu.getName() == R.string.txt_logout && cashier != null && !cashier.email.isEmpty()) {
                mEmailTextView.setVisibility(View.VISIBLE);
                mEmailTextView.setText(cashier.email);

            } else {
                mEmailTextView.setVisibility(View.GONE);
            }
            if (navMenu.getSubMenus().size() > 0) {
                indicatorImageView.setVisibility(View.VISIBLE);
                rootLinearLayout.setClickable(false);
            } else {
                indicatorImageView.setVisibility(View.INVISIBLE);
                rootLinearLayout.setClickable(true);
                rootLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNavigationMenuSelected(navMenu);
                    }
                });
            }
            /*Drawable drawable = ContextCompat.getDrawable(MainActivity.this, navMenu.getIcon());
            drawable.setBounds(0,0,R.dimen);*/
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final NavMenu navMenu = menus.get(groupPosition).getSubMenus().get(childPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_nav_menu, parent, false);
            }
            LinearLayout rootLinearLayout = (LinearLayout) convertView.findViewById(R.id.nav_root_linear_layout);
            ImageView iconImageView = (ImageView) convertView.findViewById(R.id.nav_icon_image_view);
            TextView emailTextView = (TextView) convertView.findViewById(R.id.nav_email_text_view);
            emailTextView.setVisibility(View.GONE);
            if (navMenu.getIcon() != 0) {
                iconImageView.setImageResource(navMenu.getIcon());
            } else {
                iconImageView.setVisibility(View.INVISIBLE);
            }
            TextView nameTextView = (TextView) convertView.findViewById(R.id.nav_text_view);
            nameTextView.setTypeface(mNotoSans);
            nameTextView.setText(navMenu.getName());
            rootLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationMenuSelected(navMenu);
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public void buttonDataChange() {
        try {
            ((MenuButtonFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_BUTTON)).update();
        } catch (Exception e) {
            Log.d("debug", "MenuButtonFragment cast exception");
        }
    }

    private void processSync() {
        final AppCompatActivity currentActivity = ((POSApplication) getApplicationContext()).getCurrentActivity();
        boolean showMessage = false;
        if (Utils.hasInternet(MainActivity.this)) {
            synchronized (this) {
                if (currentActivity.equals(MainActivity.this))
                    showMessage = true;
                SendSyncAsyncTask sendSyncTask = new SendSyncAsyncTask(MainActivity.this, true, showMessage, true) {
                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        Log.d("Post call", "MainActivity main post");
                        buttonDataChange();
                        resetShop(true);
                        setUpDeptList();
                        if (currentActivity.equals(MainActivity.this)) {
                            onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                        }
                    }
                };
                sendSyncTask.setListener(new SendSyncAsyncTask.SendSyncListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(JSONObject error) {
                        if (error.optString("tag").equalsIgnoreCase(Consts.EXPIRED)) {
                            PrefUtils.removeCashier(MainActivity.this);
                            PrefUtils.removeall(MainActivity.this);
                            CallRegActivity();
                        }
                    }
                });
                sendSyncTask.execute();
            }
        } else {
            Utils.alertBox(MainActivity.this, getString(R.string.txt_no_network), getString(R.string.msg_unable_to_download_data));
        }
    }

    private void enableAutoSyncTask(){
        checkTask = new TimerTask() {
            @Override
            public void run() {

                if (PrefUtils.getOfflineOption(MainActivity.this).isOffline()) {
                    onWifiConnectionChanged(Utils.isConnected(MainActivity.this));
                }

                if (PrefUtils.hasLoginInfo(MainActivity.this)) {
                    long loginLast = PrefUtils.getLoginLast(MainActivity.this);
                    long now = new Date().getTime();
                    if (now > (loginLast + 5 * 60 * 1000)) {
                        if (!mIsLogged) showPD = false;
                        //checkForServer();
                        final SendSyncAsyncTask task = new SendSyncAsyncTask(MainActivity.this, false, false) {
                            @Override
                            protected void onPostExecute(String result) {
                                mIsSynced = false;
                                super.onPostExecute(result);
                                if (result != null && result.contains("success")) {
                                    resetShop(false);
                                    buttonDataChange();
                                    onResume();
                                    PrefUtils.updateLoginLast(MainActivity.this);
                                    sendSaleToServer();
                                }
                            }
                        };
                        task.setListener(new SendSyncAsyncTask.SendSyncListener() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(JSONObject error) {
                                if (error.optString("tag").equalsIgnoreCase(Consts.EXPIRED)) {
                                    PrefUtils.removeCashier(MainActivity.this);
                                    PrefUtils.removeall(MainActivity.this);
                                    CallRegActivity();
                                }
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                task.execute();
                            }
                        });
                    }
                }
            }
        };
        mTimer.schedule(checkTask, 60 * 1000, 6 * 60 * 1000);
    }
}
