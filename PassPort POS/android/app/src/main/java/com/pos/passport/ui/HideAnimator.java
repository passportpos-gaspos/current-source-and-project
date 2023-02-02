package com.pos.passport.ui;

import android.view.View;
import android.view.animation.TranslateAnimation;

/**
 * Created by Kareem on 4/28/2016.
 */
public class HideAnimator {

    // To animate view slide out from left to right
    public static void slideToRight(View view){
        TranslateAnimation animate = new TranslateAnimation(0,view.getWidth(),0,0);
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
    // To animate view slide out from right to left
    public static void slideToLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(0,-view.getWidth(),0,0);
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    // To animate view slide out from top to bottom
    public static void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    // To animate view slide out from bottom to top
    public static void slideToTop(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.setVisibility(View.GONE);
    }

    public static void showView(View view){
        TranslateAnimation animate = new TranslateAnimation(view.getWidth(),0,0,0);
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }
}
