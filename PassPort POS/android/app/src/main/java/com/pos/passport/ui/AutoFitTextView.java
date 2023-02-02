package com.pos.passport.ui;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by karim on 11/10/15.
 */
public class AutoFitTextView extends TextView {
    private int mMaxTextSize;
    private TextPaint mTestPaint;

    public AutoFitTextView(Context context) {
        super(context);
        initialize();
    }

    public AutoFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        mTestPaint = new TextPaint();
        mTestPaint.set(this.getPaint());
    }

    private void resizeText(String text, int textWidth, int textHeight) {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        int targetHeight = textHeight - this.getPaddingTop() - this.getPaddingBottom();
        float hi = 600;
        float lo = 10;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }

        lo = 2;
        while ((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if (getTextHeight(text.subSequence(0, 1), mTestPaint, textWidth, size) >= targetHeight)
                hi = size;
            else
                lo = size;
        }

        if (mMaxTextSize > 0)
            lo = Math.min(lo, mMaxTextSize);
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        resizeText(text.toString(), this.getWidth(), getHeight());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            resizeText(this.getText().toString(), w, h);
        }
    }

    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        TextPaint paintCopy = new TextPaint(paint);
        paintCopy.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(source, paintCopy, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, true);
        return layout.getHeight();
    }

    public void setMaxTextSize(int maxTextSize) {
        this.mMaxTextSize = maxTextSize;
    }
}
