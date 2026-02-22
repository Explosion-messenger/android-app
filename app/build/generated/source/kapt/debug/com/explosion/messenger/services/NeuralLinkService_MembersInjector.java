package com.explosion.messenger.services;

import com.explosion.messenger.data.remote.NeuralWebSocketManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class NeuralLinkService_MembersInjector implements MembersInjector<NeuralLinkService> {
  private final Provider<NeuralWebSocketManager> wsManagerProvider;

  private NeuralLinkService_MembersInjector(Provider<NeuralWebSocketManager> wsManagerProvider) {
    this.wsManagerProvider = wsManagerProvider;
  }

  @Override
  public void injectMembers(NeuralLinkService instance) {
    injectWsManager(instance, wsManagerProvider.get());
  }

  public static MembersInjector<NeuralLinkService> create(
      Provider<NeuralWebSocketManager> wsManagerProvider) {
    return new NeuralLinkService_MembersInjector(wsManagerProvider);
  }

  @InjectedFieldSignature("com.explosion.messenger.services.NeuralLinkService.wsManager")
  public static void injectWsManager(NeuralLinkService instance, NeuralWebSocketManager wsManager) {
    instance.wsManager = wsManager;
  }
}
