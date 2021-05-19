package org.sddn.hibiki.plugin

import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.net.URL

suspend fun checkNewTweet(bot: Bot) {
    val group = mutableSetOf<Group>()
    lateinit var targetGroup: Group
    while (true) {
        if (!PluginData.groups.isNullOrEmpty()) break
        delay(10000L)
    }
    PluginData.groups.forEach {
        //PluginMain.logger.info(it.toString())
        while (true) {
            try {
                targetGroup = bot.getGroup(it)!!
                targetGroup
                break
            } catch (e: Exception) {
                delay(1000)
            }
        }
        group.add(targetGroup)
    }
    while (true) {
        PluginMain.logger.info("检测开始")

        if (!PluginConfig.newCardAnnouncerSwitches) {
            continue
        }
        if (PluginData.groups.isEmpty()) {
            continue
        }
        try {
            val newestTweets = getNewestTweet(
                //"from:hibikiprprpr"
            ) ?: throw Exception("Fail on getting newest Tweet")
            val data = newestTweets.getJSONArray("data").getJSONObject(0)
            val newestText = data.getString("text")
            val newestTweetID = data.getString("id")
            if (newestTweetID == PluginData.lastOCGTweetID) {
                PluginMain.logger.info("暂时没有更新,本轮检测结束")
                delay(60000L)
                continue
            }
            PluginData.lastOCGTweetID = newestTweetID
            //PluginMain.logger.info("Now trying to get mediaUrls")
            val mediaUrls = if (data.containsKey("attachments")) {
                getMediaUrlsFromKeys(
                    tweetMedia = newestTweets.getJSONObject("includes").getJSONArray("media"),
                    mediaKeys = data.getJSONObject("attachments").getJSONArray("media_keys")
                )
            } else null
            //PluginMain.logger.info("Now end getting mediaUrls")
            var toSay = buildMessageChain { }
            if ("null" != newestText) {
                toSay += newestText.toPlainText()
                //inquirerGroup.sendMessage(newestText.toPlainText())
            }
            if (!mediaUrls.isNullOrEmpty()) {
                PluginMain.logger.info("有${mediaUrls.size}张图片")
                mediaUrls.forEach {
                    PluginMain.logger.info("url = $it")
                    //inquirerGroup.sendMessage(
                    toSay += Image(
                        URL(it).openConnection(proxy).getInputStream()
                            .uploadAsImage(group.first())
                            .imageId
                    )
                    //)
                    //inquirerGroup.sendMessage(it.toString())
                }
                mediaUrls.clear()
            }
            if (!toSay.isContentEmpty()) group.forEach {
                it.sendMessage("有关注的推主更新了哦")
                it.sendMessage(toSay)
            }


        } catch (e: Exception) {
            PluginMain.logger.info(e.message + e.stackTrace)
        }
        PluginMain.logger.info("检测正常结束")
        delay(60000L)


    }
}