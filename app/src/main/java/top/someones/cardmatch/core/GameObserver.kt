package top.someones.cardmatch.core

import android.view.View

interface GameObserver {
    fun newGame(token: String): Array<Array<View?>>
    fun check(a: Int, b: Int)

    companion object {
        const val MAX_VIEW = 16
    }
}