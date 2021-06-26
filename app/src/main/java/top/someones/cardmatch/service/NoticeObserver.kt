package top.someones.cardmatch.service

import android.app.ProgressDialog
import android.content.Context
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import top.someones.cardmatch.entity.NoticeEntity
import java.net.ConnectException
import java.net.SocketTimeoutException

class NoticeObserver(
    private val context: Context,
    private val text: String?,
    private val onSuccess: (it: NoticeEntity) -> Unit = { },
    private val onFail: (it: String) -> Unit = { },
) : Observer<NoticeEntity> {

    private var mProgressDialog: ProgressDialog? = null

    override fun onSubscribe(d: Disposable?) {
        text?.let {
            mProgressDialog = ProgressDialog.show(context, "请稍后", it, true, true) {
                if (!d!!.isDisposed)
                    d.dispose()
            }
        }
    }

    override fun onComplete() {
        mProgressDialog?.dismiss()
    }

    override fun onError(e: Throwable) {
        var msg = "错误:数据异常${e.message}"
        when (e) {
            is SocketTimeoutException -> {
                msg = "网络异常，请检查您的网络状态"
            }
            is ConnectException -> {
                msg = "网络异常，请检查您的网络状态"
            }
            is ApiException -> {
                msg = "${e.message}"
            }
        }
        mProgressDialog?.dismiss()
        onFail.invoke(msg)
    }

    override fun onNext(t: NoticeEntity) {
        if (t.flag != 0)
            onFail.invoke(t.msg ?: "未知错误")
        else
            onSuccess.invoke(t)
    }

}