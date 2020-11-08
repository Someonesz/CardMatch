package top.someones.cardmatch.ui;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import top.someones.cardmatch.BaseActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.GameObserver;

public class GameActivity extends BaseActivity {

    private GameObserver mGameObserver;
    private Handler mGameHandler;
    private int mSelect1 = -1, mSelect2 = -1;
    private boolean isNewGame = true;

    private int mGameSteps = 0;
    private int mGameTime;
    private Runnable mGameTimer;

    private final MyViewSwitcher[] mGameElements = new MyViewSwitcher[16];
    private TextView mGameTimeView, mGameStepsView;
    private ImageView mRestartView, mPauseView, mRankView, mExitView;
    private LinearLayout mGamePauseInfoLayout, mGameWinInfoLayout;
    private TextView mGameFinalTimeView, mGameFinalStepsView, mGameFinalScoreView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //初始化游戏观察者
        mGameHandler = new GameHandler();
        mGameObserver = GameManagement.getGameObserver(getIntent().getStringExtra("uuid"), this, mGameHandler);
        if (mGameObserver == null) {
            Toast.makeText(this, "游戏初始化失败", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        super.immersionStatusBar(true);

        //绑定按钮
        mGameTimeView = findViewById(R.id.gameTime);
        mGameStepsView = findViewById(R.id.gameSteps);
        mGamePauseInfoLayout = findViewById(R.id.gamePauseInfo);
        mGameWinInfoLayout = findViewById(R.id.gameWinInfo);
        mGameFinalTimeView = findViewById(R.id.gameFinalTime);
        mGameFinalStepsView = findViewById(R.id.gameFinalSteps);
        mGameFinalScoreView = findViewById(R.id.gameFinalScore);
        mGameElements[0] = findViewById(R.id.c1);
        mGameElements[1] = findViewById(R.id.c2);
        mGameElements[2] = findViewById(R.id.c3);
        mGameElements[3] = findViewById(R.id.c4);
        mGameElements[4] = findViewById(R.id.c5);
        mGameElements[5] = findViewById(R.id.c6);
        mGameElements[6] = findViewById(R.id.c7);
        mGameElements[7] = findViewById(R.id.c8);
        mGameElements[8] = findViewById(R.id.c9);
        mGameElements[9] = findViewById(R.id.c10);
        mGameElements[10] = findViewById(R.id.c11);
        mGameElements[11] = findViewById(R.id.c12);
        mGameElements[12] = findViewById(R.id.c13);
        mGameElements[13] = findViewById(R.id.c14);
        mGameElements[14] = findViewById(R.id.c15);
        mGameElements[15] = findViewById(R.id.c16);

        //绑定事件
        for (int i = 0; i < mGameElements.length; i++) {
            final int index = i;
            mGameElements[i].setOnClickListener(l -> {
                if (mSelect1 == -1) {
                    mSelect1 = index;
                    mGameElements[index].showBorder();
                } else {
                    if (mSelect1 == index) {
                        mSelect1 = -1;
                        mGameElements[index].hideBorder();
                    } else {
                        mSelect2 = index;
                        mGameElements[index].showBorder();
                        tmp();
                    }
                }
            });
        }

        //新游戏
        mRestartView = findViewById(R.id.actionRestart);
        mRestartView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRestartView.setImageResource(R.drawable.action_restart_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mRestartView.setImageResource(R.drawable.action_restart);
                    break;
            }
            return false;
        });
        mRestartView.setOnClickListener(v -> newGame());

