package top.someones.cardmatch.core

interface GameCallback {
    fun onSuccess(token: String, arg1: Int, arg2: Int)
    fun onFailure(token: String, arg1: Int, arg2: Int)
    fun onWin(token: String)
}