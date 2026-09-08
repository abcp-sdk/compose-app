// Strong-typed i18n catalog for Compose Multiplatform (zh/en). Reads live from
// store.lang so switching is instant. Zero casts — every key is a typed field.
package com.agent.app

data class L10nStrings(
    val appName: String,
    val sessions: String,
    val newSession: String,
    val noSessions: String,
    val rename: String,
    val delete: String,
    val model: String,
    val preset: String,
    val stop: String,
    val compact: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val send: String,
    val messagePlaceholder: String,
    val loading: String,
    val noMessages: String,
    val selectOrCreate: String,
    val thinking: String,
    val interrupt: String,
    val error: String,
)

object L10n {
    val zh = L10nStrings(
        appName = "Agent",
        sessions = "会话",
        newSession = "新会话",
        noSessions = "暂无会话",
        rename = "重命名",
        delete = "删除",
        model = "模型",
        preset = "预设",
        stop = "停止",
        compact = "压缩",
        theme = "主题",
        themeSystem = "跟随系统",
        themeLight = "浅色",
        themeDark = "深色",
        send = "发送",
        messagePlaceholder = "输入消息…",
        loading = "加载中…",
        noMessages = "暂无消息，发送一条开始对话。",
        selectOrCreate = "选择一个会话或创建一个新的",
        thinking = "思考中…",
        interrupt = "中断",
        error = "出错了",
    )

    val en = L10nStrings(
        appName = "Agent",
        sessions = "Sessions",
        newSession = "New session",
        noSessions = "No sessions",
        rename = "Rename",
        delete = "Delete",
        model = "Model",
        preset = "Preset",
        stop = "Stop",
        compact = "Compact",
        theme = "Theme",
        themeSystem = "System",
        themeLight = "Light",
        themeDark = "Dark",
        send = "Send",
        messagePlaceholder = "Type a message…",
        loading = "Loading…",
        noMessages = "No messages yet. Send one to start.",
        selectOrCreate = "Select a session or create a new one",
        thinking = "Thinking…",
        interrupt = "Interrupt",
        error = "Error",
    )

    fun strings(lang: String): L10nStrings = if (lang == "en") en else zh
}

/** Live language holder refreshed by MainComposeApp when store.lang changes. */
object L10n_Current {
    var lang: String = "zh"
}
