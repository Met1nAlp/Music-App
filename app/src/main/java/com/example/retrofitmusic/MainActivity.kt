package com.example.retrofitmusic

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.retrofitmusic.databinding.ActivityMainBinding





class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private var postList: MutableList<Veriler> = mutableListOf()
    private lateinit var rotateAnimation : android.view.animation.Animation
    private var sayac = 0
    private var terkrarSayaci = 1
    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())
    private val HIDE_DELAY_MS = 3000L
    private val hideRunnable = Runnable {

        if (binding.kararsinLinearLayout.visibility == View.VISIBLE)
        {
            binding.kararsinLinearLayout.visibility = View.INVISIBLE
            binding.ikincil.visibility = View.VISIBLE
        }
    }


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
                    val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        intent.getParcelableExtra(MusicService.EXTRA_CURRENT_TRACK, Veriler::class.java)
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

    private fun saveVolume(context: Context, volume: Int) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("lastVolume", volume).apply()
    }

    private fun getVolume(context: Context, defaultValue: Int): Int {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("lastVolume", defaultValue)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rotateAnimation = AnimationUtils.loadAnimation(this@MainActivity , R.anim.rotate)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                if (fromUser) {
                    val intent = Intent(this@MainActivity, MusicService::class.java)
                    intent.action = "com.example.retrofitmusic.SEEK"
                    intent.putExtra("position", progress)
                    ContextCompat.startForegroundService(this@MainActivity, intent)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?)
            {
            }
        })


        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.volumeSeekBar.progress = getVolume(this, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))


        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                if (fromUser)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    saveVolume(this@MainActivity, progress)
                    val percentage = (progress * 100) / seekBar?.max!!
                    binding.volumeSeekBar.contentDescription = "Ses düzeyi ayarlama, şu anki seviye %$percentage"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else
                {
                    vibrator.vibrate(50)
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



        binding.tekrarOynatImageView.setOnClickListener {

            val intent = Intent(this, MusicService::class.java)

            val resim = resources.getDrawable(R.drawable.fulltekrar_asset)
            val resim2 = resources.getDrawable(R.drawable.tekraroynat_asset)

            if (terkrarSayaci == 0 )
            {
                binding.tekrarOynatImageView.setColorFilter(resources.getColor(R.color.white))
                intent.action = MusicService.ACTION_REPEAT
                intent.putExtra("repeatmode", MusicService.RepeatMode.OFF.toString())

                terkrarSayaci = 1
            }
            else if (terkrarSayaci == 1 )
            {
                binding.tekrarOynatImageView.setColorFilter(resources.getColor(R.color.accent_color))
                intent.action = MusicService.ACTION_REPEAT
                intent.putExtra("repeatmode", MusicService.RepeatMode.REPEAT_ONE.name)
                terkrarSayaci = 0
            }
            ContextCompat.startForegroundService(this , intent)
        }

        binding.karistirImageView.setOnClickListener {

            binding.karistirImageView.setColorFilter(resources.getColor(R.color.accent_color))

            val intent = Intent(this, MusicService::class.java)
            intent.action = MusicService.ACTION_TOGGLE_SHUFFLE
            startService(intent)
            resetHideTimer()
        }

        binding.sesAyarlaImageView.setOnClickListener {

            binding.ikincil.visibility = View.INVISIBLE
            binding.kararsinLinearLayout.visibility = View.VISIBLE

            resetHideTimer()
        }

        binding.main.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetHideTimer()

                if (binding.kararsinLinearLayout.visibility == View.VISIBLE)
                {
                    binding.kararsinLinearLayout.visibility = View.INVISIBLE
                    binding.ikincil.visibility = View.VISIBLE
                }
            }
            false
        }

        var serviceIntent = Intent(this, BackgroundService::class.java)
        startService(serviceIntent)

        val receivedList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {

            intent.getParcelableArrayListExtra("song_list", Veriler::class.java)
        }
        else
        {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("song_list") as? ArrayList<Veriler>
        }

        sayac = intent.getIntExtra("song_index" , 0)

        if (receivedList != null && receivedList.isNotEmpty() ) {
            postList.clear()
            postList.addAll(receivedList)
            updateUI(postList[sayac])
            startMusicService()
        }
        else
        {
            Toast.makeText(this, "Şarkı listesi alınamadı.", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupClickListeners()
    }

    private fun setupClickListeners()
    {
        binding.ileributonuImageView.setOnClickListener {
            sendControlToService(MusicService.ACTION_NEXT)
        }

        binding.geributonuImageView.setOnClickListener {
            sendControlToService(MusicService.ACTION_PREVIOUS)
        }

        binding.durOynatImageView.setOnClickListener {

            if (isPlaying)
            {
                sendControlToService(MusicService.ACTION_PAUSE)
            }
            else
            {
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

    private fun startMusicService()
    {
        val serviceIntent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_START

            putParcelableArrayListExtra(MusicService.EXTRA_TRACK_LIST, ArrayList(postList))
            putExtra(MusicService.EXTRA_TRACK_INDEX, sayac)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun updateUI(track: Veriler)
    {
        binding.sarkiIsmiTextView.text = track.title
        binding.sanatciIsmiTextView.text = track.artist.name

        binding.seekBar.max = track.duration * 1000

        Glide.with(this@MainActivity)
            .load(track.album.cover_medium)
            .into(binding.gorselImageView)

        Glide.with(this@MainActivity)
            .load(track.album.cover_medium)
            .into(binding.arkaplanGorselImageView)



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

    private fun resetHideTimer()
        {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, HIDE_DELAY_MS)
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