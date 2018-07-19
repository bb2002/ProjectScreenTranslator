package kr.saintdev.pst.models.components.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kr.saintdev.pst.models.components.broadcast.ProcedureBroadcastRecv
import kr.saintdev.pst.models.libs.DeviceControl
import kr.saintdev.pst.models.libs.manager.AlwaysOnNotification

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class AlwaysOnService : Service() {
    private lateinit var notifiManager: AlwaysOnNotification

    override fun onCreate() {
        super.onCreate()
        this.notifiManager = AlwaysOnNotification.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground
        startForeground(AlwaysOnNotification.NOTIFI_ID, this.notifiManager.notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}