package com.pos.passport.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.adapter.GridViewAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.MenuButtonInterface;
import com.pos.passport.model.ItemButton;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.util.EscPosDriver;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuButtonFragment extends Fragment {

    private GridView mButtonGridView;
    protected ItemButton mCurrentFolder;
    private ProductDatabase mDb;
    private List<ItemButton> mItemButtons;
    // private ItemButtonAdapter mAdapter;
    private GridViewAdapter mAdapter;
    private MenuButtonInterface mCallback;
    private View mTitleBarLayout;
    private ImageView mBackImageView;
    private TextView mTitleTextView;
    private FrameLayout mHeaderFrameLayout;
    // QueueInterface mCallback_call;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try
        {
            mCallback = (MenuButtonInterface) context;
            //mCallback_call = (QueueInterface) context;

        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + " must implement MenuButtonInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("Fragment", "Buttons Fragment");
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled (true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_button, container, false);
        mDb = ProductDatabase.getInstance(getActivity());
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUpButtons();
        setUpListeners();
    }

    private void bindUIElements(View view) {
        mButtonGridView = (GridView) view.findViewById(R.id.dynamic_grid_view);
        mTitleBarLayout = (View) view.findViewById(R.id.layout_title_bar);
        mBackImageView = (ImageView) view.findViewById(R.id.back_image_view);
        mTitleTextView = (TextView) view.findViewById(R.id.folder_name_text_view);
        mHeaderFrameLayout = (FrameLayout) view.findViewById(R.id.header_frame_layout);

    }

    private void setUpButtons() {
        mItemButtons = new ArrayList<>();
        setButtons(0);
        mAdapter = new GridViewAdapter(getActivity(), mItemButtons);
        mButtonGridView.setAdapter(mAdapter);
        if (getActivity() instanceof MainActivity)
            // mButtonGridView.setRearrangeEnabled(false);
            mButtonGridView.setClickable(true);
        //mTitleBarLayout.setVisibility(View.GONE);
    }

    private void setUpListeners()
    {
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //mCallback_call.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
                update();
            }
        });
        mButtonGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                ItemButton itemButton = (ItemButton) mAdapter.getItem(position);
                int newType = itemButton.type; //c.getInt(c.getColumnIndex("type"));
                int newParent = itemButton.parent; //c.getInt(c.getColumnIndex("_id"));
                int productID = itemButton.productID; //c.getInt(c.getColumnIndex("productID"));
                int departID = itemButton.departID; //c.getInt(c.getColumnIndex("departID"));
                String folderName = itemButton.folderName; //c.getString(c.getColumnIndex("folderName"));
                String link = itemButton.link; //c.getString(c.getColumnIndex("link"));

                Answers.getInstance().logCustom(new CustomEvent("MenuButton")
                        .putCustomAttribute("type", newType)
                        .putCustomAttribute("productId", productID)
                        .putCustomAttribute("folderName", folderName));

                /*
                if (saleProcessed) {
					Utils.alertBox(getActivity(), getString(R.string.txt_sale_processed), getString(R.string.msg_sale_processed));
					return;
				}
				*/
                //Log.e("New type ","Click new type"+newType);
                if (newType == 5) {
                    long quan = 1;
                    ArrayList<Product> products = getItemList(link);
                    for (int i = 0; i < products.size(); i++) {
                        products.get(i).quantity = (int) quan;
                        mCallback.onAddProduct(products.get(i));
                    }
                }
                if (newType == 2)
                {
                    Cursor productC = mDb.getProdById(productID);

                    if (productC != null) {
                        long quan = 1;

                        Product product = new Product();

                        product.price = new BigDecimal(productC.getString(productC.getColumnIndex("price")));
                        //Log.e("sale price", "sprice before>>>" + productC.getString(productC.getColumnIndex("salePrice")));
                        product.endSale = productC.getLong(productC.getColumnIndex("saleEndDate"));
                        product.startSale = productC.getLong(productC.getColumnIndex("saleStartDate"));

                        long now = new Date().getTime();
                        if (now >= product.startSale && now <=  product.endSale)
                            product.salePrice = new BigDecimal(productC.getString(productC.getColumnIndex("salePrice")));
                        else
                            product.salePrice = BigDecimal.ZERO;

                        product.cost = new BigDecimal(productC.getString(productC.getColumnIndex("cost")));

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
                        //Log.e("modi_data"," at add time>>"+(productC.getString(productC.getColumnIndex("modifiers"))));
                        product.comboItems = (productC.getString(productC.getColumnIndex("comboItems")));
                        product.combo = (productC.getInt(productC.getColumnIndex("combo")));
                        int taxone=(productC.getInt(productC.getColumnIndex("taxable")));
                        if(taxone ==0)
                            product.taxable = false;
                        else
                            product.taxable = true;
                        int isAlcoholic=(productC.getInt(productC.getColumnIndex("isAlcoholic")));
                        if(isAlcoholic ==0)
                            product.isAlcoholic = false;
                        else
                            product.isAlcoholic = true;
                        int isTobaco=(productC.getInt(productC.getColumnIndex("isTobaco")));
                        if(isTobaco ==0)
                            product.isTobaco = false;
                        else
                            product.isTobaco = true;


                        mCallback.onAddProduct(product);

                        productC.close();
                        itemButton.quantity=(int)(itemButton.quantity-1);
                        //mAdapter.onMethodCallback(position);
                        mAdapter.notifyDataSetChanged();
                    }

                }

                if (newType == 3) {
                    int quan = 1;

                    long price = 1;

                    Product product = new Product();
                    product.name = mDb.getCatById(departID);
                    product.cat = departID;
                    product.price = new BigDecimal(price);
                    product.quantity = quan;

                    mCallback.onAddProduct(product);
                }

                if (newType == 4)
                {
                    if (mCallback.getProducts().size() == 0) {
                        Utils.alertBox(getActivity(), getString(R.string.txt_no_products), getString(R.string.msg_enter_a_product));
                        return;
                    }

                    //if (mCallback.getCart().mTotal.compareTo(BigDecimal.ZERO) <= 0 && !cashier.permissionReturn) {
                    if (mCallback.getCart().mTotal.compareTo(BigDecimal.ZERO) <= 0) {
                        Utils.alertBox(getActivity(), getString(R.string.txt_invalid_permission), getString(R.string.msg_need_return_permission));
                        return;
                    }

                    BigDecimal paymentSum = BigDecimal.ZERO;
                    for (int p = 0; p < mCallback.getCart().mPayments.size(); p++) {
                        paymentSum = paymentSum.add(mCallback.getCart().mPayments.get(p).paymentAmount);
                    }

                    BigDecimal amount = mCallback.getCart().mTotal.subtract(paymentSum);

                    Payment payment = new Payment();
                    payment.paymentType = folderName;
                    payment.paymentAmount = new BigDecimal(amount.toString());

                    mCallback.getCart().mPayments.add(payment);
                    mCallback.onNotifyQueueChanged();
                }

                if (newType == 1) {
                    mCurrentFolder = mDb.getButtonByID(newParent);
                    setButtons(itemButton.id);
                    mTitleTextView.setText(WordUtils.capitalize(folderName));
                    mAdapter.notifyDataSetChanged();
                }

                if (newType == 7) {
                    Product product = new Product();
                    product.name = folderName;
                    product.price = BigDecimal.ZERO;
                    product.isNote = true;

                    if (folderName.equalsIgnoreCase(getString(R.string.txt_no_sale)))
                    {
                        EscPosDriver.kickDrawer(getActivity());
                    } else
                    {
                        mCallback.onAddProduct(product);
                    }
                }

                if (newType == -1)
                {
                    setButtons(0);
                    mAdapter.notifyDataSetChanged();
                    mCurrentFolder = mDb.getButtonByID(mCurrentFolder.parent);
                }
            }
        });
    }


    public ArrayList<Product> getItemList(String itemString) {
        Log.v("items", itemString);
        String[] items = itemString.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

        ArrayList<Product> results = new ArrayList<Product>();

        for (int i = 0; i < items.length; i++) {
            Log.v("items", items[i]);

            Cursor c = mDb.getProdById(Integer.valueOf(items[i]));

            if (c != null) {
                Product product = new Product();

                product.id = c.getInt(c.getColumnIndex("_id"));
                product.name = c.getString(c.getColumnIndex("name"));

                results.add(product);
            }
        }

        return results;
    }

    private void setButtons(int parent) {
        mItemButtons.clear();

        Cursor c = mDb.getButtons(parent);
        if(parent > 0 ) {
            mHeaderFrameLayout.setVisibility(View.VISIBLE);
        }
        else
            mHeaderFrameLayout.setVisibility(View.GONE);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast())
            {
                ItemButton itemButton = new ItemButton();
                itemButton.id = c.getInt(c.getColumnIndex("_id"));
                itemButton.type = c.getInt(c.getColumnIndex("type"));
                itemButton.order = c.getInt(c.getColumnIndex("orderBy"));
                itemButton.parent = c.getInt(c.getColumnIndex("parent"));
                itemButton.productID = c.getInt(c.getColumnIndex("productID"));
                itemButton.departID = c.getInt(c.getColumnIndex("departID"));
                itemButton.folderName = c.getString(c.getColumnIndex("folderName"));
                itemButton.link = c.getString(c.getColumnIndex("link"));
                itemButton.price = c.getString(c.getColumnIndex("price"));

                itemButton.startdate = c.getString(c.getColumnIndex("saleStartDate"));
                itemButton.enddate = c.getString(c.getColumnIndex("saleEndDate"));

                long now = new Date().getTime();
                if (now >= Long.parseLong(itemButton.startdate) && now <=  Long.parseLong(itemButton.enddate)) {
                    itemButton.saleprice = c.getString(c.getColumnIndex("salePrice"));
                }
                else {
                    itemButton.saleprice = "0";
                }
                //itemButton.saleprice = c.getString(c.getColumnIndex("salePrice"));

                itemButton.trackable = c.getInt(c.getColumnIndex("isTrackable"));
                itemButton.reorderLevel = c.getInt(c.getColumnIndex("reorderLevel"));
                if (itemButton.type == ItemButton.TYPE_PRODUCT)
                {
                    itemButton.quantity = mDb.getProductQuantity(itemButton.productID);
                }else
                {
                    itemButton.quantity=0;
                }
                final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));
                if (imageBlob != null) {
                    itemButton.image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                }

                if (itemButton.type == -1)
                {
                    itemButton.folderName = getString(R.string.txt_back);
                }
                else if (itemButton.type == ItemButton.TYPE_PRODUCT)
                {
                    itemButton.folderName = mDb.getProductNameById(itemButton.productID);
                } else if (itemButton.type == ItemButton.TYPE_DEPARTMENT)
                {
                    itemButton.folderName = mDb.getCatById(itemButton.departID);
                }
                mItemButtons.add(itemButton);
                c.moveToNext();
            }
        }
    }

    public void update() {
        setButtons(0);
        mAdapter.notifyDataSetChanged();
        mButtonGridView.setAdapter(mAdapter);
    }


}
