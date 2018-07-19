package kr.saintdev.pst.models.libs

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.PowerManager
import kr.saintdev.pst.models.components.services.*
import kr.saintdev.pst.models.libs.manager.AlwaysOnNotification
import kr.saintdev.pst.models.libs.manager.EnvSettingKeys
import kr.saintdev.pst.models.libs.manager.EnvSettingManager

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-19
 */
object ScreenTranslate {
    /**
     * @param context Context
     */
    fun isStartScreenTranslate(context: Context) : Boolean {
        // 스크린 번역기를 실행해도 될지 확인합니다.
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = activityManager.runningAppProcesses
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        for (info in list) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (info.processName == context.packageName) return false // 최상위에 스크린 번역기가 있습니다.
                break
            }
        }

        // 화면에 오버레이가 있다면 실행하지 않습니다.
        return !DeviceControl.isServiceRunning(context, GamingModeService::class.java) && pm.isInteractive
    }

    /**
     * @param context Context
     * 프로시저 서비스가 실행중인지 확인합니다.
     */
    fun isProcedureServiceRunning(context: Context) =
            DeviceControl.isServiceRunning(context, ShakeDetectService::class.java)
                    || DeviceControl.isServiceRunning(context, StartButtonOverlayService::class.java)

    /**
     * @param power 스크린 번역기 활성화 처리 여부
     */
    fun screenTranslatePower(power: Boolean, context: Context) {
        if(power) {
            // 스크린 번역기 서비스가 작동중인지 확인한다.
            if(isProcedureServiceRunning(context) && DeviceControl.isServiceRunning(context, DisplayCaptureService::class.java) ) {
                return
            }

            // MediaProjection 객체 확보
            val mediaProjectionIntent = MediaProj.getMediaProjectionIntent()
            if (mediaProjectionIntent == null) {
                MediaProj.openRequestMediaProjection(context, true)
                return
            }

            // System overlay 가 필요한지 확인
            if (SystemOverlay.isNeedSystemOverlay(context) && !SystemOverlay.isGrantedSystemOverlay(context)) {
                // System overlay permission 확보
                SystemOverlay.openSystemOverlayGrantActivity(context)
                return
            }

            // 스크린 번역기 실행
            // 프로시저 시작
            if(EnvSettingManager.getQuickly(context, EnvSettingKeys.RUN_EVENT) == "device_shake") {
                // shake 모드 사용
                val shakeDetect = getShakeDetectIntent(context)
                context.startService(shakeDetect)
            } else {
                // 오버레이 버튼 스타터 사용
                val startBtnOverlay = getStartButtonOverlayIntent(context)
                context.startService(startBtnOverlay)
            }

            // 디스플레이 캡쳐 시작
            val captureService = DisplayCaptureService.getDisplayCaptureIntent(context)
            captureService.putExtra("command", "init")
            context.startService(captureService)

            // Notification 을 Start 으로 변경
            AlwaysOnNotifi.notifiStatusStart(context)
        } else {
            // Always on notification 외 모든 서비스 종료
            if (DeviceControl.isServiceRunning(context, DisplayCaptureService::class.java))
                context.stopService(DisplayCaptureService.getDisplayCaptureIntent(context))

            if (DeviceControl.isServiceRunning(context, ShakeDetectService::class.java))
                context.stopService(getShakeDetectIntent(context))

            if (DeviceControl.isServiceRunning(context, StartButtonOverlayService::class.java))
                context.stopService(getStartButtonOverlayIntent(context))

            // Notification 을 Stop 으로 변경
            AlwaysOnNotifi.notifiStatusStop(context)
        }
    }
}

object AlwaysOnNotifi {
    /**
     * @param context Context
     * Always on notification service 를 시작한다.
     */
    fun startAlwaysOnNotificationService(context: Context) {
        var notifiManager = AlwaysOnNotification.getInstance(context)
        notifiManager.show()
    }

    /**
     * @param context Context
     * Notification 의 상태를 시작 으로 바꿉니다.
     */
    fun notifiStatusStart(context: Context) =
        AlwaysOnNotification.getInstance(context).setStatus(AlwaysOnNotification.NotificationStatus.RUNNING)

    /**
     * @param context Context
     * Notification 의 상태를 종료로 바꿉니다.
     */
    fun notifiStatusStop(context: Context) =
            AlwaysOnNotification.getInstance(context).setStatus(AlwaysOnNotification.NotificationStatus.PREPARED)
}