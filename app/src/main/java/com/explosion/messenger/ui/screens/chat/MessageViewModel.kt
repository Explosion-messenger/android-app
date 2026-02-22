package com.explosion.messenger.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.ChatDto
import com.explosion.messenger.data.remote.ChatUpdate
import com.explosion.messenger.data.remote.MessageCreate
import com.explosion.messenger.data.remote.MessageDto
import com.explosion.messenger.data.remote.MessageReactionDto
import com.explosion.messenger.data.remote.MessageReadOutDto
import com.explosion.messenger.data.remote.NeuralWebSocketManager
import com.explosion.messenger.data.remote.ReactionToggle
import com.explosion.messenger.data.remote.BulkDeleteRequest
import com.explosion.messenger.data.remote.AddMemberRequest
import com.explosion.messenger.data.remote.MemberAdminUpdate
import com.explosion.messenger.data.remote.UserOut
import com.explosion.messenger.util.TokenManager
import okhttp3.MultipartBody
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
    val currentToken = tokenManager.getToken() ?: ""

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _currentChat = MutableStateFlow<ChatDto?>(null)
    val currentChat: StateFlow<ChatDto?> = _currentChat.asStateFlow()

    val userStatuses: StateFlow<Map<Int, String>> = webSocketManager.onlineStatusMap

    // List of usernames typing in current chat
    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    private val _selectedMessageIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedMessageIds: StateFlow<Set<Int>> = _selectedMessageIds.asStateFlow()

    private val _replyingTo = MutableStateFlow<MessageDto?>(null)
    val replyingTo: StateFlow<MessageDto?> = _replyingTo.asStateFlow()

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
                        created_at = newMsg.created_at,
                        read_by = emptyList(),
                        reactions = emptyList(),
                        file = newMsg.file,
                        reply_to = newMsg.reply_to
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


        // Collect typing updates
        viewModelScope.launch {
            webSocketManager.typingUpdates.collect { data ->
                if (data.chat_id == currentChatId) {
                    val list = _typingUsers.value.toMutableList()
                    if (data.is_typing) {
                        if (!list.contains(data.username)) list.add(data.username)
                    } else {
                        list.remove(data.username)
                    }
                    _typingUsers.value = list

                    if (data.is_typing) {
                        kotlinx.coroutines.delay(5000)
                        val finalList = _typingUsers.value.toMutableList()
                        if (finalList.remove(data.username)) {
                            _typingUsers.value = finalList
                        }
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
                    // Reverse because UI uses reverseLayout=true (index 0 is bottom)
                    _messages.value = (response.body() ?: emptyList()).reversed()
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

    fun setReplyingTo(message: MessageDto?) {
        _replyingTo.value = message
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            try {
                val replyId = _replyingTo.value?.id
                _replyingTo.value = null
                val response = api.sendMessage(MessageCreate(chat_id = currentChatId, text = text, reply_to_id = replyId))
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

    fun sendFile(body: okhttp3.MultipartBody.Part) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val uploadResponse = api.uploadFile(body)
                if (uploadResponse.isSuccessful) {
                    val fileOut = uploadResponse.body()
                    if (fileOut != null) {
                        val replyId = _replyingTo.value?.id
                        _replyingTo.value = null
                        val msgRequest = MessageCreate(chat_id = currentChatId, file_id = fileOut.id, reply_to_id = replyId)
                        api.sendMessage(msgRequest)
                    }
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

    fun sendTypingStatus(isTyping: Boolean) {
        if (currentChatId != -1) {
            webSocketManager.sendTypingStatus(currentChatId, isTyping)
        }
    }

    fun toggleSelection(messageId: Int) {
        val current = _selectedMessageIds.value.toMutableSet()
        if (current.contains(messageId)) {
            current.remove(messageId)
        } else {
            current.add(messageId)
        }
        _selectedMessageIds.value = current
    }

    fun clearSelection() {
        _selectedMessageIds.value = emptySet()
    }

    fun deleteSelectedMessages() {
        val ids = _selectedMessageIds.value.toList()
        if (ids.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val response = api.deleteMessagesBulk(BulkDeleteRequest(ids))
                if (response.isSuccessful) {
                    _messages.value = _messages.value.filter { it.id !in ids }
                    clearSelection()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    private val _userSearchResults = MutableStateFlow<List<UserOut>>(emptyList())
    val userSearchResults: StateFlow<List<UserOut>> = _userSearchResults.asStateFlow()

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _userSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = api.getUsers(query)
                if (response.isSuccessful) {
                    val existingIds = _currentChat.value?.members?.map { it.id } ?: emptyList()
                    _userSearchResults.value = (response.body() ?: emptyList()).filter { it.id !in existingIds }
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun addMember(userId: Int) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.addMember(currentChatId, AddMemberRequest(userId))
                if (response.isSuccessful) {
                    _currentChat.value = response.body()
                    _userSearchResults.value = emptyList()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun removeMember(userId: Int, onComplete: (Boolean) -> Unit = {}) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.removeMember(currentChatId, userId)
                if (response.isSuccessful) {
                    if (userId == currentUserId) {
                        onComplete(true) // User left group
                    } else {
                        // Re-fetch chat to get updated members
                        refreshChatMetadata()
                    }
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun refreshChatMetadata() {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.getChats()
                if (response.isSuccessful) {
                    _currentChat.value = response.body()?.find { it.id == currentChatId }
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun toggleAdmin(userId: Int, currentIsAdmin: Boolean) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.updateMemberAdmin(currentChatId, userId, MemberAdminUpdate(userId, !currentIsAdmin))
                if (response.isSuccessful) {
                    _currentChat.value = response.body()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun deleteGroup(onComplete: (Boolean) -> Unit) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.deleteChat(currentChatId)
                if (response.isSuccessful) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun updateChatAvatar(file: MultipartBody.Part) {
        if (currentChatId == -1) return
        viewModelScope.launch {
            try {
                val response = api.uploadChatAvatar(currentChatId, file)
                if (response.isSuccessful) {
                    _currentChat.value = response.body()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }
}
