package top.someones.cardmatch.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public class GameObserverAdaptor extends BaseGameObserver {

    private final Bitmap mFrontRes;
    private final Bitmap[] mBackRes;

    public GameObserverAdaptor(Context context, GameCallback callback, Bitmap mFrontRes, Bitmap[] mBackRes) {
        super(context, callback);
        this.mFrontRes = mFrontRes;
        this.mBackRes = mBackRes;
    }

    @Override
    protected int[] setData() {
        return super.getRandomResourcesIndex(mBackRes.length);
    }

    @Override
    protected View[][] makeGameView(int[] gameData) {
        View[][] views = new View[16][2];
        for (int i = 0; i < 16; i++) {
            views[i][0] = getImageView(mFrontRes);
            views[i][1] = getImageView(mBackRes[gameData[i]]);
        }
        return views;
    }

    private View getImageView(Bitmap res) {
        ImageView imageView = new ImageView(super.getContext());
        imageView.setImageBitmap(res);
        return imageView;
    }
}