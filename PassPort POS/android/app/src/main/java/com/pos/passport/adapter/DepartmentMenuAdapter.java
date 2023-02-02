package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.model.Category;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 11/4/2016.
 */

public class DepartmentMenuAdapter extends BaseAdapter {

    private List<Category> menus;
    private Context mContext;
    private Typeface mNotoSans;
    private int mLayoutResourceId;
    private SetOnItemClick mListener;
    private int mSelectedId;
    private String mSelectedItem;
    int clr=0;
    int pos_dep=0;
    private static final int[] FROM_COLOR = new int[]{49, 179, 110};
    private static final int THRESHOLD = 3;
    int[] androidColors;

    public interface SetOnItemClick{
        void onItemClick(int id,int posg);
    }

    public DepartmentMenuAdapter(Context context, ArrayList<Category> menus, int resourceId)
    {
        this.menus = menus;
        this.mContext = context;
        androidColors = mContext.getResources().getIntArray(R.array.androidcolors);
        this.mLayoutResourceId = resourceId;
        mNotoSans = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getItemId(){
        return mSelectedId;
    }

    public String getItemName(){
        return mSelectedItem;
    }

    public void setListener(SetOnItemClick listener){
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Category navMenu = menus.get(position);
        if(convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(mContext.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) convertView);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text_view);
        LinearLayout frmrow = (LinearLayout) convertView.findViewById(R.id.frmrow);
        ImageView imgarr = (ImageView) convertView.findViewById(R.id.imgarr);
        textView.setTypeface(mNotoSans);
        textView.setText(navMenu.getName());
        textView.setTag(navMenu.getId());
        imgarr.setTag(navMenu.getId());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pos_dep = position;
                //Log.e("NavRightListAdapter","NavRightListAdapter click fire>>>"+navMenu.getName());
                mSelectedId = navMenu.getId();
                mSelectedItem = navMenu.getName();
                mListener.onItemClick(navMenu.getId(),pos_dep);

            }
        });
        int mColorCode = androidColors[position];
        //Log.e("Color code","new one >>"+mColorCode);
        //textView.setBackgroundColor(randomAndroidColor);
        /*BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER,randomAndroidColor);
        myBubble.setCornerRadius(0);
        myBubble.setPointerAlignment(BubbleDrawable.CENTER);
        myBubble.setPadding(0, 0, 0, 0);
        frmrow.setBackgroundDrawable(myBubble);*/
        //Log.e("Color code","Seco>>"+mColorCode);
        frmrow.setBackgroundColor(mColorCode);

        //   Drawable d = mContext.getResources().getDrawable(R.drawable.playnew);
        //   d.setColorFilter(new
        //            PorterDuffColorFilter(randomAndroidColor, PorterDuff.Mode.MULTIPLY));
        //imgarr.setImageDrawable(adjust(d,randomAndroidColor));
        //    imgarr.setImageDrawable(d);
        //Convert drawable in to bitmap
        //Bitmap sourceBitmap = Utils.convertDrawableToBitmap(d);
        //Bitmap mFinalBitmap = Utils.changeImageColor(sourceBitmap, mColorCode);
        //imgarr.setImageBitmap(mFinalBitmap);
        imgarr.setColorFilter(mColorCode);

        if(pos_dep==position)
            imgarr.setVisibility(View.VISIBLE);
        else
            imgarr.setVisibility(View.INVISIBLE);

        //clr=clr+1;
        //if(groupPosition > androidColors.length)
        //    clr=0;

        return convertView;
    }
}
