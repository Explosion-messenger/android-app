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

    private val _userStatuses = MutableStateFlow<Map<Int, String>>(emptyMap())
    val userStatuses: StateFlow<Map<Int, String>> = _userStatuses

    init {
        viewModelScope.launch {
            wsManager.onlineList.collect { list ->
                _userStatuses.value = list
            }
        }
        viewModelScope.launch {
            wsManager.userStatuses.collect { update ->
                val current = _userStatuses.value.toMutableMap()
                if (update.status == "offline") {
                    current.remove(update.user_id)
                } else {
                    current[update.user_id] = update.status
                }
                _userStatuses.value = current
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
