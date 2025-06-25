package com.example.retrofitmusic

import com.google.gson.annotations.SerializedName

data class DeezerResponse
(
    @SerializedName("data")
    val data: List<Veriler>
)

data class AlbumResponse
(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("cover_medium")
    val coverMedium: String,
    @SerializedName("tracks")
    val tracks: TracksWrapper

)

data class TracksWrapper
(
    @SerializedName("data")
    val data: List<Veriler>
)