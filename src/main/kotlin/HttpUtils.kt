package org.sddn.hibiki.plugin

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    println("coroutine: error ${throwable.message}")
}

fun httpGet(url: String) : JSONObject? {

    val client: OkHttpClient
    val builder: OkHttpClient.Builder = OkHttpClient.Builder();
    val bearerToken = "1";
    client = builder.build();
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $bearerToken")
        .addHeader("Content-Type", "application/json")
        .build()
    var result :JSONObject? = null;
    GlobalScope.launch(exceptionHandler) {
        val response = client.newCall(request).execute();
        result = JSON.parseObject(response.message)
    }

    return result;
}


