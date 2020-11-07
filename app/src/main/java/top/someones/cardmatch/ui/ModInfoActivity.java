package top.someones.cardmatch.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.BaseActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.ImageCache;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ModInfoActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private String uuid;
    private ProgressDialog mLoadingDialog;
    private OkHttpClient mHttpClient;
    private boolean mCancel = false;
    private ModLiveData mLiveData;

    private TextView mNameView, mAuthorView, mVersionView, mShowView;
    private ImageView mCoverView;
    private Button mTakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info);

        uuid = getIntent().getStringExtra("uuid");
        if (uuid == null) {
            Toast.makeText(this, "传入数据为空", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        mNameView = findViewById(R.id.modName);
        mAuthorView = findViewById(R.id.modAuthor);
        mVersionView = findViewById(R.id.modVersion);
        mShowView = findViewById(R.id.modShow);
        mCoverView = findViewById(R.id.modCover);
        mTakeView = findViewById(R.id.take);

        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + uuid).build());
        call.enqueue(new HttpCallback());

        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            mCancel = true;
            call.cancel();
            this.finish();
        });

        mLiveData = ModLiveData.getLiveData();

        mTakeView.setOnClickListener(v -> {
            mTakeView.setEnabled(false);
            if ("订阅".contentEquals(mTakeView.getText()) || "更新".contentEquals(mTakeView.getText())) {
                Toast.makeText(this, "正在下载", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    try {
                        Response response = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + uuid + "/zip").build()).execute();
                        File tmpFile = new File(ModInfoActivity.this.getFileStreamPath("tmp"), uuid);
                        FileUtils.copyToFile(response.body().byteStream(), tmpFile);
                        GameManagement.installMod(this, tmpFile);
                        mLiveData.postValue(GameManagement.getMods(this));
                        runOnUiThread(() -> {
                            Toast.makeText(this, "任务完成", Toast.LENGTH_SHORT).show();
                            mTakeView.setText("取消订阅");
                            mTakeView.setEnabled(true);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "下载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "MOD安装失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else if ("取消订阅".contentEquals(mTakeView.getText())) {
                if (GameManagement.deleteMod(ModInfoActivity.this, uuid)) {
                    mTakeView.setText("订阅");
                    try {
                        mLiveData.postValue(GameManagement.getMods(this));
                    } catch (Exception e) {
                        Toast.makeText(this, "数据更新失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
                mTakeView.setEnabled(true);
            }
        });
    }

    private class HttpCallback implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            mLoadingDialog.dismiss();
            if (mCancel)
                return;
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(ModInfoActivity.this);
                builder.setTitle("网络错误");
                builder.setCancelable(false);
                builder.setNegativeButton("返回", (dialog, which) -> ModInfoActivity.this.finish());
                builder.setMessage(e.getMessage());
                builder.create().show();
            });
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            try {
                JSONObject json = new JSONObject(response.body().string());
                String name = json.getString("name");
                String author = json.getString("author");
                double version = json.getDouble("version");
                String show = json.getString("show");
                runOnUiThread(() -> {
                    mNameView.setText(name);
                    mAuthorView.setText(author);
                    mVersionView.setText(String.valueOf(version));
                    mShowView.setText(show);
                    mCoverView.setImageBitmap(ImageCache.getCache(uuid));
                    double ver = GameManagement.getModVersion(ModInfoActivity.this, uuid);
                    if (ver > 0) {
                        if (ver < version) {
                            mTakeView.setText("更新");
                        } else {
                            mTakeView.setText("取消订阅");
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ModInfoActivity.this);
                    builder.setTitle("错误");
                    builder.setCancelable(false);
                    builder.setNegativeButton("返回", (dialog, which) -> ModInfoActivity.this.finish());
                    builder.setMessage(e.getMessage());
                    builder.create().show();
                });
            }
            mLoadingDialog.dismiss();
        }
    }

}