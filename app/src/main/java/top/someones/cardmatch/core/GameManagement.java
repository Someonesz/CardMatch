package top.someones.cardmatch.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import androidx.annotation.RequiresApi;
import top.someones.cardmatch.R;
import top.someones.cardmatch.entity.Mod;

/**
 * 信息管理
 *
 * @author Someones
 * @version 1.5
 */
public class GameManagement {

    private static boolean mInitialized = false;
    private static Bitmap mDefaultBitmap;
    private static final Random random = new Random();

    private GameManagement() {
    }

    private static SQLiteOpenHelper getSQLiteOpenHelper(Context context) {
        return new DatabaseHelper(context);

    }

    private static void init(Context context) throws Exception {
        mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
        File modDirectory = context.getFileStreamPath("mod");
        if (!modDirectory.exists()) {
            modDirectory.mkdirs();
        }
        File[] files = modDirectory.listFiles();
        if (files == null) {
            modDirectory.delete();
            modDirectory.mkdirs();
        } else {
            if (files.length == 0) {
                File tmpDirectory = copyDefaultRes(context);
                for (File modFile : tmpDirectory.listFiles()) {
                    installMod(context, modFile);
                }
            }
        }
        mInitialized = true;
    }

    public static void installMod(Context context, File modFile) throws Exception {
        ZipFile zipFile = getZipFile(modFile);
        JSONObject config = testFile(zipFile);
        if (config == null)
            throw new Exception("读取配置文件失败");

        String uuid = config.getString("UUID");
        String name = config.getString("Mod_Name");
        String author = config.getString("Author");
        String version = config.getString("Version");
        String frontImageName = config.getString("FrontImageName");
        JSONArray backImageName = config.getJSONArray("BackImageName");
        String show = null;
        if (config.has("Show"))
            show = config.getString("Show");
        String cover = null;
        if (config.has("Cover"))
            cover = config.getString("Cover");

        if (backImageName.length() < GameObserver.MAX_VIEW / 2)
            throw new Exception("Mod图片太少");

        StringBuilder backImageString = new StringBuilder();
        for (int i = 0; i < backImageName.length(); i++) {
            backImageString.append(backImageName.getString(i));
            backImageString.append(":");
        }
        backImageString.deleteCharAt(backImageString.length() - 1);

        String part = context.getFileStreamPath("mod").getPath() + "/" + uuid;
        try {
            File modDirectory = new File(part);
            if (modDirectory.exists()) {
                deleteFile(modDirectory);
            }
            if (!modDirectory.mkdirs())
                throw new Exception("Mod目录创建失败");
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                writeFile(zipFile.getInputStream(zipEntry), new File(part, zipEntry.getName()));
            }
            zipFile.close();

            try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL(SQL.DELETE_MOD, new Object[]{uuid});
                db.execSQL(SQL.ADD_MOD, new Object[]{uuid, name, author, show, version, part, cover, frontImageName, backImageString.toString()});
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("数据库出错：" + e.getMessage());
            }
        } catch (Exception e) {
            deleteFile(new File(part));
            throw e;
        }
    }

    /**
     * zip编码检查
     * 临时使用,后续使用 Apache Commons Compress™ 处理
     *
     * @param zipFile zip文件
     * @return 转换后的zip
     * @throws IOException 文件不是一个zip文件
     */
    private static ZipFile getZipFile(File zipFile) throws IOException {
        ZipFile zip = new ZipFile(zipFile, StandardCharsets.UTF_8);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        try {
            while (entries.hasMoreElements()) {
                entries.nextElement();
            }
            zip.close();
            zip = new ZipFile(zipFile, StandardCharsets.UTF_8);
            return zip;
        } catch (Exception e) {
            zip = new ZipFile(zipFile, Charset.forName("GBK"));
            return zip;
        }
    }

    public static double getModVersion(Context context, String uuid) {
        try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(SQL.FIND_MOD_VERSION, new String[]{uuid})) {
                if (cursor.moveToNext()) {
                    return cursor.getDouble(cursor.getColumnIndex("Version"));
                } else {
                    return -1;
                }
            }
        }
    }

    public static Mod[] getMods(Context context) throws Exception {
        if (!mInitialized)
            init(context);
        Mod[] mods;
        try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(SQL.SELECT_ALL_MOD, new String[0])) {
                List<Mod> list = new LinkedList<>();
                while (cursor.moveToNext()) {
                    String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));
                    String author = cursor.getString(cursor.getColumnIndex("Author"));
                    String show = cursor.getString(cursor.getColumnIndex("Show"));
                    Double version = cursor.getDouble(cursor.getColumnIndex("Version"));
                    String resPath = cursor.getString(cursor.getColumnIndex("ResPath"));
                    Bitmap bitmap = ImageCache.getCache(uuid);
                    if (bitmap == null) {
                        String cover = cursor.getString(cursor.getColumnIndex("Cover"));
                        bitmap = BitmapFactory.decodeFile(resPath + "/" + cover);
                        if (bitmap == null) {
                            String backRes = cursor.getString(cursor.getColumnIndex("BackRes"));
                            String[] subString = backRes.split(":");
                            bitmap = BitmapFactory.decodeFile(resPath + "/" + subString[random.nextInt(subString.length)]);
                        }
                        if (bitmap == null) {
                            bitmap = mDefaultBitmap;
                        }
                        ImageCache.addBaseCache(uuid, bitmap);
                    }
                    list.add(new Mod(uuid, name, bitmap, author, version, show));
                }
                mods = list.toArray(new Mod[0]);
            }
        }
        return mods;
    }

    public static GameObserver getGameObserver(String uuid, Context context, Handler handler) {
        String resPath;
        String frontResName;
        String[] backResName;
        try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(SQL.INIT_GAME, new String[]{uuid})) {
                if (cursor.moveToNext()) {
                    resPath = cursor.getString(cursor.getColumnIndex("ResPath"));
                    frontResName = cursor.getString(cursor.getColumnIndex("FrontRes"));
                    backResName = cursor.getString(cursor.getColumnIndex("BackRes")).split(":");
                } else return null;
            }
        }
        Bitmap frontRes;
        Bitmap[] backRes;
        try {
            frontRes = BitmapFactory.decodeStream(new FileInputStream(new File(resPath, frontResName)));
            List<Bitmap> bitmaps = new LinkedList<>();
            for (String fileName : backResName) {
                File resFile = new File(resPath, fileName);
                if (resFile.isFile()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(resPath + "/" + fileName);
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    }
                }
            }
            if (bitmaps.size() < GameObserver.MAX_VIEW / 2)
                return null;
            backRes = new Bitmap[bitmaps.size()];
            bitmaps.toArray(backRes);
        } catch (FileNotFoundException e) {
            return null;
        }
        if (frontRes != null)
            return new GameObserverAdaptor(context, handler, frontRes, backRes);
        return null;
    }

    private static File copyDefaultRes(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] resFilesName = assetManager.list("DefaultRes");
        File tmpDirectory = context.getFileStreamPath("tmp");
        if (!tmpDirectory.isDirectory()) {
            if (tmpDirectory.exists()) {
                deleteFile(tmpDirectory);
            }
            if (!tmpDirectory.mkdirs())
                throw new IOException("临时目录创建失败");
        }
        for (String resFileName : resFilesName) {
            File resFile = new File(tmpDirectory, resFileName);
            resFile.createNewFile();
            writeFile(assetManager.open("DefaultRes" + "/" + resFileName), resFile);
        }
        return tmpDirectory;
    }

    private static JSONObject testFile(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.isDirectory())
                continue;
            if (zipEntry.getName().equals("GameConfig.json")) {
                try {
                    JSONObject config = new JSONObject(readTextFile(zipFile.getInputStream(zipEntry)));
                    if ("Card Match".equals(config.getString("for")) && config.has("Mod_Name") && config.has("Author") && config.has("Version") && config.has("FrontImageName") && config.has("BackImageName")) {
                        return config;
                    } else
                        return null;
                } catch (JSONException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static String readTextFile(InputStream input) throws IOException {
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

    private static void writeFile(InputStream input, File outFile) throws IOException {
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

    private static void deleteFile(File file) {
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

    private static final class SQL {
        private static final String ADD_MOD = "INSERT INTO Resources (uuid,Name,Author,Show,Version,ResPath,Cover,FrontRes,BackRes) VALUES (?,?,?,?,?,?,?,?,?)";
        private static final String SELECT_ALL_MOD = "SELECT uuid,Name,Author,Show,Version,ResPath,Cover,BackRes FROM Resources ORDER BY Weight DESC";
        private static final String DELETE_MOD = "DELETE FROM Resources WHERE UUID = ?";
        private static final String FIND_MOD_VERSION = "SELECT Version FROM Resources WHERE uuid = ?";
        private static final String INIT_GAME = "SELECT Name,Author,Show,Version,ResPath,FrontRes,BackRes FROM Resources WHERE uuid = ?";
    }

    private static class GameObserverAdaptor extends BaseGameObserver {

        private final Bitmap mFrontRes;
        private final Bitmap[] mBackRes;

        public GameObserverAdaptor(Context context, Handler handler, Bitmap mFrontRes, Bitmap[] mBackRes) {
            super(context, handler);
            this.mFrontRes = mFrontRes;
            this.mBackRes = mBackRes;
        }

        @Override
        protected int[] setData() {
            return super.getRandomResourcesIndex(mBackRes.length);
        }

        @Override
        protected View[][] makeGameView(int[] gameData) {
            View[][] views = new View[16][2];
            for (int i = 0; i < 16; i++) {
                views[i][0] = getImageView(mFrontRes);
                views[i][1] = getImageView(mBackRes[gameData[i]]);
            }
            return views;
        }

        private View getImageView(Bitmap res) {
            ImageView imageView = new ImageView(super.getContext());
            imageView.setImageBitmap(res);
            return imageView;
        }
    }
}
