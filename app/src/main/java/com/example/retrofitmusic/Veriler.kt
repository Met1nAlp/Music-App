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
  //  @SerializedName("playlist")
   // val playlist: PlayList? = null,

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
/*
@Parcelize
data class PlayList(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("picture_medium") val pictureMedium: String? = null,
    @SerializedName("creator") val creator: Creator? = null,
    @SerializedName("nb_tracks") val nbTracks: Int = 0,
    @SerializedName("link") val link: String? = null,
    @SerializedName("creation_date") val creationDate: String? = null
) : Parcelable

@Parcelize
data class Creator(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = ""
) : Parcelable

 */