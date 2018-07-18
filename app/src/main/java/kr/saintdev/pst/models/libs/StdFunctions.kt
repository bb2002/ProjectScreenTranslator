package kr.saintdev.pst.models.libs

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kr.saintdev.pst.R
import kr.saintdev.pst.models.libs.manager.EnvSettingKeys
import kr.saintdev.pst.models.libs.manager.EnvSettingManager
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.activity.view.MediaProjectionAcitivty
import java.text.DecimalFormat


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */

object StdString {
    /**
     * @param data , 를 포함할 숫자 데이터
     * @return , 가 포함된 문자열 데이터
     */
    fun appendCommaToInt(data: Int?) =
        if(data == null) {
            ""
        } else {
            val format = DecimalFormat("###,###")   // 콤마
            format.format(data.toLong())
        }
}

object SystemOverlay {
    /**
     * @param context 컨텍스트
     * @return System overlay 승인 여부 (승인 : true)
     */
    fun isGrantedSystemOverlay(context: Context) =
            !(DeviceControl.checkAPILevel(Build.VERSION_CODES.M) && !Settings.canDrawOverlays(context))

    /**
     * @param context 컨텍스트
     */
    fun openSystemOverlayGrantActivity(context: Context) {
        if(!isGrantedSystemOverlay(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
        }
    }

    /**
     * @param context Context
     * @return boolean, 시스템 오버레이 권한이 필요한지 확인
     */
    fun isNeedSystemOverlay(context: Context) : Boolean{
        val nowTransMode = RepositoryManager.quicklyGet(RepositoryKey.MODE_SETTING, context = context)
        val nowRunMode = EnvSettingManager.getQuickly(context = context, key = EnvSettingKeys.RUN_EVENT)

        return nowTransMode == "gaming" || nowRunMode == "device_button_overlay"
    }
}

object ApplicationPermission {
    val REQUIRE_PERMISSION_FOR_APPALL = arrayOf(android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.GET_ACCOUNTS)
    val REQUEST_CODE_PERMISSION_FOR_APPALL = 0xff

    /**
     * @param activity 액티비티
     */
    fun openPermissionGrantDialog(activity: CommonActivity) {
        if(!checkPermissionForAppAll(activity)) {
            ActivityCompat.requestPermissions(activity, REQUIRE_PERMISSION_FOR_APPALL, REQUEST_CODE_PERMISSION_FOR_APPALL)
        }
    }

    /**
     * @param context 컨텍스트
     * @return AppAll 에서 요청하는 권한 유무 (있다면 true)
     */
    fun checkPermissionForAppAll(context: Context) =
            if(DeviceControl.checkAPILevel(Build.VERSION_CODES.M)) {
                var requestPermission = false

                REQUIRE_PERMISSION_FOR_APPALL.forEachIndexed { _, s ->
                    if (ContextCompat.checkSelfPermission(context, s) == PackageManager.PERMISSION_DENIED) {
                        requestPermission = true
                    }
                }

                !requestPermission
            } else true
}

object DeviceControl {
    /**
     * @param level 타겟 sdk 레벨 입니다.
     * @return 해당 sdk 이상 만족하는지 에 대한 결과값
     */
    fun checkAPILevel(level: Int) = android.os.Build.VERSION.SDK_INT >= level

    /**
     * @param receiver Broadcast receiver
     * @param context 컨텍스트
     * @param actions 방송 송출할 엑션
     * actions 를 브로드케스트 리시버에 등록합니다.
     */
    @TargetApi(26)
    fun registerBroadcastReceiver(receiver: BroadcastReceiver, context: Context, vararg actions: String) {
        if(checkAPILevel(Build.VERSION_CODES.O)) {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            for(a in actions) filter.addAction(a)
            context.registerReceiver(receiver, filter)
        }
    }

    /**
     * @param receiver Broadcast receiver
     * @param context 컨텍스트
     * 브로드케스트 리시버를 해제 합니다.
     */
    @TargetApi(26)
    fun unregisterBroadcastReceiver(context: Context, receiver: BroadcastReceiver) {
        if(checkAPILevel(Build.VERSION_CODES.O)) {
            context.unregisterReceiver(receiver)
        }
    }

    /**
     *
     * @param context       context
     * @param className     Service 체크를 할 class
     * @return  해당 서비스 구동 여부를 true / false 로 나타냅니다.
     */
    fun isServiceRunning(context: Context, className: Class<*>) : Boolean {
        val manager = context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { className.name == it.service.className }
    }
}

object MediaProj {
    private var mediaProjectionIntent: Intent? = null

    /**
     * @param context Context
     * @param startServiceAuto 자동으로 스크린 번역기 서비스를 시작할지 결정합니다.
     * MediaProjection 을 요청하는 함수
     */
    fun openRequestMediaProjection(context: Context, startServiceAuto: Boolean) {
        val intent = Intent(context, MediaProjectionAcitivty::class.java)
        intent.putExtra("service_start", startServiceAuto)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * @param data MediaProjection Intent
     */
    fun setMediaProjectionIntent(data: Intent) {
        this.mediaProjectionIntent = data
    }

    /**
     * @return MediaProjection Intent (nullable)
     */
    fun getMediaProjectionIntent() = mediaProjectionIntent
}

object OpenPreparedActivity {
    /**
     * 플레이스토어를 엽니다.
     */
    fun openPlayStore(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName)))
        } catch (ex: Exception) {
            Toast.makeText(context, "Can not open play store.", Toast.LENGTH_SHORT).show()
        }
    }
}



fun productItemIdToItemName(itemId: String, context: Context) : String {
    val itemIdArray = context.resources.getStringArray(R.array.vf_products_item_id)
    val itemNameArray = context.resources.getStringArray(R.array.vf_products_item_name)

    val idx = itemIdArray.indexOf(itemId)
    return itemNameArray[idx]
}