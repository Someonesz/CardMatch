package top.someones.cardmatch.ui.splash

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SplashPageAdapter(
    fragmentActivity: FragmentActivity,
    private val mFragments: List<Fragment>
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getItemCount(): Int {
        return mFragments.size
    }
}