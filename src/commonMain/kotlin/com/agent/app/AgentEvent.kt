// Strong-typed agent stream events (sealed). The raw `WatchEvent` (event +
// Map params) is coerced to exactly one of these at the SDK boundary; UI/store
// only ever sees typed variants — zero casts.
package com.agent.app

sealed class AgentEvent {
    data class TextDelta(val id: String, val text: String) : AgentEvent()
    data class ReasoningDelta(val id: String, val text: String) : AgentEvent()
    data class ToolCall(val id: String, val name: String) : AgentEvent()
    data class ToolResult(val id: String, val output: String) : AgentEvent()
    data class ToolError(val id: String, val output: String) : AgentEvent()
    data object TurnComplete : AgentEvent()
    data class Status(val type: String) : AgentEvent()
    data class Error(val message: String) : AgentEvent()
}

fun toAgentEvent(ev: WatchEvent): AgentEvent {
    val p = ev.params
    fun s(v: Any?): String = v?.toString() ?: ""
    return when (ev.event) {
        "text-delta" -> AgentEvent.TextDelta(s(p["id"]), s(p["text"]))
        "reasoning-delta" -> AgentEvent.ReasoningDelta(s(p["id"]), s(p["text"]))
        "tool-call" -> AgentEvent.ToolCall(
            s(p["toolCallId"] ?: p["id"]),
            s(p["toolName"] ?: p["name"] ?: "tool"),
        )
        "tool-result" -> AgentEvent.ToolResult(
            s(p["toolCallId"] ?: p["id"]),
            s(p["formatted"] ?: p["output"] ?: p["result"]),
        )
        "tool-error" -> AgentEvent.ToolError(
            s(p["toolCallId"] ?: p["id"]),
            s(p["formatted"] ?: p["output"] ?: p["error"]),
        )
        "turn-complete" -> AgentEvent.TurnComplete
        "status" -> AgentEvent.Status(s(p["type"]))
        "error", "provider-error" -> AgentEvent.Error(s(p["message"] ?: p["error"]))
        else -> AgentEvent.Status(s(p["type"]).ifEmpty { "idle" })
    }
}
