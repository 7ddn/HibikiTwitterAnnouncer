package org.sddn.hibiki.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import java.net.InetSocketAddress
import java.net.Proxy

object PluginConfig : AutoSavePluginConfig("config") {
    val APIs : Map<String, String> by value(mapOf(
        "recent" to "https://api.twitter.com/2/tweets/search/recent?query="
    ))

    val Proxies : Map<String, String> by value(mapOf(
        "host" to "127.0.0.1",
        "port" to "8099"
    ))

    val Tokens : Map<String, String> by value(mapOf(
        "bearerToken" to "AAAAAAAAAAAAAAAAAAAAAMHmPgEAAAAAnk1ZHn0GeuadsXZOBD0B1a7EsX0%3DlkUm5FLfeCCSDI8IeCxEF6KxdXsmmG0pvrExyDjx19L4gvNkA8"
    ))

    val Switches : Map<String, String> by value(mapOf(
        "newCard" to "1"
    ))


}