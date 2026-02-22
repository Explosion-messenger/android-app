package com.explosion.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.explosion.messenger.ui.navigation.NavGraph
import com.explosion.messenger.ui.theme.ExplosionMessengerTheme
import com.explosion.messenger.util.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Request High Refresh Rate displays
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val displayMode = window.windowManager.defaultDisplay.supportedModes.maxByOrNull { it.refreshRate }
            if (displayMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = displayMode.modeId
                params.preferredRefreshRate = displayMode.refreshRate
                window.attributes = params
            }
        }

        if (tokenManager.getToken() != null) {
            startService(android.content.Intent(this, com.explosion.messenger.services.NeuralLinkService::class.java))
        }

        setContent {
            ExplosionMessengerTheme {
                NavGraph(tokenManager)
            }
        }
    }
}
