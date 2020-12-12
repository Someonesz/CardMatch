package top.someones.cardmatch.ui.workshop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.databinding.ActivityModInfoBinding;
import top.someones.cardmatch.ui.BaseActivity;
import top.someones.cardmatch.ui.ModLiveData;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ModInfoActivity extends BaseActivity {
    private static final String HOSTS = "http://someones.top:12450/mod/";

    private String uuid;
    private Dialog mLoadingDialog;
    private OkHttpClient mHttpClient;
    private boolean mCancel = false;
    private ModLiveData mLiveData;

    private ActivityModInfoBinding mViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityModInfoBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        uuid = getIntent().getStringExtra("uuid");
        if (uuid == null) {
            Toast.makeText(this, "传入数据为空", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + uuid).build());
        mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            mCancel = true;
            call.cancel();
            this.finish();
        });
        call.enqueue(new HttpCallback());

        mLiveData = ModLiveData.getLiveData();
        mViewBinding.take.setOnClickListener(v -> {
            mViewBinding.take.setEnabled(false);
            if ("订阅".contentEquals(mViewBinding.take.getText()) || "更新".contentEquals(mViewBinding.take.getText())) {
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
                            mViewBinding.take.setText("取消订阅");
                            mViewBinding.take.setEnabled(true);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "下载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "MOD安装失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else if ("取消订阅".contentEquals(mViewBinding.take.getText())) {
                if (GameManagement.deleteMod(ModInfoActivity.this, uuid)) {
                    mViewBinding.take.setText("订阅");
                    try {
                        mLiveData.postValue(GameManagement.getMods(this));
                    } catch (Exception e) {
                        Toast.makeText(this, "数据更新失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
                mViewBinding.take.setEnabled(true);
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
                    mViewBinding.modName.setText(name);
                    mViewBinding.modAuthor.setText(author);
                    mViewBinding.modVersion.setText(String.valueOf(version));
                    mViewBinding.modShow.setText(show);
                    mViewBinding.modCover.setImageBitmap(ImageCache.getCache(uuid));
                    double ver = GameManagement.getModVersion(ModInfoActivity.this, uuid);
                    if (ver > 0) {
                        if (ver < version) {
                            mViewBinding.take.setText("更新");
                        } else {
                            mViewBinding.take.setText("取消订阅");
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