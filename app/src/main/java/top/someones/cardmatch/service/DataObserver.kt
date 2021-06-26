package top.someones.cardmatch.service

import android.app.ProgressDialog
import android.content.Context
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.net.ConnectException
import java.net.SocketTimeoutException

class DataObserver<T>(
    private val context: Context,
    private val text: String?,
    private val onNext: (it: T) -> Unit,
    private val onError: (it: String) -> Unit = {}
) : Observer<T> {

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
        onError.invoke(msg)
    }

    override fun onNext(t: T) {
        onNext.invoke(t)
    }

}