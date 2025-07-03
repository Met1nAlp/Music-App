package com.example.retrofitmusic

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
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
    private var random = 0
    private var oynatiliyor = false
    private var track: Veriler? = null
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

        toolbar.setNavigationOnClickListener {

            val gerigit = Intent(this, AnaSayfa::class.java)
            startActivity(gerigit)
        }



        binding.oynatButonu.setOnClickListener {

            val dur = resources.getDrawable(R.drawable.ic_pause)
            val devam = resources.getDrawable(R.drawable.ic_play)

            if (oynatiliyor)
            {
                binding.oynatButonu.setImageDrawable(devam)
                mediaPlayer?.pause()
                oynatiliyor = false
            }
            else
            {
                togglePlayback(random)
                binding.oynatButonu.setImageDrawable(dur)
                oynatiliyor = true
            }
        }

        binding.toolbar.setOnClickListener {
            val intent = Intent(this, AnaSayfa::class.java)
            startActivity(intent)
        }

        binding.playButton.setOnClickListener {
            togglePlayback(0)
        }

        val playlistId = intent.getLongExtra("playlist_id", -1L)

        if (playlistId != -1L)
        {
            fetchPlaylistTracks(playlistId)
        }
        else
        {
            Toast.makeText(this, "Çalma listesi ID'si bulunamadı.", Toast.LENGTH_LONG).show()
        }

        setupAdapter()
    }

    private fun togglePlayback(sayi : Int)
    {
        if ( sayi != 0 )
        {
            if (postList.isEmpty())
            {
                Toast.makeText(this, "Oynatılacak şarkı yok.", Toast.LENGTH_SHORT).show()
                return
            }

            if (mediaPlayer == null)
            {

                startPlayback(sayi)
                binding.playButton.setImageResource(R.drawable.ic_pause)

                binding.artistNameTextView.text = postList[sayi].album.title
                Glide.with(this@SarkiListe).load(postList[sayi].album.cover_medium)
                    .into(binding.artistImageView)
                Glide.with(this@SarkiListe).load(postList[sayi].album.cover_medium)
                    .into(binding.backgroundImage)

                binding.simdiOynatilanSarkiAdi.text = postList[sayi].album.title
                binding.simdiOynatilanSanatci.text = postList[sayi].artist.name
                Glide.with(this@SarkiListe).load(postList[sayi].album.cover_medium)
                    .into(binding.simdiOynatilanKapak)

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
        else
        {
            if (postList.isEmpty())
            {
                Toast.makeText(this, "Oynatılacak şarkı yok.", Toast.LENGTH_SHORT).show()
                return
            }

            if (mediaPlayer == null)
            {
                val rastgele = (0 until postList.size).random()

                currentPlayingSongIndex = rastgele
                startPlayback(currentPlayingSongIndex)
                binding.playButton.setImageResource(R.drawable.ic_pause)

                binding.artistNameTextView.text = postList[currentPlayingSongIndex].album.title
                Glide.with(this@SarkiListe).load(postList[currentPlayingSongIndex].album.cover_medium)
                    .into(binding.artistImageView)
                Glide.with(this@SarkiListe).load(postList[currentPlayingSongIndex].album.cover_medium)
                    .into(binding.backgroundImage)

                binding.simdiOynatilanSarkiAdi.text = postList[currentPlayingSongIndex].album.title
                binding.simdiOynatilanSanatci.text = postList[currentPlayingSongIndex].artist.name
                Glide.with(this@SarkiListe).load(postList[currentPlayingSongIndex].album.cover_medium)
                    .into(binding.simdiOynatilanKapak)

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




    private fun fetchPlaylistTracks(playlistId: Long) {

        val playlist: Long = 908622995L

        val call = postService.getPlaylistTracks(playlistId)
        call.enqueue(object : Callback<DeezerResponse> {
            override fun onResponse(
                call: Call<DeezerResponse>,
                response: Response<DeezerResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { tracks ->
                        postList.clear()
                        postList.addAll(tracks)

                        adapter.notifyDataSetChanged()

                        random = (0 until postList.size).random()

                        if (postList.isNotEmpty()) {
                            binding.artistNameTextView.text = postList[random].album.title
                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                                .into(binding.artistImageView)
                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                                .into(binding.backgroundImage)


                            binding.simdiOynatilanSarkiAdi.text = postList[random].album.title
                            binding.simdiOynatilanSanatci.text = postList[random].artist.name
                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                                .into(binding.simdiOynatilanKapak)
                        }

                    }
                } else {
                    Toast.makeText(
                        this@SarkiListe,
                        "Şarkılar alınamadı: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable) {
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