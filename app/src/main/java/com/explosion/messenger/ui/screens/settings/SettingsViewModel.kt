package com.explosion.messenger.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.UserOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserOut?>(null)
    val currentUser: StateFlow<UserOut?> = _currentUser.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val response = api.getMe()
                if (response.isSuccessful) {
                    _currentUser.value = response.body()
                }
            } catch (e: Exception) {
                // handle error
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
                    _currentUser.value = response.body()
                }
                tempFile.delete()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
