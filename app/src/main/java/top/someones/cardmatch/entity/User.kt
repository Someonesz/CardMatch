package top.someones.cardmatch.entity

data class User(
    val uid: Int,
    val username: String,
    var session: String?
)