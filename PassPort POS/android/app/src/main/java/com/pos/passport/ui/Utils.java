package com.pos.passport.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Kareem on 5/22/2016.
 */
public class Utils {

    public static void setTextAppearance(Context context, TextView view, int resId){
        if (Build.VERSION.SDK_INT < 23) {
            view.setTextAppearance(context, resId);

        } else {
            view.setTextAppearance(resId);
        }
    }

    public static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            return android.support.v4.content.ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static int setPadding(Context context, int dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return ((int) (dp * scale + 0.5f));
    }

    public static float pixelToDensity(Context context, float px){
        float density = context.getResources().getDisplayMetrics().density;
        return px/density;
    }

    public static Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getDrawable(drawableRes);
        } else {
            //noinspection deprecation
            return context.getResources().getDrawable(drawableRes);
        }
    }

    public static void setBackgroundDrawable(Context context, View view, @DrawableRes int drawableRes){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(getDrawable(context, drawableRes));
        } else {
            view.setBackground(getDrawable(context, drawableRes));
        }
    }
}
