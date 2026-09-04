package ru.bitvibe.waggy.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @ThemeDataStore
    fun provideThemeDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.themeDataStore
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ThemeDataStore