package top.someones.cardmatch.service.request

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import top.someones.cardmatch.entity.NetEntity
import top.someones.cardmatch.entity.User

interface UserWebRequest {

    @POST("user/login")
    @FormUrlEncoded
    fun login(
        @Field("user") user: String,
        @Field("pass") pass: String
    ): Observable<NetEntity<User>>

    @POST("user/register")
    @FormUrlEncoded
    fun register(
        @Field("user") user: String,
        @Field("pass") pass: String
    ): Observable<NetEntity<User>>

}