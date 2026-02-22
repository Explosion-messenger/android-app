package com.explosion.messenger.ui.screens.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.explosion.messenger.data.remote.MessageDto
import com.explosion.messenger.data.remote.MessageReadOutDto
import com.explosion.messenger.data.remote.UserOut
import com.explosion.messenger.ui.theme.*
import com.explosion.messenger.util.Constants
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

@Composable
fun DatePlaque(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BgSidebar.copy(alpha = 0.5f)),
            modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = date.uppercase(),
                color = TextDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun AnimatedTypingText(names: List<String>) {
    var dots by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            delay(500)
        }
    }
    val text = if (names.size == 1) "${names[0]} is typing$dots" else "Multiple people typing$dots"
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color.Green,
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    viewModel: MessageViewModel,
    chatId: Int,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val currentUserId = viewModel.currentUserId
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    val currentChat by viewModel.currentChat.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val selectedIds by viewModel.selectedMessageIds.collectAsState()
    var showEditGroupDialog by remember { mutableStateOf(false) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var activeReactionMsgId by remember { mutableStateOf<Int?>(null) }
    val userStatuses by viewModel.userStatuses.collectAsState()
    var lastDismissedTime by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    // Track if user is at the bottom (index 0 for reverseLayout)
    val isAtBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    // Smart Scroll Logic
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMsg = messages.first()
            val isMyMsg = lastMsg.sender.id == currentUserId
            
            if (isAtBottom || isMyMsg) {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val otherUser = if (currentChat?.is_group != true) currentChat?.members?.find { it.id != currentUserId } else null
                        val chatAvatar = if (currentChat?.is_group == true) currentChat?.avatar_path else otherUser?.avatar_path
                        val chatNameInitial = if (currentChat?.is_group == true) currentChat?.name?.take(1) else otherUser?.username?.take(1)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BgSidebar),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chatAvatar != null) {
                                AsyncImage(
                                    model = "${Constants.AVATAR_URL}$chatAvatar",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = (chatNameInitial ?: "?").uppercase(),
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (currentChat?.is_group == true) currentChat?.name ?: "Group Chat" else otherUser?.username ?: "Chat",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            if (typingUsers.isNotEmpty()) {
                                AnimatedTypingText(typingUsers)
                            } else {
                                if (currentChat?.is_group != true && otherUser != null) {
                                    val status = userStatuses[otherUser.id]
                                    val statusText = status ?: "offline"
                                    val color = when(statusText) {
                                        "online" -> Color.Green
                                        "away" -> com.explosion.messenger.ui.theme.AwayYellow
                                        else -> TextDim
                                    }
                                    if (statusText != "offline") {
                                        Text(
                                            text = statusText,
                                            fontSize = 11.sp,
                                            color = color
                                        )
                                    } else {
                                        Text(
                                            text = statusText,
                                            fontSize = 11.sp,
                                            color = TextDim
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection", tint = TextDim)
                        }
                        IconButton(onClick = { showBulkDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    } else if (currentChat?.is_group == true) {
                        IconButton(onClick = { showEditGroupDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Group", tint = AccentBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        containerColor = BgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (showBulkDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showBulkDeleteConfirm = false },
                        title = { Text("Delete Messages") },
                        text = { Text("Are you sure you want to delete ${selectedIds.size} messages? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteSelectedMessages()
                                    showBulkDeleteConfirm = false
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Text("DELETE")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBulkDeleteConfirm = false }) {
                                Text("CANCEL", color = TextDim)
                            }
                        },
                        containerColor = BgDark,
                        titleContentColor = TextWhite,
                        textContentColor = TextDim
                    )
                }

                if (showEditGroupDialog) {
                    GroupSettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { showEditGroupDialog = false },
                        onLeaveOrDelete = {
                            showEditGroupDialog = false
                            onBack()
                        }
                    )
                }

                if (loading && messages.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        reverseLayout = true
                    ) {
                        items(
                            count = messages.size,
                            key = { index -> messages[index].id }
                        ) { index ->
                            val msg = messages[index]
                            val currentZDT = try { ZonedDateTime.parse(msg.created_at) } catch (e: Exception) { null }
                            val currDay = currentZDT?.toLocalDate()
                            val prevMsgDay = if (index + 1 < messages.size) {
                                val pZDT = try { ZonedDateTime.parse(messages[index + 1].created_at) } catch (e: Exception) { null }
                                pZDT?.toLocalDate()
                            } else null
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (currDay != null && currDay != prevMsgDay) {
                                    DatePlaque(
                                        date = currDay.format(DateTimeFormatter.ofPattern("d MMMM, yyyy"))
                                    )
                                }

                                MessageItem(
                                    msg = msg, 
                                    isMine = msg.sender_id == currentUserId,
                                    isSelected = selectedIds.contains(msg.id),
                                selectionMode = selectedIds.isNotEmpty(),
                                onSelect = { viewModel.toggleSelection(msg.id) },
                                onDelete = { viewModel.deleteMessage(msg.id) },
                                onReact = { emoji -> 
                                    viewModel.toggleReaction(msg.id, emoji)
                                    activeReactionMsgId = null
                                },
                                timeStr = currentZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                showReactionPopup = activeReactionMsgId == msg.id,
                                onShowReactionPopup = { 
                                    if (activeReactionMsgId != null) {
                                        activeReactionMsgId = null
                                        lastDismissedTime = System.currentTimeMillis()
                                    } else {
                                        val now = System.currentTimeMillis()
                                        if (now - lastDismissedTime > 150) {
                                            activeReactionMsgId = msg.id
                                        }
                                    }
                                },
                                onCloseReactionPopup = { 
                                    activeReactionMsgId = null
                                    lastDismissedTime = System.currentTimeMillis()
                                },
                                onRead = { viewModel.markAsRead(msg.id) },
                                isGroup = currentChat?.is_group == true,
                                currentUserId = currentUserId,
                                members = currentChat?.members ?: emptyList()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            } // Close the 'else' block

            Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSidebar)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            val file = java.io.File(context.cacheDir, "upload_temp.jpg")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                java.io.FileOutputStream(file).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                            viewModel.sendFile(body)
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    }

                    IconButton(
                        onClick = { pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add Attachment", tint = AccentBlue)
                    }

                    OutlinedTextField(
                        value = textState,
                        onValueChange = { 
                            textState = it
                            viewModel.sendTypingStatus(it.isNotEmpty())
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = TextDim) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = BgDark,
                            focusedContainerColor = BgDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.sendMessage(textState)
                                viewModel.sendTypingStatus(false)
                                textState = ""
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        },
                        modifier = Modifier
                            .background(AccentBlue, RoundedCornerShape(24.dp))
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }

            if (!isAtBottom && messages.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = BgSidebar,
                    contentColor = AccentBlue,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 96.dp, end = 16.dp)
                        .size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    msg: MessageDto, 
    isMine: Boolean, 
    isSelected: Boolean,
    selectionMode: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit, 
    onReact: (String) -> Unit, 
    timeStr: String,
    showReactionPopup: Boolean,
    onShowReactionPopup: () -> Unit,
    onCloseReactionPopup: () -> Unit,
    onRead: () -> Unit,
    isGroup: Boolean,
    currentUserId: Int,
    members: List<com.explosion.messenger.data.remote.UserOut>
) {
    var showContextMenu by remember { mutableStateOf(false) }

    LaunchedEffect(msg.id) {
        if (!isMine && msg.read_by.none { it.user_id == currentUserId }) {
            onRead()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Selection Checkmark (Left side)
        if (selectionMode) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AccentBlue,
                    unselectedColor = TextDim
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        if (!isMine) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BgSidebar),
                contentAlignment = Alignment.Center
            ) {
                if (msg.sender.avatar_path != null) {
                    AsyncImage(
                        model = "${Constants.AVATAR_URL}${msg.sender.avatar_path}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (msg.sender.username).take(1).uppercase(),
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (selectionMode) onSelect()
                        else onShowReactionPopup()
                    },
                    onLongClick = { onSelect() }
                ),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (showReactionPopup) {
                ReactionMenuPopup(
                    onReact = onReact,
                    onDismiss = onCloseReactionPopup,
                    readBy = msg.read_by,
                    members = members,
                    isMine = isMine,
                    sentAt = msg.created_at,
                    isGroup = isGroup,
                    currentUserId = currentUserId
                )
            }

            Row(
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = when {
                                isSelected -> AccentBlue.copy(alpha = 0.3f)
                                isMine -> AccentBlue
                                else -> BgSidebar
                            },
                            shape = RoundedCornerShape(16.dp, 16.dp, if (isMine) 4.dp else 16.dp, if (isMine) 16.dp else 4.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = AccentBlue,
                            shape = RoundedCornerShape(16.dp, 16.dp, if (isMine) 4.dp else 16.dp, if (isMine) 16.dp else 4.dp)
                        )
                        .padding(12.dp)
                        .widthIn(max = 280.dp)
                ) {
                    if (!isMine && isGroup) {
                        Text(
                            text = msg.sender.username,
                            fontSize = 12.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    if (msg.file != null && msg.file.mime_type.startsWith("image/")) {
                        AsyncImage(
                            model = "${Constants.BASE_URL}files/download/${msg.file.path}?token=${viewModel.currentToken}",
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = if (msg.text.isNullOrBlank()) 0.dp else 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (!msg.text.isNullOrBlank()) {
                        Text(
                            text = msg.text,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    
                    if (timeStr.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp).align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = timeStr,
                                fontSize = 10.sp,
                                color = if (isMine) Color.White.copy(alpha = 0.7f) else TextDim,
                                textAlign = TextAlign.End
                            )
                            if (isMine) {
                                Spacer(modifier = Modifier.width(4.dp))
                                MessageStatusTicks(
                                    readCount = msg.read_by.size,
                                    isGroup = isGroup
                                )
                            }
                        }
                    }
                }
            }

            if (msg.reactions.isNotEmpty()) {
                val groupedReactions = msg.reactions.groupBy { it.emoji }.map { it.key to it.value.size }
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = if (isMine) 0.dp else 4.dp, end = if (isMine) 4.dp else 0.dp),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    groupedReactions.forEach { (emoji, count) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BgSidebar),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .border(1.dp, BgDark, RoundedCornerShape(12.dp))
                                .clickable { onReact(emoji) }
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (msg.sender.avatar_path != null) {
                    AsyncImage(
                        model = "${Constants.AVATAR_URL}${msg.sender.avatar_path}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (msg.sender.username).take(1).uppercase(),
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionMenuPopup(
    onReact: (String) -> Unit, 
    onDismiss: () -> Unit, 
    readBy: List<MessageReadOutDto>,
    members: List<com.explosion.messenger.data.remote.UserOut>,
    isMine: Boolean,
    sentAt: String,
    isGroup: Boolean,
    currentUserId: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🔥", "🍌", "🐳")

    Popup(
        onDismissRequest = onDismiss,
        alignment = Alignment.TopCenter
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BgDark),
            modifier = Modifier
                .padding(bottom = 8.dp)
                .border(1.dp, AccentBlue, RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp).animateContentSize().widthIn(min = 200.dp, max = 280.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clickable { onReact(emoji) }
                                .padding(8.dp)
                        )
                    }
                }

                Divider(color = BgSidebar, modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSidebar.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().border(0.5.dp, BgSidebar, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Message info",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue,
                        )

                        if (!isGroup) {
                            val sentZDT = try { ZonedDateTime.parse(sentAt) } catch(e:Exception){ null }
                            val sentTimeStr = sentZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) ?: ""
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "SENT", fontSize = 10.sp, color = TextDim, modifier = Modifier.width(45.dp))
                                    Text(text = sentTimeStr, fontSize = 12.sp, color = TextWhite)
                                }
                                
                                val recipientRead = readBy.find { it.user_id != currentUserId }
                                recipientRead?.let { read ->
                                    val readZDT = try { ZonedDateTime.parse(read.read_at) } catch(e:Exception){ null }
                                    val readTimeStr = readZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) ?: ""
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "READ", fontSize = 10.sp, color = TextDim, modifier = Modifier.width(45.dp))
                                        Text(text = readTimeStr, fontSize = 12.sp, color = TextWhite)
                                    }
                                }
                            }
                        } else {
                            if (readBy.isEmpty()) {
                                Text(text = "No one has read this yet", fontSize = 11.sp, color = TextDim)
                            } else {
                                readBy.forEach { read ->
                                    val user = members.find { it.id == read.user_id }
                                    val userName = user?.username ?: "User #${read.user_id}"
                                    val readZDT = try { ZonedDateTime.parse(read.read_at) } catch(e:Exception){ null }
                                    val timeStr = readZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = userName, fontSize = 12.sp, color = TextWhite, modifier = Modifier.weight(1f))
                                        Text(text = timeStr, fontSize = 10.sp, color = TextDim)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageStatusTicks(readCount: Int, isGroup: Boolean) {
    if (readCount == 0) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Sent",
            modifier = Modifier.size(16.dp),
            tint = com.explosion.messenger.ui.theme.TextDim
        )
    } else {
        val readColor = if (isGroup && readCount == 1) {
            Color(0xFFB026FF) // Neon purple
        } else {
            Color(0xFF39FF14) // Neon green
        }
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = "Read",
            modifier = Modifier.size(16.dp),
            tint = readColor
        )
    }
}
