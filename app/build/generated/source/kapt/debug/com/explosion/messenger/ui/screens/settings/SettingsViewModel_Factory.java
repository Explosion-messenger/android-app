package com.explosion.messenger.ui.screens.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<ApiService> apiProvider;

  private SettingsViewModel_Factory(Provider<ApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(apiProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<ApiService> apiProvider) {
    return new SettingsViewModel_Factory(apiProvider);
  }

  public static SettingsViewModel newInstance(ApiService api) {
    return new SettingsViewModel(api);
  }
}
