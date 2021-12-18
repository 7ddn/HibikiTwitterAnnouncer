package pluginController

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info
import twitter.checkNewTweet
import commandHandler.messageEventHandler
import kotlin.random.Random

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.sddn.hibiki.twitter",
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
    lateinit var bot: Bot

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //logger.info{PluginConfig.APIs["recent"].toString()}

        PluginConfig.reload()

        PluginData.reload()

        var lastMessage: MessageChain = PlainText("").serializeToMiraiCode().deserializeMiraiCode()
        var repeatingCount = 1
        PluginData.ifGroupListHasChanged = true
        //PluginConfig.APIs["usersBy"] = "https://api.twitter.com/2/users/by"
        //Logger.getLogger(OkHttpClient.javaClass.name).level = Level.FINE


        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val messageText = message.contentToString()

            //logger.info("last code = ${lastMessage.serializeToMiraiCode()} & " +
            //    "this code = ${message.serializeToMiraiCode()}")




            messageEventHandler(messageText)

            // 复读功能
            // TODO: 做成独立插件
            // 开关
            when {
                messageText.startsWith("不准复读") -> {
                    PluginData.repeatProbability = -1
                    group.sendMessage("呜呜")
                }
                messageText.startsWith("可以复读吗") -> {
                    val toSay: String = when (PluginData.repeatProbability) {
                        -1 -> "不可以复读，哭哭"
                        else -> "可以复读，概率是${(PluginData.repeatProbability).toDouble() / 100.0}"
                    }
                    group.sendMessage(toSay)
                }
                messageText.startsWith("可以复读") -> {
                    PluginData.repeatProbability = 5
                    group.sendMessage("好耶")

                }
            }


            // 跟读
            if (message.serializeToMiraiCode() == lastMessage.serializeToMiraiCode()) {
                repeatingCount++
                if (repeatingCount == 2 && PluginData.repeatProbability > 0) {
                    group.sendMessage(message)
                }

            } else {
                repeatingCount = 1
                // 随机复读
                if (Random.nextInt(100) < PluginData.repeatProbability) {
                    group.sendMessage(message)
                }
            }

            lastMessage = message
            // logger.info(message.contentToString().length.toString())


            delay(100L)
        }

        PluginMain.launch {

            while (true) {
                try {
                    bot = Bot.instances[0]
                    bot
                    break
                } catch (e: Exception) {
                    logger.info("error : ${e.message}")
                    delay(1000)
                }
            }

            checkNewTweet(bot)
        }
    }

}