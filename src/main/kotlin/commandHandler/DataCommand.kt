package commandHandler

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import pluginController.PluginData
import pluginController.PluginMain
import twitter.checkUserName

object  Follow : SimpleCommand(
    PluginMain, "follow", "关注",
    description = "add a twitter user to following list",
    ){

    @Handler
    suspend fun MemberCommandSenderOnMessage.follow(target: String){
        val twitterNameRegex = Regex("^[a-zA-Z0-9_]$")
        if (!twitterNameRegex.matches(target)){
            sendMessage("这不是一个有效的Twitter用户名呢")
            return
        }
        if (!PluginData.groups.contains(group.id)){
            sendMessage("还没有添加订阅呢，请先订阅再开始建立关注列表哦")
            return
        }
        try {
            val name = checkUserName(target)
            PluginData.listeningListByGroup[group.id]!!.add(target)
            PluginData.lastTweetID[target] = "0"
            group.sendMessage("开始关注${name}的最新推文了哦")
        } catch (e: Exception) {
            PluginMain.logger.info("error: ${e.message}")
            if (e.message == "No Such User")
                group.sendMessage("哎呀，好像没有这个人呢，请仔细检查输入的用户名是否正确呢")
        }
    }
}

object  Unfollow : SimpleCommand(
    PluginMain, "unfollow", "取消关注",
    description = "remove a twitter user to following list",
){

    @Handler
    suspend fun MemberCommandSenderOnMessage.unfollow(target: String){
        val twitterNameRegex = Regex("^[a-zA-Z0-9_]$")
        if (!twitterNameRegex.matches(target)){
            sendMessage("这不是一个有效的Twitter用户名呢")
            return
        }
        if (!PluginData.groups.contains(group.id)){
            sendMessage("还没有添加订阅呢，请先订阅再开始建立关注列表哦")
            return
        }
        if (!twitterNameRegex.matches(target)) {
            sendMessage("${target}不是一个有效的twitter用户名")
            return
        }
        if (!PluginData.listeningListByGroup[group.id]!!.contains(target)) {
            sendMessage("本来就没有关注@${target}呢")
        } else {
            sendMessage("以后不看@${target}了哦")
            PluginData.listeningListByGroup[group.id]!!.remove(target)
        }
    }
}

object StartListen : SimpleCommand(
    PluginMain, "startListen", "添加订阅",
    description = "add a listener to this group"
){
    @Handler
    suspend fun MemberCommandSenderOnMessage.startListener(){
        if (PluginData.groups.contains(group.id)) {
            group.sendMessage("已经添加过订阅了哦")
        } else {
            PluginData.groups.add(group.id)
            PluginData.listeningListByGroup[group.id] = mutableSetOf()
            group.sendMessage("添加订阅成功")
            PluginData.ifGroupListHasChanged = true
        }
    }
}

object StopListen : SimpleCommand(
    PluginMain, "startListen", "添加订阅",
    description = "add a listener to this group"
){
    @Handler
    suspend fun MemberCommandSenderOnMessage.stopListener(){
        if (PluginData.groups.contains(group.id)) {
            PluginData.groups.remove(group.id)
            group.sendMessage("呜呜 以后不说了哦")
            PluginData.ifGroupListHasChanged = true
        } else {
            group.sendMessage("哎呀，本来就没有订阅呢")
        }
    }
}

//TODO: filter
