package top.someones.cardmatch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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
public class MyViewSwitcher extends FrameLayout {

    private int mWhichChild = 0;
    private final Context mContext;
    private boolean mHasNewView = true;

    public MyViewSwitcher(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public MyViewSwitcher(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MyViewSwitcher(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public MyViewSwitcher(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setView(View v1, View v2) {
        if (this.getChildCount() != 0) {
            mHasNewView = true;
            clearAnimation();
            this.removeAllViews();
            this.setEnabled(true);
            mWhichChild = 0;
        }
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        super.addView(v1, getLayoutParams(v1));
        super.addView(v2, getLayoutParams(v2));
        View bgView = new View(mContext);
        bgView.setBackgroundResource(R.drawable.bg);
        bgView.setAlpha(0.5f);
        bgView.setVisibility(GONE);
        super.addView(bgView, getLayoutParams(bgView));
    }

    private ViewGroup.LayoutParams getLayoutParams(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, -1);
        }
        return lp;
    }

    public void showNext() {
        Rotate3d out = new Rotate3d(0, 90, getWidth() / 2.0f, getHeight() / 2.0f, 310.0f, false);
        out.setDuration(500);
        out.setInterpolator(new DecelerateInterpolator());
        out.setAnimationListener(new OutAnimationListener());
        startAnimation(out);
    }

    public void showBorder() {
        this.getChildAt(2).setVisibility(VISIBLE);
    }

    public void hideBorder() {
        this.getChildAt(2).setVisibility(GONE);
    }

    private class OutAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            mHasNewView = false;
            MyViewSwitcher.this.setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mHasNewView) {
                return;
            }
            getChildAt(mWhichChild).setVisibility(View.GONE);
            mWhichChild = mWhichChild == 0 ? 1 : 0;
            getChildAt(mWhichChild).setVisibility(View.VISIBLE);
            if (mWhichChild == 0) {
                hideBorder();
            }
            Rotate3d in = new Rotate3d(270, 360, MyViewSwitcher.this.getWidth() / 2.0f, MyViewSwitcher.this.getHeight() / 2.0f, 310.0f, false);
            in.setDuration(animation.getDuration());
            in.setInterpolator(new DecelerateInterpolator());
            in.setAnimationListener(new InAnimationListener());
            MyViewSwitcher.this.startAnimation(in);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class InAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mWhichChild == 0)
                MyViewSwitcher.this.setEnabled(true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
