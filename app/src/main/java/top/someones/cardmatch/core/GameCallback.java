package top.someones.cardmatch.core;

public interface GameCallback {

    void onSuccess(int arg1, int arg2);

    void onFailure(int arg1, int arg2);

    void onWin();

}
