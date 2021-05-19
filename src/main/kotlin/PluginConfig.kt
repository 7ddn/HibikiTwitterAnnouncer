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


}