package kr.saintdev.pst.models.libs

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import kr.saintdev.pst.R
import kr.saintdev.pst.models.components.broadcast.ProcedureBroadcastRecv
import kr.saintdev.pst.models.components.services.AlwaysOnService
import kr.saintdev.pst.models.components.services.ShakeDetectService
import kr.saintdev.pst.models.components.services.StartButtonOverlayService
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import kr.saintdev.pst.vnc.activity.CommonActivity
import java.text.DecimalFormat
import java.util.*

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
fun appendComma(`val`: Int?): String {
    if(`val` == null) {
        return ""
    }

    val format = DecimalFormat("###,###")   // 콤마
    format.format(`val`.toLong())
    return format.format(`val`.toLong())
}

/**
 *
 * @param level 타겟 sdk 레벨 입니다.
 * @return 해당 sdk 이상 만족하는지 에 대한 결과값
 */
fun checkAPILevel(level: Int) = android.os.Build.VERSION.SDK_INT >= level

/**
 * MediaProjection 객체를 얻어옵니다.
 * 물론 없을수도 있습니다.
 */
var mediaProjectionIntent: Intent? = null

/**
 * 스크린 캡쳐 후 번역기를 작동시킬 가치가 있는지 확인합니다.
 */
fun isScreenTranslaterRunningValue(context: Context): Boolean {
    // 현재 스크린 번역기가 실행될 가치가 있는지 확인합니다.
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val list = activityManager.runningAppProcesses
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    for (info in list) {
        if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            if (info.processName == context.packageName) return false // 최상위에 스크린 번역기가 있습니다.
            break
        }
    }

    // 화면에 오버레이가 있다면 중지합니다.
    return /*!isServiceRunningCheck(context, GamingModeOverlayService::class.java) &&*/ pm.isInteractive
}


/**
 *
 * @param context       context
 * @param className     Service 체크를 할 class
 * @return  해당 서비스 구동 여부를 true / false 로 나타냅니다.
 */
fun isServiceRunningCheck(context: Context, className: Class<*>): Boolean {
    val manager = context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE).any { className.name == it.service.className }
}

/**
 * API 26 레벨 이상에서는 Broadcast 를 발생 시키려면 등록 해야 합니다.
 */
fun addBroadcastReceiverFilter(receiver: BroadcastReceiver, context: Context, vararg actions: String) {
    if(checkAPILevel(Build.VERSION_CODES.O)) {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        for(a in actions) filter.addAction(a)
        context.registerReceiver(receiver, filter)
    }
}

fun unregistRecieverFilter(receiver: BroadcastReceiver, context: Context) {
    if(checkAPILevel(Build.VERSION_CODES.O)) {
        context.unregisterReceiver(receiver)
    }
}

/**
 * Always On Notification 서비스를 시작합니다.
 */
fun startAlwaysOnNotificationService(context: Context) {
    // 최상위 서비스를 실행한다.
    if(!isServiceRunningCheck(context, AlwaysOnService::class.java)) {
        val alwaysOnService = Intent(context, AlwaysOnService::class.java)
        context.startService(alwaysOnService)
    }
}

/**
 * 프로시저 서비스가 작동중인지 확인합니다.
 */
fun isProcedureServiceWorking(context: Context) =
        isServiceRunningCheck(context, ShakeDetectService::class.java) || isServiceRunningCheck(context, StartButtonOverlayService::class.java)

/**
 * 권한을 요청하러 이동했다면 true 를 리턴한다.
 */
fun requestSystemOverlayPermission(context: Context) =
    if(checkAPILevel(Build.VERSION_CODES.M) && !Settings.canDrawOverlays(context)) {
        // 오버레이 권한을 승인하러 갑니다.
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)
        true
    } else false

fun checkSystemOverlayPermisson(context: Context) =
        !(checkAPILevel(Build.VERSION_CODES.M) && !Settings.canDrawOverlays(context))

/**
 * 광고 액티비티를 열지 결정한다.
 */
var sessionOfAdsOpen = 0
fun doOpenAdsActivity(context: Context) = true
//        if(!RepositoryManager.quicklyGet(RepositoryKey.TICKET_USING, context)!!.toBoolean()
//            && sessionOfAdsOpen < 5 && true) {
//            // 정액권이 없고, 현 세션에서 5회 미만 광고가 표시됬다면
//            val rd = Random()
//            sessionOfAdsOpen ++
//            rd.nextInt(5) == 0       // 20% 확률로 광고를 발생 시킨다.
//        } else {
//            false
//        }

fun productItemIdToItemName(itemId: String, context: Context) : String {
    val itemIdArray = context.resources.getStringArray(R.array.products_item_id)
    val itemNameArray = context.resources.getStringArray(R.array.products_item_name)

    val idx = itemIdArray.indexOf(itemId)
    return itemNameArray[idx]
}
