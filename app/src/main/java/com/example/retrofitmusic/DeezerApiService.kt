package com.example.retrofitmusic

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DeezerApiService
{
    @GET("chart/0/tracks")
    fun listPost(): Call<DeezerResponse>

    @GET("album/{albumId}")
    fun getAlbum(@Path("albumId") albumId: Int): Call<AlbumResponse>

}