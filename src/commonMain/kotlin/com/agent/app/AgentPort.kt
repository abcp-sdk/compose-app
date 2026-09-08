// AgentPort: platform-agnostic agent backend interface (commonMain).
package com.agent.app

import kotlinx.coroutines.flow.Flow

data class ToolDisplay(val id: String, val name: String, val output: String)

data class MessageDisplay(
    val id: String,
    val role: String,
    val text: String,
    val reasoning: String,
    val tools: List<ToolDisplay>,
    val streaming: Boolean,
)

data class WatchEvent(val event: String, val params: Map<String, Any?>)

interface AgentPort {
    suspend fun health(): Boolean
    suspend fun listSessions(): List<String>
    suspend fun messages(id: String, limit: Int): List<MessageDisplay>
    suspend fun prompt(id: String, text: String)
    suspend fun watchSession(id: String): Flow<WatchEvent>
    suspend fun listModels(): List<String>
    suspend fun listPresets(): List<String>
    suspend fun switchModel(id: String, model: String)
    suspend fun setPreset(id: String, preset: String)
    suspend fun interrupt(id: String)
    suspend fun compact(id: String)
    suspend fun createSession(name: String?)
    suspend fun deleteSession(id: String)
    suspend fun renameSession(id: String, name: String)
}
