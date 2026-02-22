package com.explosion.messenger.data.remote

import com.explosion.messenger.util.Constants
import com.explosion.messenger.util.NotificationHelper
import com.explosion.messenger.util.TokenManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class WSMessage(
    val type: String,
    val data: JsonElement? = null
)

@Serializable
data class NewMessageData(
    val id: Int,
    val chat_id: Int,
    val sender: UserOut,
    val text: String? = null
)

@Serializable
data class ReactionData(
    val message_id: Int,
    val chat_id: Int,
    val user_id: Int,
    val emoji: String,
    val action: String // "added" or "removed"
)

@Serializable
data class ReadReceiptData(
    val message_id: Int,
    val chat_id: Int,
    val user_id: Int,
    val read_at: String
)

@Singleton
class NeuralWebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val tokenManager: TokenManager,
    private val notificationHelper: NotificationHelper,
    private val json: Json
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _messages = MutableSharedFlow<NewMessageData>()
    val messages: SharedFlow<NewMessageData> = _messages

    private val _reactions = MutableSharedFlow<ReactionData>()
    val reactions: SharedFlow<ReactionData> = _reactions

    private val _readReceipts = MutableSharedFlow<ReadReceiptData>()
    val readReceipts: SharedFlow<ReadReceiptData> = _readReceipts

    fun connect() {
        val token = tokenManager.getToken() ?: return
        val request = Request.Builder()
            .url("${Constants.WS_URL}?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val wsMsg = json.decodeFromString<WSMessage>(text)
                    if (wsMsg.type == "new_message") {
                        val msgData = json.decodeFromJsonElement<NewMessageData>(wsMsg.data!!)
                        
                        // Notify UI components
                        scope.launch {
                            _messages.emit(msgData)
                        }
                        
                        val currentUserId = tokenManager.getUserId()
                        if (msgData.sender.id != currentUserId) {
                            notificationHelper.showMessageNotification(
                                sender = msgData.sender.username,
                                message = msgData.text ?: "[Neural Attachment]"
                            )
                        }
                    } else if (wsMsg.type == "message_reaction") {
                        val reactData = json.decodeFromJsonElement<ReactionData>(wsMsg.data!!)
                        scope.launch {
                            _reactions.emit(reactData)
                        }
                    } else if (wsMsg.type == "message_read") {
                        val readData = json.decodeFromJsonElement<ReadReceiptData>(wsMsg.data!!)
                        scope.launch {
                            _readReceipts.emit(readData)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // Handle reconnection
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                // Handle reconnection
            }
        })
    }

    fun sendPresenceUpdate(status: String) {
        val jsonMsg = """{"type":"user_status_update","status":"$status"}"""
        webSocket?.send(jsonMsg)
    }

    fun disconnect() {
        webSocket?.close(1000, "User logout")
        webSocket = null
    }
}
