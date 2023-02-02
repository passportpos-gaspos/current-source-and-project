package com.pos.passport.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pos.passport.R;


/**
 * Created by karim on 11/9/15.
 */
public class ItemButtonView extends LinearLayout {
    private final String DEBUG_TAG = "[ButtonView]";

    private final int DEFAULT_TEXT_COLOR = R.color.black_color;
    private final int DEFAULT_TEXT_BACKGROUND = R.color.white_color;
    private final int DEFAULT_INITIAL_COLOR = R.color.pos_secondary_blue;
    private final int DEFAULT_INITIAL_BACKGROUND = R.color.white_color;
    private final int PRESSED_INITIAL_BACKGROUND = R.color.gray_700;

    private ImageView mImageView;
    private AutoFitTextView mTextView;
    private String mTitle;
    private TextPaint mInitialPaint;
    private boolean mDraggable;

    public ItemButtonView(Context context, String title, int width) {
        super(context);
        this.setOrientation(VERTICAL);
        setDraggable(true);
        this.mTitle = title;
        init(context, width);
    }

    private void init(Context context, int width) {
        setFocusable(false);
        setPadding(0, 0, dpToPixel(1), dpToPixel(1));
        mImageView = new ImageView(context);
        ViewGroup.LayoutParams imageLayoutParams = new ViewGroup.LayoutParams(dpToPixel(width), dpToPixel(width));
        mImageView.setLayoutParams(imageLayoutParams);
        Typeface mNotoSans = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
        Typeface mNotoSansBold = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Bold.ttf");
        mInitialPaint = new TextPaint();
        mInitialPaint.setTypeface(mNotoSansBold);
        mInitialPaint.setColor(getResources().getColor(DEFAULT_INITIAL_COLOR));
        mInitialPaint.setTextAlign(Paint.Align.CENTER);
        mInitialPaint.setAntiAlias(true);

        mImageView.setImageDrawable(new BitmapDrawable(getResources(), getLetterTile(dpToPixel(width), dpToPixel(width), mTitle, DEFAULT_INITIAL_BACKGROUND)));
        ViewGroup.LayoutParams textLayoutParams = new ViewGroup.LayoutParams(dpToPixel(width), dpToPixel(width/4));
        mTextView = new AutoFitTextView(context);
        mTextView.setSingleLine();
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setPadding(dpToPixel(5), dpToPixel(5), dpToPixel(5), dpToPixel(5));
        mTextView.setMaxLines(1);
        mTextView.setTypeface(mNotoSans);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        mTextView.setLayoutParams(textLayoutParams);

        setTitleColor(DEFAULT_TEXT_COLOR);
        setTitleBackground(DEFAULT_TEXT_BACKGROUND);
        mTextView.setText(mTitle);

        addView(mImageView);
        addView(mTextView);
        setBoarder();
    }

    public void setTitle(String title) {
        mTitle = title;
        mTextView.setText(mTitle);
    }

    public void setTitle(@StringRes int resId) {
        mTitle = getResources().getString(resId);
        mTextView.setText(mTitle);
    }

    public void setTitleColor(@ColorRes int resId) {
        if (Build.VERSION.SDK_INT < 23)
            mTextView.setTextColor(getResources().getColor(resId));
        else
            mTextView.setTextColor(getResources().getColor(resId, null));

    }

    public void setTitleBackground(@ColorRes int resId) {
        if (Build.VERSION.SDK_INT < 23)
            mTextView.setBackgroundColor(getResources().getColor(resId));
        else
            mTextView.setBackgroundColor(getResources().getColor(resId, null));
    }

    public void setImage(@DrawableRes int resId) {
        mImageView.setImageResource(resId);
    }

    public void setImage(Drawable image) {
        mImageView.setImageDrawable(image);
    }

    public Bitmap getLetterTile(int width, int height, final String displayName, final @ColorRes int bgcolor) {
        final char firstChar = displayName.charAt(0);
        char[] titleChar = displayName.toCharArray();
        char secondChar = 0;
        int idx = 0;
        try {
            if ((idx = displayName.indexOf(" ")) > 0) {
                secondChar = displayName.charAt(idx + 1);
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            return null;
        }

        Canvas c = new Canvas();
        c.setBitmap(bitmap);
        if (bgcolor == 0)
            c.drawColor(getResources().getColor(DEFAULT_INITIAL_BACKGROUND));
        else
            c.drawColor(getResources().getColor(bgcolor));

        Rect bounds = new Rect();
        if (isEnglishLetterOrDigit(firstChar)) {
            titleChar[0] = Character.toUpperCase(firstChar);

            if (isEnglishLetterOrDigit(secondChar)) {
                titleChar[1] = Character.toUpperCase(secondChar);
                mInitialPaint.setTextSize((int)(width * 0.6));
                mInitialPaint.getTextBounds(titleChar, 0, 2, bounds);
                c.drawText(titleChar, 0, 2, width / 2, height / 2 + (bounds.bottom - bounds.top) / 2, mInitialPaint);
            } else {
                mInitialPaint.setTextSize((int) (width * 0.8));
                mInitialPaint.getTextBounds(titleChar, 0, 1, bounds);
                c.drawText(titleChar, 0, 1, width / 2, height / 2 + (bounds.bottom - bounds.top) / 2, mInitialPaint);
            }
        }

        return bitmap;
    }

    private static boolean isEnglishLetterOrDigit(char c) {
        return ('A' <= c && c <= 'Z')
                || ('a' <= c && c <= 'z')
                || ('0' <= c && c <= '9');
    }

    public int dpToPixel(float dp) {
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    public void setMaxTextSize(int maxTextSizeInDp) {
        int maxTextSize = dpToPixel(maxTextSizeInDp);
        mTextView.setMaxTextSize(maxTextSize);
    }

    public void setDraggable(boolean draggable) {
        mDraggable = draggable;
    }

    public boolean isDraggable() {
        return mDraggable;
    }

    public void setBoarder(){
        GradientDrawable border = new GradientDrawable();
        border.setColor(0xE0E0E0);
        border.setStroke(1, 0xE0E0E0);
        this.setPadding(1, 1, 1,1);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackgroundDrawable(border);
        } else {
            this.setBackground(border);
        }
    }

}

