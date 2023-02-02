package com.pos.passport.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.elotouch.paypoint.register.EloTouch;
import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.activity.PayActivity;
import com.pos.passport.data.JSONHelper;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Customer;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.ReceiptSetting;
import com.pos.passport.model.ReportCart;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.TaxSetting;
import com.pos.passport.ui.BasicItemAnimator;
import com.pos.passport.util.Consts;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by karim on 10/9/15.
 */
public class QueueFragmentNewOne extends Fragment {
    private static final String DEBUG_TAG = "[QueueFragment]";
    private static final String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    private SpinnerInteractionListener mMenuItemSelectedListener;
    private LinearLayout mDateTimeLinearLayout;

    private TextView mDateTimeTextView;
    private TextView mInvoiceTextView;
    private AutoCompleteTextView mSearchAutocompleteTextView;
    private Spinner mMenuSpinner;
    private RecyclerView mItemRecyclerView;
    private ItemAdapter mItemAdapter;
    private RecyclerView.LayoutManager mItemLayoutManager;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton mHoldImageButton;
    private ImageButton mClearImageButton;
    private TableRow mDiscountAmountTableRow;
    private TextView mDiscountAmountNameTextView;
    private TextView mDiscountAmountAmountTextView;
    private TableRow mDiscountPercentTableRow;
    private TextView mDiscountPercentNameTextView;
    private TextView mDiscountPercentAmountTextView;
    private TableRow mSubtotalTableRow;
    private TextView mSubtotalAmountTextView;
    private TableRow mTax1TableRow;
    private TextView mTax1NameTextView;
    private TextView mTax1AmountTextView;
    private TableRow mTax2TableRow;
    private TextView mTax2NameTextView;
    private TextView mTax2AmountTextView;
    private TableRow mTax3TableRow;
    private TextView mTax3NameTextView;
    private TextView mTax3AmountTextView;
    private TextView mTotalAmountTextView;
    private ImageView mPayButton;
    private TableLayout mPaymentsTableLayout;
    private boolean mSendingToDisplay;
    private Cart mCart;
    private Cart mTempCart;
    private Cashier mCashier;
    private QueueInterface mCallback;
    private boolean mSaleProcessed;
    private ProductDatabase mDb;
    private ArrayAdapter<CharSequence> mMenuAdapter;
    private LinearLayout mAddCustomerLayout;
    private LinearLayout mRemoveCustomerLayout;
    private LinearLayout mAddNoteLayout;
    private LinearLayout mAddTotalDiscountLayout;
    private LinearLayout mAddTaxLayout;
    private LinearLayout mBottomLayout;
    private LinearLayout mReturnLayout;
    private EloTouch mElo;


