package top.someones.cardmatch.core

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.json.JSONException
import org.json.JSONObject
import top.someones.cardmatch.R
import top.someones.cardmatch.entity.Mod
import java.io.*
import java.util.*

/**
 * 信息管理
 *
 *
 *  更新信息
 * ver1.5:使用数据库管理信息
 * ver1.6:添加了两个外部包(commons-io,commons-compress)用于处理文件
 *
 * @author Someones
 * @version 1.6
 */
object GameManagement {
    private var mInitialized = false
    private lateinit var mDefaultBitmap: Bitmap
    private val mRandom = Random()
    private fun getSQLiteOpenHelper(context: Context): SQLiteOpenHelper {
        return DatabaseHelper(context)
    }

    @Throws(Exception::class)
    fun installMod(context: Context, modFile: File?) {
        val zipFile = ZipFile(modFile)
        val config = testFile(zipFile) ?: throw Exception("读取配置文件失败")
        val uuid = config.getString("uuid")
        val name = config.getString("name")
        val author = config.getString("author")
        val version = config.getString("version")
        val frontImageName = config.getString("frontImageName")
        val backImageName = config.getJSONArray("backImageName")
        var show: String? = null
        if (config.has("show")) show = config.getString("show")
        var cover: String? = null
        if (config.has("cover")) cover = config.getString("cover")
        if (backImageName.length() < GameObserver.MAX_VIEW / 2) throw Exception("Mod图片太少")
        val backImageString = StringBuilder()
        for (i in 0 until backImageName.length()) {
            backImageString.append(backImageName.getString(i))
            backImageString.append(":")
        }
        backImageString.deleteCharAt(backImageString.length - 1)
        val part = context.getFileStreamPath("mod").path + "/" + uuid
        try {
            val modDirectory = File(part)
            if (modDirectory.exists()) {
                FileUtils.deleteQuietly(modDirectory)
            }
            if (!modDirectory.mkdirs()) throw Exception("Mod目录创建失败")
            val entries = zipFile.entries
            var zipEntry: ZipArchiveEntry
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement()
                if (zipEntry.isDirectory) continue
                FileUtils.copyToFile(zipFile.getInputStream(zipEntry), File(part, zipEntry.name))
            }
            zipFile.close()
            try {
                val db = getSQLiteOpenHelper(context).writableDatabase
                db.execSQL(SQL.DELETE_MOD, arrayOf<Any>(uuid))
                db.execSQL(
                    SQL.ADD_MOD,
                    arrayOf<Any?>(
                        uuid,
                        name,
                        author,
                        show,
                        version,
                        part,
                        cover,
                        frontImageName,
                        backImageString.toString()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("数据库出错：" + e.message)
            }
        } catch (e: Exception) {
            FileUtils.deleteQuietly(File(part))
            throw e
        }
        FileUtils.deleteQuietly(modFile)
    }

    fun getModVersion(context: Context, uuid: String?): Double {
        val db = getSQLiteOpenHelper(context).readableDatabase
        db.rawQuery(SQL.FIND_MOD_VERSION, arrayOf(uuid)).use { cursor ->
            return if (cursor.moveToNext()) {
                cursor.getDouble(cursor.getColumnIndex("version"))
            } else {
                (-1).toDouble()
            }
        }
    }

    @Throws(Exception::class)
    fun getMods(context: Context): Array<Mod> {
        if (!mInitialized) init(context)
        var mods: Array<Mod>
        val db = getSQLiteOpenHelper(context).readableDatabase
        db.rawQuery(SQL.SELECT_ALL_MOD, arrayOfNulls(0)).use { cursor ->
            val list: MutableList<Mod> = LinkedList()
            while (cursor.moveToNext()) {
                val uuid = cursor.getString(cursor.getColumnIndex("uuid"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val author = cursor.getString(cursor.getColumnIndex("author"))
                val show = cursor.getString(cursor.getColumnIndex("show"))
                val version = cursor.getDouble(cursor.getColumnIndex("version"))
                val resPath = cursor.getString(cursor.getColumnIndex("resPath"))
                var bitmap = ImageCache.getCache(uuid)
                if (bitmap == null) {
                    val cover = cursor.getString(cursor.getColumnIndex("cover"))
                    if (cover != null) {
                        bitmap = BitmapFactory.decodeFile("$resPath/$cover")
                    }
                    if (bitmap == null) {
                        val backRes = cursor.getString(cursor.getColumnIndex("backRes"))
                        val subString = backRes.split(":").toTypedArray()
                        bitmap = BitmapFactory.decodeFile(
                            resPath + "/" + subString[mRandom.nextInt(subString.size)]
                        )
                    }
                    if (bitmap == null) {
                        bitmap = mDefaultBitmap
                    }
                    ImageCache.addBaseCache(uuid, bitmap)
                }
                list.add(Mod(uuid, name, bitmap, author, version, show))
            }
            mods = list.toTypedArray()
        }
        return mods
    }

    fun getGameObserver(uuid: String, context: Context, callback: GameCallback): GameObserver? {
        var resPath: String
        var frontResName: String
        var backResName: Array<String>

        getSQLiteOpenHelper(context).readableDatabase.use {
            it.rawQuery(SQL.INIT_GAME, arrayOf(uuid)).use { cursor ->
                if (cursor.moveToNext()) {
                    resPath = cursor.getString(cursor.getColumnIndex("resPath"))
                    frontResName = cursor.getString(cursor.getColumnIndex("frontRes"))
                    backResName =
                        cursor.getString(cursor.getColumnIndex("backRes")).split(":").toTypedArray()
                } else
                    return null
            }
        }
        val frontRes: Bitmap
        val backRes: Array<Bitmap>
        try {
            frontRes = BitmapFactory.decodeStream(FileInputStream(File(resPath, frontResName)))
            val bitmaps: MutableList<Bitmap> = LinkedList()
            for (fileName in backResName) {
                val resFile = File(resPath, fileName)
                if (resFile.isFile) {
                    val bitmap = BitmapFactory.decodeFile("$resPath/$fileName")
                    if (bitmap != null) {
                        bitmaps.add(bitmap)
                    }
                }
            }
            if (bitmaps.size < GameObserver.MAX_VIEW / 2) return null
            backRes = bitmaps.toTypedArray()
        } catch (e: FileNotFoundException) {
            return null
        }
        return frontRes?.let { GameObserverAdaptor(context, callback, it, backRes) }
    }

    fun deleteMod(context: Context, uuid: String): Boolean {
        val modDirectory = context.getFileStreamPath("mod")
        return if (FileUtils.deleteQuietly(
                File(
                    modDirectory.path,
                    uuid
                )
            )
        ) {
            getSQLiteOpenHelper(context).readableDatabase.use {
                it.execSQL(
                    SQL.DELETE_MOD,
                    arrayOf<Any>(uuid)
                )
                true
            }
        } else false
    }

    @Throws(Exception::class)
    private fun init(context: Context) {
        mDefaultBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.error)
        val modDirectory = context.getFileStreamPath("mod")
        val files = modDirectory.listFiles()
        if (files == null || files.isEmpty()) {
            if (modDirectory.exists()) FileUtils.deleteDirectory(modDirectory)
            val tmpDirectory = copyDefaultRes(context).listFiles()
                ?: throw Exception("初始化失败")
            for (modFile in tmpDirectory) {
                installMod(context, modFile)
            }
        }
        mInitialized = true
    }

    @Throws(IOException::class)
    private fun copyDefaultRes(context: Context): File {
        val assetManager = context.assets
        val resFilesName = assetManager.list("DefaultRes") ?: throw IOException("默认资源读取失败")
        val tmpDirectory = context.getFileStreamPath("tmp")
        for (resFileName in resFilesName) {
            val `in` = assetManager.open("DefaultRes/$resFileName")
            val resFile = File(tmpDirectory, resFileName)
            FileUtils.copyToFile(`in`, resFile)
            IOUtils.close(`in`)
        }
        return tmpDirectory
    }

    @Throws(IOException::class)
    private fun testFile(zipFile: ZipFile): JSONObject? {
        val zipEntry = zipFile.getEntry("GameConfig.json") ?: return null
        return try {
            val config = JSONObject(readTextFile(zipFile.getInputStream(zipEntry)))
            if ("Card Match" == config.getString("for") && config.has("name") && config.has("author") && config.has(
                    "version"
                ) && config.has("frontImageName") && config.has("backImageName")
            ) {
                config
            } else null
        } catch (e: JSONException) {
            null
        }
    }

    @Throws(IOException::class)
    private fun readTextFile(input: InputStream): String {
        val sb = StringBuilder()
        BufferedReader(InputStreamReader(input)).use { br ->
            var str = br.readLine()
            while (str != null) {
                sb.append(str)
                str = br.readLine()
            }
            input.close()
        }
        return sb.toString()
    }

    private object SQL {
        const val ADD_MOD =
            "INSERT INTO Resources (uuid,name,author,show,version,resPath,cover,frontRes,backRes) VALUES (?,?,?,?,?,?,?,?,?)"
        const val SELECT_ALL_MOD =
            "SELECT uuid,name,author,show,version,resPath,cover,backRes FROM Resources ORDER BY Weight DESC"
        const val DELETE_MOD = "DELETE FROM Resources WHERE UUID = ?"
        const val FIND_MOD_VERSION = "SELECT version FROM Resources WHERE uuid = ?"
        const val INIT_GAME =
            "SELECT name,author,show,version,resPath,frontRes,backRes FROM Resources WHERE uuid = ?"
    }
}
