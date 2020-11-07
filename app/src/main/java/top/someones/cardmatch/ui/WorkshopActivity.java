package top.someones.cardmatch.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.BaseActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.entity.Mod;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WorkshopActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private ProgressDialog mLoadingDialog;
    private OkHttpClient mHttpClient;
    private boolean mCancel = false;

    private RecyclerView mModListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop);

        //初始化列表
        mModListView = findViewById(R.id.modList);
        mModListView.setLayoutManager(new LinearLayoutManager(this));

        //连接后端服务,获取首页列表项
        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + "hot").build());

        //显示进度条
        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            mCancel = true;
            call.cancel();
            this.finish();
        });

        call.enqueue(new HttpCallback());
    }

    @Override
    protected void onDestroy() {
        //销毁时清除图片缓存
        ImageCache.cleanWorkshopCache();
        super.onDestroy();
    }

    private class HttpCallback implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            mLoadingDialog.dismiss();
            if (mCancel)
                return;
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(WorkshopActivity.this);
                builder.setTitle("网络错误");
                builder.setCancelable(false);
                builder.setNegativeButton("返回", (dialog, which) -> WorkshopActivity.this.finish());
                builder.setMessage(e.getMessage());
                builder.create().show();
            });
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            try {
                String html = response.body().string();
                JSONArray json = new JSONArray(html);
                Mod[] mods = new Mod[json.length()];
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
                    mods[i] = new Mod(uuid, jsonItem.getString("name"), bitmap, jsonItem.getString("author"), jsonItem.getDouble("version"), null);
                }
                Intent intent = new Intent(WorkshopActivity.this, ModInfoActivity.class);
                runOnUiThread(() -> mModListView.setAdapter(new ModAdapter(mods, mod -> startActivity(intent.putExtra("uuid", mod.getUUID())))));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WorkshopActivity.this);
                    builder.setTitle("错误");
                    builder.setCancelable(false);
                    builder.setNegativeButton("返回", (dialog, which) -> WorkshopActivity.this.finish());
                    builder.setMessage(e.getMessage());
                    builder.create().show();
                });
            }
            mLoadingDialog.dismiss();
        }
    }
}