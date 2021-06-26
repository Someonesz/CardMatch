package top.someones.cardmatch.service.request

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*
import top.someones.cardmatch.entity.GameScore
import top.someones.cardmatch.entity.NetEntity
import top.someones.cardmatch.entity.NoticeEntity

interface GameWebRequest {
    @GET("score/get/{uuid}")
    fun getScore(
        @Path("uuid") uuid: String
    ): Observable<NetEntity<List<GameScore>>>

    @POST("score/add")
    @FormUrlEncoded
    fun addScore(
        @Field("uid") uid: Int,
        @Field("session") pass: String,
        @Field("uuid") uuid: String,
        @Field("score") score: Int
    ): Observable<NoticeEntity>
}