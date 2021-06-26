package top.someones.cardmatch.entity

import android.graphics.Bitmap

data class Mod(
    val uuid: String,
    val name: String,
    val cover: Bitmap?,
    val author: String,
    val version: Double,
    val show: String?
)