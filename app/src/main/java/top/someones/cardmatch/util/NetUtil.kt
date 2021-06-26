package top.someones.cardmatch.util

import io.reactivex.rxjava3.functions.Function
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import top.someones.cardmatch.entity.NetEntity
import top.someones.cardmatch.service.ApiException

object NetUtil {
    const val MAIN = "https://cardmatch.someones.cn/api/"
    val okHttpClient = OkHttpClient()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(MAIN)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()


    class HttpResultFunction<T> : Function<NetEntity<T>, T> {
        override fun apply(t: NetEntity<T>?): T {
            if (t == null) {
                throw ApiException("服务器响应错误")
            }
            if (t.flag != 0) {
                throw ApiException(t.msg)
            }
            return t.data!!
        }
    }
}