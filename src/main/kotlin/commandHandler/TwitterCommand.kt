package commandHandler

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import pluginController.PluginMain
import twitter.getTimelineAndSendMessage

object Query : SimpleCommand (
    PluginMain, "query", "查询",
    description = "Query a set of tweets from twitter id"
    ){
        @Handler
        suspend fun MemberCommandSenderOnMessage.query(target: String, number: Int){
            getTimelineAndSendMessage(group, target = "from:${String}", maxCount = number)
        }
}

object Search : SimpleCommand (
    PluginMain, "Search", "查找",
    description = "Search a set of tweets from key word"
    ){
        @Handler
        suspend fun MemberCommandSenderOnMessage.search(target: String, number: Int){
            getTimelineAndSendMessage(group, target = "${String}", maxCount = number)
        }
}