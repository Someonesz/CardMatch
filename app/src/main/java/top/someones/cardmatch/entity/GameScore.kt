package top.someones.cardmatch.entity


import java.io.Serializable

data class GameScore(var uuid: String, var uid: Int, var username: String, var score: Int) :
    Serializable