package com.example.retrofitmusic

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DeezerApiService
{
    @GET("album/{id}/tracks")
    fun listPost(@Path("id") id: String): Call<DeezerResponse>

    @GET("album/{id}")
    fun getAlbum(@Path("id") id: String): Call<AlbumResponse>
}