package org.sddn.hibiki.plugin
import com.alibaba.fastjson.JSON
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.sddn.hibiki.plugin.PluginConfig.APIs
import java.net.URL

suspend fun getTimeline(inquirerGroup: Group){
    val timeline = APIs["recent"]?.let { httpGet2(it) }
    val tweetData = timeline?.getJSONArray("data")
    val tweetMedia = timeline?.getJSONObject("includes")?.getJSONArray("media")
    val tweetMeta = JSON.parseObject(timeline?.getJSONObject("meta").toString())
    //PluginMain.logger.info("${tweetMeta.toString()}")
    val nextToken = tweetMeta?.getString("next_token")
    val resultCount = tweetMeta?.getString("result_count").toString()
    val mediaUrls : MutableList<String> = mutableListOf("")

    PluginMain.logger.info{"成功获取$resultCount" + "条tweets"}

    val newestTweet = tweetData?.getJSONObject(0)
    val newestID = newestTweet?.getString("id").toString()
    val newestText = newestTweet?.getString("text").toString()
    if (newestTweet != null) {
        if (newestTweet.containsKey("attachments")){
            val mediaKeys = newestTweet.getJSONObject("attachments").getJSONArray("media_keys").toList();

            for (i in 0 until tweetMedia?.size!!){
                val media = tweetMedia.getJSONObject(i)
                if (media.getString("media_key").toString() in mediaKeys ){
                    mediaUrls.add(media.getString("url").toString())
                }
            }
        }
    }

    if ("null" != newestText){
        inquirerGroup.sendMessage(newestText.toPlainText())
    }
    if (mediaUrls.isNotEmpty()){
        PluginMain.logger.info("有${mediaUrls.size}张图片" )
        mediaUrls.forEach{url ->
            PluginMain.logger.info("url = $url")
            /*inquirerGroup.sendMessage(Image(
                URL(url).openConnection().getInputStream()
                    .uploadAsImage(inquirerGroup)
                    .imageId))*/
            inquirerGroup.sendMessage(url.toString())
        }

    }


}