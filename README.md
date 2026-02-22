# Explosion Messenger Android App

This is the Android version of the Explosion Messenger, built with Jetpack Compose and modern Android architecture components.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModel + StateFlow
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Serialization**: Kotlinx Serialization
- **Image Loading**: Coil

## Setup
1. Open this folder in Android Studio.
2. Ensure the backend URL in `NetworkModule.kt` is correct (default is `http://10.0.2.2:8000` for emulator).
3. Build and Run.

## Features
- Mandatory TOTP 2FA during login.
- 2FA Neural Bypass (Passwordless login via TOTP).
- JWT Authentication via Interceptor.
- Real-time-ish UI with StateFlow.
