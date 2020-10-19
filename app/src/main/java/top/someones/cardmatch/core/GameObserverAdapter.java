package top.someones.cardmatch.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class GameObserverAdapter {

    public static GameObserver makeGameObserver(Context context, Handler handler, GameResources<?> res) {
        if (res.getFrontResource() instanceof Bitmap) {
            new ExternalPicture(context, handler, (GameResources<Bitmap>) res);
        } else if (res.getFrontResource() instanceof Integer)
            return new InternalPicture(context, handler, (GameResources<Integer>) res);
        return null;
    }

    private static class InternalPicture extends BaseGameObserver {
        private GameResources<Integer> res;

        public InternalPicture(Context context, Handler handler, GameResources<Integer> res) {
            super(context, handler);
            this.res = res;
        }

        @Override
        protected int[] setData() {
            return super.getRandomResourcesIndex(res.size());
        }

        @Override
        protected View[][] makeGameView(int[] gameData) {
            View[][] views = new View[16][2];
            for (int i = 0; i < 16; i++) {
                views[i][0] = getImageView(res.getFrontResource());
                views[i][1] = getImageView(res.getBackResources(gameData[i]));
            }
            return views;
        }

        private View getImageView(int res) {
            ImageView imageView = new ImageView(super.getContext());
            imageView.setImageResource(res);
            return imageView;
        }
    }


    private static class ExternalPicture extends BaseGameObserver {
        private GameResources<Bitmap> res;

        public ExternalPicture(Context context, Handler handler, GameResources<Bitmap> res) {
            super(context, handler);
            this.res = res;
        }

        @Override
        protected int[] setData() {
            return super.getRandomResourcesIndex(res.size());
        }

        @Override
        protected View[][] makeGameView(int[] gameData) {
            View[][] views = new View[16][2];
            for (int i = 0; i < 16; i++) {
                views[i][0] = getImageView(res.getFrontResource());
                views[i][1] = getImageView(res.getBackResources(gameData[i]));
            }
            return views;
        }

        private View getImageView(Bitmap res) {
            ImageView imageView = new ImageView(super.getContext());
            imageView.setImageBitmap(res);
            return imageView;
        }
    }

}
