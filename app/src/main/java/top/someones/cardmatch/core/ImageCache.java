package top.someones.cardmatch.core;

import android.graphics.Bitmap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {
    private static final Map<String, Bitmap> mBaseCache = new ConcurrentHashMap<>();
    private static final Map<String, Bitmap> mWorkshopCache = new ConcurrentHashMap<>();

    public static Bitmap getCache(String uuid) {
        Bitmap bitmap = mBaseCache.get(uuid);
        if (bitmap != null)
            return bitmap;
        return mWorkshopCache.get(uuid);
    }

    public static void addBaseCache(String uuid, Bitmap bitmap) {
        mBaseCache.put(uuid, bitmap);
    }

    public static void addWorkshopCache(String uuid, Bitmap bitmap) {
        if (uuid == null || bitmap == null)
            return;
        mWorkshopCache.put(uuid, bitmap);
    }

    public static void cleanWorkshopCache() {
        mWorkshopCache.clear();
    }
}
