package com.explosion.messenger.data.repository

import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.LoginResponse
import com.explosion.messenger.util.TokenManager
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(username, password)
            if (response.isSuccessful) {
                val body = response.body()!!
                if (!body.requires_2fa && body.access_token != null) {
                    tokenManager.saveToken(body.access_token)
                    val me = api.getMe()
                    if (me.isSuccessful) {
                        tokenManager.saveUserId(me.body()!!.id)
                    }
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verify2fa(username: String, code: String): Result<LoginResponse> {
        return try {
            val response = api.verify2fa(username, mapOf("code" to code))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.access_token != null) {
                    tokenManager.saveToken(body.access_token)
                    val me = api.getMe()
                    if (me.isSuccessful) {
                        tokenManager.saveUserId(me.body()!!.id)
                    }
                }
                Result.success(body)
            } else {
                Result.failure(Exception("2FA failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginPasswordless(username: String, code: String): Result<LoginResponse> {
        return try {
            val response = api.loginPasswordless(mapOf("username" to username, "code" to code))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.access_token != null) {
                    tokenManager.saveToken(body.access_token)
                    val me = api.getMe()
                    if (me.isSuccessful) {
                        tokenManager.saveUserId(me.body()!!.id)
                    }
                }
                Result.success(body)
            } else {
                Result.failure(Exception("Bypass failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
