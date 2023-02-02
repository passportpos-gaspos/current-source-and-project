package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.elotouch.paypoint.register.EloTouch;
import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.OpenorderData;
import com.pos.passport.ui.BasicItemAnimator;
import com.pos.passport.util.Consts;
import com.pos.passport.util.MessageHandler;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by karim on 10/9/15.
 */
public class FFQueueFragment extends Fragment {
    private static final String DEBUG_TAG = "[QueueFragment]";
    private static final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";
    public LinearLayout mDateTimeLinearLayout;

    public TextView mDateTimeTextView;
    public TextView mInvoiceTextView;
    public TextView name_txt;
    public AutoCompleteTextView mSearchAutocompleteTextView;
    public Spinner mMenuSpinner;
    public RecyclerView mItemRecyclerView;
    public ItemAdapter mItemAdapter;
    public RecyclerView.LayoutManager mItemLayoutManager;
    public ItemTouchHelper mItemTouchHelper;
    public LinearLayout mHoldImageButton;
    //public ImageButton mClearImageButton;
    public TableRow mDiscountAmountTableRow;
    public TextView mDiscountAmountNameTextView;
    public TextView mDiscountAmountAmountTextView;
    public TableRow mDiscountPercentTableRow;
    public TextView mDiscountPercentNameTextView;
    public TextView mDiscountPercentAmountTextView;
    public TextView mTipAmountTextView;
    public TextView mTotalCartTextView;
    public TableRow mTotalCartTableRow;
    public TableRow mSubtotalTableRow;
    public TextView mSubtotalAmountTextView;
    public TableRow mTax1TableRow;
    public TextView mTax1NameTextView;
    public TextView mTax1AmountTextView;
    public TableRow mTax2TableRow;
    public TableRow mTipAmountTableRow;
    public TextView mTax2NameTextView;
    public TextView mTax2AmountTextView;
    public TableRow mTax3TableRow;
    public TextView mTax3NameTextView;
    public TextView mTax3AmountTextView;
    public TextView mTotalAmountTextView;
    // public LinearLayout mPayButton;
    public TableLayout mPaymentsTableLayout;
    public boolean mSendingToDisplay;
    public List<OpenorderData> mCart = new ArrayList<>();
    public JSONArray mProductCart = new JSONArray();
    public Cart mTempCart;
    public Cashier mCashier;
    public QueueInterface mCallback;
    public boolean mSaleProcessed;
    public ProductDatabase mDb;
    public ArrayAdapter<CharSequence> mMenuAdapter;
    public LinearLayout mAddCustomerLayout;
    public LinearLayout mRemoveCustomerLayout;
    public LinearLayout mAddNoteLayout;
    public LinearLayout mAddTotalDiscountLayout;
    public LinearLayout mAddTaxLayout;
    public LinearLayout mBottomLayout;
    public LinearLayout recentviewshow_layout;
    public LinearLayout comanviewshow_layout;
    public LinearLayout mReturnLayout;
    public EloTouch mElo;
    public LinearLayout mPrintLayout;
    public LinearLayout printbtn;
    public LinearLayout returnback_button;
    public LinearLayout mVoidLayout;

    Context mcontext;
    public MessageHandler mHandler;


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
        View v = inflater.inflate(R.layout.fragment_queue_ff, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        mcontext = getActivity();
        mHandler = new MessageHandler(getActivity());
        return v;
    }

    //    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (mCart != null)
//            outState.putSerializable("cart", mCart);
//    }
   /* public static FFQueueFragment newInstance(OpenorderData tenPadType) {
        FFQueueFragment f = new FFQueueFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", tenPadType);
        f.setArguments(args);

        return f;
    }*/

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpUIs();

//        if (getArguments().getSerializable("type") != null)
        //  mCart.add((OpenorderData) getArguments().getSerializable("type"));

//        if (cart == null)
//            setData();
//        else
//            mCart = cart;

        //setUpListeners();

