package top.someones.cardmatch.ui.workshop

import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import okhttp3.*
import org.apache.commons.io.FileUtils
import top.someones.cardmatch.core.*
import top.someones.cardmatch.databinding.ActivityModInfoBinding
import top.someones.cardmatch.service.WorkShopService
import top.someones.cardmatch.ui.BaseActivity
import top.someones.cardmatch.ui.ModLiveData
import top.someones.cardmatch.util.NetUtil
import java.io.File
import java.io.IOException

class ModInfoActivity : BaseActivity() {
    private var uuid: String? = null
    private var mLiveData: ModLiveData? = null
    private lateinit var mViewBinding: ActivityModInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityModInfoBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        uuid = intent.getStringExtra("uuid")
        if (uuid == null) {
            Toast.makeText(this, "错误：UUID为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        WorkShopService.getModInfo(this, uuid!!) {
            mViewBinding.modName.text = it.name
            mViewBinding.modAuthor.text = it.author
            mViewBinding.modVersion.text = it.version.toString()
            mViewBinding.modShow.text = it.show
            Glide.with(this)
                .load("${NetUtil.MAIN}mod/${it.uuid}/img")
                .into(mViewBinding.modCover)
            val ver = GameManagement.getModVersion(this@ModInfoActivity, uuid)
            if (ver > 0) {
                if (ver < it.version) {
                    mViewBinding.take.text = "更新"
                } else {
                    mViewBinding.take.text = "取消订阅"
                }
            }
        }


        mLiveData = ModLiveData.liveData
        mViewBinding.take.setOnClickListener {
            mViewBinding.take.isEnabled = false
            if ("订阅".contentEquals(mViewBinding.take.text) || "更新".contentEquals(mViewBinding.take.text)) {
                Toast.makeText(this, "正在下载", Toast.LENGTH_SHORT).show()
                Thread {
                    try {
                        val response = NetUtil.okHttpClient.newCall(
                            Request.Builder().get().url("${NetUtil.MAIN}mod/${uuid}/zip").build()
                        ).execute()
                        val tmpFile = File(getFileStreamPath("tmp"), uuid!!)
                        FileUtils.copyToFile(response.body!!.byteStream(), tmpFile)
                        GameManagement.installMod(this, tmpFile)
                        mLiveData!!.postValue(GameManagement.getMods(this))
                        runOnUiThread {
                            Toast.makeText(this, "任务完成", Toast.LENGTH_SHORT).show()
                            mViewBinding.take.text = "取消订阅"
                            mViewBinding.take.isEnabled = true
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "下载失败：" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "MOD安装失败：" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            } else if ("取消订阅".contentEquals(mViewBinding.take.text)) {
                if (GameManagement.deleteMod(this@ModInfoActivity, uuid!!)) {
                    mViewBinding.take.text = "订阅"
                    try {
                        mLiveData!!.postValue(GameManagement.getMods(this))
                    } catch (e: Exception) {
                        Toast.makeText(this, "数据更新失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                }
                mViewBinding.take.isEnabled = true
            }
        }
    }
}
