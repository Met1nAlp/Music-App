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



data class Playlist(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("cover_medium")
    val cover_medium: String,
    @SerializedName("picture_medium")
    val picture_medium: String,
    @SerializedName("tracks")
    val tracks: DeezerResponse
)