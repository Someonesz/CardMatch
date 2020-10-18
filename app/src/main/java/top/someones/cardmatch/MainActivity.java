package top.someones.cardmatch;

import androidx.appcompat.app.AppCompatActivity;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.ui.MyImageView;
import top.someones.cardmatch.ui.TwoActivity;
import top.someones.cardmatch.ui.ZipActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageSwitcher;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    MyImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = getFileStreamPath("mod");
        if (!file.exists()) {
            file.mkdir();
        }

        GameManagement.addMod(this);

        startActivity(new Intent(this, TwoActivity.class));

        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.b);

        findViewById(R.id.btn1).setOnClickListener(v -> {
            imageView.switchImage(R.drawable.b);
        });

        findViewById(R.id.btn2).setOnClickListener(v -> {
            imageView.switchImage(R.drawable.c);
        });
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

    @Override
    protected void onDestroy() {
        deleteFile(getFileStreamPath("tmp"));
        super.onDestroy();
    }
}