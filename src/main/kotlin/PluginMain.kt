package org.sddn.hibiki.plugin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.info
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.sddn.hibiki",
        name = "HibikiTwitterAnnouncer",
        version = "0.1.0"
    ) {
        author("七度")

        info("""
            转发官推内容到QQ
        """.trimIndent())

        // author 和 info 可以删除.
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        PluginConfig.reload()

        PluginData.reload()

        var lastMessage = ""
        var repeatingSwitch = true
        Logger.getLogger(OkHttpClient.javaClass.name).level = Level.FINE

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val messageText = message.contentToString()
            if (messageText.startsWith("查询最新官推")) {
                GlobalScope.launch {
                    getTimeline(group)
                }
            }
            repeatingSwitch = if (messageText == lastMessage && repeatingSwitch){
                group.sendMessage(messageText.toPlainText())
                false
            } else true

            lastMessage = messageText

            delay(100L)
        }
    }
}