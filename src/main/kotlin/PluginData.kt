package org.sddn.hibiki.plugin

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData : AutoSavePluginData("data"){
    val groups : MutableList<Long> by value()
    var lastTweetID : String by value()
    var repeatProbability : Int by value(5) // -1 for disable
}