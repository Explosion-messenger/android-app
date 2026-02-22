package com.explosion.messenger.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.ChatDto
import com.explosion.messenger.data.remote.ChatUpdate
import com.explosion.messenger.data.remote.MessageCreateRequest
import com.explosion.messenger.data.remote.MessageDto
import com.explosion.messenger.data.remote.MessageReactionDto
import com.explosion.messenger.data.remote.MessageReadOutDto
import com.explosion.messenger.data.remote.NeuralWebSocketManager
import com.explosion.messenger.data.remote.ReactionToggle
import com.explosion.messenger.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val webSocketManager: NeuralWebSocketManager
) : ViewModel() {

    val currentUserId = tokenManager.getUserId()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _currentChat = MutableStateFlow<ChatDto?>(null)
    val currentChat: StateFlow<ChatDto?> = _currentChat.asStateFlow()

    private var currentChatId: Int = -1

    init {
        // Collect real-time messages from WebSocket
        viewModelScope.launch {
            webSocketManager.messages.collect { newMsg ->
                if (newMsg.chat_id == currentChatId) {
                    val dto = MessageDto(
                        id = newMsg.id,
                        text = newMsg.text,
                        sender_id = newMsg.sender.id,
                        sender = newMsg.sender,
                        created_at = java.time.Instant.now().toString(),
                        read_by = emptyList(),
                        reactions = emptyList()
                    )
                    _messages.value = listOf(dto) + _messages.value
                }
            }
        }
        
        // Collect real-time reactions from WebSocket
        viewModelScope.launch {
            webSocketManager.reactions.collect { incomingReaction ->
                if (incomingReaction.chat_id == currentChatId) {
                    val currentList = _messages.value.toMutableList()
                    val msgIndex = currentList.indexOfFirst { it.id == incomingReaction.message_id }
                    if (msgIndex != -1) {
                        val msg = currentList[msgIndex]
                        val rList = msg.reactions.toMutableList()
                        
                        if (incomingReaction.action == "added") {
                            // Backend now returns full message to toggleReaction, 
                            // but for live updates from others, we still need this:
                            if (rList.none { it.user_id == incomingReaction.user_id && it.emoji == incomingReaction.emoji }) {
                                rList.add(MessageReactionDto(
                                    id = 0,
                                    user_id = incomingReaction.user_id,
                                    emoji = incomingReaction.emoji,
                                    created_at = ""
                                ))
                            }
                        } else {
                            rList.removeAll { it.user_id == incomingReaction.user_id && it.emoji == incomingReaction.emoji }
                        }
                        
                        currentList[msgIndex] = msg.copy(reactions = rList)
                        _messages.value = currentList
                    }
                }
            }
        }

        // Collect real-time read receipts from WebSocket
        viewModelScope.launch {
            webSocketManager.readReceipts.collect { readData ->
                if (readData.chat_id == currentChatId) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == readData.message_id) {
                            if (msg.read_by.none { it.user_id == readData.user_id }) {
                                msg.copy(
                                    read_by = msg.read_by + MessageReadOutDto(
                                        user_id = readData.user_id,
                                        read_at = readData.read_at
                                    )
                                )
                            } else msg
                        } else msg
                    }
                }
            }
        }
    }

    fun loadMessages(chatId: Int) {
        currentChatId = chatId
        viewModelScope.launch {
            _loading.value = true
            try {
                // Fetch Chat Metadata
                val chatsResponse = api.getChats()
                if (chatsResponse.isSuccessful) {
                    _currentChat.value = chatsResponse.body()?.find { it.id == chatId }
                }

                // Fetch Messages
                val response = api.getMessages(chatId)
                if (response.isSuccessful) {
                    _messages.value = response.body() ?: emptyList()
                    // After loading messages, mark the whole chat as read
                    markChatAsRead(chatId)
                }
            } catch (e: Exception) {
                // handle
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            try {
                val response = api.sendMessage(MessageCreateRequest(chat_id = currentChatId, text = text))
                if (response.isSuccessful) {
                    // Message usually arrives via WS or we can add it directly. Let's rely on REST response if WS is slow.
                    // Wait, websocket might duplicate it. Backend sends to all participants in the room via WS.
                    // We'll let the websocket flow catch it if the backend broadcasts to sender.
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            try {
                val response = api.deleteMessage(messageId)
                if (response.isSuccessful) {
                    _messages.value = _messages.value.filter { it.id != messageId }
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun toggleReaction(messageId: Int, emoji: String) {
        viewModelScope.launch {
            try {
                val response = api.toggleReaction(messageId, ReactionToggle(emoji))
                if (response.isSuccessful) {
                    // Update the local message visually if the backend returns the updated MessageDto
                    // For now, re-fetching or WS will handle it, but we can also just do a targeted replace
                    response.body()?.let { updatedMsg ->
                        _messages.value = _messages.value.map { if (it.id == updatedMsg.id) updatedMsg else it }
                    }
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun updateGroupName(name: String) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.updateChat(currentChatId, ChatUpdate(name = name))
                if (response.isSuccessful) {
                    _currentChat.value = response.body()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun markAsRead(messageId: Int) {
        val msg = _messages.value.find { it.id == messageId }
        // Optimization: only call if I am not in read_by
        if (msg != null && msg.read_by.none { it.user_id == currentUserId }) {
            viewModelScope.launch {
                try {
                    api.markMessageAsRead(messageId)
                } catch (e: Exception) {
                    // handle
                }
            }
        }
    }

    fun updatePresence(status: String) {
        webSocketManager.sendPresenceUpdate(status)
    }

    fun markChatAsRead(chatId: Int) {
        viewModelScope.launch {
            try {
                api.markChatAsRead(chatId)
            } catch (e: Exception) {
                // handle
            }
        }
    }
}
