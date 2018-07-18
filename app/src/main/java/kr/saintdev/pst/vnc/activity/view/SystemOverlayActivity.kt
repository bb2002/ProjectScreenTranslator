package kr.saintdev.pst.vnc.activity.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_overlay.*
import kr.saintdev.pst.R
import kr.saintdev.pst.models.libs.SystemOverlay
import kr.saintdev.pst.vnc.activity.CommonActivity
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-08
 */
class SystemOverlayActivity : CommonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)
        sysoverlay_try.setOnClickListener {
            SystemOverlay.openSystemOverlayGrantActivity(this)
        }
    }

    override fun onResume() {
        super.onResume()

        // 오버레이가 허용되었는지 확인한다.
        if(SystemOverlay.isGrantedSystemOverlay(this)) {
            sysoverlay_try.text = R.string.sysoverlay_button_ok.str()
            sysoverlay_try.backgroundColor = R.color.colorGray.color()
            sysoverlay_try.textColor = R.color.colorWhite
            sysoverlay_content.text = R.string.sysoverlay_content_ok.str()
            sysoverlay_try.setOnClickListener { finish() }
        }
    }
}

fun startSystemOverlayGrantActivity(context: Context) =
        context.startActivity(Intent(context, SystemOverlayActivity::class.java))