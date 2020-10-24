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
    private static final String DOMAIN = "http://192.168.43.50:8080/CardMatchService/";
    private OkHttpClient httpClient;
    private RecyclerView modList;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop);

        modList = findViewById(R.id.modList);
        modList.setLayoutManager(new LinearLayoutManager(this));
        loading = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊");
        httpClient = new OkHttpClient();
        Intent intent = new Intent(WorkshopActivity.this, ModInfoActivity.class);

        Request request = new Request.Builder().get().url(DOMAIN + "Index").build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
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
                    JSONObject json = new JSONObject(html);
                    JSONArray jarr = json.getJSONArray("hot");
                    Mod[] mods = new Mod[jarr.length()];
                    for (int i = 0; i < jarr.length(); i++) {
                        mods[i] = jsonToMod(jarr.getJSONObject(i));
                    }
                    runOnUiThread(() -> modList.setAdapter(new ModAdapter(mods, v -> startActivity(intent.putExtra("uuid", v.getUUID())))));
                } catch (Exception e) {
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
            Call imageCall = httpClient.newCall(new Request.Builder().get().url(DOMAIN + "GetImgBinServlet?uuid=" + uuid).build());
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