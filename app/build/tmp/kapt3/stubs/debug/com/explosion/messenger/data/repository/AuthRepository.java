package com.explosion.messenger.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J&\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\fH\u0086@\u00a2\u0006\u0004\b\u000e\u0010\u000fJ&\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\u0086@\u00a2\u0006\u0004\b\u0012\u0010\u000fJ&\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\fH\u0086@\u00a2\u0006\u0004\b\u0014\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/explosion/messenger/data/repository/AuthRepository;", "", "api", "Lcom/explosion/messenger/data/remote/ApiService;", "tokenManager", "Lcom/explosion/messenger/util/TokenManager;", "<init>", "(Lcom/explosion/messenger/data/remote/ApiService;Lcom/explosion/messenger/util/TokenManager;)V", "login", "Lkotlin/Result;", "Lcom/explosion/messenger/data/remote/LoginResponse;", "username", "", "password", "login-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verify2fa", "code", "verify2fa-0E7RQCE", "loginPasswordless", "loginPasswordless-0E7RQCE", "app_debug"})
public final class AuthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.data.remote.ApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.explosion.messenger.util.TokenManager tokenManager = null;
    
    @javax.inject.Inject()
    public AuthRepository(@org.jetbrains.annotations.NotNull()
    com.explosion.messenger.data.remote.ApiService api, @org.jetbrains.annotations.NotNull()
    com.explosion.messenger.util.TokenManager tokenManager) {
        super();
    }
}