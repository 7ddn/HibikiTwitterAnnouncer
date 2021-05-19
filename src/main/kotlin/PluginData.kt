package org.sddn.hibiki.plugin

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData : AutoSavePluginData("data"){
    val groups : MutableList<Long> by value()
    val lastTweetID : String by value()
}