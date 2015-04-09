/*
 * USED FOR SINGLETON PATTERN
 */

package edu.auburn.ppl.cyclecolumbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.view.View;

public class FadeSingleton {

    private static FadeSingleton instance = new FadeSingleton();

    private FadeSingleton() {}

    public static FadeSingleton getInstance() {
        return instance;
    }

    /**
     * Uses the View v to fade it into view.
     * @param v Holds the id to fade into view
     * @param duration This is how long it will take to fade it in.
     */
    @SuppressLint("NewApi")
    public void fadeIn(final View v, int duration) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        v.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * Uses the View v to fade out of view.
     * Sets an onAnimationEnd listener to set the view to GONE after completion.
     * @param v Holds the id to fade into view
     * @param duration This is how long it will take to fade it in.
     */
    @SuppressLint("NewApi")
    public void fadeOut(final View v, int duration) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        v.setAlpha(1f);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        v.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                    v.setVisibility(View.GONE);
	                }
	            });
    }

}
