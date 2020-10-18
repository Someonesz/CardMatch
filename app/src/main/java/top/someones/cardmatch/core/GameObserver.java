package top.someones.cardmatch.core;

import android.content.Context;
import android.os.Handler;
import android.view.View;

public interface GameObserver {
    int MATCH_SUCCEED = 101;

    int MATCH_FAILED = 102;

    int GAME_WIN = 103;

    int MAX_VIEW = 16;

    GameObserver initGameObserver(Context context, Handler handler);

    View[][] newGame();

    int getGameSteps();

    void check(int a, int b);
}
