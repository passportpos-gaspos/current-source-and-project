package com.pos.passport.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by imd-macmini on 8/29/16.
 */

public class VertiText extends TextView {

    private int _width, _height;
    private final Rect _bounds = new Rect();

    public VertiText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VertiText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VertiText(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // vise versa
        _height = getMeasuredWidth();
        _width = getMeasuredHeight();
        setMeasuredDimension(_width, _height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.translate(_width, _height);
        canvas.rotate(-90);

        TextPaint paint = getPaint();
        paint.setColor(getTextColors().getDefaultColor());

        String text = text();

        paint.getTextBounds(text, 0, text.length(), _bounds);
        canvas.drawText(text, getCompoundPaddingLeft(), (_bounds.height() - _width) / 2, paint);

        canvas.restore();
    }

    private String text() {
        return super.getText().toString();
    }
}