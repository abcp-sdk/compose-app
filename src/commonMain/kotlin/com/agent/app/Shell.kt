// Shared Compose Multiplatform root composable: resolves the port, owns the
// AgentStore, and renders the responsive shell (drawer on phones, split on
// desktop). Platform calls MainComposeApp(port = ...).
package com.agent.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun MainComposeApp(port: AgentPort) {
    val scope = rememberCoroutineScope()
    val store = remember { AppStore(port, scope) }
    LaunchedEffect(Unit) { store.init() }

    Box(Modifier.fillMaxSize()) {
        ResponsiveShell(
            store = store,
            onSelect = { scope.launch { store.select(it) } },
            onCreate = { scope.launch { store.create() } },
            onRename = { scope.launch { store.rename(it) } },
            onDelete = { scope.launch { store.delete(it) } },
            onSend = { scope.launch { store.send(it) } },
        )
    }
}
