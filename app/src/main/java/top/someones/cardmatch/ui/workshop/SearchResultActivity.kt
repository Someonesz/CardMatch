package top.someones.cardmatch.ui.workshop

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.*
import top.someones.cardmatch.core.*
import top.someones.cardmatch.databinding.ActivitySearchResultBinding
import top.someones.cardmatch.service.WorkShopService
import top.someones.cardmatch.ui.BaseActivity
import java.util.*

class SearchResultActivity : BaseActivity() {
    private var mInputMethodManager: InputMethodManager? = null
    private var mViewBinding: ActivitySearchResultBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivitySearchResultBinding.inflate(
            layoutInflater
        )
        setContentView(mViewBinding!!.root)

        // 获取参数
        val keyWord = intent.getStringExtra("keyword")
        if (keyWord == null) {
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // 准备列表
        mViewBinding!!.searchResultList.layoutManager = LinearLayoutManager(this)

        // 异步请求数据
        searchByKeyWord(keyWord)
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mViewBinding!!.searchKeyWord.setText(keyWord)
        // 绑定事件
        mViewBinding!!.actionBack.setOnClickListener { finish() }
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
        // 去除搜索框的焦点
        mViewBinding!!.searchBox.requestFocus()
        WorkShopService.search(this, keyWord) {
            mViewBinding!!.searchResultList.adapter = ModAdapter(it) { mod ->
                startActivity(
                    Intent().putExtra(
                        "uuid",
                        mod.uuid
                    )
                )
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        // 去除搜索框的焦点
        mViewBinding!!.searchBox.requestFocus()
    }

}