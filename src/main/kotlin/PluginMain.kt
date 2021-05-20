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
import net.mamoe.mirai.message.data.toMessageChain
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

            // 帮助

            if (messageText == "帮助") {
                var toSay =
                    PlainText("Hibiki-Twitter-Announcer 操作指南").toMessageChain()
                group.sendMessage(toSay)
                delay(500L)
                toSay =
                    PlainText(
                        "查询<number>条官推 -> " +
                            "获取<number>条游戏王官方Twitter(@YuGiOh_OCG_Info)的最新Twitter," +
                            "其中<number>为阿拉伯数字"
                    ).toMessageChain()
                group.sendMessage(toSay)
                delay(500L)
                toSay =
                    PlainText(
                        "查询<twitterID>的<number>条推文 ->" +
                            "获取<number>条来自@<twitterID>的最新推文,其中<number>为阿拉伯数字,"
                    ).toMessageChain()
                // "<twitterID>为只包含英文/数字/下划线的标准TwitterID").toMessageChain()
                group.sendMessage(toSay)
                delay(500L)
                toSay =
                    PlainText(
                        "查询关于<object>的<number>条推文 ->" +
                            "获取包含<object>关键字的<number>条推文，" +
                            "其中<object>为任意UTF-8标准字符,<number>为阿拉伯数字"
                    ).toMessageChain()
                group.sendMessage(toSay)
                delay(500L)
                toSay =
                    PlainText(
                        "添加订阅 ->" +
                            "添加对游戏王官推的新推文自动推送\r\n"
                    ) +
                        PlainText(
                            "取消订阅 ->" +
                                "取消对游戏王官推的新推文自动推送"
                        )
                delay(500L)
                group.sendMessage(toSay)
                toSay =
                    PlainText(
                        "关注<username> ->" +
                            "添加对@<username>的新推文的自动转发，需要先添加订阅才能使用这个功能\r\n"
                    ) +
                        PlainText(
                            "取消关注<username> ->" +
                                "取消对@<username>的新推文的自动转发"
                        )
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
                            getTimelineAndSendMessage(
                                inquirerGroup = group,
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
                                maxCount = matches.map { it.groupValues[2] }.joinToString().toInt(),
                                target = "from:" + matches.map { it.groupValues[1] }.joinToString(),
                            )
                        }
                    }
                    patternAbout.matches(messageText) -> {
                        val matches = patternAbout.findAll(messageText)
                        GlobalScope.launch {
                            getTimelineAndSendMessage(
                                inquirerGroup = group,
                                startCount = 0,
                                maxCount = matches.map { it.groupValues[2] }.joinToString().toInt(),
                                target = matches.map { it.groupValues[1] }.joinToString(),
                            )
                        }
                    }
                }


            }

            // 自动推送
            if (messageText == "添加订阅") {
                if (PluginData.groups.contains(group.id)) {
                    group.sendMessage("已经添加过订阅了哦")
                } else {
                    PluginData.groups.add(group.id)
                    PluginData.listeningListByGroup[group.id] = mutableSetOf()
                    group.sendMessage("添加订阅成功")
                    PluginData.ifGroupListHasChanged = true
                }
            }
            val patternAddListener = Regex("^关注([a-zA-Z_]+)$")
            if (patternAddListener.matches(messageText)) {
                if (!PluginData.groups.contains(group.id)) {
                    group.sendMessage("还没有添加订阅呢，请先订阅再开始建立关注列表哦")
                } else {
                    val matches = patternAddListener.findAll(messageText)
                    val addingUserName = matches.map { it.groupValues[1] }.joinToString()
                    try {
                        val name = checkUserName(addingUserName)
                        PluginData.listeningListByGroup[group.id]!!.add(addingUserName)
                        PluginData.lastTweetID[addingUserName] = "0"
                        group.sendMessage("开始关注${name}的最新推文了哦")
                    } catch (e: Exception) {
                        PluginMain.logger.info("error: ${e.message}")
                        if (e.message == "No Such User")
                            group.sendMessage("哎呀，好像没有这个人呢，请仔细检查输入的用户名是否正确呢")
                    }

                }
            }

            val patternRemoveListener = Regex("^取消关注([a-zA-z_]+)$")
            if (patternRemoveListener.matches(messageText)) {
                if (!PluginData.groups.contains(group.id)) {
                    group.sendMessage("还没有添加订阅呢，清闲订阅再开始管理关注列表哦")
                } else {
                    val matches = patternRemoveListener.findAll(messageText)
                    val removingUserName = matches.map { it.groupValues[1] }.joinToString()
                    if (!PluginData.listeningListByGroup[group.id]!!.contains(removingUserName)) {
                        group.sendMessage("本来就没有关注@${removingUserName}呢")
                    } else {
                        group.sendMessage("以后不看@${removingUserName}了哦")
                        PluginData.listeningListByGroup[group.id]!!.remove(removingUserName)
                    }
                }
            }

            /*if (messageText == "取消全部订阅") {
                PluginData.listeningListByGroup[group.id]!!.clear()
            }*/

            if (messageText == "取消订阅") {
                if (PluginData.groups.contains(group.id)) {
                    PluginData.groups.remove(group.id)
                    group.sendMessage("呜呜 以后不说了哦")
                    PluginData.ifGroupListHasChanged = true
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