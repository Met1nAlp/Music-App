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

    @GET("playlist/{playlistId}/tracks")
    fun getPlaylistTracks(@Path("playlistId") playlistId: Long): Call<DeezerResponse>

    @GET("playlist/{playlistId}")
    fun getPlaylistDetails(@Path("playlistId") playlistId: Long): Call<Playlist>
}