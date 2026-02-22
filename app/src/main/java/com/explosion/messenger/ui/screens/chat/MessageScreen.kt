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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explosion.messenger.data.remote.MessageDto
import com.explosion.messenger.ui.theme.AccentGreen
import com.explosion.messenger.ui.theme.BgDark
import com.explosion.messenger.ui.theme.BgSidebar
import com.explosion.messenger.ui.theme.TextDim
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.*

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
    var showEditGroupDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val fallbackName = currentChat?.members?.firstOrNull { it.id != currentUserId }?.username ?: "CHAT"
                    val displayTitle = currentChat?.name ?: fallbackName
                    Text(
                        displayTitle.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDim)
                    }
                },
                actions = {
                    if (currentChat?.is_group == true) {
                        IconButton(onClick = { showEditGroupDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Group", tint = AccentGreen)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    CircularProgressIndicator(color = AccentGreen)
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
                    items(messages.size) { index ->
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
                            onReact = { emoji -> viewModel.toggleReaction(msg.id, emoji) },
                            timeStr = currentZDT?.withZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
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
                        focusedBorderColor = AccentGreen,
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
                        .background(AccentGreen, RoundedCornerShape(24.dp))
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(msg: MessageDto, isMine: Boolean, onDelete: () -> Unit, onReact: (String) -> Unit, timeStr: String) {
    var showContextMenu by remember { mutableStateOf(false) } // For Delete (Long Press)
    var showReactionMenu by remember { mutableStateOf(false) } // For React (Double Tap)

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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        AnimatedVisibility(
            visible = showReactionMenu,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 20 })
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgSidebar),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    listOf("👍", "👎", "😂", "❤️", "🚀", "🔥", "🎉", "👀").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { 
                                    onReact(emoji)
                                    showReactionMenu = false 
                                }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = if (isMine) AccentGreen else BgSidebar,
                        shape = RoundedCornerShape(16.dp, 16.dp, if (isMine) 4.dp else 16.dp, if (isMine) 16.dp else 4.dp)
                    )
                    .combinedClickable(
                        onClick = { showReactionMenu = !showReactionMenu },
                        onLongClick = { if (isMine) showContextMenu = true }
                    )
                    .padding(12.dp)
                    .widthIn(max = 280.dp)
            ) {
                if (!isMine) {
                    Text(
                        text = msg.sender.username,
                        fontSize = 12.sp,
                        color = AccentGreen,
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
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else TextDim,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
                    )
                }
            }
        }

        if (msg.reactions.isNotEmpty()) {
            val groupedReactions = msg.reactions.groupBy { it.emoji }.map { it.key to it.value.size }
            Row(
                modifier = Modifier.padding(top = 4.dp, start = if (isMine) 0.dp else 8.dp, end = if (isMine) 8.dp else 0.dp),
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
