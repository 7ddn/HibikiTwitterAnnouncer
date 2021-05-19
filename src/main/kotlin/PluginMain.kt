package org.sddn.hibiki.plugin

import kotlinx.coroutines.GlobalScope
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
import kotlin.random.Random

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
    lateinit var bot : Bot

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

            // 帮助

            if (messageText == "帮助") {
                val toSay =
                    PlainText("Hibiki-Twitter-Announcer 操作指南\n\r") +
                    PlainText("查询<number>条官推 -> " +
                        "获取<number>条游戏王官方Twitter(@YuGiOh_OCG_Info)的最新Twitter,其中<number>为阿拉伯数字\n\r") +
                    PlainText("查询最新官推 -> " +
                        "获取游戏王官方Twitter(@YuGiOh_OCG_Info)的一条最新Twitter\n\r") +
                    PlainText( "查询<twitterID>的<number>条推文 ->" +
                        "获取<number>条来自@<twitterID>的最新推文,其中<number>为阿拉伯数字," +
                        "<twitterID>为只包含英文/数字/下划线的标准TwitterID\n\r") +
                    PlainText("查询关于<object>的<number>条推文 ->" +
                        "获取包含<object>关键字的<number>条推文，其中<object>为任意UTF-8标准字符,<number>为阿拉伯数字\n\r") +
                    PlainText("添加订阅 ->"+
                        "添加对游戏王官推的新推文自动推送\r\n") +
                    PlainText("取消订阅 ->" +
                        "取消对游戏王官推的新推文自动推送")
                group.sendMessage(toSay)
            }

            // 查询官推
            if (messageText.startsWith("查询")) {
                val patternYGO = Regex("查询\\d+条官推")
                val patternUser = Regex("^查询([0-9a-zA-Z_]+)的(\\d+)条推文$")
                val patternAbout = Regex("^查询关于(.+)的(\\d+)条推文$")
                when {
                    messageText == "查询最新官推" -> {
                        GlobalScope.launch {
                            getTimelineAndSendMessage(group)
                        }
                    }
                    patternYGO.matches(messageText) -> {
                        GlobalScope.launch {
                            getTimelineAndSendMessage(inquirerGroup = group,
                                startCount = 0,
                                maxCount = Regex("\\d+").find(messageText)?.value?.toInt() ?: 1
                            )
                        }
                    }
                    patternUser.matches(messageText) -> {
                        val matches = patternUser.findAll(messageText)
                        GlobalScope.launch {
                            getTimelineAndSendMessage(
                                inquirerGroup = group,
                                startCount = 0,
                                maxCount = matches.map{it.groupValues[2]}.joinToString().toInt(),
                                target = "from:" + matches.map{it.groupValues[1]}.joinToString(),
                            )
                        }
                    }
                    patternAbout.matches(messageText) -> {
                        val matches = patternAbout.findAll(messageText)
                        GlobalScope.launch {
                            getTimelineAndSendMessage(
                                inquirerGroup = group,
                                startCount = 0,
                                maxCount = matches.map{it.groupValues[2]}.joinToString().toInt(),
                                target = matches.map{it.groupValues[1]}.joinToString(),
                            )
                        }
                    }
                }


            }

            // 自动推送
            if (messageText == "添加订阅") {
                if (PluginData.groups.contains(group.id)){
                    group.sendMessage("已经添加过订阅了哦")
                } else {
                    PluginData.groups.add(group.id)
                    group.sendMessage("添加订阅成功")
                }
            }

            if (messageText == "取消订阅") {
                if (PluginData.groups.contains(group.id)){
                    PluginData.groups.remove(group.id)
                    group.sendMessage("呜呜 以后不说了哦")
                } else {
                    group.sendMessage("哎呀，本来就没有订阅呢")
                }
            }

            // 复读功能
            // TODO: 做成独立插件
            // 开关
            when {
                messageText.startsWith("不准复读") -> {
                    PluginData.repeatProbability = -1
                    group.sendMessage("呜呜")
                }
                messageText.startsWith("可以复读吗") -> {
                    val toSay:String = when (PluginData.repeatProbability){
                        -1 -> "不可以复读，哭哭"
                        else -> "可以复读，概率是${(PluginData.repeatProbability).toDouble()/100.0}"
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
                if (Random.nextInt(100)< PluginData.repeatProbability) {
                    group.sendMessage(message)
                }
            }

            lastMessage = message


            delay(100L)
        }

        PluginMain.launch {

            while (true){
                try{
                    bot = Bot.instances[0]
                    bot
                    break
                } catch (e:Exception){
                    logger.info(e.message)
                    delay(1000)
                }
            }

            checkNewTweet(bot)
        }
    }
}