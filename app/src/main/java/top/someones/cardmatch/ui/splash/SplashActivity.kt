package top.someones.cardmatch.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import top.someones.cardmatch.R
import top.someones.cardmatch.databinding.ActivitySplashBinding
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.ui.BaseActivity
import top.someones.cardmatch.ui.main.MainActivity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivitySplashBinding.inflate(
            layoutInflater
        )
        setContentView(viewBinding.root)
        statusBarTextDark()
        val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
        UserData.uid = sharedPreferences.getInt("uid", 0)
        UserData.username = sharedPreferences.getString("username", "")
        UserData.session = sharedPreferences.getString("session", "")
        if (UserData.uid > 0 && UserData.username != "" && UserData.session != "") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        } else {
            val fragments: List<Fragment> = listOf(
                WelcomeFragment.newInstance(R.mipmap.page1),
                WelcomeFragment.newInstance(R.mipmap.page2),
                WelcomeFragment.newInstance(R.mipmap.page3),
                WelcomeFragment.newInstance(R.mipmap.page4),
                UserFragment()
            )
            viewBinding.welcomePager.adapter = SplashPageAdapter(this, fragments)
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            viewBinding.navigationList.layoutManager = layoutManager
            val adapter = NavigationAdapter(ImageResource.size + 1)
            viewBinding.navigationList.adapter = adapter
            viewBinding.welcomePager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    adapter.setSelect(position)
                    if (position > 3)
                        viewBinding.navigationList.visibility = View.GONE
                    else
                        viewBinding.navigationList.visibility = View.VISIBLE
                }
            })
        }
    }

    companion object {
        private val ImageResource =
            intArrayOf(R.mipmap.page1, R.mipmap.page2, R.mipmap.page3, R.mipmap.page4)
    }
}