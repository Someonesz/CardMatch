package top.someones.cardmatch.ui.main

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import top.someones.cardmatch.R
import top.someones.cardmatch.core.GameManagement
import top.someones.cardmatch.databinding.ActivityMainBinding
import top.someones.cardmatch.databinding.MainListLayoutBinding
import top.someones.cardmatch.entity.Mod
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.ui.BaseActivity
import top.someones.cardmatch.ui.ModLiveData
import top.someones.cardmatch.ui.PermissionsManagement
import top.someones.cardmatch.ui.game.GameActivity
import top.someones.cardmatch.ui.splash.SplashActivity
import top.someones.cardmatch.ui.workshop.WorkShopActivity
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var mIntent: Intent
    private var mLiveData: ModLiveData = ModLiveData.liveData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        mIntent = Intent(this, GameActivity::class.java)
        viewBinding.modList.layoutManager = LinearLayoutManager(this)
        mLiveData.observe(
            this,
            { mods: Array<Mod>? -> viewBinding.modList.adapter = mods?.let { ListAdapter(it) } })
        val loadingDialog: Dialog = ProgressDialog.show(this, "请稍后", "正在加载数据")
        Thread {
            try {
                val mods = GameManagement.getMods(this)
                runOnUiThread {
                    viewBinding.modList.adapter = ListAdapter(mods)
                    loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "初始化失败", Toast.LENGTH_SHORT).show()
                }
                loadingDialog.dismiss()
            }
        }.start()
    }

    private fun selectFile() {
        if (PermissionsManagement.checkPermissions(this, PERMISSIONS)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/zip"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, REQUEST_READ_ZIP_FILE)
        } else {
            PermissionsManagement.verifyPermissions(this, PERMISSIONS, REQUEST_STORAGE_PERMISSION)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.importWorkshop -> {
                startActivity(Intent(this@MainActivity, WorkShopActivity::class.java))
            }
            R.id.import_local -> {
                selectFile()
            }
            R.id.logout -> {
                UserData.logout(this)
                this.finish()
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            var hasPermissions = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = false
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show()
                    break
                }
            }
            if (hasPermissions) selectFile()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_READ_ZIP_FILE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null && uri.path != null) {
                val path = getRealPath(uri.path)
                Thread {
                    try {
                        GameManagement.installMod(this, File(path))
                        mLiveData.postValue(GameManagement.getMods(this))
                        runOnUiThread { Toast.makeText(this, "Mod安装成功", Toast.LENGTH_SHORT).show() }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Mod安装失败：" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     *
     */
    private inner class ListAdapter(private val mModList: Array<Mod>) :
        RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                MainListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val mod = mModList[position]
            holder.binding.root.setOnClickListener {
                startActivity(
                    mIntent.putExtra(
                        "uuid",
                        mod.uuid
                    )
                )
            }
            holder.binding.modCover.setImageBitmap(mod.cover)
            holder.binding.modName.text = mod.name
            holder.binding.modAuthor.text = "作者：" + mod.author
            holder.binding.modVersion.text = "版本：" + mod.version.toString()
        }

        override fun getItemCount(): Int {
            return mModList.size
        }

        private inner class ViewHolder(val binding: MainListLayoutBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 90
        private const val REQUEST_READ_ZIP_FILE = 91
        private val PERMISSIONS = arrayOf<String?>("android.permission.READ_EXTERNAL_STORAGE")

        /**
         * 暴力解决，临时用
         *
         * @param uriPath 返回的路径
         * @return 真实文件路径
         */
        private fun getRealPath(uriPath: String?): String {
            val document = "/document/primary:"
            return if (uriPath!!.startsWith(document)) {
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + uriPath.substring(document.length)
            } else uriPath
        }
    }
}