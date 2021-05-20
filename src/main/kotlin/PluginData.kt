package org.sddn.hibiki.plugin

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group

object PluginData : AutoSavePluginData("data") {
    val groups: MutableSet<Long> by value()
    val listeningListByGroup: MutableMap<Long, MutableSet<String>> by value()
    var ifGroupListHasChanged: Boolean by value(true)
    var lastOCGTweetID: String by value("0")
    var lastTweetID: MutableMap<String, String> by value()
    var repeatProbability: Int by value(5) // -1 for disable
}