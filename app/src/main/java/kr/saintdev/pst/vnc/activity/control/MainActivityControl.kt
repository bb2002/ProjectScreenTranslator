package kr.saintdev.pst.vnc.activity.control

import android.content.Intent
import android.widget.Button
import com.github.angads25.toggle.LabeledSwitch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.vf_activity_main.*
import kr.saintdev.pst.R
import kr.saintdev.pst.R.id.vf_main_power_switch
import kr.saintdev.pst.models.consts.WORDPRESS_HELP
import kr.saintdev.pst.models.libs.appendComma
import kr.saintdev.pst.vnc.activity.view.*
import kr.saintdev.pst.vnc.dialog.message.ModeSettingDialog
import kr.saintdev.pst.vnc.libs.TileManager

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
    }
}