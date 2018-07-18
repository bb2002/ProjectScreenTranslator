package kr.saintdev.pst.vnc.activity.control

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.github.angads25.toggle.LabeledSwitch
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.kyad.adlibrary.AppAllOfferwallSDK
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.vf_activity_main.*
import kr.saintdev.pst.R
import kr.saintdev.pst.R.id.*
import kr.saintdev.pst.models.consts.WORDPRESS_HELP
import kr.saintdev.pst.models.http.modules.downloader.ImageDownloader
import kr.saintdev.pst.models.libs.*
import kr.saintdev.pst.models.libs.ApplicationPermission.checkPermissionForAppAll
import kr.saintdev.pst.models.libs.OpenPreparedActivity.openPlayStore
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.*
import kr.saintdev.pst.vnc.activity.DialogType
import kr.saintdev.pst.vnc.activity.view.*
import libs.mjn.prettydialog.PrettyDialogCallback
import org.jetbrains.anko.imageBitmap
import java.lang.Exception

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class MainActivityControl(val activity: MainActivity) {
    val repositoryManager = RepositoryManager.getInstance(activity)
    val envManager = EnvSettingManager.getInstance(activity)

    /**
     * 서비스 전원에 대한 클릭 리스너
     */
    val onServicePowerClickListener = OnToggledListener {
        _, isOn ->
        if(isOn) ScreenTranslate.screenTranslatePower(true, activity)
        else ScreenTranslate.screenTranslatePower(false, activity)
    }

    /**
     * 번역 모드 변경에 대한 클릭 리스너
     */
    val onTranslateModeClickListener = View.OnClickListener {
        view ->
        val data = when(view.id) {
            R.id.vf_mode_selector_default -> {
                settingCheckedChange(activity.vf_mode_selector_default, activity.vf_mode_selector_gaming)
                arrayOf(RepositoryKey.MODE_SETTING, "default")
            }
            R.id.vf_mode_selector_gaming -> {
                if(SystemOverlay.isGrantedSystemOverlay(activity)) {
                    // 권한이 있다면 저장
                    settingCheckedChange(activity.vf_mode_selector_gaming, activity.vf_mode_selector_default)
                    arrayOf(RepositoryKey.MODE_SETTING, "gaming")
                } else {
                    // 권한이 없다면 요청
                    activity.openPrettyConfirmDialog(R.string.error_msg_title_warning, R.string.sysoverlay_title, DialogType.ERROR, PrettyDialogCallback {
                        SystemOverlay.openSystemOverlayGrantActivity(activity)
                    })
                    null
                }
            }
            else -> null
        }

        if(data != null) {
            repositoryManager.createHashValue(data[0] as RepositoryKey, data[1] as String)
            displayNowSettings()
            activity.openPrettyDialog(R.string.info_msg_title, R.string.home_saveok_content, DialogType.SUCCESS)
        }
    }

    /**
     * Run Event 변경에 대한 클릭 리스너
     */
    val onRunEventClickListener = View.OnClickListener {
        view ->
        val data = when(view.id) {
            R.id.vf_run_event_selector_shake -> {
                settingCheckedChange(activity.vf_run_event_selector_shake, activity.vf_run_event_selector_overlay)
                arrayOf("settings_runtime_event", "device_shake")
            }
            R.id.vf_run_event_selector_overlay -> {
                // Overlay 권한을 확인한다.
                // 07-18 2018.
                if(SystemOverlay.isGrantedSystemOverlay(activity)) {
                    // 권한이 있다면 저장
                    settingCheckedChange(activity.vf_run_event_selector_overlay, activity.vf_run_event_selector_shake)
                    arrayOf("settings_runtime_event", "device_button_overlay")
                } else {
                    // 권한이 없다면 요청
                    activity.openPrettyConfirmDialog(R.string.error_msg_title_warning, R.string.sysoverlay_title, DialogType.ERROR, PrettyDialogCallback {
                        SystemOverlay.openSystemOverlayGrantActivity(activity)
                    })
                    null
                }
            }
            else -> null
        }

        if(data != null) {
            envManager.forceWrite(data[0], data[1])
            displayNowSettings()
            activity.openPrettyDialog(R.string.info_msg_title, R.string.home_plz_restart_content, DialogType.WARNING)
        }
    }

    /**
     * ListItem 을 클릭함에 대한 리스너
     */
    val onListItemClickListener = View.OnClickListener {
        view ->
        when(view.id) {
            R.id.vf_main_listitem_1 -> openPlaystoreForReply()      // 플레이스토어를 연다.
            R.id.vf_main_listitem_2 -> openHelpWebview()            // 도움말 화면을 연다.
            R.id.vf_main_listitem_3 -> openActivity(AboutActivity::class.java)
        }
    }


    fun setAllocInodeView(remain: Int) {
        activity.vf_main_remain_count.text = StdString.appendCommaToInt(remain)
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
                    OpenPreparedActivity.openPlayStore(activity)
                }
        )
    }

    /**
     * 무료 충전소 1을 엽니다.
     */
    fun openFreeCharge1() {
        if(ApplicationPermission.checkPermissionForAppAll(activity)) {
            // 권한이 있습니다
            if(!AppAllOfferwallSDK.getInstance().showAppAllOfferwall(activity)) {
                // 무료 충전소를 열지 못했습니다.
                activity.openPrettyDialog(R.string.home_appall_error, R.string.home_appall_free_charge_failed, DialogType.ERROR)
            }
        } else {
            // 권한을 요청한다.
            activity.openPrettyConfirmDialog(R.string.home_require_permission, R.string.home_require_permission_content, DialogType.WARNING, PrettyDialogCallback {
                ApplicationPermission.openPermissionGrantDialog(activity)
            })
        }
    }

    /**
     * 무료 충전소 2을 엽니다.
     */
    fun openFreeCharge2() {
        activity.openPrettyDialog(R.string.home_charge2_ready, R.string.home_charge2_ready_content, DialogType.WARNING)
    }


    /**
     * Functions
     */
    /**
     * 초기 값을 표시한다.
     */
    fun displayNowSettings() {
        val modeSetting = repositoryManager.getHashValue(RepositoryKey.MODE_SETTING)

        val button = when(modeSetting) {
            "default" -> arrayOf(activity.vf_mode_selector_default, activity.vf_mode_selector_gaming)
            "gaming" -> arrayOf(activity.vf_mode_selector_gaming, activity.vf_mode_selector_default)
            else -> null
        }

        // 번역 모드 설정 부문 표시
        if(button != null) {
            settingCheckedChange(button[0] as Button, button[1] as Button)
        }

        val runEventSetting = envManager.read(EnvSettingKeys.RUN_EVENT)
        val runEventButton = when(runEventSetting) {
            "device_shake" -> arrayOf(activity.vf_run_event_selector_shake, activity.vf_run_event_selector_overlay)
            "device_button_overlay" -> arrayOf(activity.vf_run_event_selector_overlay, activity.vf_run_event_selector_shake)
            else -> null
        }

        if(runEventButton != null) settingCheckedChange(runEventButton[0], runEventButton[1])
    }

    /**
     * 프로필 사진과 이메일을 표시한다.
     */
    fun displayProfileData() {
        val user = AuthManager.Account.firebaseAuth.currentUser

        if(user != null) {
            val headerView = activity.vf_nav_container.getHeaderView(0)

            val profileView = headerView.findViewById<CircleImageView>(R.id.vf_navigation_profile_icon)
            val emailView = headerView.findViewById<TextView>(R.id.vf_navigation_email)

            emailView.text = user.email
            ImageDownloader(user.photoUrl.toString(), 0x0, object : OnBackgroundWorkListener {
                override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>?) {
                    val image = worker?.result as? Bitmap

                    if(image != null) {
                        profileView.imageBitmap = image
                    } else {
                        onFailed(0x0, null)
                    }
                }

                override fun onFailed(requestCode: Int, ex: Exception?) {
                    activity.openPrettyDialog(R.string.error_msg_title_warning, R.string.home_profile_download_failed, DialogType.ERROR)
                }
            }).execute()
        }
    }
}