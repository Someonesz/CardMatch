package top.someones.cardmatch.entity

import android.content.Context

object UserData {
    var uid: Int = 0
    var username: String? = null
    var session: String? = null

    fun addData(
        context: Context,
        uid: Int,
        username: String,
        session: String
    ) {
        val edit = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
        edit.putInt("uid", uid)
        edit.putString("username", username)
        edit.putString("session", session)
        edit.apply()
        this.uid = uid
        this.username = username
        this.session = session
    }

    fun logout(context: Context) {
        uid = 0
        username = null
        session = null
        context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
