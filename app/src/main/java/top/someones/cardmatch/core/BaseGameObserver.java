package top.someones.cardmatch.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BaseGameObserver implements GameObserver {

    private final Handler mHandler;
    private final Context mContext;
    private final int[] mData = new int[16];
    private int mGameSteps = 0;
    private int mGameProgress = 0;

    public BaseGameObserver(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    private int getData(int index) {
        return mData[index];
    }

    @Override
    public View[][] newGame() {
        mGameSteps = 0;
        System.arraycopy(setData(), 0, mData, 0, mData.length);
        return makeGameView(mData);
    }

    protected abstract int[] setData();

    protected abstract View[][] makeGameView(int[] gameData);

    protected Context getContext() {
        return mContext;
    }

    @Override
    public int getGameSteps() {
        return mGameSteps;
    }

    @Override
    public void check(int a, int b) {
        mGameSteps++;
        if (getData(a) == getData(b)) {
            mHandler.sendEmptyMessage(GameObserver.MATCH_SUCCEED);
            mGameProgress++;
            if (mGameProgress == MAX_VIEW / 2) {
                Message msg = Message.obtain();
                msg.what = GAME_WIN;
                msg.arg1 = getGameSteps();
                msg.arg2 = b;
                mHandler.sendMessage(msg);
            }
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Message msg = Message.obtain();
                msg.what = MATCH_FAILED;
                msg.arg1 = a;
                msg.arg2 = b;
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected int[] getRandomResourcesIndex(int resLength) {
        List<Integer> one = new ArrayList<>(resLength);
        for (int i = 0; i < resLength; i++) {
            one.add(i);
        }
        List<Integer> two = new ArrayList<>(MAX_VIEW);
        Random random = new Random();
        int index;
        for (int i = 0; i < 8; i++) {
            index = random.nextInt(MAX_VIEW / 2 - i);
            two.add(one.get(index));
            one.remove(index);
        }
        two.addAll(two);
        int[] data = new int[MAX_VIEW];
        for (int i = 0; i < MAX_VIEW; i++) {
            index = random.nextInt(MAX_VIEW - i);
            data[i] = two.get(index);
            two.remove(index);
        }
        return data;
    }

}
