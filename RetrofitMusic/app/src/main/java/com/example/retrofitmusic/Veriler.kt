package com.example.retrofitmusic

import com.google.gson.annotations.SerializedName

data class Veriler
(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("cover_medium")
    val cover_medium: String = "",
    @SerializedName("preview")
    val preview: String = ""
)
