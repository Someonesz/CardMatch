package top.someones.cardmatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.ui.TwoActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipFile;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 90;
    private static final int REQUEST_READ_ZIP_FILE = 91;

    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean mAllowRead = false;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAllowRead = checkPermissions(this);
        if (!mAllowRead) {
            verifyPermissions(this);
        }
        listView = findViewById(R.id.gameSelect);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new LinkedList<>(GameManagement.getAllGameName(this))));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(MainActivity.this, TwoActivity.class).putExtra("GameName", (String) parent.getItemAtPosition(position)));
        });
    }

    public void selectFile(View v) {
        if (mAllowRead) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_READ_ZIP_FILE);
        } else {
            Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
            verifyPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED)
                    return;
            }
            mAllowRead = true;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_READ_ZIP_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                File file = new File(uri.getPath());
                try {
                    ZipFile zipFile = new ZipFile(uri.getPath());
                    Toast.makeText(this, "Mod安装：" + GameManagement.installMod(this, zipFile), Toast.LENGTH_SHORT).show();
                    listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new LinkedList<>(GameManagement.getAllGameName(this))));
                } catch (IOException e) {
                    Log.d("InstallMod", e.getMessage());
                    Toast.makeText(this, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkPermissions(Context context) {
        for (String permission : MainActivity.PERMISSIONS) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    public void verifyPermissions(Activity activity) {
        try {
            ActivityCompat.requestPermissions(activity, MainActivity.PERMISSIONS, REQUEST_EXTERNAL_STORAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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