package top.someones.cardmatch.core

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView

class GameObserverAdaptor(
    context: Context,
    callback: GameCallback,
    private val mFrontRes: Bitmap,
    private val mBackRes: Array<Bitmap>
) : BaseGameObserver(context, callback) {
    override fun setData(): IntArray {
        return super.getRandomResourcesIndex(mBackRes.size)
    }

    override fun makeGameView(gameData: IntArray): Array<Array<View?>> {
        val views = Array(16) { arrayOfNulls<View>(2) }
        for (i in 0..15) {
            views[i][0] = getImageView(mFrontRes)
            views[i][1] = getImageView(mBackRes[gameData[i]])
        }
        return views
    }

    private fun getImageView(res: Bitmap?): View {
        val imageView = ImageView(super.context)
        imageView.setImageBitmap(res)
        return imageView
    }
}