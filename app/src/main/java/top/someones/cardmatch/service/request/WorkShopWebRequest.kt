package top.someones.cardmatch.service.request

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*
import top.someones.cardmatch.entity.GameScore
import top.someones.cardmatch.entity.NetEntity
import top.someones.cardmatch.entity.NoticeEntity
import top.someones.cardmatch.entity.WorkShopMod

interface WorkShopWebRequest {

    @GET("mod/hot")
    fun getHot(): Observable<NetEntity<List<WorkShopMod>>>

    @GET("mod/{uuid}")
    fun getModInfo(
        @Path("uuid") uuid: String
    ): Observable<NetEntity<WorkShopMod>>

    @POST("mod/search")
    @FormUrlEncoded
    fun search(
        @Field("keyword") keyword: String
    ): Observable<NetEntity<List<WorkShopMod>>>

}