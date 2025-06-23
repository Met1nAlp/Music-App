// Veriler.kt
package com.example.retrofitmusic

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Veriler
(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("preview")
    val preview: String = "",
    @SerializedName("cover_medium")
    val cover_medium: String = "",
    @SerializedName("artist")
    val artist: Artist ,

    var isFavorite: Boolean = false
) : Serializable

data class Artist
(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = ""
) : Serializable