package top.someones.cardmatch.ui.workshop;

import androidx.recyclerview.widget.LinearLayoutManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.databinding.ActivitySearchResultBinding;
import top.someones.cardmatch.entity.Mod;
import top.someones.cardmatch.ui.BaseActivity;

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
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private OkHttpClient mHttpClient;
    private final List<Mod> mSearchResult = new ArrayList<>();
    private ModAdapter mResultListAdapter;
    private InputMethodManager mInputMethodManager;
    private Dialog mLoadingDialog;

    private ActivitySearchResultBinding mViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        // 获取参数
        String keyWord = getIntent().getStringExtra("keyword");
        if (keyWord == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        // 准备列表
        Intent intent = new Intent(this, ModInfoActivity.class);
        mResultListAdapter = new ModAdapter(mSearchResult, mod -> startActivity(intent.putExtra("uuid", mod.getUUID())));
        mViewBinding.searchResultList.setAdapter(mResultListAdapter);
        mViewBinding.searchResultList.setLayoutManager(new LinearLayoutManager(this));

        // 异步请求数据
        mHttpClient = new OkHttpClient();
        searchByKeyWord(keyWord);

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mViewBinding.searchKeyWord.setText(keyWord);
        // 绑定事件
        mViewBinding.actionBack.setOnClickListener(v -> this.finish());
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
        // 去除搜索框的焦点
        mViewBinding.searchBox.requestFocus();
        // 准备Post请求用的表单
        FormBody formBody = new FormBody.Builder().add("keyword", keyWord).build();
        Request request = new Request.Builder().url(HOSTS + "search/").post(formBody).build();
        Call call = mHttpClient.newCall(request);
        // 显示进度条
        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在搜索", true, true, l -> {
            call.cancel();
        });
        // 开始异步网络请求
        call.enqueue(new HttpCallBack());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 去除搜索框的焦点
        mViewBinding.searchBox.requestFocus();
    }

    private class HttpCallBack implements Callback {

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
            String in = response.body().string();
            JSONObject json;
            try {
                json = new JSONObject(in);
            } catch (JSONException e) {
                showErrorDialog("错误", "返回的信息不是JSON格式");
                return;
            }
            try {
                if (json.getInt("flag") != 1) {
                    showErrorDialog("错误", "服务器内部错误");
                    return;
                }
                mSearchResult.clear();
                JSONArray jsonArray = json.getJSONArray("result");
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
                    mSearchResult.add(new Mod(uuid, jsonItem.getString("name"), bitmap, jsonItem.getString("author"), jsonItem.getDouble("version"), null));
                }
                runOnUiThread(mResultListAdapter::notifyDataSetChanged);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultActivity.this);
                builder.setTitle(title);
                builder.setNegativeButton("我知道了", null);
                builder.setMessage(msg);
                builder.create().show();
            });
        }
    }
}