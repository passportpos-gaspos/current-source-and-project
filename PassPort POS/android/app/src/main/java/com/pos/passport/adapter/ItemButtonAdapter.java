package com.pos.passport.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pos.passport.R;
import com.pos.passport.model.ItemButton;
import com.pos.passport.ui.ItemButtonView;
import com.pos.passport.util.Utils;

import java.util.List;

/**
 * Created by karim on 11/13/15.
 */
public class ItemButtonAdapter extends BaseAdapter {
    private List<ItemButton> mItemButtons;
    private Context mContext;

    public ItemButtonAdapter(Context context, List<ItemButton> itemButtons) {
        this.mContext = context;
        this.mItemButtons = itemButtons;
    }

    @Override
    public int getCount() {
        return mItemButtons.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemButtons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItemButtons.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemButton itemButton = mItemButtons.get(position);
        ItemButtonView view;
        convertView = new ItemButtonView(mContext, itemButton.folderName, 100);
        view = (ItemButtonView)convertView;
        view.setMaxTextSize(20);
        Utils.setTypeFace(Typeface.createFromAsset(mContext.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) convertView);
        if (itemButton.image != null) {
            view.setImage(new BitmapDrawable(mContext.getResources(), itemButton.image));
        }

        if (itemButton.type == -1) {
            //view.setImage(R.drawable.back);
            view.setDraggable(false);
            view.setTitle(R.string.txt_back);
        } else if (itemButton.type == ItemButton.TYPE_TENDER) {
            //view.setTitle(itemButton.folderName);
            if(itemButton.folderName.equals("Cash")) {
               // view.setImage(R.drawable.bill);
                //view.setDraggable(false);
            } if(itemButton.folderName.equals("Credit Card")) {
                view.setDraggable(false);
                //view.setImage(R.drawable.credit_card);
            } if(itemButton.folderName.equals("Check")) {
                //view.setImage(R.drawable.check);
                view.setDraggable(false);
            }
        } else {
            view.setTitle(itemButton.folderName);
        }

        return convertView;
    }

    public List<ItemButton> getItemButtons() {
        return mItemButtons;
    }

}
