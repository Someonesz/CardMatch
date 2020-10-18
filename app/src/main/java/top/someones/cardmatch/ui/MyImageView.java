package top.someones.cardmatch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Deprecated
public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void switchImage(int nextImage) {
        Rotate3d out = new Rotate3d(0, 90, getWidth() / 2.0f, getHeight() / 2.0f, 310.0f, false);
        out.setDuration(500);
        out.setInterpolator(new DecelerateInterpolator());
        out.setAnimationListener(new MyAnimationListener(nextImage));
        startAnimation(out);
    }

    private final class MyAnimationListener implements Animation.AnimationListener {

        private final int nextPic;

        public MyAnimationListener(int nextPic) {
            this.nextPic = nextPic;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            MyImageView.this.setImageResource(nextPic);
            Rotate3d in = new Rotate3d(270, 360, MyImageView.this.getWidth() / 2.0f, MyImageView.this.getHeight() / 2.0f, 310.0f, false);
            in.setDuration(animation.getDuration());
            in.setInterpolator(new DecelerateInterpolator());
            MyImageView.this.startAnimation(in);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


}
