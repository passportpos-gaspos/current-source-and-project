package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Modifier;
import com.pos.passport.model.Product;
import com.pos.passport.ui.AutoFitTextView;
import com.pos.passport.util.Consts;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 4/29/2016.
 */
public class EditItemFragment extends DialogFragment {

    public static String TAG_EDIT_ITEM_FRAGMENT = "tag_edit_item_fragment";

    private QueueInterface mCallback;
    private ProductDatabase mDb;

    private TextView mBackTextView;
    private TextView mItemNameTextView;
    private ImageView mItemImageView;
    private Button mMinusButton;
    private Button mPlusButton;
    private EditText mQuantityEditText;
    private Button mSetQuantityButton;
    private ImageView mModifierButton;
    private ImageView mNoteButton;
    private ImageView mRemoveTaxButton;
    private ImageView mItemDiscountButton;
    private GridView mModifierGridView;
    private LinearLayout mGridLayout;
    private Button mAddButton;

    private boolean mBlockEdit = false;
    private int mPreviousQty;
    private Product mProduct;
    private int mPosition;
    private Cart mCart;
    private List<Modifier> mModifiersList=new ArrayList<>();
    private SparseBooleanArray mSparseBooleanArray;
    private ModifierAdapter mModifierAdapter;
    private static final String TAG_FRAGMENT_BUTTON = "EditItemFragment";
    FrameLayout content_fragment;
   // LinearLayout notll,tenpagll;
   private ViewPager mEditViewPager;
   //private ViewPagerAdapter mViewPagerAdapter;

