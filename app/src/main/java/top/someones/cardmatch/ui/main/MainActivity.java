package top.someones.cardmatch.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.someones.cardmatch.ui.BaseActivity;
import top.someones.cardmatch.R;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.databinding.ActivityMainBinding;
import top.someones.cardmatch.databinding.MainListLayoutBinding;
import top.someones.cardmatch.entity.Mod;
import top.someones.cardmatch.ui.PermissionsManagement;
import top.someones.cardmatch.ui.game.GameActivity;
import top.someones.cardmatch.ui.ModLiveData;
import top.someones.cardmatch.ui.workshop.WorkShopActivity;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 90;
    private static final int REQUEST_READ_ZIP_FILE = 91;
    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    private Intent mIntent;
    private ModLiveData mLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        mIntent = new Intent(this, GameActivity.class);
        viewBinding.modList.setLayoutManager(new LinearLayoutManager(this));
        mLiveData = ModLiveData.getLiveData();
        mLiveData.observe(this, mods -> viewBinding.modList.setAdapter(new ListAdapter(mods)));

        Dialog loadingDialog = ProgressDialog.show(this, "请稍后", "正在加载数据");
        new Thread(() -> {
            try {
                Mod[] mods = GameManagement.getMods(this);
                runOnUiThread(() -> {
                    viewBinding.modList.setAdapter(new ListAdapter(mods));
                    loadingDialog.dismiss();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show());
                loadingDialog.dismiss();
            }
        }).start();
    }

    public void selectFile() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.importWorkshop) {
            startActivity(new Intent(MainActivity.this, WorkShopActivity.class));
        } else if (id == R.id.import_local) {
            selectFile();
        }
        return super.onOptionsItemSelected(item);
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
                selectFile();
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

        private final Mod[] mModList;

        public ListAdapter(Mod[] modList) {
            this.mModList = modList;
        }

        @NonNull
        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MainListLayoutBinding binding = MainListLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ListAdapter.ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
            Mod mod = mModList[position];
            holder.binding.getRoot().setOnClickListener(l -> startActivity(mIntent.putExtra("uuid", mod.getUUID())));
            holder.binding.modCover.setImageBitmap(mod.getCover());
            holder.binding.modName.setText(mod.getName());
            holder.binding.modAuthor.setText("作者：".concat(mod.getAuthor()));
            holder.binding.modVersion.setText("版本：".concat(String.valueOf(mod.getVersion())));
        }

        @Override
        public int getItemCount() {
            return mModList.length;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            final MainListLayoutBinding binding;

            public ViewHolder(MainListLayoutBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
