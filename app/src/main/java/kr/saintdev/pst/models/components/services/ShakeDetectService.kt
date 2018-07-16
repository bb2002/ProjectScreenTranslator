package kr.saintdev.pst.models.components.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import kr.saintdev.pst.models.libs.isScreenTranslaterRunningValue
import kr.saintdev.pst.models.libs.manager.AlwaysOnNotification
import kr.saintdev.pst.models.libs.manager.EnvSettingKeys
import kr.saintdev.pst.models.libs.manager.EnvSettingManager
import org.jetbrains.anko.sensorManager

/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-03
 */
class ShakeDetectService : Service() {
    private var sensorManager: SensorManager? = null
    private var accelSensor: Sensor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if(this.sensorManager != null) {
            this.accelSensor = this.sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelSensor == null) {
                Toast.makeText(this@ShakeDetectService, "가속도 센서가 없습니다!\n오버레이 버튼을 사용하세요.", Toast.LENGTH_LONG).show()
            } else {
                sensorManager!!.registerListener(onSensorChangeListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST)
                startForeground(AlwaysOnNotification.NOTIFI_ID, AlwaysOnNotification.getInstance(this@ShakeDetectService).notification)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.sensorManager!!.unregisterListener(onSensorChangeListener)
    }

    private val SHAKE_SKIP_TIME = 5000     // 두 흔들림의 딜레이
    private val SHAKE_THRESHOLD_GRAVITY = 2.9f
    private var shakeTime: Long = 0
    private val onSensorChangeListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[0]
                val z = event.values[0]

                val gravityX = x / SensorManager.GRAVITY_EARTH
                val gravityY = y / SensorManager.GRAVITY_EARTH
                val gravityZ = z / SensorManager.GRAVITY_EARTH

                val f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ
                val squaredD = Math.sqrt(f.toDouble())
                val force = squaredD.toFloat()

                if (force > SHAKE_THRESHOLD_GRAVITY) {
                    // 최소한의 흔들림은 받아야 합니다.
                    val currectTime = System.currentTimeMillis()

                    if (shakeTime + SHAKE_SKIP_TIME < currectTime) { // 휴대폰이 흔들렸습니다.
                        // 05 26 업데이트, 흔들기 강도가 일치하는지 확인합니다.
                        val powerSize =
                                try {
                                    EnvSettingManager.getQuickly(this@ShakeDetectService, EnvSettingKeys.SHAKE_SENS).toInt() + 1
                                } catch(ex: Exception) { 2 }

                        if (SHAKE_THRESHOLD_GRAVITY + powerSize < force) {
                            // 장치가 흔들렸습니다.
                            // 휴대폰 화면이 켜져있는지 확인합니다.
                            if (!isScreenTranslaterRunningValue(this@ShakeDetectService)) return

                            shakeTime = currectTime

                            // 화면 캡쳐 서비스를 호출한다.
                            val captureService = Intent(applicationContext, DisplayCaptureService::class.java)
                            captureService.putExtra("command", "capture")
                            startService(captureService)
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?) = null
}

fun getShakeDetectIntent(context: Context) = Intent(context, ShakeDetectService::class.java)