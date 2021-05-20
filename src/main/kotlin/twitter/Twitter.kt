package twitter

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import utils.httpGet
import pluginController.PluginConfig
import pluginController.PluginMain
import utils.proxy
import utils.recentSearchUrlGenerator
import java.net.URL
import java.net.URLEncoder
import kotlin.math.min

var globalNextToken: String? = ""

suspend fun getNewestTweet(
    target: String = "from:YuGiOh_OCG_INFO"
): JSONObject? {
    return try {
        val timeline = httpGet(
            recentSearchUrlGenerator(
                searchTarget = target
            )
        )
        timeline
    } catch (e: Exception) {
        PluginMain.logger.info(e.message)
        null
    }
}

suspend fun getTimelineAndSendMessage(
    inquirerGroup: Group,
    nextToken: String = "",
    maxCount: Int = 1,
    startCount: Int = 0,
    target: String = "from:YuGiOh_OCG_INFO",
    maxResults: Int = 10,
) {
    //val timeline = APIs["baseRecent"]?.let { httpGet(it) }
    try {
        val timeline = httpGet(
            recentSearchUrlGenerator(
                nextToken = nextToken,
                searchTarget = URLEncoder.encode(target, "utf-8"),
                maxResults = maxResults
            )
        )

        val tweetData = timeline.getJSONArray("data")
        val tweetMedia = timeline.getJSONObject("includes")?.getJSONArray("media")
        val authors = timeline.getJSONObject("includes")?.getJSONArray("users")
        //PluginMain.logger.info("一共有${tweetMedia?.size}张图片")
        val tweetMeta = JSON.parseObject(timeline.getJSONObject("meta").toString())
        //PluginMain.logger.info("${tweetMeta.toString()}")
        globalNextToken = tweetMeta?.getString("next_token")
        val resultCount = tweetMeta?.getString("result_count").toString()
        var mediaUrls: MutableList<String> = mutableListOf()

        PluginMain.logger.info { "成功获取$resultCount" + "条tweets" }
        when {
            resultCount.toInt() > 0 ->
                inquirerGroup.sendMessage("成功获取${resultCount}条推文")
            else ->
                inquirerGroup.sendMessage(
                    PlainText("哎呀，什么都没有找到呢")
                        + Image("{29635AF7-C078-33BB-1317-E4A96600BC25}.png")
                )
        }

        for (count in startCount until min(startCount + maxCount, min(10, resultCount.toInt()))) {
            var toSay: Message = buildMessageChain { }

            val newestTweet = tweetData?.getJSONObject(count)
            val newestID = newestTweet?.getString("id").toString()
            val newestText = newestTweet?.getString("text").toString()
            val authorID = newestTweet?.getString("author_id")

            if (newestTweet != null) {
                if (newestTweet.containsKey("attachments")) {
                    val mediaKeys = newestTweet.getJSONObject("attachments").getJSONArray("media_keys").toList()
                    //PluginMain.logger.info("这条tweet的配图id分别是${mediaKeys.toString()}")
                    mediaUrls = getMediaUrlsFromKeys(tweetMedia, mediaKeys)
                }

            }

            for (i in 0 until authors?.size!!) {
                val users = authors.getJSONObject(i)
                if (users.getString("id").toString() == authorID) {
                    toSay = PlainText(
                        users.getString("name").toString() +
                            "(@${users.getString("username")}):\r\n"
                    )
                }
            }


            if ("null" != newestText) {
                toSay += newestText.toPlainText()
                //inquirerGroup.sendMessage(newestText.toPlainText())
            }

            // 由于tx不让一次发送约100(104?)个字符以上的PlainText，故此处使用特殊处理分割,可以通过命令开关
            // TODO: 当机器人可以正常运行后删除

            if (PluginConfig.ifNeedToSplit) toSay = sendAndSplitToUnder100(toSay.content.toPlainText(), inquirerGroup)

            if (mediaUrls.isNotEmpty()) {
                PluginMain.logger.info("有${mediaUrls.size}张图片")
                mediaUrls.forEach {
                    PluginMain.logger.info("url = $it")
                    //inquirerGroup.sendMessage(
                    toSay += Image(
                        URL(it).openConnection(proxy).getInputStream()
                            .uploadAsImage(inquirerGroup)
                            .imageId
                    )
                    //)
                    //inquirerGroup.sendMessage(it.toString())
                }
                mediaUrls.clear()
            }

            if (!toSay.isContentEmpty()) inquirerGroup.sendMessage(toSay)

        }
    } catch (e: Exception) {
        when {
            e.toString() == "No Available Bearer Token" -> {
                inquirerGroup.sendMessage(
                    PlainText(
                        "哎呀，还没有设置Bearer Token呢"
                    ) +
                        Image("{372536B0-2903-CE21-EE53-D89C18BC4363}.jpg")
                )
            }
        }
    }


}

suspend fun checkUserName(userName: String): String {
    try {
        val userData = httpGet(
            PluginConfig.APIs["usersBy"].toString() +
                "/username/$userName"
        )
        if (userData.containsKey("errors")) throw Exception("No Such User")
        val name = userData.getJSONObject("data").getString("name")
        PluginMain.logger.info(name)
        return name
    } catch (e: Exception) {
        throw e
    }
}

fun getMediaUrlsFromKeys(
    tweetMedia: JSONArray?,
    mediaKeys: List<Any>,
): MutableList<String> {
    val mediaUrls: MutableList<String> = mutableListOf()
    for (i in 0 until tweetMedia?.size!!) {
        val media = tweetMedia.getJSONObject(i)
        if (media.getString("media_key").toString() in mediaKeys) {
            //PluginMain.logger.info(media.toString())
            mediaUrls.add(media.getString("url"))
        }
    }
    return mediaUrls
}

suspend fun sendAndSplitToUnder100 (message : PlainText, target: Contact) : MessageChain { //将要发送的PlainText拆分成最长99的字节并发送
    PluginMain.logger.info("正在分割长句$message")
    var messageText = message.content
    while (messageText.length > 99){
        val sub100 =  messageText.substring(0, 99)
        target.sendMessage(sub100.toPlainText())
        messageText = messageText.substring(99, messageText.length - 1)
    }
    return messageText.toPlainText().toMessageChain()
}