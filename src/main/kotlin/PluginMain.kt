package org.sddn.hibiki.plugin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.info
import kotlin.random.Random as Random

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.sddn.hibiki",
        name = "HibikiTwitterAnnouncer",
        version = "0.1.0"
    ) {
        author("七度")

        info(
            """
            转发官推内容到QQ
        """.trimIndent()
        )

        // author 和 info 可以删除.
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //logger.info{PluginConfig.APIs["recent"].toString()}

        PluginConfig.reload()

        PluginData.reload()

        var lastMessage: MessageChain = PlainText("").serializeToMiraiCode().deserializeMiraiCode()
        var repeatingCount = 1
        //Logger.getLogger(OkHttpClient.javaClass.name).level = Level.FINE

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val messageText = message.contentToString()

            //logger.info("last code = ${lastMessage.serializeToMiraiCode()} & " +
            //    "this code = ${message.serializeToMiraiCode()}")

            // 查询官推
            if (messageText.startsWith("查询最新官推")) {
                GlobalScope.launch {
                    getTimeline(group)
                }
            }

            // 复读功能
            // TODO: 做成独立插件
            // 跟读
            if (message.serializeToMiraiCode() == lastMessage.serializeToMiraiCode()) {
                repeatingCount++
                if (repeatingCount == 2) {
                    group.sendMessage(message)
                }

            } else {
                repeatingCount = 1
                // 随机复读
                if (Random.nextInt(100)<20) {
                    group.sendMessage(message)
                }
            }

            lastMessage = message




            delay(100L)
        }
    }
}