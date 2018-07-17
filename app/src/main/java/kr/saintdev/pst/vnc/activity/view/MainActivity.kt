package kr.saintdev.pst.vnc.activity.view

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.daimajia.swipe.SwipeLayout
import com.github.angads25.toggle.LabeledSwitch
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.kyad.adlibrary.AppAllOfferwallSDK
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.vf_activity_main.*
import kotlinx.android.synthetic.main.vf_layout_navigation.*
import kotlinx.android.synthetic.main.vf_layout_navigation.view.*
import kr.saintdev.pst.R
import kr.saintdev.pst.R.id.*
import kr.saintdev.pst.models.components.broadcast.ProcedureEngine
import kr.saintdev.pst.models.http.HttpResponseObject
import kr.saintdev.pst.models.http.PREPARED_REQUEST_DEFINE_INODE_UPDATER
import kr.saintdev.pst.models.http.modules.downloader.ImageDownloader
import kr.saintdev.pst.models.http.modules.updater.Updater
import kr.saintdev.pst.models.http.requestInodeUpdate
import kr.saintdev.pst.models.http.saveInodeUpdate
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.isProcedureServiceWorking
import kr.saintdev.pst.models.libs.manager.*
import kr.saintdev.pst.models.libs.startAlwaysOnNotificationService
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.activity.DialogType
import kr.saintdev.pst.vnc.activity.control.MainActivityControl
import org.jetbrains.anko.imageBitmap
import android.widget.Toast



class MainActivity : CommonActivity(), NavigationView.OnNavigationItemSelectedListener, AppAllOfferwallSDK.AppAllOfferwallSDKListener {

    /**
     * Listener
     */
    private val onServicePowerClickListener = OnToggledListener {
        _, isOn ->

        if(isOn) {
            // 서비스 실행
            ProcedureEngine.screenTransServiceOn(this)
            false
        } else {
            // 서비스 중지
            ProcedureEngine.screenTransServiceOff(this)
        }
    }
    private val onTranslateModeClickListener = View.OnClickListener {
        view ->
        val data = when(view.id) {
            R.id.vf_mode_selector_default -> {
                control.settingCheckedChange(vf_mode_selector_default, vf_mode_selector_gaming)
                arrayOf(RepositoryKey.MODE_SETTING, "default")
            }
            R.id.vf_mode_selector_gaming -> {
                control.settingCheckedChange(vf_mode_selector_gaming, vf_mode_selector_default)
                arrayOf(RepositoryKey.MODE_SETTING, "gaming")
            }
            else -> null
        }

        openPrettyDialog(R.string.info_msg_title, R.string.home_saveok_content, DialogType.SUCCESS)

        if(data != null) {
            repositoryManager.createHashValue(data[0] as RepositoryKey, data[1] as String)
            displayNowSettings()
        }
    }
    private val onRunEventClickListener = View.OnClickListener {
        view ->
        val data = when(view.id) {
            R.id.vf_run_event_selector_shake -> {
                control.settingCheckedChange(vf_run_event_selector_shake, vf_run_event_selector_overlay)
                arrayOf("settings_runtime_event", "device_shake")
            }
            R.id.vf_run_event_selector_overlay -> {
                control.settingCheckedChange(vf_run_event_selector_overlay, vf_run_event_selector_shake)
                arrayOf("settings_runtime_event", "device_button_overlay")
            }
            else -> null
        }

        openPrettyDialog(R.string.info_msg_title, R.string.home_plz_restart_content, DialogType.WARNING)

        if(data != null) {
            envManager.forceWrite(data[0], data[1])
            displayNowSettings()
        }
    }
    private val onListItemClickListener = View.OnClickListener {
        view ->
        when(view.id) {
            R.id.vf_main_listitem_1 -> control.openPlaystoreForReply()      // 플레이스토어를 연다.
            R.id.vf_main_listitem_2 -> control.openHelpWebview()            // 도움말 화면을 연다.
            R.id.vf_main_listitem_3 -> control.openActivity(AboutActivity::class.java)
        }
    }

