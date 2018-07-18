package kr.saintdev.pst.models.components.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kr.saintdev.pst.models.components.services.DisplayCaptureService
import kr.saintdev.pst.models.libs.DeviceControl
import kr.saintdev.pst.models.libs.ScreenTranslate


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-04
 */
class ProcedureBroadcastRecv : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action

        when (action) {
            "kr.saintdev.psct.aos.switch" -> switchProcedureService(context)
        }
    }

    fun switchProcedureService(context: Context) {
        if (ScreenTranslate.isProcedureServiceRunning(context) && DeviceControl.isServiceRunning(context, DisplayCaptureService::class.java)) {
            // 서비스 작동중, 정지
            ScreenTranslate.screenTranslatePower(false, context)
        } else {
            // 서비스 정지, 시작
            ScreenTranslate.screenTranslatePower(true, context)
        }
    }
}