        //暂停计时
        mPauseView = findViewById(R.id.actionPause);
        mPauseView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPauseView.setImageResource(R.drawable.action_pause_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mPauseView.setImageResource(R.drawable.action_pause);
                    break;
            }
            return false;
        });
        mPauseView.setOnClickListener(v -> {
            if (mGamePauseInfoLayout.getVisibility() == View.VISIBLE) {
                mGamePauseInfoLayout.setVisibility(View.INVISIBLE);
                mGameHandler.postDelayed(mGameTimer, 0);
            } else {
                mGamePauseInfoLayout.setVisibility(View.VISIBLE);
                mGameHandler.removeCallbacks(mGameTimer);
            }
        });

        //展示排名
        mRankView = findViewById(R.id.actionRank);
        mRankView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mRankView.setImageResource(R.drawable.action_rank_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mRankView.setImageResource(R.drawable.action_rank);
                    break;
            }
            return false;
        });
        mRankView.setOnClickListener(v -> {
            // TODO:展示排行榜
            Toast.makeText(GameActivity.this, "即将上线", Toast.LENGTH_SHORT).show();
        });

        //放弃并返回主页
        mExitView = findViewById(R.id.actionExit);
        mExitView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mExitView.setImageResource(R.drawable.action_exit_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mExitView.setImageResource(R.drawable.action_exit);
                    break;
            }
            return false;
        });
        mExitView.setOnClickListener(v -> GameActivity.this.finish());

        //消耗点击事件，阻止事件向下传递
        mGamePauseInfoLayout.setOnClickListener(null);
        mGameWinInfoLayout.setOnClickListener(null);

        mGameTimer = new Timer();
        newGame();
    }

    private void newGame() {
        isNewGame = true;
        mSelect1 = -1;
        mSelect2 = -1;
        mGameSteps = 0;
        View[][] views = mGameObserver.newGame();
        for (int i = 0; i < mGameElements.length; i++) {
            mGameElements[i].setView(views[i][0], views[i][1]);
        }
        mGameStepsView.setText(String.valueOf(0));
        mGameTime = -1;
        mPauseView.setEnabled(true);
        mRankView.setEnabled(true);
        mGamePauseInfoLayout.setVisibility(View.INVISIBLE);
        mGameWinInfoLayout.setVisibility(View.INVISIBLE);
        mGameHandler.removeCallbacks(mGameTimer);
        mGameHandler.postDelayed(mGameTimer, 0);
    }

    private void tmp() {
        isNewGame = false;
        mGameElements[mSelect1].showNext();
        mGameElements[mSelect2].showNext();
        mGameObserver.check(mSelect1, mSelect2);
        mSelect1 = -1;
        mSelect2 = -1;
        mGameStepsView.setText(String.valueOf(++mGameSteps));
    }

    @Override
    protected void onDestroy() {
        mGameHandler.removeCallbacks(mGameTimer);
        super.onDestroy();
    }

    class GameHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!isNewGame) {
                switch (msg.what) {
                    case GameObserver.MATCH_FAILED:
                        mGameElements[msg.arg1].showNext();
                        mGameElements[msg.arg2].showNext();
                        mGameElements[msg.arg1].hideBorder();
                        mGameElements[msg.arg2].hideBorder();
                        break;
                    case GameObserver.MATCH_SUCCEED:
                        break;
                    case GameObserver.GAME_WIN:
                        this.removeCallbacks(mGameTimer);
                        mPauseView.setEnabled(false);
                        mRankView.setEnabled(false);
                        mGameFinalTimeView.setText("用时:".concat(String.valueOf(mGameTime)));
                        mGameFinalStepsView.setText("步数:".concat(String.valueOf(mGameSteps)));
                        mGameFinalScoreView.setText("分数:".concat(String.valueOf(getScore(msg.arg1, mGameTime))));
                        mGameWinInfoLayout.setVisibility(View.VISIBLE);
                        break;
                }
                super.handleMessage(msg);
            }
        }

        private int getScore(int steps, int time) {
            double last = 9d / ((steps - 3) * 5 + time);
            return (int) (last * 100000);
        }
    }

    private class Timer implements Runnable {
        @Override
        public void run() {
            mGameTimeView.setText(String.valueOf(++mGameTime));
            mGameHandler.postDelayed(this, 1000);
        }
    }

}