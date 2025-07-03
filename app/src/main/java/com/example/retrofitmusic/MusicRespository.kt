package com.example.retrofitmusic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MusicRepository
{

    private val deezerApiService: DeezerApiService = ApiClient.getClient().create(DeezerApiService::class.java)

    fun getChartTracks(): LiveData<List<Veriler>>
    {
        val data = MutableLiveData<List<Veriler>>()

        deezerApiService.listPost().enqueue(object : Callback<DeezerResponse> {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>) {
                if (response.isSuccessful)
                {
                    data.value = response.body()?.data
                }
                else
                {

                    println("API HATA: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                println("API BAŞARISIZ: ${t.message}")
            }
        })
        return data
    }

    fun getPlaylistDetails(playlistId: Long): LiveData<Playlist>
    {
        val data = MutableLiveData<Playlist>()

        deezerApiService.getPlaylistDetails(playlistId).enqueue(object : Callback<Playlist>
        {
            override fun onResponse(call: Call<Playlist>, response: Response<Playlist>)
            {
                if (response.isSuccessful)
                {
                    data.value = response.body()
                }
                else
                {
                    println("API HATA: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Playlist>, t: Throwable)
            {
                println("API BAŞARISIZ: ${t.message}")
            }
        })
        return data
    }

    fun getPlaylistTracks(playlistId: Long) : LiveData<List<Veriler>>
    {
        val data = MutableLiveData<List<Veriler>>()

        deezerApiService.getPlaylistTracks(playlistId).enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(
                call: Call<DeezerResponse>,
                response: Response<DeezerResponse>
            ) {
                if (response.isSuccessful)
                {
                    data.value = response.body()?.data
                }
                else
                {
                    println("API HATA: ${response.message()}")
                }
            }

            override fun onFailure(
                p0: Call<DeezerResponse?>,
                p1: Throwable
            ) {
                println("API BAŞARISIZ: ${p1.message}")
            }


        })

        return data
    }
}