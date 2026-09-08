// Shared Compose Multiplatform root composable: resolves the port, owns the
// AgentStore, and renders the responsive shell. Platform calls
// MainComposeApp(port = ...).
//
// Provides live language (zh/en) + theme via CompositionLocalProvider so
// stringResource(Res.string.*) and MaterialTheme react to store.lang/theme.
package com.agent.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun MainComposeApp(port: AgentPort) {
    val scope = rememberCoroutineScope()
    val store = remember { AppStore(port, scope) }
    LaunchedEffect(Unit) { store.init() }

    // Push live language so composables pick up zh/en instantly.
    L10n_Current.lang = store.lang

    // Theme: system | light | dark (default system).
    val systemDark = isSystemInDarkTheme()
    val dark = when (store.theme) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    val scheme =
        if (dark) darkColorScheme(primary = Color(0xFF2F81F7))
        else lightColorScheme(primary = Color(0xFF1F6FEB))

    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize()) {
            ResponsiveShell(
                store = store,
                models = rememberModels(store),
                presets = rememberPresets(store),
                onSelect = { scope.launch { store.select(it) } },
                onCreate = { scope.launch { store.create() } },
                onRename = { scope.launch { store.rename(it) } },
                onDelete = { scope.launch { store.delete(it) } },
                onSend = { scope.launch { store.send(it) } },
                onSwitchModel = { scope.launch { store.switchModel(it) } },
                onSetPreset = { scope.launch { store.setPreset(it) } },
                onInterrupt = { scope.launch { store.interrupt() } },
                onCompact = { scope.launch { store.compact() } },
                onTheme = { store.theme = it },
                onLang = { store.lang = it },
            )
        }
    }
}

@Composable
private fun rememberModels(store: AppStore): List<String> {
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) { models = port_listModels(store) }
    return models
}

@Composable
private fun rememberPresets(store: AppStore): List<String> {
    var presets by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) { presets = port_listPresets(store) }
    return presets
}

private suspend fun port_listModels(store: AppStore): List<String> = store.listModels()
private suspend fun port_listPresets(store: AppStore): List<String> = store.listPresets()
