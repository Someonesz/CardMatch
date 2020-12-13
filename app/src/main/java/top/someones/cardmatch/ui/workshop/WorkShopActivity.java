package top.someones.cardmatch.ui.workshop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.databinding.ActivityWorkshopBinding;
import top.someones.cardmatch.entity.Mod;
import top.someones.cardmatch.ui.BaseActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkShopActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private Dialog mLoadingDialog;
    private OkHttpClient mHttpClient;
    private boolean mCancel = false;
    private InputMethodManager mInputMethodManager;

    private ActivityWorkshopBinding mViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityWorkshopBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        //初始化列表
        mViewBinding.modList.setLayoutManager(new LinearLayoutManager(this));

        //连接后端服务,获取首页列表项
        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + "hot").build());
        call.enqueue(new HttpCallback());

        //显示进度条
        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            mCancel = true;
            call.cancel();
            this.finish();
        });

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mViewBinding.actionSearch.setOnClickListener(v -> search(mViewBinding.searchKeyWord.getText().toString().trim(), mViewBinding.searchKeyWord));
        mViewBinding.searchKeyWord.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    search(mViewBinding.searchKeyWord.getText().toString().trim(), v);
                }
                return true;
            }
            return false;
        });
    }

    private void search(String keyWord, View v) {
        // 隐藏软键盘
        if (mInputMethodManager != null)
            mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        startActivity(new Intent(WorkShopActivity.this, SearchResultActivity.class).putExtra("keyword", keyWord));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 去除搜索框的焦点
        mViewBinding.searchBox.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁时清除图片缓存
        ImageCache.cleanWorkshopCache();
    }

    private class HttpCallback implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            mLoadingDialog.dismiss();
            if (mCancel)
                return;
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(WorkShopActivity.this);
                builder.setTitle("网络错误");
                builder.setCancelable(false);
                builder.setNegativeButton("返回", (dialog, which) -> WorkShopActivity.this.finish());
                builder.setMessage(e.getMessage());
                builder.create().show();
            });
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            try {
                String html = response.body().string();
                JSONArray json = new JSONArray(html);
                List<Mod> mods = new ArrayList<>(json.length());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject jsonItem = json.getJSONObject(i);
                    String uuid = jsonItem.getString("uuid");
                    //尝试从缓存中获取图片
                    Bitmap bitmap = ImageCache.getCache(uuid);
                    if (bitmap == null) {
                        //缓存中没有相应的图片，从后端获取
                        Call imageCall = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + uuid + "/img").build());
                        Response imageResponse = imageCall.execute();
                        bitmap = BitmapFactory.decodeStream(imageResponse.body().byteStream());
                        ImageCache.addWorkshopCache(uuid, bitmap);
                    }
                    mods.add(new Mod(uuid, jsonItem.getString("name"), bitmap, jsonItem.getString("author"), jsonItem.getDouble("version"), null));
                }
                Intent intent = new Intent(WorkShopActivity.this, ModInfoActivity.class);
                runOnUiThread(() -> mViewBinding.modList.setAdapter(new ModAdapter(mods, mod -> startActivity(intent.putExtra("uuid", mod.getUUID())))));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WorkShopActivity.this);
                    builder.setTitle("错误");
                    builder.setCancelable(false);
                    builder.setNegativeButton("返回", (dialog, which) -> WorkShopActivity.this.finish());
                    builder.setMessage(e.getMessage());
                    builder.create().show();
                });
            }
            mLoadingDialog.dismiss();
        }
    }
}