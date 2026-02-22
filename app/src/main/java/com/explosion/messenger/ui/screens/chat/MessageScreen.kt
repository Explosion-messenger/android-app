package com.explosion.messenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Popup
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explosion.messenger.data.remote.MessageDto
import com.explosion.messenger.ui.theme.AccentBlue
import com.explosion.messenger.ui.theme.BgDark
import com.explosion.messenger.ui.theme.BgSidebar
import com.explosion.messenger.ui.theme.TextDim
import com.explosion.messenger.ui.theme.TextWhite
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyRow
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import androidx.compose.foundation.border
import androidx.compose.animation.animateContentSize
import com.explosion.messenger.data.remote.MessageReadOutDto
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import coil.compose.AsyncImage
import com.explosion.messenger.util.Constants
import androidx.compose.ui.layout.ContentScale

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
    var showEditGroupDialog by remember { mutableStateOf(false) }
    var activeReactionMsgId by remember { mutableStateOf<Int?>(null) }
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
            val lastMsg = messages.firstOrNull() // Newest because of reverseLayout list order? 
            // wait, list is reversed in UI, but what about the backing data?
            // viewModel.loadMessages: response.body()?.reversed()
            // webSocketManager.messages.collect: [dto] + _messages.value
            // So messages[0] is the NEWEST.
            
            val isMyMsg = lastMsg?.sender_id == currentUserId
            
            if (isMyMsg || isAtBottom) {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Typing status sender logic
    LaunchedEffect(textState) {
        if (textState.isNotEmpty()) {
            viewModel.sendTypingStatus(true)
            kotlinx.coroutines.delay(4000) // Backend timeout is usually around 5s
            viewModel.sendTypingStatus(false)
        } else {
            viewModel.sendTypingStatus(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val userStatuses by viewModel.userStatuses.collectAsState()
                    val otherMember = currentChat?.members?.firstOrNull { it.id != currentUserId }
                    val status = if (currentChat?.is_group == true) null else otherMember?.id?.let { userStatuses[it] } ?: "offline"

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar in Header
                        Box(modifier = Modifier.size(38.dp)) {
                            if (currentChat?.is_group == true) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(BgSidebar),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (currentChat?.name ?: "G").take(1).uppercase(),
                                        color = AccentBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (otherMember?.avatar_path != null) {
                                        AsyncImage(
                                            model = "${Constants.AVATAR_URL}${otherMember.avatar_path}",
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(BgSidebar),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (otherMember?.username ?: "?").take(1).uppercase(),
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                    
                                    // Status Badge (Non-clipping)
                                    if (status != null && status != "offline") {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(x = 2.dp, y = 2.dp)
                                                .size(12.dp)
                                                .background(BgDark, CircleShape)
                                                .padding(2.dp)
                                                .background(if (status == "online") Color(0xFF22C55E) else com.explosion.messenger.ui.theme.AwayYellow, CircleShape)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                (currentChat?.name ?: otherMember?.username ?: "CHAT").uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                            
                            val statusText = when {
                                typingUsers.isNotEmpty() -> {
                                    if (typingUsers.size == 1) "${typingUsers[0]} is typing..." else "${typingUsers.size} are typing..."
                                }
                                status != null -> status.lowercase()
                                else -> ""
                            }
                            
                            if (statusText.isNotEmpty()) {
                                Text(
                                    statusText,
                                    fontSize = 12.sp,
                                    color = if (typingUsers.isNotEmpty()) AccentBlue else TextDim,
                                    fontWeight = if (typingUsers.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDim)
                    }
                },
                actions = {
                    if (currentChat?.is_group == true) {
                        IconButton(onClick = { showEditGroupDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Group", tint = AccentBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                if (showEditGroupDialog) {
                    var editNameState by remember { mutableStateOf(currentChat?.name ?: "") }
                    AlertDialog(
                        onDismissRequest = { showEditGroupDialog = false },
                        title = { Text("Edit Group") },
                        text = {
                            OutlinedTextField(
                                value = editNameState,
                                onValueChange = { editNameState = it },
                                label = { Text("Group Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (editNameState.isNotBlank()) {
                                    viewModel.updateGroupName(editNameState)
                                }
                                showEditGroupDialog = false
                            }) {
                                Text("SAVE")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditGroupDialog = false }) { Text("Cancel") }
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
                            
                            // Simple Date Divider Logic: Check if day differs from the next message in list (because of reverseLayout, next is index+1)
                            val currentZDT = try { ZonedDateTime.parse(msg.created_at) } catch (e: Exception) { null }
                            val currDay = currentZDT?.toLocalDate()
                            
                            val prevMsgDay = if (index + 1 < messages.size) {
                                val pZDT = try { ZonedDateTime.parse(messages[index + 1].created_at) } catch (e: Exception) { null }
                                pZDT?.toLocalDate()
                            } else null
                            
                            if (currDay != null && currDay != prevMsgDay) {
                                Text(
                                    text = currDay.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                    color = TextDim,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }

                            MessageItem(
                                msg = msg, 
                                isMine = msg.sender_id == currentUserId,
                                onDelete = { viewModel.deleteMessage(msg.id) },
                                onReact = { emoji -> 
                                    viewModel.toggleReaction(msg.id, emoji)
                                    activeReactionMsgId = null
                                },
                                timeStr = currentZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                                showReactionPopup = activeReactionMsgId == msg.id,
                                onShowReactionPopup = { activeReactionMsgId = msg.id },
                                onCloseReactionPopup = { activeReactionMsgId = null },
                                onRead = { viewModel.markAsRead(msg.id) },
                                isGroup = currentChat?.is_group == true,
                                currentUserId = currentUserId
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Input Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSidebar)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = TextDim) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = BgDark,
                            focusedContainerColor = BgDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(textState)
                            textState = ""
                        },
                        modifier = Modifier
                            .background(AccentBlue, RoundedCornerShape(24.dp))
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }

            // Scroll to Bottom FAB
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
    onDelete: () -> Unit, 
    onReact: (String) -> Unit, 
    timeStr: String,
    showReactionPopup: Boolean,
    onShowReactionPopup: () -> Unit,
    onCloseReactionPopup: () -> Unit,
    onRead: () -> Unit,
    isGroup: Boolean,
    currentUserId: Int
) {
    var showContextMenu by remember { mutableStateOf(false) } // For Delete (Long Press)

    // Trigger read status when message is displayed
    LaunchedEffect(msg.id) {
        if (!isMine && msg.read_by.none { it.user_id == currentUserId }) {
            onRead()
        }
    }

    if (showContextMenu) {
        AlertDialog(
            onDismissRequest = { showContextMenu = false },
            title = { Text("Message Options") },
            text = {
                Column {
                    if (isMine) {
                        Button(
                            onClick = {
                                onDelete()
                                showContextMenu = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Message")
                        }
                    } else {
                        Text("No options available.")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showContextMenu = false }) { Text("Cancel") }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
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
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (showReactionPopup) {
                ReactionMenuPopup(
                    onReact = onReact,
                    onDismiss = onCloseReactionPopup,
                    readBy = msg.read_by
                )
            }

            Row(
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = if (isMine) AccentBlue else BgSidebar,
                            shape = RoundedCornerShape(16.dp, 16.dp, if (isMine) 4.dp else 16.dp, if (isMine) 16.dp else 4.dp)
                        )
                        .padding(12.dp)
                        .widthIn(max = 280.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onShowReactionPopup() },
                                onLongPress = { if (isMine) showContextMenu = true }
                            )
                        }
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
                    Text(
                        text = msg.text ?: "",
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    
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
                            modifier = Modifier.padding(end = 4.dp).border(1.dp, BgDark, RoundedCornerShape(12.dp))
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionMenuPopup(onReact: (String) -> Unit, onDismiss: () -> Unit, readBy: List<MessageReadOutDto>) {
    var isExpanded by remember { mutableStateOf(false) }
    val primary = listOf("❤️", "🤝", "👍", "👌", "🍌", "🐳")
    val extend = listOf("👎", "🖕", "🍾", "🤔", "🥰", "👏", "😁", "🤯", "🤬", "😔", "🎉", "🤩", "🤮", "💩", "🙏", "🕊️", "🤡", "🥱", "🥴", "😍")

    Popup(
        onDismissRequest = onDismiss,
        alignment = Alignment.TopCenter
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BgDark),
            modifier = Modifier.padding(bottom = 8.dp).border(1.dp, BgSidebar, RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp).animateContentSize().widthIn(max = 280.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val visibleEmojis = if (isExpanded) (primary + extend) else primary
                    visibleEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .clickable { onReact(emoji) }
                                .padding(8.dp)
                        )
                    }
                    if (!isExpanded) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = TextDim,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .clickable { isExpanded = true }
                                .padding(8.dp)
                                .size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Collapse",
                            tint = TextDim,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .clickable { isExpanded = false }
                                .padding(8.dp)
                                .size(28.dp)
                        )
                    }
                }

                if (readBy.isNotEmpty()) {
                    Divider(color = BgSidebar, modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Read By",
                        fontSize = 10.sp,
                        color = TextDim,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                    )
                    readBy.forEach { read ->
                        val readZDT = try { java.time.ZonedDateTime.parse(read.read_at) } catch(e:Exception){ null }
                        val timeStr = readZDT?.withZoneSameInstant(java.time.ZoneId.systemDefault())?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "User #${read.user_id}", fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                            Text(text = timeStr, fontSize = 10.sp, color = TextDim)
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
            modifier = Modifier.size(14.dp),
            tint = TextWhite.copy(alpha = 0.5f)
        )
    } else {
        val color = if (isGroup) {
            // For groups, maybe purple for "some read" and blue for "all read"? 
            // Web usually just uses the accent color for any read.
            // Let's use AccentBlue for all read cases for simplicity and parity.
            AccentBlue
        } else {
            AccentBlue
        }
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = "Read",
            modifier = Modifier.size(14.dp),
            tint = color
        )
    }
}
