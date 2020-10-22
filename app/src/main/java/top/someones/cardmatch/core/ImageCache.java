package top.someones.cardmatch.core;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Bitmap> mBaseCache = new HashMap<>();
    private static final Map<String, Bitmap> mWorkshopCache = new HashMap<>();

    public Bitmap getCache(String uuid) {
        Bitmap bitmap = mBaseCache.get(uuid);
        if (bitmap != null)
            return bitmap;
        synchronized (mWorkshopCache) {
            return mWorkshopCache.get(uuid);
        }
    }

    public void setBaseCache(String uuid, Bitmap bitmap) {
        synchronized (mBaseCache) {
            mBaseCache.put(uuid, bitmap);
        }
    }

    public void setWorkshopCache(String uuid, Bitmap bitmap) {
        synchronized (mWorkshopCache) {
            mWorkshopCache.put(uuid, bitmap);
        }
    }

    public void cleanWorkshopCache() {
        synchronized (mWorkshopCache) {
            mWorkshopCache.clear();
        }
    }
}
