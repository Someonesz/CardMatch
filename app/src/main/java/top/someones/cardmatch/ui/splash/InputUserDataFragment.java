package top.someones.cardmatch.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import top.someones.cardmatch.R;
import top.someones.cardmatch.databinding.FragmentInputUserDataBinding;
import top.someones.cardmatch.ui.main.MainActivity;

/**
 * 欢迎页面最后一页
 * 询问昵称并保存
 */
public class InputUserDataFragment extends Fragment {

    private InputMethodManager mInputMethodManager;

    /**
     * 使用此工厂方法创建此片段的新实例。
     *
     * @return 片段WelcomeFragment的新实例。
     */
    public static InputUserDataFragment newInstance() {
        return new InputUserDataFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 膨胀此Fragment的布局
        return inflater.inflate(R.layout.fragment_input_user_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 视图创建完成，绑定事件
        FragmentInputUserDataBinding viewBinding = FragmentInputUserDataBinding.bind(view);
        viewBinding.nickName.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (mInputMethodManager != null)
                        mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    submit(viewBinding.nickName.getText().toString().trim());
                }
                return true;
            }
            return false;
        });
        viewBinding.submit.setOnClickListener(v -> submit(viewBinding.nickName.getText().toString().trim()));
    }

    private void submit(String nickName) {
        if (nickName.length() < 1) {
            Toast.makeText(getContext(), "请输入昵称", Toast.LENGTH_SHORT).show();
        } else {
            Context context = getContext();
            if (context == null)
                return;
            SharedPreferences.Editor editor = context.getSharedPreferences("game", Context.MODE_PRIVATE).edit();
            editor.putString("nickName", nickName);
            editor.apply();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}