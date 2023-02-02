package com.pos.passport.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.adapter.NoteModifierAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.AdminSetting;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Category;
import com.pos.passport.model.ItemComoSelection;
import com.pos.passport.model.Modifier;
import com.pos.passport.model.Product;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class ShowEditItem extends Fragment implements View.OnClickListener {

    public static final int MODIFIER_BUTTON = 0;
    public static final int ITEM_DISCOUNT_BUTTON = 1;
    public static final int NOTE_BUTTON = 2;
    public static final int TAX_BUTTON = 3;
    private Context mcontext;
    private Dialog dialog;
    private LinearLayout modill, padll, notll, taxll, ok_taxbutton, ll_list_note_modifiers, ll_addnote;
    private ImageView mModifierButton;
    private ImageView mNoteButton;
    private ImageView mRemoveTaxButton;
    private ImageView mItemDiscountButton;
    private LinearLayout mNoteButtonLayout;
    private LinearLayout mModifierButtonLayout;
    private LinearLayout mTaxButtonLayout;
    private TextView mItemNameTextView, tax_msg;
    private ImageView canclebtn;
    private ViewPager awesomePager;
    private ProductDatabase mDb;
    private LinearLayout taxlayoutobj;
    private List<ModifierAdapter> modifierAdapters_list = new ArrayList<>();
    private GridViewPagerAdapter pm;
    private LinearLayout quantity_downbtn;
    private LinearLayout quantity_upbtn;
    private EditText quantity_txt;

    //------------------ note part --------------
    @IntDef({NOTE_SCOPE_ALL, NOTE_SCOPE_ITEM, NOTE_SCOPE_ADD_ITEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NoteScope {
    }

    public final static int NOTE_SCOPE_ITEM = 0;
    public final static int NOTE_SCOPE_ALL = 1;
    public final static int NOTE_SCOPE_ADD_ITEM = 2;

    private final static String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    int mNoteScope;
    int mPreviousQty;
    List<Product> mProducts;
    List<Category> mCategories;
    int mSelected;
    String mNote;
    public BigDecimal mPrice;

    Spinner mItemSpinner;
    EditText mNoteEditText;
    LinearLayout mOkButton, mAddviewCall;
    Button mCancelButton;
    public Button mPriceButton;

    NoteListener mNoteListener;
    QueueInterface mCallback;
    Product mProduct;
    int mPosition;
    Cart mCart;
    TextView deletitem, preitem, nextitem, updateitem;
    //private List<Modifier> mModifiersList=new ArrayList<>();
    //List<List<Modifier>> mModifiersList=new ArrayList<>();
    SparseBooleanArray mSparseBooleanArray = new SparseBooleanArray();
    // private ModifierAdapter mModifierAdapter;
    List<List<Modifier>> mdata_main = new ArrayList<>();
    ListView list_note_modifiers;
    List<Product> note_modifiers;
    NoteModifierAdapter noteModifierAdapter;
    public boolean noteEditable = false;
    public int position_not_select = 0;
    //------------------ TenPad part --------------

    @IntDef({TEN_PAD_TYPE_ADMIN, TEN_PAD_TYPE_DISCOUNT, TEN_PAD_TYPE_PRICE, TEN_PAD_TYPE_CASH, TEN_PAD_TYPE_LOGIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TenPadType {
    }

    @IntDef({DISCOUNT_AMOUNT, DISCOUNT_PERCENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DiscountType {
    }

    public static final String BUNDLE_ATTACH_AS_FRAGMENT = "bundle_attach_as_fragment";

    public static final int TEN_PAD_TYPE_ADMIN = 0;
    public static final int TEN_PAD_TYPE_DISCOUNT = 1;
    public static final int TEN_PAD_TYPE_PRICE = 2;
    public static final int TEN_PAD_TYPE_CASH = 3;
    public static final int TEN_PAD_TYPE_LOGIN = 4;

    public static final int DISCOUNT_AMOUNT = 0;
    public static final int DISCOUNT_PERCENT = 1;

    public static final int MAX_AMOUNT = 100000;   // $1000.00

    private int mType;
    private BigDecimal mMaxValue;
    private BigDecimal mPricetp;
    private BigDecimal mValue;
    private int mDiscountType;
    private boolean mAttachAsFragment;
    private LinearLayout mTitleLayout;
    private TextView mTitleTextView;
    private TextView mDisplayTextView;
    private List<Button> mButtons;
    private RadioGroup mDiscountRadioGroup;
    private RadioButton mAmountRadioButton;
    private RadioButton mPercentRadioButton;
    private ImageButton mDeleteImageButton;
    private Button mCancelButtontenpad;
    private Button mOkButtontenpad_btn;
    private LinearLayout mOkButtontenpad;
    private LinearLayout mBottomlayount;
    private Button mButton0;
    private LinearLayout mTenPadLayout;
    private TenPadListener mTenPadListener;
    private DiscountListener mDiscountListener;
    private PriceListener mPriceListener;
    private Cashier mCashier;
    List<Product> modifiers_temp;
    ArrayList<String> modi_ids_ = new ArrayList<>();
    //ArrayList<String> combo_ids_ = new ArrayList<>();
    //List<String> combo_pos = new ArrayList<>();
    List<String> combo_ids = new ArrayList<>();
    List<String> combo_itemid = new ArrayList<>();

    List<String> combo_ids_temp = new ArrayList<>();
    List<String> combo_itemid_temp = new ArrayList<>();

    List<ItemComoSelection> comboSelection = new ArrayList<>();

    ArrayList<String> modi_ids_temp = new ArrayList<>();
    ArrayList<Modifier> selectedList = new ArrayList<>();
    // private Context mCallback;
    private TaxListener taxListener;

    public interface TaxListener {
        void onTaxshow(Product product, int id);
    }

    public interface TenPadListener {
        void onAdminAccessGranted();

        void onAdminAccessDenied();
    }

    public interface DiscountListener {
        void onDiscountPrice(BigDecimal amount);

        void onDiscountPercent(BigDecimal percent);
    }

    public interface PriceListener {
        void onSetPrice(BigDecimal amount);
    }


    public interface NoteListener {
        void onNote(int position, String note, BigDecimal amount);

        void onDelete();

        void CallFrag(BigDecimal amount, Button btn);
    }

    QueueFragment queueFragment;

    public ShowEditItem(Context mContext, Bundle bundle, Product mProduct, int mPosition, Cart mCart, Bundle bundletp, LinearLayout taxlayoutobj) {
        this.mcontext = mContext;
        this.mProduct = mProduct;

        this.mPosition = mPosition;
        this.mCart = mCart;
        mDb = ProductDatabase.getInstance(mcontext);
        modifiers_temp = new ArrayList<>();
        modi_ids_ = new ArrayList<>();
        // combo_ids_ = new ArrayList<>();
        combo_ids = new ArrayList<>();
        //combo_pos = new ArrayList<>();
        combo_itemid = new ArrayList<>();
        combo_ids_temp = new ArrayList<>();
        combo_itemid_temp = new ArrayList<>();
        note_modifiers = new ArrayList<>();
        queueFragment = new QueueFragment();
        this.taxlayoutobj = taxlayoutobj;
        try {
            mCallback = (QueueInterface) mcontext;
        } catch (ClassCastException e) {
            throw new ClassCastException(mcontext.toString() + " must implement QueueInterface");
        }
        List<Product> modifiers_temp1 = new ArrayList<>();
        modifiers_temp1 = mProduct.modifiers;
        modifiers_temp = modifiers_temp1;
        if (modifiers_temp.size() > 0) {
            setModi_ids(modifiers_temp);
            setUpNoteModifiers(modifiers_temp);
        }
        DataBundle(bundle, bundletp);
        ShowDialogView();


    }

    public void setModi_ids(List<Product> modifiers_) {

        for (int i = 0; i < modifiers_.size(); i++) {
            //Log.e("Type check 1", "mid>>" + String.valueOf(modifiers_.get(i).id) + "<< combo id>>" + String.valueOf(modifiers_.get(i).combo_id));
            if (modifiers_.get(i).type_check == 0) {
                modi_ids_.add(String.valueOf(modifiers_.get(i).id));
            } else if (modifiers_.get(i).type_check == 1) {
                comboSelection.add(new ItemComoSelection(modifiers_.get(i).combo_id, modifiers_.get(i).id));
            }
        }
        if (comboSelection.size() > 0) {
            //CollectionCombo();
            CollectionComboTemp();
        }
    }

    public void setUpNoteModifiers(List<Product> nModifier) {
        for (int m = 0; m < nModifier.size(); m++) {
            if ((nModifier.get(m).modifierType == Product.MODIFIER_TYPE_DESC || nModifier.get(m).modifierType == Product.MODIFIER_TYPE_ADDON) && (nModifier.get(m).id == 0 && nModifier.get(m).combo_id == 0)) {
                Product item1 = new Product();
                item1.name = nModifier.get(m).name;
                item1.price = nModifier.get(m).price;
                item1.modifierType = nModifier.get(m).modifierType;
                note_modifiers.add(item1);
            }
        }
    }

    public boolean checkModiId(int id, int combo_id, String type) {
        boolean checkeditem = false;
        String mcid = String.valueOf(id);

        String comboid = String.valueOf(combo_id);
        //Log.d("mcid", "" + mcid);
        //Log.d("type", "" + type);
        if (modi_ids_.size() > 0) {

            if (modi_ids_.contains(mcid)) {
                int ik = modi_ids_.indexOf(mcid);
                modi_ids_.remove(ik);
                checkeditem = true;
            } else {
                modi_ids_.add(mcid);
                checkeditem = true;
            }
        } else {
            modi_ids_.add(mcid);
            checkeditem = true;
        }
        int posadapter = awesomePager.getCurrentItem();
        //Log.e("notified pos", "adapter pos>>" + posadapter);
        modifierAdapters_list.get(posadapter).notifyDataSetChanged();
        pm.notifyDataSetChanged();
        return checkeditem;
    }

    public boolean checkComboId(int id, int combo_id, String type) {

        boolean checkeditem = false;
        try {

            String mcid = String.valueOf(id);
            int itemid = id;
            String comboid = String.valueOf(combo_id);
            int isaddition = 0;
            for (int c = 0; c < comboSelection.size(); c++) {
                if (comboSelection.get(c).getComboid() == combo_id) {
                    if (comboSelection.get(c).getItemid() == itemid) {
                        isaddition = 1;
                        comboSelection.remove(c);
                        break;
                    } else {
                        comboSelection.remove(c);
                        break;
                    }
                }
            }
            if (isaddition == 0) {
                comboSelection.add(new ItemComoSelection(combo_id, itemid));
                checkeditem = true;
            }
            CollectionComboTemp();
            int posadapter = awesomePager.getCurrentItem();
            modifierAdapters_list.get(posadapter).notifyDataSetChanged();
            pm.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkeditem;
    }

    public void DataBundle(Bundle bundle, Bundle bundletp) {
        mNoteScope = bundle.getInt("noteScope");

        ArrayList<Product> products = (ArrayList<Product>) bundle.getSerializable("products");
        ArrayList<Category> categories = (ArrayList<Category>) bundle.getSerializable("categories");
        if (categories != null) {
            mCategories = (ArrayList<Category>) categories.clone();
        }
        mSelected = bundle.getInt("selected");
        mPrice = (BigDecimal) bundle.getSerializable("price");
        if (mPrice == null)
            mPrice = BigDecimal.ZERO;
        mNote = bundle.getString("note");

/* ------- tenpad       */
        mType = bundletp.getInt("type");
        try {
            if (bundletp.getSerializable("maxAmount") != null)
                mMaxValue = (BigDecimal) bundletp.getSerializable("maxAmount");
            else
                mMaxValue = new BigDecimal(MAX_AMOUNT);
            if (bundletp.getSerializable("price") != null)
                if (mType == TEN_PAD_TYPE_CASH)
                    mPricetp = (BigDecimal) bundletp.getSerializable("price");
                else
                    mValue = (BigDecimal) bundletp.getSerializable("price");
            mDiscountType = bundletp.getInt("discountType");
            mAttachAsFragment = bundletp.getBoolean(BUNDLE_ATTACH_AS_FRAGMENT);

            if (bundletp.getSerializable("cashier") != null) {
                mCashier = (Cashier) bundletp.getSerializable("cashier");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mMaxValue = new BigDecimal(MAX_AMOUNT);
        }

    }

    public void ShowDialogView() {
        try {
            dialog = new Dialog(mcontext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.fragment_item_edit_new);
            // dialog.setTitle("Custom Dialog");
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            modill = (LinearLayout) dialog.findViewById(R.id.modill);
            padll = (LinearLayout) dialog.findViewById(R.id.padll);
            notll = (LinearLayout) dialog.findViewById(R.id.notll);
            taxll = (LinearLayout) dialog.findViewById(R.id.taxll);
            awesomePager = (ViewPager) dialog.findViewById(R.id.pager);
            deletitem = (TextView) dialog.findViewById(R.id.delete_item);
            preitem = (TextView) dialog.findViewById(R.id.pre_item);
            nextitem = (TextView) dialog.findViewById(R.id.next_item);
            updateitem = (TextView) dialog.findViewById(R.id.update_item);
            canclebtn = (ImageView) dialog.findViewById(R.id.canclebtn);
            mItemNameTextView = (TextView) dialog.findViewById(R.id.item_name_text_view);
            mModifierButton = (ImageView) dialog.findViewById(R.id.item_add_modifier);
            mNoteButtonLayout = (LinearLayout) dialog.findViewById(R.id.item_add_note_ll);
            mModifierButtonLayout = (LinearLayout) dialog.findViewById(R.id.item_add_modifier_ll);
            mNoteButton = (ImageView) dialog.findViewById(R.id.item_add_note);
            mTaxButtonLayout = (LinearLayout) dialog.findViewById(R.id.item_tax_ll);
            mRemoveTaxButton = (ImageView) dialog.findViewById(R.id.item_remove_tax);
            mItemDiscountButton = (ImageView) dialog.findViewById(R.id.item_add_discount);
            ok_taxbutton = (LinearLayout) dialog.findViewById(R.id.ok_taxbutton);
            tax_msg = (TextView) dialog.findViewById(R.id.tax_msg);

            mItemNameTextView.setText(mProduct.name);
            mPreviousQty = mProduct.quantity;
            canclebtn.setOnClickListener(this);
            mModifierButton.setOnClickListener(this);
            mNoteButton.setOnClickListener(this);
            mRemoveTaxButton.setOnClickListener(this);
            mItemDiscountButton.setOnClickListener(this);
            ok_taxbutton.setOnClickListener(this);
            View view = dialog.getWindow().getDecorView();
            Typeface tf = Typeface.createFromAsset(this.mcontext.getAssets(), "fonts/NotoSans-Regular.ttf");
            Utils.setTypeFace(Typeface.createFromAsset(this.mcontext.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);

            mItemNameTextView.setTypeface(tf, Typeface.BOLD);
            deletitem.setTypeface(tf, Typeface.BOLD);
            preitem.setTypeface(tf, Typeface.BOLD);
            nextitem.setTypeface(tf, Typeface.BOLD);
            updateitem.setTypeface(tf, Typeface.BOLD);

            quantity_downbtn = (LinearLayout) dialog.findViewById(R.id.downbtn);
            quantity_upbtn = (LinearLayout) dialog.findViewById(R.id.upbtn);
            quantity_txt = (EditText) dialog.findViewById(R.id.qnty_txt);
            quantity_txt.setTypeface(tf, Typeface.BOLD);
            updateitem.setOnClickListener(this);
            bindUIElementsNote(dialog);
            setUpListenersNote();
            bindUIElementsTenPad(dialog);
            setUITenPad();
            setUpListenersTenPad();
            quantity_txt.setText("" + mProduct.quantity);
            preitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (awesomePager.getCurrentItem() != 0)
                        awesomePager.setCurrentItem(awesomePager.getCurrentItem() - 1);
                }
            });

            nextitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mProduct.combo == 0) {
                        if (awesomePager.getCurrentItem() < mdata_main.size())
                            awesomePager.setCurrentItem(awesomePager.getCurrentItem() + 1);
                    } else {
                        int posadapter = awesomePager.getCurrentItem();
                        int comboid = mdata_main.get(posadapter).get(0).getCombo_id();
                        if (comboid != 0) {
                            if (combo_ids_temp.contains("" + comboid)) {
                                if (awesomePager.getCurrentItem() < mdata_main.size())
                                    awesomePager.setCurrentItem(awesomePager.getCurrentItem() + 1);
                            } else {
                                Toast.makeText(mcontext, "Select any one Item.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (awesomePager.getCurrentItem() < mdata_main.size())
                                awesomePager.setCurrentItem(awesomePager.getCurrentItem() + 1);
                        }
                    }
                }
            });
            awesomePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (awesomePager.getCurrentItem() == 0) {
                        preitem.setVisibility(View.GONE);
                        deletitem.setVisibility(View.VISIBLE);
                        if (mdata_main.size() > 1) {
                            nextitem.setVisibility(View.VISIBLE);
                            updateitem.setVisibility(View.GONE);
                        } else {
                            preitem.setVisibility(View.GONE);
                            nextitem.setVisibility(View.GONE);
                            updateitem.setVisibility(View.VISIBLE);
                        }
                    } else {
                        deletitem.setVisibility(View.GONE);
                        if (awesomePager.getCurrentItem() == (mdata_main.size() - 1)) {
                            nextitem.setVisibility(View.GONE);
                            preitem.setVisibility(View.VISIBLE);
                            updateitem.setVisibility(View.VISIBLE);
                        } else {
                            preitem.setVisibility(View.VISIBLE);
                            nextitem.setVisibility(View.VISIBLE);
                            updateitem.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            deletitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    mNoteListener.onDelete();
                }
            });
            if (mProduct.combo == 0) {
                updateitem.setText("Update");
            } else {
                updateitem.setText("Add to Order");

            }
            quantity_downbtn = (LinearLayout) dialog.findViewById(R.id.downbtn);
            quantity_upbtn = (LinearLayout) dialog.findViewById(R.id.upbtn);
            quantity_txt = (EditText) dialog.findViewById(R.id.qnty_txt);
            quantity_downbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(quantity_txt.getText().toString().equalsIgnoreCase("") || Integer.valueOf(quantity_txt.getText().toString()) <= 0)) {
                        int quantity = Integer.valueOf(quantity_txt.getText().toString());
                        quantity--;
                        quantity_txt.setText("" + quantity);
                    }
                }
            });
            quantity_upbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int quantity = Integer.valueOf(quantity_txt.getText().toString());
                    quantity++;
                    quantity_txt.setText("" + quantity);


                }
            });

            quantity_txt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (s.toString().length() > 0) {
                            int qty = Integer.parseInt(s.toString());
                            if (qty == 1)
                                quantity_downbtn.setVisibility(View.INVISIBLE);
                            else if (qty == 99)
                                quantity_upbtn.setVisibility(View.INVISIBLE);
                            else {
                                quantity_downbtn.setVisibility(View.VISIBLE);
                                quantity_upbtn.setVisibility(View.VISIBLE);
                            }
                            if (TextUtils.isEmpty(quantity_txt.getText().toString().trim())) {
                            } else {
                                int quant = Integer.parseInt(quantity_txt.getText().toString().trim());
                                Product item = mProduct;
                                item.quantity = quant;
                                mCallback.onEditItem(mProduct, mPosition);
                            }
                        }
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            setModifierData();
            //awesomePager.setCurrentItem(0);
            if (awesomePager.getCurrentItem() == 0) {
                preitem.setVisibility(View.GONE);
                deletitem.setVisibility(View.VISIBLE);
                if (mdata_main.size() > 1) {
                    nextitem.setVisibility(View.VISIBLE);
                    updateitem.setVisibility(View.GONE);
                } else {
                    preitem.setVisibility(View.GONE);
                    nextitem.setVisibility(View.GONE);
                    updateitem.setVisibility(View.VISIBLE);
                }
            }
            WindowManager wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            Double width = metrics.widthPixels * .5;
            Double height = Double.parseDouble("" + metrics.heightPixels);
            if (ResourceSize() == 0) {
                width = metrics.widthPixels * .5;
                height = Double.parseDouble("" + metrics.heightPixels);
            } else {
                width = metrics.widthPixels * .36;
                height = metrics.heightPixels * .9;//Double.parseDouble("" + metrics.heightPixels);
            }
            Window win = dialog.getWindow();
            win.setLayout(width.intValue(), height.intValue());
            showView(0);
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int ResourceSize() {
        return mcontext.getResources().getInteger(R.integer.popuptype);
    }

    //mModifierButton,mNoteButton
    public void showView(int pos) {
        switch (pos) {
            case MODIFIER_BUTTON:
                modill.setVisibility(View.VISIBLE);
                padll.setVisibility(View.GONE);
                notll.setVisibility(View.GONE);
                taxll.setVisibility(View.GONE);
                mModifierButton.setImageResource(R.drawable.m_sel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mModifierButton, R.drawable.border_rec_fill);
                mModifierButton.setPadding(10, 10, 10, 10);
                mItemDiscountButton.setImageResource(R.drawable.ic_discount_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mItemDiscountButton, R.drawable.border_rect_blue);
                mNoteButton.setImageResource(R.drawable.n_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mNoteButton, R.drawable.border_rect_blue);
                mNoteButton.setPadding(10, 10, 10, 10);
                mRemoveTaxButton.setImageResource(R.drawable.ic_taxbtn_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mRemoveTaxButton, R.drawable.border_rect_blue);
                break;
            case ITEM_DISCOUNT_BUTTON:
                modill.setVisibility(View.GONE);
                padll.setVisibility(View.VISIBLE);
                notll.setVisibility(View.GONE);
                taxll.setVisibility(View.GONE);
                mModifierButton.setImageResource(R.drawable.m_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mModifierButton, R.drawable.border_rect_blue);
                mModifierButton.setPadding(10, 10, 10, 10);
                mItemDiscountButton.setImageResource(R.drawable.ic_discount_sel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mItemDiscountButton, R.drawable.border_rec_fill);
                mNoteButton.setImageResource(R.drawable.n_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mNoteButton, R.drawable.border_rect_blue);
                mNoteButton.setPadding(10, 10, 10, 10);
                mRemoveTaxButton.setImageResource(R.drawable.ic_taxbtn_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mRemoveTaxButton, R.drawable.border_rect_blue);
                break;
            case NOTE_BUTTON:
                modill.setVisibility(View.GONE);
                padll.setVisibility(View.GONE);
                notll.setVisibility(View.VISIBLE);
                taxll.setVisibility(View.GONE);
                // setUpSpinnerNote();
                // setUIsNote();
                mModifierButton.setImageResource(R.drawable.m_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mModifierButton, R.drawable.border_rect_blue);
                mModifierButton.setPadding(10, 10, 10, 10);
                mItemDiscountButton.setImageResource(R.drawable.ic_discount_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mItemDiscountButton, R.drawable.border_rect_blue);
                mNoteButton.setImageResource(R.drawable.n_sel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mNoteButton, R.drawable.border_rec_fill);
                mNoteButton.setPadding(10, 10, 10, 10);
                mRemoveTaxButton.setImageResource(R.drawable.ic_taxbtn_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mRemoveTaxButton, R.drawable.border_rect_blue);
                break;
            case TAX_BUTTON:
                modill.setVisibility(View.GONE);
                padll.setVisibility(View.GONE);
                notll.setVisibility(View.GONE);
                taxll.setVisibility(View.VISIBLE);
                if (mProduct.taxable) {
                    tax_msg.setText("Tax remov to the cost of an imported item.");
                } else {
                    tax_msg.setText("Tax add to the cost of an imported item.");
                }

                mModifierButton.setImageResource(R.drawable.m_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mModifierButton, R.drawable.border_rect_blue);
                mModifierButton.setPadding(10, 10, 10, 10);
                mItemDiscountButton.setImageResource(R.drawable.ic_discount_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mItemDiscountButton, R.drawable.border_rect_blue);
                mNoteButton.setImageResource(R.drawable.n_unsel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mNoteButton, R.drawable.border_rect_blue);
                mNoteButton.setPadding(10, 10, 10, 10);
                mRemoveTaxButton.setImageResource(R.drawable.ic_taxbtn_sel);
                com.pos.passport.ui.Utils.setBackgroundDrawable(mcontext, mRemoveTaxButton, R.drawable.border_rec_fill);
                break;
        }

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

    @Override
    public void onClick(View v) {
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.item_add_modifier:
                showView(0);
                break;
            case R.id.item_add_note:
                showView(2);
                break;
            case R.id.item_remove_tax:
                showView(3);

                break;
            case R.id.ok_taxbutton:
                mProduct.taxable = mProduct.taxable ? false : true;
                taxListener.onTaxshow(mProduct, mPosition);
                if (mProduct.taxable) {
                    // Utils.alertBox(mcontext, "Tax", "Tax added to the cost of an imported item.");
                    //Utils.alertBoxchange(mcontext, "Tax", "Tax added to the cost of an imported item.");
                    tax_msg.setText("Tax remov to the cost of an imported item.");
                } else {
                    //Utils.alertBox(mcontext, "Tax", "Tax removed to the cost of an imported item.");
                    //Utils.alertBoxchange(mcontext, "Tax", "Tax removed to the cost of an imported item.");

                    tax_msg.setText("Tax add to the cost of an imported item.");
                }
                break;
            case R.id.item_add_discount:
                showView(1);
                break;
            case R.id.canclebtn:
                dialog.cancel();
                break;
            case R.id.delete_item:
                break;
            case R.id.pre_item:
                break;
            case R.id.next_item:
                break;
            case R.id.update_item:
                ModifierDataSetUP(0);
                mCallback.onEditItem(mProduct, mPosition);
                dialog.cancel();
                break;
            default:
                break;
        }
    }

    public void ModifierDataSetUP(int check) {
        if (check == 0) {
            if (mProduct.combo != 0) {
                int posadapter = awesomePager.getCurrentItem();
                int comboid = mdata_main.get(posadapter).get(0).getCombo_id();
                if (comboid != 0) {
                    if (combo_ids_temp.contains("" + comboid)) {

                    } else {
                        Toast.makeText(mcontext, "Select any one Item.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        }
        int sizeget = modifiers_temp.size();
        if (mProduct.getModifiers().size() > 0)
            mProduct.getModifiers().clear();

        AddNoteModifiers();
        CollectionCombo();


        for (int i = 0; i < mdata_main.size(); i++) {
            List<Modifier> mmdata = mdata_main.get(i);

            for (int ik = 0; ik < mmdata.size(); ik++) {
                if (modi_ids_.contains("" + mmdata.get(ik).getId())) {
                    Product m = new Product();
                    m.id = mmdata.get(ik).getId();
                    m.name = mmdata.get(ik).getName();
                    m.modifierType = Product.MODIFIER_TYPE_ADDON;
                    m.price = mmdata.get(ik).getPrice();
                    m.cat = mmdata.get(ik).getCat();
                    m.cost = mmdata.get(ik).getCost();
                    m.combo_id = mmdata.get(ik).getCombo_id();
                    m.comboname = mmdata.get(ik).getTitle();
                    m.quantity = 1;
                    if (mmdata.get(ik).type.equalsIgnoreCase("combo"))
                        m.type_check = 1;
                    else
                        m.type_check = 0;

                    mProduct.addModifier(m);
                }
            }
        }

        AddCombo();
    }

    public void CollectionCombo() {
        combo_ids = new ArrayList<>();
        combo_itemid = new ArrayList<>();

        for (int s = 0; s < comboSelection.size(); s++) {
            combo_ids.add("" + comboSelection.get(s).getComboid());
            combo_itemid.add("" + comboSelection.get(s).getItemid());
        }
    }

    public void CollectionComboTemp() {
        combo_ids_temp = new ArrayList<>();
        combo_itemid_temp = new ArrayList<>();
        for (int s = 0; s < comboSelection.size(); s++) {
            combo_ids_temp.add("" + comboSelection.get(s).getComboid());
            combo_itemid_temp.add("" + comboSelection.get(s).getItemid());
        }
    }

    public void AddNoteModifiers() {
        for (int nm = 0; nm < note_modifiers.size(); nm++) {
            mProduct.addModifier(note_modifiers.get(nm));
        }

    }

    public void AddCombo() {

        for (int i = 0; i < mdata_main.size(); i++) {
            List<Modifier> mmdata = mdata_main.get(i);

            for (int ik = 0; ik < mmdata.size(); ik++) {
                if (combo_ids.contains("" + mmdata.get(ik).getCombo_id())) {
                    int itemindex = combo_ids.indexOf("" + mmdata.get(ik).getCombo_id());
                    int itemget = Integer.parseInt(combo_itemid.get(itemindex));

                    if (itemget == mmdata.get(ik).getId()) {
                        Product m = new Product();
                        m.id = mmdata.get(ik).getId();
                        m.name = mmdata.get(ik).getName();
                        m.modifierType = Product.MODIFIER_TYPE_ADDON;
                        m.price = mmdata.get(ik).getPrice();
                        m.cat = mmdata.get(ik).getCat();
                        m.cost = mmdata.get(ik).getCost();
                        m.combo_id = mmdata.get(ik).getCombo_id();
                        m.comboname = mmdata.get(ik).getTitle();
                        m.quantity = 1;
                        if (mmdata.get(ik).type.equalsIgnoreCase("combo"))
                            m.type_check = 1;
                        else
                            m.type_check = 0;

                        mProduct.addModifier(m);
                    }
                }
            }
        }
    }

    private void bindUIElementsNote(Dialog v) {
        mItemSpinner = (Spinner) v.findViewById(R.id.item_spinner);
        mNoteEditText = (EditText) v.findViewById(R.id.note_edit_text);
        mPriceButton = (Button) v.findViewById(R.id.price_button);
        mOkButton = (LinearLayout) v.findViewById(R.id.note_add_button);
        mAddviewCall = (LinearLayout) v.findViewById(R.id.note_ok_button);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);

        ll_list_note_modifiers = (LinearLayout) v.findViewById(R.id.ll_list_note_modifiers);
        ll_addnote = (LinearLayout) v.findViewById(R.id.ll_addnote);
        list_note_modifiers = (ListView) v.findViewById(R.id.list_note_modifiers);
        noteModifierAdapter = new NoteModifierAdapter(mcontext, note_modifiers);
        list_note_modifiers.setAdapter(noteModifierAdapter);
        noteModifierAdapter.notifyDataSetChanged();
        noteModifierAdapter.setListener(new NoteModifierAdapter.SetOnItemClick() {
            @Override
            public void onItemClick(int posg) {
                note_modifiers.remove(posg);
                noteModifierAdapter.notifyDataSetChanged();
                ModifierDataSetUP(1);
                mCallback.onEditItem(mProduct, mPosition);
            }
        });
        ll_list_note_modifiers.setVisibility(View.VISIBLE);
        ll_addnote.setVisibility(View.GONE);
        mOkButton.setVisibility(View.GONE);

    }

    private void setModifierData() {

        List<Modifier> mdata = new ArrayList<>();
        List<Modifier> combodata = new ArrayList<>();
        try {
            // Log.e("Modi data","at showitem >>"+mProduct.modi_data);
            if (mProduct.modi_data.equalsIgnoreCase("")) {
            } else {
                JSONArray modifidata = new JSONArray(mProduct.modi_data);
                for (int mm = 0; mm < modifidata.length(); mm++) {
                    mdata.add(parseModifier(modifidata.getJSONObject(mm), "Modifiers", 0));
                }
                mdata_main.add(mdata);
            }
            if (mProduct.combo == 0) {

            } else {

                JSONArray comd = new JSONArray(mProduct.comboItems);
                for (int cm = 0; cm < comd.length(); cm++) {
                    combodata = new ArrayList<>();
                    JSONObject datatemp = comd.getJSONObject(cm);
                    int com_id = Integer.parseInt(comd.getJSONObject(cm).getString("id"));
                    String com_name = comd.getJSONObject(cm).getString("name");
                    JSONArray items = comd.getJSONObject(cm).getJSONArray("items");
                    for (int ii = 0; ii < items.length(); ii++) {
                        combodata.add(parseCombo(items.getJSONObject(ii), com_name, com_id));
                    }
                    mdata_main.add(combodata);
                }

            }
            if (mdata_main.size() > 0) {
                pm = new GridViewPagerAdapter(mcontext, mdata_main);
                awesomePager.setAdapter(pm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Modifier parseModifier(JSONObject c, String title, int comid) {
        Modifier m = new Modifier();
        try {
            m.type = "modifiers";
            m.title = title;
            m.combo_id = comid;
            m.id = c.getInt("id");
            m.name = c.getString("name");
            m.desc = c.getString("description");
            m.barcode = c.getString("barcode");
            m.cat = c.getInt("department");
            m.price = new BigDecimal(c.getString("price"));
            m.cost = new BigDecimal(c.getString("cost"));
            m.quantity = c.getInt("quantity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    public static Modifier parseCombo(JSONObject c, String title, int comid) {
        Modifier m = new Modifier();
        try {
            m.type = "combo";
            m.title = title;
            m.combo_id = comid;
            m.id = c.getInt("itemId");
            m.name = c.getString("name");
            m.desc = c.getString("description");
            m.barcode = c.getString("barcode");
            m.cat = c.getInt("departmentId");
            m.price = new BigDecimal("0.0");
            m.cost = new BigDecimal("0.0");
            m.quantity = c.getInt("quantity");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    private void setUpSpinnerNote() {
        if (mNoteScope == NOTE_SCOPE_ALL) {
            if (mProducts == null)
                mProducts = new ArrayList<>();
            Product product = new Product();
            product.name = mcontext.getResources().getString(R.string.txt_all_items);
            mProducts.add(0, product);
        }
        if (mProducts != null) {
            ItemAdapter adapter = new ItemAdapter(mcontext, R.layout.view_spinner_item, mProducts);
            mItemSpinner.setAdapter(adapter);
            if (mNoteScope == NOTE_SCOPE_ITEM) {
                mItemSpinner.setSelection(mSelected);
                mItemSpinner.setEnabled(false);
            }
        } else if (mCategories != null) {
            CategoryAdapter adapter = new CategoryAdapter(mcontext, R.layout.view_spinner_item, mCategories);
            mItemSpinner.setAdapter(adapter);
            if (mNoteScope == NOTE_SCOPE_ADD_ITEM) {
                mItemSpinner.setSelection(mSelected);
            }
        } else {
            mItemSpinner.setVisibility(View.GONE);
        }
    }

    private void setUpListenersNote() {
        mItemSpinner.setOnItemSelectedListener(mItemSelectedListener);
        mNoteEditText.addTextChangedListener(mNoteWatcher);
        mPriceButton.setOnClickListener(mPriceClickListener);
        mOkButton.setOnClickListener(mOkClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
        mAddviewCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteEditable = false;
                SetUpView("", BigDecimal.ZERO);
            }
        });
        list_note_modifiers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                noteEditable = true;
                position_not_select = i;
                SetUpView(note_modifiers.get(i).name, note_modifiers.get(i).price);
            }
        });
    }

    private void SetUpView(String mNote, BigDecimal price_note) {
        ll_list_note_modifiers.setVisibility(View.GONE);
        ll_addnote.setVisibility(View.VISIBLE);
        mAddviewCall.setVisibility(View.GONE);
        mOkButton.setVisibility(View.VISIBLE);
        setUpSpinnerNote();
        setUIsNote(mNote, price_note);
    }

    private void setUIsNote(String mNote, BigDecimal mPrice1) {
        if (mProducts == null)
            mItemSpinner.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(mNote)) {
            mNoteEditText.setText(mNote);
            mNoteEditText.setSelection(mNote.length());
        }
        if (mPrice1 != null && mPrice1.compareTo(BigDecimal.ZERO) != 0) {
            //mPriceButton.setText(DecimalFormat.getCurrencyInstance().format(mPrice.divide(Consts.HUNDRED)));
            mPriceButton.setText(DecimalFormat.getCurrencyInstance().format(mPrice1));
            mPrice = mPrice1;
        }

        if (TextUtils.isEmpty(mNote))
            mOkButton.setEnabled(false);
        if (mCategories != null)
            mItemSpinner.setVisibility(View.VISIBLE);
    }

    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelected = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private TextWatcher mNoteWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                mOkButton.setEnabled(false);
            } else {
                mOkButton.setEnabled(true);
            }
        }
    };

    private View.OnClickListener mPriceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
         /*   TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_PRICE, mPrice);
            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
            fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
                @Override
                public void onSetPrice(BigDecimal amount) {
                    mPrice = amount;
                    mPriceButton.setText(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)));
                }
            });*/
            mNoteListener.CallFrag(mPrice, mPriceButton);
            //fragment.show(getActivity().getFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };


    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NoteValidator validator = validate();
            if (validator.resId != 0) {
                Utils.alertBox(mcontext, R.string.txt_add_note, validator.resId);
                validator.view.requestFocus();
                return;
            }
            Utils.dismissKeyboard(v);
            if (noteEditable) {
                //addNotetolist(position_not_select,mNoteEditText.getText().toString().trim(), mPrice);
                note_modifiers.get(position_not_select).name = mNoteEditText.getText().toString().trim();
                note_modifiers.get(position_not_select).price = mPrice.divide(Consts.HUNDRED);
                ModifierDataSetUP(1);
                mCallback.onEditItem(mProduct, mPosition);
                noteEditable = false;
            } else {
                mNoteListener.onNote(mSelected - 1, mNoteEditText.getText().toString().trim(), mPrice);
                noteEditable = false;
            }
            dialog.cancel();
        }
    };

    public void addNotetolist(String note, BigDecimal amount) {
        Product item1 = new Product();
        item1.name = note;
        BigDecimal amount1 = amount.divide(Consts.HUNDRED);
        item1.price = amount1;
        if (amount.compareTo(BigDecimal.ZERO) == 0)
            item1.modifierType = Product.MODIFIER_TYPE_DESC;
        else
            item1.modifierType = Product.MODIFIER_TYPE_ADDON;

        note_modifiers.add(item1);
    }

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            //Utils.dismissKeyboard(v);
            //dismiss();

            dialog.cancel();
        }
    };

    private class NoteValidator {
        private View view;
        private
        @StringRes
        int resId;
    }

    private NoteValidator validate() {
        NoteValidator validator = new NoteValidator();

        if (TextUtils.isEmpty(mNoteEditText.getText().toString().trim())) {
            validator.view = mNoteEditText;
            validator.resId = R.string.msg_add_note;
        }

        return validator;
    }

    public void setNoteListener(NoteListener l) {
        mNoteListener = l;
    }

    private class CategoryAdapter extends ArrayAdapter<Category> {
        private Context context;
        private
        @LayoutRes
        int resource;

        public CategoryAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mCategories != null) {
                Category category = mCategories.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(category.getName());
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mCategories != null) {
                Category category = mCategories.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(category.getName());
            }

            return view;
        }
    }

    private class ItemAdapter extends ArrayAdapter<Product> {
        private Context context;
        private
        @LayoutRes
        int resource;

        public ItemAdapter(Context context, int resource, List<Product> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mProducts != null) {
                Product product = mProducts.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(product.name);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mProducts != null) {
                Product product = mProducts.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(product.name);
            }

            return view;
        }
    }

    public class GridViewPagerAdapter extends PagerAdapter {

        private Context _activity;
        private List<List<Modifier>> listingdata;
        private LayoutInflater inflater;
        public ModifierAdapter mModifierAdapter;

        // constructor
        public GridViewPagerAdapter(Context activity,
                                    List<List<Modifier>> listingdata) {
            this._activity = activity;
            this.listingdata = listingdata;
        }

        @Override
        public int getCount() {
            //Log.e("g view size",">>>>"+listingdata.size());
            return listingdata.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            GridView mModifierGridView;
            TextView texttile;
            Button btnClose;

            inflater = (LayoutInflater) _activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.view_grid_new, container, false);

            mModifierGridView = (GridView) viewLayout.findViewById(R.id.modifier_grid_view);
            texttile = (TextView) viewLayout.findViewById(R.id.texttile);
            List<Modifier> listdata = listingdata.get(position);
            if (listdata.size() > 0)
                texttile.setText(listdata.get(0).getTitle());

            mModifierAdapter = new ModifierAdapter(listdata);
            mModifierGridView.setAdapter(mModifierAdapter);
            saveAdater(mModifierAdapter);
            modifierAdapters_list.add(mModifierAdapter);
            mModifierAdapter.notifyDataSetChanged();


            ((ViewPager) container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((LinearLayout) object);
        }

        public void saveAdater(ModifierAdapter mModifierAdapter) {
            this.mModifierAdapter = mModifierAdapter;
        }

        public void getmModifierAdapter() {
            this.mModifierAdapter.notifyDataSetChanged();
        }
    }

    public class ModifierAdapter extends BaseAdapter {
        public List<Modifier> mModifiersList;

        public ModifierAdapter(List<Modifier> mModifiersList) {
            this.mModifiersList = mModifiersList;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mcontext).inflate(R.layout.view_text_checkbox_new, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Modifier dataget = (Modifier) getItem(position);
            String name = dataget.getName();
            int id = dataget.getId();
            int idc = dataget.getCombo_id();

            holder.textView.setText(name);
            if (modi_ids_.contains("" + id) && idc == 0) {
                holder.checkBox.setVisibility(View.VISIBLE);
                convertView.setBackgroundColor(Color.parseColor("#fffcd5"));
            } else if (combo_ids_temp.contains("" + idc) && combo_itemid_temp.contains("" + id)) {
                holder.checkBox.setVisibility(View.VISIBLE);
                convertView.setBackgroundColor(Color.parseColor("#fffcd5"));
            } else {
                holder.checkBox.setVisibility(View.INVISIBLE);
                convertView.setBackgroundColor(Color.WHITE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id1 = dataget.getId();
                    int cid1 = dataget.getCombo_id();
                    String typeget = dataget.type;

                    if (typeget.equalsIgnoreCase("combo"))
                        checkComboId(id1, cid1, typeget);
                    else
                        checkModiId(id1, cid1, typeget);
                }
            });
            return convertView;
        }

        public int dpToPixel(float dp) {
            return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
        }

        public final int getCount() {
            return mModifiersList.size();
        }

        public Object getItem(int position) {
            return mModifiersList.get(position);
        }

        public long getItemId(int position) {
            return position;
            //return mModifiersList.get(position).getId();
        }

        private class ViewHolder {
            TextView textView;
            ImageView checkBox;

            public ViewHolder(View row) {
                textView = (TextView) row.findViewById(R.id.item_text_view);
                checkBox = (ImageView) row.findViewById(R.id.item_check_box);
            }

        }
    }

    public ArrayList<Modifier> getCheckedItems() {

        ArrayList<Modifier> selectedList = new ArrayList<>();
        for (int i = 0; i < mdata_main.size(); i++) {
            List<Modifier> mmdata = mdata_main.get(i);

            if (mSparseBooleanArray.get(i)) {
                selectedList.add(mmdata.get(i));
            }
        }
        return selectedList;
    }

    public class CheckableLayout extends LinearLayout implements Checkable {
        private boolean mChecked;

        public CheckableLayout(Context context) {
            super(context);
            setOnClickListener(null);
        }

        @Override
        public void setChecked(boolean checked) {
            int id = getId();
            mSparseBooleanArray.put(id, checked);
            mChecked = checked;
            if (checked) {
                ((LinearLayout) getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
            } else {
                ((LinearLayout) getChildAt(0)).getChildAt(1).setVisibility(View.INVISIBLE);
            }
        }

        public boolean isChecked() {
            return mChecked;
        }

        @Override
        public boolean performClick() {
            toggle();
            return super.performClick();
        }

        @Override
        public void toggle() {
            setChecked(!mChecked);
        }

    }

    public int GetMofiViewId(int id) {
        int setid = 0;
        List<Modifier> data = mdata_main.get(awesomePager.getCurrentItem());
        setid = data.get(id).getId();
        return setid;
    }
/*------------------------------------------- Ten Pad-------------------------------------*/

    private View.OnClickListener mCancelClickListenerTP = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           /* if(!mAttachAsFragment)
                dismiss();
            else
                try {
                    ((QueueInterface) mCallback).onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                }catch (ClassCastException e){
                    e.printStackTrace();
                }*/
            dialog.cancel();
        }
    };

    private View.OnClickListener mTitleBarCloseButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                ((QueueInterface) mCallback).onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mValue == null)
                return;
            if (mValue.toString().length() > 1) {
                mValue = new BigDecimal(mValue.toString().substring(0, mValue.toString().length() - 1));
                showDisplay();
            } else if (mValue.toString().length() == 1) {
                setValue(BigDecimal.ZERO);
                showDisplay();
            }
        }
    };

    private View.OnClickListener mOkClickListenerTP = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mType == TEN_PAD_TYPE_ADMIN) {
                if (mValue != null && mValue.toString().equals(AdminSetting.password)) {
                    mTenPadListener.onAdminAccessGranted();
                } else {
                    mDisplayTextView.setError(getString(R.string.txt_invalid_pin));
                    return;
                    //mTenPadListener.onAdminAccessDenied();
                }
            } else if (mType == TEN_PAD_TYPE_LOGIN) {
                if (mValue != null && mValue.toString().equals(mCashier.pin)) {
                    mTenPadListener.onAdminAccessGranted();
                } else {
                    mDisplayTextView.setError(getString(R.string.txt_invalid_pin));
                    return;
                    //mTenPadListener.onAdminAccessDenied();
                }
            } else if (mType == TEN_PAD_TYPE_DISCOUNT) {
                if (mValue != null) {
                    Log.e("Max value", "Vale>>>" + mValue);
                    if (mAmountRadioButton.isChecked()) {
                        mDiscountListener.onDiscountPrice(mValue);
                    } else {
                        mDiscountListener.onDiscountPercent(mValue);
                    }
                }
            } else {
                if (mValue != null) {
                    if (mType == TEN_PAD_TYPE_CASH || mType == TEN_PAD_TYPE_PRICE) {
                        if (mValue.compareTo(BigDecimal.ZERO) == 0) {
                            mDisplayTextView.setError(getString(R.string.txt_error_valid_amount));
                            return;
                        }
                        mPriceListener.onSetPrice(mValue);
                    }

                }
            }

            /*if(!mAttachAsFragment)
                dismiss();*/
            dialog.cancel();
        }
    };

    private RadioGroup.OnCheckedChangeListener mDiscountCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.ten_pad_percent_radio_button) {
                if (mValue.compareTo(Consts.HUNDRED) == 1)
                    setValue(BigDecimal.ZERO);
                mPercentRadioButton.setTextColor(Color.WHITE);
                mAmountRadioButton.setTextColor(Utils.getColor(mcontext, R.color.pos_secondary_blue));
                showDisplay();
            } else {
                mPercentRadioButton.setTextColor(Utils.getColor(mcontext, R.color.pos_secondary_blue));
                mAmountRadioButton.setTextColor(Color.WHITE);
                showDisplay();
            }
        }
    };

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String s = ((Button) v).getText().toString();

            if (mType == TEN_PAD_TYPE_DISCOUNT && mAmountRadioButton.isChecked()) {
                Log.d("Get text", "amount value " + s);
                switch (s) {

                    case ".":
                        if (mValue.compareTo(Consts.HUNDRED) == -1) {
                            mValue = mValue.multiply(Consts.HUNDRED);
                            //mValue = mValue;//.multiply(Consts.HUNDRED);
                        }
                        break;

                    default:
                        if (mValue.toString().endsWith("00"))
                            mValue = new BigDecimal(mValue.toString().replace("00", s + "0"));
                        else if (mValue.toString().endsWith("0"))
                            mValue = new BigDecimal(mValue.toString().substring(0, mValue.toString().length() - 1) + s);
                        else
                            mValue = new BigDecimal(mValue.toString() + s);

                        Log.d("final mvalue", " mvalue>>>" + mValue);
                }
            } else {
                switch (s) {
                    case ".":
                        if (!mValue.toString().contains(".")) {
                            if (mType == TEN_PAD_TYPE_DISCOUNT) {
                                mValue = new BigDecimal(mValue + s + "0");
                            } else
                                mValue = new BigDecimal(mValue + s);
                        }
                        break;

                    case "00":
                        if (!mValue.toString().endsWith("0"))
                            mValue = new BigDecimal(mValue + s);
                        break;

                    default:
                        if (!mValue.toString().equals("0")) {
                            if (mValue.toString().endsWith(".0"))
                                mValue = new BigDecimal(mValue.toString().replace(".0", ".") + s);
                            else if (getNumberOfFractionDigits(mValue) < 2)
                                mValue = new BigDecimal(mValue + s);
                        } else
                            mValue = new BigDecimal(s);
                }
            }

            showDisplay();
        }
    };

    private void bindUIElementsTenPad(Dialog v) {
        mTenPadLayout = (LinearLayout) v.findViewById(R.id.ten_pad_layout);
        mTitleLayout = (LinearLayout) v.findViewById(R.id.ten_pad_title_layout);
        mTitleTextView = (TextView) v.findViewById(R.id.ten_pad_title_text_view);
        mDisplayTextView = (TextView) v.findViewById(R.id.display_text_view);
        mDiscountRadioGroup = (RadioGroup) v.findViewById(R.id.ten_pad_discount_radio_group);
        mAmountRadioButton = (RadioButton) v.findViewById(R.id.ten_pad_amount_radio_button);
        mPercentRadioButton = (RadioButton) v.findViewById(R.id.ten_pad_percent_radio_button);
        mButtons = new ArrayList<>();
        Button button1 = (Button) v.findViewById(R.id.ten_pad_1_button);
        Button button2 = (Button) v.findViewById(R.id.ten_pad_2_button);
        Button button3 = (Button) v.findViewById(R.id.ten_pad_3_button);
        Button button4 = (Button) v.findViewById(R.id.ten_pad_4_button);
        Button button5 = (Button) v.findViewById(R.id.ten_pad_5_button);
        Button button6 = (Button) v.findViewById(R.id.ten_pad_6_button);
        Button button7 = (Button) v.findViewById(R.id.ten_pad_7_button);
        Button button8 = (Button) v.findViewById(R.id.ten_pad_8_button);
        Button button9 = (Button) v.findViewById(R.id.ten_pad_9_button);
        mButton0 = (Button) v.findViewById(R.id.ten_pad_0_button);
        Button button00 = (Button) v.findViewById(R.id.ten_pad_00_button);
        Button buttondot = (Button) v.findViewById(R.id.ten_pad_dot_button);
        mButtons.add(button1);
        mButtons.add(button2);
        mButtons.add(button3);
        mButtons.add(button4);
        mButtons.add(button5);
        mButtons.add(button6);
        mButtons.add(button7);
        mButtons.add(button8);
        mButtons.add(button9);
        mButtons.add(mButton0);
        mButtons.add(button00);
        mButtons.add(buttondot);
        mDeleteImageButton = (ImageButton) v.findViewById(R.id.ten_pad_delete_image_button);
        mCancelButtontenpad = (Button) v.findViewById(R.id.ten_pad_cancel_button);
        mCancelButtontenpad.setVisibility(View.GONE);
        mOkButtontenpad = (LinearLayout) v.findViewById(R.id.ten_pad_ok_button_new);
        mBottomlayount = (LinearLayout) v.findViewById(R.id.btn_ll);
        mBottomlayount.setVisibility(View.GONE);
        // mOkButtontenpad_btn = (Button) v.findViewById(R.id.ten_pad_ok_button);
        //mOkButtontenpad_btn.setVisibility(View.GONE);
    }

    private void setUITenPad() {
        if (mType == TEN_PAD_TYPE_DISCOUNT) {
            mTitleTextView.setText(R.string.txt_item_discount);
            mDiscountRadioGroup.setVisibility(View.VISIBLE);
            if (mDiscountType == Product.MODIFIER_TYPE_DISCOUNT_AMOUNT) {
                mPercentRadioButton.setVisibility(View.GONE);
                mAmountRadioButton.setChecked(true);
                mAmountRadioButton.setTextColor(Color.WHITE);
                mAmountRadioButton.setBackgroundColor(Utils.getColor(mcontext, R.color.pos_secondary_blue));
            } else if (mDiscountType == Product.MODIFIER_TYPE_DISCOUNT_PERCENT) {
                mAmountRadioButton.setVisibility(View.GONE);
                mPercentRadioButton.setChecked(true);
                mPercentRadioButton.setTextColor(Color.WHITE);
                mPercentRadioButton.setBackgroundColor(Utils.getColor(mcontext, R.color.pos_secondary_blue));
            }
            //mOkButtontenpad.setText(R.string.txt_add);
            // mOkButtontenpad.setText(R.string.txt_add_discount);
            // mOkButtontenpad.setPadding(80,13,80,13);

        } else if (mType == TEN_PAD_TYPE_ADMIN) {
            mTitleTextView.setText(R.string.txt_admin_login);
            mDiscountRadioGroup.setVisibility(View.GONE);
        } else if (mType == TEN_PAD_TYPE_LOGIN) {
            mTitleTextView.setText(mCashier.name);
            mDiscountRadioGroup.setVisibility(View.GONE);
        } else {
            mTitleTextView.setText(R.string.txt_amount);
            mDiscountRadioGroup.setVisibility(View.VISIBLE);
            mPercentRadioButton.setVisibility(View.GONE);
            if (mType == TEN_PAD_TYPE_CASH) {
                //setCancelable(false);
                // mOkButtontenpad.setText(R.string.txt_pay);
            }
        }
        initValue();
        showDisplay();
    }

    private void setUpListenersTenPad() {
        mDeleteImageButton.setOnClickListener(mDeleteClickListener);
        mCancelButtontenpad.setOnClickListener(mCancelClickListenerTP);
        mOkButtontenpad.setOnClickListener(mOkClickListenerTP);
        mDiscountRadioGroup.setOnCheckedChangeListener(mDiscountCheckedChangeListener);
        for (Button button : mButtons) {
            button.setOnClickListener(mButtonClickListener);
        }
    }

    private void initValue() {
        //Log.d("Mvalue initValue", "at init>>>>>" + mValue);
        if (mValue == null || mValue.compareTo(BigDecimal.ZERO) == 0)
            mValue = BigDecimal.ZERO;
    }

    private void setValue(BigDecimal value) {
        mValue = value;
    }

    private void showDisplay() {
        //Log.d("Mvalue showdisplay", "showDisplay()>>>>>" + mValue);
        if (mType == TEN_PAD_TYPE_DISCOUNT) {
            if (mAmountRadioButton.isChecked()) {
                if (mValue.compareTo(mMaxValue) == 1)
                    mValue = mMaxValue;
                mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));
            } else {
                if (mValue.compareTo(Consts.HUNDRED) == 1)
                    mValue = Consts.HUNDRED;
                mDisplayTextView.setText(String.format("%s%%", mValue.toString()));
            }
        } else if (mType == TEN_PAD_TYPE_PRICE || mType == TEN_PAD_TYPE_CASH) {
            if (mValue.compareTo(mMaxValue) == 1)
                mValue = mMaxValue;
            mDisplayTextView.setText(DecimalFormat.getCurrencyInstance().format(mValue.divide(Consts.HUNDRED)));

        } else {
            mDisplayTextView.setText(mValue.toString());
        }
    }

    private int getNumberOfFractionDigits(BigDecimal number) {
        String digits[] = number.toString().split("\\.");
        if (digits.length < 2)
            return 0;

        return digits[1].length();
    }

    public void setTenPadListener(TenPadListener l) {
        mTenPadListener = l;
    }

    public void setDiscountListener(DiscountListener l) {
        mDiscountListener = l;
    }

    public void setPriceListener(PriceListener l) {
        mPriceListener = l;
    }

    public void setTaxListener(TaxListener l) {
        taxListener = l;
    }
}
