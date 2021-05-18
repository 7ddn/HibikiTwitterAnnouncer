package org.sddn.hibiki.plugin

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import javax.swing.UIManager.getString
import kotlin.coroutines.resumeWithException

val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    println("coroutine: error ${throwable.message}")
}

val bearerToken = "AAAAAAAAAAAAAAAAAAAAAMHmPgEAAAAAnk1ZHn0GeuadsXZOBD0B1a7EsX0%3DlkUm5FLfeCCSDI8IeCxEF6KxdXsmmG0pvrExyDjx19L4gvNkA8";


suspend fun Call.awaitResponse(): okhttp3.Response {

    return suspendCancellableCoroutine {

        it.invokeOnCancellation {
            cancel()
        }

        enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                it.resume(response, null)
            }
        })
    }
}


fun httpGet(url: String) : JSONObject? {

    val client: OkHttpClient
    val builder: OkHttpClient.Builder = OkHttpClient.Builder();
    builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8099)))

    PluginMain.logger.info("url = $url")

    PluginMain.logger.info("now building client")


    client = builder.build();

    PluginMain.logger.info("now making request")
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .addHeader("Content-Type", "application/json")
        .build()

    var result: JSONObject? = null

    PluginMain.logger.info("now sending client")
    GlobalScope.launch(exceptionHandler) {
        //val response = client.newCall(request).awaitResponse();
        val response = client.newCall(request).execute()
        PluginMain.logger.info("Response = ${response.message} ")
        PluginMain.logger.info("Response is ${response.isSuccessful} ")
        result = JSON.parseObject(response.message)

    }

    //PluginMain.logger.info(result.toString())
    return result;
}

fun httpGet2(url: String): JSONObject{
    val link = URL(url)
    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8099))
    val connection = link.openConnection(proxy) as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 5000
    connection.setRequestProperty("Content-Type","application/json; charset=utf-8")
    connection.setRequestProperty("Authorization", "Bearer $bearerToken")

    try{
        val reader: BufferedReader = BufferedReader(
            InputStreamReader(connection.inputStream,"utf-8"))
        val output: String = reader.readLine()
        //PluginMain.logger.info("Response = ${output.toString()} ")

        return JSON.parseObject(output)

    } catch (exception: Exception){
        PluginMain.logger.info(exception.message)
        throw Exception(exception.message)
    }

}

