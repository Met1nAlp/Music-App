package com.example.retrofitmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var trackList: List<Veriler> = listOf()
    private var originalTrackList: List<Veriler> = listOf() // Orijinal listeyi saklamak için
    private var currentRepeatMode: RepeatMode = RepeatMode.OFF
    private var currentTrackIndex = 0
    private var isPaused = false
    private var isShuffled = false // Karıştırma durumunu takip et
    private var progressJob: Job? = null

    companion object {
        const val CHANNEL_ID = "MusicServiceChannel"
        const val NOTIFICATION_ID = 0

        // Service Actions
        const val ACTION_START = "com.example.retrofitmusic.START"
        const val ACTION_PLAY = "com.example.retrofitmusic.PLAY"
        const val ACTION_PAUSE = "com.example.retrofitmusic.PAUSE"
        const val ACTION_NEXT = "com.example.retrofitmusic.NEXT"
        const val ACTION_PREVIOUS = "com.example.retrofitmusic.PREVIOUS"
        const val ACTION_STOP = "com.example.retrofitmusic.STOP"
        const val ACTION_REPEAT = "com.example.retrofitmusic.REPEAT"
        const val ACTION_TOGGLE_SHUFFLE = "com.example.retrofitmusic.TOGGLE_SHUFFLE"

        // Broadcast Actions
        const val BROADCAST_TRACK_CHANGED = "com.example.retrofitmusic.TRACK_CHANGED"
        const val BROADCAST_STATE_CHANGED = "com.example.retrofitmusic.STATE_CHANGED"
        const val BROADCAST_PROGRESS_UPDATE = "com.example.retrofitmusic.PROGRESS_UPDATE"

        // Intent Extras
        const val EXTRA_TRACK_LIST = "track_list"
        const val EXTRA_TRACK_INDEX = "track_index"
        const val EXTRA_CURRENT_TRACK = "current_track"
        const val EXTRA_IS_PLAYING = "is_playing"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_SHUFFLE_STATE = "shuffle_state"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    enum class RepeatMode {
        OFF,
        REPEAT_ONE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "com.example.retrofitmusic.SEEK" -> {
                val position = intent.getIntExtra("position", 0)
                mediaPlayer?.seekTo(position)
            }
            ACTION_START -> {
                val newTrackList = intent.getSerializableExtra(EXTRA_TRACK_LIST) as? List<Veriler>
                val startIndex = intent.getIntExtra(EXTRA_TRACK_INDEX, 0)
                if (!newTrackList.isNullOrEmpty()) {
                    originalTrackList = newTrackList // Orijinal listeyi sakla
                    trackList = newTrackList.toMutableList() // Kopya ile çalış
                    currentTrackIndex = startIndex
                    if (isShuffled) {
                        shuffleTrackList()
                    }
                    startMusic()
                }
            }
            ACTION_REPEAT -> {
                val ali = intent.getStringExtra("repeatmode")
                currentRepeatMode = RepeatMode.entries.find { it.name == ali } ?: RepeatMode.OFF
                Log.d("MusicService", "Repeat Mode: $currentRepeatMode")
                mediaPlayer?.isLooping = currentRepeatMode == RepeatMode.REPEAT_ONE
                updateNotification()
                sendTrackUpdateBroadcast()
            }
            ACTION_TOGGLE_SHUFFLE -> {
                toggleShuffle()
            }
            ACTION_PLAY -> playMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_NEXT -> nextTrack()
            ACTION_PREVIOUS -> previousTrack()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }

    private fun toggleShuffle() {
        isShuffled = !isShuffled
        if (isShuffled) {
            shuffleTrackList()
        } else {
            restoreOriginalTrackList()
        }
        updateNotification()
        sendTrackUpdateBroadcast()
    }

    private fun shuffleTrackList() {
        val currentTrack = trackList.getOrNull(currentTrackIndex) ?: return
        val mutableTrackList = trackList.toMutableList()
        mutableTrackList.removeAt(currentTrackIndex) // Mevcut şarkıyı koru
        mutableTrackList.shuffle() // Geri kalanları karıştır
        mutableTrackList.add(0, currentTrack) // Mevcut şarkıyı başa ekle
        trackList = mutableTrackList
        currentTrackIndex = 0 // Mevcut şarkı şimdi listenin başında
    }

    private fun restoreOriginalTrackList() {
        val currentTrack = trackList.getOrNull(currentTrackIndex) ?: return
        trackList = originalTrackList.toMutableList()
        currentTrackIndex = trackList.indexOfFirst { it.id == currentTrack.id }
        if (currentTrackIndex == -1) currentTrackIndex = 0
    }

    private fun startMusic() {
        if (trackList.isEmpty() || currentTrackIndex !in trackList.indices) {
            Log.w("MusicService", "Track list empty or index out of bounds.")
            stopService()
            return
        }

        val track = trackList[currentTrackIndex]
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(track.preview)
                isLooping = currentRepeatMode == RepeatMode.REPEAT_ONE
                prepareAsync()
                setOnPreparedListener {
                    start()
                    isPaused = false
                    startForeground(NOTIFICATION_ID, createNotification())
                    sendTrackUpdateBroadcast()
                    startProgressBarUpdates()
                }
                setOnCompletionListener {
                    if (currentRepeatMode == RepeatMode.REPEAT_ONE) {
                        seekTo(0)
                        start()
                        sendTrackUpdateBroadcast()
                    } else {
                        nextTrack()
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                    Toast.makeText(applicationContext, "Müzik çalınırken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                    stopService()
                    true
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error starting music: ${e.message}", e)
                Toast.makeText(applicationContext, "Müzik başlatılırken genel bir hata oluştu.", Toast.LENGTH_SHORT).show()
                stopService()
            }
        }
    }

    private fun playMusic() {
        if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
            updateNotification()
            sendTrackUpdateBroadcast()
            startProgressBarUpdates()
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
            updateNotification()
            sendTrackUpdateBroadcast()
            progressJob?.cancel()
        }
    }

    private fun nextTrack() {
        if (trackList.isNotEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % trackList.size
            startMusic()
        }
    }

    private fun previousTrack() {
        if (trackList.isNotEmpty()) {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else trackList.size - 1
            startMusic()
        }
    }

    private fun stopService() {
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun startProgressBarUpdates() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer?.isPlaying == true) {
                val intent = Intent(BROADCAST_PROGRESS_UPDATE).apply {
                    putExtra(EXTRA_PROGRESS, mediaPlayer?.currentPosition ?: 0)
                    putExtra(EXTRA_DURATION, mediaPlayer?.duration ?: 0)
                }
                sendBroadcast(intent)
                delay(1000)
            }
        }
    }

    private fun sendTrackUpdateBroadcast() {
        if (trackList.isEmpty() || currentTrackIndex !in trackList.indices) return

        val track = trackList[currentTrackIndex]
        val intent = Intent(BROADCAST_TRACK_CHANGED).apply {
            putExtra(EXTRA_CURRENT_TRACK, track)
            putExtra(EXTRA_IS_PLAYING, !isPaused)
            putExtra(EXTRA_SHUFFLE_STATE, isShuffled)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification {
        val currentTrack = trackList.getOrNull(currentTrackIndex) ?: return NotificationCompat.Builder(this, CHANNEL_ID).build()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = PendingIntent.getService(this, 1, Intent(this, MusicService::class.java).setAction(ACTION_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        val playPauseIntent = PendingIntent.getService(this, 2, Intent(this, MusicService::class.java).setAction(if (isPaused) ACTION_PLAY else ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getService(this, 3, Intent(this, MusicService::class.java).setAction(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getService(this, 4, Intent(this, MusicService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon = if (isPaused) R.drawable.devam_asset else R.drawable.durdur_asset
        val playPauseText = if (isPaused) "Oynat" else "Duraklat"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTrack.title)
            .setContentText(currentTrack.artist.name)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ileri_geri_asset, "Önceki", prevIntent)
            .addAction(playPauseIcon, playPauseText, playPauseIntent)
            .addAction(R.drawable.ileri_asset, "Sonraki", nextIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = Glide.with(applicationContext)
                    .asBitmap()
                    .load(currentTrack.album.cover_medium)
                    .submit()
                    .get()
                withContext(Dispatchers.Main) {
                    builder.setLargeIcon(bitmap)
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error loading large icon: ${e.message}", e)
            }
        }

        return builder.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Müzik Çalar Bildirimleri",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Müzik çalma kontrolleri için bildirim kanalı"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        progressJob?.cancel()
        Log.d("MusicService", "Service destroyed.")
    }
}