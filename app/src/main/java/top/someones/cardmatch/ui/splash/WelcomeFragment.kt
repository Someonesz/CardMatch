package top.someones.cardmatch.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.someones.cardmatch.R
import top.someones.cardmatch.databinding.FragmentWelcomeBinding

/**
 * 欢迎页面
 */
class WelcomeFragment : Fragment() {
    private var mImageResourceId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mImageResourceId = requireArguments().getInt(PARAM_1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 膨胀此Fragment的布局
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 视图创建完成，绑定事件
        val viewBinding = FragmentWelcomeBinding.bind(view)
        viewBinding.welcomeImage.setImageResource(mImageResourceId)
    }

    companion object {
        private const val PARAM_1 = "ImageResourceId"

        /**
         * 使用此工厂方法提供的参数创建此片段的新实例。
         *
         * @param imageResourceId 图片资源ID。
         * @return 片段WelcomeFragment的新实例。
         */
        fun newInstance(imageResourceId: Int): WelcomeFragment {
            val fragment = WelcomeFragment()
            val args = Bundle()
            args.putInt(PARAM_1, imageResourceId)
            fragment.arguments = args
            return fragment
        }
    }
}