        setRecyclerView();
        updateTotals();
        if (Build.MODEL.contains(Consts.ELO_MODEL))
            mElo = new EloTouch(getActivity());
    }

    private void bindUIElements(View v) {
        mDateTimeLinearLayout = (LinearLayout) v.findViewById(R.id.date_time_linear_layout);
        name_txt = (TextView) v.findViewById(R.id.name_txt);
        mDateTimeTextView = (TextView) v.findViewById(R.id.date_time_text_view);
        mInvoiceTextView = (TextView) v.findViewById(R.id.invoice_text_view);
        mSearchAutocompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.search_autocomplete_text_view);
        mMenuSpinner = (Spinner) v.findViewById(R.id.menu_spinner);
        mItemRecyclerView = (RecyclerView) v.findViewById(R.id.item_recycler_view);
        mHoldImageButton = (LinearLayout) v.findViewById(R.id.hold_cart_image_button);

        mDiscountAmountTableRow = (TableRow) v.findViewById(R.id.discount_amount_table_row);
        mDiscountAmountNameTextView = (TextView) v.findViewById(R.id.discount_amount_name_text_view);
        mDiscountAmountAmountTextView = (TextView) v.findViewById(R.id.discount_amount_amount_text_view);
        mDiscountPercentTableRow = (TableRow) v.findViewById(R.id.discount_percent_table_row);
        mDiscountPercentNameTextView = (TextView) v.findViewById(R.id.discount_percent_name_text_view);
        mDiscountPercentAmountTextView = (TextView) v.findViewById(R.id.discount_percent_amount_text_view);
        mTipAmountTableRow = (TableRow) v.findViewById(R.id.tip_amount_table_row);
        mTipAmountTextView = (TextView) v.findViewById(R.id.tip_amount_text_view);
        mTotalCartTableRow = (TableRow) v.findViewById(R.id.total_cart_amount_table_row);
        mTotalCartTextView = (TextView) v.findViewById(R.id.total_cart_amount_text_view);
        mSubtotalTableRow = (TableRow) v.findViewById(R.id.subtotal_table_row);
        mSubtotalAmountTextView = (TextView) v.findViewById(R.id.subtotal_amount_text_view);
        mTax1TableRow = (TableRow) v.findViewById(R.id.tax_1_table_row);
        mTax1NameTextView = (TextView) v.findViewById(R.id.tax_1_name_text_view);
        mTax1AmountTextView = (TextView) v.findViewById(R.id.tax_1_amount_text_view);
        mTax2TableRow = (TableRow) v.findViewById(R.id.tax_2_table_row);
        mTax2NameTextView = (TextView) v.findViewById(R.id.tax_2_name_text_view);
        mTax2AmountTextView = (TextView) v.findViewById(R.id.tax_2_amount_text_view);
        mTax3TableRow = (TableRow) v.findViewById(R.id.tax_3_table_row);
        mTax3NameTextView = (TextView) v.findViewById(R.id.tax_3_name_text_view);
        mTax3AmountTextView = (TextView) v.findViewById(R.id.tax_3_amount_text_view);
        mTotalAmountTextView = (TextView) v.findViewById(R.id.total_amount_text_view);
        //mPayButton = (LinearLayout) v.findViewById(R.id.pay_button);
        mPaymentsTableLayout = (TableLayout) v.findViewById(R.id.payments_table_layout);
        mAddCustomerLayout = (LinearLayout) v.findViewById(R.id.add_customer_layout);
        mRemoveCustomerLayout = (LinearLayout) v.findViewById(R.id.remove_customer_layout);
        mAddNoteLayout = (LinearLayout) v.findViewById(R.id.add_note_layout);
        mAddTotalDiscountLayout = (LinearLayout) v.findViewById(R.id.add_discount_layout);
        mAddTaxLayout = (LinearLayout) v.findViewById(R.id.tax_layout);
        mReturnLayout = (LinearLayout) v.findViewById(R.id.return_layout);
        mPrintLayout = (LinearLayout) v.findViewById(R.id.print_layout);
        mBottomLayout = (LinearLayout) v.findViewById(R.id.queue_bottom_layout);

        recentviewshow_layout = (LinearLayout) v.findViewById(R.id.recentviewshow);
        comanviewshow_layout = (LinearLayout) v.findViewById(R.id.comanviewshow);
        printbtn = (LinearLayout) v.findViewById(R.id.printbtn);
        returnback_button = (LinearLayout) v.findViewById(R.id.returnback_button);
        mVoidLayout = (LinearLayout) v.findViewById(R.id.void_linear_layout);
    }

    private void setUpUIs() {
        mHoldImageButton.setEnabled(false);
        //mClearImageButton.setEnabled(false);
        String[] menus = getResources().getStringArray(R.array.queue_menu);
        ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menus));
        //mMenuAdapter = new MenuArrayAdapter(getActivity(), R.layout.view_queue_spinner_item, menuList);
        //mMenuAdapter.setDropDownViewResource(R.layout.view_queue_spinner_dropdown_item);
        //mMenuSpinner.setAdapter(mMenuAdapter);
        Utils.disableEnableControls(false, mBottomLayout);
        mRemoveCustomerLayout.setVisibility(View.GONE);
    }

    private void setData() {
        //   mCart = new Cart(getActivity());
        mCashier = PrefUtils.getCashierInfo(getActivity());
    }

