package kr.saintdev.pst.vnc.activity.control

import android.content.Intent
import android.widget.Button
import com.github.angads25.toggle.LabeledSwitch
import com.kyad.adlibrary.AppAllOfferwallSDK
import kotlinx.android.synthetic.main.vf_activity_main.*
import kr.saintdev.pst.R
import kr.saintdev.pst.R.id.vf_main_power_switch
import kr.saintdev.pst.models.consts.WORDPRESS_HELP
import kr.saintdev.pst.models.libs.appendComma
import kr.saintdev.pst.models.libs.checkPermissionForAppAll
import kr.saintdev.pst.models.libs.grantPermissionForAppAll
import kr.saintdev.pst.models.libs.openPlayStore
import kr.saintdev.pst.vnc.activity.DialogType
import kr.saintdev.pst.vnc.activity.view.*
import kr.saintdev.pst.vnc.dialog.message.ModeSettingDialog
import kr.saintdev.pst.vnc.libs.TileManager
import libs.mjn.prettydialog.PrettyDialogCallback

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class MainActivityControl(val activity: MainActivity) {
    fun setAllocInodeView(remain: Int) {
        activity.vf_main_remain_count.text = appendComma(remain)
    }

    /**
     * 도움말 화면을 여는 함수
     */
    fun openHelpWebview() {
        val intent = Intent(activity, WebBrowserActivity::class.java)
        intent.putExtra("url", WORDPRESS_HELP)
        activity.startActivity(intent)
    }

    /**
     * 특정 Activity 를 엽니다.
     */
    fun openActivity(clazz: Class<*>) {
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
    }

    fun updateServicePowerView(b: Boolean) {
        val btn = activity.findViewById<LabeledSwitch>(R.id.vf_main_power_switch)
        btn.isOn = b
    }

    fun settingCheckedChange(enableButton: Button, disableButton: Button) {
        enableButton.setBackgroundColor(activity.resources.getColor(R.color.colorGreenWhite))
        disableButton.setBackgroundColor(activity.resources.getColor(R.color.colorWhite))
    }

    /**
     * 평점을 남기기 위해 플레이스토어를 엽니다.
     */
    fun openPlaystoreForReply() {
        activity.openPrettyConfirmDialog(
                R.string.home_play_store_reply,
                R.string.home_play_store_reply_content,
                DialogType.WARNING,
                PrettyDialogCallback {
                    openPlayStore(activity)
                }
        )
    }

    /**
     * 무료 충전소 1을 엽니다.
     */
    fun openFreeCharge1() {
        if(checkPermissionForAppAll(activity)) {
            // 권한이 있습니다
            if(!AppAllOfferwallSDK.getInstance().showAppAllOfferwall(activity)) {
                // 무료 충전소를 열지 못했습니다.
                activity.openPrettyDialog(R.string.home_appall_error, R.string.home_appall_free_charge_failed, DialogType.ERROR)
            }
        } else {
            // 권한을 요청한다.
            activity.openPrettyConfirmDialog(R.string.home_require_permission, R.string.home_require_permission_content, DialogType.WARNING, PrettyDialogCallback {
                grantPermissionForAppAll(activity)
            })
        }
    }

    /**
     * 무료 충전소 2을 엽니다.
     */
    fun openFreeCharge2() {
        activity.openPrettyDialog(R.string.home_charge2_ready, R.string.home_charge2_ready_content, DialogType.WARNING)
    }
}