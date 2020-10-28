package top.someones.cardmatch.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.ImageCache;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ModInfoActivity extends AppCompatActivity {
    private static final String DOMAIN = "http://192.168.3.14:8080/CardMatchService/";
    private ProgressDialog loading;
    private TextView modName, modAuthor, modVersion, modShow;
    private ImageView modCover;
    private Button take;
    private OkHttpClient httpClient;
    private String uuid;

    private ModLiveData mLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info);

        String uuid = getIntent().getStringExtra("uuid");
        if (uuid == null) {
            Toast.makeText(this, "传入数据为空", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        this.uuid = uuid;
        modName = findViewById(R.id.modName);
        modAuthor = findViewById(R.id.modAuthor);
        modVersion = findViewById(R.id.modVersion);
        modShow = findViewById(R.id.modShow);
        modCover = findViewById(R.id.modCover);
        take = findViewById(R.id.take);

        loading = ProgressDialog.show(this, "请稍后", "正在加载数据", true, false, dialog -> ModInfoActivity.this.finish());

        mLiveData = ModLiveData.getLiveData();

        httpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(DOMAIN + "ModInfoServlet?uuid=" + uuid).build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                loading.dismiss();
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
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        modName.setText(json.getString("Mod_Name"));
                        modAuthor.setText(json.getString("Author"));
                        double version = json.getDouble("Version");
                        modVersion.setText(version + "");
                        modShow.setText(json.getString("Show"));
                        Bitmap bitmap = ImageCache.getCache(uuid);
                        modCover.setImageBitmap(bitmap);
                        double ver = GameManagement.getModVersion(ModInfoActivity.this, uuid);
                        if (ver > 0) {
                            if (ver < version) {
                                take.setText("更新");
                            } else {
                                take.setText("取消订阅");
                            }
                        }
                        loading.dismiss();
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModInfoActivity.this);
                            builder.setTitle("错误");
                            builder.setCancelable(false);
                            builder.setNegativeButton("返回", (dialog, which) -> ModInfoActivity.this.finish());
                            builder.setMessage(e.getMessage());
                            builder.create().show();
                        });
                    }
                });
            }
        });

        take.setOnClickListener(v -> {
            Button btn = (Button) v;
            btn.setEnabled(false);
            if ("订阅".contentEquals(btn.getText()) || "更新".contentEquals(btn.getText())) {
                Toast.makeText(this, "正在下载", Toast.LENGTH_SHORT).show();
                downloadAndInstall(btn);
            } else if ("取消订阅".contentEquals(btn.getText())) {
                if (GameManagement.deleteMod(ModInfoActivity.this, uuid)) {
                    btn.setText("订阅");
                    try {
                        mLiveData.postValue(GameManagement.getMods(this));
                    } catch (Exception e) {
                        Toast.makeText(this, "数据更新失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
                btn.setEnabled(true);
            }
        });
    }

    public void downloadAndInstall(Button btn) {
        new Thread(() -> {
            Request down = new Request.Builder().get().url(DOMAIN + "DownloadModServlet?uuid=" + uuid).build();
            try {
                Response response = httpClient.newCall(down).execute();
                File tmpFile = new File(this.getFileStreamPath("tmp"), uuid);
                FileUtils.copyToFile(response.body().byteStream(), tmpFile);
                GameManagement.installMod(this, tmpFile);
                mLiveData.postValue(GameManagement.getMods(this));
                runOnUiThread(() -> {
                    Toast.makeText(this, "任务完成", Toast.LENGTH_SHORT).show();
                    btn.setText("取消订阅");
                    btn.setEnabled(true);
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "下载失败" + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "MOD安装失败" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}