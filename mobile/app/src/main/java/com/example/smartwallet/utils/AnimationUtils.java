package com.example.smartwallet.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
    
    public static void fadeIn(View view, int duration) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(duration);
        fadeIn.setFillAfter(true);
        view.startAnimation(fadeIn);
    }
    
    public static void fadeOut(View view, int duration) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(duration);
        fadeOut.setFillAfter(true);
        view.startAnimation(fadeOut);
    }
    
    public static void slideInFromRight(View view, int duration) {
        TranslateAnimation slideIn = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        );
        slideIn.setDuration(duration);
        slideIn.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(slideIn);
    }
    
    public static void slideOutToLeft(View view, int duration) {
        TranslateAnimation slideOut = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        );
        slideOut.setDuration(duration);
        slideOut.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(slideOut);
    }
    
    public static void scaleIn(View view, int duration) {
        ScaleAnimation scaleIn = new ScaleAnimation(
            0.0f, 1.0f, 0.0f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleIn.setDuration(duration);
        scaleIn.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(scaleIn);
    }
    
    public static void scaleOut(View view, int duration) {
        ScaleAnimation scaleOut = new ScaleAnimation(
            1.0f, 0.0f, 1.0f, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleOut.setDuration(duration);
        scaleOut.setInterpolator(new AccelerateDecelerateInterpolator());
        view.startAnimation(scaleOut);
    }
    
    public static void pulse(View view, int duration) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f, 1.0f);
        
        scaleX.setDuration(duration);
        scaleY.setDuration(duration);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        
        scaleX.start();
        scaleY.start();
    }
    
    public static void shake(View view, int duration) {
        TranslateAnimation shake = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.1f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        shake.setDuration(duration);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        view.startAnimation(shake);
    }
    
    public static void crossFade(View fromView, View toView, int duration) {
        fadeOut(fromView, duration);
        fadeIn(toView, duration);
    }
    
    public static void animateVisibility(View view, boolean visible, int duration) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
            fadeIn(view, duration);
        } else {
            fadeOut(view, duration);
            view.setVisibility(View.GONE);
        }
    }
}