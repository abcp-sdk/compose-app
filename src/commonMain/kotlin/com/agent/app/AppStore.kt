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
                when (ev.event) {
                    "text-delta" -> appendDelta(ev.params["id"].toString(), ev.params["text"].toString(), false)
                    "reasoning-delta" -> appendDelta(ev.params["id"].toString(), ev.params["text"].toString(), true)
                    "tool-call" -> {}
                    "turn-complete" -> finishStream()
                    "status" -> if (ev.params["type"] == "busy") sending = true else finishStream()
                    "error" -> finishStream()
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
}
