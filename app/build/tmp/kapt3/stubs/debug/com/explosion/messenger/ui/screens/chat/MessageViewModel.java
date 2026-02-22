package com.explosion.messenger.ui.screens.chat;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\u000e\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\u000bJ\u000e\u0010\"\u001a\u00020 2\u0006\u0010#\u001a\u00020$J\u000e\u0010%\u001a\u00020 2\u0006\u0010&\u001a\u00020\u000bJ\u0016\u0010\'\u001a\u00020 2\u0006\u0010&\u001a\u00020\u000b2\u0006\u0010(\u001a\u00020$J\u000e\u0010)\u001a\u00020 2\u0006\u0010*\u001a\u00020$R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00170\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u0016\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0015R\u000e\u0010\u001e\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/explosion/messenger/ui/screens/chat/MessageViewModel;", "Landroidx/lifecycle/ViewModel;", "api", "Lcom/explosion/messenger/data/remote/ApiService;", "tokenManager", "Lcom/explosion/messenger/util/TokenManager;", "webSocketManager", "Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;", "<init>", "(Lcom/explosion/messenger/data/remote/ApiService;Lcom/explosion/messenger/util/TokenManager;Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;)V", "currentUserId", "", "getCurrentUserId", "()I", "_messages", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/explosion/messenger/data/remote/MessageDto;", "messages", "Lkotlinx/coroutines/flow/StateFlow;", "getMessages", "()Lkotlinx/coroutines/flow/StateFlow;", "_loading", "", "loading", "getLoading", "_currentChat", "Lcom/explosion/messenger/data/remote/ChatDto;", "currentChat", "getCurrentChat", "currentChatId", "loadMessages", "", "chatId", "sendMessage", "text", "", "deleteMessage", "messageId", "toggleReaction", "emoji", "updateGroupName", "name", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MessageViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.data.remote.ApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.util.TokenManager tokenManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.data.remote.NeuralWebSocketManager webSocketManager = null;
    private final int currentUserId = 0;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.explosion.messenger.data.remote.MessageDto>> _messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.explosion.messenger.data.remote.MessageDto>> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> loading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.explosion.messenger.data.remote.ChatDto> _currentChat = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.explosion.messenger.data.remote.ChatDto> currentChat = null;
    private int currentChatId = -1;
    
    @javax.inject.Inject()
    public MessageViewModel(@org.jetbrains.annotations.NotNull()
    com.explosion.messenger.data.remote.ApiService api, @org.jetbrains.annotations.NotNull()
    com.explosion.messenger.util.TokenManager tokenManager, @org.jetbrains.annotations.NotNull()
    com.explosion.messenger.data.remote.NeuralWebSocketManager webSocketManager) {
        super();
    }
    
    public final int getCurrentUserId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.explosion.messenger.data.remote.MessageDto>> getMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.explosion.messenger.data.remote.ChatDto> getCurrentChat() {
        return null;
    }
    
    public final void loadMessages(int chatId) {
    }
    
    public final void sendMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public final void deleteMessage(int messageId) {
    }
    
    public final void toggleReaction(int messageId, @org.jetbrains.annotations.NotNull()
    java.lang.String emoji) {
    }
    
    public final void updateGroupName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
}