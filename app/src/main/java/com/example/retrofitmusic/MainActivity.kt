package com.example.retrofitmusic

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivityMainBinding
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable



class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var postService: DeezerApiService
    private var postList: MutableList<Veriler> = mutableListOf()
    private lateinit var rotateAnimation : android.view.animation.Animation
    private var selectId : String? = null
    private var sayac = 0
    private var isPlaying = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted)
        {
            Toast.makeText(this, "Bildirim izni verildi.", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(this, "Bildirim izni reddedildi. Müzik bildirimleri gösterilemeyebilir.", Toast.LENGTH_LONG).show()
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
                    // Güvenli type casting kullanıyoruz
                    val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        intent.getSerializableExtra(MusicService.EXTRA_CURRENT_TRACK, Veriler::class.java)
                    }
                    else
                    {
                        @Suppress("DEPRECATION")
                        intent.getSerializableExtra(MusicService.EXTRA_CURRENT_TRACK) as? Veriler
                    }
                    isPlaying = intent.getBooleanExtra(MusicService.EXTRA_IS_PLAYING, false)
                    track?.let { updateUI(it) }
                }
                MusicService.BROADCAST_PROGRESS_UPDATE ->
                {
                    val progress = intent.getIntExtra(MusicService.EXTRA_PROGRESS, 0)
                    val duration = intent.getIntExtra(MusicService.EXTRA_DURATION, 0)

                    binding.seekBar.max = duration
                    binding.seekBar.progress = progress

                    binding.currentTimeTextView.text = formatTime(progress)
                    binding.totalTimeTextView.text = formatTime(duration)
                }
            }
        }

        private fun formatTime(millis: Int): String {
            val minutes = (millis / 1000) / 60
            val seconds = (millis / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rotateAnimation = AnimationUtils.loadAnimation(this@MainActivity , R.anim.rotate)
        binding.gorselImageView.startAnimation(rotateAnimation)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val intent = Intent(this@MainActivity, MusicService::class.java)
                    intent.action = "com.example.retrofitmusic.SEEK"
                    intent.putExtra("position", progress)
                    ContextCompat.startForegroundService(this@MainActivity, intent)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı SeekBar'a dokunmaya başladığında yapılacak işlemler
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı SeekBar'dan elini çektiğinde yapılacak işlemler
            }
        })


        val serviceIntent = Intent(this, BackgroundService::class.java)
        startService(serviceIntent)

        selectId = intent.getIntExtra("id", -1).takeIf { it != -1 }.toString()
        println("Seçilen ID: $selectId")

        postService = ApiClient.getClient().create(DeezerApiService::class.java)
        fetchAlbumData()
        fetchTrackListAndStartService()

        setupClickListeners()
    }

    private fun setupClickListeners()
    {
        binding.ileributonuImageView.setOnClickListener {
            binding.gorselImageView.startAnimation(rotateAnimation)
            sendControlToService(MusicService.ACTION_NEXT)
        }

        binding.geributonuImageView.setOnClickListener {
            binding.gorselImageView.startAnimation(rotateAnimation)
            sendControlToService(MusicService.ACTION_PREVIOUS)
        }

        binding.durOynatImageView.setOnClickListener {

            if (isPlaying)
            {
                binding.gorselImageView.clearAnimation()
                sendControlToService(MusicService.ACTION_PAUSE)
            }
            else
            {
                binding.gorselImageView.startAnimation(rotateAnimation)
                sendControlToService(MusicService.ACTION_PLAY)
            }
        }
    }

    private fun sendControlToService(action: String)
    {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }

        ContextCompat.startForegroundService(this, intent)
    }

    private fun fetchAlbumData()
    {
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
                            .into(binding.gorselImageView)
                    }
                }
                else
                {
                    Toast.makeText(applicationContext, "Albüm verisi çekilirken hata: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<AlbumResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Albüm verisi çekilirken ağ hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTrackListAndStartService()
    {
        val call = postService.listPost("302127") // Hardcoded albüm ID'si

        call.enqueue(object : Callback<DeezerResponse>
        {
            override fun onResponse(call: Call<DeezerResponse>, response: Response<DeezerResponse>)
            {
                if (response.isSuccessful)
                {
                    response.body()?.data?.let { tracks ->
                        if (tracks.isNotEmpty())
                        {
                            postList.clear()
                            postList.addAll(tracks)

                            sayac = selectId?.let { id ->

                               val ali = id.toInt()

                                postList.indexOfFirst { it.id == ali  }.takeIf { it != -1 } ?: 0
                            } ?: 0

                            startMusicService()
                        }
                        else
                        {
                            Toast.makeText(applicationContext, "Parça listesi boş", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Parça listesi çekilirken hata: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DeezerResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Parça listesi çekilirken ağ hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startMusicService()
    {
        val serviceIntent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_START
            putExtra(MusicService.EXTRA_TRACK_LIST, postList as Serializable)
            putExtra(MusicService.EXTRA_TRACK_INDEX, sayac)
        }

        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun updateUI(track: Veriler)
    {
        binding.baslikTextView.text = track.title

        binding.seekBar.max = track.duration * 1000

        if (isPlaying)
        {
            binding.durOynatImageView.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.durdur_asset)
            )
        }
        else
        {
            binding.durOynatImageView.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.devam_asset)
            )
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart()
    {
        super.onStart()
        val intentFilter = IntentFilter().apply {
            addAction(MusicService.BROADCAST_TRACK_CHANGED)
            addAction(MusicService.BROADCAST_PROGRESS_UPDATE)
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

    override fun onStop()
    {
        super.onStop()
        unregisterReceiver(serviceStateReceiver)
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
}