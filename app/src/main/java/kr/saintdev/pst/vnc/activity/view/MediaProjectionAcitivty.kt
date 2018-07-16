package kr.saintdev.pst.vnc.activity.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mediaprojection.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.components.broadcast.ProcedureBroadcastRecv
import kr.saintdev.pst.models.components.services.DisplayCaptureService
import kr.saintdev.pst.models.libs.addBroadcastReceiverFilter
import kr.saintdev.pst.models.libs.mediaProjectionIntent
import kr.saintdev.pst.models.libs.unregistRecieverFilter
import kr.saintdev.pst.vnc.activity.CommonActivity

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-05
 */
class MediaProjectionAcitivty : CommonActivity() {
    val REQUEST_MEDIA_PROJECTION = 0x0
    val revciver = ProcedureBroadcastRecv()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediaprojection)

        startMediaProjection()
        addBroadcastReceiverFilter(revciver, this, "kr.saintdev.psct.aos.force_on")

        mediaproj_retry.setOnClickListener {
            startMediaProjection()
        }
    }

    fun startMediaProjection() {
        // 스크린 번역기, 캡쳐 서비스가 꺼져있 을 때만 실행 시킨다.
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(REQUEST_MEDIA_PROJECTION == requestCode && resultCode == Activity.RESULT_OK) {
            // MediaProjection 에 대한 결과를 가져온다.
            mediaProjectionIntent = data

            val intent = Intent("kr.saintdev.psct.aos.force_on")
            sendBroadcast(intent)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        unregistRecieverFilter(revciver, this)
    }
}