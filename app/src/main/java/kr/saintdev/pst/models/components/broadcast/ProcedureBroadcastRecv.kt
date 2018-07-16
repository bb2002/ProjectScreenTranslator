package kr.saintdev.pst.models.components.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kr.saintdev.pst.models.components.broadcast.ProcedureEngine.screenTransServiceOff
import kr.saintdev.pst.models.components.broadcast.ProcedureEngine.screenTransServiceOn
import kr.saintdev.pst.models.components.services.*
import kr.saintdev.pst.models.libs.checkSystemOverlayPermisson
import kr.saintdev.pst.models.libs.isServiceRunningCheck
import kr.saintdev.pst.models.libs.manager.*
import kr.saintdev.pst.models.libs.mediaProjectionIntent
import kr.saintdev.pst.models.libs.requestSystemOverlayPermission
import kr.saintdev.pst.vnc.activity.view.MainActivity
import kr.saintdev.pst.vnc.activity.view.MediaProjectionAcitivty
import kr.saintdev.pst.vnc.activity.view.startSystemOverlayGrantActivity
import kr.saintdev.pst.vnc.dialog.message.DialogManager

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-04
 */
class ProcedureBroadcastRecv : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null || intent == null) return
        val action = intent.action

        when(action) {
            "kr.saintdev.psct.aos.settings" -> openSettingActivity(context)
            "kr.saintdev.psct.aos.switch" -> switchProcedureService(context)
            "kr.saintdev.psct.aos.force_on" -> screenTransServiceOn(context)
            "kr.saintdev.psct.aos.force_off" -> screenTransServiceOff(context)
        }
    }

    fun openSettingActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun switchProcedureService(context: Context) {
        if(isServiceRunningCheck(context, DisplayCaptureService::class.java)) {
            // 캡쳐 서비스가 활성화 되어 있슴.
            // 모든 서비스를 종료한다.
            screenTransServiceOff(context)
        } else {
            // 캡쳐서비스가 꺼져있슴.
            // 스크린 번역기 부팅
            screenTransServiceOn(context)
        }

// 일단 지금은 휴대폰 흔들기만 가능합니다.
//        val repoManager = RepositoryManager.quicklyGet(RepositoryKey.MODE_SETTING, context)
//        when(repoManager) {
//        }

        startShakeDetectService(context)
    }

    private fun startShakeDetectService(context: Context) {
        if(!isServiceRunningCheck(context, ShakeDetectService::class.java)) {
            // 휴대폰 흔들림 감지 서비스를 시작한다.
            val captureService = Intent(context, ShakeDetectService::class.java)
            context.startService(captureService)
        }
    }

    private fun startButtonOverlayService() {

    }
}

object ProcedureEngine {
    fun screenTransServiceOn(context: Context) {
        if (mediaProjectionIntent == null) {
            // MediaProjection 객체를 받아옵니다.
            val mediaProj = Intent(context, MediaProjectionAcitivty::class.java)
            mediaProj.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(mediaProj)
        } else {
            // Gaming 모드 또는 Button overlay starter 를 사용중일 경우 권한을 요청함
            if(EnvSettingManager.getQuickly(context, EnvSettingKeys.RUN_EVENT) == "device_button_overlay"
                    || RepositoryManager.quicklyGet(RepositoryKey.MODE_SETTING, context) == "gaming") {
                if(!checkSystemOverlayPermisson(context)) {
                    // 시스템 오버레이 권한을 승인하러 이동한다.
                    startSystemOverlayGrantActivity(context)
                    return
                }
            }

            if(EnvSettingManager.getQuickly(context, EnvSettingKeys.RUN_EVENT) == "device_shake") {
                // shake 모드 사용
                val shakeDetect = getShakeDetectIntent(context)
                context.startService(shakeDetect)
            } else {
                // 오버레이 버튼 스타터 사용
                val startBtnOverlay = getStartButtonOverlayIntent(context)
                context.startService(startBtnOverlay)
            }

            // capture 서비스 실행
            val captureService = DisplayCaptureService.getDisplayCaptureIntent(context)
            captureService.putExtra("command", "init")
            context.startService(captureService)

            // Notification message 변경
            val notifiManager = AlwaysOnNotification.getInstance(context)
            notifiManager.setStatus(AlwaysOnNotification.NotificationStatus.RUNNING)
        }
    }

    fun screenTransServiceOff(context: Context) {
        // 스크린 번역기를 종료한다.
        if (isServiceRunningCheck(context, DisplayCaptureService::class.java))
            context.stopService(DisplayCaptureService.getDisplayCaptureIntent(context))

        if (isServiceRunningCheck(context, ShakeDetectService::class.java))
            context.stopService(getShakeDetectIntent(context))

        if (isServiceRunningCheck(context, StartButtonOverlayService::class.java))
            context.stopService(getStartButtonOverlayIntent(context))

        // Notification message 변경
        val notifiManager = AlwaysOnNotification.getInstance(context)
        notifiManager.setStatus(AlwaysOnNotification.NotificationStatus.PREPARED)
    }
}