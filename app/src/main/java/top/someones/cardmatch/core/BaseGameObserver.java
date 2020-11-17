package top.someones.cardmatch.core;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BaseGameObserver implements GameObserver {

    private final GameCallback callback;
    private final Context mContext;
    private final int[] mData = new int[16];
    private int mGameProgress = 0;

    public BaseGameObserver(Context context, GameCallback callback) {
        this.mContext = context;
        this.callback = callback;
    }

    private int getData(int index) {
        return mData[index];
    }

    @Override
    public View[][] newGame() {
        System.arraycopy(setData(), 0, mData, 0, mData.length);
        return makeGameView(mData);
    }

    protected abstract int[] setData();

    protected abstract View[][] makeGameView(int[] gameData);

    protected Context getContext() {
        return mContext;
    }

    @Override
    public void check(int a, int b) {
        if (getData(a) == getData(b)) {
            mGameProgress++;
            callback.onSuccess(a, b);
            if (mGameProgress == GameObserver.MAX_VIEW / 2)
                callback.onWin();
        } else
            callback.onFailure(a, b);
    }

    protected int[] getRandomResourcesIndex(int resLength) {
        List<Integer> one = new ArrayList<>(resLength);
        for (int i = 0; i < resLength; i++) {
            one.add(i);
        }
        List<Integer> two = new ArrayList<>(GameObserver.MAX_VIEW);
        Random random = new Random();
        int index;
        for (int i = 0; i < GameObserver.MAX_VIEW / 2; i++) {
            index = random.nextInt(resLength - i);
            two.add(one.get(index));
            one.remove(index);
        }
        two.addAll(two);
        int[] data = new int[GameObserver.MAX_VIEW];
        for (int i = 0; i < GameObserver.MAX_VIEW; i++) {
            index = random.nextInt(GameObserver.MAX_VIEW - i);
            data[i] = two.remove(index);
        }
        return data;
    }

}
