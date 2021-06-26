package top.someones.cardmatch.service

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import top.someones.cardmatch.entity.GameScore
import top.someones.cardmatch.entity.NoticeEntity
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.service.request.GameWebRequest
import top.someones.cardmatch.util.NetUtil

object GameService {
    private val service: GameWebRequest = NetUtil.retrofit.create(GameWebRequest::class.java)

    fun getScore(
        context: Context,
        gameUUID: String,
        onSuccess: (it: List<GameScore>) -> Unit
    ) {
        service.getScore(gameUUID).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(NetUtil.HttpResultFunction())
            .subscribe(DataObserver(context, null, onSuccess))
    }

    fun addScore(
        context: Context,
        uuid: String,
        score: Int,
        onSuccess: (it: NoticeEntity) -> Unit,
        onFail: (it: String) -> Unit
    ) {
        service.addScore(UserData.uid, UserData.session!!, uuid, score)
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(NoticeObserver(context, null, onSuccess, onFail))
    }

}