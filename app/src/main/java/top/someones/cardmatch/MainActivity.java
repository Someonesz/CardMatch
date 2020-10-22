package top.someones.cardmatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.entity.GameResource;
import top.someones.cardmatch.ui.CreativeWorkshopActivity;
import top.someones.cardmatch.ui.PermissionsManagement;
import top.someones.cardmatch.ui.TwoActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 90;
    private static final int REQUEST_READ_ZIP_FILE = 91;

    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.gameSelect);
        listView.setAdapter(new ListAdapter(this, GameManagement.getAllGameRes(this)));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(MainActivity.this, TwoActivity.class).putExtra("GameName", ((GameResource) parent.getItemAtPosition(position)).getUUID()));
        });

        startActivity(new Intent(MainActivity.this, CreativeWorkshopActivity.class));
    }

    public void jump(View v) {
        startActivity(new Intent(MainActivity.this, CreativeWorkshopActivity.class));
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
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                return;
            }
            selectFile(null);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_READ_ZIP_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    String path = getRealPath(uri.getPath());
                    Log.d("InstallMod", path);
                    ZipFile zipFile = new ZipFile(path);
                    Toast.makeText(this, "Mod安装：" + GameManagement.installMod(this, zipFile), Toast.LENGTH_SHORT).show();
                    listView.setAdapter(new ListAdapter(this, GameManagement.getAllGameRes(this)));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("InstallMod", e.getMessage());
                    Toast.makeText(this, "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    private static class ListAdapter extends BaseAdapter {

        private final GameResource[] gamesResource;
        private final LayoutInflater mInflater;

        public ListAdapter(Context context, GameResource[] gamesResource) {
            mInflater = LayoutInflater.from(context);
            this.gamesResource = gamesResource;
        }

        @Override
        public int getCount() {
            return gamesResource.length;
        }

        @Override
        public Object getItem(int position) {
            return gamesResource[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            final TextView text;

            if (convertView == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }
            text = (TextView) view;
            text.setText(gamesResource[position].getName());
            return text;
        }
    }
}

