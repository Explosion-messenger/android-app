package com.explosion.messenger.ui.screens.login;

import com.explosion.messenger.data.remote.ApiService;
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<ApiService> apiProvider;

  private RegisterViewModel_Factory(Provider<ApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(apiProvider.get());
  }

  public static RegisterViewModel_Factory create(Provider<ApiService> apiProvider) {
    return new RegisterViewModel_Factory(apiProvider);
  }

  public static RegisterViewModel newInstance(ApiService api) {
    return new RegisterViewModel(api);
  }
}
