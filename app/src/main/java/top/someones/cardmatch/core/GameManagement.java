package top.someones.cardmatch.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameManagement {

    private static final Map<String, GameResource> mGameResources = new HashMap<>();
    private static boolean mInitialized = false;
    private static String DEFAULT_RESOURCE = "DefaultRes";

    private GameManagement() {
    }

    public static void init(Context context) {
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
                copyDefaultRes(context);
            }
            files = modDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        GameResource res = getGameRes(file);
                        if (res != null) {
                            mGameResources.put(res.getUUID(), res);
                        }
                    }
                }
                mInitialized = true;
            }
        }
    }

    public static GameObserver getGameObserver(String name, Context context, Handler handler) {
        if (!mInitialized)
            init(context);
        GameResource res = mGameResources.get(name);
        if (res == null)
            return null;
        Bitmap frontRes;
        Bitmap[] backRes;
        try {
            frontRes = BitmapFactory.decodeStream(new FileInputStream(new File(res.getResPath(), res.getFrontResName())));
            List<Bitmap> bitmaps = new LinkedList<>();
            for (String backResName : res.getBackResName()) {
                File resFile = new File(res.getResPath(), backResName);
                if (resFile.isFile()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(res.getResPath() + "/" + backResName);
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

    public static GameResource[] getAllGameRes(Context context) {
        if (!mInitialized)
            init(context);
        return mGameResources.values().toArray(new GameResource[0]);
    }

    private static GameResource getGameRes(File resDirectory) {
        try {
            String resPath = resDirectory.getPath();
            File[] files = resDirectory.listFiles();
            if (files != null) {
                for (File gameConfig : files) {
                    if (gameConfig.isFile() && gameConfig.getName().equals("GameConfig.json")) {
                        JSONObject config = new JSONObject(readTextFile(new FileInputStream(gameConfig)));
                        String gameName = config.getString("Mod_Name");
                        String uuid = config.getString("uuid");
                        String version = config.getString("version");
                        String frontImagePath = config.getString("FrontImageName");
                        JSONArray jarr = config.getJSONArray("BackImageName");
                        List<String> backImagesName = new LinkedList<>();
                        for (int i = 0; i < jarr.length(); i++) {
                            backImagesName.add(jarr.getString(i));
                        }
                        if (backImagesName.size() < GameObserver.MAX_VIEW / 2)
                            return null;
                        return new GameResource(gameName, uuid, version, resPath, frontImagePath, backImagesName.toArray(new String[0]));
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static boolean installMod(Context context, ZipFile zipFile) {
        try {
            if (!testFile(zipFile))
                return false;
            String uuid = UUID.randomUUID().toString();
            String part = context.getFileStreamPath("mod").getPath() + "/" + uuid;
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
            e.printStackTrace();
            return false;
        }
        GameManagement.init(context);
        return true;
    }

    private static void copyDefaultRes(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            String[] resDirectoriesName = assetManager.list("DefaultRes");
            File modDirectory = context.getFileStreamPath("mod");
            for (String resDirectoryName : resDirectoriesName) {
                try {
                    File resDirectory = new File(modDirectory, resDirectoryName);
                    resDirectory.mkdirs();
                    String[] resFilesName = assetManager.list("DefaultRes" + "/" + resDirectoryName);
                    for (String resFileName : resFilesName) {
                        File resFile = new File(modDirectory + "/" + resDirectoryName + "/" + resFileName);
                        resFile.createNewFile();
                        writeFile(assetManager.open("DefaultRes" + "/" + resDirectoryName + "/" + resFileName), resFile);
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean testFile(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
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
