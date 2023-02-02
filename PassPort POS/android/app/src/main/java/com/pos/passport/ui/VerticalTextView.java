package com.pos.passport.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by imd-macmini on 7/27/16.
 */
public class VerticalTextView extends TextView {
    final boolean topDown;

    public VerticalTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        final int gravity = getGravity();
        if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM)
        {
            setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.CENTER);
            topDown = false;
        }else
            topDown = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas){
        TextPaint textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.drawableState = getDrawableState();

        canvas.save();

        //if(topDown){
        //    canvas.translate(getWidth(), 0);
        //    canvas.rotate(90);
        //}else {
            //canvas.translate(0, getHeight());
           // canvas.rotate(-90);

        TextView temp = new TextView(getContext());
        temp.setText(this.getText().toString());
        temp.setTypeface(this.getTypeface());
        temp.measure(0, 0);
        canvas.rotate(-90);
        int max = -1 * ((getWidth() - temp.getMeasuredHeight())/2);
        canvas.translate(canvas.getClipBounds().left, canvas.getClipBounds().top - max);



        //}
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        //canvas.translate(getCompoundPaddingLeft()+25, getExtendedPaddingTop());
        //canvas.translate(getExtendedPaddingTop(),getCompoundPaddingRight()+25);
        getLayout().draw(canvas);
        canvas.restore();
    }
   /* @Override
    protected void onDraw(Canvas canvas){
        TextPaint textPaint = getPaint();
        textPaint.setColor(getCurrentTextColor());
        textPaint.drawableState = getDrawableState();
        canvas.save();
        if(topDown){
            canvas.translate(getWidth()/2, 0);
            canvas.rotate(90);
        }else{
            TextView temp = new TextView(getContext());
            temp.setText(this.getText().toString());
            temp.setTypeface(this.getTypeface());
            temp.measure(0, 0);
            canvas.rotate(-90);
            int max = -1 * ((getWidth() - temp.getMeasuredHeight())/2);
            canvas.translate(canvas.getClipBounds().left, canvas.getClipBounds().top - max);
        }
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        getLayout().draw(canvas);
        canvas.restore();
    }*/


    /*
    final boolean topDown;

   public VerticalTextView(Context context, AttributeSet attrs){
      super(context, attrs);
      final int gravity = getGravity();
      if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
         setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
         topDown = false;
      }else
         topDown = true;
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
      super.onMeasure(heightMeasureSpec, widthMeasureSpec);
      setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
   }

   @Override
   protected boolean setFrame(int l, int t, int r, int b){
      return super.setFrame(l, t, l+(b-t), t+(r-l));
   }

   @Override
   public void draw(Canvas canvas){
      if(topDown){
         canvas.translate(getHeight(), 0);
         canvas.rotate(90);
      }else {
         canvas.translate(0, getWidth());
         canvas.rotate(-90);
      }
      canvas.clipRect(0, 0, getWidth(), getHeight(), android.graphics.Region.Op.REPLACE);
      super.draw(canvas);
   }
}
     */




}
