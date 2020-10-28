package top.someones.cardmatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.entity.Mod;
import top.someones.cardmatch.ui.ModLiveData;
import top.someones.cardmatch.ui.PermissionsManagement;
import top.someones.cardmatch.ui.TwoActivity;
import top.someones.cardmatch.ui.WorkshopActivity;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 90;
    private static final int REQUEST_READ_ZIP_FILE = 91;
    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    private Intent intent;
    private ModLiveData mLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressDialog mLoadingDialog = ProgressDialog.show(this, "请稍后", "正在加载数据");
        RecyclerView mModList = findViewById(R.id.modList1);
        intent = new Intent(this, TwoActivity.class);
        mLiveData = ModLiveData.getLiveData();

        mModList.setLayoutManager(new LinearLayoutManager(this));
        mLiveData.observe(this, mods -> mModList.setAdapter(new ListAdapter(mods)));
        new Thread(() -> {
            try {
                Mod[] mods = GameManagement.getMods(this);
                runOnUiThread(() -> {
                    mModList.setAdapter(new ListAdapter(mods));
                    mLoadingDialog.dismiss();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show());
                mLoadingDialog.dismiss();
            }
        }).start();
    }

    public void jump(View v) {
        startActivity(new Intent(MainActivity.this, WorkshopActivity.class));
    }

    public void selectFile(View v) {
        if (PermissionsManagement.checkPermissions(this, MainActivity.PERMISSIONS)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
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
                    break;
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
            if (uri != null && uri.getPath() != null) {
                String path = getRealPath(uri.getPath());
                new Thread(() -> {
                    try {
                        GameManagement.installMod(this, new File(path));
                        mLiveData.postValue(GameManagement.getMods(this));
                        runOnUiThread(() -> Toast.makeText(this, "Mod安装成功", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Mod安装失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
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
        FileUtils.deleteQuietly(getFileStreamPath("tmp"));
        super.onDestroy();
    }

    /**
     *
     */
    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private static final String AUTHOR = "作者：";
        private static final String VERSION = "版本：";

        private final Mod[] mModList;

        public ListAdapter(Mod[] modList) {
            this.mModList = modList;
        }

        @NonNull
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_info_layout, parent, false);
            return new ListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
            Mod mod = mModList[position];
            holder.view.setOnClickListener(l -> startActivity(intent.putExtra("uuid", mod.getUUID())));
            holder.modImage.setImageBitmap(mod.getImage());
            holder.modName.setText(mod.getName());
            holder.modAuthor.setText(AUTHOR + mod.getAuthor());
            holder.modVersion.setText(VERSION + mod.getVersion());
        }

        @Override
        public int getItemCount() {
            return mModList.length;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            View view;
            ImageView modImage;
            TextView modName, modAuthor, modVersion;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                modImage = view.findViewById(R.id.modImage);
                modName = view.findViewById(R.id.modName);
                modAuthor = view.findViewById(R.id.modAuthor);
                modVersion = view.findViewById(R.id.modVersion);
            }
        }
    }


}





