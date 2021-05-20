package commandHandler

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import pluginController.PluginConfig
import pluginController.PluginMain

object ConfigCommand : CompositeCommand(
    owner = PluginMain,
    "set", "设置",
    description = "Set Plugin Configs",
){

    @SubCommand
    suspend fun CommandSender.api(target: String, value: String){
        try {
            PluginConfig.APIs[target] = value
            sendMessage("成功将API${target}更改为${value}")
        } catch (e:Exception){
            sendMessage("更改API${target}失败，可能不存在该API")
        }
    }

    @SubCommand
    suspend fun CommandSender.token(target: String, value: String){
        try {
            PluginConfig.Tokens[target] = value
            sendMessage("成功设置${target}为${value}")
        } catch (e:Exception){
            sendMessage("设置${target}失败，可能不存在该${target}")
        }
    }

    @SubCommand
    suspend fun CommandSender.proxy(target: String, value: String){
        try {
            PluginConfig.Proxies[target] = value
            sendMessage("成功设置${target}为${value}")
        } catch (e:Exception){
            sendMessage("设置${target}失败")
        }
    }


}