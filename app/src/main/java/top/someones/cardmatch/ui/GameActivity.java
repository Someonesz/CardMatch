package top.someones.cardmatch.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.GameObserver;

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

public class GameActivity extends AppCompatActivity {

    private GameObserver gameObserver;
    private Handler mGameHandler;
    private int mSelect1 = -1, mSelect2 = -1;
    private boolean isNewGame = true;

    private int mGameStep = 0;
    private int mGameTime;
    private Runnable mTimer;

    private final MyViewSwitcher[] switchers = new MyViewSwitcher[16];
    private TextView mGameRealTime, mGameRealTimeSteps;
    private ImageView mActionRestart, mActionPause, mActionRank, mActionExit;
    private LinearLayout mGamePauseInfo, mGameWinInfo;
    private TextView mGameFinalTime, mGameFinalSteps, mGameFinalScore;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //初始化游戏观察者
        mGameHandler = new GameHandler();
        gameObserver = GameManagement.getGameObserver(getIntent().getStringExtra("uuid"), this, mGameHandler);
        if (gameObserver == null) {
            Toast.makeText(this, "游戏初始化失败", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        //绑定按钮
        mGameRealTime = findViewById(R.id.game_real_time);
        mGameRealTimeSteps = findViewById(R.id.game_real_time_steps);
        mGamePauseInfo = findViewById(R.id.game_pause_info);
        mGameWinInfo = findViewById(R.id.game_win_info);
        mGameFinalTime = findViewById(R.id.game_final_time);
        mGameFinalSteps = findViewById(R.id.game_final_steps);
        mGameFinalScore = findViewById(R.id.game_final_score);
        switchers[0] = findViewById(R.id.c1);
        switchers[1] = findViewById(R.id.c2);
        switchers[2] = findViewById(R.id.c3);
        switchers[3] = findViewById(R.id.c4);
        switchers[4] = findViewById(R.id.c5);
        switchers[5] = findViewById(R.id.c6);
        switchers[6] = findViewById(R.id.c7);
        switchers[7] = findViewById(R.id.c8);
        switchers[8] = findViewById(R.id.c9);
        switchers[9] = findViewById(R.id.c10);
        switchers[10] = findViewById(R.id.c11);
        switchers[11] = findViewById(R.id.c12);
        switchers[12] = findViewById(R.id.c13);
        switchers[13] = findViewById(R.id.c14);
        switchers[14] = findViewById(R.id.c15);
        switchers[15] = findViewById(R.id.c16);

        //绑定事件
        for (int i = 0; i < switchers.length; i++) {
            final int index = i;
            switchers[i].setOnClickListener(l -> {
                if (mSelect1 == -1) {
                    mSelect1 = index;
                    switchers[index].showBorder();
                } else {
                    if (mSelect1 == index) {
                        mSelect1 = -1;
                        switchers[index].hideBorder();
                    } else {
                        mSelect2 = index;
                        switchers[index].showBorder();
                        tmp();
                    }
                }
            });
        }

        //新游戏
        mActionRestart = findViewById(R.id.action_restart);
        mActionRestart.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mActionRestart.setImageResource(R.drawable.action_restart_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mActionRestart.setImageResource(R.drawable.action_restart);
                    break;
            }
            return false;
        });
        mActionRestart.setOnClickListener(v -> newGame());

        //暂停计时
        mActionPause = findViewById(R.id.action_pause);
        mActionPause.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mActionPause.setImageResource(R.drawable.action_pause_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mActionPause.setImageResource(R.drawable.action_pause);
                    break;
            }
            return false;
        });
        mActionPause.setOnClickListener(v -> {
            if (mGamePauseInfo.getVisibility() == View.VISIBLE) {
                mGamePauseInfo.setVisibility(View.INVISIBLE);
                mGameHandler.postDelayed(mTimer, 0);
            } else {
                mGamePauseInfo.setVisibility(View.VISIBLE);
                mGameHandler.removeCallbacks(mTimer);
            }
        });

        //展示排名
        mActionRank = findViewById(R.id.action_rank);
        mActionRank.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mActionRank.setImageResource(R.drawable.action_rank_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mActionRank.setImageResource(R.drawable.action_rank);
                    break;
            }
            return false;
        });
        mActionRank.setOnClickListener(v -> {
            // TODO:展示排行榜
            Toast.makeText(GameActivity.this, "即将上线", Toast.LENGTH_SHORT).show();
        });

        //放弃并返回主页
        mActionExit = findViewById(R.id.action_exit);
        mActionExit.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mActionExit.setImageResource(R.drawable.action_exit_touch);
                    break;
                case MotionEvent.ACTION_UP:
                    mActionExit.setImageResource(R.drawable.action_exit);
                    break;
            }
            return false;
        });
        mActionExit.setOnClickListener(v -> GameActivity.this.finish());

        //消耗点击事件，阻止事件向下传递
        mGamePauseInfo.setOnClickListener(null);
        mGameWinInfo.setOnClickListener(null);

        mTimer = new Timer();
        newGame();
    }

    private void newGame() {
        isNewGame = true;
        mSelect1 = -1;
        mSelect2 = -1;
        mGameStep = 0;
        View[][] views = gameObserver.newGame();
        for (int i = 0; i < switchers.length; i++) {
            switchers[i].setView(views[i][0], views[i][1]);
        }
        mGameRealTimeSteps.setText(String.valueOf(0));
        mGameTime = -1;
        mActionPause.setEnabled(true);
        mActionRank.setEnabled(true);
        mGamePauseInfo.setVisibility(View.INVISIBLE);
        mGameWinInfo.setVisibility(View.INVISIBLE);
        mGameHandler.removeCallbacks(mTimer);
        mGameHandler.postDelayed(mTimer, 0);
    }

    private void tmp() {
        isNewGame = false;
        switchers[mSelect1].showNext();
        switchers[mSelect2].showNext();
        gameObserver.check(mSelect1, mSelect2);
        mSelect1 = -1;
        mSelect2 = -1;
        mGameRealTimeSteps.setText(String.valueOf(++mGameStep));
    }

    @Override
    protected void onDestroy() {
        mGameHandler.removeCallbacks(mTimer);
        super.onDestroy();
    }

    class GameHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!isNewGame) {
                switch (msg.what) {
                    case GameObserver.MATCH_FAILED:
                        switchers[msg.arg1].showNext();
                        switchers[msg.arg2].showNext();
                        switchers[msg.arg1].hideBorder();
                        switchers[msg.arg2].hideBorder();
                        break;
                    case GameObserver.MATCH_SUCCEED:
                        break;
                    case GameObserver.GAME_WIN:
                        this.removeCallbacks(mTimer);
                        mActionPause.setEnabled(false);
                        mActionRank.setEnabled(false);
                        mGameFinalTime.setText("用时:".concat(String.valueOf(mGameTime)));
                        mGameFinalSteps.setText("步数:".concat(String.valueOf(mGameStep)));
                        mGameFinalScore.setText("分数:".concat(String.valueOf(getScore(msg.arg1, mGameTime))));
                        mGameWinInfo.setVisibility(View.VISIBLE);
                        break;
                }
                super.handleMessage(msg);
            }
        }

        private int getScore(int step, int time) {
            double last = 9d / ((step - 3) * 5 + time);
            return (int) (last * 100000);
        }
    }

    private class Timer implements Runnable {
        @Override
        public void run() {
            mGameRealTime.setText(String.valueOf(++mGameTime));
            mGameHandler.postDelayed(this, 1000);
        }
    }

}