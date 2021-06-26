package top.someones.cardmatch.core

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentHashMap

object ImageCache {
    private val mBaseCache: MutableMap<String, Bitmap> = ConcurrentHashMap()
    private val mWorkshopCache: MutableMap<String, Bitmap> = ConcurrentHashMap()
    fun getCache(uuid: String): Bitmap? {
        val bitmap = mBaseCache[uuid]
        return bitmap ?: mWorkshopCache[uuid]
    }

    fun addBaseCache(uuid: String, bitmap: Bitmap) {
        mBaseCache[uuid] = bitmap
    }

}