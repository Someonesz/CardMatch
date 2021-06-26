package top.someones.cardmatch.ui.workshop

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.*
import top.someones.cardmatch.core.*
import top.someones.cardmatch.databinding.ActivityWorkshopBinding
import top.someones.cardmatch.service.WorkShopService
import top.someones.cardmatch.ui.BaseActivity
import java.util.*

class WorkShopActivity : BaseActivity() {
    private var mInputMethodManager: InputMethodManager? = null
    private var mViewBinding: ActivityWorkshopBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityWorkshopBinding.inflate(
            layoutInflater
        )
        setContentView(mViewBinding!!.root)

        // 初始化列表
        val intent = Intent(this@WorkShopActivity, ModInfoActivity::class.java)
        mViewBinding!!.modList.layoutManager = LinearLayoutManager(this)

        WorkShopService.getHot(this) {
            mViewBinding!!.modList.adapter = ModAdapter(it) { mod ->
                startActivity(
                    intent.putExtra(
                        "uuid",
                        mod.uuid
                    )
                )
            }
        }

        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mViewBinding!!.actionSearch.setOnClickListener {
            searchByKeyWord(
                mViewBinding!!.searchKeyWord.text.toString().trim { it <= ' ' })
        }
        mViewBinding!!.searchKeyWord.setOnKeyListener { _: View?, keyCode: Int, event: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_UP) {
                    searchByKeyWord(mViewBinding!!.searchKeyWord.text.toString().trim { it <= ' ' })
                }
                return@setOnKeyListener true
            }
            false
        }
    }

    /**
     * 按关键字搜索
     *
     * @param keyWord 关键字
     */
    private fun searchByKeyWord(keyWord: String) {
        // 隐藏软键盘
        if (mInputMethodManager != null) mInputMethodManager!!.hideSoftInputFromWindow(
            mViewBinding!!.searchKeyWord.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        startActivity(
            Intent(
                this@WorkShopActivity,
                SearchResultActivity::class.java
            ).putExtra("keyword", keyWord)
        )
    }

    override fun onRestart() {
        super.onRestart()
        // 去除搜索框的焦点
        mViewBinding!!.searchBox.requestFocus()
    }

}