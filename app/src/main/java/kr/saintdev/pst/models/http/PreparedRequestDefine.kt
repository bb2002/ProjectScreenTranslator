package kr.saintdev.pst.models.http

import android.content.Context
import kr.saintdev.pst.models.consts.HTTP_INODE_UPDATER
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import org.json.JSONException


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * 미리 정의된 http 요청 처리
 * @Date 2018-07-03
 */

/**
 * inode 값을 업데이트 받아 옵니다.
 */
const val PREPARED_REQUEST_DEFINE_INODE_UPDATER = 0x10

fun requestInodeUpdate(listener: OnBackgroundWorkListener?, context: Context) {
    val repoManager = RepositoryManager.getInstance(context)
    val defaultListener = object : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            saveInodeUpdate(worker, context)
        }

        override fun onFailed(requestCode: Int, ex: Exception?) {}
    }

    val token = repoManager.getHashValue(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN)
    if(token != null) {
        val requester = HttpRequester(HTTP_INODE_UPDATER,
                hashMapOf("account_token" to token),
                PREPARED_REQUEST_DEFINE_INODE_UPDATER,
                listener ?: defaultListener
        )
        requester.execute()
    }
}

/**
 * inode 값을 저장합니다.
 */
fun saveInodeUpdate(worker: BackgroundWork<*>, context: Context) {
    val response = worker.result

    // 서버로 부터 정상적인 응답을 받았습니다.
    try {
        if (response is HttpResponseObject && response.isSuccess) {
            val repoManager = RepositoryManager.getInstance(context)

            if (response.isSuccess) {
                val data = response.message
                repoManager.createHashValue(RepositoryKey.INODE_REMAIN_PACKET, data.getString("inode_remain_nodes"))
                repoManager.createHashValue(RepositoryKey.TICKET_USING, data.getString("is_ticket"))
            }
        }
    } catch(ex: JSONException) {}
}