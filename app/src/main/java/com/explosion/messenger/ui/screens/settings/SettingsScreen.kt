package com.explosion.messenger.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.explosion.messenger.ui.components.CircularCropperDialog
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import coil.compose.AsyncImage
import com.explosion.messenger.ui.theme.AccentBlue
import com.explosion.messenger.ui.theme.BgDark
import com.explosion.messenger.ui.theme.BgSidebar
import com.explosion.messenger.ui.theme.TextDim
import com.explosion.messenger.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showCropper by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            showCropper = true
        }
    }

    if (showCropper && selectedUri != null) {
        CircularCropperDialog(
            uri = selectedUri!!,
            onDismiss = { showCropper = false },
            onCropped = { bitmap ->
                showCropper = false
                val bos = java.io.ByteArrayOutputStream()
                bitmap.compress(CompressFormat.JPEG, 90, bos)
                val bitmapData = bos.toByteArray()
                
                val requestFile = bitmapData.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile)
                viewModel.uploadAvatarPart(body)
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SETTINGS", fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDim)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(BgSidebar),
                contentAlignment = Alignment.Center
            ) {
                if (user?.avatar_path != null) {
                    AsyncImage(
                        model = "${Constants.AVATAR_URL}${user?.avatar_path}",
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (user?.username ?: "?").take(1).uppercase(),
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("CHANGE AVATAR", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("USERNAME", fontSize = 12.sp, color = TextDim, modifier = Modifier.fillMaxWidth())
            Text(
                text = user?.username ?: "Loading...",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Default.Logout, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOG OUT", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
