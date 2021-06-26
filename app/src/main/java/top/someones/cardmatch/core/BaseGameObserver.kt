package top.someones.cardmatch.core

import android.content.Context
import android.view.View
import java.util.*

abstract class BaseGameObserver(
    protected val context: Context,
    private val callback: GameCallback
) : GameObserver {
    private val mData = IntArray(16)
    private var mGameProgress = 0
    private lateinit var mGameToken: String
    private fun getData(index: Int): Int {
        return mData[index]
    }

    override fun newGame(token: String): Array<Array<View?>> {
        mGameToken = token
        mGameProgress = 0
        System.arraycopy(setData()!!, 0, mData, 0, mData.size)
        return makeGameView(mData)
    }

    protected abstract fun setData(): IntArray?
    protected abstract fun makeGameView(gameData: IntArray): Array<Array<View?>>
    override fun check(a: Int, b: Int) {
        if (getData(a) == getData(b)) {
            mGameProgress++
            callback.onSuccess(mGameToken, a, b)
            if (mGameProgress == GameObserver.MAX_VIEW / 2) callback.onWin(mGameToken)
        } else callback.onFailure(mGameToken, a, b)
    }

    protected fun getRandomResourcesIndex(resLength: Int): IntArray {
        val one: MutableList<Int> = ArrayList(resLength)
        for (i in 0 until resLength) {
            one.add(i)
        }
        val two: MutableList<Int> = ArrayList<Int>(GameObserver.MAX_VIEW)
        val random = Random()
        var index: Int
        for (i in 0 until GameObserver.MAX_VIEW / 2) {
            index = random.nextInt(resLength - i)
            two.add(one[index])
            one.removeAt(index)
        }
        two.addAll(two)
        val data = IntArray(GameObserver.MAX_VIEW)
        for (i in 0 until GameObserver.MAX_VIEW) {
            index = random.nextInt(GameObserver.MAX_VIEW - i)
            data[i] = two.removeAt(index)
        }
        return data
    }
}