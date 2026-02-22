package com.explosion.messenger.util

object Constants {
    // For local testing on emulator: http://10.0.2.2:8000/
    // For production: https://your-domain.com/api/
    // (Note: Caddyfile handles /api/ prefix)
    const val BASE_URL = "https://explosion.warhead88.space/api/v1/"
    const val WS_URL = "wss://explosion.warhead88.space/ws"
    
    // For avatars, the URL depends on where Caddy serves them
    // In our Caddyfile, /avatars picks them from backend.
    const val AVATAR_URL = "https://explosion.warhead88.space/avatars/"
}
