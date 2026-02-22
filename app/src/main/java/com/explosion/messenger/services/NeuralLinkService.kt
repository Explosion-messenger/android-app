package com.explosion.messenger.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.explosion.messenger.data.remote.NeuralWebSocketManager
import com.explosion.messenger.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NeuralLinkService : Service() {

    @Inject
    lateinit var wsManager: NeuralWebSocketManager

    override fun onCreate() {
        super.onCreate()
        startNeuralLinkForeground()
        wsManager.connect()
    }

    private fun startNeuralLinkForeground() {
        // No longer starting foreground to avoid persistent notification
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        wsManager.disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
