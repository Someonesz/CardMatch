package top.someones.cardmatch.ui.game

import android.animation.*
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import top.someones.cardmatch.R

/**
 * 翻牌子
 * 两个VIEW切换
 *
 * @author Someones
 */
class Cell : FrameLayout {
    private var mWhichChild = 0
    private lateinit var mBorder: View
    private lateinit var mCardOutAnimator: AnimatorSet
    private lateinit var mCardInAnimator: AnimatorSet
    private lateinit var mCardFadeOutAnimator: ObjectAnimator

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        mBorder = View(context)
        mBorder.setBackgroundResource(R.drawable.rounded_red_border)
        mBorder.alpha = 0.5f
        addView(mBorder, getLayoutParams(mBorder))
        mCardOutAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out) as AnimatorSet
        mCardInAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in) as AnimatorSet
        mCardFadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f)
        mCardFadeOutAnimator.duration = 500
        mCardOutAnimator.setTarget(this)
        mCardInAnimator.setTarget(this)
        mCardOutAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                this@Cell.isEnabled = false
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
            }

            override fun onAnimationEnd(animation: Animator) { // 翻转90度之后，换图
                getChildAt(mWhichChild).visibility = GONE
                mWhichChild = if (mWhichChild == 1) 2 else 1
                getChildAt(mWhichChild).visibility = VISIBLE
                if (mWhichChild == 1) {
                    hideBorder()
                }
                mCardInAnimator.start()
            }
        })
        mCardInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (mWhichChild == 1) this@Cell.isEnabled = true
            }
        })
    }

    fun setView(v1: View?, v2: View?) {
        if (mCardOutAnimator.isRunning) mCardOutAnimator.end()
        if (childCount > 1) {
            super.removeViewAt(2)
            super.removeViewAt(1)
        }
        mWhichChild = 1
        mCardFadeOutAnimator.end()
        this.isEnabled = true
        this.alpha = 1.0f
        v1!!.visibility = VISIBLE
        v2!!.visibility = GONE
        super.addView(v1, getLayoutParams(v1))
        super.addView(v2, getLayoutParams(v2))
        mBorder.visibility = GONE
    }

    private fun getLayoutParams(v: View): ViewGroup.LayoutParams {
        var lp: ViewGroup.LayoutParams? = v.layoutParams
        if (lp == null) {
            lp = LayoutParams(LayoutParams.MATCH_PARENT, -1)
        }
        return lp
    }

    fun showNext() {
        mCardOutAnimator.start()
    }

    fun fadeOut() {
        this.isEnabled = false
        mCardFadeOutAnimator.start()
    }

    fun showBorder() {
        mBorder.visibility = VISIBLE
    }

    fun hideBorder() {
        mBorder.visibility = GONE
    }
}