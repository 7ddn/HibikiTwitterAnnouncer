package org.sddn.hibiki.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object PluginConfig : AutoSavePluginConfig("config") {
    val APIs : Map<String, String> by value(mapOf(
        "recent" to "https://api.twitter.com/2/tweets/search/recent?query=from:YuGiOh_OCG_INFO" +
            "&expansions=attachments.media_keys" +
            "&media.fields=url"

    ))

}