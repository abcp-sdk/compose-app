// Desktop (JVM) entrypoint. Builds the AgentPort over agent-sdk-kotlin and
// launches the shared Compose UI.
package com.agent.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.agent.AgentClient
import com.connectrpc.getOrThrow
import com.agent.v1.Message
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** Wraps the JVM AgentClient into the common AgentPort. */
class JvmAgentPort(
    baseUrl: String,
    token: String,
) : AgentPort {
    private val client = AgentClient(baseUrl = baseUrl, token = token)
    private val agent get() = client.agent

    private fun <T> rx(channel: ReceiveChannel<T>) = flow {
        for (item in channel) emit(item)
    }

    override suspend fun health(): Boolean = true
    override suspend fun listSessions(): List<String> {
        val r = agent.listSessions(com.agent.v1.listSessionsRequest { })
        return r.getOrThrow().sessionsList.map { it.name }
    }
    override suspend fun messages(id: String, limit: Int): List<MessageDisplay> {
        val r = agent.listMessages(com.agent.v1.listMessagesRequest { this.id = id; this.limit = limit })
        return r.getOrThrow().messagesList.map { toDisplay(it) }
    }
    override suspend fun prompt(id: String, text: String) {
        val stream = agent.prompt()
        stream.sendAndClose(com.agent.v1.promptRequest { this.id = id; prompt = text })
        for (m in stream.responseChannel()) { if (m.event == "accepted") break }
    }
    override suspend fun watchSession(id: String): Flow<WatchEvent> {
        val stream = agent.watchSession()
        stream.sendAndClose(com.agent.v1.watchSessionRequest { this.id = id })
        return rx(stream.responseChannel()).map { ev ->
            WatchEvent(ev.event, ev.params.fields.entries.associate { (k, v) -> k to valueToAny(v) })
        }
    }
    override suspend fun listModels(): List<String> {
        val r = agent.listModels(com.agent.v1.listModelsRequest { })
        return r.getOrThrow().modelsList.map { it.id }
    }
    override suspend fun listPresets(): List<String> {
        val r = agent.listPresets(com.agent.v1.listPresetsRequest { })
        return r.getOrThrow().presetsList.map { it.id }
    }
    override suspend fun switchModel(id: String, model: String) {
        agent.setModel(com.agent.v1.setModelRequest { this.id = id; this.model = model })
    }
    override suspend fun setPreset(id: String, preset: String) {
        agent.updateSettings(com.agent.v1.updateSettingsRequest { this.id = id; this.preset = preset })
    }
    override suspend fun interrupt(id: String) {
        agent.interrupt(com.agent.v1.interruptRequest { this.id = id })
    }
    override suspend fun compact(id: String) {
        agent.compact(com.agent.v1.compactRequest { this.id = id })
    }
    override suspend fun createSession(name: String?) {
        agent.createSession(com.agent.v1.createSessionRequest { this.name = name ?: "" })
    }
    override suspend fun deleteSession(id: String) {
        agent.deleteSession(com.agent.v1.deleteSessionRequest { this.id = id })
    }
    override suspend fun renameSession(id: String, name: String) {
        agent.rename(com.agent.v1.renameRequest { this.id = id; this.name = name })
    }
}

private fun valueToAny(v: com.google.protobuf.Value): Any? = when {
    v.hasStringValue() -> v.stringValue
    v.hasNumberValue() -> v.numberValue
    v.hasBoolValue() -> v.boolValue
    v.hasStructValue() -> v.structValue.fields.entries.associate { (k, x) -> k to valueToAny(x) }
    v.hasListValue() -> v.listValue.valuesList.map { valueToAny(it) }
    else -> null
}

fun toDisplay(m: Message): MessageDisplay {
    var text = ""; var reasoning = ""; val tools = mutableListOf<ToolDisplay>()
    for (p in m.partsList) {
        when (p.type) {
            "text" -> text += p.data
            "reasoning" -> reasoning += p.data
            "tool" -> tools += ToolDisplay(p.id, p.data, "")
        }
    }
    return MessageDisplay(m.id, m.role, text, reasoning, tools, false)
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Agent") {
        MainComposeApp(port = rememberJvmPort())
    }
}

@Composable
fun rememberJvmPort(): AgentPort {
    val base = System.getenv("AGENT_BASE_URL") ?: "https://agent.example.com"
    val token = System.getenv("AGENT_TOKEN") ?: ""
    return androidx.compose.runtime.remember { JvmAgentPort(base, token) }
}
