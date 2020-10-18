package top.someones.cardmatch.game;

import android.view.View;
import android.widget.ImageView;

import top.someones.cardmatch.R;
import top.someones.cardmatch.core.BaseGameObserver;

public class Go extends BaseGameObserver {

    public static final int FrontImage = R.drawable.b;
    public static final int[] BackImage = {R.drawable.poker1, R.drawable.poker2, R.drawable.poker3, R.drawable.poker4, R.drawable.poker5, R.drawable.poker6, R.drawable.poker7, R.drawable.poker8, R.drawable.poker9, R.drawable.poker10, R.drawable.poker11, R.drawable.poker12};

    @Override
    protected int[] setData() {
        return super.getRandomImageRes(BackImage.length);
    }

    @Override
    protected View[][] makeGameView() {
        View[][] views = new View[16][2];
        for (int i = 0; i < 16; i++) {
            views[i][0] = getImageView(FrontImage);
            views[i][1] = getImageView(BackImage[getData(i)]);
        }
        return views;

    }

    private View getImageView(int res) {
        ImageView imageView = new ImageView(super.getContext());
        imageView.setImageResource(res);

        return imageView;
    }

    public static String getGameName() {
        return "扑克";
    }

}
