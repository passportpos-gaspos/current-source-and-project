package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.model.ItemButton;
import com.pos.passport.util.ImageLoaderImage;
import com.pos.passport.util.Utils;

import org.apache.commons.lang3.text.WordUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private int layoutResourceId;
    private List<ItemButton> mItemButtons;
    private float buttonWidth = 500;
    private float buttonHeight = 180;

    public void onMethodCallback(int pos) {
        mItemButtons.get(pos).quantity = (int) (mItemButtons.get(pos).quantity - 1);
    }

    public GridViewAdapter(Context context, List<ItemButton> itemButtons) {
        this.context = context;
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
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.grid_item_layout, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
            holder = new ViewHolder();
            holder.frm_one = (FrameLayout) row.findViewById(R.id.frm_one);
            holder.frm_two = (FrameLayout) row.findViewById(R.id.frm_two);
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.pricetxt = (TextView) row.findViewById(R.id.txt_price);
            holder.imageTitle_one = (TextView) row.findViewById(R.id.text_one);
            holder.pricetxt_one = (TextView) row.findViewById(R.id.txt_price_one);
            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.imageone = (ImageView) row.findViewById(R.id.imageone);
            holder.dis_icon = (ImageView) row.findViewById(R.id.discounticon);
            holder.level_icon = (ImageView) row.findViewById(R.id.level_icon);
            holder.error_icon = (ImageView) row.findViewById(R.id.una_icon);
            holder.dis_icon_one = (ImageView) row.findViewById(R.id.discounticon_one);
            holder.level_icon_one = (ImageView) row.findViewById(R.id.level_icon_one);
            holder.error_icon_one = (ImageView) row.findViewById(R.id.una_icon_one);
            holder.img_level = (ImageView) row.findViewById(R.id.img_showlevel);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.imageTitle.setText(WordUtils.capitalize(itemButton.folderName));
        holder.imageTitle_one.setText(WordUtils.capitalize(itemButton.folderName));

        if (itemButton.price == null || itemButton.price.equalsIgnoreCase("")) {
            holder.pricetxt.setText("");
            holder.pricetxt_one.setText("");
        } else {
            holder.pricetxt.setText("$" + Utils.formatTotal(itemButton.price));
            holder.pricetxt_one.setText("$" + Utils.formatTotal(itemButton.price));
        }

        if (itemButton.type == ItemButton.TYPE_PRODUCT) {
            if (itemButton.startdate.equalsIgnoreCase("0") || itemButton.enddate.equalsIgnoreCase("0")) {
                holder.dis_icon_one.setVisibility(View.GONE);
                holder.dis_icon.setVisibility(View.GONE);
                holder.img_level.setVisibility(View.GONE);
            } else {
                long now = new Date().getTime();
                BigDecimal sum;
                if (now >= Long.valueOf(itemButton.startdate) && now <= Long.valueOf(itemButton.enddate)) {
                    holder.dis_icon_one.setVisibility(View.VISIBLE);
                    holder.dis_icon.setVisibility(View.VISIBLE);
                    holder.img_level.setVisibility(View.VISIBLE);
                    DrawableCompat.setTint(holder.img_level.getDrawable(), ContextCompat.getColor(context, R.color.img_level_green));

                } else {
                    holder.dis_icon_one.setVisibility(View.GONE);
                    holder.dis_icon.setVisibility(View.GONE);
                    holder.img_level.setVisibility(View.GONE);
                }
            }
            if (itemButton.trackable == 0) {
                holder.level_icon_one.setVisibility(View.GONE);
                holder.error_icon_one.setVisibility(View.GONE);
                holder.level_icon.setVisibility(View.GONE);
                holder.error_icon.setVisibility(View.GONE);
            } else {
                if (itemButton.quantity < itemButton.reorderLevel) {
                    holder.level_icon_one.setVisibility(View.VISIBLE);
                    holder.level_icon.setVisibility(View.VISIBLE);
                    holder.img_level.setVisibility(View.VISIBLE);
                    DrawableCompat.setTint(holder.img_level.getDrawable(), ContextCompat.getColor(context, R.color.img_level_orange));

                } else {
                    holder.level_icon_one.setVisibility(View.GONE);
                    holder.level_icon.setVisibility(View.GONE);
                    holder.img_level.setVisibility(View.GONE);
                }
                if (itemButton.quantity <= 0) {
                    holder.error_icon_one.setVisibility(View.VISIBLE);
                    holder.error_icon.setVisibility(View.VISIBLE);
                    holder.level_icon_one.setVisibility(View.GONE);
                    holder.level_icon.setVisibility(View.GONE);
                    holder.frm_one.setAlpha(0.5f);
                    holder.img_level.setVisibility(View.VISIBLE);
                    DrawableCompat.setTint(holder.img_level.getDrawable(), ContextCompat.getColor(context, R.color.img_level_reddark));
                } else {
                    holder.img_level.setVisibility(View.GONE);
                    holder.error_icon_one.setVisibility(View.GONE);
                    holder.error_icon.setVisibility(View.GONE);
                }
            }
        }
        if (itemButton.link.equalsIgnoreCase("") || itemButton.link.equalsIgnoreCase("null")) {
        } else {
            String newSrc = (String) holder.image.getTag();
            if (newSrc != null) {
                if (newSrc.equals(itemButton.link)) {
                } else {
                    ImageLoaderImage imLoader = ImageLoaderImage.getImLoader(context);
                    //holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imLoader.DisplayImage(itemButton.link, holder.image);
                    holder.image.setTag(itemButton.link);
                }
            } else {
                ImageLoaderImage imLoader = ImageLoaderImage.getImLoader(context);
                imLoader.DisplayImage(itemButton.link, holder.image);
                holder.image.setTag(itemButton.link);
            }
        }

        if (itemButton.link.equalsIgnoreCase("") || itemButton.link.equalsIgnoreCase("null")) {
            holder.frm_one.setVisibility(View.GONE);
            holder.frm_two.setVisibility(View.VISIBLE);
        } else {
            holder.frm_one.setVisibility(View.VISIBLE);
            holder.frm_two.setVisibility(View.GONE);
        }
        return row;
    }


    static class ViewHolder {
        TextView imageTitle, imageTitle_one, pricetxt, pricetxt_one;
        ImageView imageone, dis_icon, level_icon, error_icon, dis_icon_one, level_icon_one, error_icon_one, image, img_level;
        FrameLayout frm_one, frm_two;
    }
}