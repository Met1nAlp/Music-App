package com.example.retrofitmusic

import com.google.gson.annotations.SerializedName

data class DeezerResponse
(
    @SerializedName("data")
    val data: List<Veriler>
)

data class AlbumResponse
(
    @SerializedName("cover_medium")
    val cover_medium: String,
    @SerializedName("id")
    val id : Int

)