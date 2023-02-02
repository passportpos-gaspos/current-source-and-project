package com.pos.passport.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pos.passport.R;
import com.pos.passport.activity.MainActivity;
import com.pos.passport.adapter.GridViewAdapter;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.MenuButtonInterface;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.ItemButton;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kareem on 4/25/2016.
 */
public class DepartmentButtonFragment extends Fragment {


    private GridView mButtonGridView;
    protected ItemButton mCurrentFolder;
    private ProductDatabase mDb;
    private List<ItemButton> mItemButtons;
   // private ItemButtonAdapter mAdapter;
    private GridViewAdapter mAdapter;
    private MenuButtonInterface mMenuCallback;
    private QueueInterface mCallback;
    private TextView mCloseImageView;
    private TextView mTitleTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mMenuCallback = (MenuButtonInterface) context;
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
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
        mCloseImageView = (TextView) view.findViewById(R.id.back_button);
        mTitleTextView = (TextView) view.findViewById(R.id.title_text_view);
        mCloseImageView.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
    }

    private void setUpButtons() {
        mItemButtons = new ArrayList<>();
        setButtons(getArguments().getInt("id"));
        mTitleTextView.setText(getArguments().getString("name"));
       // mAdapter = new ItemButtonAdapter(getActivity(), mItemButtons);
       // mButtonGridView.setAdapter(mAdapter);
       // if (getActivity() instanceof MainActivity)
       // mButtonGridView.setRearrangeEnabled(false);
       // mButtonGridView.setClickable(true);
        mAdapter = new GridViewAdapter(getActivity(), mItemButtons);
        mButtonGridView.setAdapter(mAdapter);
//        mAdapter.setAdapterCallback(new GridViewAdapter.AdapterCallback()
//        {
//            @Override
//            public void onMethodCallback()
//            {
//
//            }
//        });
        if (getActivity() instanceof MainActivity)
            // mButtonGridView.setRearrangeEnabled(false);
            mButtonGridView.setClickable(true);
        //mTitleBarLayout.setVisibility(View.GONE);
    }

    private void setUpListeners() {
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            }
        });
        mButtonGridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                ItemButton itemButton = (ItemButton) mAdapter.getItem(position);
                int newType = itemButton.type; //c.getInt(c.getColumnIndex("type"));
                int newParent = itemButton.parent; //c.getInt(c.getColumnIndex("_id"));
                int productID = itemButton.productID; //c.getInt(c.getColumnIndex("productID"));
                int departID = itemButton.departID; //c.getInt(c.getColumnIndex("departID"));
                String folderName = itemButton.folderName; //c.getString(c.getColumnIndex("folderName"));
                String link = itemButton.link;

                Answers.getInstance().logCustom(new CustomEvent("MenuButton")
                        .putCustomAttribute("type", newType)
                        .putCustomAttribute("productId", productID)
                        .putCustomAttribute("folderName", folderName));

                Cursor productC = mDb.getProdById(productID);

                if (productC != null) {
                    long quan = 1;

                    Product product = new Product();
                    product.price = new BigDecimal(productC.getString(productC.getColumnIndex("price")));
                    //product.salePrice = new BigDecimal(productC.getString(productC.getColumnIndex("salePrice")));
                    product.startSale = productC.getLong(productC.getColumnIndex("saleStartDate"));
                    product.endSale = productC.getLong(productC.getColumnIndex("saleEndDate"));

                    long now = new Date().getTime();
                    Log.e("Now",""+now);

                    if (now >= product.startSale && now <=  product.endSale)
                    {
                        Log.e("if sale","on sale");
                        product.salePrice = new BigDecimal(productC.getString(productC.getColumnIndex("salePrice")));
                    }
                    else
                    {
                        Log.e("else sale"," else on sale");
                        product.salePrice = BigDecimal.ZERO;
                    }



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
                    if(taxone == 0)
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
                    //Log.e("combo","DepartmentButtonFragment>>> "+product.combo);
                    //Log.e("comboItems","DepartmentButtonFragment>>>> "+product.comboItems);
                    //Log.e("modi_data","DepartmentButtonFragment>>>> "+product.modi_data);
                    mMenuCallback.onAddProduct(product);

                    productC.close();
                    itemButton.quantity=(int)(itemButton.quantity-1);
                    //mAdapter.onMethodCallback(position);
                    mAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    private void setButtons(int id)
    {
        mItemButtons.clear();
        Cursor c = mDb.getProductByCategory(id);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ItemButton itemButton = new ItemButton();
                itemButton.id = c.getInt(c.getColumnIndex("_id"));
                itemButton.type = ItemButton.TYPE_PRODUCT;
                itemButton.productID = c.getInt(c.getColumnIndex("_id"));
                itemButton.departID = c.getInt(c.getColumnIndex("catid"));
                itemButton.folderName = c.getString(c.getColumnIndex("name"));
                itemButton.link = c.getString(c.getColumnIndex("image"));
                itemButton.price = c.getString(c.getColumnIndex("price"));

                itemButton.quantity=c.getInt(c.getColumnIndex("quantity"));//mDb.getProductQuantityCategory(id);
                itemButton.startdate = c.getString(c.getColumnIndex("saleStartDate"));
                itemButton.enddate = c.getString(c.getColumnIndex("saleEndDate"));
                //itemButton.saleprice = c.getString(c.getColumnIndex("salePrice"));
                long now = new Date().getTime();
                if (now >= Long.parseLong(itemButton.startdate) && now <=  Long.parseLong(itemButton.enddate)) {
                    itemButton.saleprice = c.getString(c.getColumnIndex("salePrice"));
                }
                else {
                    itemButton.saleprice = "0";
                }
                itemButton.trackable = c.getInt(c.getColumnIndex("isTrackable"));
                itemButton.reorderLevel = c.getInt(c.getColumnIndex("reorderLevel"));
                mItemButtons.add(itemButton);
                c.moveToNext();

                /*final byte[] imageBlob = c.getBlob(c.getColumnIndexOrThrow("image"));
                if (imageBlob != null) {
                    itemButton.image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
                }*/



            }
        }
    }
}
