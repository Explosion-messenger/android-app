package com.explosion.messenger.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.ChatDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.explosion.messenger.data.remote.CreateChatRequest
import com.explosion.messenger.data.remote.UserOut
import com.explosion.messenger.util.TokenManager
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import com.explosion.messenger.data.remote.NeuralWebSocketManager

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val wsManager: com.explosion.messenger.data.remote.NeuralWebSocketManager
) : ViewModel() {


    // chat_id -> List of users typing
    private val _typingUsers = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val typingUsers: StateFlow<Map<Int, List<String>>> = _typingUsers

    val userStatuses: StateFlow<Map<Int, String>> = wsManager.onlineStatusMap

    init {
        // Collect typing updates
        viewModelScope.launch {
            wsManager.typingUpdates.collect { data ->
                val current = _typingUsers.value.toMutableMap()
                val list = (current[data.chat_id] ?: emptyList()).toMutableList()
                
                if (data.is_typing) {
                    if (!list.contains(data.username)) {
                        list.add(data.username)
                    }
                } else {
                    list.remove(data.username)
                }
                
                if (list.isEmpty()) {
                    current.remove(data.chat_id)
                } else {
                    current[data.chat_id] = list
                }
                _typingUsers.value = current

                // Auto-clear after 5 seconds
                if (data.is_typing) {
                    kotlinx.coroutines.delay(5000)
                    val finalCurrent = _typingUsers.value.toMutableMap()
                    val finalList = (finalCurrent[data.chat_id] ?: emptyList()).toMutableList()
                    if (finalList.remove(data.username)) {
                        if (finalList.isEmpty()) finalCurrent.remove(data.chat_id)
                        else finalCurrent[data.chat_id] = finalList
                        _typingUsers.value = finalCurrent
                    }
                }
            }
        }

        // Collect new messages to update last_message and unread_count in chat list
        viewModelScope.launch {
            wsManager.messages.collect { msgData ->
                _chats.value = _chats.value.map { chat ->
                    if (chat.id != msgData.chat_id) return@map chat
                    val isMyMsg = msgData.sender_id == currentUserId
                    val newUnread = if (!isMyMsg) (chat.unread_count + 1) else chat.unread_count
                    val lastMsg = com.explosion.messenger.data.remote.MessageDto(
                        id = msgData.id,
                        chat_id = msgData.chat_id,
                        sender_id = msgData.sender_id,
                        sender = msgData.sender,
                        text = msgData.text,
                        file = msgData.file,
                        created_at = msgData.created_at,
                        reactions = emptyList(),
                        read_by = emptyList()
                    )
                    chat.copy(last_message = lastMsg, unread_count = newUnread)
                }.sortedByDescending {
                    it.last_message?.created_at ?: ""
                }
            }
        }

        // Collect read receipts to decrement unread_count when current user reads
        viewModelScope.launch {
            wsManager.readReceipts.collect { receipt ->
                if (receipt.user_id == currentUserId) {
                    _chats.value = _chats.value.map { chat ->
                        if (chat.id == receipt.chat_id) {
                            chat.copy(unread_count = maxOf(0, chat.unread_count - 1))
                        } else chat
                    }
                }
            }
        }
    }

    private val _chats = MutableStateFlow<List<ChatDto>>(emptyList())
    val chats: StateFlow<List<ChatDto>> = _chats

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchChats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = api.getChats()
                if (response.isSuccessful) {
                    _chats.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearUnread(chatId: Int) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(unread_count = 0) else chat
        }
    }

    val currentUserId = tokenManager.getUserId()

    private val _searchResults = MutableStateFlow<List<UserOut>>(emptyList())
    val searchResults: StateFlow<List<UserOut>> = _searchResults

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                val response = api.getUsers(query)
                if (response.isSuccessful) {
                    _searchResults.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun createChat(userIds: List<Int>, name: String? = null, isGroup: Boolean = false, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                // If not group, just take the first ID as recipient
                val req = if (isGroup) {
                    CreateChatRequest(member_ids = userIds, name = name, is_group = true)
                } else {
                    CreateChatRequest(recipient_id = userIds.firstOrNull(), is_group = false)
                }
                val response = api.createChat(req)
                if (response.isSuccessful) {
                    response.body()?.id?.let(onSuccess)
                    fetchChats()
                }
            } catch (e: Exception) {
                // handle
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val tempFile = File(context.cacheDir, "avatar_upload.jpg")
                FileOutputStream(tempFile).use { out ->
                    inputStream.copyTo(out)
                }
                
                val reqFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, reqFile)
                
                val response = api.uploadUserAvatar(body)
                if (response.isSuccessful) {
                    // Avatar updated, optionally re-fetch self data or rely on WS
                }
                tempFile.delete()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
