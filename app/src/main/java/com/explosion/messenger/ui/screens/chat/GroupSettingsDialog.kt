package com.explosion.messenger.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.explosion.messenger.data.remote.UserOut
import com.explosion.messenger.ui.theme.*
import com.explosion.messenger.util.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsDialog(
    viewModel: MessageViewModel,
    onDismiss: () -> Unit,
    onLeaveOrDelete: () -> Unit
) {
    val chat by viewModel.currentChat.collectAsState()
    val searchResults by viewModel.userSearchResults.collectAsState()
    val userStatuses by viewModel.userStatuses.collectAsState()
    val currentUserId = viewModel.currentUserId
    
    val currentUserMember = chat?.members?.find { it.id == currentUserId }
    val isOwner = currentUserMember?.is_chat_owner == true
    val isAdmin = currentUserMember?.is_chat_admin == true || isOwner
    
    var editNameState by remember { mutableStateOf(chat?.name ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = File(context.cacheDir, "temp_avatar.jpg")
            context.contentResolver.openInputStream(it)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            viewModel.updateChatAvatar(body)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BgDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "GROUP INTELLIGENCE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDim)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgSidebar.copy(alpha = 0.5f))
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Area
                    item {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(40.dp))
                                    .background(BgSidebar)
                                    .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(40.dp))
                                    .clickable(enabled = isAdmin) { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (chat?.avatar_path != null) {
                                    AsyncImage(
                                        model = "${Constants.AVATAR_URL}${chat?.avatar_path}",
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextDim)
                                }
                                
                                if (isAdmin) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentBlue)
                                    .border(2.dp, BgDark, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Name Edit
                    item {
                        OutlinedTextField(
                            value = editNameState,
                            onValueChange = { editNameState = it },
                            enabled = isAdmin,
                            placeholder = { Text("GROUP NAME", color = TextDim.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgSidebar.copy(alpha = 0.3f),
                                unfocusedContainerColor = BgSidebar.copy(alpha = 0.3f),
                                disabledContainerColor = BgSidebar.copy(alpha = 0.1f),
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            trailingIcon = {
                                if (isAdmin && editNameState != chat?.name && editNameState.isNotBlank()) {
                                    IconButton(onClick = { viewModel.updateGroupName(editNameState) }) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentBlue)
                                    }
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Members List Section
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "AUTHORIZED PERSONNEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = TextDim
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "${chat?.members?.size ?: 0}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(chat?.members ?: emptyList()) { member ->
                        MemberItem(
                            member = member,
                            isOnline = userStatuses[member.id] == "online",
                            isCurrentUser = member.id == currentUserId,
                            isCallerOwner = isOwner,
                            isCallerAdmin = isAdmin,
                            onToggleAdmin = { viewModel.toggleAdmin(member.id, member.is_chat_admin == true) },
                            onRemove = { viewModel.removeMember(member.id) { if (it) onLeaveOrDelete() } }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add Members Section
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            "EXTEND ACCESS",
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = TextDim
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.searchUsers(it)
                            },
                            placeholder = { Text("SCAN DATABASE...", color = TextDim.copy(alpha = 0.3f)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentBlue) },
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    }

                    items(searchResults) { user ->
                        SearchResultItem(user = user) {
                            viewModel.addMember(user.id)
                            searchQuery = ""
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Bottom Buttons
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        if (isOwner) {
                            Button(
                                onClick = { viewModel.deleteGroup { onLeaveOrDelete() } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("PURGE GROUP", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.removeMember(currentUserId!!) { onLeaveOrDelete() } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextDim, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SEVER LINK", color = TextDim, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    member: UserOut,
    isOnline: Boolean,
    isCurrentUser: Boolean,
    isCallerOwner: Boolean,
    isCallerAdmin: Boolean,
    onToggleAdmin: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = BgSidebar.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = if (member.avatar_path != null) "${Constants.AVATAR_URL}${member.avatar_path}" else null,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(BgDark),
                    contentScale = ContentScale.Crop
                )
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .border(2.dp, BgDark, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("YOU", color = TextDim, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (member.is_chat_owner == true) {
                        RoleBadge("OWNER", AccentBlue)
                    } else if (member.is_chat_admin == true) {
                        RoleBadge("ADMIN", TextDim)
                    }
                }
            }

            if (isCallerOwner && member.is_chat_owner != true && !isCurrentUser) {
                IconButton(onClick = onToggleAdmin) {
                    Icon(
                        if (member.is_chat_admin == true) Icons.Default.RemoveModerator else Icons.Default.AddModerator,
                        contentDescription = null,
                        tint = if (member.is_chat_admin == true) Color.Red.copy(alpha = 0.7f) else AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if ((isCallerAdmin || isCurrentUser) && member.is_chat_owner != true) {
                IconButton(onClick = onRemove) {
                    Icon(
                        if (isCurrentUser) Icons.Default.Logout else Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (isCurrentUser) TextDim else Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoleBadge(text: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun SearchResultItem(user: UserOut, onClick: () -> Unit) {
    Surface(
        color = BgSidebar.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (user.avatar_path != null) "${Constants.AVATAR_URL}${user.avatar_path}" else null,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(BgDark),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(user.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Add, contentDescription = null, tint = AccentBlue)
        }
    }
}
