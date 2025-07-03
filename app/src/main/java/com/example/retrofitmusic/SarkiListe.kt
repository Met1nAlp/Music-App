package com.example.retrofitmusic

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivitySarkiListeBinding
import com.google.android.material.appbar.AppBarLayout


class SarkiListe : AppCompatActivity()
{

    private lateinit var binding: ActivitySarkiListeBinding
    private lateinit var adapter: ListAdapter
    private lateinit var musicViewModel: MusicViewModel
    private var random = 0
    private var oynatiliyor = false
    private val postList: MutableList<Veriler> = mutableListOf()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingSongIndex: Int = -1
    private var currentPlaylistId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySarkiListeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)

        adapter = ListAdapter(postList)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {

            onBackPressedDispatcher.onBackPressed()
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

        currentPlaylistId = intent.getLongExtra("playlist_id", -1L)

        if (currentPlaylistId != -1L)
        {
            observeViewModelData(currentPlaylistId)
        }
        else
        {
            Toast.makeText(this, "Çalma listesi ID'si bulunamadı.", Toast.LENGTH_LONG).show()
        }

        setupAdapter()
        setupScrollListener()
    }

    private fun observeViewModelData(playlistId: Long)
    {

        musicViewModel.getPlaylistTracks(playlistId).observe(this) { tracks ->
            tracks?.let {
                postList.clear()
                postList.addAll(it)
                adapter.notifyDataSetChanged()


                if (postList.isNotEmpty()) {
                    random = (0 until postList.size).random()

                    binding.artistNameTextView.text = postList[random].album.title
                    binding.toolbarartistNameTextView.text = postList[random].album.title

                    Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                        .into(binding.artistImageView)
                    Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                        .into(binding.backgroundImage)

                    binding.simdiOynatilanSarkiAdi.text = postList[random].album.title
                    binding.simdiOynatilanSanatci.text = postList[random].artist.name
                    Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
                        .into(binding.simdiOynatilanKapak)
                }
            } ?: run {
                Toast.makeText(this@SarkiListe, "Şarkı listesi boş geldi.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("ResourceType")
    private fun setupScrollListener()
    {
        val appBarLayout = binding.appbar
        val toolbarPlayButton = binding.toolbarPlayButton
        val artistImageCard = binding.artistImageCardView
        val artistName = binding.artistNameTextView
        val mainPlayButton = binding.playButton
        val backgroundImage = binding.backgroundImage
        val gradientScrim = findViewById<View>(R.id.gradientScrimView)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            val maxScroll = appBarLayout.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

            if (percentage >= 0.8f)
            {
                toolbarPlayButton.visibility = View.VISIBLE
                binding.toolbarartistNameTextView.visibility = View.VISIBLE

                toolbarPlayButton.alpha = (percentage - 0.8f) / 0.2f
                binding.toolbarartistNameTextView.alpha = (percentage - 0.8f) / 0.2f
            }
            else
            {
                toolbarPlayButton.visibility = View.GONE
                binding.toolbarartistNameTextView.visibility = View.GONE
                toolbarPlayButton.alpha = 0.0f
                binding.toolbarartistNameTextView.alpha = 0.0f
            }

            val collapseAlpha = 1.0f - percentage
            artistImageCard.alpha = collapseAlpha
            artistName.alpha = collapseAlpha
            mainPlayButton.alpha = collapseAlpha
            backgroundImage.alpha = collapseAlpha
            gradientScrim.alpha = collapseAlpha


            if (percentage >= 0.99f)
            {
                artistImageCard.visibility = View.GONE
                artistName.visibility = View.GONE
                mainPlayButton.visibility = View.GONE
                backgroundImage.visibility = View.GONE
                gradientScrim.visibility = View.GONE
            }
            else
            {
                artistImageCard.visibility = View.VISIBLE
                artistName.visibility = View.VISIBLE
                mainPlayButton.visibility = View.VISIBLE
                backgroundImage.visibility = View.VISIBLE
                gradientScrim.visibility = View.VISIBLE
            }
        })

        toolbarPlayButton.setOnClickListener {
            togglePlayback(0)
        }
    }

    private fun updatePlayButtonStates(isPlaying: Boolean)
    {
        val playIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val toolbarPlayButton = findViewById<ImageView>(R.id.toolbarPlayButton)

        binding.playButton.setImageResource(playIcon)
        toolbarPlayButton.setImageResource(playIcon)
    }

    private fun togglePlayback(sayi: Int)
    {
        if (sayi != 0)
        {
            if (postList.isEmpty())
            {
                Toast.makeText(this, "Oynatılacak şarkı yok.", Toast.LENGTH_SHORT).show()
                return
            }

            if (mediaPlayer == null)
            {
                startPlayback(sayi)
                updatePlayButtonStates(true)


                binding.artistNameTextView.text = postList[sayi].album.title
                binding.toolbarartistNameTextView.text = postList[sayi].album.title
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
                updatePlayButtonStates(false)
            }
            else
            {
                mediaPlayer?.start()
                updatePlayButtonStates(true)
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
                val rastgeleIndex = (0 until postList.size).random()
                currentPlayingSongIndex = rastgeleIndex
                startPlayback(currentPlayingSongIndex)
                updatePlayButtonStates(true)

                // UI güncellemelerini buradan yapın
                binding.artistNameTextView.text = postList[currentPlayingSongIndex].album.title
                binding.toolbarartistNameTextView.text = postList[currentPlayingSongIndex].album.title

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
                updatePlayButtonStates(false)
            }
            else
            {
                mediaPlayer?.start()
                updatePlayButtonStates(true)
            }
        }
    }

    private fun startPlayback(index: Int) {
        if (index < 0 || index >= postList.size) {
            Toast.makeText(this, "Geçersiz şarkı indeksi.", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer?.release()
        mediaPlayer = null

        val song = postList[index]
        val previewUrl = song.preview

        binding.artistNameTextView.text = song.artist.name // Bu satırı kaldırdım, çünkü togglePlayback içinde de var
        binding.toolbarartistNameTextView.text = song.artist.name // Bu satırı kaldırdım

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
                    updatePlayButtonStates(false)
                    false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Medya oynatıcı başlatılırken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            mediaPlayer = null
            updatePlayButtonStates(false)
        }
    }

    private fun playNextSong() {
        currentPlayingSongIndex++
        if (currentPlayingSongIndex < postList.size) {
            startPlayback(currentPlayingSongIndex)
        } else {
            Toast.makeText(this, "Çalma listesi sona erdi.", Toast.LENGTH_SHORT).show()
            mediaPlayer?.release()
            mediaPlayer = null
            currentPlayingSongIndex = -1
            updatePlayButtonStates(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    //    private fun fetchPlaylistTracks(playlistId: Long) {
    //        val playlist: Long = 908622995L
    //
    //        val call = postService.getPlaylistTracks(playlistId)
    //        call.enqueue(object : Callback<DeezerResponse> {
    //            override fun onResponse(
    //                call: Call<DeezerResponse>,
    //                response: Response<DeezerResponse>
    //            ) {
    //                if (response.isSuccessful) {
    //                    response.body()?.data?.let { tracks ->
    //                        postList.clear()
    //                        postList.addAll(tracks)
    //
    //                        adapter.notifyDataSetChanged()
    //
    //                        random = (0 until postList.size).random()
    //
    //                        if (postList.isNotEmpty()) {
    //                            binding.artistNameTextView.text = postList[random].album.title
    //                            binding.toolbarartistNameTextView.text = postList[random].album.title
    //
    //                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
    //                                .into(binding.artistImageView)
    //                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
    //                                .into(binding.backgroundImage)
    //
    //                            binding.simdiOynatilanSarkiAdi.text = postList[random].album.title
    //                            binding.simdiOynatilanSanatci.text = postList[random].artist.name
    //                            Glide.with(this@SarkiListe).load(postList[random].album.cover_medium)
    //                                .into(binding.simdiOynatilanKapak)
    //                        }
    //                    }
    //                } else {
    //                    Toast.makeText(
    //                        this@SarkiListe,
    //                        "Şarkılar alınamadı: ${response.message()}",
    //                        Toast.LENGTH_LONG
    //                    ).show()
    //                }
    //            }
    //
    //            override fun onFailure(call: Call<DeezerResponse>, t: Throwable) {
    //                Toast.makeText(this@SarkiListe, "Ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
    //            }
    //        })
    //    }
    //
    //
    //     */

    private fun setupAdapter() {
        binding.recyclerViewSongs.apply {
            layoutManager = LinearLayoutManager(this@SarkiListe)
            adapter = this@SarkiListe.adapter
        }
    }
}