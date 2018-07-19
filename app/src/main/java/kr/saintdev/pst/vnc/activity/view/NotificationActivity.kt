package kr.saintdev.pst.vnc.activity.view

import android.os.Bundle
import kr.saintdev.pst.R
import kr.saintdev.pst.models.components.services.DisplayCaptureService
import kr.saintdev.pst.models.libs.DeviceControl
import kr.saintdev.pst.models.libs.ScreenTranslate
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.activity.DialogType

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-20
 */
class NotificationActivity : CommonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // 현재 상태를 반전합니다.
        val nowStatus = ScreenTranslate.isProcedureServiceRunning(this) && DeviceControl.isServiceRunning(this, DisplayCaptureService::class.java)
        ScreenTranslate.screenTranslatePower(
                !nowStatus,
                this
        )

        val data =
                if(nowStatus) {
                    // 켜짐 상태라면 꺼졌다고 안내 한다.
                    R.string.notifi_activity_stopping
                } else {
                    // 꺼짐 상태라면 켜졌다고 안내 한다.
                    R.string.notifi_activity_running
                }
        val dialog = openPrettyDialog(R.string.notifi_activity_title, data, DialogType.SUCCESS)
        dialog.setOnDismissListener { finish() }
    }
}