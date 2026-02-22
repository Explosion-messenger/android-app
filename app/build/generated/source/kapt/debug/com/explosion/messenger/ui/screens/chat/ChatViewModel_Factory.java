package com.explosion.messenger.ui.screens.chat;

import com.explosion.messenger.data.remote.ApiService;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<ApiService> apiProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  private ChatViewModel_Factory(Provider<ApiService> apiProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.apiProvider = apiProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(apiProvider.get(), tokenManagerProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<ApiService> apiProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new ChatViewModel_Factory(apiProvider, tokenManagerProvider);
  }

  public static ChatViewModel newInstance(ApiService api, TokenManager tokenManager) {
    return new ChatViewModel(api, tokenManager);
  }
}
