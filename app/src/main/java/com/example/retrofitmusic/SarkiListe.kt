package com.example.retrofitmusic

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivitySarkiListeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SarkiListe : AppCompatActivity() {

    private lateinit var binding: ActivitySarkiListeBinding
    private lateinit var adapter: ListAdapter
    private lateinit var postService: DeezerApiService
    private val postList: MutableList<Veriler> = mutableListOf()

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingSongIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySarkiListeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postService = ApiClient.getClient().create(DeezerApiService::class.java)
        adapter = ListAdapter(postList)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        /*
        binding.sayfaGeriImageView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

         */

        binding.playButton.setOnClickListener {
            togglePlayback()
        }

        val playlistId = intent.getLongExtra("playlist_id", -1L)

        if (playlistId != -1L) {
            fetchPlaylistTracks(playlistId)
        } else {
            Toast.makeText(this, "Çalma listesi ID'si bulunamadı.", Toast.LENGTH_LONG).show()
        }

        //setData()
        setupAdapter()
    }

    private fun togglePlayback() {
        if (postList.isEmpty()) {
            Toast.makeText(this, "Oynatılacak şarkı yok.", Toast.LENGTH_SHORT).show()
            return
        }

        if (mediaPlayer == null)
        {
            currentPlayingSongIndex = 0
            startPlayback(currentPlayingSongIndex)
            binding.playButton.setImageResource(R.drawable.ic_pause)
        }
        else if (mediaPlayer?.isPlaying == true)
        {
            mediaPlayer?.pause()
            binding.playButton.setImageResource(R.drawable.ic_play)
        }
        else
        {
            mediaPlayer?.start()
            binding.playButton.setImageResource(R.drawable.ic_pause)
        }
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

        binding.artistNameTextView.text = song.artist.name

        if (previewUrl.isNullOrEmpty()) {
            Toast.makeText(this, "${song.title} için önizleme URL'si bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                    Toast.makeText(this@SarkiListe, "${song.title} çalıyor.", Toast.LENGTH_SHORT).show()
                }
                setOnCompletionListener {
                    playNextSong()
                }
                setOnErrorListener { mp, what, extra ->
                    Toast.makeText(this@SarkiListe, "Oynatma hatası: $what, $extra", Toast.LENGTH_LONG).show()
                    mp?.release()
                    mediaPlayer = null
                    binding.playButton.setImageResource(R.drawable.devam_asset)
                    false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Medya oynatıcı başlatılırken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            mediaPlayer = null
            binding.playButton.setImageResource(R.drawable.devam_asset)
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
            binding.playButton.setImageResource(R.drawable.devam_asset)
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

/*
    private fun setData() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

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
                    }
                }
                else
                {
                    Toast.makeText(this@SarkiListe, "Liste alınamadı: ${response.message()}", Toast.LENGTH_LONG).show()
                }

                getAlbumData()
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                Toast.makeText(this@SarkiListe, "Ağ hatası (Liste): ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }




    private fun getAlbumData()
    {
        val callAlbum = postService.getAlbum(302127)
        callAlbum.enqueue(object : Callback<AlbumResponse>
        {
            override fun onResponse(
                call: Call<AlbumResponse>,
                response: Response<AlbumResponse>
            ) {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE

                if (response.isSuccessful)
                {
                    response.body()?.tracks?.data?.let { albumTracks ->
                        postList.addAll(albumTracks)
                    }
                }
                else
                {
                    Toast.makeText(
                        this@SarkiListe,
                        "Albüm alınamadı: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                binding.emptyView.visibility = if (postList.isEmpty()) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<AlbumResponse>, t: Throwable)
            {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                Toast.makeText(
                    this@SarkiListe,
                    "Ağ hatası (Albüm): ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

 */

    private fun fetchPlaylistTracks(playlistId: Long) {


        // API'den playlist'in şarkılarını istiyoruz
        val call = postService.getPlaylistTracks(playlistId)
        call.enqueue(object : Callback<DeezerResponse> {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>) {


                if (response.isSuccessful) {
                    response.body()?.data?.let { tracks ->
                        postList.clear()
                        postList.addAll(tracks)
                        adapter.notifyDataSetChanged()


                        if (postList.isNotEmpty()) {
                            binding.artistNameTextView.text = "Çalma Listesi Şarkıları"
                            Glide.with(this@SarkiListe).load(postList[0].album.cover_medium).into(binding.artistImageView)
                            Glide.with(this@SarkiListe).load(postList[0].album.cover_medium).into(binding.backgroundImage)
                        }

                    }
                } else {
                    Toast.makeText(this@SarkiListe, "Şarkılar alınamadı: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                Toast.makeText(this@SarkiListe, "Ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupAdapter()
    {
        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@SarkiListe)
            adapter = this@SarkiListe.adapter
        }
    }
}