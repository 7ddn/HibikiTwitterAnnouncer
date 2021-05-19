package org.sddn.hibiki.plugin

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL


fun recentSearchUrlGenerator(
    searchTarget: String = "from:YuGiOh_OCG_INFO",
    nextToken : String = "",
    expansions : String = "attachments.media_keys",
    mediaFields : String = "url"
    ) : String {
        return "${PluginConfig.APIs["recent"]}" +
            searchTarget +
            "&expansions=$expansions"+
            "&media.fields=$mediaFields"+
            if (nextToken!="") "&next_token=$nextToken" else ""
}

//"baseRecent" to "https://api.twitter.com/2/tweets/search/recent?query=from:YuGiOh_OCG_INFO" +
//            "&expansions=attachments.media_keys" +
//            "&media.fields=url"

val bearerToken = "AAAAAAAAAAAAAAAAAAAAAMHmPgEAAAAAnk1ZHn0GeuadsXZOBD0B1a7EsX0%3DlkUm5FLfeCCSDI8IeCxEF6KxdXsmmG0pvrExyDjx19L4gvNkA8";
val proxy =  Proxy(Proxy.Type.HTTP, InetSocketAddress(
    PluginConfig.Proxies["host"].toString(),
    PluginConfig.Proxies["port"].toString().toInt()))

fun httpGet(url: String): JSONObject{
    val link = URL(url)

    val connection = link.openConnection(proxy) as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 5000
    connection.setRequestProperty("Content-Type","application/json; charset=utf-8")
    connection.setRequestProperty("Authorization", "Bearer $bearerToken")

    try{
        val reader = BufferedReader(
            InputStreamReader(connection.inputStream,"utf-8"))
        val output: String = reader.readLine()
        //PluginMain.logger.info("Response = ${output.toString()} ")

        return JSON.parseObject(output)

    } catch (exception: Exception){
        PluginMain.logger.info(exception.message)
        throw Exception(exception.message)
    }

}

