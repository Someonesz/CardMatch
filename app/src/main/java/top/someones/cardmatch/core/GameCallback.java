package top.someones.cardmatch.core;

public interface GameCallback {

    void onSuccess(String token, int arg1, int arg2);

    void onFailure(String token, int arg1, int arg2);

    void onWin(String token);

}
