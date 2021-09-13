package utils

import com.cloudconvert.client.CloudConvertClient
import com.cloudconvert.client.setttings.StringSettingsProvider
import com.cloudconvert.dto.request.ConvertFilesTaskRequest
import com.cloudconvert.dto.request.UrlExportRequest
import com.cloudconvert.dto.request.UrlImportRequest
import com.cloudconvert.dto.response.TaskResponse
import com.google.common.collect.ImmutableMap
import java.io.InputStream

suspend fun convertMP4ToGIF(url: String): InputStream? {
    //print(1)
    try {
        val cloudConvertClient = CloudConvertClient(
            StringSettingsProvider(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiY2JiMzM2YTYzODYyNWYzMjdkODAyNzY0ZDVmMTBlNzFhMjY2MmQxZjI1NTkzNWFjYWZmZTVhMmNjNTM1YTdmNmZhMWYyNDM0MzNmMDNiZjEiLCJpYXQiOjE2MzE0NjQ4NjguNjg0NTA5LCJuYmYiOjE2MzE0NjQ4NjguNjg0NTEyLCJleHAiOjQ3ODcxMzg0NjguNjUxMDczLCJzdWIiOiI1MzM2NjEwMyIsInNjb3BlcyI6WyJ0YXNrLnJlYWQiLCJ0YXNrLndyaXRlIiwid2ViaG9vay5yZWFkIiwid2ViaG9vay53cml0ZSJdfQ.rvw6mOq8ezAAsut3AT9kvz5HMlPE-N43fOFC4Nu4UvmFEMO8QwJaDyKlG-aB_WevBDl21bVElc96eda2PqZR78LW1C4u4v4DbG-o0aFr9JG6iY6k85DPQH1qQmFEEjsflWHyf0TDmRAdkb22NyDde-5vT0yxvWB9rTKRfMOLrjplYC2i0K2Xe7mGRyPS8BBVWXeDzLHZTkvbMWfnGFsKoeo-r5CawPhTjRoiCOfWrxcbHeBy60HuOXY6e2VOoxS5rdYZXBFqvB7SAFmKtGrbcEdsFg_1bvTE1hiVdpZ1Tv9ftCQ2qiDUtHZ1lNGYEYEz0Wp9Oov1AspCkBIy4BDoc46j6zQD0n67dTe4obnrdXmxJewro1g5A1GkXIC4HHR_OGaJV9pVD_HOtI3PnYl2qOVp5ytp21S0HmcVv0pQkwaqmjoj9jYBH1uEFapOS5M_CTKd1uLTV5-whLX9eWritXoQO7fzfIfr-WoJqcR7a_T8iLddvIw3hUS3XspnMlfbg8AEWjmJi8ruKjwSLgtEJguA7-1b6kef5PRkHne9Aby8ye1R7qf2JTo5lyCAJdjrjF4JgnQ89CHLSrc2H-2eFZA8gpoqAOgejANHJFmF8jek-IWwiobICvonK2kEfym4aJ_aCRrGK_d_KSEuR8sp5GxdqgnU7WgJKumgYzPAnyQ",
                "webhook-signing-secret", false
            )
        )

        //println(cloudConvertClient)

        val createJobResponse = cloudConvertClient.jobs().create(
            ImmutableMap.of(
                "import-my-file",
                UrlImportRequest().setUrl(url),
                "convert-my-file",
                ConvertFilesTaskRequest().setInput("import-my-file")
                    .setOutputFormat("gif"),
                "export-my-file",
                UrlExportRequest().setInput("convert-my-file")
            )
        ).body

        //println(createJobResponse)

        val waitJobResponse = cloudConvertClient.jobs().wait(createJobResponse!!.id).body

        //print(waitJobResponse)

        val exportUrlTask =
            waitJobResponse!!.tasks.stream().filter { taskResponse: TaskResponse -> taskResponse.name == "export-my-file" }
                .findFirst().get()

        //println(exportUrlTask)

        val exportUrl = exportUrlTask.result.files[0]["url"]!!

        println(exportUrl)

        return cloudConvertClient.files().download(exportUrl).body!!
    } catch (e:Exception) {
        println("error at convertMP4ToGIF")
        return null
    }


}