package top.someones.cardmatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.entity.Mod;
import top.someones.cardmatch.ui.ModAdapter;
import top.someones.cardmatch.ui.PermissionsManagement;
import top.someones.cardmatch.ui.TwoActivity;
import top.someones.cardmatch.ui.WorkshopActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 90;
    private static final int REQUEST_READ_ZIP_FILE = 91;
    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    private Intent intent;
    private ProgressDialog loading;
    private RecyclerView modList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = ProgressDialog.show(this, "请稍后", "正在加载数据");
        modList = findViewById(R.id.modList1);
        intent = new Intent(this, TwoActivity.class);
        new Thread(() -> {
            try {
                Mod[] mods = GameManagement.getMods(this);
                runOnUiThread(() -> {
                    try {
                        modList.setAdapter(new ModAdapter(mods, mod -> startActivity(intent.putExtra("uuid", mod.getUUID()))));
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            loading.dismiss();
        }).start();
        modList.setLayoutManager(new LinearLayoutManager(this));
    }

    public void jump(View v) {
        startActivity(new Intent(MainActivity.this, WorkshopActivity.class));
    }

    public void selectFile(View v) {
        if (PermissionsManagement.checkPermissions(this, MainActivity.PERMISSIONS)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_READ_ZIP_FILE);
        } else {
            PermissionsManagement.verifyPermissions(this, MainActivity.PERMISSIONS, MainActivity.REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            boolean hasPermissions = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = false;
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                }
            }
            if (hasPermissions)
                selectFile(null);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_READ_ZIP_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = getRealPath(uri.getPath());
                Log.d("InstallMod", path);
                try {
                    GameManagement.installMod(this, new File(path));
                    Toast.makeText(this, "Mod安装成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Mod安装失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                new Thread(() -> {
                    try {
                        Mod[] mods = GameManagement.getMods(this);
                        runOnUiThread(() -> {
                            try {
                                modList.setAdapter(new ModAdapter(mods, mod -> startActivity(intent.putExtra("uuid", mod.getUUID()))));
                            } catch (Exception ignored) {
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    loading.dismiss();
                }).start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 暴力解决，临时用
     *
     * @param uriPath 返回的路径
     * @return 真实文件路径
     */
    private static String getRealPath(String uriPath) {
        String document = "/document/primary:";
        if (uriPath.startsWith(document)) {
            return Environment.getExternalStorageDirectory().toString() + "/" + uriPath.substring(document.length());
        }
        return uriPath;
    }

    @Override
    protected void onDestroy() {
        deleteFile(getFileStreamPath("tmp"));
        super.onDestroy();
    }

    private void deleteFile(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File tmpFile : file.listFiles())
                if (tmpFile.isDirectory()) {
                    deleteFile(tmpFile);
                } else tmpFile.delete();
        }
        file.delete();
    }

}

