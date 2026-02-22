package com.explosion.messenger.data.remote;

import com.explosion.messenger.util.NotificationHelper;
import com.explosion.messenger.util.TokenManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NeuralWebSocketManager_Factory implements Factory<NeuralWebSocketManager> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  private final Provider<Json> jsonProvider;

  private NeuralWebSocketManager_Factory(Provider<OkHttpClient> clientProvider,
      Provider<TokenManager> tokenManagerProvider,
      Provider<NotificationHelper> notificationHelperProvider, Provider<Json> jsonProvider) {
    this.clientProvider = clientProvider;
    this.tokenManagerProvider = tokenManagerProvider;
    this.notificationHelperProvider = notificationHelperProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public NeuralWebSocketManager get() {
    return newInstance(clientProvider.get(), tokenManagerProvider.get(), notificationHelperProvider.get(), jsonProvider.get());
  }

  public static NeuralWebSocketManager_Factory create(Provider<OkHttpClient> clientProvider,
      Provider<TokenManager> tokenManagerProvider,
      Provider<NotificationHelper> notificationHelperProvider, Provider<Json> jsonProvider) {
    return new NeuralWebSocketManager_Factory(clientProvider, tokenManagerProvider, notificationHelperProvider, jsonProvider);
  }

  public static NeuralWebSocketManager newInstance(OkHttpClient client, TokenManager tokenManager,
      NotificationHelper notificationHelper, Json json) {
    return new NeuralWebSocketManager(client, tokenManager, notificationHelper, json);
  }
}
