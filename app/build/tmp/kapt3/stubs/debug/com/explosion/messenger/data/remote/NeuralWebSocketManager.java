package com.explosion.messenger.data.remote;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B)\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0004\b\n\u0010\u000bJ\u0006\u0010\u001b\u001a\u00020\u001cJ\u0006\u0010\u001d\u001a\u00020\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00180\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0016\u00a8\u0006\u001e"}, d2 = {"Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;", "", "client", "Lokhttp3/OkHttpClient;", "tokenManager", "Lcom/explosion/messenger/util/TokenManager;", "notificationHelper", "Lcom/explosion/messenger/util/NotificationHelper;", "json", "Lkotlinx/serialization/json/Json;", "<init>", "(Lokhttp3/OkHttpClient;Lcom/explosion/messenger/util/TokenManager;Lcom/explosion/messenger/util/NotificationHelper;Lkotlinx/serialization/json/Json;)V", "webSocket", "Lokhttp3/WebSocket;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "_messages", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/explosion/messenger/data/remote/NewMessageData;", "messages", "Lkotlinx/coroutines/flow/SharedFlow;", "getMessages", "()Lkotlinx/coroutines/flow/SharedFlow;", "_reactions", "Lcom/explosion/messenger/data/remote/ReactionData;", "reactions", "getReactions", "connect", "", "disconnect", "app_debug"})
public final class NeuralWebSocketManager {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.util.TokenManager tokenManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.util.NotificationHelper notificationHelper = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.Nullable()
    private okhttp3.WebSocket webSocket;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.explosion.messenger.data.remote.NewMessageData> _messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.explosion.messenger.data.remote.NewMessageData> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.explosion.messenger.data.remote.ReactionData> _reactions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.explosion.messenger.data.remote.ReactionData> reactions = null;
    
    @javax.inject.Inject()
    public NeuralWebSocketManager(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient client, @org.jetbrains.annotations.NotNull()
    com.explosion.messenger.util.TokenManager tokenManager, @org.jetbrains.annotations.NotNull()
    com.explosion.messenger.util.NotificationHelper notificationHelper, @org.jetbrains.annotations.NotNull()
    kotlinx.serialization.json.Json json) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.explosion.messenger.data.remote.NewMessageData> getMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.explosion.messenger.data.remote.ReactionData> getReactions() {
        return null;
    }
    
    public final void connect() {
    }
    
    public final void disconnect() {
    }
}