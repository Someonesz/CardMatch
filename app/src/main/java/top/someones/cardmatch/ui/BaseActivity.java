package top.someones.cardmatch.ui;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void immersionStatusBar(boolean textColorReversal) {
        this.getWindow().setStatusBarColor(Color.TRANSPARENT);
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        int vis = decorView.getSystemUiVisibility();
        if (textColorReversal) {
            vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(vis);
    }

}
