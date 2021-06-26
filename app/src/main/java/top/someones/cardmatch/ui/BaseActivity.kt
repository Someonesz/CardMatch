package top.someones.cardmatch.ui

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    protected fun immersionStatusBar(textColorReversal: Boolean = true) {
        this.window.statusBarColor = Color.TRANSPARENT
        val decorView = this.window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        var vis = decorView.systemUiVisibility
        vis = if (textColorReversal) {
            vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = vis
    }

    protected open fun statusBarTextDark(textDark: Boolean = true) {
        this.window.statusBarColor = Color.TRANSPARENT
        val decorView: View = this.window.decorView
        var vis: Int = decorView.systemUiVisibility
        vis = if (textDark) {
            vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = vis
    }
}