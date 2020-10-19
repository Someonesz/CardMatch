package top.someones.cardmatch;

import androidx.appcompat.app.AppCompatActivity;
import top.someones.cardmatch.core.GameManagement;
import top.someones.cardmatch.ui.TwoActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = getFileStreamPath("mod");
        if (!file.exists()) {
            file.mkdir();
        }

        SharedPreferences spf = getSharedPreferences("Config", Context.MODE_PRIVATE);
        if (spf.getBoolean("newInstall", true)) {
            if (GameManagement.installDefaultRes(this)) {
                SharedPreferences.Editor editor = spf.edit();
                editor.putBoolean("newInstall", false);
                editor.apply();
            }
        }
        GameManagement.init(this);
        List<String> list = new LinkedList<>();
        for (String str:GameManagement.getAllGameName()){
            list.add(str);
        }
        ListView listView=findViewById(R.id.gameSelect);
        ArrayAdapter<String> array = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(array);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(MainActivity.this, TwoActivity.class).putExtra("GameName",(String) parent.getItemAtPosition(position)));
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