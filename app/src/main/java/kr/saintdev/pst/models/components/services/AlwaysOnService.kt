package kr.saintdev.pst.models.components.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kr.saintdev.pst.models.libs.manager.AlwaysOnNotification

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class AlwaysOnService : Service() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notifiManager = AlwaysOnNotification.getInstance(this)
        startForeground(AlwaysOnNotification.NOTIFI_ID, notifiManager.show())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

fun getAlwaysOnIntent(context: Context) = Intent(context, AlwaysOnService::class.java)