    private lateinit var control: MainActivityControl
    private lateinit var repositoryManager: RepositoryManager
    private lateinit var envManager: EnvSettingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vf_activity_main)
        setSupportActionBar(vf_main_content_toolbar)

        // 햄버거 바를 만든다.
        val drawerToggle =
                ActionBarDrawerToggle(this, vf_main_layout, vf_main_content_toolbar, R.string.common_positive_ok, R.string.common_negative_no)
        drawerToggle.isDrawerIndicatorEnabled = false
        drawerToggle.toolbarNavigationClickListener = View.OnClickListener {
            vf_main_layout.openDrawer(GravityCompat.START)
        }
        drawerToggle.setHomeAsUpIndicator(R.drawable.vf_ic_menu_white)

        // init
        this.control = MainActivityControl(this)
        this.repositoryManager = RepositoryManager.getInstance(this)
        this.envManager = EnvSettingManager.getInstance(this)
        vf_nav_container.setNavigationItemSelectedListener(this)

        // click to close.
        findViewById<SwipeLayout>(R.id.vf_main_mode_setting).isClickToClose = true
        findViewById<SwipeLayout>(R.id.vf_main_run_event_setting).isClickToClose = true


        // Add listener
        vf_main_power_switch.setOnToggledListener(onServicePowerClickListener)
        vf_mode_selector_default.setOnClickListener(onTranslateModeClickListener)
        vf_mode_selector_gaming.setOnClickListener(onTranslateModeClickListener)
        vf_run_event_selector_shake.setOnClickListener(onRunEventClickListener)
        vf_run_event_selector_overlay.setOnClickListener(onRunEventClickListener)
        vf_main_listitem_1.setOnClickListener(onListItemClickListener)
        vf_main_listitem_2.setOnClickListener(onListItemClickListener)
        vf_main_listitem_3.setOnClickListener(onListItemClickListener)

        // 업데이트를 확인합니다.
        Updater.checkUpdate({ updater: Updater? ->
            if (updater != null)
                if (updater.isNeedUpdate) Updater.openUpdateDialog(this@MainActivity, updater.versionCode)
        }, this)

        val psctToken = repositoryManager.getHashValue(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN)
        AppAllOfferwallSDK.getInstance().initOfferWall(this, R.string.appall_key.str(), psctToken)
    }

    override fun onResume() {
        super.onResume()

        // 로그인 되었는지 확인한다.
        if(!AuthManager.Manager.isLoginned(this)) {
            // 로그인 되지 않은 사용자
            AuthManager.Manager.openSigninActivity(this)
            finish()
            return
        } else {
            // 로그인 된 사용자
            val remain = repositoryManager.getHashValue(RepositoryKey.INODE_REMAIN_PACKET)
            if (remain != null) control.setAllocInodeView(remain.toInt())

            // Inode 상태 값 업데이트
            requestInodeUpdate(OnInodeUpdateCallback(), this)

            // Always On Notification 을 시작한다.
            startAlwaysOnNotificationService(this)

            // 스크린 번역기 상태를 버튼으로 표시한다.
            control.updateServicePowerView(isProcedureServiceWorking(this))
            displayNowSettings()        // 현재 상태값 업데이트
            displayProfileData()        // 프로필 업데이트
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val clazz = when(item.itemId) {
            R.id.navigation_item_new_pay -> BillActivity::class.java
            R.id.navigation_item_pay_log -> BillingLogActivity::class.java
            R.id.navigation_item_charge_1 -> {
                control.openFreeCharge1()
                null
            }
            R.id.navigation_item_charge_2 -> {
                control.openFreeCharge2()
                null
            }
            R.id.navigation_item_charge_log -> FreeChargeLogActivity::class.java
            R.id.navigation_item_settings -> SettingActivity::class.java
            else -> null
        }

        vf_main_layout.closeDrawer(GravityCompat.START)

        return if(clazz == null) {
            false
        } else {
            control.openActivity(clazz)
            true
        }
    }

    override fun AppAllOfferwallSDKCallback(response: Int) {
        when (response) {
            AppAllOfferwallSDK.AppAllOfferwallSDK_INVALID_USER_ID -> openPrettyDialog(R.string.home_appall_error, R.string.home_appall_error_1_content, DialogType.ERROR)
            AppAllOfferwallSDK.AppAllOfferwallSDK_INVALID_KEY -> openPrettyDialog(R.string.home_appall_error, R.string.home_appall_error_2_content, DialogType.ERROR)
            AppAllOfferwallSDK.AppAllOfferwallSDK_NOT_GET_ADID -> openPrettyDialog(R.string.home_appall_error, R.string.home_appall_error_3_content, DialogType.WARNING)
        }
    }

    /**
     * Callback class
     * inode 값을 업데이트 Callback 처리
     */
    inner class OnInodeUpdateCallback : OnBackgroundWorkListener {
        override fun onSuccess(requestCode: Int, worker: BackgroundWork<*>) {
            saveInodeUpdate(worker, this@MainActivity)      // inode 값을 저장합니다.

            val response = worker.result
            if(response is HttpResponseObject && response.isSuccess) {
                val data = response.message

                val remainInode = data.getString("inode_remain_nodes").toInt()
                val remainTime = data.getString("reset_remain_time").split(":")

                // 할당량을 표시합니다.
                control.setAllocInodeView(remainInode)
                vf_main_reset_timer.text =
                        String.format(R.string.home_card_remain_reset_time.str(), String.format("%02d", remainTime[0].toInt()), String.format("%02d", remainTime[1].toInt()))
            } else {
                onFailed(PREPARED_REQUEST_DEFINE_INODE_UPDATER, Exception("Can not parse response data."))
            }
        }

        override fun onFailed(requestCode: Int, ex: Exception?) {
            openMessageDialog(
                    R.string.error_msg_title_warning.str(),
                    R.string.home_inode_update_failed.str()
            )
        }
    }

    /**
     * Functions
     */
    /**
     * 초기 값을 표시한다.
     */
    private fun displayNowSettings() {
        val modeSetting = repositoryManager.getHashValue(RepositoryKey.MODE_SETTING)

        val button = when(modeSetting) {
            "default" -> arrayOf(vf_mode_selector_default, vf_mode_selector_gaming)
            "gaming" -> arrayOf(vf_mode_selector_gaming, vf_mode_selector_default)
            else -> null
        }

        // 번역 모드 설정 부문 표시
        if(button != null) {
            control.settingCheckedChange(button[0] as Button, button[1] as Button)
        }

        val runEventSetting = envManager.read(EnvSettingKeys.RUN_EVENT)
        val runEventButton = when(runEventSetting) {
            "device_shake" -> arrayOf(vf_run_event_selector_shake, vf_run_event_selector_overlay)
            "device_button_overlay" -> arrayOf(vf_run_event_selector_overlay, vf_run_event_selector_shake)
            else -> null
        }

        if(runEventButton != null) control.settingCheckedChange(runEventButton[0], runEventButton[1])
    }

    /**
     * 프로필 사진과 이메일을 표시한다.
     */
    private fun displayProfileData() {
        val user = AuthManager.Account.firebaseAuth.currentUser

        if(user != null) {
            val headerView = vf_nav_container.getHeaderView(0)

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

                override fun onFailed(requestCode: Int, ex: java.lang.Exception?) {
                    openPrettyDialog(R.string.error_msg_title_warning, R.string.home_profile_download_failed, DialogType.ERROR)
                }
            }).execute()
        }
    }
}
