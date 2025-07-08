package com.example.retrofitmusic

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

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

/*

    @GET("chart")
    suspend fun listPost(): DeezerResponse

    @GET("playlist/{id}")
    suspend fun getPlaylistDetails(@Path("id") playlistId: Long): Playlist

    @GET("playlist/{id}/tracks")
    suspend fun getPlaylistTracks(@Path("id") playlistId: Long): DeezerResponse


     */
}