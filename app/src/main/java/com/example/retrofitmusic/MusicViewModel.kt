package com.example.retrofitmusic

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MusicViewModel : ViewModel()
{

    private val repository = MusicRepository()

    val chartTracks: LiveData<List<Veriler>> = repository.getChartTracks()

    fun getPlaylist(playlistId: Long): LiveData<Playlist>
    {
        return repository.getPlaylistDetails(playlistId)
    }

    fun getPlaylistTracks(playlistId: Long): LiveData<List<Veriler>>
    {
        return repository.getPlaylistTracks(playlistId)
    }

}