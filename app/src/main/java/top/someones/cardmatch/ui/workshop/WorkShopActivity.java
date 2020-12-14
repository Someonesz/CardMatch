package top.someones.cardmatch.ui.workshop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkShopActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private Dialog mLoadingDialog;
    private final List<Mod> mResultSet = new ArrayList<>();
    private ModAdapter mListAdapter;
    private OkHttpClient mHttpClient;
    private InputMethodManager mInputMethodManager;

    private ActivityWorkshopBinding mViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityWorkshopBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        // 初始化列表
        Intent intent = new Intent(WorkShopActivity.this, ModInfoActivity.class);
        mListAdapter = new ModAdapter(mResultSet, mod -> startActivity(intent.putExtra("uuid", mod.getUUID())));
        mViewBinding.modList.setAdapter(mListAdapter);
        mViewBinding.modList.setLayoutManager(new LinearLayoutManager(this));

        // 连接后端服务,获取首页列表项
        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + "hot").build());
        // 显示进度条
        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            call.cancel();
            this.finish();
        });
        call.enqueue(new HttpCallback());

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mViewBinding.actionSearch.setOnClickListener(v -> searchByKeyWord(mViewBinding.searchKeyWord.getText().toString().trim()));
        mViewBinding.searchKeyWord.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    searchByKeyWord(mViewBinding.searchKeyWord.getText().toString().trim());
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 按关键字搜索
     *
     * @param keyWord 关键字
     */
    private void searchByKeyWord(String keyWord) {
        // 隐藏软键盘
        if (mInputMethodManager != null)
            mInputMethodManager.hideSoftInputFromWindow(mViewBinding.searchKeyWord.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        // 销毁时清除图片缓存
        ImageCache.cleanWorkshopCache();
    }

    private class HttpCallback implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            // 如果主动取消请求则不显示错误提示框
            if (call.isCanceled()) {
                mLoadingDialog.dismiss();
                return;
            }
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.startsWith("Failed to connect to someones.top"))
                    errorMsg = "无法连接到服务器。";
                else if (errorMsg.startsWith("Unable to resolve host \"someones.top\"")) {
                    errorMsg = "无法连接到服务器，请检查网络连接。";
                }
            }
            showErrorDialog("网络错误", errorMsg);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            String html = response.body().string();
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(html);
            } catch (JSONException e) {
                showErrorDialog("错误", "返回的信息不是JSON格式");
                return;
            }
            try {
                mResultSet.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonItem = jsonArray.getJSONObject(i);
                    String uuid = jsonItem.getString("uuid");
                    // 尝试从缓存中获取图片
                    Bitmap bitmap = ImageCache.getCache(uuid);
                    if (bitmap == null) {
                        // 缓存中没有相应的图片，从后端获取
                        Call imageCall = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + uuid + "/img").build());
                        Response imageResponse = imageCall.execute();
                        bitmap = BitmapFactory.decodeStream(imageResponse.body().byteStream());
                        ImageCache.addWorkshopCache(uuid, bitmap);
                    }
                    mResultSet.add(new Mod(uuid, jsonItem.getString("name"), bitmap, jsonItem.getString("author"), jsonItem.getDouble("version"), null));
                }
                runOnUiThread(mListAdapter::notifyDataSetChanged);
                mLoadingDialog.dismiss();
            } catch (JSONException e) {
                showErrorDialog("错误", "JSON解析错误");
            }
        }

        /**
         * 显示错误提示框
         *
         * @param title 提示框标题
         * @param msg   提示框正文
         */
        private void showErrorDialog(String title, String msg) {
            mLoadingDialog.dismiss();
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(WorkShopActivity.this);
                builder.setTitle(title);
                builder.setNegativeButton("返回", (dialog, which) -> WorkShopActivity.this.finish());
                builder.setOnCancelListener(v -> WorkShopActivity.this.finish());
                builder.setMessage(msg);
                builder.create().show();
            });
        }
    }
}