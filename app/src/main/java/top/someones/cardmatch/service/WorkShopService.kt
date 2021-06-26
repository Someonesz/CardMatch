package top.someones.cardmatch.service

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import top.someones.cardmatch.entity.WorkShopMod
import top.someones.cardmatch.service.request.WorkShopWebRequest
import top.someones.cardmatch.util.NetUtil


object WorkShopService {

    private val service: WorkShopWebRequest =
        NetUtil.retrofit.create(WorkShopWebRequest::class.java)

    fun getHot(
        context: Context,
        onSuccess: (it: List<WorkShopMod>) -> Unit
    ) {
        service.getHot().unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(NetUtil.HttpResultFunction())
            .subscribe(DataObserver(context, "正在连接到创意工坊", onSuccess))
    }

    fun search(
        context: Context,
        keyword: String,
        onSuccess: (it: List<WorkShopMod>) -> Unit
    ) {
        service.search(keyword).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(NetUtil.HttpResultFunction())
            .subscribe(DataObserver(context, "正在查找", onSuccess))
    }

    fun getModInfo(
        context: Context,
        uuid: String,
        onSuccess: (it: WorkShopMod) -> Unit
    ) {
        service.getModInfo(uuid).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(NetUtil.HttpResultFunction())
            .subscribe(DataObserver(context, "获取数据中...", onSuccess))
    }

}