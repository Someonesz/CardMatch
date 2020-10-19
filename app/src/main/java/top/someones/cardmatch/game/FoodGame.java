//package top.someones.cardmatch.game;
//
//import android.view.View;
//import android.widget.ImageView;
//
//import top.someones.cardmatch.R;
//import top.someones.cardmatch.core.BaseGameObserver;
//
//public class FoodGame extends BaseGameObserver {
//
//    public static final int mFrontImage = R.drawable.b;
//    public static final int[] mBackImage = {R.drawable.food1, R.drawable.food2, R.drawable.food3, R.drawable.food4, R.drawable.food5, R.drawable.food6, R.drawable.food7, R.drawable.food8, R.drawable.food9, R.drawable.food10, R.drawable.food11, R.drawable.food12};
//
//    @Override
//    protected int[] setData() {
//        return super.getRandomImageRes(mBackImage.length);
//    }
//
//    @Override
//    protected View[][] makeGameView() {
//        View[][] views = new View[16][2];
//        for (int i = 0; i < 16; i++) {
//            views[i][0] = getImageView(mFrontImage);
//            views[i][1] = getImageView(mBackImage[getData(i)]);
//        }
//        return views;
//    }
//
//    private View getImageView(int res) {
//        ImageView imageView = new ImageView(super.getContext());
//        imageView.setImageResource(res);
//        return imageView;
//    }
//
//    public static String getGameName() {
//        return "食物";
//    }
//
//}
