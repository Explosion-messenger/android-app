package com.explosion.messenger.data.remote

import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("register/setup")
    suspend fun registerSetup(@Body request: UserCreate): Response<TwoFASetup>

    @POST("register/confirm")
    suspend fun registerConfirm(@Body request: UserRegisterConfirm): Response<UserOut>

    @POST("login/2fa")
    suspend fun verify2fa(
        @Query("username") username: String,
        @Body code: Map<String, String>
    ): Response<LoginResponse>

    @POST("login/2fa/passwordless")
    suspend fun loginPasswordless(
        @Body data: Map<String, String>
    ): Response<LoginResponse>

    @GET("me")
    suspend fun getMe(): Response<UserOut>

    @GET("chats")
    suspend fun getChats(): Response<List<ChatDto>>

    @GET("users")
    suspend fun getUsers(@Query("q") query: String = ""): Response<List<UserOut>>

    @POST("chats/create")
    suspend fun createChat(@Body request: CreateChatRequest): Response<ChatDto>

    @PATCH("chats/{chat_id}")
    suspend fun updateChat(
        @Path("chat_id") chatId: Int,
        @Body request: ChatUpdate
    ): Response<ChatDto>

    @GET("messages/{chat_id}")
    suspend fun getMessages(@Path("chat_id") chatId: Int, @Query("offset") offset: Int = 0): Response<List<MessageDto>>

    @POST("messages/send")
    suspend fun sendMessage(@Body request: MessageCreateRequest): Response<MessageDto>

    @DELETE("messages/{message_id}")
    suspend fun deleteMessage(@Path("message_id") messageId: Int): Response<Unit>

    @POST("messages/{message_id}/reactions")
    suspend fun toggleReaction(
        @Path("message_id") messageId: Int,
        @Body request: ReactionToggle
    ): Response<MessageDto> // Assume it returns MessageOut from backend

    @Multipart
    @POST("me/avatar")
    suspend fun uploadUserAvatar(@Part file: MultipartBody.Part): Response<UserOut>

    @Multipart
    @POST("chats/{chat_id}/avatar")
    suspend fun uploadChatAvatar(
        @Path("chat_id") chatId: Int,
        @Part file: MultipartBody.Part
    ): Response<ChatDto>
}

@kotlinx.serialization.Serializable
data class ReactionToggle(
    val emoji: String
)

@kotlinx.serialization.Serializable
data class UserCreate(
    val username: String,
    val email: String? = null,
    val password: String
)

@kotlinx.serialization.Serializable
data class UserRegisterConfirm(
    val username: String,
    val email: String? = null,
    val password: String,
    val secret: String,
    val code: String
)

@kotlinx.serialization.Serializable
data class TwoFASetup(
    val otp_auth_url: String,
    val secret: String
)

@kotlinx.serialization.Serializable
data class CreateChatRequest(
    val recipient_id: Int? = null,
    val member_ids: List<Int>? = null,
    val name: String? = null,
    val is_group: Boolean = false
)

@kotlinx.serialization.Serializable
data class MessageCreateRequest(
    val chat_id: Int,
    val text: String? = null,
    val file_id: Int? = null
)

@kotlinx.serialization.Serializable
data class ChatDto(
    val id: Int,
    val name: String? = null,
    val is_group: Boolean,
    val last_message: MessageDto? = null,
    val members: List<UserOut>
)

@kotlinx.serialization.Serializable
data class MessageReactionDto(
    val id: Int,
    val user_id: Int,
    val emoji: String,
    val created_at: String
)

@kotlinx.serialization.Serializable
data class MessageReadOutDto(
    val user_id: Int,
    val read_at: String
)

@kotlinx.serialization.Serializable
data class MessageDto(
    val id: Int,
    val text: String? = null,
    val sender_id: Int,
    val sender: UserOut,
    val created_at: String,
    val read_by: List<MessageReadOutDto> = emptyList(),
    val reactions: List<MessageReactionDto> = emptyList()
)

@kotlinx.serialization.Serializable
data class ChatUpdate(
    val name: String? = null,
    val avatar_path: String? = null
)
