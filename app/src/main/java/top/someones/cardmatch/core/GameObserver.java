package top.someones.cardmatch.core;

import android.view.View;

public interface GameObserver {

    int MAX_VIEW = 16;

    View[][] newGame();

    void check(int a, int b);
}
