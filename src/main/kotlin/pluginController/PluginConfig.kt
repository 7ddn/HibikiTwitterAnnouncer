package pluginController

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object PluginConfig : AutoSavePluginConfig("config") {
    val APIs: MutableMap<String, String> by value(
        mutableMapOf(
            "usersBy" to "https://api.twitter.com/2/users/by",
            "recent" to "https://api.twitter.com/2/tweets/search/recent?query=",
        )
    )

    val Proxies: MutableMap<String, String> by value(
        mutableMapOf(
            "host" to "127.0.0.1",
            "port" to ""
        )
    )

    val Tokens: MutableMap<String, String> by value(
        mutableMapOf(
            "bearerToken" to ""
        )
    )

    var ifNeedToSplit : Boolean by value(false)


}