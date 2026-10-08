package com.example.volctl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class VolService : Service() {
    private var saved = -1

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            val am = c.getSystemService(AudioManager::class.java)
            try {
                when (i.action) {
                    Intent.ACTION_USER_PRESENT -> {
                        if (saved < 0) saved = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
                        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0)
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        if (saved >= 0) {
                            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, saved, 0)
                            saved = -1
                        }
                    }
                }
            } catch (e: SecurityException) { }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("vol", "Ses kontrolü", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, "vol")
            .setContentTitle("Bildirim sesi kontrolü aktif")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .build()
        startForeground(1, n)
        registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY

    override fun onDestroy() {
        unregisterReceiver(receiver)
        try {
            if (saved >= 0) getSystemService(AudioManager::class.java)
                .setStreamVolume(AudioManager.STREAM_NOTIFICATION, saved, 0)
        } catch (e: SecurityException) {}
        super.onDestroy()
    }

    override fun onBind(i: Intent?) = null
}
