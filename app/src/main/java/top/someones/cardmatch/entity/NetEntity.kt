package top.someones.cardmatch.entity

data class NetEntity<T>(
    var flag: Int,
    var msg: String,
    var data: T? = null
)

data class NoticeEntity(
    var flag: Int = 0,
    var msg: String? = null
)