//    private void setUpListeners()
//    {
//        mSearchAutocompleteTextView.setOnEditorActionListener(mKeyListener);
//        mHoldImageButton.setOnClickListener(mHoldClickListener);
//        mClearImageButton.setOnClickListener(mClearClickListener);
//        mPayButton.setOnClickListener(mPayClickListener);
//
//        mDiscountAmountNameTextView.setOnClickListener(mDiscountTextClickListener);
//        mDiscountPercentNameTextView.setOnClickListener(mDiscountTextClickListener);
//        mMenuItemSelectedListener = new SpinnerInteractionListener();
//        mMenuSpinner.setOnTouchListener(mMenuItemSelectedListener);
//        mMenuSpinner.setOnItemSelectedListener(mMenuItemSelectedListener);
//        mAddCustomerLayout.setOnClickListener(mAddCustomerLayoutClickListener);
//        mRemoveCustomerLayout.setOnClickListener(mRemoveCusClickListener);
//        mAddTotalDiscountLayout.setOnClickListener(mTotalDiscountClickListener);
//        mAddNoteLayout.setOnClickListener(mAddNoteClickListener);
//        mAddTaxLayout.setOnClickListener(mAddTaxClickListener);
//        mReturnLayout.setOnClickListener(mReturnClickListener);
//        mPrintLayout.setOnClickListener(mPrintClickListener);
//
//        printbtn.setOnClickListener(mPrintClickListener);
//        returnback_button.setOnClickListener(mReturnClickListener);
//        mVoidLayout.setOnClickListener(mClearClickListener);
//        //mPopupMenu.setOnMenuItemClickListener(new PopUpListener());
//    }

    private void setRecyclerView() {
        try {
            mItemLayoutManager = new LinearLayoutManager(getActivity());
            mItemRecyclerView.setLayoutManager(mItemLayoutManager);
            mItemRecyclerView.setItemAnimator(new BasicItemAnimator());
            if (mCart.size() > 0) {
                mProductCart = new JSONArray(mCart.get(0).getOrderItems());
                mItemAdapter = new ItemAdapter(mProductCart);
                mItemRecyclerView.setAdapter(mItemAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // mItemTouchHelper = new ItemTouchHelper(mSimpleItemTouchCallback);
        // mItemTouchHelper.attachToRecyclerView(mItemRecyclerView);
    }

    public void SetDataCart(OpenorderData data) {
        try {
            if (mCart.size() > 0)
                mCart.clear();

            mCart.add(data);
            mProductCart = new JSONArray(data.getOrderItems());
            mItemAdapter = new ItemAdapter(mProductCart);
            mItemRecyclerView.setAdapter(mItemAdapter);
            mItemAdapter.notifyDataSetChanged();
            updateTotals();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void ClearCart() {
        try {
            if (mCart.size() > 0)
                mCart.clear();

            mProductCart = new JSONArray();
            mItemAdapter = new ItemAdapter(mProductCart);
            mItemRecyclerView.setAdapter(mItemAdapter);
            mItemAdapter.notifyDataSetChanged();
            updateTotals();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//    public void saveItemEditChanges(Product p, int id) {
//        Product product = mItemAdapter.products.get(id);
//        product.modifiers = p.modifiers;
//        product.quantity = p.quantity;
//        product.taxable = p.taxable;
//        product.discountAmount = p.discountAmount;
//        product.discountName = p.discountName;
//        product.discountPercent = p.discountPercent;
//        product.modifierType = p.modifierType;
//        notifyChanges();
//        updateTotals();
//
//    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
        private JSONArray products;
        public boolean resetChildAdapter = true;
        private int previousQty = 1;
        private boolean blockEdit = false;
        private int selectedIndex = -1;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView itemNameTextView;
            EditText itemQtyTextView;
            TextView itemPriceTextView;
            LinearLayout itemDetailLinearLayout;
            Button addButton;
            Button minusButton;
            TableRow itemDiscountTableRow;
            TextView itemDiscountTextView;
            TextView itemDiscountAmountTextView;
            ImageView itemDiscountCloseImageView;
            TableLayout modifierTableView;

            public ViewHolder(View v) {
                super(v);
                itemNameTextView = (TextView) v.findViewById(R.id.item_name_text_view);
                itemQtyTextView = (EditText) v.findViewById(R.id.item_quantity_edit_text);
                itemPriceTextView = (TextView) v.findViewById(R.id.item_price_text_view);
                itemDetailLinearLayout = (LinearLayout) v.findViewById(R.id.item_detail_linear_layout);
                addButton = (Button) v.findViewById(R.id.item_quantity_plus_button);
                minusButton = (Button) v.findViewById(R.id.item_quantity_minus_button);
                itemDiscountAmountTextView = (TextView) v.findViewById(R.id.item_discount_price_text_view);
                itemDiscountTextView = (TextView) v.findViewById(R.id.item_discount_name_text_view);
                itemDiscountCloseImageView = (ImageView) v.findViewById(R.id.item_discount_close_image);
                itemDiscountTableRow = (TableRow) v.findViewById(R.id.item_discount_table_row);
                modifierTableView = (TableLayout) v.findViewById(R.id.modifiers_table);

                setUpViewHolderListeners(v);
            }

            private void setUpViewHolderListeners(final View v) {
                /*itemQtyTextView.setOnFocusChangeListener(new View.OnFocusChangeListener()
                {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (v.equals(itemQtyTextView) && !hasFocus && !blockEdit) {
                            if (TextUtils.isEmpty(itemQtyTextView.getText().toString().trim())) {
                                int position = getLayoutPosition();
                                Product item = products.get(position);
                                item.quantity = previousQty;
                                //updateTotals();
                            }
                        }
                    }
                });*/

                /*itemQtyTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int position = getLayoutPosition();
                        //showPopupAni(mcontext, view, 0, 0, itemQtyTextView, position);
                    }
                });*/

                /*itemQtyTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            int qty = Integer.parseInt(s.toString());
                            if (qty == 1)
                                minusButton.setVisibility(View.GONE);
                            else if (qty == 99)
                                addButton.setVisibility(View.GONE);
                            else {
                                minusButton.setVisibility(View.GONE);
                                addButton.setVisibility(View.GONE);
                            }
                            if (!blockEdit) {
                                previousQty = qty;
                                int position = getLayoutPosition();
                                Product item = products.get(position);
                                item.quantity = qty;
                                //updateTotals();
                            }
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                });*/

               /* itemDetailLinearLayout.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.e("Detail lin", " onclick call detail linear");

                        final int position = getLayoutPosition();
                        final Product item = products.get(position);
                        //Log.e("comboitems",">>>>>"+item.comboItems);
                        ViewHolder viewHolder = (ViewHolder) mItemRecyclerView.findViewHolderForLayoutPosition(position);
                        View childView = viewHolder.itemView;

                        if (childView == null)
                            return;

                        if (childView.isSelected()) {
                            childView.setSelected(false);
                            selectedIndex = -1;
                        } else {
                            if (selectedIndex != -1) {
                                View selectedView = mItemRecyclerView.getChildAt(selectedIndex);
                                if (selectedView != null) {
                                    selectedView.setSelected(false);
                                }
                            }
                            childView.setSelected(true);
                            selectedIndex = position;
                        }

                        if (!item.isNote) {

                            //callCombo(getAdapterPosition());
                        } else if (item.modifierType == Product.MODIFIER_TYPE_NONE) {
                            // Log.e("QueFrag", " else if con 2");
                            NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ITEM, item.name, item.price);
                            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                            fragment.setNoteListener(new NoteFragment.NoteListener() {
                                @Override
                                public void onNote(int pos, String note, BigDecimal amount) {
                                    item.name = note;
                                    item.price = amount.divide(Consts.HUNDRED);
                                    products.set(position, item);
                                   // notifyChanges();
                                   // updateTotals();
                                }

                                @Override
                                public void onDelete() {
                                    products.remove(item);
                                   // notifyChanges();
                                  //  updateTotals();
                                }
                            });
                        }
                    }
                });*/

                /*addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemQtyTextView.getText().toString().equals("")) {
                            blockEdit = true;
                            itemQtyTextView.setText("1");
                            blockEdit = false;
                        }

                        int quantity = Integer.valueOf(itemQtyTextView.getText().toString());
                        quantity++;
                        blockEdit = true;
                        itemQtyTextView.setText(String.format("%d", quantity));
                        blockEdit = false;

                        int position = getAdapterPosition();
                        Product item = products.get(position);
                        item.quantity = quantity;
                        //saveItemEditChanges(item, position);
                    }
                });*/

                /*minusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemQtyTextView.getText().toString().equals("")) {
                            blockEdit = true;
                            itemQtyTextView.setText("1");
                            blockEdit = false;
                        }

                        int quantity = Integer.valueOf(itemQtyTextView.getText().toString());
                        quantity--;
                        blockEdit = true;
                        itemQtyTextView.setText(String.format("%d", quantity));
                        blockEdit = false;

                        int position = getAdapterPosition();
                        Product item = products.get(position);
                        item.quantity = quantity;
                       // saveItemEditChanges(item, position);
                    }
                });*/
            }
        }

        public ItemAdapter(JSONArray products) {
            this.products = products;
        }

        public void setData(JSONArray products) {
            this.products = products;
        }

        public void clearData() {
            setUpUIs();
            products = null;
            resetChildAdapter = true;
            //  mPayButton.setEnabled(false);
            mVoidLayout.setEnabled(false);
        }

        public void clearChildren() {
            resetChildAdapter = true;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_inventory_item, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            try {
                final JSONObject item = products.getJSONObject(position);

            /*if (item.isNote) {
                holder.itemNameTextView.setText(WordUtils.capitalize(item.name));
                holder.itemPriceTextView.setText(Utils.formatCurrency(item.price()));
                holder.itemQtyTextView.setVisibility(View.GONE);
                holder.addButton.setVisibility(View.GONE);
                holder.minusButton.setVisibility(View.GONE);

            } else {*/
                holder.itemNameTextView.setText(WordUtils.capitalize(item.getString("name")));
                //String price = item.taxable ? String.format(getString(R.string.txt_price_taxable), item.displayPrice(mCart.mDate)) : item.displayPrice(mCart.mDate);
                //String price = item.taxable ? String.format(getString(R.string.txt_price_taxable), Utils.formatCurrency(item.displayPriceNew(mCart.mDate))) : Utils.formatCurrency(item.displayPriceNew(mCart.mDate));
                ;
                holder.itemPriceTextView.setText(Utils.formatCurrency(new BigDecimal(item.getString("totalPrice"))));
                holder.itemQtyTextView.setVisibility(View.VISIBLE);
                holder.itemQtyTextView.setText(item.getString("quantity"));
                //holder.addButton.setVisibility(View.GONE);
                // if (item.quantity > 1)
                //     holder.minusButton.setVisibility(View.GONE);
                // else
                //     holder.minusButton.setVisibility(View.GONE);
                //}

            /*if (item.discountPercent.compareTo(BigDecimal.ZERO) > 0 || item.discountAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                holder.itemDiscountTableRow.setVisibility(View.VISIBLE);
                holder.itemDiscountTextView.setText(item.discountName);
                holder.itemDiscountAmountTextView.setText(String.format("(%S)", Utils.formatCurrency(item.discountAmount).toString()));
                holder.itemDiscountCloseImageView.setVisibility(View.GONE);
                holder.itemDiscountCloseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.discountAmount = BigDecimal.ZERO;
                        item.discountPercent = BigDecimal.ZERO;
                        item.discountName = "";
                        item.modifierType = Product.PRODUCT_TYPE_ITEM;
                        // saveItemEditChanges(item, position);
                    }
                });
            } else*/
                holder.itemDiscountTableRow.setVisibility(View.GONE);

                holder.itemView.setSelected(selectedIndex == position);
                JSONArray modifiers = new JSONArray();
                modifiers = item.getJSONArray("modifiers");
                JSONArray comboItems = new JSONArray();
                comboItems = item.getJSONArray("comboItems");
                if (modifiers.length() > 0) {
                    //Log.e("Modi size ", "Item adapter modi size>>>" + item.modifiers.size());
                    holder.modifierTableView.removeAllViews();
                    holder.modifierTableView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < modifiers.length(); i++) {
                        final JSONObject m = modifiers.getJSONObject(i);
                        final TableRow row = new TableRow(getActivity());
                        row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        row.setGravity(Gravity.RIGHT);
                        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
                        TextView mNameView = new TextView(getActivity());
                        mNameView.setSingleLine(true);
                        mNameView.setGravity(Gravity.LEFT);
                        mNameView.setPadding(4, 2, 5, 0);
                        mNameView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                        mNameView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .5f));

                        TextView mPriceView = new TextView(getActivity());
                        mPriceView.setSingleLine(true);
                        mPriceView.setGravity(Gravity.RIGHT);
                        mPriceView.setPadding(5, 2, 0, 0);

                        mPriceView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                        mPriceView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .3f));

                        //mNameView.setText(String.format("\t  %s", m.name));//Utils.formatCurrency(m.price()
                        mNameView.setText(String.format("%1$s (%2$s)", m.optString("name"), m.optString("price")));

                        if (ResourceSize() == 0)
                            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                        else if (ResourceSize() == 1)
                            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                        else
                            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);

                        //mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getActivity().getResources().getDimension(R.dimen.receipt_row_modi_size));
                        //mPriceView.setText(Utils.formatCurrency(m.price()));
                        mPriceView.setText(m.optString("price"));
                        mPriceView.setVisibility(View.INVISIBLE);
                        row.addView(mNameView);
                        row.addView(mPriceView);

                        //if (m.price().compareTo(BigDecimal.ZERO) < 0)
                        //{
                        //    mNameView.setTextColor(Color.RED);
                        //    mPriceView.setTextColor(Color.RED);
                        //}
                        holder.modifierTableView.addView(row, i);
                    }
                } else if (comboItems.length() > 0) {
                    //Log.e("Modi size ", "Item adapter modi size>>>" + item.modifiers.size());
                    // holder.modifierTableView.removeAllViews();
                    holder.modifierTableView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < comboItems.length(); i++) {
                        JSONObject m = comboItems.getJSONObject(i);
                        JSONArray items = m.getJSONArray("items");
                        for (int c = 0; c < items.length(); c++) {

                            final TableRow row = new TableRow(getActivity());
                            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            row.setGravity(Gravity.RIGHT);
                            Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
                            TextView mNameView = new TextView(getActivity());
                            mNameView.setSingleLine(true);
                            mNameView.setGravity(Gravity.LEFT);
                            mNameView.setPadding(4, 2, 5, 0);
                            mNameView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                            mNameView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .5f));

                            TextView mPriceView = new TextView(getActivity());
                            mPriceView.setSingleLine(true);
                            mPriceView.setGravity(Gravity.RIGHT);
                            mPriceView.setPadding(5, 2, 0, 0);

                            mPriceView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                            mPriceView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .3f));

                            //mNameView.setText(String.format("\t  %s", m.name));//Utils.formatCurrency(m.price()
                            mNameView.setText(items.getJSONObject(c).optString("name"));

                            if (ResourceSize() == 0)
                                mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                            else if (ResourceSize() == 1)
                                mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                            else
                                mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);

                            //mNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getActivity().getResources().getDimension(R.dimen.receipt_row_modi_size));
                            //mPriceView.setText(Utils.formatCurrency(m.price()));
                            mPriceView.setText("");
                            mPriceView.setVisibility(View.INVISIBLE);
                            row.addView(mNameView);
                            row.addView(mPriceView);

                            //if (m.price().compareTo(BigDecimal.ZERO) < 0)
                            //{
                            //    mNameView.setTextColor(Color.RED);
                            //    mPriceView.setTextColor(Color.RED);
                            //}
                            holder.modifierTableView.addView(row, i);
                        }
                    }
                } else {
                    holder.modifierTableView.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return products.length();
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    public void updateTotals() {
        try {

            if (mCart.size() > 0) {
                mDateTimeLinearLayout.setVisibility(View.GONE);
                mDateTimeTextView.setText("");
                mInvoiceTextView.setText("");
                name_txt.setText("");

                name_txt.setText(String.format(getString(R.string.txt_customer_carry_out_ff), mCart.get(0).getCustomerName(), mCart.get(0).getOrderType()));
                mInvoiceTextView.setText(String.format(getString(R.string.txt_trans_no_), mCart.get(0).getOrderId()));
                mDateTimeTextView.setText(ShowDateTime(ConvertDates(mCart.get(0).getOrderPaidDate())));

                printbtn.setEnabled(true);
                mVoidLayout.setEnabled(true);

                BigDecimal mSubtotal = new BigDecimal(mCart.get(0).getOrderSubTotal());
                mSubtotalAmountTextView.setText(Utils.formatCurrency(mSubtotal));

                mDiscountAmountTableRow.setVisibility(View.GONE);
                mDiscountPercentTableRow.setVisibility(View.GONE);

                mTax2TableRow.setVisibility(View.GONE);
                mTax3TableRow.setVisibility(View.GONE);
                BigDecimal totaltaxamout = new BigDecimal(mCart.get(0).getOrderTax());
                mTax1TableRow.setVisibility(View.VISIBLE);
                mTax1NameTextView.setText("Tax");
                mTax1AmountTextView.setText(Utils.formatCurrency(totaltaxamout));

                BigDecimal paymentSum = BigDecimal.ZERO;
                BigDecimal paymentTip = new BigDecimal(mCart.get(0).getOrderTip());

                if (paymentTip.compareTo(BigDecimal.ZERO) > 0) {
                    mTipAmountTableRow.setVisibility(View.VISIBLE);
                    mTipAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(paymentTip));
                } else {
                    mTipAmountTableRow.setVisibility(View.GONE);
                }

                BigDecimal mTotal = new BigDecimal(mCart.get(0).getOrderTotal());
                mTotalAmountTextView.setText(Utils.formatCurrency(mTotal));

            } else {
                name_txt.setText("");
                mInvoiceTextView.setText("");
                mDateTimeTextView.setText("");
                printbtn.setEnabled(false);
                mVoidLayout.setEnabled(false);
                mDiscountAmountTableRow.setVisibility(View.GONE);
                mDiscountPercentTableRow.setVisibility(View.GONE);
                mTax2TableRow.setVisibility(View.GONE);
                mTax3TableRow.setVisibility(View.GONE);
                mSubtotalAmountTextView.setText(Utils.formatCurrency(BigDecimal.ZERO));
                mTax1TableRow.setVisibility(View.VISIBLE);
                mTax1NameTextView.setText("Tax");
                mTax1AmountTextView.setText(Utils.formatCurrency(BigDecimal.ZERO));
                mTipAmountTableRow.setVisibility(View.GONE);
                mTotalAmountTextView.setText(Utils.formatCurrency(BigDecimal.ZERO));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Long ConvertDates(String dateget) {
        Date date = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            date = (Date) formatter.parse(dateget);
            //System.out.println("convert date " + date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public String ShowDateTime(long timestampget) {
        String timeshow = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestampget);
        String ampm = "";
        if (cal.get(Calendar.AM_PM) == 1)
            ampm = "PM";
        else
            ampm = "AM";

        timeshow = "" + String.format("%02d", cal.get(Calendar.HOUR)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE)) + " " + ampm;
        return timeshow;
    }

    public int ResourceSize() {
        return mcontext.getResources().getInteger(R.integer.popuptype);
    }
}