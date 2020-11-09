package top.someones.cardmatch.ui;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import top.someones.cardmatch.BaseActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.GameObserver;
import top.someones.cardmatch.databinding.ActivityGameBinding;

public class GameActivity extends BaseActivity {

    private GameObserver mGameObserver;
    private Handler mGameHandler;
    private int mSelect1 = -1, mSelect2 = -1;
    private boolean isNewGame = true;

    private int mGameSteps = 0;
    private int mGameTime;
    private Runnable mGameTimer;

    private final MyViewSwitcher[] mGameElements = new MyViewSwitcher[16];

    private ActivityGameBinding mViewBinding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

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
        mGameElements[0] = mViewBinding.c1;
        mGameElements[1] = mViewBinding.c2;
        mGameElements[2] = mViewBinding.c3;
        mGameElements[3] = mViewBinding.c4;
        mGameElements[4] = mViewBinding.c5;
        mGameElements[5] = mViewBinding.c6;
        mGameElements[6] = mViewBinding.c7;
        mGameElements[7] = mViewBinding.c8;
        mGameElements[8] = mViewBinding.c9;
        mGameElements[9] = mViewBinding.c10;
        mGameElements[10] = mViewBinding.c11;
        mGameElements[11] = mViewBinding.c12;
        mGameElements[12] = mViewBinding.c13;
        mGameElements[13] = mViewBinding.c14;
        mGameElements[14] = mViewBinding.c15;
        mGameElements[15] = mViewBinding.c16;

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
                        submit();
                    }
                }
            });
        }

        //新游戏
        mViewBinding.actionRestart.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mViewBinding.actionRestart.setImageResource(R.drawable.action_restart_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mViewBinding.actionRestart.setImageResource(R.drawable.action_restart);
                    break;
            }
            return false;
        });
        mViewBinding.actionRestart.setOnClickListener(v -> newGame());

        //暂停计时
        mViewBinding.actionPause.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mViewBinding.actionPause.setImageResource(R.drawable.action_pause_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mViewBinding.actionPause.setImageResource(R.drawable.action_pause);
                    break;
            }
            return false;
        });
        mViewBinding.actionPause.setOnClickListener(v -> {
            if (mViewBinding.gamePauseInfo.getVisibility() == View.VISIBLE) {
                mViewBinding.gamePauseInfo.setVisibility(View.INVISIBLE);
                mGameHandler.postDelayed(mGameTimer, 0);
            } else {
                mViewBinding.gamePauseInfo.setVisibility(View.VISIBLE);
                mGameHandler.removeCallbacks(mGameTimer);
            }
        });

        //展示排名
        mViewBinding.actionRank.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mViewBinding.actionRank.setImageResource(R.drawable.action_rank_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mViewBinding.actionRank.setImageResource(R.drawable.action_rank);
                    break;
            }
            return false;
        });
        mViewBinding.actionRank.setOnClickListener(v -> {
            // TODO:展示排行榜
            Toast.makeText(GameActivity.this, "即将上线", Toast.LENGTH_SHORT).show();
        });

        //放弃并返回主页
        mViewBinding.actionExit.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mViewBinding.actionExit.setImageResource(R.drawable.action_exit_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mViewBinding.actionExit.setImageResource(R.drawable.action_exit);
                    break;
            }
            return false;
        });
        mViewBinding.actionExit.setOnClickListener(v -> GameActivity.this.finish());

        //消耗点击事件，阻止事件向下传递
        mViewBinding.gamePauseInfo.setOnClickListener(null);
        mViewBinding.gameWinInfo.setOnClickListener(null);

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
        mViewBinding.gameSteps.setText(String.valueOf(0));
        mGameTime = -1;
        mViewBinding.actionPause.setEnabled(true);
        mViewBinding.actionRank.setEnabled(true);
        mViewBinding.gamePauseInfo.setVisibility(View.INVISIBLE);
        mViewBinding.gameWinInfo.setVisibility(View.INVISIBLE);
        mGameHandler.removeCallbacks(mGameTimer);
        mGameHandler.postDelayed(mGameTimer, 0);
    }

    private void submit() {
        isNewGame = false;
        mGameElements[mSelect1].showNext();
        mGameElements[mSelect2].showNext();
        mGameObserver.check(mSelect1, mSelect2);
        mSelect1 = -1;
        mSelect2 = -1;
        mViewBinding.gameSteps.setText(String.valueOf(++mGameSteps));
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
                        mViewBinding.actionPause.setEnabled(false);
                        mViewBinding.actionRank.setEnabled(false);
                        mViewBinding.gameFinalTime.setText("用时:".concat(String.valueOf(mGameTime)));
                        mViewBinding.gameFinalSteps.setText("步数:".concat(String.valueOf(mGameSteps)));
                        mViewBinding.gameFinalScore.setText("分数:".concat(String.valueOf(getScore(msg.arg1, mGameTime))));
                        mViewBinding.gameWinInfo.setVisibility(View.VISIBLE);
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
            mViewBinding.gameTime.setText(String.valueOf(++mGameTime));
            mGameHandler.postDelayed(this, 1000);
        }
    }

}