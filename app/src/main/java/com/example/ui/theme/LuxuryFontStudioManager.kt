package com.example.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.data.FontSettingsDataStore
import com.example.data.dataStore
import com.example.model.LuxuryFontConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import androidx.compose.ui.text.font.FontFamily

object LuxuryFontStudioManager {
    private val _fontConfig = MutableStateFlow(LuxuryFontConfig())
    val fontConfig: StateFlow<LuxuryFontConfig> = _fontConfig

    fun init(context: Context) {
        GlobalScope.launch {
            context.dataStore.data.map { preferences ->
                preferences[FontSettingsDataStore.FONT_CONFIG]?.let {
                    try { Json.decodeFromString<LuxuryFontConfig>(it) } catch(e: Exception) { LuxuryFontConfig() }
                } ?: LuxuryFontConfig()
            }.collect {
                _fontConfig.value = it
            }
        }
    }

    fun updateConfig(context: Context, config: LuxuryFontConfig) {
        GlobalScope.launch {
            context.dataStore.edit { preferences ->
                preferences[FontSettingsDataStore.FONT_CONFIG] = Json.encodeToString(config)
            }
        }
    }

    fun resetConfig(context: Context) {
        updateConfig(context, LuxuryFontConfig())
    }

    fun getFontFamily(name: String) = AvailableFontFamilies[name] ?: FontFamily.Default
}