    private View.OnClickListener mBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
        }
    };

    private View.OnClickListener mMinusButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mQuantityEditText.getText().toString().equals("")) {
                mBlockEdit = true;
                mQuantityEditText.setText("1");
                mBlockEdit = false;
            }

            int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
            quantity--;
            mBlockEdit = true;
            mQuantityEditText.setText(String.format("%d", quantity));
            mBlockEdit = false;
            mProduct.quantity = quantity;
            mCallback.onEditItem(mProduct, mPosition);
        }
    };

    private View.OnClickListener mPlusButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mQuantityEditText.getText().toString().equals("")) {
                mBlockEdit = true;
                mQuantityEditText.setText("1");
                mBlockEdit = false;
            }

            int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
            quantity++;
            mBlockEdit = true;
            mQuantityEditText.setText(String.format("%d", quantity));
            mBlockEdit = false;
            mProduct.quantity = quantity;
            mCallback.onEditItem(mProduct, mPosition);
        }
    };

    private View.OnClickListener mSetQuantityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener mModifierClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v)
        {
            content_fragment.setVisibility(View.GONE);
            setUpGridView();
            mGridLayout.setVisibility(View.VISIBLE);
            /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            ft.replace(R.id.content_fragment, newFragment);*/
        }
    };

    private View.OnClickListener mNoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            content_fragment.setVisibility(View.VISIBLE);
           // notll.setVisibility(View.VISIBLE);
            mGridLayout.setVisibility(View.GONE);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            /*NoteFragment fragment = new NoteFragment();
            Bundle args = new Bundle();
            args.putInt("noteScope", NoteFragment.NOTE_SCOPE_ITEM);
            args.putSerializable("products", mCart.getProducts());
            args.putInt("selected", mPosition);
            fragment.setArguments(args);*/
            NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ITEM, mCart.getProducts(), mPosition);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
           // ft.add(R.id.content_fragment_edit, fragment);
            transaction.add(fragment, "CustomTag");
            transaction.commit();
            //ft.addToBackStack(null);
            //ft.commit();
           // fragment.show(getChildFragmentManager(), TAG_EDIT_ITEM_FRAGMENT);
            fragment.setNoteListener(new NoteFragment.NoteListener() {
                @Override
                public void onNote(int position, String note, BigDecimal amount)
                {
                    Product m = new Product();
                    m.name = note;
                    m.price = amount;
                    if (amount.compareTo(BigDecimal.ZERO) == 0)
                        m.modifierType = Product.MODIFIER_TYPE_DESC;
                    else
                        m.modifierType = Product.MODIFIER_TYPE_ADDON;

                    mProduct.addModifier(m);
                    mCallback.onEditItem(mProduct, mPosition);
                }

                @Override
                public void onDelete() {
                }
            });
        }
    };

    private View.OnClickListener mRemoveTaxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mProduct.taxable = mProduct.taxable ? false : true;
            setUpUi();
            mCallback.onEditItem(mProduct, mPosition);
        }
    };

    private View.OnClickListener mItemDiscountClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            content_fragment.setVisibility(View.VISIBLE);
            //tenpagll.setVisibility(View.VISIBLE);
            mGridLayout.setVisibility(View.GONE);
            if(PrefUtils.getCashierInfo(getActivity()).permissionPriceModify)
            {

                if (mProduct.discountAmount.compareTo(BigDecimal.ZERO) > 0){
                    updateItemDiscount();
                    return;
                }
                addItemDiscount();
                return;
            }
            /*TenPadFragment f = new TenPadFragment();

            Bundle args = new Bundle();
            args.putInt("type", TenPadFragment.TEN_PAD_TYPE_ADMIN);
            if (mProduct.price != null) {
                if (TenPadFragment.TEN_PAD_TYPE_ADMIN == 1)
                    args.putSerializable("maxAmount", mProduct.price);
                else if (TenPadFragment.TEN_PAD_TYPE_ADMIN == 2 || TenPadFragment.TEN_PAD_TYPE_ADMIN == 3)
                    args.putSerializable("price", mProduct.price);
            }
            f.setArguments(args);
            f.setTenPadListener(new TenPadFragment.TenPadListener() {
                @Override
                public void onAdminAccessGranted() {
                    addItemDiscount();
                }

                @Override
                public void onAdminAccessDenied() {

                }
            });*/
            TenPadFragment newFragment = TenPadFragment.newInstance(TenPadFragment.TEN_PAD_TYPE_ADMIN, mProduct.price);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            //ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            ft.replace(R.id.content_fragment_edit, newFragment);
            ft.addToBackStack(null);
            ft.commit();
           // newFragment.show(getChildFragmentManager(), TAG_EDIT_ITEM_FRAGMENT);
            newFragment.setTenPadListener(new TenPadFragment.TenPadListener() {
                @Override
                public void onAdminAccessGranted() {
                    addItemDiscount();
                }

                @Override
                public void onAdminAccessDenied() {

                }
            });
        }
    };

    private class QuantityTextChangeListener implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            {
                try {
                    int qty = Integer.parseInt(s.toString());
                    /*if (qty == 1)
                        mMinusButton.setVisibility(View.INVISIBLE);
                    else if (qty == 99)
                        mPlusButton.setVisibility(View.INVISIBLE);
                    else {
                        mMinusButton.setVisibility(View.VISIBLE);
                        mPlusButton.setVisibility(View.VISIBLE);
                    }*/
                    if (!mBlockEdit) {
                        mPreviousQty = qty;
                        mProduct.quantity = qty;
                        mCallback.onEditItem(mProduct, mPosition);
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private View.OnClickListener mAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGridLayout.setVisibility(View.GONE);
            ArrayList<Modifier> selectedList = mModifierAdapter.getCheckedItems();
            for(Modifier c : selectedList){
                Product m = new Product();
                m.id = c.id;
                m.name = c.getName();
                m.modifierType = Product.MODIFIER_TYPE_ADDON;
                m.price = c.getPrice();
                m.cat = c.getCat();
                m.cost = c.getCost();
                mProduct.addModifier(m);
            }
            mCallback.onEditItem(mProduct, mPosition);
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_edit, container, false);
        mEditViewPager = (ViewPager) view.findViewById(R.id.edit_view_pager);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) mEditViewPager);
        /*Fragment newFragment = new NoteFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content_fragment, newFragment, TAG_FRAGMENT_BUTTON).commit();*/
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        mProduct = (Product)getArguments().getSerializable("item");
        mPosition = getArguments().getInt("position");
        mCart = (Cart) getArguments().getSerializable("cart");
        content_fragment=(FrameLayout)view.findViewById(R.id.content_fragment_edit);
        content_fragment.setVisibility(View.GONE);
        bindUIElements(view);
        setUpUi();
        setUpListeners();
        setUpGridView();
        //setupViewPager();
       // mGridLayout.setVisibility(View.VISIBLE);

    }

    /*public static EditItemFragment newInstance( int noteScope, ArrayList<Product> products, int selected) {
        NoteFragment f = new NoteFragment();

        Bundle args = new Bundle();
        args.putInt("noteScope", noteScope);
        args.putSerializable("products", products);
        args.putInt("selected", selected);
        f.setArguments(args);

        return f;
    }*/
    private void bindUIElements(View v){
        mBackTextView = (TextView) v.findViewById(R.id.back_text_view);
        mItemNameTextView = (TextView) v.findViewById(R.id.item_name_text_view);
        mItemImageView = (ImageView) v.findViewById(R.id.item_image_view);
        mMinusButton = (Button) v.findViewById(R.id.item_quantity_minus_button);
        mPlusButton = (Button) v.findViewById(R.id.item_quantity_plus_button);
        mQuantityEditText = (EditText)  v.findViewById(R.id.item_quantity_edit_text);
        mModifierButton = (ImageView) v.findViewById(R.id.item_add_modifier);
        mSetQuantityButton = (Button) v.findViewById(R.id.item_set_quantity);
        mNoteButton = (ImageView) v.findViewById(R.id.item_add_note);
        mRemoveTaxButton = (ImageView) v.findViewById(R.id.item_remove_tax);
        mItemDiscountButton = (ImageView) v.findViewById(R.id.item_add_discount);
        mModifierGridView = (GridView) v.findViewById(R.id.modifier_grid_view);
        mGridLayout = (LinearLayout) v.findViewById(R.id.grid_layout);
        mAddButton = (Button) v.findViewById(R.id.add_button);


      //  notll=(LinearLayout)v.findViewById(R.id.notll);
      //  tenpagll=(LinearLayout)v.findViewById(R.id.tenpadll);


    }

    private void setUpListeners(){
        mBackTextView.setOnClickListener(mBackClickListener);
        mMinusButton.setOnClickListener(mMinusButtonClickListener);
        mPlusButton.setOnClickListener(mPlusButtonClickListener);
        mSetQuantityButton.setOnClickListener(mSetQuantityClickListener);
        mModifierButton.setOnClickListener(mModifierClickListener);
        mNoteButton.setOnClickListener(mNoteClickListener);
        mRemoveTaxButton.setOnClickListener(mRemoveTaxClickListener);
        mItemDiscountButton.setOnClickListener(mItemDiscountClickListener);
        mQuantityEditText.addTextChangedListener(new QuantityTextChangeListener());
        mAddButton.setOnClickListener(mAddButtonClickListener);
        mQuantityEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.equals(mQuantityEditText) && !hasFocus && !mBlockEdit) {
                    if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                        mProduct.quantity = mPreviousQty;
                        mCallback.onEditItem(mProduct, mPosition);
                    }
                }
            }
        });
    }

    private void setUpUi(){
        mPreviousQty = mProduct.quantity;
        mQuantityEditText.setText(String.format("%d", mProduct.quantity));
        mItemNameTextView.setText(mProduct.name);
        mItemImageView.setVisibility(View.GONE);
        mSetQuantityButton.setVisibility(View.INVISIBLE);
       // mRemoveTaxButton.setText(getResources().getString(R.string.txt_remove_tax));
        mGridLayout.setVisibility(View.GONE);
        if(!mProduct.taxable) {
            //   mRemoveTaxButton.setText(getResources().getString(R.string.txt_add_tax));
        }
        //if(mProduct.quantity == 1)
        //    mMinusButton.setVisibility(View.INVISIBLE);
    }

    private void setUpGridView(){
        mModifiersList = mDb.getModifiers();
        mModifierAdapter = new ModifierAdapter();
        mModifierGridView.setAdapter(mModifierAdapter);
        mModifierGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mSparseBooleanArray = new SparseBooleanArray();
    }

    public class ModifierAdapter extends BaseAdapter {
        public ModifierAdapter() {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            CheckableLayout l = new CheckableLayout(getActivity());;
            AutoFitTextView textView;
            ImageView checkBox;
            String name = mModifiersList.get(position).getName();
            int id = position;

            if(convertView == null){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.view_text_checkbox, null);
                textView = (AutoFitTextView) convertView.findViewById(R.id.item_text_view);
                checkBox = (ImageView) convertView.findViewById(R.id.item_check_box);

                l.setLayoutParams(new GridView.LayoutParams(
                        GridView.LayoutParams.WRAP_CONTENT,
                        GridView.LayoutParams.WRAP_CONTENT));
                l.addView(convertView);
            }else {
                l = (CheckableLayout) convertView;
                FrameLayout f = (FrameLayout) l.getChildAt(0);
                textView = (AutoFitTextView) f.getChildAt(0);
                checkBox = (ImageView) f.getChildAt(1);
            }
            l.forceLayout();
            l.setPadding(0,0,0,20);
            l.setId(position);
            textView.setText(name);

            return l;
        }

        public int dpToPixel(float dp) {
            return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
        }

        public final int getCount() {
            return mModifiersList.size();
        }

        public final Object getItem(int position) {
            return mModifiersList.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }

        public ArrayList<Modifier> getCheckedItems(){
            ArrayList<Modifier> selectedList = new ArrayList<>();
            for(int i=0;i<mModifiersList.size();i++) {
                if(mSparseBooleanArray.get(i)){
                    selectedList.add(mModifiersList.get(i));
                }
            }
            return selectedList;
        }
    }

    public class CheckableLayout extends FrameLayout implements Checkable {
        private boolean mChecked;

        public CheckableLayout(Context context) {
            super(context);
            setOnClickListener(null);
        }

        @Override
        public void setChecked(boolean checked) {
            int id  = getId();
            mSparseBooleanArray.put(id, checked);
            mChecked = checked;
            if(checked) {
                ((FrameLayout)getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
            }
            else {
                ((FrameLayout)getChildAt(0)).getChildAt(1).setVisibility(View.INVISIBLE);
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

    private void addItemDiscount() {
        TenPadFragment newFragment = TenPadFragment.newInstance(TenPadFragment.TEN_PAD_TYPE_DISCOUNT, mProduct.totalPrice(mCart.mDate).multiply(Consts.HUNDRED));
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.content_fragment_edit, newFragment);
        ft.addToBackStack(null);
        ft.commit();
       // newFragment.show(getChildFragmentManager(), TAG_EDIT_ITEM_FRAGMENT);
        newFragment.setDiscountListener(new TenPadFragment.DiscountListener() {
            @Override
            public void onDiscountPrice(BigDecimal amount) {

                mProduct.discountName = getString(R.string.txt_discount);
                mProduct.discountAmount = amount;
                mProduct.modifierType = amount.compareTo(BigDecimal.ZERO) > 0 ? Product.MODIFIER_TYPE_DISCOUNT_AMOUNT : Product.PRODUCT_TYPE_ITEM;;
                mCallback.onEditItem(mProduct, mPosition);
            }

            @Override
            public void onDiscountPercent(BigDecimal percent) {
                mProduct.discountName = String.format(getString(R.string.txt_percent_off), percent.toString());
                mProduct.discountPercent = percent;
                mProduct.discountAmount = mProduct.totalPrice(mCart.mDate).multiply(percent);
                mProduct.modifierType = percent.compareTo(BigDecimal.ZERO) > 0 ? Product.MODIFIER_TYPE_DISCOUNT_PERCENT : Product.PRODUCT_TYPE_ITEM;
                mCallback.onEditItem(mProduct, mPosition);
            }
        });
    }

    private void updateItemDiscount() {
        TenPadDialogFragment newFragment;
        if (mProduct.modifierType == Product.MODIFIER_TYPE_DISCOUNT_AMOUNT)
        {
            BigDecimal amount = mProduct.discountAmount;
            BigDecimal maxPrice = mProduct.totalPrice(mCart.mDate).multiply(Consts.HUNDRED);
            newFragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, maxPrice, amount, mProduct.modifierType);
        }
        else
        {
            newFragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, Consts.HUNDRED, mProduct.discountPercent, mProduct.modifierType);
        }
        newFragment.show(getChildFragmentManager(), TAG_EDIT_ITEM_FRAGMENT);
        newFragment.setDiscountListener(new TenPadDialogFragment.DiscountListener() {

            @Override
            public void onDiscountPrice(BigDecimal amount) {
                mProduct.discountName = getString(R.string.txt_discount);
                mProduct.discountAmount = amount;
                mProduct.modifierType = amount.compareTo(BigDecimal.ZERO)> 0 ? Product.MODIFIER_TYPE_DISCOUNT_AMOUNT : Product.PRODUCT_TYPE_ITEM;
                mCallback.onEditItem(mProduct, mPosition);
            }

            @Override
            public void onDiscountPercent(BigDecimal percent) {
                mProduct.discountName = String.format(getString(R.string.txt_percent_off), percent.toString());
                mProduct.discountPercent = percent;
                mProduct.discountAmount = mProduct.totalPrice(mCart.mDate).multiply(percent);
                mProduct.modifierType = percent.compareTo(BigDecimal.ZERO)>0 ?  Product.MODIFIER_TYPE_DISCOUNT_PERCENT : Product.PRODUCT_TYPE_ITEM;
                mCallback.onEditItem(mProduct, mPosition);
            }
        });
    }
   /* private void setupViewPager()
    {
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ITEM, mCart.getProducts(), mPosition);
        TenPadFragment newFragment;
        if(PrefUtils.getCashierInfo(getActivity()).permissionPriceModify)
        {

            if (mProduct.discountAmount.compareTo(BigDecimal.ZERO) > 0){
                updateItemDiscount();
                return;
            }
             newFragment = TenPadFragment.newInstance(TenPadFragment.TEN_PAD_TYPE_DISCOUNT, mProduct.totalPrice(mCart.mDate).multiply(Consts.HUNDRED));
            return;
        }else {

            newFragment = TenPadFragment.newInstance(TenPadFragment.TEN_PAD_TYPE_ADMIN, mProduct.price);
        }

        mViewPagerAdapter.addFragment(fragment, R.string.txt_hardware);
        mViewPagerAdapter.addFragment(newFragment, R.string.txt_receipts);
        mViewPagerAdapter.addFragment(fragment, R.string.txt_tax);

       // mViewPagerAdapter.addFragment(new OfflineFragment(), R.string.txt_offline);
        mEditViewPager.setAdapter(mViewPagerAdapter);
    }
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Integer> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
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
    }*/
}
