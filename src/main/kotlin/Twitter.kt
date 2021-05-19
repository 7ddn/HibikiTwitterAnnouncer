package org.sddn.hibiki.plugin

import com.alibaba.fastjson.JSON
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import java.net.URL
import java.net.URLEncoder
import kotlin.math.min

var globalNextToken: String? = ""


suspend fun getTimeline(
    inquirerGroup: Group,
    nextToken: String = "",
    maxCount: Int = 1,
    startCount: Int = 0,
    target: String = "from:YuGiOh_OCG_INFO",
) {
    //val timeline = APIs["baseRecent"]?.let { httpGet(it) }
    val timeline = httpGet(recentSearchUrlGenerator(nextToken = nextToken, searchTarget = URLEncoder.encode(target,"utf-8")))
    val tweetData = timeline.getJSONArray("data")
    val tweetMedia = timeline.getJSONObject("includes")?.getJSONArray("media")
    //PluginMain.logger.info("一共有${tweetMedia?.size}张图片")
    val tweetMeta = JSON.parseObject(timeline.getJSONObject("meta").toString())
    //PluginMain.logger.info("${tweetMeta.toString()}")
    globalNextToken = tweetMeta?.getString("next_token")
    val resultCount = tweetMeta?.getString("result_count").toString()
    val mediaUrls: MutableList<String> = mutableListOf("")

    PluginMain.logger.info { "成功获取$resultCount" + "条tweets" }
    when {
        resultCount.toInt() > 0 ->
            inquirerGroup.sendMessage("成功获取${resultCount}条推文")
        else ->
            inquirerGroup.sendMessage( PlainText("哎呀，什么都没有找到呢")
                + Image("{29635AF7-C078-33BB-1317-E4A96600BC25}.png")
            )
    }

    for (count in startCount until min(startCount + maxCount, min(10, resultCount.toInt()))) {
        var toSay : Message = buildMessageChain {  }

        mediaUrls.clear()
        val newestTweet = tweetData?.getJSONObject(count)
        val newestID = newestTweet?.getString("id").toString()
        val newestText = newestTweet?.getString("text").toString()
        if (newestTweet != null) {
            if (newestTweet.containsKey("attachments")) {
                val mediaKeys = newestTweet.getJSONObject("attachments").getJSONArray("media_keys").toList()
                //PluginMain.logger.info("这条tweet的配图id分别是${mediaKeys.toString()}")
                mediaUrls.clear()
                for (i in 0 until tweetMedia?.size!!) {
                    val media = tweetMedia.getJSONObject(i)
                    if (media.getString("media_key").toString() in mediaKeys) {
                        //PluginMain.logger.info(media.toString())
                        mediaUrls.add(media.getString("url"))
                    }
                }
            }
        }

        if ("null" != newestText) {
            toSay = newestText.toPlainText()
            //inquirerGroup.sendMessage(newestText.toPlainText())
        }
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


}