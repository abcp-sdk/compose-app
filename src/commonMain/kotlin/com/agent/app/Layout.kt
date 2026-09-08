// Responsive layout for Compose Multiplatform, mirroring platform/web.
package com.agent.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Breakpoints shared with the other clients. */
const val COMPACT_BELOW = 600
const val WIDE_THRESHOLD = 1024

/** Width -> breakpoint kind. */
enum class Breakpoint { compact, medium, wide }

fun breakpointOf(widthPx: Int): Breakpoint = when {
    widthPx < COMPACT_BELOW -> Breakpoint.compact
    widthPx < WIDE_THRESHOLD -> Breakpoint.medium
    else -> Breakpoint.wide
}

fun sessionPanelWidth(listWidthPx: Int): Dp =
    (listWidthPx * 0.30f).coerceIn(240f, 360f).dp

@Composable
fun ResponsiveShell(
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val bp = breakpointOf(maxWidth.value.toInt())
        store.compact = bp == Breakpoint.compact
        when (bp) {
            Breakpoint.compact -> CompactShell(
                store, models, presets, onSelect, onCreate, onRename, onDelete, onSend,
                onSwitchModel, onSetPreset, onInterrupt, onCompact, onTheme, onLang,
            )
            else -> WideShell(
                store, models, presets, onSelect, onCreate, onRename, onDelete, onSend,
                onSwitchModel, onSetPreset, onInterrupt, onCompact, onTheme, onLang,
            )
        }
    }
}
