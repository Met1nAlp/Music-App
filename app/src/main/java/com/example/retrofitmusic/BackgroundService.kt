package com.example.retrofitmusic

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class BackgroundService : Service()
{
    override fun onBind(intent: Intent?): IBinder?
    {
        return null
    }

    private companion object
    {
        const val CHANNEL_ID = "BackgroundServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate()
    {
        super.onCreate()

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        thread {
            while (true)
            {
                try
                {
                    Thread.sleep(1000)
                    println("Service çalışıyor")

                    sendNotificaiton()
                }
                catch (e: InterruptedException)
                {
                    e.printStackTrace()
                }
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Arkaplan Bildirimleri",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }


    }


    private fun BackgroundService.sendNotificaiton()
    {
        val notification = NotificationCompat.Builder(this , CHANNEL_ID)
            .setContentTitle("Arkaplan Servisi")
            .setContentText("Uygulama arkaplanda çalışıyor")
            .setSmallIcon(R.drawable.devam_asset)
            .build()

        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}


