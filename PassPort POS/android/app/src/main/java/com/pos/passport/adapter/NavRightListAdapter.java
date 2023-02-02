package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.model.Category;
import com.pos.passport.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 4/25/2016.
 */
public class NavRightListAdapter extends BaseExpandableListAdapter {

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

    public NavRightListAdapter(Context context, ArrayList<Category> menus, int resourceId, int[] androidColors)
    {
        this.menus = menus;
        this.mContext = context;
        this.androidColors = androidColors;
        //androidColors = mContext.getResources().getIntArray(R.array.androidcolors);
        this.mLayoutResourceId = resourceId;
        mNotoSans = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    public void setListener(SetOnItemClick listener){
        mListener = listener;
    }

    @Override
    public int getGroupCount()
    {
        return menus.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return menus.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return menus.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return menus.get(groupPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public int getItemId(){
        return mSelectedId;
    }

    public String getItemName(){
        return mSelectedItem;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Category navMenu = menus.get(groupPosition);
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
                pos_dep=groupPosition;
                //Log.e("NavRightListAdapter","NavRightListAdapter click fire>>>"+navMenu.getName());
                mSelectedId = navMenu.getId();
                mSelectedItem = navMenu.getName();
                mListener.onItemClick(navMenu.getId(),pos_dep);

            }
        });
        //int mColorCode = androidColors[groupPosition];
        int mColorCode = androidColors[groupPosition];
        frmrow.setBackgroundColor(mColorCode);
        imgarr.setColorFilter(mColorCode);

        if(pos_dep==groupPosition)
            imgarr.setVisibility(View.VISIBLE);
        else
            imgarr.setVisibility(View.INVISIBLE);

        //clr=clr+1;
        //if(groupPosition > androidColors.length)
        //    clr=0;

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    private Drawable adjust(Drawable d, int clr)
    {
        int to = clr;// Color.RED;

        //Need to copy to ensure that the bitmap is mutable.
        Bitmap src = ((BitmapDrawable) d).getBitmap();
        Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
        for(int x = 0;x < bitmap.getWidth();x++)
            for(int y = 0;y < bitmap.getHeight();y++)
                if(match(bitmap.getPixel(x, y)))
                    bitmap.setPixel(x, y, to);

        return new BitmapDrawable(bitmap);
    }

    private boolean match(int pixel)
    {
        //There may be a better way to match, but I wanted to do a comparison ignoring
        //transparency, so I couldn't just do a direct integer compare.
        return Math.abs(Color.red(pixel) - FROM_COLOR[0]) < THRESHOLD &&
                Math.abs(Color.green(pixel) - FROM_COLOR[1]) < THRESHOLD &&
                Math.abs(Color.blue(pixel) - FROM_COLOR[2]) < THRESHOLD;
    }
}
