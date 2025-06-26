package com.example.retrofitmusic

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivityAnaSayfaBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnaSayfa : AppCompatActivity()
{

    private lateinit var binding: ActivityAnaSayfaBinding
    private lateinit var musicadapter: AnaSayfaSarkiAdapter
    private lateinit var albumadapter: AnaSayfaAlbumAdapter
    private lateinit var postService: DeezerApiService
    private var isPlaying = false
    private var track: Veriler? = null
    private val postList: MutableList<Veriler> = mutableListOf()
    private val albumList: MutableList<Playlist> = mutableListOf()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingSongIndex: Int = -1


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ana_sayfa)

        binding = ActivityAnaSayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            intent.getParcelableExtra(MusicService.EXTRA_CURRENT_TRACK, Veriler::class.java)
        }
        else
        {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(MusicService.EXTRA_CURRENT_TRACK) as? Veriler
        }


        binding.oynatButonu.setOnClickListener {
            val resim = resources.getDrawable(R.drawable.devam_asset)
            val resim2 = resources.getDrawable(R.drawable.durdur_asset)

            val serviceIntent = Intent(this, MusicService::class.java).apply {

                if (isPlaying)
                {
                    binding.oynatButonu.setImageDrawable(resim2)
                    action = MusicService.ACTION_PLAY
                    isPlaying = false
                }
                else
                {
                    binding.oynatButonu.setImageDrawable(resim)
                    action = MusicService.ACTION_PAUSE
                    isPlaying = true
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(serviceIntent)
            }
            else
            {
                startService(serviceIntent)
            }

        }


        updateSimdiOynatilan(track)

        postService = ApiClient.getClient().create(DeezerApiService::class.java)
        musicadapter = AnaSayfaSarkiAdapter(postList)
        albumadapter = AnaSayfaAlbumAdapter(albumList)

        fetchPlaylistDetails()
        setData()
        setupAdapter()
    }

    private fun startPlayback(index: Int)
    {
        if (index < 0 || index >= postList.size)
        {
            Toast.makeText(this, "Geçersiz şarkı indeksi.", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer?.release()
        mediaPlayer = null

        val song = postList[index]
        val previewUrl = song.preview

        if (previewUrl.isNullOrEmpty()) {
            Toast.makeText(this, "${song.title} için önizleme URL'si bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        try
        {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    Toast.makeText(this@AnaSayfa, "${song.title} çalıyor.", Toast.LENGTH_SHORT).show()
                }
                setOnCompletionListener {
                    playNextSong()
                }
                setOnErrorListener { mp, what, extra ->
                    Toast.makeText(this@AnaSayfa, "Oynatma hatası: $what, $extra", Toast.LENGTH_LONG).show()
                    mp?.release()
                    mediaPlayer = null
                    false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Medya oynatıcı başlatılırken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            mediaPlayer = null
        }
    }

    private fun updateSimdiOynatilan(track: Veriler? = null)
    {


        if (track != null)
        {
            println("ali" + track?.title)
            println("veli" + track?.artist?.name)

            binding.simdiOynatilanSarkiAdi.text = track.title
            binding.simdiOynatilanSanatci.text = track.artist.name

            Glide.with(this@AnaSayfa)
                .load(track.album.cover_medium)
                .into(binding.simdiOynatilanKapak)

        }
        else
        {
            binding.simdiOynatilanSarkiAdi.text = "Oynatılan şarkı yok"
            binding.simdiOynatilanSanatci.text = "Oynatılan şarkı yok"
        }


    }

    private fun playNextSong()
    {
        currentPlayingSongIndex++
        if (currentPlayingSongIndex < postList.size)
        {
            startPlayback(currentPlayingSongIndex)
        }
        else
        {
            Toast.makeText(this, "Çalma listesi sona erdi.", Toast.LENGTH_SHORT).show()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPlayingSongIndex = -1
        }
    }

    private val serviceStateReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            when (intent?.action)
            {
                MusicService.BROADCAST_TRACK_CHANGED ->
                    {
                    track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        intent.getParcelableExtra(MusicService.EXTRA_CURRENT_TRACK, Veriler::class.java)
                    }
                    else
                    {
                        @Suppress("DEPRECATION")
                        intent.getSerializableExtra(MusicService.EXTRA_CURRENT_TRACK) as? Veriler
                    }
                    updateSimdiOynatilan(track)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart()
    {
        super.onStart()
        val intentFilter = IntentFilter().apply {
            addAction(MusicService.BROADCAST_TRACK_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            registerReceiver(serviceStateReceiver, intentFilter, RECEIVER_EXPORTED)
        }
        else
        {
            registerReceiver(serviceStateReceiver, intentFilter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(serviceStateReceiver)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setData()
    {

        val callChart = postService.listPost()
        callChart.enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>)
            {
                if (response.isSuccessful)
                {
                    response.body()?.data?.let { chartTracks ->
                        postList.clear()
                        postList.addAll(chartTracks)

                        musicadapter.notifyDataSetChanged()
                    }
                }
                else
                {
                    Toast.makeText(this@AnaSayfa, "Liste alınamadı: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                binding.sarkiListesiRecyclerView.visibility = View.VISIBLE
                Toast.makeText(this@AnaSayfa, "Ağ hatası (Liste): ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchPlaylistDetails()
    {
        val playlistId: Long = 908622995L
        val call = postService.getPlaylistDetails(playlistId)

        call.enqueue(object : Callback<Playlist> {
            override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
                if (response.isSuccessful) {
                    response.body()?.let { playlist ->
                        albumList.clear()
                        albumList.add(playlist)
                        albumadapter.notifyDataSetChanged()
                    } ?: run {
                        Toast.makeText(this@AnaSayfa, "Çalma listesi verisi boş geldi.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AnaSayfa, "Çalma listesi alınamadı: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Playlist>, t: Throwable) {
                Toast.makeText(this@AnaSayfa, "Ağ hatası (Çalma Listesi): ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun setupAdapter()
    {
        binding.sarkiListesiRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AnaSayfa)
            adapter = musicadapter
        }

        binding.albumIzgarasiRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AnaSayfa, LinearLayoutManager.HORIZONTAL, false)
            adapter = albumadapter
        }
    }

}