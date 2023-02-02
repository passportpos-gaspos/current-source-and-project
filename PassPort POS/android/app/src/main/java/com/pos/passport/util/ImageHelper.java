package com.pos.passport.util;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageHelper {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(90, 90, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int smallside = 0;
        if(bitmap.getWidth() >= bitmap.getHeight()) {
        	smallside = bitmap.getHeight();
        } else {
        	smallside = bitmap.getWidth();
        }

        final int color = 0xffffff00;
        final Paint paint = new Paint();
        final Rect src = new Rect(bitmap.getWidth()/2-smallside/2, bitmap.getHeight()/2-smallside/2, bitmap.getWidth()/2+smallside/2, bitmap.getHeight()/2+smallside/2);
        final Rect dest = new Rect(0, 0, 90, 90);

        final RectF rectF = new RectF(dest);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dest, paint);

        return output;
    }
}