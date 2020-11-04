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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

@Deprecated
public class TwoActivity extends AppCompatActivity {

    private final MyViewSwitcher[] switchers = new MyViewSwitcher[16];
    private int mSelect1 = -1, mSelect2 = -1;
    private GameObserver gameObserver;
    private boolean isNewGame = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);

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

        gameObserver = GameManagement.getGameObserver(getIntent().getStringExtra("uuid"), this, new GameHandler());
        if (gameObserver == null) {
            Toast.makeText(this, "游戏初始化失败", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        for (int i = 0; i < switchers.length; i++) {
            int finalI = i;
            switchers[i].setOnClickListener(l -> {
                if (mSelect1 == -1) {
                    mSelect1 = finalI;
                    switchers[finalI].showBorder();
                } else {
                    if (mSelect1 == finalI) {
                        mSelect1 = -1;
                        switchers[finalI].hideBorder();
                    } else {
                        mSelect2 = finalI;
                        switchers[finalI].showBorder();
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
        View[][] views = gameObserver.newGame();
        for (int i = 0; i < switchers.length; i++) {
            switchers[i].setView(views[i][0], views[i][1]);
        }
    }

    private void tmp() {
        isNewGame = false;
        switchers[mSelect1].showNext();
        switchers[mSelect2].showNext();
        gameObserver.check(mSelect1, mSelect2);
        mSelect1 = -1;
        mSelect2 = -1;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage("返回上一层？");
                builder.setPositiveButton("确定", (dialog, which) -> TwoActivity.this.finish());
                builder.setNegativeButton("取消", null);
                builder.setNeutralButton("新游戏", (dialog, which) -> newGame());
                builder.create().show();
                return true;
            }
        return super.dispatchKeyEvent(event);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(TwoActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("恭喜过关！");
                        builder.setMessage("总步数:" + msg.arg1 + "\n开始新游戏吗？");
                        builder.setPositiveButton("确定", (dialog, which) -> newGame());
                        builder.setNegativeButton("取消", null);
                        builder.create().show();
                        break;
                }
                super.handleMessage(msg);
            }
        }
    }
}
