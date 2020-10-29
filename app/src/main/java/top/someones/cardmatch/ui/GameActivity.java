package top.someones.cardmatch.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.GameObserver;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private final MyViewSwitcher[] switchers = new MyViewSwitcher[16];
    private GameObserver gameObserver;
    private int mSelect1 = -1, mSelect2 = -1;
    private boolean isNewGame = true;
    private int mGameSteps = 0;
    private long mStartTime;
    private TextView mGameTime_tv, mGameSteps_tv;
    private Thread mGameTimeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameObserver = GameManagement.getGameObserver(getIntent().getStringExtra("uuid"), this, new GameHandler());
        if (gameObserver == null) {
            Toast.makeText(this, "游戏初始化失败", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }
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

        mGameTime_tv = findViewById(R.id.gameTime);
        mGameSteps_tv = findViewById(R.id.gameSteps);

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

        newGame();
    }

    private void newGame() {
        isNewGame = true;
        mSelect1 = -1;
        mSelect2 = -1;
        mGameSteps = 0;
        View[][] views = gameObserver.newGame();
        for (int i = 0; i < switchers.length; i++) {
            switchers[i].setView(views[i][0], views[i][1]);
        }
        mGameSteps_tv.setText(String.valueOf(0));
        if (mGameTimeThread != null) {
            mGameTimeThread.interrupt();
        }
        mGameTimeThread = new UpdateGameTime();
        mGameTimeThread.start();
        mStartTime = System.currentTimeMillis();
    }

    private void tmp() {
        isNewGame = false;
        switchers[mSelect1].showNext();
        switchers[mSelect2].showNext();
        gameObserver.check(mSelect1, mSelect2);
        mSelect1 = -1;
        mSelect2 = -1;
        mGameSteps_tv.setText(String.valueOf(++mGameSteps));
    }

    @Override
    protected void onDestroy() {
        if (mGameTimeThread != null) {
            mGameTimeThread.interrupt();
        }
        super.onDestroy();
    }

    class UpdateGameTime extends Thread {

        private int second = 0;

        @Override
        public void run() {
            try {
                while (true) {
                    runOnUiThread(() -> {
                        mGameTime_tv.setText(String.valueOf(second++));
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {

            }
        }
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
                        int gameTime = (int) ((System.currentTimeMillis() - mStartTime) / 1000);
                        mGameTimeThread.interrupt();
                        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("恭喜过关！");
                        builder.setMessage("步数:" + msg.arg1 +
                                "\n耗时:" + gameTime +
                                "\n分数:" + getScore(msg.arg1, gameTime) +
                                "\n开始新游戏吗？");
                        builder.setPositiveButton("确定", (dialog, which) -> newGame());
                        builder.setNegativeButton("取消", null);
                        builder.create().show();
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

}