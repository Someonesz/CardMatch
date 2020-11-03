package top.someones.cardmatch.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import top.someones.cardmatch.R;
import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.entity.Mod;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class WorkshopActivity extends AppCompatActivity {
    private static final String DOMAIN = "http://192.168.3.14:8080/";

    private ProgressDialog loading;
    private OkHttpClient mHttpClient;
    private boolean mCancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop);

        RecyclerView modList = findViewById(R.id.modList);
        modList.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = new Intent(this, ModInfoActivity.class);
        mHttpClient = new OkHttpClient();
        Call call = mHttpClient.newCall(new Request.Builder().get().url(DOMAIN + "hot").build());
        loading = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊", true, true, l -> {
            mCancel = true;
            call.cancel();
            this.finish();
        });

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (mCancel)
                    return;
                loading.dismiss();
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
                loading.dismiss();
                try {
                    String html = response.body().string();
                    Log.d("netd", html);
                    JSONArray json = new JSONArray(html);
                    Mod[] mods = new Mod[json.length()];
                    for (int i = 0; i < json.length(); i++) {
                        mods[i] = jsonToMod(json.getJSONObject(i));
                    }
                    runOnUiThread(() -> modList.setAdapter(new ModAdapter(mods, v -> startActivity(intent.putExtra("uuid", v.getUUID())))));
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
            }
        });
    }

    private Mod jsonToMod(JSONObject json) throws Exception {
        String uuid = json.getString("UUID");
        Bitmap bitmap = ImageCache.getCache(uuid);
        if (bitmap == null) {
            Call imageCall = mHttpClient.newCall(new Request.Builder().get().url(DOMAIN + uuid + "/img").build());
            Response imageResponse = imageCall.execute();
            bitmap = BitmapFactory.decodeStream(imageResponse.body().byteStream());
            if (bitmap != null) {
                ImageCache.addWorkshopCache(uuid, bitmap);
            }
        }
        return new Mod(uuid, json.getString("Mod_Name"), bitmap, json.getString("Author"), json.getDouble("Version"), null);
    }

    @Override
    protected void onDestroy() {
        ImageCache.cleanWorkshopCache();
        super.onDestroy();
    }
}