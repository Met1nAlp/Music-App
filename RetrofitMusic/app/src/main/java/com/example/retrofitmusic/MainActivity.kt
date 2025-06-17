package com.example.retrofitmusic

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var postService: DeezerApiService
    private var postList: MutableList<Veriler> = mutableListOf()
    private var progressJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var sayac = 0
    private var progressBardegeri = 0
    private var dur = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postService = ApiClient.getClient().create(DeezerApiService::class.java)
        fetchPosts()

        binding.ileributonuImageView.setOnClickListener {

            progressJob?.cancel()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            progressBardegeri = 0
            sayac = if (sayac < postList.size - 1) sayac + 1 else 0

            if (dur)
            {
                dur = false
                binding.durOynatImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.durdur_asset)
                )
            }

            fetchPosts()
        }

        binding.geributonuImageView.setOnClickListener {

            progressJob?.cancel()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            progressBardegeri = 0
            sayac = if (sayac > 0) sayac - 1 else postList.size - 1

            if (dur)
            {
                dur = false
                binding.durOynatImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.durdur_asset)
                )
            }

            fetchPosts()
        }

        binding.durOynatImageView.setOnClickListener {

            progressJob?.cancel()

            if (dur)
            {
                dur = false
                mediaPlayer?.start()
                binding.durOynatImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.durdur_asset)
                )
                startProgressBar()
            }
            else
            {
                dur = true
                mediaPlayer?.pause()
                binding.durOynatImageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.devam_asset)
                )
            }
        }
    }

    private fun fetchPosts()
    {
        val call = postService.listPost("302127")

        call.enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>)
            {
                if (response.isSuccessful)
                {
                    response.body()?.let { deezerResponse ->
                        postList.clear()
                        postList.addAll(deezerResponse.data)


                        if (postList.isNotEmpty())
                        {
                            binding.baslikTextView.text = postList[sayac].title
                            binding.progressBar.max = postList[sayac].duration

                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {

                                try {
                                    setDataSource(postList[sayac].preview)
                                    prepareAsync()
                                    setOnPreparedListener{

                                        if (!dur)
                                        {
                                            start()
                                            startProgressBar()
                                        }
                                    }
                                    setOnErrorListener { _, what, extra ->

                                        Toast.makeText(applicationContext,"Oynatma hatası: $what, $extra",Toast.LENGTH_LONG).show()

                                        true
                                    }
                                    setOnCompletionListener {

                                        if (sayac < postList.size - 1)
                                        {
                                            sayac++
                                            fetchPosts()
                                        }
                                        else
                                        {
                                            sayac = 0
                                            fetchPosts()
                                        }
                                    }
                                }
                                catch (e: Exception)
                                {
                                    Toast.makeText(applicationContext,"Hata: ${e.message}",Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else
                        {
                            Toast.makeText(applicationContext,"Parça listesi boş",Toast.LENGTH_LONG).show()
                        }
                    } ?: run {
                        Toast.makeText(applicationContext,"Veri bulunamadı",Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    Toast.makeText(applicationContext,"Hata: ${response.message()}",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable)
            {
                Toast.makeText(applicationContext,"Hata: ${t.message ?: "Bilinmeyen hata"}",Toast.LENGTH_LONG).show()
            }
        })


        val albumCall = postService.getAlbum("302127")

        albumCall.enqueue(object : Callback<AlbumResponse>
        {
            override fun onResponse(call: Call<AlbumResponse>, response: Response<AlbumResponse>)
            {
                if (response.isSuccessful)
                {
                    response.body()?.let { album ->

                        Glide.with(this@MainActivity)
                            .load(album.cover_medium)
                            .placeholder(R.drawable.durdur_asset)
                            .error(R.drawable.ic_launcher_background)
                            .into(binding.gorselImageView)

                    } ?: run {

                        Toast.makeText(applicationContext,"Albüm verisi bulunamadı",Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    Toast.makeText(applicationContext,"Hata: ${response.message()}",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AlbumResponse>, t: Throwable)
            {
                t.printStackTrace()

                Toast.makeText(applicationContext,"Hata: ${t.message ?: "Bilinmeyen hata"}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startProgressBar()
    {
        progressJob = CoroutineScope(Dispatchers.Main).launch {

            while (progressBardegeri <= postList[sayac].duration && !dur)
            {
                binding.progressBar.progress = progressBardegeri
                delay(1000L)
                progressBardegeri++
            }
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}