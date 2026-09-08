// Shared Compose UI: session list, chat, adaptive shell (mobile < 600 -> drawer / desktop -> split).
// Locale-aware via Compose Resources (zh/en). Tool parts render as small cards.
package com.agent.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.agent.agent_compose_app.generated.resources.Res
import com.agent.agent_compose_app.generated.resources.appName
import com.agent.agent_compose_app.generated.resources.compact
import com.agent.agent_compose_app.generated.resources.delete
import com.agent.agent_compose_app.generated.resources.error
import com.agent.agent_compose_app.generated.resources.interrupt
import com.agent.agent_compose_app.generated.resources.loading
import com.agent.agent_compose_app.generated.resources.messagePlaceholder
import com.agent.agent_compose_app.generated.resources.model
import com.agent.agent_compose_app.generated.resources.newSession
import com.agent.agent_compose_app.generated.resources.noMessages
import com.agent.agent_compose_app.generated.resources.noSessions
import com.agent.agent_compose_app.generated.resources.preset
import com.agent.agent_compose_app.generated.resources.rename
import com.agent.agent_compose_app.generated.resources.send
import com.agent.agent_compose_app.generated.resources.selectOrCreate
import com.agent.agent_compose_app.generated.resources.sessions
import com.agent.agent_compose_app.generated.resources.stop
import com.agent.agent_compose_app.generated.resources.theme
import com.agent.agent_compose_app.generated.resources.thinking
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionList(
    sessions: List<String>,
    activeId: String,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Button(onClick = onCreate, Modifier.fillMaxWidth()) {
            Text("+ " + stringResource(Res.string.newSession))
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(sessions) { s ->
                Card(onClick = { onSelect(s) }) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically,
                    ) {
                        Text(s, style = MaterialTheme.typography.bodyLarge)
                        Row {
                            TextButton(onClick = { onRename(s) }) { Text(stringResource(Res.string.rename)) }
                            TextButton(onClick = { onDelete(s) }) { Text("×") }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ChatPane(
    messages: List<MessageDisplay>,
    sending: Boolean,
    onSend: (String) -> Unit,
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { m -> MessageBubble(m) }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(Res.string.messagePlaceholder)) },
            )
            Spacer(Modifier.width(8.dp))
            Button(enabled = !sending, onClick = { onSend(input.text); input = TextFieldValue("") }) {
                Text(stringResource(Res.string.send))
            }
        }
    }
}

@Composable
fun MessageBubble(m: MessageDisplay) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (m.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.widthIn(max = 640.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            for (t in m.tools) {
                Card(Modifier.padding(vertical = 2.dp)) {
                    Column(Modifier.padding(6.dp)) {
                        Text("⚙ ${t.name}", style = MaterialTheme.typography.labelMedium)
                        if (t.output.isNotEmpty()) Text(t.output, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (m.reasoning.isNotEmpty()) {
                Text(m.reasoning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            if (m.text.isNotEmpty()) Text(m.text)
            if (m.streaming) Text("…")
        }
    }
}

@Composable
fun SettingsBar(
    store: AppStore,
    models: List<String>,
    presets: List<String>,
    onSwitchModel: (String) -> Unit,
    onSetPreset: (String) -> Unit,
    onInterrupt: () -> Unit,
    onCompact: () -> Unit,
    onTheme: (String) -> Unit,
    onLang: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DropdownMenuButton(label = stringResource(Res.string.model), options = models, onSelect = onSwitchModel)
        Spacer(Modifier.width(8.dp))
        DropdownMenuButton(label = stringResource(Res.string.preset), options = presets, onSelect = onSetPreset)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onInterrupt) { Text(stringResource(Res.string.stop)) }
        IconButton(onClick = onCompact) { Text(stringResource(Res.string.compact)) }
        IconButton(onClick = { onTheme("light") }) { Text("☀") }
        IconButton(onClick = { onTheme("dark") }) { Text("🌙") }
        DropdownMenuButton(label = store.lang, options = listOf("zh", "en"), onSelect = onLang)
    }
}

@Composable
private fun DropdownMenuButton(label: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text(label) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = {
                    open = false
                    onSelect(opt)
                })
            }
        }
    }
}

// ---- compact (phone) shell: single column ----
@Composable
fun CompactShell(
    store: AppStore,
    models: List<String>,
    presets: List<String>,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSend: (String) -> Unit,
    onSwitchModel: (String) -> Unit,
    onSetPreset: (String) -> Unit,
    onInterrupt: () -> Unit,
    onCompact: () -> Unit,
    onTheme: (String) -> Unit,
    onLang: (String) -> Unit,
) {
    androidx.compose.material3.Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (store.activeId.isEmpty()) {
                SessionList(store.sessions, store.activeId, onSelect, onCreate, onRename, onDelete)
            } else {
                Column(Modifier.fillMaxSize()) {
                    SettingsBar(store, models, presets, onSwitchModel, onSetPreset, onInterrupt, onCompact, onTheme, onLang)
                    ChatPane(store.messages, store.sending, onSend)
                }
            }
        }
    }
}

// ---- wide (desktop) shell: split ----
@Composable
fun WideShell(
    store: AppStore,
    models: List<String>,
    presets: List<String>,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSend: (String) -> Unit,
    onSwitchModel: (String) -> Unit,
    onSetPreset: (String) -> Unit,
    onInterrupt: () -> Unit,
    onCompact: () -> Unit,
    onTheme: (String) -> Unit,
    onLang: (String) -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        Box(Modifier.width(sessionPanelWidth(600))) {
            SessionList(store.sessions, store.activeId, onSelect, onCreate, onRename, onDelete)
        }
        VerticalDivider()
        Box(Modifier.weight(1f)) {
            if (store.activeId.isEmpty()) {
                Column(Modifier.padding(16.dp)) { Text(stringResource(Res.string.selectOrCreate)) }
            } else {
                Column(Modifier.fillMaxSize()) {
                    SettingsBar(store, models, presets, onSwitchModel, onSetPreset, onInterrupt, onCompact, onTheme, onLang)
                    ChatPane(store.messages, store.sending, onSend)
                }
            }
        }
    }
}
