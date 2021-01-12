package top.someones.cardmatch.ui.splash;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import top.someones.cardmatch.R;
import top.someones.cardmatch.databinding.ActivitySplashBinding;
import top.someones.cardmatch.ui.BaseActivity;
import top.someones.cardmatch.ui.main.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private static final int[] ImageResource = {R.mipmap.page1, R.mipmap.page2, R.mipmap.page3, R.mipmap.page4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding viewBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        super.immersionStatusBar(true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        String nickName = getSharedPreferences("game", Context.MODE_PRIVATE).getString("nickName", null);
        Log.d("启动", "nickName:" + nickName);
        if (nickName != null) {
            startActivity(intent);
        } else {
            List<Fragment> fragments = new ArrayList<>(ImageResource.length + 1);
            for (int id : ImageResource) {
                fragments.add(WelcomeFragment.newInstance(id));
            }
            fragments.add(InputUserDataFragment.newInstance());
            viewBinding.welcomePager.setAdapter(new SplashPageAdapter(this, fragments));

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            viewBinding.navigationList.setLayoutManager(layoutManager);
            NavigationAdapter adapter = new NavigationAdapter(ImageResource.length + 1);
            viewBinding.navigationList.setAdapter(adapter);
            viewBinding.welcomePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    adapter.setSelect(position);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            });
        }
    }

}