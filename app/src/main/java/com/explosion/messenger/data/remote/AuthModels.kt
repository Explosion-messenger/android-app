package com.explosion.messenger.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val requires_2fa: Boolean = false,
    val username: String? = null
)

@Serializable
data class TwoFASetupResponse(
    val otp_auth_url: String,
    val secret: String
)

@Serializable
data class UserOut(
    val id: Int,
    val username: String,
    val email: String? = null,
    val avatar_path: String? = null,
    val is_2fa_enabled: Boolean = false,
    val is_chat_admin: Boolean? = false,
    val is_chat_owner: Boolean? = false
)

@Serializable
data class Token(
    val access_token: String,
    val token_type: String
)
