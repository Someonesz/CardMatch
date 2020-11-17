package top.someones.cardmatch.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import top.someones.cardmatch.R;
import top.someones.cardmatch.entity.Mod;

/**
 * 信息管理
 *
 * <p> 更新信息
 * ver1.5:使用数据库管理信息
 * ver1.6:添加了两个外部包(commons-io,commons-compress)用于处理文件
 *
 * @author Someones
 * @version 1.6
 */
public class GameManagement {

    private static boolean mInitialized = false;
    private static Bitmap mDefaultBitmap;
    private static final Random mRandom = new Random();

    private GameManagement() {
    }

    private static SQLiteOpenHelper getSQLiteOpenHelper(Context context) {
        return new DatabaseHelper(context);
    }

    public static void installMod(Context context, File modFile) throws Exception {
        ZipFile zipFile = new ZipFile(modFile);
        JSONObject config = testFile(zipFile);
        if (config == null)
            throw new Exception("读取配置文件失败");

        String uuid = config.getString("uuid");
        String name = config.getString("name");
        String author = config.getString("author");
        String version = config.getString("version");
        String frontImageName = config.getString("frontImageName");
        JSONArray backImageName = config.getJSONArray("backImageName");
        String show = null;
        if (config.has("show"))
            show = config.getString("show");
        String cover = null;
        if (config.has("cover"))
            cover = config.getString("cover");
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
                FileUtils.deleteQuietly(modDirectory);
            }
            if (!modDirectory.mkdirs())
                throw new Exception("Mod目录创建失败");
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            ZipArchiveEntry zipEntry;
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                FileUtils.copyToFile(zipFile.getInputStream(zipEntry), new File(part, zipEntry.getName()));
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
            FileUtils.deleteQuietly(new File(part));
            throw e;
        }
    }

    public static double getModVersion(Context context, String uuid) {
        try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(SQL.FIND_MOD_VERSION, new String[]{uuid})) {
                if (cursor.moveToNext()) {
                    return cursor.getDouble(cursor.getColumnIndex("version"));
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
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String author = cursor.getString(cursor.getColumnIndex("author"));
                    String show = cursor.getString(cursor.getColumnIndex("show"));
                    Double version = cursor.getDouble(cursor.getColumnIndex("version"));
                    String resPath = cursor.getString(cursor.getColumnIndex("resPath"));
                    Bitmap bitmap = ImageCache.getCache(uuid);
                    if (bitmap == null) {
                        String cover = cursor.getString(cursor.getColumnIndex("cover"));
                        if (cover != null) {
                            bitmap = BitmapFactory.decodeFile(resPath + "/" + cover);
                        }
                        if (bitmap == null) {
                            String backRes = cursor.getString(cursor.getColumnIndex("backRes"));
                            String[] subString = backRes.split(":");
                            bitmap = BitmapFactory.decodeFile(resPath + "/" + subString[mRandom.nextInt(subString.length)]);
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

    public static GameObserver getGameObserver(String uuid, Context context, GameCallback callback) {
        String resPath;
        String frontResName;
        String[] backResName;
        try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
            SQLiteDatabase db = helper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(SQL.INIT_GAME, new String[]{uuid})) {
                if (cursor.moveToNext()) {
                    resPath = cursor.getString(cursor.getColumnIndex("resPath"));
                    frontResName = cursor.getString(cursor.getColumnIndex("frontRes"));
                    backResName = cursor.getString(cursor.getColumnIndex("backRes")).split(":");
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
            return new GameObserverAdaptor(context, callback, frontRes, backRes);
        return null;
    }

    public static boolean deleteMod(Context context, String uuid) {
        File modDirectory = context.getFileStreamPath("mod");
        if (FileUtils.deleteQuietly(new File(modDirectory.getPath(), uuid))) {
            try (SQLiteOpenHelper helper = getSQLiteOpenHelper(context)) {
                helper.getReadableDatabase().execSQL(SQL.DELETE_MOD, new Object[]{uuid});
            }
            return true;
        } else
            return false;
    }

    private static void init(Context context) throws Exception {
        mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
        File modDirectory = context.getFileStreamPath("mod");
        File[] files = modDirectory.listFiles();
        if (files == null || files.length == 0) {
            if (modDirectory.exists())
                FileUtils.deleteDirectory(modDirectory);
            File[] tmpDirectory = copyDefaultRes(context).listFiles();
            if (tmpDirectory == null) {
                throw new Exception("初始化失败");
            }
            for (File modFile : tmpDirectory) {
                installMod(context, modFile);
            }
        }
        mInitialized = true;
    }

    private static File copyDefaultRes(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] resFilesName = assetManager.list("DefaultRes");
        if (resFilesName == null) {
            throw new IOException("默认资源读取失败");
        }
        File tmpDirectory = context.getFileStreamPath("tmp");
        for (String resFileName : resFilesName) {
            InputStream in = assetManager.open("DefaultRes" + "/" + resFileName);
            File resFile = new File(tmpDirectory, resFileName);
            FileUtils.copyToFile(in, resFile);
            IOUtils.close(in);
        }
        return tmpDirectory;
    }

    private static JSONObject testFile(ZipFile zipFile) throws IOException {
        ZipArchiveEntry zipEntry = zipFile.getEntry("GameConfig.json");
        if (zipEntry == null) {
            return null;
        }
        try {
            JSONObject config = new JSONObject(readTextFile(zipFile.getInputStream(zipEntry)));
            if ("Card Match".equals(config.getString("for")) && config.has("name") && config.has("author") && config.has("version") && config.has("frontImageName") && config.has("backImageName")) {
                return config;
            } else
                return null;
        } catch (JSONException e) {
            return null;
        }
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

    private static final class SQL {
        private static final String ADD_MOD = "INSERT INTO Resources (uuid,name,author,show,version,resPath,cover,frontRes,backRes) VALUES (?,?,?,?,?,?,?,?,?)";
        private static final String SELECT_ALL_MOD = "SELECT uuid,name,author,show,version,resPath,cover,backRes FROM Resources ORDER BY Weight DESC";
        private static final String DELETE_MOD = "DELETE FROM Resources WHERE UUID = ?";
        private static final String FIND_MOD_VERSION = "SELECT version FROM Resources WHERE uuid = ?";
        private static final String INIT_GAME = "SELECT name,author,show,version,resPath,frontRes,backRes FROM Resources WHERE uuid = ?";
    }

    private static class GameObserverAdaptor extends BaseGameObserver {

        private final Bitmap mFrontRes;
        private final Bitmap[] mBackRes;

        public GameObserverAdaptor(Context context, GameCallback callback, Bitmap mFrontRes, Bitmap[] mBackRes) {
            super(context, callback);
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
