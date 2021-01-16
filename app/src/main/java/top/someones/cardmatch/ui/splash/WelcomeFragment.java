package top.someones.cardmatch.ui.splash;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import top.someones.cardmatch.R;
import top.someones.cardmatch.databinding.FragmentWelcomeBinding;

/**
 * 欢迎页面
 */
public class WelcomeFragment extends Fragment {
    private static final String PARAM_1 = "ImageResourceId";

    private int mImageResourceId;

    /**
     * 使用此工厂方法提供的参数创建此片段的新实例。
     *
     * @param imageResourceId 图片资源ID。
     * @return 片段WelcomeFragment的新实例。
     */
    public static WelcomeFragment newInstance(int imageResourceId) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_1, imageResourceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageResourceId = getArguments().getInt(PARAM_1);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 膨胀此Fragment的布局
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 视图创建完成，绑定事件
        FragmentWelcomeBinding viewBinding = FragmentWelcomeBinding.bind(view);
        viewBinding.welcomeImage.setImageResource(mImageResourceId);
    }
}