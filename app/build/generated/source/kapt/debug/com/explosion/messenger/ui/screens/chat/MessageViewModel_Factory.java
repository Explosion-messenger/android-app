package com.explosion.messenger.ui.screens.chat;

import com.explosion.messenger.data.remote.ApiService;
import com.explosion.messenger.data.remote.NeuralWebSocketManager;
import com.explosion.messenger.util.TokenManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MessageViewModel_Factory implements Factory<MessageViewModel> {
  private final Provider<ApiService> apiProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<NeuralWebSocketManager> webSocketManagerProvider;

  private MessageViewModel_Factory(Provider<ApiService> apiProvider,
      Provider<TokenManager> tokenManagerProvider,
      Provider<NeuralWebSocketManager> webSocketManagerProvider) {
    this.apiProvider = apiProvider;
    this.tokenManagerProvider = tokenManagerProvider;
    this.webSocketManagerProvider = webSocketManagerProvider;
  }

  @Override
  public MessageViewModel get() {
    return newInstance(apiProvider.get(), tokenManagerProvider.get(), webSocketManagerProvider.get());
  }

  public static MessageViewModel_Factory create(Provider<ApiService> apiProvider,
      Provider<TokenManager> tokenManagerProvider,
      Provider<NeuralWebSocketManager> webSocketManagerProvider) {
    return new MessageViewModel_Factory(apiProvider, tokenManagerProvider, webSocketManagerProvider);
  }

  public static MessageViewModel newInstance(ApiService api, TokenManager tokenManager,
      NeuralWebSocketManager webSocketManager) {
    return new MessageViewModel(api, tokenManager, webSocketManager);
  }
}
