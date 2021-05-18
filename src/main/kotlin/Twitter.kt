package org.sddn.hibiki.plugin
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.sddn.hibiki.plugin.PluginConfig.APIs
import java.net.URL

suspend fun getTimeline(bot :Bot, inquirerGroup: Group){
    val timeline = APIs["recent"]?.let { httpGet(it) }
    val tweetData = timeline?.getJSONArray("data")
    val tweetMedia = timeline?.getJSONObject("includes")?.getJSONArray("media")
    val tweetMeta = timeline?.getJSONObject("meta")
    val nextToken = tweetMeta?.getJSONObject("next_token")
    val resultCount = tweetMeta?.getJSONObject("result_count").toString()
    val mediaUrls : MutableList<String> = mutableListOf("")

    PluginMain.logger.info{"成功获取$resultCount" + "条tweets"}

    val newestTweet = tweetData?.getJSONObject(0)
    val newestID = newestTweet?.getJSONObject("id").toString()
    val newestText = newestTweet?.getJSONObject("text").toString()
    if (newestTweet != null) {
        if (newestTweet.containsKey("attachments")){
            val mediaKeys = newestTweet.getJSONObject("attachments").getJSONArray("media_keys").toList();

            for (i in 0 until tweetMedia?.size!!){
                val media = tweetMedia.getJSONObject(i)
                if (media.getJSONObject("media_key").toString() in mediaKeys ){
                    mediaUrls.add(media.getJSONObject("url").toString())
                }
            }
        }
    }

    if ("" != newestText){
        inquirerGroup.sendMessage(newestText.toPlainText())
    }
    if (mediaUrls.isNotEmpty()){
        mediaUrls.forEach{url ->
            inquirerGroup.sendMessage(Image(
                URL(url).openConnection().getInputStream()
                    .uploadAsImage(inquirerGroup)
                    .imageId))
        }
    }


}