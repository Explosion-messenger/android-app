package com.explosion.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import coil.compose.AsyncImage
import com.explosion.messenger.data.remote.ChatDto
import com.explosion.messenger.ui.theme.AccentBlue
import com.explosion.messenger.ui.theme.BgDark
import com.explosion.messenger.ui.theme.BgSidebar
import com.explosion.messenger.ui.theme.TextDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onLogout: () -> Unit,
    onChatClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val chats by viewModel.chats.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchChats()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MESSAGES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = AccentBlue)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = TextDim)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        },
        containerColor = BgDark
    ) { padding ->
        if (loading && chats.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        }
        
        if (showCreateDialog) {
            CreateChatDialog(
                viewModel = viewModel,
                onDismiss = { showCreateDialog = false },
                onChatCreated = {
                    showCreateDialog = false
                    onChatClick(it)
                }
            )
        }

        val userStatuses by viewModel.userStatuses.collectAsState()
        val typingUsersMap by viewModel.typingUsers.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(chats) { chat ->
                ChatItem(chat, viewModel.currentUserId, userStatuses, typingUsersMap[chat.id], onChatClick)
            }
        }
    }
}

@Composable
fun CreateChatDialog(viewModel: ChatViewModel, onDismiss: () -> Unit, onChatCreated: (Int) -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Direct, 1 = Group
    val tabs = listOf("Direct", "Group")

    var searchQuery by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()
    
    // Store selected users
    var selectedUsers by remember { mutableStateOf(setOf<com.explosion.messenger.data.remote.UserOut>()) }
    var groupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Message") },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = AccentBlue
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { 
                                selectedTabIndex = index 
                                // Auto-correct selection logic
                                if (index == 0 && selectedUsers.size > 1) {
                                    selectedUsers = setOf(selectedUsers.first())
                                }
                            },
                            text = { Text(title, color = if (selectedTabIndex == index) AccentBlue else TextDim) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTabIndex == 1) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (selectedUsers.isNotEmpty()) {
                    LazyRow {
                        items(selectedUsers.toList()) { u ->
                            Card(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable { selectedUsers = selectedUsers - u },
                                colors = CardDefaults.cardColors(containerColor = AccentBlue)
                            ) {
                                Text(u.username, Modifier.padding(4.dp), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        if (it.length > 2) viewModel.searchUsers(it)
                    },
                    label = { Text("Search users") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(results) { user ->
                        val isSelected = selectedUsers.contains(user)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isSelected) {
                                        selectedUsers = selectedUsers - user
                                    } else {
                                        if (selectedTabIndex == 0) {
                                            selectedUsers = setOf(user) // Only 1 allowed in direct
                                        } else {
                                            selectedUsers = selectedUsers + user
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(user.username, fontSize = 16.sp, fontWeight = if(isSelected) FontWeight.Black else FontWeight.Normal, color = if(isSelected) AccentBlue else Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedUsers.isNotEmpty()) {
                        val ids = selectedUsers.map { it.id }
                        viewModel.createChat(
                            userIds = ids,
                            name = if (selectedTabIndex == 1) groupName else null,
                            isGroup = selectedTabIndex == 1,
                            onSuccess = onChatCreated
                        )
                    }
                },
                enabled = selectedUsers.isNotEmpty()
            ) {
                Text(if (selectedTabIndex == 1) "CREATE GROUP" else "START CHAT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChatItem(chat: ChatDto, currentUserId: Int, userStatuses: Map<Int, String>, typingUsers: List<String>?, onChatClick: (Int) -> Unit) {
    val otherMember = chat.members.firstOrNull { it.id != currentUserId } ?: chat.members.firstOrNull()
    val status = if (chat.is_group) null else otherMember?.id?.let { userStatuses[it] } ?: "offline"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick(chat.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgSidebar),
                contentAlignment = Alignment.Center
            ) {
                if (otherMember?.avatar_path != null) {
                    AsyncImage(
                        model = "${com.explosion.messenger.util.Constants.AVATAR_URL}${otherMember.avatar_path}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (chat.name ?: otherMember?.username ?: "?").take(1).uppercase(),
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Status Dot (Non-clipping)
            if (!chat.is_group && status != "offline") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(16.dp)
                        .background(BgDark, CircleShape)
                        .padding(2.5.dp)
                        .background(if (status == "online") Color(0xFF22C55E) else com.explosion.messenger.ui.theme.AwayYellow, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name ?: otherMember?.username ?: "Unknown",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val typingText = when {
                typingUsers.isNullOrEmpty() -> chat.last_message?.text ?: "No messages yet"
                typingUsers.size == 1 -> "${typingUsers[0]} is typing..."
                else -> "${typingUsers.size} users are typing..."
            }
            Text(
                text = typingText,
                color = if (!typingUsers.isNullOrEmpty()) AccentBlue else TextDim,
                fontSize = 14.sp,
                maxLines = 1,
                fontWeight = if (!typingUsers.isNullOrEmpty()) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (chat.last_message != null) {
            Text(
                text = "12:45", // Mock time
                color = TextDim,
                fontSize = 11.sp
            )
        }
    }
}
