package kr.saintdev.pst.models.http

import java.util.HashMap
import java.util.concurrent.TimeUnit

import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by yuuki on 18. 4. 22.
 */

class HttpRequester(url: String, args: HashMap<String, Any>?, requestCode: Int, listener: OnBackgroundWorkListener?) : BackgroundWork<HttpResponseObject>(requestCode, listener) {
    private val url: String?
    private val param: HashMap<String, Any>?

    init {
        this.url = url
        this.param = args
    }

    @Throws(Exception::class)
    public override fun script(): HttpResponseObject {
        val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        val reqBuilder = FormBody.Builder()

        // 인자 값이 있다면 넣어줍니다.
        if (param != null) {
            for((key, value) in param) {
                reqBuilder.add(key, value.toString())
            }
        }

        val reqBody = reqBuilder.build()
        val request = Request.Builder().url(this.url!!).post(reqBody).build()

        val response = client.newCall(request).execute()
        val jsonScript = response.body()!!.string()

        val responseObj = HttpResponseObject(jsonScript)

        // 응답 완료
        response.close()

        return responseObj
    }
}

