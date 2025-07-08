package com.example.retrofitmusic


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


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


/*
class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    val chartTracks = repository.getChartTracks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getPlaylistDetails(playlistId: Long): StateFlow<Playlist?> =
        repository.getPlaylistDetails(playlistId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
}

 */
