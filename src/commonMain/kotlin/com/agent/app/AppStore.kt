// AppStore: Compose-state holder bridging AgentPort to the UI. Mirrors the
// store semantics used by the webui/flutter clients (no polling; watchSession
// drives live updates).
package com.agent.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppStore(private val port: AgentPort, private val scope: CoroutineScope) {
    var sessions by mutableStateOf<List<String>>(emptyList())
    var activeId by mutableStateOf("")
    var messages by mutableStateOf<List<MessageDisplay>>(emptyList())
    var sending by mutableStateOf(false)
    var loading by mutableStateOf(false)

    var theme by mutableStateOf("system")
    var compact by mutableStateOf(false)
    var lang by mutableStateOf("zh")

    // ---- tool handlers (mirror webui/flutter): keep tool parts on messages ----
    fun ensureTool(id: String, name: String) {
        ensureStreaming()
        val last = messages.last()
        val existing = last.tools.any { it.id == id }
        if (existing) return
        val updated = last.copy(tools = last.tools + ToolDisplay(id, name, ""))
        messages = messages.dropLast(1) + updated
    }

    fun toolResult(id: String, output: String) {
        for (i in messages.indices.reversed()) {
            val m = messages[i]
            val idx = m.tools.indexOfFirst { it.id == id }
            if (idx >= 0) {
                val tools = m.tools.toMutableList()
                tools[idx] = tools[idx].copy(output = output)
                messages = messages.toMutableList().also {
                    it[i] = m.copy(tools = tools)
                }
                return
            }
        }
    }

    suspend fun init() {
        refresh()
    }

    suspend fun refresh() {
        sessions = port.listSessions()
    }

    suspend fun select(id: String) {
        activeId = id
        loading = true
        try {
            messages = port.messages(id, 50)
        } finally {
            loading = false
        }
        // Live stream: drive updates from watchSession.
        launchStream(id)
    }

    private fun launchStream(id: String) {
        scope.launch {
            port.watchSession(id).collect { ev ->
                when (val e = toAgentEvent(ev)) {
                    is AgentEvent.TextDelta -> appendDelta(e.id, e.text, false)
                    is AgentEvent.ReasoningDelta -> appendDelta(e.id, e.text, true)
                    is AgentEvent.ToolCall -> ensureTool(e.id, e.name)
                    is AgentEvent.ToolResult -> toolResult(e.id, e.output)
                    is AgentEvent.ToolError -> toolResult(e.id, e.output)
                    AgentEvent.TurnComplete -> finishStream()
                    is AgentEvent.Status -> if (e.type == "busy") sending = true else finishStream()
                    is AgentEvent.Error -> finishStream()
                }
            }
        }
    }

    fun appendDelta(pid: String, text: String, reasoning: Boolean) {
        ensureStreaming()
        val last = messages.last()
        val updated = if (reasoning) last.copy(reasoning = last.reasoning + text) else last.copy(text = last.text + text)
        messages = messages.dropLast(1) + updated
    }

    fun ensureStreaming() {
        if (messages.isNotEmpty() && messages.last().streaming) return
        messages = messages + MessageDisplay("__stream", "assistant", "", "", emptyList(), true)
        sending = true
    }

    fun finishStream() {
        sending = false
        messages = messages.filter { !it.streaming }
    }

    suspend fun send(text: String) {
        if (activeId.isEmpty()) return
        sending = true
        messages = messages + MessageDisplay("__user", "user", text, "", emptyList(), false)
        try {
            port.prompt(activeId, text)
        } catch (_: Exception) {
        }
    }

    suspend fun create() {
        port.createSession(null)
        refresh()
    }

    suspend fun rename(id: String) {
        port.renameSession(id, id)
        refresh()
    }

    suspend fun delete(id: String) {
        port.deleteSession(id)
        if (activeId == id) {
            activeId = ""
            messages = emptyList()
        }
        refresh()
    }

    suspend fun switchModel(m: String) = port.switchModel(activeId, m)
    suspend fun setPreset(p: String) = port.setPreset(activeId, p)
    suspend fun interrupt() = port.interrupt(activeId)
    suspend fun compact() = port.compact(activeId)
    suspend fun listModels(): List<String> = port.listModels()
    suspend fun listPresets(): List<String> = port.listPresets()
}
