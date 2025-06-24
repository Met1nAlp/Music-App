// Veriler.kt
package com.example.retrofitmusic

import com.google.gson.annotations.SerializedName
import android.os.Parcelable // Parcelable import edin
import kotlinx.parcelize.Parcelize // @Parcelize import edin

@Parcelize
data class Veriler
    (
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("preview")
    val preview: String = "",
    @SerializedName("artist")
    val artist: Artist ,
    @SerializedName("album")
    val album: Album ,

    var isFavorite: Boolean = false
) : Parcelable

@Parcelize
data class Artist
    (
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "" ,

    @SerializedName("cover_medium")
    val cover_medium: String? = null
) : Parcelable

@Parcelize
data class Album
    (
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("cover_medium")
    val cover_medium: String? = null
) : Parcelable