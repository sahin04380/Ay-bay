package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hisab_settings")

class ThemePreferences(context: Context) {
  private val appContext = context.applicationContext
  private val themeKey = stringPreferencesKey("theme_mode")

  val themeMode: Flow<ThemeMode> = appContext.dataStore.data.map { prefs ->
    when (prefs[themeKey]) {
      ThemeMode.LIGHT.name -> ThemeMode.LIGHT
      ThemeMode.DARK.name -> ThemeMode.DARK
      else -> ThemeMode.SYSTEM
    }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    appContext.dataStore.edit { prefs ->
      prefs[themeKey] = mode.name
    }
  }
}
