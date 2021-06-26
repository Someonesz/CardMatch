package top.someones.cardmatch.service

import android.content.Context
import top.someones.cardmatch.entity.User
import top.someones.cardmatch.entity.NetEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import top.someones.cardmatch.service.request.UserWebRequest
import top.someones.cardmatch.util.NetUtil

object UserService {
    private val service: UserWebRequest = NetUtil.retrofit.create(UserWebRequest::class.java)

    fun login(
        context: Context,
        user: String,
        pass: String,
        onSuccess: (it: User) -> Unit
    ) {
        loginOrRegister(service.login(user, pass), context, onSuccess)
    }

    fun register(
        context: Context,
        user: String,
        pass: String,
        onSuccess: (it: User) -> Unit
    ) {
        loginOrRegister(service.register(user, pass), context, onSuccess)
    }

    private fun loginOrRegister(
        ob: Observable<NetEntity<User>>,
        context: Context,
        onSuccess: (it: User) -> Unit
    ) {
        ob.subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(NetUtil.HttpResultFunction())
            .subscribe(DataObserver(context, "正在连接到服务器", onSuccess))
    }


}