package com.explosion.messenger.services;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\n\u001a\u00020\u000bH\u0016J\b\u0010\f\u001a\u00020\u000bH\u0002J\"\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u000eH\u0016J\b\u0010\u0013\u001a\u00020\u000bH\u0016J\u0014\u0010\u0014\u001a\u0004\u0018\u00010\u00152\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0016R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t\u00a8\u0006\u0016"}, d2 = {"Lcom/explosion/messenger/services/NeuralLinkService;", "Landroid/app/Service;", "<init>", "()V", "wsManager", "Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;", "getWsManager", "()Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;", "setWsManager", "(Lcom/explosion/messenger/data/remote/NeuralWebSocketManager;)V", "onCreate", "", "startNeuralLinkForeground", "onStartCommand", "", "intent", "Landroid/content/Intent;", "flags", "startId", "onDestroy", "onBind", "Landroid/os/IBinder;", "app_debug"})
public final class NeuralLinkService extends android.app.Service {
    @javax.inject.Inject()
    public com.explosion.messenger.data.remote.NeuralWebSocketManager wsManager;
    
    public NeuralLinkService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.explosion.messenger.data.remote.NeuralWebSocketManager getWsManager() {
        return null;
    }
    
    public final void setWsManager(@org.jetbrains.annotations.NotNull()
    com.explosion.messenger.data.remote.NeuralWebSocketManager p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void startNeuralLinkForeground() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
}