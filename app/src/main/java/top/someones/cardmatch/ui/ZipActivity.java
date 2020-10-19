package top.someones.cardmatch.ui;

import androidx.appcompat.app.AppCompatActivity;
import top.someones.cardmatch.R;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zip);

        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView2);

        button.setOnClickListener(l -> {
            readModFile(getFileStreamPath("Solar.zip"));
        });


    }


    private boolean readModFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            if (!testFile(zipFile))
                return false;
            String uuid = UUID.randomUUID().toString();
            String part = getFileStreamPath("mod").getPath() + "/" + uuid;
            new File(part).mkdirs();
            Enumeration<? extends ZipEntry> entris = zipFile.entries();
            while (entris.hasMoreElements()) {
                ZipEntry zipEntry = entris.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                writeFile(zipFile.getInputStream(zipEntry), new File(part, zipEntry.getName()));
            }
            zipFile.close();
        } catch (Exception e) {
            Log.d("filess", e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean testFile(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entris = zipFile.entries();
        while (entris.hasMoreElements()) {
            ZipEntry zipEntry = entris.nextElement();
            if (zipEntry.isDirectory())
                continue;
            if (zipEntry.getName().equals("GameConfig.json")) {
                String str = readTextFile(zipFile.getInputStream(zipEntry));
                try {
                    JSONObject json = new JSONObject(str);
                    return "Card Match".equals(json.getString("for"));
                } catch (JSONException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private String readTextFile(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String str = br.readLine();
            while (str != null) {
                sb.append(str);
                str = br.readLine();
            }
            input.close();
        }
        return sb.toString();
    }

    private void writeFile(InputStream input, File outFile) throws IOException {
        byte[] bytes = new byte[2048];
        int len;
        if (outFile.exists())
            outFile.delete();
        outFile.createNewFile();
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            while ((len = input.read(bytes)) > 0) {
                out.write(bytes, 0, len);
            }
            out.flush();
            input.close();
        }

    }


}