package kr.saintdev.pst.vnc.activity.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import com.daimajia.swipe.SwipeLayout
import com.kyad.adlibrary.AppAllOfferwallSDK
import kotlinx.android.synthetic.main.vf_activity_main.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.components.services.AlwaysOnService
import kr.saintdev.pst.models.http.HttpResponseObject
import kr.saintdev.pst.models.http.PREPARED_REQUEST_DEFINE_INODE_UPDATER
import kr.saintdev.pst.models.http.modules.updater.Updater
import kr.saintdev.pst.models.http.requestInodeUpdate
import kr.saintdev.pst.models.http.saveInodeUpdate
import kr.saintdev.pst.models.libs.AlwaysOnNotifi
import kr.saintdev.pst.models.libs.ScreenTranslate
import kr.saintdev.pst.models.libs.async.BackgroundWork
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener
import kr.saintdev.pst.models.libs.manager.AuthManager
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.activity.DialogType
import kr.saintdev.pst.vnc.activity.control.MainActivityControl


class MainActivity : CommonActivity(), NavigationView.OnNavigationItemSelectedListener, AppAllOfferwallSDK.AppAllOfferwallSDKListener {

    /**
     * Listener
     */

    private lateinit var control: MainActivityControl

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
        vf_nav_container.setNavigationItemSelectedListener(this)

        // click to close.
        findViewById<SwipeLayout>(R.id.vf_main_mode_setting).isClickToClose = true
        findViewById<SwipeLayout>(R.id.vf_main_run_event_setting).isClickToClose = true

        // Add listener
        vf_main_power_switch.setOnToggledListener(control.onServicePowerClickListener)
        vf_mode_selector_default.setOnClickListener(control.onTranslateModeClickListener)
        vf_mode_selector_gaming.setOnClickListener(control.onTranslateModeClickListener)
        vf_run_event_selector_shake.setOnClickListener(control.onRunEventClickListener)
        vf_run_event_selector_overlay.setOnClickListener(control.onRunEventClickListener)
        vf_main_listitem_1.setOnClickListener(control.onListItemClickListener)
        vf_main_listitem_2.setOnClickListener(control.onListItemClickListener)
        vf_main_listitem_3.setOnClickListener(control.onListItemClickListener)

        // 업데이트를 확인합니다.
        Updater.checkUpdate({ updater: Updater? ->
            if (updater != null)
                if (updater.isNeedUpdate) Updater.openUpdateDialog(this@MainActivity, updater.versionCode)
        }, this)

        // AppAll 을 정의 한다.
        val psctToken = control.repositoryManager.getHashValue(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN)
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
            val remain = control.repositoryManager.getHashValue(RepositoryKey.INODE_REMAIN_PACKET)
            if (remain != null) control.setAllocInodeView(remain.toInt())

            // Inode 상태 값 업데이트
            requestInodeUpdate(OnInodeUpdateCallback(), this)

            // Always On Notification 을 시작한다.
            AlwaysOnNotifi.startAlwaysOnNotificationService(this)

            // Always on notifiction sync service
            val service = Intent(this, AlwaysOnService::class.java)
            startService(service)

            // 스크린 번역기 상태를 버튼으로 표시한다.
            control.updateServicePowerView(ScreenTranslate.isProcedureServiceRunning(this))
            control.displayNowSettings()        // 현재 상태값 업데이트
            control.displayProfileData()        // 프로필 업데이트
        }
    }

    override fun onStop() {
        super.onStop()

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
}
