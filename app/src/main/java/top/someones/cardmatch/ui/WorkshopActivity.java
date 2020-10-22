package top.someones.cardmatch.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.core.ImageCache;
import top.someones.cardmatch.entity.Mod;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorkshopActivity extends AppCompatActivity {

    private RecyclerView modList;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop);

        modList = findViewById(R.id.modList);
        modList.setLayoutManager(new LinearLayoutManager(this));
        loading = ProgressDialog.show(this, "请稍后", "正在连接到创意工坊");
        Intent intent = new Intent(WorkshopActivity.this, ModInfoActivity.class);

        Mod[] mods = GameManagement.getMods(this);

        Request request = new Request.Builder().get().url("https:someones.top").build();
        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WorkshopActivity.this);
                            builder.setTitle("网络错误");
                            builder.setMessage(e.getMessage());
                            builder.setCancelable(false);
                            builder.setNegativeButton("返回", (dialog, which) -> WorkshopActivity.this.finish());
                            builder.create().show();
                        }
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("net!", response.code() + "");
                runOnUiThread(() -> {
                    modList.setAdapter(new ModAdapter(mods, v -> {
                        startActivity(intent.putExtra("dd", "ds"));
                    }));
                    loading.dismiss();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        ImageCache.cleanWorkshopCache();
        super.onDestroy();
    }
}