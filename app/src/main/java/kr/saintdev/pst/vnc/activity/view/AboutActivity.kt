package kr.saintdev.pst.vnc.activity.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.consts.COMMENT_PAGE
import kr.saintdev.pst.models.consts.WORDPRESS_NOTIFY
import kr.saintdev.pst.models.consts.version.Versions
import kr.saintdev.pst.models.libs.manager.RepositoryKey
import kr.saintdev.pst.models.libs.manager.RepositoryManager
import kr.saintdev.pst.vnc.activity.CommonActivity
import kr.saintdev.pst.vnc.adapter.AboutAdapter
import kr.saintdev.pst.vnc.adapter.AboutItem
import org.jetbrains.anko.startActivity

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-11
 */
class AboutActivity : CommonActivity() {
    private val adapter = AboutAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        title = R.string.title_about_activity.str()

        about_content.adapter = adapter
        about_content.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when(position) {
                0 -> openNotificationPage()
                1 -> openCommentPage()
            }
        }
        initItems()
    }

    fun initItems() {
        adapter.addItem(AboutItem(R.string.about_notice_title.str(), R.string.about_notice_content.str(), R.drawable.ic_alarm_black, 0))
        adapter.addItem(AboutItem(R.string.about_comment_title.str(), R.string.about_comment_content.str(), R.drawable.ic_comment_black, 1))
        adapter.addItem(AboutItem(R.string.abont_engine_name.str(), Versions.getVersionName(), R.drawable.ic_elec_black, 2))
        adapter.addItem(AboutItem(R.string.about_version_info.str(), Versions.getVersionString(), R.drawable.ic_version_black, 3))
        adapter.addItem(AboutItem(R.string.about_dev_info.str(), R.string.abount_dev_copyright.str(), R.drawable.ic_algori_black, 4))
    }

    fun openCommentPage() {
        val intent = Intent(this, WebBrowserActivity::class.java)

        val token = RepositoryManager.quicklyGet(RepositoryKey.PSCT_AUTH_ACCOUNT_TOKEN, this)
        val url = String.format(COMMENT_PAGE, token)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    fun openNotificationPage() {
        val intent = Intent(this, WebBrowserActivity::class.java)
        intent.putExtra("url", WORDPRESS_NOTIFY)
        startActivity(intent)
    }
}