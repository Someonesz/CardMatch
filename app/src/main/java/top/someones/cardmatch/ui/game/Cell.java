package top.someones.cardmatch.ui.game;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.someones.cardmatch.R;

/**
 * 翻牌子
 * 两个VIEW切换
 *
 * @author Someones
 */
public class Cell extends FrameLayout {

    private int mWhichChild = 0;
    private View mBorder;
    private AnimatorSet mCardOutAnimator;
    private AnimatorSet mCardInAnimator;
    private ObjectAnimator mCardFadeOutAnimator;

    public Cell(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Cell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Cell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public Cell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mBorder = new View(context);
        mBorder.setBackgroundResource(R.drawable.rounded_red_border);
        mBorder.setAlpha(0.5f);
        addView(mBorder, getLayoutParams(mBorder));
        mCardOutAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
        mCardInAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);
        mCardFadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
        mCardFadeOutAnimator.setDuration(500);
        mCardOutAnimator.setTarget(this);
        mCardInAnimator.setTarget(this);
        mCardOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                Cell.this.setEnabled(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {// 翻转90度之后，换图
                getChildAt(mWhichChild).setVisibility(View.GONE);
                mWhichChild = mWhichChild == 1 ? 2 : 1;
                getChildAt(mWhichChild).setVisibility(View.VISIBLE);
                if (mWhichChild == 1) {
                    hideBorder();
                }
                mCardInAnimator.start();
            }
        });
        mCardInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mWhichChild == 1)
                    Cell.this.setEnabled(true);
            }
        });
    }

    public void setView(View v1, View v2) {
        if (mCardOutAnimator.isRunning())
            mCardOutAnimator.end();
        if (getChildCount() > 1) {
            super.removeViewAt(2);
            super.removeViewAt(1);
        }
        mWhichChild = 1;
        mCardFadeOutAnimator.end();
        this.setEnabled(true);
        this.setAlpha(1.0f);
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        super.addView(v1, getLayoutParams(v1));
        super.addView(v2, getLayoutParams(v2));
        mBorder.setVisibility(GONE);
    }

    private ViewGroup.LayoutParams getLayoutParams(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, -1);
        }
        return lp;
    }

    public void showNext() {
        mCardOutAnimator.start();
    }

    public void fadeOut() {
        this.setEnabled(false);
        mCardFadeOutAnimator.start();
    }

    public void showBorder() {
        mBorder.setVisibility(VISIBLE);
    }

    public void hideBorder() {
        mBorder.setVisibility(GONE);
    }

}