    private View.OnClickListener mPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Queue")
                    .putCustomAttribute("button", "Pay"));
            mCart.mTotal = new BigDecimal(Utils.formatCartTotal(mCart.mTotal));
            Intent intent = new Intent(getActivity(), PayActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PayActivity.BUNDLE_PAYMENTS, mCart.mPayments);
            intent.putExtra(PayActivity.BUNDLE_AMOUNT, mCart.mTotal.divide(Consts.HUNDRED).toString());
            intent.putExtras(bundle);
            getActivity().startActivityForResult(intent, Consts.REQUEST_PAY);
        }
    };

    private View.OnClickListener mAddCustomerLayoutClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_SEARCH_CUSTOMER);
        }
    };

    private View.OnClickListener mRemoveCusClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            removeCustomer();
            setImageVisibility();
        }
    };

    private View.OnClickListener mAddTaxClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mCart.mEnableTax = mCart.mEnableTax ? false : true;
            updateTotals();
        }
    };

    private View.OnClickListener mReturnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (PrefUtils.getCashierInfo(getActivity()).permissionReturn) {
                mCallback.onChangeFragment(MainActivity.FRAGMENT_RETURN);
            }
        }
    };

    private View.OnClickListener mAddNoteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_NOTE);
        }
    };

    private View.OnClickListener mTotalDiscountClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(PrefUtils.getCashierInfo(getActivity()).permissionPriceModify) {
                if (mCart.hasDiscountPercent())
                    mDiscountPercentNameTextView.performClick();
                else if (mCart.hasDiscountAmount())
                    mDiscountAmountNameTextView.performClick();
                else
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_DISCOUNT);
            }else{
                TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {
                    @Override
                    public void onAdminAccessGranted() {
                        if (mCart.hasDiscountPercent())
                            mDiscountPercentNameTextView.performClick();
                        else if (mCart.hasDiscountAmount())
                            mDiscountAmountNameTextView.performClick();
                        else
                            mCallback.onChangeFragment(MainActivity.FRAGMENT_ADD_DISCOUNT);
                    }

                    @Override
                    public void onAdminAccessDenied() {

                    }
                });
            }
        }
    };

    private ItemTouchHelper.SimpleCallback mSimpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = mItemRecyclerView.getChildAdapterPosition(viewHolder.itemView);

            Answers.getInstance().logCustom(new CustomEvent("Queue")
                    .putCustomAttribute("action", "swiped")
                    .putCustomAttribute("position", position));

            mTempCart = mCart.clone();
            mItemAdapter.clearChildren();
            mCart.getProducts().remove(position);
            if (mCart.getProducts().size() == 0) {
                mHoldImageButton.setEnabled(false);
                mClearImageButton.setEnabled(false);
                mPayButton.setEnabled(false);
                Utils.disableEnableControls(false, mBottomLayout);
            }
            mItemAdapter.notifyItemRemoved(position);
            updateTotals();
            mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            Snackbar.make(viewHolder.itemView, R.string.msg_item_deleted, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.txt_undo_cap, mUndoClickListener)
                    .show();
        }
    };

    private View.OnClickListener mHoldClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mCart.mOnHold) {
                saveCurrentSale(mCart.mName);
                notifyChanges();
                updateTotals();
                removeAll();
                Toast.makeText(getActivity(), String.format(getString(R.string.msg_order_save), mCart.mName), Toast.LENGTH_LONG).show();
                return;
            }
            mCallback.onChangeFragment(MainActivity.FRAGMENT_SAVE_SALE);
        }

    };

    private View.OnClickListener mDiscountTextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Queue")
                    .putCustomAttribute("button", "Discount Text"));

            final int discountType;
            if (v.equals(mDiscountPercentNameTextView)) {
                discountType = Product.MODIFIER_TYPE_DISCOUNT_PERCENT;
            } else {
                discountType = Product.MODIFIER_TYPE_DISCOUNT_AMOUNT;
            }

            TenPadDialogFragment fragment;
            if (discountType == Product.MODIFIER_TYPE_DISCOUNT_AMOUNT)
                fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, mCart.mTotal.add(mCart.mDiscountAmount), mCart.mDiscountAmount, discountType);
            else
                fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, mCart.mTotal, mCart.mDiscountPercent, discountType);
            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);

            fragment.setDiscountListener(new TenPadDialogFragment.DiscountListener() {
                @Override
                public void onDiscountPrice(BigDecimal amount) {
                    mCart.mDiscountAmount = amount;
                    updateTotals();
                }

                @Override
                public void onDiscountPercent(BigDecimal percent) {
                    mCart.mDiscountPercent = percent;
                    mCart.mDiscountName = String.format(getString(R.string.txt_percent_off), percent.toString());
                    mCart.mDiscountAmount = BigDecimal.ZERO;
                    updateTotals();
                }
            });
        }
    };

    private View.OnClickListener mUndoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Answers.getInstance().logCustom(new CustomEvent("Queue")
                    .putCustomAttribute("button", "Undo"));
            if (mTempCart != null) {
                mCart = mTempCart.clone();
                mCart.mVoided = false;
                updateTotals();
                mItemAdapter.setData(mCart.getProducts());
                notifyChanges();
                mTempCart = null;
            }
        }
    };

    private View.OnClickListener mClearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Answers.getInstance().logCustom(new CustomEvent("Queue")
                    .putCustomAttribute("button", "Clear"));
            AlertDialogFragment f = AlertDialogFragment.getInstance(getActivity(), R.string.txt_void_sale, R.string.msg_void_sale, R.string.txt_yes, R.string.txt_no);
            f.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
            f.setAlertListener(new AlertDialogFragment.AlertListener() {
                @Override
                public void ok() {
                    mTempCart = mCart.clone();
                    mCart.mVoided = true;
                    mCart.mStatus = mCart.VOIDED;
                    saveSale(null);
                    mCallback.onSaleDone();
                    /*mItemAdapter.clearData();
                    removeAll();
                    updateTotals();*/
                    mMenuSpinner.setSelection(0);
                    Snackbar.make(v, R.string.msg_item_cleared, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.txt_undo_cap, mUndoClickListener)
                            .show();
                }

                @Override
                public void cancel() {}
            });
        }
    };

    private TextView.OnEditorActionListener mKeyListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String search = mSearchAutocompleteTextView.getText().toString();
                if (!search.equals("")) {
                    Product product = mDb.findByBarcode(search.trim());
                    if (product == null) {
                        Utils.alertBox(getActivity(), getString(R.string.txt_not_found), getString(R.string.txt_product_not_found));
                    } else {
                        mCart.addProduct(new Product(product));
                        notifyChanges();
                        updateTotals();
                    }

                    mSearchAutocompleteTextView.setText("");
                    mSearchAutocompleteTextView.setSelection(0);
                }
            }
            return true;
        }
    };

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
        View v = inflater.inflate(R.layout.fragment_queue, container, false);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCart != null)
            outState.putSerializable("cart", mCart);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDb = ProductDatabase.getInstance(getActivity());
        bindUIElements(view);
        setUpUIs();
        Cart cart = null;
        if (savedInstanceState != null) {
            cart = (Cart) savedInstanceState.getSerializable("cart");
        }
        if (cart == null)
            setData();
        else
            mCart = cart;
        setUpListeners();
        setRecyclerView();
        if (Build.MODEL.contains(Consts.ELO_MODEL))
            mElo = new EloTouch(getActivity());
    }

    private void bindUIElements(View v) {
        mDateTimeLinearLayout = (LinearLayout) v.findViewById(R.id.date_time_linear_layout);
        mDateTimeTextView = (TextView) v.findViewById(R.id.date_time_text_view);
        mInvoiceTextView = (TextView) v.findViewById(R.id.invoice_text_view);
        mSearchAutocompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.search_autocomplete_text_view);
        mMenuSpinner = (Spinner) v.findViewById(R.id.menu_spinner);
        mItemRecyclerView = (RecyclerView) v.findViewById(R.id.item_recycler_view);
        mHoldImageButton = (ImageButton) v.findViewById(R.id.hold_cart_image_button);
        mClearImageButton = (ImageButton) v.findViewById(R.id.clear_image_button);
        mDiscountAmountTableRow = (TableRow) v.findViewById(R.id.discount_amount_table_row);
        mDiscountAmountNameTextView = (TextView) v.findViewById(R.id.discount_amount_name_text_view);
        mDiscountAmountAmountTextView = (TextView) v.findViewById(R.id.discount_amount_amount_text_view);
        mDiscountPercentTableRow = (TableRow) v.findViewById(R.id.discount_percent_table_row);
        mDiscountPercentNameTextView = (TextView) v.findViewById(R.id.discount_percent_name_text_view);
        mDiscountPercentAmountTextView = (TextView) v.findViewById(R.id.discount_percent_amount_text_view);
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
        mPayButton = (ImageView) v.findViewById(R.id.pay_button);
        mPaymentsTableLayout = (TableLayout) v.findViewById(R.id.payments_table_layout);
        mAddCustomerLayout = (LinearLayout) v.findViewById(R.id.add_customer_layout);
        mRemoveCustomerLayout = (LinearLayout) v.findViewById(R.id.remove_customer_layout);
        mAddNoteLayout = (LinearLayout) v.findViewById(R.id.add_note_layout);
        mAddTotalDiscountLayout = (LinearLayout) v.findViewById(R.id.add_discount_layout);
        mAddTaxLayout = (LinearLayout) v.findViewById(R.id.tax_layout);
        mBottomLayout = (LinearLayout) v.findViewById(R.id.queue_bottom_layout);
        mReturnLayout = (LinearLayout) v.findViewById(R.id.return_layout);

    }

    private void setUpUIs() {
        mHoldImageButton.setEnabled(false);
        mClearImageButton.setEnabled(false);
        String[] menus = getResources().getStringArray(R.array.queue_menu);
        ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menus));
        mMenuAdapter = new MenuArrayAdapter(getActivity(), R.layout.view_queue_spinner_item, menuList);
        mMenuAdapter.setDropDownViewResource(R.layout.view_queue_spinner_dropdown_item);
        mMenuSpinner.setAdapter(mMenuAdapter);
        Utils.disableEnableControls(false, mBottomLayout);
        mRemoveCustomerLayout.setVisibility(View.GONE);
    }

    private void setData() {
        mCart = new Cart(getActivity());
        mCashier = PrefUtils.getCashierInfo(getActivity());
    }

    private void setUpListeners() {
        mSearchAutocompleteTextView.setOnEditorActionListener(mKeyListener);
        mHoldImageButton.setOnClickListener(mHoldClickListener);
        mClearImageButton.setOnClickListener(mClearClickListener);
        mPayButton.setOnClickListener(mPayClickListener);

        mDiscountAmountNameTextView.setOnClickListener(mDiscountTextClickListener);
        mDiscountPercentNameTextView.setOnClickListener(mDiscountTextClickListener);
        mMenuItemSelectedListener = new SpinnerInteractionListener();
        mMenuSpinner.setOnTouchListener(mMenuItemSelectedListener);
        mMenuSpinner.setOnItemSelectedListener(mMenuItemSelectedListener);
        mAddCustomerLayout.setOnClickListener(mAddCustomerLayoutClickListener);
        mRemoveCustomerLayout.setOnClickListener(mRemoveCusClickListener);
        mAddTotalDiscountLayout.setOnClickListener(mTotalDiscountClickListener);
        mAddNoteLayout.setOnClickListener(mAddNoteClickListener);
        mAddTaxLayout.setOnClickListener(mAddTaxClickListener);
        mReturnLayout.setOnClickListener(mReturnClickListener);
        //mPopupMenu.setOnMenuItemClickListener(new PopUpListener());
    }

    private void setRecyclerView() {
        mItemLayoutManager = new LinearLayoutManager(getActivity());
        mItemRecyclerView.setLayoutManager(mItemLayoutManager);
        mItemRecyclerView.setItemAnimator(new BasicItemAnimator());

        mItemAdapter = new ItemAdapter(mCart.getProducts());
        mItemRecyclerView.setAdapter(mItemAdapter);
        mItemTouchHelper = new ItemTouchHelper(mSimpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mItemRecyclerView);
    }

    /*
    @Override
    public void onActivityResult(int request, int result, Intent intent){
        if(request == AddToSaleFragment.REQUEST_CODE){
            switch (result){
                case AddToSaleFragment.RESULT_CODE_CUSTOMER:
                    addCustomer();
                    break;
                case AddToSaleFragment.RESULT_CODE_DISCOUNT:
                    addDiscountToSale();
                    break;
                case AddToSaleFragment.RESULT_CODE_NOTE:
                    addNoteToSale();
                    break;
            }
        }
    }
    */

    public void saveItemEditChanges(Product p, int id) {
        Product product = mItemAdapter.products.get(id);
        product.modifiers = p.modifiers;
        product.quantity = p.quantity;
        product.taxable = p.taxable;
        product.discountAmount = p.discountAmount;
        product.discountName = p.discountName;
        product.discountPercent = p.discountPercent;
        product.modifierType = p.modifierType;
        notifyChanges();
        updateTotals();

    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>
    {
        private ArrayList<Product> products;
        public boolean resetChildAdapter = true;
        private int previousQty = 1;
        private boolean blockEdit = false;
        private int selectedIndex = -1;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView itemNameTextView;
            public EditText itemQtyTextView;
            public TextView itemPriceTextView;
            public LinearLayout itemDetailLinearLayout;
            public Button addButton;
            public Button minusButton;
            public TableRow itemDiscountTableRow;
            public TextView itemDiscountTextView;
            public TextView itemDiscountAmountTextView;
            public ImageView itemDiscountCloseImageView;
            public TableLayout modifierTableView;

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
                itemQtyTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (v.equals(itemQtyTextView) && !hasFocus && !blockEdit)
                        {
                            if (TextUtils.isEmpty(itemQtyTextView.getText().toString().trim()))
                            {
                                int position = getLayoutPosition();
                                Product item = products.get(position);
                                item.quantity = previousQty;
                                updateTotals();
                            }
                        }
                    }
                });
                itemQtyTextView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent)
                    {
                        Log.e("Edit text touch","on touch call edittext ");
                        int x = (int)motionEvent.getX();
                        int y = (int)motionEvent.getY();
                        showPopup(getActivity(),view,x,y,itemQtyTextView);
                        return true;
                    }
                });

                itemQtyTextView.addTextChangedListener(new TextWatcher() {
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
                                minusButton.setVisibility(View.INVISIBLE);
                            else if (qty == 99)
                                addButton.setVisibility(View.INVISIBLE);
                            else {
                                minusButton.setVisibility(View.VISIBLE);
                                addButton.setVisibility(View.VISIBLE);
                            }
                            if (!blockEdit) {
                                previousQty = qty;
                                int position = getLayoutPosition();
                                Product item = products.get(position);
                                item.quantity = qty;
                                updateTotals();
                            }
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                itemDetailLinearLayout.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Log.e("Detail lin"," onclick call detail linear");

                        final int position = getLayoutPosition();
                        final Product item = products.get(position);
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

                        if (!item.isNote)
                        {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("item", products.get(getAdapterPosition()));
                            bundle.putInt("position", getAdapterPosition());
                            bundle.putSerializable("cart", mCart);
                         /*   mCallback.onChangeFragment(MainActivity.FRAGMENT_EDIT_ITEM, bundle);
                            return;*/

                            EditItemFragment newFragment = new EditItemFragment();
                            newFragment.setArguments(bundle);
                            newFragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);

                            /*if (childView.isSelected()) {
                                itemExpandedLinearLayout.setVisibility(View.VISIBLE);
                                expanderImageView.setImageResource(R.drawable.ic_expand_less_black_24dp);
                                item.expandBar = true;

                            } else {
                                itemExpandedLinearLayout.setVisibility(View.GONE);
                                expanderImageView.setImageResource(R.drawable.ic_expand_more_black_24dp);
                                item.expandBar = false;
                            }*/
                        } else if (item.modifierType == Product.MODIFIER_TYPE_NONE)
                        {
                           /* NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ITEM, item.name, item.price);
                            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                            fragment.setNoteListener(new NoteFragment.NoteListener() {
                                @Override
                                public void onNote(int pos, String note, BigDecimal amount) {
                                    item.name = note;
                                    item.price = amount;
                                    products.set(position, item);
                                    notifyChanges();
                                    updateTotals();
                                }

                                @Override
                                public void onDelete() {
                                    products.remove(item);
                                    notifyChanges();
                                    updateTotals();
                                }
                            });*/
                        } else if (item.modifierType > 2) {
                            /*NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ITEM, item.name, item.price);
                            //fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                            fragment.setNoteListener(new NoteFragment.NoteListener() {
                                @Override
                                public void onNote(int pos, String note, BigDecimal amount) {
                                    Product product = products.get(position);
                                    product.name = note;
                                    product.price = amount;
                                    products.set(position, product);
                                    notifyChanges();
                                    updateTotals();
                                }

                                @Override
                                public void onDelete() {
                                    Product product = products.get(position);
                                    products.remove(product);
                                    notifyChanges();
                                    updateTotals();
                                }
                            });*/
                        } else {
                            TenPadDialogFragment fragment;
                            if (item.modifierType == Product.MODIFIER_TYPE_DISCOUNT_AMOUNT) {
                                BigDecimal amount = item.price.multiply(Consts.MINUS_ONE);
                                fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, item.maxPrice, amount, item.modifierType);
                            } else {
                                fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, Consts.HUNDRED, item.discount, item.modifierType);
                            }

                            fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                            fragment.setDiscountListener(new TenPadDialogFragment.DiscountListener() {
                                @Override
                                public void onDiscountPrice(BigDecimal amount) {
                                    Product m = products.get(position);
                                    m.price = amount.multiply(Consts.MINUS_ONE);
                                    products.set(position, m);
                                    notifyChanges();
                                    updateTotals();
                                }

                                @Override
                                public void onDiscountPercent(BigDecimal percent) {
                                    Product m = products.get(position);
                                    m.name = String.format(getString(R.string.txt_percent_off), percent.toString());
                                    m.discount = percent;
                                    products.set(position, m);
                                    notifyChanges();
                                    updateTotals();
                                }
                            });
                        }
                    }
                });

                addButton.setOnClickListener(new View.OnClickListener() {
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
                        saveItemEditChanges(item, position);
                    }
                });

                minusButton.setOnClickListener(new View.OnClickListener() {
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
                        saveItemEditChanges(item, position);
                    }
                });
            }
        }

        public ItemAdapter(ArrayList<Product> products) {
            this.products = products;
        }

        public void setData(ArrayList<Product> products) {
            this.products = products;
        }

        public void clearData() {
            setUpUIs();
            products.clear();
            resetChildAdapter = true;
            mPayButton.setEnabled(false);
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
            final Product item = products.get(position);

            if (item.isNote) {
                holder.itemNameTextView.setText(item.name);
                holder.itemPriceTextView.setText(Utils.formatCurrency(item.price()));
                holder.itemQtyTextView.setVisibility(View.GONE);
                holder.addButton.setVisibility(View.GONE);
                holder.minusButton.setVisibility(View.GONE);

            } else {
                holder.itemNameTextView.setText(item.name);
                String price = item.taxable ? String.format(getString(R.string.txt_price_taxable), item.displayPrice(mCart.mDate)) : item.displayPrice(mCart.mDate);
                holder.itemPriceTextView.setText(price);
                holder.itemQtyTextView.setVisibility(View.VISIBLE);
                holder.itemQtyTextView.setText(String.format("%d", item.quantity));
                holder.addButton.setVisibility(View.VISIBLE);
                if (item.quantity > 1)
                    holder.minusButton.setVisibility(View.VISIBLE);
                else
                    holder.minusButton.setVisibility(View.INVISIBLE);
            }

            if (item.discountPercent.compareTo(BigDecimal.ZERO) > 0 || item.discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                holder.itemDiscountTableRow.setVisibility(View.VISIBLE);
                holder.itemDiscountTextView.setText(item.discountName);
                holder.itemDiscountAmountTextView.setText(Utils.formatDiscount(item.discountAmount).toString());
                holder.itemDiscountCloseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.discountAmount = BigDecimal.ZERO;
                        item.discountPercent = BigDecimal.ZERO;
                        item.discountName = "";
                        item.modifierType = Product.PRODUCT_TYPE_ITEM;
                        saveItemEditChanges(item, position);
                    }
                });
            } else
                holder.itemDiscountTableRow.setVisibility(View.GONE);

            holder.itemView.setSelected(selectedIndex == position);

            if (item.modifiers.size() > 0) {
                holder.modifierTableView.removeAllViews();
                holder.modifierTableView.setVisibility(View.VISIBLE);
                for (int i = 0; i < item.modifiers.size(); i++) {
                    final Product m = item.modifiers.get(i);
                    final TableRow row = new TableRow(getActivity());
                    row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    row.setGravity(Gravity.RIGHT);
                    Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
                    TextView mNameView = new TextView(getActivity());
                    mNameView.setSingleLine(true);
                    mNameView.setGravity(Gravity.RIGHT);
                    mNameView.setPadding(5, 2, 5, 0);
                    mNameView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                    mNameView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .5f));

                    TextView mPriceView = new TextView(getActivity());
                    mPriceView.setSingleLine(true);
                    mPriceView.setGravity(Gravity.RIGHT);
                    mPriceView.setPadding(5, 2, 2, 0);
                    mPriceView.setTextColor(com.pos.passport.ui.Utils.getColor(getActivity(), R.color.secondaryText));
                    mPriceView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, .3f));

                    ImageView mImageView = new ImageView(getActivity());
                    mImageView.setScaleType(ImageView.ScaleType.CENTER);
                    mImageView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    mNameView.setText(String.format("\t  %s", m.name));
                    mPriceView.setText(Utils.formatCurrency(m.price()));
                    mImageView.setImageResource(R.drawable.ic_close_24dp);

                    mImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.modifiers.remove(m);
                            saveItemEditChanges(item, position);
                            Snackbar.make(v, R.string.msg_item_deleted, Snackbar.LENGTH_SHORT).show();
                            holder.modifierTableView.removeView(row);
                        }
                    });
                    row.addView(mNameView);
                    row.addView(mPriceView);
                    row.addView(mImageView);

                    if (m.price().compareTo(BigDecimal.ZERO) < 0) {
                        mNameView.setTextColor(Color.RED);
                        mPriceView.setTextColor(Color.RED);
                    }

                    holder.modifierTableView.addView(row, i);
                }
            } else {
                holder.modifierTableView.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void showPopup(final Activity context, View v1, int x, int y, final EditText textchange)
        {
            try {
                Log.e("showPopup","in method");


                // Inflate the popup_layout.xml
                //LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.popup, null);

                // Creating the PopupWindow
                //final PopupWindow popup = new PopupWindow(context);
                final PopupWindow popup = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                popup.setContentView(layout);
                popup.setFocusable(true);

                // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
                int OFFSET_X = 30;
                int OFFSET_Y = 30;

                // Clear the default translucent background
                popup.setBackgroundDrawable(new BitmapDrawable());

                // Displaying the popup at the specified location, + offsets.
                // popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
                Rect location = Utils.locateView(layout);
                // popup.showAtLocation(v1, Gravity.START, 0, 0);
                // Getting a reference to Close button, and close the popup when clicked.
                final EditText qnty_txt = (EditText) layout.findViewById(R.id.qnty_txt);
                final ImageView downbtn=(ImageView)layout.findViewById(R.id.downbtn);
                final ImageView upbtn=(ImageView)layout.findViewById(R.id.upbtn);
                TextView update_item=(TextView)layout.findViewById(R.id.update_item);
                TextView delete_item=(TextView)layout.findViewById(R.id.delete_item);
                downbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        int quantity = Integer.valueOf(qnty_txt.getText().toString());
                        quantity++;
                        qnty_txt.setText(""+quantity);
                    }
                });
                upbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        int quantity = Integer.valueOf(qnty_txt.getText().toString());
                        quantity--;
                        qnty_txt.setText(""+quantity);

                    }
                });
                update_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        if (TextUtils.isEmpty(qnty_txt.getText().toString().trim()))
                        {

                        }
                        else
                        {
                            textchange.setText(qnty_txt.getText().toString());
                        }
                    }
                });
                delete_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {

                    }
                });

                qnty_txt.requestFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(qnty_txt, InputMethodManager.SHOW_IMPLICIT);
                qnty_txt.addTextChangedListener(new TextWatcher() {
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
                                downbtn.setVisibility(View.INVISIBLE);
                            else if (qty == 99)
                                upbtn.setVisibility(View.INVISIBLE);
                            else {
                                downbtn.setVisibility(View.VISIBLE);
                                upbtn.setVisibility(View.VISIBLE);
                            }
                            /*if (!blockEdit) {
                                previousQty = qty;
                                int position = getLayoutPosition();
                                Product item = products.get(position);
                                item.quantity = qty;
                                updateTotals();
                            }*/
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                });


                //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                ImageView close = (ImageView) layout.findViewById(R.id.canclebtn);
                close.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v)
                    {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(qnty_txt.getWindowToken(), 0);
                        popup.dismiss();
                    }
                });
                popup.showAtLocation(v1, Gravity.NO_GRAVITY,x,y);
                //popup.showAsDropDown(layout, -layout.getHeight(), -layout.getWidth());
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    ;

    public void updateTotals() {
        if (isAdded()) {
            mCart.mSubtotal = BigDecimal.ZERO;
            mCart.mTaxable1SubTotal = BigDecimal.ZERO;
            mCart.mTaxable2SubTotal = BigDecimal.ZERO;
            mCart.mTaxable3SubTotal = BigDecimal.ZERO;

            mDateTimeLinearLayout.setVisibility(View.GONE);
            mDateTimeTextView.setText("");
            mInvoiceTextView.setText("");

            if ((mCart.hasCustomer() || mCart.getProducts().size() > 0) && !mSaleProcessed && !mCart.mHasTransNumber) {
                mInvoiceTextView.setText(R.string.txt_pending);
                //new GetTranNumber().execute();
                setTransNumber();
            } else if ((mCart.hasCustomer() || mCart.getProducts().size() > 0) && !mSaleProcessed && mCart.mHasTransNumber) {
                mInvoiceTextView.setText(String.format(getString(R.string.txt_trans_no), mCart.mTrans));
            }

            BigDecimal nonDiscountTotal = BigDecimal.ZERO;

            for (int i = 0; i < mCart.getProducts().size(); i++) {
                mCart.mDate = new Date().getTime();

                mDateTimeTextView.setText(DateFormat.getDateTimeInstance().format(new Date(mCart.mDate)));

                Product item = mCart.getProducts().get(i);

                item.subDiscount = mCart.mSubtotalDiscount;
                mCart.mSubtotal = mCart.mSubtotal.add(item.total());
                nonDiscountTotal = nonDiscountTotal.add(item.itemNonDiscountTotal(mCart.mDate));

                if (item.cat != 0) {
                    String cat = mDb.getCatById(item.cat);
                    int catPos = mDb.getCatagoryString().indexOf(cat);

                    if (catPos > -1 && item.taxable) {
                        if (mDb.getCatagories().get(catPos).getTaxable1() && item.taxable) {
                            mCart.mTaxable1SubTotal = mCart.mTaxable1SubTotal.add(item.itemTotal(mCart.mDate));
                        }

                        if (mDb.getCatagories().get(catPos).getTaxable2() && item.taxable) {
                            mCart.mTaxable2SubTotal = mCart.mTaxable2SubTotal.add(item.itemTotal(mCart.mDate));
                        }

                        if (mDb.getCatagories().get(catPos).getTaxable3() && item.taxable) {
                            mCart.mTaxable3SubTotal = mCart.mTaxable3SubTotal.add(item.itemTotal(mCart.mDate));
                        }
                    }
                }

                ArrayList<Product> modifiers = mCart.getProducts().get(i).modifiers;
                for (Product modifier : modifiers) {
                    String cat = mDb.getCatById(modifier.cat);
                    int catPos = mDb.getCatagoryString().indexOf(cat);
                    if (modifier.cat != 0) {
                        if (catPos > -1 && item.taxable) {
                            if (mDb.getCatagories().get(catPos).getTaxable1() && item.taxable) {
                                mCart.mTaxable1SubTotal = mCart.mTaxable1SubTotal.add(modifier.itemTotal(mCart.mDate));
                            }

                            if (mDb.getCatagories().get(catPos).getTaxable2() && item.taxable) {
                                mCart.mTaxable2SubTotal = mCart.mTaxable2SubTotal.add(modifier.itemTotal(mCart.mDate));
                            }

                            if (mDb.getCatagories().get(catPos).getTaxable3() && item.taxable) {
                                mCart.mTaxable3SubTotal = mCart.mTaxable3SubTotal.add(modifier.itemTotal(mCart.mDate));
                            }
                        }
                    }
                }
            }

            mCart.mTotal = mCart.mSubtotal;
            if (mCart.getProducts().size() > 0) {
                mPayButton.setEnabled(true);
                mHoldImageButton.setEnabled(true);
                mClearImageButton.setEnabled(true);
                Utils.disableEnableControls(true, mBottomLayout);
            }

            mSubtotalAmountTextView.setText(Utils.formatCurrency(mCart.mSubtotal));
            if (mCart.mSubtotalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                mDiscountAmountTableRow.setVisibility(View.VISIBLE);
                mDiscountAmountNameTextView.setText(String.format(getString(R.string.txt_discount_percent_placeholder), mCart.mSubtotalDiscount.toString()));
                mDiscountAmountAmountTextView.setText(DecimalFormat.getCurrencyInstance().format(mCart.mSubtotal.subtract(nonDiscountTotal.divide(Consts.HUNDRED))));
            } else if (mCart.mDiscountAmount.compareTo(BigDecimal.ZERO) == 0 && mCart.mDiscountPercent.compareTo(BigDecimal.ZERO) == 0) {
                mDiscountAmountTableRow.setVisibility(View.GONE);
                mDiscountPercentTableRow.setVisibility(View.GONE);
            } else {
                mDiscountAmountTableRow.setVisibility(View.GONE);
                mDiscountPercentTableRow.setVisibility(View.GONE);
                if (mCart.mDiscountPercent.compareTo(BigDecimal.ZERO) > 0) {
                    mDiscountPercentTableRow.setVisibility(View.VISIBLE);
                    mDiscountPercentNameTextView.setText(mCart.mDiscountName);
                    BigDecimal discount = mCart.mSubtotal.multiply(mCart.mDiscountPercent).divide(Consts.HUNDRED);
                    mCart.mDiscountAmount = discount;
                    mDiscountPercentAmountTextView.setText(Utils.formatDiscount(discount));
                    mCart.mTotal = mCart.mTotal.subtract(discount);
                } else if (mCart.mDiscountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    mDiscountAmountTableRow.setVisibility(View.VISIBLE);
                    mDiscountAmountNameTextView.setText(mCart.mDiscountName);
                    mDiscountAmountAmountTextView.setText(Utils.formatDiscount(mCart.mDiscountAmount));
                    mCart.mTotal = mCart.mTotal.subtract(mCart.mDiscountAmount);
                }
            }
            if (TaxSetting.getTax1Name() != null) {
                if (!TaxSetting.getTax1Name().equals("")) {
                    mTax1TableRow.setVisibility(View.VISIBLE);
                    BigDecimal taxAmount1 = mCart.mEnableTax ? mCart.mTaxable1SubTotal.multiply(new BigDecimal(TaxSetting.getTax1()).divide(Consts.HUNDRED))
                            : BigDecimal.ZERO;
                    mCart.mTotal = mCart.mTotal.add(taxAmount1);
                    mTax1NameTextView.setText(TaxSetting.getTax1Name());
                    mTax1AmountTextView.setText(Utils.formatCurrency(taxAmount1));
                } else {
                    mTax1TableRow.setVisibility(View.GONE);
                }
            } else {
                mTax1TableRow.setVisibility(View.GONE);
            }

            if (TaxSetting.getTax2Name() != null) {
                if (!TaxSetting.getTax2Name().equals("")) {
                    mTax2TableRow.setVisibility(View.VISIBLE);
                    BigDecimal taxAmount2 = mCart.mEnableTax ? mCart.mTaxable2SubTotal.multiply(new BigDecimal(TaxSetting.getTax2()).divide(Consts.HUNDRED))
                            : BigDecimal.ZERO;
                    mCart.mTotal = mCart.mTotal.add(taxAmount2);
                    mTax2NameTextView.setText(TaxSetting.getTax2Name());
                    mTax2AmountTextView.setText(Utils.formatCurrency(taxAmount2));
                } else {
                    mTax2TableRow.setVisibility(View.GONE);
                }
            } else {
                mTax2TableRow.setVisibility(View.GONE);
            }

            if (TaxSetting.getTax3Name() != null) {
                if (!TaxSetting.getTax3Name().equals("")) {
                    mTax3TableRow.setVisibility(View.VISIBLE);
                    BigDecimal taxAmount3 = mCart.mEnableTax ? mCart.mTaxable3SubTotal.multiply(new BigDecimal(TaxSetting.getTax3()).divide(Consts.HUNDRED))
                            : BigDecimal.ZERO;
                    mCart.mTotal = mCart.mTotal.add(taxAmount3);
                    mTax3NameTextView.setText(TaxSetting.getTax3Name());
                    mTax3AmountTextView.setText(Utils.formatCurrency(taxAmount3));
                } else {
                    mTax3TableRow.setVisibility(View.GONE);
                }
            } else {
                mTax3TableRow.setVisibility(View.GONE);
            }

            BigDecimal paymentSum = BigDecimal.ZERO;

            if (mCart.mPayments.size() > 0) {
                mPaymentsTableLayout.setVisibility(View.VISIBLE);
                mPaymentsTableLayout.removeAllViews();
                for (int p = 0; p < mCart.mPayments.size(); p++) {
                    TableRow row = new TableRow(getActivity());
                    mPaymentsTableLayout.addView(row);
                    row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView tv1 = new TextView(getActivity());
                    TextView tv2 = new TextView(getActivity());

                    tv1.setText(mCart.mPayments.get(p).paymentType);
                    tv1.setGravity(Gravity.END);
                    if (Build.VERSION.SDK_INT < 23) {
                        tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearanceBigger);
                        tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearanceBigger);
                    } else {
                        tv1.setTextAppearance(R.style.textLayoutAppearanceBigger);
                        tv2.setTextAppearance(R.style.textLayoutAppearanceBigger);
                    }
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tv1);

                    tv2.setText(Utils.formatCurrency(mCart.mPayments.get(p).paymentAmount));
                    tv2.setGravity(Gravity.END);

                    tv2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tv2);

                    paymentSum = paymentSum.add(mCart.mPayments.get(p).paymentAmount);
                }

                if (mCart.mTotal.compareTo(paymentSum) < 0 || mCart.mTotal.compareTo(BigDecimal.ZERO) < 0) {
                    TableRow row = new TableRow(getActivity());
                    mPaymentsTableLayout.addView(row);
                    row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView tv1 = new TextView(getActivity());
                    TextView tv2 = new TextView(getActivity());

                    tv1.setText(getString(R.string.txt_change_label));
                    tv1.setGravity(Gravity.END);
                    tv1.setTextAppearance(getActivity(), R.style.textLayoutAppearanceBigger);
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tv1);

                    tv2.setText(DecimalFormat.getCurrencyInstance().format(mCart.mTotal.subtract(paymentSum).divide(Consts.HUNDRED)));
                    if (mCart.mTotal.compareTo(BigDecimal.ZERO) < 0)
                        tv2.setText(Utils.formatCurrency(mCart.mTotal));

                    tv2.setGravity(Gravity.END);
                    tv2.setTextAppearance(getActivity(), R.style.textLayoutAppearanceBigger);
                    tv2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tv2);
                }
            } else {
                mPaymentsTableLayout.setVisibility(View.GONE);
            }

            /*if (mCart.mSubtotal.compareTo(mCart.mTotal) == 0)
                mSubtotalTableRow.setVisibility(View.GONE);
            else
                mSubtotalTableRow.setVisibility(View.VISIBLE);*/

            mTotalAmountTextView.setText(Utils.formatCurrency(mCart.mTotal));

            if (mCart.getProducts().size() > 0) {
                Product test1 = mCart.getProducts().get(mCart.getProducts().size() - 1);

                StringBuffer message = new StringBuffer("                                        ");

                message.replace(0, test1.name.length() - 1, test1.name);
                message.replace(message.length() / 2 - test1.displayPrice(mCart.mDate).length(), 19, test1.displayPrice(mCart.mDate));

                String total = getString(R.string.txt_total) + ": " + Utils.formatCurrency(mCart.mTotal);
                if (Build.MODEL.contains(Consts.ELO_MODEL))
                    onPrintTextDisplay(message.toString().trim(), total);
                else {
                    message.replace(20, 39, total);

                    onPrintTextDisplay(message.toString());
                }

            } else {
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

                if (Build.MODEL.contains(Consts.ELO_MODEL))
                    onPrintTextDisplay(getString(R.string.txt_welcome_to), StoreSetting.getName());
                else
                    onPrintTextDisplay(message.toString());
            }
        }
    }

    public void saveSale(@Nullable String gatewayId) {
        mCashier = PrefUtils.getCashierInfo(getActivity());
        try {
            JSONObject json = new JSONObject();

            JSONArray jsonPayments = JSONHelper.toJSONPaymentArray(getActivity(), mCart, gatewayId);
            json.put("Payments", jsonPayments);

            JSONArray jsonProducts = JSONHelper.toJSONProductArray(getActivity(), mCart);
            json.put("Products", jsonProducts);

            String result = json.toString();

            if (TaxSetting.getTax1Name() != null) {
                if (!TaxSetting.getTax1Name().equals("")) {
                    mCart.mTax1Name = (TaxSetting.getTax1Name());
                    mCart.mTax1 = mCart.mTaxable1SubTotal.multiply(new BigDecimal(TaxSetting.getTax1()).divide(Consts.HUNDRED));
                    mCart.mTax1Percent = new BigDecimal(TaxSetting.getTax1());
                }
            }

            if (TaxSetting.getTax2Name() != null) {
                if (!TaxSetting.getTax2Name().equals("")) {
                    mCart.mTax2Name = (TaxSetting.getTax2Name());
                    mCart.mTax2 = mCart.mTaxable2SubTotal.multiply(new BigDecimal(TaxSetting.getTax2()).divide(Consts.HUNDRED));
                    mCart.mTax2Percent = new BigDecimal(TaxSetting.getTax2());
                }
            }

            if (TaxSetting.getTax3Name() != null) {
                if (!TaxSetting.getTax3Name().equals("")) {
                    mCart.mTax3Name = (TaxSetting.getTax3Name());
                    mCart.mTax3 = mCart.mTaxable3SubTotal.multiply(new BigDecimal(TaxSetting.getTax3()).divide(Consts.HUNDRED));
                    mCart.mTax3Percent = new BigDecimal(TaxSetting.getTax3());
                }
            }

            if (!mCart.mVoided) {
                if (mCart.hasCustomer()) {
                    if (mCart.mTotal.compareTo(BigDecimal.ZERO) > 0) {
                        mCart.getCustomer().sales++;
                    } else {
                        mCart.getCustomer().returns++;
                    }

                    mCart.getCustomer().total = mCart.getCustomer().total.add(mCart.mTotal);

                    mDb.replaceCustomer(mCart.getCustomer());
                }
            }

            mCart.mCashier = mCashier;
            if (!mCart.mVoided) {
                if (mCart.mCashier != null) {
                    if (mCart.mTotal.compareTo(BigDecimal.ZERO) > 0) {
                        mCart.mCashier.sales++;
                    } else {
                        mCart.mCashier.returns++;
                    }

                    mCart.mCashier.total = mCart.mCashier.total.add(mCart.mTotal);

                    if (mCart.mCashier.id > 0)
                        mDb.replaceCashier(mCart.mCashier);
                }
            }

            mCart.mOnHold = false;
            mCart.mIsReceived = false;
            if (TextUtils.isEmpty(gatewayId))
                mCart.mIsProcessed = ReportCart.PROCESS_STATUS_OFFLINE;
            else
                mCart.mIsProcessed = ReportCart.PROCESS_STATUS_APPROVED;
           // mDb.insertSale(mCart, result, 0);

        } catch (JSONException e) {
            Utils.alertBox(getActivity(), R.string.txt_exception, R.string.msg_creating_json_failed);
            e.printStackTrace();
        }

        if (!mCart.mVoided) {
            for (int i = 0; i < mCart.getProducts().size(); i++) {
                if (mCart.getProducts().get(i).id > 0) {
                    Product oldProduct = mCart.getProducts().get(i);
                    Cursor c = mDb.getProdById(oldProduct.id);

                    if (c != null) {
                        Product product = new Product();

                        product.price = new BigDecimal(c.getString(c.getColumnIndex("price")));
                        product.salePrice = new BigDecimal(c.getColumnIndex("salePrice"));
                        product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
                        product.startSale = c.getLong(c.getColumnIndex("saleStartDate"));

                        product.cost = new BigDecimal(c.getString(c.getColumnIndex("cost")));
                        product.id = c.getInt(c.getColumnIndex("_id"));
                        product.barcode = (c.getString(c.getColumnIndex("barcode")));
                        product.name = (c.getString(c.getColumnIndex("name")));
                        product.desc = (c.getString(c.getColumnIndex("desc")));
                        product.onHand = (c.getInt(c.getColumnIndex("quantity")));
                        product.cat = (c.getInt(c.getColumnIndex("catid")));
                        product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
                        product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
                        product.lastReceived = (c.getInt(c.getColumnIndex("lastReceived")));
                        product.lowAmount = (c.getInt(c.getColumnIndex("lowAmount")));
                        product.track = (c.getInt(c.getColumnIndex("track")) != 0);

                        c.close();

                        product.onHand -= oldProduct.quantity;

                        mDb.replaceItem(product);
                    }
                }
            }
        }
    }

    public void onPrintTextDisplay(String message) {
        if (!mSendingToDisplay) {
            mSendingToDisplay = true;
            new SendToDisplay().execute(message);
        }
    }

    public void onPrintTextDisplay(String message1, String message2) {
        new SendToDisplay().execute(message1, message2);
    }

    public Cart getCart() {
        return mCart;
    }

    public void setCart(Cart cart) {
        this.mCart = cart;
    }

    public void setProduct() {
        mItemAdapter.setData(mCart.getProducts());
    }

    public ArrayList<Product> getProducts() {
        return mCart.getProducts();
    }

    public void addProduct(Product product) {
        mCart.addProduct(product);
        notifyChanges();
        updateTotals();
    }

    public void removeAll() {
        mCart.removeAll();
        mItemAdapter.clearData();
        mItemAdapter.notifyDataSetChanged();
        setImageVisibility();
    }

    public void setPayment(Payment payment) {
        mCart.mPayments.clear();
        mCart.mPayments.add(payment);
    }

    public void setPayment(ArrayList<Payment> payments) {
        mCart.mPayments.clear();
        mCart.mPayments = payments;
    }

    public void notifyChanges() {
        mItemAdapter.notifyDataSetChanged();
        mItemRecyclerView.getLayoutManager().smoothScrollToPosition(mItemRecyclerView, null, mItemAdapter.getItemCount());
    }

    public void notifyItemChanged(int position) {
        if (position == -1)
            mItemAdapter.notifyItemInserted(mCart.getProducts().size() - 1);
        else
            mItemAdapter.notifyItemChanged(position);
    }

    private class SendToDisplay extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            for (String t : ReceiptSetting.printers) {
                try {
                    JSONObject object = new JSONObject(t);

                    ReceiptSetting.denabled = true;
                    ReceiptSetting.daddress = object.getString("address");
                    ReceiptSetting.dmake = object.getInt("printer");
                    ReceiptSetting.dsize = object.getInt("size");
                    ReceiptSetting.dtype = object.getInt("type");
                    ReceiptSetting.ddrawer = object.getBoolean("cashDrawer");
                    if (object.has("main"))
                        ReceiptSetting.dmainPrinter = object.getBoolean("main");
                    else
                        ReceiptSetting.dmainPrinter = true;

                    String message = params[0];
                    if (ReceiptSetting.dmake == ReceiptSetting.MAKE_CUSTOM)
                        EscPosDriver.SendToDisplay(getActivity(), message);
                    if (ReceiptSetting.dmake == ReceiptSetting.MAKE_PT6210)
                        EscPosDriver.sendToPT6210Display(getActivity(), message);
                    if (Build.MODEL.contains(Consts.ELO_MODEL) && ReceiptSetting.dmake == ReceiptSetting.MAKE_ELOTOUCH) {
                        mElo.clearEloDisplay();
                        mElo.setEloDisplayLine1(message);
                        mElo.setEloDisplayLine2(params[1].toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mSendingToDisplay = false;
        }
    }

    public void setTransNumber() {
        if (!mCart.mHasTransNumber) {
            BigDecimal currentTrans = new BigDecimal(PrefUtils.getCurrentTrans(getActivity()));
            PrefUtils.updateTransNumber(getActivity(), String.valueOf(currentTrans.add(BigDecimal.ONE)));
            mCart.mHasTransNumber = true;
            mCart.mTrans = currentTrans.add(BigDecimal.ONE);
        }
    }

    public Fragment addNoteToSale() {
        final NoteFragment fragment = NoteFragment.newInstance(NoteFragment.NOTE_SCOPE_ALL, mCart.getProducts(), 0);
        fragment.setNoteListener(new NoteFragment.NoteListener() {
            @Override
            public void onNote(final int position, final String note, final BigDecimal amount) {
                if (PrefUtils.getCashierInfo(getActivity()).permissionInventory) {
                    addNote(position, note, amount);
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    return;
                }
                TenPadDialogFragment newFragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                newFragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                newFragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                    @Override
                    public void onAdminAccessGranted() {
                        addNote(position, note, amount);
                        mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    }

                    @Override
                    public void onAdminAccessDenied() {
                        Utils.alertBox(getActivity(), R.string.txt_admin_login, R.string.msg_admin_login_failed);
                    }
                });
            }

            @Override
            public void onDelete() {
            }
        });

        return fragment;
    }

    public Fragment addDiscountToSale() {
        Answers.getInstance().logCustom(new CustomEvent("Queue")
                .putCustomAttribute("button", "Discount"));

        TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_DISCOUNT, mCart.mTotal);
        Bundle args = fragment.getArguments();
        args.putBoolean(TenPadDialogFragment.BUNDLE_ATTACH_AS_FRAGMENT, true);
        fragment.setDiscountListener(new TenPadDialogFragment.DiscountListener() {
            @Override
            public void onDiscountPrice(final BigDecimal amount) {
                if (PrefUtils.getCashierInfo(getActivity()).permissionPriceModify) {
                    mCart.mDiscountAmount = mCart.mDiscountAmount.add(amount);
                    mCart.mDiscountName = getString(R.string.txt_discount);
                    updateTotals();
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    return;
                }
                TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                    @Override
                    public void onAdminAccessGranted() {
                        mCart.mDiscountAmount = mCart.mDiscountAmount.add(amount);
                        mCart.mDiscountName = getString(R.string.txt_discount);
                        updateTotals();
                        mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    }

                    @Override
                    public void onAdminAccessDenied() {
                        Utils.alertBox(getActivity(), R.string.txt_admin_login, R.string.msg_admin_login_failed);
                    }
                });
            }

            @Override
            public void onDiscountPercent(final BigDecimal percent) {
                if (PrefUtils.getCashierInfo(getActivity()).permissionPriceModify) {
                    mCart.mDiscountPercent = percent;
                    mCart.mDiscountName = String.format(getString(R.string.txt_percent_off), mCart.mDiscountPercent.toString());
                    mCart.mDiscountAmount = BigDecimal.ZERO;
                    updateTotals();
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    return;
                }
                TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_ADMIN);
                fragment.show(getChildFragmentManager(), TAG_DIALOG_FRAGMENT);
                fragment.setTenPadListener(new TenPadDialogFragment.TenPadListener() {

                    @Override
                    public void onAdminAccessGranted() {
                        mCart.mDiscountPercent = percent;
                        mCart.mDiscountName = String.format(getString(R.string.txt_percent_off), mCart.mDiscountPercent.toString());
                        mCart.mDiscountAmount = BigDecimal.ZERO;
                        updateTotals();
                        mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    }

                    @Override
                    public void onAdminAccessDenied() {
                        Utils.alertBox(getActivity(), R.string.txt_admin_login, R.string.msg_admin_login_failed);
                    }
                });

            }
        });

        return fragment;
    }

    public Fragment addNewCustomer() {
        Answers.getInstance().logCustom(new CustomEvent("Queue")
                .putCustomAttribute("button", "Customer"));
        CustomerAddFragment fragment = new CustomerAddFragment();
        return fragment;
    }

    public void printSale() {
        ReceiptDialogFragment fragment = new ReceiptDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ReceiptDialogFragment.BUNDLE_CART, mCart);
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
    }

    public void saveCurrentSale(String firstName) {
        mCart.mName = firstName;
        mCart.mOnHold = true;
        mCart.mIsProcessed = ReportCart.PROCESS_STATUS_OFFLINE;
        mCart.mIsReceived = false;

        JSONObject lineItemJSONObject = new JSONObject();
        try {
            JSONArray productsJSONArray = JSONHelper.toJSONProductArray(getActivity(), mCart);
            lineItemJSONObject.put("Products", productsJSONArray);

            JSONArray paymentJsonArray = JSONHelper.toJSONPaymentArray(getActivity(), mCart, null);
            lineItemJSONObject.put("Payments", paymentJsonArray);

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        //mDb.insertSale(mCart, lineItemJSONObject.toString() ,0);
        mCart.removeAll();
        notifyChanges();
        mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
    }

    public void assignCustomer(Customer customer) {
        mCart.setCustomer(customer);
        mMenuAdapter.add(String.format("%s %s", getString(R.string.txt_order_label), customer.getFullName()));
        mMenuSpinner.setSelection(2);
        setImageVisibility();
    }

    public void assignOpenOrder(String name) {
        mMenuAdapter.add(String.format("%s %s", getString(R.string.txt_open_order_label), name));
        mMenuSpinner.setSelection(2);
    }

    public void removeCustomer() {
        mCart.setCustomer(null);
        mMenuAdapter.remove(mMenuAdapter.getItem(2));
        mMenuSpinner.setSelection(0);
    }

    private class MenuArrayAdapter extends ArrayAdapter {
        private Typeface raleway;

        public MenuArrayAdapter(Context context, @LayoutRes int textViewResourceId, @NonNull List<String> objects) {
            super(context, textViewResourceId, objects);
            raleway = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        }

        public TextView getView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            Utils.setTypeFace(raleway, parent);
            return v;
        }

        public TextView getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            Utils.setTypeFace(raleway, parent);
            return v;
        }
    }

    class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: // New Order
                    if (mCart.mProducts.size() > 0) {
                        mClearImageButton.performClick();
                    }
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                    break;

                case 1: // Open Orders
                    mCallback.onChangeFragment(MainActivity.FRAGMENT_OPEN_ORDERS);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    public void addNote(int position, String note, BigDecimal amount) {
        if (position == -1) {
            Product product = new Product();
            product.name = note;
            product.price = amount;
            product.isNote = true;
            mCart.addProduct(product);
            notifyChanges();
            updateTotals();
        } else {
            Product product = mCart.getProducts().get(position);
            Product m = new Product();
            m.name = note;
            m.price = amount;
            m.isNote = true;
            if (amount.compareTo(BigDecimal.ZERO) == 0)
                m.modifierType = Product.MODIFIER_TYPE_DESC;
            else
                m.modifierType = Product.MODIFIER_TYPE_ADDON;
            product.addModifier(m);
            notifyChanges();
            updateTotals();
        }
    }

    private void setImageVisibility() {

        if (mCart.hasCustomer()) {
            mAddCustomerLayout.setVisibility(View.GONE);
            mRemoveCustomerLayout.setVisibility(View.VISIBLE);
        } else {
            mAddCustomerLayout.setVisibility(View.VISIBLE);
            mRemoveCustomerLayout.setVisibility(View.GONE);
        }
    }


}